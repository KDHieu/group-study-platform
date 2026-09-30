package com.grouplearning.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.grouplearning.backend.dto.request.CreateGroupRequest;
import com.grouplearning.backend.dto.response.GroupResponse;
import com.grouplearning.backend.service.StudyGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    public StudyGroupController(
            StudyGroupService studyGroupService
    ) {
        this.studyGroupService = studyGroupService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse createGroup(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateGroupRequest request
    ) {
        UUID currentUserId =
                UUID.fromString(jwt.getSubject());

        return studyGroupService.createGroup(
                currentUserId,
                request
        );
    }

    @GetMapping
    public Page<GroupResponse> getGroups(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        return studyGroupService.getGroups(
                search,
                pageable
        );
    }

    @GetMapping("/{groupId}")
    public GroupResponse getGroupById(
            @PathVariable UUID groupId
    ) {
        return studyGroupService.getGroupById(groupId);
    }

    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID currentUserId =
                UUID.fromString(jwt.getSubject());

        studyGroupService.deleteGroup(
                groupId,
                currentUserId
        );
    }
}