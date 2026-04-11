#!/usr/bin/env kotlin

@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.7.0")
@file:Repository("https://bindings.krzeminski.it")
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
    on = listOf(
        Push(
            branches = listOf("develop", "main"),
        ),
        PullRequest(
            branches = listOf("develop", "main"),
        ),
    ),
    sourceFile = __FILE__,
    permissions = mapOf(
        Permission.Checks to Mode.Write,
        Permission.PullRequests to Mode.Write,
    )
) {
    buildJob(
        id = "build-linux-x64",
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
                    checkName = "Tests (linux x64)",
                    files = listOf("**/build/test-results/**/*.xml"),
                ),
            )
        }
    )
    buildJob(
        id = "build-linux-arm64",
        runsOn = RunnerType.Custom("ubuntu-24.04-arm"),
        extraSetup = {
            run(name = "Update dependencies", command = "sudo apt-get update")
            run(name = "Install libEGL", command = "sudo apt install -y libegl1")
        },
        buildArguments = "build -Dsplit_targets",
        afterBuild = {
            uses(
                name = "Publish Test Result",
                `if` = expr("!cancelled()"),
                action = PublishUnitTestResultAction(
                    checkName = "Tests (linux arm64)",
                    files = listOf("**/build/test-results/**/*.xml"),
                ),
            )
        }
    )
    buildJob(
        id = "build-windows",
        runsOn = RunnerType.WindowsLatest,
        buildArguments = "build -Dsplit_targets",
        afterBuild = {
            uses(
                name = "Publish Test Result",
                `if` = expr("!cancelled()"),
                action = PublishUnitTestResultActionWindows(
                    checkName = "Tests (windows)",
                    files = listOf("**/build/test-results/**/*.xml"),
                ),
            )
        }
    )
    buildJob(
        id = "build-macos",
        runsOn = RunnerType.MacOSLatest,
        buildArguments = "build -Dsplit_targets",
        afterBuild = {
            uses(
                name = "Publish Test Result",
                `if` = expr("!cancelled()"),
                action = PublishUnitTestResultActionMacos(
                    checkName = "Tests (macos)",
                    files = listOf("**/build/test-results/**/*.xml"),
                ),
            )
        }
    )
}
