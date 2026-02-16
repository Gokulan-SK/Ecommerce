# E-Commerce Log Generator

This is a Spring Boot 3.4 monolithic e-commerce application designed as a **Controlled Log Generator** for Machine Learning-based Log Anomaly Detection.

## Technology Stack

- **Java**: 21
- **Spring Boot**: 3.4.0
- **Build Tool**: Maven
- **Database**: H2 In-Memory
- **Security**: Spring Security 6 + JWT
- **Template Engine**: Thymeleaf
- **UI Framework**: Bootstrap 5
- **Logging**: Structured ECS JSON format (Logback + Logstash encoder)
- **AOP**: Spring AOP for service-level logging

## Project Structure

```
src/main/java/com/ecommerce/
├── EcommerceApplication.java    # Main application class
├── controller/                  # REST and Web controllers
├── service/                     # Business logic layer
├── repository/                  # Spring Data JPA repositories
├── entity/                      # JPA entities
├── dto/                         # Data Transfer Objects
├── config/                      # Configuration classes
├── security/                    # Security filters and JWT utilities
├── logging/                     # Structured logging infrastructure
├── chaos/                       # Chaos engineering components
└── util/                        # Utility classes

src/main/resources/
├── application.properties       # Application configuration
├── logback-spring.xml          # Structured logging configuration
├── schema.sql                  # Database schema (to be created)
├── data.sql                    # Seed data (to be created)
└── templates/                  # Thymeleaf templates (to be created)
```

## Quick Start

### Prerequisites
- Java 21
- Maven 3.8+

### Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

### Access Points

- **Application**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:ecommerce_db`
  - Username: `sa`
  - Password: (leave empty)

## Configuration

### JWT Settings
- Secret key configured in `application.properties`
- Token expiration: 24 hours

### Chaos Engineering
Chaos features can be toggled in `application.properties`:
- `chaos.random.error.enabled` - Random 500 error injection (5% probability)
- `chaos.payment.bypass.enabled` - Payment simulation bypass
- `chaos.db.latency.spike.enabled` - Database latency simulation
- `chaos.force.auth.failure.enabled` - Force authentication failures

### Logging
Structured logs are written to:
- **File**: `logs/ecommerce_sentinel.json` (ECS JSON format)
- **Console**: Human-readable format for development

## Phase 1 Status

✅ **Completed**:
- Maven project structure
- `pom.xml` with all dependencies
- `application.properties` configuration
- `logback-spring.xml` for structured logging
- Base package structure
- Main application class

⏳ **Next Phases**:
- Phase 2: Database layer (schema, entities, repositories)
- Phase 3: Security & Authentication (JWT, Spring Security)
- Phase 4: Logging Infrastructure (AOP, Interceptors)
- Phase 5: Business Modules (Auth, Product, Cart, Order, Payment)
- Phase 6: Chaos Engineering
- Phase 7: UI Layer (Thymeleaf + Bootstrap)

## License

Educational / Research Project
