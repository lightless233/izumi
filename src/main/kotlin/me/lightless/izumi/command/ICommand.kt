package me.lightless.izumi.command

import me.lightless.izumi.ApplicationContext
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.slf4j.Logger

interface ICommand {

    val commandName: String
    val logger: Logger
    val command: List<String>

    suspend fun handler(cmd: String, groupMessage: GroupMessageEvent)

    fun checkRole(qq: Long): Boolean

    fun isAdmin(qq: Long): Boolean {
        return try {
            ApplicationContext.botConfig!!.admins.contains(qq)
        } catch (e: Exception) {
            logger.debug("$commandName isAdmin failed, return false...")
            false
        }
    }
}
