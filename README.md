# M2

[![Generic badge](https://img.shields.io/badge/Platform-Android-green.svg)](https://github.com/o4x/M2)
[![Generic badge](https://img.shields.io/badge/minSdkVersion-28-green.svg)](https://github.com/o4x/M2)

A clean, local music player for Android with a focus on a smooth playback experience and a polished Material 3 UI.

## Installation

M2 is not yet available on any app store. For now, install it by downloading the latest APK from [GitHub Releases](https://github.com/o4x/M2/releases) or by building from source.

## Screenshots
| <img src="screenshots/1.jpg" width="250"/> | <img src="screenshots/2.jpg" width="250"/> | <img src="screenshots/3.jpg" width="250"/> |
|:---:|:---:|:---:|
| Home | Player | Album |

## Building from source

Requirements:
- Android Studio (latest stable)
- JDK 21
- Android SDK with API 36

### Firebase (optional)

Analytics and Crashlytics are only enabled when `app/google-services.json` exists.
The file is not checked into the repository, and the project builds fine without it —
Firebase simply stays disabled.

To enable it, create your own [Firebase project](https://console.firebase.google.com/),
register two Android apps with the package names `github.o4x.m2` and `github.o4x.m2.debug`
(the debug build adds a `.debug` suffix), then download the generated
`google-services.json` into the `app/` directory.
