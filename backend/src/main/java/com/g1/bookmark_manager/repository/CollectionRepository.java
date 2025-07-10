package com.g1.bookmark_manager.repository;

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
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    
    // Basic queries
    List<Collection> findByUser(User user);
    Page<Collection> findByUser(User user, Pageable pageable);
    
    // Find by user and ID (for security check)
    Optional<Collection> findByIdAndUser(Long id, User user);
    
    // Find by name (for duplicate check)
    Optional<Collection> findByUserAndName(User user, String name);
    boolean existsByUserAndName(User user, String name);
    
    // Public collections
    Page<Collection> findByIsPublicTrue(Pageable pageable);
    List<Collection> findByIsPublicTrue();
    
    // Search collections
    @Query("SELECT c FROM Collection c WHERE c.user = :user AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Collection> searchCollections(@Param("user") User user, @Param("keyword") String keyword);
    
    // Order by display order and creation date
    List<Collection> findByUserOrderByDisplayOrderAscCreatedAtAsc(User user);
    
    // Count queries
    long countByUser(User user);
}
