package com.kadali.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CellExecutionRequest {
    
    @NotBlank(message = "Code is required")
    private String code;
    
    private String cellType = "code"; // code or markdown
}

