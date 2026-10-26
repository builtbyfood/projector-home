# Releasing

<!-- projector-home RELEASING v0.2.1 -->

Release APKs are signed with a dedicated key that never enters this repository.

Certificate SHA-256: `51ba0861208a74415cd3a3ad69b7a01b685eeed7619ce6f3b1ef7204a866a51d`

Every release must be signed with this key. Android refuses to update an installed app signed with a different one, so losing the key means users must uninstall and reinstall.

## Steps

1. Bump the version in **all** of: `AndroidManifest.xml` (`versionCode` +1 and `versionName`), `build.sh` (`VERSION`), the header stamp of every source file, and `CHANGELOG.md`.
2. Build:
   ```bash
   RELEASE=1 KEYSTORE=/path/outside/repo/release.keystore \
     KEYSTORE_PASS_FILE=/path/outside/repo/release.pass ./build.sh
   ```
   Check that the printed certificate SHA-256 matches the one above.
3. Test on a device: both modes, a channel or app tile, the HDMI input picker, keystone tile.
4. Tag `vX.Y.Z` and create a GitHub Release with `build/ProjectorHome-X.Y.Z.apk` and `build/ProjectorHome-X.Y.Z.apk.sha256`.
5. Optional: scan the APK at virustotal.com and link the result in the release notes.

Never attach a `-dev` APK to a release.
