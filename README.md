# Chat Service 💬

A robust, scalable **real-time messaging microservice** for the VinaAcademy platform, enabling direct and group conversations with WebSocket-based instant delivery.

## 🚀 Quick Status

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Java Version](https://img.shields.io/badge/java-17-orange)]()
[![Spring Boot](https://img.shields.io/badge/spring--boot-3.x-brightgreen)]()
[![License](https://img.shields.io/badge/license-MIT-blue)]()

## 📋 Overview

Chat Service is a **production-ready microservice** that powers real-time messaging within the VinaAcademy educational platform. It provides:

- **Real-time messaging** via WebSocket + STOMP protocol
- **Direct and group conversations** with flexible chat types
- **Message persistence** using PostgreSQL with optimized indexing
- **Event-driven architecture** powered by Apache Kafka
- **Secure authentication** via JWT validation through gRPC
- **Service discovery** integration with Eureka
- **Scalable horizontal deployment** across multiple instances

Perfect for building feature-rich communication platforms that require low-latency, reliable message delivery.

## 📚 Table of Contents

- [Features & Highlights](#-features--highlights)
- [Technology Stack](#-technology-stack)
- [Installation & Setup](#-installation--setup)
- [Configuration](#-configuration)
- [Usage & Examples](#-usage--examples)
- [Architecture](#-architecture)
- [Development](#-development)
- [Contributing](#-contributing)

## ✨ Features & Highlights

### Core Capabilities
- **WebSocket Real-time Messaging**: Instant message delivery with STOMP protocol
- **Direct & Group Chats**: Support for 1:1 private messages and group conversations
- **Message Types**: Text, image, and file content support with validation
- **Message Pagination**: Efficient message history retrieval with sequence-based pagination
- **User Presence**: Track online/offline status of users in real-time
- **Event Streaming**: Kafka-based event distribution for microservice coordination
- **Database Migrations**: Automatic schema management with Flyway

### Quality & Reliability
- **Authentication & Authorization**: JWT-based security with gRPC validation
- **Database Optimization**: Composite indexes for fast conversation listing and message queries
- **Health Checks**: Actuator endpoints for monitoring service health
- **Production-Ready**: Docker support with multi-stage builds
- **Spring Cloud Integration**: Service discovery and distributed tracing ready

## 🛠 Technology Stack

### Core Framework
- **Java 17** - LTS version for long-term support
- **Spring Boot 3.x** - Modern Spring framework with auto-configuration
- **Spring Cloud 2024.0.2** - Microservices ecosystem (Eureka, Config, etc.)

### Communication & Messaging
- **WebSocket + STOMP** - Real-time bidirectional communication
- **Apache Kafka** - Distributed event streaming
- **Spring Kafka** - Kafka integration with Spring

### Data Persistence
- **PostgreSQL** - Primary relational database
- **Spring Data JPA** - ORM and repository pattern
- **Flyway** - Database schema versioning and migrations
- **Hibernate** - JPA implementation

### Additional Libraries
- **MapStruct 1.5.5** - Bean mapping with annotation processing
- **Lombok 1.18.32** - Boilerplate reduction (getters, setters, builders)
- **Apache Commons Lang 3.18.0** - Utility functions
- **gRPC 1.72.0** - High-performance RPC for authentication
- **Protobuf 4.30.2** - Protocol Buffers for gRPC

### Development & Deployment
- **Maven 3.8.3+** - Build automation
- **Docker** - Containerization with Alpine base image
- **Spring Boot Actuator** - Health checks and metrics

## 🚀 Installation & Setup

### Prerequisites

Ensure you have the following installed:

- **Java 17 LTS** - [Download](https://adoptopenjdk.net/)
- **Maven 3.8.3+** - [Download](https://maven.apache.org/download.cgi)
- **Docker** (optional) - [Download](https://www.docker.com/)
- **PostgreSQL 12+** - [Download](https://www.postgresql.org/)
- **Apache Kafka 3.x** (optional) - [Download](https://kafka.apache.org/quickstart)
- **Redis** (optional) - For caching support

### Clone & Build

```bash
# Clone the repository
git clone https://github.com/VinaAcademy/chat-service.git
cd chat-service

# Build the project
./mvnw clean package

# Run tests
./mvnw test
```

### Local Development Setup

#### 1. Database Setup

```bash
# Create PostgreSQL database
psql -U postgres
CREATE DATABASE vinaacademy_chat;
CREATE USER chat_user WITH PASSWORD 'chat_password';
GRANT ALL PRIVILEGES ON DATABASE vinaacademy_chat TO chat_user;
```

#### 2. Environment Variables

Create a `.env` file in the project root:

```bash
# Database Configuration
DB_URL=localhost:5432
DB_NAME=vinaacademy_chat
DB_USERNAME=chat_user
DB_PASSWORD=chat_password

# Server Configuration
SERVER_PORT=8080

# Eureka Service Discovery
EUREKA_CLIENT_SERVICE_DEFAULTZONE=http://localhost:8761/eureka/
EUREKA_INSTANCE_HOSTNAME=localhost

# Kafka Configuration
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SPRING_KAFKA_CONSUMER_GROUP_ID=chat-service-group

# Redis Configuration (optional)
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# JWT & gRPC Security
SECURITY_SERVER_HOST=localhost
SECURITY_SERVER_PORT=50051

# WebSocket Configuration
APP_WS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

#### 3. Run Locally

```bash
# Using Maven
./mvnw spring-boot:run

# Or with IDE (IntelliJ/Eclipse)
# Run ChatServiceApplication.java as Java Application
```

The service will start at `http://localhost:8080`

### Docker Deployment

#### Build Docker Image

```bash
# Build the image
docker build -t chat-service:latest .

# Or with custom registry
docker build -t your-registry/chat-service:latest .
```

#### Run with Docker Compose

Create a `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: vinaacademy_chat
      POSTGRES_USER: chat_user
      POSTGRES_PASSWORD: chat_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    ports:
      - "9092:9092"

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  chat-service:
    build: .
    environment:
      DB_URL: postgres:5432
      DB_NAME: vinaacademy_chat
      DB_USERNAME: chat_user
      DB_PASSWORD: chat_password
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - kafka

volumes:
  postgres_data:
```

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f chat-service
```

## ⚙️ Configuration

### Key Application Properties

Configure in `src/main/resources/application.yml`:

```yaml
# Server
server.port: 8080

# Database
spring.datasource.url: jdbc:postgresql://localhost:5432/vinaacademy_chat
spring.jpa.hibernate.ddl-auto: validate
spring.flyway.enabled: true

# Kafka
spring.kafka.bootstrap-servers: localhost:9092
spring.kafka.consumer.group-id: chat-service-group

# Redis (optional caching)
spring.data.redis.host: localhost
spring.data.redis.port: 6379

# Eureka Discovery
eureka.client.service-url.defaultZone: http://localhost:8761/eureka/

# Actuator
management.endpoints.web.exposure.include: health,info,metrics
```

### WebSocket Configuration

- **Endpoint**: `/ws`
- **Message Destination**: `/app/pm` (private messages)
- **Queue Subscription**: `/queue/pm`
- **CORS Origins**: Configure via `app.ws.allowed-origins` environment variable
- **Authentication**: JWT token (via header or `?token=` parameter)

## 📖 Usage & Examples

### 1. WebSocket Connection

**Connect to WebSocket endpoint with JWT authentication:**

```javascript
// JavaScript Example
const token = "your-jwt-token";
const socket = new SockJS("http://localhost:8080/ws");
const stompClient = Stomp.over(socket);

stompClient.connect(
  { "Authorization": `Bearer ${token}` },
  function() {
    console.log("Connected to Chat Service");
    
    // Subscribe to personal message queue
    stompClient.subscribe("/user/queue/pm", function(message) {
      console.log("Received message:", JSON.parse(message.body));
    });
  },
  function(error) {
    console.error("Connection failed:", error);
  }
);
```

### 2. Send a Private Message

```javascript
// Send message via WebSocket
const messagePayload = {
  conversationId: "conv-123",
  content: "Hello! How are you?",
  type: "TEXT"
};

stompClient.send("/app/pm", {}, JSON.stringify(messagePayload));
```

### 3. REST API Examples

#### Get Conversations

```bash
curl -X GET http://localhost:8080/api/conversations \
  -H "Authorization: Bearer <jwt-token>"
```

#### Get Messages in Conversation

```bash
curl -X GET http://localhost:8080/api/messages/conv-123?limit=50 \
  -H "Authorization: Bearer <jwt-token>"
```

#### Create New Conversation

```bash
curl -X POST http://localhost:8080/api/conversations \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "DIRECT",
    "members": ["user-1", "user-2"]
  }'
```

### 4. Health Check

```bash
curl http://localhost:8080/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "kafkaProducer": { "status": "UP" },
    "livenessState": { "status": "UP" },
    "readinessState": { "status": "UP" }
  }
}
```

## 🏗 Architecture

### Message Flow

```
1. WebSocket Reception
   Client → /app/pm → PrivateChatController

2. Message Publishing
   PrivateChatController → KafkaMessageService → Kafka Topic

3. Event Distribution
   Kafka Consumer → @KafkaListener → Message Processing

4. WebSocket Delivery
   WebSocketService → SimpMessagingTemplate → /queue/pm → Client
```

### Core Components

| Component | Responsibility |
|-----------|-----------------|
| **PrivateChatController** | Receives WebSocket messages from clients |
| **KafkaMessageService** | Publishes messages to Kafka topics |
| **MessageService** | Business logic for message handling |
| **ConversationService** | Manages conversation lifecycle |
| **JwtHandshakeInterceptor** | Validates JWT tokens via gRPC |
| **MessageRepository** | Database queries with optimized indexes |

### Database Schema Highlights

- **Conversation**: Stores chat metadata (type, creation date)
- **ConversationMember**: Junction table with composite keys
- **Message**: Polymorphic content with `seq` for pagination
- **Indexes**: 
  - `idx_conversation_last_msg` - For conversation listing
  - `idx_message_seq` - For efficient message pagination

## 🔧 Development

### Project Structure

```
chat-service/
├── src/main/
│   ├── java/vn/vinaacademy/chat/
│   │   ├── config/              # Spring configurations
│   │   ├── controller/          # REST & WebSocket controllers
│   │   ├── service/             # Business logic
│   │   ├── repository/          # Data access layer
│   │   ├── entity/              # JPA entities
│   │   ├── dto/                 # Data transfer objects
│   │   ├── mapper/              # MapStruct mappers
│   │   ├── event/               # Kafka event handling
│   │   ├── interceptor/         # Custom interceptors
│   │   └── domain/              # Domain services
│   └── resources/
│       ├── application.yml      # Configuration
│       └── db/migration/        # Flyway migrations
├── Dockerfile                   # Docker build
└── pom.xml                      # Maven configuration
```

### Building & Testing

```bash
# Clean build
./mvnw clean package

# Run tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report

# Format code
./mvnw spring-javaformat:apply

# Check code quality
./mvnw sonar:sonar
```

### Common Tasks

- **Add new dependency**: Edit `pom.xml` and run `./mvnw dependency:resolve`
- **Create migration**: Add SQL file to `src/main/resources/db/migration/`
- **Generate DTOs**: Update entities, MapStruct will auto-generate mappers
- **Debug SQL**: Enable `show_sql: true` in `application.yml`

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

### Code Submission

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/your-feature-name`
3. **Commit** with clear messages: `git commit -m "feat: add new feature"`
4. **Push** to your fork: `git push origin feature/your-feature-name`
5. **Open** a Pull Request with description

### Development Guidelines

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Write unit tests for new features (aim for 80%+ coverage)
- Update documentation and comments
- Ensure all tests pass: `./mvnw test`
- Use meaningful commit messages with conventional commit format

### Commit Message Format

```
<type>: <description>

[optional body]

Fixes #<issue-number>
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

### Testing Requirements

- Unit tests for business logic
- Integration tests for repository/service interactions
- WebSocket tests for real-time features
- API contract tests for REST endpoints

### Code Review Process

1. All PRs require at least 1 code review
2. CI/CD pipeline must pass
3. No conflicts with main branch
4. Documentation updated if needed

---

## 📞 Support & Contact

**Lead Developer**: Nguyen Huu Loc

For questions or issues:
- 📧 Email: [huuloc2155@gmail.com]
- 🐛 Issues: [GitHub Issues](https://github.com/VinaAcademy/chat-service/issues)
- 💬 Discussions: [GitHub Discussions](https://github.com/VinaAcademy/chat-service/discussions)

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Made with ❤️ by the VinaAcademy Team**
