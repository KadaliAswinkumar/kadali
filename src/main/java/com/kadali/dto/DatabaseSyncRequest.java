package com.kadali.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DatabaseSyncRequest {
    
    @NotBlank(message = "Source type is required (postgres, mysql)")
    private String sourceType;
    
    @NotBlank(message = "JDBC URL is required")
    private String jdbcUrl;
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotBlank(message = "Source table is required")
    private String sourceTable;
    
    @NotBlank(message = "Target database is required")
    private String targetDatabase;
    
    @NotBlank(message = "Target table is required")
    private String targetTable;
}

