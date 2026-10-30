# Privacy Policy for Andro-Assist (Spiandro)

**Effective Date:** October 30, 2026  
**Last Updated:** October 30, 2026

## 1. Overview & Commitment to Child Safety
Andro-Assist ("we", "our", or "the App") is a digital wellbeing and parental screen time management platform. We are deeply committed to protecting the privacy of children and families. Andro-Assist operates under a strict **Zero-Surveillance Architecture**: we believe effective digital guidance requires healthy boundary enforcement, not intrusive spying.

## 2. Information We Access & How We Use It
Andro-Assist accesses strictly limited device information essential for screen time calculations:

- **App Foreground Usage Data (`PACKAGE_USAGE_STATS`)**: The App queries Android's native `UsageStatsManager` to aggregate daily minutes spent across application packages. This data is used solely to calculate remaining quotas and display top-used apps on the parental dashboard.
- **Boot Completion (`RECEIVE_BOOT_COMPLETED`)**: Used solely to restart the transparent foreground notification service when the child device reboots.
- **Device Administration (`BIND_DEVICE_ADMIN`)**: Used exclusively to prevent uninstallation without the parent master security PIN.
- **Family Pairing Tokens**: Anonymous, randomly generated 6-digit tokens (`PAIR-XXXXXX`) and random UUIDs used to bind parent and child devices without requiring email addresses or phone numbers.

## 3. What We NEVER Collect (Zero-Surveillance Guarantee)
Andro-Assist does **NOT**:
- Access, stream, or record device cameras.
- Access, stream, or record microphone audio.
- Record or capture device screen pixels or screenshots.
- Log keystrokes or text inputs.
- Read SMS messages, call logs, or private messaging contents.
- Track precise GPS location or track physical movements.
- Sell or monetize any user or device data.

## 4. Google Play Families & Anti-Stalkerware Compliance
- **Transparent Operation**: Whenever Andro-Assist is active on a child device, a non-dismissible persistent notification is permanently visible in the notification shade stating that screen time is being monitored.
- **Prominent Disclosure**: Before requesting any system permission, the App presents an explicit disclosure modal detailing what is accessed and why.
- **Emergency SOS Guarantee**: The telephone dialer is hardcoded to bypass all locks, ensuring children can always reach parents or emergency services.

## 5. Data Retention & Security
All usage statistics and schedule configurations are stored locally on the device using Android's private app sandbox. Remote sync payloads consist solely of numerical minutes and schedule hours transmitted over encrypted TLS connections.

## 6. Parental Rights & Controls
Parents retain complete control over device policies. A parent can reset roles, revoke permissions, or wipe local application data at any time via Android Application Settings.

## 7. Contact Us
For questions regarding this Privacy Policy or our child data safety practices, contact the maintainer at `support@androassist.app` or via our GitHub repository issues.
