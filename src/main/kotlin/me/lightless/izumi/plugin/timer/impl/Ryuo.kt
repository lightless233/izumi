package me.lightless.izumi.plugin.timer.impl

import kotlinx.coroutines.delay
import me.lightless.izumi.ApplicationContext
import me.lightless.izumi.dao.ChatMessage
import me.lightless.izumi.dao.ChatMessageDAO
import me.lightless.izumi.plugin.timer.ITimer
import net.mamoe.mirai.Bot
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.slf4j.LoggerFactory

@Suppress("unused")
class Ryuo : ITimer {

    private val logger = LoggerFactory.getLogger(javaClass)

    override val name: String
        get() = "ryuo"
    override val period: Long
        get() = 60 * 1000   // 每分钟运行一次

    private fun getBotInstance(): Bot? {
        return ApplicationContext.bot
    }

    override suspend fun process() {
        this.logger.debug("$name start!")
        val bot = this.getBotInstance()
        if (bot == null) {
            logger.debug("[${this.name}] bot instance is null!")
            return
        }
        val allowedGroups = ApplicationContext.botConfig?.allowedGroups ?: listOf()
        logger.debug("groupNumber: $allowedGroups")

        while (true) {
            val datetime = DateTime()
            val h = datetime.hourOfDay
            val m = datetime.minuteOfHour

            // 每天早上 9 点 30 分，发送龙王数据
            if (h == 9 && m == 30) {
//                TODO()
                val today = LocalDate.now(DateTimeZone.forOffsetHours(8))
                val yesterday = today.minusDays(1)
                val ryuoMap = mutableMapOf<Long, MutableMap<Long, Long>>()

                // 从数据库里查出来昨天的全部信息
                val messageList = transaction {
                    ChatMessageDAO.find {
                        ChatMessage.createdTime less today and
                                (ChatMessage.createdTime greaterEq yesterday)
                    }.toList()
                }

                // 计算龙王信息
                for (msgDAO in messageList) {
                    if (msgDAO.groupId !in ryuoMap.keys) {
                        ryuoMap[msgDAO.groupId] = mutableMapOf()
                    }
                    val innerMap = ryuoMap[msgDAO.groupId] ?: continue
                    if (msgDAO.qq !in innerMap.keys) {
                        innerMap[msgDAO.qq] = 0
                    } else {
                        innerMap[msgDAO.qq] = innerMap[msgDAO.qq] as Long + 1
                    }
                }
                this.logger.debug("ryuoMap: $ryuoMap")

                // 发消息
//                for (allowedGroupId in allowedGroups) {
//                    val innerMap = ryuoMap.get(allowedGroupId) ?: continue
//
//                }
//                var message = "[龙王通知] 恭喜 xxx 成为今天的龙王，快来给大家表演个喷水吧！\n"


                // 多 sleep 5 秒，防止同一分钟内发两次消息
                delay(65 * 1000)
            } else {
                delay(60 * 1000)
            }
        }
    }
}

//fun main() {
//    val today = LocalDate.now(DateTimeZone.forOffsetHours(8))
//    println(today.toString())
//
//    val yesterday = today.minusDays(1)
//    println(yesterday.toString())
//}