---
description: Run the integration test suite against the current committed branch state. Building block used by /merge; also runnable standalone (e.g., on develop before /promote).
---

You are running the integration verification gate. It boots the full Spring context and exercises endpoints over real HTTP. This does not modify code — it's a pass/fail gate.

**Backend path.** `mvn` invocations below run from `backend/`.

This command is the underlying integration-test runner. `/merge` invokes equivalent logic as part of the pre-merge gate. Use this standalone when you want to verify integration tests on any committed branch (e.g., on `develop` before invoking `/promote`, or on a feature branch ad-hoc).

## 0. Preflight
- Confirm the working tree is clean with `git status --porcelain`. If there are uncommitted changes, stop and surface them — integration verification should run against a known committed state, not a moving target.
- Print the current branch and HEAD SHA in the report so the user knows what was verified.

## 1. Run integration tests
Invoke the integration-tests Maven profile:

    mvn test -P integration-tests

This activates a Surefire config that runs only tests tagged `@Tag("integration")` (under `src/test/java/.../integration/`). These tests use `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate` to boot the full Spring context on a random port and hit endpoints over real HTTP.

## 2. Handle results
- **All pass**: report success with the count of tests run.
- **Any fail**: stop. Surface the failing test names, the assertion / exception, and the relevant stack trace. Do not attempt to "fix" — integration test failures on a committed branch usually mean a real regression and warrant a bug-fix branch (workflow step 6), not silent edits here.
- **No integration tests found**: report this clearly. Suggest creating one in `backend/src/test/java/com/example/deskscheduler/integration/`.

## 3. Report
End with a short summary:
- Branch + commit SHA being verified
- Integration tests run: pass/fail counts
- If failures: top 1-2 failures with one-line summaries
- Recommendation: proceed to `/promote` (develop → main PR), or branch for bug fix

Do not push, do not commit, do not modify any files.
