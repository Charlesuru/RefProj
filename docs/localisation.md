# Localisation

Languages: **English** (default, `values/`) and **Swedish** (`values-sv/`). The app follows the device language and
the per-app language setting (Android 13+ *App languages*, iOS *Settings → RefProj → Language*).

## Where strings live
Each module owns its strings in `src/commonMain/composeResources/values*/strings.xml` and gets its own `Res` class
(`<module package>.resources.Res`). Prefix keys with the module name (`home_title`, `designsystem_retry`) so they stay
unique in logs and reviews. Never hardcode UI text (`CLAUDE.md` §2).

```kotlin
Text(stringResource(Res.string.home_environment, environmentName)) // "Environment: %1$s"
```

## Guard rails
- `TranslationsTest` (app:shared, host test) fails when a key is missing in either language or the placeholders differ.
- Android: `localeFilters = en, sv` strips other locales pulled in by libraries; `res/xml/locales_config.xml` lists the
  languages for the system's per-app language picker.
- iOS: `CFBundleLocalizations: [en, sv]` in `iosApp/project.yml` makes iOS offer the language and pass it to the app.

## Adding a language
1. Add `values-<code>/strings.xml` to every module that has strings.
2. Add the code to `localeFilters` (`app/android/build.gradle.kts`), `locales_config.xml` and `CFBundleLocalizations`.
3. Extend `TranslationsTest` to check the new folder.

## Testing a language
- Android: `adb shell cmd locale set-app-locales com.studiodraug.refproj.dev --locales sv`
- iOS simulator: `xcrun simctl launch booted com.studiodraug.refproj.dev -AppleLanguages '(sv)'`
