# Security

<!-- projector-home SECURITY v0.3.0 -->

## Reporting a vulnerability

Please open a GitHub security advisory (Security tab → Report a vulnerability) rather than a public issue.

## Verifying a download

Every release APK is signed with the certificate whose SHA-256 is:

```
51ba0861208a74415cd3a3ad69b7a01b685eeed7619ce6f3b1ef7204a866a51d
```

Check with `apksigner verify --print-certs <apk>`. An APK signed with anything else did not come from this project.

## Scope

Projector Home holds no credentials and requests only `INTERNET`, which it uses only to reach the Roku address you enter. The optional remote-passthrough accessibility service cannot read screen content; it sees which app is in front and remote key presses, and forwards navigation keys only to that Roku address. It cannot fix the device it runs on: if your projector shipped with malware, see [projector-reclaim](https://github.com/builtbyfood/projector-reclaim) to find and remove it.
