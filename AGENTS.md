# API REST - Spring Boot Project

## Project Overview
- **Project name**: api-rest
- **Framework**: Spring Boot 3.4.1 with Java 21
- **Architecture**: Layered Architecture (Controller -> Service -> Repository)
- **Database**: H2 (development), compatible with any JPA database (Oracle, PostgreSQL, MySQL)

## Technology Stack
- Spring Boot 3.4.1
- Java 21
- Spring Data JPA
- Spring Security
- H2 Database (dev/test)
- MapStruct for DTO mapping
- Lombok
- JUnit 5 + Mockito (Testing)

## Package Structure
```
com.tuempresa.api/
├── controller/      # REST endpoints
├── service/         # Business logic
├── repository/      # Data access (Spring Data JPA)
├── model/
│   ├── entity/      # JPA entities
│   └── dto/         # Data Transfer Objects
├── mapper/          # MapStruct mappers
├── exception/       # Custom exceptions + GlobalExceptionHandler
├── config/          # Configuration classes
└── security/       # Security configuration
```

## Coding Conventions
1. **DTOs**: Separate Request/Response objects, never expose entities directly
2. **Services**: Use interfaces when needed for testing, always use @Transactional
3. **Exceptions**: Throw domain-specific exceptions (ResourceNotFoundException, ConflictException)
4. **Validation**: Use Jakarta validation annotations (@NotBlank, @Email, @Size, etc.)
5. **Naming**: Use meaningful names, follow Java conventions (camelCase, PascalCase)
6. **Testing**: Write unit tests for services, integration tests for controllers

## API Conventions
- Base URL: `/api/v1/`
- RESTful principles: GET (read), POST (create), PUT (update), DELETE (remove)
- Consistent response format via GlobalExceptionHandler

## Testing Strategy
- Unit tests: Service layer with Mockito
- Integration tests: Controller layer with @SpringBootTest + MockMvc
- Run tests: `mvn test`

## Common Commands
```bash
mvn spring-boot:run   # Run application
mvn test             # Run tests
mvn clean package    # Build JAR
```
