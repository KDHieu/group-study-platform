package com.grouplearning.backend.controller;

import com.grouplearning.backend.service.GroupMembershipService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.grouplearning.backend.dto.response.GroupMemberResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
public class GroupMembershipController {

    private final GroupMembershipService groupMembershipService;

    public GroupMembershipController(
            GroupMembershipService groupMembershipService
    ) {
        this.groupMembershipService = groupMembershipService;
    }

    @PostMapping("/{groupId}/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void joinGroup(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID currentUserId =
                UUID.fromString(jwt.getSubject());

        groupMembershipService.joinGroup(
                groupId,
                currentUserId
        );
    }

    @DeleteMapping("/{groupId}/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveGroup(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID currentUserId =
                UUID.fromString(jwt.getSubject());

        groupMembershipService.leaveGroup(
                groupId,
                currentUserId
        );
    }

    @GetMapping("/{groupId}/members")
    public List<GroupMemberResponse> getMembers(
            @PathVariable UUID groupId
    ) {
        return groupMembershipService.getMembers(groupId);
    }
}