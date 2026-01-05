# 🚀 Complete Step-by-Step Guide to Run Kadali Data Platform

## Overview

You have a complete Databricks-style data analytics platform with:
- **Backend**: Spring Boot + Apache Spark + Delta Lake
- **Frontend**: React Dashboard
- **Infrastructure**: Kubernetes-ready deployment

---

## PART 1: Local Development Setup (Fastest Way to See It Working)

### Step 1: Prerequisites Check

Open terminal and verify:

```bash
# Check Java (need 17+)
java -version
# Should show: java version "17.x.x" or higher

# Check Maven
mvn -version
# Should show: Apache Maven 3.x.x

# Check Docker
docker --version
# Should show: Docker version 20.x.x or higher

# Check Node.js (need 18+)
node --version
# Should show: v18.x.x or higher

# Check npm
npm --version
# Should show: 9.x.x or higher
```

If anything is missing, install it first!

---

### Step 2: Start Infrastructure Services

```bash
# Navigate to project
cd /Users/aswinkumar/Downloads/Aswin/Startups/backend-project/kadali

# Start PostgreSQL, MinIO, and Metastore
docker-compose up -d

# Wait 30 seconds for services to be ready
sleep 30

# Check all services are running
docker-compose ps

# You should see:
# ✅ kadali-postgres (port 5432)
# ✅ kadali-minio (ports 9000, 9001)
# ✅ kadali-metastore (port 9083)
```

**What's happening?**
- PostgreSQL stores metadata (clusters, notebooks, datasets)
- MinIO provides S3-compatible storage for Delta Lake
- Hive Metastore manages data catalog

---

### Step 3: Start Backend API

Open a **NEW terminal window** (keep docker-compose running):

```bash
# Navigate to project
cd /Users/aswinkumar/Downloads/Aswin/Startups/backend-project/kadali

# Build the project (first time only)
mvn clean install -DskipTests

# This will take 2-5 minutes to download dependencies and compile
# You'll see lots of output - that's normal!

# When build succeeds, you'll see:
# [INFO] BUILD SUCCESS

# Now run the application
mvn spring-boot:run

# Wait for this message:
# "Started KadaliDataPlatformApplication in X seconds"
```

**What's happening?**
- Maven compiles Java code
- Spring Boot starts the API server on port 8080
- Database migrations run automatically (Flyway)
- Spark configuration initializes

**Backend is now running at: http://localhost:8080**

---

### Step 4: Test Backend API

Open a **THIRD terminal window**:

```bash
# Test health endpoint
curl http://localhost:8080/actuator/health

# Expected output:
# {"status":"UP"}

# Create your first Spark cluster
curl -X POST http://localhost:8080/api/v1/clusters \
  -H "X-Tenant-ID: my-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "test-cluster",
    "type": "INTERACTIVE",
    "driverMemory": "1g",
    "driverCores": 1,
    "executorMemory": "1g",
    "executorCores": 1,
    "executorCount": 1
  }'

# You should see JSON response with cluster details!

# List all clusters
curl http://localhost:8080/api/v1/clusters \
  -H "X-Tenant-ID: my-startup"

# Expected: Array with your cluster
```

**🎉 If you see cluster data, backend is working!**

---

### Step 5: Start Frontend Dashboard

Open a **FOURTH terminal window**:

```bash
# Navigate to frontend directory
cd /Users/aswinkumar/Downloads/Aswin/Startups/backend-project/kadali/frontend

# Install dependencies (first time only - takes 2-3 minutes)
npm install

# If you see any warnings about vulnerabilities, that's normal for now

# Start development server
npm run dev

# You'll see:
#   VITE v5.x.x  ready in XXX ms
#   ➜  Local:   http://localhost:3000/
#   ➜  Network: use --host to expose
```

**Frontend is now running at: http://localhost:3000**

---

### Step 6: Access the Dashboard

1. **Open your browser** to: http://localhost:3000

2. **You should see:**
   - Kadali logo on the left sidebar
   - Dashboard page with 4 stat cards
   - Navigation menu: Dashboard, Clusters, Notebooks, Data Explorer, ML Models, Settings

3. **Set your tenant ID:**
   - Click **"Settings"** in the sidebar
   - Enter Tenant ID: `my-startup`
   - Click **"Save Settings"**
   - Page will reload

4. **View your cluster:**
   - Click **"Clusters"** in the sidebar
   - You should see your "test-cluster" from Step 4!
   - Status should show as "RUNNING" or "CREATING"

5. **Create a database:**
   - Click **"Data Explorer"** in the sidebar
   - Click on **"Databases"** tab
   - In terminal, run:
     ```bash
     curl -X POST "http://localhost:8080/api/v1/data/databases?databaseName=analytics" \
       -H "X-Tenant-ID: my-startup"
     ```
   - Refresh the page - you'll see "analytics" database!

6. **Run your first SQL query:**
   - Go to **"SQL Editor"** tab
   - Enter this query:
     ```sql
     CREATE TABLE analytics.test_users (
       id INT,
       name STRING,
       email STRING
     ) USING DELTA
     ```
   - Click **"Execute"** button
   - You should see "Query completed!" notification

7. **Insert some data:**
   - Enter this query:
     ```sql
     INSERT INTO analytics.test_users VALUES
       (1, 'Alice', 'alice@example.com'),
       (2, 'Bob', 'bob@example.com'),
       (3, 'Charlie', 'charlie@example.com')
     ```
   - Click **"Execute"**

8. **Query the data:**
   - Enter:
     ```sql
     SELECT * FROM analytics.test_users
     ```
   - Click **"Execute"**
   - You should see a table with your 3 users! 🎉

---

### Step 7: Explore More Features

**Create another cluster via UI:**
1. Go to **Clusters** page
2. Click **"Create Cluster"** button
3. Fill in the form:
   - Name: `analytics-cluster`
   - Type: INTERACTIVE
   - Driver Memory: 2g
   - Driver Cores: 1
   - Executor Memory: 2g
   - Executor Cores: 1
   - Executor Count: 2
4. Click **"Create"**
5. Watch it appear in the list!

**Browse data catalog:**
1. Go to **Data Explorer**
2. Click **"Tables"** tab
3. You'll see your `analytics.test_users` table with metadata
4. Note the row count, size, and format (Delta)

**Access MinIO Console:**
1. Open browser to: http://localhost:9001
2. Login:
   - Username: `minioadmin`
   - Password: `minioadmin`
3. Browse your lakehouse data files!

---

### Step 8: Stop Everything

When you're done testing:

```bash
# Stop frontend (Terminal 4)
# Press Ctrl+C

# Stop backend (Terminal 2)
# Press Ctrl+C

# Stop Docker services (Terminal 1)
docker-compose down

# Or to remove all data:
docker-compose down -v
```

---

## PART 2: Production Deployment on Kubernetes

### Prerequisites for K8s Deployment

- Kubernetes cluster (AWS EKS, Google GKE, Azure AKS, or local minikube)
- `kubectl` configured
- Docker Hub account (or other container registry)
- Domain name (optional, for SSL)

### Step 1: Prepare Docker Images

```bash
cd /Users/aswinkumar/Downloads/Aswin/Startups/backend-project/kadali

# Set your Docker Hub username
export DOCKER_REGISTRY="your-dockerhub-username"
export IMAGE_TAG="v1.0.0"

# Login to Docker Hub
docker login

# Build backend image
docker build -t $DOCKER_REGISTRY/kadali-api:$IMAGE_TAG .

# Build frontend image
cd frontend
docker build -t $DOCKER_REGISTRY/kadali-frontend:$IMAGE_TAG .

# Push both images
docker push $DOCKER_REGISTRY/kadali-api:$IMAGE_TAG
docker push $DOCKER_REGISTRY/kadali-frontend:$IMAGE_TAG
```

### Step 2: Update Kubernetes Manifests

```bash
cd /Users/aswinkumar/Downloads/Aswin/Startups/backend-project/kadali

# Update API image
sed -i '' "s|your-registry/kadali-api:latest|$DOCKER_REGISTRY/kadali-api:$IMAGE_TAG|g" k8s/base/kadali-api-deployment.yaml

# Update Frontend image
sed -i '' "s|your-registry/kadali-frontend:latest|$DOCKER_REGISTRY/kadali-frontend:$IMAGE_TAG|g" k8s/base/frontend-deployment.yaml
```

### Step 3: Update Secrets (IMPORTANT!)

```bash
# Edit secrets with secure passwords
nano k8s/base/postgres-deployment.yaml
# Change POSTGRES_PASSWORD

nano k8s/base/minio-deployment.yaml
# Change MINIO_ROOT_PASSWORD

nano k8s/base/kadali-api-deployment.yaml
# Change DATABASE_PASSWORD, STORAGE_SECRET_KEY, JWT_SECRET
```

### Step 4: Deploy to Kubernetes

```bash
# Create namespace
kubectl apply -f k8s/base/namespace.yaml

# Deploy PostgreSQL
kubectl apply -f k8s/base/postgres-deployment.yaml

# Deploy MinIO
kubectl apply -f k8s/base/minio-deployment.yaml

# Wait for databases (3-5 minutes)
kubectl wait --for=condition=ready pod -l app=postgres -n kadali --timeout=300s
kubectl wait --for=condition=ready pod -l app=minio -n kadali --timeout=300s

# Deploy Kadali API
kubectl apply -f k8s/base/kadali-api-deployment.yaml

# Deploy Frontend
kubectl apply -f k8s/base/frontend-deployment.yaml

# Deploy HPA (auto-scaling)
kubectl apply -f k8s/base/hpa.yaml

# Deploy Ingress
kubectl apply -f k8s/base/ingress.yaml
```

### Step 5: Verify Deployment

```bash
# Check all pods are running
kubectl get pods -n kadali

# Expected output:
# NAME                           READY   STATUS    RESTARTS   AGE
# postgres-xxx                   1/1     Running   0          2m
# minio-xxx                      1/1     Running   0          2m
# kadali-api-xxx                 1/1     Running   0          1m
# kadali-frontend-xxx            1/1     Running   0          1m

# Check services
kubectl get svc -n kadali

# Check ingress
kubectl get ingress -n kadali

# Get logs
kubectl logs -f -l app=kadali-api -n kadali
```

### Step 6: Access Production

```bash
# Get ingress IP
kubectl get ingress kadali-ingress -n kadali

# Access via IP (or configure DNS)
# API: http://<INGRESS-IP>/api/v1
# Frontend: http://<INGRESS-IP>
```

---

## PART 3: Troubleshooting

### Backend won't start

```bash
# Check Java version
java -version  # Must be 17+

# Check port 8080 is free
lsof -i :8080
# If something is using it, kill it or change port in application.yml

# Check Docker services are running
docker-compose ps
```

### Frontend won't start

```bash
# Clear node modules and reinstall
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run dev

# Check port 3000 is free
lsof -i :3000
```

### Can't connect to backend from frontend

```bash
# Check backend is running
curl http://localhost:8080/actuator/health

# Check CORS settings in SecurityConfig.java
# Check proxy settings in frontend/vite.config.ts
```

### Database connection errors

```bash
# Restart PostgreSQL
docker-compose restart postgres

# Check connection
docker exec -it kadali-postgres psql -U postgres -d kadali_platform

# Inside psql:
\dt  # List tables
\q   # Quit
```

---

## Quick Reference

### Useful Commands

```bash
# Backend
mvn spring-boot:run                    # Start backend
mvn clean install                      # Build
mvn test                               # Run tests

# Frontend
npm install                            # Install dependencies
npm run dev                            # Start dev server
npm run build                          # Production build
npm run preview                        # Preview production build

# Docker
docker-compose up -d                   # Start services
docker-compose down                    # Stop services
docker-compose ps                      # Check status
docker-compose logs -f                 # View logs

# Kubernetes
kubectl get pods -n kadali             # List pods
kubectl logs -f <pod-name> -n kadali   # View logs
kubectl describe pod <pod-name> -n kadali  # Debug pod
kubectl exec -it <pod-name> -n kadali -- bash  # Shell into pod
```

### Port Reference

- **8080**: Backend API
- **3000**: Frontend dev server
- **5432**: PostgreSQL
- **9000**: MinIO API
- **9001**: MinIO Console
- **9083**: Hive Metastore

### Default Credentials

**Local Development:**
- PostgreSQL: postgres/postgres
- MinIO: minioadmin/minioadmin
- Tenant ID: (you choose, e.g., "my-startup")

**Production:** CHANGE ALL DEFAULTS!

---

## 🎉 Success Criteria

You know everything is working when:

1. ✅ Backend responds at http://localhost:8080/actuator/health
2. ✅ Frontend loads at http://localhost:3000
3. ✅ You can see clusters in the dashboard
4. ✅ You can execute SQL queries
5. ✅ You can create and query Delta Lake tables
6. ✅ MinIO console shows your data files

---

## Next Steps After Setup

1. **Explore the UI** - Click through all pages
2. **Create more tables** - Build your data catalog
3. **Upload CSV files** - Use the connector API
4. **Create notebooks** - Start analyzing data
5. **Deploy to production** - Follow K8s deployment guide

---

**Need Help?** Check these docs:
- `GETTING_STARTED.md` - Quick start guide
- `KUBERNETES_DEPLOYMENT.md` - K8s details
- `PROJECT_SUMMARY.md` - Feature list
- `frontend/README.md` - Frontend docs

**Everything working?** Congratulations! You're running a production-grade data platform! 🚀

