# Momentum — Habit & Mood Intelligence Android App

**Momentum** is a modern, privacy-first Android application built with Kotlin and Jetpack Compose that tracks habits and daily energy/mood side-by-side. Designed around modern Material 3 design guidelines with default dark theme support, Momentum allows users to discover how their daily routines impact their subjective energy levels and well-being over time.

---

## 🌟 Key Features

- **Default Dark Theme & Design System**: Custom `MomentumTheme` featuring high-contrast dark palette, dynamic habit tile tinting (`sleep`, `move`, `noPhone`, `water`), refined Material 3 typography scale, and 4dp grid dimensions.
- **Today Screen & Mood Gauge**: 
  - Centered "ENERGY & MOOD" heading with 5 rating emojis (`😔`, `😐`, `🙂`, `😊`, `⚡`).
  - Interactive habit completion list with animated checkmarks, habit category icons, and flame streak badges.
  - Daily check-in reminder quick settings.
- **Monthly Habit Calendar (Chain)**:
  - Interactive monthly grid with previous/next month navigation.
  - Color-coded daily states (`DONE`, `GRACE`, `MISS`, `FUTURE`).
  - Month statistics: Total completed days, active streak length, and completion rate %.
  - Built-in grace period logic (1 missed day per week supported without breaking streak).
- **Insights & Correlation Engine**:
  - Uncovers positive energy correlations between specific completed habits and mood scores.
  - Visual progress indicators showing data signal readiness.
- **Full Onboarding Flow**:
  - Guided 4-step onboarding (Welcome, Pick Habits, First Check-in, Evening Reminder Setup).
  - Custom habit creation with emoji/icon selection and color pickers.
- **Habit Reminders & Management**:
  - Flexible per-habit notification time selectors.
  - Full edit, archive, and delete bottom sheet controls.

---

## 🏗️ Technical Architecture

- **Language & Runtime**: Kotlin 1.9+ / JVM
- **UI Framework**: Jetpack Compose (100% declarative UI) with Material Design 3
- **State Management**: Jetpack `ViewModel`, `StateFlow`, `collectAsStateWithLifecycle`
- **Data Persistence**: Room Database for offline-first local persistence
- **Navigation**: Jetpack Compose Navigation
- **Design System Tokens**:
  - `Theme.kt`: Light & Dark color schemes (`LightColors`, `DarkColors`), custom CompositionLocal `LocalHabitTints`, `MomentumTypography`, and `MomentumShapes`.
  - `dimens.xml` & `colors.xml`: XML fallback tokens for system UI consistency.

---

## 🛠️ Project Structure

```
app/src/main/java/com/momentum/app/
├── MainActivity.kt               # Entry point & Scaffold navigation
├── data/
│   ├── model/                    # Habit & HabitLog data entities
│   └── database/                 # Room DB & DAOs
├── ui/
│   ├── theme/
│   │   ├── Color.kt              # Color tokens
│   │   ├── Theme.kt              # MomentumTheme & CompositionLocal providers
│   │   └── Type.kt               # M3 Typography mapping
│   ├── components/
│   │   ├── FlameBadge.kt         # Animated streak flame badge
│   │   ├── HabitDetailSheet.kt   # Create & Edit habit bottom sheet
│   │   ├── HabitRow.kt           # Habit item list card
│   │   ├── IconHelper.kt         # Key-to-Vector icon resolver
│   │   ├── MoodGauge.kt          # Centered 5-point mood gauge
│   │   └── ReminderTimeSelector.kt # Flexible time picker component
│   └── screens/
│       ├── TodayScreen.kt        # Primary daily dashboard
│       ├── ChainScreen.kt        # Monthly habit calendar view
│       ├── InsightsScreen.kt     # Energy correlation analytics
│       ├── OnboardingScreen.kt   # 4-step onboarding flow
│       └── MainViewModel.kt      # Core UI state manager
```

---

## 🚀 Getting Started

1. Open the project in Android Studio (Ladybug or newer recommended).
2. Ensure JDK 17+ and Android SDK 34/36 are configured.
3. Run `:app:assembleDebug` or press **Run** in Android Studio to launch on a target device or emulator.
4. Verify tests via `./gradlew :app:testDebugUnitTest`.

---

## 🔒 Privacy & Data

All habits, logs, and mood ratings are stored locally on-device using Room Database. No analytics or personal data leave the device.
