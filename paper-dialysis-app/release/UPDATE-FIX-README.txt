UPDATE FIX - "App not installed / package conflicts" (v1.5 debug -> v1.6 release)
================================================================================
WHY: Android only allows an update if package name AND signing key match.
- Old files (updates/*.apk, new-updates/*.apk, app-debug.apk): versionCode 6, signed "CN=Android Debug"
  cert SHA-256 94f99dd9252b36711206b0e9e32990074aaf451ec5378967e840b2f04c2bcc58
- New files (release/daftarche-dializ-v1.6.apk/.aab): versionCode 7, signed "CN=Daftarche Dializ"
  cert SHA-256 9b4107a1c7e2df390cd43fd18cca49215b79f8c92944d948b0a722a1b38c539f
Different key = Android blocks install. This is phone security working correctly. No code bug.
There is NO way to update without uninstall this one time. After this move, all future
release-key updates install normally with no data loss.

DO THIS ORDER - BACKUP FIRST (uninstall erases the logbook)
1. Open the OLD app on the phone. Tap file/menu > Export (استخراج).
   Share sheet appears > Save to Downloads (or send to yourself on WhatsApp).
   File name looks like: dialysis-backup-1404-07-20.dializ
2. Verify backup: file size > 0, and remember the record count shown in the app.
3. Uninstall the OLD app (long-press icon > Uninstall). If Android asks about data, confirm.
4. Install release/daftarche-dializ-v1.6.apk (tap the file > Install).
   If phone warns "Install unknown apps", allow it once for Files/WhatsApp.
5. Open the NEW blue-icon app. In-app: file/menu > Import > pick the .dializ file.
   Or tap the .dializ file in Downloads/WhatsApp > Open with Daftarche Dializ.
   Confirm dialog shows "new: N records" > tap Add (اضافه کن).
6. Check count matches step 2. Done. Keep the .dializ file as extra backup.

FOR YOU (developer)
- Never distribute debug APKs again. Only share release/daftarche-dializ-v*.apk (release key).
- All future builds MUST use android/dialysis-release.keystore (passwords in
  android/keystore-passwords-BACKUP.txt). Same key = updates work.
- Pick ONE channel per phone: Play Store OR sideload. A phone with the Play version
  cannot sideload-overwrite with the same file if Play App Signing changed the final cert.
- If a user skipped backup and already uninstalled: data is gone (private storage,
  allowBackup=false). No recovery. This is why step 1 comes first.
