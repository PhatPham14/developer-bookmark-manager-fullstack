package com.g1.bookmark_manager.controller;

import com.g1.bookmark_manager.dto.request.BookmarkRequest;
import com.g1.bookmark_manager.dto.response.BookmarkResponse;
import com.g1.bookmark_manager.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@Tag(name = "Bookmarks", description = "Bookmark management API")
@CrossOrigin(origins = "*", maxAge = 3600)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @GetMapping
    @Operation(summary = "Get all bookmarks for the current user with pagination")
    public ResponseEntity<Page<BookmarkResponse>> getAllBookmarks(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        String username = getCurrentUsername();
        Page<BookmarkResponse> bookmarks = bookmarkService.getAllBookmarks(username, page, size, sortBy, sortDir);
        return ResponseEntity.ok(bookmarks);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all bookmarks for the current user (no pagination)")
    public ResponseEntity<List<BookmarkResponse>> getAllBookmarksNoPagination() {
        String username = getCurrentUsername();
        List<BookmarkResponse> bookmarks = bookmarkService.getAllBookmarks(username);
        return ResponseEntity.ok(bookmarks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bookmark by ID")
    public ResponseEntity<BookmarkResponse> getBookmarkById(@PathVariable Long id) {
        String username = getCurrentUsername();
        BookmarkResponse bookmark = bookmarkService.getBookmarkById(id, username);
        return ResponseEntity.ok(bookmark);
    }

    @PostMapping
    @Operation(summary = "Create a new bookmark")
    public ResponseEntity<BookmarkResponse> createBookmark(@Valid @RequestBody BookmarkRequest request) {
        String username = getCurrentUsername();
        BookmarkResponse bookmark = bookmarkService.createBookmark(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookmark);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing bookmark")
    public ResponseEntity<BookmarkResponse> updateBookmark(
            @PathVariable Long id, 
            @Valid @RequestBody BookmarkRequest request) {
        String username = getCurrentUsername();
        BookmarkResponse bookmark = bookmarkService.updateBookmark(id, request, username);
        return ResponseEntity.ok(bookmark);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bookmark")
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long id) {
        String username = getCurrentUsername();
        bookmarkService.deleteBookmark(id, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/favorites")
    @Operation(summary = "Get favorite bookmarks with pagination")
    public ResponseEntity<Page<BookmarkResponse>> getFavoriteBookmarks(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        String username = getCurrentUsername();
        Page<BookmarkResponse> bookmarks = bookmarkService.getFavoriteBookmarks(username, page, size, sortBy, sortDir);
        return ResponseEntity.ok(bookmarks);
    }

    @GetMapping("/collection/{collectionId}")
    @Operation(summary = "Get bookmarks by collection with pagination")
    public ResponseEntity<Page<BookmarkResponse>> getBookmarksByCollection(
            @PathVariable Long collectionId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        String username = getCurrentUsername();
        Page<BookmarkResponse> bookmarks = bookmarkService.getBookmarksByCollection(collectionId, username, page, size, sortBy, sortDir);
        return ResponseEntity.ok(bookmarks);
    }

    @GetMapping("/uncategorized")
    @Operation(summary = "Get uncategorized bookmarks with pagination")
    public ResponseEntity<Page<BookmarkResponse>> getUncategorizedBookmarks(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        String username = getCurrentUsername();
        Page<BookmarkResponse> bookmarks = bookmarkService.getUncategorizedBookmarks(username, page, size, sortBy, sortDir);
        return ResponseEntity.ok(bookmarks);
    }

    @GetMapping("/search")
    @Operation(summary = "Search bookmarks with pagination")
    public ResponseEntity<Page<BookmarkResponse>> searchBookmarks(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        String username = getCurrentUsername();
        Page<BookmarkResponse> bookmarks = bookmarkService.searchBookmarks(keyword, username, page, size, sortBy, sortDir);
        return ResponseEntity.ok(bookmarks);
    }

    @GetMapping("/tag/{tag}")
    @Operation(summary = "Get bookmarks by tag with pagination")
    public ResponseEntity<Page<BookmarkResponse>> getBookmarksByTag(
            @PathVariable String tag,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        String username = getCurrentUsername();
        Page<BookmarkResponse> bookmarks = bookmarkService.getBookmarksByTag(tag, username, page, size, sortBy, sortDir);
        return ResponseEntity.ok(bookmarks);
    }

    @GetMapping("/tags")
    @Operation(summary = "Get all tags for the current user")
    public ResponseEntity<List<String>> getTags() {
        String username = getCurrentUsername();
        List<String> tags = bookmarkService.getTags(username);
        return ResponseEntity.ok(tags);
    }

    @PatchMapping("/{id}/favorite")
    @Operation(summary = "Toggle favorite status of a bookmark")
    public ResponseEntity<BookmarkResponse> toggleFavorite(@PathVariable Long id) {
        String username = getCurrentUsername();
        BookmarkResponse bookmark = bookmarkService.toggleFavorite(id, username);
        return ResponseEntity.ok(bookmark);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get bookmark statistics for the current user")
    public ResponseEntity<BookmarkService.BookmarkStats> getBookmarkStats() {
        String username = getCurrentUsername();
        BookmarkService.BookmarkStats stats = bookmarkService.getBookmarkStats(username);
        return ResponseEntity.ok(stats);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
