plugins {
    val kotlinVersion = "1.4.10"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion

    id("org.gradle.application")

    id("org.openjfx.javafxplugin") version "0.0.9"
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

version = "1.0.0"
group = "de.passbutler.desktop"

// TODO: Use proper configuration
val buildType = "debug"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(project(":PassButlerCommon"))

    // Kotlin
    val kotlinVersion = "1.4.10"
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    // Kotlin Coroutines for JavaFX
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.4.1")

    // TinyLog logger
    val tinylogVersion = "2.2.0"
    implementation("org.tinylog:tinylog-api-kotlin:$tinylogVersion")
    implementation("org.tinylog:tinylog-impl:$tinylogVersion")

    // TornadoFX
    implementation("no.tornado:tornadofx:1.7.20")

    // TornadoFX depends on old version - explicitly use version matching the Kotlin version
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    // SQLDelight
    implementation("com.squareup.sqldelight:sqlite-driver:1.4.4")

    // JFoenix
    implementation("com.jfoenix:jfoenix:9.0.10")
}

javafx {
    version = "15.0.1"
    modules("javafx.controls", "javafx.fxml")
}

sourceSets.all {
    java.srcDir("./build/generated/source/")
}

tasks {
    val mainClassPath = "de.passbutler.desktop.PassButlerApplication"

    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"

        dependsOn("generateBuildConfig")
    }

    application {
        mainClass.set(mainClassPath)
    }

    jar {
        manifest {
            attributes["Class-Path"] = configurations.compile.get().all.joinToString(" ") { it.name }
            attributes["Main-Class"] = mainClassPath
        }
    }

    defaultTasks("run")
}

task("generateBuildConfig") {
    generateBuildConfig()
}

fun generateBuildConfig() {
    val applicationId = group.toString()
    val buildTimestamp = System.currentTimeMillis()
    val gitCommitHashShort = "git rev-parse --short HEAD".executeCommand()
    val versionName = version
    val gitCommitCount = "git rev-list HEAD --count".executeCommand()

    val buildConfigTemplate = buildString {
        appendln("package $applicationId")
        appendln("")
        appendln("object BuildConfig {")
        appendlnIndented("const val APPLICATION_ID = \"$applicationId\"")
        appendlnIndented("const val BUILD_TYPE = \"$buildType\"")
        appendlnIndented("const val BUILD_TIMESTAMP = $buildTimestamp")
        appendlnIndented("const val BUILD_REVISION_HASH = \"$gitCommitHashShort\"")
        appendlnIndented("const val VERSION_NAME = \"$versionName\"")
        appendlnIndented("const val VERSION_CODE = $gitCommitCount")
        appendln("}")
    }

    val packagePath = applicationId.replace(".", "/")
    val generatedDirectory = file("./build/generated/source/$packagePath")
    generatedDirectory.mkdirs()

    val buildConfigPath = "${generatedDirectory.path}/BuildConfig.kt"
    file(buildConfigPath).writeText(buildConfigTemplate)
}

fun String.executeCommand(workingDir: File = file("./")): String {
    val commandParts = split(" ")
    val commandProcess = ProcessBuilder(*commandParts.toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    commandProcess.waitFor(5, TimeUnit.SECONDS)

    return commandProcess.inputStream.bufferedReader().readText().trim()
}

fun StringBuilder.appendlnIndented(value: String, indentCount: Int = 4): StringBuilder {
    return appendln(value.prependIndent(" ".repeat(indentCount)))
}
