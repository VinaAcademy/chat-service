# Chat Service - GitHub Actions Workflows

This repository contains three GitHub Actions workflows for the Chat Service microservice:

## 📋 Workflows Overview

### 1. PR CD Pipeline (`pr-cd.yml`)
- **Trigger**: On pull requests to `main` or `master` branches
- **Purpose**: Continuous Delivery for pull requests
- **Actions**:
  - Run unit tests with PostgreSQL
  - Build the application
  - Create Docker image (not pushed)
  - Code quality checks (checkstyle, spotbugs)
  - Security scanning with Trivy

### 2. Main CI/CD Pipeline (`main-cicd.yml`)
- **Trigger**: On push to `main` or `master` branches
- **Purpose**: Continuous Integration and Deployment to production
- **Actions**:
  - Run tests
  - Build and push Docker image to DockerHub
  - Deploy to VPS
  - Send notifications (optional)

### 3. Manual Deploy (`manual-deploy.yml`)
- **Trigger**: Manual workflow dispatch
- **Purpose**: Deploy any branch to any environment on-demand
- **Features**:
  - Choose branch to deploy
  - Select environment (production/staging/development)
  - Option to skip tests
  - Force deployment option
  - Comprehensive logging and health checks

## 🔧 Setup Requirements

### GitHub Secrets

You need to configure the following secrets in your GitHub repository:

#### Required Secrets:
```
DOCKERHUB_USERNAME          # Your DockerHub username
DOCKERHUB_TOKEN            # DockerHub access token
VPS_HOST                   # Your VPS IP address or hostname
VPS_USER                   # SSH username for VPS
VPS_SSH_KEY               # Private SSH key for VPS access
```

#### Optional Secrets:
```
VPS_PORT                   # SSH port (default: 22)
SLACK_WEBHOOK_URL         # Slack webhook for notifications

# For staging environment (if different from production):
STAGING_VPS_HOST
STAGING_VPS_USER
STAGING_VPS_SSH_KEY
STAGING_VPS_PORT

# For development environment (if different from production):
DEV_VPS_HOST
DEV_VPS_USER
DEV_VPS_SSH_KEY
DEV_VPS_PORT
```

### VPS Setup

Your VPS should have the following structure:

```bash
/home/$VPS_USER/ms-ci-cd/
├── docker-compose.yml           # For production
├── docker-compose.staging.yml   # For staging (optional)
├── docker-compose.dev.yml       # For development (optional)
└── .git/                        # Git repository
```

#### Sample docker-compose.yml:
```yaml
version: '3.8'

services:
  chat-service:
    image: ${DOCKERHUB_USERNAME}/chat-service:latest
    ports:
      - "8080:8080"
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/chat_service
      - SPRING_DATASOURCE_USERNAME=chat_user
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka:8761/eureka
    depends_on:
      - postgres
      - kafka
    networks:
      - microservices

  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=chat_service
      - POSTGRES_USER=chat_user
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - microservices

  # Add other services (kafka, eureka, etc.) as needed

volumes:
  postgres_data:

networks:
  microservices:
    driver: bridge
```

## 🚀 Usage

### Automatic Deployments

1. **Pull Request Testing**: 
   - Create a PR to `main` branch
   - Workflow automatically runs tests and builds

2. **Production Deployment**:
   - Merge PR to `main` branch
   - Workflow automatically builds, pushes, and deploys

### Manual Deployment

1. Go to GitHub Actions tab in your repository
2. Select "Chat Service - Manual Deploy" workflow
3. Click "Run workflow"
4. Choose your options:
   - **Branch**: Select branch to deploy
   - **Environment**: Choose target environment
   - **Skip tests**: Skip running tests (faster deployment)
   - **Force deploy**: Skip health checks

## 🔍 Monitoring

### Health Checks
The workflows include health checks using the Spring Boot Actuator endpoint:
- Endpoint: `http://localhost:8081/actuator/health`
- Timeout: 60 seconds (6 attempts × 10 seconds)

### Logs
- Deployment logs are available in GitHub Actions
- Service logs can be viewed on VPS: `docker-compose logs chat-service`

### Notifications
Optional Slack notifications for deployment status. Configure `SLACK_WEBHOOK_URL` secret to enable.

## 🛠️ Customization

### Adding New Environments

1. Add new environment secrets (e.g., `TEST_VPS_HOST`, `TEST_VPS_USER`, etc.)
2. Update the `manual-deploy.yml` workflow to include the new environment in the choice options
3. Add the environment case in the "Set deployment variables" step

### Modifying Docker Image Tags

The workflows use the following tagging strategy:
- **Production**: `latest` (from main branch)
- **Branch builds**: `{sanitized-branch-name}`
- **Manual builds**: `manual-deploy-{run-number}`
- **Timestamped**: `{branch}-{timestamp}`

### Adding More Services

To extend this for multiple services in a monorepo:
1. Create separate workflow files for each service
2. Use path filters to trigger only when relevant code changes
3. Modify the Docker build context and compose service names

## 🔒 Security Considerations

1. **SSH Keys**: Use dedicated deployment keys with minimal permissions
2. **DockerHub Tokens**: Use access tokens instead of passwords
3. **Environment Secrets**: Keep production secrets separate from staging/dev
4. **VPS Access**: Limit SSH access to deployment user only
5. **Docker Security**: Regularly update base images and scan for vulnerabilities

## 📚 Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Hub Documentation](https://docs.docker.com/docker-hub/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

## 🐛 Troubleshooting

### Common Issues:

1. **Docker build fails**: Check Dockerfile and build context
2. **SSH connection fails**: Verify VPS secrets and network connectivity
3. **Health check fails**: Check service logs and application configuration
4. **Image push fails**: Verify DockerHub credentials and repository permissions
5. **Service won't start**: Check docker-compose.yml and environment variables

### Debug Commands:

```bash
# Check service status
docker-compose ps

# View service logs
docker-compose logs chat-service

# Check container health
docker inspect <container-name> | grep Health

# Test SSH connection
ssh -i ~/.ssh/id_rsa $VPS_USER@$VPS_HOST

# Test DockerHub login
docker login -u $DOCKERHUB_USERNAME
```
