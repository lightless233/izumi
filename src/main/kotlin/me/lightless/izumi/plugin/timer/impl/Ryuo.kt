package me.lightless.izumi.plugin.timer.impl

import me.lightless.izumi.plugin.timer.ITimer
import org.slf4j.LoggerFactory

@Suppress("unused")
class Ryuo : ITimer {

    private val logger = LoggerFactory.getLogger(javaClass)

    override val name: String
        get() = "ryuo"
    override val period: Long
        get() = 60 * 1000   // 每分钟运行一次

    override suspend fun process() {
        this.logger.debug("$name start!")

    }
}