package com.kadali.service;

import com.kadali.entity.SparkCluster;
import com.kadali.entity.Tenant;
import com.kadali.repository.SparkClusterRepository;
import com.kadali.repository.TenantRepository;
import com.kadali.spark.K8sSparkClusterManager;
import io.fabric8.kubernetes.api.model.Pod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SparkClusterService {
    
    private final SparkClusterRepository clusterRepository;
    private final TenantRepository tenantRepository;
    private final K8sSparkClusterManager k8sManager;
    
    @Transactional
    public SparkCluster createCluster(String tenantId, String name, 
                                      SparkCluster.ClusterType type,
                                      String driverMemory, int driverCores,
                                      String executorMemory, int executorCores, 
                                      int executorCount) {
        log.info("Creating Spark cluster for tenant: {}", tenantId);
        
        Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
        
        String clusterId = "cluster-" + UUID.randomUUID().toString().substring(0, 8);
        String namespace = "kadali-" + tenantId;
        
        SparkCluster cluster = SparkCluster.builder()
                .clusterId(clusterId)
                .tenant(tenant)
                .name(name)
                .clusterType(type)
                .status(SparkCluster.ClusterStatus.CREATING)
                .driverMemory(driverMemory)
                .driverCores(driverCores)
                .executorMemory(executorMemory)
                .executorCores(executorCores)
                .executorCount(executorCount)
                .namespace(namespace)
                .build();
        
        cluster = clusterRepository.save(cluster);
        
        try {
            // Create Spark cluster on Kubernetes
            Pod driverPod = k8sManager.createSparkDriverPod(
                    clusterId, tenantId,
                    driverMemory, driverCores,
                    executorMemory, executorCores, executorCount
            );
            
            cluster.setDriverPodName(driverPod.getMetadata().getName());
            cluster.setStatus(SparkCluster.ClusterStatus.RUNNING);
            cluster.setStartedAt(LocalDateTime.now());
            
            // Get Spark UI URL
            String sparkUiUrl = k8sManager.getSparkUIUrl(clusterId, tenantId);
            cluster.setSparkUiUrl(sparkUiUrl);
            
            log.info("Spark cluster created successfully: {}", clusterId);
            
        } catch (Exception e) {
            log.error("Failed to create Spark cluster: {}", clusterId, e);
            cluster.setStatus(SparkCluster.ClusterStatus.ERROR);
            throw new RuntimeException("Failed to create Spark cluster", e);
        }
        
        return clusterRepository.save(cluster);
    }
    
    @Transactional
    public void terminateCluster(String clusterId) {
        log.info("Terminating Spark cluster: {}", clusterId);
        
        SparkCluster cluster = clusterRepository.findByClusterId(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found: " + clusterId));
        
        cluster.setStatus(SparkCluster.ClusterStatus.TERMINATING);
        clusterRepository.save(cluster);
        
        try {
            k8sManager.deleteSparkCluster(clusterId, cluster.getTenant().getTenantId());
            
            cluster.setStatus(SparkCluster.ClusterStatus.TERMINATED);
            cluster.setTerminatedAt(LocalDateTime.now());
            clusterRepository.save(cluster);
            
            log.info("Spark cluster terminated: {}", clusterId);
            
        } catch (Exception e) {
            log.error("Failed to terminate Spark cluster: {}", clusterId, e);
            cluster.setStatus(SparkCluster.ClusterStatus.ERROR);
            clusterRepository.save(cluster);
            throw new RuntimeException("Failed to terminate Spark cluster", e);
        }
    }
    
    public List<SparkCluster> getClustersByTenant(String tenantId) {
        return clusterRepository.findByTenant_TenantId(tenantId);
    }
    
    public SparkCluster getCluster(String clusterId) {
        return clusterRepository.findByClusterId(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found: " + clusterId));
    }
    
    @Transactional
    public void updateClusterActivity(String clusterId) {
        SparkCluster cluster = clusterRepository.findByClusterId(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found: " + clusterId));
        cluster.setLastActivityAt(LocalDateTime.now());
        clusterRepository.save(cluster);
    }
    
    /**
     * Auto-terminate idle clusters every 5 minutes
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
    @Transactional
    public void autoTerminateIdleClusters() {
        log.debug("Checking for idle clusters to auto-terminate");
        
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(60);
        List<SparkCluster> idleClusters = clusterRepository.findIdleClusters(threshold);
        
        for (SparkCluster cluster : idleClusters) {
            if (cluster.getAutoTerminateMinutes() != null && 
                cluster.getAutoTerminateMinutes() > 0) {
                
                LocalDateTime idleThreshold = LocalDateTime.now()
                        .minusMinutes(cluster.getAutoTerminateMinutes());
                
                if (cluster.getLastActivityAt().isBefore(idleThreshold)) {
                    log.info("Auto-terminating idle cluster: {}", cluster.getClusterId());
                    try {
                        terminateCluster(cluster.getClusterId());
                    } catch (Exception e) {
                        log.error("Failed to auto-terminate cluster: {}", cluster.getClusterId(), e);
                    }
                }
            }
        }
    }
}

