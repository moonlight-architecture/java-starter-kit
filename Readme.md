# Moonlight Java Starter Kit

Spring Boot application template using the [Moonlight Java SDK](https://github.com/moonlight-architecture/java-sdk) for annotation-driven action routing instead of per-endpoint controllers.

| | |
|---|---|
| **Spring Boot** | 4.1.0 |
| **Java** | 25 (LTS) |
| **Java SDK** | `com.jet.moonlight:jet:0.0.3` |
| **License** | MIT |
| **Repository** | https://github.com/moonlight-architecture/java-starter-kit |

## Quick start

Install the [Moonlight CLI](https://github.com/moonlight-architecture/setup-script) (macOS, Linux, or Windows Git Bash), then create a project:

```bash
curl -fsSL https://raw.githubusercontent.com/moonlight-architecture/setup-script/main/install.sh | bash
moonlight new myapp
cd myapp
moonlight run
```

On macOS and Linux you can also install with Homebrew (`moonlight-cli`). See the [CLI readme](https://github.com/moonlight-architecture/setup-script).

Or clone this template and run:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Profiles: `dev`, `uat`, `prod` via `spring.profiles.active` in `application.properties`. Requires Java 25+ and PostgreSQL.

## Architecture

```
Client → JwtFilter → jet.api routes (Java SDK) → ActionDispatcher
                                              → AuthService / other @MarkedAsJetService beans
```

- **No business action controller** — `jet.api` auto-registers `/api/{version}/…`
- **Java SDK auto-config** — no manual `@ComponentScan("com.jet.moonlight")` needed
- **UniversalService** — domain helpers (auth context from principal); extend for business services
- **SpringSecurityJetContext** — implements `JetSecurityContext` (`isAuthenticated`, permissions, role, domain)
- **Permissions** — `Perms` catalog enum (`JetPermission`); boot upserts; catalog validated against the action registry
- **Optional** `JetDispatchObserver` beans for audit / telemetry

## Creating a service

`AuthService` is the documentation example: public `login` (with `@RateLimit`) and protected `me` (`@Authenticated` + `@RequiresPermission(Perms.USERS_VIEW)`). Copy that shape; drop `@Doc` / `@Field` properties you do not need (defaults are empty / `-1` / `false`).

```java
@MarkedAsJetService(name = "auth", description = "Authentication — login and identity")
public class AuthService extends UniversalService {

    @PostOnly
    @RateLimit(limit = 5, windowSeconds = 60)
    @Action(name = "login")
    public JetResponse login(JetRequest request) { ... }

    @Authenticated
    @RequiresPermission(Perms.USERS_VIEW)
    @Action(name = "me")
    public JetResponse me(JetRequest request) {
        return JetResponse.okData(UserView.from(authenticatedUser()));
    }
}
```

Always set an explicit `@MarkedAsJetService(name = "...")` — the name is matched case-insensitively. `@MarkedAsJetService` already includes Spring `@Service`.

Add a constant to [`Perms`](src/main/java/com/servicecops/project/permissions/Perms.java). Boot upserts the row; unknown `@RequiresPermission` slugs fail startup. Protect actions with `@RequiresPermission(Perms.USERS_VIEW)` from that same package (the library annotation still accepts a string slug).

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
| `@RequiresRole` / `@RequiresDomain` | Role / domain gate (SDK 0.0.3+) |
| `@RateLimit` | In-memory per-node throttle before security |
| `@Deprecated` | OpenAPI `deprecated: true` |

`login` is public on purpose: no `@Authenticated`. `me` is the protected template. Open `/docs` after restart to see both (or `/docs?token=…` if you set `jet.docs.token`).

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

JWT is validated in `JwtFilter` (claims parsed once). The user row is loaded **once** per request (needed for `token_version`). Role name, domain, and permissions come from `RolePermissionCache` after the first hit for that role. The principal carries those fields; services use `UniversalService.authenticatedUser()` with no extra DB round-trip.

Login returns `UserView` (never the password hash). Each login **atomically** increments `token_version` so previous JWTs stop working and concurrent logins cannot issue the same version. Change role grants through `RoleGrantService` so the cache is invalidated.

Provide `Authorization: Bearer <jwt>` for authenticated actions. Use `@Authenticated`, `@RequiresRole`, or `@RequiresDomain` from the Java SDK, and `@RequiresPermission(Perms.…)` / `@RequiresAnyPermission` from the app catalog. Auth remains fail-open: unprotected actions stay public until you annotate them.

## Docs

The Java SDK auto-injects `/docs` (Scalar) and `/docs/openapi.json` from those annotations.

`/docs` is public when `jet.docs.token` is blank (the default). To lock the UI and spec, set `JET_DOCS_TOKEN` or `jet.docs.token`, then open with `?token=`, `X-Docs-Token`, or `Authorization: Bearer` using the **docs token**, not a user JWT.

`JwtFilter` skips `{jet.docs.path}/**` so a docs Bearer value is not parsed as a login token. That skip does not protect the API; only the docs UI and spec are gated.

## Performance defaults

Configured in `application.properties`:

- `spring.jpa.open-in-view=false`
- HikariCP pool sizing (`maximum-pool-size=10`, `minimum-idle=1`) — intentional starter limit; saturate with timeouts, not larger pools by default
- Authenticated path target: **1** `system_user` read per request after role-cache warm
- HTTP response compression enabled
- CORS via `jet.cors.origins` (default `http://localhost:8080`)

Avoid per-request logging in filters. Do not re-query the user from the database when already in `SecurityContext`. Prefer `JetRepository.page(request)` for list actions. Grant/revoke via `RoleGrantService` so `RolePermissionCache` stays coherent.

## Refresh Java SDK dependency

From a checkout of [java-sdk](https://github.com/moonlight-architecture/java-sdk):

```bash
mvn clean install
cp target/jet-0.0.3.jar /path/to/java-starter-kit/local-maven-repo/com/jet/moonlight/jet/0.0.3/
```

## Configuration

Shared settings in `application.properties`; profile overrides in `application-{profile}.properties`.

| Area | Keys |
|------|------|
| Database | `spring.datasource.*`, PostgreSQL |
| Mail | `jet.mail.enabled` (default `false`); when on, `spring.mail.*` |
| JWT | `secret` / `JWT_SECRET` (dummy local default; `prod`/`uat` refuse the dummy) |
| CORS | `jet.cors.origins` / `JET_CORS_ORIGINS` |
| App version | `app.version` |
| Docs | `jet.docs.*` (UI/spec). Set `jet.docs.token` to lock. Keep `jet.docs.api-base-path` in sync with `jet.api.base-path` |
| API | `jet.api.*` (dispatch routes; default on) |

## Dependencies

- Spring Boot Web MVC (Jetty), JPA, Security, Mail (opt-in), Thymeleaf
- Moonlight Java SDK `jet:0.0.3`
- PostgreSQL, Liquibase, JWT (jjwt 0.13)

`com.jet.moonlight.util` (hash, crypto, signatures, SMS SPI, phone helpers) is on the classpath and is **not** auto-wired. Call it from an action when you need it. This template does not register an `SmsSender` or crypto bean.

## Changelog

See [CHANGELOG.md](CHANGELOG.md). Git tags are the template version; Maven coordinates stay `0.0.1-SNAPSHOT`.

**v0.0.3** ships Java SDK `0.0.3`, `UserView` + `token_version`, permission upsert/catalog validation, role permission cache, `auth/me`, and leaner resources.

**v0.0.2** ships Java SDK `0.0.2`, puts datasource defaults in `application.properties`, and drops unused template packages.

**v0.0.1** shipped Java SDK `0.0.1` (observers + `jet.api` routes; no action controller).

### Run from an IDE

- Project SDK must be Java 25 or newer.
- Optional VM option: `--sun-misc-unsafe-memory-access=allow` (Fastjson2 transitive on JDK 25+)
- Or from the project root: `moonlight run` / `moonlight run uat`

