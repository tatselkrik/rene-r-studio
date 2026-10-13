# Rene'R Studio: Git and Release Guide

## What Git Does

Git preserves named snapshots of the source code. Local Git works without internet; publishing to GitHub requires an authenticated account. Released source lives on `main` in [tatselkrik/rene-r-id-print](https://github.com/tatselkrik/rene-r-id-print). The current release is **Rene'R Studio v1.0.7 / versionCode 11**. Earlier tags and release assets remain preserved.

Kirk explicitly authorized v1.0.7 GitHub publication, a current screenshot gallery and HPC-style README/release logs on October 13, 2026. Preserve the exact prepared build 11 APK and its signing identity. This authorization supersedes the earlier local-candidate-only state; it does not establish a successful physical landscape print test. That limitation belongs in the release notes.

Version 1.0.7 adds remembered Matte/Glossy and landscape capture/import/output. The previous ID workflow and calibration remain preserved. See [CHANGELOG.md](CHANGELOG.md) and the [v1.0.7 user guide](docs/user-guide-v1.0.7.md).

Useful commands in Android Studio's Terminal:

```powershell
git status
git log --oneline --decorate
git tag
```

- `git status` shows files changed since the last saved snapshot.
- `git log --oneline --decorate` shows saved versions.
- `git tag` lists release markers such as `v1.0.0`, `v1.0.1`, and `v1.0.2`.

## Adding a Feature Later

Start new work on a separate branch so the stable release remains safe. For example:

```powershell
git switch -c codex/short-feature-name
```

After the feature has been tested:

```powershell
git add -A
git commit -m "Add selectable preview layouts"
git switch main
git merge codex/short-feature-name
```

For the next release, choose a new `versionName`, increase `versionCode` above `11`, build the installer with the same signing key, and publish a matching tag only after the required checks and Kirk's publication authorization.

## Local Git Versus Backup

Local Git is still stored on this computer. The public GitHub repository backs up source, documentation and reviewed demo images; it is not a backup of private signing state or customer photos. Keep those materials in appropriate private backups, never in public history.

## Creating the Signed Installer

From v1.0.3, signing is automatic on the configured Windows account. Run:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\create-signed-installer.ps1
```

To sign the already verified release build without rebuilding:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\create-signed-installer.ps1 -UsePreparedApk
```

The helper reads the generated password from Windows DPAPI-protected local storage, signs with the permanent automatic key, and verifies the APK's signature and identity. No password prompt is required. It checks the APK version, package, and non-debuggable status. Prepared mode also checks the recorded unsigned APK hash. Current output is `release/ReneR-Studio-v<version>.apk`; legacy ReneR-ID-Print filenames remain recognized for certificate comparisons.

The private key and encrypted credential are under the ignored `private-signing/automatic-v1` directory. Its permissions restrict access to the Windows owner and SYSTEM. Never commit these files or include them in a release. The encrypted credential is bound to this Windows account and installation; copying the folder alone to a different PC is not a portable recovery method. Preserve a secure system backup and deliberately migrate signing before replacing Windows or the account.

Missing or inaccessible signing files cause a failure, never a silent key replacement. `-InitializeAutomaticSigning` is only for an explicitly authorized first setup and refuses to overwrite existing signing state. Normal releases must not use it.

### Signing identity change in v1.0.3

Kirk explicitly authorized a new signing identity because the old password was unavailable. The original key and v1.0.0–v1.0.2 APKs remain preserved. Version 1.0.3 requires uninstalling the old signed app before installation; this clears its private settings and cache. Save any photos that need to be kept before uninstalling. The new app will discover and pair with the printer again.

Later releases must keep the v1.0.3 automatic key, allowing normal updates without uninstalling. Keep the application identifier `com.idphoto.printing` unchanged. The helper verifies the pinned automatic certificate and compares it with previous releases from v1.0.3 onward.

## Publication checklist

1. Confirm the version, repository, exact candidate files and explicit publication authorization. When Kirk requests phone testing first, stop after local preparation until he authorizes publication. Do not infer physical results from permission to publish.
2. Verify the candidate's signatures, package/version, byte size and SHA-256; tie its application source hashes to the release commit. Preserve the exact tested APK instead of rebuilding unchanged source.
3. Review new source, docs and screenshots for credentials, customer data and local configuration. Keep `private-signing/`, `release/`, build output and task checkpoints excluded from Git.
4. Commit only the intended changes, merge to `main` and push without rewriting history. Verify the remote SHA.
5. Wait for **Android quality checks** to succeed on that exact SHA. A queued or running workflow is not a pass.
6. Create and push the version tag at that verified commit. Publish the signed APK and matching SHA256SUMS file as GitHub release assets, with changes, upgrade instructions and honest verification limits.
7. Verify the remote tag's commit, published release state and asset names/sizes/digests. Check the README download and screenshot paths. Do not replace existing tags/assets or rotate keys as a routine release step.

Maintain [CHANGELOG.md](CHANGELOG.md), the README release table and the versioned user guide together. Current screenshots belong in individual collapsible sections; older interfaces remain clearly labeled as historical.
