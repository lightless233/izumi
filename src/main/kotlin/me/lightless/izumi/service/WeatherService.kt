package me.lightless.izumi.service

import com.alibaba.fastjson.JSON
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import org.slf4j.LoggerFactory


class WeatherService(
    private val apiKey: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val userAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.190 Safari/537.36"

    private val baseAPI = "https://devapi.qweather.com"
    private val weather3dAPI = "${baseAPI}/v7/weather/3d"

    private val httpClient = HttpClient(OkHttp) {
        install(UserAgent) {
            agent = userAgent
        }
    }

    suspend fun getTomorrowWeather(cityId: String): SimpleResult<String> {
        val finalUrl = "${weather3dAPI}?key=${apiKey}&location=${cityId}"
        this.logger.debug("finalUrl: $finalUrl")

        val content = this.httpClient.get<String>(finalUrl)
        this.logger.debug("response content: $content")
        val jsonObject = JSON.parseObject(content)

        // 开始解析 json
        val code = jsonObject.getString("code")
        if (code != "200") {
            return SimpleResult(false, "response code error, code: $code", null)
        }

        val tomorrowData = jsonObject.getJSONArray("daily").getJSONObject(0)
        val sb = StringBuilder()

        // 气温
        val minTemperature = tomorrowData.getString("tempMin")
        val maxTemperature = tomorrowData.getString("tempMax")
        sb.append("明天气温：$minTemperature°C~$maxTemperature°C\n")

        // 天气
        val dayWeather = tomorrowData.getString("textDay")
        val nightWeather = tomorrowData.getString("textNight")
        val textWeather = if (dayWeather == nightWeather) {
            "明天全天${dayWeather}\n"
        } else {
            "明天白天${dayWeather}，夜间${nightWeather}\n"
        }
        sb.append(textWeather)

        // 湿度和降水量
        val humidity = tomorrowData.getString("humidity")
        val precip = tomorrowData.getString("precip")
        sb.append("明天相对湿度${humidity}%，预计降水量${precip}毫米\n")

        // 风向
        val windDirDay = tomorrowData.getString("windDirDay")
        val windScaleDay = tomorrowData.getString("windScaleDay")
        val windSpeedDay = tomorrowData.getString("windSpeedDay")
        val windDirNight = tomorrowData.getString("windDirNight")
        val windScaleNight = tomorrowData.getString("windScaleNight")
        val windSpeedNight = tomorrowData.getString("windSpeedNight")
        sb.append("明天白天，${windDirDay}${windScaleDay}级，风速${windSpeedDay}公里/小时\n")
        sb.append("明天夜间，${windDirNight}${windScaleNight}级，风速${windSpeedNight}公里/小时\n")

        // 紫外线
        val uvIndex = tomorrowData.getString("uvIndex")
        sb.append("明天紫外线等级${uvIndex}级")

        return SimpleResult(true, "", sb.toString())
    }

}
