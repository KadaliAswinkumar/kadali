package com.kadali.controller;

import com.kadali.dto.ClusterCreateRequest;
import com.kadali.dto.ClusterResponse;
import com.kadali.entity.SparkCluster;
import com.kadali.service.SparkClusterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/clusters")
@RequiredArgsConstructor
@Slf4j
public class ClusterController {
    
    private final SparkClusterService clusterService;
    
    @PostMapping
    public ResponseEntity<ClusterResponse> createCluster(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody ClusterCreateRequest request) {
        
        log.info("Creating cluster for tenant: {}", tenantId);
        
        SparkCluster cluster = clusterService.createCluster(
                tenantId,
                request.getName(),
                SparkCluster.ClusterType.valueOf(request.getType().toUpperCase()),
                request.getDriverMemory(),
                request.getDriverCores(),
                request.getExecutorMemory(),
                request.getExecutorCores(),
                request.getExecutorCount()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(cluster));
    }
    
    @GetMapping
    public ResponseEntity<List<ClusterResponse>> listClusters(
            @RequestHeader("X-Tenant-ID") String tenantId) {
        
        List<SparkCluster> clusters = clusterService.getClustersByTenant(tenantId);
        List<ClusterResponse> response = clusters.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{clusterId}")
    public ResponseEntity<ClusterResponse> getCluster(
            @PathVariable String clusterId) {
        
        SparkCluster cluster = clusterService.getCluster(clusterId);
        return ResponseEntity.ok(toResponse(cluster));
    }
    
    @DeleteMapping("/{clusterId}")
    public ResponseEntity<Void> terminateCluster(
            @PathVariable String clusterId) {
        
        log.info("Terminating cluster: {}", clusterId);
        clusterService.terminateCluster(clusterId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{clusterId}/activity")
    public ResponseEntity<Void> updateActivity(
            @PathVariable String clusterId) {
        
        clusterService.updateClusterActivity(clusterId);
        return ResponseEntity.ok().build();
    }
    
    private ClusterResponse toResponse(SparkCluster cluster) {
        return ClusterResponse.builder()
                .clusterId(cluster.getClusterId())
                .name(cluster.getName())
                .type(cluster.getClusterType().name())
                .status(cluster.getStatus().name())
                .driverMemory(cluster.getDriverMemory())
                .driverCores(cluster.getDriverCores())
                .executorMemory(cluster.getExecutorMemory())
                .executorCores(cluster.getExecutorCores())
                .executorCount(cluster.getExecutorCount())
                .sparkUiUrl(cluster.getSparkUiUrl())
                .createdAt(cluster.getCreatedAt())
                .startedAt(cluster.getStartedAt())
                .lastActivityAt(cluster.getLastActivityAt())
                .build();
    }
}

