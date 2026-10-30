# Design System: Andro-Assist (Spiandro)

## 1. Visual Theme & Atmosphere
The visual identity of **Andro-Assist** embodies a **Tactical Digital Health & Cyber-Shield** aesthetic. The atmosphere balances tactical precision with reassuring protective warmth — evoking a secure personal mission control rather than a restrictive surveillance monitor. 

- **Density:** 7 (Cockpit / Data-Dense Dashboard) — Glanceable metrics, compact card grids, and high-frequency status indicators.
- **Variance:** 8 (Offset Asymmetric Bento Grids) — Dynamic card weights where the primary shield metric dominates, offset by compact action tiles.
- **Motion:** 6 (Fluid Spring-Physics CSS / Micro-Choreography) — Tactile hardware-accelerated transitions that communicate defensive readiness.

---

## 2. Color Palette & Roles
The interface adheres strictly to a **Deep Obsidian & Emerald Armor** palette. Saturation is kept below 80% to eliminate cognitive fatigue during evening hours. Pure black (`#000000`) and generic neon glows are forbidden.

- **Obsidian Canvas** (`#0A0F1D`) — Root dark background canvas providing maximum contrast without OLED harshness.
- **Armor Surface** (`#111827`) — Primary card container fill with neutral Zinc-900 depth.
- **Sub-Armor Surface** (`#1E293B`) — Secondary interactive tile and header container background.
- **Cold Starlight White** (`#F8FAFC`) — Primary text headers and active values (Slate-50 depth).
- **Muted Steel** (`#94A3B8`) — Secondary labels, descriptions, and passive metadata (Slate-400 depth).
- **Shield Rim Border** (`rgba(148, 163, 184, 0.12)`) — 1px hairline container borders defining structural zones.
- **Emerald Guard (Accent)** (`#10B981`) — Single primary accent for active protection status, safe progress bars, and primary CTAs.
- **Warning Amber** (`#F59E0B`) — Threshold state when screen time reaches 80% of daily quota.
- **Lockdown Crimson (Destructive)** (`#EF4444`) — Quota exhausted, bedtime lockdown active, and emergency SOS controls.

*(Strict Rule: Zero purple/cyan gradients. Zero neon outer glow blurs. One singular active accent color per viewport state.)*

---

## 3. Typography Rules
Typography is calibrated for instantaneous legibility on small touchscreen devices under direct sunlight and low-light bedtime environments.

- **Display & Headings:** `Cabinet Grotesk` (fallback: `Outfit`, `Geist`) — Track-tight (`-0.03em`), geometric weight-driven hierarchy. Never oversized or shouting.
- **Body & Controls:** `Geist` (fallback: `Satoshi`) — Relaxed leading (`1.55`), strictly capped at 65 characters per line for readable parent notices.
- **Monospace Numbers & Telemetry:** `JetBrains Mono` (fallback: `Geist Mono`) — Mandated for all clock hours, minute counters, battery percentages, and 6-digit family pairing tokens (`PAIR-XXXXXX`).
- **Forbidden Fonts:** `Inter` is BANNED as generic AI filler. All traditional serifs (`Times New Roman`, `Georgia`, `Garamond`) are strictly BANNED in software dashboard views.

---

## 4. Component Stylings

### Buttons & Interactive Controls
- **Primary Action (Emergency / Instant Pause):** Flat solid fill (`#EF4444` or `#2563EB`). No outer box-shadow glows. Tactile `-1px` vertical translation (`translateY(1px)`) on `:active`.
- **Secondary Actions:** Dark border-outlined tiles (`#1E293B` background with `1px solid rgba(148, 163, 184, 0.16)`).
- **Minimum Tap Target:** Strictly $\ge 48 \times 48\,\text{dp}$ touch bounding boxes across all mobile breakpoints.

### Bento-Grid Cards
- **Geometry:** Clean `16px` (`1rem`) border radius. Never sharp $0\text{px}$ edges, never pill-shaped blobs.
- **Elevation:** Diffused whisper shadow tinted directly to the canvas hue (`box-shadow: 0 4px 20px -2px rgba(10, 15, 29, 0.5)`). Elevation communicates functional priority.
- **Dividers:** Hairline `1px` horizontal rules tinted to `#1E293B` instead of heavy drop dividers.

### Telemetry & Progress Gauges
- **Linear Bar:** `10px` height with `5px` corner radius. Smooth multi-stop transition from Emerald (`#10B981`) to Warning Amber (`#F59E0B`) to Crimson (`#EF4444`).
- **Remaining Counter:** Monospace bold numerals paired with an unambiguous `"Remaining"` semantic status label.

### Form Inputs & Dialogs
- **Security PIN Boxes:** Centered 4-cell numeric split inputs with high-contrast active borders (`#10B981`). Focus rings never obscure adjoining fields.
- **Error States:** Immediate inline reporting below the target input in `#EF4444` with a descriptive resolution hint.

### Loading & Empty States
- **Skeleton Shimmer:** Dimensional pulse blocks matching exact card geometries. Generic spinning circular loaders are forbidden.
- **Empty Activity State:** Composed informational card instructing parents on how usage telemetry populates upon first app unlock.

---

## 5. Layout Principles

- **Bento-Grid Asymmetry:** The primary screen time metric card occupies a double-width priority anchor, flanked by compact secondary action tiles (Pairing, Chores, Bedtime).
- **Strict Mobile-First Single-Column Collapse (< 768px):** All multi-column desktop or tablet bento grids collapse to a single clean vertical stream on mobile devices.
- **Max-Width Containment:** Root viewports are bound within a `1280px` centered container with proportional lateral padding (`clamp(16px, 4vw, 32px)`).
- **Safe Area Insets:** Strict adherence to Android system navigation bar and camera punch-hole padding (`windowInsets` compatibility).
- **Zero Horizontal Overflow:** `overflow-x: hidden` enforced at root. Horizontal scrollbars on mobile viewport are classified as critical bugs.

---

## 6. Motion & Interaction

- **Spring Physics Standard:** `stiffness: 100, damping: 20` across all modal entrances and card expands. Linear CSS transitions are forbidden.
- **Perpetual Micro-Interactions:** The active protection shield status badge features an infinite, low-frequency subtle breathing pulse (`scale: 1.0 -> 1.02` over 3.2 seconds at 15% opacity change).
- **Staggered Waterfall Reveals:** Dashboard cards mount with a calibrated `40ms` cascade delay (`transition-delay: calc(index * 40ms)`).
- **Hardware-Accelerated Only:** Animations animate exclusively via GPU-composited `transform` and `opacity`. Animating layout triggers (`width`, `height`, `top`, `margin`) is banned.
- **Reduced Motion Support:** All transitions honor `prefers-reduced-motion: reduce` by replacing kinetic translation with instant opacity toggles.

---

## 7. Anti-Patterns (Banned AI Tells)

The following patterns are **explicitly forbidden** in the Andro-Assist interface:

1. **NO Emojis Anywhere**: Use clean vector icons (Lucide/Material Symbols) exclusively.
2. **NO Inter Font**: Eliminates generic corporate AI dashboard aesthetics.
3. **NO Pure Black (`#000000`)**: Prevents harsh clipping on mobile screens; use Obsidian Slate (`#0A0F1D`).
4. **NO Neon Outer Glows**: Glowing neon dropshadows are amateur AI cliches; use calibrated ambient diffusion.
5. **NO 3-Column Equal Card Rows**: Enforce asymmetric bento visual weighting.
6. **NO Fabricated Telemetry**: Never invent fake statistics ("99.9% UPTIME", "42ms LATENCY"). Display only genuine device usage minutes and verified battery levels.
7. **NO Overlapping Content**: Text and icons must never collide or absolute-position stack over one another.
8. **NO Centered Hero Marketing Blocks**: Dashboard layouts start directly with glanceable actionable status.
9. **NO AI Copywriting Tropes**: Ban words like *"Elevate"*, *"Seamless"*, *"Unleash"*, and *"Next-Gen"*.
10. **NO Broken Image URLs**: Use vector asset drawables or local SVGs exclusively.
