package me.lightless.izumi.plugin.timer

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import me.lightless.izumi.ApplicationContext
import me.lightless.izumi.Constant
import me.lightless.izumi.util.BotLoader
import org.slf4j.LoggerFactory
import kotlin.reflect.full.createInstance

object TimerLoader {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val timers = mutableListOf<ITimer>()

    suspend fun loadAndStart() {
        val timerClass = BotLoader.load(Constant.TIMER_PACKAGE_NAME)
        logger.debug("timer class name: $timerClass")

        var allowedTimers = ApplicationContext.botConfig?.enabledTimers
        if (allowedTimers == null) {
            allowedTimers = emptyList()
        }

        timerClass.forEach {
            try {
                val p = Class.forName(it).kotlin.createInstance() as ITimer
                if (p.name in allowedTimers) {
                    timers.add(p)
                }
            } catch (e: ClassNotFoundException) {
                logger.error("Can't register timer class: $it")
            }
        }
        logger.debug("load finished. count: ${timers.size}, " +
                "list: ${timers.joinToString { it.name }}")

        // 启动所有的 timer，每个timer单独一个协程
        // TODO 在 global scope 上开启一堆协程不是一个好事，先跑起来，下次一定改
        timers.forEach {
            GlobalScope.async {
                try {
                    it.process()
                } catch (e: Exception) {
                    logger.error("timer error, name: ${it.name}")
                    e.printStackTrace()
                }
            }
        }
    }

}