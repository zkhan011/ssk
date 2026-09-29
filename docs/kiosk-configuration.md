# Kiosk Configuration

Configure inactivity timeout, languages, gates, visitor categories, visit purposes, and pass validity from seeded database records and future admin configuration screens. Kiosk screens are optimized for 1920x1080 touch displays.

## Kiosk identity provisioning

Each kiosk resolves a stable identity and sends it to the server in the `X-Kiosk-Id` header. The gate-pass verification payload also includes the identity so integration workflow templates can use `{{context.kioskId}}`.

Provision browser kiosks by opening the shared application URL once with a `kioskId` query parameter, for example `https://kiosk.example.com/kiosk?kioskId=GATE-01`. The value is stored in that browser's local storage and remains in use after the query parameter is removed. A later valid query parameter replaces the stored identity. IDs may contain letters, digits, periods, underscores, and hyphens, and must be at most 64 characters.

For a common deployment default, set `VITE_KIOSK_ID` when building the frontend. If neither a URL identity nor a build default exists, the application generates and persists a unique `WEB-<uuid>` identity.

For Android kiosks, set both values before building:

```bash
export ANDROID_KIOSK_URL=https://kiosk.example.com/kiosk
export ANDROID_KIOSK_ID=GATE-01
cd android && gradle :app:assembleRelease
```

The Android host app appends its configured identity to the shared URL; the web application then persists and reports it in the same way as a browser kiosk.
