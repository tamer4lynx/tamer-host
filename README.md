# tamer-host

Production Lynx host templates for injecting LynxView into existing Android and iOS apps.

## Overview

`tamer-host` provides the minimal native infrastructure needed to run a Lynx bundle in an existing app:

- **Android**: `App.java`, `TemplateProvider.java`, `MainActivity.kt`
- **iOS**: `AppDelegate.swift`, `SceneDelegate.swift`, `ViewController.swift`, `LynxProvider.swift`, `LynxInitProcessor.swift`

## Installation

```bash
npm install tamer-host
```

## Usage

### Inject into an existing project

After creating an Android or iOS project (or if you already have one), run:

```bash
t4l android inject
t4l ios inject
```

Use `--force` to overwrite existing files:

```bash
t4l android inject --force
```

### Prerequisites

- `tamer.config.json` with `android.packageName` / `ios.appName` and `ios.bundleId`
- An existing Android project (`android/app/src/main/java/`, `android/app/src/main/kotlin/`) or iOS project (`ios/<AppName>/`)
- Run `t4l link` after injecting to register native modules

### Create flow

`t4l android create` and `t4l ios create` use `tamer-host` templates when the package is installed. If `tamer-host` is not installed, the CLI falls back to inline templates.

## Related

- [tamer-dev-client](https://github.com/tamer4lynx/tamer-dev-client) — Adds dev launcher (QR scan, HMR) on top of the host
- [Embedding LynxView into Native View](https://lynxjs.org/guide/embed-lynx-to-native) — Lynx guide for embedding LynxView in existing layouts
