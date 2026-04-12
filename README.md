# WipeOut
Screen locker app from network.

WipeOut is an Android application that protects your phone against unauthorised access after it is lost or stolen. When the owner sends an SMS containing their secret code to the lost device, WipeOut immediately locks the screen.

## Features

| Feature | Details |
|---|---|
| **Secret-code generation** | A cryptographically-random 8-character code is created on first launch using `SecureRandom`. |
| **SMS-triggered lock** | `SmsReceiver` monitors incoming messages; when the secret code is found the screen is locked instantly via `DevicePolicyManager.lockNow()`. |
| **Device Administrator** | The app requests Device Admin privileges so it can lock the screen without any user interaction. |
| **Airplane-mode alert** | `AirplaneModeReceiver` detects flight mode and shows a high-priority notification warning that SMS-based locking is temporarily unavailable. |
| **Boot persistence** | `BootReceiver` ensures all protections are active as soon as the device restarts. |
| **Internet permission** | Declared for potential future remote-command features. |

## How it works

1. **First launch** – `SetupActivity` generates the secret code and guides the user through granting SMS, notification, and device-admin permissions.
2. **Normal use** – `MainActivity` shows the current protection status and lets the user view or regenerate their secret code.
3. **Lost phone** – The owner sends an SMS containing the secret code from any device. `SmsReceiver` matches the code and calls `LockManager.lockDevice()`.

## Project structure

```
app/src/main/
├── java/com/wipeout/
│   ├── MainActivity.kt            – Home screen; shows status and code management
│   ├── SetupActivity.kt           – First-run wizard
│   ├── SecretCodeManager.kt       – Generates / stores / validates the secret code
│   ├── LockManager.kt             – Locks screen; sends notifications on failure
│   ├── SmsReceiver.kt             – BroadcastReceiver for incoming SMS
│   ├── AirplaneModeReceiver.kt    – BroadcastReceiver for airplane mode changes
│   ├── BootReceiver.kt            – BroadcastReceiver for device boot
│   └── WipeOutDeviceAdminReceiver.kt – Device-admin receiver
├── res/
│   ├── layout/
│   │   ├── activity_setup.xml
│   │   └── activity_main.xml
│   ├── values/strings.xml
│   └── xml/device_admin.xml       – Declares force-lock device-admin policy
└── AndroidManifest.xml
```

## Build requirements

- Android Studio Hedgehog (or later) **or** Gradle 8.4 via command line
- Android SDK platform 34
- JDK 17
- `minSdk 26` (Android 8.0+)

```bash
./gradlew assembleDebug
```
