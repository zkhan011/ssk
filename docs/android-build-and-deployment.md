# Android kiosk build and deployment
The `android/` module is a native Kotlin WebView host for the same published browser kiosk and appearance API. It uses Android Gradle Plugin 8.7.3, Kotlin 2.0.21, JDK 17, `minSdk 31`, `compileSdk 35`, and `targetSdk 35`.

## Commands
```bash
cd android
gradle clean
gradle :app:assembleDebug
gradle :app:assembleRelease
gradle :app:bundleRelease
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.ssk.kiosk.debug/com.ssk.kiosk.MainActivity
adb logcat | grep ssk
```
Set `ANDROID_KIOSK_URL` to an HTTPS kiosk URL and set a unique `ANDROID_KIOSK_ID` for each kiosk build before building. The app safely appends that identity as the `kioskId` URL parameter, which provisions the browser client and its `X-Kiosk-Id` API header. Debug permits the build flag for development configuration only; the manifest and network security policy disable cleartext by default. Release signing must be added through untracked Gradle properties/environment variables. Device-owner lock-task mode and reboot auto-start require MDM/device-owner provisioning and are deliberately not bypassed by this application.
