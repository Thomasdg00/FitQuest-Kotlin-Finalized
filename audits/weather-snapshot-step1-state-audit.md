# Weather Snapshot Step 1 State Audit

Date: 2026-06-30
Branch: main
Safety tag: kotlin-before-weather-snapshot-removal exists at HEAD
Verdict: READY_TO_COMMIT_STEP_1

## 1. Executive Summary

Verdict: READY_TO_COMMIT_STEP_1.

FIX: none found for Step 1 before commit.

Step 1 is no longer in an incomplete middle state. The runtime and UI usage of per-workout weather snapshots has been removed, while daily Home forecast support remains intact.

The refactor did not modify the intended Room DB/schema files. `FitQuestDatabase.kt`, `WeatherSnapshotEntity.kt`, `WeatherSnapshotDao.kt`, and exported schema JSON are still present and unmodified. These are the expected Step 2 DB cleanup remnants.

Required validation passed:

- `.\gradlew.bat testDebugUnitTest`: PASS
- `.\gradlew.bat assembleDebug`: PASS
- `.\gradlew.bat assembleDebugAndroidTest`: PASS
- `git diff --check`: PASS
- `connectedDebugAndroidTest`: SKIPPED because SDK `adb.exe devices` showed no attached devices

Home forecast is preserved: `WeatherRepository.fetchDailyForecast`, `OpenMeteoApi.dailyForecast`, daily DTOs, daily mappers, `DailyForecast`, `WeatherCodeLabels`, `HomeViewModel`, `HomeScreen`, and daily weather tests remain present and compile.

Recommended next action: commit Step 1.

## 2. Git State

Branch:

```text
main
```

Pre-report `git status --short`:

```text
 M app/src/main/java/com/univpm/fitquest/data/remote/OpenMeteoApi.kt
 M app/src/main/java/com/univpm/fitquest/data/remote/OpenMeteoDtos.kt
 M app/src/main/java/com/univpm/fitquest/data/remote/OpenMeteoMappers.kt
 M app/src/main/java/com/univpm/fitquest/data/repository/WeatherRepository.kt
 M app/src/main/java/com/univpm/fitquest/data/repository/WorkoutRepository.kt
 M app/src/main/java/com/univpm/fitquest/di/AppContainer.kt
 D app/src/main/java/com/univpm/fitquest/domain/model/WeatherSnapshotDraft.kt
 M app/src/main/java/com/univpm/fitquest/tracking/service/TrackingService.kt
 M app/src/main/java/com/univpm/fitquest/tracking/service/TrackingServiceState.kt
 D app/src/main/java/com/univpm/fitquest/tracking/service/WeatherCaptureHelper.kt
 M app/src/main/java/com/univpm/fitquest/tracking/service/WorkoutSaveCoordinator.kt
 M app/src/main/java/com/univpm/fitquest/ui/screens/history/WorkoutDetailScreen.kt
 M app/src/main/java/com/univpm/fitquest/ui/screens/track/TrackComponents.kt
 M app/src/main/java/com/univpm/fitquest/ui/screens/track/TrackScreen.kt
 M app/src/main/java/com/univpm/fitquest/viewmodel/TrackViewModel.kt
 M app/src/main/java/com/univpm/fitquest/viewmodel/WorkoutDetailViewModel.kt
 M app/src/main/res/values-it/strings.xml
 M app/src/main/res/values/strings.xml
 M app/src/test/java/com/univpm/fitquest/data/remote/OpenMeteoMappersTest.kt
 M app/src/test/java/com/univpm/fitquest/data/repository/WeatherRepositoryTest.kt
```

Recent log:

```text
dbd54ed (HEAD -> main, tag: kotlin-before-weather-snapshot-removal, origin/main, origin/HEAD) Remove unused background location permission
719a9cd Extract workout save coordinator
4d5a754 (tag: kotlin-before-workout-save-coordinator) Extract weather capture helper
c05447c Remove unused repository and DAO helpers
12fb602 Extract tracking notification helper
ed290cd Fix null weather capture status
9f3b693 Fix duplicated bottom navigation accessibility labels
eb7e647 Add settings input validation feedback
```

Safety tag check:

```text
kotlin-before-weather-snapshot-removal
```

`git diff --name-status`:

```text
M	app/src/main/java/com/univpm/fitquest/data/remote/OpenMeteoApi.kt
M	app/src/main/java/com/univpm/fitquest/data/remote/OpenMeteoDtos.kt
M	app/src/main/java/com/univpm/fitquest/data/remote/OpenMeteoMappers.kt
M	app/src/main/java/com/univpm/fitquest/data/repository/WeatherRepository.kt
M	app/src/main/java/com/univpm/fitquest/data/repository/WorkoutRepository.kt
M	app/src/main/java/com/univpm/fitquest/di/AppContainer.kt
D	app/src/main/java/com/univpm/fitquest/domain/model/WeatherSnapshotDraft.kt
M	app/src/main/java/com/univpm/fitquest/tracking/service/TrackingService.kt
M	app/src/main/java/com/univpm/fitquest/tracking/service/TrackingServiceState.kt
D	app/src/main/java/com/univpm/fitquest/tracking/service/WeatherCaptureHelper.kt
M	app/src/main/java/com/univpm/fitquest/tracking/service/WorkoutSaveCoordinator.kt
M	app/src/main/java/com/univpm/fitquest/ui/screens/history/WorkoutDetailScreen.kt
M	app/src/main/java/com/univpm/fitquest/ui/screens/track/TrackComponents.kt
M	app/src/main/java/com/univpm/fitquest/ui/screens/track/TrackScreen.kt
M	app/src/main/java/com/univpm/fitquest/viewmodel/TrackViewModel.kt
M	app/src/main/java/com/univpm/fitquest/viewmodel/WorkoutDetailViewModel.kt
M	app/src/main/res/values-it/strings.xml
M	app/src/main/res/values/strings.xml
M	app/src/test/java/com/univpm/fitquest/data/remote/OpenMeteoMappersTest.kt
M	app/src/test/java/com/univpm/fitquest/data/repository/WeatherRepositoryTest.kt
```

`git diff --stat`:

```text
 .../univpm/fitquest/data/remote/OpenMeteoApi.kt    | 10 ----
 .../univpm/fitquest/data/remote/OpenMeteoDtos.kt   | 10 ----
 .../fitquest/data/remote/OpenMeteoMappers.kt       | 13 -----
 .../fitquest/data/repository/WeatherRepository.kt  | 17 -------
 .../fitquest/data/repository/WorkoutRepository.kt  |  7 ---
 .../java/com/univpm/fitquest/di/AppContainer.kt    |  1 -
 .../fitquest/domain/model/WeatherSnapshotDraft.kt  |  9 ----
 .../fitquest/tracking/service/TrackingService.kt   | 30 -----------
 .../tracking/service/TrackingServiceState.kt       |  8 ---
 .../tracking/service/WeatherCaptureHelper.kt       | 27 ----------
 .../tracking/service/WorkoutSaveCoordinator.kt     | 17 -------
 .../ui/screens/history/WorkoutDetailScreen.kt      | 43 ----------------
 .../fitquest/ui/screens/track/TrackComponents.kt   | 58 ----------------------
 .../fitquest/ui/screens/track/TrackScreen.kt       |  8 ---
 .../univpm/fitquest/viewmodel/TrackViewModel.kt    |  3 --
 .../fitquest/viewmodel/WorkoutDetailViewModel.kt   |  6 +--
 app/src/main/res/values-it/strings.xml             | 13 +----
 app/src/main/res/values/strings.xml                | 13 +----
 .../fitquest/data/remote/OpenMeteoMappersTest.kt   | 25 ----------
 .../data/repository/WeatherRepositoryTest.kt       | 52 -------------------
 20 files changed, 5 insertions(+), 365 deletions(-)
```

Note: Git printed CRLF/LF replacement warnings for touched working-copy files during diff commands. `git diff --check` still exited 0.

Specific DB file check:

- Original prompt paths under `core/database` and `weather/data/local` are not project paths and had no git status output.
- Actual project paths found:
  - `app/src/main/java/com/univpm/fitquest/data/local/database/FitQuestDatabase.kt`
  - `app/src/main/java/com/univpm/fitquest/data/local/entity/WeatherSnapshotEntity.kt`
  - `app/src/main/java/com/univpm/fitquest/data/local/dao/WeatherSnapshotDao.kt`
- Actual DB paths had no `git status --short` output and no `git diff --name-status` output.
- `app/schemas` had no `git diff --name-status` output.

## 3. Changed Files by Category

Tracking/service:

- `M app/src/main/java/com/univpm/fitquest/tracking/service/TrackingService.kt`
  - Removed `WeatherCaptureHelper`, `WeatherSnapshotDraft`, weather request state, `startWeatherFetch`, and weather snapshot passing into save.
  - Preserved route point collection, workout save call, foreground service start, pause/resume/stop action handling, and notification helper usage.
- `M app/src/main/java/com/univpm/fitquest/tracking/service/TrackingServiceState.kt`
  - Removed `weatherStatus` and `WeatherCaptureStatus`.
- `D app/src/main/java/com/univpm/fitquest/tracking/service/WeatherCaptureHelper.kt`
  - Deleted current-weather capture helper.
- `M app/src/main/java/com/univpm/fitquest/tracking/service/WorkoutSaveCoordinator.kt`
  - Removed weather draft to entity conversion and `workoutRepository.saveWeather`.
  - Preserved `addWorkout` and `addRoutePoints`.

Repositories/AppContainer:

- `M app/src/main/java/com/univpm/fitquest/data/repository/WorkoutRepository.kt`
  - Removed `WeatherSnapshotDao` dependency, `observeWeather`, and `saveWeather`.
  - Preserved workout and route repository methods.
- `M app/src/main/java/com/univpm/fitquest/di/AppContainer.kt`
  - Removed `database.weatherSnapshotDao()` argument from `WorkoutRepository`.

Domain/model:

- `D app/src/main/java/com/univpm/fitquest/domain/model/WeatherSnapshotDraft.kt`
  - Deleted runtime draft model.

Workout detail:

- `M app/src/main/java/com/univpm/fitquest/viewmodel/WorkoutDetailViewModel.kt`
  - Removed `weatherSnapshot` from detail UI state and combine flow.
- `M app/src/main/java/com/univpm/fitquest/ui/screens/history/WorkoutDetailScreen.kt`
  - Removed saved-weather card and per-workout weather display.

Tracking UI/ViewModel:

- `M app/src/main/java/com/univpm/fitquest/ui/screens/track/TrackComponents.kt`
  - Removed `WeatherStatusPill`.
- `M app/src/main/java/com/univpm/fitquest/ui/screens/track/TrackScreen.kt`
  - Removed tracking weather status pill display.
- `M app/src/main/java/com/univpm/fitquest/viewmodel/TrackViewModel.kt`
  - Removed weather status from panel UI state mapping.

Weather API/repository/mappers:

- `M app/src/main/java/com/univpm/fitquest/data/remote/OpenMeteoApi.kt`
  - Removed `currentWeather` endpoint and current-weather constants.
  - Preserved `dailyForecast`.
- `M app/src/main/java/com/univpm/fitquest/data/remote/OpenMeteoDtos.kt`
  - Removed current-weather DTOs.
  - Preserved daily forecast DTOs.
- `M app/src/main/java/com/univpm/fitquest/data/remote/OpenMeteoMappers.kt`
  - Removed current-weather snapshot mapper.
  - Preserved `toDailyForecasts`.
- `M app/src/main/java/com/univpm/fitquest/data/repository/WeatherRepository.kt`
  - Removed `fetchCurrentWeather`.
  - Preserved `fetchDailyForecast`.

Strings/resources:

- `M app/src/main/res/values/strings.xml`
- `M app/src/main/res/values-it/strings.xml`
  - Removed per-workout weather/status strings.
  - Updated workout detail subtitle and delete confirmation to remove weather snapshot wording.
  - Preserved Home weather and weather condition labels.

Tests:

- `M app/src/test/java/com/univpm/fitquest/data/remote/OpenMeteoMappersTest.kt`
  - Removed current-weather snapshot mapper tests.
  - Preserved daily forecast mapper tests.
- `M app/src/test/java/com/univpm/fitquest/data/repository/WeatherRepositoryTest.kt`
  - Removed current-weather repository tests.
  - Preserved daily forecast repository tests.

Room DB/schema:

- No modified DB/schema files.
- `FitQuestDatabase.kt` unchanged.
- `WeatherSnapshotEntity.kt` unchanged.
- `WeatherSnapshotDao.kt` unchanged.
- `app/schemas/com.univpm.fitquest.data.local.database.FitQuestDatabase/1.json` unchanged.

Other:

- No other changed files before this report.
- This audit file was created after the captured git state.

## 4. Build and Test Results

`.\gradlew.bat testDebugUnitTest`

- Exit code: 0
- Result: PASS
- Key output:

```text
> Task :app:testDebugUnitTest UP-TO-DATE
BUILD SUCCESSFUL in 1s
27 actionable tasks: 27 up-to-date
```

`.\gradlew.bat assembleDebug`

- Exit code: 0
- Result: PASS
- Key output:

```text
> Task :app:assembleDebug UP-TO-DATE
BUILD SUCCESSFUL in 1s
38 actionable tasks: 38 up-to-date
```

`.\gradlew.bat assembleDebugAndroidTest`

- Exit code: 0
- Result: PASS
- Key output:

```text
> Task :app:assembleDebugAndroidTest UP-TO-DATE
BUILD SUCCESSFUL in 1s
53 actionable tasks: 53 up-to-date
```

Connected tests:

- `adb devices`: failed because `adb` is not on PATH.
- SDK adb fallback command used: `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe devices`
- SDK adb output:

```text
List of devices attached
```

- Result: SKIPPED
- Reason: no attached emulator/device was listed.

`git diff --check`

- Exit code: 0
- Result: PASS
- Output: only CRLF/LF replacement warnings for touched working-copy files; no whitespace errors.

## 5. Step 1 Removed-Symbol Grep

Command pattern:

```text
WeatherSnapshotDraft|WeatherCaptureHelper|WeatherCaptureStatus|weatherStatus|WeatherStatusPill|observeWeather|saveWeather|fetchCurrentWeather
```

Scope:

```text
app/src/main/java
app/src/test/java
app/src/androidTest/java
```

Result: no matches.

Classification:

- PROBLEM: none
- OK_STEP_2: none in this removed-symbol grep
- OK_HOME_FORECAST: none in this removed-symbol grep
- OK_TEST_DAILY: none in this removed-symbol grep
- UNKNOWN: none

Additional runtime/current-weather grep also found no matches for:

```text
currentWeather
OpenMeteoCurrent
OPEN_METEO_CURRENT
```

Conclusion: runtime/UI/test references that should have been removed in Step 1 are gone.

## 6. DB Remnant Grep

Command pattern:

```text
WeatherSnapshotEntity|WeatherSnapshotDao
```

Matches:

- `app/src/main/java/com/univpm/fitquest/data/local/dao/WeatherSnapshotDao.kt`
  - `WeatherSnapshotDao`
  - `WeatherSnapshotEntity`
  - `upsert`
  - `observeForWorkout`
  - `getForWorkout`
  - Classification: OK_STEP_2
- `app/src/main/java/com/univpm/fitquest/data/local/database/FitQuestDatabase.kt`
  - imports `WeatherSnapshotDao`
  - imports `WeatherSnapshotEntity`
  - includes `WeatherSnapshotEntity::class`
  - exposes `weatherSnapshotDao()`
  - Classification: OK_STEP_2
- `app/src/main/java/com/univpm/fitquest/data/local/entity/WeatherSnapshotEntity.kt`
  - defines `WeatherSnapshotEntity`
  - Classification: OK_STEP_2

Extended DB grep also found exported schema references:

- `app/schemas/com.univpm.fitquest.data.local.database.FitQuestDatabase/1.json`
  - `weather_snapshots`
  - `index_weather_snapshots_workoutId`
  - Classification: OK_STEP_2

No matches were found in:

- `TrackingService`
- `WorkoutSaveCoordinator`
- tracking UI
- workout detail UI
- ViewModels
- runtime repositories after `WorkoutRepository` weather methods were removed

Conclusion: DB remnants are isolated to Room DB/entity/DAO/schema and are suitable for Step 2 cleanup.

## 7. Broad Weather/Meteo Summary

Expected Home forecast references remain:

- `HomeViewModel` keeps `weatherForecast` in `HomeUiState`.
- `HomeViewModel` calls `weatherRepository.fetchDailyForecast(location.latitude, location.longitude)`.
- `HomeScreen` keeps `WeatherWidget`, `WeatherForecastRow`, weather icons, weather labels, and unavailable state.
- `FitQuestNavHost` still passes `appContainer.weatherRepository` into `HomeViewModel`.

Expected OpenMeteo/daily forecast references remain:

- `OpenMeteoApi.dailyForecast`
- `OPEN_METEO_DAILY_FIELDS`
- `OPEN_METEO_FORECAST_DAYS`
- `OPEN_METEO_TIMEZONE`
- `OpenMeteoDailyResponseDto`
- `OpenMeteoDailyDto`
- `toDailyForecasts`
- `WeatherRepository.fetchDailyForecast`
- `DailyForecast`

Expected daily tests remain:

- `OpenMeteoMappersTest.mapsDailyForecastToAtMostThreeRows`
- `OpenMeteoMappersTest.missingDailyForecastReturnsEmptyList`
- `WeatherRepositoryTest.fetchDailyForecastReturnsEmptyListOnFailure`
- `WeatherRepositoryTest.fetchDailyForecastMapsSuccessfulResponse`
- `WeatherCodeLabelsTest`

Suspicious per-workout weather references:

- None found outside DB/entity/DAO/schema Step 2 remnants.

Strings/resources:

- Removed per-workout snapshot/status strings:
  - `weather_ready`
  - `weather_waiting_gps`
  - `weather_fetching`
  - `weather_saved`
  - `weather_no_snapshot`
  - `weather_temperature`
  - `weather_humidity`
  - `weather_wind`
  - `weather_code`
- Preserved Home weather strings and weather condition labels.
- DEFER: `weather_condition_with_code` appears in `values` and `values-it` but no current Kotlin reference was found. It is not a per-workout snapshot runtime reference and does not block Step 1 commit.

## 8. Home Forecast Preservation Check

Does Home forecast still compile?

- Yes. `testDebugUnitTest`, `assembleDebug`, and `assembleDebugAndroidTest` all passed.

Does `fetchDailyForecast` or equivalent still exist?

- Yes. `WeatherRepository.fetchDailyForecast(latitude, longitude): List<DailyForecast>` exists.
- It calls `OpenMeteoApi.dailyForecast` with daily fields, forecast days, and timezone.
- It maps through `toDailyForecasts()`.
- It returns `emptyList()` on failure.

Are daily forecast tests still present?

- Yes.
- `OpenMeteoMappersTest` still tests daily DTO mapping and missing daily data.
- `WeatherRepositoryTest` still tests daily fetch success and failure.
- `WeatherCodeLabelsTest` still tests weather condition labels.

Was any daily forecast code accidentally deleted?

- No accidental deletion found.
- Current-weather/per-workout snapshot API and DTO code was deleted.
- Daily forecast API, DTOs, mapper, repository, model, Home ViewModel wiring, Home UI, and tests remain.

## 9. Step 1 Scope Checklist

- Was `FitQuestDatabase.kt` modified? No.
- Was database version changed? No. It remains `version = 1`.
- Were exported schema JSON files modified/deleted? No.
- Was `WeatherSnapshotEntity.kt` deleted? No.
- Was `WeatherSnapshotDao.kt` deleted? No.
- Was `WeatherCaptureHelper.kt` deleted? Yes.
- Was `WeatherSnapshotDraft.kt` deleted? Yes.
- Was `weatherStatus` removed from runtime tracking state? Yes.
- Was workout detail weather display removed? Yes.
- Was tracking weather status pill removed? Yes.
- Was workout weather save removed? Yes.
- Were core workout fields and route save preserved? Yes. `addWorkout` and `addRoutePoints` remain and are still called by `WorkoutSaveCoordinator`.
- Was GPS/start/pause/resume/stop untouched? Yes by diff review. The action routing, pause/resume/stop methods, route collection, and route point snapshots remain; only weather fetch state/trigger code was removed.
- Was notification behavior untouched? Yes by diff review. `TrackingNotificationHelper.kt` has no diff, and foreground notification setup remains. Removed notification updates were only the internal weather status updates.

## 10. Risk Assessment

Overall risk: low for committing Step 1.

Reasons:

- Build/test validation passed.
- Step 1 removed-symbol grep has no runtime/UI/test matches.
- DB/schema files were not touched.
- Home daily forecast path remains and is covered by tests.
- Workout save and route save remain in place.
- No new dependencies, Gradle changes, Room version changes, schema changes, commits, reverts, or code formatting were performed by this audit.

Residual risks:

- DEFER: `connectedDebugAndroidTest` was skipped because no emulator/device was attached.
- DEFER: Room DB cleanup remains for Step 2.
- DEFER: `weather_condition_with_code` may be an unused string resource, but it is outside the per-workout snapshot runtime path and can wait for resource hygiene.
- IGNORE for Step 1: Git CRLF/LF warnings appeared during diff/check commands, but `git diff --check` passed.

Classification: READY_TO_COMMIT_STEP_1.

## 11. Recommended Next Action

Commit Step 1.

Suggested commit message:

```text
Remove per-workout weather snapshot runtime usage
```

Do not start Step 2 in the same commit. Step 2 should separately remove the Room entity/DAO/database registration/schema remnants with a controlled Room/schema decision.

## 12. Exact Commands Run

Repository instructions:

```powershell
Get-Content -Raw AGENTS.md
```

Git state:

```powershell
git branch --show-current
git status --short
git log --oneline --decorate -8
git tag --list "kotlin-before-weather-snapshot-removal"
git diff --name-status
git diff --stat
git status --short -- app/src/main/java/com/univpm/fitquest/core/database/FitQuestDatabase.kt app/src/main/java/com/univpm/fitquest/weather/data/local/WeatherSnapshotEntity.kt app/src/main/java/com/univpm/fitquest/weather/data/local/WeatherSnapshotDao.kt
git diff --name-status -- app/src/main/java/com/univpm/fitquest/core/database/FitQuestDatabase.kt app/src/main/java/com/univpm/fitquest/weather/data/local/WeatherSnapshotEntity.kt app/src/main/java/com/univpm/fitquest/weather/data/local/WeatherSnapshotDao.kt
Get-ChildItem app/src/main/java -Recurse -Filter FitQuestDatabase.kt
Get-ChildItem app/src/main/java -Recurse -Filter WeatherSnapshotEntity.kt
Get-ChildItem app/src/main/java -Recurse -Filter WeatherSnapshotDao.kt
git diff --name-status -- app/schemas
```

Validation:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat assembleDebugAndroidTest
adb devices
$adbPath = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'; if (Test-Path $adbPath) { & $adbPath devices } else { Write-Output "ADB_NOT_FOUND:$adbPath" }
git diff --check
```

Grep and inspection:

```powershell
Get-ChildItem app/src/main/java,app/src/test/java,app/src/androidTest/java -Recurse -Filter *.kt | Select-String "WeatherSnapshotDraft|WeatherCaptureHelper|WeatherCaptureStatus|weatherStatus|WeatherStatusPill|observeWeather|saveWeather|fetchCurrentWeather"
Get-ChildItem app/src/main/java,app/src/test/java,app/src/androidTest/java -Recurse -Filter *.kt | Select-String "WeatherSnapshotEntity|WeatherSnapshotDao"
Get-ChildItem app/src/main/java,app/src/test/java,app/src/androidTest/java -Recurse | Select-String -Pattern "weather|meteo" -CaseSensitive:$false
Get-Content -Raw app/src/main/java/com/univpm/fitquest/data/repository/WeatherRepository.kt
Get-Content -Raw app/src/main/java/com/univpm/fitquest/data/remote/OpenMeteoApi.kt
Get-Content -Raw app/src/main/java/com/univpm/fitquest/data/remote/OpenMeteoDtos.kt
Get-Content -Raw app/src/main/java/com/univpm/fitquest/data/remote/OpenMeteoMappers.kt
Get-Content -Raw app/src/main/java/com/univpm/fitquest/domain/model/DailyForecast.kt
Get-Content -Raw app/src/main/java/com/univpm/fitquest/ui/resources/WeatherCodeLabels.kt
Select-String -Path app/src/main/java/com/univpm/fitquest/viewmodel/HomeViewModel.kt -Pattern "weatherForecast|fetchDailyForecast|WeatherRepository|loadWeatherForecast" -Context 3,5
Select-String -Path app/src/main/java/com/univpm/fitquest/ui/screens/home/HomeScreen.kt -Pattern "WeatherWidget|WeatherForecastRow|weatherForecast|weatherCodeToLabelRes|getWeatherIcon" -Context 2,4
Get-Content -Raw app/src/test/java/com/univpm/fitquest/data/remote/OpenMeteoMappersTest.kt
Get-Content -Raw app/src/test/java/com/univpm/fitquest/data/repository/WeatherRepositoryTest.kt
Get-Content -Raw app/src/test/java/com/univpm/fitquest/ui/resources/WeatherCodeLabelsTest.kt
git diff -- app/src/main/java/com/univpm/fitquest/tracking/service/TrackingService.kt app/src/main/java/com/univpm/fitquest/tracking/service/TrackingServiceState.kt app/src/main/java/com/univpm/fitquest/tracking/service/WorkoutSaveCoordinator.kt
git diff -- app/src/main/java/com/univpm/fitquest/data/repository/WorkoutRepository.kt app/src/main/java/com/univpm/fitquest/di/AppContainer.kt app/src/main/java/com/univpm/fitquest/viewmodel/WorkoutDetailViewModel.kt
git diff -- app/src/main/java/com/univpm/fitquest/ui/screens/history/WorkoutDetailScreen.kt app/src/main/java/com/univpm/fitquest/ui/screens/track/TrackComponents.kt app/src/main/java/com/univpm/fitquest/ui/screens/track/TrackScreen.kt app/src/main/java/com/univpm/fitquest/viewmodel/TrackViewModel.kt
git diff -- app/src/main/java/com/univpm/fitquest/data/remote/OpenMeteoApi.kt app/src/main/java/com/univpm/fitquest/data/remote/OpenMeteoDtos.kt app/src/main/java/com/univpm/fitquest/data/remote/OpenMeteoMappers.kt app/src/main/java/com/univpm/fitquest/data/repository/WeatherRepository.kt
git diff -- app/src/main/res/values/strings.xml app/src/main/res/values-it/strings.xml app/src/test/java/com/univpm/fitquest/data/remote/OpenMeteoMappersTest.kt app/src/test/java/com/univpm/fitquest/data/repository/WeatherRepositoryTest.kt
rg -n "weather_(ready|waiting_gps|fetching|saved|no_snapshot|temperature|humidity|wind|code|condition_with_code|condition_|cloudy|sunny|partly_cloudy|foggy|rainy|snowy|stormy|unavailable)" app/src/main app/src/test app/src/androidTest
rg -n "WeatherSnapshotDraft|WeatherCaptureHelper|WeatherCaptureStatus|weatherStatus|WeatherStatusPill|observeWeather|saveWeather|fetchCurrentWeather|currentWeather|OpenMeteoCurrent|OPEN_METEO_CURRENT" app/src/main app/src/test app/src/androidTest
rg -n "weatherSnapshotDao|WeatherSnapshotEntity|WeatherSnapshotDao|weather_snapshots" app/src/main app/src/test app/src/androidTest app/schemas
git diff --name-status -- app/src/main/java/com/univpm/fitquest/data/local/database/FitQuestDatabase.kt app/src/main/java/com/univpm/fitquest/data/local/entity/WeatherSnapshotEntity.kt app/src/main/java/com/univpm/fitquest/data/local/dao/WeatherSnapshotDao.kt app/schemas
Test-Path app/src/main/java/com/univpm/fitquest/tracking/service/WeatherCaptureHelper.kt; Test-Path app/src/main/java/com/univpm/fitquest/domain/model/WeatherSnapshotDraft.kt
Select-String -Path app/src/main/java/com/univpm/fitquest/data/local/database/FitQuestDatabase.kt -Pattern "version|WeatherSnapshotEntity|weatherSnapshotDao|entities" -Context 1,2
git status --short -- app/src/main/java/com/univpm/fitquest/data/local/database/FitQuestDatabase.kt app/src/main/java/com/univpm/fitquest/data/local/entity/WeatherSnapshotEntity.kt app/src/main/java/com/univpm/fitquest/data/local/dao/WeatherSnapshotDao.kt app/schemas
git ls-files app/schemas
git diff -- app/src/main/java/com/univpm/fitquest/tracking/service/TrackingNotificationHelper.kt app/src/main/java/com/univpm/fitquest/tracking/service/TrackingActionHandler.kt
rg -n "ACTION_START|ACTION_PAUSE|ACTION_RESUME|ACTION_STOP|pause|resume|notification|saveCompletedWorkout|routePoints|addRoutePoints|addWorkout" app/src/main/java/com/univpm/fitquest/tracking/service app/src/main/java/com/univpm/fitquest/data/repository/WorkoutRepository.kt
Test-Path audits
Test-Path audits/weather-snapshot-step1-state-audit.md
git status --short
New-Item -ItemType Directory -Path audits
```

Report write:

```text
Created audits/weather-snapshot-step1-state-audit.md with apply_patch.
```

## 13. Notes for Step 2 DB Cleanup

Step 2 should be a separate, controlled DB cleanup. Current Step 2 remnants are:

- `app/src/main/java/com/univpm/fitquest/data/local/entity/WeatherSnapshotEntity.kt`
- `app/src/main/java/com/univpm/fitquest/data/local/dao/WeatherSnapshotDao.kt`
- `WeatherSnapshotEntity::class` in `FitQuestDatabase.kt`
- `weatherSnapshotDao()` in `FitQuestDatabase.kt`
- `weather_snapshots` table and index in exported schema JSON

Step 2 must decide Room handling explicitly:

- remove entity/DAO/database registration;
- update exported schema intentionally;
- handle database version/migration or destructive dev-only strategy according to project submission needs;
- rerun `testDebugUnitTest`, `assembleDebug`, `assembleDebugAndroidTest`, and `git diff --check`;
- run connected tests if a device/emulator is attached.

Do not mix Step 2 DB/schema cleanup into the Step 1 commit.
