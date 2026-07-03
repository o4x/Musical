# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Building and Debugging

The user builds, runs, and debugs the app themselves in Android Studio. Do NOT run Gradle builds, launch the app, or attempt to debug it yourself — just make the code changes and let the user verify them.

The app uses Gradle version catalogs (`gradle/libs.versions.toml`) for all dependency version management.

## Git

Do NOT add a "Co-Authored-By: Claude" line (or any Claude attribution) to commit messages.

## Architecture Overview

**Musical** is an Android music player app (package `github.o4x.m2`, minSdk 28, targetSdk 36) written in Kotlin. The UI is traditional View-based (no Jetpack Compose); theming extends the Material 3 Expressive XML themes.

### Dependency Injection

Koin is used throughout. Modules are wired together in `MainModule.kt`:
- `dataModule` (repository layer) — `repository/DataModule.kt`
- `viewModules` (ViewModels) — `ui/viewmodel/ViewModules.kt`
- `networkModule` — `network/NetworkModule.kt`
- `roomModule` — `db/RoomModule.kt`

### Music Playback Pipeline

The core playback flow runs through a bound `Service`:

1. **`MusicService`** (`service/MusicService.kt`) — the foreground service that owns the playback state, queue, Media3 `MediaSession`, and notification. It broadcasts local intents (`META_CHANGED`, `PLAY_STATE_CHANGED`, `QUEUE_CHANGED`, etc.) when state changes. All playback calls run on the main thread (ExoPlayer requirement); Room queue persistence runs in coroutines.
2. **`MusicPlayerRemote`** (`helper/MusicPlayerRemote.kt`) — a singleton `object` that activities/fragments call to control playback. It binds to `MusicService` and delegates all commands.
3. **`Media3Playback`** (`service/player/Media3Playback.kt`) — wraps Media3 `ExoPlayer`, which owns audio focus (incl. ducking), becoming-noisy pauses, the playback wake lock, and gapless playback (the next track is queued as a second playlist item). The ExoPlayer playlist never holds the full queue — the service does.
4. The `MediaSession` is built around a `ForwardingPlayer` (`QueueNavigationPlayer` in `MusicService`) that reroutes next/previous/stop from external controllers (notification, headset, lockscreen) into the service's queue logic.

### UI Layer

Activity hierarchy:
- `AbsBaseActivity` → `AbsMusicServiceActivity` → `AbsMusicPanelActivity` → `MainActivity`
- `AbsMusicServiceActivity` binds to `MusicService` and forwards events to registered `MusicServiceEventListener`s (fragments and ViewModels implement this interface).
- `AbsMusicPanelActivity` adds the persistent mini-player bar at the bottom; tapping it opens `PlayerActivity`.

`MainActivity` hosts a Navigation Component graph. Main destinations are fragments under `ui/fragments/mainactivity/`:
- `HomeFragment` — recently added/played, top tracks
- `LibraryFragment` — tabbed pager (songs, albums, artists, playlists, genres)
- `FoldersFragment` — file-system browser
- `QueueFragment`, `SearchFragment`, `TimerFragment`

Player UI lives in `ui/fragments/player/`:
- `PlayerFragment` — full-screen player
- `MiniPlayerFragment` — collapsed bottom bar

### ViewModel / Data Flow

`LibraryViewModel` is the central ViewModel. It holds `LiveData` for all library content (songs, albums, artists, playlists, genres, recently played/added) and also implements `MusicServiceEventListener` to react to playback events. `PlayerViewModel` tracks the current queue and palette color.

ViewModels call `Repository`, which aggregates all sub-repositories:
- `SongRepository`, `AlbumRepository`, `ArtistRepository` — query `MediaStore` via `ContentResolver`
- `GenreRepository`, `PlaylistRepository`, `LastAddedRepository`, `SearchRepository`
- `RoomRepository` — wraps Room DAOs for app-managed data

### Room Database

`MusicalDatabase` (version 12) stores:
- `HistoryEntity` / `PlayCountEntity` — for "recently played" and "top tracks" smart playlists
- `QueueEntity` / `QueueOriginalEntity` — persist the playback queue across app restarts
- `LyricsEntity` — locally cached lyrics

### Image Loading

`GlideLoader` (`imageloader/glide/loader/GlideLoader.kt`) is the single entry point for all image loading. It wraps Glide with custom Glide models for:
- `AudioFileCover` — embedded album art from audio files
- `ArtistImage` — artist images (fetched via Last.fm API or local cache)
- `MultiImage` — mosaic of multiple covers

Glide targets in `imageloader/glide/targets/` integrate with `Palette` to extract dominant colors and propagate them through `PaletteTargetListener`.

### Networking

Retrofit + OkHttp client in `network/` talks to the Last.fm API (`LastFMService`). Results are wrapped in a `Result` sealed class (`Success`/`Error`).

### Preferences

All user preferences are accessed through `PreferenceUtil` (`prefs/PreferenceUtil.kt`). It wraps `SharedPreferences` directly — no DataStore.

### Tag Editing

`AbsTagEditorActivity` and its subclasses use the bundled `jaudiotagger-2.2.4-SNAPSHOT.jar` (`app/libs/`) for reading and writing audio file metadata. Online tag search talks to MusicBrainz/Last.fm via `network/`.

### Custom Module

`recyclerview-fastscroll/` is a local library module included via `implementation(project(":recyclerview-fastscroll"))`.

## Key Patterns

- **Smart playlists** (`model/smartplaylist/`) extend `AbsSmartPlaylist` and generate negative IDs via `PlaylistIdGenerator` to avoid collisions with MediaStore playlist IDs.
- **RTL support is disabled** (`android:supportsRtl="false"` in the manifest).
- **ViewBinding** is used for all layouts (DataBinding is not enabled).
- **KSP** is used for Glide's `@GlideModule` and Room's `@Database` annotation processing (no kapt). Glide's generated API (GlideApp) is not used — code calls `Glide.with(...)` directly.
- The debug build appends `.debug` to the applicationId, so debug and release can coexist on device.
