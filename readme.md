# Event Management System

Enterprise-level Event Management System built with Spring Boot 3.x and MySQL.

## Features

- JWT Authentication & Authorization
- Role-Based Access Control (SUPER_ADMIN, ADMIN, ATTENDEE)
- User Management (CRUD)
- Password Management (Change & Reset)
- Activity Audit Logging
- Login/Logout History
- Password Change History

## Technology Stack

- **Backend:** Spring Boot 3.2.0
- **Security:** Spring Security 6.x + JWT
- **Database:** MySQL 8.0
- **ORM:** Spring Data JPA
- **Build Tool:** Maven
- **Documentation:** Swagger/OpenAPI

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0+

## Setup

### 1. Clone Repository

git clone https://github.com/nabilnko/event-management.git
cd event-management

### 2. Configure Database

Create database:

CREATE DATABASE eventdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

Update `src/main/resources/application.properties`:

spring.datasource.url=jdbc:mysql://localhost:3306/eventdb
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
jwt.secret=YOUR_SECRET_KEY
### 3. Build & Run

mvn clean install
mvn spring-boot:run
Application starts at: `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Default Credentials

**Username:** `superadmin`  
**Password:** `admin123`

⚠️ Change in production!

## API Endpoints

### Authentication
- `POST /auth/login` - User login
- `POST /auth/logout` - User logout
- `GET /auth/validate-token` - Validate JWT

### User Management
- `POST /users` - Create user (SUPER_ADMIN)
- `GET /users` - List users (SUPER_ADMIN, ADMIN)
- `GET /users/{id}` - Get user (SUPER_ADMIN, ADMIN)
- `PUT /users/{id}` - Update user (SUPER_ADMIN)
- `DELETE /users/{id}` - Delete user (SUPER_ADMIN)

### Password Management
- `POST /users/change-my-password` - Change own password (All)
- `PATCH /users/{id}/reset-password` - Reset password (SUPER_ADMIN)

### History
- `GET /history/my-activities` - Own activities (All)
- `GET /history/my-logins` - Own logins (All)
- `GET /history/my-password-changes` - Own password changes (All)

## Project Structure

event-management/
├── src/
│ ├── main/
│ │ ├── java/com/example/eventmanagement/
│ │ │ ├── config/ # Configuration
│ │ │ ├── controller/ # REST Controllers
│ │ │ ├── dto/ # DTOs
│ │ │ ├── enums/ # Enums
│ │ │ ├── model/ # Entities
│ │ │ ├── repository/ # Repositories
│ │ │ ├── security/ # Security
│ │ │ ├── service/ # Services
│ │ │ └── util/ # Utils
│ │ └── resources/
│ │ └── application.properties
│ └── test/
├── pom.xml
└── README.md

## Author

Nabil Kowsar Orbe - [GitHub](https://github.com/nabilnko)

---

**Version:** 1.0.0  
**Last Updated:** December 10, 2025
