# Porting HDMI input switching to other chipsets

<!-- projector-home docs/porting.md v0.2.1 -->

Projector Home's Roku channel launching and apps mode work on any Android 9+ device. **Automatic input switching** is chipset-specific: it currently supports HiSilicon TV firmware. This page shows how that support was found, so the same approach can be used on Amlogic, MStar/MediaTek, Rockchip, Allwinner, or other boards.

All of the commands below are read-only.

## 1. Find the input player

Switch to an HDMI input with the projector's own remote, then:

```bash
adb shell dumpsys activity activities | grep mResumedActivity
```

On the WiMiUS P63 this shows `com.hisilicon.tvui/.MainActivity`. That activity displays whatever source is selected, so opening it is half the job. Note the package and class for your device.

## 2. Find out how the source is selected

Watch the system log while switching inputs by hand:

```bash
adb logcat -c
# switch inputs with the remote now
adb logcat -d | grep -iE "source|hdmi|input" | tail -60
```

On the P63 this showed the vendor's source manager (`SourceManagerImpl ... getCurSourceId: SourceId:10`) and a CEC line mapping the active device to `[10]HDMI2`. Look for a source ID and the class that logs it.

## 3. Check the standard TV Input Framework first

Many TV firmwares implement Android's TV Input Framework, which would allow a portable approach:

```bash
adb shell dumpsys tv_input | grep -E "TvInputInfo|mId|id="
```

If HDMI inputs appear here with IDs, a passthrough URI (`TvContract.buildChannelUriForPassthroughInput(inputId)`) opened with `ACTION_VIEW` may switch inputs with no vendor code at all. The P63 lists nothing here, which is why it uses the vendor API. A TIF-based fallback in `HdmiInput.java` would be a welcome contribution.

## 4. Find the vendor API

Vendor libraries are usually declared as shared libraries:

```bash
adb shell 'grep -rl library /system/etc/permissions /vendor/etc/permissions 2>/dev/null'
adb shell 'ls -la /system/framework | grep -iE "tv|hi|mtk|amlogic|rk"'
```

On the P63, `/system/etc/permissions/com.hisilicon.android.tvapi.xml` maps the library to `/system/framework/HitvShare.jar`.

These jars are often **stripped**: a few hundred bytes, with the real code only in pre-compiled `oat/<arch>/*.vdex` files that desktop tools can't read. The workaround is to ask the device's own runtime to list the methods, using a tiny reflection tool run under `app_process`:

```java
// Inspect.java
import java.lang.reflect.*;
public class Inspect {
  public static void main(String[] a) throws Exception {
    for (String cn : a) {
      Class<?> c = Class.forName(cn);
      System.out.println("== " + c.getName());
      for (Method m : c.getDeclaredMethods()) System.out.println("  " + m.toGenericString());
    }
  }
}
```

```bash
PLAT=$HOME/Android/Sdk/platforms/android-28/android.jar
javac --release 11 -cp "$PLAT" -d out Inspect.java
$HOME/Android/Sdk/build-tools/34.0.0/d8 --min-api 28 --lib "$PLAT" --output . out/Inspect.class
adb push classes.dex /data/local/tmp/inspect.dex
adb shell 'CLASSPATH=/data/local/tmp/inspect.dex:/system/framework/<VendorLib>.jar \
  app_process /system/bin Inspect <fully.qualified.ClassName>'
adb shell rm /data/local/tmp/inspect.dex
```

On the P63 this revealed `HitvManager.getInstance().getSourceManager()` with `selectSource(int, int)` and `getSelectSourceId()`.

## 5. Wire it in

`HdmiInput.java` is the only file to change:

- `PLAYER_PKG` / `PLAYER_CLS`: your input player from step 1
- `sourceManager()`, `selectSource()`, `lastSelectedSource()`: your vendor's equivalents, called through reflection so the APK still runs on devices without the library
- `AndroidManifest.xml`: add `<uses-library android:name="<your.vendor.library>" android:required="false" />`

Keep everything behind reflection and `required="false"`, so one APK works across devices.

## 6. Test

```bash
adb logcat -c
# press a tile in Projector Home
adb logcat -d | grep -E "ProjectorHome|<your vendor tag>"
```

Projector Home logs `selectSource(<id>) -> true/false` under the tag `ProjectorHome`.

Please open a pull request or an issue with your device model, `ro.board.platform`, and what you found, even if you don't write the code.
