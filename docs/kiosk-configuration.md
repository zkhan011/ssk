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

## Camera fallback

When Tasreeh verification succeeds but Pangu cannot register the image supplied by the integration workflow, the kiosk automatically opens the face-capture screen, starts the front camera, captures a JPEG, and retries Pangu registration. There is no extra in-application permission question. Browsers control camera permission themselves, so administrators must pre-grant camera access for the kiosk origin using managed-browser/OS policy; web code cannot bypass a browser permission prompt. The Android WebView grants video capture only to the configured kiosk origin, and the Android `CAMERA` runtime permission must be pre-granted by MDM/device-owner policy.
