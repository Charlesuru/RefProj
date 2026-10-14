<!-- Title: "REF-123: <summary>". Branch: <type>/REF-123-short-description. See CLAUDE.md §3. -->

## Ticket
REF-

## What and why
<!-- What changes for users or developers, and why. Link the spec / acceptance criteria. -->

## How it was tested
<!-- Commands run, devices/simulators, screenshots for UI changes. -->
- [ ] `./gradlew spotlessCheck detekt allTests :app:android:assembleDevDebug`
- [ ] iOS built and run (`bundle exec fastlane ios build env:dev` or Xcode)

## Checklist
- [ ] Acceptance criteria of the ticket are met
- [ ] New logic has tests, each test class marked with its type (`// UNIT TEST`, …)
- [ ] Unfinished user-facing work is behind a feature flag that is **OFF in PROD**
- [ ] UI text in `strings.xml` (English + Swedish); design-system components and tokens only
- [ ] No secrets, keys or personal data in code, logs or this PR (`docs/secrets.md` updated if a secret was added)
- [ ] New dependency? Justified below (license, maintenance, size, KMP support)
- [ ] Docs / ADR updated if architecture, CI or the workflow changed

## Notes for the reviewer
<!-- Risks, follow-ups, anything you want a second pair of eyes on. -->
