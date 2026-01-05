package com.kadali.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class NotebookCreateRequest {
    
    @NotBlank(message = "Notebook name is required")
    private String name;
    
    @NotBlank(message = "Language is required")
    @Pattern(regexp = "python|sql|scala|r", message = "Language must be python, sql, scala, or r")
    private String language = "python";
}

