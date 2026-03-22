# Nucleus

> Nucleus is the all-in-one toolkit for shipping JVM desktop applications. It combines a Gradle plugin, runtime
> libraries, and GitHub Actions to handle performance (AOT), distribution (16 packaging formats), native look & feel (
> decorated windows, dark mode, system colors), and GraalVM Native Image support. Compatible with any JVM application,
> optimized for Compose Desktop.

Docs: https://nucleus.kdroidfilter.com
GitHub: https://github.com/kdroidFilter/Nucleus
Gradle Plugin Portal: https://plugins.gradle.org/plugin/io.github.kdroidfilter.nucleus
Maven Central group: io.github.kdroidfilter
License: MIT

## Why Nucleus?

### Performance

- **JDK 25+ AOT cache (Project Leyden)** — Dramatically faster cold startup with ahead-of-time class loading cache,
  enabled as a simple Gradle flag, no GraalVM required
- **GraalVM Native Image** — ~0.5s cold boot, ~100–150 MB RAM, smaller bundle size (experimental, requires reflection
  configuration)

### Distribution

- **16 packaging formats** — DMG, PKG, NSIS, MSI, AppX, Portable, DEB, RPM, AppImage, Snap, Flatpak, and archives
- **Plug-and-play store distribution** — Store-ready outputs for Mac App Store (PKG), Microsoft Store (AppX),
  Snapcraft (Snap), and Flathub (Flatpak)
- **Code signing & notarization** — Windows (PFX, Azure Trusted Signing) and macOS (Apple Developer ID) with full
  notarization support
- **Auto-update built-in** — Integrated update metadata generation and runtime update library; publish directly to
  GitHub Releases, S3, or generic HTTP server
- **One DSL** — Configure everything from a single `nucleus.application { }` block

### Native Look & Feel

- **Decorated windows** — Custom title bar content (icons, text, gradients) while preserving native window controls and
  behavior on all platforms. Two backends: JBR and JNI
- **Fullscreen sliding overlay** — Native-style sliding title bar in fullscreen (Safari-like on macOS, Edge-like on
  Windows, Firefox-like on Linux)
- **Liquid Glass (macOS 26)** — Automatic SDK version patching via `vtool`, works with any JDK
- **Reactive dark mode detection** — OS-level dark mode listener via JNI (no JNA), triggers recomposition instantly when
  the user changes their theme
- **System accent color & high contrast** — Reactive OS accent color and high contrast detection via JNI on all
  platforms
- **Energy management** — Process/thread energy efficiency mode and screen-awake (caffeine) API
- **Trusted CA certificates** — Import custom CA certs into the bundled JVM at build time; no SSL errors behind
  corporate proxies or ISP filters
- **Platform-accurate Linux rendering** — GNOME Adwaita and KDE Breeze window controls, proper window shape clipping,
  border styling, and full behavior emulation

### CI/CD

- **Reusable GitHub Actions** — `setup-nucleus` composite action + workflows for multi-platform matrix builds, universal
  macOS binaries, and MSIX bundles
- **Deep links & file associations** — Cross-platform protocol handlers and file type registration

## Requirements

| Requirement | Version                 | Note               |
|-------------|-------------------------|--------------------|
| JDK         | 17+ (25+ for AOT cache) | JBR 25 recommended |
| Gradle      | 8.0+                    |                    |
| Kotlin      | 2.0+                    |                    |

---

# Getting Started

## Installation

Add the Nucleus plugin to your `build.gradle.kts`:

```kotlin
plugins {
    id("io.github.kdroidfilter.nucleus") version "<version>"
}
```

The plugin is available on the Gradle Plugin Portal. No additional repository configuration is needed.

### Runtime Libraries (Optional)

Runtime libraries are published on Maven Central:

```kotlin
dependencies {
    implementation(compose.desktop.currentOs)

    // Executable type detection + single instance + deep links + app metadata (NucleusApp)
    implementation("io.github.kdroidfilter:nucleus.core-runtime:<version>")

    // AOT cache runtime detection (includes core-runtime)
    implementation("io.github.kdroidfilter:nucleus.aot-runtime:<version>")

    // Auto-update library with update level detection and post-update events (includes core-runtime)
    implementation("io.github.kdroidfilter:nucleus.updater-runtime:<version>")

    // Native taskbar/dock progress bar and attention requests
    implementation("io.github.kdroidfilter:nucleus.taskbar-progress:<version>")

    // Custom decorated window with native title bar — choose one backend:
    implementation("io.github.kdroidfilter:nucleus.decorated-window-jbr:<version>")  // JBR backend (requires JetBrains Runtime)
    implementation("io.github.kdroidfilter:nucleus.decorated-window-jni:<version>")  // JNI backend (any JVM, GraalVM)

    // Optional design system wrappers for decorated windows:
    implementation("io.github.kdroidfilter:nucleus.decorated-window-jewel:<version>")    // Jewel (IntelliJ)
    implementation("io.github.kdroidfilter:nucleus.decorated-window-material2:<version>") // Material 2
    implementation("io.github.kdroidfilter:nucleus.decorated-window-material3:<version>") // Material 3

    // Reactive dark mode detection
    implementation("io.github.kdroidfilter:nucleus.darkmode-detector:<version>")

    // Reactive system accent color and high contrast detection
    implementation("io.github.kdroidfilter:nucleus.system-color:<version>")

    // Energy efficiency mode and screen-awake API
    implementation("io.github.kdroidfilter:nucleus.energy-manager:<version>")

    // OS trust store integration — merges native certs with JVM defaults
    implementation("io.github.kdroidfilter:nucleus.native-ssl:<version>")

    // java.net.http.HttpClient pre-configured with native OS trust
    implementation("io.github.kdroidfilter:nucleus.native-http:<version>")

    // OkHttp client pre-configured with native OS trust
    implementation("io.github.kdroidfilter:nucleus.native-http-okhttp:<version>")

    // Ktor HttpClient extension for native OS trust (CIO, Java, OkHttp, Apache5)
    implementation("io.github.kdroidfilter:nucleus.native-http-ktor:<version>")

    // Linux HiDPI scale detection (for non-JBR runtimes)
    implementation("io.github.kdroidfilter:nucleus.linux-hidpi:<version>")

    // GraalVM native-image bootstrap (includes linux-hidpi)
    implementation("io.github.kdroidfilter:nucleus.graalvm-runtime:<version>")
}
```

## Minimal Configuration

```kotlin
nucleus.application {
    mainClass = "com.example.MainKt"

    nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Nsis, TargetFormat.Deb)
        packageName = "MyApp"
        packageVersion = "1.0.0"
    }
}
```

## Gradle Tasks

### Development

| Task               | Description                               |
|--------------------|-------------------------------------------|
| `run`              | Run the application from the IDE/terminal |
| `runDistributable` | Run the packaged application image        |

### Packaging

| Task                                     | Description                                                               |
|------------------------------------------|---------------------------------------------------------------------------|
| `packageDistributionForCurrentOS`        | Build all configured formats for the current OS                           |
| `package<Format>`                        | Build a specific format (e.g., `packageDmg`, `packageNsis`, `packageDeb`) |
| `packageReleaseDistributionForCurrentOS` | Same as above with ProGuard release build                                 |
| `createDistributable`                    | Create the application image without an installer                         |
| `createReleaseDistributable`             | Same with ProGuard                                                        |

### GraalVM Native Image

| Task                                                             | Description                                                   |
|------------------------------------------------------------------|---------------------------------------------------------------|
| `runWithNativeAgent`                                             | Run with GraalVM tracing agent to collect reflection metadata |
| `packageGraalvmNative`                                           | Compile as native binary                                      |
| `runGraalvmNative`                                               | Build and run the native image directly                       |
| `packageGraalvmDeb` / `packageGraalvmDmg` / `packageGraalvmNsis` | Package native image as installer                             |

### Utility

| Task                         | Description                                       |
|------------------------------|---------------------------------------------------|
| `suggestModules`             | Suggest JDK modules required by your dependencies |
| `packageUberJarForCurrentOS` | Create a single fat JAR with all dependencies     |

## Output Location

Build artifacts are generated in:

```
build/compose/binaries/main/<format>/
build/compose/binaries/main-release/<format>/   # Release builds
```

Override with:

```kotlin
nativeDistributions {
    outputBaseDir.set(project.layout.buildDirectory.dir("custom-output"))
}
```

## JDK Modules

The plugin does not automatically detect required JDK modules. Use `suggestModules` to identify them:

```bash
./gradlew suggestModules
```

Then declare them in the DSL:

```kotlin
nativeDistributions {
    modules("java.sql", "java.net.http", "jdk.accessibility")
}
```

Or include everything (larger binary):

```kotlin
nativeDistributions {
    includeAllModules = true
}
```

## Application Icons

```kotlin
nativeDistributions {
    macOS {
        iconFile.set(project.file("icons/app.icns"))
        layeredIconDir.set(project.file("icons/MyApp.icon")) // macOS 26+ layered icons
    }
    windows {
        iconFile.set(project.file("icons/app.ico"))
    }
    linux {
        iconFile.set(project.file("icons/app.png"))
    }
}
```

| Platform | Format  | Recommended Size |
|----------|---------|------------------|
| macOS    | `.icns` | 1024x1024        |
| Windows  | `.ico`  | 256x256          |
| Linux    | `.png`  | 512x512          |

## Application Resources

Include extra files in the installation directory via `appResourcesRootDir`:

```kotlin
nativeDistributions {
    appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
}
```

Resource directory structure:

```
resources/
  common/          # Included on all platforms
  macos/           # macOS only
  macos-arm64/     # macOS Apple Silicon only
  macos-x64/       # macOS Intel only
  windows/         # Windows only
  linux/           # Linux only
```

Access at runtime:

```kotlin
val resourcesDir = File(System.getProperty("compose.application.resources.dir"))
```

---

# Configuration

All Nucleus configuration lives inside the `nucleus.application { }` block in your `build.gradle.kts`.

## Overview

```kotlin
nucleus.application {
    mainClass = "com.example.MainKt"
    jvmArgs += listOf("-Xmx512m")

    buildTypes {
        release {
            proguard {
                isEnabled = true
                optimize = true
                obfuscate.set(false)
                configurationFiles.from(project.file("proguard-rules.pro"))
            }
        }
    }

    nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Nsis, TargetFormat.Deb)

        packageName = "MyApp"
        packageVersion = "1.0.0"
        description = "My awesome desktop app"
        vendor = "My Company"
        copyright = "Copyright 2025 My Company"
        homepage = "https://myapp.example.com"
        licenseFile.set(project.file("LICENSE"))

        modules("java.sql", "java.net.http")

        cleanupNativeLibs = true
        enableAotCache = true
        splashImage = "splash.png"
        compressionLevel = CompressionLevel.Maximum
        artifactName = "${name}-${version}-${os}-${arch}.${ext}"

        protocol("MyApp", "myapp")
        fileAssociation(
            mimeType = "application/x-myapp",
            extension = "myapp",
            description = "MyApp Document",
        )

        publish { /* ... */ }

        macOS { /* ... */ }
        windows { /* ... */ }
        linux { /* ... */ }
    }

    graalvm {
        isEnabled = true
        imageName = "my-app"
        javaLanguageVersion = 25
        jvmVendor = JvmVendorSpec.BELLSOFT
        buildArgs.addAll("-H:+AddAllCharsets", "-Djava.awt.headless=false", "-Os")
    }
}
```

## Target Formats

| Format                  | Platform | Task Name         |
|-------------------------|----------|-------------------|
| `TargetFormat.Dmg`      | macOS    | `packageDmg`      |
| `TargetFormat.Pkg`      | macOS    | `packagePkg`      |
| `TargetFormat.Exe`      | Windows  | `packageExe`      |
| `TargetFormat.Msi`      | Windows  | `packageMsi`      |
| `TargetFormat.Nsis`     | Windows  | `packageNsis`     |
| `TargetFormat.NsisWeb`  | Windows  | `packageNsisWeb`  |
| `TargetFormat.Portable` | Windows  | `packagePortable` |
| `TargetFormat.AppX`     | Windows  | `packageAppX`     |
| `TargetFormat.Deb`      | Linux    | `packageDeb`      |
| `TargetFormat.Rpm`      | Linux    | `packageRpm`      |
| `TargetFormat.AppImage` | Linux    | `packageAppImage` |
| `TargetFormat.Snap`     | Linux    | `packageSnap`     |
| `TargetFormat.Flatpak`  | Linux    | `packageFlatpak`  |
| `TargetFormat.Zip`      | All      | `packageZip`      |
| `TargetFormat.Tar`      | All      | `packageTar`      |
| `TargetFormat.SevenZ`   | All      | `packageSevenZ`   |

Target all formats at once:

```kotlin
targetFormats(*TargetFormat.entries.toTypedArray())
```

## Package Metadata

| Property              | Type                  | Default                  | Description                          |
|-----------------------|-----------------------|--------------------------|--------------------------------------|
| `packageName`         | `String`              | Gradle project name      | Application display name             |
| `packageVersion`      | `String`              | Gradle project version   | Application version                  |
| `description`         | `String?`             | `null`                   | Short application description        |
| `vendor`              | `String?`             | `null`                   | Publisher / company name             |
| `copyright`           | `String?`             | `null`                   | Copyright notice                     |
| `homepage`            | `String?`             | `null`                   | Application homepage URL             |
| `licenseFile`         | `RegularFileProperty` | —                        | Path to the license file             |
| `appResourcesRootDir` | `DirectoryProperty`   | —                        | Root directory for bundled resources |
| `outputBaseDir`       | `DirectoryProperty`   | `build/compose/binaries` | Output directory for built packages  |

### Version Formats

| Platform  | Format                        | Example |
|-----------|-------------------------------|---------|
| macOS     | `MAJOR.MINOR.PATCH`           | `1.2.3` |
| Windows   | `MAJOR.MINOR.BUILD`           | `1.2.3` |
| Linux DEB | `[EPOCH:]UPSTREAM[-REVISION]` | `1.2.3` |
| Linux RPM | No dashes                     | `1.2.3` |

## Nucleus-Specific Features

### Native Library Cleanup

Strips `.dll`, `.so`, `.dylib` files for non-target platforms from dependency JARs, reducing package size significantly.

```kotlin
nativeDistributions {
    cleanupNativeLibs = true
}
```

### AOT Cache (JDK 25+)

Generates an ahead-of-time compilation cache for faster startup:

```kotlin
nativeDistributions {
    enableAotCache = true
}
```

Requires JDK 25+ and that the application self-terminates during the training run.

### Splash Screen

```kotlin
nativeDistributions {
    splashImage = "splash.png" // Relative to appResources
}
```

### Artifact Naming

```kotlin
nativeDistributions {
    artifactName = "${name}-${version}-${os}-${arch}.${ext}"
}
```

| Variable     | Description      | Example                     |
|--------------|------------------|-----------------------------|
| `${name}`    | Package name     | `MyApp`                     |
| `${version}` | Package version  | `1.0.0`                     |
| `${os}`      | Operating system | `macos`, `windows`, `linux` |
| `${arch}`    | Architecture     | `amd64`, `arm64`            |
| `${ext}`     | File extension   | `dmg`, `exe`, `deb`         |

### Compression Level

| Level                      | Description        |
|----------------------------|--------------------|
| `CompressionLevel.Store`   | No compression     |
| `CompressionLevel.Fast`    | Fast compression   |
| `CompressionLevel.Normal`  | Balanced (default) |
| `CompressionLevel.Maximum` | Best compression   |

### Deep Links (Protocol Handler)

Register a custom URL protocol across all platforms:

```kotlin
nativeDistributions {
    protocol("MyApp", "myapp")
    // Registers myapp:// protocol
}
```

- **macOS**: `CFBundleURLTypes` in `Info.plist`
- **Windows**: Registry entries via NSIS/MSI
- **Linux**: `MimeType` in `.desktop` file

### File Associations

```kotlin
nativeDistributions {
    fileAssociation(
        mimeType = "application/x-myapp",
        extension = "myapp",
        description = "MyApp Document",
        linuxIconFile = project.file("icons/file.png"),
        windowsIconFile = project.file("icons/file.ico"),
        macOSIconFile = project.file("icons/file.icns"),
    )
}
```

Multiple associations are supported by calling `fileAssociation()` multiple times.

### Trusted CA Certificates

Import custom CA certificates (PEM or DER format) into the bundled JVM's `cacerts` keystore at build time. Useful when
your app must connect to HTTPS servers behind a corporate proxy, VPN gateway, or internal PKI that uses a private root
CA not present in the default JDK trust store.

```kotlin
nativeDistributions {
    trustedCertificates.from(files(
        "certs/corp-root-ca.crt",
        "certs/proxy-ca.pem",
    ))
}
```

**How it works:**

1. After `createRuntimeImage` (jlink), Nucleus copies the runtime to `build/compose/tmp/<appName>/runtime-patched/`.
2. For each certificate, it runs `keytool -import -trustcacerts` against `lib/security/cacerts` in the copy.
3. Both `createDistributable` and `createSandboxedDistributable` use the patched runtime, so all packaging formats embed
   the trusted certificate.

**Alias generation:** Each certificate is imported under `<filename>-<sha256-8chars>` (e.g. `corp-root-ca-3a1f8b2c`),
guaranteeing uniqueness even when multiple files share the same name.

**Gradle task:** `patchCaCertificates` — only registered when `trustedCertificates` is non-empty. Runs automatically as
a dependency of `createDistributable`.

## ProGuard (Release Builds)

```kotlin
buildTypes {
    release {
        proguard {
            version = "7.8.1"
            isEnabled = true
            optimize = true
            obfuscate.set(false)
            joinOutputJars.set(true)
            configurationFiles.from(project.file("proguard-rules.pro"))
        }
    }
}
```

Release build tasks are suffixed with `Release`:

```bash
./gradlew packageReleaseDmg
./gradlew packageReleaseNsis
./gradlew packageReleaseDistributionForCurrentOS
```

## Full DSL Tree

```
nucleus.application {
    mainClass
    jvmArgs
    buildTypes {
        release {
            proguard { isEnabled, version, optimize, obfuscate, joinOutputJars, configurationFiles }
        }
    }
    nativeDistributions {
        targetFormats(...)
        packageName, packageVersion, description, vendor, copyright, homepage
        licenseFile, appResourcesRootDir, outputBaseDir
        modules(...), includeAllModules
        cleanupNativeLibs, enableAotCache, splashImage
        compressionLevel, artifactName
        protocol(name, vararg schemes)
        fileAssociation(mimeType, extension, description, linuxIconFile?, windowsIconFile?, macOSIconFile?)
        publish { github { ... }, s3 { ... }, generic { ... } }
        macOS { iconFile, bundleID, dockName, appCategory, layeredIconDir, macOsSdkVersion, signing { ... }, notarization { ... }, dmg { ... }, infoPlist { ... } }
        windows { iconFile, upgradeUuid, signing { ... }, nsis { ... }, appx { ... } }
        linux { iconFile, debMaintainer, debDepends, rpmRequires, appImage { ... }, snap { ... }, flatpak { ... } }
    }
    graalvm {
        isEnabled, imageName, javaLanguageVersion, jvmVendor, march, buildArgs, nativeImageConfigBaseDir
    }
}
```

---

# macOS Targets

## Formats

| Format | Extension | Auto-Update | Sandboxed         |
|--------|-----------|-------------|-------------------|
| DMG    | `.dmg`    | Yes         | No                |
| PKG    | `.pkg`    | Yes         | Yes (App Sandbox) |

## General macOS Settings

```kotlin
nativeDistributions {
    macOS {
        bundleID = "com.example.myapp"
        dockName = "MyApp"
        appCategory = "public.app-category.utilities"
        minimumSystemVersion = "12.0"
        iconFile.set(project.file("icons/app.icns"))
        layeredIconDir.set(project.file("icons/MyApp.icon"))

        entitlementsFile.set(project.file("entitlements.plist"))
        runtimeEntitlementsFile.set(project.file("runtime-entitlements.plist"))

        infoPlist {
            extraKeysRawXml = """
                <key>NSMicrophoneUsageDescription</key>
                <string>This app requires microphone access.</string>
            """.trimIndent()
        }
    }
}
```

## macOS 26 Window Appearance (Liquid Glass)

macOS 26 introduces Liquid Glass window chrome: larger traffic light buttons and more rounded window corners. Nucleus *
*automatically patches** the app launcher's `LC_BUILD_VERSION` via `vtool` so that AppKit enables Liquid Glass. This
works with **any JDK**.

```kotlin
macOS {
    macOsSdkVersion = "26.0"  // default — enables Liquid Glass
    // macOsSdkVersion = null // disable SDK version patching
}
```

The `run` task uses a cached patched copy of the JVM. Distributable builds patch the launcher before signing. Requires
Xcode Command Line Tools.

## DMG Customization

```kotlin
macOS {
    dmg {
        title = "${productName} ${version}"
        iconSize = 128
        iconTextSize = 12

        window {
            x = 400
            y = 100
            width = 540
            height = 380
        }

        background.set(project.file("packaging/dmg-background.png"))
        // or: backgroundColor = "#FFFFFF"

        format = DmgFormat.UDZO

        content(x = 130, y = 220, type = DmgContentType.File, name = "MyApp.app")
        content(x = 410, y = 220, type = DmgContentType.Link, path = "/Applications")
    }
}
```

## Layered Icons (macOS 26+)

macOS 26 introduced layered icons with dynamic tilt and depth effects. Provide a `.icon` directory created with Xcode
26+ or Apple Icon Composer:

```kotlin
macOS {
    iconFile.set(project.file("icons/app.icns"))       // fallback for older macOS
    layeredIconDir.set(project.file("icons/MyApp.icon")) // macOS 26+
}
```

## Universal Binaries

Build on both arm64 and x64, then merge with `lipo` using the `build-macos-universal` composite action.

## App Sandbox (PKG)

PKG targets automatically use the sandboxed build pipeline with native library extraction, JVM args injection, and code
signing.

```kotlin
macOS {
    provisioningProfile.set(project.file("packaging/MyApp.provisionprofile"))
    runtimeProvisioningProfile.set(project.file("packaging/MyApp_Runtime.provisionprofile"))
}
```

## Signing & Notarization

```kotlin
macOS {
    signing {
        sign.set(true)
        identity.set("Developer ID Application: My Company (TEAMID)")
    }
    notarization {
        appleID.set("dev@example.com")
        password.set("@keychain:AC_PASSWORD")
        teamID.set("TEAMID")
    }
}
```

## macOS DSL Reference

| Property                     | Type                  | Default         | Description                                             |
|------------------------------|-----------------------|-----------------|---------------------------------------------------------|
| `iconFile`                   | `RegularFileProperty` | —               | `.icns` icon file                                       |
| `bundleID`                   | `String?`             | `null`          | macOS bundle identifier                                 |
| `dockName`                   | `String?`             | `null`          | Name displayed in the Dock                              |
| `appCategory`                | `String?`             | `null`          | App Store / Finder category                             |
| `minimumSystemVersion`       | `String?`             | `null`          | Minimum macOS version                                   |
| `macOsSdkVersion`            | `String?`             | `"26.0"`        | SDK version for Liquid Glass. Set to `null` to disable. |
| `layeredIconDir`             | `DirectoryProperty`   | —               | `.icon` directory for macOS 26+                         |
| `installationPath`           | `String?`             | `/Applications` | PKG install location / DMG symlink target               |
| `entitlementsFile`           | `RegularFileProperty` | —               | Entitlements plist                                      |
| `runtimeEntitlementsFile`    | `RegularFileProperty` | —               | Runtime entitlements plist                              |
| `provisioningProfile`        | `RegularFileProperty` | —               | Provisioning profile                                    |
| `runtimeProvisioningProfile` | `RegularFileProperty` | —               | Runtime provisioning profile                            |

---

# Windows Targets

## Formats

| Format   | Extension | Auto-Update | Sandboxed |
|----------|-----------|-------------|-----------|
| NSIS     | `.exe`    | Yes         | No        |
| NSIS Web | `.exe`    | Yes         | No        |
| MSI      | `.msi`    | Yes         | No        |
| AppX     | `.appx`   | No (Store)  | No        |
| Portable | `.exe`    | No          | No        |

## General Windows Settings

```kotlin
nativeDistributions {
    windows {
        iconFile.set(project.file("icons/app.ico"))
        upgradeUuid = "d24e3b8d-3e9b-4cc7-a5d8-5e2d1f0c9f1b"
        console = false
        perUserInstall = true
        menuGroup = "My Company"
        dirChooser = true
    }
}
```

## NSIS Installer

```kotlin
windows {
    nsis {
        oneClick = false
        allowElevation = true
        perMachine = true
        allowToChangeInstallationDirectory = true
        createDesktopShortcut = true
        createStartMenuShortcut = true
        runAfterFinish = true
        deleteAppDataOnUninstall = false
        multiLanguageInstaller = true
        installerLanguages = listOf("en_US", "fr_FR", "de_DE")
        installerIcon.set(project.file("packaging/installer.ico"))
        uninstallerIcon.set(project.file("packaging/uninstaller.ico"))
        license.set(project.file("LICENSE"))
        includeScript.set(project.file("packaging/custom.nsi"))
    }
}
```

## AppX (Windows Store / MSIX)

```kotlin
windows {
    appx {
        applicationId = "MyApp"
        publisherDisplayName = "My Company"
        displayName = "My App"
        publisher = "CN=D541E802-6D30-446A-864E-2E8ABD2DAA5E"
        identityName = "MyCompany.MyApp"
        languages = listOf("en-US", "fr-FR")
        backgroundColor = "#001F3F"
    }
}
```

## Code Signing

```kotlin
windows {
    signing {
        enabled = true
        certificateFile.set(file("certs/certificate.pfx"))
        certificatePassword = "your-password"
        algorithm = SigningAlgorithm.Sha256
        timestampServer = "http://timestamp.digicert.com"
    }
}
```

Azure Trusted Signing:

```kotlin
windows {
    signing {
        enabled = true
        azureTenantId = "your-tenant-id"
        azureEndpoint = "https://your-region.codesigning.azure.net"
        azureCertificateProfileName = "your-profile"
        azureCodeSigningAccountName = "your-account"
    }
}
```

---

# Linux Targets

## Formats

| Format   | Extension   | Auto-Update | Sandboxed |
|----------|-------------|-------------|-----------|
| DEB      | `.deb`      | Yes         | No        |
| RPM      | `.rpm`      | Yes         | No        |
| AppImage | `.AppImage` | Yes         | No        |
| Snap     | `.snap`     | No (Store)  | No        |
| Flatpak  | `.flatpak`  | No          | Yes       |

## General Linux Settings

```kotlin
nativeDistributions {
    linux {
        iconFile.set(project.file("icons/app.png"))
        shortcut = true
        packageName = "myapp"
        appRelease = "1"
        appCategory = "Utility"
        debMaintainer = "Your Name <dev@example.com>"
        debDepends = listOf("libfuse2", "libgtk-3-0")
        rpmRequires = listOf("gtk3", "libX11")
    }
}
```

## AppImage

```kotlin
linux {
    appImage {
        category = AppImageCategory.Utility
        genericName = "My Application"
        synopsis = "A short description of the app"
    }
}
```

## Snap

```kotlin
linux {
    snap {
        confinement = SnapConfinement.Strict
        grade = SnapGrade.Stable
        summary = "My awesome desktop app"
        base = "core22"
        plugs = listOf(
            SnapPlug.Desktop, SnapPlug.Home, SnapPlug.X11,
            SnapPlug.Wayland, SnapPlug.Network, SnapPlug.AudioPlayback,
        )
    }
}
```

## Flatpak

```kotlin
linux {
    flatpak {
        runtime = "org.freedesktop.Platform"
        runtimeVersion = "23.08"
        sdk = "org.freedesktop.Sdk"
        finishArgs = listOf(
            "--share=ipc", "--socket=x11", "--socket=wayland",
            "--socket=pulseaudio", "--device=dri", "--filesystem=home",
        )
    }
}
```

---

# Sandboxing

Nucleus automatically manages a store build pipeline for store formats. When your target formats include PKG, AppX, or
Flatpak, the plugin splits the build into two parallel pipelines: one for direct-distribution and one for store formats
that require special handling.

## Store Formats

| Format  | OS      | Sandbox Type                                |
|---------|---------|---------------------------------------------|
| PKG     | macOS   | App Sandbox                                 |
| AppX    | Windows | MSIX packaging (full trust — not sandboxed) |
| Flatpak | Linux   | Flatpak sandbox                             |

## What the Sandboxed Pipeline Does

1. **Extract native libraries from JARs** — Sandboxed apps cannot load unsigned native code from temp directories
2. **Strip native libraries from JARs** — Avoid duplication
3. **Prepare sandboxed app resources** — Merge app resources + extracted native libs
4. **Inject sandboxing JVM arguments** — Redirect native library loading to pre-extracted locations
5. **Sign native libraries** (macOS) — Individually code-sign `.dylib` files
6. **Handle Skiko and icudtl.dat** — Adjust library paths

---

# Code Signing

## Windows — PFX Certificate

```kotlin
windows {
    signing {
        enabled = true
        certificateFile.set(file("certs/certificate.pfx"))
        certificatePassword = "your-password"
        algorithm = SigningAlgorithm.Sha256
        timestampServer = "http://timestamp.digicert.com"
    }
}
```

| Property                 | Type                  | Default  | Description                         |
|--------------------------|-----------------------|----------|-------------------------------------|
| `enabled`                | `Boolean`             | `false`  | Enable code signing                 |
| `certificateFile`        | `RegularFileProperty` | —        | Path to `.pfx` / `.p12` certificate |
| `certificatePassword`    | `String?`             | `null`   | Certificate password                |
| `certificateSha1`        | `String?`             | `null`   | SHA-1 thumbprint                    |
| `certificateSubjectName` | `String?`             | `null`   | Subject name                        |
| `algorithm`              | `SigningAlgorithm`    | `Sha256` | `Sha1`, `Sha256`, `Sha512`          |
| `timestampServer`        | `String?`             | `null`   | Timestamp server URL                |

## Windows — Azure Trusted Signing

```kotlin
windows {
    signing {
        enabled = true
        azureTenantId = "your-tenant-id"
        azureEndpoint = "https://your-region.codesigning.azure.net"
        azureCertificateProfileName = "your-profile"
        azureCodeSigningAccountName = "your-account"
    }
}
```

## macOS — Signing & Notarization

```kotlin
macOS {
    signing {
        sign.set(true)
        identity.set("Developer ID Application: My Company (TEAMID)")
    }
    notarization {
        appleID.set("dev@example.com")
        password.set("@keychain:AC_PASSWORD")
        teamID.set("TEAMID")
    }
}
```

---

# Auto Update

Nucleus provides a complete auto-update solution compatible with the electron-builder update format.

## Updatable Formats

| Platform | Updatable Formats       |
|----------|-------------------------|
| macOS    | DMG, ZIP                |
| Windows  | EXE/NSIS, NSIS Web, MSI |
| Linux    | DEB, RPM, AppImage      |

## Update Metadata (YML Files)

Three YAML files per release: `latest-mac.yml`, `latest.yml` (Windows), `latest-linux.yml`

```yaml
version: 1.2.3
files:
  - url: MyApp-1.2.3-macos-arm64.dmg
    sha512: VkJl1gDqcBHYbYhMb0HRI...
    size: 102400000
releaseDate: '2025-06-15T10:30:00.000Z'
```

## Release Channels

| Channel  | YML Files      | Tag Pattern      |
|----------|----------------|------------------|
| `latest` | `latest-*.yml` | `v1.0.0`         |
| `beta`   | `beta-*.yml`   | `v1.0.0-beta.1`  |
| `alpha`  | `alpha-*.yml`  | `v1.0.0-alpha.1` |

## Runtime Library

### Quick Start

```kotlin
import io.github.kdroidfilter.nucleus.updater.NucleusUpdater
import io.github.kdroidfilter.nucleus.updater.UpdateResult
import io.github.kdroidfilter.nucleus.updater.provider.GitHubProvider

val updater = NucleusUpdater {
    provider = GitHubProvider(owner = "myorg", repo = "myapp")
}

when (val result = updater.checkForUpdates()) {
    is UpdateResult.Available -> {
        println("Update available: ${result.info.version}")
        updater.downloadUpdate(result.info).collect { progress ->
            println("${progress.percent.toInt()}%")
            if (progress.file != null) {
                updater.installAndRestart(progress.file!!)
            }
        }
    }
    is UpdateResult.NotAvailable -> println("Up to date")
    is UpdateResult.Error -> println("Error: ${result.exception.message}")
}
```

### Configuration

```kotlin
NucleusUpdater {
    currentVersion = "1.0.0"       // auto-detected from jpackage.app-version
    provider = GitHubProvider(owner = "myorg", repo = "myapp")
    channel = "latest"             // "latest", "beta", or "alpha"
    allowDowngrade = false
    allowPrerelease = false
    executableType = null           // auto-detected
}
```

### Providers

**GitHub Releases**:

```kotlin
provider = GitHubProvider(owner = "myorg", repo = "myapp", token = "ghp_...")
```

**Generic HTTP Server**:

```kotlin
provider = GenericProvider(baseUrl = "https://updates.example.com")
```

### API Reference

| Method                                                     | Description                                                       |
|------------------------------------------------------------|-------------------------------------------------------------------|
| `isUpdateSupported(): Boolean`                             | Check if the current executable type supports auto-update         |
| `suspend checkForUpdates(): UpdateResult`                  | Check for a newer version (includes `UpdateLevel`)                |
| `downloadUpdate(info: UpdateInfo): Flow<DownloadProgress>` | Download the installer with progress                              |
| `installAndRestart(installerFile: File)`                   | Launch installer, exit, relaunch after install                    |
| `installAndQuit(installerFile: File)`                      | Launch installer and exit without relaunching                     |
| `consumeUpdateEvent(): UpdateEvent?`                       | Returns post-update event if app was just updated, then clears it |
| `wasJustUpdated(): Boolean`                                | Non-consuming check — `true` if app was launched after an update  |

```kotlin
data class DownloadProgress(
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val percent: Double,       // 0.0 .. 100.0
    val file: File? = null,    // Non-null on the final emission
)

sealed class UpdateResult {
    data class Available(val info: UpdateInfo, val level: UpdateLevel)
    data object NotAvailable
    data class Error(val exception: UpdateException)
}

enum class UpdateLevel { MAJOR, MINOR, PATCH, PRE_RELEASE }

data class UpdateEvent(
    val previousVersion: String,
    val newVersion: String,
    val updateLevel: UpdateLevel,
)
```

### Compose Desktop Integration

```kotlin
@Composable
fun UpdateBanner() {
    val updater = remember {
        NucleusUpdater {
            provider = GitHubProvider(owner = "myorg", repo = "myapp")
        }
    }

    var status by remember { mutableStateOf("Checking for updates...") }
    var progress by remember { mutableStateOf(-1.0) }
    var downloadedFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(Unit) {
        when (val result = updater.checkForUpdates()) {
            is UpdateResult.Available -> {
                status = "Downloading v${result.info.version}..."
                updater.downloadUpdate(result.info).collect {
                    progress = it.percent
                    if (it.file != null) {
                        downloadedFile = it.file
                        status = "Ready to install v${result.info.version}"
                    }
                }
            }
            is UpdateResult.NotAvailable -> status = "Up to date"
            is UpdateResult.Error -> status = "Error: ${result.exception.message}"
        }
    }

    Column {
        Text(status)
        if (progress in 0.0..99.9) {
            LinearProgressIndicator(progress = { (progress / 100.0).toFloat() })
        }
        downloadedFile?.let { file ->
            Button(onClick = { updater.installAndRestart(file) }) {
                Text("Install & Restart")
            }
        }
    }
}
```

### Using a Native HTTP Client

By default, the updater uses a plain `java.net.http.HttpClient` backed by the JDK trust store. On machines with
enterprise proxies, corporate CAs, or user-installed certificates, HTTPS requests may fail with `SSLHandshakeException`.

Inject a client built with `NativeHttpClient` to trust OS-level certificates:

```kotlin
dependencies {
    implementation("io.github.kdroidfilter:nucleus.updater-runtime:<version>")
    implementation("io.github.kdroidfilter:nucleus.native-http:<version>")
}
```

```kotlin
import io.github.kdroidfilter.nucleus.nativehttp.NativeHttpClient
import io.github.kdroidfilter.nucleus.updater.NucleusUpdater
import io.github.kdroidfilter.nucleus.updater.provider.GitHubProvider

val updater = NucleusUpdater {
    provider = GitHubProvider(owner = "myorg", repo = "myapp")
    httpClient = NativeHttpClient.create()
}
```

---

# Publishing

## GitHub Releases

```kotlin
publish {
    publishMode = PublishMode.Auto // Never, Auto, Always
    github {
        enabled = true
        owner = "myorg"
        repo = "myapp"
        token = System.getenv("GITHUB_TOKEN")
        channel = ReleaseChannel.Latest  // Latest, Beta, Alpha
        releaseType = ReleaseType.Release // Release, Draft, Prerelease
    }
}
```

## Amazon S3

```kotlin
publish {
    s3 {
        enabled = true
        bucket = "my-updates-bucket"
        region = "us-east-1"
        path = "releases/myapp"
        acl = "public-read"
    }
}
```

## Generic HTTP Server

```kotlin
publish {
    generic {
        enabled = true
        url = "https://updates.example.com/releases/"
        channel = ReleaseChannel.Latest
        useMultipleRangeRequest = true
    }
}
```

---

# CI/CD

## `setup-nucleus` Action

Sets up JBR 25, packaging tools, Gradle, and Node.js.

```yaml
- uses: kdroidFilter/Nucleus/.github/actions/setup-nucleus@main
  with:
    jbr-version: '25.0.2b329.66'
    packaging-tools: 'true'
    flatpak: 'true'
    snap: 'true'
    setup-gradle: 'true'
    setup-node: 'true'
```

| Input              | Default         | Description                         |
|--------------------|-----------------|-------------------------------------|
| `jbr-version`      | `25.0.2b329.66` | JBR version                         |
| `jbr-variant`      | `jbrsdk`        | JBR variant                         |
| `jbr-download-url` | —               | Override complete JBR download URL  |
| `packaging-tools`  | `true`          | Install xvfb, rpm, fakeroot (Linux) |
| `flatpak`          | `false`         | Install Flatpak + SDK (Linux)       |
| `snap`             | `false`         | Install Snapcraft (Linux)           |
| `setup-gradle`     | `true`          | Setup Gradle                        |
| `setup-node`       | `false`         | Setup Node.js                       |

## Build Matrix

```yaml
strategy:
  fail-fast: false
  matrix:
    include:
      - os: ubuntu-latest
        arch: amd64
      - os: ubuntu-24.04-arm
        arch: arm64
      - os: windows-latest
        arch: amd64
      - os: windows-11-arm
        arch: arm64
      - os: macos-latest
        arch: arm64
      - os: macos-15-intel
        arch: amd64
```

## Version from Tag

```kotlin
val releaseVersion = System.getenv("RELEASE_VERSION")
    ?.removePrefix("v")
    ?.takeIf { it.isNotBlank() }
    ?: "1.0.0"

nucleus.application {
    nativeDistributions {
        packageVersion = releaseVersion
    }
}
```

## Composite Actions Reference

| Action                     | Usage                                                                | Description                                      |
|----------------------------|----------------------------------------------------------------------|--------------------------------------------------|
| `setup-nucleus`            | `kdroidFilter/Nucleus/.github/actions/setup-nucleus@main`            | Setup JBR 25, packaging tools, Gradle, Node.js   |
| `setup-macos-signing`      | `kdroidFilter/Nucleus/.github/actions/setup-macos-signing@main`      | Create keychain and import certificates          |
| `build-macos-universal`    | `kdroidFilter/Nucleus/.github/actions/build-macos-universal@main`    | Merge arm64 + x64 via `lipo`, sign, package      |
| `build-windows-appxbundle` | `kdroidFilter/Nucleus/.github/actions/build-windows-appxbundle@main` | Combine amd64 + arm64 `.appx` into `.msixbundle` |
| `generate-update-yml`      | `kdroidFilter/Nucleus/.github/actions/generate-update-yml@main`      | Generate update metadata YML files               |
| `publish-release`          | `kdroidFilter/Nucleus/.github/actions/publish-release@main`          | Create GitHub Release with all artifacts         |

## Required Secrets Summary

| Secret                         | Used By          | Description                                  |
|--------------------------------|------------------|----------------------------------------------|
| `GITHUB_TOKEN`                 | Release workflow | Auto-provided by GitHub Actions              |
| `WIN_CSC_LINK`                 | Build (Windows)  | Base64-encoded `.pfx` certificate            |
| `WIN_CSC_KEY_PASSWORD`         | Build (Windows)  | Certificate password                         |
| `MAC_CERTIFICATES_P12`         | Universal macOS  | Base64-encoded `.p12`                        |
| `MAC_CERTIFICATES_PASSWORD`    | Universal macOS  | Password for `.p12`                          |
| `MAC_DEVELOPER_ID_APPLICATION` | Universal macOS  | Developer ID Application identity            |
| `MAC_APP_STORE_APPLICATION`    | Universal macOS  | 3rd Party Mac Developer Application identity |
| `MAC_APP_STORE_INSTALLER`      | Universal macOS  | 3rd Party Mac Developer Installer identity   |
| `MAC_PROVISIONING_PROFILE`     | Universal macOS  | Base64-encoded provisioning profile          |
| `MAC_NOTARIZATION_APPLE_ID`    | Universal macOS  | Apple ID for notarization                    |
| `MAC_NOTARIZATION_PASSWORD`    | Universal macOS  | App-specific password for notarization       |
| `MAC_NOTARIZATION_TEAM_ID`     | Universal macOS  | Apple Team ID                                |

---

# Runtime APIs

## Executable Type Detection

```kotlin
import io.github.kdroidfilter.nucleus.core.runtime.ExecutableRuntime
import io.github.kdroidfilter.nucleus.core.runtime.ExecutableType

val type: ExecutableType = ExecutableRuntime.type()

if (ExecutableRuntime.isDev()) { /* Running via ./gradlew run */ }
if (ExecutableRuntime.isDmg()) { /* Installed from DMG */ }
if (ExecutableRuntime.isNsis()) { /* Installed from NSIS */ }
```

Available types: `DEV`, `DMG`, `PKG`, `EXE`, `MSI`, `NSIS`, `NSIS_WEB`, `PORTABLE`, `APPX`, `DEB`, `RPM`, `SNAP`,
`FLATPAK`, `APPIMAGE`, `ZIP`, `TAR`, `SEVEN_Z`

The plugin injects a `nucleus.executable.type` system property at packaging time.

## AOT Cache

```kotlin
import io.github.kdroidfilter.nucleus.aot.runtime.AotRuntime
import io.github.kdroidfilter.nucleus.aot.runtime.AotRuntimeMode

AotRuntime.isTraining()  // true during AOT cache generation
AotRuntime.isRuntime()   // true when AOT cache is loaded
AotRuntime.mode()        // TRAINING, RUNTIME, or OFF
```

Training mode pattern (app must self-terminate):

```kotlin
fun main() {
    if (AotRuntime.isTraining()) {
        Thread({
            Thread.sleep(30_000)
            System.exit(0)
        }, "aot-timer").apply { isDaemon = true; start() }
    }

    if (!AotRuntime.isTraining()) {
        initializeHeavyResources()
    }

    application {
        Window(onCloseRequest = ::exitApplication) { App() }
    }
}
```

## Single Instance

```kotlin
import io.github.kdroidfilter.nucleus.core.runtime.SingleInstanceManager

application {
    var restoreRequested by remember { mutableStateOf(false) }

    val isSingle = remember {
        SingleInstanceManager.isSingleInstance(
            onRestoreFileCreated = {
                // New instance: write deep link data for the primary instance
            },
            onRestoreRequest = {
                // Primary instance: notify UI to bring window to front
                restoreRequested = true
            },
        )
    }

    if (!isSingle) {
        exitApplication()
        return@application
    }

    Window(onCloseRequest = ::exitApplication) {
        LaunchedEffect(restoreRequested) {
            if (restoreRequested) {
                window.toFront()
                restoreRequested = false
            }
        }
        App()
    }
}
```

Uses file-based locking (`java.nio.channels.FileLock`). Configurable:

```kotlin
SingleInstanceManager.configuration = SingleInstanceManager.Configuration(
    lockFilesDir = Paths.get(System.getProperty("java.io.tmpdir")),
    lockIdentifier = "com.example.myapp",
)
```

## Deep Links

```kotlin
import io.github.kdroidfilter.nucleus.core.runtime.DeepLinkHandler

// In build.gradle.kts:
// nativeDistributions { protocol("MyApp", "myapp") }

fun main(args: Array<String>) {
    DeepLinkHandler.register(args) { uri ->
        println("Received deep link: $uri")
    }

    val currentUri = DeepLinkHandler.uri
}
```

Integration with SingleInstanceManager:

```kotlin
DeepLinkHandler.register(args) { uri -> handleDeepLink(uri) }

val isSingle = SingleInstanceManager.isSingleInstance(
    onRestoreFileCreated = { DeepLinkHandler.writeUriTo(this) },
    onRestoreRequest = { DeepLinkHandler.readUriFrom(this); restoreRequested = true },
)
```

| Platform | Mechanism                            |
|----------|--------------------------------------|
| macOS    | Apple Events (`setOpenURIHandler`)   |
| Windows  | CLI argument via registry handler    |
| Linux    | CLI argument via `.desktop` MimeType |

## Decorated Window

Custom title bar with native window controls. Two backends: `decorated-window-jbr` (JetBrains Runtime) and
`decorated-window-jni` (any JVM, including GraalVM).

Both backends expose the same public API. Choose based on your runtime:

- **JBR backend**: Battle-tested, uses JBR's `CustomTitleBar` API. Requires JetBrains Runtime. Known issue: window
  cannot open in maximized state on Windows.
- **JNI backend**: No JBR dependency, works on any JVM. Fixes resize artifacts (macOS, Windows), true fullscreen on
  Windows, DWM dark mode sync. Supports GraalVM native-image.

### Quick Start

```kotlin
import io.github.kdroidfilter.nucleus.window.*

fun main() = application {
    NucleusDecoratedWindowTheme(isDark = true) {
        DecoratedWindow(
            onCloseRequest = ::exitApplication,
            title = "My App",
        ) {
            TitleBar(
                modifier = Modifier
                    .newFullscreenControls()   // sliding overlay title bar in fullscreen
                    .macOSLargeCornerRadius()   // 26pt corner radius (Finder/Safari style)
            ) { state ->
                Text(title, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            MyContent()
        }
    }
}
```

### Components

- `DecoratedWindow` — Drop-in replacement for Compose `Window()`. Manages window state and platform decorations.
  `onCloseRequest` intercepts macOS Cmd+Q and Dock quit events.
- `DecoratedDialog` — Same concept for dialogs. Auto-centers on parent window (JNI only).
- `TitleBar` / `DialogTitleBar` — Platform-dispatched title bar. Content uses
  `Modifier.align(Alignment.Start/CenterHorizontally/End)`.
- `NucleusDecoratedWindowTheme` — Provides styling via `CompositionLocal`. Accepts custom `DecoratedWindowStyle` and
  `TitleBarStyle`.

### `controlButtonsDirection` — Independent Button Placement

Decouples window control buttons from the content layout direction:

```kotlin
TitleBar(
    controlButtonsDirection = ControlButtonsDirection.Ltr,
) { state ->
    // Content follows LocalLayoutDirection (e.g. RTL for Arabic),
    // but control buttons always stay on the right side.
    Text(title, modifier = Modifier.align(Alignment.CenterHorizontally))
}
```

| Value    | Behavior                                           |
|----------|----------------------------------------------------|
| `Auto`   | Follows Compose `LocalLayoutDirection` (default)   |
| `System` | Follows the JVM platform locale                    |
| `Ltr`    | Always places buttons as in a left-to-right layout |
| `Rtl`    | Always places buttons as in a right-to-left layout |

### `Modifier.clientRegion()` — Interactive Title Bar Regions (JBR only)

Registers composable elements as interactive areas within the title bar, so clicks go to the element instead of
triggering window dragging:

```kotlin
TitleBar { state ->
    Dropdown(
        modifier = Modifier.align(Alignment.Start).clientRegion("main_menu"),
    ) { Text("File") }
}
```

The JNI module handles this automatically via native platform APIs — no `clientRegion` needed.

### `backgroundContent` — Custom Title Bar Background

Composable rendered behind title bar content, for custom gradients, shapes, or images:

```kotlin
TitleBar(
    backgroundContent = {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = Color(0xFF6200EE), size = Size(size.width / 2, size.height))
        }
    },
) { state -> /* content */ }
```

### `newFullscreenControls()` — Sliding Overlay Title Bar

Safari-like on macOS, Edge-like on Windows, Firefox-like on Linux. Title bar hides in fullscreen and slides down on
pointer-near-top. Available on all platforms with `decorated-window-jni`.

### `macOSLargeCornerRadius()` — 26pt Corner Radius

Applies 26pt window corner radius on macOS by installing an invisible NSToolbar. Traffic lights repositioned to match
Finder/Safari. macOS + `decorated-window-jni` only.

### Platform Comparison Summary

| Capability                          |    JBR     | JNI |
|-------------------------------------|:----------:|:---:|
| Custom title bar                    |     ✅      |  ✅  |
| Works on any JDK                    |            |  ✅  |
| GraalVM native-image                |            |  ✅  |
| No resize artifacts (macOS/Windows) | macOS only |  ✅  |
| True fullscreen (Windows)           |            |  ✅  |
| DWM dark mode sync (Windows)        |            |  ✅  |
| 26pt corner radius (macOS)          |            |  ✅  |
| Fullscreen sliding title bar (all)  |            |  ✅  |
| RTL live hot-swap (all)             |            |  ✅  |

## Decorated Window — Design System Wrappers

Three optional modules provide automatic color mapping for decorated windows:

### Material 3

```kotlin
import io.github.kdroidfilter.nucleus.window.material.*

MaterialTheme(colorScheme = colorScheme) {
    MaterialDecoratedWindow(onCloseRequest = ::exitApplication, title = "My App") {
        MaterialTitleBar { state -> Text(title) }
        Surface(modifier = Modifier.fillMaxSize()) { /* content */ }
    }
}
```

Components: `MaterialDecoratedWindow`, `MaterialDecoratedDialog`, `MaterialTitleBar`, `MaterialDialogTitleBar`. Color
mapping: surface → title bar background, onSurface → content, outlineVariant → borders, error → close button hover.

### Material 2

Same API as Material 3 but reads from `MaterialTheme.colors` instead of `MaterialTheme.colorScheme`. Import from
`io.github.kdroidfilter.nucleus.window.material2.*`.

### Jewel (IntelliJ Theme)

```kotlin
IntUiTheme(theme = theme, styling = ComponentStyling.default()) {
    JewelDecoratedWindow(onCloseRequest = ::exitApplication, title = "My App") {
        JewelTitleBar { state -> Text(title) }
    }
}
```

Components: `JewelDecoratedWindow`, `JewelDecoratedDialog`, `JewelTitleBar`, `JewelDialogTitleBar`. Color mapping from
`JewelTheme.globalColors`.

## Dark Mode Detector

Reactive `@Composable` function that detects OS dark mode via JNI (no JNA, no polling).

```kotlin
import io.github.kdroidfilter.nucleus.darkmodedetector.isSystemInDarkMode

@Composable
fun App() {
    val isDark = isSystemInDarkMode()
    val colorScheme = if (isDark) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = colorScheme) {
        // UI recomposes when OS theme changes
    }
}
```

| Platform | Method                                                                        | Reactive                      |
|----------|-------------------------------------------------------------------------------|-------------------------------|
| macOS    | `NSDistributedNotificationCenter` on `AppleInterfaceThemeChangedNotification` | Yes — JNI callback            |
| Windows  | Registry key `AppsUseLightTheme` + `RegNotifyChangeKeyValue`                  | Yes — background thread       |
| Linux    | D-Bus `org.freedesktop.portal.Settings`                                       | Yes — `SettingChanged` signal |

## System Color

Reactive OS accent color and high contrast detection via JNI.

```kotlin
@Composable
fun App() {
    val accentColor = systemAccentColor()         // Color? — recomposes on change
    val highContrast = isSystemInHighContrast()    // Boolean — recomposes on change
}
```

`isSystemAccentColorSupported()` — non-composable check for feature gating.

| Platform | Accent Color                              | High Contrast                                | Reactive                        |
|----------|-------------------------------------------|----------------------------------------------|---------------------------------|
| macOS    | `NSColor.controlAccentColor`              | `accessibilityDisplayShouldIncreaseContrast` | Yes — NSNotification            |
| Windows  | DWM `AccentColor` registry (AABBGGRR)     | `SPI_GETHIGHCONTRAST`                        | Yes — `RegNotifyChangeKeyValue` |
| Linux    | XDG Desktop Portal `accent-color` (D-Bus) | XDG Portal `contrast`                        | Yes — `SettingChanged` signal   |

### Material You on Desktop

Combine with [material-kolor](https://github.com/jordond/MaterialKolor) for dynamic theming from system accent color:

```kotlin
@Composable
fun App() {
    val accentColor = systemAccentColor()
    val seedColor = accentColor ?: Color(0xFF6750A4)

    DynamicMaterialTheme(
        seedColor = seedColor,
        isDark = isSystemInDarkTheme(),
        animate = true,
        style = PaletteStyle.TonalSpot,
    ) {
        // Entire color scheme adapts to OS accent color in real time
    }
}
```

## Energy Manager

Process/thread energy efficiency mode and screen-awake (caffeine) API.

```kotlin
import io.github.kdroidfilter.nucleus.energymanager.EnergyManager

// Process-level efficiency
EnergyManager.enableEfficiencyMode()     // full power saving (CPU + I/O throttling)
EnergyManager.disableEfficiencyMode()
EnergyManager.enableLightEfficiencyMode()  // light (CPU only, no I/O throttling)
EnergyManager.disableLightEfficiencyMode()

// Thread-level efficiency (coroutines)
val result = EnergyManager.withEfficiencyMode { computeHeavyReport() }
EnergyManager.withLightEfficiencyMode { syncDataFromServer() }

// Thread-level efficiency (raw threads)
EnergyManager.enableThreadEfficiencyMode()
EnergyManager.disableThreadEfficiencyMode()

// Screen-awake (caffeine)
EnergyManager.keepScreenAwake()
EnergyManager.releaseScreenAwake()
EnergyManager.isScreenAwakeActive()
```

| Feature           | Windows                       | macOS                       | Linux                                          |
|-------------------|-------------------------------|-----------------------------|------------------------------------------------|
| Full efficiency   | EcoQoS + IDLE_PRIORITY_CLASS  | PRIO_DARWIN_BG + QoS TIER_5 | nice +19, ioprio IDLE, timerslack 100ms        |
| Light efficiency  | EcoQoS only                   | task_policy_set(TIER_5)     | nice +10                                       |
| Thread efficiency | EcoQoS + THREAD_PRIORITY_IDLE | QOS_CLASS_BACKGROUND        | nice +19, ioprio IDLE, timerslack 100ms        |
| Screen-awake      | SetThreadExecutionState       | IOPMAssertion               | DBus (GNOME/logind) or X11 XScreenSaverSuspend |

### Recommended: Focus-aware efficiency

```kotlin
LaunchedEffect(state.isMinimized, isWindowFocused) {
    when {
        state.isMinimized -> {
            EnergyManager.disableLightEfficiencyMode()
            EnergyManager.enableEfficiencyMode()  // full power saving
        }
        !isWindowFocused -> {
            EnergyManager.disableEfficiencyMode()
            EnergyManager.enableLightEfficiencyMode()  // light (CPU only)
        }
        else -> {
            EnergyManager.disableEfficiencyMode()
            EnergyManager.disableLightEfficiencyMode()  // normal
        }
    }
}
```

## Native SSL

JVM applications shipped with a bundled JRE use only the certificates baked into that JRE. The `native-ssl` module reads
trusted certificates from the OS trust store and merges them with the JVM defaults.

```kotlin
import io.github.kdroidfilter.nucleus.nativessl.NativeTrustManager

val trustManager: X509TrustManager = NativeTrustManager.trustManager
val sslContext: SSLContext = NativeTrustManager.sslContext
val sslSocketFactory: SSLSocketFactory = NativeTrustManager.sslSocketFactory
```

| Platform | Method                                                                                    |
|----------|-------------------------------------------------------------------------------------------|
| macOS    | Security framework via JNI — system root CAs + user/admin domain certs                    |
| Windows  | Crypt32 via JNI — ROOT + CA stores across 5 locations. Falls back to SunMSCAPI.           |
| Linux    | Pure JVM — reads PEM bundles from standard paths (Debian, Fedora, RHEL, openSUSE, Alpine) |

## Native HTTP

Three integration modules provide HTTP clients pre-wired with `NativeTrustManager`. All pull in `native-ssl`
transitively.

### java.net.http.HttpClient

```kotlin
import io.github.kdroidfilter.nucleus.nativehttp.NativeHttpClient

val client = NativeHttpClient.create()
// or:
val client = HttpClient.newBuilder().withNativeSsl().connectTimeout(Duration.ofSeconds(30)).build()
```

### OkHttp

```kotlin
import io.github.kdroidfilter.nucleus.nativehttp.okhttp.NativeOkHttpClient

val client = NativeOkHttpClient.create()
// or:
val client = OkHttpClient.Builder().withNativeSsl().callTimeout(30, TimeUnit.SECONDS).build()
```

### Ktor

```kotlin
import io.github.kdroidfilter.nucleus.nativehttp.ktor.installNativeSsl

val client = HttpClient(CIO) { installNativeSsl() }
```

`installNativeSsl()` auto-detects the active engine (CIO, Java, OkHttp, Apache5) and applies the correct SSL config.

## Linux HiDPI

Native scale factor detection for non-JBR runtimes (GraalVM, OpenJDK). Call before AWT initializes:

```kotlin
import io.github.kdroidfilter.nucleus.hidpi.getLinuxNativeScaleFactor

fun main() {
    if (System.getProperty("sun.java2d.uiScale") == null) {
        val scale = getLinuxNativeScaleFactor()
        if (scale > 0.0) System.setProperty("sun.java2d.uiScale", scale.toString())
    }
    application { /* ... */ }
}
```

Detection sources (priority order): `J2D_UISCALE`, GSettings, `GDK_SCALE`, `GDK_DPI_SCALE`, `Xft.dpi`.

## GraalVM Runtime

Bootstrap module for GraalVM native-image. Handles Metal L&F, `java.home`, `java.library.path`, charset init,
fontmanager preload, and Linux HiDPI.

```kotlin
import io.github.kdroidfilter.nucleus.graalvm.GraalVmInitializer

fun main() {
    GraalVmInitializer.initialize()  // Must be first line
    application { Window(onCloseRequest = ::exitApplication) { App() } }
}
```

Requires BellSoft Liberica NIK 25 (full). Use `decorated-window-jni` for decorated windows in native-image builds.

### Automatic Resource Inclusion

The module ships a `native-image.properties` that registers broad resource patterns at compile time via
`-H:IncludeResources`. Any project depending on `nucleus.graalvm-runtime` automatically gets:

| Pattern                | Covers                                                                                                                  |
|------------------------|-------------------------------------------------------------------------------------------------------------------------|
| `.*\.(svg\|ttf\|otf)`  | All SVG icons and fonts — Jewel, IntelliJ Platform icons, Compose resources, app icons, dark/light variants, @2x retina |
| `nucleus/native/.*`    | All Nucleus JNI native libraries (.dll, .dylib, .so)                                                                    |
| `META-INF/services/.*` | All ServiceLoader descriptors (ktor, coil, SLF4J, etc.)                                                                 |

This eliminates the need to run the tracing agent (`runWithNativeAgent`) just to discover icon and font resources. All
SVGs and fonts on the classpath are included in the native binary automatically.

The tracing agent is still required for: reflection metadata, JNI metadata, resource bundles (locale `.properties`), and
non-standard resources (`.sha256`, `.class`, ICU data).

Trade-off: `.*\.(svg|ttf|otf)` includes all SVGs/fonts from all classpath JARs. If binary size is critical, override
with targeted patterns in your own `resource-config.json`.

## Taskbar Progress

Native taskbar/dock progress bar and attention requests on all platforms via JNI.

```kotlin
import io.github.kdroidfilter.nucleus.taskbarprogress.TaskbarProgress

// Show progress (0.0–1.0)
TaskbarProgress.showProgress(window, 0.75)

// States: NORMAL, ERROR, PAUSED, INDETERMINATE, NO_PROGRESS
TaskbarProgress.showError(window)
TaskbarProgress.showPaused(window, 0.5)
TaskbarProgress.showIndeterminate(window)
TaskbarProgress.hideProgress(window)

// Attention requests
TaskbarProgress.requestAttention(window, TaskbarProgress.AttentionType.INFORMATIONAL)
TaskbarProgress.stopAttention(window)
```

| Platform | Progress API                                           | Attention API                        |
|----------|--------------------------------------------------------|--------------------------------------|
| Windows  | `ITaskbarList3` (per-window)                           | `FlashWindowEx`                      |
| macOS    | `NSDockTile` + `NSProgressIndicator` (app-wide)        | `NSApplication.requestUserAttention` |
| Linux    | D-Bus `com.canonical.Unity.LauncherEntry` (GNOME, KDE) | D-Bus `urgent` property              |

States: `NO_PROGRESS`, `INDETERMINATE`, `NORMAL` (green/blue), `ERROR` (red), `PAUSED` (yellow).
Attention types: `INFORMATIONAL` (flash 4 times / bounce once), `CRITICAL` (flash until focus / continuous bounce).

For Linux, the `.desktop` file is auto-detected from `NucleusApp.appId`, env vars, or XDG dirs. Override with
`TaskbarProgress.linuxDesktopFilename`.

## App Metadata (`NucleusApp`)

`NucleusApp` is a singleton in `core-runtime` that exposes plugin-injected metadata at runtime:

```kotlin
import io.github.kdroidfilter.nucleus.core.runtime.NucleusApp

val appId: String = NucleusApp.appId           // Application identifier
val version: String? = NucleusApp.version      // Application version
val vendor: String? = NucleusApp.vendor        // Application vendor
val description: String? = NucleusApp.description  // Application description
val configured: Boolean = NucleusApp.isConfigured  // true if plugin injected metadata
```

Metadata is populated from two sources (first non-null wins):

1. **System properties** — `-Dnucleus.app.id=...` injected by the plugin during `run` and `packageDistribution` tasks
2. **Classpath resource** — `nucleus/nucleus-app.properties` generated at build time by the plugin

`NucleusApp.appId` is used by other modules (e.g. `taskbar-progress` for Linux `.desktop` detection, `updater-runtime`
for update marker storage path).

### GraalVM DSL Reference

| Property                   | Type                   | Default      | Description                                       |
|----------------------------|------------------------|--------------|---------------------------------------------------|
| `isEnabled`                | `Boolean`              | `false`      | Enable GraalVM native compilation                 |
| `javaLanguageVersion`      | `Int`                  | `25`         | Gradle toolchain language version                 |
| `jvmVendor`                | `JvmVendorSpec`        | —            | Set to `BELLSOFT` for Liberica NIK                |
| `imageName`                | `String`               | project name | Output executable name                            |
| `march`                    | `String`               | `"native"`   | CPU architecture target                           |
| `buildArgs`                | `ListProperty<String>` | empty        | Extra `native-image` arguments                    |
| `nativeImageConfigBaseDir` | `DirectoryProperty`    | —            | Directory containing `reachability-metadata.json` |

---

# Migration from org.jetbrains.compose

Nucleus extends the official JetBrains Compose Desktop plugin. All existing configuration is preserved.

## Steps

1. Add the Nucleus plugin alongside the existing compose plugin:

```diff
 plugins {
     id("org.jetbrains.compose") version "1.10.2"
+    id("io.github.kdroidfilter.nucleus") version "<version>"
 }
```

2. Update DSL imports from JetBrains Compose to Nucleus:

```diff
-import org.jetbrains.compose.desktop.application.dsl.TargetFormat
+import io.github.kdroidfilter.nucleus.desktop.application.dsl.TargetFormat
```

This applies to all DSL types (`TargetFormat`, `CompressionLevel`, `SigningAlgorithm`, etc.).

3. Replace `compose.desktop.application { }` with `nucleus.application { }`:

```diff
-compose.desktop.application {
+nucleus.application {
     mainClass = "com.example.MainKt"
     nativeDistributions { /* ... all existing config works */ }
 }
```

4. Add Nucleus features (optional): `cleanupNativeLibs`, `enableAotCache`, `protocol()`, `fileAssociation()`, new
   formats, `publish {}`, `graalvm {}`, etc.

5. Add runtime libraries (optional): `nucleus.core-runtime`, `nucleus.aot-runtime`, `nucleus.updater-runtime`,
   `nucleus.decorated-window-jni`, etc.

## What Changes

| Feature              | Before (compose)                                  | After (nucleus)                                            |
|----------------------|---------------------------------------------------|------------------------------------------------------------|
| DSL entry point      | `compose.desktop.application`                     | `nucleus.application`                                      |
| DSL imports          | `org.jetbrains.compose.desktop.application.dsl.*` | `io.github.kdroidfilter.nucleus.desktop.application.dsl.*` |
| Target formats       | DMG, PKG, MSI, EXE, DEB, RPM                      | + NSIS, AppX, Portable, AppImage, Snap, Flatpak, archives  |
| Native lib cleanup   | Manual                                            | `cleanupNativeLibs = true`                                 |
| AOT cache            | Not available                                     | `enableAotCache = true`                                    |
| GraalVM Native Image | Not available                                     | `graalvm { isEnabled = true }`                             |
| Liquid Glass         | Not available                                     | Automatic (`macOsSdkVersion = "26.0"`)                     |
| Deep links           | Manual (macOS only)                               | Cross-platform `protocol()`                                |
| File associations    | Limited                                           | Cross-platform `fileAssociation()`                         |
| Store pipeline       | Not available                                     | Automatic for PKG, AppX, Flatpak                           |
| Auto-update          | Not available                                     | Built-in                                                   |
| Code signing         | macOS only                                        | + Windows PFX / Azure Trusted Signing                      |
| DMG appearance       | Not customizable                                  | Full `dmg { }` DSL                                         |

## What Stays the Same

Everything from the official plugin works unchanged: `mainClass`, `jvmArgs`, `nativeDistributions`, `buildTypes` /
ProGuard, `modules()`, all existing Gradle tasks, `compose.desktop.currentOs`.
