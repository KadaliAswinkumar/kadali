package com.kadali.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ClusterCreateRequest {
    
    @NotBlank(message = "Cluster name is required")
    private String name;
    
    @NotBlank(message = "Cluster type is required")
    @Pattern(regexp = "INTERACTIVE|JOB|ML", message = "Type must be INTERACTIVE, JOB, or ML")
    private String type;
    
    private String driverMemory = "2g";
    
    @Min(value = 1, message = "Driver cores must be at least 1")
    private int driverCores = 1;
    
    private String executorMemory = "2g";
    
    @Min(value = 1, message = "Executor cores must be at least 1")
    private int executorCores = 1;
    
    @Min(value = 1, message = "Executor count must be at least 1")
    private int executorCount = 2;
}

