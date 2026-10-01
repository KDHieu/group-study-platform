package com.grouplearning.backend.service;

import org.springframework.web.multipart.MultipartFile;
import com.grouplearning.backend.dto.request.UpdateProfileRequest;
import com.grouplearning.backend.dto.response.UserProfileResponse;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.NotFoundException;
import com.grouplearning.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.grouplearning.backend.dto.response.UserSearchResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final ObjectStorageService objectStorageService;

    public UserProfileService(
            UserRepository userRepository,
            ObjectStorageService objectStorageService
    ) {
        this.userRepository = userRepository;
        this.objectStorageService = objectStorageService;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        User user = findUser(userId);
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(
            UUID userId,
            UpdateProfileRequest request
    ) {
        User user = findUser(userId);

        user.updateProfile(
                request.displayName().trim(),
                normalizeBio(request.bio())
        );

        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateAvatar(
            UUID userId,
            MultipartFile file
    ) {
        User user = findUser(userId);

        String objectName =
                objectStorageService.uploadAvatar(
                        userId,
                        file
                );

        user.updateAvatar(objectName);

        return toResponse(user);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public ObjectStorageService.StoredObject getAvatar(
            UUID userId
    ) {
        User user = findUser(userId);

        if (user.getAvatarUrl() == null
                || user.getAvatarUrl().isBlank()) {
            throw new NotFoundException(
                    "User avatar not found"
            );
        }

        return objectStorageService.getObject(
                user.getAvatarUrl()
        );
    }

    @Transactional(readOnly = true)
    public Page<UserSearchResponse> searchUsers(
            UUID currentUserId,
            String query,
            Pageable pageable
    ) {
        String normalizedQuery =
                query == null
                        ? ""
                        : query.trim();

        return userRepository
                .searchUsers(
                        currentUserId,
                        normalizedQuery,
                        pageable
                )
                .map(this::toSearchResponse);
    }

    private String normalizeBio(String bio) {
        if (bio == null) {
            return null;
        }

        String trimmed = bio.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }

    private UserProfileResponse toResponse(User user) {
        String avatarUrl = null;

        if (user.getAvatarUrl() != null
                && !user.getAvatarUrl().isBlank()) {

            avatarUrl =
                    "/api/users/"
                            + user.getId()
                            + "/avatar";
        }

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getBio(),
                avatarUrl
        );
    }

    private UserSearchResponse toSearchResponse(
            User user
    ) {
        String avatarUrl = null;

        if (user.getAvatarUrl() != null
                && !user.getAvatarUrl().isBlank()) {

            avatarUrl =
                    "/api/users/"
                            + user.getId()
                            + "/avatar";
        }

        return new UserSearchResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getBio(),
                avatarUrl
        );
    }
}