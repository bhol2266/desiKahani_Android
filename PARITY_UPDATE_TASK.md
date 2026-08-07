# Job: Bring desiKahani_Android up to parity with desikahaniyaAdultApp

## Context

This project (`com.bhola.desiKahaniya`, versionCode 35) is the **live app**. A newer sibling
project, `desikahaniyaAdultApp` (`com.sgs.desiKahaniyaAdult`, versionCode 6), located at
`D:\DesktopData\Android Applications\desikahaniyaAdultApp`, has newer dependency versions, a
rebuilt audio-playback architecture, and other improvements. The goal is to port those
improvements into **this** project while preserving this project's own identity.

Read this whole file before making changes — several items require an explicit decision
(flagged **DECIDE**) rather than blind copying, because copying verbatim would break or change
production behavior.

## Hard constraints — never overwrite these with the reference app's values

- `applicationId` stays `com.bhola.desiKahaniya` (do NOT change to `com.sgs.desiKahaniyaAdult`).
- Keep this project's own `google-services.json` / Firebase project.
- Keep this project's own ad unit IDs in `app/src/main/res/values/strings.xml`:
  - `app_ID` = `ca-app-pub-8226562131234590~7133484445`
  - `facebook_app_id` = `[773552330024929]`
  - All other `ca-app-pub-8226562131234590/...` unit IDs already in this file.
  - Do NOT copy the reference app's ad unit IDs (it currently even ships a Google *test* rewarded-interstitial ID — not something to import).
- Keep the Firebase Realtime Database root node name `"Sexy_Desi_Kahani"` in `SplashScreen.java` (reference app uses `"Hindi_desi_Kahani_Adult"` — that's specific to its own Firebase project, do not copy).
- Keep `versionCode`/`versionName` progression native to this project (increment from 35, don't reset to reference's 6).
- Keep `rootProject.name` as `"Sexy Desi Kahaniya"` in `settings.gradle`.

Reference app's source: `D:\DesktopData\Android Applications\desikahaniyaAdultApp`. Diff everything against that path.

---

## Phase 1 — Build toolchain & dependency versions

Edit `app/build.gradle`:

1. `compileSdkVersion 34` → `compileSdk 36`; `buildToolsVersion "30.0.3"` → `'36.0.0'`.
2. `compileOptions`: `JavaVersion.VERSION_1_8` → `JavaVersion.VERSION_17` (both source and target compatibility).
3. Add `google()` to the `repositories { }` block.
4. Bump these dependency versions to match reference:
   - `androidx.appcompat:appcompat` → 1.7.1
   - `androidx.constraintlayout:constraintlayout` → 2.2.1
   - `androidx.recyclerview:recyclerview` → 1.4.0
   - `com.google.android.material:material` → 1.12.0
   - `com.google.android.gms:play-services-ads` → 24.4.0
   - `com.facebook.android:audience-network-sdk` → 6.20.0
   - `com.google.ads.mediation:facebook` → 6.20.0.0
   - firebase-bom platform → 33.16.0
   - `com.firebaseui:firebase-ui-database` → 8.0.2
   - `com.google.android.play:asset-delivery` → 2.3.0
   - `com.google.android.gms:play-services-tasks` → 18.3.2
   - `com.airbnb.android:lottie` → 6.3.0
   - `androidx.lifecycle:lifecycle-runtime` / `-process` / `-compiler` → 2.9.1
   - `androidx.test.ext:junit` → 1.2.1
   - `androidx.test.espresso:espresso-core` → 3.6.1
   - Leave `com.android.billingclient:billing` at 7.1.1 (this project's current version is newer than reference's 7.1.0 — don't downgrade).
5. Remove the individually-pinned Firebase versions (`firebase-database:21.0.0`, `-messaging:24.1.0`, `-auth:23.1.0`, `-storage:21.0.1`, `-firestore:25.1.1`, `-crashlytics:19.3.0`, `-analytics:22.1.2`) and instead declare those artifacts with no version, same as reference — let the BOM (33.16.0) manage them. Keep the BOM platform declaration.
6. Replace `implementation 'com.android.support:multidex:1.0.3'` with `implementation 'androidx.multidex:multidex:2.0.1'`.
7. Add new dependencies present in reference:
   - `implementation 'androidx.media:media:1.7.0'` (required for the new `AudioPlayerService`'s `MediaSessionCompat`)
   - `implementation 'androidx.activity:activity:1.8.0'`
   - Do **not** add `com.google.android.exoplayer:exoplayer:2.19.1` — reference declares it but never uses it (still uses plain `MediaPlayer`). Skip it; it's dead weight in the reference project.
8. **DECIDE**: `com.google.android.play:app-update:2.1.0` — this project uses it for `InAppUpdate.java` (in-app update flow), a feature the reference app dropped entirely. Recommendation: keep it, since real users currently get this feature. Don't remove `InAppUpdate.java` or this dependency unless you've confirmed the feature should be retired.

Edit root `build.gradle`:
- AGP `com.android.tools.build:gradle` 8.2.2 → 8.11.1
- `com.google.gms:google-services` 4.4.2 → 4.4.3
- `com.google.firebase:firebase-crashlytics-gradle` 3.0.2 → 3.0.4

Edit `gradle/wrapper/gradle-wrapper.properties`:
- `gradle-8.2-bin.zip` → `gradle-8.13-bin.zip`

Edit `nativetemplates/build.gradle` (submodule):
- `targetSdkVersion` 33 → keep as-is or align to reference's 30 only after checking which is actually correct — reference's value (30) is *lower* than this project's (33) and looks like unintentional drift on the reference side, not an upgrade. **DECIDE**: recommend leaving this project's `targetSdkVersion 33` alone rather than downgrading to match reference.
- Bump `androidx.appcompat` → 1.7.1, `junit` → 4.13.2, `androidx.test:runner` → 1.6.2, `espresso-core` → 3.6.1, `play-services-ads` → 24.4.0, `constraintlayout` → 2.2.1, `error_prone_annotations` → 2.26.0, to match reference.
- Remove the `lintOptions { resourcePrefix 'gnt_' }` block (reference dropped it).

`proguard-rules.pro` and `gradle.properties` are already identical to reference — no changes needed.

---

## Phase 2 — AndroidManifest.xml

Edit `app/src/main/AndroidManifest.xml`:

1. Add permissions:
   ```xml
   <uses-permission android:name="com.android.vending.BILLING" />
   <uses-permission android:name="com.google.android.gms.permission.AD_ID" />
   <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
   ```
2. **DECIDE**: `android:allowBackup` — this project currently has `"false"`, reference has `"true"`. This is a real behavioral difference (whether app data can be backed up/restored by the OS/Play). Don't flip it without deciding intentionally; default recommendation is to leave it `"false"` unless there's a specific reason to allow backup.
3. Add the new service/receiver (needed for Phase 3's audio architecture):
   ```xml
   <service android:name=".AudioPlayerService" android:foregroundServiceType="mediaPlayback" android:exported="false" />
   <receiver android:name=".NotificationDismissedReceiver" android:exported="false" />
   ```
4. Keep the existing `<activity android:name=".AudioPlayerOffline" .../>` entry — do not remove it (see Phase 3 note on offline playback).
5. Do **not** add `<activity android:name=".AudioPlayer2" .../>` — it's a dead manifest entry in the reference app (no corresponding `AudioPlayer2.java` class exists there either).
6. Leave `com.google.android.gms.ads.APPLICATION_ID` and `com.facebook.sdk.ApplicationId` meta-data pointing at `@string/app_ID` / `@string/facebook_app_id` (this project's own values, already correct).

---

## Phase 3 — Audio playback architecture (the big structural change)

This is the largest piece of work. Reference app moved audio playback out of the `AudioPlayer`
Activity and into a foreground `Service` with lock-screen/notification media controls. Port this
pattern:

1. **Add `AudioPlayerService.java`** (new file, copy from reference and update package declaration to `com.bhola.desiKahaniya`): foreground `Service`, `START_STICKY`, owns the `MediaPlayer` + a `MediaSessionCompat`, drives a persistent notification (`NotificationCompat.Builder` + `MediaStyle`, play/pause actions, `setDeleteIntent` → `NotificationDismissedReceiver`). Responds to explicit `Intent` actions: `"PLAY"`, `"PAUSE"`, `"TOGGLE"`, `"SEEK"`, `"SYNC"`. Broadcasts `PROGRESS_UPDATE`, `BUFFER_UPDATE`, `PAUSE_PLAY_BTN_UPDATE`.

2. **Add `BaseActivity.java`** (new file, adapt package): base `AppCompatActivity` that gives every screen a persistent mini/bottom audio-player bar reacting to the `PROGRESS_UPDATE` broadcast.

3. **Add `NotificationDismissedReceiver.java`** (new file, adapt package): stops `AudioPlayerService` when the media notification is swiped away and the app process isn't alive.

4. **Rewrite `AudioPlayer.java`** to start/bind to `AudioPlayerService` instead of holding its own `MediaPlayer` directly, matching reference's version — but **DECIDE** on one specific point first: reference's `AudioPlayer.java` reads `storyURL`/`audioHref`/`title` **plain** from intent extras, with no `SplashScreen.decryption()` call. This project's current `AudioPlayer.java` decrypts these values (`SplashScreen.decryption(getIntent().getStringExtra(...))`) because the data is encrypted end-to-end. Before porting, confirm whether reference intentionally dropped decryption (e.g. because it changed how data is stored) or whether this is a bug in the reference app. If this project's data is still encrypted at rest/in intents, **keep the decryption call** — do not blindly copy reference's unencrypted version, or playback will break (garbled URLs).

5. **`MyApplication.java`**: add the `NotificationChannel` creation for `AudioPlayerService.CHANNEL_ID` (`IMPORTANCE_LOW`) in `onCreate()`, matching reference. Leave the `AppOpenAdManager` logic as-is (already identical between projects; optional cosmetic rename of `TEST_AD_UNIT_ID` → `AD_UNIT_ID` constant, no behavior change either way).

6. **`SplashScreen.java`**: add the audio-resume branch to `handler_forIntent()` — when the app is relaunched via `AudioPlayerService`'s notification `PendingIntent` (carrying `ComingFromAudioPlayer` + `storyURL`/`storyName`/`title`/`audioHref`/`AudioDownloadState` extras), route straight into `AudioPlayer.class` with those extras instead of the normal `Notification_Story_Detail`/`Collection_GridView` flow. Copy this branch from reference's `SplashScreen.java` (~lines 308-320) and adjust for whatever decision was made in step 4 about encryption.

7. **DECIDE**: `AudioPlayerOffline.java` (offline audio playback, this project only) — reference folded offline playback into the same `AudioPlayer.java`/`AudioPlayerService` flow using an `AudioDownloadState` intent extra instead of a separate Activity. Recommendation: fold offline playback the same way during this port (cleaner, one code path) rather than maintaining two separate audio activities — but this is a larger refactor than a straight port, so treat it as its own sub-task and test offline playback thoroughly afterward. If time-constrained, it's acceptable to leave `AudioPlayerOffline.java` as a separate legacy path for now and revisit later.

---

## Phase 4 — Everything else (no action needed, confirmed identical/compatible)

These were diffed in detail and found to be either identical or safe as-is — do not spend time on them:

- `ADS_ADMOB.java`, `ADS_FACEBOOK.java` — identical logic to reference already (banner/interstitial/rewarded-interstitial AdMob + Facebook interstitial/banner). No changes needed.
- `VipMembership.java`, `Vip_CustomAdapter.java` — billing flow, product IDs, offer-timer logic all identical to reference already. One cosmetic-only difference: this project's layout also has a `coinsItem_progressbar` view reference not present in reference's layout — leave as-is, it's harmless.
- `DatabaseHelper.java` — structurally identical to reference (table creation, `CheckDatabases()`, `readLatestStoryDate()`, `addstories()`). No changes needed.
- `checkout.aar` in `app/libs/` — present in both projects but unreferenced by any Gradle dependency or Java code in either. It's dead weight in both. Optional cleanup: safe to delete, but not required for this task.
- `proguard-rules.pro`, `gradle.properties` — byte-identical already.
- Native ad template invocation (`ftab2.java`, `StoryDetails_Adapter.java`, `Notification_Story_Detail.java`) — same usage pattern in both, only the `nativetemplates` module's own dependency versions changed (handled in Phase 1).

## Phase 5 — Flagged pre-existing bugs (not caused by this migration, fix opportunistically)

- `app/src/main/res/values/strings.xml`: the `AppOpen` ad unit string has a stray leading dot — e.g. `".ca-app-pub-8226562131234590/4634556992"` should be `"ca-app-pub-8226562131234590/4634556992"`. This bug exists in both projects; fix it in this one while you're in the file for Phase 1/ad-unit work. Do not touch the actual ID digits, only strip the leading `.`.
- `SplashScreen.java`: `currentApp_Version` constant is currently `1`. Bump it deliberately (not to reference's `3` — that's reference's own internal counter) when you ship this update, so any version-gated logic tied to it fires correctly.
- **DECIDE**: `updateStoriesInDB()` is currently called unconditionally in `onCreate()` on every cold start (a Firestore read on every launch). Reference gates the same call behind `if (SplashScreen.Login_Times > 5)` to throttle it. Recommend adopting the same throttle to reduce Firestore read costs — but confirm this doesn't break any assumption elsewhere in the app about fresh data being available immediately.
- The `readStoryFromJson()`/`trasferData()` fake-story-seeding method is dead code in **both** projects (not called from `onCreate()` in either — commented out here, simply unused in reference). Leave as dead code; do not wire it up as part of this task unless explicitly asked to.

---

## Verification checklist before considering this done

1. Project builds clean with AGP 8.11.1 / Gradle 8.13 / Java 17 (`./gradlew assembleDebug`).
2. App launches, splash screen completes, Firebase sync against `"Sexy_Desi_Kahani"` node still works.
3. Story browsing, story reading (`StoryPage`) still work.
4. Audio playback: start a story, background the app, confirm notification media controls (play/pause) work, confirm playback survives Activity destruction, confirm swiping the notification away stops playback via `NotificationDismissedReceiver`.
5. Tapping the audio notification while app is killed relaunches correctly into `AudioPlayer` via the new `SplashScreen.handler_forIntent()` branch (Phase 3.6) — verify URLs are not garbled (this is the decryption checkpoint from Phase 3.4).
6. Offline/downloaded story playback still works (whichever path was chosen in Phase 3.7).
7. VIP purchase flow still completes and unlocks content (existing flow, should be unaffected — verify only, no code changed here).
8. Ads: banner, interstitial, rewarded-interstitial, native, and App Open ad all still fire with this project's own (real) ad unit IDs — not reference's test/placeholder IDs.
9. `google-services.json` and `applicationId` are untouched — confirm with `grep applicationId app/build.gradle` shows `com.bhola.desiKahaniya`.
