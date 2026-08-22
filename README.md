# Emergency SOS Android App

An Android safety application designed to help a user quickly start an emergency response. The app brings together SOS activation, shake-gesture detection, location access, phone/SMS actions, and an emergency foreground service in one mobile experience.

> **Project status:** Hackathon prototype. Test all emergency flows carefully before relying on the app in a real situation.

## Features

- Trigger an SOS emergency flow from the app
- Detect a shake gesture to activate an emergency action
- Access the device's current location for emergency assistance
- Start phone calls and send SMS alerts when permission is granted
- Use camera and microphone capabilities when required by the emergency flow
- Run the emergency workflow in a foreground service
- Restore the shake-detection service after a device restart when enabled
- Show notifications for active emergency services on Android 13+

## Tech Stack

- Kotlin
- Android SDK
- Android foreground services
- Android runtime permissions
- Device sensors (accelerometer)
- Google Maps / Google Play services 

## Requirements

- Android Studio (latest stable version recommended)
- Android SDK compatible with the project's Gradle configuration
- A physical Android device is recommended for testing sensors, calling, SMS, camera, microphone, and location
- A Google Maps API key if the app displays Google Maps

## Getting Started

1. Clone the repository.

   ```bash
   git clone https://github.com/ahamsrivastav2608-hub/NIRBHAYA.git
   ```

2. Open the project folder in Android Studio.
3. Allow Android Studio to sync the project dependencies.
4. If maps are used, add a valid Google Maps API key through your local configuration. Do not commit API keys to GitHub.
5. Run the app on a physical device.
6. Grant only the permissions needed for the feature you are testing.

## Permissions

The application may request the following permissions depending on the feature used:

| Permission | Why it is used |
| --- | --- |
| Location | Obtain the user's location during an SOS event. |
| Phone | Start an emergency phone call. |
| SMS | Send an emergency text message. |
| Camera and microphone | Support emergency capture or related features. |
| Notifications | Display foreground-service and emergency status notifications. |
| Boot completed | Restore enabled background monitoring after a restart. |

Users should be told clearly why each permission is needed before it is requested.

## Project Structure

```text
app/
├── MainActivity.kt              # Main app entry point and UI
├── logic/
│   ├── EmergencyService.kt       # Foreground emergency workflow
│   ├── ShakeTriggerService.kt    # Accelerometer-based shake detection
│   └── BootReceiver.kt           # Restarts enabled monitoring after boot
├── res/                          # Layouts, strings, icons, themes, and XML resources
└── AndroidManifest.xml           # Components and required permissions
```

## Safety and Privacy

- This project is a prototype and is not a replacement for local emergency services.
- Always test with safe, non-emergency contacts and clearly inform test recipients.
- Handle location, contacts, recordings, and API keys securely.
- Never hard-code secrets such as Maps API keys in source files or commits.

## Contributors

Add your team members here:

- AHAM SRIVASTAVA- BACKEND
- PARTH AGARWAL- FRONTEND
- PRANSHI - UI designer
- TRISHIR - PITCHING
- SHUB - PITCHING



