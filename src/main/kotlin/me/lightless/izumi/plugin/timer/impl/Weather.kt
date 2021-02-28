package me.lightless.izumi.plugin.timer.impl

import kotlinx.coroutines.delay
import me.lightless.izumi.ApplicationContext
import me.lightless.izumi.plugin.timer.ITimer
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

@Suppress("unused")
class Weather : ITimer {
    override val name: String
        get() = "weather"
    override val period: Long
        get() = 60 * 1000

    private val logger = LoggerFactory.getLogger(javaClass)

    private val apiUrl = "https://devapi.qweather.com/v7/weather/3d"

    // TODO 城市列表，先写死，以后增加查询功能
    private val cityList = listOf(
        "101210101",    // 杭州
        "101210106",    // 余杭区
        "101210113",    // 西湖区
        "101210114",    // 滨江区
        "101010100",    // 北京
    )

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

        while (true) {
            val datetime = DateTime()
            val h = datetime.hourOfDay
            val m = datetime.minuteOfHour

            // 每天晚上 7:30 发送天气预报
            if (h == 19 && m == 30) {

                cityList.forEach {
                    TODO()
                }


            } else {
                delay(1000 * 60)
            }
        }

    }

}
