# desk-scheduler

Monorepo: Spring Boot 3.4 / Java 21 / Maven backend at `backend/`, Angular 19 frontend at `frontend/` (scaffolded in phase 3). No Maven wrapper — use system `mvn`.

## Renaming for a new project

This repo is a base template. The project name `desk-scheduler` appears in three forms; renaming a new project is three find/replace passes plus a directory rename.

| Pattern | Replace with | Where it appears |
|---|---|---|
| `desk-scheduler` | `your-new-project` (kebab) | `backend/pom.xml` (`<artifactId>`, `<name>`), `backend/src/main/resources/application.properties` (`spring.application.name`), `docker-compose.yml` (`container_name`), `README.md` title, this file's title, `.vscode/launch.json` (`projectName`) |
| `desk_scheduler` | `your_new_project` (snake) | `application.properties` (DB URL default + user + password), `docker-compose.yml` (`POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` / healthcheck) |
| `deskscheduler` | `yournewproject` (single token) | All Java package declarations and the directory `backend/src/main/java/com/example/deskscheduler/` (plus the test mirror under `backend/src/test/java/...`). After the find/replace, rename the directory to match. |

One-liner to find any stragglers after renaming:

```bash
grep -rE "desk-scheduler|desk_scheduler|deskscheduler" \
  --include="*.java" --include="*.xml" --include="*.properties" \
  --include="*.yml" --include="*.json" --include="*.md" .
```

After renaming, run `mvn verify` from `backend/` and `ng build` from `frontend/` to confirm nothing's broken. Then `git remote set-url origin <your-new-repo-url>` and push.

## Repository layout
```
backend/    — Maven project root (pom.xml lives here, all mvn commands run from here)
frontend/   — Angular 19 application (npm + ng commands run from here once scaffolded)
.github/    — GitHub Actions workflows
.claude/    — slash commands, hooks, project settings (settings.local.json gitignored)
PLAN.md, CLAUDE.md, README.md
```

## Build & test (backend, from `backend/`)
- Unit tests: `mvn test`
- Unit tests + coverage check: `mvn verify` (JaCoCo enforces 80% line coverage at bundle level; `Application.class` is excluded)
- Integration tests: `mvn test -P integration-tests`
- Lint check: `mvn spotless:check`
- Lint apply: `mvn spotless:apply`
- Build: `mvn package`
- Run: `mvn spring-boot:run`

Coverage report after `mvn test` is at `backend/target/site/jacoco/index.html` (or `jacoco.csv` for parsing).

Source layout: `backend/src/main/java/com/example/deskscheduler/`, tests mirror under `backend/src/test/java/...`.
Integration tests live in `backend/src/test/java/com/example/deskscheduler/integration/` and are tagged `@Tag("integration")`. They use `@SpringBootTest(webEnvironment=RANDOM_PORT)` + `TestRestTemplate` to hit endpoints over real HTTP. The `integration` tag is excluded from default `mvn test` runs (Surefire `<excludedGroups>` config in pom.xml) and is the sole group included by the `integration-tests` Maven profile.

## Build & test (frontend, from `frontend/`)
Angular 19, standalone components, no router yet, **SCSS** styling. First-time setup: `npm install`.

- Dev server: `ng serve` (http://localhost:4200, hot reload)
- Unit tests: `ng test` (Karma + Jasmine; one-shot: `npx ng test --watch=false --browsers=ChromeHeadless`)
- Production build: `ng build` (output at `frontend/dist/frontend/`)
- Lint: `npm run lint` (ESLint via `@angular-eslint`); auto-fix: `npx ng lint --fix`
- Format check: `npm run format:check` (Prettier); auto-fix: `npm run format`

### Shared styles (SCSS)
Design tokens live in [frontend/src/styles/_variables.scss](frontend/src/styles/_variables.scss) (colors, spacing, typography). Components reuse them via `@use 'variables' as v;` and then `v.$color-text`, `v.$space-md`, etc. The `src/styles/` directory is wired into Angular's `stylePreprocessorOptions.includePaths` in `frontend/angular.json`, so partial paths resolve cleanly from any component depth.

**Convention:** prefer shared tokens over hardcoded values in component styles. If a new value is needed in more than one place, add it to `_variables.scss` instead of duplicating. Add new shared partials (e.g., `_mixins.scss`, `_typography.scss`) to `src/styles/` and `@use` them the same way. New components scaffolded with `ng generate component` default to `.scss` (configured in `angular.json` schematics).

## API documentation (Swagger / OpenAPI)

The backend exposes auto-generated OpenAPI 3 docs via springdoc:
- **Swagger UI** (browse + try endpoints in the browser): http://localhost:8080/swagger-ui.html
- **OpenAPI JSON** (raw spec): http://localhost:8080/v3/api-docs

### Required annotations on every endpoint
Every new `@RestController` and every endpoint method MUST include OpenAPI annotations. `/verify` blocks merges that introduce undocumented endpoints.

- **Controller class**: `@Tag(name = "...", description = "...")` — groups endpoints in the UI.
- **Each endpoint method**:
  - `@Operation(summary = "...", description = "...")`.
  - `@ApiResponse(responseCode = "200", description = "...")` (or `@ApiResponses({...})` when multiple status codes are possible). Document the success case AND every error case the endpoint can return.
  - `@Parameter(description = "...", example = "...")` on each `@RequestParam` / `@PathVariable`, with an **example value** so the UI's "Try it out" form is pre-filled and one-click executable.
- **DTOs** (request/response records and classes): `@Schema(description = "...", example = "...")` on each field. Same goal — pre-fill the UI.

### Template

```java
@RestController
@RequestMapping("/api/things")
@Tag(name = "Things", description = "Manage things")
public class ThingController {

    @Operation(summary = "List all things")
    @ApiResponse(responseCode = "200", description = "List returned")
    @GetMapping
    public List<ThingResponse> list() { ... }

    @Operation(summary = "Create a thing")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<ThingResponse> create(@RequestBody CreateThingRequest req) { ... }
}

public record CreateThingRequest(
    @Schema(description = "Thing name", example = "my first thing") String name) {}

public record ThingResponse(
    @Schema(example = "42") Long id,
    @Schema(example = "my first thing") String name) {}
```

Example values in `@Schema` and `@Parameter` are what populate Swagger UI's "Try it out" form — that's how each endpoint stays one-click testable from the browser.

## Database

Postgres 16, accessed via Spring Data JPA. Local Postgres runs via `docker compose up -d` from the repo root.

- **Connection config**: `backend/src/main/resources/application.properties` — env vars (`DB_URL`, `DB_USER`, `DB_PASSWORD`) with defaults matching `docker-compose.yml` for zero-config local dev.
- **Migrations**: Flyway, files at `backend/src/main/resources/db/migration/`, naming `V<#>__<description>.sql` (two underscores). Flyway runs on Spring Boot startup; Hibernate `ddl-auto=validate` makes Flyway the sole schema source.
- **Tests**: Testcontainers via `@ServiceConnection` (see [backend/src/test/java/com/example/deskscheduler/TestcontainersConfig.java](backend/src/test/java/com/example/deskscheduler/TestcontainersConfig.java)). Each `mvn verify` run spins up a fresh Postgres container — Docker must be running, but `docker compose up` is not required.

## Code style

Mechanical style (formatting, imports, whitespace) is enforced by Spotless. The points below cover the substance layer — they're lenses, not rules. When any of them fights with readability, **readability wins**.

- **Readability first.** Three similar lines beat a clever abstraction that hides intent. Code is read more often than it's written; optimize for the reader.
- **Testable by design.** Prefer constructor injection over field injection (`@Autowired` on fields makes test setup harder). Keep side effects at the edges; make core logic pure where it can be. If a method is awkward to unit-test, that's usually a design smell, not a test smell.
- **DRY, pragmatically.** Rule of three: don't extract a shared utility on the second duplication. Wait until the pattern stabilizes. A wrong abstraction costs more than a duplicated block.
- **SOLID, pragmatically.** Use the principles as lenses, not commandments:
  - *Single responsibility* — classes/methods small enough to describe in one sentence.
  - *Open/closed, Liskov, Interface segregation* — useful when polymorphism is real; **don't create interfaces for single-implementation classes** just because.
  - *Dependency inversion* — depend on what you *use*, not on what something *is*; but again, only when polymorphism is real.
- **No premature abstraction.** No "future-proofing." If a method has one caller, inline it. If a class has no real polymorphism, make it concrete.
- **Comments explain *why*, not *what*.** Well-named identifiers cover the *what*. Comments earn their keep on non-obvious constraints, hidden invariants, or surprising workarounds.
- **Tests are code too.** Same readability bar. A test you can't grok in 10 seconds is a bad test. Avoid clever DSLs and over-engineered fixtures.

## Branch policy
- `main` = release/stable. Tagged for releases. Don't commit directly.
- `develop` = integration branch. Features land here. Not guaranteed bug-free.
- `feature/<short-kebab>` = work happens here. Branched off `develop`, merged back via `/ship`.
- After merging a feature branch, delete it without asking.

## Workflow (10 steps)

1. All features branch off `develop`.
2. New work → run **`/plan-feature`** to capture intent (goal, scope, success criteria, out-of-scope, open questions) into `PLAN.md` and create the `feature/<kebab>` branch from `develop`. Confirm the name with the user.
3. Make changes on the feature branch and commit incrementally via **`/ship`**. `/ship` updates `PLAN.md`'s Changelog and pauses for commit-message approval.
4. Run **`/verify`** on the feature branch. It runs `mvn test`, adds missing coverage, runs Spotless lint (auto-applies if dirty), and security-reviews the diff. Reports findings; the user resolves anything blocking.
5. Run **`/merge`** on the feature branch. It runs the integration test suite (`mvn test -P integration-tests`), shows results + the diff against develop, and **pauses for explicit `approve`**. On approval, it merges to `develop`, deletes the feature branch, and pushes `develop` to origin (so GitHub Actions can pick it up). (For ad-hoc integration runs not tied to a merge, `/integration-verify` runs the same test suite without the merge step.)
6. Bugs found later → new branch off `develop`, fix, `/verify`, `/merge`. Same loop.
7. When `develop` is ready to release, run **`/promote`**. It (a) reviews `git diff main..HEAD` for README-worthy changes and proposes a README update on `develop` if anything is stale, (b) creates a `develop → main` PR with title and body derived from `git log main..develop` and the PLAN.md changelog. Requires `gh` CLI installed and authenticated. Consider running `/integration-verify` on `develop` first as a final smoke check.
8. Run **`/review <PR#>`** for an in-conversation review that posts to the PR as a review comment for transparency. (`/ultrareview` is also available — it runs a fresh-context multi-agent cloud review and posts findings as PR comments — but it's billed, so `/review` is the default.)
9. User reviews the PR + the review's findings. To work through the findings systematically, invoke **`/address-review <PR#>`** — it fetches comments via `gh`, lists them numbered, lets you pick which to act on, branches off `develop`, and applies the accepted changes as uncommitted edits. From there you run `/verify` → `/ship` → `/merge` per the bug-fix loop (step 6). After merge to `develop`, the existing `develop → main` PR picks up the new commits automatically.
10. User merges the PR into `main` in the GitHub UI.

### Per-step command reference
| Step | Command | Location |
|---|---|---|
| 2 (feature kickoff) | `/plan-feature` | [.claude/commands/plan-feature.md](.claude/commands/plan-feature.md) |
| 3 (each commit) | `/ship` | [.claude/commands/ship.md](.claude/commands/ship.md) |
| 4 (pre-merge gate) | `/verify` | [.claude/commands/verify.md](.claude/commands/verify.md) |
| 5 (integration + merge) | `/merge` | [.claude/commands/merge.md](.claude/commands/merge.md) |
| 5/7 (ad-hoc integration run) | `/integration-verify` | [.claude/commands/integration-verify.md](.claude/commands/integration-verify.md) |
| 7 (PR develop→main) | `/promote` | [.claude/commands/promote.md](.claude/commands/promote.md) |
| 8 (PR review, default) | `/review <PR#>` | [.claude/commands/review.md](.claude/commands/review.md) |
| 8 (PR review, escalation) | `/ultrareview <PR#>` | built-in Claude Code skill (billed; user-triggered) |
| 9 (apply PR review fixes) | `/address-review <PR#>` | [.claude/commands/address-review.md](.claude/commands/address-review.md) |
| — (trivial changes only) | `/quickfix` | [.claude/commands/quickfix.md](.claude/commands/quickfix.md) |

### Lightweight path for trivial changes
For genuinely trivial work (typos, doc tweaks, comment-only edits, one-line obvious fixes), use **`/quickfix`** instead of the full pipeline. It runs unit tests + Spotless, proposes a commit, pauses for approval, commits, and (on a feature branch) optionally merges to `develop`. Skips integration tests and the coverage threshold. Use sparingly — when in doubt, take the full path.

## Backgrounded verify
Because `/verify` supports branch mode (clean tree on a feature branch → diffs against `develop`), it can run as a background agent in an isolated worktree against a committed feature branch while the user continues on a different feature in the foreground. Pattern: user `/ship`s feature A → "spawn a background agent in an isolated worktree to run /verify on feature/A" → user moves on to feature B → agent reports back with a branch to merge.

## Plan document
[PLAN.md](PLAN.md) at the repo root is the source of truth for current goals, decisions, open questions, and the per-ship changelog. Keep it up to date — `/ship` updates it, `/promote` reads from it for the PR body.

## One-time setup
- **GitHub branch protection on `main`** — enforces the no-direct-commits rule at the platform level. In GitHub: **Settings → Branches → Add branch protection rule** for branch name pattern `main`. Recommended settings:
  - Require a pull request before merging (set required approvals to 0 if you're solo, otherwise 1+)
  - Require status checks to pass before merging — add `PR checks / backend` and `PR checks / frontend` (from [.github/workflows/pr-checks.yml](.github/workflows/pr-checks.yml)) once they've run at least once. `Integration tests` (from [.github/workflows/integration-tests.yml](.github/workflows/integration-tests.yml)) fires on push to `main`, so it's not a PR gate but is visible per-commit on the main timeline.
  - Require linear history (forces fast-forward / rebase; prevents merge commits)
  - Do not allow administrators to bypass the above

  After the first PR has triggered the workflow, run [scripts/enable-required-pr-checks.sh](scripts/enable-required-pr-checks.sh). The script PUTs the full baseline (PR review + linear history + admin enforcement + no-force/delete + the two required contexts) in a single API call, so it can stand in for the UI step entirely — but only after a workflow run has registered the contexts with GitHub.
- **`gh` CLI** — install via `brew install gh && gh auth login`. Required for `/promote` and `/address-review`.

## Automated feedback
A `Stop` hook runs `mvn test -q` after each of my turns when there are uncommitted `.java` changes (see `.claude/hooks/test-on-java-changes.sh`). Failing tests surface automatically; you don't need to ask me to run them.
