package me.lightless.izumi.plugin.pipeline.impl

import me.lightless.izumi.dao.ChatMessageDAO
import me.lightless.izumi.plugin.pipeline.IPipeline
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Face
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.content
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 这个 pipeline 只管记录所有的消息
 * 另外的 timer 来计算龙王的数据
 */
@Suppress("unused")
class LogMessage : IPipeline {
    override val logger: Logger
        get() = LoggerFactory.getLogger(javaClass)
    override val name: String
        get() = "log_message"

    /**
     * 记录所有的消息
     */
    override suspend fun process(event: GroupMessageEvent) {

        val message = event.message
        val senderId = event.sender.id
        val senderNick = event.sender.nameCardOrNick
        val groupId = event.group.id

        // 获取clean的消息内容
        var cleanMessage = ""
        message.forEach {
            cleanMessage += when (it) {
                is Image -> it.imageId
                is Face -> it.toString()
                else -> it.content
            }
        }
        logger.debug("[${this.name}] clean message: $cleanMessage")

        // 记录消息
        transaction {
            ChatMessageDAO.new {
                this.qq = senderId
                this.nickname = senderNick
                this.groupId = groupId
                this.message = cleanMessage
            }
        }
    }
}