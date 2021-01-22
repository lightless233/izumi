package me.lightless.izumi.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime

object ChatMessage : LongIdTable("izumi_chat_message") {
    val qq = long("qq").default(0)
    val nickname = text("nickname").default("")
    val groupId = long("group_id").default(0)
    val message = text("message").default("")

    val createdTime = datetime("created_time").default(DateTime.now())
    val updatedTime = datetime("updated_time").default(DateTime.now())
}

@Suppress("unused")
class ChatMessageDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ChatMessageDAO>(ChatMessage)

    var qq by ChatMessage.qq
    var nickname by ChatMessage.nickname
    var groupId by ChatMessage.groupId
    var message by ChatMessage.message

    var createdTime by ChatMessage.createdTime
    var updatedTime by ChatMessage.updatedTime
}