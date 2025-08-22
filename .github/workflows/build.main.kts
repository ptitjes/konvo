#!/usr/bin/env kotlin

@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.5.0")
@file:Repository("https://bindings.krzeminski.it")
@file:DependsOn("actions:checkout:v4")
@file:DependsOn("actions:setup-java:v5")
@file:DependsOn("actions:cache:v4")
@file:DependsOn("actions:upload-artifact:v4")
@file:DependsOn("gradle:gradle-build-action:v3")
@file:DependsOn("EnricoMi:publish-unit-test-result-action:v2")
@file:DependsOn("EnricoMi:publish-unit-test-result-action__macos:v2")
@file:DependsOn("EnricoMi:publish-unit-test-result-action__windows:v2")
@file:Import("./shared.main.kts")

import io.github.typesafegithub.workflows.actions.enricomi.*
import io.github.typesafegithub.workflows.domain.*
import io.github.typesafegithub.workflows.domain.triggers.*
import io.github.typesafegithub.workflows.dsl.*
import io.github.typesafegithub.workflows.dsl.expressions.*

workflow(
    name = "Build",
    on = listOf(Push(), PullRequest()),
    sourceFile = __FILE__,
    permissions = mapOf(
        Permission.Checks to Mode.Write,
        Permission.PullRequests to Mode.Write,
    )
) {
    buildJob(
        id = "linux-x64-build",
        name = "Build on Linux x64",
        runsOn = RunnerType.UbuntuLatest,
        extraSetup = {
            run(name = "Update dependencies", command = "sudo apt-get update")
        },
        buildArguments = "build -Dsplit_targets",
        afterBuild = {
            uses(
                name = "Publish Test Result",
                `if` = expr("!cancelled()"),
                action = PublishUnitTestResultAction(
                    checkName = "Linux x64 Build",
                    files = listOf("**/build/test-results/**/*.xml"),
                ),
            )
        }
    )
    buildJob(
        id = "linux-arm64-build",
        name = "Build on Linux arm64",
        runsOn = RunnerType.Custom("ubuntu-24.04-arm"),
        extraSetup = {
            run(name = "Update dependencies", command = "sudo apt-get update")
        },
        buildArguments = "build -Dsplit_targets",
        afterBuild = {
            uses(
                name = "Publish Test Result",
                `if` = expr("!cancelled()"),
                action = PublishUnitTestResultAction(
                    checkName = "Linux arm64 Build",
                    files = listOf("**/build/test-results/**/*.xml"),
                ),
            )
        }
    )
    buildJob(
        id = "windows-build",
        name = "Build on Windows",
        runsOn = RunnerType.WindowsLatest,
        buildArguments = "build -Dsplit_targets",
        afterBuild = {
            uses(
                name = "Publish Test Result",
                `if` = expr("!cancelled()"),
                action = PublishUnitTestResultActionWindows(
                    checkName = "Windows Build",
                    files = listOf("**/build/test-results/**/*.xml"),
                ),
            )
        }
    )
    buildJob(
        id = "macos-build",
        name = "Build on MacOS",
        runsOn = RunnerType.MacOSLatest,
        buildArguments = "build -Dsplit_targets",
        afterBuild = {
            uses(
                name = "Publish Test Result",
                `if` = expr("!cancelled()"),
                action = PublishUnitTestResultActionMacos(
                    checkName = "MacOS Build",
                    files = listOf("**/build/test-results/**/*.xml"),
                ),
            )
        }
    )
}
