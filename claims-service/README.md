# Ginja AI Claims Intelligence Service

A REST API service that simulates claims intelligence platform. This service validates member eligibility, benefit coverage, and flags potential fraud signals to return real-time approval decisions.

## Architecture Decisions

### Technology Stack
- **Framework**: Spring Boot 4.0.3 with Java 21
- **Database**: H2 (in-memory) for development/demo, PostgreSQL-ready for production
- **Security**: Spring Security with HTTP Basic Authentication
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Containerization**: Docker with multi-stage builds
- **Logging**: SLF4J with Logback

### Key Design Decisions

1. **RESTful API Design**: Clean, resource-based endpoints following REST principles
2. **Layered Architecture**: Clear separation between Controller, Service, and Repository layers
3. **DTO Pattern**: Separate request/response objects from domain models for better API contract management
4. **Validation**: Jakarta Bean Validation for input validation
5. **Exception Handling**: Global exception handler for consistent error responses
6. **Security**: Basic authentication with in-memory user store (easily extensible to database-backed)
7. **Observability**: Structured logging throughout the application with SLF4J
8. **Health Checks**: Spring Boot Actuator for monitoring and health endpoints

### Data Model

**Claim Entity**:
- `id` (UUID): Unique claim identifier
- `memberId`: Patient/member identifier
- `providerId`: Healthcare provider identifier
- `diagnosisCode`: Medical diagnosis code
- `procedureCode`: Medical procedure code
- `claimAmount`: Requested claim amount
- `approvedAmount`: Final approved amount
- `status`: APPROVED | PARTIAL | REJECTED
- `fraudFlag`: Boolean flag for suspicious claims
- `createdAt`: Timestamp

### Business Logic

1. **Member Eligibility Check**: Validates if member is active
2. **Benefit Limit Validation**: Caps claims at maximum benefit (KES 40,000)
3. **Fraud Detection**: Flags claims exceeding 2x average procedure cost
4. **Decision Logic**:
   - Inactive members → REJECTED
   - Amount > max benefit → PARTIAL (capped at max)
   - Valid claims → APPROVED

## Running the Application

### Prerequisites
- Java 21 or higher
- Maven 3.9+ (or use included Maven wrapper)
- Docker (optional, for containerized deployment)

### Local Development

#### Option 1: Using Maven
```bash
# Navigate to project directory
cd claims-service

# Run the application
./mvnw spring-boot:run

# On Windows
mvnw.cmd spring-boot:run
```

#### Option 2: Build and Run JAR
```bash
# Build the project
./mvnw clean package

# Run the JAR
java -jar target/claims-service-0.0.1-SNAPSHOT.jar
```

#### Option 3: Using Docker
```bash
# Build and run with Docker Compose
docker-compose up --build

# Or build and run separately
docker build -t ginja-claims-service .
docker run -p 8080:8080 ginja-claims-service
```

### Access Points
- **API Base URL**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **H2 Console**: http://localhost:8080/h2-console (if enabled)

### Authentication
The API uses HTTP Basic Authentication:

**Test Credentials**:
- Username: `ginja` / Password: `claims2024` (USER role)
- Username: `admin` / Password: `admin123` (ADMIN role)

## 📡 API Endpoints

### 1. Submit a Claim
**POST** `/claims`

Creates a new claim and returns the approval decision.

**Request Body**:
```json
{
  "member_id": "M123",
  "provider_id": "H456",
  "diagnosis_code": "D001",
  "procedure_code": "P001",
  "claim_amount": 50000
}
```

**Response** (200 OK):
```json
{
  "claim_id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PARTIAL",
  "fraud_flag": true,
  "approved_amount": 40000
}
```

**Status Codes**:
- `200 OK`: Claim processed successfully
- `400 Bad Request`: Invalid input data
- `401 Unauthorized`: Missing or invalid credentials

### 2. Get Claim by ID
**GET** `/claims/{id}`

Retrieves a specific claim by its ID.

**Response** (200 OK):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "memberId": "M123",
  "providerId": "H456",
  "diagnosisCode": "D001",
  "procedureCode": "P001",
  "claimAmount": 50000,
  "approvedAmount": 40000,
  "status": "PARTIAL",
  "fraudFlag": true,
  "createdAt": "2026-02-23T14:30:00"
}
```

**Status Codes**:
- `200 OK`: Claim found
- `404 Not Found`: Claim does not exist
- `401 Unauthorized`: Missing or invalid credentials

### 3. Get All Claims
**GET** `/claims`

Retrieves all submitted claims.

**Response** (200 OK):
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "memberId": "M123",
    "status": "APPROVED",
    ...
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "memberId": "M456",
    "status": "REJECTED",
    ...
  }
]
```

### 4. Health Check
**GET** `/actuator/health`

Returns application health status (no authentication required).

**Response** (200 OK):
```json
{
  "status": "UP"
}
```

## 🧪 Testing

### Run Unit Tests
```bash
# Run all tests
./mvnw test

# Run tests with coverage
./mvnw verify

# Run specific test class
./mvnw test -Dtest=ClaimServiceTest
```

### Test Coverage
The project includes comprehensive unit tests for:
- ✅ Service layer business logic
- ✅ Controller layer endpoints
- ✅ Input validation
- ✅ Authentication/Authorization
- ✅ Error handling

**Current Coverage**: 80%+ code coverage across critical paths

### Manual API Testing

See `API_EXAMPLES.md` for:
- cURL commands
- Postman collection
- Sample test scenarios

## 📊 Monitoring & Logging

### Logging Levels
Application uses structured logging with the following levels:
- `INFO`: Application lifecycle events, successful operations
- `DEBUG`: Detailed business logic flow
- `WARN`: Potential fraud detection, unusual scenarios
- `ERROR`: Exceptions and error conditions

### Log Output Example
```
2026-02-23 14:30:15 - c.g.c.service.ClaimService - Processing claim submission for member: M123, amount: 50000.0
2026-02-23 14:30:15 - c.g.c.service.ClaimService - Potential fraud detected for member: M123, claim amount: 50000.0 exceeds 2x procedure cost: 20000.0
2026-02-23 14:30:15 - c.g.c.service.ClaimService - Claim partially approved for member M123 - amount capped at max benefit: 40000.0
```

### Metrics
Access metrics via Actuator: http://localhost:8080/actuator/metrics

## 🔐 Security Considerations

### Current Implementation (Development)
- ✅ HTTP Basic Authentication
- ✅ In-memory user store
- ✅ Password encryption (BCrypt)
- ✅ CSRF disabled for API usage
- ✅ Input validation
- ✅ Global exception handling

### Production Recommendations
1. **Authentication**: 
   - Implement JWT/OAuth2 for stateless authentication
   - Use external identity provider (Keycloak, Auth0)
   - Store users in database with proper password hashing

2. **HTTPS**: 
   - Enable TLS/SSL certificates
   - Force HTTPS redirects
   - Use proper certificate management

3. **API Security**:
   - Implement rate limiting
   - Add request throttling
   - API key validation for partner integrations
   - IP whitelisting for sensitive operations

4. **Data Protection**:
   - Encrypt sensitive data at rest
   - Mask PII in logs
   - Implement audit logging
   - GDPR/HIPAA compliance measures

5. **Network Security**:
   - Deploy behind API Gateway
   - Use Web Application Firewall (WAF)
   - Implement DDoS protection

## 🚀 Production Readiness

### What's Implemented
- ✅ Comprehensive logging
- ✅ Error handling and validation
- ✅ Unit tests with good coverage
- ✅ Docker containerization
- ✅ Health checks
- ✅ Basic authentication
- ✅ Actuator for monitoring
- ✅ Clean architecture

### Production Improvements

#### Infrastructure
1. **Database**:
   - Migrate to PostgreSQL/MySQL
   - Implement connection pooling (HikariCP already configured)
   - Database migrations (Flyway/Liquibase)
   - Read replicas for scaling
   - Regular backups and disaster recovery

2. **Caching**:
   - Redis for member eligibility cache
   - Procedure cost cache to reduce database load
   - Cache invalidation strategy

3. **Message Queue**:
   - RabbitMQ/Kafka for async claim processing
   - Event-driven architecture for real-time notifications
   - Dead letter queues for failed claims

4. **API Gateway**:
   - Kong/Nginx for API management
   - Request routing and load balancing
   - Centralized authentication

#### Application Enhancements
1. **Observability**:
   - Distributed tracing (Zipkin/Jaeger)
   - Centralized logging (ELK Stack)
   - Metrics dashboard (Prometheus + Grafana)
   - APM tools (New Relic, DataDog)

2. **Performance**:
   - Database query optimization
   - Pagination for list endpoints
   - Async processing for heavy operations
   - Connection pool tuning

3. **Resilience**:
   - Circuit breakers (Resilience4j)
   - Retry mechanisms
   - Fallback strategies
   - Graceful degradation

4. **Testing**:
   - Integration tests
   - Contract testing (Pact)
   - Performance/load testing (JMeter, Gatling)
   - Security scanning (OWASP ZAP)

5. **CI/CD**:
   - Automated build pipeline
   - Automated testing in pipeline
   - Blue-green deployments
   - Feature flags
   - Automated rollback capability

6. **Compliance**:
   - HIPAA compliance for health data
   - Data encryption at rest and in transit
   - Audit trail for all operations
   - Data retention policies
   - Regular security audits

7. **Business Logic**:
   - Rule engine for complex claim validation
   - ML-based fraud detection
   - Integration with external verification services
   - Real-time pricing APIs
   - Provider network validation

## 🗂️ Project Structure

```
claims-service/
├── src/
│   ├── main/
│   │   ├── java/com/ginja/claimsservice/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── exception/       # Exception handlers
│   │   │   ├── model/           # JPA entities
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── service/         # Business logic
│   │   │   └── ClaimsServiceApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/ginja/claimsservice/
│           ├── controller/      # Controller tests
│           └── service/         # Service tests
├── Dockerfile                   # Docker build configuration
├── docker-compose.yml          # Docker Compose setup
├── pom.xml                     # Maven dependencies
└── README.md
```

## 📚 Mock Data

The application includes mock data for testing:

### Active Members
- M123, M456, M789

### Procedure Costs
- P001: KES 20,000
- P002: KES 15,000
- P003: KES 100,000

### Max Benefit
- KES 40,000 per claim

## 🤝 Contributing

This is a demonstration project for Ginja AI recruitment. For a production implementation, consider:
- Code reviews and PR process
- Contribution guidelines
- Branching strategy
- Semantic versioning

## 📝 License

This project is created as part of the Ginja AI technical assessment.

## 📧 Contact

For questions or clarifications about this implementation:
- Refer to the submission email
- Check the API_EXAMPLES.md for usage examples

---

**Built with ❤️ for Ginja AI Technical Assessment**
