@echo off

echo 🔨 Starting Android Security Build...

call .\gradlew clean

echo 🏗️ Building...

call .\gradlew assembleDebug assembleRelease

echo 🧪 Testing...

call .\gradlew test

echo 🔍 Linting...

call .\gradlew lint

echo 📱 Creating release builds...

dir /b app\build\outputs\apk\*

echo ✅ Build complete!

echo Debug APK: app\build\outputs\apk\debug\app-debug.apk

echo Release APK: app\build\outputs\apk\release\app-release.apk