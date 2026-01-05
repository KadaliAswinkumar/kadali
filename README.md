# 🚀 Kadali Data Platform

A production-ready, Databricks-style data platform for collaborative analytics, built with Apache Spark, Delta Lake, and Spring Boot.

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)
![Apache Spark](https://img.shields.io/badge/Apache%20Spark-3.5-red.svg)

---

## 📋 Overview

Kadali is an enterprise-grade data platform that provides:

- **🔥 Apache Spark on Kubernetes** - Distributed data processing
- **💾 Delta Lake** - ACID transactions for data lakes
- **📊 SQL Analytics** - Interactive SQL queries
- **📓 Collaborative Notebooks** - JupyterHub integration
- **🤖 ML Operations** - MLflow for model management
- **🔄 Workflow Orchestration** - Apache Airflow for ETL/ELT
- **🎨 Modern React Dashboard** - Beautiful UI with Material-UI
- **☸️ Kubernetes Native** - Production-ready deployment

---

## ✨ Features

### Core Capabilities

- **Multi-Tenant Architecture** - Isolated workspaces for teams
- **Spark Cluster Management** - Create, scale, and manage Spark clusters
- **Data Catalog** - Centralized metadata management
- **Query Engine** - Execute SQL queries on Delta Lake
- **Data Connectors** - Connect to PostgreSQL, MySQL, MongoDB, S3, GCS
- **File Upload** - Support for CSV, Parquet, JSON
- **ML Model Registry** - Track and deploy ML models
- **REST APIs** - Complete programmatic access

### Infrastructure

- **PostgreSQL** - Metadata storage
- **MinIO** - S3-compatible object storage
- **Hive Metastore** - Delta Lake catalog
- **JupyterHub** - Multi-user notebooks with PySpark
- **Apache Airflow** - Workflow scheduling
- **MLflow** - Experiment tracking

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     React Dashboard (Frontend)               │
│              Material-UI • React Query • Vite                │
└─────────────────┬───────────────────────────────────────────┘
                  │ REST APIs
┌─────────────────▼───────────────────────────────────────────┐
│              Spring Boot Backend (Kadali API)                │
│    Spark Integration • Delta Lake • Kubernetes Client        │
└─────┬─────────┬──────────┬──────────┬────────────┬──────────┘
      │         │          │          │            │
┌─────▼───┐ ┌──▼──────┐ ┌─▼────────┐ ┌▼──────────┐ ┌▼─────────┐
│PostgreSQL│ │  MinIO   │ │  Spark   │ │JupyterHub │ │ Airflow  │
│Metadata  │ │Data Lake │ │ Clusters │ │ Notebooks │ │Workflows │
└──────────┘ └──────────┘ └──────────┘ └───────────┘ └──────────┘
```

---

## 🚀 Quick Start

### Prerequisites

- **Java 17+**
- **Maven 3.6+**
- **Docker Desktop**
- **Node.js 18+**
- **kubectl** (for K8s deployment)

### Local Development

```bash
# 1. Clone the repository
git clone https://github.com/KadaliAswinkumar/kadali.git
cd kadali

# 2. Run the automated setup
./quickstart.sh

# This will:
# ✅ Check prerequisites
# ✅ Start Docker services (PostgreSQL, MinIO, Metastore)
# ✅ Build the backend
# ✅ Guide you through starting services
```

### Manual Setup

```bash
# Terminal 1: Start infrastructure
docker compose up -d

# Terminal 2: Start backend
mvn clean install -DskipTests
mvn spring-boot:run

# Terminal 3: Start frontend
cd frontend
npm install
npm run dev
```

### Access the Platform

- **Dashboard**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin)
- **API Health**: http://localhost:8080/actuator/health

---

## 📚 Documentation

- **[STEP_BY_STEP_GUIDE.md](STEP_BY_STEP_GUIDE.md)** - Detailed setup guide
- **[THE_COMPLETE_JOURNEY.md](THE_COMPLETE_JOURNEY.md)** - Full build documentation
- **[KUBERNETES_DEPLOYMENT.md](KUBERNETES_DEPLOYMENT.md)** - Production deployment
- **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** - Feature overview
- **[NO_DOCKER_SETUP.md](NO_DOCKER_SETUP.md)** - Cloud-based setup
- **[RUN_IT_NOW.md](RUN_IT_NOW.md)** - Quick reference

---

## 🎯 Usage Examples

### Create a Spark Cluster

```bash
curl -X POST http://localhost:8080/api/v1/clusters \
  -H "X-Tenant-ID: my-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "analytics-cluster",
    "type": "INTERACTIVE",
    "driverMemory": "2g",
    "driverCores": 2,
    "executorMemory": "2g",
    "executorCores": 2,
    "executorCount": 2
  }'
```

### Run SQL Query

```bash
curl -X POST http://localhost:8080/api/v1/data/query \
  -H "X-Tenant-ID: my-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "SELECT * FROM analytics.users LIMIT 10",
    "limit": 100
  }'
```

### Upload Data

```bash
curl -X POST http://localhost:8080/api/v1/data/upload \
  -H "X-Tenant-ID: my-startup" \
  -F "file=@data.csv" \
  -F "tableName=users" \
  -F "databaseName=analytics"
```

---

## ☸️ Production Deployment

### Deploy to Kubernetes

```bash
# Build and deploy
./deploy.sh

# Or manually:
# 1. Build Docker images
docker build -t your-registry/kadali-api:latest .
docker build -t your-registry/kadali-frontend:latest ./frontend

# 2. Push images
docker push your-registry/kadali-api:latest
docker push your-registry/kadali-frontend:latest

# 3. Deploy to Kubernetes
kubectl apply -f k8s/base/
```

### Configure Ingress

Access the platform at: `https://kadali.yourdomain.com`

See [KUBERNETES_DEPLOYMENT.md](KUBERNETES_DEPLOYMENT.md) for details.

---

## 🧪 Testing

### Test Backend

```bash
# Run unit tests
mvn test

# Test API endpoints
./test-api.sh
```

### Test Frontend

```bash
cd frontend
npm test
```

---

## 🛠️ Tech Stack

### Backend
- **Spring Boot 3.2** - Application framework
- **Apache Spark 3.5** - Data processing
- **Delta Lake 3.2** - Storage format
- **PostgreSQL** - Metadata database
- **Kubernetes Client** - Cluster management

### Frontend
- **React 18** - UI framework
- **Material-UI v5** - Component library
- **React Query** - API state management
- **Vite** - Build tool
- **Axios** - HTTP client

### Infrastructure
- **Docker & Docker Compose** - Containerization
- **Kubernetes** - Orchestration
- **MinIO** - Object storage
- **Hive Metastore** - Catalog
- **Nginx** - Web server

---

## 📦 Project Structure

```
kadali/
├── src/main/java/com/kadali/       # Backend source code
│   ├── controller/                  # REST controllers
│   ├── service/                     # Business logic
│   ├── entity/                      # JPA entities
│   ├── repository/                  # Data repositories
│   ├── config/                      # Configuration
│   └── spark/                       # Spark integration
├── src/main/resources/              # Application config
│   ├── application.yml              # Main configuration
│   └── db/migration/                # Flyway migrations
├── frontend/                        # React dashboard
│   ├── src/                         # Frontend source
│   ├── public/                      # Static assets
│   └── package.json                 # NPM dependencies
├── k8s/                             # Kubernetes manifests
│   └── base/                        # Base deployments
├── docker-compose.yml               # Local infrastructure
├── Dockerfile                       # Backend container
├── quickstart.sh                    # Automated setup
├── deploy.sh                        # K8s deployment
└── test-api.sh                      # API testing
```

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **Apache Spark** - Distributed computing engine
- **Delta Lake** - ACID storage layer
- **Spring Boot** - Application framework
- **Databricks** - Inspiration for the platform

---

## 📞 Contact

**Kadali Aswin Kumar**

- GitHub: [@KadaliAswinkumar](https://github.com/KadaliAswinkumar)
- LinkedIn: [in/kadaliaswinkumar](https://linkedin.com/in/kadaliaswinkumar)
- Email: kadaliaswinkumar@gmail.com
- Twitter: [@kadali76](https://twitter.com/kadali76)
- Website: [linktr.ee/kadaliaswinkumar](https://linktr.ee/kadaliaswinkumar)

---

## ⭐ Star this repo if you find it useful!

**Built with ❤️ by Kadali Aswin Kumar**

---

## 🎯 What's Next?

- [ ] GraphQL API support
- [ ] Real-time streaming with Spark Structured Streaming
- [ ] Advanced RBAC and SSO integration
- [ ] Auto-scaling clusters based on workload
- [ ] Iceberg table format support
- [ ] BI tool integrations (Tableau, Power BI)
- [ ] Advanced monitoring and observability

---

**Happy Data Engineering! 🚀**
