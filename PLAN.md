# Plan

> Source of truth for current goals, decisions, and open questions.
> Updated by `/ship` and by hand. Newest entries at the top of each section.

## Current focus
**Phase 1 — Backbone complete** on `feature/phase-1-desks-backbone` (4 checkpoints shipped). All exit gates green; 6 seeded desks render in the browser via the Angular Material page. Ready for `/verify` + `/merge` to land on `develop`. Next: **Phase 2 — Check-in / check-out** (see **Phases → Phase 2**).

## Goals (in scope for the hour)
1. Track floors, rooms, and desks with type metadata (standard / standing / conference).
2. Assign an employee to a desk via check-in, finalize via check-out — both server-stamped timestamps.
3. List desks with their current occupancy (`occupiedBy: null` or `{ employeeName, checkedInAt }`).
4. Filter to available desks. **Stretch:** search by employee name or desk type.

## Out of scope (cut to fit the hour)
Authentication. Multiple buildings. Scheduled / future bookings (only "check in now"). Capacity/equipment metadata. Real-time updates. Pagination. Audit log. Employee profiles (free-text names only).

## Phases

### Phase 1 — Backbone (~20 min): "I can see desks"
- Flyway `V2__core_tables.sql` — `floors`, `rooms`, `desks` (with `desks.type` as TEXT + CHECK constraint for clean JPA mapping). Seed 1 floor / 2 rooms / 6 desks for demo. (V1 is the Phase 0 baseline placeholder.)
- JPA entities (Lombok `@Getter`/`@Setter` + custom equals/hashCode on id) + repositories.
- DTO records: `FloorSummary`, `RoomSummary`, `DeskResponse` (initially with `occupiedBy: null`).
- `GET /api/desks` controller + service. OpenAPI annotations per CLAUDE.md.
- Angular component listing desks with the shared SCSS tokens.
- Tests: 1 service-layer unit test + 1 integration test hitting the endpoint.

**Exit:** `mvn verify` green, `ng build` green, list page renders the seeded desks.

### Phase 2 — Check-in + check-out (~25 min): "I can claim and release a desk"
- Flyway `V2__bookings.sql` — `bookings(id, desk_id FK, employee_name, checked_in_at, checked_out_at NULL)` + **partial unique index** `(desk_id) WHERE checked_out_at IS NULL`. The DB enforces "one active booking per desk"; concurrent check-in races become 23505 → mapped to 409.
- `BookingService` with `POST /api/bookings` (check-in) and `PATCH /api/bookings/{id}/checkout`.
- `GET /api/desks` returns `occupiedBy` via a **JPQL projection query** that left-joins bookings filtered by `checked_out_at IS NULL` — explicit join in the query, no `@Where`/`@Formula` magic.
- `@ControllerAdvice` mapping the common errors (entity-not-found → 404, constraint violation → 409, bean-validation → 400).
- Frontend: click a desk → modal for employee name → POST /api/bookings. Show "Check out" button on occupied desks.
- Tests: BookingService happy path + a test that a second POST returns 409 + a check-out test.

### Phase 3 — Filter + optional search (~15 min, stretch)
- `GET /api/desks?available=true` filter + a frontend toggle for available-only.
- **If time:** `?employee=<name>` (Postgres `ILIKE '%name%'`) and `?type=<type>` (equality). Note: leading-wildcard ILIKE skips the BTREE index; fine for demo, future fix is `pg_trgm` GIN.

## Decisions
_Append-only log of meaningful technical decisions and the reasoning behind them._

- 2026-05-13 — **`@angular/animations` added manually** alongside `@angular/material@^19.2.19` / `@angular/cdk@^19.2.19`. The Material 19 schematic doesn't pull animations or wire `provideAnimationsAsync()` even with `--animations=enabled`; without animations, `provideAnimationsAsync()` fails at bundle time. Lesson for future Material upgrades: explicit `npm install @angular/animations` is required.
- 2026-05-13 — **Angular signals + `inject()` over constructor injection** for `DesksListComponent` and `DesksService`. Matches Angular 19 idiom; uses `signal<T>()` for component state (`desks`, `loading`, `error`) and the new `@if` / `@for` control flow in templates. No DI surprises since the project has no legacy components to mirror.
- 2026-05-13 — **Backend URL hardcoded to `http://localhost:8080`** in `DesksService` for Phase 1. CORS is handled by Spring's `@CrossOrigin`. Refactor to a `proxy.config.json` for dev (and an env-driven base URL for prod) when there's a production deploy story; not worth the angular.json churn for the MVP.
- 2026-05-13 — **CORS via `@CrossOrigin("http://localhost:4200")` on `DeskController`** for Phase 1. With a single controller, the annotation is the cheapest tool — refactor to a global `WebMvcConfigurer` when Phase 2 adds the booking controller. Resolves the prior Open Question.
- 2026-05-13 — **No-N+1 via `JOIN FETCH d.room r JOIN FETCH r.floor`** in `DeskRepository.findAllWithRoomAndFloor()`. The DTO mapping always traverses `desk → room → floor`, so eager fetching in the query is the right tradeoff over per-call `@EntityGraph` annotations.
- 2026-05-13 — **Inline `static from(entity)` factories on DTO records**, no separate Mapper class. Single consumer per DTO; mapping is 3-4 lines. Add a mapper layer if Phase 2 grows a second consumer.
- 2026-05-13 — **Lombok bumped to 1.18.38** (Spring Boot 3.4.1 manages 1.18.36). 1.18.36 hits `TypeTag :: UNKNOWN` on JDK 21.0.11+ because javac removed an internal enum value Lombok was reflecting against; 1.18.38 ships the compat shim. Override is one line in `<properties>` (`lombok.version`).
- 2026-05-13 — **Unidirectional `@ManyToOne` only** on `Room → Floor` and `Desk → Room`. No reverse `@OneToMany` collections — Phase 1's endpoint returns a flat list of desks with `room → floor` inline; the parent→children direction has no consumer yet and would introduce fetch-strategy decisions for no gain.
- 2026-05-13 — **Vlad-style id-based equals/hashCode** on JPA entities: `equals` returns false when `id == null` (transient entities aren't equal to each other), `hashCode` returns `getClass().hashCode()` (constant, survives the transient→managed identity assignment). Canonical pattern; safe in HashSets across persist boundaries.
- 2026-05-13 — **Angular Material** chosen as the frontend component library. Standardizes look-and-feel without hand-rolling layout/dialog/list/table primitives; pairs with the shared SCSS tokens in `frontend/src/styles/_variables.scss` for theming. First use lands in Phase 1's desks-list page; first-time install via `ng add @angular/material`.
- 2026-05-13 — **Lombok** added (`@Getter`, `@Setter`, `@RequiredArgsConstructor` for constructor injection). `lombok.config` enables `@lombok.Generated` so JaCoCo auto-excludes generated methods. Avoids hand-written boilerplate without giving up the "test core logic, not boilerplate" stance.
- 2026-05-13 — **JaCoCo line-coverage threshold lowered 80% → 50%** and `**/dto/**` + `**/entity/**` excluded from the bundle. The remaining coverage signal applies to packages that actually have logic (services, controllers, mappers). Tighten the threshold later as the service layer grows.
- 2026-05-13 — **Employee modeled as a free-text `employee_name` column on `bookings`**, not a separate `employees` table. Two bookings under "Alice Johnson" and "alice johnson" are different people in this model — acceptable for the 1-hour MVP; extract to its own entity when adding auth or profiles.
- 2026-05-13 — **Bookings as a separate entity, not a state column on `desks`.** Enables history and scheduled bookings without schema rewrite.
- 2026-05-13 — **Concurrency-safe assignment via Postgres partial unique index** `(desk_id) WHERE checked_out_at IS NULL` rather than application-level locking. DB is the source of truth; app code can be naive and still correct.
- 2026-05-13 — **`occupiedBy` computed via JPQL projection** (`LEFT JOIN bookings WHERE checked_out_at IS NULL`), not a JPA relationship + `@Where`. The relationship would have to "switch identity" as bookings come and go, which is awkward in Hibernate; the projection is explicit and indexable.

## Open questions
_Things we haven't resolved. Move resolved ones into Decisions._

- HTTP method for check-out: `PATCH /api/bookings/{id}/checkout` vs. `POST /api/bookings/{id}/checkout`. Picking PATCH in Phase 2 unless we hit a reason to switch.

## Changelog
_Short bullet per `/ship`, newest first. Format: `YYYY-MM-DD — <summary>`_

- 2026-05-13 — Phase 1 checkpoint 4 (Phase 1 exit): Angular Material frontend. `ng add @angular/material@^19.2.19` (azure-blue theme, typography, animations) installed in a parallel agent worktree while the component code was drafted in the foreground. `DesksListComponent` (standalone, signals, `@if`/`@for`) renders the seeded desks as `MatCard` tiles in a responsive grid with `MatToolbar` shell and `MatChip` badges for type/availability. `DesksService` calls `GET http://localhost:8080/api/desks`. Karma tests cover the happy path + error state + service mocking. Manually added `@angular/animations` since the Material schematic didn't pull it. **Phase 1 exit gates all green:** `mvn verify` ✓, `mvn test -P integration-tests` ✓, `ng build` ✓, `ng test` ✓, lint + Prettier ✓, browser visual confirmed.
- 2026-05-13 — Phase 1 checkpoint 3: `GET /api/desks` end-to-end. DTO records (`FloorSummary`, `RoomSummary`, `OccupiedBy`, `DeskResponse` with `occupiedBy=null`), `DeskService` with `@Transactional(readOnly=true)`, `DeskController` with full OpenAPI annotations + `@CrossOrigin` for `localhost:4200`. `DeskRepository.findAllWithRoomAndFloor()` uses `JOIN FETCH` to avoid N+1. Tests: service unit test + integration test asserting 6 seeded desks. `mvn verify` green (incl. JaCoCo coverage gate); `mvn test -P integration-tests` green.
- 2026-05-13 — Phase 1 checkpoint 2: JPA entities `Floor` / `Room` / `Desk` + `DeskType` enum + matching repositories. Unidirectional `@ManyToOne` from Room→Floor and Desk→Room (LAZY). Lombok bumped 1.18.36 → 1.18.38 to fix `TypeTag :: UNKNOWN` on JDK 21.0.11. Hibernate `ddl-auto=validate` confirms entity mappings line up with the V2 schema.
- 2026-05-13 — Phase 1 checkpoint 1: Flyway `V2__core_tables.sql` adds `floors` / `rooms` / `desks` with a TEXT + CHECK constraint on `desks.type` (cleaner JPA mapping than a PG ENUM). Seeds 1 floor / 2 rooms / 6 desks for the demo. PLAN.md Current focus narrowed to Phase 1; Angular Material logged as the frontend component library.
- 2026-05-13 — Phase 0: Lombok wired in (deps + annotation processor + `lombok.config`). JaCoCo threshold 80% → 50% and `dto`/`entity` packages excluded from the bundle. PLAN.md rewritten to reflect actual desk-scheduler scope, decisions, and phasing.
- 2026-05-13 — Cloned from `claude-base-monorepo` template; ran the three-pass rename to `desk-scheduler`. `mvn verify` and `ng build` green on the rename.
