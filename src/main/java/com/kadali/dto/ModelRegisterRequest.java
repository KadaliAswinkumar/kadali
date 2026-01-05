package com.kadali.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class ModelRegisterRequest {
    
    @NotBlank(message = "Model name is required")
    private String name;
    
    @NotBlank(message = "Version is required")
    private String version;
    
    @NotBlank(message = "Framework is required")
    private String framework; // spark_ml, sklearn, tensorflow, pytorch
    
    private String algorithm;
    private Map<String, Object> metrics;
    private Map<String, Object> params;
    private String artifactPath;
}

