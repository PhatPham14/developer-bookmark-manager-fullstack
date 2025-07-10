package com.g1.bookmark_manager.repository;

import com.g1.bookmark_manager.entity.Bookmark;
import com.g1.bookmark_manager.entity.Collection;
import com.g1.bookmark_manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    
    // Basic CRUD with pagination
    Page<Bookmark> findByUser(User user, Pageable pageable);
    List<Bookmark> findByUser(User user);
    
    // Find by user and ID (for security check)
    Optional<Bookmark> findByIdAndUser(Long id, User user);
    
    // Favorites
    Page<Bookmark> findByUserAndIsFavoriteTrue(User user, Pageable pageable);
    List<Bookmark> findByUserAndIsFavoriteTrue(User user);
    
    // Collection-based queries
    Page<Bookmark> findByUserAndCollection(User user, Collection collection, Pageable pageable);
    List<Bookmark> findByUserAndCollection(User user, Collection collection);
    
    // Collection is null (uncategorized bookmarks)
    Page<Bookmark> findByUserAndCollectionIsNull(User user, Pageable pageable);
    List<Bookmark> findByUserAndCollectionIsNull(User user);
    
    // Search with pagination
    @Query("SELECT b FROM Bookmark b WHERE b.user = :user AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "EXISTS (SELECT t FROM b.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<Bookmark> searchBookmarks(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT b FROM Bookmark b WHERE b.user = :user AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "EXISTS (SELECT t FROM b.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    List<Bookmark> searchBookmarks(@Param("user") User user, @Param("keyword") String keyword);
    
    // Public bookmarks (for sharing)
    Page<Bookmark> findByIsPublicTrue(Pageable pageable);
    
    // Tags-based queries
    @Query("SELECT DISTINCT t FROM Bookmark b JOIN b.tags t WHERE b.user = :user")
    List<String> findDistinctTagsByUser(@Param("user") User user);
    
    @Query("SELECT b FROM Bookmark b JOIN b.tags t WHERE b.user = :user AND LOWER(t) = LOWER(:tag)")
    Page<Bookmark> findByUserAndTag(@Param("user") User user, @Param("tag") String tag, Pageable pageable);
    
    // Count queries
    long countByUser(User user);
    long countByUserAndIsFavoriteTrue(User user);
    long countByUserAndCollection(User user, Collection collection);
    long countByUserAndCollectionIsNull(User user);
}
