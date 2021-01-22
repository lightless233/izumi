package me.lightless.izumi.dao

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime

/**
 * 存储每日的龙王信息
 */
object Ryuo : Table("izumi_ryuo") {

    val id = long("id").autoIncrement()
    val qq = long("qq").default(0)
    val nickname = text("nickname").default("")

    val createdTime = datetime("created_time").default(DateTime.now())
    val updatedTime = datetime("updated_time").default(DateTime.now())

    override val primaryKey: PrimaryKey = PrimaryKey(id, name = "pk")
}