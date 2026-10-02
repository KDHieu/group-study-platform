package com.grouplearning.backend.service;

import io.minio.GetObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.RemoveObjectArgs;
import java.util.Set;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class ObjectStorageService {

    private final MinioClient minioClient;
    private final String bucket;
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;
    private static final long MAX_AUDIO_SIZE = 10 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public ObjectStorageService(
            MinioClient minioClient,
            @Value("${app.storage.bucket}") String bucket
    ) {
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    public record StoredObject(
            byte[] content,
            String contentType
    ) {
    }

    public String uploadAvatar(
            UUID userId,
            MultipartFile file
    ) {
        validateAvatar(file);
        try {
            ensureBucketExists();

            String extension =
                    getExtension(file.getOriginalFilename());

            String objectName =
                    "avatars/"
                            + userId
                            + "/"
                            + UUID.randomUUID()
                            + extension;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(
                                    file.getInputStream(),
                                    file.getSize(),
                                    -1
                            )
                            .contentType(file.getContentType())
                            .build()
            );

            return objectName;

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to upload avatar",
                    exception
            );
        }
    }

    private synchronized void ensureBucketExists()
            throws Exception {

        boolean exists =
                minioClient.bucketExists(
                        BucketExistsArgs.builder()
                                .bucket(bucket)
                                .build()
                );

        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucket)
                            .build()
            );
        }
    }

    private String getExtension(String filename) {
        if (filename == null) {
            return "";
        }

        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex < 0) {
            return "";
        }

        return filename.substring(dotIndex);
    }

    private void validateAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Avatar file must not be empty"
            );
        }

        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new IllegalArgumentException(
                    "Avatar file size must not exceed 5 MB"
            );
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Avatar must be JPEG, PNG, or WebP"
            );
        }
    }

    public StoredObject getObject(String objectName) {
        try {
            var metadata = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );

            try (var stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            )) {
                return new StoredObject(
                        stream.readAllBytes(),
                        metadata.contentType()
                );
            }

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to retrieve object from storage",
                    exception
            );
        }


    }

    public String uploadAudio(
            UUID userId,
            MultipartFile file
    ) {
        validateAudio(file);

        try {
            ensureBucketExists();

            String extension =
                    resolveAudioExtension(
                            file.getContentType()
                    );

            String objectKey =
                    "audio/"
                            + userId
                            + "/"
                            + UUID.randomUUID()
                            + extension;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(
                                    file.getInputStream(),
                                    file.getSize(),
                                    -1
                            )
                            .contentType(
                                    file.getContentType()
                            )
                            .build()
            );

            return objectKey;

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not upload audio file",
                    exception
            );
        }
    }

    private void validateAudio(
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Audio file must not be empty"
            );
        }

        if (file.getSize() > MAX_AUDIO_SIZE) {
            throw new IllegalArgumentException(
                    "Audio file must not exceed 10 MB"
            );
        }

        String contentType =
                normalizeContentType(
                        file.getContentType()
                );

        if (contentType == null) {
            throw new IllegalArgumentException(
                    "Audio content type is required"
            );
        }

        boolean supported =
                contentType.equals("audio/webm")
                        || contentType.equals("audio/ogg")
                        || contentType.equals("audio/mpeg")
                        || contentType.equals("audio/mp4");

        if (!supported) {
            throw new IllegalArgumentException(
                    "Unsupported audio format"
            );
        }
    }

    private String resolveAudioExtension(
            String contentType
    ) {
        String normalized =
                normalizeContentType(
                        contentType
                );

        return switch (normalized) {
            case "audio/webm" -> ".webm";
            case "audio/ogg" -> ".ogg";
            case "audio/mpeg" -> ".mp3";
            case "audio/mp4" -> ".m4a";
            default ->
                    throw new IllegalArgumentException(
                            "Unsupported audio format"
                    );
        };
    }

    private String normalizeContentType(
            String contentType
    ) {
        if (contentType == null) {
            return null;
        }

        int separatorIndex =
                contentType.indexOf(';');

        if (separatorIndex >= 0) {
            return contentType
                    .substring(
                            0,
                            separatorIndex
                    )
                    .trim()
                    .toLowerCase();
        }

        return contentType
                .trim()
                .toLowerCase();
    }

    public void deleteObject(
            String objectName
    ) {
        if (objectName == null
                || objectName.isBlank()) {
            return;
        }

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to delete object from storage",
                    exception
            );
        }
    }
}