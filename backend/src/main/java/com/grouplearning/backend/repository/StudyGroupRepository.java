package com.grouplearning.backend.repository;

import com.grouplearning.backend.entity.GroupVisibility;
import com.grouplearning.backend.entity.StudyGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StudyGroupRepository
        extends JpaRepository<StudyGroup, UUID> {

    @Override
    @EntityGraph(attributePaths = "owner")
    Page<StudyGroup> findAll(
            Pageable pageable
    );

    @EntityGraph(attributePaths = "owner")
    Page<StudyGroup> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    /**
     * Used when no search term is provided.
     *
     * In the current discovery flow this is called with PUBLIC,
     * so private groups are never recommended automatically.
     */
    @EntityGraph(attributePaths = "owner")
    Page<StudyGroup> findByVisibility(
            GroupVisibility visibility,
            Pageable pageable
    );

    /**
     * Discovery search rules:
     *
     * PUBLIC:
     * - partial name matching is allowed.
     *
     * PRIVATE:
     * - only an exact name match is returned.
     * - matching is case-insensitive.
     *
     * The visibility argument is PUBLIC in the current
     * StudyGroupService discovery flow.
     */
    @EntityGraph(attributePaths = "owner")
    @Query("""
            SELECT g
            FROM StudyGroup g
            WHERE
                (
                    g.visibility = :visibility
                    AND LOWER(g.name) LIKE
                        LOWER(CONCAT('%', :name, '%'))
                )
                OR
                (
                    g.visibility =
                        com.grouplearning.backend.entity.GroupVisibility.PRIVATE
                    AND LOWER(g.name) = LOWER(:name)
                )
            """)
    Page<StudyGroup>
    findByVisibilityAndNameContainingIgnoreCase(
            @Param("visibility")
            GroupVisibility visibility,

            @Param("name")
            String name,

            Pageable pageable
    );

    @Override
    @EntityGraph(attributePaths = "owner")
    Optional<StudyGroup> findById(
            UUID id
    );
}