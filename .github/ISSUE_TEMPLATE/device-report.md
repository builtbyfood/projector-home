---
name: Device report
about: Tell us whether Projector Home works on your projector or TV box
title: "Device report: <brand> <model>"
labels: device-report
---

**Device:** brand and model

**Output of:**
```
adb shell getprop ro.product.model
adb shell getprop ro.board.platform
adb shell getprop ro.build.version.release
adb shell pm list packages | grep -E "hisilicon.tvui|tvinput"
```

**Mode used:** Roku over HDMI / Apps on this projector

**What worked:** tiles, launching, keystone tile, HDMI input switching

**What didn't:**

**Projector Home version:** (long-press Settings; it's in the title)
