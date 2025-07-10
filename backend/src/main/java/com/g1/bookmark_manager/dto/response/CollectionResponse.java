package com.g1.bookmark_manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionResponse {
    
    private Long id;
    private String name;
    private String description;
    private String colorCode;
    private Boolean isPublic;
    private Integer displayOrder;
    private Integer bookmarkCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;
}
