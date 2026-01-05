# Getting Started with Kadali Data Platform

## 5-Minute Quick Start

### Step 1: Start the Infrastructure (2 minutes)

```bash
cd /Users/aswinkumar/Downloads/Aswin/Startups/backend-project/kadali

# Start PostgreSQL, MinIO, and Metastore
docker-compose up -d

# Check everything is running
docker-compose ps
```

You should see:
- ✅ `kadali-postgres` running on port 5432
- ✅ `kadali-minio` running on ports 9000 and 9001
- ✅ `kadali-metastore` running on port 9083

### Step 2: Start Kadali API (1 minute)

```bash
# Build the project (first time only)
mvn clean install

# Run the application
mvn spring-boot:run
```

Wait for: `Started KadaliDataPlatformApplication`

### Step 3: Test the Platform (2 minutes)

**Create a Spark Cluster:**
```bash
curl -X POST http://localhost:8080/api/v1/clusters \
  -H "X-Tenant-ID: test-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "my-first-cluster",
    "type": "INTERACTIVE",
    "driverMemory": "1g",
    "driverCores": 1,
    "executorMemory": "1g",
    "executorCores": 1,
    "executorCount": 1
  }'
```

**Create a Database:**
```bash
curl -X POST "http://localhost:8080/api/v1/data/databases?databaseName=analytics" \
  -H "X-Tenant-ID: test-startup"
```

**Run Your First Query:**
```bash
curl -X POST http://localhost:8080/api/v1/data/query \
  -H "X-Tenant-ID: test-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "CREATE TABLE analytics.test_table (id INT, name STRING) USING DELTA",
    "limit": 1000
  }'
```

**Insert Data:**
```bash
curl -X POST http://localhost:8080/api/v1/data/query \
  -H "X-Tenant-ID: test-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "INSERT INTO analytics.test_table VALUES (1, '\''Alice'\''), (2, '\''Bob'\'')",
    "limit": 1000
  }'
```

**Query Data:**
```bash
curl -X POST http://localhost:8080/api/v1/data/query \
  -H "X-Tenant-ID: test-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "SELECT * FROM analytics.test_table",
    "limit": 1000
  }'
```

## 🎉 Success!

If you see data returned, **congratulations!** You've successfully:
- ✅ Created a Spark cluster
- ✅ Created a database
- ✅ Created a Delta Lake table
- ✅ Inserted data
- ✅ Queried data

---

## Common Commands

### Check System Health
```bash
curl http://localhost:8080/actuator/health
```

### List All Clusters
```bash
curl http://localhost:8080/api/v1/clusters \
  -H "X-Tenant-ID: test-startup"
```

### Upload CSV File
```bash
curl -X POST http://localhost:8080/api/v1/connectors/upload \
  -H "X-Tenant-ID: test-startup" \
  -F "file=@your-data.csv" \
  -F "database=analytics" \
  -F "tableName=uploaded_data" \
  -F "format=csv"
```

### Create a Notebook
```bash
curl -X POST http://localhost:8080/api/v1/notebooks \
  -H "X-Tenant-ID: test-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My First Notebook",
    "language": "python"
  }'
```

### Execute SQL in Notebook
```bash
# Replace {notebookId} with actual ID from create response
curl -X POST http://localhost:8080/api/v1/notebooks/{notebookId}/execute/sql \
  -H "Content-Type: application/json" \
  -d '{
    "code": "SELECT * FROM analytics.test_table"
  }'
```

---

## Explore the UI

### MinIO Console (Object Storage)
- URL: http://localhost:9001
- Username: minioadmin
- Password: minioadmin

Browse your lakehouse data files!

### PostgreSQL (Metadata Database)
```bash
docker exec -it kadali-postgres psql -U postgres -d kadali_platform
```

Then run:
```sql
-- See your clusters
SELECT * FROM spark_clusters;

-- See your datasets
SELECT * FROM datasets;

-- See your notebooks
SELECT * FROM notebooks;
```

---

## Troubleshooting

### Port Already in Use
If port 8080 is busy, edit `src/main/resources/application.yml`:
```yaml
server:
  port: 8081
```

### Docker Services Not Starting
```bash
# Stop all
docker-compose down

# Remove volumes and restart
docker-compose down -v
docker-compose up -d
```

### Can't Connect to MinIO
Check that MinIO is running:
```bash
curl http://localhost:9000/minio/health/live
```

---

## Next Steps

1. **Read the full docs:** [README.md](README.md)
2. **Explore all APIs:** [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)
3. **Set up JupyterHub:** [SETUP_GUIDES.md](SETUP_GUIDES.md)
4. **Deploy Airflow:** [SETUP_GUIDES.md](SETUP_GUIDES.md)
5. **Build a frontend:** Connect your React/Vue app to the APIs!

---

## Example Use Cases

### 1. ETL Pipeline
```bash
# 1. Connect to source database
curl -X POST http://localhost:8080/api/v1/connectors/sync-database \
  -H "X-Tenant-ID: test-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceType": "postgres",
    "jdbcUrl": "jdbc:postgresql://external-db:5432/source_db",
    "username": "user",
    "password": "pass",
    "sourceTable": "users",
    "targetDatabase": "analytics",
    "targetTable": "users"
  }'

# 2. Transform data
curl -X POST http://localhost:8080/api/v1/data/query \
  -H "X-Tenant-ID: test-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "CREATE TABLE analytics.active_users AS SELECT * FROM analytics.users WHERE last_login > current_date() - 30",
    "limit": 1000
  }'

# 3. Export results
# (Add export API call here)
```

### 2. Ad-Hoc Analysis
```bash
# Complex query with joins and aggregations
curl -X POST http://localhost:8080/api/v1/data/query \
  -H "X-Tenant-ID: test-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "SELECT country, COUNT(*) as user_count FROM analytics.users GROUP BY country ORDER BY user_count DESC LIMIT 10",
    "limit": 100
  }'
```

### 3. Machine Learning
```bash
# 1. Register a model
curl -X POST http://localhost:8080/api/v1/models \
  -H "X-Tenant-ID: test-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "churn_prediction",
    "version": "1.0",
    "framework": "spark_ml",
    "algorithm": "RandomForest",
    "metrics": {"accuracy": 0.95, "auc": 0.92},
    "params": {"numTrees": 100, "maxDepth": 10}
  }'

# 2. Make predictions
curl -X POST http://localhost:8080/api/v1/models/{modelId}/predict \
  -H "Content-Type: application/json" \
  -d '{
    "inputData": [
      {"feature1": 0.5, "feature2": 0.8},
      {"feature1": 0.3, "feature2": 0.6}
    ]
  }'
```

---

**🚀 You're ready to build amazing data applications!**

