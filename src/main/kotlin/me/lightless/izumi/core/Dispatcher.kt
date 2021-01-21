package me.lightless.izumi.core

import me.lightless.izumi.ApplicationContext
import me.lightless.izumi.core.handler.CommandHandler
import me.lightless.izumi.core.handler.MessageHandler
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.slf4j.LoggerFactory

class Dispatcher {

    private val logger = LoggerFactory.getLogger(javaClass)

    private var allowedGroups: List<Long>

    // 测试是否为 command 使用的正则
    private val commandPattern = Regex("^/[a-zA-Z0-9-_.]+\\b")

    // command handler
    private val commandHandler = CommandHandler()
    private val messageHandler = MessageHandler()

    init {
        logger.info("Dispatcher start!")
        allowedGroups = ApplicationContext.botConfig!!.allowedGroups
    }

    private fun isCommand(message: String): Boolean {
        return commandPattern.containsMatchIn(message)
    }

    /**
     * 处理群组消息
     */
    suspend fun onGroupMessage(groupMessage: GroupMessageEvent, group: Group) {

        val message = groupMessage.message
        val sender = groupMessage.sender

        if (group.id !in allowedGroups) {
            return
        }

        logger.debug("[$group] [$sender] $message")

        when {
            isCommand(message.contentToString()) -> commandHandler.dispatch(groupMessage, group)
            else -> messageHandler.dispatch(groupMessage)
        }

    }

    /**
     * 未来将支持私聊消息，目前先不管
     */
    fun onPrivateMessage() {
        TODO()
    }
}
