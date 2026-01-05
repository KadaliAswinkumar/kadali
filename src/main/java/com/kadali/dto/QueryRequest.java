package com.kadali.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QueryRequest {
    
    @NotBlank(message = "SQL query is required")
    private String sql;
    
    @Min(value = 1, message = "Limit must be at least 1")
    private int limit = 1000;
}

