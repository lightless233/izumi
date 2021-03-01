package me.lightless.izumi.config

import kotlin.properties.Delegates

class BotConfig {
    var botQQ by Delegates.notNull<Long>()

    lateinit var botPassword: String

    lateinit var allowedGroups: List<Long>

    lateinit var admins: List<Long>

    // 需要加载的命令列表
    lateinit var enabledCommands: List<String>

    // 需要加载的定时器列表
    lateinit var enabledTimers: List<String>

    // 需要加载的 pipeline
    lateinit var enabledPipelines: List<String>

    // DB 的文件路径，未来需要支持更多的数据库类型
    // 目前仅支持 SQLite
    lateinit var dbFilename: String

    // ColorImage 需要的配置
    lateinit var colorImageKey: String

    // socks5 代理配置
    var useProxy by Delegates.notNull<Boolean>()
    lateinit var proxyIp: String
    var proxyPort by Delegates.notNull<Int>()

    // 天气预报的 key
    lateinit var weatherKey: String
}
