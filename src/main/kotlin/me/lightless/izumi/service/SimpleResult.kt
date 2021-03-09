package me.lightless.izumi.service

class SimpleResult<T>(
    val success: Boolean = false,
    val message: String = "",
    val data: T?
)
