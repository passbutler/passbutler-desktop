plugins {
    val kotlinVersion = "1.3.72"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion

    id("org.openjfx.javafxplugin") version "0.0.9"
    id("org.gradle.application")
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()

        maven(url = "https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.1.0")
        classpath("org.openjfx:javafx-plugin:0.0.9")
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
    version = "12"
    modules("javafx.controls", "javafx.fxml")
}

tasks {
    val mainClass = "de.passbutler.desktop.PassButlerApplication"

    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
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
