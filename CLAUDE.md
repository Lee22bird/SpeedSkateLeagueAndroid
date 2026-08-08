# SSL Android App

The **SpeedSkateLeague Android app** — Kotlin, Gradle (Kotlin DSL). An SSL
client for identity/community (profiles, teams, leagues, roles, notifications,
history) that talks to the SSL backend's APIs. NOT a meet-operations app — meet
ops belong to SSM. See `../CLAUDE.md` for the SSL-vs-SSM boundary.

**Parity rule (important):** this app should become the match to **SSL iOS**
(`../Speed Skate League APP`) — same nav, same SSL APIs, same notification
behavior, same brand/UX. When building or changing a screen, mirror how iOS does
it; if a feature ships on only one platform, leave a clear note and plan the match.

## Layout
- Gradle: root `build.gradle.kts` / `settings.gradle.kts`; app module in `app/`
  (`app/build.gradle.kts`).
- Package `com.speedskateleague.android` — sources under
  `app/src/main/java/com/speedskateleague/android/` (`MainActivity.kt`,
  `SslApplication.kt`, …). Manifest at `app/src/main/AndroidManifest.xml`.
- `local.properties` is machine-local (SDK path) — never commit changes to it.

## Build / run
```sh
./gradlew assembleDebug        # build
./gradlew installDebug         # install to a running emulator/device
./gradlew test                 # unit tests
```
- Ignore anything under `app/build/` — generated output, not source.
