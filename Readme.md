# Moonlight Java Starter Kit

Spring Boot template using the **Moonlight Java SDK** for annotation-driven action routing instead of per-endpoint controllers.

**Spring Boot:** 4.1.0  
**Java:** 25 (LTS)  
**Java SDK:** `com.jet.moonlight:jet:0.0.1` (local Maven repo)  
**Library source:** [java-sdk](../java-sdk)  
**Remote:** https://github.com/moonlight-architecture/java-starter-kit

## Quick start

```bash
curl -fsSL https://raw.githubusercontent.com/moonlight-architecture/setup-script/main/install.sh | bash
moonlight new myapp
```

Or clone this template and run:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Profiles: `dev`, `uat`, `prod` via `spring.profiles.active` in `application.properties`.

## Architecture

```
Client → JwtFilter → jet.api routes (Java SDK) → ActionDispatcher
                                              → AuthService / other @MarkedAsJetService beans
```

- **No business action controller** — `jet.api` auto-registers `/api/{version}/…`
- **Java SDK auto-config** — no manual `@ComponentScan("com.jet.moonlight")` needed
- **UniversalService** — domain helpers (auth context, roles, domains); extend for business services
- **SpringSecurityJetContext** — implements `JetSecurityContext` for Moonlight security annotations
- **Optional** `JetDispatchObserver` beans for audit / telemetry

## Creating a service

`AuthService.login` is the full documentation example — every `@Doc` / `@Field` property is set so `/docs` shows what each one does. Copy this shape; drop properties you do not need (defaults are empty / `-1` / `false`).

```java
@Doc(
    summary = "Authentication",
    description = """
        Session and identity actions.

        Classic RPC: `POST /api/v1` with `{ "service": "auth", "action": "…" }`.
        Path form: `POST /api/v1/auth/{action}`.
        """,
    tags = {"Authentication"},
    hidden = false
)
@MarkedAsJetService(name = "auth", description = "Authentication — login and identity")
public class AuthService extends UniversalService {

    @Doc(
        summary = "Log in",
        description = """
            Authenticate with **username** and **password**. Public — no Bearer token.

            - Success: JWT in `returnData.token` plus the user profile
            - Failure: non-zero `returnCode` (typically 401)
            """,
        tags = {"Authentication"},
        hidden = false,
        responseDescription = "JWT and authenticated user profile",
        responseExample = """
            {"token":"eyJhbGciOiJIUzI1NiJ9...","user":{"username":"admin","lastName":"Admin"}}
            """
    )
    @JetFields({
        @Field(
            name = "username",
            required = true,
            type = FieldType.STRING,
            format = FieldFormat.NONE,
            pattern = "",
            minLength = 1,
            maxLength = 128,
            min = "",
            max = "",
            in = "",
            description = "Account login (username or email)",
            example = "admin"
        ),
        @Field(
            name = "password",
            required = true,
            type = FieldType.STRING,
            format = FieldFormat.NONE,
            pattern = "",
            minLength = 1,
            maxLength = 128,
            min = "",
            max = "",
            in = "",
            description = "Account password",
            example = "changeme"
        )
    })
    @PostOnly
    @Action(name = "login")
    public JetResponse login(JetRequest request) { ... }
}
```

Always set an explicit `@MarkedAsJetService(name = "...")` — the name is matched case-insensitively. `@MarkedAsJetService` already includes Spring `@Service`.

## Documenting APIs

Do not write a separate OpenAPI file. Annotate the action; the Java SDK compiles an OpenAPI 3.1 spec at boot and serves Scalar at `/docs`. Restart after annotation changes.

### `@Doc` — class or method

| Property | Default | In `/docs` |
|----------|---------|------------|
| `summary` | `""` → action name | Operation title |
| `description` | `""` | Markdown body under the title |
| `tags` | `{}` → service name | OpenAPI tag (first value wins on a method) |
| `hidden` | `false` | `true` omits the service (class) or action (method) |
| `responseDescription` | `"Action payload"` | Description of `returnData` |
| `responseExample` | `""` | JSON example for `returnData` only, not the full envelope |

Class-level `@Doc` supplies the tag description (else `@MarkedAsJetService(description)`). Method-level `@Doc` supplies the operation.

### `@MarkedAsJetService`

| Property | Default | In `/docs` |
|----------|---------|------------|
| `name` | simple class name, lowercased | Service key / path segment / default tag |
| `description` | `""` | Tag description if class `@Doc.description` is blank |

### `@Field` (inside `@JetFields`)

`description` and `example` are OpenAPI-only. Every other property is also emitted into the request schema **and** enforced at runtime (blank / `-1` / `NONE` means skip).

| Property | Default | Docs / validation |
|----------|---------|-------------------|
| `name` | required | JSON / query key |
| `required` | `false` | Required in schema; 422 if missing |
| `type` | `ANY` | `STRING`, `INTEGER`, `NUMBER`, `BOOLEAN`, `ARRAY`, `ANY` |
| `format` | `NONE` | `EMAIL`, `UUID`, `URL`, `PHONE` (OpenAPI `format` + validation) |
| `pattern` | `""` | Regex; skipped when blank |
| `minLength` / `maxLength` | `-1` | String length; skipped when `-1` |
| `min` / `max` | `""` | Numeric bounds; skipped when blank |
| `in` | `""` | Comma-separated enum; skipped when blank |
| `description` | `""` | Schema description |
| `example` | `""` | Schema / Try-it example |

### Other annotations that show up in `/docs`

| Annotation | Effect |
|------------|--------|
| `@Action(name)` | Operation id / path action segment |
| `@PostOnly` / `@GetOnly` / `@AllowGetAndPost` | Which HTTP verbs appear |
| `@Authenticated` | Bearer security on the operation (do **not** put this on `login`) |
| `@RequiresPermission` / `@RequiresAnyPermission` | `x-jet-permissions` plus description text |
| `@Deprecated` | OpenAPI `deprecated: true` |

`login` is public on purpose: no `@Authenticated`. Open `/docs` after restart to see this operation (or `/docs?token=…` if you set `jet.docs.token`).

## Mail

Off by default. Enable in `application.properties`:

```properties
jet.mail.enabled=true
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=...
spring.mail.password=...
```

Then inject `MailService` and call `sendHtml(...)` or `sendTemplate("email-template", model)`.

## HTTP endpoints

| Method | Path | Body / params |
|--------|------|---------------|
| GET | `/api/{version}` | Health ping (jet.api) |
| POST | `/api/{version}` | JSON, form-urlencoded, or multipart with `service`, `action`, and fields |
| GET | `/api/{version}/{service}/{action}` | Path variables (GET-only or `@AllowGetAndPost` actions) |
| POST | `/api/{version}/{service}/{action}` | Path POST / OpenAPI Try-it |
| GET | `/docs` | Scalar UI (locked when `jet.docs.token` is set) |
| GET | `/docs/openapi.json` | OpenAPI 3.1 spec (same lock) |

Errors return `JetResponse` with non-zero `returnCode` via `dispatchSafely()`.

## Auth

JWT is validated in `JwtFilter`. The user is loaded **once** per request and stored in `SecurityContext`. Services read the principal via `UniversalService.authenticatedUser()` without an extra DB round-trip.

Provide `Authorization: Bearer <jwt>` for authenticated actions. Use `@Authenticated`, `@RequiresPermission`, or `@RequiresAnyPermission` from the Java SDK on action methods.

## Docs

The Java SDK auto-injects `/docs` (Scalar) and `/docs/openapi.json` from those annotations.

`/docs` is public when `jet.docs.token` is blank (the default). To lock the UI and spec, set `JET_DOCS_TOKEN` or `jet.docs.token`, then open with `?token=`, `X-Docs-Token`, or `Authorization: Bearer` using the **docs token**, not a user JWT.

`JwtFilter` skips `{jet.docs.path}/**` so a docs Bearer value is not parsed as a login token. That skip does not protect the API; only the docs UI and spec are gated.

## Performance defaults

Configured in `application.properties`:

- `spring.jpa.open-in-view=false`
- HikariCP pool sizing (`maximum-pool-size=20`, `minimum-idle=5`)
- HTTP response compression enabled

Avoid per-request logging in filters. Do not re-query the user from the database when already in `SecurityContext`.

## Refresh Java SDK dependency

After building the library:

```bash
cd ../java-sdk && mvn clean install
cp target/jet-0.0.1.jar local-maven-repo/com/jet/moonlight/jet/0.0.1/   # from starter kit root
```

## Configuration

Shared settings in `application.properties`; profile overrides in `application-{profile}.properties`.

| Area | Keys |
|------|------|
| Database | `spring.datasource.*`, PostgreSQL |
| Mail | `jet.mail.enabled` (default `false`); when on, `spring.mail.*` |
| JWT | `secret` / `JWT_SECRET` (dummy local default; generate your own) |
| App version | `app.version` |
| Docs | `jet.docs.*` (UI/spec). Set `jet.docs.token` to lock. Keep `jet.docs.api-base-path` in sync with `jet.api.base-path` |
| API | `jet.api.*` (dispatch routes; default on) |

## Dependencies

- Spring Boot Web MVC (Jetty), JPA, Security, Mail (opt-in), Quartz, Thymeleaf
- Moonlight Java SDK `jet:0.0.1`
- PostgreSQL, Liquibase, JWT (jjwt 0.13), Fastjson2 2.0.64

`com.jet.moonlight.util` (hash, crypto, signatures, SMS SPI, phone helpers) is on the classpath and is **not** auto-wired. Call it from an action when you need it. This template does not register an `SmsSender` or crypto bean.

## Changelog

See [CHANGELOG.md](CHANGELOG.md). Git tags are the template version; Maven coordinates stay `0.0.1-SNAPSHOT`.

**v0.0.1** ships Java SDK `0.0.1` (observers + `jet.api` routes; no action controller).

### Run (IntelliJ)

- Project SDK must be 25+ (e.g. `openjdk-26`). Do not pin the Spring Boot run config to JDK 19.
- Optional VM option: `--sun-misc-unsafe-memory-access=allow` (Fastjson2 on JDK 25+)

