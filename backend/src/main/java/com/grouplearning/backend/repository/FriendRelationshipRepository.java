package com.grouplearning.backend.repository;

import com.grouplearning.backend.entity.FriendRelationship;
import com.grouplearning.backend.entity.FriendshipStatus;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface FriendRelationshipRepository
        extends JpaRepository<FriendRelationship, UUID> {

    @Query("""
            SELECT CASE
                WHEN COUNT(fr) > 0 THEN true
                ELSE false
            END
            FROM FriendRelationship fr
            WHERE
                (
                    fr.requester.id = :userA
                    AND fr.addressee.id = :userB
                )
                OR
                (
                    fr.requester.id = :userB
                    AND fr.addressee.id = :userA
                )
            """)
    boolean existsBetweenUsers(
            @Param("userA") UUID userA,
            @Param("userB") UUID userB
    );

    @EntityGraph(
            attributePaths = {
                    "requester",
                    "addressee"
            }
    )
    @Query("""
            SELECT fr
            FROM FriendRelationship fr
            WHERE fr.id = :relationshipId
            """)
    Optional<FriendRelationship> findDetailedById(
            @Param("relationshipId")
            UUID relationshipId
    );

    @Query("""
            SELECT COUNT(fr)
            FROM FriendRelationship fr
            WHERE fr.status = :status
              AND (
                    fr.requester.id = :userId
                    OR fr.addressee.id = :userId
                  )
            """)
    long countByUserAndStatus(
            @Param("userId") UUID userId,
            @Param("status") FriendshipStatus status
    );

    @EntityGraph(
            attributePaths = {
                    "requester",
                    "addressee"
            }
    )
    @Query("""
        SELECT fr
        FROM FriendRelationship fr
        WHERE fr.addressee.id = :userId
          AND fr.status = :status
        ORDER BY fr.createdAt DESC
        """)
    List<FriendRelationship> findIncomingRequests(
            @Param("userId") UUID userId,
            @Param("status") FriendshipStatus status
    );

    @EntityGraph(
            attributePaths = {
                    "requester",
                    "addressee"
            }
    )
    @Query("""
        SELECT fr
        FROM FriendRelationship fr
        WHERE fr.requester.id = :userId
          AND fr.status = :status
        ORDER BY fr.createdAt DESC
        """)
    List<FriendRelationship> findOutgoingRequests(
            @Param("userId") UUID userId,
            @Param("status") FriendshipStatus status
    );

    @EntityGraph(
            attributePaths = {
                    "requester",
                    "addressee"
            }
    )
    @Query("""
        SELECT fr
        FROM FriendRelationship fr
        WHERE fr.status = :status
          AND (
                fr.requester.id = :userId
                OR fr.addressee.id = :userId
              )
        ORDER BY fr.updatedAt DESC
        """)
    List<FriendRelationship> findByUserAndStatus(
            @Param("userId") UUID userId,
            @Param("status") FriendshipStatus status
    );

    @EntityGraph(
            attributePaths = {
                    "requester",
                    "addressee"
            }
    )
    @Query("""
        SELECT fr
        FROM FriendRelationship fr
        WHERE fr.status = :status
          AND (
                (
                    fr.requester.id = :userA
                    AND fr.addressee.id = :userB
                )
                OR
                (
                    fr.requester.id = :userB
                    AND fr.addressee.id = :userA
                )
              )
        """)
    Optional<FriendRelationship> findBetweenUsersByStatus(
            @Param("userA") UUID userA,
            @Param("userB") UUID userB,
            @Param("status") FriendshipStatus status
    );
}