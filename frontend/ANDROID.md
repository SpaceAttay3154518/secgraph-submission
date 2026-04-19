# SecGraph Android

This frontend is wrapped as an Android app with Capacitor.

## Emulator

Use `10.0.2.2` to reach the backend running on your computer:

```bash
npm run android:sync
npm run android:open
```

Then run the app from Android Studio.

## Real Android Device

Use your computer's LAN IP address instead of `localhost`:

```bash
VITE_API_URL=http://YOUR_COMPUTER_IP:8080/api npm run android:sync
npm run android:open
```

Use your own computer IP when you build for a phone:

```bash
VITE_API_URL=http://192.168.1.100:8080/api npm run android:open:phone
```

The phone and backend machine must be on the same network, and the backend must accept connections from the phone.

## Build From Android Studio

Open `frontend/android`, then build or run the `app` configuration.

For production, use an HTTPS API URL and remove the development cleartext settings from `capacitor.config.ts` and `android/app/src/main/AndroidManifest.xml`.
