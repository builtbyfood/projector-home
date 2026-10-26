# Contributing

<!-- projector-home CONTRIBUTING v0.2.1 -->

Useful contributions, roughly in order of value:

1. **Device reports.** Did it work on your projector or TV box? Open an issue with the *Device report* template: model, `ro.board.platform`, Android version, which mode, and whether input switching worked.
2. **Input switching for other chipsets.** See [docs/porting.md](docs/porting.md). Keep vendor calls behind reflection so one APK runs everywhere.
3. **Other streaming devices** (Fire TV, Apple TV, Chromecast with Google TV, etc.). `RokuClient.java` is the only Roku-specific code; a similar client with a mode in `Config` would do it.

Ground rules:

- No new dependencies, analytics, or network destinations beyond the device the user configures.
- Plain Java, Android 9 (API 28) minimum, built with `build.sh` (no Gradle).
- Keep the version stamp at the top of every file you change in step with `build.sh` and the manifest.
