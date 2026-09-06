# AGENTS.md — Moonlight Java Starter Kit

## Project identity

- **Type:** Spring Boot 4.1.0 application template
- **Group:** `com.servicecops:project`
- **Action routing:** Moonlight Java SDK `com.jet.moonlight:jet:0.0.1`
- **Library source:** [java-sdk](../java-sdk)
- **Remote:** https://github.com/moonlight-architecture/java-starter-kit

## Build commands

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn clean package
mvn clean compile
```

Refresh the SDK after library changes:

```bash
cd ../java-sdk && mvn clean install
cp target/jet-0.0.1.jar local-maven-repo/com/jet/moonlight/jet/0.0.1/   # from starter kit root
```

## Architecture rules

- HTTP actions go through Moonlight `jet.api` auto-routes (`/api/{version}/…`) → `ActionDispatcher.dispatchSafely()` — no `WebActionsController`
- POST accepts JSON, form-urlencoded, and multipart via `JetRequest.preparePath`
- New endpoints = new `@Action` methods on `@MarkedAsJetService` classes — **no new controllers**. `@MarkedAsJetService` includes Spring `@Service`.
- Services extend `UniversalService` (domain helpers) which extends `JetService`
- Auth: `JwtFilter` loads user once; `SecurityContext` principal is `SystemUserModel`
- `JwtFilter` must skip `{jet.docs.path}/**` so a docs Bearer token is not parsed as a user JWT
- Docs: library auto-injects `/docs`; blank `jet.docs.token` is public. To lock, set `JET_DOCS_TOKEN` (`?token=`, `X-Docs-Token`, or Bearer). Do not add a docs controller
- `SpringSecurityJetContext` implements `JetSecurityContext` for Moonlight security annotations
- Mail: `MailService` exists only when `jet.mail.enabled=true` (default off). No GCP storage.
- Moonlight `com.jet.moonlight.util` is on the classpath and is **not** auto-wired. Do not add template `SmsSender` / crypto beans.

## Code conventions

- Return `JetResponse` from all `@Action` methods
- Use Moonlight annotations: `@JetFields`, `@Authenticated`, `@RequiresPermission`, `@RequiresAnyPermission`
- Do not recreate AOP handlers — they live in the Java SDK
- Lombok is used in this project (`@Data`, `@RequiredArgsConstructor`, …)

## Never do

- Add per-endpoint routing switches or duplicate dispatch logic
- Add `log.info` (or similar) on every request in filters
- Re-query user from DB in services when principal is already in `SecurityContext`
- Commit secrets (`.env`, credential JSON, `application.properties` secrets in public repos)

## Performance priorities

- **0–1 DB reads per authenticated request** for JWT validation + user resolution
- Keep `spring.jpa.open-in-view=false`
- Use HikariCP settings in `application.properties`

## When changing this project

1. Prefer upstream fixes in `java-sdk` over duplicating framework code
2. Update README and AGENTS.md when integration patterns change
3. Run `mvn clean compile` before finishing

## Related projects

- **Java SDK:** `../java-sdk` — action dispatch library; build and publish here first
- **Setup CLI:** `../setup-script` — `moonlight new` / `check` / `update`
