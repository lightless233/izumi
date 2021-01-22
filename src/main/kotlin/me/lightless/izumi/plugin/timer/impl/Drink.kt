package me.lightless.izumi.plugin.timer.impl

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.lightless.izumi.ApplicationContext
import me.lightless.izumi.plugin.timer.ITimer
import net.mamoe.mirai.message.data.buildMessageChain
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class Drink : ITimer {

    val logger: Logger = LoggerFactory.getLogger(javaClass)

    override val name: String
        get() = "drink"

    // 每分钟运行一次
    override val period: Long
        get() = 60 * 1000

    override suspend fun process() {
        val groupNumber = ApplicationContext.botConfig?.allowedGroups ?: listOf()
        logger.debug("groupNumber: $groupNumber")
        val weekend = listOf(DateTimeConstants.SUNDAY, DateTimeConstants.SATURDAY)

        while (true) {

            val datetime = DateTime()
            val h = datetime.hourOfDay
            val m = datetime.minuteOfHour

            // 跳过周末
            if (datetime.dayOfWeek in weekend) {
                delay(1000 * 3600 * 2)
                continue
            }

            if (h < 10 || h > 18 || h == 13) {
                delay(1000 * 30)
                continue
            }

            if (m == 0) {
                if (h == 18) {
                    sendDrinkMsg("【喝水提醒小助手】\n该喝水了哦~\n晚上也要多~喝~水~哦~\n\n防止痛风从喝水做起~", groupNumber)
                } else if (h % 2 == 0) {
                    sendDrinkMsg("【喝水提醒小助手】\n该喝水了哦~\n\n防止痛风从喝水做起~", groupNumber)
                }
                // 整点的时候多sleep 5秒钟，防止在同一分钟内发出去多条消息
                delay(1000 * 65)
                continue
            }

            delay(1000 * 30)

        }
    }

    private suspend fun sendDrinkMsg(msg: String, groupNumber: List<Long>) {

        val bot = ApplicationContext.bot

        groupNumber.forEach {
            bot?.launch {
                bot.getGroup(it)?.sendMessage(buildMessageChain {
                    add(msg)
                })
            } ?: logger.error("bot instance is null!")
        }
    }

}