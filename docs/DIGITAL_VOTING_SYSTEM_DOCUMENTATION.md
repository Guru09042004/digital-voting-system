# Digital Voting System
## Technical Documentation, Architecture, Workflows, and Setup

**Repository:** https://github.com/Guru09042004/digital-voting-system  
**Backend:** Java 17, Spring Boot 3.5.6  
**Frontend:** HTML5, CSS3, Vanilla JavaScript  
**Database:** PostgreSQL  
**Cache:** Redis  
**API style:** REST/JSON  
**Current backend port:** `8082`  
**Current frontend development port:** `8081`  
**Documentation status:** Based on the repository structure, configuration, and behavior verified during development. Re-check environment-specific values before deployment.

---

## 1. Project Overview

The Digital Voting System is a web application prototype for managing voters, elections, candidates, eligibility, authentication, and vote submission. The browser-based frontend communicates with a Java Spring Boot REST API. PostgreSQL persists application records, while Redis is configured as a cache/service dependency.

The backend is organized into controllers, DTOs, entities, repositories, services, security components, and application configuration. JWT-based authentication is used to identify authenticated requests, and role-based authorization separates voter-facing functionality from administrative functionality.

> **Important scope statement:** This is a development/academic project, not a certified election platform. A successful build and selected endpoint tests do not prove suitability for public/government elections. Before real-world use, the system would need independent security review, privacy and legal review, formal election procedures, accessibility testing, operational controls, disaster recovery, and independent verification of ballot secrecy and integrity.

## 2. Goals and Scope

### Main goals
- Provide a browser interface for voter registration/login and voting-related tasks.
- Expose REST endpoints for authentication, elections, candidates, voter dashboards, administration, and voting.
- Persist core records in PostgreSQL.
- Authenticate API requests with JWTs.
- Enforce authorization between voter and administrator routes.
- Prevent duplicate votes for the same voter/election using application validation and a database uniqueness constraint.
- Produce a receipt identifier after a successful vote operation.

### Known boundaries
- The application has been built and locally exercised; automated tests were skipped during the last successful Maven package command (`-DskipTests`).
- Redis is configured, but the exact cache usage and production topology must be confirmed in source/configuration before claiming that a particular operation is cached.
- OTP-related endpoints and services exist in the codebase; delivery provider, rate limits, expiration settings, and production behavior should be confirmed from the current configuration.
- Development-only endpoints such as `/api/dev/**` must not be exposed in production.
- A receipt is a confirmation/reference, not by itself cryptographic proof of an anonymous or independently auditable election.

## 3. Technology Stack

| Layer | Technology | Responsibility |
|---|---|---|
| Frontend | HTML5 | Page structure/forms |
| Frontend | CSS3 | Layout and styling |
| Frontend | Vanilla JavaScript | Browser interactions and API requests |
| Backend | Java 17 | Application runtime |
| Backend framework | Spring Boot 3.5.6 | REST API and application lifecycle |
| Security | Spring Security + JWT | Authentication and route authorization |
| Persistence | Spring Data JPA / Hibernate | Object-relational mapping and persistence |
| Database | PostgreSQL (tested with PostgreSQL 16.x in the local environment) | Durable application data |
| Cache/service | Redis | Configured cache/service dependency; verify exact usage |
| Build | Maven | Dependency management and packaging |
| Source control | Git + GitHub | Version history and remote repository |

## 4. High-Level Architecture

```text
+---------------------------+
| User's Web Browser        |
| HTML / CSS / JavaScript   |
| Frontend :8081 (dev)      |
+-------------+-------------+
              | HTTP REST / JSON
              v
+---------------------------+
| Spring Boot REST API      |
| :8082                     |
|                           |
| Controllers               |
| DTO validation/mapping    |
| Services / business rules |
| JWT filter + authorization|
| Repositories (JPA)        |
+---------+-----------+-----+
          |           |
          | JPA/JDBC  | Redis client (configured)
          v           v
+----------------+  +----------------+
| PostgreSQL     |  | Redis          |
| durable data   |  | cache/service  |
+----------------+  +----------------+
```

### Request path
1. A user opens a frontend HTML page in a browser.
2. JavaScript validates the input at a basic UI level and sends an HTTP request to the API.
3. For protected requests, the JWT authentication filter reads the `Authorization: Bearer <token>` header.
4. The security layer establishes the authenticated principal and applies route/role rules.
5. The controller receives the request and maps request/response DTOs.
6. The service layer applies business rules.
7. Repositories use JPA/Hibernate to read or write PostgreSQL data.
8. The API returns a JSON response and HTTP status.
9. The frontend updates the page based on the response.

## 5. Backend Package Responsibilities

The current backend source tree includes these principal areas:

| Package/path | Responsibility |
|---|---|
| `com.digitalvoting` | Spring Boot application entry point |
| `config/SecurityConfig.java` | HTTP security rules, filter-chain setup, route authorization |
| `controller/` | REST endpoints: authentication, elections, candidates, votes, dashboards, administration, health |
| `dto/` | Request and response objects exchanged over the API |
| `entity/` | JPA entities mapped to database tables |
| `exception/GlobalExceptionHandler.java` | Centralized API error handling |
| `repository/` | Spring Data JPA data access interfaces |
| `security/JwtService.java` | JWT creation, claim extraction, and validation |
| `security/JwtAuthenticationFilter.java` | Reads and validates bearer tokens for incoming requests |
| `service/` | Business logic for authentication, OTP, voters, elections, candidates, eligibility, and votes |
| `src/main/resources/application.properties` | Runtime configuration |

The `database/schema.sql` file contains SQL schema/setup definitions. Keep it aligned with entity mappings and any database migrations.

## 6. Data Model

The following core tables have been observed in the development database:

### `voters`
Stores voter profile and account data. Observed fields include:
- `id`
- `voter_id` (unique)
- `full_name`
- `date_of_birth`
- `mobile` (unique)
- `email` (unique)
- `password_hash`
- `status` (default observed as `PENDING`)
- `created_at`
- `updated_at`

### `elections`
Stores election definitions and lifecycle/status information.

### `candidates`
Stores candidates associated with an election. Exact columns and foreign-key names should be checked against `database/schema.sql` and the current entity.

### `voter_eligibility`
Stores the eligibility relationship between voters and elections.

### `votes`
Stores vote submissions and their references. The database has a uniqueness constraint on `(election_id, voter_id)` to prevent a voter from having more than one vote record for the same election. A receipt field/constraint is also present in the observed schema.

### Relationship overview

```text
voters 1 ---- * voter_eligibility * ---- 1 elections
voters 1 ---- * votes * ---------------- 1 elections
candidates 1 - * votes (candidate selected by a vote)
```

This is a conceptual relationship diagram. Verify exact foreign-key names, nullability, cascade rules, and indexes against `database/schema.sql` and the JPA entities.

## 7. Main User Workflows

### 7.1 Voter registration and authentication
1. Voter opens the registration or login page.
2. Frontend collects the required fields and sends them to the relevant API endpoint.
3. Backend validates the request and checks uniqueness/eligibility requirements as applicable.
4. Passwords should be stored as one-way password hashes, never as plaintext. Confirm the current configured encoder and its parameters in the security/service code.
5. Login verifies the submitted credentials.
6. If authentication succeeds, the API returns a JWT and related response data.
7. The browser sends the token with protected requests using `Authorization: Bearer <token>`.

### 7.2 JWT-protected API request
1. Browser sends a request to a protected endpoint with a bearer token.
2. `JwtAuthenticationFilter` extracts the token.
3. `JwtService` validates token integrity and reads claims such as voter identity and role.
4. Spring Security applies authorization rules.
5. A valid voter token can access allowed voter routes; a voter token must not grant administrator access.
6. Invalid or expired tokens should be rejected according to the filter and exception handling behavior.

### 7.3 Election and candidate administration
1. An administrator authenticates and obtains a token with the appropriate role/claims.
2. The admin UI/client sends election or candidate management requests.
3. Security configuration restricts administrative routes.
4. Controllers pass validated requests to service methods.
5. Services persist changes through repositories.
6. API responses report success or validation/business-rule errors.

### 7.4 Vote-casting workflow
1. Voter signs in.
2. Voter requests election/candidate information and eligibility information.
3. Backend checks that the election is in `LIVE` status.
4. Backend verifies that the voter is eligible for that election.
5. Backend verifies that the selected candidate belongs to the relevant election and is `ACTIVE`.
6. Backend prevents duplicate vote submission for the same voter/election.
7. The vote and receipt reference are persisted transactionally.
8. The API returns a confirmation/receipt reference.
9. A repeated submission should be rejected by application logic and/or the database uniqueness constraint.

```text
Voter
  |
  v
Login -> JWT
  |
  v
Load elections/candidates
  |
  v
Submit selected candidate
  |
  v
Authenticate + authorize
  |
  v
Election LIVE? ---- no ----> Reject
  |
 yes
  v
Voter eligible? --- no ----> Reject
  |
 yes
  v
Candidate ACTIVE and belongs to election?
  | no
  +--------------------------> Reject
  |
 yes
  v
Duplicate vote exists? -- yes -> Reject
  |
 no
  v
Transactional database write
  |
  v
Receipt returned to client
```

### 7.5 Result viewing
1. An authorized request asks for results for an election.
2. Backend checks the endpoint's authorization and election/result rules.
3. Results are calculated/read from persisted vote records.
4. API returns the result DTO.
5. The frontend renders the result information.

Confirm the exact result-publication policy in the current service/controller code before deploying; do not assume results are hidden or revealed at a particular lifecycle stage unless enforced by code.

## 8. API Surface (Controller-Level Guide)

The source tree includes these controllers. Exact HTTP methods, request bodies, and path variables must be taken from controller annotations or the generated OpenAPI specification if one is enabled.

| Controller | Area |
|---|---|
| `HealthController` | Health/status check; `/api/health` was verified locally |
| `AuthController` | Registration, login, OTP-related authentication |
| `UserController` | User/voter account operations |
| `ElectionController` | Election listing/details for normal users |
| `ElectionAdminController` | Admin election management |
| `CandidateController` | Candidate listing/details |
| `CandidateAdminController` | Admin candidate management |
| `EligibilityAdminController` | Admin eligibility management |
| `VoteController` | Vote submission and related vote operations |
| `VoterDashboardController` | Voter dashboard |
| `AdminController` | Administrative dashboard/results operations |

### Locally verified behavior
- `GET /api/health` returned HTTP 200 and an UP status.
- Voter login returned a token in the tested development flow.
- `GET /api/me` with a voter token returned HTTP 200.
- `GET /api/voter/dashboard` with a voter token returned HTTP 200.
- `GET /api/admin/dashboard` with a voter token returned HTTP 403.
- An admin token accessed the admin dashboard successfully.
- An admin results request was exercised for a development election.
- These are development checks, not a full automated test suite.

## 9. Configuration and Local Setup

### Prerequisites
- Ubuntu/Linux shell
- Java 17
- Maven 3.8+
- PostgreSQL (the development environment used PostgreSQL 16.x)
- Redis if the configured application startup/runtime path requires it
- Git

### Clone
```bash
git clone https://github.com/Guru09042004/digital-voting-system.git
cd digital-voting-system
```

### Backend location and build
In the developer's workspace, the backend was under `backend/digital-voting-api`:
```bash
cd backend/digital-voting-api
mvn clean package -DskipTests
```

### Required environment variables
`application.properties` is configured to use `DB_PASSWORD`, and `JwtService` reads `JWT_SECRET` from the environment. Set these in the same shell used to start the application.

```bash
export DB_PASSWORD='YOUR_DATABASE_PASSWORD'
export JWT_SECRET="$(openssl rand -base64 48)"
```

- Do not commit actual values to Git.
- Generate a fresh secret for each environment and store it in a proper secret manager for production.
- If the JWT secret changes, previously issued tokens may no longer validate.
- Do not paste credentials into chat, documentation, screenshots, or source control.

### Database configuration
Check `backend/digital-voting-api/src/main/resources/application.properties` for the JDBC URL, username, port, JPA settings, and Redis settings. The development configuration observed used:
- JDBC URL: `jdbc:postgresql://127.0.0.1:5432/digital_voting`
- Database user: `voting_user`
- API port: `8082`
- Redis host/port: `127.0.0.1:6379`

These are local development values; change them for other environments. Ensure the database and required tables exist and match the JPA schema validation setting before startup.

### Start the backend
From `backend/digital-voting-api`, after exporting the environment variables and starting PostgreSQL/Redis:
```bash
mvn spring-boot:run
```

Health check from another terminal:
```bash
curl -i http://localhost:8082/api/health
```

### Start the frontend
From the frontend directory:
```bash
cd frontend
python3 -m http.server 8081
```
Open:
- `http://localhost:8081/`
- `http://localhost:8081/login.html`

If port `8081` is busy, choose another free port and adjust API/CORS configuration as required.

## 10. Security Design and Operational Notes

### Implemented/observed mechanisms
- JWT-based authentication in the Spring Security filter chain.
- Separate voter/admin authorization behavior verified for selected endpoints.
- Environment-variable lookup for DB password and JWT secret.
- Database uniqueness constraint for one vote per voter/election.
- Vote workflow validations for live election, voter eligibility, active candidate, and duplicate submissions.

### Required hardening before production
1. Remove or disable `/api/dev/**` endpoints and ensure no development-only routes are permitted anonymously.
2. Rotate any credential or signing secret that was ever committed or shared; invalidate/reissue tokens as needed.
3. Use HTTPS/TLS in any non-local deployment.
4. Use least-privilege database credentials; do not run the application as a database superuser.
5. Apply rate limits and monitoring to login, OTP, registration, and voting endpoints.
6. Ensure OTP expiration, single-use behavior, attempt limits, and delivery controls are enforced.
7. Use a strong, maintained password hashing algorithm and safe parameters; confirm the actual configured encoder.
8. Avoid logging passwords, OTPs, JWTs, full personal data, or sensitive ballot choices.
9. Add security headers, restrictive CORS, request validation, and safe error responses.
10. Use managed secrets rather than shell exports for production.
11. Add automated unit, integration, authorization, concurrency, and end-to-end tests.
12. Add database backups, restore drills, migrations, health monitoring, alerting, and incident response procedures.
13. Review vote secrecy and identity-to-ballot linkage independently. A conventional relational schema with voter references does not automatically guarantee ballot anonymity.
14. Conduct an independent security audit and threat model before any real election.

## 11. Error Handling and Troubleshooting

### Build fails
```bash
java -version
mvn -version
mvn clean package
```
Read the first compiler error carefully. Do not assume that a successful package with `-DskipTests` means tests passed.

### Missing environment variable
If startup says `JWT_SECRET` is not configured, export it in the same shell:
```bash
export JWT_SECRET="$(openssl rand -base64 48)"
```
For database authentication failures, set `DB_PASSWORD` to the actual local database password. Password input may not display characters in a terminal.

### API health check fails
```bash
ss -ltnp | grep 8082
curl -i http://localhost:8082/api/health
```
Check Spring Boot logs, PostgreSQL connectivity, schema validation, and Redis requirements.

### Unauthorized/forbidden responses
- `401 Unauthorized` generally means authentication is missing or invalid.
- `403 Forbidden` means the authenticated identity lacks permission for that resource.
- Verify the token is current and that the route's required role matches the token claims and security configuration.

### Database schema mismatch
Check `database/schema.sql`, JPA entity definitions, and `spring.jpa.hibernate.ddl-auto`. The observed configuration used `validate`, which expects the database schema to be prepared and compatible.

## 12. Verification Checklist

- [ ] Java 17 and Maven are installed.
- [ ] PostgreSQL is running and the expected database/schema exist.
- [ ] Redis is running if required by the active code path.
- [ ] `DB_PASSWORD` and `JWT_SECRET` are configured outside source control.
- [ ] Maven package completes successfully.
- [ ] Health endpoint returns HTTP 200.
- [ ] Login and token validation work.
- [ ] Voter routes reject unauthenticated requests.
- [ ] Voter role cannot access admin routes.
- [ ] Admin role can access intended admin routes.
- [ ] Ineligible voters cannot vote.
- [ ] Non-live elections reject votes.
- [ ] Invalid/inactive or unrelated candidates are rejected.
- [ ] Duplicate vote submissions are rejected, including concurrent submissions.
- [ ] Receipt generation and persistence are tested.
- [ ] Automated tests run without `-DskipTests`.
- [ ] Development endpoints are disabled before deployment.
- [ ] No credentials, tokens, personal data, or backups are committed.
- [ ] Backup restoration and monitoring have been tested.

## 13. Current Development Status

- Repository initialized and pushed to GitHub on branch `main`.
- Maven package completed successfully with tests skipped.
- Selected health, authentication, role-separation, dashboard, and results flows were exercised locally.
- Database credentials and JWT signing key are read from environment variables in the current source configuration.
- Production hardening and full automated test execution remain necessary.

## 14. Suggested Repository Documentation Layout

```text
digital-voting-system/
├── README.md
├── docs/
│   └── DIGITAL_VOTING_SYSTEM_DOCUMENTATION.md
├── DIGITAL_VOTING_SYSTEM_DOCUMENTATION.docx
├── backend/
│   └── digital-voting-api/
├── database/
│   └── schema.sql
└── frontend/
```

## 15. Glossary

- **API:** Application Programming Interface used by the frontend to communicate with the backend.
- **JWT:** JSON Web Token used to represent authenticated identity/claims.
- **RBAC:** Role-Based Access Control; permissions depend on a user's role.
- **DTO:** Data Transfer Object used for request/response payloads.
- **JPA:** Java Persistence API, used to map Java entities to relational data.
- **OTP:** One-Time Password used as an additional verification step.
- **Receipt:** A generated reference returned after a successful vote operation.
- **Idempotency/duplicate prevention:** Controls that stop the same voter from submitting more than one vote for the same election.
