package me.lightless.izumi.config

import me.lightless.izumi.ApplicationContext
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class ConfigParser(private val filename: String) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var parsedConfig: BotConfig

    fun parse(): BotConfig? {
        val cwd = ApplicationContext.cwd ?: return null
        val configPath = Paths.get(cwd, filename)

        val yamlLoader = Yaml(Constructor(BotConfig::class.java))
        try {
            val content = String(Files.readAllBytes(configPath))
            this.parsedConfig = yamlLoader.load(content)
            return this.parsedConfig
        } catch (e: NoSuchFileException) {
            logger.error("No config file found, default config file name is `izumi_config.yml`, exit...")
            exitProcess(-1)
        }
    }

    fun getConfig(): BotConfig {
        return parsedConfig
    }

}
