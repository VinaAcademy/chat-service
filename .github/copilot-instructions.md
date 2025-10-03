# Chat Service - AI Development Guide

## Architecture Overview
This is a **Spring Boot microservice** for real-time messaging within the VinaAcademy platform. Key components:
- **WebSocket + STOMP** for real-time messaging (`/ws` endpoint)
- **Kafka** for message distribution across service instances
- **PostgreSQL** with Flyway migrations for persistence
- **Eureka** service discovery integration
- **gRPC** authentication via `security-client` library

## Core Message Flow
1. **WebSocket Reception**: `PrivateChatController.handlePrivate()` receives `/app/pm` messages
2. **Kafka Publishing**: Messages sent to `private-messages` topic via `KafkaMessageService`
3. **Kafka Consumption**: `@KafkaListener` forwards messages to WebSocket `/queue/pm` destinations
4. **Authentication**: JWT validation through `JwtHandshakeInterceptor` using gRPC calls

## Entity Relationships
- **Conversation**: Supports both `DIRECT` (1:1) and `GROUP` chat types
- **ConversationMember**: Junction table with composite key (`ConversationMemberId`)
- **Message**: Polymorphic content (TEXT/IMAGE/FILE) with `seq` for pagination
- **Indexes**: Optimized for conversation listing (`last_msg_at DESC`) and message pagination (`seq DESC`)

## Development Patterns

### Authentication Integration
- WebSocket auth via `JwtHandshakeInterceptor` - validates tokens through gRPC
- REST endpoints use `@PreAuthorize("isAuthenticated()")` from `security-client`
- User context accessible via `SecurityContextHolder.getCurrentUserId()`

### Database Conventions
- All IDs use UUID with `@UuidGenerator`
- Flyway migrations in `src/main/resources/db/migration/V1.1__*.sql`
- Use `@CreationTimestamp` for audit fields, `@Builder.Default` for enum defaults
- Composite keys implement `Serializable` with proper `equals()`/`hashCode()`

### Message Processing
- WebSocket controllers use `@MessageMapping` (NOT `@RestController`)
- Kafka topics: `private-messages`, `group-messages` 
- JSON serialization configured for `vn.vinaacademy.chat.dto` package
- Use `SimpMessagingTemplate.convertAndSendToUser()` for targeted delivery

## Build & Development

### Local Development
```bash
./mvnw spring-boot:run
# Or with Docker:
docker build -t chat-service .
docker run -p 8081:8080 chat-service
```

### Key Dependencies
- MapStruct mappers with Lombok binding (`lombok-mapstruct-binding`)
- Custom libraries: `common-library:2.0.0`, `security-client:1.0.0`
- Swagger UI available at root path (`/`)

### Configuration
- Environment variables for DB, Kafka, Eureka, and gRPC endpoints
- WebSocket CORS configured via `app.ws.allowed-origins`
- Actuator health checks on port 8081

## Testing & Debugging
- Extensive SQL logging enabled via `show_sql: true` and `BasicBinder: TRACE`
- WebSocket testing requires JWT token (header or `?token=` parameter)
- Use `/actuator/health` for service health checks
- Kafka consumer group: `chat-service-group`

## Common Patterns
- Repository methods follow Spring Data naming: `findTop50ByConversationAndSeqLessThanOrderBySeqDesc`
- DTOs use Lombok `@Data`, entities use separate `@Getter/@Setter`
- Validation in entity `@PrePersist` methods (see `Message.validateContent()`)
- Service layer handles business logic, controllers are thin adapters
