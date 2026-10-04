# Seat Reservation Service

A production-oriented seat reservation service built with Java and Spring Boot, designed to remain correct under concurrent reservation attempts.

The primary focus of the implementation is **correctness under contention**:

- A seat must never be confirmed for two users.
- A user must not exceed the per-show booking limit.
- Retrying a request must not create a duplicate reservation.
- Multi-seat reservations are all-or-nothing.
- Cancellation must release seats safely.
- The service exposes health, readiness, metrics and operational information.

---

## Deliverables

| Deliverable | Link |
|---|---|
| Public Git repository | [GitHub Repository](https://github.com/Rishabrp99/seat-reservation-service) |
| Live service | [https://seat-reservation-service-55hx.onrender.com](https://seat-reservation-service-55hx.onrender.com) |
| Burst test | [`hot_seat_20k.py`](./hot_seat_20k.py) |
| Design write-up | [`WRITEUP.md`](./WRITEUP.md) |
| Health | [`/health`](https://seat-reservation-service-55hx.onrender.com/health) |
| Actuator health | [`/actuator/health`](https://seat-reservation-service-55hx.onrender.com/actuator/health) |
| Readiness | [`/actuator/health/readiness`](https://seat-reservation-service-55hx.onrender.com/actuator/health/readiness) |
| Liveness | [`/actuator/health/liveness`](https://seat-reservation-service-55hx.onrender.com/actuator/health/liveness) |
| Prometheus metrics | [`/actuator/prometheus`](https://seat-reservation-service-55hx.onrender.com/actuator/prometheus) |

The repository contains the complete commit history showing the incremental development of the service.

---

## Tech Stack

- **Java 21**
- **Spring Boot**
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security
- PostgreSQL
- Flyway dependency
- Micrometer / Prometheus
- Docker
- Docker Compose

---

# Architecture

At a high level:

```text
                         HTTP Clients
                              |
                              v
                    +-------------------+
                    |   Spring Boot     |
                    |    REST API       |
                    +---------+---------+
                              |
                    +---------v---------+
                    | Reservation       |
                    | Service           |
                    +---------+---------+
                              |
                 +------------+-------------+
                 |                          |
                 v                          v
        +----------------+        +------------------+
        | PostgreSQL     |        | Prometheus /     |
        |                |        | Actuator metrics |
        +----------------+        +------------------+
