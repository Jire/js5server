package org.jire.js5server

import java.util.*

data class Js5ServiceConfig(
    val cachePath: String,
    val groupRepository: String,

    val listenPorts: IntArray,

    val version: Int,
    val checkVersion: Boolean,

    val supportPrefetch: Boolean,
) {

    constructor(properties: Properties) : this(
        properties.getProperty("cache_path"),
        properties.getProperty("group_repository"),

        properties.getProperty("listen_ports")
            .split(", ")
            .map { it.toInt() }
            .toIntArray(),

        properties.getProperty("version").toInt(),
        properties.getProperty("check_version").toBoolean(),

        properties.getProperty("support_prefetch").toBoolean(),
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Js5ServiceConfig

        if (!listenPorts.contentEquals(other.listenPorts)) return false
        if (version != other.version) return false
        if (checkVersion != other.checkVersion) return false
        if (supportPrefetch != other.supportPrefetch) return false
        if (cachePath != other.cachePath) return false
        if (groupRepository != other.groupRepository) return false

        return true
    }

    override fun hashCode(): Int {
        var result = listenPorts.contentHashCode()
        result = 31 * result + version
        result = 31 * result + checkVersion.hashCode()
        result = 31 * result + supportPrefetch.hashCode()
        result = 31 * result + cachePath.hashCode()
        result = 31 * result + groupRepository.hashCode()
        return result
    }

}
