import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.5.20"
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

val buildType = obtainBuildType()
val mainClassPath = "de.passbutler.desktop.PassButlerApplicationKt"

val javaVersion = JavaVersion.VERSION_14
val javaFxVersion = "14.0.2.1"

val kotlinVersion = "1.5.20"
val kotlinJvmTargetVersion = "14"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(project(":PassButlerCommon"))

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    // Kotlin Coroutines for JavaFX
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.5.1")

    // TinyLog logger
    val tinylogVersion = "2.3.2"
    implementation("org.tinylog:tinylog-api-kotlin:$tinylogVersion")
    implementation("org.tinylog:tinylog-impl:$tinylogVersion")

    // JSON library
    implementation("org.json:json:20210307")

    // SQLDelight
    implementation("com.squareup.sqldelight:sqlite-driver:1.5.1")

    // TornadoFX
    implementation(files("lib/tornadofx-2.0.0-SNAPSHOT.jar"))

    // TornadoFX dependencies
    val javaxJsonVersion = "1.1.4"
    implementation("javax.json:javax.json-api:$javaxJsonVersion")
    implementation("org.glassfish:javax.json:$javaxJsonVersion")

    // TornadoFX depends on old version - explicitly use version matching the Kotlin version
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    // JFoenix
    implementation("com.jfoenix:jfoenix:9.0.10")

    // JSON Web Token library
    implementation("com.auth0:java-jwt:3.18.1")

    // CSV library
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.15.2")

    // JUnit 5
    val junitVersion = "5.7.2"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    // Mockk.io
    testImplementation("io.mockk:mockk:1.12.0")
}

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

javafx {
    version = javaFxVersion
    modules("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set(mainClassPath)
}

sourceSets.all {
    java.srcDir("./build/generated/source/")
}

tasks {
    // Both default and test compile tasks
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = kotlinJvmTargetVersion
        }
    }

    // Only the non-test compile task
    compileKotlin {
        dependsOn("generateBuildConfig")
    }

    withType<Test> {
        useJUnitPlatform()
    }

    named<JavaExec>("run") {
        jvmArgs("--add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED")
    }

    jar {
        manifest {
            attributes["Class-Path"] = configurations.compile.get().all.joinToString(" ") { it.name }
            attributes["Main-Class"] = mainClassPath
        }
    }
}

task("generateBuildConfig") {
    generateBuildConfig()
}

fun obtainBuildType(): String {
    return System.getenv("PASSBUTLER_BUILD_TYPE").takeIf { listOf("debug", "release").contains(it) } ?: "debug"
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
