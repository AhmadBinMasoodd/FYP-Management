# FYP Management Backend

Spring Boot 4.0 service for managing Final Year Projects: students, faculty, admins, documents, submissions, grades, and feedback. Built with Spring Security, Spring Data JPA, MySQL, and SMTP mail.

## Quick Start
```powershell
# clone and enter
git clone https://github.com/AhmadBinMasoodd/FYP-Management.git
cd FYP-Management

# adjust JDK if needed (pom java.version is 25)

mvn spring-boot:run
```
App listens on port 8080 by default.

## Tech Stack
- Java (project java.version: 25 in [pom.xml](pom.xml); set to your installed JDK if different)
- Spring Boot 4.0.0: web, security, data-jpa, mail, devtools
- MySQL 8.x
- Maven 3.9+

## Architecture at a Glance
- Layered: controller → service → repository → database
- Security: session-based; credentials checked with BCrypt; Spring Security context stored in HTTP session
- Persistence: Spring Data JPA with MySQL; `ddl-auto=update` for dev convenience
- File storage: uploaded files under `src/main/resources/static/uploads` (served as static content)
- Email: Spring Mail via SMTP (configure credentials)

## Configuration (override via env vars or properties)
Defaults live in [src/main/resources/application.properties](src/main/resources/application.properties). Recommended to keep secrets in environment variables.

| Purpose | Property | Default |
| --- | --- | --- |
| DB URL | `SPRING_DATASOURCE_URL` | jdbc:mysql://localhost:3306/scd_lab |
| DB user | `SPRING_DATASOURCE_USERNAME` | root |
| DB password | `SPRING_DATASOURCE_PASSWORD` | Rizwan123 (replace) |
| JPA DDL | `SPRING_JPA_HIBERNATE_DDL_AUTO` | update |
| Mail host | `SPRING_MAIL_HOST` | smtp.gmail.com |
| Mail port | `SPRING_MAIL_PORT` | 587 |
| Mail user | `SPRING_MAIL_USERNAME` | your_email@gmail.com |
| Mail password | `SPRING_MAIL_PASSWORD` | your_app_specific_password_or_token |
| Upload size | `SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE` | 20MB |
| Upload request cap | `SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE` | 50MB |

Set via PowerShell (example):
```powershell
setx SPRING_DATASOURCE_URL "jdbc:mysql://localhost:3306/scd_lab"
setx SPRING_DATASOURCE_USERNAME "root"
setx SPRING_DATASOURCE_PASSWORD "your_db_password"
setx SPRING_MAIL_USERNAME "your_email@gmail.com"
setx SPRING_MAIL_PASSWORD "your_app_password"
```

## Running
```powershell
# dev
mvn spring-boot:run

# build + run jar
mvn clean package
java -jar target/fyp_management-0.0.1-SNAPSHOT.jar
```

## Authentication and Sessions
- Students/Faculty login: `POST /auth/login` with body `{ "id": "STU-123" | "FAC-45", "password": "..." }`
- Admin login: `POST /auth/admin/login` with `{ "username": "admin", "password": "..." }`
- Session info: `GET /auth/me`
- Logout: `POST /auth/logout`
- Roles are derived from ID prefix (student/faculty) or admin record; stored in HTTP session and mirrored into Spring Security context.

### Smoke Test (PowerShell + curl)
```powershell
curl -X POST http://localhost:8080/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"STU-123\",\"password\":\"pass\"}" -i

curl http://localhost:8080/auth/me -i
```

## Domain Areas (Controllers/Services)
- Auth: login/logout/session for students, faculty, admins
- Students/Faculty/Admin: CRUD and lookups for people and roles
- Documents: document types, file submissions, states, uploads (stored under static/uploads)
- Feedback and Grades: capture review cycles and scoring

## Database Notes
- Dialect: `org.hibernate.dialect.MySQLDialect`
- `spring.jpa.hibernate.ddl-auto=update` auto-creates tables in dev; switch to `validate` and use migrations for production.
- Ensure MySQL user has create/alter privileges if you keep `update`.

## Security Notes
- Current `SecurityFilterChain` permits all paths; controllers enforce session presence. Harden before production:
  - Add antMatchers per role
  - Enable CSRF strategy appropriate for your clients
  - Enforce password rules and rate limiting
- Passwords are stored with BCrypt via the configured `PasswordEncoder` bean.

## File Uploads
- Limits: single file 20MB; request 50MB
- Stored under `src/main/resources/static/uploads`; ensure the directory exists and has write permissions when running from jar (consider externalizing for prod).

## Project Structure
- [src/main/java/com/company/fyp_management/config](src/main/java/com/company/fyp_management/config) — security, web config, password encoder
- [src/main/java/com/company/fyp_management/controller](src/main/java/com/company/fyp_management/controller) — REST controllers (auth, student, faculty, etc.)
- [src/main/java/com/company/fyp_management/entity](src/main/java/com/company/fyp_management/entity) — JPA entities (Student, Faculty, Admin, DocumentTypes, FileSubmission, Feedback, Grades, State)
- [src/main/java/com/company/fyp_management/repository](src/main/java/com/company/fyp_management/repository) — Spring Data repositories
- [src/main/java/com/company/fyp_management/service](src/main/java/com/company/fyp_management/service) — business logic, email sending
- [src/main/resources](src/main/resources) — properties, static assets, uploads

## Development Tips
- Keep secrets out of VCS; prefer env vars or a local `application-local.properties` added to `.gitignore`.
- If you tighten security, update the `SecurityFilterChain` and add method-level guards where needed.
- Align `java.version` in [pom.xml](pom.xml) with your installed JDK to avoid build failures.
- For large file uploads in production, move storage outside the jar location and serve via CDN or reverse proxy.

## Testing
```powershell
mvn test
```

## Troubleshooting
- Push rejected because branch is behind: `git pull --rebase origin main` then `git push origin main`.
- MySQL connection fails: check host/port/user/pass and ensure the DB `scd_lab` exists; verify `spring.datasource.url`.
- Mail errors: use an app password if on Gmail and confirm `spring.mail.username`/`spring.mail.password`.

## License
Specify your license here (currently not set in pom).
