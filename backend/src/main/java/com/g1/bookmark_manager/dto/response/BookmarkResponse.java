package com.g1.bookmark_manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookmarkResponse {
    private Long id;
    private String title;
    private String url;
    private String description;
    private List<String> tags;
    private CollectionInfo collection;
    private Boolean isPublic;
    private Boolean isFavorite;
    private String favicon;
    private Integer clickCount;
    private LocalDateTime lastAccessed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CollectionInfo {
        private Long id;
        private String name;
        private String description;
        private String colorCode;
    }
}
