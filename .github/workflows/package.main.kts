#!/usr/bin/env kotlin

@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.7.0")
@file:Repository("https://bindings.krzeminski.it")
@file:DependsOn("actions:upload-artifact:v7")
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
        id = "package-linux-x64",
        runsOn = RunnerType.UbuntuLatest,
        buildArguments = ":apps:desktop-app:packageReleaseDeb :apps:desktop-app:packageReleaseRpm",
        afterBuild = {
            uses(
                name = "Upload artifacts",
                action = UploadArtifact(
                    name = "app-linux-x64",
                    path = listOf(
                        "apps/desktop-app/build/compose/binaries/main-release/deb",
                        "apps/desktop-app/build/compose/binaries/main-release/rpm",
                    ),
                ),
            )
        },
    )
    buildJob(
        id = "package-linux-arm64",
        runsOn = RunnerType.Custom("ubuntu-24.04-arm"),
        extraSetup = {
            run(name = "Update dependencies", command = "sudo apt-get update")
            run(name = "Install libEGL", command = "sudo apt install -y libegl1")
        },
        buildArguments = ":apps:desktop-app:packageReleaseDeb :apps:desktop-app:packageReleaseRpm",
        afterBuild = {
            uses(
                name = "Upload artifacts",
                action = UploadArtifact(
                    name = "app-linux-arm64",
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
        runsOn = RunnerType.WindowsLatest,
        buildArguments = ":apps:desktop-app:packageReleaseMsi",
        afterBuild = {
            uses(
                name = "Upload artifacts",
                action = UploadArtifact(
                    name = "app-windows",
                    path = listOf(
                        "apps/desktop-app/build/compose/binaries/main-release/msi",
                    ),
                )
            )
        },
    )
    buildJob(
        id = "package-macos",
        runsOn = RunnerType.MacOSLatest,
        buildArguments = ":apps:desktop-app:packageReleaseDmg",
        afterBuild = {
            uses(
                name = "Upload artifacts",
                action = UploadArtifact(
                    name = "app-macos",
                    path = listOf(
                        "apps/desktop-app/build/compose/binaries/main-release/dmg",
                    ),
                ),
            )
        },
    )
}
