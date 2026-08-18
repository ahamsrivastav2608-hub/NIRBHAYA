# CampusGuard — Hackathon Android Prototype

Features included:
- Large emergency SOS
- SOS demo trigger representing 4 rapid power-button presses
- Warden calling
- Optional police calling switch in Settings
- Live location via Android LocationManager
- Anonymous anti-ragging reporting with anonymous report ID
- Dark, presentation-friendly UI

## Important Android limitation
A normal Android application cannot universally intercept the physical power button four times across all phones. OEM/OS emergency-key behavior varies. For a real deployment, the 4× power-button trigger should be implemented using supported device/OEM emergency APIs or an approved accessibility/device-management design. The included "SIMULATE 4× POWER PRESS" button is for the hackathon demo.

## Build
Open this folder in Android Studio and let Gradle sync. Then:
Build > Build Bundle(s) / APK(s) > Build APK(s)

The app uses only AndroidX AppCompat/Core and standard Android APIs.
