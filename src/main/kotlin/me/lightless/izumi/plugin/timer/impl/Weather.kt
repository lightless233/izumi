package me.lightless.izumi.plugin.timer.impl

import kotlinx.coroutines.delay
import me.lightless.izumi.ApplicationContext
import me.lightless.izumi.plugin.timer.ITimer
import me.lightless.izumi.service.WeatherService
import net.mamoe.mirai.message.data.buildMessageChain
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import kotlin.collections.emptyList
import kotlin.collections.forEach
import kotlin.collections.listOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set

@Suppress("unused")
class Weather : ITimer {
    override val name: String
        get() = "weather"
    override val period: Long
        get() = 60 * 1000

    private val logger = LoggerFactory.getLogger(javaClass)

    private val apiUrl = "https://devapi.qweather.com/v7/weather/3d"
    private val userAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.190 Safari/537.36"

    // TODO 城市列表，先写死，以后增加查询功能
    private val cityList = listOf(
        "101210101",    // 杭州
        "101210106",    // 余杭区
        "101210113",    // 西湖区
        "101210114",    // 滨江区
        "101010100",    // 北京
        "101020100",    // 上海
    )
    private val newCityList = mutableMapOf<String, String>().apply {
        this["101210101"] = "杭州市区"
        this["101210106"] = "杭州市余杭区"
        this["101210113"] = "杭州市西湖区"
        this["101210114"] = "杭州市滨江区"
        this["101010100"] = "北京市区"
        this["101020100"] = "上海市区"
    }

    override suspend fun process() {

        this.logger.debug("$name start.")

        val bot = ApplicationContext.bot
        if (bot == null) {
            this.logger.debug("[$name] bot instance is null!")
            return
        }

        val groupNumber = ApplicationContext.botConfig?.allowedGroups ?: emptyList()
        val apiKey = ApplicationContext.botConfig?.weatherKey
        if (apiKey == null) {
            this.logger.debug("[$name] weatherKey is null!")
            return
        }
        val weatherService = WeatherService(apiKey)

        while (true) {
            val datetime = DateTime()
            val h = datetime.hourOfDay
            val m = datetime.minuteOfHour

            // 每天晚上 7:30 发送天气预报
            if (h == 19 && m == 30) {
                cityList.forEach { cityId ->
                    val result = weatherService.getTomorrowWeather(cityId)
                    if (result.success) {
                        val weatherString = result.data
                        this.logger.debug("weatherString: $weatherString")
                        // 发到 qq 群里
                        groupNumber.forEach { groupNum ->
                            bot.getGroup(groupNum)?.sendMessage(buildMessageChain {
                                add(newCityList[cityId] ?: "Unknown City")
                                add(weatherString!!)
                            })
                        }
                    }
                }
                delay(1000 * 65)
            } else {
                delay(1000 * 60)
            }
        }

    }
}
