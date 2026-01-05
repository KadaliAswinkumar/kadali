package com.kadali.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "spark_clusters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SparkCluster {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String clusterId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClusterType clusterType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClusterStatus status;
    
    // Resources
    private String driverMemory;
    private Integer driverCores;
    private String executorMemory;
    private Integer executorCores;
    private Integer executorCount;
    
    // Kubernetes details
    private String namespace;
    private String driverPodName;
    private String sparkUiUrl;
    
    // Metadata
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime startedAt;
    private LocalDateTime terminatedAt;
    private LocalDateTime lastActivityAt;
    
    @Builder.Default
    private Integer autoTerminateMinutes = 60;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActivityAt = LocalDateTime.now();
    }
    
    public enum ClusterType {
        INTERACTIVE, JOB, ML
    }
    
    public enum ClusterStatus {
        CREATING, RUNNING, IDLE, TERMINATING, TERMINATED, ERROR
    }
}

