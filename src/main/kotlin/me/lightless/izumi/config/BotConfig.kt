package me.lightless.izumi.config

import kotlin.properties.Delegates

class BotConfig {
    var botQQ by Delegates.notNull<Long>()

    lateinit var botPassword: String

    lateinit var allowedGroups: List<Long>

    lateinit var admins: List<Long>
}