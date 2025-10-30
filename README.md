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
- [License](#license)  

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
