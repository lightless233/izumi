package me.lightless.izumi.plugin.command.impl

import me.lightless.izumi.plugin.command.ICommand
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.buildMessageChain
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class Help:ICommand {
    override val commandName: String
        get() = "help"
    override val logger: Logger
        get() = LoggerFactory.getLogger(javaClass)
    override val command: List<String>
        get() = listOf("/help")

    /**
     * 任何人都可以使用
     */
    override fun checkRole(qq: Long): Boolean {
        return true
    }

    override suspend fun handler(cmd: String, event: GroupMessageEvent) {
        this.logger.debug("$commandName called.")

        val message = """
            | /help - 显示本菜单
            | /about, /version - 显示 bot 信息
            | /c, /chong - 获取一张涩图 (WIP)
            | /kkp, /bgs, /bugouse - 显示涩图原图 (WIP)
            | /r18 - 涩图 R18 开关
            | /nsfw - NSFW 开关
        """.trimMargin()

        event.group.sendMessage(buildMessageChain {
            add(At(event.sender))
            add("\n")
            add(message)
        })
    }



}