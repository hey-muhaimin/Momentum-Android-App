# Momentum — UI Redesign Mockups

Fourteen dark-theme screen and overlay mockups for the "Organic Intelligence" design system
(DESIGN v2.0), built with dependency-free HTML/CSS. **Design artifacts only — no production
Kotlin was changed.**

Open `index.html` in a browser for the gallery, or open any file in `screens/` directly.

> Tool note: the requested Stitch AI MCP server is not configured in this environment, and the
> built-in image generator failed with `Invalid credential`. Per your choice, the mockups were
> produced as code-based HTML/CSS screens using the repo's
> `public/assets/aistudio/DESIGN_30be52e8.md` spec and the actual Compose screens as the brief.

## Design system enhancements applied

| Area | Current app | Redesign direction |
|---|---|---|
| Palette | Two competing green systems (`Color.kt` legacy tokens vs. `Theme.kt` M3 scheme) | One semantic token set: olive surfaces `#0B0E08 / #11160D / #191F12`, lime signal `#D7F75B`, lime container `#303B16`, text `#F2F3E9 / #B4B9A4` |
| Accent discipline | Lime/teal/amber used interchangeably | Lime reserved for action, active state and progress; warning amber, info blue and error coral are strictly semantic |
| Typography | Mixed 700–900 weights, ad-hoc inline sizes, very tight tracking | Editorial scale: 400 body, 500 quiet chips, 600 headings/actions, 700 only for checkmarks; 26px page titles, 18px section titles, 31px onboarding display; looser line heights |
| Alignment | `.center` only set flex alignment, so block children (rings, play button, icon tile) sat left inside centered cards | Added `.mx-auto` and `.text-center` utilities; timer ring/play button and other centered blocks now measure 0px off-center |
| Shape | Mixed 8/12/16/20/32dp radii | 8 / 12 / 18 / 24 / 30 / pill scale, at most two radii per screen |
| Spacing | Ad-hoc 16/20/24dp padding | 4dp grid; 20dp screen gutters, 16–20dp card padding, 24dp section gaps |
| Navigation | Four tabs exist, but Chain / Settings / Onboarding are not reachable from `MainActivity` | Four fixed tabs (Today, Habits, Insights, Focus); Chain via habit history; Settings via avatar; onboarding as first-run flow |
| Status language | Streak flame, "Tap to confirm" and red-tinted delete | Neutral grace/paused states, explicit source labels, no red for missed days, no streak-shaming |
| Evidence honesty | Trend/chart cards render fixed sample values | Show matched-day counts, "associated with" phrasing and an explicit insufficient-data state; no invented percentages |
| Touch targets | Several 27–36dp controls | 48dp minimum for primary controls; separate detail tap from completion control |
| Accessibility | Decorative emoji/inline icons carry meaning | Icon + text labels, semantic content descriptions, readable contrast, no color-only status |

## Mockup inventory (dark theme, 390 × 844 portrait)

| # | File | Source screen / state |
|---|---|---|
| 01 | `screens/01-onboarding-welcome.html` | Onboarding step 1 — Welcome |
| 02 | `screens/02-onboarding-pick-habits.html` | Onboarding step 2 — Pick habits |
| 03 | `screens/03-onboarding-first-checkin.html` | Onboarding step 3 — First check-in |
| 04 | `screens/04-onboarding-reminder.html` | Onboarding step 4 — Reminder |
| 05 | `screens/05-today.html` | Today dashboard — progress, check-in, habits list |
| 06 | `screens/06-mood-energy-sheet.html` | Daily metrics / mood & energy check-in sheet |
| 07 | `screens/07-habits.html` | Habits list with filters |
| 08 | `screens/08-habit-detail-sheet.html` | Habit detail / edit bottom sheet |
| 09 | `screens/09-add-habit-sheet.html` | New habit creation sheet |
| 10 | `screens/10-insights.html` | Insights — lag selector, trend, relationship, evidence note |
| 11 | `screens/11-chain-history.html` | Chain / monthly calendar with all five cell states |
| 12 | `screens/12-focus.html` | Focus timer with presets and Brain Dump entry |
| 13 | `screens/13-brain-dump-sheet.html` | Brain Dump bottom sheet |
| 14 | `screens/14-settings.html` | Profile / settings — preferences, privacy, about |

Shared tokens and components live in `assets/design.css`.

All sample numbers shown in the mockups are illustrative design content only; production
screens must continue to read from the ViewModels and analysis engines (see DESIGN v2 §12).
