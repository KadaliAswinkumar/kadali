# 🚀 No Docker? No Problem! - Cloud Setup Guide

If you don't want to install Docker, you can use free cloud services instead.

## 📋 **What You Need:**

1. **PostgreSQL Database** - For metadata storage
2. **S3-Compatible Storage** - For data lake storage

---

## ☁️ **Free Cloud Services You Can Use**

### **1. PostgreSQL - ElephantSQL (FREE)**

**Steps:**
1. Go to: https://www.elephantsql.com/
2. Sign up (free)
3. Create a "Tiny Turtle" free instance
4. Copy the connection URL

You'll get something like:
```
postgres://username:password@hostname.db.elephantsql.com/database
```

### **2. S3 Storage - Backblaze B2 or Wasabi (FREE tier)**

**Option A: Backblaze B2**
1. Go to: https://www.backblaze.com/b2/cloud-storage.html
2. Sign up (10GB free)
3. Create a bucket called `kadali-data-lake`
4. Get your Application Key and ID

**Option B: Wasabi**
1. Go to: https://wasabi.com/
2. Sign up (1TB free for 30 days)
3. Create a bucket called `kadali-data-lake`
4. Get your Access Key and Secret Key

---

## ⚙️ **Configuration Steps**

### **Step 1: Update Application Config**

Edit `src/main/resources/application-cloud.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://YOUR_ELEPHANTSQL_HOST/YOUR_DATABASE
    username: YOUR_USERNAME
    password: YOUR_PASSWORD

minio:
  endpoint: https://s3.us-west-001.backblazeb2.com  # Or Wasabi endpoint
  access-key: YOUR_ACCESS_KEY
  secret-key: YOUR_SECRET_KEY
  bucket: kadali-data-lake

spark:
  master: local[*]  # Run Spark locally
```

### **Step 2: Run with Cloud Profile**

```bash
cd /Users/aswinkumar/Downloads/Aswin/Startups/backend-project/kadali

# Build the project
mvn clean install -DskipTests

# Run with cloud profile
mvn spring-boot:run -Dspring-boot.run.profiles=cloud
```

### **Step 3: Start Frontend**

```bash
cd frontend
npm install
npm run dev
```

---

## 🎯 **Quick Start with Cloud Services**

```bash
# Terminal 1: Backend
cd /Users/aswinkumar/Downloads/Aswin/Startups/backend-project/kadali
export SPRING_PROFILES_ACTIVE=cloud
export DB_URL="jdbc:postgresql://YOUR_HOST/YOUR_DB"
export DB_USERNAME="YOUR_USERNAME"
export DB_PASSWORD="YOUR_PASSWORD"
export S3_ENDPOINT="https://YOUR_S3_ENDPOINT"
export S3_ACCESS_KEY="YOUR_ACCESS_KEY"
export S3_SECRET_KEY="YOUR_SECRET_KEY"

mvn spring-boot:run

# Terminal 2: Frontend
cd /Users/aswinkumar/Downloads/Aswin/Startups/backend-project/kadali/frontend
npm run dev
```

---

## ⚡ **But Honestly... Docker Desktop is MUCH Easier!**

**Installing Docker takes 5 minutes** and gives you:
- ✅ Everything works out of the box
- ✅ No cloud account setup needed
- ✅ No configuration changes needed
- ✅ Local development is faster
- ✅ No internet dependency

**Just install Docker Desktop and run:**
```bash
./quickstart.sh
```

Done! 🎉

---

## 💡 **My Recommendation**

**Install Docker Desktop** - It's the standard tool for modern development and you'll need it for other projects too!

Download here: https://www.docker.com/products/docker-desktop

Once installed, just run `./quickstart.sh` and everything will work perfectly! 🚀

