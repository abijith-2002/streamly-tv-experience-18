/**
 * PUBLIC_INTERFACE
 * Root Gradle build script.
 * Adds plugin coordinates and exposes a CI-friendly 'deepClean' task that removes
 * build dirs and attempts to evict potentially corrupted AAPT intermediates.
 */
// Top-level build file
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.3.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
    }
}

plugins {
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

// PUBLIC_INTERFACE
tasks.register("deepClean") {
    description = "Removes build dirs and common Gradle caches to fix resource merge cache issues."
    group = "ci"
    doLast {
        val buildDir = rootProject.layout.buildDirectory.asFile.get()
        if (buildDir.exists()) buildDir.deleteRecursively()
        println("Deleted build directory at: ${buildDir.absolutePath}")

        // Attempt to remove AAPT2 intermediates at module level
        rootProject.allprojects.forEach { p ->
            val pBuild = p.layout.buildDirectory.asFile.get()
            if (pBuild.exists()) {
                println("Deleting ${p.name} build dir: ${pBuild.absolutePath}")
                pBuild.deleteRecursively()
            }
        }
        println("Deep clean finished.")
    }
}
