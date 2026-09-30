package com.grouplearning.backend.repository;

import com.grouplearning.backend.entity.StudyGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface StudyGroupRepository
        extends JpaRepository<StudyGroup, UUID> {

    @Override
    @EntityGraph(attributePaths = "owner")
    Page<StudyGroup> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    Page<StudyGroup> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    @Override
    @EntityGraph(attributePaths = "owner")
    Optional<StudyGroup> findById(UUID id);
}