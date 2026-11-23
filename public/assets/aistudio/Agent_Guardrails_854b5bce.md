# AGENTS.md — Repository Rules for Momentum

## Source of Truth
- Read `PRD.md` first for product scope, data models, and non-goals. Do not add features listed under **Non-Goals** unless explicitly instructed.

## Working Style for Autonomous Agents
1. **Plan Before Coding:** State the files you plan to create/edit and the verification command you will run.
2. **Small, Vertical Slices:** Implement complete, testable slices (e.g., Room schema + DAO + ViewModel + Compose screen) rather than half-wired stubs.
3. **Preserve Package & Release Continuity:** Never alter `applicationId`, keystore signing configs, or existing Room database migration paths without explicit confirmation.
4. **No Fake Data or Silent Swallows:** Never use hardcoded mock timers in production code paths; handle edge cases (e.g., midnight date boundaries, timer pause/resume math) explicitly.

## Verification Checklist (Run Before Finishing)
- Unit tests pass: `./gradlew testDebugUnitTest`
- Debug AAB/APK builds cleanly: `./gradlew assembleDebug bundleDebug`
- Verify no compiler warnings are introduced in modified Kotlin files and no secrets/API keys are committed.