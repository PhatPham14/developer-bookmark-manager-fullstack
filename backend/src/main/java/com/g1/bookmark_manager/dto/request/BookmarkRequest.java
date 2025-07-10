package com.g1.bookmark_manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Data
public class BookmarkRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;
    
    @NotBlank(message = "URL is required")
    @URL(message = "Invalid URL format")
    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    private String url;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private List<String> tags;
    
    private Long collectionId;
    
    private Boolean isPublic = false;
    
    private Boolean isFavorite = false;
    
    @Size(max = 2048, message = "Favicon URL must not exceed 2048 characters")
    private String favicon;
}
