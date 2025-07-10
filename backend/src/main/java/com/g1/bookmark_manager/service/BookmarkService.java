package com.g1.bookmark_manager.service;

import com.g1.bookmark_manager.dto.request.BookmarkRequest;
import com.g1.bookmark_manager.dto.response.BookmarkResponse;
import com.g1.bookmark_manager.entity.Bookmark;
import com.g1.bookmark_manager.entity.Collection;
import com.g1.bookmark_manager.entity.User;
import com.g1.bookmark_manager.exception.BadRequestException;
import com.g1.bookmark_manager.exception.ResourceNotFoundException;
import com.g1.bookmark_manager.exception.DuplicateResourceException;
import com.g1.bookmark_manager.repository.BookmarkRepository;
import com.g1.bookmark_manager.repository.CollectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final CollectionRepository collectionRepository;
    private final AuthService authService;

    // Constants for validation
    private static final int MAX_TAGS_PER_BOOKMARK = 10;
    private static final int MAX_TAG_LENGTH = 50;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Get all bookmarks for a user with pagination
     */
    @Transactional(readOnly = true)
    public Page<BookmarkResponse> getAllBookmarks(String username, int page, int size, String sortBy, String sortDir) {
        User user = findUserByUsername(username);
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        
        Page<Bookmark> bookmarks = bookmarkRepository.findByUser(user, pageable);
        return bookmarks.map(this::convertToResponse);
    }

    /**
     * Get all bookmarks for a user (without pagination)
     */
    @Transactional(readOnly = true)
    public List<BookmarkResponse> getAllBookmarks(String username) {
        User user = findUserByUsername(username);
        List<Bookmark> bookmarks = bookmarkRepository.findByUser(user);
        return bookmarks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get bookmark by ID with security check
     */
    @Transactional(readOnly = true)
    public BookmarkResponse getBookmarkById(Long id, String username) {
        User user = findUserByUsername(username);
        Bookmark bookmark = findBookmarkByIdAndUser(id, user);
        
        // Increment click count and update last accessed
        bookmark.incrementClickCount();
        bookmarkRepository.save(bookmark);
        
        return convertToResponse(bookmark);
    }

    /**
     * Create a new bookmark with validation
     */
    public BookmarkResponse createBookmark(BookmarkRequest request, String username) {
        User user = findUserByUsername(username);
        
        // Validate the request
        validateBookmarkRequest(request, user);
        
        // Check for duplicate URL
        checkForDuplicateUrl(request.getUrl(), user, null);
        
        // Get collection if specified
        Collection collection = null;
        if (request.getCollectionId() != null) {
            collection = findCollectionByIdAndUser(request.getCollectionId(), user);
        }
        
        // Create bookmark
        Bookmark bookmark = new Bookmark();
        mapRequestToBookmark(request, bookmark, user, collection);
        
        bookmark = bookmarkRepository.save(bookmark);
        log.info("Created bookmark with ID: {} for user: {}", bookmark.getId(), username);
        
        return convertToResponse(bookmark);
    }

    /**
     * Update an existing bookmark
     */
    public BookmarkResponse updateBookmark(Long id, BookmarkRequest request, String username) {
        User user = findUserByUsername(username);
        Bookmark bookmark = findBookmarkByIdAndUser(id, user);
        
        // Validate the request
        validateBookmarkRequest(request, user);
        
        // Check for duplicate URL (excluding current bookmark)
        checkForDuplicateUrl(request.getUrl(), user, id);
        
        // Get collection if specified
        Collection collection = null;
        if (request.getCollectionId() != null) {
            collection = findCollectionByIdAndUser(request.getCollectionId(), user);
        }
        
        // Update bookmark
        mapRequestToBookmark(request, bookmark, user, collection);
        
        bookmark = bookmarkRepository.save(bookmark);
        log.info("Updated bookmark with ID: {} for user: {}", bookmark.getId(), username);
        
        return convertToResponse(bookmark);
    }

    /**
     * Delete a bookmark
     */
    public void deleteBookmark(Long id, String username) {
        User user = findUserByUsername(username);
        Bookmark bookmark = findBookmarkByIdAndUser(id, user);
        
        bookmarkRepository.delete(bookmark);
        log.info("Deleted bookmark with ID: {} for user: {}", id, username);
    }

    /**
     * Get favorite bookmarks with pagination
     */
    @Transactional(readOnly = true)
    public Page<BookmarkResponse> getFavoriteBookmarks(String username, int page, int size, String sortBy, String sortDir) {
        User user = findUserByUsername(username);
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        
        Page<Bookmark> bookmarks = bookmarkRepository.findByUserAndIsFavoriteTrue(user, pageable);
        return bookmarks.map(this::convertToResponse);
    }

    /**
     * Get bookmarks by collection with pagination
     */
    @Transactional(readOnly = true)
    public Page<BookmarkResponse> getBookmarksByCollection(Long collectionId, String username, int page, int size, String sortBy, String sortDir) {
        User user = findUserByUsername(username);
        Collection collection = findCollectionByIdAndUser(collectionId, user);
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        
        Page<Bookmark> bookmarks = bookmarkRepository.findByUserAndCollection(user, collection, pageable);
        return bookmarks.map(this::convertToResponse);
    }

    /**
     * Get uncategorized bookmarks with pagination
     */
    @Transactional(readOnly = true)
    public Page<BookmarkResponse> getUncategorizedBookmarks(String username, int page, int size, String sortBy, String sortDir) {
        User user = findUserByUsername(username);
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        
        Page<Bookmark> bookmarks = bookmarkRepository.findByUserAndCollectionIsNull(user, pageable);
        return bookmarks.map(this::convertToResponse);
    }

    /**
     * Search bookmarks with pagination
     */
    @Transactional(readOnly = true)
    public Page<BookmarkResponse> searchBookmarks(String keyword, String username, int page, int size, String sortBy, String sortDir) {
        if (!StringUtils.hasText(keyword)) {
            throw new BadRequestException("Search keyword cannot be empty");
        }
        
        User user = findUserByUsername(username);
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        
        Page<Bookmark> bookmarks = bookmarkRepository.searchBookmarks(user, keyword.trim(), pageable);
        return bookmarks.map(this::convertToResponse);
    }

    /**
     * Get bookmarks by tag with pagination
     */
    @Transactional(readOnly = true)
    public Page<BookmarkResponse> getBookmarksByTag(String tag, String username, int page, int size, String sortBy, String sortDir) {
        if (!StringUtils.hasText(tag)) {
            throw new BadRequestException("Tag cannot be empty");
        }
        
        User user = findUserByUsername(username);
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        
        Page<Bookmark> bookmarks = bookmarkRepository.findByUserAndTag(user, tag.trim(), pageable);
        return bookmarks.map(this::convertToResponse);
    }

    /**
     * Get all tags for a user
     */
    @Transactional(readOnly = true)
    public List<String> getTags(String username) {
        User user = findUserByUsername(username);
        return bookmarkRepository.findDistinctTagsByUser(user);
    }

    /**
     * Toggle favorite status of a bookmark
     */
    public BookmarkResponse toggleFavorite(Long id, String username) {
        User user = findUserByUsername(username);
        Bookmark bookmark = findBookmarkByIdAndUser(id, user);
        
        bookmark.toggleFavorite();
        bookmark = bookmarkRepository.save(bookmark);
        
        log.info("Toggled favorite status for bookmark ID: {} to: {}", id, bookmark.getIsFavorite());
        return convertToResponse(bookmark);
    }

    /**
     * Get bookmark statistics for a user
     */
    @Transactional(readOnly = true)
    public BookmarkStats getBookmarkStats(String username) {
        User user = findUserByUsername(username);
        
        long totalBookmarks = bookmarkRepository.countByUser(user);
        long favoriteBookmarks = bookmarkRepository.countByUserAndIsFavoriteTrue(user);
        long uncategorizedBookmarks = bookmarkRepository.countByUserAndCollectionIsNull(user);
        
        return new BookmarkStats(totalBookmarks, favoriteBookmarks, uncategorizedBookmarks);
    }

    // Helper methods
    
    private User findUserByUsername(String username) {
        return authService.findByUsername(username);
    }
    
    private Bookmark findBookmarkByIdAndUser(Long id, User user) {
        return bookmarkRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found with id: " + id));
    }
    
    private Collection findCollectionByIdAndUser(Long collectionId, User user) {
        return collectionRepository.findByIdAndUser(collectionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with id: " + collectionId));
    }
    
    private void validateBookmarkRequest(BookmarkRequest request, User user) {
        // Validate URL format
        validateUrl(request.getUrl());
        
        // Validate tags
        validateTags(request.getTags());
        
        // Validate title uniqueness within user's bookmarks
        // Note: This is optional and might be too restrictive
        // validateTitleUniqueness(request.getTitle(), user, bookmarkId);
    }
    
    private void validateUrl(String url) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new BadRequestException("Invalid URL format: " + url);
        }
    }
    
    private void validateTags(List<String> tags) {
        if (tags == null) {
            return;
        }
        
        if (tags.size() > MAX_TAGS_PER_BOOKMARK) {
            throw new BadRequestException("Maximum " + MAX_TAGS_PER_BOOKMARK + " tags allowed per bookmark");
        }
        
        for (String tag : tags) {
            if (!StringUtils.hasText(tag)) {
                throw new BadRequestException("Tags cannot be empty or contain only whitespace");
            }
            
            if (tag.trim().length() > MAX_TAG_LENGTH) {
                throw new BadRequestException("Tag length cannot exceed " + MAX_TAG_LENGTH + " characters");
            }
        }
    }
    
    private void checkForDuplicateUrl(String url, User user, Long excludeBookmarkId) {
        List<Bookmark> existingBookmarks = bookmarkRepository.findByUser(user);
        
        boolean isDuplicate = existingBookmarks.stream()
                .filter(bookmark -> excludeBookmarkId == null || !bookmark.getId().equals(excludeBookmarkId))
                .anyMatch(bookmark -> bookmark.getUrl().equalsIgnoreCase(url));
        
        if (isDuplicate) {
            throw new DuplicateResourceException("A bookmark with this URL already exists");
        }
    }
    
    private void mapRequestToBookmark(BookmarkRequest request, Bookmark bookmark, User user, Collection collection) {
        bookmark.setTitle(request.getTitle().trim());
        bookmark.setUrl(request.getUrl().trim());
        bookmark.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
        bookmark.setTags(request.getTags() != null ? 
                request.getTags().stream().map(String::trim).filter(StringUtils::hasText).collect(Collectors.toList()) : null);
        bookmark.setCollection(collection);
        bookmark.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
        bookmark.setIsFavorite(request.getIsFavorite() != null ? request.getIsFavorite() : false);
        bookmark.setFavicon(StringUtils.hasText(request.getFavicon()) ? request.getFavicon().trim() : null);
        bookmark.setUser(user);
    }
    
    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        // Validate and set default values
        int validatedPage = Math.max(0, page);
        int validatedSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        if (validatedSize <= 0) {
            validatedSize = DEFAULT_PAGE_SIZE;
        }
        
        // Set default sort
        String validatedSortBy = StringUtils.hasText(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        return PageRequest.of(validatedPage, validatedSize, Sort.by(direction, validatedSortBy));
    }
    
    private BookmarkResponse convertToResponse(Bookmark bookmark) {
        BookmarkResponse response = new BookmarkResponse();
        response.setId(bookmark.getId());
        response.setTitle(bookmark.getTitle());
        response.setUrl(bookmark.getUrl());
        response.setDescription(bookmark.getDescription());
        response.setTags(bookmark.getTags());
        response.setIsPublic(bookmark.getIsPublic());
        response.setIsFavorite(bookmark.getIsFavorite());
        response.setFavicon(bookmark.getFavicon());
        response.setClickCount(bookmark.getClickCount());
        response.setLastAccessed(bookmark.getLastAccessed());
        response.setCreatedAt(bookmark.getCreatedAt());
        response.setUpdatedAt(bookmark.getUpdatedAt());
        response.setUsername(bookmark.getUser().getUsername());
        
        // Set collection info if present
        if (bookmark.getCollection() != null) {
            Collection collection = bookmark.getCollection();
            BookmarkResponse.CollectionInfo collectionInfo = new BookmarkResponse.CollectionInfo(
                    collection.getId(),
                    collection.getName(),
                    collection.getDescription(),
                    collection.getColorCode()
            );
            response.setCollection(collectionInfo);
        }
        
        return response;
    }
    
    // Inner class for statistics
    public static class BookmarkStats {
        private final long totalBookmarks;
        private final long favoriteBookmarks;
        private final long uncategorizedBookmarks;
        
        public BookmarkStats(long totalBookmarks, long favoriteBookmarks, long uncategorizedBookmarks) {
            this.totalBookmarks = totalBookmarks;
            this.favoriteBookmarks = favoriteBookmarks;
            this.uncategorizedBookmarks = uncategorizedBookmarks;
        }
        
        public long getTotalBookmarks() { return totalBookmarks; }
        public long getFavoriteBookmarks() { return favoriteBookmarks; }
        public long getUncategorizedBookmarks() { return uncategorizedBookmarks; }
    }
}
