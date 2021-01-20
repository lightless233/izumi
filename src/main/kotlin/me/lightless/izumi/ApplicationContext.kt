package me.lightless.izumi

import me.lightless.izumi.config.BotConfig
import net.mamoe.mirai.Bot

class ApplicationContext {

    companion object {
        // bot config
        var botConfig: BotConfig? = null

        // bot instance
        var bot: Bot? = null

        // current working dir
        var cwd: String? = null
    }

}
