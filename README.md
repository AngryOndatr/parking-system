Markdown
# 🅿️ Parking Lot Management System

## Project Overview
This is a comprehensive pet project implementing an automated parking lot management system. The system is designed to handle automatic and manual modes of operation, manage subscriptions, calculate payments, and track parking space occupancy. The architecture is based on microservices.

## 🚀 Technologies Used

| Category | Technologies |
| :--- | :--- |
| **Backend** | Java 17+, Spring Boot 3, Spring Security (JWT/OAuth2), REST API. |
| **Database** | PostgreSQL. |
| **Frontend** | React, TypeScript (recommended), React Router. |
| **Architecture** | Microservices (Split into Gate Control, Billing, Client/Subscription, Reporting). |
| **DevOps** | Docker, Docker Compose. |
| **Testing** | JUnit 5, Mockito, Spring Boot Test. |

## ⚙️ System Architecture (Microservices)

The project consists of the following core microservices:

1.  **`client-service`**: Manages the client database and subscriptions, and verifies their validity.
2.  **`gate-control-service`**: Receives events from scanners, decides on vehicle admission, and manages the barrier gates (via an emulator).
3.  **`billing-service`**: Calculates the cost for one-time parking sessions and records payments.
4.  **`management-service`**: Tracks available parking spaces and provides an API for the external information display board.
5.  **`	`**: Collects and stores all system logs, and generates reports.

## 🔑 Functional Requirements

* **Automatic Mode:** Free access for subscribers (via license plate recognition). Ticket issuance/payment for one-time visitors.
* **Manual Mode:** Ability for operators to manually control entry/exit and calculate fees (in case of automation failure).
* **Logging:** A log of all arrivals/departures, and an audit trail of operator/administrator actions.
* **Security:** Role-based authentication and authorization (`ADMIN`, `OPERATOR`) using Spring Security.

## 🛠️ Running the Project (Docker Compose)

Use Docker Compose for the quick deployment of the entire stack (PostgreSQL, all microservices, and Frontend).

**Prerequisites:** Docker and Docker Compose must be installed.

1.  **Build the Images:**
    ```bash
    # Build all Java services and the React interface
    ./mvnw clean install  # or the corresponding command for your build tool
    docker-compose build
    ```
2.  **Start the Services:**
    ```bash
    docker-compose up -d
    ```

### Default Access Points:

| Service | Address |
| :--- | :--- |
| **Backend API Gateway** (if implemented) | `http://localhost:8080` |
| **Frontend Web UI** | `http://localhost:3000` |
| **PostgreSQL** | `localhost:5432` |

## 💻 Development and Testing

### Folder Structure

-   `backend/`: Spring Boot microservice code.
-   `frontend/`: React web interface code.
-   `devops/`: Dockerfiles and `docker-compose.yml`.
-   `database/`: Migration scripts (e.g., Flyway or Liquibase).

### Running Tests

To run all Unit and Integration tests:
```bash
cd backend
./mvnw test

📝 Future Enhancements
Integration with a message broker (Kafka/RabbitMQ) for asynchronous communication.
Implementation of various subscription types (day/night, limited entry count).
Cloud deployment (AWS/GCP/Azure).
