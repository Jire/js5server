@file:Suppress("UnstableApiUsage")

rootProject.name = "js5server"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://repo.openrs2.org/repository/openrs2-snapshots/")
    }
    pluginManagement.plugins.apply {
        kotlin("jvm").version("1.9.10")
        id("com.github.johnrengelman.shadow") version "8.1.1"
    }
}
