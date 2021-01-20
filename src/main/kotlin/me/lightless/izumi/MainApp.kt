package me.lightless.izumi

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class MainApp {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun start() {
        logger.info("Hello!")
    }

}

fun main(): Unit = runBlocking {
    val app = MainApp()
    app.start()
}