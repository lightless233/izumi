package me.lightless.izumi.plugin.timer.impl

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.lightless.izumi.ApplicationContext
import me.lightless.izumi.dao.ChatMessage
import me.lightless.izumi.dao.ChatMessageDAO
import me.lightless.izumi.plugin.timer.ITimer
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.buildMessageChain
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
            // 每天早上 9 点 30 分，发送龙王数据
            val datetime = DateTime()
            if (datetime.hourOfDay == 9 && datetime.minuteOfHour == 30) {
                this.doProcess(allowedGroups, bot)
                // 多 sleep 5 秒，防止同一分钟内发两次消息
                delay(65 * 1000)
            } else {
                delay(60 * 1000)
            }
        }
    }

    private suspend fun doProcess(allowedGroups: List<Long>, bot: Bot) {
        val today = LocalDate.now(DateTimeZone.forOffsetHours(8))
        val yesterday = today.minusDays(1)
        val ryuoMap = mutableMapOf<Long, MutableMap<Long, Long>>()
        val nicknameMap = mutableMapOf<Long, MutableMap<Long, String>>()

        // 从数据库里查出来昨天的全部信息
        val messageList = transaction {
            ChatMessageDAO.find {
                ChatMessage.createdTime less today.toDateTimeAtStartOfDay() and
                        (ChatMessage.createdTime greaterEq yesterday.toDateTimeAtStartOfDay())
            }.toList()
        }

        // 计算龙王信息
        for (msgDAO in messageList) {
            if (msgDAO.groupId !in ryuoMap.keys) {
                ryuoMap[msgDAO.groupId] = mutableMapOf()
                nicknameMap[msgDAO.groupId] = mutableMapOf()
            }
            val ryuoInnerMap = ryuoMap[msgDAO.groupId] ?: continue
            val nicknameInnerMap = nicknameMap[msgDAO.groupId] ?: continue

            if (msgDAO.qq !in ryuoInnerMap.keys) {
                ryuoInnerMap[msgDAO.qq] = 0
            } else {
                ryuoInnerMap[msgDAO.qq] = ryuoInnerMap[msgDAO.qq] as Long + 1
            }

            nicknameInnerMap[msgDAO.qq] = msgDAO.nickname
        }
        this.logger.debug("ryuoMap: $ryuoMap")

        // 发消息
        for (allowedGroupId in allowedGroups) {
            val ryuoInnerMap = ryuoMap[allowedGroupId] ?: continue
            val nicknameInnerMap = nicknameMap[allowedGroupId] ?: continue
            val sortedInnerMap = ryuoInnerMap.entries
                .sortedByDescending { it.value }.associateBy({ it.key }, { it.value })
            this.logger.debug("sortedInnerMap: $sortedInnerMap")

            var ryuoId = 0L
            var yesterdayMessage = "昨日摸鱼：\n"
            sortedInnerMap.forEach { (t, u) ->
                if (ryuoId == 0L) {
                    ryuoId = t
                }
                yesterdayMessage += "${nicknameInnerMap[t]}($t) -> $u\n"
            }

            val fullMessage = buildMessageChain {
                add("[龙王] 恭喜 ")
                add(At(ryuoId))
                add(" 成为今天的龙王，快来给大家表演个喷水吧！\n")
                add(yesterdayMessage)
            }

            bot.launch {
                bot.getGroup(allowedGroupId)?.sendMessage(fullMessage)
            }

        }
    }

}
