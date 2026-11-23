# PRD: Momentum (Focus Timer & Daily Micro-Habits)

## 1. Overview & Goal
Build and refine **Momentum**, a fast, offline-first Android focus timer and daily micro-habit check-in app designed for students, creators, and working adults. The release prioritizes reliability, zero-friction onboarding, and clean Material 3 UI so closed-test users and returning users enjoy effortless daily sessions.

## 2. Target Audience & Core Jobs-To-Be-Done
- **Primary Users:** Students, young professionals, and self-learners (ages 16–35) looking for a distraction-free focus timer and light daily reset.
- **Jobs-To-Be-Done:**
  1. Start a focus sprint (e.g., 25 min) and rest break (e.g., 5 min) in one tap.
  2. Jot down an intrusive thought ("Brain Dump") mid-sprint without leaving the timer.
  3. Complete 1–3 lightweight daily habits (e.g., Hydrate, Stretch, Wind Down) and see weekly consistency.

## 3. Tech Stack & Release Continuity Guidelines
- **Platform:** Android (minSdk 24, targetSdk 35+), Kotlin + Jetpack Compose + Material 3.
- **Play Store Continuity:** Preserve the existing `applicationId` (package name) and signing keystore from the prior **Momentum** Play Store release, incrementing `versionCode` and `versionName` cleanly.
- **Persistence:** Local Room database + DataStore preferences (100% offline-first; no backend or account required).
- **Timer Service:** Foreground service with live notification progress so timers continue accurately when the screen locks or the app is backgrounded.
- **Architecture:** Clean MVVM with unidirectional state (`StateFlow`) and clear module/package boundaries (`ui/`, `domain/`, `data/`, `service/`).

## 4. Functional Requirements & Acceptance Criteria

### FR-1: Focus & Break Timer
- Presets: `25/5` (Classic Sprint), `50/10` (Deep Block), plus custom minutes (`1–120` focus, `1–30` break).
- Controls: Start, Pause, Resume, Reset, Skip to Break/Next Sprint.
- Persistence: Survives configuration changes, backgrounding, and process death via persisted start timestamp + remaining duration.
- Notification: Ongoing notification during active timer; completion alert/sound/vibration when a phase finishes.

### FR-2: Quick Brain Dump Notes
- Accessible directly from the active timer screen via a lightweight bottom sheet or card.
- Users can add, check off, or delete quick distraction notes tied to the current day.

### FR-3: Daily Micro-Habits (Up to 3 Active Habits)
- Pre-populated with 3 sensible defaults (editable/replaceable):
  1. `Drink 6 glasses of water`
  2. `10-min away-from-screen break`
  3. `Plan tomorrow's top 1 task`
- One-tap toggle for today's completion; automatic daily reset at local midnight while preserving history.
- Simple weekly summary (e.g., completed count out of 21 possible check-ins over 7 days) without harsh streak penalties.

### FR-4: Settings & Feedback Helper
- Toggle sound/vibration alerts and default timer preset.
- "Send Test Feedback" action that copies or opens a mail/share intent with app version and device/OS summary (no sensitive data).

## 5. Non-Goals (Out of Scope)
- User accounts, cloud sync, or social leaderboards.
- Ads, subscriptions, or paywalls.
- Complex analytics dashboards or team workspaces.

## 6. Data Models (Conceptual)
- `FocusSession`: `id`, `startedAtEpochMs`, `durationSec`, `mode` (`FOCUS` | `BREAK`), `completed` (`Boolean`).
- `BrainNote`: `id`, `createdAtEpochMs`, `text`, `isDone` (`Boolean`).
- `HabitItem`: `id`, `title`, `targetPerDay` (`1`), `isActive` (`Boolean`).
- `HabitLog`: `id`, `habitId`, `dateIso` (`YYYY-MM-DD`), `completedAtEpochMs`.

## 7. Agent Execution Rules
- Always refer to `AGENTS.md` in the repository root before writing or modifying code.
- Keep diffs scoped to one feature or fix per task.
- Write unit tests for timer state transitions, habit date rollover logic, and Room DAOs.
- Ensure `./gradlew assembleDebug testDebugUnitTest` passes cleanly before concluding a task.