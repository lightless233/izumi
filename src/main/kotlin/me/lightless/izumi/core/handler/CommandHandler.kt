package me.lightless.izumi.core.handler

import me.lightless.izumi.ApplicationContext
import me.lightless.izumi.Constant
import me.lightless.izumi.command.ICommand
import me.lightless.izumi.util.BotLoader
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import org.slf4j.LoggerFactory
import kotlin.reflect.full.createInstance

class CommandHandler {

    private val logger = LoggerFactory.getLogger(javaClass)
    private var commands = mutableListOf<ICommand>()

    init {
        logger.info("CommandHandler init...")

        var enabledCommands = ApplicationContext.botConfig?.enabledCommands
        enabledCommands = enabledCommands ?: emptyList<String>()

        val commandClassname = BotLoader.load(Constant.COMMAND_PACKAGE_NAME)
        logger.debug("All command classname: $commandClassname")
        commandClassname.forEach {
            try {
                val p = Class.forName(it).kotlin.createInstance() as ICommand
                // 只加载启用了的命令
                if (p.commandName in enabledCommands) {
                    logger.debug("register command: $it")
                    commands.add(p)
                }
            } catch (e: ClassNotFoundException) {
                logger.error("Can't register command class: $it")
            }
        }
        logger.info("Command load finished. count: ${commands.size}, list: ${commands.joinToString { it.commandName }}")

        val cl = commands.flatMap { it.command }.joinToString(", ", prefix = "[", postfix = "]")
        logger.info("Available command: $cl")
    }

    /**
     * 把命令分发到对应的 CommandHandler 上
     */
    suspend fun dispatch(event: GroupMessageEvent, group: Group) {

        val messageChain = event.message
        logger.debug("receive command message: $messageChain")
        val message = messageChain.firstIsInstanceOrNull<PlainText>()
        if (message == null) {
            logger.warn("No PlainText in messageChain, stop dispatch...")
            return
        }
        val msgArray = message.content.split("""\s+""".toRegex())

        // 遍历所有加载的 command handler
        // 如果有多个 handler 注册了相同的命令，那么都执行
        var findCommand = false
        commands.forEach {
            if (it.command.contains(msgArray[0]) && it.checkRole(event.sender.id)) {
                it.handler(msgArray[0], event)
                findCommand = true
            }
        }

        if (!findCommand) {
            group.sendMessage(messageChainOf(
                At(event.sender),
                PlainText("\nUnknown Command!")
            ))
        }

    }

}