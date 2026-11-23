# Momentum — Design System

**Version:** 2.1 (redesign refinement)  
**Status:** Design source of truth for `./redesign`; implementation guidance for the Android app  
**Base specification:** `public/assets/aistudio/DESIGN_30be52e8.md` (DESIGN v2.0, “Organic Intelligence”)  
**Platform:** Android · Kotlin · Jetpack Compose · Material 3  
**Product direction:** Organic Intelligence  
**Design principle:** *Understand yourself. Move forward.*  
**Product promise:** Small steps. Better understanding. Lasting momentum.

> This document describes the visual and interaction system used by the 14 HTML/CSS mockups in
> `./redesign`. It is a design artifact, not a claim that production Kotlin has been changed.
> All sample values in the mockups are illustrative; production screens must read from
> ViewModels, repositories and analysis engines.

---

## 1. Vision and experience principles

Momentum is a private, offline-first behavioral companion. It helps people notice what matters
today, act on a small number of meaningful habits, and understand patterns as personal data
accumulates. The interface is calm, premium, warm and quietly intelligent — dark olive surfaces,
luminous lime signals, botanical warmth, compact editorial type. It must never become a neon
gaming interface or a generic wellness template.

| Principle | Meaning in the UI |
|---|---|
| Progress without pressure | No shame language, punitive streak loss or alarmist red states. A lighter day still counts. |
| Clarity before decoration | Every element supports an action, status or insight. Decorative imagery never carries function. |
| Evidence before confidence | No invented analytics. Show matched-day counts, use “associated with”, expose insufficient data. |
| Privacy by default | State plainly that habit and mood data stays on the device. No sign-in gate for local use. |
| Intentional interaction | Passive Health Connect values require user confirmation before counting as completed. |
| Low-friction daily use | The core check-in is achievable in seconds. |
| Accessible calm | Strong contrast, readable type, 48dp targets, TalkBack labels, reduced motion. |

---

## 2. Scope, sources and non-goals

### Source of truth order

1. `PRD.md` — product scope, data models, minimum-observation rules and non-goals.
2. `public/assets/aistudio/DESIGN_30be52e8.md` — original v2.0 product design specification.
3. This document — refined tokens, typography, alignment and mockup decisions.
4. `README.md` in this folder — inventory and change summary.
5. `assets/design.css` — executable token and component definitions for the mockups.

### Preserved capabilities

Live Kotlin screens, Room persistence, repositories/DAOs, `data/analysis`, Health Connect,
manual and passive habits, unconfirmed entries, mood logging, calendar states (`DONE`, `GRACE`,
`PAUSED`, `MISS`, `FUTURE`), focus timer, Brain Dump, insights with T-0/T-1/T-2/time-of-day lags,
onboarding and settings all remain authoritative. See the source spec §2 for the full inventory.

### Non-goals

This redesign does **not** authorize replacing local data with mocks, adding accounts/cloud sync,
sending health or mood data anywhere, presenting correlation as causation, removing Health Connect
confirmation, adding streak-shaming or social leaderboards, or rebuilding the app in another
framework.

---

## 3. Brand expression

- **Name:** Momentum, consistently. No alternate product names.
- **Mark:** simple leaf / forward-motion glyph.
- **Wordmark:** “Momentum” in a clean medium-to-semibold sans-serif.
- **Personality:** calm, observant, encouraging, intelligent.
- **Density:** medium-low; breathable layout with meaningful information.
- **Motion:** soft, purposeful, short; never continuous decoration.

**Preferred microcopy:** “A pattern worth noticing”, “You’re building consistency”, “Your energy
was higher on days you moved”, “Not enough data yet”, “Confirm detected activity”, “A lighter day
still counts”, “Start where you are”.

**Avoid:** “You failed”, “You broke your streak”, “You are lazy”, “Guaranteed improvement”, or
any claim that a habit *causes* a mood change when the analysis only supports association.

---

## 4. Design tokens

### 4.1 Color — dark theme (default)

| Token | CSS variable | Hex | Use |
|---|---|---:|---|
| Background | `--bg` | `#0B0E08` | App root |
| Surface | `--surface` | `#11160D` | Screen surface, bottom nav |
| Container | `--card` | `#191F12` | Cards, grouped panels, ring interior |
| Elevated | `--elevated` | `#202817` | Bottom sheets, dialogs |
| Muted surface | `--muted-surface` | `#262D1B` | Inset rows, inactive steps, empty calendar cells |
| Primary | `--primary` | `#D7F75B` | Main action, active state, progress, key data |
| Primary pressed | `--primary-pressed` | `#B8D83D` | Pressed / selected interaction |
| Primary container | `--primary-container` | `#303B16` | Selected chip, completed habit, lime tint |
| On primary | `--on-primary` | `#18200A` | Text/icon on lime |
| Text primary | `--text` | `#F2F3E9` | Headings, key values |
| Text secondary | `--text-2` | `#B4B9A4` | Body, supporting copy |
| Text muted | `--text-3` | `#858D76` | Metadata, inactive nav, disabled text |
| Outline | `--outline` | `#343C27` | 1dp card borders, dividers, unchecked controls |
| Success | `--success` | `#A9D95B` | Confirmed positive state |
| Warning | `--warning` | `#E9BD68` | Pending / caution state |
| Info | `--info` | `#91B8D0` | Connected / informational source state |
| Error | `--error` | `#E28D80` | Destructive errors only |

Category tints used in mockups: sleep `#1D352F`, move `#3A2F1B`, phone `#3A221F`, water
`#1B2B3A`.

### 4.2 Color — light theme (implementation target; not in mockups)

| Token | Hex | Token | Hex |
|---|---:|---|---:|
| Background | `#F4F5EA` | Primary | `#526B16` |
| Surface | `#FAFBEF` | Primary container | `#E4F0B4` |
| Container | `#FFFFFF` | On primary | `#FFFFFF` |
| Elevated | `#FFFFFF` | Text primary | `#1C2114` |
| Muted surface | `#E9EDDA` | Text secondary | `#59604A` |
| Outline | `#D9DEC9` | Text muted | `#777D69` |

Dark is the primary art direction, but light mode must be complete and intentional, never a
fallback. Default appearance preference is **System**.

### 4.3 Color rules

- Lime is a **signal**, not a general background. Most surfaces stay olive/charcoal.
- Use primary for the current action, selected navigation, progress and high-value data.
- Amber is caution/pending; blue is informational; coral is destructive only.
- A missed day is **not** an error and must never be red.
- Never encode meaning by color alone: pair with icon, label, shape or text.
- Charts need accessible contrast plus labels/legend and a textual summary.

### 4.4 Typography

Font stack: `"Inter", "Segoe UI Variable Text", "Segoe UI", system-ui, -apple-system, Roboto,
Arial, sans-serif`. In the mockups, Inter is preferred but Segoe UI Variable is the expected
fallback on Windows.

| Role | CSS | Size | Weight | Line height | Tracking | Use |
|---|---|---:|---:|---:|---:|---|
| Display | `.display` | 31px | 600 | 1.16 | −0.5px | Onboarding hero headline |
| Wordmark | `.wordmark` | 22px | 600 | 1.2 | −0.2px | Brand name |
| Screen title | `h1` | 26px | 600 | 1.2 | −0.25px | Page headings |
| Section title | `h2` | 18px | 600 | 1.3 | −0.15px | Card/section headings |
| Card title | `h3` | 14.5px | 600 | 1.35 | −0.05px | Habit, insight and row titles |
| Body | `body` | 14px | 400 | 1.5 | 0 | Default text |
| Paragraph | `p` | 13px | 400 | 1.55 | 0 | Supporting descriptions |
| Small | `.small` | 12.5px | 400 | 1.45 | 0 | Dense supporting text |
| Tiny | `.tiny` | 11px | 400 | 1.45 | 0 | Metadata, legends, captions |
| Eyebrow | `.eyebrow` | 10px | 600 | 1.2 | 1.15px | Tracked uppercase labels |
| Button | `.btn` | 15px | 600 | — | 0.1px | Primary/secondary actions |
| Chip | `.chip` | 12px | 500 | — | 0 | Filters; active chips use 600 |
| Badge | `.badge` | 10px | 600 | — | 0.1px | Short status labels |
| Metric | `.numeral` + size | 16–42px | 600 | — | −0.5px | Timer, progress, statistics |
| Navigation | `.nav-item` | 10px | 600 | — | 0.1px | Bottom navigation labels |

**Typography rules**

- Default to 400. Use 500 for quiet chips, 600 for headings/actions, and 700 only for checkmarks.
  Avoid 700–900 for whole headings and buttons; it reads as shouting.
- Use tabular numerals (`.mono`, `.numeral`) for timers, percentages and metrics.
- Keep lowercase body text; reserve all-caps for short eyebrow labels only.
- Support Android font scaling; never clip text in fixed-height containers.
- Do not use monospace for all text.

### 4.5 Spacing

4dp base grid. CSS variables: `--s1` 4, `--s2` 8, `--s3` 12, `--s4` 16, `--s5` 20, `--s6` 24,
`--s8` 32, `--s10` 40.

- Screen gutters: 20px (16px minimum on compact devices).
- Standard section gap: 24px (`.section`).
- Card padding: 16px standard, 20px hero (`.card`, `.card.lg`), 12px compact (`.card.compact`).
- Habit rows: 12px 14px padding, 72px minimum height.
- Sheet padding: 10px 22px 24px; grabber 36×4px.
- Bottom navigation: 78px height, 10px top padding, respects gesture insets.

### 4.6 Shape

| Token | Value | Use |
|---|---:|---|
| `--r-xs` | 8px | Calendar cells, small badges |
| `--r-sm` | 12px | Icon tiles, fields |
| `--r-md` | 18px | Standard cards, habit rows, chips in selection grids |
| `--r-lg` | 24px | Hero cards, large panels |
| `--r-xl` | 30px | Bottom sheets, onboarding panels |
| `--r-pill` | 999px | Buttons, filter chips, progress tracks, nav pill |

Use at most two unrelated radii per screen. Prefer tonal surface separation over shadows; use a
1px `--outline` border for cards that need separation and stronger elevation only for sheets and
dialogs.

### 4.7 Motion

Motion explains change; it never decorates inactivity.

| Interaction | Duration / guidance |
|---|---|
| Habit completion | 160–220ms scale/check transition |
| Progress ring | 350–550ms eased sweep |
| Tab change | Minimal fade / content transition |
| Sheet opening | Native Material 3 motion |
| Insight reveal | Short fade + vertical movement |
| Calendar month change | Short crossfade |
| Timer | No continuous distracting animation |

Respect Android animator duration scale and reduced-motion settings. Use a light haptic on habit
completion; no haptic on every slider tick.

---

## 5. Layout and alignment system

### 5.1 Screen frame

Mockups are 390 × 844 portrait inside a device frame. The frame contains a 40px status bar, a
flexible scrollable `.screen`, and an optional 78px `.bottom-nav`. Secondary screens (Chain,
Settings) and onboarding omit the primary tab bar; sheets overlay the current screen with a
scrim.

### 5.2 Shared primitives in `assets/design.css`

| Class | Purpose |
|---|---|
| `.row`, `.col`, `.stack` | Flex rows, columns and 12px-gapped vertical stacks |
| `.between`, `.center`, `.grow` | Justify, align and flex-grow helpers |
| `.page-head`, `.head-text`, `.back`, `.section-head` | Consistent header rows and back affordance |
| `.section`, `.mt2`–`.mt8` | Vertical rhythm (24px sections; 8–32px micro spacing) |
| `.card`, `.card.lg`, `.card.compact`, `.card.stat`, `.card.flat`, `.card.lime-tint` | Surface variants |
| `.chip`, `.chip.active`, `.chip.select` | Filters and selectable preset cards |
| `.btn`, `.btn.secondary`, `.btn.ghost`, `.btn.sm`, `.play-btn` | Actions |
| `.mx-auto`, `.text-center` | Block centering and inline-text centering |
| `.onboarding`, `.chip-row`, `.scrim-bg` | Onboarding flow, chip rows, dimmed sheet backgrounds |
| `.ring`, `.track`, `.badge`, `.tile`, `.check`, `.switch`, `.field` | Data and control components |

> **Alignment rule learned during refinement:** `.center` only sets `justify-content` and
> `align-items`; it has no effect unless the element is a flex container. `text-align:center`
> centers inline content but not block children such as `.ring`, `.play-btn` or a fixed-width
> `.tile`. Use `.mx-auto` for block centering and `.text-center` for captions. Verified result:
> timer ring and play button are 0px off-center.

### 5.3 Hierarchy pattern

1. Page head: title + eyebrow on the left, single contextual action on the right.
2. Primary hero or focus card.
3. Supporting metric cards.
4. Contextual insight / status card.
5. Section head + list of rows.
6. Optional footer note.

Use scrollable content rather than compressing cards on small devices. Keep primary actions
reachable one-handed.

---

## 6. Navigation architecture

Four primary destinations:

1. **Today** — daily overview and check-in.
2. **Habits** — habit management and detail.
3. **Insights** — behavioral patterns and analytics.
4. **Focus** — timer and sessions.

Secondary destinations:

- **Chain / History** — reached from Today progress or a habit detail; not a primary tab.
- **Profile / Settings** — reached from the Today avatar; not a primary tab.
- **Onboarding** — first-run flow, four steps.

Selected tab uses lime icon plus compact lime-container pill; unselected tabs use muted icon and
label. Never use icon-only navigation without an accessible label. Preserve tab state and system
back behavior.

---

## 7. Component library

| Component | CSS / mockup class | Compose counterpart | States |
|---|---|---|---|
| Top bar / page head | `.page-head`, `.back` | `MomentumTopBar` | Default, scrolled, with back |
| Bottom bar | `.bottom-nav`, `.nav-item` | `MomentumBottomBar` | Selected, unselected |
| Progress ring | `.ring`, `.numeral` | `ProgressRing` | Empty, partial, complete |
| Metric card | `.card`, `.card.compact` | `MetricCard` | Logged, pending, unavailable |
| Habit row | `.habit`, `.check`, `.tile` | `HabitRow` | Active, done, pending confirmation |
| Insight card | `.card.lime-tint`, `.eyebrow.lime` | `InsightCard` | Sufficient data, insufficient data |
| Lag selector | `.chip` | `LagSelector` | T-0, T-1, T-2, by time |
| Trend card | `.card`, inline SVG chart | `TrendCard` | Loading, data, empty |
| Calendar cell | `.cal-cell` | `CalendarCell` | Done, grace, paused, miss, future |
| Filter chip | `.chip` | `FilterChip` | Selected, unselected |
| Buttons | `.btn`, `.btn.secondary`, `.btn.ghost` | `PrimaryButton`, `SecondaryButton` | Enabled, pressed, disabled, loading |
| Bottom sheet | `.sheet-wrap`, `.sheet`, `.grabber` | Existing sheet / dialog patterns | Open, closed, scrollable |
| Switch | `.switch` | Material 3 switch | On, off |
| Empty state | `.card.flat`, `.txt3` | `EmptyState` | Screen-specific |

Component rules: components receive state and callbacks, never repository logic; use Material 3
foundations with Momentum tokens; provide semantic descriptions for meaningful icons; keep a small
reusable set rather than many nearly identical cards.

---

## 8. Screen specifications

### 8.1 Onboarding (01–04)

Four steps: Welcome → Pick habits → First check-in → Reminder. Progress uses four slim bars.
Welcome shows the leaf mark, wordmark, headline “Understand yourself. Move forward.”, privacy
note and a single lime CTA. Pick habits offers six presets plus Custom habit; selection is a lime
outline/check, not a bright panel. First check-in makes mood/energy optional and allows saving
without blocking entry. Reminder offers a time chip and an explicit “Not now”. Preserve entered
values when moving backward; never use fake testimonials or guaranteed-outcome claims.

### 8.2 Today (05)

Hierarchy: localized greeting + date + avatar → progress hero (ring, count, “Today”) → mood and
energy cards → one contextual insight card → today’s habit stack. Pending Health Connect entries
show source (“Awaiting confirmation”) and a separate confirm control. Habit rows separate detail
tap from completion. Empty, no-log, all-complete and offline states remain useful. Insights only
appear when the engine passes its threshold.

### 8.3 Mood & energy sheet (06)

A dimmed Today screen behind a bottom sheet. Title “How are you feeling?”, explicit “Not logged
yet” badge, two distinct sliders/tracks for mood and energy, clear 1/5/10 anchors, Save and Skip.
Both ratings stay local. Do not visually imply independent energy tracking unless the data model
supports it.

### 8.4 Habits (07) and creation sheet (09)

Habits header includes count, add action and optional category filters. Rows show category icon,
name, source/streak metadata and completion state. Creation sheet covers name, icon, color accent,
tracking method (manual/Health) and reminder. Presets are suggestions; custom creation is
first-class.

### 8.5 Habit detail sheet (08)

Shows habit identity, cadence/reminder, three compact stats (streak, consistency, days done), a
14-day history using filled/hollow dots for done/grace, a related-pattern note with matched-day
count and association wording, plus Archive and explicit Delete actions. Long content scrolls.

### 8.6 Insights (10)

Title + promise, lag chips with a short T-1 explanation, mood trend with labeled axes and sample
size, a relationship card (“Associated with +0.8 mood points the next day”, “6 matched days”,
“Association, not proof”), context insight and an explicit data-sufficiency card. Minimum 5
dual-observation days applies; never show placeholder percentages or fake charts.

### 8.7 Chain / History (11)

Month calendar with `DONE` (filled lime + check), `GRACE` (lime outline), `MISS` (neutral),
`FUTURE` (low contrast) and a paused marker in the legend. Monthly summary metrics are scoped to
the selected habit. Provide a screen-reader summary and locale-aware week start.

### 8.8 Focus (12) and Brain Dump (13)

Large timer ring with tabular remaining time, centered 64px play/pause control, preset chips,
Brain Dump entry and today’s focus total. The ring and button must be horizontally centered in the
hero card. Timer states: idle, running, paused, complete, restored after backgrounding. Brain Dump
is a private quick-capture sheet; do not imply permanent persistence unless implemented.

### 8.9 Settings (14)

Profile summary, Appearance (System/Light/Dark), timer alerts, Health Connect and reminder state,
Data & privacy copy, About and feedback. Privacy copy must match actual local storage behavior.

---

## 9. Content and evidence rules

- All numbers in mockups are illustrative design content; production values come from ViewModels
  and analysis engines.
- Show matched-day counts and selected lag window on relationship cards.
- Use “associated with”, “tends to” or “was observed alongside”, never “causes”.
- Expose insufficient-data states instead of inventing findings.
- Health Connect: request only needed permissions, explain why, show the source, require
  confirmation, never treat denial as user failure and never show missing data as zero.
- Calendar states remain five distinct values; do not collapse them into a Boolean.
- Privacy copy: “Your habit and mood data is stored locally on this device.” Do not claim
  encryption, backup or sync unless implemented and verified.
- Avoid sensitive mood/health values in notification text by default.

---

## 10. Accessibility and inclusivity

- Meet WCAG AA contrast; verify muted text on olive surfaces in both themes.
- Minimum 48dp interactive targets; primary buttons are 52px high.
- TalkBack labels for meaningful icons, charts, calendar cells and month navigation.
- Decorative emoji must not be the only carrier of meaning; pair with text.
- Never use color alone for status; pair with icon, outline, check or label.
- Support font scaling without clipping; test large-text settings.
- Respect reduced motion and system animator duration.
- Test compact devices, both themes, offline operation and permission-denied paths.

---

## 11. Implementation mapping for this repository

### Keep the architecture

Preserve `data/model`, `data/local`, `data/repository`, `data/analysis`, `data/health`,
`data/preferences`, `data/wear`, `ui/screens`, `ui/components` and `ui/theme`. Do not move business
logic into composables. UI renders state and sends actions to ViewModels.

### Suggested design-system files

```text
ui/theme/
  Color.kt      # semantic tokens from §4
  Theme.kt      # Material 3 dark/light schemes
  Type.kt       # refined type scale from §4.4
  Shape.kt      # radii from §4.6
  Spacing.kt    # 4dp grid from §4.5
ui/components/
  MomentumTopBar.kt, MomentumBottomBar.kt, ProgressRing.kt, MetricCard.kt,
  InsightCard.kt, LagSelector.kt, TrendCard.kt, CalendarCell.kt, EmptyState.kt
```

Reuse existing `HabitRow`, `HabitDetailSheet`, `DailyMetricsForm`, `MoodGauge`, `FlameBadge`,
`TrendsView` and `ReminderTimeSelector` where behavior is already correct.

### Migration sequence

1. Consolidate semantic tokens; replace scattered literal colors. Remove legacy `Color.kt`
   aliases only after checking every reference.
2. Wire Today, Insights and Chain into navigation; keep four primary destinations.
3. Establish shared top bar, bottom bar, buttons, chips and cards.
4. Refine Today, check-in, habit rows and passive confirmation states.
5. Refine Habits, creation, detail and Chain calendar.
6. Refine Insights against real analysis output and thresholds.
7. Refine Focus, Brain Dump, onboarding and settings.
8. Test accessibility, offline behavior, both themes and regression suites.

### Verification commands (from repository guidance)

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug bundleDebug
```

---

## 12. Acceptance criteria

### Global

- [ ] Dark and light themes are complete; System selection works.
- [ ] Four primary destinations; Profile/Settings and Chain reachable without extra tabs.
- [ ] All primary controls meet 48dp target guidance.
- [ ] Text scales without clipping; TalkBack traversal is logical.
- [ ] No screen depends on network access.
- [ ] No placeholder data appears in production.

### Per screen

- [ ] **Today:** localized greeting, real progress, accurate mood model, skippable check-in,
      Health Connect confirmation, gated insights, empty/all-complete states.
- [ ] **Habits:** custom creation, distinguishable sources, real detail history, safe archive/delete.
- [ ] **Insights:** lag controls, labeled charts, sample counts, honest insufficient-data states.
- [ ] **Focus:** accurate timer states, connected presets, honest Brain Dump persistence, calm completion.
- [ ] **Chain:** five distinct calendar states, working month navigation, scoped labeled metrics.
- [ ] **Settings:** appearance and permission states reflect reality; privacy copy is accurate.

### Review questions

1. Is the primary action obvious within two seconds?
2. Is the most important information visually dominant?
3. Does the screen avoid shame, pressure and false urgency?
4. Is every displayed number backed by real data?
5. Does the UI explain uncertainty where necessary?
6. Can a user with larger text or TalkBack complete the task?
7. Does it feel like Momentum rather than a generic habit tracker?

---

## 13. Mockup inventory

| # | File | Screen / state |
|---|---|---|
| 01 | `screens/01-onboarding-welcome.html` | Onboarding — Welcome |
| 02 | `screens/02-onboarding-pick-habits.html` | Onboarding — Pick habits |
| 03 | `screens/03-onboarding-first-checkin.html` | Onboarding — First check-in |
| 04 | `screens/04-onboarding-reminder.html` | Onboarding — Reminder |
| 05 | `screens/05-today.html` | Today dashboard |
| 06 | `screens/06-mood-energy-sheet.html` | Mood & energy sheet |
| 07 | `screens/07-habits.html` | Habits list |
| 08 | `screens/08-habit-detail-sheet.html` | Habit detail sheet |
| 09 | `screens/09-add-habit-sheet.html` | New habit sheet |
| 10 | `screens/10-insights.html` | Insights |
| 11 | `screens/11-chain-history.html` | Chain / history |
| 12 | `screens/12-focus.html` | Focus timer |
| 13 | `screens/13-brain-dump-sheet.html` | Brain Dump sheet |
| 14 | `screens/14-settings.html` | Profile & settings |

Open `index.html` for the gallery. Tokens and components are defined in `assets/design.css`.

---

## 14. Governance

- Treat this file, `README.md` and `assets/design.css` as a single design-system change set.
- Any token change must be reflected in the CSS variables and the tables above.
- Any new component must state its states, accessibility labels and Compose counterpart.
- Mockups are evaluated against §12; implementation is evaluated against the source spec §14.
- Revisit the system after usability testing, font-scaling checks and TalkBack audits.

**Final direction:** Organic Intelligence + Adaptive Theme + Complete App Coverage. Keep the dark
olive atmosphere, lime focus color, botanical warmth and rounded modular cards; improve
hierarchy, accessibility and evidence transparency.
