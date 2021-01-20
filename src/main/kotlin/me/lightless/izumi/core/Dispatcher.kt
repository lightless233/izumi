package me.lightless.izumi.core

import me.lightless.izumi.ApplicationContext
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.slf4j.LoggerFactory

class Dispatcher {

    private val logger = LoggerFactory.getLogger(javaClass)

    private var allowedGroups: List<Long>

    // 测试是否为 command 使用的正则
    private val commandPattern = Regex("^/[a-zA-Z0-9-_.]+\\b")

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
    suspend fun onGroupMessage(groupMessage: GroupMessageEvent) {

        val group = groupMessage.group
        val message = groupMessage.message
        val sender = groupMessage.sender

        if (group.id !in allowedGroups) {
            return
        }

        logger.debug("[$group] [$sender] $message")

//        when {
//            isCommand(message.contentToString()) -> commandHandler.dispatcher(groupMessage)
//            else -> messageHandler.dispatcher(groupMessage)
//        }

    }

    /**
     * 未来将支持私聊消息，目前先不管
     */
    fun onPrivateMessage() {
        TODO()
    }
}
