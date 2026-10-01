package com.grouplearning.backend.repository;

import com.grouplearning.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    @Query("""
        SELECT u
        FROM User u
        WHERE u.id <> :currentUserId
          AND (
                LOWER(u.username)
                    LIKE LOWER(CONCAT('%', :query, '%'))
                OR
                LOWER(u.displayName)
                    LIKE LOWER(CONCAT('%', :query, '%'))
              )
        """)
    Page<User> searchUsers(
            @Param("currentUserId") UUID currentUserId,
            @Param("query") String query,
            Pageable pageable
    );
}