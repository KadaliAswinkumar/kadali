package com.kadali.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ModelPredictRequest {
    
    @NotEmpty(message = "Input data is required")
    private List<Map<String, Object>> inputData;
}

