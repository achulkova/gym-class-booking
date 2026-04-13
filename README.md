# Gym Class Booking API

A RESTful API for managing gym classes and bookings with **JWT authentication**, built using **Spring Boot**, with a lightweight **Vanilla JavaScript frontend**.
This project demonstrates a complete backend application with authentication, validation, business rules, and comprehensive testing.

---

## Tech Stack

* **Java 21**
* **Spring Boot**
* Spring Web
* Spring Data JPA
* Spring Security
* JWT (jjwt)
* H2 Database
* Maven
* JUnit 5 + Mockito
* HTML, CSS, Vanilla JavaScript (Frontend)

## Features

### Authentication (JWT)

* User registration (`POST /auth/register`)
* Login with JWT token (`POST /auth/login`)
* Role-based access control (`USER`, `ADMIN`)
* Stateless authentication using **JWT**

---

### Frontend

* Simple UI built with **HTML, CSS, and Vanilla JavaScript**
* Communicates with backend via `fetch` API
* Role-based interface (different features for USER and ADMIN)
* Booking, class management, and search available via UI
* Uses **localStorage** to persist JWT session

---

### Gym Classes

* Create, update, delete classes (**ADMIN only**)
* Get all classes (with pagination & sorting)
* Search classes by instructor
* View class details with bookings

---

### Bookings

* Book a spot in a class (**USER / ADMIN**)
* View bookings per class
* Delete booking (**ADMIN only**)

---

### Business Rules

* ❗ Prevent overbooking
  → Returns **409 Conflict** when class is full

---

### Validation & Error Handling

* Bean Validation (`@NotBlank`, `@Email`, etc.)
* Global exception handling
* Standardized error responses:

```json
{
  "status": 409,
  "message": "GymClass is full",
  "timestamp": "2026-03-15T10:15:30"
}
```

---

## Running the Project

### 1. Clone repo

```bash
git clone https://github.com/achulkova/05_java_enterprice_assignment_4_individual.git
```

### 2. Run backend

```bash
mvn spring-boot:run
```

### 3. Open application

Open in your browser: http://localhost:8080/

## 🌐 API Access

* Swagger UI:
  http://localhost:8080/swagger-ui.html

* H2 Console:
  http://localhost:8080/h2-console


---

## Testing

This project includes **multiple test types**:

* ✅ Controller tests (`@WebMvcTest`)
* ✅ Service unit tests (Mockito)
* ✅ Repository tests (`@DataJpaTest`)
* ✅ Integration tests (`@SpringBootTest` + JWT)

Run all tests:

```bash
mvn test
```

---

## HTTP Requests

Pre-configured requests are available in:

```
generated-requests.http
```

Includes:

* Login flow
* JWT usage
* Protected endpoints
* Error scenarios (401, 403, 404, 409)


