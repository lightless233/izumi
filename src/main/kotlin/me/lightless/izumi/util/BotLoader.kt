package me.lightless.izumi.util

import org.slf4j.LoggerFactory
import java.io.File
import java.util.jar.JarFile

object BotLoader {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun load(packageName: String): List<String> {

        val clazzList = mutableListOf<String>()
        val jarFile = File(javaClass.protectionDomain.codeSource.location.path)
        val commandPackagePath = packageName.replace(".", "/")

        if (jarFile.isFile) {
            logger.debug("Running with JAR file")
            val jar = JarFile(jarFile)
            val e = jar.entries()
            while (e.hasMoreElements()) {
                val name = e.nextElement().name
                if (name.startsWith(commandPackagePath)) {
                    if (name.endsWith(".class") && !name.contains("$")) {
                        clazzList.add(name.replace(".class", "").replace("/", "."))
                    }
                }
            }
        } else {
            logger.debug("Run with IDE")
            val loader = Thread.currentThread().contextClassLoader
            val url = loader.getResource(commandPackagePath)
            when {
                url == null -> {
                    logger.error("loader.getResource() is null, return")
                    return emptyList()
                }
                url.protocol != "file" -> {
                    logger.error("url protocol is not 'file', return")
                    return emptyList()
                }
            }

            val file = File(url!!.path)
            val files = file.listFiles()
            files!!.forEach { f ->
                val filename = f.name
                if (filename.endsWith(".class") && !filename.contains("$")) {
                    val className = packageName + "." + filename.replace(".class", "")
                    clazzList.add(className)
                }
            }
        }

        return clazzList
    }

}