package me.lightless.izumi.core.handler

import me.lightless.izumi.ApplicationContext
import me.lightless.izumi.Constant
import me.lightless.izumi.plugin.pipeline.IPipeline
import me.lightless.izumi.util.BotLoader
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.slf4j.LoggerFactory
import kotlin.reflect.full.createInstance

class MessageHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    private var pipelines = mutableListOf<IPipeline>()

    init {
        logger.info("MessageHandler init...")

        var enabledPipelines = ApplicationContext.botConfig?.enabledPipelines
        enabledPipelines = enabledPipelines ?: emptyList()

        val pipelineClassname = BotLoader.load(Constant.PIPELINE_PACKAGE_NAME)
        logger.debug("All pipeline classname: $pipelineClassname")
        pipelineClassname.forEach {
            try {
                val p = Class.forName(it).kotlin.createInstance() as IPipeline
                if (p.name in enabledPipelines) {
                    logger.debug("register pipeline $it")
                    pipelines.add(p)
                }
            } catch (e: ClassNotFoundException) {
                logger.error("Can't register pipeline class: $it")
            }
        }

        // 根据优先级排序
        // order 越大，优先级越高
        pipelines.sortedByDescending { it.order }

        logger.info(
            "pipeline load finished. count: ${pipelines.size}, " +
                    "list: ${pipelines.joinToString { "${it.name}_${it.order}" }}"
        )
    }

    suspend fun dispatch(event: GroupMessageEvent) {
        pipelines.forEach {
            it.process(event)
            if (!it.doNext) {
                logger.debug("Abort process pipeline by ${it.name}")
                return
            }
        }
    }

}