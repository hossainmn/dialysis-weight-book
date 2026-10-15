# Daftarche Dializ - Paper style (Persian)

This folder wraps the HTML prototype into an Android APK with Capacitor.

## 1. Preview (already done)
Open `W:\ai\dialysis-paper-logbook-v2.html` in Chrome.
Same file copied to `www/index.html` for the app.

## 2. Install + add Android (run once)
```
cd W:\ai\paper-dialysis-app
"C:\Program Files\nodejs\npm.cmd" install
"C:\Program Files\nodejs\npx.cmd" cap add android
"C:\Program Files\nodejs\npx.cmd" cap sync
```

## 3. Build APK
- Open `android/` in Android Studio
- Build > Build APK (debug) for testing
- For release: Build > Generate Signed Bundle/APK

## Notes
- App is offline, RTL Persian, uses localStorage.
- No server needed.
- To update app UI after editing HTML:
  Copy-Item W:\ai\dialysis-paper-logbook-v2.html -> www\index.html, then `npx cap sync`

## Credits
- Launcher icon: "nephrology" glyph from Material Symbols by Google,
  Apache License 2.0 (https://github.com/google/material-design-icons/blob/master/LICENSE).
- Font: Vazirmatn (https://github.com/rastikerdar/vazirmatn), OFL 1.1,
  bundled locally via @fontsource/vazirmatn.
