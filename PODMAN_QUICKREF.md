# 🦭 **PODMAN BRANCH - QUICK REFERENCE**

## 🎯 **YOU NOW HAVE 2 OPTIONS!**

### **Option 1: Docker (main branch)** 🐳
```bash
git checkout main
./run-kadali.sh
```
**Use when:** You have Docker Desktop working

---

### **Option 2: Podman (podman branch)** 🦭
```bash
git checkout podman
./run-kadali-podman.sh
```
**Use when:** Docker is blocked by your company

---

## 🚀 **GETTING STARTED WITH PODMAN**

### **Step 1: Install Podman** (one-time)
```bash
# Install Podman and Podman Compose
brew install podman podman-compose

# Create Podman machine
podman machine init --cpus 4 --memory 8192 --disk-size 50

# Start Podman machine
podman machine start

# Verify
podman ps
```

### **Step 2: Switch to Podman Branch**
```bash
cd /Users/aswinkumar/Downloads/Aswin/Startups/backend-project/kadali
git checkout podman
```

### **Step 3: Run Kadali!**
```bash
# Easy mode (with setup wizard)
./quickstart-podman.sh

# Or direct run
./run-kadali-podman.sh
```

### **Step 4: Open Dashboard**
```
http://localhost:3000
```

---

## 🛑 **STOPPING**

```bash
./stop-kadali-podman.sh
```

---

## 📊 **USEFUL COMMANDS**

### **Check Podman Status**
```bash
podman machine list     # Check if machine is running
podman ps               # List running containers
podman-compose ps       # Check services
```

### **View Logs**
```bash
tail -f backend.log     # Backend logs
tail -f frontend.log    # Frontend logs
podman-compose logs     # Infrastructure logs
```

### **Troubleshooting**
```bash
# Start Podman machine
podman machine start

# Restart everything
./stop-kadali-podman.sh
./run-kadali-podman.sh

# Clean up
podman system prune -a -f
```

---

## 🔄 **SWITCHING BETWEEN DOCKER AND PODMAN**

### **To switch from Docker → Podman:**
```bash
# Stop Docker version
./stop-kadali.sh

# Switch branch
git checkout podman

# Run Podman version
./run-kadali-podman.sh
```

### **To switch from Podman → Docker:**
```bash
# Stop Podman version
./stop-kadali-podman.sh

# Switch branch
git checkout main

# Run Docker version
./run-kadali.sh
```

---

## 📚 **DOCUMENTATION**

| Branch | Setup Guide | Quick Start | Run Script |
|--------|-------------|-------------|------------|
| **main** | STEP_BY_STEP_GUIDE.md | quickstart.sh | run-kadali.sh |
| **podman** | PODMAN_SETUP.md | quickstart-podman.sh | run-kadali-podman.sh |

---

## 🎓 **ALIASES FOR MUSCLE MEMORY**

Add to your `~/.zshrc`:
```bash
# Podman aliases (makes it feel like Docker)
alias docker='podman'
alias docker-compose='podman-compose'
```

Then you can use Docker commands naturally:
```bash
docker ps              # Actually runs: podman ps
docker-compose up      # Actually runs: podman-compose up
```

---

## ✅ **WHAT'S IDENTICAL**

Both branches have:
- ✅ Same Java backend (Spring Boot)
- ✅ Same React frontend
- ✅ Same PostgreSQL database
- ✅ Same MinIO storage
- ✅ Same Apache Spark
- ✅ Same REST APIs
- ✅ Same features
- ✅ Same Kubernetes deployment

**Only difference:** Container runtime (Docker vs Podman)

---

## 🌐 **ACCESS POINTS** (Same for Both!)

| Service | URL | Credentials |
|---------|-----|-------------|
| Dashboard | http://localhost:3000 | None |
| Backend API | http://localhost:8080 | None |
| Health Check | http://localhost:8080/actuator/health | None |
| MinIO Console | http://localhost:9001 | minioadmin / minioadmin |
| PostgreSQL | localhost:5432 | postgres / postgres |

---

## 🆘 **HELP!**

### **Issue: "podman: command not found"**
```bash
brew install podman
```

### **Issue: "no podman machine VM found"**
```bash
podman machine init --cpus 4 --memory 8192 --disk-size 50
podman machine start
```

### **Issue: "Cannot connect to Podman socket"**
```bash
podman machine start
podman ps  # Verify connection
```

### **More help:**
```bash
cat PODMAN_SETUP.md  # Full detailed guide
```

---

## 🎉 **SUMMARY**

You now have **TWO ways** to run Kadali:

1. **Docker** (main branch) - Standard, common approach
2. **Podman** (podman branch) - Corporate-friendly, no Docker needed

**Both work identically!** Choose based on your environment! 🚀

---

Quick start with Podman:
```bash
brew install podman podman-compose
podman machine init --cpus 4 --memory 8192 --disk-size 50
podman machine start
git checkout podman
./run-kadali-podman.sh
```

**That's it!** 🎊
