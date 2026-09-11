# Employee Leave Management System

A multi-level leave approval system (Employee → Manager → HR) built with **Spring Boot, Hibernate, and MySQL**, with role-based access control via **Spring Security**.

## Features
- Multi-level approval workflow: Employee submits → Manager approves → HR gives final approval
- Automated leave-balance calculation on approval
- Conflict detection: manager approvals are blocked if too many team members would be on leave in the same window (staffing threshold check)
- Role-based access control (`EMPLOYEE`, `MANAGER`, `HR`) using Spring Security
- REST APIs for applying, approving, rejecting, and tracking leave requests
- JUnit test coverage for approval-chain and leave-balance logic

## Tech Stack
- Java 17, Spring Boot 3, Spring Data JPA (Hibernate), Spring Security, MySQL, Maven, JUnit 5 + Mockito

## Running Locally
1. Create a MySQL database `leave_management_db`
2. Update credentials in `src/main/resources/application.properties`
3. `mvn spring-boot:run`

## Running Tests
```
mvn test
```

## Author
**Sachin Rathod** — MCA Student | Aspiring Java Developer
