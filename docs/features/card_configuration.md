# Card Configuration
- Split config by issuer (e.g., `chase.json`, `citi.json`), still validated/merged to a single in-app catalog at load time.
- On first launch, download configs and cache locally; allow users to edit local configs.
- Add a "Sync" button to pull remote configs and override local configs only (user-added cards remain untouched).
- Provide schema validation across merged configs and a "last known good" fallback to avoid corrupting the catalog.
