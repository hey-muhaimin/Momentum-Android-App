# Product Requirements Document (PRD)

## Momentum v2: The Personal Behavioral Scientist

| **Document Meta** | |
| :--- | :--- |
| **Product** | Momentum Android App |
| **Version** | 2.0 |
| **Status** | Implemented (Phase 1) |
| **Target Release** | Q4 2026 |
| **Design System** | `DESIGN.md` v1.0 |

---

## 1. Executive Summary
Momentum v1 established the market position of an **offline-first, privacy-preserving habit tracker that surfaces correlations** between habits and mood. 

**Momentum v2** evolves the product from a *tracker that shows what worked* into a **Personal Behavioral Scientist that teaches you your own operating system**. By introducing passive sensing (via Android Health Connect simulation and APIs), time-lagged causal analysis (T-0, T-1, T-2), and context-aware insights, Momentum v2 answers not just *"Did I do it?"* but *"When, why, and under what conditions does this habit actually change my life?"*

---

## 2. Core Vision & Principles

1. **Zero Network Requirement:** Fully offline-first. All data, analysis, and correlations run locally on device with Room DB.
2. **Read-Only Passive Sensing:** Health Connect reads sleep duration and step count directly into local `HabitLog` schemas without sending data anywhere.
3. **Honest Data & Statistical Gating:** No fake or made-up correlation percentages. Patterns require a minimum of 5 dual-observation days (habit + mood) to prevent false statistical signals.
4. **User-in-the-Loop Confirmation:** Passively sensed data flags habits for tap confirmation, keeping habit formation intentional.

---

## 3. Core Features Implemented (Phase 1)

### 3.1 Passive Sensing & Health Connect Integration
- **Tracking Sources:** Manual, Health Connect Sleep (Hours), Health Connect Steps (Count).
- **Target Value Configuration:** Configurable target numbers (e.g. 8.0 hours sleep, 10,000 steps).
- **Unconfirmed Sync State:** Passively sensed entries display a sync icon (`Icons.Default.Sync`) and an intuitive "Tap to confirm" flow on the Today screen.

### 3.2 Time-Lagged Causal Analysis Engine
- **Lag Windows Supported:**
  - `T0_SAME_DAY`: Same day habit completion vs. mood rating.
  - `T1_PREVIOUS_DAY`: Previous day habit completion vs. today's mood rating.
  - `T2_TWO_DAYS_PRIOR`: 2-day residual impact on mood.
  - `TIME_OF_DAY`: Morning vs. Afternoon vs. Evening completion impact.
- **Natural Language Causal Insights:** Generates clear human explanations like: *"Exercising yesterday increased your mood today by +0.8 points."*

### 3.3 Redesigned UI & Monthly Calendar
- **Today Screen:** Clean brand top bar, streak counters, centered ENERGY & MOOD rating gauge with responsive emoji sizing and spacing, habit rows with sync status.
- **Insights Screen:** Time-lagged causal engine with interactive Lag Window chips (`T-0`, `T-1`, `T-2`, `By Time`), interactive mood/habit line charts, and honest gathering signal indicators.
- **Chain Screen:** Full monthly calendar grid view with month navigation (`< Month Year >`), completed stats, streak counter, completion rate, and Grace day indicators.

---

## 4. Local Architecture & Data Schema
- **Local Persistence:** Room Database with KSP.
- **Entities:**
  - `Habit`: `id`, `name`, `colorHex`, `iconKey`, `sourceType` (MANUAL, SLEEP_HOURS, STEP_COUNT), `healthConnectTargetValue`.
  - `HabitLog`: `id`, `habitId`, `date`, `completed`, `exactTimestamp`, `detectedValue`, `isConfirmed`.
  - `MoodLog`: `id`, `date`, `score` (1..5), `timestamp`.

---

## 5. Technical Stack
- **Framework:** Jetpack Compose (Kotlin)
- **Database:** Room (SQLite) with KSP
- **Asynchronous Flow:** Kotlin Coroutines & Flow (`collectAsStateWithLifecycle`)
- **Theme:** Material Design 3 (M3) dark/light adaptive color system
