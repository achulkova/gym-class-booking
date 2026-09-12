# Gym Class Booking

Gym Class Booking is a full-stack web application for managing gym classes and bookings, featuring JWT-based authentication, role-based access control, and a lightweight Vanilla JavaScript frontend.

The project demonstrates backend development with Spring Boot, REST APIs, authentication and authorization, database persistence, validation, business logic, automated testing, and frontend integration.

---

## Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Spring Security
- JWT (JJWT)
- Spring Validation
- Maven

### Database

- H2 Database

### Testing

- JUnit 5
- Mockito
- Spring Boot Test
- Spring MVC Test
- Spring Data JPA Test

### API Documentation

- Swagger / OpenAPI
- SpringDoc

### Frontend

- HTML
- CSS
- Vanilla JavaScript
- Fetch API
- LocalStorage

---

## Features

### Authentication & Authorization

- User registration
- Login with JWT authentication
- Stateless authentication using JWT
- Password hashing with BCrypt
- Role-based access control
- `USER` and `ADMIN` roles
- Protected REST API endpoints

### Gym Class Management

- View available gym classes
- View class details
- Create gym classes
- Update gym classes
- Delete gym classes
- Pagination and sorting
- Search classes by instructor
- View bookings associated with a class

Class management operations are restricted according to user roles.

### Booking Management

- Book a spot in a gym class
- View bookings for a class
- Delete bookings
- Capacity validation to prevent overbooking

When a class reaches its maximum capacity, the API returns:

```text
409 Conflict
```

### Frontend

The application includes a lightweight frontend built with HTML, CSS, and Vanilla JavaScript.

The frontend:

- Communicates with the backend using the Fetch API
- Supports user registration and login
- Stores the JWT session in LocalStorage
- Provides role-based functionality
- Allows users to browse and search gym classes
- Allows authenticated users to create bookings
- Provides administrative class and booking management

---

## REST API

The application exposes REST endpoints for authentication, gym classes, and bookings.

Examples:

```text
POST /auth/register
POST /auth/login
```

Authentication-protected endpoints require a JWT token.

Swagger UI provides interactive documentation for the complete API.

---

## Validation & Error Handling

The application uses Bean Validation for request validation, including annotations such as:

```text
@NotBlank
@Email
```

A global exception handling mechanism provides standardized API error responses.

Example:

```json
{
  "status": 409,
  "message": "GymClass is full",
  "timestamp": "2026-03-15T10:15:30"
}
```

---

## Security

The application uses Spring Security with stateless JWT authentication.

The JWT signing secret is **not stored in the source code**.

Instead, the application reads it from the following environment variable:

```text
JWT_SECRET
```

The application configuration references it as:

```properties
jwt.secret=${JWT_SECRET}
```

This allows different secrets to be used for local development and production without exposing credentials in the repository.

---

## Running the Project Locally

### 1. Clone the repository

```bash
git clone https://github.com/achulkova/gym-class-booking.git
cd gym-class-booking
```

### 2. Configure the JWT secret

The application requires the `JWT_SECRET` environment variable.

#### Windows PowerShell

```powershell
$env:JWT_SECRET="your-secret-key"
```

Use a sufficiently long random value for the secret.

### 3. Run the application

Using the Maven Wrapper:

#### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

#### macOS / Linux

```bash
./mvnw spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

Open the application in your browser:

```text
http://localhost:8080/
```

---

## API Documentation

After starting the application, Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI documentation is available at:

```text
http://localhost:8080/v3/api-docs
```

---

## Database

The project currently uses an in-memory H2 database.

The database is initialized when the application starts and recreated when the application restarts.

For local development, the H2 Console is available at:

```text
http://localhost:8080/h2-console
```

Default local configuration:

```text
JDBC URL: jdbc:h2:mem:gymDB
Username: sa
Password: sa
```

---

## Testing

The project includes multiple levels of automated testing.

### Controller Tests

Controller behavior is tested using:

```text
@WebMvcTest
```

### Service Tests

Business logic is tested using JUnit 5 and Mockito.

### Repository Tests

Database repository behavior is tested using:

```text
@DataJpaTest
```

### Integration Tests

Application flows, including authentication and JWT-protected functionality, are tested using:

```text
@SpringBootTest
```

Run all tests with:

#### Windows

```powershell
.\mvnw.cmd test
```

#### macOS / Linux

```bash
./mvnw test
```

---

## HTTP Requests

Preconfigured HTTP requests for testing the API are available in:

```text
generated-requests.http
```

They include examples for:

- Registration and login
- JWT authentication
- Protected endpoints
- Gym class operations
- Booking operations
- Authorization scenarios
- Error responses such as `401`, `403`, `404`, and `409`

---

## Project Structure

```text
src/
├── main/
│   ├── java/
│   │   └── se.edugrade.java25.enterprise.gym/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── exception/
│   │       ├── model/
│   │       ├── repository/
│   │       ├── security/
│   │       └── service/
│   │
│   └── resources/
│       ├── static/
│       │   ├── index.html
│       │   ├── app.js
│       │   └── style.css
│       ├── application.properties
│       └── data.sql
│
└── test/
```

The Spring Boot application serves both the REST API and the static frontend.

---

## Architecture

```text
Browser
   │
   │ HTML / CSS / JavaScript
   │
   ▼
Spring Boot Application
   │
   ├── REST Controllers
   │
   ├── Spring Security + JWT
   │
   ├── Service Layer
   │
   ├── Spring Data JPA / Hibernate
   │
   ▼
H2 Database
```

---

## Repository
