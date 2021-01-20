package me.lightless.izumi.config

import org.slf4j.LoggerFactory

class ConfigParser {

    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var parsedConfig: BotConfig

    fun parse(): BotConfig? {

        return null
    }

    fun getConfig(): BotConfig {
        return parsedConfig
    }

}