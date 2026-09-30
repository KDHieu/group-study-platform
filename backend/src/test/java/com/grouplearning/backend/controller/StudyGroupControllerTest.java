package com.grouplearning.backend.controller;

import com.grouplearning.backend.dto.request.CreateGroupRequest;
import com.grouplearning.backend.dto.response.GroupResponse;
import com.grouplearning.backend.service.StudyGroupService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class StudyGroupControllerTest {

    private final StudyGroupService studyGroupService =
            mock(StudyGroupService.class);

    private final StudyGroupController controller =
            new StudyGroupController(studyGroupService);

    @Test
    void createGroup_shouldUseUserIdFromJwtSubject() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject(userId.toString())
                .claim("username", "hieu")
                .build();

        CreateGroupRequest request =
                new CreateGroupRequest(
                        "Software Architecture",
                        "Study together"
                );

        GroupResponse expected =
                new GroupResponse(
                        groupId,
                        "Software Architecture",
                        "Study together",
                        userId,
                        "hieu",
                        Instant.now()
                );

        when(studyGroupService.createGroup(
                userId,
                request
        )).thenReturn(expected);

        GroupResponse actual =
                controller.createGroup(jwt, request);

        assertSame(expected, actual);

        verify(studyGroupService).createGroup(
                userId,
                request
        );
    }
}