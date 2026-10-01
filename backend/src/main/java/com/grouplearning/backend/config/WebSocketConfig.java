package com.grouplearning.backend.config;

import com.grouplearning.backend.repository.GroupMemberRepository;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.UUID;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig
        implements WebSocketMessageBrokerConfigurer {

    private static final String GROUP_TOPIC_PREFIX =
            "/topic/groups/";

    private final JwtDecoder jwtDecoder;
    private final GroupMemberRepository groupMemberRepository;

    public WebSocketConfig(
            JwtDecoder jwtDecoder,
            GroupMemberRepository groupMemberRepository
    ) {
        this.jwtDecoder = jwtDecoder;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry
    ) {
        registry
                .addEndpoint("/ws")
                .setAllowedOrigins(
                        "http://localhost:5173"
                );
    }

    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry registry
    ) {
        registry.enableSimpleBroker(
                "/topic"
        );

        registry.setApplicationDestinationPrefixes(
                "/app"
        );
    }

    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration
    ) {
        registration.interceptors(
                new ChannelInterceptor() {

                    @Override
                    public Message<?> preSend(
                            Message<?> message,
                            MessageChannel channel
                    ) {
                        StompHeaderAccessor accessor =
                                MessageHeaderAccessor.getAccessor(
                                        message,
                                        StompHeaderAccessor.class
                                );

                        if (accessor == null) {
                            return message;
                        }

                        StompCommand command =
                                accessor.getCommand();

                        if (StompCommand.CONNECT.equals(command)) {
                            authenticateConnect(accessor);
                        }

                        if (StompCommand.SUBSCRIBE.equals(command)) {
                            authorizeSubscription(accessor);
                        }

                        return message;
                    }
                }
        );
    }

    private void authenticateConnect(
            StompHeaderAccessor accessor
    ) {
        String authorization =
                accessor.getFirstNativeHeader(
                        HttpHeaders.AUTHORIZATION
                );

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            throw new MessagingException(
                    "Missing WebSocket authentication token"
            );
        }

        String token =
                authorization.substring(7);

        try {
            Jwt jwt =
                    jwtDecoder.decode(token);

            JwtAuthenticationToken authentication =
                    new JwtAuthenticationToken(jwt);

            accessor.setUser(authentication);

        } catch (JwtException exception) {
            throw new MessagingException(
                    "Invalid WebSocket authentication token",
                    exception
            );
        }
    }

    private void authorizeSubscription(
            StompHeaderAccessor accessor
    ) {
        Principal principal =
                accessor.getUser();

        if (!(principal
                instanceof JwtAuthenticationToken authentication)) {

            throw new MessagingException(
                    "Unauthenticated WebSocket subscription"
            );
        }

        String destination =
                accessor.getDestination();

        if (destination == null
                || !destination.startsWith(
                GROUP_TOPIC_PREFIX
        )) {

            return;
        }

        UUID groupId =
                extractGroupId(destination);

        UUID userId;

        try {
            userId =
                    UUID.fromString(
                            authentication
                                    .getToken()
                                    .getSubject()
                    );
        } catch (IllegalArgumentException exception) {
            throw new MessagingException(
                    "Invalid authenticated user identifier"
            );
        }

        boolean isMember =
                groupMemberRepository
                        .existsByGroup_IdAndUser_Id(
                                groupId,
                                userId
                        );

        if (!isMember) {
            throw new MessagingException(
                    "You must be a member of this study group to subscribe to chat"
            );
        }
    }

    private UUID extractGroupId(
            String destination
    ) {
        String remaining =
                destination.substring(
                        GROUP_TOPIC_PREFIX.length()
                );

        int separatorIndex =
                remaining.indexOf('/');

        String groupIdValue =
                separatorIndex >= 0
                        ? remaining.substring(
                        0,
                        separatorIndex
                )
                        : remaining;

        if (groupIdValue.isBlank()) {
            throw new MessagingException(
                    "Missing group identifier"
            );
        }

        try {
            return UUID.fromString(
                    groupIdValue
            );

        } catch (IllegalArgumentException exception) {
            throw new MessagingException(
                    "Invalid group identifier",
                    exception
            );
        }
    }
}