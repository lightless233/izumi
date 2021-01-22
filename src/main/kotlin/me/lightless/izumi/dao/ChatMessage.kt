package me.lightless.izumi.dao

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime

object ChatMessage:Table("izumi_chat_message") {

    val id = long("id").autoIncrement()
    val qq = long("qq").default(0)
    val nickname = text("nickname").default("")
    val group_id = long("group_id").default(0)
    val message = text("message").default("")

    val createdTime = datetime("created_time").default(DateTime.now())
    val updatedTime = datetime("updated_time").default(DateTime.now())

    override val primaryKey: PrimaryKey = PrimaryKey(id, name = "pk")
}