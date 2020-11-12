plugins {
    val kotlinVersion = "1.3.72"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion

    // Version "0.0.9" seems not working properly (`JavaFXOptions` reference issue)
    id("org.openjfx.javafxplugin") version "0.0.8"

    id("org.gradle.application")
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(project(":PassButlerCommon"))

    // Kotlin
    val kotlinVersion = "1.3.72"
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")

    // Kotlin Coroutines for JavaFX
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.3.3")

    // TinyLog logger
    val tinylogVersion = "2.1.2"
    implementation("org.tinylog:tinylog-api-kotlin:$tinylogVersion")
    implementation("org.tinylog:tinylog-impl:$tinylogVersion")

    // TornadoFX
    implementation("no.tornado:tornadofx:1.7.20")

    // SQLDelight
    implementation("com.squareup.sqldelight:sqlite-driver:1.3.0")

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
    val mainClass = "de.passbutler.desktop.PassButlerApplication"

    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"

        dependsOn("generateBuildConfig")
    }

    application {
        mainClassName = mainClass
    }

    jar {
        manifest {
            attributes["Class-Path"] = configurations.compile.get().all.joinToString(" ") { it.name }
            attributes["Main-Class"] = mainClass
        }
    }

    defaultTasks("run")
}

task("generateBuildConfig") {
    generateBuildConfig()
}

fun generateBuildConfig() {
    val applicationId = "de.passbutler.desktop"
    val buildType = "debug"
    val versionName = "1.0.0"

    val buildTimestamp = System.currentTimeMillis()
    val gitCommitHashShort = "git rev-parse --short HEAD".executeCommand()
    val gitCommitCount = "git rev-list HEAD --count".executeCommand()

    val buildConfigTemplate = buildString {
        appendln("package $applicationId")
        appendln("object BuildConfig {")
        appendln("const val APPLICATION_ID = \"$applicationId\"")
        appendln("const val BUILD_TYPE = \"$buildType\"")
        appendln("const val BUILD_TIMESTAMP = $buildTimestamp")
        appendln("const val BUILD_REVISION_HASH = \"$gitCommitHashShort\"")
        appendln("const val VERSION_CODE = $gitCommitCount")
        appendln("const val VERSION_NAME = \"$versionName\"")
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
