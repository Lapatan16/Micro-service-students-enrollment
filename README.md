# Students & Enrollments Microservices Project

## Table of Contents

- [Project Overview](#project-overview)  
- [Architecture](#architecture)  
- [Technologies Used](#technologies-used)  
- [Modules](#modules)  
- [Database Configuration](#database-configuration)  
- [H2 Console Access](#h2-console-access)  
- [Resilience & Fault Tolerance](#resilience--fault-tolerance)  
- [Running the Project](#running-the-project)  
- [Endpoints](#endpoints)  
- [Validation & Error Handling](#validation--error-handling)  

---

## Project Overview

This project is a **microservices-based system** for managing students and their course enrollments. It demonstrates best practices in:

- Microservices architecture with **service discovery** (Eureka)  
- **RESTful APIs** for CRUD operations  
- **Validation** using Jakarta Bean Validation (`@Valid`, `@NotNull`, `@Pattern`)  
- **Resilience and fault tolerance** using Resilience4j (Circuit Breaker + Retry)  
- In-memory **H2 database** for fast development and testing  

The system consists of two main services:

1. **Students Service** — manages student data (full name, email, index number).  
2. **Enrollments Service** — manages course enrollments and validates students via Feign calls to Students Service.  

---

## Architecture

```mermaid
flowchart LR
    Eureka[Eureka Server]:::service
    Students[Students Service<br/>(CRUD on Student)]:::service
    Enrollments[Enrollments Service<br/>(CRUD on Enrollment)]:::service
    Feign[Feign Client]:::client

    Enrollments -->|Registers| Eureka
    Students -->|Registers| Eureka
    Enrollments -->|Validates studentId| Feign
    Feign --> Students

    classDef service fill:#f9f,stroke:#333,stroke-width:2px;
    classDef client fill:#bbf,stroke:#333,stroke-width:2px;
```

**Flow explanation:**

1. Enrollments Service calls Students Service via **Feign client** to validate `studentId`.  
2. If Students Service is down, **Resilience4j Circuit Breaker + Retry** prevents cascading failures.  
3. Enrollment details endpoint aggregates student data, falling back to `"UNKNOWN"` if Students Service is unavailable.  

---

## Technologies Used

- **Java 17**  
- **Spring Boot 3.x**  
- **Spring Data JPA** with Hibernate  
- **H2 Database** (in-memory for development)  
- **Spring Cloud OpenFeign** for inter-service calls  
- **Resilience4j** for fault tolerance  
- **Spring Cloud Eureka** for service discovery  
- **Jakarta Bean Validation** (`@NotNull`, `@Email`, `@Pattern`)  
- **Maven** for dependency management  

---

## Modules

### 1. Students Service
- Exposes endpoints to **create, read, update, and delete students**.  
- DTO (`StudentDTO`) ensures proper validation (full name, email, index number).  
- Returns **meaningful HTTP responses** on validation errors or conflicts.  

### 2. Enrollments Service
- Exposes endpoints to **create, read, update, and delete enrollments**.  
- Validates **student existence** via Feign client to Students Service.  
- DTO (`EnrollmentDTO`) ensures course code and semester follow proper patterns.  
- Aggregates student data for detailed views (`EnrollmentDetails`).  
- Uses **Resilience4j** to handle downtime of Students Service gracefully.  

---

## Database Configuration

Both services use **in-memory H2 databases**:

| Service              | JDBC URL                                         | User | Password |
|---------------------|-------------------------------------------------|------|----------|
| Students Service     | `jdbc:h2:mem:studentsdb;DB_CLOSE_DELAY=-1`     | sa   | (empty)  |
| Enrollments Service  | `jdbc:h2:mem:enrolldb;DB_CLOSE_DELAY=-1`       | sa   | (empty)  |

- The database is **in-memory**, so all data is lost when the service stops.  
- Hibernate `ddl-auto: update` ensures tables are created automatically on startup.  

---

## H2 Console Access

Both services expose an H2 console for database inspection:

| Service              | Console URL                     |
|---------------------|---------------------------------|
| Students Service     | `http://localhost:9081/h2`      |
| Enrollments Service  | `http://localhost:9082/h2`      |

- **JDBC URL:** same as `spring.datasource.url`  
- **User:** `sa`  
- **Password:** leave empty  
- Ensure **the service is running** before opening the console.  

---

## Resilience & Fault Tolerance

- **Circuit Breaker:** prevents cascading failures when Students Service is down.  
- **Retry:** automatically retries failed requests before triggering fallback.  
- **Fallbacks:**  
  - For `create`/`update` in Enrollments Service, if Students Service is down, a **503 Service Unavailable** is returned.  
  - For `details` endpoint, a partial response is returned with student fields marked `"UNKNOWN"`.  

---

## Running the Project

1. Make sure **Eureka Server** is running on `http://localhost:8761`.  
2. Start **Students Service**: runs on port `9081`.  
3. Start **Enrollments Service**: runs on port `9082`.  
4. Access H2 consoles if needed:

5. Use **REST client** (Postman, curl, etc.) to test endpoints.  

---

## Endpoints

### Students Service (`/students`)
| Method | Endpoint               | Description                     |
|--------|-----------------------|---------------------------------|
| GET    | `/students`            | List all students               |
| GET    | `/students/{id}`       | Get student by ID               |
| POST   | `/students`            | Create a new student            |
| PUT    | `/students/{id}`       | Update student                  |
| DELETE | `/students/{id}`       | Delete student                  |

### Enrollments Service (`/enrollments`)
| Method | Endpoint                         | Description                                 |
|--------|---------------------------------|---------------------------------------------|
| GET    | `/enrollments`                   | List all enrollments                        |
| GET    | `/enrollments/{id}`              | Get enrollment by ID                         |
| POST   | `/enrollments`                   | Create new enrollment                        |
| PUT    | `/enrollments/{id}`              | Update enrollment                            |
| DELETE | `/enrollments/{id}`              | Delete enrollment                            |
| GET    | `/enrollments/{id}/details`      | Get enrollment + student details             |

---

## Validation & Error Handling

- **Students Service** validates:
  - Full name: letters and spaces only  
  - Email: valid email format  
  - Index number: format `XXX/YYYY`  

- **Enrollments Service** validates:
  - Student ID: must exist and be positive  
  - Course code: 2–4 uppercase letters + 2–4 digits (e.g., `DS101`)  
  - Semester: format `1/2022`  

- **Meaningful responses:**
  - `404 Not Found` → for missing student or enrollment  
  - `409 Conflict` → for duplicate email or index number  
  - `400 Bad Request` → validation errors  
  - `503 Service Unavailable` → when dependent service is down

---
