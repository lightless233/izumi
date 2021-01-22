package me.lightless.izumi.plugin.timer

interface ITimer {

    val name: String

    @Suppress("unused")
    val daemon: Boolean get() = true

    val period: Long

    suspend fun process()
}