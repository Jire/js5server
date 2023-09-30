plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow")
}

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

kotlin {
    jvmToolchain(17)
}

dependencies {
    val slf4jVersion = "2.0.9"
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    runtimeOnly("org.slf4j:slf4j-simple:$slf4jVersion")

    for (module in listOf(
        "handler",
        "buffer",
        "transport-native-epoll",
        "transport-native-kqueue",
    )) implementation("io.netty:netty-$module:4.1.99.Final")

    implementation("io.netty.incubator:netty-incubator-transport-native-io_uring:0.0.22.Final")

    implementation("it.unimi.dsi:fastutil:8.5.12")
    implementation("org.jctools:jctools-core:4.0.1")

    for (module in listOf("buffer", "cache"))
        implementation("org.openrs2:openrs2-$module:0.1.0-SNAPSHOT")
}