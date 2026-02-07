# Architecture Overview

## App Entry + Dependency Wiring
- `MainActivity` owns Compose navigation, bottom bar, and screen wiring.
- `AppContainer` builds the Room database, repositories, config provider, template importer, Firestore syncer, and schedules tracker work.

## Data Layer (`app/src/main/java/.../data`)
- `local/`
  - `AppDatabase`, migrations, and enum converters.
  - `entity/`: Room entities + relations for issuers, cards, templates, benefits, offers, trackers, and notifications.
  - `dao/`: Room DAO interfaces (pure queries).
  - `repository/`: `CardRepository` as the primary data access + orchestration layer (CRUD, undo snapshots, template import, tracker/notification updates).
- `remote/`: `FirestoreSyncer` for syncing template issuer/card catalogs from Firestore.
- `worker/`: `TrackerRefreshWorker` + `TrackerWorkScheduler` to keep tracker periods up to date.

## Configuration & Templates (`config/`, `template/`)
- `config/`: `CardConfigLoader` reads `app/src/main/assets/card_config.json`, `CardConfigParser` validates/parses into `CardConfigModels`, and `CardConfigProvider` exposes it.
- `template/`: `CardTemplateImporter` maps template cards/benefits into user `ProfileCard` data in Room.

## Notifications (`notifications/`)
- `NotificationHelper` + `NotificationReceiver` display tracker reminders.
- `TrackerReminderScheduler` schedules `AlarmManager` reminders; `BootReceiver` reschedules after reboot.
- `NotificationScheduleEntity` in the data layer stores reminder metadata.

## UI Layer (`ui/`)
- Feature packages: `cardlist`, `carddetail` (components + tabs), `cardcreate`, `benefitcreate`, `offercreate`, `tracker`.
- ViewModels manage feature state and actions; Compose screens render state and emit events.
- `ui/theme` holds the shared Material 3 theme.

## Resources & Docs
- `app/src/main/res` for strings, icons, and other Android resources.
- `docs/` holds product, architecture, feature, and schema references.
