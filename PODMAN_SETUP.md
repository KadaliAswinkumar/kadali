# 🐳 **PODMAN SETUP GUIDE FOR KADALI**

> **This is the Podman branch** - Docker-free version for corporate environments!

---

## 📋 **What is Podman?**

**Podman** is a **daemonless, rootless container engine** that's 100% Docker-compatible. It's perfect for:
- ✅ Corporate environments with Docker restrictions
- ✅ Security-conscious deployments (no root daemon)
- ✅ Almost identical commands to Docker
- ✅ Works with Docker Compose files via `podman-compose`

---

## 🚀 **QUICK START** (5 Minutes)

### **Step 1: Install Podman**

```bash
# Install via Homebrew (recommended)
brew install podman podman-compose

# Initialize Podman machine (one-time setup)
podman machine init --cpus 4 --memory 8192 --disk-size 50

# Start Podman machine
podman machine start

# Verify installation
podman --version
podman-compose --version
```

### **Step 2: Run Kadali with Podman**

```bash
# Make scripts executable
chmod +x run-kadali-podman.sh
chmod +x stop-kadali-podman.sh

# Run everything!
./run-kadali-podman.sh
```

**That's it!** 🎉 Open http://localhost:3000

---

## 🔧 **DETAILED INSTALLATION**

### **macOS (Recommended Method)**

```bash
# 1. Install Podman and Podman Compose
brew install podman podman-compose

# 2. Initialize Podman VM (one-time)
podman machine init \
  --cpus 4 \
  --memory 8192 \
  --disk-size 50 \
  --now

# 3. Verify Podman is running
podman machine list
# Should show: NAME    VM TYPE     CREATED     LAST UP     CPUS  MEMORY  DISK SIZE
#              podman  qemu        X ago       Currently   4     8GB     50GB

# 4. Test Podman
podman run --rm hello-world
```

### **Troubleshooting Podman Machine**

```bash
# Check machine status
podman machine list

# Start machine if stopped
podman machine start

# Stop machine
podman machine stop

# Restart machine (if having issues)
podman machine stop
podman machine start

# Remove and recreate machine (nuclear option)
podman machine stop
podman machine rm
podman machine init --cpus 4 --memory 8192 --disk-size 50 --now
```

---

## 📜 **AVAILABLE SCRIPTS**

### **1. run-kadali-podman.sh** ⭐ (Main script)
Starts everything automatically:
- Infrastructure (PostgreSQL, MinIO, Metastore)
- Backend API (Spring Boot)
- Frontend Dashboard (React)

```bash
./run-kadali-podman.sh
```

### **2. stop-kadali-podman.sh** 🛑
Stops everything gracefully:
```bash
./stop-kadali-podman.sh
```

### **3. quickstart-podman.sh** 🚀
Checks prerequisites and guides you through setup:
```bash
./quickstart-podman.sh
```

---

## 🆚 **DOCKER vs PODMAN COMMANDS**

Here's what changed under the hood:

| Docker Command | Podman Command | Notes |
|----------------|----------------|-------|
| `docker ps` | `podman ps` | Same output |
| `docker run` | `podman run` | Same syntax |
| `docker build` | `podman build` | Same syntax |
| `docker-compose up` | `podman-compose up` | Uses same YAML |
| `docker images` | `podman images` | Same output |
| `docker pull` | `podman pull` | Same syntax |

**Pro tip:** Create aliases for muscle memory:
```bash
# Add to your ~/.zshrc or ~/.bashrc
alias docker='podman'
alias docker-compose='podman-compose'
```

---

## 🌐 **ACCESS POINTS** (Same as Docker!)

Once running, access these URLs:

| Service | URL | Credentials |
|---------|-----|-------------|
| **Dashboard** 🎨 | http://localhost:3000 | None |
| **Backend API** 🔧 | http://localhost:8080 | None |
| **Health Check** ❤️ | http://localhost:8080/actuator/health | None |
| **MinIO Console** 💾 | http://localhost:9001 | minioadmin / minioadmin |
| **PostgreSQL** 🐘 | localhost:5432 | postgres / postgres |

---

## 🧪 **TESTING**

Test all APIs automatically:
```bash
./test-api.sh
```

Manual API tests:
```bash
# Health check
curl http://localhost:8080/actuator/health

# Create a cluster
curl -X POST http://localhost:8080/api/clusters \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: my-startup" \
  -d '{
    "name": "test-cluster",
    "clusterType": "interactive",
    "numExecutors": 2,
    "executorCores": 2,
    "executorMemory": "2g"
  }'

# List clusters
curl http://localhost:8080/api/clusters \
  -H "X-Tenant-ID: my-startup"
```

---

## 🐛 **TROUBLESHOOTING**

### **Issue: "podman: command not found"**
```bash
# Install Podman
brew install podman

# Verify
which podman
```

### **Issue: "podman-compose: command not found"**
```bash
# Install via Homebrew
brew install podman-compose

# Or via pip
pip3 install podman-compose

# Verify
which podman-compose
```

### **Issue: "Error: no podman machine VM found"**
```bash
# Create and start machine
podman machine init --cpus 4 --memory 8192 --disk-size 50 --now

# Verify
podman machine list
```

### **Issue: "Cannot connect to Podman socket"**
```bash
# Start Podman machine
podman machine start

# Check status
podman machine list

# Test connection
podman ps
```

### **Issue: "Port already in use"**
```bash
# Check what's using the port
lsof -i :8080  # For backend
lsof -i :3000  # For frontend
lsof -i :5432  # For PostgreSQL
lsof -i :9001  # For MinIO

# Stop services
./stop-kadali-podman.sh

# Or kill specific processes
kill $(lsof -t -i :8080)
```

### **Issue: Services won't start**
```bash
# Check Podman containers
podman ps -a

# Check logs
podman-compose logs

# Restart everything
./stop-kadali-podman.sh
./run-kadali-podman.sh
```

### **Issue: "Out of memory" errors**
```bash
# Stop machine
podman machine stop

# Remove machine
podman machine rm

# Recreate with more resources
podman machine init \
  --cpus 6 \
  --memory 16384 \
  --disk-size 100 \
  --now
```

---

## 📊 **RESOURCE MANAGEMENT**

### **Check Podman Machine Resources**
```bash
# Show machine info
podman machine info

# Show container stats
podman stats

# Show disk usage
podman system df
```

### **Clean Up**
```bash
# Remove stopped containers
podman container prune -f

# Remove unused images
podman image prune -a -f

# Remove unused volumes
podman volume prune -f

# Clean everything (nuclear option)
podman system prune -a --volumes -f
```

---

## 🔄 **SWITCHING BETWEEN DOCKER AND PODMAN**

### **To use Docker branch:**
```bash
git checkout main
./run-kadali.sh
```

### **To use Podman branch:**
```bash
git checkout podman
./run-kadali-podman.sh
```

Both branches have the **exact same features** - just different container runtimes!

---

## 🚀 **PRODUCTION DEPLOYMENT**

Podman supports Kubernetes pod definitions natively!

### **Generate Kubernetes YAML from running containers:**
```bash
# Export entire setup to Kubernetes
podman generate kube kadali-postgres > k8s-podman/postgres.yaml
podman generate kube kadali-minio > k8s-podman/minio.yaml
```

### **Deploy to Kubernetes:**
```bash
# Our existing k8s manifests work with both!
kubectl apply -k k8s/base/
```

---

## 💡 **WHY PODMAN FOR KADALI?**

1. **Security** ✅
   - No root daemon running
   - Rootless containers by default
   - Better for corporate compliance

2. **Compatibility** ✅
   - Same Docker commands
   - Uses same Dockerfiles
   - Uses same compose files

3. **Performance** ✅
   - Faster startup (no daemon)
   - Lower resource usage
   - Native systemd integration

4. **Kubernetes Native** ✅
   - Generate K8s YAML from pods
   - Runs Kubernetes pods directly
   - Perfect for our K8s deployment!

---

## 📚 **USEFUL COMMANDS**

```bash
# Start Podman machine
podman machine start

# Stop Podman machine
podman machine stop

# SSH into Podman machine
podman machine ssh

# List containers
podman ps

# View logs
podman logs <container-name>
podman-compose logs

# Follow logs
podman logs -f <container-name>

# Inspect container
podman inspect <container-name>

# Execute command in container
podman exec -it <container-name> bash

# Port forwarding check
podman port <container-name>

# Network list
podman network ls

# Volume list
podman volume ls
```

---

## 🎯 **NEXT STEPS**

1. **Run the platform:**
   ```bash
   ./run-kadali-podman.sh
   ```

2. **Open dashboard:**
   ```
   http://localhost:3000
   ```

3. **Set tenant ID:**
   - Go to Settings
   - Set Tenant ID: `my-startup`
   - Save configuration

4. **Create a cluster:**
   - Go to Clusters tab
   - Click "Create Cluster"
   - Use default settings
   - Click Create!

5. **Run SQL queries:**
   - Go to Data Explorer
   - Try: `SELECT 1 as test_value, 'Hello from Podman!' as message`

---

## 🤝 **SUPPORT**

Having issues? Check:
1. **Podman machine is running:** `podman machine list`
2. **Services are up:** `podman ps`
3. **Logs for errors:** `podman-compose logs`
4. **Ports are free:** `lsof -i :8080` `lsof -i :3000`

---

## 🌟 **SUCCESS!**

You're now running Kadali Data Platform with **Podman** - no Docker needed! 🎉

**Everything works exactly the same**, just with a more secure, enterprise-friendly container runtime!

---

Made with ❤️ for developers dealing with corporate Docker restrictions! 🐳➡️🦭
