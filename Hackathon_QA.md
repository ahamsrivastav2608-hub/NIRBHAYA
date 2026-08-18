# Nirbhaya App - Hackathon Presentation Q&A

This guide prepares you for common technical and conceptual questions during your hackathon demo.

---

## Technical Implementation

**Q: How do you ensure the app triggers an SOS even when it's closed?**
**A:** We implemented a `ShakeTriggerService`, which is an Android **Foreground Service**. Unlike standard background tasks, a Foreground Service is granted higher priority by the system and can continue to access sensors like the accelerometer even if the main UI is killed. It displays a persistent notification to comply with Android's power management and privacy policies.

**Q: Does constant shake monitoring drain the battery?**
**A:** We optimized battery consumption by using `SensorManager.SENSOR_DELAY_NORMAL`. This sampling rate is sufficient for detecting a vigorous emergency shake while minimizing CPU wake-ups. Additionally, the service only runs if the user explicitly enables "Shake Detection" in the settings.

**Q: How do you handle video recording without the app being in the foreground?**
**A:** We use **CameraX** integrated into a Lifecycle-aware Foreground Service (`EmergencyService`). By binding the camera lifecycle to the service, we can capture evidence while the user is on the lock screen or the app is minimized. We follow Android's privacy guidelines by showing a foreground notification and respecting system camera indicators.

**Q: How do you ensure data persistence?**
**A:** We use **Jetpack DataStore** (with Protocol Buffers/Serialization) for reliable, asynchronous storage of contacts, settings, and incident history. This is more robust than SharedPreferences and provides a reactive `Flow`-based API that updates the UI automatically when data changes.

---

## Security & Privacy

**Q: Is the location data shared with third parties?**
**A:** No. The app only shares location with the **Emergency Contacts** that the user has personally configured. Data is sent via standard SMS and intended WhatsApp redirects, ensuring the user remains in control of their information.

**Q: Where is the video evidence stored?**
**A:** Evidence is stored in the app's secure internal storage or dedicated media folders (scoped storage). It is not uploaded to any cloud server by default, ensuring that sensitive evidence remains on the device until the user decides to share it.

---

## Use Case & Impact

**Q: Why a 3-second countdown after tapping SOS?**
**A:** Transitioning from a "hold" to a "tap" makes it faster to trigger in a panic, but increases the risk of accidental activation. The 3-second countdown acts as a "Fail-Safe" window, giving the user a chance to cancel if the button was tapped by mistake.

**Q: How does the "Anti-Ragging" feature differ from the SOS?**
**A:** The SOS is for immediate physical danger. The Anti-Ragging feature is for reporting misconduct anonymously. It generates a unique tracking ID and allows for a detailed description, intended for administrative follow-up rather than immediate emergency response.

---

## Future Scope

**Q: What would you add if you had more time?**
**A:**
1.  **AI Voice Trigger:** Triggering SOS via a specific keyword (e.g., "Help Nirbhaya").
2.  **Cloud Backup:** Encrypted upload of evidence to a secure server in case the phone is destroyed.
3.  **Low Data Mode:** Sending location via SMS even when internet connectivity is zero.
4.  **Wear OS Integration:** A companion app for smartwatches for even faster access.
