# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run instrumented tests
./gradlew connectedAndroidTest

# Run unit tests
./gradlew test

# Clean build
./gradlew clean

# Lint check (abortOnError = false, MissingTranslation and InvalidPackage are disabled)
./gradlew lint
```

The app uses Gradle version catalogs (`gradle/libs.versions.toml`) for all dependency version management.

## Architecture Overview

**Musical** is an Android music player app (package `github.o4x.musical`, minSdk 28, targetSdk 36) written in Kotlin. It uses a mix of traditional View-based UI and Jetpack Compose.

### Dependency Injection

Koin is used throughout. Modules are wired together in `MainModule.kt`:
- `dataModule` (repository layer) — `repository/DataModule.kt`
- `viewModules` (ViewModels) — `ui/viewmodel/ViewModules.kt`
- `networkModule` — `network/NetworkModule.kt`
- `roomModule` — `db/RoomModule.kt`

### Music Playback Pipeline

The core playback flow runs through a bound `Service`:

1. **`MusicService`** (`service/MusicService.kt`) — the foreground service that owns the playback state, queue, MediaSession, and notification. It broadcasts local intents (`META_CHANGED`, `PLAY_STATE_CHANGED`, `QUEUE_CHANGED`, etc.) when state changes.
2. **`MusicPlayerRemote`** (`helper/MusicPlayerRemote.kt`) — a singleton `object` that activities/fragments call to control playback. It binds to `MusicService` and delegates all commands.
3. **`MultiPlayer`** (`service/player/MultiPlayer.kt`) — wraps Android `MediaPlayer` for gapless playback.
4. **`PlaybackHandler`** (`service/playback/PlaybackHandler.kt`) — a `Handler` on a background looper that manages audio ducking via timed messages.

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
- **ViewBinding and DataBinding** are both enabled.
- **kapt** is used for Glide's `@GlideModule` and Room's `@Database` annotation processing; Dagger is also wired but Koin is the active DI framework.
- The debug build appends `.debug` to the applicationId, so debug and release can coexist on device.
