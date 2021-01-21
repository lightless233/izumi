package me.lightless.izumi.plugin.pipeline.impl

import me.lightless.izumi.plugin.pipeline.IPipeline
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.random.Random

@Suppress("unused")
class Repeat : IPipeline {
    override val logger: Logger
        get() = LoggerFactory.getLogger(javaClass)
    override val name: String
        get() = "repeat"

    // 记录消息的时候需要加上群号，不然测试群的消息会干扰online的情况
    private var messageRecord = mutableMapOf<Long, MutableMap<String, Any?>>()

    // 格式化小数用的formatter
    private val formatter: DecimalFormat = DecimalFormat("0.##")

    init {
        formatter.roundingMode = RoundingMode.FLOOR
    }

    override suspend fun process(event: GroupMessageEvent) {
        // 不能使用这个 groupMessage.message.contentToString() 方式取 消息内容，图片消息会变成 [图片]
        // 从而导致误禁言的问题

        // 取出消息记录
        val groupNumber = event.group.id
        var map = messageRecord[groupNumber]
        if (map == null) {
            map = mutableMapOf("msg" to "", "cnt" to 0)
            messageRecord[groupNumber] = map
        }

        // 获取消息内容
        var cleanMessage = ""
        event.message.forEach {
            cleanMessage += when (it) {
                is Image -> it.imageId
                is Face -> it.toString()
                else -> it.content
            }
        }
        logger.debug("clean message: $cleanMessage")

        // 累加消息计数
        if (map["msg"] == cleanMessage) {
            map["cnt"] = map["cnt"] as Int + 1
        } else {
            map["cnt"] = 1
            map["msg"] = cleanMessage
        }

        // 禁言流程
        val repeatCnt = map["cnt"] as Int
        when {
            repeatCnt <= 2 -> {
                return
            }
            repeatCnt >= 10 -> {
                try {
                    event.sender.mute(60 * 10)
                } catch (e: PermissionDeniedException) {
                    event.group.sendMessage(
                        messageChainOf(PlainText("嘤嘤嘤，没有权限抓人呢！"))
                    )
                    return
                }
                event.group.sendMessage(
                    messageChainOf(
                        At(event.sender),
                        PlainText("\n您怕不是个复读机吧，劝你次根香蕉🍌冷静冷静！")
                    )
                )
            }
            else -> {
                // 计算禁言概率
                val prob = formatter.format(3.0 / (11 - repeatCnt))
                val userPoint = formatter.format(Random.nextFloat())
                logger.debug("prob: $prob, userPoint: $userPoint")

                if (userPoint < prob) {
                    try {
                        event.sender.mute((repeatCnt * repeatCnt / 2) * 60)
                    } catch (e: PermissionDeniedException) {
                        event.group.sendMessage(
                            messageChainOf(PlainText("嘤嘤嘤，没有权限抓人呢！"))
                        )
                        return
                    }
                    event.group.sendMessage(buildMessageChain {
                        add(At(event.sender))
                        add("\n嘤嘤嘤，复读机被抓住了呢！劝你次根香蕉🍌冷静冷静！")
                        add("\n禁言概率：$prob, 你的点数: $userPoint")
                    })
                }

            }
        }

    }
}