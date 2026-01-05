# Kubernetes Deployment Guide

## Prerequisites

1. **Kubernetes Cluster**
   - AWS EKS, Google GKE, Azure AKS, or self-hosted
   - kubectl configured
   - At least 3 nodes with 4GB RAM each

2. **Container Registry**
   - Docker Hub, AWS ECR, GCR, or private registry
   - Docker logged in

3. **Ingress Controller** (choose one)
   ```bash
   # NGINX Ingress
   kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml
   
   # Or Traefik
   helm install traefik traefik/traefik --namespace traefik --create-namespace
   ```

4. **Cert Manager** (for SSL)
   ```bash
   kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml
   ```

---

## Quick Deploy

### Option 1: Automated Script

```bash
# Set your Docker registry
export DOCKER_REGISTRY="your-dockerhub-username"
export IMAGE_TAG="v1.0.0"

# Run deployment
./deploy.sh
```

### Option 2: Manual Deployment

```bash
# 1. Build and push image
docker build -t your-registry/kadali-api:latest .
docker push your-registry/kadali-api:latest

# 2. Update image in manifests
sed -i 's|your-registry|YOUR_ACTUAL_REGISTRY|g' k8s/base/kadali-api-deployment.yaml

# 3. Deploy to Kubernetes
kubectl apply -f k8s/base/namespace.yaml
kubectl apply -f k8s/base/postgres-deployment.yaml
kubectl apply -f k8s/base/minio-deployment.yaml
kubectl apply -f k8s/base/kadali-api-deployment.yaml
kubectl apply -f k8s/base/hpa.yaml
kubectl apply -f k8s/base/ingress.yaml
```

---

## Configuration

### 1. Update Secrets

**⚠️ IMPORTANT: Change default passwords!**

Edit `k8s/base/postgres-deployment.yaml`:
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: postgres-secret
stringData:
  POSTGRES_PASSWORD: "YOUR_SECURE_PASSWORD_HERE"
```

Edit `k8s/base/minio-deployment.yaml`:
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: minio-secret
stringData:
  MINIO_ROOT_USER: "admin"
  MINIO_ROOT_PASSWORD: "YOUR_SECURE_PASSWORD_HERE"
```

Edit `k8s/base/kadali-api-deployment.yaml`:
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: kadali-secret
stringData:
  DATABASE_PASSWORD: "YOUR_SECURE_PASSWORD_HERE"
  STORAGE_SECRET_KEY: "YOUR_SECURE_PASSWORD_HERE"
  JWT_SECRET: "your-256-bit-secret-key-change-this"
```

### 2. Update Domain Names

Edit `k8s/base/ingress.yaml`:
```yaml
spec:
  tls:
  - hosts:
    - api.yourdomain.com      # Change this
    - app.yourdomain.com       # Change this
    - minio.yourdomain.com     # Change this
  rules:
  - host: api.yourdomain.com   # Change this
```

### 3. Configure SSL Certificates

Create ClusterIssuer for Let's Encrypt:

```yaml
# k8s/base/cert-issuer.yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: your-email@example.com  # Change this
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
```

Apply it:
```bash
kubectl apply -f k8s/base/cert-issuer.yaml
```

---

## Verification

### Check Deployment Status

```bash
# All pods should be Running
kubectl get pods -n kadali

# Check services
kubectl get svc -n kadali

# Check ingress
kubectl get ingress -n kadali

# Get external IP
kubectl get ingress kadali-ingress -n kadali -o jsonpath='{.status.loadBalancer.ingress[0].ip}'
```

### Test API

```bash
# Get the API URL
API_URL=$(kubectl get ingress kadali-ingress -n kadali -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

# Test health endpoint
curl http://$API_URL/actuator/health

# Or with domain
curl https://api.yourdomain.com/actuator/health
```

---

## Scaling

### Manual Scaling

```bash
# Scale API replicas
kubectl scale deployment kadali-api --replicas=5 -n kadali

# Scale down
kubectl scale deployment kadali-api --replicas=2 -n kadali
```

### Auto-scaling (HPA is already configured)

The HPA will automatically scale based on:
- CPU usage > 70%
- Memory usage > 80%
- Min replicas: 3
- Max replicas: 10

Monitor auto-scaling:
```bash
kubectl get hpa -n kadali -w
```

---

## Monitoring

### View Logs

```bash
# API logs
kubectl logs -f -l app=kadali-api -n kadali

# PostgreSQL logs
kubectl logs -f -l app=postgres -n kadali

# MinIO logs
kubectl logs -f -l app=minio -n kadali

# All logs
kubectl logs -f --all-containers=true -n kadali
```

### Resource Usage

```bash
# Pod resource usage
kubectl top pods -n kadali

# Node resource usage
kubectl top nodes
```

### Events

```bash
# Recent events
kubectl get events -n kadali --sort-by='.lastTimestamp'
```

---

## Backup Strategy

### Database Backup

```bash
# Create backup job
kubectl create job --from=cronjob/postgres-backup manual-backup-$(date +%s) -n kadali

# Restore from backup
kubectl exec -it postgres-0 -n kadali -- pg_restore -U postgres -d kadali_platform /backups/backup.sql
```

### Persistent Volume Snapshots

```bash
# List PVCs
kubectl get pvc -n kadali

# Create snapshot (cloud-specific)
# AWS EBS
aws ec2 create-snapshot --volume-id vol-xxxxx --description "kadali-backup"

# GCP
gcloud compute disks snapshot DISK_NAME --snapshot-names=kadali-backup
```

---

## Troubleshooting

### Pods Not Starting

```bash
# Describe pod
kubectl describe pod <pod-name> -n kadali

# Check events
kubectl get events -n kadali | grep <pod-name>

# Check resource limits
kubectl top pods -n kadali
```

### Database Connection Issues

```bash
# Test PostgreSQL connection
kubectl exec -it <kadali-api-pod> -n kadali -- \
  psql -h postgres -U postgres -d kadali_platform

# Check service endpoints
kubectl get endpoints -n kadali
```

### Image Pull Errors

```bash
# Check image pull secrets
kubectl get secrets -n kadali

# Create Docker registry secret
kubectl create secret docker-registry regcred \
  --docker-server=https://index.docker.io/v1/ \
  --docker-username=YOUR_USERNAME \
  --docker-password=YOUR_PASSWORD \
  --docker-email=YOUR_EMAIL \
  -n kadali

# Update deployment to use secret
# Add to pod spec:
spec:
  imagePullSecrets:
  - name: regcred
```

### SSL/TLS Issues

```bash
# Check cert-manager
kubectl get certificate -n kadali
kubectl describe certificate kadali-tls -n kadali

# Check challenge
kubectl get challenge -n kadali

# Logs
kubectl logs -f -l app=cert-manager -n cert-manager
```

---

## Production Checklist

- [ ] Update all default passwords
- [ ] Configure persistent volume backups
- [ ] Set up monitoring (Prometheus + Grafana)
- [ ] Configure log aggregation (ELK or Loki)
- [ ] Set up alerts (PagerDuty, Slack)
- [ ] Configure DNS records
- [ ] Enable SSL/TLS
- [ ] Set resource limits and requests
- [ ] Configure pod disruption budgets
- [ ] Set up network policies
- [ ] Enable pod security policies
- [ ] Configure RBAC properly
- [ ] Set up CI/CD pipeline
- [ ] Document runbooks
- [ ] Perform load testing
- [ ] Set up disaster recovery plan

---

## Uninstalling

```bash
# Delete all resources
kubectl delete namespace kadali

# Or delete individual components
kubectl delete -f k8s/base/kadali-api-deployment.yaml
kubectl delete -f k8s/base/postgres-deployment.yaml
kubectl delete -f k8s/base/minio-deployment.yaml
kubectl delete -f k8s/base/ingress.yaml
kubectl delete -f k8s/base/hpa.yaml
kubectl delete -f k8s/base/namespace.yaml
```

---

## Cost Optimization

### Development Environment

- Use smaller node types
- Reduce replicas to 1
- Use node autoscaling
- Set aggressive scale-down policies

### Production Environment

- Use reserved/committed instances
- Right-size pods based on metrics
- Use spot/preemptible instances for non-critical workloads
- Enable cluster autoscaling
- Use horizontal pod autoscaling

---

**🚀 Your Kadali platform is now running on Kubernetes!**

