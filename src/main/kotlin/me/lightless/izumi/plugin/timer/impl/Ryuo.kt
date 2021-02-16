package me.lightless.izumi.plugin.timer.impl

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.lightless.izumi.ApplicationContext
import me.lightless.izumi.dao.ChatMessage
import me.lightless.izumi.dao.ChatMessageDAO
import me.lightless.izumi.dao.RyuoDAO
import me.lightless.izumi.plugin.timer.ITimer
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact.Companion.sendImage
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
            // 每天早上 10 点，发送龙王数据
            val datetime = DateTime()
            if (datetime.hourOfDay == 10 && datetime.minuteOfHour == 0) {
                this.doProcess(allowedGroups, bot)

                // 如果是周五，发送彩蛋信息
                if (datetime.dayOfWeek == 5) {
                    val pic = javaClass.classLoader.getResourceAsStream("friday.jpg")
                    for (allowedGroupId in allowedGroups) {
                        if (pic != null) {
                            bot.launch {
                                bot.getGroup(allowedGroupId)?.sendImage(pic)
                                bot.getGroup(allowedGroupId)?.sendMessage(buildMessageChain {
                                    add("今天是周五啦，快乐摸鱼！")
                                })
                            }
                        }
                    }
                }

                // 多 sleep 5 秒，防止同一分钟内发两次消息
                delay(65 * 1000)
            } else {
                delay(10 * 1000)
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
                ryuoInnerMap[msgDAO.qq] = 1
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

            // 需要处理消息数量相同的情况
            val ryuoIds = mutableListOf<Long>()
            var maxCount = 0
            var yesterdayMessage = "昨日摸鱼：\n"
            sortedInnerMap.forEach { (t, u) ->
                if (u >= maxCount) {
                    ryuoIds.add(t)
                    maxCount = u.toInt()
                }
                yesterdayMessage += "${nicknameInnerMap[t]}($t) -> $u\n"
            }

            var fullMessage = buildMessageChain {
                add("【摸鱼助手】 恭喜 ")
            }
            for (rid in ryuoIds) {
                fullMessage = fullMessage.plus(At(rid))
            }
            fullMessage = fullMessage.plus(buildMessageChain {
                add(" 成为今天的龙王🐉，快来给大家表演个喷水吧！🐉\n")
                add(yesterdayMessage)
            })

            // FIXME 这么写，会导致某天有多位龙王的时候，连任计数出错，以后再改
            for (rid in ryuoIds) {
                val historyCnt = this.checkHistory(rid)
                fullMessage = fullMessage.plus(buildMessageChain {
                    add("\n")
                    add(At(rid))
                    add(" 已经连任 $historyCnt 天的龙王了，加油哦~")
                })
            }

            // 存起来
            transaction {
                for (rid in ryuoIds) {
                    RyuoDAO.new {
                        this.qq = rid
                        this.nickname = nicknameInnerMap[rid]!!
                        this.groupId = allowedGroupId
                        this.msgCount = maxCount.toLong()
                    }
                }
            }

            bot.launch {
                bot.getGroup(allowedGroupId)?.sendMessage(fullMessage)
            }

        }
    }

    private fun checkHistory(todayRyuo: Long): Int = transaction {
        val ryuoList = transaction { RyuoDAO.all().sortedByDescending { it.createdTime } }
        var cnt = 1
        ryuoList
            .takeWhile { it.qq == todayRyuo }
            .forEach { _ -> cnt += 1 }

        return@transaction cnt
    }

}
