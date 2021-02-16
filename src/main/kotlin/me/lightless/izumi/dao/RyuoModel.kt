package me.lightless.izumi.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime

/**
 * 存储每日的龙王信息
 */
object RyuoModel : LongIdTable("izumi_ryuo") {

    val qq = long("qq").default(0)
    val nickname = text("nickname").default("")
    val groupId = long("group_id").default(0)
    val msgCount = long("msg_count").default(0)

    val createdTime = datetime("created_time").default(DateTime.now())
    val updatedTime = datetime("updated_time").default(DateTime.now())
}

@Suppress("unused")
class RyuoDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<RyuoDAO>(RyuoModel)

    var qq by RyuoModel.qq
    var nickname by RyuoModel.nickname
    var groupId by RyuoModel.groupId
    var msgCount by RyuoModel.msgCount

    var createdTime by RyuoModel.createdTime
    var updatedTime by RyuoModel.updatedTime
}
