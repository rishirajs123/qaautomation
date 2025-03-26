# Android QA Automation Test App

A comprehensive Android application designed for testing various device capabilities commonly used in Appium automation testing. This app serves as a diagnostic tool for cloud testing platforms like LambdaTest and BrowserStack.

## Quick Start Guide for Beginners

### Prerequisites
- Java JDK 17 or higher (Amazon Corretto 17.0.13 recommended)
- Android Studio (latest version, Hedgehog or newer)
- Android SDK with API level 34 installed (compile SDK)
- Gradle 8.13 (included in the project)
- Git (for cloning the repository)
- ADB (Android Debug Bridge) installed and in your PATH
- An Android device or emulator (minimum API level 26, Android 8.0)

### Build Environment Details
- Gradle: 8.13
- Kotlin: 1.9.20
- Java: 17.0.13 JDK (Amazon Corretto)
- compileSdk: 34
- targetSdk: 34 
- minSdk: 26

### Setting Up the Environment

#### Install Java using SDKMan:
```bash
# Install sdkman if not already installed
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install and use the required Java version
sdk install java 17.0.13-amzn
sdk use java 17.0.13-amzn
```

#### Install Android Studio and Android SDK:
1. Download Android Studio from [developer.android.com](https://developer.android.com/studio)
2. Install Android Studio following the installation wizard
3. Open Android Studio and go to SDK Manager
4. Install Android SDK for API level 34
5. Install Android SDK Command-line Tools
6. Install Android SDK Build-Tools

### Building and Installing the App

1. **Clone the repository (if you haven't already):**
   ```bash
   git clone https://github.com/yourusername/app.git
   cd app
   ```

2. **Verify your environment:**
   ```bash
   ./gradlew --version  # Should show Gradle 8.13
   java -version  # Should show Java 17
   ```

3. **Clean and build the app:**
   ```bash
   ./gradlew clean
   ./gradlew :app:assembleDebug :app:assembleDebugAndroidTest
   ```
   
   The APKs will be generated at:
   - Main app: `app/build/outputs/apk/debug/app-debug.apk`
   - Test app: `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`

4. **Check if your device is connected:**
   ```bash
   adb devices
   ```
   Make sure your device appears in the list and is in "device" state, not "unauthorized".

5. **Enable Developer Options on your Android device:**
   - Go to Settings > About phone
   - Tap "Build Number" 7 times
   - Go back to Settings > System > Developer options
   - Enable USB debugging

6. **Uninstall previous versions (if necessary):**
   ```bash
   adb uninstall com.example.qaautomation
   adb uninstall com.example.qaautomation.test
   ```

7. **Install the app and test APKs:**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   adb install -r -t app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
   ```

### Running the App

- Simply tap on the app icon on your device to launch it
- The main screen displays different test features like GPS Location, IP Geolocation, Network Logs, and Browser
- Allow any permission requests that appear for full functionality

### Running Tests

To run automated tests using the HiltTestRunner:

1. **Run basic app tests:**
   ```bash
   adb shell am instrument -w -e class 'com.example.qaautomation.BasicAppTest' com.example.qaautomation.test/com.example.qaautomation.HiltTestRunner
   ```

2. **Run browser tests (with LambdaTest integration):**
   ```bash
   adb shell am instrument -w -e class 'com.example.qaautomation.BrowserTest' com.example.qaautomation.test/com.example.qaautomation.HiltTestRunner
   ```

3. **Run GPS location tests:**
   ```bash
   adb shell am instrument -w -e class 'com.example.qaautomation.GpsLocationTest' com.example.qaautomation.test/com.example.qaautomation.HiltTestRunner
   ```

4. **View test logs:**
   ```bash
   adb logcat -d | grep LambdaTest  # For LambdaTest screenshot logs
   adb logcat -d | grep HiltTestRunner  # For test runner logs
   ```

5. **Run all tests at once:**
   ```bash
   adb shell am instrument -w com.example.qaautomation.test/com.example.qaautomation.HiltTestRunner
   ```

### All-in-One Build and Test Commands

To build, install, and run tests in one go:

```bash
# Build and install everything
sdk use java 17.0.13-amzn && \
./gradlew clean && \
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest && \
adb uninstall com.example.qaautomation || echo "App not installed" && \
adb uninstall com.example.qaautomation.test || echo "Test app not installed" && \
adb install -r app/build/outputs/apk/debug/app-debug.apk && \
adb install -r -t app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

# Run specific test
adb shell am instrument -w -e class 'com.example.qaautomation.BasicAppTest' com.example.qaautomation.test/com.example.qaautomation.HiltTestRunner
```

### Troubleshooting

1. **Permission Issues:**
   - The HiltTestRunner will automatically try to grant location permissions
   - If you see permission dialog boxes, accept them
   - For manual permission granting: `adb shell pm grant com.example.qaautomation android.permission.ACCESS_FINE_LOCATION`

2. **Build Errors:**
   - Make sure you're using Java 17: `sdk use java 17.0.13-amzn`
   - Check if all dependencies are resolved: `./gradlew app:dependencies`
   - Try with the `--stacktrace` flag: `./gradlew clean --stacktrace`

3. **Test Failures:**
   - Verify device is connected to the internet
   - Ensure location services are enabled
   - Check that the app has all necessary permissions: Settings > Apps > QA Automation > Permissions

4. **Installation Issues:**
   - If installation fails with "INSTALL_FAILED_TEST_ONLY", add the -t flag:
     ```bash
     adb install -r -t app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
     ```
   - If you see signature conflicts: `adb uninstall com.example.qaautomation` before installing

5. **Device Connection Issues:**
   - Try restarting ADB: `adb kill-server && adb start-server`
   - Disconnect and reconnect the USB cable
   - Try a different USB port or cable


## License

This project is licensed under the MIT License - see the LICENSE file for details. 