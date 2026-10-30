# Projector Home

<!-- projector-home README v0.3.0 -->

**A small, private home screen (launcher) for Android projectors and TV boxes.** About 30 KB, no analytics, no accounts, no ads, and no network access beyond what you point it at.

Projector Home was written to replace a vendor launcher that was caught installing ad-fraud malware on a budget Android projector. The full investigation is in **[projector-reclaim](https://github.com/builtbyfood/projector-reclaim)**: what the malware does, how to check your own devices, and how to remove it.

It was built for one setup: **a Roku plugged into the projector's HDMI port**, with the projector used purely as a display, so the projector itself never needs internet access or holds a streaming login. It also has a mode for people who watch with **apps installed on the projector**.

| Mode | Home tiles | Selecting a tile |
|---|---|---|
| **Roku over HDMI** | Your Roku channels, with real icons fetched from the Roku | The Roku opens that channel (over its local control API, TCP 8060) and the projector switches to the Roku's HDMI input |
| **Apps on this projector** | Apps installed on the projector | Opens the app. An **HDMI Input** tile switches to a streaming stick, console, or laptop |

Both modes include **Keystone / Focus** and **Settings** tiles. Long-press Settings for setup.

In Roku mode, the **projector's own remote can drive the Roku** (arrows, OK, Back, play/pause) while the Roku is on screen, so you don't need two remotes. See [Using the projector remote with the Roku](#using-the-projector-remote-with-the-roku).

---

## Will it work on my device?

| Device | Roku mode | Apps mode | Automatic HDMI input switching |
|---|---|---|---|
| WiMiUS P63 and other **HiSilicon**-based projectors (have `com.hisilicon.tvui`) | Yes | Yes | Yes |
| Other Android 9+ projectors and TV boxes | Yes (channel launching) | Yes | No: needs a small port for your chipset, see [docs/porting.md](docs/porting.md) |
| Android 8 or older | Needs a rebuild with a lower `minSdk` | | |

Check your chipset:

```bash
adb shell getprop ro.board.platform              # e.g. bigfish = HiSilicon
adb shell pm list packages | grep hisilicon.tvui # present = input switching supported
```

Tested on: **WiMiUS P63** (HiSilicon Hi3751V350, Android 9). Reports from other devices are welcome; see [Contributing](CONTRIBUTING.md).

---

## Install

You need `adb` (Android platform-tools) and ADB access to the projector. Most of these devices hide it: unlock Developer options (Settings → About, press OK on **Build** seven times), then enable USB or network debugging. Some firmware puts network ADB behind a separate "factory test" toggle. The [projector-reclaim removal guide](https://github.com/builtbyfood/projector-reclaim/blob/main/docs/removal-guide.md) §1 covers the common snags.

1. **Download** `ProjectorHome-0.3.0.apk` and `ProjectorHome-0.3.0.apk.sha256` from [Releases](../../releases).
2. **Verify** it:
   ```bash
   sha256sum -c ProjectorHome-0.3.0.apk.sha256
   apksigner verify --print-certs ProjectorHome-0.3.0.apk
   #   Signer #1 certificate SHA-256 digest: 51ba0861208a74415cd3a3ad69b7a01b685eeed7619ce6f3b1ef7204a866a51d
   ```
   The `apksigner` step needs Android SDK build-tools and is optional, but it's how you know the APK is the one published here. Every release is signed with this same certificate.
3. **Install** and make it the home screen:
   ```bash
   adb install ProjectorHome-0.3.0.apk
   adb shell cmd package set-home-activity com.builtbyfood.projectorhome/.HomeActivity
   adb shell input keyevent HOME
   ```
   Or press Home on the remote and choose Projector Home when Android asks.
4. On first launch, choose **Roku over HDMI** or **Apps on this projector**.

**Updating:** `adb install -r` the new APK. Settings are kept.

**Removing:** `adb uninstall com.builtbyfood.projectorhome`, then press Home to return to the previous launcher.

---

## Setup

### Roku mode

1. Give the Roku a fixed IP address (a DHCP reservation in your router).
2. On the Roku: **Settings → System → Advanced system settings → Control by mobile apps → Network access** must allow control. If the projector and Roku are on different networks or VLANs, allow TCP 8060 from the projector to the Roku.
3. Enter the Roku's IP when Projector Home asks (on the Roku: Settings → Network → About).

### Choosing the HDMI input (both modes)

1. With the projector remote, switch to the input your Roku or streaming device is plugged into, and let it show.
2. Press Home, long-press **Settings**, choose **Streaming input**, then **Use last-watched input**.

If you skip this, Projector Home shows whichever input was used last, which is fine if you only ever use one.

### Using the projector remote with the Roku

Projector Home includes an optional accessibility service that forwards the projector remote to the Roku over its local control API while the Roku's HDMI input is on screen.

| Projector remote | Goes to |
|---|---|
| Arrows, OK, Back | Roku (Up/Down/Left/Right, Select, Back) |
| Play/pause, rewind, fast-forward, info | Roku |
| **Home** | Projector: back to Projector Home |
| Volume, mute, power | Projector |
| **Menu** | Hands the remote back to the projector (its picture menu opens). Press Menu again to give it back to the Roku |

Android requires you to switch it on once:

- On the projector: **Settings → Device Preferences → Accessibility → Projector Home: Roku remote → On**, or
- From a computer, **adding** to whatever is already enabled (some firmware relies on its own accessibility service; on the WiMiUS P63 that's `com.hisilicon.rmservice/…HiRMService`, which must stay on):
  ```bash
  SVC=com.builtbyfood.projectorhome/com.builtbyfood.projectorhome.RokuRemoteService
  CUR=$(adb shell settings get secure enabled_accessibility_services | tr -d '\r')
  if [ -z "$CUR" ] || [ "$CUR" = null ]; then NEW=$SVC; else NEW="$CUR:$SVC"; fi
  adb shell settings put secure enabled_accessibility_services "$NEW"
  adb shell settings put secure accessibility_enabled 1
  ```
  Don't run `settings put` with only Projector Home's service: that switches the others off.

Verified on a WiMiUS P63 with the projector's own remote. (Keys injected with `adb shell input keyevent` bypass Android's accessibility key filter, so use the real remote to test.)

Turn passthrough on or off later from long-press **Settings → Projector remote controls Roku**. It only acts in Roku mode and only while the projector's HDMI player is in front; everywhere else the remote behaves normally.

### Home tiles

Open **All Channels** (Roku mode) or **All Apps** (apps mode) and long-press to add a tile to home. Long-press a home tile to remove it. Tiles size themselves to the screen.

### Recommended network setup

- **Roku mode:** the projector needs no internet at all. Put it on an isolated network and allow only TCP 8060 to the Roku. This is the configuration Projector Home was designed for.
- **Apps mode:** the projector needs internet for streaming. Keep it isolated from your computers and other devices, and block known malware domains in your DNS ([projector-reclaim/iocs](https://github.com/builtbyfood/projector-reclaim/tree/main/iocs)).

---

## Privacy

Projector Home:

- requests only the `INTERNET` permission, used solely to reach the Roku address you enter (Roku mode)
- stores its settings (mode, Roku address, input, tiles, passthrough on/off) and cached channel icons in its own private app storage
- contains no analytics, crash reporting, ads, accounts, or third-party libraries
- makes no connections at all in apps mode
- **remote passthrough (optional, off until you enable it):** as an accessibility service it is notified which app is in front (package and class name only; `canRetrieveWindowContent` is false, so it cannot read the screen) and sees remote-control key presses. It forwards navigation keys to your Roku only while the projector's HDMI player is in front, and lets every other key through untouched. Nothing is logged or stored

The source is five Java files. Read them: [`src/com/builtbyfood/projectorhome/`](src/com/builtbyfood/projectorhome/).

---

## Build from source

Requirements: JDK 11 or newer, and Android SDK packages `platforms;android-28` and `build-tools;34.0.0`. No Gradle, no Android Studio.

```bash
sdkmanager --sdk_root=$HOME/Android/Sdk "platforms;android-28" "build-tools;34.0.0"
./build.sh
# -> build/ProjectorHome-0.3.0-dev.apk, signed with a local development key
```

A development build has a different signing certificate from the release, so Android won't install it over a release build (or vice versa). Uninstall first when switching.

Release builds are signed with a key kept outside this repository; see [RELEASING.md](RELEASING.md).

---

## How it works

| File | Role |
|---|---|
| `HomeActivity.java` | The launcher: home screen, All Channels / All Apps, setup menus, focus handling and screen-width tile sizing |
| `Config.java` | Settings: mode, Roku address, HDMI source, home tiles |
| `RokuClient.java` | Minimal Roku External Control Protocol client: channel list, icons, launch, keypress |
| `HdmiInput.java` | HDMI input switching through the HiSilicon TV API (`com.hisilicon.android.tvapi`), loaded by reflection so the APK runs on any device |
| `RokuRemoteService.java` | Optional accessibility service: forwards the projector remote's navigation keys to the Roku while its HDMI input is shown |
| `res/` | The accessibility service's configuration and description text |
| `AndroidManifest.xml` | Declares the HOME / LEANBACK_LAUNCHER activity, the optional remote service, and the optional vendor library |

On HiSilicon firmware the vendor TV jars are stripped down to stubs, with the real code only in pre-compiled form. [docs/porting.md](docs/porting.md) shows how the input API was found by asking the device's own runtime, which is also the way to port it to other chipsets.

---

## Versions

See [CHANGELOG.md](CHANGELOG.md).

## License

MIT. See [LICENSE](LICENSE).

Not affiliated with Roku, WiMiUS, HiSilicon, or any device vendor. Roku is a trademark of Roku, Inc.
