# LifeRise — Modern Personal Growth & Mindfulness Android Application

**LifeRise** is a native, modern Android application engineered with Kotlin, Jetpack Compose, Material 3, Room, and MVVM Architecture. It integrates private daily journaling, AI-driven personal growth analysis, structured daily goals, scheduled alarms with interactive quizzes, social status sharing, and privacy-first controls.

---

## 🌟 Key Features Implemented

### 1. Private Daily Diary
- **Local-First & Private**: Persisted entirely on-device via Room database. Entries and attachments are never leaked to public feeds.
- **Rich Entry Management**: Create, edit, and delete reflections with confirmation dialogs.
- **Mood Tracking**: Emoji and state selectors (🌟 Excellent, 😊 Good, 🌿 Peaceful, 😐 Neutral, 🥱 Tired, 😰 Stressed, 😔 Low).
- **Search & Tagging**: Full-text search across titles and reflection content, with custom hashtags and category filtering.
- **Privacy Badges**: Visible privacy and offline verification badges throughout writing and reading flows.

### 2. AI Diary Growth Insights
- **Dual-Engine Architecture**:
  - **Local Rule-Based Analyzer (Demo Mode)**: Operates completely offline without sending any data to external networks. Strictly separates explicitly stated facts from constructive suggestions.
  - **Remote Cloud AI Gateway**: Pre-configured provider interface allowing external AI models to be hooked up via secure backend proxy where secrets are never exposed on Android.
- **Structured Findings**: Summary, key achievements, difficulties/mistakes, lessons learned, suggested improvements, and one practical action for tomorrow.
- **User Control**: Optional processing with explicit user consent toggle and ability to permanently delete stored analyses.

### 3. Daily Goals & Progress Tracking
- **Normalized Weighted Scoring (0–100 pts)**:
  - Task Completion: 35% weight
  - Daily Missions: 30% weight
  - Diary Reflection Continuity: 20% weight
  - Mindfulness & Habit Reflection: 15% weight
- **Cognitive Metric Segregation**: Challenge alarm quiz accuracy is recorded separately to prevent puzzle difficulty from penalizing productivity.
- **Dynamic Feedback**: Real-time positive highlights, areas for improvement, today vs. yesterday delta, and 7-day trend bar chart.
- **Inspectable Formula**: In-app breakdown dialog explaining exactly how scores are derived.

### 4. Tasks & Focus Reminders
- **Prioritization & Scheduling**: Due dates, due times, and priority levels (High, Medium, Low).
- **Recurrence**: None, Daily, and Weekly task repetition.
- **Exact Alarms**: High-priority notifications and exact clock scheduling with permission handling for Android 12+ (`SCHEDULE_EXACT_ALARM`).
- **Resilience**: `BootReceiver` reschedules pending tasks upon device reboot or time-zone shifts.

### 5. Creative Challenge Alarm & Ringing Experience
- **Interactive Ringing Screen**: Full-screen lockscreen activity (`AlarmRingingActivity`) that plays alarm audio and vibrates until the challenge is solved.
- **Customization**: Configurable hour/minute, repeat days, difficulty (Easy, Medium, Hard), and required correct answers (1 to 5).
- **Safety First**: Guaranteed Emergency Stop button and Snooze option so alarms can never trap the user.
- **OS Reliability**: Uses `AlarmManager.setAlarmClock()` and foreground service with wake lock for execution across Doze mode.

### 6. Bilingual Quiz Engine
- **Categories**: Mathematics (mental arithmetic, algebra), Kannada Riddles & Proverbs (ಒಗಟುಗಳು ಮತ್ತು ಗಾದೆಗಳು), General Knowledge, and Mixed Questions.
- **Authentic Unicode Kannada**: Proper script rendering with cultural and folk literature attribution.
- **Smart Shuffling**: Options are shuffled dynamically while preserving correct target indices.
- **Accuracy Tracking**: Tracks attempts, correctness percentage, and completion duration.

### 7. Daily 3 Missions
- **Micro-Commitments**: 3 curated daily missions across Focus, Reflection, and Cognitive Training.
- **Regeneration & Replacement**: Skip or replace missions dynamically from the candidate pool.
- **Progress Synchronization**: Mission completion feeds directly into daily progress score calculations.

### 8. Social Status Updates
- **Community Feed**: Public status posts, captions, attached photos, and hashtags.
- **Strict Privacy Barrier**: Diary entries, moods, alarms, and personal scores are strictly quarantined from social posts.
- **Safety & Moderation**: Report post dialog and author deletion controls.

### 9. Multi-User Community & Cloud Photo Storage
- **Supabase Integration**: Ready for Supabase PostgreSQL and Storage bucket `post-photos`.
- **Offline Demo Mode**: When credentials are not configured, the app runs with rich sample data and clear demo badges.
- **Photo Compression**: System photo picker with automatic resizing and compression (< 5 MB validation).

### 10. Privacy & App Settings
- **Theme Selection**: Light, Dark, or System Default with Deep Indigo and Teal palette.
- **Language**: English and Kannada (ಕನ್ನಡ) localized strings.
- **Security Lock**: 4-digit PIN protection (hashed via SHA-256) and Biometric authentication where supported.
- **Data Export & Wipe**: Export diary and task records to structured JSON; permanent local data wipe with confirmation.

---

## 📁 Project Directory Structure

```
LifeRise/
├── app/
│   ├── build.gradle.kts                      # Module build script & dependencies
│   ├── proguard-rules.pro                    # ProGuard / R8 rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml           # Permissions, activities, receivers, services
│       │   ├── java/com/liferise/app/
│       │   │   ├── LifeRiseApplication.kt    # Application entry, DI, notification channels
│       │   │   ├── MainActivity.kt           # Navigation bar, Compose entry
│       │   │   ├── ai/                       # AI providers, ViewModel, Analysis Screen
│       │   │   ├── alarms/                   # Schedulers, Receivers, Audio Service, Ringing UI
│       │   │   ├── community/                # Social posts, PostItemView, CreatePost, Demo Data
│       │   │   ├── core/
│       │   │   │   ├── designsystem/         # Color, Type, Shape, Theme, Reusable Components
│       │   │   │   ├── di/                   # AppContainer dependency locator
│       │   │   │   ├── navigation/           # Routes, NavGraph, BottomNavItem
│       │   │   │   ├── security/             # PIN hashing, Biometrics, SecureStorage
│       │   │   │   └── util/                 # DateUtils, Resource wrapper, Dispatchers
│       │   │   ├── data/
│       │   │   │   ├── local/                # Room database, DAOs, Entities, Type Converters
│       │   │   │   └── repository/           # Repositories for all 10 feature domains
│       │   │   ├── diary/                    # List, Edit, Detail, Mood selector
│       │   │   ├── domain/model/             # Pure Kotlin domain data models
│       │   │   ├── home/                     # Home Dashboard, Greeting, Highlights
│       │   │   ├── missions/                 # Daily 3 Missions generator and UI
│       │   │   ├── photos/                   # Photo compression & validation
│       │   │   ├── progress/                 # Weighted scoring engine & Progress Screen
│       │   │   ├── quizzes/                  # Bilingual Quiz Engine & Question Packs
│       │   │   ├── settings/                 # App Preferences, Security, JSON Export
│       │   │   └── tasks/                    # Task management, reminders, dialogs
│       │   └── res/
│       │       ├── drawable/                 # Launcher vectors and shapes
│       │       ├── mipmap-anydpi-v26/        # Adaptive launcher icons
│       │       ├── values/                   # Strings, Colors, Themes
│       │       ├── values-kn/                # Kannada localized strings
│       │       └── xml/                      # Backup and data extraction rules
│       └── test/java/com/liferise/app/       # Unit test suites
├── backend/
│   └── supabase/
│       ├── migrations/
│       │   └── 001_initial_schema.sql        # Supabase PostgreSQL schema & RLS policies
│       └── README.md                         # Supabase backend deployment guide
├── gradle/
│   ├── libs.versions.toml                    # Version catalog
│   └── wrapper/                              # Gradle wrapper distribution
├── build.gradle.kts                          # Root build script
├── settings.gradle.kts                       # Settings and repositories
├── gradle.properties                         # JVM arguments and flags
├── local.properties.example                  # Template for Supabase & SDK setup
└── README.md                                 # Full documentation
```

---

## ⚙️ Configuration & Setup

### 1. Requirements
- **JDK**: Java 17, 21, or 22
- **Android Studio**: Android Studio Koala / Ladybug or newer
- **Android SDK**: Compile SDK 34 (Android 14) or 35 (Android 15), Min SDK 26 (Android 8.0+)

### 2. Configure `local.properties`
Copy `local.properties.example` to `local.properties`:
```properties
# Android SDK location
sdk.dir=C:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\Sdk

# Optional: Supabase backend credentials (Demo mode works out of the box if omitted)
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key

# Optional: Cloud AI proxy endpoint
AI_ENDPOINT_URL=https://api.yourbackend.com/v1/analyze-diary
```

---

## 🚀 Building & Running

### Opening in Android Studio
1. Open Android Studio and select **File -> Open**.
2. Select the `LifeRise` root directory.
3. Allow Gradle to synchronize dependencies.
4. Select a virtual device (AVD) or connect a physical Android device.
5. Click **Run** (`Shift + F10`).

### Generating a Debug APK via Command Line
```bash
./gradlew assembleDebug
```
The resulting APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Installing on a Physical Android Phone
1. Enable **Developer Options** and **USB Debugging** on your phone.
2. Connect your phone via USB cable and verify connection:
   ```bash
   adb devices
   ```
3. Install the APK directly:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 🧪 Automated Test Coverage

The project includes 6 unit test suites located in `app/src/test/java/com/liferise/app/`:
1. `ProgressCalculatorTest`: Validates weighted point allocations (Tasks 35%, Missions 30%, Diary 20%, Reflection 15%), missing data resilience, and delta calculations.
2. `QuizEngineTest`: Validates answer shuffling preserving correct targets, Kannada Unicode character verification, and accuracy tracking.
3. `MissionGeneratorTest`: Validates daily 3 missions generation, category uniqueness, and replacement logic.
4. `RuleBasedAiAnalyzerTest`: Validates extraction of explicit facts vs. suggestions, and input length checks.
5. `SocialVisibilityTest`: Verifies public, followers-only, and private visibility policies and strict diary isolation.
6. `DateUtilsTest`: Verifies boundary timestamps, overdue status, and 7-day windows.

Run tests using:
```bash
./gradlew test
```
