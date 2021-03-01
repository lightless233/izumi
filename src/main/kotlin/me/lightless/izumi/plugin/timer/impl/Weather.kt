package me.lightless.izumi.plugin.timer.impl

import com.alibaba.fastjson.JSON
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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
    private val userAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.190 Safari/537.36"

    // TODO 城市列表，先写死，以后增加查询功能
    private val cityList = listOf(
        "101210101",    // 杭州
        "101210106",    // 余杭区
        "101210113",    // 西湖区
        "101210114",    // 滨江区
        "101010100",    // 北京
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

        while (true) {
            val datetime = DateTime()
            val h = datetime.hourOfDay
            val m = datetime.minuteOfHour

            // 每天晚上 7:30 发送天气预报
            if (h == 19 && m == 30) {
                val client = HttpClient(OkHttp) {
                    install(UserAgent) { agent = this@Weather.userAgent }
                }
                cityList.forEach {
                    val url = this.buildFullApiUrl(it, apiKey)
                    val respJsonStr = client.get<String>(url)
                    this.logger.debug("[$name] response content: $respJsonStr")

                    val jsonObj = JSON.parseObject(respJsonStr)
                    val code = jsonObj.getString("code")
                    if (code != "200") {
                        this.logger.debug("[$name] response code error, code: $code")
                        return
                    }

                    val tomorrowWeather = jsonObj.getJSONArray("daily").getJSONObject(0)
                    val temp = "${jsonObj.getString("tempMin")}°C~${jsonObj.getString("tempMax")}°C"

                    val textDay = jsonObj.getString("textDay")
                    val textNight = jsonObj.getString("textNight")
                    val weatherText = if (textDay == textNight) {
                        textDay
                    } else {
                        "$textDay 转 $textNight"
                    }
                }


            } else {
                delay(1000 * 60)
            }
        }

    }

    private fun buildFullApiUrl(cityId: String, apiKey: String): String {
        return "${this.apiUrl}?key=${apiKey}&location=${cityId}"
    }

    private fun getHttpClient() {

    }

}
