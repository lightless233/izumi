package me.lightless.izumi.plugin.pipeline.impl

import me.lightless.izumi.plugin.pipeline.IPipeline
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.event.events.GroupEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Face
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class Thumb : IPipeline {
    override val logger: Logger
        get() = LoggerFactory.getLogger(javaClass)
    override val name: String
        get() = "thumb"

    /*
    self.blacklist = [
        "&#91;强&#93;",
        "[CQ:face,id=76]",
        b"\xf0\x9f\x91\x8d".decode("UTF-8"),
        "[CQ:emoji,id=128077]",
        "4",
    ]
     */
    override suspend fun process(event: GroupMessageEvent) {
        var thumbCnt = 0

        // 检查大拇指的数量
        event.message.spliterator().forEachRemaining {
            logger.debug("message part: $it -> ${it.javaClass}")
            when (it) {
                is Face -> {
                    if (it.id == 76) {
                        thumbCnt += 1
                    }
                }
                is PlainText -> {
                    var index = it.contentToString().indexOf("\uD83D\uDC4D")
                    while (index != -1) {
                        thumbCnt += 1
                        index = it.contentToString().indexOf("\uD83D\uDC4D", index + 2)
                    }
                }
            }
        }

        // 开始禁言，一个大拇指2分钟
        logger.debug("thumb count: $thumbCnt")
        if (thumbCnt != 0) {
            // 禁言
            try {
                event.sender.mute(thumbCnt * 2 * 60)
                event.group.sendMessage(buildMessageChain {
                    add(At(event.sender))
                    add("\n嘤嘤嘤，发现大拇指了呢，呐，大拇指什么的是不可以的呢！")
                })
            } catch (e: PermissionDeniedException) {
                event.group.sendMessage(buildMessageChain {
                    add("嘤嘤嘤，没有权限夹人呢！")
                })
            }
        }
    }
}