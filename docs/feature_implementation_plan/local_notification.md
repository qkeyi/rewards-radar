# Local Notification

## Tracker
1) Notification Manager and Channel: Create a `NotificationHelper` to initialize a "Tracker Reminders" notification channel with appropriate importance. Implement `showNotification` taking title, body, and a target `PendingIntent` for navigation.
2) Broadcast Receiver: Create `NotificationReceiver` to handle `AlarmManager` intents. It should extract tracker IDs, fetch tracker/card details from the database, ignore non-active trackers (completed/expired), and clear any remaining reminder schedules for inactive trackers.
3) WorkManager Integration: Update `TrackerRefreshWorker` to also handle notification scheduling. After generating or updating trackers:
   - Identify active trackers with an upcoming `endDateUtc`.
   - Use `AlarmManager.setExactAndAllowWhileIdle` when permitted, otherwise fall back to an inexact alarm.
   - Persist scheduled alarm IDs or timestamps in a new notification scheduling table to avoid redundant alarms.
4) Reminder List UI: Replace the toggle with a reminders list in `TrackerEditScreen`. Users can add multiple reminders (1-7 days) via a plus icon and remove reminders via a trash icon per list item. Do not preselect any reminder option, and prevent duplicate reminder offsets.
5) Schedule Timing: Reminders fire at 12:00 am local time on the chosen day offset. Example: if a tracker ends on 06/30 and reminder is 1 day before, fire on 06/29 at 12:00 am.
6) Boot Receiver: Implement a `BootReceiver` to reschedule all active tracker alarms when the device restarts, as `AlarmManager` alarms are cleared on reboot.
7) Permissions: Handle `POST_NOTIFICATIONS` (Android 13+) and `SCHEDULE_EXACT_ALARM` (Android 12+) permission requests in the UI (e.g., when adding a reminder).
8) Deep Linking: Ensure the notification `PendingIntent` navigates directly to the specific `TrackerEditScreen` by `trackerId`.

## Card
- [ ] anniversary
- [ ]
