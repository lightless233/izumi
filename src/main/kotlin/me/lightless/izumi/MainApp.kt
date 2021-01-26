package me.lightless.izumi

import kotlinx.coroutines.runBlocking
import me.lightless.izumi.config.ConfigParser
import me.lightless.izumi.core.Dispatcher
import me.lightless.izumi.dao.ChatMessage
import me.lightless.izumi.dao.Ryuo
import me.lightless.izumi.plugin.timer.TimerLoader
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.utils.BotConfiguration
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.sql.Connection
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
        logger.debug("Try to connect database...")
        this.connectDatabase()
        logger.info("Connect database finished.")

        // 启动 bot 实例
        val botInstance = BotFactory.newBot(
            ApplicationContext.botConfig!!.botQQ,
            ApplicationContext.botConfig!!.botPassword
        ) {
            protocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE
            heartbeatPeriodMillis = 30 * 1000L  // 把心跳时间调整为 30 秒，尝试解决自动断线的问题
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
        TimerLoader.loadAndStart()

        logger.info("izumi start finished.")
    }

    private fun connectDatabase() {
        val dbFilename = ApplicationContext.botConfig?.dbFilename
        if (dbFilename == null) {
            logger.error("`dbFilename` is empty, please check config file.")
            exitProcess(-1)
        }

        // 初始化 db
        Database.connect("jdbc:sqlite:$dbFilename", "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        transaction {
            addLogger(StdOutSqlLogger)  // 添加 sql 的日志
            SchemaUtils.create(
                Ryuo, ChatMessage
            )
        }
    }

}

fun main(): Unit = runBlocking {
    val app = MainApp()
    app.start()
}
