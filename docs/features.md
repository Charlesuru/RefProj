# Features

What the app does (step 5, ticket REF-5): a small personal task tracker. The app flow is secondary —
it exists to show the architecture end to end.

## App flow
```
start ──► onboarding done? ──no──► Onboarding (3 pages; the last asks for analytics consent)
              │yes
              ▼
         session? ──no──► Login ──success──┐
              │yes                         ▼
              └──────────────────────► Tab shell: Home · Tasks · Profile
                                         Profile ► Log out ──► Login
```
| Piece | Where |
|---|---|
| `AppViewModel`: picks `OnboardingRoute` / `LoginRoute` / `MainRoute` from onboarding + `AuthState`. Screens never navigate after login, logout or onboarding — the state changes and the root follows | `app:shared` |
| `MainShell` + `MainTabsState`: tab bar, one back stack per tab; back returns to Home, then leaves the app; re-selecting a tab pops it to its root | `app:shared/navigation` |
| Screen views: the route type (`home`, `tasks`, …) is logged for every top screen, never arguments | `app:shared/navigation/ScreenNames.kt` |
| Features | `feature:onboarding`, `feature:login`, `feature:home`, `feature:tasks`, `feature:profile` |

## Session and authentication
| Piece | Where |
|---|---|
| `User`, `AuthState`, `AuthRepository`, `LoginUseCase` (local validation), `LogoutUseCase` | `domain/auth` |
| `AuthRepositoryImpl`: session in `SecureStore` (token **and** profile), restored on first collection | `data/auth` |
| `FakeAuthApi`: the backend until a real one exists; 800 ms latency | `data/auth` |

Test account of the fake backend (public demo values, not a secret — no real backend accepts them):

| E-mail | Password | Result |
|---|---|---|
| `test@example.com` | `Password1` | Signed in as "Test User" |
| `offline@example.com` | anything | Network error (to see the error state) |
| anything else | anything | Wrong e-mail or password |

With the [debug menu](debug-menu.md) enabled (never in a PROD release build) the login form starts filled in with
the test account, and *Sign in as test user* jumps from any screen straight to the tab shell. The account reaches
the app through `TestAccountProvider` (domain), implemented next to the fake in `data`; `feature:login` never sees
the values.

Replacing the fake: implement `AuthApi` with Ktor (`safeApiCall` in `core:network`) and bind it in `dataModule`,
per environment if needed. Nothing above the data layer changes.

While signed in, the pseudonymous `User.id` is set as the Crashlytics and Analytics user ID; it is cleared on logout.
Display name and e-mail are personal data and are never logged or sent to analytics.

## Analytics consent (GDPR)
Analytics is **opt-in**: off until the user turns it on. See [observability.md](observability.md#when-data-is-collected).
| Piece | Where |
|---|---|
| `ConsentRepository`, `ObserveAnalyticsConsentUseCase`, `SetAnalyticsConsentUseCase` | `domain/consent` |
| `ConsentRepositoryImpl`: choice in `KeyValueStore`, applied to analytics at startup and on change | `data/consent` |
| `RestrictedAnalyticsTracker`: never collects in debug builds | `core:observability` |

## Tasks
| Piece | Where |
|---|---|
| `Task`, `TaskSummary`, `TaskRepository`, add (trim, 1–200 characters) / complete / delete use cases | `domain/tasks` |
| `TaskRepositoryImpl` over Room (`TaskDao`, `RefProjDatabase`); open tasks first, newest first | `data/tasks`, `data/database` |

Tasks are stored on the device only. `LogoutUseCase` deletes them before signing out, so the next person on a shared
device never sees them. Database decision and migration rules: [ADR 0009](adr/0009-local-database-room.md).
