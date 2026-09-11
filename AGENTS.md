# AGENTS.md — Moonlight Java Starter Kit

## Project identity

- **Type:** Spring Boot 4.1.0 application template
- **Group:** `com.servicecops:project`
- **Action routing:** Moonlight Java SDK `com.jet.moonlight:jet:0.0.3`
- **Library source:** [java-sdk](https://github.com/moonlight-architecture/java-sdk)
- **Repository:** https://github.com/moonlight-architecture/java-starter-kit

## Build commands

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn clean package
mvn clean compile
```

Refresh the SDK after library changes (from a [java-sdk](https://github.com/moonlight-architecture/java-sdk) checkout):

```bash
mvn clean install
cp target/jet-0.0.3.jar /path/to/java-starter-kit/local-maven-repo/com/jet/moonlight/jet/0.0.3/
```

## Architecture rules

- HTTP actions go through Moonlight `jet.api` auto-routes (`/api/{version}/…`) → `ActionDispatcher.dispatchSafely()` — no `WebActionsController`
- POST accepts JSON, form-urlencoded, and multipart via `JetRequest.preparePath`
- New endpoints = new `@Action` methods on `@MarkedAsJetService` classes — **no new controllers**. `@MarkedAsJetService` includes Spring `@Service`.
- Services extend `UniversalService` (domain helpers) which extends `JetService`
- Auth: `JwtFilter` loads user once; `SecurityContext` principal is `SystemUserModel` with role, domain, and authorities filled
- Permission catalog is `Perms` (`JetPermission` enum) — boot **upserts**, never `deleteAll`
- `PermissionCatalogValidator` fails boot if `@RequiresPermission` slugs are not in the catalog
- Role auth is cached in `RolePermissionCache` (`roleName` / domain / permission codes). Use `RoleGrantService.grant` / `revoke` so the cache is always invalidated after assignment changes.
- JWT `token_version` (`tv`) is bumped **atomically** on login so concurrent logins cannot share a version; previous JWTs fail
- Login returns `UserView` — never serialize the password hash
- `JwtFilter` must skip `{jet.docs.path}/**` so a docs Bearer token is not parsed as a user JWT
- Docs: library auto-injects `/docs`; blank `jet.docs.token` is public. To lock, set `JET_DOCS_TOKEN` (`?token=`, `X-Docs-Token`, or Bearer). Do not add a docs controller
- `SpringSecurityJetContext` implements `JetSecurityContext` (including `hasRole` / `hasDomain`)
- Mail: `MailService` exists only when `jet.mail.enabled=true` (default off). No GCP storage.
- Moonlight `com.jet.moonlight.util` is on the classpath and is **not** auto-wired. Do not add template `SmsSender` / crypto beans.
- Auth stays fail-open: protect actions with `@Authenticated` / `@RequiresPermission` / `@RequiresRole` / `@RequiresDomain`
- Use `@RateLimit` on public expensive actions (e.g. login)

## Code conventions

- Return `JetResponse` from all `@Action` methods
- Use Moonlight annotations: `@JetFields`, `@Authenticated`, `@RequiresPermission`, `@RequiresAnyPermission`, `@RequiresRole`, `@RequiresDomain`, `@RateLimit`. Typed permission annotations live next to `Perms` and take the catalog enum.
- Prefer `@RequiresPermission(Perms.USERS_VIEW)` (catalog enum) over raw permission strings
- Prefer `JetRepository.getRequired` / `page(JetRequest)` / `saveAndRefresh`
- Do not recreate AOP handlers — they live in the Java SDK
- Lombok is used in this project (`@Data`, `@RequiredArgsConstructor`, …)

## Never do

- Add per-endpoint routing switches or duplicate dispatch logic
- Add `log.info` (or similar) on every request in filters
- Re-query user from DB in services when principal is already in `SecurityContext`
- Call `permissionRepository.deleteAll()` on boot (wipes role grants via CASCADE)
- Commit secrets (`.env`, credential JSON, `application.properties` secrets in public repos)
- Return `SystemUserModel` (or any entity with password) in API responses — use `UserView`
- Ship unit tests or test-only dependencies in this template (`src/test` stays empty; coverage belongs in java-sdk)
- Commit `stress-app/`, `.idea/`, or `.cursor/` — they are local tooling, not the template

## Performance priorities

- **1 DB read per authenticated request** after the role cache is warm: reload `system_user` for `token_version` (and active flag). Role name, domain, and permissions come from `RolePermissionCache` (0 extra reads on hit). Zero user reads is incompatible with single-session `tv` checks.
- Keep `spring.jpa.open-in-view=false`
- Use HikariCP settings in `application.properties` (`maximum-pool-size=10`, `minimum-idle=1`) — expect pool wait/timeouts under saturation, not a wedged JVM
- Login `@RateLimit` is in-memory and **per node / per client IP**
- Do not put class-level `@Transactional` on `UniversalService`
- Map `SystemUserModel` with `schema = "public"` so PostgreSQL does not treat unquoted `system_user` as the reserved keyword

## When changing this project

1. Prefer upstream fixes in `java-sdk` over duplicating framework code
2. Update README and AGENTS.md when integration patterns change
3. Run `mvn clean compile` before finishing

## Related projects

- **Java SDK:** https://github.com/moonlight-architecture/java-sdk — action dispatch library; build and publish there first
- **Setup CLI:** https://github.com/moonlight-architecture/setup-script — `moonlight new` / `run` / `setup`
