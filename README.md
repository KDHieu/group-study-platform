# 📚 Group Study Platform

Nền tảng học nhóm trực tuyến giúp sinh viên tạo nhóm học tập, giao tiếp real-time qua chat và video call, quản lý bạn bè, nhắn tin trực tiếp, và tham gia thử thách hàng ngày.

---

## 📖 Mục lục

- [Tổng quan](#-tổng-quan)
- [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
- [Tech Stack](#-tech-stack)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Domain Model & Database Schema](#-domain-model--database-schema)
- [Tính năng chi tiết](#-tính-năng-chi-tiết)
- [API Specification](#-api-specification)
- [Giao tiếp Real-time (WebSocket)](#-giao-tiếp-real-time-websocket)
- [Bảo mật](#-bảo-mật)
- [Hướng dẫn cài đặt](#-hướng-dẫn-cài-đặt)
- [Biến môi trường](#-biến-môi-trường)

---

## 🎯 Tổng quan

Group Study Platform là một ứng dụng web full-stack được thiết kế theo kiến trúc **microservices**, phục vụ cho nhu cầu học nhóm trực tuyến với các tính năng chính:

- **Quản lý nhóm học tập** — tạo, tìm kiếm, tham gia, rời nhóm với chế độ công khai/riêng tư
- **Chat nhóm real-time** — tin nhắn văn bản, hình ảnh, ghi âm giọng nói qua WebSocket (STOMP)
- **Video/Audio Call** — phòng gọi trong nhóm thông qua LiveKit SFU
- **Hệ thống bạn bè** — gửi/chấp nhận/từ chối lời mời kết bạn
- **Nhắn tin trực tiếp** — tin nhắn riêng tư giữa hai người dùng qua WebSocket
- **Daily Challenges** — câu hỏi trắc nghiệm hàng ngày để thúc đẩy học tập
- **Hồ sơ người dùng** — avatar, bio, display name

---

## 🏗 Kiến trúc hệ thống

Hệ thống sử dụng kiến trúc **microservices** với mô hình **API Gateway Pattern**, triển khai bằng Docker Compose.

### Sơ đồ kiến trúc tổng quan

```
┌─────────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                               │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │              Frontend (React + Vite + TypeScript)           │   │
│   │                        :5173                                │   │
│   │  ┌──────────┐ ┌──────────┐ ┌────────┐ ┌────────────────┐   │   │
│   │  │  Pages   │ │Components│ │  Auth  │ │  API Layer     │   │   │
│   │  │Dashboard │ │GroupChat │ │Context │ │ Axios + STOMP  │   │   │
│   │  │Groups    │ │VoiceRec. │ │JWT Mgmt│ │                │   │   │
│   │  │Friends   │ │VideoCall │ │        │ │                │   │   │
│   │  │Messages  │ │Sidebar   │ │        │ │                │   │   │
│   │  │Rooms     │ │          │ │        │ │                │   │   │
│   │  │Challenges│ │          │ │        │ │                │   │   │
│   │  │Profile   │ │          │ │        │ │                │   │   │
│   │  └──────────┘ └──────────┘ └────────┘ └────────────────┘   │   │
│   └───────────────────────┬─────────────────────────────────────┘   │
│                           │ HTTP (REST) + WebSocket (STOMP)         │
└───────────────────────────┼─────────────────────────────────────────┘
                            │
┌───────────────────────────┼─────────────────────────────────────────┐
│                     GATEWAY LAYER                                   │
│                           ▼                                         │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │       API Gateway (Spring Cloud Gateway WebFlux)            │   │
│   │                        :8080                                │   │
│   │                                                             │   │
│   │  • CORS handling (allowed-origin: http://localhost:*)       │   │
│   │  • Request routing (/api/** → Backend :8081)                │   │
│   │  • WebSocket proxying (/ws/** → Backend :8081)              │   │
│   └───────────────────────┬─────────────────────────────────────┘   │
│                           │                                         │
└───────────────────────────┼─────────────────────────────────────────┘
                            │
┌───────────────────────────┼─────────────────────────────────────────┐
│                     SERVICE LAYER                                   │
│                           ▼                                         │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │              Backend (Spring Boot 4.1)                      │   │
│   │                        :8081                                │   │
│   │                                                             │   │
│   │  ┌─────────────────────────────────────────────────────┐    │   │
│   │  │                  Controller Layer                   │    │   │
│   │  │  AuthController · StudyGroupController              │    │   │
│   │  │  StudyRoomController · FriendController             │    │   │
│   │  │  UserController · DirectMessageController           │    │   │
│   │  │  DailyChallengeController                           │    │   │
│   │  │  StudyRoomWebSocketController (STOMP)               │    │   │
│   │  └────────────────────┬────────────────────────────────┘    │   │
│   │                       ▼                                     │   │
│   │  ┌─────────────────────────────────────────────────────┐    │   │
│   │  │                   Service Layer                     │    │   │
│   │  │  AuthService · StudyGroupService                    │    │   │
│   │  │  StudyRoomService · FriendService                   │    │   │
│   │  │  UserService · DirectMessageService                 │    │   │
│   │  │  DailyChallengeService · ObjectStorageService       │    │   │
│   │  └────────────────────┬────────────────────────────────┘    │   │
│   │                       ▼                                     │   │
│   │  ┌─────────────────────────────────────────────────────┐    │   │
│   │  │                 Repository Layer (JPA)              │    │   │
│   │  │  UserRepository · StudyGroupRepository              │    │   │
│   │  │  GroupMemberRepository · ChatMessageRepository      │    │   │
│   │  │  FriendRelationshipRepository · CallRoomRepository  │    │   │
│   │  │  DirectMessageRepository · GroupJoinRequestRepository│   │   │
│   │  │  DailyChallengeRepository · DailyChallengeAttempt...│    │   │
│   │  └────────────────────┬────────────────────────────────┘    │   │
│   │                       │                                     │   │
│   └───────────────────────┼─────────────────────────────────────┘   │
│                           │                                         │
└───────────────────────────┼─────────────────────────────────────────┘
                            │
┌───────────────────────────┼─────────────────────────────────────────┐
│                INFRASTRUCTURE LAYER                                 │
│                           ▼                                         │
│   ┌──────────────┐  ┌──────────────┐  ┌─────────────────────┐      │
│   │  PostgreSQL  │  │    MinIO      │  │     LiveKit SFU     │      │
│   │    :5432     │  │  :9000/:9001  │  │   :7880/:7881/:7882 │      │
│   │              │  │              │  │                     │      │
│   │  • Users     │  │  • Avatars   │  │  • WebRTC rooms     │      │
│   │  • Groups    │  │  • Voice msg │  │  • Video/Audio call │      │
│   │  • Messages  │  │  • Images    │  │  • Token auth       │      │
│   │  • Friends   │  │              │  │                     │      │
│   │  • Challenges│  │              │  │                     │      │
│   └──────────────┘  └──────────────┘  └─────────────────────┘      │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Kiến trúc Backend — Layered Architecture

Backend tuân theo kiến trúc **3-layer** (Controller → Service → Repository) của Spring Boot:

| Layer          | Vai trò                                                      | Packages                        |
| -------------- | ------------------------------------------------------------ | ------------------------------- |
| **Controller** | Xử lý HTTP request/response, validation đầu vào, mapping DTO | `controller/`                   |
| **Service**    | Business logic, transaction management, authorization        | `service/`                      |
| **Repository** | Data access, JPA queries, database interaction               | `repository/`                   |
| **Entity**     | Domain model, JPA entity mapping                             | `entity/`                       |
| **DTO**        | Data transfer objects (request/response)                     | `dto/request/`, `dto/response/` |
| **Config**     | Spring configuration beans                                   | `config/`                       |
| **Security**   | Security filter chain, JWT resource server                   | `security/`                     |
| **Exception**  | Global exception handling, custom exceptions                 | `exception/`                    |

### Các Design Pattern được sử dụng

| Pattern                             | Nơi áp dụng                                                       |
| ----------------------------------- | ----------------------------------------------------------------- |
| **API Gateway**                     | Spring Cloud Gateway WebFlux — điểm vào duy nhất, routing, CORS   |
| **Repository Pattern**              | Spring Data JPA repositories trừu tượng hóa truy cập dữ liệu      |
| **DTO Pattern**                     | Tách biệt domain entity và API contract qua request/response DTOs |
| **Stateless Authentication**        | JWT + OAuth2 Resource Server — không lưu session phía server      |
| **Database Migration**              | Flyway — quản lý schema versioning qua migration scripts          |
| **Object Storage**                  | MinIO (S3-compatible) — lưu trữ file media tách biệt khỏi DB      |
| **Message Broker**                  | STOMP over WebSocket — simple in-memory broker cho pub/sub        |
| **SFU (Selective Forwarding Unit)** | LiveKit server — media streaming cho video/audio call             |

---

## 🛠 Tech Stack

### Backend

| Công nghệ                         | Phiên bản       | Mục đích                             |
| --------------------------------- | --------------- | ------------------------------------ |
| **Java**                          | 17              | Ngôn ngữ lập trình                   |
| **Spring Boot**                   | 4.1.1           | Framework chính                      |
| **Spring Data JPA**               | —               | ORM & data access                    |
| **Spring Security**               | —               | Authentication & authorization       |
| **Spring OAuth2 Resource Server** | —               | JWT token validation                 |
| **Spring WebSocket**              | —               | STOMP messaging                      |
| **Spring Validation**             | —               | Request validation (Bean Validation) |
| **Spring Cloud Gateway**          | 2025.1.3        | API Gateway (WebFlux-based)          |
| **PostgreSQL**                    | 16              | Relational database                  |
| **Flyway**                        | —               | Database migration                   |
| **MinIO**                         | 8.5.17 (client) | S3-compatible object storage         |
| **LiveKit Server SDK**            | 0.16.0          | Video/Audio call token generation    |
| **SpringDoc OpenAPI**             | 3.0.2           | Swagger UI & API documentation       |

### Frontend

| Công nghệ         | Phiên bản | Mục đích                    |
| ----------------- | --------- | --------------------------- |
| **React**         | 19.2.8    | UI framework                |
| **TypeScript**    | 6.0.2     | Type-safe JavaScript        |
| **Vite**          | 8.3.0     | Build tool & dev server     |
| **React Router**  | 7.18.4    | Client-side routing         |
| **Axios**         | 1.20.0    | HTTP client                 |
| **STOMP.js**      | 7.3.0     | WebSocket STOMP client      |
| **LiveKit React** | 2.9.24    | Video/Audio call components |
| **Tailwind CSS**  | 4.3.3     | Utility-first CSS           |
| **shadcn/ui**     | 4.21.0    | UI component library        |
| **Lucide React**  | 1.49.0    | Icon library                |
| **Base UI**       | 1.8.0     | Headless UI primitives      |

### Infrastructure

| Công nghệ          | Mục đích                                     |
| ------------------ | -------------------------------------------- |
| **Docker Compose** | Container orchestration cho toàn bộ hệ thống |
| **LiveKit Server** | Self-hosted WebRTC SFU server                |
| **MinIO Server**   | Self-hosted S3-compatible object storage     |

---

## 📂 Cấu trúc dự án

```
group-study-platform/
├── backend/                          # Backend service (Spring Boot)
│   ├── src/main/java/com/grouplearning/backend/
│   │   ├── BackendApplication.java   # Entry point
│   │   ├── config/                   # Configuration classes
│   │   │   ├── JwtConfig.java        #   JWT encoder/decoder beans
│   │   │   ├── LiveKitProperties.java#   LiveKit connection properties
│   │   │   ├── OpenApiConfig.java    #   Swagger/OpenAPI configuration
│   │   │   ├── PasswordConfig.java   #   BCrypt password encoder
│   │   │   ├── SpringDataWebConfig.java # Pageable defaults
│   │   │   ├── StorageConfig.java    #   MinIO client configuration
│   │   │   └── WebSocketConfig.java  #   STOMP broker & auth interceptor
│   │   ├── controller/               # REST + WebSocket controllers
│   │   │   ├── AuthController.java
│   │   │   ├── StudyGroupController.java
│   │   │   ├── StudyRoomController.java
│   │   │   ├── StudyRoomWebSocketController.java
│   │   │   ├── FriendController.java
│   │   │   ├── UserController.java
│   │   │   ├── DirectMessageController.java
│   │   │   └── DailyChallengeController.java
│   │   ├── service/                  # Business logic
│   │   │   ├── AuthService.java
│   │   │   ├── StudyGroupService.java
│   │   │   ├── StudyRoomService.java
│   │   │   ├── FriendService.java
│   │   │   ├── UserService.java
│   │   │   ├── DirectMessageService.java
│   │   │   ├── DailyChallengeService.java
│   │   │   └── ObjectStorageService.java
│   │   ├── repository/               # Spring Data JPA repositories
│   │   ├── entity/                   # JPA entities (domain model)
│   │   ├── dto/                      # Request/Response DTOs
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── security/                 # SecurityFilterChain config
│   │   └── exception/                # Global exception handler
│   ├── src/main/resources/
│   │   ├── application.yml           # Main config
│   │   ├── application-local.yml     # Local dev profile
│   │   ├── application-test.yml      # Test profile
│   │   └── db/migration/             # Flyway SQL migrations (V1–V12)
│   ├── Dockerfile                    # Multi-stage build (JDK → JRE)
│   └── pom.xml
│
├── gateway/                          # API Gateway (Spring Cloud Gateway)
│   ├── src/main/resources/
│   │   └── application.yml           # Route + CORS configuration
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                         # Frontend SPA (React + Vite)
│   ├── src/
│   │   ├── App.tsx                   # Route definitions
│   │   ├── main.tsx                  # React entry point
│   │   ├── pages/                    # Page-level components
│   │   │   ├── DashboardPage.tsx
│   │   │   ├── GroupsPage.tsx
│   │   │   ├── GroupDetailPage.tsx
│   │   │   ├── StudyRoomsPage.tsx
│   │   │   ├── FriendsPage.tsx
│   │   │   ├── MessagesPage.tsx
│   │   │   ├── ChallengesPage.tsx
│   │   │   ├── ProfilePage.tsx
│   │   │   ├── LoginPage.tsx
│   │   │   └── RegisterPage.tsx
│   │   ├── components/               # Reusable UI components
│   │   │   ├── layout/              #   AppLayout, Sidebar, Header
│   │   │   ├── chat/                #   GroupChat, VoiceRecorder, VoicePlayer
│   │   │   ├── video/               #   CallRoomsPanel (LiveKit)
│   │   │   ├── groups/              #   CreateGroupDialog
│   │   │   └── ui/                  #   shadcn/ui primitives
│   │   ├── api/                      # API client layer
│   │   │   ├── client.ts            #   Axios instance + interceptors
│   │   │   ├── authApi.ts           #   Login, register
│   │   │   ├── groups.ts            #   Group CRUD
│   │   │   ├── chatApi.ts           #   Chat messages
│   │   │   ├── chatSocket.ts        #   STOMP WebSocket client
│   │   │   ├── friendsApi.ts        #   Friend requests
│   │   │   ├── directMessageApi.ts  #   Direct messages
│   │   │   ├── videoApi.ts          #   LiveKit token
│   │   │   ├── challengeApi.ts      #   Daily challenges
│   │   │   └── profileApi.ts        #   User profile
│   │   ├── auth/                     # Auth context, token storage
│   │   ├── call/                     # Call provider, global overlay
│   │   ├── hooks/                    # Custom React hooks
│   │   ├── types/                    # TypeScript type definitions
│   │   ├── config/                   # Navigation config
│   │   └── lib/                      # Utilities
│   ├── Dockerfile
│   └── package.json
│
├── docs/
│   ├── openapi.yaml                  # OpenAPI 3.1 specification
│   ├── openapi.json
│   └── development/
│       └── backend-architecture.md
│
├── docker-compose.yml                # Full-stack orchestration
├── .env                              # Environment variables
└── pom.xml                           # Root Maven POM (multi-module)
```

---

## 💾 Domain Model & Database Schema

### Entity Relationship Diagram

```
┌──────────────────┐       ┌──────────────────────┐       ┌──────────────────┐
│      users       │       │    study_groups      │       │   group_members  │
├──────────────────┤       ├──────────────────────┤       ├──────────────────┤
│ id          UUID │◄──┐   │ id           UUID    │◄──┐   │ id        UUID   │
│ username  VC(50) │   │   │ name        VC(100)  │   │   │ group_id  UUID ──┤──► study_groups
│ email    VC(255) │   │   │ description VC(1000) │   │   │ user_id   UUID ──┤──► users
│ password VC(255) │   │   │ owner_id     UUID ───┤──►│   │ role    VC(20)   │
│ display  VC(100) │   │   │ visibility  VC(20)   │   │   │ joined_at TSTAMP │
│ bio      VC(500) │   │   │ created_at  TSTAMP   │   │   └──────────────────┘
│ avatar  VC(1000) │   │   │ updated_at  TSTAMP   │   │
│ created_at TSAMP │   │   └──────────────────────┘   │   ┌──────────────────────┐
│ updated_at TSAMP │   │                              │   │ group_join_requests  │
└──────────────────┘   │                              │   ├──────────────────────┤
         ▲             │                              │   │ id          UUID     │
         │             │                              ├───│ group_id    UUID     │
         │             │   ┌──────────────────────┐   │   │ user_id     UUID ────┤──► users
         │             │   │   chat_messages      │   │   │ status     VC(20)    │
         │             │   ├──────────────────────┤   │   │ created_at TSTAMP    │
         │             │   │ id          UUID     │   │   │ updated_at TSTAMP    │
         │             ├───│ group_id    UUID     │   │   └──────────────────────┘
         │             │   │ sender_id   UUID ────┤──►┘
         │             │   │ type       VC(20)    │        ┌──────────────────────┐
         │             │   │ content      TEXT    │        │     call_rooms       │
         │             │   │ media_url VC(1000)   │        ├──────────────────────┤
         │             │   │ duration_ms  INT     │        │ id          UUID     │
         │             │   │ created_at  TSTAMP   │        │ group_id    UUID ────┤──► study_groups
         │             │   └──────────────────────┘        │ name       VC(100)   │
         │             │                                   │ created_by  UUID  ───┤──► users
         │             │   ┌──────────────────────┐        │ created_at TSTAMP    │
         │             │   │ friend_relationships │        └──────────────────────┘
         │             │   ├──────────────────────┤
         │             ├───│ requester_id UUID    │        ┌──────────────────────┐
         │             └───│ addressee_id UUID    │        │   direct_messages    │
         │                 │ status     VC(20)    │        ├──────────────────────┤
         │                 │ created_at TSTAMP    │        │ id          UUID     │
         │                 │ updated_at TSTAMP    │        │ sender_id   UUID ────┤──► users
         │                 └──────────────────────┘        │ receiver_id UUID ────┤──► users
         │                                                 │ content      TEXT    │
         │                 ┌──────────────────────┐        │ created_at  TSTAMP   │
         │                 │  daily_challenges    │        └──────────────────────┘
         │                 ├──────────────────────┤
         │                 │ id            UUID   │        ┌────────────────────────────┐
         │                 │ challenge_date DATE  │        │ daily_challenge_attempts   │
         │                 │ question       TEXT  │        ├────────────────────────────┤
         │                 │ option_a       TEXT  │        │ id            UUID         │
         │                 │ option_b       TEXT  │◄───────│ challenge_id  UUID         │
         │                 │ option_c       TEXT  │        │ user_id       UUID ────────┤──► users
         │                 │ option_d       TEXT  │        │ selected_option VC(1)      │
         │                 │ correct_option VC(1) │        │ is_correct    BOOLEAN      │
         │                 │ explanation    TEXT  │        │ created_at    TSTAMP       │
         │                 │ created_at    TSTAMP │        └────────────────────────────┘
         │                 └──────────────────────┘
         │
         └─── (foreign key references)
```

### Danh sách Entities

| Entity                  | Table                      | Mô tả                                                               |
| ----------------------- | -------------------------- | ------------------------------------------------------------------- |
| `User`                  | `users`                    | Người dùng hệ thống (username, email, password hash, profile)       |
| `StudyGroup`            | `study_groups`             | Nhóm học tập (name, description, owner, visibility: PUBLIC/PRIVATE) |
| `GroupMember`           | `group_members`            | Thành viên nhóm (role: OWNER/MEMBER)                                |
| `GroupJoinRequest`      | `group_join_requests`      | Yêu cầu tham gia nhóm private (status: PENDING/APPROVED/REJECTED)   |
| `ChatMessage`           | `chat_messages`            | Tin nhắn nhóm (type: TEXT/IMAGE/AUDIO, hỗ trợ media URL & duration) |
| `CallRoom`              | `call_rooms`               | Phòng gọi video/audio trong nhóm                                    |
| `FriendRelationship`    | `friend_relationships`     | Quan hệ bạn bè (status: PENDING/ACCEPTED)                           |
| `DirectMessage`         | `direct_messages`          | Tin nhắn trực tiếp giữa hai người dùng                              |
| `DailyChallenge`        | `daily_challenges`         | Câu hỏi trắc nghiệm hàng ngày (4 đáp án A-D)                        |
| `DailyChallengeAttempt` | `daily_challenge_attempts` | Lượt trả lời thử thách của người dùng                               |

### Database Migrations (Flyway)

| Version | Mô tả                                                           |
| ------- | --------------------------------------------------------------- |
| `V1`    | Tạo bảng `users`                                                |
| `V2`    | Tạo bảng `study_groups`                                         |
| `V3`    | Tạo bảng `group_members`                                        |
| `V4`    | Thêm trường profile cho `users` (display_name, bio, avatar_url) |
| `V5`    | Tạo bảng `friend_relationships`                                 |
| `V6`    | Tạo bảng `chat_messages`                                        |
| `V7`    | Thêm trường `duration_ms` cho voice messages                    |
| `V8`    | Thêm trường `visibility` cho `study_groups`                     |
| `V9`    | Tạo bảng `group_join_requests`                                  |
| `V10`   | Tạo bảng `call_rooms`                                           |
| `V11`   | Tạo bảng `direct_messages`                                      |
| `V12`   | Tạo bảng `daily_challenges` và `daily_challenge_attempts`       |

---

## ✨ Tính năng chi tiết

### 1. Authentication & Authorization

- Đăng ký tài khoản (username, email, password)
- Đăng nhập trả về JWT access token
- Stateless authentication — JWT (RSA-signed) qua OAuth2 Resource Server
- Password hashing bằng BCrypt
- Protected routes trên frontend (ProtectedRoute component)
- Token storage trên client, auto-attach qua Axios interceptor

### 2. Quản lý nhóm học tập (Study Groups)

- Tạo nhóm với name, description, visibility (PUBLIC / PRIVATE)
- Tìm kiếm nhóm theo tên (paginated)
- Xem chi tiết nhóm
- Tham gia nhóm công khai / gửi yêu cầu tham gia nhóm riêng tư
- Duyệt/từ chối yêu cầu tham gia (cho owner)
- Rời nhóm (owner không được rời)
- Xóa nhóm (chỉ owner, cascade xóa members)
- Xem danh sách thành viên nhóm (với role: OWNER / MEMBER)

### 3. Study Room — Chat nhóm real-time

- Tin nhắn văn bản (TEXT)
- Tin nhắn hình ảnh (IMAGE) — upload lên MinIO
- Tin nhắn giọng nói (AUDIO) — ghi âm trên trình duyệt, upload lên MinIO, hiển thị duration
- Giao tiếp real-time qua STOMP/WebSocket:
  - Destination: `/topic/groups/{groupId}/messages`
  - Typing indicator: `/topic/groups/{groupId}/typing`
  - Send message: `/app/groups/{groupId}/messages`
- Lịch sử tin nhắn được lưu trong DB, load paginated qua REST

### 4. Video/Audio Call

- Tạo phòng gọi trong nhóm (CallRoom entity)
- Tích hợp LiveKit SFU server cho WebRTC
- Backend tạo JWT token cho LiveKit qua `LiveKit Server SDK`
- Frontend sử dụng `@livekit/components-react` để render video/audio UI
- Global call overlay — duy trì cuộc gọi khi chuyển trang

### 5. Hệ thống bạn bè (Friends)

- Tìm kiếm người dùng (search by username)
- Gửi lời mời kết bạn (PENDING)
- Xem danh sách lời mời đến/đi
- Chấp nhận / Từ chối lời mời
- Hủy lời mời đã gửi
- Xem danh sách bạn bè
- Xóa bạn bè

### 6. Nhắn tin trực tiếp (Direct Messages)

- Gửi tin nhắn riêng giữa hai người dùng
- Giao tiếp real-time qua STOMP WebSocket:
  - Destination: `/user/queue/messages`
  - Spring auto-resolve session-specific queue
- Lịch sử tin nhắn giữa hai người (REST API)

### 7. Daily Challenges

- Câu hỏi trắc nghiệm hàng ngày (4 đáp án A-D)
- Mỗi ngày 1 challenge (unique constraint on `challenge_date`)
- Người dùng submit đáp án, kiểm tra đúng/sai
- Xem explanation sau khi trả lời
- Theo dõi lượt attempt của mỗi user

### 8. Hồ sơ người dùng (Profile)

- Xem/chỉnh sửa display name, bio
- Upload avatar (lưu trên MinIO, public GET endpoint)
- Xem profile người dùng khác

---

## 📡 API Specification

### Authentication (`/api/auth`)

| Method | Endpoint             | Mô tả                       | Auth |
| ------ | -------------------- | --------------------------- | ---- |
| `POST` | `/api/auth/register` | Đăng ký tài khoản           | ❌   |
| `POST` | `/api/auth/login`    | Đăng nhập, nhận JWT token   | ❌   |
| `GET`  | `/api/auth/me`       | Lấy thông tin user hiện tại | ✅   |

### Study Groups (`/api/groups`)

| Method   | Endpoint                           | Mô tả                              | Auth |
| -------- | ---------------------------------- | ---------------------------------- | ---- |
| `POST`   | `/api/groups`                      | Tạo nhóm mới                       | ✅   |
| `GET`    | `/api/groups`                      | Danh sách nhóm (search, paginated) | ✅   |
| `GET`    | `/api/groups/{groupId}`            | Chi tiết nhóm                      | ✅   |
| `DELETE` | `/api/groups/{groupId}`            | Xóa nhóm (owner only)              | ✅   |
| `POST`   | `/api/groups/{groupId}/join`       | Tham gia nhóm                      | ✅   |
| `DELETE` | `/api/groups/{groupId}/members/me` | Rời nhóm                           | ✅   |
| `GET`    | `/api/groups/{groupId}/members`    | Danh sách thành viên               | ✅   |

### Study Rooms (`/api/groups/{groupId}`)

| Method | Endpoint                            | Mô tả                    | Auth |
| ------ | ----------------------------------- | ------------------------ | ---- |
| `GET`  | `/api/groups/{groupId}/messages`    | Lịch sử chat (paginated) | ✅   |
| `POST` | `/api/groups/{groupId}/messages`    | Gửi tin nhắn (multipart) | ✅   |
| `POST` | `/api/groups/{groupId}/video/token` | Lấy LiveKit token        | ✅   |

### Users (`/api/users`)

| Method  | Endpoint                      | Mô tả                  | Auth |
| ------- | ----------------------------- | ---------------------- | ---- |
| `GET`   | `/api/users/me/profile`       | Xem profile bản thân   | ✅   |
| `PATCH` | `/api/users/me/profile`       | Cập nhật profile       | ✅   |
| `PATCH` | `/api/users/me/avatar`        | Upload avatar          | ✅   |
| `GET`   | `/api/users/{userId}/profile` | Xem profile người khác | ✅   |
| `GET`   | `/api/users/{userId}/avatar`  | Lấy avatar (public)    | ❌   |
| `GET`   | `/api/users/search`           | Tìm kiếm user          | ✅   |

### Friends (`/api/friends`)

| Method   | Endpoint                                   | Mô tả               | Auth |
| -------- | ------------------------------------------ | ------------------- | ---- |
| `GET`    | `/api/friends`                             | Danh sách bạn bè    | ✅   |
| `POST`   | `/api/friends/requests/{userId}`           | Gửi lời mời kết bạn | ✅   |
| `GET`    | `/api/friends/requests/incoming`           | Lời mời đến         | ✅   |
| `GET`    | `/api/friends/requests/outgoing`           | Lời mời đi          | ✅   |
| `POST`   | `/api/friends/requests/{requestId}/accept` | Chấp nhận           | ✅   |
| `POST`   | `/api/friends/requests/{requestId}/reject` | Từ chối             | ✅   |
| `DELETE` | `/api/friends/requests/{requestId}`        | Hủy lời mời         | ✅   |
| `DELETE` | `/api/friends/{friendUserId}`              | Xóa bạn bè          | ✅   |

### Swagger UI

API documentation tự động được tạo bởi SpringDoc OpenAPI và truy cập tại:

- **Swagger UI**: `http://localhost:8081/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8081/v3/api-docs`

---

## 🔌 Giao tiếp Real-time (WebSocket)

### Kiến trúc STOMP

```
Frontend (STOMP.js)          Backend (Spring WebSocket)
      │                              │
      │  CONNECT + Bearer JWT        │
      ├─────────────────────────────►│  authenticateConnect()
      │                              │  → JwtDecoder.decode(token)
      │  CONNECTED                   │  → Set JwtAuthenticationToken
      │◄─────────────────────────────┤
      │                              │
      │  SUBSCRIBE                   │
      │  /topic/groups/{id}/messages │  authorizeSubscription()
      ├─────────────────────────────►│  → Verify membership via DB
      │                              │
      │  SEND                        │
      │  /app/groups/{id}/messages   │  StudyRoomWebSocketController
      ├─────────────────────────────►│  → Save to DB
      │                              │  → Broadcast to topic
      │  MESSAGE                     │
      │  /topic/groups/{id}/messages │
      │◄─────────────────────────────┤
```

### WebSocket Endpoints

| Endpoint | Protocol             | Mô tả                   |
| -------- | -------------------- | ----------------------- |
| `/ws`    | STOMP over WebSocket | Main WebSocket endpoint |

### STOMP Destinations

| Destination                        | Loại      | Mô tả                                         |
| ---------------------------------- | --------- | --------------------------------------------- |
| `/app/groups/{groupId}/messages`   | SEND      | Gửi tin nhắn chat nhóm                        |
| `/app/groups/{groupId}/typing`     | SEND      | Gửi typing indicator                          |
| `/topic/groups/{groupId}/messages` | SUBSCRIBE | Nhận tin nhắn chat nhóm (membership required) |
| `/topic/groups/{groupId}/typing`   | SUBSCRIBE | Nhận typing indicator                         |
| `/user/queue/messages`             | SUBSCRIBE | Nhận tin nhắn trực tiếp (auto user-resolved)  |

### Bảo mật WebSocket

- **CONNECT**: Yêu cầu `Authorization: Bearer <JWT>` header
- **SUBSCRIBE /topic/groups/{id}/\***: Kiểm tra user có phải member của group qua DB
- **SUBSCRIBE /user/queue/\***: Spring tự resolve sang session-specific queue
- **Direct /queue/ subscription**: Bị chặn (phải dùng `/user/queue/...`)

---

## 🔒 Bảo mật

### Authentication Flow

```
┌──────────┐    POST /api/auth/login     ┌──────────┐
│  Client  │ ──────────────────────────► │  Backend │
│          │    { email, password }       │          │
│          │                              │          │
│          │    ◄─────────────────────── │          │
│          │    { accessToken, user }     │          │
│          │                              │          │
│          │    GET /api/groups           │          │
│          │    Authorization: Bearer ... │          │
│          │ ──────────────────────────► │          │
│          │                              │  OAuth2  │
│          │    ◄─────────────────────── │  Resource│
│          │    200 OK                    │  Server  │
└──────────┘                              └──────────┘
```

| Aspect                  | Implementation                                                                             |
| ----------------------- | ------------------------------------------------------------------------------------------ |
| **Password Storage**    | BCrypt hash (PasswordConfig)                                                               |
| **Token Format**        | JWT (RSA-signed via JwtConfig)                                                             |
| **Token Lifetime**      | Configurable (default: 3600s = 1 hour)                                                     |
| **Session**             | Stateless (`SessionCreationPolicy.STATELESS`)                                              |
| **CORS**                | Gateway-level CORS (localhost:\* pattern)                                                  |
| **Public Endpoints**    | `/api/auth/register`, `/api/auth/login`, `/swagger-ui/**`, `/ws/**`, `/api/users/*/avatar` |
| **Protected Endpoints** | Tất cả endpoints khác yêu cầu valid JWT                                                    |

---

## 🚀 Hướng dẫn cài đặt

### Yêu cầu hệ thống

- **Docker** & **Docker Compose** (v2+)
- **Java 17** (nếu chạy local không dùng Docker)
- **Node.js 22** (nếu chạy frontend local)
- **Maven 3.9+** (nếu build local)

### Chạy bằng Docker Compose (Khuyến nghị)

```bash
# 1. Clone repository
git clone https://github.com/KDHieu/group-study-platform.git
cd group-study-platform

# 2. Tạo file .env (hoặc dùng mặc định)
cp .env.example .env

# 3. Khởi chạy toàn bộ hệ thống
docker compose up --build

# 4. Truy cập ứng dụng
#    Frontend:   http://localhost:5173
#    Gateway:    http://localhost:8080
#    Backend:    http://localhost:8081
#    Swagger UI: http://localhost:8081/swagger-ui.html
#    MinIO UI:   http://localhost:9001
```

### Chạy riêng từng service (Development)

```bash
# 1. Khởi động infrastructure (PostgreSQL, MinIO, LiveKit)
docker compose up postgres minio livekit

# 2. Chạy Backend
cd backend
./mvnw spring-boot:run

# 3. Chạy Gateway
cd gateway
./mvnw spring-boot:run

# 4. Chạy Frontend
cd frontend
npm install
npm run dev
```

### Port mặc định

| Service           | Port       | Mô tả                            |
| ----------------- | ---------- | -------------------------------- |
| **Frontend**      | `5173`     | Vite dev server                  |
| **Gateway**       | `8080`     | API Gateway                      |
| **Backend**       | `8081`     | Spring Boot REST API + WebSocket |
| **PostgreSQL**    | `5432`     | Database                         |
| **MinIO API**     | `9000`     | Object storage API               |
| **MinIO Console** | `9001`     | MinIO web UI                     |
| **LiveKit**       | `7880`     | WebRTC signaling                 |
| **LiveKit**       | `7881`     | WebRTC TURN/TCP                  |
| **LiveKit**       | `7882/udp` | WebRTC TURN/UDP                  |

---

## ⚙ Biến môi trường

| Biến                                  | Mặc định                 | Mô tả                 |
| ------------------------------------- | ------------------------ | --------------------- |
| `POSTGRES_DB`                         | `group_learning`         | Tên database          |
| `POSTGRES_USER`                       | `postgres`               | Database username     |
| `POSTGRES_PASSWORD`                   | `postgres`               | Database password     |
| `BACKEND_PORT`                        | `8081`                   | Port backend          |
| `GATEWAY_PORT`                        | `8080`                   | Port gateway          |
| `FRONTEND_PORT`                       | `5173`                   | Port frontend         |
| `JWT_SECRET`                          | `dev-only-secret-key...` | JWT signing secret    |
| `JWT_ACCESS_TOKEN_EXPIRATION_SECONDS` | `3600`                   | Token lifetime (giây) |
| `MINIO_ROOT_USER`                     | `minioadmin`             | MinIO admin username  |
| `MINIO_ROOT_PASSWORD`                 | `minioadmin`             | MinIO admin password  |
| `LIVEKIT_API_KEY`                     | `devkey`                 | LiveKit API key       |
| `LIVEKIT_API_SECRET`                  | `secret`                 | LiveKit API secret    |

---

## 📄 License

Dự án này được phát triển cho mục đích học tập môn **Kiến trúc phần mềm (Software Architecture)**.
