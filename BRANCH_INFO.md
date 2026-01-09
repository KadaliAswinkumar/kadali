# 🌳 **KADALI BRANCHES GUIDE**

This repository has **two main branches** with identical features but different container runtimes!

---

## 📦 **BRANCHES**

### **`main` Branch** 🐳
**Uses:** Docker & Docker Compose

**Best for:**
- ✅ Standard development environments
- ✅ Systems with Docker Desktop installed
- ✅ Most tutorials and documentation
- ✅ Production Kubernetes deployments

**Run commands:**
```bash
git checkout main
./run-kadali.sh
```

---

### **`podman` Branch** 🦭
**Uses:** Podman & Podman Compose

**Best for:**
- ✅ Corporate environments with Docker restrictions
- ✅ Security-conscious deployments (rootless containers)
- ✅ Avoiding Docker Desktop licensing
- ✅ Lighter resource usage

**Run commands:**
```bash
git checkout podman
./run-kadali-podman.sh
```

---

## 🔄 **SWITCHING BETWEEN BRANCHES**

### **To switch to Docker version:**
```bash
git checkout main
./stop-kadali-podman.sh  # If Podman is running
./run-kadali.sh
```

### **To switch to Podman version:**
```bash
git checkout podman
./stop-kadali.sh  # If Docker is running
./run-kadali-podman.sh
```

---

## 📊 **FEATURE COMPARISON**

| Feature | `main` (Docker) | `podman` (Podman) |
|---------|-----------------|-------------------|
| **Container Runtime** | Docker Engine | Podman |
| **Compose Tool** | docker-compose | podman-compose |
| **Backend** | ✅ Spring Boot | ✅ Spring Boot |
| **Frontend** | ✅ React Dashboard | ✅ React Dashboard |
| **Database** | ✅ PostgreSQL | ✅ PostgreSQL |
| **Storage** | ✅ MinIO | ✅ MinIO |
| **Spark** | ✅ Apache Spark | ✅ Apache Spark |
| **APIs** | ✅ Full REST API | ✅ Full REST API |
| **Kubernetes** | ✅ Full Support | ✅ Full Support |
| **Security** | Good | Excellent (rootless) |
| **Resource Usage** | Moderate | Lower |
| **Setup Complexity** | Easy | Easy |

---

## 🎯 **WHICH BRANCH SHOULD YOU USE?**

### **Use `main` (Docker) if:**
- ✅ You already have Docker Desktop installed
- ✅ You're following standard tutorials
- ✅ Your company allows Docker
- ✅ You want the most common setup

### **Use `podman` (Podman) if:**
- ✅ Docker is blocked by your company
- ✅ You want rootless containers
- ✅ You need better security
- ✅ You want to avoid Docker Desktop licensing

---

## 📚 **DOCUMENTATION BY BRANCH**

### **Main Branch (Docker)**
- **Setup Guide:** `STEP_BY_STEP_GUIDE.md`
- **Run Guide:** `RUN_IT_NOW.md`
- **Quick Start:** `./quickstart.sh`
- **Run Script:** `./run-kadali.sh`
- **Stop Script:** `./stop-kadali.sh`

### **Podman Branch**
- **Setup Guide:** `PODMAN_SETUP.md` ⭐
- **Quick Start:** `./quickstart-podman.sh`
- **Run Script:** `./run-kadali-podman.sh`
- **Stop Script:** `./stop-kadali-podman.sh`

---

## 🔧 **MAINTENANCE**

### **Updating Both Branches**

When adding new features, update both branches:

```bash
# Work on main branch
git checkout main
# ... make changes ...
git add .
git commit -m "Add feature X"
git push origin main

# Cherry-pick to podman branch
git checkout podman
git cherry-pick <commit-hash>
# Update podman-specific scripts if needed
git push origin podman
```

### **Keeping Branches in Sync**

Both branches should have:
- ✅ Same Java code
- ✅ Same React code
- ✅ Same database migrations
- ✅ Same docker-compose.yml (podman-compose uses it!)
- ❌ Different run scripts (`run-kadali.sh` vs `run-kadali-podman.sh`)

---

## 🌐 **ACCESS POINTS** (Same for Both!)

| Service | URL | Credentials |
|---------|-----|-------------|
| **Dashboard** | http://localhost:3000 | None |
| **Backend API** | http://localhost:8080 | None |
| **MinIO Console** | http://localhost:9001 | minioadmin / minioadmin |
| **PostgreSQL** | localhost:5432 | postgres / postgres |

---

## 🚀 **DEPLOYMENT**

### **Both branches deploy identically to Kubernetes!**

The Kubernetes manifests in `k8s/` work with both Docker and Podman:

```bash
# Deploy to Kubernetes (works from either branch)
./deploy.sh

# Or manually
kubectl apply -k k8s/base/
```

Why? Because:
- Kubernetes uses **containerd** runtime
- Both Docker and Podman create OCI-compliant images
- Same image registry (Docker Hub, etc.)

---

## 📦 **DOCKER IMAGES**

Both branches build **identical Docker images**:

```bash
# Build from main branch
git checkout main
docker build -t kadali:latest .

# Build from podman branch
git checkout podman
podman build -t kadali:latest .

# Both create the same OCI-compliant image!
```

---

## 🎓 **LEARNING PATH**

### **For Beginners:**
Start with `main` branch (Docker) - it's more common and has more tutorials online.

### **For Corporate Users:**
Use `podman` branch immediately if Docker is blocked.

### **For Production:**
Either works! Choose based on your infrastructure:
- Docker → More common, more support
- Podman → Better security, rootless

---

## 🤝 **CONTRIBUTING**

When contributing:
1. Make changes in `main` first
2. Test thoroughly
3. Port to `podman` branch
4. Test again with Podman
5. Submit PRs for both branches

---

## 🆘 **GETTING HELP**

### **Docker Issues (main branch):**
- Read: `STEP_BY_STEP_GUIDE.md`
- Check: `TOMORROW_CHECKLIST.md`
- Run: `./quickstart.sh`

### **Podman Issues (podman branch):**
- Read: `PODMAN_SETUP.md` ⭐
- Run: `./quickstart-podman.sh`
- Check: `podman machine list`

---

## 🎉 **SUMMARY**

- **Two branches, same features, different runtimes**
- **Choose based on your environment**
- **Both production-ready**
- **Switch anytime!**

---

🚀 **Happy Coding!** 🚀
