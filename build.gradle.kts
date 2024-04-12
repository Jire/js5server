plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow")

    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin")
}

group = "org.jire"
version = "1.0.3"
description = "fast simple JS5 server"

application {
    mainClass.set("org.jire.js5server.Main")
    applicationDefaultJvmArgs += arrayOf(
        "-XX:-OmitStackTraceInFastThrow",

        "-Xmx8g",
        "-Xms4g",

        "-XX:+UseZGC",
        "-XX:MaxGCPauseMillis=100",

        "-Dio.netty.tryReflectionSetAccessible=true", // allow Netty to use direct buffer optimizations
    )
}

dependencies {
    val slf4jVersion = "2.0.12"
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    runtimeOnly("org.slf4j:slf4j-simple:$slf4jVersion")

    for (module in listOf(
        "handler",
        "buffer",
        "transport-native-epoll",
        "transport-native-kqueue",
    )) implementation("io.netty:netty-$module:4.1.107.Final")

    implementation("io.netty.incubator:netty-incubator-transport-native-io_uring:0.0.25.Final")

    implementation("it.unimi.dsi:fastutil:8.5.13")
    implementation("org.jctools:jctools-core:4.0.3")

    for (module in listOf("buffer", "cache"))
        implementation("org.openrs2:openrs2-$module:0.1.0-SNAPSHOT")
}

kotlin {
    jvmToolchain(21)
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name = rootProject.name
                description = rootProject.description
                url = "https://github.com/Jire/js5server"
                packaging = "jar"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://github.com/Jire/js5server/blob/main/LICENSE.txt"
                    }
                }
                developers {
                    developer {
                        id = "Jire"
                        name = "Thomas Nappo"
                        email = "thomasgnappo@gmail.com"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/Jire/js5server.git"
                    developerConnection = "scm:git:ssh://git@github.com/Jire/js5server.git"
                    url = "https://github.com/Jire/js5server"
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

nexusPublishing {
    repositories {
        sonatype {
            if (false) { // only for users registered in Sonatype after 24 Feb 2021
                nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
                snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            }

            val ossrhUsername = providers.environmentVariable("OSSRH_USERNAME")
            val ossrhPassword = providers.environmentVariable("OSSRH_PASSWORD")
            if (ossrhUsername.isPresent && ossrhPassword.isPresent) {
                username.set(ossrhUsername.get())
                password.set(ossrhPassword.get())
            }
        }
    }
}

// do not generate extra load on Nexus with new staging repository if signing fails
val initializeSonatypeStagingRepository by tasks.existing
subprojects {
    initializeSonatypeStagingRepository {
        shouldRunAfter(tasks.withType<Sign>())
    }
}