package me.lightless.izumi

import kotlinx.coroutines.runBlocking
import me.lightless.izumi.config.ConfigParser
import me.lightless.izumi.core.Dispatcher
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.utils.BotConfiguration
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class MainApp {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun start() {
        logger.info("Starting izumi bot...")
        // 获取当前工作路径
        ApplicationContext.cwd = System.getProperty("user.dir")
        logger.info("CWD: ${ApplicationContext.cwd}")

        // 加载配置文件
        // TODO: 配置文件名称从命令行参数传递
        logger.debug("starting config file...")
        val parser = ConfigParser("izumi_config.yml")
        ApplicationContext.botConfig = parser.parse()
        if (ApplicationContext.botConfig == null) {
            logger.error("Can't load config file, default config file name is `izumi_config.yml`, exit...")
            exitProcess(-1)
        }
        logger.info("Load config file finished.")
        logger.debug("config: ${ApplicationContext.botConfig}")

        // connect db

        // 启动 bot 实例
        val botInstance = BotFactory.newBot(
            ApplicationContext.botConfig!!.botQQ,
            ApplicationContext.botConfig!!.botPassword
        ) {
            protocol = BotConfiguration.MiraiProtocol.ANDROID_PAD
            fileBasedDeviceInfo("botDevice.json")
        }.alsoLogin()

        ApplicationContext.bot = botInstance

        // 开启消息分发
        val dispatcher = Dispatcher()
        botInstance.eventChannel.subscribeAlways<GroupMessageEvent> { event ->
            dispatcher.onGroupMessage(event, subject)
        }
        this.logger.info("MessageDispatcher done.")

        // 开启定时器

        logger.info("izumi start finished.")
    }

}

fun main(): Unit = runBlocking {
    val app = MainApp()
    app.start()
}
