package me.lightless.izumi.plugin.timer.impl

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.lightless.izumi.ApplicationContext
import me.lightless.izumi.plugin.timer.ITimer
import net.mamoe.mirai.message.data.buildMessageChain
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode

@Suppress("unused")
class Life: ITimer {

    private val logger = LoggerFactory.getLogger(javaClass)

    override val name: String
        get() = "life"

    // 每分钟运行一次
    override val period: Long
        get() = 60 * 1000

    override suspend fun process() {
        logger.debug("$name start!")

        val bot = ApplicationContext.bot
        if (bot == null) {
            logger.debug("[$name] bot instance is null!")
            return
        }
        val groupNumber = ApplicationContext.botConfig?.allowedGroups ?: listOf()
        logger.debug("groupNumber: $groupNumber")

        while (true) {
            val datetime = DateTime()
            val h: Int = datetime.hourOfDay
            val m: Int = datetime.minuteOfHour

            // 每天早上 9 点 30 分，发送今年的进度
            if (h == 9 && m == 30) {
                val days = datetime.dayOfYear
                val percent = if (datetime.year().isLeap) {
                    BigDecimal(days / 366.00 * 100.00).setScale(2, RoundingMode.HALF_UP)
                } else {
                    BigDecimal(days / 365.00 * 100.00).setScale(2, RoundingMode.HALF_UP)
                }

                val message = "【虚度光阴小助手】 \n今天是 ${datetime.year} 年的第 $days 天，今年已经过了 $percent% 啦！"
                bot.launch {
                    groupNumber.forEach {
                        bot.getGroup(it)?.sendMessage(buildMessageChain {
                            add(message)
                        })
                    }
                }
                // 多 sleep 5 秒，防止同一分钟内发两次消息
                delay(1000 * 65)
            } else {
                // 如果不是 9 点 30 分就直接 sleep
                delay(1000 * 60)
            }
        }
    }
}