# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PayrollSystem is a Mexican payroll system built with Spring Boot 3.4.1 and Java 17. It handles Mexican-specific payroll concepts including ISR (income tax), IMSS (social security), Aguinaldo, Prima Vacacional, and other statutory benefits. The application uses PostgreSQL for persistence and RabbitMQ for messaging, following a containerized architecture with Docker.

## Technology Stack

- **Framework**: Spring Boot 3.4.1
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: PostgreSQL 16
- **Messaging**: RabbitMQ 3 (AMQP)
- **ORM**: Spring Data JPA / Hibernate
- **Containerization**: Docker with multi-stage builds

## Essential Commands

### Local Development (without Docker)

Requires PostgreSQL and RabbitMQ running locally:

```bash
# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=PayrollSystemApplicationTests

# Run a single test method
./mvnw test -Dtest=PayrollSystemApplicationTests#testMethod

# Build JAR (skipping tests)
./mvnw clean package -DskipTests

# Build JAR (running tests)
./mvnw clean package
```

### Docker Development

```bash
# Start all services (PostgreSQL, RabbitMQ, app)
docker-compose up --build

# Start services in background
docker-compose up -d --build

# Stop all services
docker-compose down

# Stop and remove volumes (clears database)
docker-compose down -v

# View logs
docker-compose logs -f payroll_app

# Rebuild only the application container
docker-compose up --build payroll_app
```

### Accessing Services

- **Application**: http://localhost:8080
- **RabbitMQ Management UI**: http://localhost:15672 (guest/guest)
- **PostgreSQL**: localhost:5432/payroll_db

## Architecture & Configuration

### Environment-Based Configuration

The application uses environment variables with fallback defaults in `application.properties`:

- **Docker environment**: Uses credentials from `docker-compose.yml`
  - DB: `payroll_user`/`secret_password`@`postgres_db:5432/payroll_db`
  - RabbitMQ: `rabbitmq_broker:5672`

- **Local environment**: Uses default values
  - DB: `postgres`/`postgres`@`localhost:5432/payroll_db`
  - RabbitMQ: `localhost:5672`

### Docker Architecture

The `docker-compose.yml` defines three services connected via `payroll-net` bridge network:

1. **postgres_db**: PostgreSQL with health checks and persistent volume
2. **rabbitmq_broker**: RabbitMQ with management interface
3. **payroll_app**: Spring Boot application that depends on healthy database and messaging services

The application uses a multi-stage Dockerfile:
- **Stage 1 (builder)**: Maven build with dependency caching
- **Stage 2 (runtime)**: Minimal JRE image running the compiled JAR

Tests are skipped during Docker build (use `-DskipTests=true`) since database is not available during build time.

### Package Structure

Base package: `mx.payroll.system`

The project follows Domain-Driven Design (DDD) structure:
- **Domain Layer**:
  - Models: `mx.payroll.system.domain.model` - JPA entities (e.g., Employee, Concept, PayrollResult)
  - Repositories: `mx.payroll.system.domain.repository` - Data access interfaces
- **Application Layer**:
  - Services: `mx.payroll.system.service` - Business logic and orchestration
  - DTOs: `mx.payroll.system.dto` - Data Transfer Objects for API contracts
- **Infrastructure Layer**:
  - Controllers: `mx.payroll.system.controller` - REST API endpoints
  - Messaging: `mx.payroll.system.messaging` - RabbitMQ listeners/publishers

**Current Implementation:**
- Employee entity with full CRUD operations (domain/model/Employee.java)
- EmployeeRepository with custom query methods (domain/repository/EmployeeRepository.java)
- EmployeeService with business logic (service/EmployeeService.java)

### Database Schema Management

The application uses SQL scripts for schema initialization:
- `schema.sql`: Creates database tables (executed on startup)
- `data.sql`: Loads initial data including concepts, formulas, and tax tables

**JPA Configuration:**
- `spring.jpa.hibernate.ddl-auto=none` - Schema creation is managed by SQL scripts, not JPA
- `spring.sql.init.mode=always` - SQL scripts are executed on every startup

**Database Schema:**
The system follows a flexible payroll engine design:
- `concepts`: Catalog of earnings (EARNING) and deductions (DEDUCTION)
- `concept_formulas`: Formula expressions with versioning support
- `employees`: Employee master data
- `employee_concept_values`: Fixed values per employee (e.g., base salary)
- `payroll_periods`: Payroll processing periods
- `payroll_results`: Calculated payroll headers
- `payroll_result_details`: Line items of each payroll
- `tax_tables`: Mexican ISR tax brackets (2024 tables included)

## Key Configuration Files

- `pom.xml`: Maven dependencies and build configuration
- `application.properties`: Spring configuration with environment variable support
- `docker-compose.yml`: Multi-container orchestration with health checks
- `Dockerfile`: Multi-stage build optimized for layer caching
