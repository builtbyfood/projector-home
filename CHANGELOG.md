# Changelog

<!-- projector-home CHANGELOG v0.3.0 -->

## 0.3.0
- **Projector remote controls the Roku.** Optional accessibility service: while the Roku's HDMI input is on screen, arrows, OK, Back and play keys go to the Roku over ECP. Home returns to Projector Home; Menu hands the remote back to the projector. Enable once in Accessibility settings; toggle in the setup menu.
- Setup menu shows the passthrough state and how to enable it.
- Build: compiles a small `res/` folder (service config and strings) and links it as base resources; javac pinned to UTF-8; `.sha256` files name the APK without a path.
- Verified on a WiMiUS P63: arrows, OK, Back reach the Roku; Menu hands the remote back to the projector. The vendor's own accessibility service (`HiRMService`) is kept enabled alongside.

## 0.2.1 — 2026-10-26
- First release as a standalone repository.
- Removed an unused draft class (`AppCatalog`) that was included in 0.2.0; no functional change.

## 0.2.0 — 2026-10-26
- **Apps mode** for people who watch with apps installed on the projector.
- **HDMI input picker**: Settings → Streaming input → Use last-watched input. No rebuild needed for a different port.
- Setup menu (long-press Settings), first-run mode choice.
- Release builds signed with a dedicated key.

## 0.1.3
- No built-in Roku address: asks on first run. Generic default tiles.

## 0.1.2
- Tile sizes derived from screen width. Fixes the fifth tile being clipped when firmware forces density 320 (a 1920-px screen is only 960 dp wide). Verified on device.

## 0.1.1
- Cleanup; calls the confirmed `selectSource(int, int)` signature directly.

## 0.1.0
- First build. Verified on a WiMiUS P63: tile → Roku launches channel → middleware logs `select source to [10]HDMI2 finish!`.
