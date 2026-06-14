# Book Library System

A RESTful API for managing a simple library: register books and borrowers, list books, and borrow / return individual book copies.

Built with **Java 17** and **Spring Boot 3.5**.

## Architecture Overview

Classic layered Spring Boot architecture:

```
HTTP request
   │
   ▼
 Controller   (booklibrarysystem.controller)   ← request/response DTOs, validation
   │
   ▼
 Service      (booklibrarysystem.service.impl) ← business rules, @Transactional
   │
   ▼
 Repository   (booklibrarysystem.repository)   ← Spring Data JPA interfaces
   │
   ▼
   DB         (PostgreSQL / H2)
```

Cross-cutting:
- `GlobalExceptionHandler` translates domain exceptions to HTTP responses with a consistent `ErrorResponse` body.
- Request DTOs use Bean Validation (`@NotBlank`, `@Email`, `@Pattern`, etc.); validation failures surface as HTTP 400.

---

## Quick Start

### Prerequisites

- JDK 17
- Maven wrapper is included (`./mvnw` on Linux/macOS, `mvnw.cmd` on Windows) — no Maven install needed.
- Docker + Docker Compose for the containerised flow.

### Option 1 — Run locally with H2 (dev profile, fastest)

The default profile is `dev`, which uses an in-memory H2 database. No setup required.

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`.

- Swagger UI:    `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON:  `http://localhost:8080/v3/api-docs`
- H2 console:    `http://localhost:8080/h2-console`
  (JDBC URL: `jdbc:h2:mem:librarydb`, user: `sa`, no password)

### Option 2 — Run against a local PostgreSQL

```bash
export SPRING_PROFILES_ACTIVE=dev
export DB_URL=jdbc:postgresql://localhost:5432/librarydb
export DB_USERNAME=library
export DB_PASSWORD=librarypass

./mvnw spring-boot:run
```

### Option 3 — Run via Docker Compose (app + PostgreSQL)

```bash
docker compose up --build
```

This starts:
- `db` — PostgreSQL 16 on `localhost:5433` (host) → `5432` (container)
- `app` — Spring Boot on `localhost:8080`, with `SPRING_JPA_HIBERNATE_DDL_AUTO=update` so the schema is auto-created on first run.
---

## API Reference
Base URL: `http://localhost:8080`
### 1. Register a borrower

```
POST /api/borrowers
Content-Type: application/json

{
  "name":  "Alice",
  "email": "alice@example.com"
}
```

**201 Created**
```json
{ "name": "Alice", "email": "alice@example.com" }
```

**409 Conflict** — email already registered.
**400 Bad Request** — missing/blank `name`, missing or invalid `email`.

```bash
curl -X POST http://localhost:8080/api/borrowers \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com"}'
```

---

### 2. Register a book

```
POST /api/book/register
Content-Type: application/json

{
  "isbn":   "9780132350884",
  "title":  "Clean Code",
  "author": "Robert C. Martin"
}
```

**201 Created**
```json
{ "isbn": "9780132350884", "title": "Clean Code", "author": "Robert C. Martin" }
```

**400 Bad Request** — missing fields, or ISBN not 10/13 digits.
**409 Conflict** — same ISBN already registered with a *different* title/author (ISBN must always map to the same book).

> Registering the **same** ISBN with the **same** title and author is allowed — this creates a new copy with a new auto-generated `id`. Multiple physical copies of the same book are supported.

```bash
curl -X POST http://localhost:8080/api/book/register \
  -H "Content-Type: application/json" \
  -d '{"isbn":"9780132350884","title":"Clean Code","author":"Robert C. Martin"}'
```

---

### 3. List all books

```
GET /api/book/books
```

**200 OK**
```json
[
  { "isbn": "9780132350884", "title": "Clean Code", "author": "Robert C. Martin" },
  { "isbn": "9780201633610", "title": "Design Patterns", "author": "Gamma et al." }
]
```

```bash
curl http://localhost:8080/api/book/books
```

> Returns every book in the catalogue. The response currently omits the `id` field; the book id is needed for borrow/return. See *Known Gaps* — id should be exposed in the response payload.

---

### 4. Borrow a book

```
POST /api/bookrecord/{bookId}/borrow
Content-Type: application/json

{ "borrowerId": 1 }
```

**200 OK**
```json
{
  "borrowRecordId": 7,
  "bookId": 3,
  "borrowerId": 1,
  "borrowedAt": "2026-06-14T10:15:30",
  "returnedAt": null
}
```

**404 Not Found** — `bookId` or `borrowerId` does not exist.
**409 Conflict** — that specific book (by id) is already borrowed by someone.

```bash
curl -X POST http://localhost:8080/api/bookrecord/3/borrow \
  -H "Content-Type: application/json" \
  -d '{"borrowerId":1}'
```

---

### 5. Return a book

```
POST /api/bookrecord/{bookId}/return
```

**200 OK** — same shape as the borrow response, with `returnedAt` populated.

**404 Not Found** — book id does not exist.
**409 Conflict** — the book is not currently borrowed.

```bash
curl -X POST http://localhost:8080/api/bookrecord/3/return
```

---

## Data Models

All entities extend `BaseEntity` which provides `id` (auto-generated), `createdTime`, and a `@Version` field for optimistic locking.

### Book
| Field   | Type   | Notes                           |
|---------|--------|---------------------------------|
| id      | Long   | PK, auto-generated              |
| isbn    | String | 10 or 13 digits, numeric only   |
| title   | String | non-blank                       |
| author  | String | non-blank                       |

### Borrower
| Field  | Type   | Notes                                |
|--------|--------|--------------------------------------|
| id     | Long   | PK, auto-generated                   |
| name   | String | non-blank                            |
| email  | String | valid email, application-unique      |

### BorrowRecord
| Field          | Type          | Notes                                                                 |
|----------------|---------------|-----------------------------------------------------------------------|
| id             | Long          | PK                                                                    |
| book           | Book          | many-to-one                                                           |
| borrower       | Borrower      | many-to-one                                                           |
| borrowedAt     | LocalDateTime | set on borrow                                                         |
| returnedAt     | LocalDateTime | `null` while active, set on return                                    |
| activeBookId   | Long          | `bookId` while borrow is active, `NULL` after return — **unique**     |

The unique constraint on `active_book_id` is the database-level guard that prevents two simultaneously-active records for the same book (see *Assumptions #6*).

---

## Error Format

All errors share the same JSON shape, produced by `GlobalExceptionHandler`:

```json
{
  "error": "Bad Request",
  "title": "isbn: ISBN must be 10 or 13 digits"
}
```

| HTTP status | Trigger                                                         |
|-------------|-----------------------------------------------------------------|
| 400         | Bean Validation failure on a request DTO                        |
| 404         | `ResourceNotFoundException` (unknown book / borrower id)        |
| 409         | `ConflictException` (duplicate email, double-borrow, etc.)      |
| 409         | `BookStateException` (ISBN re-registered with different title/author) |

---

## Database Choice

**Production: PostgreSQL.**

- **ACID guarantees** — borrowing must be atomic. The "no two active borrows of the same book" rule depends on transactional consistency *and* a unique constraint that the DB enforces even under concurrent requests.
- **Mature unique-constraint semantics with `NULL`s** — PostgreSQL treats `NULL`s as distinct in unique indexes, which is exactly what `BorrowRecord.activeBookId` relies on: many historical rows with `NULL` are allowed, but only one row per book may hold a non-null value.
- **Industry standard** — strong tooling, ops familiarity, broad managed-service availability (RDS, Cloud SQL, Aurora, Neon, etc.).
- **Open source / no licensing cost.**

**Dev/Test: H2 (in-memory, PostgreSQL compatibility mode).**

- Zero-install — `./mvnw spring-boot:run` is the only step required to develop locally.
- `MODE=PostgreSQL` keeps SQL dialect drift between dev and prod small (12-factor X — dev/prod parity).
- Test profile uses `create-drop`, giving every test run a clean schema.

---

## Assumptions

These are decisions made where the brief was silent or ambiguous.

1. **ISBN format.** ISBN is validated as a string of either 10 or 13 digits (regex `\d{10}|\d{13}`). Hyphenated forms (`978-0-13-235088-4`) and the trailing `X` of legacy ISBN-10 check digits are **not** accepted. Callers must normalise before sending.
2. **Returns are unauthenticated.** `POST /api/bookrecord/{bookId}/return` does not take a `borrowerId` or require any proof that the caller is the original borrower. Any caller who knows the book id can return it. The brief did not require otherwise; assumed acceptable for an internal/staff-facing API.
3. **No authentication or authorisation layer** is in scope. All endpoints are open. A real deployment would sit behind an auth proxy or add Spring Security.
---
## Project Structure

```
src/
├── main/
│   ├── java/booklibrarysystem/
│   │   ├── Application.java
│   │   ├── controller/         # BookController, BorrowerController, BorrowController
│   │   ├── service/            # interfaces
│   │   │   └── impl/           # BookServiceImpl, BorrowerServiceImpl, BorrowServiceImpl
│   │   ├── repository/         # Spring Data JPA interfaces
│   │   ├── model/              # JPA entities + BaseEntity
│   │   ├── dto/
│   │   │   ├── request/        # CreateBookRequest, CreateBorrowerRequest, BorrowRequest
│   │   │   └── response/       # BookResponse, BorrowerResponse, BorrowResponse, ErrorResponse
│   │   └── exception/          # ConflictException, ResourceNotFoundException, BookStateException, GlobalExceptionHandler
│   └── resources/
│       ├── application.properties
│       ├── application-dev.properties
│       ├── application-prod.properties
│       └── application-test.properties
├── test/java/booklibrarysystem/
│   ├── controller/             # @WebMvcTest slices
│   └── service/impl/           # service unit tests
Dockerfile
docker-compose.yml
.github/workflows/docker.yml
pom.xml
mvnw, mvnw.cmd
```
