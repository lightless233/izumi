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
object Ryuo : LongIdTable("izumi_ryuo") {

    val qq = long("qq").default(0)
    val nickname = text("nickname").default("")
    val groupId = long("group_id").default(0)
    val msgCount = long("msg_count").default(0)

    val createdTime = datetime("created_time").default(DateTime.now())
    val updatedTime = datetime("updated_time").default(DateTime.now())

    override val primaryKey: PrimaryKey = PrimaryKey(id, name = "pk")
}

@Suppress("unused")
class RyuoDAO(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<RyuoDAO>(Ryuo)

    var qq by Ryuo.qq
    var nickname by Ryuo.nickname
    var groupId by Ryuo.groupId
    var msgCount by Ryuo.msgCount

    var createdTime by Ryuo.createdTime
    var updatedTime by Ryuo.updatedTime
}
