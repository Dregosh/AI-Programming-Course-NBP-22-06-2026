# Design Guidelines — Narodowy Bank Polski (NBP)

Design system extracted from [https://nbp.pl/](https://nbp.pl/) on **2026-06-24**.
Use these tokens and rules when building UIs that should stay visually consistent with the NBP brand.

> All user-facing text in the course app is in **Polish** — these guidelines describe the visual layer only.

---

## 1. Assets

| Asset | Path | Notes |
|---|---|---|
| Homepage screenshot | [`../assets/homepage.png`](../assets/homepage.png) | Reference for layout & spacing |
| Logo (wordmark) | [`../assets/logo.svg`](../assets/logo.svg) | NBP eagle + wordmark, 205×64 viewBox |
| Favicon | [`../assets/favicon.ico`](../assets/favicon.ico) | Browser tab icon |
| Design tokens | [`../assets/design-tokens.json`](../assets/design-tokens.json) | Machine-readable tokens |

---

## 2. Colors

### Brand
| Token | Hex | Usage |
|---|---|---|
| `brand.primary` | `#152E52` | NBP navy — headers, headings, primary surfaces, logo |
| `brand.accent` | `#BDAD7D` | Sand/gold — accent buttons, badges, highlights |
| `brand.link` | `#4A74B0` | Interactive blue — links, primary buttons |
| `brand.error` | `#C0392B` | Error / validation states |
| `brand.success` | `#2E7D32` | Success states |

### Backgrounds
| Token | Hex | Usage |
|---|---|---|
| `background.default` | `#FFFFFF` | Page background |
| `background.light` | `#F7F7F7` | Section / card backgrounds |
| `background.muted` | `#C4C4C4` | Disabled / placeholder surfaces |
| `background.dark` | `#152E52` | Header, footer, dark sections |
| `background.overlay` | `rgba(21,46,82,0.6)` | Modal / image overlays |

### Text
| Token | Hex | Usage |
|---|---|---|
| `text.primary` | `#464646` | Body copy |
| `text.heading` | `#152E52` | Headings on light backgrounds |
| `text.secondary` | `#323232` | Emphasised body text |
| `text.muted` | `#717171` | Captions, metadata |
| `text.link` | `#4A74B0` | Links |
| `text.onDark` | `#FFFFFF` | Text on navy / dark surfaces |

### Borders
| Token | Hex | Usage |
|---|---|---|
| `border.default` | `#BFCEDD` | Input borders, dividers (light blue) |
| `border.muted` | `#C4C4C4` | Neutral dividers |

---

## 3. Typography

Two custom typefaces are loaded as local `@font-face` (`font-display: swap`):

- **Headings — `Brygada 1918`** (serif). Weights 400/500/600/700 + italics. Falls back to Georgia / serif.
- **Body — `Libre Franklin`** (sans-serif). Full weight range 100–900 + italics. Falls back to system / Arial.

```css
--font-heading: "Brygada 1918", Georgia, "Times New Roman", serif;
--font-body: "Libre Franklin", -apple-system, Arial, "Noto Sans", sans-serif;
```

### Weight scale
| Name | Value |
|---|---|
| regular | 400 |
| medium | 500 (default for headings & buttons) |
| semibold | 600 (top-level nav) |
| bold | 700 |

### Size scale
| Token | Size | Usage |
|---|---|---|
| `sm` | 13px | Buttons, labels, captions |
| `base` | 15.5px | Body text (line-height ~24px / 1.55) |
| `md` | 16px | Larger body |
| `lg` | 19.6px | Top-level nav, subheadings |
| `xl` | 24px | Section subtitles |
| `2xl` | 33px | H1 / H2 (line-height ~44px / 1.33) |

Headings (`h1`, `h2`) use **Brygada 1918, weight 500**, navy `#152E52` on light or white on dark.

---

## 4. Spacing

Base unit **4px**, used on an 8px rhythm.

| Token | Value | | Token | Value |
|---|---|---|---|---|
| 1 | 4px | | 5 | 20px |
| 2 | 8px | | 6 | 24px |
| 3 | 12px | | 7 | 28px |
| 4 | 16px | | 8 | 32px |

---

## 5. Border Radius

| Token | Value | Usage |
|---|---|---|
| `none` | 0px | Flat sections, banners |
| `xs` | 2px | Subtle controls |
| `sm` | 4px | Buttons, badges |
| `md` | 6px | Inputs, search fields |
| `full` | 999px | Pills |
| `circle` | 50% | Avatars, icon/date badges |

NBP uses **small radii** — the brand reads institutional and restrained; avoid large rounded corners.

---

## 6. Components

### Header
Navy `#152E52` bar with the white NBP wordmark, search field, and primary navigation. Full-bleed, no radius.

### Navigation
- Top-level items: `#152E52`, **uppercase**, semibold (600), ~19.6px.
- Sub-items / dropdown links: `#152E52`, regular (400), 15.5px, sentence case.

### Buttons
| Variant | Background | Text | Radius | Padding | Notes |
|---|---|---|---|---|---|
| Primary | `#4A74B0` | `#FFFFFF` | 4px | 6px 12px | Uppercase, weight 500, 1px border same as bg (e.g. "WIĘCEJ") |
| Accent | `#BDAD7D` | `#152E52` | 4px | 12px 27px | Uppercase, weight 500 (e.g. "MENU") |

### Inputs / Search
White background, `1px solid #BFCEDD` border, **6px** radius, ~10px padding, text `#464646`.

### Badges
Circular (`50%`) sand `#BDAD7D` badges used for dates/counts (e.g. calendar day markers).

---

## 7. Logo Usage

- Use [`assets/logo.svg`](../assets/logo.svg) — the NBP eagle mark + wordmark (205×64).
- Primary placement: top-left of the header on the navy `#152E52` bar.
- Maintain clear space around the mark; do not recolor or distort it.
- On dark/navy backgrounds use the white wordmark variant; on light backgrounds the navy variant.
- Link the logo to the homepage.

---

## 8. Visual Style Summary

NBP's identity is **institutional, authoritative, and trustworthy** — the visual language of a national central bank. A deep navy `#152E52` anchors the palette, paired with a restrained sand/gold `#BDAD7D` accent that signals heritage and value. The serif `Brygada 1918` headings convey tradition and gravitas, while `Libre Franklin` keeps body text clean and highly legible. Layouts are spacious and grid-aligned with small border radii and generous whitespace, projecting order and stability over flair.
