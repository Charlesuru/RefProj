# Features

What the app does (step 5, ticket REF-5): a small personal task tracker. The app flow is secondary —
it exists to show the architecture end to end.

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
