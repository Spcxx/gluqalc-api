# GluQalc API

![Java](https://img.shields.io/badge/Java-25-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-6DB33F.svg)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![Redis](https://img.shields.io/badge/Redis-8.6.1-red.svg)

Backend API for diabetes and nutrition tracking. It provides authentication, consent handling, user profiles, meal logging, product management, statistics, Redis-backed sessions, OpenAPI docs and Open Food Facts integration.

---

## Disclaimers

[![Medical Disclaimer](https://img.shields.io/badge/%E2%9A%A0%EF%B8%8F_Medical_Disclaimer-Read_Before_Use-red?style=for-the-badge)](./MEDICAL_DISCLAIMER.md)

- **Regulatory Disclaimer**: This software and its codebase have not been evaluated, cleared or approved by any regulatory health authority.
- **Intended Use**: This project is provided strictly for educational, research and academic purposes only. It is not a certified medical device and must not be used for clinical diagnosis, medical treatment or management of any health condition.

---

## Docker installation

Requirements: Docker and Docker Compose.

The compose stack starts PostgreSQL, Redis, Adminer and the API container.


1. Copy `.env.template` to `.env` and fill the values:

```bash
cp .env.template .env
```

2. Run the stack:

```bash
docker-compose up -d --build
```

API: `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui.html`

Adminer: `http://localhost:8081`

---

## Usage

While the GluQalc API is fully functional on its own and can be interacted with directly via HTTP requests or the Swagger UI documentation, it is primarily designed to be used alongside its official companion multiplatform client application: https://github.com/Spcxx/gluqalc-app

---

## What is in the project

- JWT auth with refresh tokens and Google login
- email verification, account deletion, password reset and email change with email code verification
- consent acceptance flow required to use the API
- user profile, biometrics, nutrition targets, profile history
- meal categories and meal entries with real-time calculations
- product CRUD, barcode lookup, external import, moderation flows
- daily summaries and statistics export
- OpenAPI docs and Swagger UI

---

## Environment variables

`.env.template` contains:

| Variable | Meaning                            |
| :--- |:-----------------------------------|
| `SPRING_PROFILES_ACTIVE` | Active profile (dev, prod)         |
| `DB_URL` | PostgreSQL JDBC URL                |
| `DB_USERNAME` | Database user                      |
| `DB_PASSWORD` | Database password                  |
| `REDIS_HOST` | Redis host                         |
| `REDIS_PORT` | Redis port                         |
| `JWT_SECRET` | JWT signing secret key             |
| `GOOGLE_CLIENT_ID` | Google OAuth client id             |
| `GOOGLE_CLIENT_SECRET` | Google OAuth client secret         |
| `MAIL_HOST` | SMTP host                          |
| `MAIL_PORT` | SMTP port                          |
| `MAIL_USERNAME` | SMTP username                      |
| `MAIL_PASSWORD` | SMTP password                      |
| `MAIL_FROM` | Sender email address               |
| `OFF_USER_AGENT` | Open Food Facts user agent         |
| `OFF_WRITE_USER_ID` | Open Food Facts write API user     |
| `OFF_WRITE_PASSWORD` | Open Food Facts write API password |
| `OFF_WRITE_APP_UUID` | Open Food Facts app UUID           |
| `OFF_CONTACT_EMAIL` | Open Food Facts contact email      |
| `DB_ENCRYPTION_KEY` | Database encryption key            |

---

## Stack

- Spring Boot 4.0.2
- Java 25
- PostgreSQL
- Redis
- Flyway
- Spring Security
- Springdoc OpenAPI
- Auth0 JWT
- Google API client
- Mail + Thymeleaf

---

## Config and runtime

- `application.yaml` configures Flyway, OpenAPI, Redis, database connection pooling, mail, Spring Boot Actuator (health/metrics) and custom JWT/session/rate-limiting properties.
- `application-dev.yaml` sets logging levels to `DEBUG` for application and Hibernate SQL.
- `application-prod.yaml` restricts logging levels to `INFO` and `WARN` for SQL for cleaner production output.
- Logs (info, errors, requests) are written to the `logs/` directory.
---

## Database

Flyway migrations live in `src/main/resources/db/migration`.

The schema covers:

- users, roles and auth providers
- device sessions
- consents and user consents
- products, names, portions and product changes
- meal categories and meal entries
- user profiles and profile history

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.