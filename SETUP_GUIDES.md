# Setup Guides for External Services

## JupyterHub with PySpark Integration

### Option 1: Kubernetes Deployment (Recommended for Production)

1. **Install JupyterHub using Helm**

```bash
# Add JupyterHub Helm repository
helm repo add jupyterhub https://jupyterhub.github.io/helm-chart/
helm repo update

# Install JupyterHub
helm install kadali-jupyter jupyterhub/jupyterhub \
  --version=3.2.1 \
  --namespace jupyter \
  --create-namespace \
  --values jupyterhub-values.yaml
```

2. **Create `jupyterhub-values.yaml`**

```yaml
hub:
  config:
    JupyterHub:
      authenticator_class: dummy  # For development; use OAuth in production
    DummyAuthenticator:
      password: kadali123
  
  service:
    type: LoadBalancer

singleuser:
  image:
    name: jupyter/pyspark-notebook
    tag: latest
  
  storage:
    type: dynamic
    capacity: 10Gi
  
  cpu:
    limit: 2
    guarantee: 1
  memory:
    limit: 4G
    guarantee: 2G
  
  # Environment variables for Spark
  extraEnv:
    SPARK_MASTER: "k8s://https://kubernetes.default.svc:443"
    SPARK_EXECUTOR_MEMORY: "2g"
    SPARK_EXECUTOR_CORES: "1"

prePuller:
  hook:
    enabled: false
```

3. **Create Custom PySpark Kernel**

Create `Dockerfile-pyspark-notebook`:

```dockerfile
FROM jupyter/pyspark-notebook:latest

USER root

# Install Delta Lake
RUN pip install delta-spark==3.0.0

# Configure Spark with Delta Lake
RUN mkdir -p /usr/local/spark/conf
COPY spark-defaults.conf /usr/local/spark/conf/

USER $NB_UID
```

Create `spark-defaults.conf`:

```
spark.sql.extensions=io.delta.sql.DeltaSparkSessionExtension
spark.sql.catalog.spark_catalog=org.apache.spark.sql.delta.catalog.DeltaCatalog
spark.hadoop.fs.s3a.endpoint=http://kadali-minio:9000
spark.hadoop.fs.s3a.access.key=minioadmin
spark.hadoop.fs.s3a.secret.key=minioadmin
spark.hadoop.fs.s3a.path.style.access=true
```

4. **Build and Deploy**

```bash
docker build -t kadali-pyspark-notebook:latest -f Dockerfile-pyspark-notebook .
docker push your-registry/kadali-pyspark-notebook:latest

# Update jupyterhub-values.yaml to use your custom image
helm upgrade kadali-jupyter jupyterhub/jupyterhub \
  --values jupyterhub-values.yaml \
  --namespace jupyter
```

### Option 2: Docker Compose (For Local Development)

Add to `docker-compose.yml`:

```yaml
  jupyterhub:
    image: jupyterhub/jupyterhub:latest
    container_name: kadali-jupyter
    ports:
      - "8000:8000"
    volumes:
      - ./jupyter_config.py:/srv/jupyterhub/jupyterhub_config.py
      - jupyter_data:/srv/jupyterhub
    command: jupyterhub --config /srv/jupyterhub/jupyterhub_config.py
```

Create `jupyter_config.py`:

```python
c.JupyterHub.authenticator_class = 'jupyterhub.auth.DummyAuthenticator'
c.DummyAuthenticator.password = "kadali123"

c.Spawner.default_url = '/lab'
c.Spawner.cpu_limit = 2
c.Spawner.mem_limit = '4G'

# PySpark environment
c.Spawner.environment = {
    'PYSPARK_SUBMIT_ARGS': '--master local[*] --conf spark.sql.extensions=io.delta.sql.DeltaSparkSessionExtension pyspark-shell'
}
```

---

## Apache Airflow for Workflow Orchestration

### Option 1: Kubernetes Deployment (Recommended)

1. **Install Airflow using Helm**

```bash
# Add Airflow Helm repository
helm repo add apache-airflow https://airflow.apache.org
helm repo update

# Install Airflow
helm install kadali-airflow apache-airflow/airflow \
  --namespace airflow \
  --create-namespace \
  --values airflow-values.yaml
```

2. **Create `airflow-values.yaml`**

```yaml
executor: "KubernetesExecutor"

defaultAirflowRepository: apache/airflow
defaultAirflowTag: "2.8.0"

webserver:
  service:
    type: LoadBalancer

postgresql:
  enabled: true
  auth:
    username: airflow
    password: airflow
    database: airflow

redis:
  enabled: true

config:
  core:
    dags_folder: /opt/airflow/dags
    load_examples: false
  webserver:
    expose_config: true
  
  # Connect to Kadali API
  api:
    auth_backends: airflow.api.auth.backend.basic_auth

extraEnv: |
  - name: KADALI_API_URL
    value: "http://kadali-api:8080"
  - name: KADALI_TENANT_ID
    value: "default"
```

3. **Create Sample DAG**

Create `dags/kadali_etl_example.py`:

```python
from airflow import DAG
from airflow.operators.python import PythonOperator
from airflow.utils.dates import days_ago
import requests

default_args = {
    'owner': 'kadali',
    'start_date': days_ago(1),
}

def create_spark_cluster():
    response = requests.post(
        'http://kadali-api:8080/api/v1/clusters',
        headers={'X-Tenant-ID': 'default'},
        json={
            'name': 'etl-cluster',
            'type': 'JOB',
            'driverMemory': '2g',
            'driverCores': 1,
            'executorMemory': '2g',
            'executorCores': 1,
            'executorCount': 2
        }
    )
    return response.json()['clusterId']

def run_etl_query():
    response = requests.post(
        'http://kadali-api:8080/api/v1/data/query',
        headers={'X-Tenant-ID': 'default'},
        json={
            'sql': 'SELECT * FROM analytics.users',
            'limit': 1000
        }
    )
    return response.json()

with DAG(
    'kadali_daily_etl',
    default_args=default_args,
    schedule_interval='@daily',
    catchup=False
) as dag:
    
    create_cluster = PythonOperator(
        task_id='create_cluster',
        python_callable=create_spark_cluster
    )
    
    run_query = PythonOperator(
        task_id='run_query',
        python_callable=run_etl_query
    )
    
    create_cluster >> run_query
```

### Option 2: Docker Compose (Local Development)

Add to `docker-compose.yml`:

```yaml
  airflow:
    image: apache/airflow:2.8.0
    container_name: kadali-airflow
    environment:
      AIRFLOW__CORE__EXECUTOR: LocalExecutor
      AIRFLOW__DATABASE__SQL_ALCHEMY_CONN: postgresql+psycopg2://airflow:airflow@postgres:5432/airflow
      AIRFLOW__CORE__LOAD_EXAMPLES: "false"
      AIRFLOW__API__AUTH_BACKENDS: airflow.api.auth.backend.basic_auth
      KADALI_API_URL: http://kadali-api:8080
    ports:
      - "8081:8080"
    volumes:
      - ./dags:/opt/airflow/dags
      - ./logs:/opt/airflow/logs
      - ./plugins:/opt/airflow/plugins
    command: >
      bash -c "
      airflow db init &&
      airflow users create --username admin --password admin --firstname Admin --lastname User --role Admin --email admin@kadali.io &&
      airflow webserver & airflow scheduler
      "
    depends_on:
      - postgres
```

---

## Integration with Kadali Platform

### JupyterHub Integration

The Kadali API already supports notebook execution. To connect JupyterHub:

1. **Configure notebook to use Kadali clusters**

Add this to your notebook startup:

```python
import os
import requests

KADALI_API = os.getenv('KADALI_API_URL', 'http://localhost:8080')
TENANT_ID = os.getenv('TENANT_ID', 'my-startup')

# Create or get existing cluster
response = requests.post(
    f'{KADALI_API}/api/v1/clusters',
    headers={'X-Tenant-ID': TENANT_ID},
    json={
        'name': 'notebook-cluster',
        'type': 'INTERACTIVE',
        'driverMemory': '2g',
        'driverCores': 1,
        'executorMemory': '2g',
        'executorCores': 1,
        'executorCount': 2
    }
)

cluster_id = response.json()['clusterId']
print(f"Using cluster: {cluster_id}")
```

2. **Execute queries through Kadali**

```python
# Execute SQL via Kadali API
response = requests.post(
    f'{KADALI_API}/api/v1/data/query',
    headers={'X-Tenant-ID': TENANT_ID},
    json={
        'sql': 'SELECT * FROM analytics.users LIMIT 10',
        'limit': 100
    }
)

data = response.json()['data']
import pandas as pd
df = pd.DataFrame(data)
df.head()
```

### Airflow Integration

Airflow DAGs can use Kadali APIs to:
- Create/terminate Spark clusters
- Execute SQL queries
- Upload data
- Trigger notebook executions

See the example DAG above for reference.

---

## Testing the Setup

### Test JupyterHub

```bash
# Access JupyterHub
open http://localhost:8000

# Login with dummy credentials
Username: any
Password: kadali123

# Create a new notebook and run:
import requests
response = requests.get('http://kadali-api:8080/actuator/health')
print(response.json())
```

### Test Airflow

```bash
# Access Airflow UI
open http://localhost:8081

# Login
Username: admin
Password: admin

# Trigger the example DAG
airflow dags trigger kadali_daily_etl
```

---

## Production Recommendations

1. **Security**
   - Enable OAuth for JupyterHub (GitHub, Google, etc.)
   - Use RBAC in Airflow
   - Secure all endpoints with HTTPS
   - Use Kubernetes secrets for credentials

2. **Scalability**
   - Use KubernetesExecutor for Airflow
   - Auto-scale JupyterHub user pods
   - Implement resource quotas

3. **Monitoring**
   - Integrate Prometheus for metrics
   - Set up Grafana dashboards
   - Configure alerts for failures

4. **Backup**
   - Backup PostgreSQL databases
   - Version control DAGs in Git
   - Snapshot notebook volumes

---

## Troubleshooting

### JupyterHub pods not starting
- Check resource limits
- Verify image pull permissions
- Check PVC availability

### Airflow tasks failing
- Verify Kadali API connectivity
- Check authentication headers
- Review task logs in Airflow UI

### Spark connection issues
- Ensure Kubernetes RBAC permissions
- Verify network policies
- Check Spark driver logs

