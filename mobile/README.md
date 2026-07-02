# Mobile App (Android)

This project is an Android app built with Gradle Kotlin DSL.

## Prerequisites

- Android SDK installed (this repo uses: `C:\Users\aditya kumar\AppData\Local\Android\Sdk`)
- At least one Android Virtual Device (AVD) created
- JDK 17+

## Run On Emulator

From project root:

```powershell
cd "c:\Users\aditya kumar\OneDrive\Desktop\Projects\rahul assignment\mobile"
```

1. List available AVDs:

```powershell
"C:\Users\aditya kumar\AppData\Local\Android\Sdk\emulator\emulator.exe" -list-avds
```

2. Start emulator (example AVD: `Pixel_9_Pro`):

```powershell
"C:\Users\aditya kumar\AppData\Local\Android\Sdk\emulator\emulator.exe" -avd Pixel_9_Pro
```

3. Verify device is connected:

```powershell
"C:\Users\aditya kumar\AppData\Local\Android\Sdk\platform-tools\adb.exe" devices
```

4. Build + install debug app on emulator:

```powershell
.\gradlew.bat installDebug
```

5. Launch app manually (if needed):

```powershell
"C:\Users\aditya kumar\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell am start -n com.rahul.mobile/.MainActivity
```

## Build APK

### Debug APK

```powershell
.\gradlew.bat assembleDebug
```

Output APK:

- `app\build\outputs\apk\debug\app-debug.apk`

### Release APK

```powershell
.\gradlew.bat assembleRelease
```

Output APK:

- `app\build\outputs\apk\release\app-release.apk`

This is a **signed, installable** release build. Signing is configured via `keystore.properties` (project root, gitignored) pointing at the keystore in `app/keystore/` (also gitignored) — both are local-only and must not be committed. If either file is missing, `assembleRelease` still succeeds but produces an unsigned APK.

To generate a new keystore (e.g. for a different signing identity):

```powershell
keytool -genkeypair -v -keystore app\keystore\campussync-release.jks -alias campussync -keyalg RSA -keysize 2048 -validity 10000
```

Then create `keystore.properties` at the project root:

```properties
storeFile=app/keystore/campussync-release.jks
storePassword=<your store password>
keyAlias=campussync
keyPassword=<your key password>
```

## App Icon

The launcher icon is generated from `app/src/main/assets/images/app.png` and lives in:

- `app/src/main/res/mipmap-*/ic_launcher.png` and `ic_launcher_round.png` (legacy icon, all API levels)
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` + `app/src/main/res/drawable/ic_launcher_foreground.png` (adaptive icon, API 26+)

To regenerate after changing `app.png`, resize/pad it into each `mipmap-*` density folder (48/72/96/144/192px) and the `drawable` foreground (432px), then rebuild.

## Quick One-Liner (Debug Install)

```powershell
.\gradlew.bat installDebug
```

This command builds and installs the app on the currently connected emulator/device.
