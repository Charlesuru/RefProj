# Design system (`core:designsystem`)

Every screen is built from these tokens and components. Features never use raw colours, dp literals
or Material components that have a `Ref*` counterpart (`CLAUDE.md` §2).

## Tokens
| Token | Where | Use |
|---|---|---|
| Colour roles | `theme/Color.kt` → `MaterialTheme.colorScheme` | All Material 3 roles, light + dark. Brand primary `#1F5EFF` |
| Extended colours | `RefTheme.colors` | `success`, `warning` (+ `on…`, `…Container`) |
| Typography | `theme/Typography.kt` → `MaterialTheme.typography` | Material 3 type scale on the system font |
| Spacing | `RefTheme.spacing` | 4 dp grid: `xxs` 2 · `xs` 4 · `sm` 8 · `md` 12 · `lg` 16 · `xl` 24 · `xxl` 32 · `xxxl` 48 |
| Sizes | `RefTheme.sizes` | Icons, touch target (48), button height, max content width (600), illustration (96), pager dot (8) |
| Shapes | `MaterialTheme.shapes` | 4 / 8 / 12 / 16 / 28 dp corner radii |
| Elevation | `RefTheme.elevation` | `none`, `low`, `medium`, `high` |

Accessibility: `ColorContrastTest` checks that every content colour reaches WCAG AA (4.5:1) on its
background, in both schemes. A palette change that breaks contrast fails the build.

## Components (`component/`)
| Component | Notes |
|---|---|
| `RefButton` | Styles `Primary`, `Secondary`, `Tertiary`, `Destructive`; `loading` blocks double submits |
| `RefTextField` | Types `Text`, `Email`, `Password`, `Number`; `error` replaces the supporting text |
| `RefCard` | Optional `onClick` |
| `RefTopAppBar` | Back button when `onBack` is set |
| `RefListItem`, `RefSwitchListItem`, `RefCheckboxListItem`, `RefDivider` | Whole row is the touch target; a checked checkbox row is struck through |
| `RefNavigationBar` | Bottom tabs for the logged-in shell |
| `RefPagerIndicator` | Dots under a pager, announced as "Page X of Y" |
| `RefLoadingState`, `RefErrorState`, `RefEmptyState` | Full-screen states; every screen uses these |
| `RefConfirmDialog` | `destructive = true` for irreversible actions |
| `RefSurface`, `RefContentWidth` | Screen background; centred content with a max width |

Icons: `RefIcons` (vector drawables from Material Icons, Apache 2.0, in `composeResources/drawable`).
Add new icons there instead of depending on the frozen `material-icons` artifacts.

Strings used by components (`Back`, `Try again`, …) are in this module's `composeResources` (English + Swedish).

## Adding a component
1. Only when two features need it, or it encodes a design decision (spacing, states, accessibility).
2. Build it on Material 3, expose a small API, take `modifier: Modifier = Modifier` as the first optional parameter.
3. Add a `@Preview` covering its variants (light/dark previews come from the IDE).
4. Screenshot tests for components arrive in plan step 7 (Roborazzi).
