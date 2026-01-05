#!/bin/bash
# Test API Script - Run this after everything is started

echo "🧪 Testing Kadali API..."
echo ""

# Test 1: Health check
echo "1️⃣ Testing health endpoint..."
curl -s http://localhost:8080/actuator/health | jq '.' 2>/dev/null || curl -s http://localhost:8080/actuator/health
echo ""
echo ""

# Test 2: Create a cluster
echo "2️⃣ Creating a test Spark cluster..."
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
  }' | jq '.' 2>/dev/null || curl -X POST http://localhost:8080/api/v1/clusters \
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
echo ""
echo ""

# Test 3: List clusters
echo "3️⃣ Listing all clusters..."
curl -s http://localhost:8080/api/v1/clusters \
  -H "X-Tenant-ID: my-startup" | jq '.' 2>/dev/null || curl -s http://localhost:8080/api/v1/clusters \
  -H "X-Tenant-ID: my-startup"
echo ""
echo ""

# Test 4: Create database
echo "4️⃣ Creating analytics database..."
curl -X POST "http://localhost:8080/api/v1/data/databases?databaseName=analytics" \
  -H "X-Tenant-ID: my-startup"
echo ""
echo ""

# Test 5: Run SQL query
echo "5️⃣ Creating a test table..."
curl -X POST http://localhost:8080/api/v1/data/query \
  -H "X-Tenant-ID: my-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "CREATE TABLE IF NOT EXISTS analytics.test_users (id INT, name STRING, email STRING) USING DELTA",
    "limit": 1000
  }' | jq '.' 2>/dev/null || curl -X POST http://localhost:8080/api/v1/data/query \
  -H "X-Tenant-ID: my-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "CREATE TABLE IF NOT EXISTS analytics.test_users (id INT, name STRING, email STRING) USING DELTA",
    "limit": 1000
  }'
echo ""
echo ""

# Test 6: Insert data
echo "6️⃣ Inserting test data..."
curl -X POST http://localhost:8080/api/v1/data/query \
  -H "X-Tenant-ID: my-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "INSERT INTO analytics.test_users VALUES (1, \"Alice\", \"alice@example.com\"), (2, \"Bob\", \"bob@example.com\")",
    "limit": 1000
  }' | jq '.' 2>/dev/null || curl -X POST http://localhost:8080/api/v1/data/query \
  -H "X-Tenant-ID: my-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "INSERT INTO analytics.test_users VALUES (1, \"Alice\", \"alice@example.com\"), (2, \"Bob\", \"bob@example.com\")",
    "limit": 1000
  }'
echo ""
echo ""

# Test 7: Query data
echo "7️⃣ Querying the data..."
curl -X POST http://localhost:8080/api/v1/data/query \
  -H "X-Tenant-ID: my-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "SELECT * FROM analytics.test_users",
    "limit": 100
  }' | jq '.' 2>/dev/null || curl -X POST http://localhost:8080/api/v1/data/query \
  -H "X-Tenant-ID: my-startup" \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "SELECT * FROM analytics.test_users",
    "limit": 100
  }'
echo ""
echo ""

echo "✅ API tests complete!"
echo ""
echo "Now open http://localhost:3000 to see the dashboard!"

