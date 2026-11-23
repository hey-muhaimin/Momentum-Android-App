# Momentum Android — Design System & UI Specification

**Version:** 2.0  
**Status:** Proposed implementation specification  
**Platform:** Android  
**Framework:** Kotlin, Jetpack Compose, Material 3  
**Product direction:** Organic Intelligence  
**Visual reference:** Dark olive surfaces, luminous lime accents, soft botanical atmosphere, compact editorial typography  
**Design principle:** *Understand yourself. Move forward.*

---

## 1. Product design vision

Momentum is not simply a habit checklist. It is a private, offline-first personal behavioral companion that helps people understand how their routines relate to mood, energy and focus.

The interface should make three things feel effortless:

1. **Notice** what matters today.
2. **Act** on a small number of meaningful habits.
3. **Understand** patterns as personal data accumulates.

The visual language should feel calm, premium, warm and quietly intelligent. It can use expressive lime highlights and nature-inspired imagery, but must not become a neon gaming interface or a generic wellness template.

### Product promise

> Small steps. Better understanding. Lasting momentum.

### Experience principles

- **Progress without pressure:** no shame language, punitive streak loss or alarmist red states.
- **Clarity before decoration:** every visual element must support an action, status or insight.
- **Evidence before confidence:** never display invented analytics or imply causation from correlation.
- **Privacy by default:** communicate that personal data and analysis remain on the device.
- **Intentional interaction:** passive health data is surfaced for confirmation rather than silently treated as a deliberate habit completion.
- **Low-friction daily use:** the core check-in should remain achievable in seconds.
- **Accessible calm:** strong contrast, readable type, generous touch targets and reduced-motion support.

---

## 2. Scope and relationship to the existing app

This specification is based on the supplied Momentum Android project and its PRDs, Kotlin screens, components and theme files. It defines a visual and interaction direction, not a replacement architecture.

### Existing product capabilities to preserve

The supplied project includes:

- Kotlin + Jetpack Compose UI and Material 3 theming.
- Room local persistence and repository / DAO layers.
- Habit records and habit logs.
- Mood logging and daily metrics UI.
- Manual habits and Health Connect-derived sleep / step data.
- Unconfirmed passive data that requires user confirmation.
- Habit detail UI.
- Habit streak and calendar concepts, including `DONE`, `GRACE`, `MISS` and `FUTURE` states.
- Focus timer and Brain Dump UI.
- Insights UI with time-lagged analysis concepts: T-0, T-1, T-2 and time-of-day.
- Context and disruption analysis components.
- Onboarding and settings.
- Wear OS integration scaffolding.

The top-level PRD describes Momentum v2 as a **Personal Behavioral Scientist**, with offline-first analysis, local data storage, minimum-observation gating and careful handling of passive sensing. These are product requirements and must survive the redesign.

### Current navigation baseline

In the supplied `MainActivity.kt`, the active bottom navigation currently exposes **Focus**, **Habits** and **Settings**. `TodayScreen`, `InsightsScreen` and `ChainScreen` exist in the project but are not currently connected from that activity's tab switch.

The proposed design navigation below is therefore a product-level target. Integrate existing screens and ViewModels deliberately; do not assume that a screen is already wired merely because its source file exists.

### Non-goals

This redesign does not authorize:

- Replacing Room, repositories, ViewModels or analysis engines with mock data.
- Adding a mandatory account, cloud dependency or network requirement.
- Sending health or mood data to a remote service.
- Presenting correlation as proven causation.
- Removing Health Connect confirmation.
- Adding aggressive gamification, social leaderboards or streak-shaming.
- Rebuilding the app in a different framework.

---

## 3. Brand identity

### Brand character

| Attribute | Direction |
|---|---|
| Personality | Calm, observant, encouraging, intelligent |
| Emotional tone | Optimistic without forced positivity |
| Visual character | Organic, minimal, tactile, quietly futuristic |
| Density | Medium-low; meaningful information, breathable layout |
| Motion | Soft, purposeful, short |
| Metaphor | A personal greenhouse for habits and self-knowledge |

### Naming and voice

Use **Momentum** consistently. Avoid alternate product names in the interface.

Preferred microcopy:
- “A pattern worth noticing”
- “You’re building consistency”
- “Your energy was higher on days you moved”
- “Not enough data yet”
- “Confirm detected activity”
- “A lighter day still counts”
- “Start where you are”

Avoid:
- “You failed”
- “You broke your streak”
- “You are lazy”
- “Guaranteed improvement”
- “This habit causes happiness” when the analysis only supports association.

### Logo and app icon direction

- Primary mark: a simple leaf / forward-motion glyph.
- Wordmark: “Momentum” in a clean, medium-to-bold sans-serif.
- App icon: dark olive field with a luminous lime leaf / growth mark.
- Keep the mark legible at Android launcher sizes.
- Avoid fine detail, tiny text, gradients that collapse at small sizes, and overly literal fitness symbols.

---

## 4. Visual system

### 4.1 Color philosophy

The supplied visual reference uses deep olive-black surfaces with bright yellow-green highlights. The existing app theme already has light and dark Material 3 schemes, but its current green / teal values should be consolidated into a clearer semantic token system.

The following is the proposed **Momentum Organic Intelligence palette**. Treat these values as design tokens and validate contrast in implementation.

#### Dark theme (default)

| Token | Hex | Usage |
|---|---:|---|
| `background` | `#0B0E08` | App root background |
| `surface` | `#11160D` | Main screen surface |
| `surfaceContainer` | `#191F12` | Cards and grouped panels |
| `surfaceElevated` | `#202817` | Dialogs, sheets, raised cards |
| `surfaceMuted` | `#262D1B` | Secondary rows and inset areas |
| `primary` | `#D7F75B` | Main action, active state, progress |
| `primaryPressed` | `#B8D83D` | Pressed / selected interaction |
| `primaryContainer` | `#303B16` | Selected card / chip background |
| `onPrimary` | `#18200A` | Text / icon on lime |
| `textPrimary` | `#F2F3E9` | Main text |
| `textSecondary` | `#B4B9A4` | Supporting text |
| `textMuted` | `#858D76` | Metadata / disabled text |
| `outline` | `#343C27` | Card borders and dividers |
| `success` | `#A9D95B` | Confirmed positive state |
| `warning` | `#E9BD68` | Caution / pending state |
| `info` | `#91B8D0` | Informational / connected state |
| `error` | `#E28D80` | Destructive errors only |

#### Light theme

| Token | Hex | Usage |
|---|---:|---|
| `background` | `#F4F5EA` | App root background |
| `surface` | `#FAFBEF` | Main screen surface |
| `surfaceContainer` | `#FFFFFF` | Cards |
| `surfaceElevated` | `#FFFFFF` | Dialogs and sheets |
| `surfaceMuted` | `#E9EDDA` | Secondary rows |
| `primary` | `#526B16` | Main action and selected state |
| `primaryContainer` | `#E4F0B4` | Selected container |
| `onPrimary` | `#FFFFFF` | Text / icon on primary |
| `textPrimary` | `#1C2114` | Main text |
| `textSecondary` | `#59604A` | Supporting text |
| `textMuted` | `#777D69` | Metadata |
| `outline` | `#D9DEC9` | Borders |
| `success` | `#557C25` | Positive state |
| `warning` | `#95651B` | Pending state |
| `info` | `#416D86` | Informational state |
| `error` | `#B64F43` | Destructive errors only |

### 4.2 Color usage rules

- Lime is a **signal color**, not a background color for every card.
- Use the primary accent for the current action, selected navigation destination, progress and high-value data points.
- Keep most surfaces dark olive / charcoal, not pure black.
- Use warm amber for caution or neutral highlights, never as a second competing primary.
- Use category colors sparingly and consistently.
- Do not encode meaning by color alone. Pair color with an icon, label, shape or text.
- Red is reserved for destructive or genuine error states. A missed habit is not an error.
- Charts must use accessible line / point contrast and labels or legends.

### 4.3 Gradients and imagery

- Prefer subtle tonal gradients between nearby olive surfaces.
- Avoid strong glossy glassmorphism, heavy blur and large neon glows.
- Nature photography may be used for onboarding, empty states or brand storytelling, not behind dense functional screens.
- If a background image is used, apply a strong dark scrim and ensure all content remains readable.
- Do not make functional information dependent on decorative imagery.

---

## 5. Typography

Use the platform sans-serif family by default. The supplied project currently maps Material 3 typography to a sans-serif scale; retain this approach unless a bundled font is introduced intentionally.

| Role | Size | Weight | Use |
|---|---:|---|---|
| Display | 34–38sp | ExtraBold | Hero metrics / streak number |
| Screen title | 26–30sp | Bold | Main screen heading |
| Section title | 18–20sp | Bold | Card and section headings |
| Card title | 15–16sp | SemiBold | Habit / insight title |
| Body | 14–15sp | Normal | Main descriptions |
| Supporting | 12–13sp | Medium | Secondary information |
| Eyebrow | 10–11sp | Bold, tracked | Small section labels |
| Metric | 28–36sp | Bold | Progress, timer, key values |

Rules:
- Use tabular numerals for timers, percentages and metrics where possible.
- Keep body line-height comfortable (approximately 1.4–1.55).
- Avoid long all-caps labels.
- Do not use monospace for all text. Reserve it for technical metadata only if it improves scanning.
- Support Android font scaling and avoid fixed-height text containers that clip larger fonts.

---

## 6. Layout, spacing and shape

### 6.1 Grid and spacing

Use a 4dp base spacing scale.

| Token | Value |
|---|---:|
| `space.1` | 4dp |
| `space.2` | 8dp |
| `space.3` | 12dp |
| `space.4` | 16dp |
| `space.5` | 20dp |
| `space.6` | 24dp |
| `space.8` | 32dp |
| `space.10` | 40dp |
| `space.12` | 48dp |

- Screen horizontal padding: 20dp (16dp minimum on compact devices).
- Standard vertical section gap: 24dp.
- Card internal padding: 16–20dp.
- Habit row vertical padding: 14–16dp.
- Bottom navigation content must respect system gesture insets.
- Use scrollable content rather than compressing cards on smaller devices.

### 6.2 Corner radii

| Token | Value | Use |
|---|---:|---|
| `radius.xs` | 8dp | Small badges / calendar cells |
| `radius.sm` | 12dp | Icon containers / compact chips |
| `radius.md` | 18dp | Habit rows / standard cards |
| `radius.lg` | 24dp | Hero cards / large panels |
| `radius.xl` | 30dp | Bottom sheets / onboarding panels |
| `radius.pill` | 999dp | Chips / buttons / progress tracks |

Use rounded shapes consistently. Avoid mixing too many unrelated radii on one screen.

### 6.3 Elevation and borders

- Prefer tonal surface separation over large drop shadows.
- Use a 1dp outline for cards that need separation.
- Reserve stronger elevation for dialogs, modal sheets and floating controls.
- Avoid borders around every nested element.

---

## 7. Navigation architecture and app shell

### Proposed primary navigation

Use four primary destinations:

1. **Today** — daily overview and check-in.
2. **Habits** — habit management and habit detail.
3. **Insights** — behavioral patterns and analytics.
4. **Focus** — focus timer and sessions.

**Chain / History** should be accessible from a Today progress / streak entry point or from an individual habit detail. It is a meaningful destination, but does not need to compete with the four daily-use destinations in the primary bar.

**Profile / Settings** should be accessible from the Today top bar avatar or a profile entry point. Keep settings out of the main four-tab hierarchy.

### Navigation behavior

- Use a Material 3 `NavigationBar` adapted to the Organic Intelligence visual system.
- Selected destination: lime icon + compact selected pill / tonal background + readable label.
- Unselected destinations: muted icon and label.
- Preserve tab state when switching destinations.
- Use Navigation Compose if the app adopts a navigation graph; otherwise implement a clear state-driven navigation layer without scattering tab integers through screen code.
- Deep links and system back behavior should be defined before adding more destinations.
- Do not use icon-only navigation without accessible labels.

### App shell

- Edge-to-edge content with safe system-bar insets.
- Background follows selected theme.
- Top app bar is minimal and screen-specific.
- Snackbar messages use the app's semantic surface and text colors.
- Bottom navigation remains visually quiet and must not obscure scroll content.

---

## 8. Screen specifications

## 8.1 Onboarding

**Goal:** Explain the unique value of Momentum, collect an initial set of habits and get the user to their first meaningful check-in.

The existing project has a multi-step onboarding screen. Preserve the four-step product flow while applying the new visual language.

### Step 1 — Welcome

- Full-screen deep olive background.
- Leaf / growth mark and Momentum wordmark.
- Headline: “Understand yourself. Move forward.”
- Supporting copy: “Build routines and discover how they connect to your mood and energy.”
- Primary CTA: “Get started”.
- Optional small privacy note: “Your data stays on your device.”

### Step 2 — Choose habits

- Title: “What would you like to build?”
- Selectable preset habit cards.
- Presets from the supplied app PRD include sleep, movement, reading, no phone in bed, drinking water and journaling.
- Custom habit entry remains available.
- Selected state uses lime outline / filled icon container, not a large bright panel.
- Explain that habits can be changed later.

### Step 3 — First check-in

- Explain mood / energy rating briefly.
- Allow optional initial rating.
- Make skipping safe and explicit.
- Never block the user from entering the app if they do not want to log immediately.

### Step 4 — Reminders

- Let the user choose a reminder time.
- Explain notification permission at the moment it is needed.
- Permission denial must not block core app usage.
- Provide a clear “Not now” path.

### Onboarding interaction states

- Current step indicator.
- Back and continue controls.
- Disabled continue only when a genuinely required input is missing.
- Preserve partially entered values when moving backward.
- Avoid fake testimonials, fabricated stats or claims of guaranteed outcomes.

---

## 8.2 Today dashboard

**Goal:** Make the next useful action obvious and make daily progress feel encouraging.

### Hierarchy

1. Greeting, date and profile shortcut.
2. Daily progress hero.
3. Mood and energy check-in.
4. Contextual insight / adaptive grace message.
5. Today's habits.
6. Optional focus shortcut / recent focus summary.

### Header

- Greeting changes by local time: “Good morning”, “Good afternoon” or “Good evening”.
- Show the user's first name only if available.
- Date uses the device locale.
- Avatar / profile icon opens profile and settings.
- Avoid crowding the header with multiple utility icons.

### Daily progress hero

- Large circular progress ring or a clear progress bar.
- Show completed count and total, e.g. “3 of 5 habits”.
- Center label should explain the value: “Today” or “Daily progress”.
- Do not imply that a partially completed day is a failure.
- Progress animation should be subtle and respect reduced motion.
- If no habits exist, replace the ring with a helpful empty state and “Add your first habit”.

### Mood and energy check-in

The existing `DailyMetricsForm` and `MoodGauge` components represent mood / energy logging. Preserve their data flow and rating scale; do not silently change the stored scale.

- Present as a dedicated card titled “How are you feeling?”
- Make mood and energy distinct controls if the data model stores them separately.
- If the current data model stores only a mood score, do not visually imply an independent energy measurement without implementing the corresponding data support.
- Use clear labels and emoji / icon cues.
- Show “Logged” or “Not logged yet”.
- Keep the action compact and finishable in a few taps.
- Provide an edit path after logging.

### Contextual insight card

Use a single high-value insight or context note, not a wall of analytics.

Example:
- Eyebrow: “A pattern worth noticing”
- Message: “You tend to report better mood on days you move.”
- Supporting detail: “Based on 6 matched days” (only if the actual engine provides this count).
- CTA: “Explore insight”

Rules:
- Display only if the relevant analysis passes its minimum data threshold.
- Use language appropriate to association strength.
- If there is insufficient data, show a supportive learning-state message such as “Your patterns will appear as you log a little more.”
- If disruption / grace logic is active, explain it neutrally and do not frame it as a penalty.

### Today's habits

- List active habits in a consistent vertical stack.
- Each row: icon / category tint, habit name, optional target / progress, source status and completion control.
- Completed state uses a clear checkmark and muted secondary styling, while preserving legibility.
- Pending Health Connect entry displays a sync / confirmation state with “Tap to confirm”.
- Manual completion should provide light haptic feedback and a short check animation.
- Habit detail opens on row tap; completion control remains a separate, easy-to-hit target.
- Use a FAB or inline add action only if it does not compete with the primary task.

### Empty and edge states

- No habits: invite user to add one.
- No mood log: prompt a short check-in without blocking habit completion.
- All habits complete: celebrate quietly; avoid confetti overload.
- Passive value pending confirmation: clearly identify source and ask for confirmation.
- Offline: no error banner; local functionality continues.

---

## 8.3 Habits

**Goal:** Help users create, organize and maintain routines without making the screen feel like a spreadsheet.

### Main screen

- Header: “Habits” with a short count or supporting line.
- Add action in the top bar.
- Optional filter chips: All, Daily, Health, Personal (only if categories exist in the model).
- Habit rows grouped only when grouping improves navigation.
- Keep sorting predictable.

### Habit row anatomy

1. Category icon in a tinted container.
2. Habit title.
3. Small supporting line: schedule, target or current streak.
4. Optional source / sync indicator.
5. Completion state or detail affordance.

Do not show a streak on every row if it creates visual noise. Prioritize today's completion and target.

### Create habit flow

The create flow should support:
- Habit name.
- Icon / category.
- Cadence or schedule, where supported.
- Optional target value.
- Tracking source: manual or supported Health Connect source.
- Reminder configuration.
- Save / cancel.

Presets can be shown as suggestions, but custom habit creation must remain first-class.

### Habit detail

The existing `HabitDetailSheet` should be the starting point.

Recommended sections:
- Habit name and icon.
- Current target / cadence.
- Completion history.
- Streak and consistency summary.
- Source and Health Connect status.
- Related mood / energy patterns when sufficient data exists.
- Edit and archive / delete actions.

Use a modal bottom sheet on compact screens if the existing interaction model supports it. Ensure long content can scroll and controls remain reachable.

### Health Connect states

- Connected and synced.
- Permission not granted.
- No recent data.
- Detected value awaiting confirmation.
- Sync error.

Do not represent missing health data as zero. Explain permission requirements in plain language and allow manual tracking where appropriate.

---

## 8.4 Insights

**Goal:** Turn collected logs into honest, understandable personal observations.

This is Momentum's product differentiator. The interface must make evidence quality visible and make uncertainty understandable.

### Screen hierarchy

1. Title: “Insights”.
2. Short description: “Learn what seems to shape your days.”
3. Time range / lag controls.
4. Primary finding.
5. Trend visualization.
6. Habit relationship / time-lagged analysis.
7. Contextual insight or experiment.
8. Data sufficiency explanation.

### Lag selector

Preserve the supported analysis windows:
- `T0` — same-day relationship.
- `T-1` — previous-day habit vs today's mood.
- `T-2` — two-days-prior relationship.
- `By time` — morning / afternoon / evening analysis.

Use concise chips with a selected lime state. Include explanatory tooltips or supporting text for users unfamiliar with lag windows.

### Trend cards

- Mood / energy trend over time.
- Habit completion trend.
- Clear axis labels, units and date range.
- Avoid misleading truncated axes.
- Show the actual sample size.
- Provide empty, loading and insufficient-data states.
- Charts must remain understandable in dark and light themes.

### Correlation / relationship cards

Recommended structure:
- Habit name.
- Outcome name (mood or energy).
- Direction and measured difference / coefficient only when supplied by the analysis engine.
- Number of matched observations.
- Selected lag window.
- Plain-language explanation.
- Caveat that an observed relationship does not prove causation.

### Statistical honesty

The top-level PRD specifies a minimum of **5 dual-observation days** before surfacing patterns. Respect the actual engine's gating and do not introduce a lower UI-only threshold.

When data is insufficient:
- Show how many matched days are available if the source exposes that count.
- Explain what kind of logging would help.
- Do not display placeholder percentages or decorative fake charts.
- Do not invent confidence scores.

When data is sufficient:
- Show the result and sample count.
- Use “associated with”, “tends to” or “was observed alongside” unless the underlying methodology supports stronger causal language.
- Keep correlation and causal inference distinct.

### Experiments

If an experiment feature is surfaced from existing models:
- Explain the hypothesis.
- Show the habit / variable being tested.
- Show duration and status.
- Present a clear start / pause / finish action.
- Never imply that an experiment has produced a result before its observations exist.

---

## 8.5 Focus

**Goal:** Offer a calm, low-friction environment for a focused work session.

The supplied `FocusScreen` includes timer state, presets, Brain Dump notes and focus totals. Preserve those behaviors.

### Focus screen

- Header: “Focus” and concise supporting line.
- Large timer ring.
- Remaining time in tabular numerals.
- Primary play / pause control.
- Preset chips such as Focus, Study and Relax, based on the existing supported presets.
- Brain Dump entry point.
- Today's focus total / session summary if available.

### Timer states

- Idle: show selected duration and Start.
- Running: show remaining time and Pause.
- Paused: show remaining time and Resume.
- Complete: show a restrained completion state and a clear next action.
- Interrupted / app backgrounded: restore timer state accurately using the existing timer logic.

### Brain Dump

- Use a sheet or dedicated editor.
- Keep the entry quick and private.
- Provide save / close behavior.
- Do not discard text accidentally.
- If notes are not persisted by the current implementation, do not imply that they are saved permanently.

### Completion reflection

Optional, lightweight:
- “How did that session feel?”
- Do not force a rating before the user can leave.
- Keep focus session data local.

---

## 8.6 Chain / History

**Goal:** Show consistency over time while treating real life realistically.

The supplied `ChainScreen` contains calendar states `DONE`, `GRACE`, `PAUSED`, `MISS` and `FUTURE`. Preserve their semantic distinctions.

### Screen hierarchy

- Header: “Your journey” or “History”.
- Selected habit, if the calendar is habit-specific.
- Current streak / consistency metric.
- Month selector.
- Calendar grid.
- Monthly summary.

### Calendar states

| State | Visual | Meaning |
|---|---|---|
| `DONE` | Filled primary / lime cell with check | Habit completed |
| `GRACE` | Outlined or softly tinted cell | Grace logic applied |
| `PAUSED` | Muted neutral cell with pause marker | Habit intentionally paused |
| `MISS` | Neutral low-emphasis cell | Elapsed day without completion |
| `FUTURE` | Low-contrast surface | Date has not occurred |

- Never use bright red for a missed day.
- Use icon / shape in addition to color.
- Month navigation must work with TalkBack labels.
- Calendar should support Monday-first or locale-aware week start consistently.
- Provide an accessible text summary of the calendar.

### Summary metrics

- Completed days.
- Current / best streak if supported.
- Completion rate.
- Grace days.
- Period label.

Do not mix habit-specific and all-habits statistics without labeling the scope.

---

## 8.7 Profile and settings

**Goal:** Make personal preferences and integrations easy to find without overwhelming the daily experience.

### Profile overview

- Avatar or initials.
- Display name.
- “Your journey” summary.
- Current streak / monthly consistency only when backed by data.
- Entry points for preferences, goals and integrations.

### Settings groups

1. Appearance: System / Light / Dark.
2. Reminders and notifications.
3. Health Connect.
4. Data and privacy.
5. Backup / restore only if implemented.
6. Help and about.
7. App version.

### Privacy messaging

Use precise language:
- “Your habit and mood data is stored locally on this device.”
- “Health Connect data is read for the features you enable.”
- Do not claim encrypted backup, cloud sync or deletion behavior unless implemented and verified.

### Appearance

Default to **System / Adaptive**. The app should support light and dark themes. Dark is the primary art direction shown in the reference, but light mode must be a complete, intentional design rather than a fallback.

---

## 9. Reusable component library

Build or consolidate components around semantic roles. Avoid duplicating one-off card styling across screens.

| Component | Purpose | Key states |
|---|---|---|
| `MomentumTopBar` | Screen title, date, avatar / action | Default, scrolled |
| `MomentumBottomBar` | Primary app destinations | Selected, unselected |
| `ProgressRing` | Daily progress / focus timer | Empty, partial, complete |
| `MetricCard` | Mood, energy, streak or summary | Logged, pending, unavailable |
| `HabitRow` | Habit status and action | Active, done, pending confirmation |
| `HabitIconTile` | Category icon background | Category variants |
| `InsightCard` | Behavioral observation | Sufficient data, insufficient data |
| `LagSelector` | T0 / T-1 / T-2 / time-of-day | Selected, unselected |
| `TrendCard` | Chart with title and range | Loading, data, empty |
| `CalendarCell` | Daily history state | All five calendar states |
| `ReminderRow` | Reminder summary and edit | Enabled, disabled |
| `PrimaryButton` | Main action | Enabled, pressed, disabled, loading |
| `SecondaryButton` | Secondary action | Enabled, pressed, disabled |
| `FilterChip` | Filter selection | Selected, unselected |
| `EmptyState` | Helpful no-data state | Screen-specific |
| `ConfirmationSheet` | Confirm passive data / destructive action | Open, closed |

### Component rules

- Components receive state and callbacks; they should not own repository or database logic.
- Use Material 3 foundations where appropriate, then apply Momentum tokens.
- Keep components previewable with realistic sample data.
- Provide semantic content descriptions for meaningful icons.
- Use a single source of truth for colors, typography, shapes and spacing.
- Prefer a small, reusable component set over a large library of nearly identical cards.

---

## 10. Interaction and motion

### Motion principles

Motion should explain change, not decorate inactivity.

| Interaction | Motion guidance |
|---|---|
| Habit completion | 160–220ms scale / check transition |
| Progress ring | 350–550ms eased sweep |
| Tab change | Minimal fade / content transition |
| Sheet opening | Native Material 3 motion |
| Insight reveal | Short fade / vertical movement |
| Timer state | No continuous distracting animation |
| Calendar month change | Short crossfade |

- Use Compose animation APIs with restrained easing.
- Respect Android animator duration scale and reduced-motion preferences.
- Do not animate every card on initial load.
- Avoid parallax, pulsing glow and continuous decorative motion.

### Haptics

- Light confirmation haptic for habit completion.
- Selection haptic for meaningful controls where appropriate.
- No haptic on every slider tick.
- Respect system settings and avoid relying on haptics as the only feedback.

### Touch and gesture

- Minimum interactive target: 48dp.
- Separate habit detail tap from completion action.
- Do not require swipe gestures for essential actions.
- Provide explicit buttons for destructive or irreversible operations.

---

## 11. Accessibility and responsive behavior

- Meet WCAG AA contrast targets where applicable.
- Minimum touch target: 48dp.
- Support TalkBack with meaningful labels and logical traversal order.
- Charts need textual summaries or accessible data descriptions.
- Do not use color alone to communicate habit state.
- Support font scaling without clipping or overlapping.
- Respect system dark / light theme selection.
- Test compact devices and large font settings.
- Keep important actions reachable one-handed.
- Avoid tiny chart labels and dense calendar legends.
- Use locale-aware date, time and number formatting.

---

## 12. Data integrity, privacy and insight language

This section is a design constraint, not optional polish.

### Offline-first behavior

The app must remain useful without a network connection. Screens should read from local state and show local results. Do not introduce remote-only loading states for core features.

### Health Connect

- Request only permissions needed for selected features.
- Explain why each permission is requested.
- Make the source of a detected value visible.
- Require user confirmation for passive entries as described by the PRD.
- Never treat permission denial as a user failure.

### Analytics

- Use real values from the existing ViewModels / analysis engine.
- Respect minimum matched-observation thresholds.
- Label the period and sample count.
- Explain missing or insufficient data.
- Avoid fabricated sample charts in production.
- Distinguish descriptive trends, correlations and causal claims.

### Privacy

- Avoid displaying sensitive mood / health values in notification text by default.
- Do not log private notes or health readings to analytics / crash logs.
- Keep permission and data controls understandable.
- Do not add a sign-in gate to local app use.

---

## 13. Implementation guidance for this repository

### Preserve current architecture

Keep the existing layers and responsibilities:

- `data/model`: domain / persistence models.
- `data/local`: Room database, DAOs and converters.
- `data/repository`: repository operations.
- `data/analysis`: context, disruption and time-lagged analysis.
- `data/health`: Health Connect integration.
- `data/preferences`: local user preferences.
- `data/wear`: Wear OS integration.
- `ui/screens`: screen composables and ViewModels.
- `ui/components`: reusable visual components.
- `ui/theme`: design tokens and Material 3 theme.

Do not move business logic into composables. UI should render state and send user actions to ViewModels.

### Suggested design-system structure

A practical next step is to consolidate tokens and components without reorganizing the entire project:

```text
ui/
  components/
    MomentumTopBar.kt
    MomentumBottomBar.kt
    ProgressRing.kt
    MetricCard.kt
    InsightCard.kt
    EmptyState.kt
    ...
  screens/
    TodayScreen.kt
    HabitsScreen.kt
    HabitDetailScreen.kt
    InsightsScreen.kt
    FocusScreen.kt
    ChainScreen.kt
    ProfileScreen.kt
    SettingsScreen.kt
    OnboardingScreen.kt
  theme/
    Color.kt
    Theme.kt
    Type.kt
    Shape.kt
    Spacing.kt
```

This is a suggested organization, not a requirement to rename or relocate every existing file. Reuse `HabitRow`, `HabitDetailSheet`, `DailyMetricsForm`, `MoodGauge`, `FlameBadge`, `TrendsView` and `ReminderTimeSelector` where their behavior is already correct.

### Navigation implementation

1. Add the Today, Insights and Chain destinations to the app's navigation layer.
2. Keep the four primary destinations: Today, Habits, Insights, Focus.
3. Make Chain reachable from progress / habit history.
4. Make Profile / Settings reachable from the top bar.
5. Preserve ViewModel state and existing snackbar behavior.
6. Add back-stack and state restoration tests.

### Theme implementation

- Replace scattered literal colors with semantic tokens.
- Update both light and dark Material 3 color schemes.
- Remove or deprecate ambiguous legacy aliases only after checking every reference.
- Do not change the default theme behavior without reviewing the desired System / Light / Dark preference flow.
- Add Compose previews for both themes.

### Data integration

- All metric and chart values must originate from actual app state.
- Keep sample data confined to previews / tests.
- Preserve `isConfirmed`, source type, detected values and exact timestamps for Health Connect-derived logs.
- Do not simplify calendar states into a Boolean.
- Do not change mood rating scale or database schema as part of a visual-only pass.

---

## 14. Screen acceptance criteria

### Global
- [ ] Dark and light themes are complete.
- [ ] System theme selection works.
- [ ] Navigation has four primary destinations.
- [ ] Profile / settings are reachable without a primary tab.
- [ ] All primary controls meet 48dp target guidance.
- [ ] Text scales without clipping.
- [ ] No screen depends on network access.

### Today
- [ ] Greeting and date are localized.
- [ ] Daily progress uses real habit state.
- [ ] Mood / energy controls reflect the actual data model.
- [ ] Check-in can be skipped and edited.
- [ ] Health Connect pending values require confirmation.
- [ ] Insights are shown only when data is sufficient.
- [ ] Empty, loading and all-complete states are designed.

### Habits
- [ ] Users can create and edit custom habits.
- [ ] Presets remain suggestions, not mandatory choices.
- [ ] Manual and Health Connect sources are distinguishable.
- [ ] Habit detail shows real history and status.
- [ ] Archive / delete actions are explicit and safe.

### Insights
- [ ] T0, T-1, T-2 and time-of-day controls are represented.
- [ ] Charts use real data and labeled axes.
- [ ] Sample count is shown when available.
- [ ] Insufficient-data states do not show fake findings.
- [ ] Correlation language does not overclaim causation.

### Focus
- [ ] Timer states are clear and recover correctly.
- [ ] Presets remain connected to existing timer logic.
- [ ] Brain Dump preserves text according to actual persistence behavior.
- [ ] Completion state is calm and non-blocking.

### Chain / History
- [ ] DONE, GRACE, PAUSED, MISS and FUTURE remain distinct.
- [ ] Month navigation works.
- [ ] Metrics are scoped and labeled.
- [ ] Calendar is accessible with TalkBack.

### Settings
- [ ] System / Light / Dark appearance options work.
- [ ] Notification settings reflect actual permission state.
- [ ] Health Connect permission state is understandable.
- [ ] Privacy copy matches actual storage and data behavior.

---

## 15. Recommended implementation sequence

### Phase 1 — Foundation and app shell

- Consolidate semantic color, typography, shape and spacing tokens.
- Implement the app shell and four-destination navigation.
- Add light / dark previews.
- Establish reusable top bar, bottom bar, buttons, chips and cards.

### Phase 2 — Daily experience

- Redesign Today.
- Refine mood / energy check-in without changing its underlying scale.
- Refine habit rows and passive confirmation states.
- Add progress and empty-state treatments.

### Phase 3 — Habit management and history

- Refine Habits and habit creation.
- Refine Habit Detail.
- Refine Chain calendar and all five calendar states.

### Phase 4 — Behavioral intelligence

- Redesign Insights around the existing analysis engine.
- Add clear lag-window explanations.
- Implement evidence / sample-size states.
- Validate every insight against actual repository data.

### Phase 5 — Focus and settings

- Refine Focus timer and Brain Dump.
- Refine Profile and Settings.
- Complete onboarding visual treatment.

### Phase 6 — Quality and release readiness

- Test compact and large Android devices.
- Test TalkBack, font scaling and reduced motion.
- Test offline operation.
- Test Health Connect permission denial and confirmation.
- Verify no placeholder data appears in production.
- Run UI, repository and persistence regression tests.

---

## 16. Design review checklist

Before accepting a screen, ask:

1. Is the primary action obvious within two seconds?
2. Can the screen be understood without reading every label?
3. Is the most important information visually dominant?
4. Does the screen avoid shame, pressure and false urgency?
5. Is every displayed number backed by real data?
6. Does the UI explain uncertainty where necessary?
7. Does the screen work in light mode and dark mode?
8. Can a user with larger text or TalkBack complete the task?
9. Does it preserve existing product behavior?
10. Does the visual design feel like Momentum rather than a generic habit tracker?

---

## 17. Final design direction

**Adopt: Organic Intelligence + Adaptive Theme + Complete App Coverage.**

The reference image is a visual north star, not a pixel-perfect template. Keep its dark olive atmosphere, lime focus color, botanical warmth and rounded modular cards. Improve its information hierarchy, accessibility, evidence transparency and Android-native behavior.

Momentum should feel like a quiet place to understand your patterns: visually distinctive, emotionally generous and technically honest.

**Small steps. Better understanding. Lasting momentum.**
