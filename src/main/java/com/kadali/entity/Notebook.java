package com.kadali.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notebooks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notebook {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String notebookId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
    
    @Column(nullable = false)
    private String name;
    
    private String path;
    
    @Column(nullable = false)
    @Builder.Default
    private String kernelType = "pyspark";
    
    @Column(nullable = false)
    @Builder.Default
    private String language = "python";
    
    @Column(columnDefinition = "TEXT")
    private String content; // JSON format
    
    @Builder.Default
    private Integer version = 1;
    
    // Execution
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id")
    private SparkCluster cluster;
    
    private LocalDateTime lastExecutedAt;
    
    @Builder.Default
    private Integer executionCount = 0;
    
    // Collaboration
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;
    
    @Builder.Default
    private Boolean isPublic = false;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

