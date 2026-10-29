 Here is the complete engineering and product upgrade roadplan to transition Andro-Assist from its current local
  MVP to a production-ready, multi-device platform eligible for Google Play Store distribution.
  ──────
  │ Diagram exceeds terminal width (277 > 117 cols)
  │ Displayed as code block. Widen terminal to view inline.

    flowchart TD
        P1["Phase 1: Cloud Sync & Pairing<br/>(FCM + Firestore + QR/PIN Pairing)"] --> P2["Phase 2: Enforcement &
        Lock Overlays<br/>(Bedtime + Quota Block Screen)"]
        P2 --> P3["Phase 3: Anti-Tamper & Device Admin<br/>(Uninstall Protection + Safety Guard)"]
        P3 --> P4["Phase 4: Positive Habits & Rewards<br/>(Chore Requests + Eye-Strain Prompts)"]
        P4 --> P5["Phase 5: Play Store Submission<br/>(Families Policy + Prominent Disclosures)"]
  ──────
  ### Phase 1: Real-Time Multi-Device Pairing & Cloud Sync

  Objective: Enable a parent device to manage multiple child devices remotely via lightweight policy messages.

   Feature / Task         │ Technical Implementation                  │ Deliverable
  ────────────────────────┼───────────────────────────────────────────┼────────────────────────────────────────────
   Family Token Pairing   │ Anonymous Firebase Auth + 6-digit random  │ Zero-PII pairing workflow between parent
                          │ pairing code or QR code scan.             │ and child phones.
   Cloud Policy Sync      │ Firestore document schema:                │ Real-time synchronization of daily limits
                          │ families/{familyId}/devices/{deviceId}/po │ and instant pause.
                          │ licy.                                     │
   Instant Push Triggers  │ Firebase Cloud Messaging (FCM) high-      │ Near-instant (<2s) enforcement of "Instant
                          │ priority data payloads.                   │ Pause" without continuous polling.
   Usage Telemetry Upload │ Daily background sync worker              │ Parent dashboard displays up-to-date
                          │ (WorkManager) pushing total foreground    │ metrics from remote devices.
                          │ minutes per package.                      │
  ──────
  ### Phase 2: App Blocking & Scheduled Bedtime Enforcement

  Objective: Replace passive progress monitoring with active, non-intrusive boundary enforcement.

   Feature / Task       │ Technical Implementation                   │ Deliverable
  ──────────────────────┼────────────────────────────────────────────┼─────────────────────────────────────────────
   "Time's Up" Overlay  │ Full-screen activity or                    │ Friendly locking screen informing the child
                        │ SYSTEM_ALERT_WINDOW view when daily quota  │ that daily time has expired.
                        │ reaches 100%.                              │
   Emergency SOS Bypass │ Hardcoded package whitelist (Phone dialer, │ Child can always call parents or emergency
                        │ SMS, Emergency contacts).                  │ services, even when locked.
   Bedtime Downtime     │ AlarmManager exact triggers evaluating     │ Automatic device lock during sleep hours.
                        │ local device hours against active bedtime  │
                        │ windows (e.g., 9:00 PM – 7:00 AM).         │
   "Ask for More Time"  │ One-tap button on the lock screen sending  │ Parent can approve extra time with a single
                        │ an FCM notification to the parent device   │ tap from their notification drawer.
                        │ (+15m / +30m).                             │
  ──────
  ### Phase 3: Anti-Tamper & Device Administration

  Objective: Prevent children from bypassing limits or disabling permissions.

   Feature / Task       │ Technical Implementation                   │ Deliverable
  ──────────────────────┼────────────────────────────────────────────┼─────────────────────────────────────────────
   Uninstall Protection │ Implementation of standard Android         │ Deletion of the app is blocked unless the
                        │ DeviceAdminReceiver.                       │ parent security PIN is supplied.
   Permission Watcher   │ Broadcast receiver checking                │ Alerts parent if child manually revokes
                        │ PACKAGE_USAGE_STATS and notification       │ permissions in Android Settings.
                        │ status on boot and app resume.             │
   Restart Resilience   │ BOOT_COMPLETED receiver automatically      │ Protection remains active after device
                        │ restarting the persistent                  │ reboots.
                        │ ChildMonitorService.                       │
  ──────
  ### Phase 4: Positive Habits, Ergonomics & Rewards

  Objective: Make the app constructive and engaging rather than purely restrictive.

   Feature / Task            │ Technical Implementation                 │ Deliverable
  ───────────────────────────┼──────────────────────────────────────────┼──────────────────────────────────────────
   20-20-20 Eye Strain Alert │ Periodic timer during continuous screen  │ Encourages healthy posture and visual
                             │ use prompting a 20-second break.         │ ergonomics.
   Chore & Task Board        │ Parent assigns tasks (e.g., "Homework    │ Gamified positive reinforcement for
                             │ finished") that reward +20m screen time  │ healthy habits.
                             │ upon verification.                       │
   Category-Based Quotas     │ Group apps by Play Store category (Games │ Separate caps (e.g., max 45m for Games,
                             │ vs. Educational vs. Social).             │ unlimited for Reading).
   Weekly PDF / CSV Report   │ Room DB historical aggregation exported  │ Downloadable weekly usage summary for
                             │ via Android FileProvider.                │ family review.
  ──────
  ### Phase 5: Google Play Compliance & Store Release

  Objective: Satisfy all legal, privacy, and store listing criteria.

   Requirement           │ Action Items
  ───────────────────────┼─────────────────────────────────────────────────────────────────────────────────────────
   Families Policy Audit │ Verify that no ad SDKs collect identifier data and the target audience questionnaire is
                         │ accurately filled out.
   Prominent Disclosures │ In-app modal clearly stating: "Andro-Assist uses Usage Access to track screen time and
                         │ enforce bedtime schedules configured by your parent."
   FGS Declaration Video │ Submit a 30-second screen capture demonstrating why the persistent Foreground Service
                         │ notification is required.
   Data Safety Sheet     │ Declare that usage metrics are strictly transmitted over TLS and never sold or shared
                         │ with third parties.
  ──────
  ### Suggested Immediate Next Sprint

  Implement Phase 1 (Pairing & Cloud Policy Sync) using Firebase Firestore to allow the Parent view on Phone A to
  control the Child view on Phone B.
  ──────
  Skipped: Backend cloud provisioning and third-party Firebase dependencies have not been added to Gradle yet.
  Risk: Publishing parental controls on Google Play requires creating and hosting an external Privacy Policy URL.
