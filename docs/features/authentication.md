# Authentication
- [ ] Google account login (FirebaseUI Compose, Google provider only)
  - Launch: check existing auth session; if signed in, go to Cards, otherwise show LoginScreen.
  - Login screen UI: FirebaseUI Auth screen with Google sign-in; errors surfaced via snackbar.
  - Sign-in flow: Credential Manager Google ID token -> Firebase Auth (handled by FirebaseUI).
  - Failure handling: user cancel returns to idle; auth/network errors show message and retry.
  - Post-login sync: deferred.
