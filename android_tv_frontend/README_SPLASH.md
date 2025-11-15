# Splash Screen

This app uses a TV-friendly themed splash screen:

- Background color: #121212 (tv_background)
- Theme: SplashTheme (inherits Theme.Leanback)
- Entry point: SplashActivity (launcher)
- Transition: Navigates to MainActivity after ~600ms
- No user interaction on splash

Files:
- app/src/main/java/com/android/streamly/SplashActivity.kt
- app/src/main/res/layout/activity_splash.xml
- app/src/main/res/values/styles.xml (SplashTheme)
