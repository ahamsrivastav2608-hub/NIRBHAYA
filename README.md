Nirbhaya — Personal Safety Android App
Full source for the Nirbhaya SIH project: Kotlin + Jetpack Compose, MVVM, CameraX, Fused Location Provider, DataStore, Foreground Services.
How to open this project
1.Open Android Studio (Koala/2024.1 or newer recommended).
2.File > Open → select this Nirbhaya folder (the one containing settings.gradle.kts).
3.Let Gradle sync — it will download the dependencies listed in gradle/libs.versions.toml.
4.Run on a device or emulator with API 26+ (minSdk 26). A real device is strongly recommended for testing camera, SMS, and shake detection — emulators can't send real SMS or shake.
What's implemented
Area
File(s)
UI / screens
ui/screens/*.kt, ui/navigation/NirbhayaNavHost.kt
Business logic
logic/EmergencyTriggerManager.kt, logic/*Helper.kt
AndroidManifest
app/src/main/AndroidManifest.xml
Permissions
Declared in manifest; requested at runtime in MainActivity.kt
Services
services/EmergencyService.kt, services/ShakeTriggerService.kt, services/BootReceiver.kt
Storage
data/NirbhayaRepository.kt (Jetpack DataStore + kotlinx.serialization)
External APIs
Fused Location Provider (logic/LocationHelper.kt), Google Maps deep links
Build system
build.gradle.kts, app/build.gradle.kts, gradle/libs.versions.toml
Resources
res/values/*.xml, res/xml/*.xml, res/drawable/*.xml
Known gaps to finish before the demo
These are placeholders you should replace/verify — call them out honestly if judges ask, it shows you understand the codebase rather than just having AI generate it blindly:
•Launcher icon: currently a simple vector shield placeholder — swap in a real designed icon (res/mipmap-anydpi-v26/, res/drawable/ic_launcher_*.xml).
•4-tap power button trigger: mentioned in the settings screen but not yet wired to a real listener — implementing this reliably needs an Accessibility Service (power button key events aren't broadcast to normal apps on modern Android). Ask me and I'll build that next.
•Permission UX: MainActivity requests all permissions at once on first launch. For a polished demo, consider requesting them contextually (e.g., ask for SMS permission right before adding a contact) with rationale dialogs — Android review guidelines increasingly expect this.
•CameraX on Android 10+ locked-screen recording: works, but you should test on your actual target device — behavior varies by OEM (Xiaomi/Samsung battery optimization can kill foreground services; you may need to guide users to disable battery optimization for the app).
•Encryption: architecture doc mentions "encrypted video/audio recording." Current code saves plain .mp4 files to app-private scoped storage (already inaccessible to other apps). If you need actual at-rest encryption, I can add Jetpack Security's EncryptedFile wrapper.
Explaining this to SIH judges
Every file here maps directly to a module in your architecture doc (EmergencyTriggerManager = "Singleton Brain", EmergencyService = "Persistent Heart", ShakeTriggerService = "Background Listener"). Walk judges through EmergencyTriggerManager.kt first — it's the state machine that ties the whole app together, and it's the easiest place to demonstrate you understand why the architecture is built this way, not just that it compiles.
