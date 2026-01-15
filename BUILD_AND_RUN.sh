#!/bin/bash
# 🚀 BUILD AND RUN KADALI - COMPLETE SCRIPT
# Run this in your terminal to make everything work!

set -e

echo "🚀 KADALI - COMPLETE BUILD AND RUN"
echo "=================================="
echo ""

# Navigate to project
cd /Users/aswinkumar/Downloads/Aswin/Startups/backend-project/kadali

# 1. Ensure Podman is running
echo "1️⃣ Checking Podman..."
if ! podman machine list | grep -q "Currently running"; then
    echo "   Starting Podman machine..."
    podman machine start
    sleep 5
fi
echo "   ✅ Podman is running"
echo ""

# 2. Clean old build
echo "2️⃣ Cleaning old builds..."
podman stop kadali-api kadali-metastore 2>/dev/null || true
podman rm kadali-api kadali-metastore 2>/dev/null || true
podman rmi localhost/kadali_kadali-api:latest 2>/dev/null || true
echo "   ✅ Cleaned"
echo ""

# 3. Build fresh (with Jetty exclusions)
echo "3️⃣ Building API (this will take 2-3 minutes)..."
echo "   ⏳ Downloading dependencies and compiling..."
podman-compose build --no-cache kadali-api
echo "   ✅ Build complete!"
echo ""

# 4. Start all services
echo "4️⃣ Starting all services..."
podman-compose up -d
sleep 10
echo "   ✅ Services started"
echo ""

# 5. Check status
echo "5️⃣ Checking service status..."
podman-compose ps
echo ""

# 6. Wait for API to be ready
echo "6️⃣ Waiting for API to start (30 seconds)..."
sleep 30

# 7. Test API
echo "7️⃣ Testing API..."
if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
    echo "   ✅ API is UP and healthy!"
else
    echo "   ⚠️  API not responding yet. Checking logs..."
    echo ""
    echo "   Last 30 lines of API logs:"
    podman logs kadali-api 2>&1 | tail -30
    echo ""
    echo "   If you see errors above, the build may need fixes."
fi
echo ""

# 8. Start frontend
echo "8️⃣ Starting frontend..."
cd frontend
if [ ! -d "node_modules" ]; then
    echo "   Installing frontend dependencies..."
    npm install
fi
echo "   Starting React dev server..."
npm run dev &
FRONTEND_PID=$!
echo $FRONTEND_PID > ../frontend.pid
echo "   ✅ Frontend starting (PID: $FRONTEND_PID)"
cd ..
echo ""

# 9. Final status
echo "🎉 KADALI IS READY!"
echo "===================="
echo ""
echo "📊 Access your platform:"
echo "   Dashboard:    http://localhost:3000"
echo "   Backend API:  http://localhost:8080"
echo "   Health Check: http://localhost:8080/actuator/health"
echo "   MinIO Console: http://localhost:9001"
echo ""
echo "🛑 To stop everything:"
echo "   ./stop-kadali-podman.sh"
echo ""
echo "📋 Check logs:"
echo "   podman logs kadali-api"
echo "   podman-compose logs"
echo ""
