# Changelog

<!-- projector-home CHANGELOG v0.2.1 -->

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
