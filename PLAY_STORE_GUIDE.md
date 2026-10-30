# Google Play Console Submission & Release Guide: Andro-Assist

This guide provides exact step-by-step instructions and questionnaire answers for publishing **Andro-Assist** on the Google Play Store under the **Families & Digital Wellbeing** category.

---

## 1. Generating Release Signing Key & Building App Bundle

### Generate Keystore
Run the following command to generate a release key:
```bash
keytool -genkey -v -keystore release.keystore \
    -alias androassist \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000
```

### Build Android App Bundle (.aab)
```bash
./gradlew bundleRelease
```
*Output artifact located at:*  
`app/build/outputs/bundle/release/app-release.aab`

---

## 2. Google Play Console Questionnaire Declarations

### A. Target Audience & Content
- **Target Age Groups**: Select **Ages 9-12**, **Ages 13-17**, and **Parents**.
- **Designed for Families**: Select **Yes**.
- **Ads Declaration**: Select **No, my app does not contain ads**.

### B. Data Safety Form
| Question | Answer | Details |
| :--- | :---: | :--- |
| Does your app collect or share user data? | **Yes** | App activity data only. |
| Is data encrypted in transit? | **Yes** | All network traffic uses HTTPS/TLS. |
| Do you provide a way for users to request that their data is deleted? | **Yes** | Deleting app or resetting role wipes all local data. |
| Data Type: **App Info and Performance** | **App Activity** | Usage stats collected solely for screen time quota tracking. |
| Is this data shared with third parties? | **No** | Stored locally or private family cluster only. |

### C. Foreground Service (FGS) Special Use Declaration
Google Play requires declaring why `FOREGROUND_SERVICE_SPECIAL_USE` is used. Copy and paste the exact text below:

> **FGS Purpose Description:**  
> *"Andro-Assist is a transparent parental wellbeing app. The foreground service displays an ongoing, non-dismissible notification informing the child that screen time protection is actively enforcing daily quotas and bedtime schedules, fully complying with Google Play's Anti-Stalkerware transparency policy."*

---

## 3. Mandatory External Links for Console
- **Privacy Policy URL**: Host `PRIVACY_POLICY.md` on GitHub Pages (e.g. `https://tabrezahmed51.github.io/Andro-Assist/PRIVACY_POLICY.html`).
- **Support Email**: Enter your contact email in the Store Settings.

---

## 4. Pre-Launch Checklist
- [x] Verified `PACKAGE_USAGE_STATS` prominent disclosure modal in app
- [x] Tested hardcoded Emergency SOS bypass dialer
- [x] Verified persistent ongoing notification on Android 14+
- [x] Confirmed zero background audio/video recording components
- [x] Adaptive vector icons configured in `mipmap-anydpi-v26`
