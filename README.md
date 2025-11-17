# Android Security App

Enterprise-grade Android security application with Firebase integration.

## Features

- Firebase Authentication (Email/Password)
- Firestore Database with Security Rules
- Cloud Storage with User Isolation
- Firebase Cloud Messaging
- Crashlytics for Monitoring
- Device Security Monitoring
- Biometric Authentication Ready
- ProGuard Obfuscation
- Offline Persistence
- Jetpack Compose UI

## Prerequisites

- Java 17
- Android SDK
- Firebase Project

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/karvifi/security-android.git
   cd android-security
   ```

2. Create Firebase project at https://console.firebase.google.com/

3. Enable services:
   - Authentication
   - Firestore
   - Storage
   - Cloud Messaging
   - Crashlytics

4. Download `google-services.json` and place in `app/` directory

5. Configure Firebase CLI:
   ```bash
   firebase login
   firebase use <your-project-id>
   firebase deploy --only firestore:rules,storage
   ```

## Build

### Local Build
```bash
# Windows
scripts\deploy.bat

# Or manually
./gradlew clean
./gradlew assembleDebug assembleRelease
```

### CI/CD
GitHub Actions workflow automatically builds on push/PR.

## Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Lint
```bash
./gradlew lint
```

## Security Analysis

This app incorporates security best practices and is designed for analysis using tools from:

- [MobSF](https://github.com/MobSF/Mobile-Security-Framework-MobSF)
- [TrustDevice](https://github.com/trustdecision/trustdevice-android)
- [RMS](https://github.com/m0bilesecurity/RMS-Runtime-Mobile-Security)
- And other security frameworks listed in the session.

## Deployment

1. Build release APK
2. Sign with keystore
3. Upload to Google Play Store

## Architecture

- **MVVM** with Jetpack Compose
- **Firebase** for backend
- **Kotlin Coroutines** for async
- **Security-focused** permissions and rules

## Contributing

1. Fork the repo
2. Create feature branch
3. Add tests
4. Submit PR

## License

MIT License