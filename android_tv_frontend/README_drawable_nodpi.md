# drawable-nodpi assets

This module uses nodpi bitmap assets for TV UI:
- avatar_placeholder.png
- hero_main.png
- poster_*.png
- hero_right_slice.png (not referenced by code currently; kept only if future designs require)

Notes:
- Keep filenames stable to avoid AAPT2 stale cache issues in CI.
- If a resource-merging cache error occurs (e.g., *.png.flat store error), run:
  ./gradlew --no-daemon :app:ciClean :app:assembleDebug
- Ensure image files are valid PNGs and readable in Docker.
