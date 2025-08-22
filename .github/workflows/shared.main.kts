#!/usr/bin/env kotlin

@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.5.0")
@file:Repository("https://bindings.krzeminski.it")
@file:DependsOn("actions:checkout:v4")
@file:DependsOn("actions:setup-java:v5")
@file:DependsOn("actions:cache:v4")
@file:DependsOn("actions:upload-artifact:v4")
@file:DependsOn("gradle:actions__setup-gradle:v4")

import io.github.typesafegithub.workflows.actions.actions.*
import io.github.typesafegithub.workflows.actions.gradle.*
import io.github.typesafegithub.workflows.domain.*
import io.github.typesafegithub.workflows.dsl.*
import io.github.typesafegithub.workflows.dsl.expressions.*

fun WorkflowBuilder.buildJob(
    id: String,
    name: String,
    runsOn: RunnerType,
    extraSetup: JobBuilder<JobOutputs.EMPTY>.() -> Unit = {},
    buildArguments: String,
    afterBuild: JobBuilder<JobOutputs.EMPTY>.() -> Unit = {},
) {
    job(
        id = id,
        name = name,
        runsOn = runsOn
    ) {
        uses(name = "Checkout", action = Checkout())
        uses(
            name = "Install Java",
            action = SetupJava(javaVersion = "17", distribution = SetupJava.Distribution.Adopt),
        )
        uses(name = "Build", action = ActionsSetupGradle())

        extraSetup()

        uses(
            name = "Set up cache",
            action = Cache(
                path = listOf("~/.gradle/caches"),
                key = expr { runner.os } + "-gradle-" + expr { hashFiles("**/*.gradle*", quote = true) },
                restoreKeys = listOf(expr { runner.os } + "-gradle-"),
            ),
        )
        run(name = "Build", command = "./gradlew $buildArguments")

        afterBuild()
    }
}
