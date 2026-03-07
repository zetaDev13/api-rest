# ZetaSoft API

Modern REST API built with Spring Boot 3.4 and Java 21.

## Features

- **Layered Architecture**: Controller -> Service -> Repository
- **CRUD Operations**: Complete user management API
- **Validation**: Jakarta Bean Validation
- **Exception Handling**: Global exception handler with consistent error responses
- **JWT Authentication**: Secure API with JSON Web Tokens
- **Database**: H2 in-memory (easily switchable to Oracle, PostgreSQL, MySQL)
- **Testing**: Unit tests and integration tests
- **API Documentation**: Swagger UI for interactive API testing

## Quick Start

### Prerequisites
- Java 21
- Maven 3.9+

### Run the Application

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### Authentication

Default credentials (for testing):
- **Username**: `admin`
- **Password**: `admin123`

To access protected endpoints, first obtain a JWT token:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Then use the token in subsequent requests:

```bash
curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer <your-token>"
```

### API Documentation (Swagger UI)

Access the interactive API documentation at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

From Swagger UI you can:
- View all available endpoints
- Test API calls directly from the browser
- See request/response schemas
- Execute POST, PUT, DELETE operations interactively

### H2 Console
Access the H2 console at `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:apidb`
- Username: `sa`
- Password: (empty)

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/auth/login | Login and get JWT token |

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/users | Get all users |
| GET | /api/v1/users/{id} | Get user by ID |
| POST | /api/v1/users | Create new user |
| PUT | /api/v1/users/{id} | Update user |
| DELETE | /api/v1/users/{id} | Delete user |

## Example Request

**Login**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Create User**
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123",
    "name": "John Doe"
  }'
```

## Testing

```bash
mvn test
```

## Technology Stack

- Spring Boot 3.4.1
- Java 21
- Spring Data JPA
- Spring Security
- JWT (jjwt)
- H2 Database
- Lombok
- MapStruct
- JUnit 5
- SpringDoc OpenAPI

## License

MIT
