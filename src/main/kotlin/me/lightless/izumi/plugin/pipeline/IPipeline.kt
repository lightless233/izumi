package me.lightless.izumi.plugin.pipeline

import net.mamoe.mirai.event.events.GroupMessageEvent
import org.slf4j.Logger

interface IPipeline {

    val logger: Logger
    val doNext: Boolean get() = true
    val order: Int get() = 5
    val name: String

    suspend fun process(event: GroupMessageEvent)

}