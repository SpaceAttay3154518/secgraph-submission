import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.secgraph.app',
  appName: 'SecGraph',
  webDir: 'dist',
  android: {
    allowMixedContent: true,
    backgroundColor: '#0f1115'
  },
  server: {
    cleartext: true
  }
};

export default config;
