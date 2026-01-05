package com.kadali.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterResponse {
    private String clusterId;
    private String name;
    private String type;
    private String status;
    private String driverMemory;
    private Integer driverCores;
    private String executorMemory;
    private Integer executorCores;
    private Integer executorCount;
    private String sparkUiUrl;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime lastActivityAt;
}

