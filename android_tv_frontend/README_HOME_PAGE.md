# Android TV Home Page - Native Implementation

## Overview

This document describes the native Android TV implementation of the Streamly Home Page, built to match the Figma design pixel-for-pixel without using WebViews.

## Design Specifications

- **Screen Resolution**: 1920x1080 (Full HD TV)
- **Background Color**: #121212 (dark theme)
- **Design System**: Based on `assets/common.css` and `assets/home-page-1-2.css`
- **Typography**: Roboto font family with sizes 21-36sp
- **Navigation**: Full DPAD support for TV remote control

## Architecture

### Components

1. **HomeFragment** (`HomeFragment.kt`)
   - Main fragment hosting the entire home page
   - Manages three horizontal RecyclerViews for navigation, movies, and TV channels
   - Coordinates DPAD focus navigation between sections

2. **NavigationAdapter** (`NavigationAdapter.kt`)
   - Horizontal navigation bar with 6 items (Inicio, Películas, Series, TV en vivo, Kids, Mis Contenidos)
   - Active state: Bold text, white color, red background (#9b0f0f), 37dp radius
   - Inactive state: Regular text, gray color (#7f8282), transparent background
   - Typography: Roboto 29sp

3. **MovieCardAdapter** (`MovieCardAdapter.kt`)
   - "Seguí viendo" (Continue Watching) content rail
   - First card: 474x329dp, others: 412x312dp
   - Progress bars with #de1717 fill, #2c2c2c track
   - Scale to 1.05x on focus
   - Title: Roboto 30sp Medium, #ffffff

4. **TvChannelCardAdapter** (`TvChannelCardAdapter.kt`)
   - "Canales de TV" rail with 745x212dp cards
   - Live badge: "EN VIVO", #eb0045 background, 3.81dp radius
   - Play button overlay: 92dp circle, #c60000 background
   - Title: Roboto 36sp Bold
   - Channel info: Roboto 30sp Regular
   - Scale to 1.02x on focus

## Layout Structure

### Main Layout (`fragment_home.xml`)

```
1920x1080 Screen
├── Header (y:36dp)
│   ├── Claro Logo (x:89dp)
│   ├── Navigation Bar (x:356dp, 1159x74dp)
│   ├── Search Icon (x:380dp, y:67dp)
│   └── User Avatar (x:1435dp, y:57dp, 56x56dp)
├── Hero Banner (y:152dp, 1744x444dp)
├── Continue Watching Section (y:632dp)
│   ├── Section Title "Seguí viendo" (32sp)
│   └── Horizontal RecyclerView with 5 movie cards
└── TV Channels Section (y:1040dp)
    ├── Section Title "Canales de TV" (32sp)
    └── Horizontal RecyclerView with 3 channel cards
```

## Design System Colors

All colors match the CSS design system from `common.css`:

| Element | Color Code | CSS Variable |
|---------|------------|--------------|
| Background | #121212 | color_121212 |
| Primary Text | #FFFFFF | color_ffffff |
| Navigation Bar | #28292F | color_28292f |
| Active Nav | #9B0F0F | color_9b0f0f |
| Inactive Nav | #7F8282 | color_7f8282 |
| Progress Fill | #DE1717 | color_de1717 |
| Progress Track | #2C2C2C | color_2c2c2c |
| Live Badge | #EB0045 | color_eb0045 |
| Play Button | #C60000 | color_c60000 |
| Card Title BG | #323131 | color_323131 |

## DPAD Navigation

### Focus Order
1. **Navigation Bar** (initial focus)
   - Left/Right: Navigate between nav items
   - Down: Move to Continue Watching rail

2. **Continue Watching Rail**
   - Left/Right: Navigate between movie cards
   - Up: Return to Navigation Bar
   - Down: Move to TV Channels rail

3. **TV Channels Rail**
   - Left/Right: Navigate between channel cards
   - Up: Return to Continue Watching rail

### Focus Visual Feedback
- Movie cards scale to 1.05x
- TV channel cards scale to 1.02x
- Navigation items show red background when active
- All focusable elements are properly marked with `focusable="true"`

## Image Assets

### Used Images (PNG format)
Located in `app/src/main/res/drawable/`:

- **Hero Banner**: `figma_image_1_13.png` (1744x444dp)
- **Movie Posters**:
  - `figma_image_1_41.png` (Rogue One)
  - `figma_image_1_68.png` (Ex Machina)
  - `figma_image_1_85.png` (Sing Street)
  - `figma_image_1_102.png` (2012)
  - `figma_image_1_119.png` (Ad Astra)
- **TV Thumbnails**:
  - `figma_image_1_154.png` (Marca Claro Radio)
  - `figma_image_1_180.png` (E.T.)
  - `figma_image_1_218.png` (Marca Claro Radio)
- **User Avatar**: `figma_image_1_231.png` (56x56dp)

### Vector Drawables
Created custom vector drawables for:
- `claro_logo.xml` (Claro Video logo)
- `ic_search.xml` (Search icon)
- `ic_play_circle.xml` (Play button with circle)
- `ic_tv.xml` (TV icon for channels)

### Progress Bars
- `progress_track.xml` / `progress_track_large.xml` (#2c2c2c, 8dp/9dp radius)
- `progress_fill.xml` / `progress_fill_large.xml` (#de1717, 4dp/5dp radius)
- `tv_progress_track.xml` (#2c2c2c 80% opacity, 2dp radius)
- `tv_progress_fill.xml` (#de1717, 2dp radius)

### Badges
- `live_badge_bg.xml` (#eb0045, 4dp radius)
- `rental_tag_bg.xml` (#ffffff, 2dp radius)

## Typography Mapping

CSS to Android SP values (1:1 at 1920x1080):

| CSS Style | Usage | Android Size | Weight |
|-----------|-------|--------------|--------|
| typo_23 | Movie titles | 30sp | Medium |
| typo_24 | Section titles | 32sp | Regular |
| typo_25 | TV program titles | 36sp | Bold |
| typo_26 | Channel info | 30sp | Regular |
| typo_27 | Badge text | 21sp | Medium |
| typo_28 | Inactive nav | 29sp | Regular |
| typo_29 | Active nav | 29sp | Bold |

## Key Implementation Details

### Pixel-Perfect Positioning
- All positions calculated from Figma coordinates (x, y)
- Root offset: x: -40, y: 13 applied to all elements
- Margins and sizes match CSS pixel values exactly (converted to dp)

### RecyclerView Configuration
- `LinearLayoutManager` with `HORIZONTAL` orientation
- `clipToPadding="false"` for edge cards visibility
- Proper spacing using `layout_marginEnd` on items

### Focus Management
- Initial focus set on navigation bar via `view.post {}`
- RecyclerViews marked as focusable
- Items scale smoothly on focus (200ms animation)

### Card Dimensions
- **Movie Card (standard)**: 412x312dp, 40dp gap
- **Movie Card (first)**: 474x329dp, 9dp gap to next
- **TV Channel Card**: 745x212dp, 82dp gap

## Dependencies

Added to `build.gradle.kts`:
```kotlin
implementation("androidx.cardview:cardview:1.0.0")
implementation("androidx.recyclerview:recyclerview:1.3.2")
```

## Testing

To test the Home Page:
1. Build and run on Android TV emulator or device
2. Use DPAD/remote to navigate
3. Verify focus transitions between sections
4. Check visual styling matches Figma design
5. Test card scaling on focus

## Future Enhancements

1. Add hero carousel auto-rotation
2. Implement click handlers for cards
3. Add delete button functionality for Continue Watching
4. Implement search functionality
5. Add user profile menu
6. Load content dynamically from API
7. Add smooth horizontal scrolling animations
8. Implement content filtering by nav selection

## Notes

- SVG assets moved to `res/raw/` (Android doesn't support SVG in drawable)
- Static data used for demonstration
- All measurements in dp (density-independent pixels)
- Landscape orientation enforced in AndroidManifest.xml
- Minimum SDK: 21 (Android 5.0 Lollipop)
- Target SDK: 34 (Android 14)
