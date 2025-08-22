#!/usr/bin/env kotlin

@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.5.0")
@file:Repository("https://bindings.krzeminski.it")
@file:DependsOn("actions:checkout:v4")
@file:DependsOn("actions:setup-java:v5")
@file:DependsOn("actions:cache:v4")
@file:DependsOn("actions:upload-artifact:v4")
@file:DependsOn("gradle:gradle-build-action:v3")
@file:Import("./shared.main.kts")

import io.github.typesafegithub.workflows.actions.actions.*
import io.github.typesafegithub.workflows.domain.*
import io.github.typesafegithub.workflows.domain.triggers.*
import io.github.typesafegithub.workflows.dsl.*

workflow(
    name = "Package",
    on = listOf(WorkflowDispatch()),
    sourceFile = __FILE__,
) {
    buildJob(
        id = "package-x64-linux",
        name = "Package on Linux x64",
        runsOn = RunnerType.UbuntuLatest,
        buildArguments = ":apps:desktop-app:packageReleaseDeb :apps:desktop-app:packageReleaseRpm",
        afterBuild = {
            uses(
                name = "Upload artifacts",
                action = UploadArtifact(
                    name = "linux-x64-packages",
                    path = listOf(
                        "apps/desktop-app/build/compose/binaries/main-release/deb",
                        "apps/desktop-app/build/compose/binaries/main-release/rpm",
                    ),
                ),
            )
        },
    )
    buildJob(
        id = "package-arm64-linux",
        name = "Package on Linux arm64",
        runsOn = RunnerType.Custom("ubuntu-24.04-arm"),
        buildArguments = ":apps:desktop-app:packageReleaseDeb :apps:desktop-app:packageReleaseRpm",
        afterBuild = {
            uses(
                name = "Upload artifacts",
                action = UploadArtifact(
                    name = "linux-arm64-packages",
                    path = listOf(
                        "apps/desktop-app/build/compose/binaries/main-release/deb",
                        "apps/desktop-app/build/compose/binaries/main-release/rpm",
                    ),
                ),
            )
        },
    )
    buildJob(
        id = "package-windows",
        name = "Package on Windows",
        runsOn = RunnerType.WindowsLatest,
        buildArguments = ":apps:desktop-app:packageReleaseMsi",
        afterBuild = {
            uses(
                name = "Upload artifacts",
                action = UploadArtifact(
                    name = "windows-packages",
                    path = listOf(
                        "apps/desktop-app/build/compose/binaries/main-release/msi",
                    ),
                )
            )
        },
    )
    buildJob(
        id = "package-macos",
        name = "Package on MacOS",
        runsOn = RunnerType.MacOSLatest,
        buildArguments = ":apps:desktop-app:packageReleaseDmg",
        afterBuild = {
            uses(
                name = "Upload artifacts",
                action = UploadArtifact(
                    name = "macos-packages",
                    path = listOf(
                        "apps/desktop-app/build/compose/binaries/main-release/dmg",
                    ),
                ),
            )
        },
    )
}
