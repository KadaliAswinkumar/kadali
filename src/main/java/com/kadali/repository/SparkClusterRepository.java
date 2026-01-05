package com.kadali.repository;

import com.kadali.entity.SparkCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SparkClusterRepository extends JpaRepository<SparkCluster, Long> {
    Optional<SparkCluster> findByClusterId(String clusterId);
    List<SparkCluster> findByTenant_TenantId(String tenantId);
    List<SparkCluster> findByStatus(SparkCluster.ClusterStatus status);
    
    @Query("SELECT c FROM SparkCluster c WHERE c.status = 'RUNNING' AND c.lastActivityAt < :threshold")
    List<SparkCluster> findIdleClusters(LocalDateTime threshold);
}

