# Product Requirements Document (PRD) — Momentum

## 1. Executive Summary
**Momentum** is a personal habit and energy tracking application designed for Android. Unlike conventional habit trackers that measure completion in isolation, Momentum correlates completed daily habits against the user's subjective mood and energy levels. This enables users to answer questions like: *"Which habits actually improve my mood and focus?"*

---

## 2. Product Objectives & Target Audience
- **Target Audience**: Individuals seeking mindful routine building, productivity tracking, and self-reflection without overwhelming complexity or aggressive gamification.
- **Core Goals**:
  1. Provide a frictionless daily check-in experience under 10 seconds.
  2. Maintain dark-theme visual comfort with high contrast, spacious padding, and responsive touch targets.
  3. Offer a calendar view of monthly consistency with built-in grace logic for realistic habit retention.
  4. Uncover habit-energy correlation insights automatically as logs accumulate.

---

## 3. Key Feature Specifications

### 3.1 Onboarding & First-Time User Experience (FTUE)
- **Step 1 (Welcome)**: Introduces the core value proposition ("Tracks habits & mood side-by-side").
- **Step 2 (Habit Selection)**: Pre-suggested habits (`Sleep 7+ hrs`, `Move 20 min`, `Read`, `No phone in bed`, `Drink water`, `Journal`) + custom habit creation.
- **Step 3 (First Log)**: Option to rate initial mood/energy score (1–5) and mark habits completed for today.
- **Step 4 (Reminders)**: Evening check-in time selection with Android notification permission handling.

### 3.2 Today Dashboard
- **Top Bar**: Streak counter badge (`FlameBadge`), reminder settings shortcut, and brand title.
- **Energy & Mood Gauge**:
  - Centered text heading: `ENERGY & MOOD`.
  - 5 animated emoji rating buttons (`😔`, `😐`, `🙂`, `😊`, `⚡`).
  - Smooth bouncy scale animation and shadow elevation on selection.
- **Habit Checklist**:
  - Distinct tile background tints for habit categories (`sleep`, `move`, `noPhone`, `water`).
  - Haptic feedback and animated checkmark scale on completion toggle.
  - Floating Action Button (FAB) for adding new habits.

### 3.3 Monthly Habit Calendar ("Chain")
- **Habit Selector**: Horizontal scrolling chip row to select an active habit.
- **Calendar Grid**: 7-column monthly grid (Mon–Sun) with previous/next month navigation buttons.
- **Cell States**:
  - `DONE`: Primary habit accent color with checkmark.
  - `GRACE`: Outlined cell for single missed days buffered by surrounding completions.
  - `MISS`: Dark gray background for uncompleted elapsed days.
  - `FUTURE`: Low opacity background.
- **Monthly Metrics**: Completed days count, streak days, and completion percentage rate.

### 3.4 Insights & Analytics
- **Correlation Cards**: Identifies top habits linked to higher energy ratings.
- **Data Signal Indicator**: Prompts the user when additional log days are required for statistically meaningful correlations.

---

## 4. UI/UX & Design Guidelines
- **Color Palette**: Dark background (`#121417`), dark surface containers (`#1D2026`), primary brand teal (`#7DD8C6`), secondary warm accent (`#FFB84D`), purple accent (`#B08CFF`), coral error (`#FF7A6E`).
- **Typography**: Clean sans-serif headings paired with monospace labels and metrics for high technical legibility.
- **Accessibility**: Touch targets >= 48dp, minimum contrast ratios meeting WCAG AA.

---

## 5. Future Roadmap
- Cloud sync and backup export (JSON / CSV).
- Home Screen Widgets for quick daily habit check-ins.
- Detailed correlation charts (Vico / Compose Charts integration).
