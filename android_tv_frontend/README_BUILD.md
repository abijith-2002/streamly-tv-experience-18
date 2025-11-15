# Streamly Android TV - CI/Build Notes

This project is configured to build reliably in CI/Docker with the following measures:
- Gradle wrapper pinned to 8.7-bin with SHA-256 checksum and mirror fallback.
- Network timeouts and retry/backoff enabled in gradle.properties.
- AndroidX-only project with Jetifier enabled; Leanback updated to 1.1.0-rc01 to avoid legacy support artifacts.
- Resource processing:
  - Avoids PNG crunching in debug.
  - Provides tasks to clean intermediates when AAPT2 cache corruption is suspected.

Common CI commands:
- Clean and rebuild from scratch:
  ./gradlew --no-daemon deepClean :app:assembleDebug

- If resource merge fails with ".png.flat store error":
  ./gradlew --no-daemon :app:ciClean :app:assembleDebug

Ensure gradlew is executable:
- git respects .gitattributes; if needed locally:
  chmod +x gradlew
