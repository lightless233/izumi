package me.lightless.izumi.plugin.command.impl

import com.alibaba.fastjson.JSON
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import me.lightless.izumi.ApplicationContext
import me.lightless.izumi.Constant
import me.lightless.izumi.plugin.command.ICommand
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.buildMessageChain
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.*

@KtorExperimentalAPI
@Suppress("unused")
class ColorImage : ICommand {
    override val commandName: String
        get() = "color_image"
    override val logger: Logger
        get() = LoggerFactory.getLogger(javaClass)
    override val command: List<String>
        get() = listOf("/chong", "/c", "/kkp", "/bgs", "/bugouse", "/r18", "/nsfw")

    private val r18Commands = listOf("/r18")
    private val nsfwCommands = listOf("/nsfw")
    private val showRawImageCommands = listOf("/kkp", "/bgs", "/bugouse")

    private var r18switch = false
    private var nsfwSwitch = true

    private var latestImageUrl: String? = null
    private var latestImagePixivId: Long? = null

    override suspend fun handler(cmd: String, event: GroupMessageEvent) {
        this.logger.debug("$commandName called!")

        // 检查 key 的配置
        val colorImageKey = ApplicationContext.botConfig?.colorImageKey
        if (colorImageKey == null) {
            this.logger.warn("no `colorImageKey` in config file...")
            event.group.sendMessage(buildMessageChain {
                add(At(event.sender))
                add("\nColorImageKey配置错误, 请联系 Bot 维护人员!")
            })
            return
        }

        // 解析命令参数
        val message = event.message.contentToString()
        val msgList = message.split("""\s+""".toRegex())
        val arg = msgList.getOrNull(1)

        // 处理开关
        if (cmd in r18Commands) {
            processR18(arg, event)
            return
        }

        if (cmd in nsfwCommands) {
            processNSFW(arg, event)
            return
        }

        // 用户是否要求高清图
        if (cmd in showRawImageCommands) {
            processShowRawImageRequest(event)
            return
        }

        // 发图
        val fullUrl = buildURL(arg, colorImageKey)
        this.logger.debug("Final API URL: $fullUrl")

        val responseJson = this.getHttpClient().use {
            it.get<String>(fullUrl)
        }
        this.logger.debug("Response Content: $responseJson")

        val obj = JSON.parseObject(responseJson)
        this.logger.debug("Json OBJ: $obj")
        when {
            obj["code"] == 429 -> {
                // 超限制了
                event.group.sendMessage(buildMessageChain {
                    add(At(event.sender))
                    add("调用次数已达当日上限!")
                })
                return
            }
            obj["code"] == 404 -> {
                event.group.sendMessage(buildMessageChain {
                    add(At(event.sender))
                    add("真是奇怪的XP系统呢，找不到你要的涩图呢！")
                })
                return
            }
            obj["code"] != 0 -> {
                event.group.sendMessage(buildMessageChain {
                    add(At(event.sender))
                    add("API 出问题了！")
                })
                return
            }
        }

        // 解析元信息
        val imageData = obj.getJSONArray("data").getJSONObject(0)
        val pixivId = imageData.getLong("pid")
        val title = imageData.getString("title")
        val imageUrl = imageData.getString("url")
        val tags = imageData.getJSONArray("tags").joinToString()
        // 防止被夹，编码一次
        val bTags = Base64.getEncoder().encodeToString(tags.toByteArray())

        this.latestImagePixivId = pixivId
        this.latestImageUrl = imageUrl

        // 如果开了 NSFW 模式，那么只发送链接
        if (this.nsfwSwitch) {
            event.group.sendMessage(buildMessageChain {
                add(At(event.sender))
                add("\nTitle: $title")
                add("\nPixivId: $pixivId")
                add("\nTags: $bTags")
                add("\nImageUrl: $imageUrl")
                add("\n由于 NSFW 开关已开启，暂时屏蔽涩图，请移步 pixiv 查看：\nhttps://www.pixiv.net/artworks/$pixivId")
            })
            return
        }

        // 非 NSFW 情况下，获取图片数据
        // TODO 打码处理
        val image = this.getImage(imageUrl)
        event.group.sendImage(image)
        event.group.sendMessage(buildMessageChain {
            add(At(event.sender))
            add("\nTitle: $title")
            add("\nPixivId: $pixivId")
            add("\nTags: $bTags")
            add("\nImageUrl: $imageUrl")
        })
    }

    override fun checkRole(qq: Long): Boolean {
        // 任何人都可以使用这个命令
        return true
    }

    private fun doMosaic() {
        TODO()
    }

    @KtorExperimentalAPI
    private fun getHttpClient(): HttpClient {
        val botConfig = ApplicationContext.botConfig
        val useProxy = botConfig?.useProxy
        if (useProxy == null) {
            // 不是用代理，返回一个正常的client
            return HttpClient(OkHttp) {
                CurlUserAgent()
            }
        } else {
            // 代理的情况，从配置文件中读取代理
            val proxyIp = botConfig.proxyIp
            val proxyPort = botConfig.proxyPort
            val socksProxy = ProxyBuilder.socks(host = proxyIp, port = proxyPort)
            return HttpClient(OkHttp) {
                CurlUserAgent()
                engine {
                    proxy = socksProxy
                }
            }
        }
    }

    @KtorExperimentalAPI
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun getImage(url: String): InputStream {
        val client = getHttpClient()
        val response = client.use {
            it.get<HttpResponse>(url)
        }
        // 这有个 BUG ，ImageIO.read 会有个 warning
        // 先用 @Suppress 规避掉
        // https://youtrack.jetbrains.com/issue/KTIJ-838
//        return withContext(Dispatchers.IO) {
//            return@withContext ImageIO.read(response.receive<InputStream>())
//        }
        return response.receive()
    }

    private suspend fun processR18(arg: String?, event: GroupMessageEvent) {
        when (arg) {
            in listOf("on", "enable", "true") -> {
                this.r18switch = true
                event.group.sendMessage(buildMessageChain {
                    add(At(event.sender))
                    add("\nR18 开关已打开，请谨慎看图!")
                })
            }
            in listOf("off", "disable", "false") -> {
                this.r18switch = false
                event.group.sendMessage(buildMessageChain {
                    add(At(event.sender))
                    add("\nR18 开关已关闭!")
                })
            }
            in listOf("status") -> {
                event.group.sendMessage(buildMessageChain {
                    add(At(event.sender))
                    add("\n当前 R18 开关状态：${this@ColorImage.r18switch}")
                })
            }
            else -> {
                event.group.sendMessage(buildMessageChain {
                    add(At(event.sender))
                    add("\n???你在说什么?")
                })
            }
        }
    }

    private suspend fun processNSFW(arg: String?, event: GroupMessageEvent) {
        when (arg) {
            in listOf("on", "enable", "true") -> {
                this.nsfwSwitch = true
                event.group.sendMessage(buildMessageChain {
                    add(At(event.sender))
                    add("\nNSFW 开关已开启!")
                })
            }
            in listOf("off", "disable", "false") -> {
                this.nsfwSwitch = false
                event.group.sendMessage(buildMessageChain {
                    add(At(event.sender))
                    add("\nNSFW 开关已关闭, 请谨慎看图!")
                })
            }
            in listOf("status") -> {
                event.group.sendMessage(buildMessageChain {
                    add(At(event.sender))
                    add("\n当前 NSFW 开关状态：${this@ColorImage.nsfwSwitch}")
                })
            }
            else -> {
                event.group.sendMessage(buildMessageChain {
                    add(At(event.sender))
                    add("\n???你在说什么?")
                })
            }
        }
    }

    private fun buildURL(arg: String?, colorKey: String): String {
        var url = "${Constant.COLOR_IMAGE_API}&apikey=${colorKey}"
        url = if (this.r18switch) {
            "${url}&r18=1"
        } else {
            "${url}&r18=0"
        }

        if (arg != null) {
            url = "${url}&keyword=$arg"
        }

        return url
    }

    @KtorExperimentalAPI
    private suspend fun processShowRawImageRequest(event: GroupMessageEvent) {
        if (latestImageUrl == null) {
            event.group.sendMessage(buildMessageChain {
                add(At(event.sender))
                add("\n???")
            })
        } else {
            if (this.nsfwSwitch) {
                event.group.sendMessage(buildMessageChain {
                    add(At(event.sender))
                    add("\n由于 NSFW 开关已开启，暂时屏蔽涩图，请移步 pixiv 查看：\nhttps://www.pixiv.net/artworks/$latestImagePixivId")
                })
            } else {
                try {
                    val image = getImage(latestImageUrl!!)
                    event.group.sendImage(image)
                } catch (e: Exception) {
                    e.printStackTrace()
                    event.group.sendMessage(buildMessageChain {
                        add(At(event.sender))
                        add("\n出错了喵，换一张看吧...(${e.message})")
                        add("\n当前 pixiv id: $latestImagePixivId")
                    })
                }
            }
        }
    }
}