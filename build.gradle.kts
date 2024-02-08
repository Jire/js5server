plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow")

    `maven-publish`
    signing
}

group = "org.jire"
version = "1.0.0"

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
    )) implementation("io.netty:netty-$module:4.1.106.Final")

    implementation("io.netty.incubator:netty-incubator-transport-native-io_uring:0.0.24.Final")

    implementation("it.unimi.dsi:fastutil:8.5.13")
    implementation("org.jctools:jctools-core:4.0.3")

    for (module in listOf("buffer", "cache"))
        implementation("org.openrs2:openrs2-$module:0.1.0-SNAPSHOT")
}

kotlin {
    jvmToolchain(17)
}

java {
    withJavadocJar()
    withSourcesJar()
}

val ossUsername: String? by lazy { System.getProperty("OSS_USERNAME") }
val ossPassword: String? by lazy { System.getProperty("OSS_PASSWORD ") }

publishing {
    repositories {
        maven {
            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = ossUsername
                password = ossPassword
            }
        }
    }
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