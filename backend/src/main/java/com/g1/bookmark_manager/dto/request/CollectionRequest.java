package com.g1.bookmark_manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CollectionRequest {
    
    @NotBlank(message = "Collection name is required")
    @Size(max = 100, message = "Collection name must not exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", 
             message = "Color code must be a valid hex color (e.g., #FF5733)")
    private String colorCode;
    
    private Boolean isPublic = false;
    
    private Integer displayOrder;
}
