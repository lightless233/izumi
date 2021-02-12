package me.lightless.izumi.plugin.command.impl

import me.lightless.izumi.plugin.command.ICommand
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.buildMessageChain
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class About : ICommand {
    override val commandName: String
        get() = "about"
    override val logger: Logger
        get() = LoggerFactory.getLogger(javaClass)
    override val command: List<String>
        get() = listOf("/about", "/version")


    override suspend fun handler(cmd: String, event: GroupMessageEvent) {
        logger.debug("$commandName called")
        val message = """
            |BotIzumi Project
            |Version: 1.0.5-SNAPSHOT
            |
            |A QQ bot based on mirai (https://github.com/mamoe/mirai).
            |
            |Github: https://github.com/lightless233/izumi
            |CHANGELOG: https://github.com/lightless233/izumi/blob/master/CHANGELOG.MD
            |
            |If you find any bugs or new ideas, feel free to report at: https://github.com/lightless233/izumi/issues
            |
            |Contributors
            |lightless, GeruzoniAnsasu
        """.trimMargin()

        event.group.sendMessage(buildMessageChain {
            add(At(event.sender))
            add("\n")
            add(message)
        })
    }

    override fun checkRole(qq: Long): Boolean {
        // 所有人都可以调用
        return true
    }
}
