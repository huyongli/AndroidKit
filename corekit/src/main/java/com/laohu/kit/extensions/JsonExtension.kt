package com.laohu.kit.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.laohu.kit.util.handler

val gson = Gson()

fun Any?.toJson(): String {
    if (this == null) return ""
    return try {
        gson.toJson(this, this::class.java)
    } catch (e: Throwable) {
        e.handler()
        ""
    }
}

inline fun <reified T> String?.fromJson(): T? {
    if (this.isNullOrEmpty()) return null
    return try {
        val type = object : TypeToken<T>() {}.type
        gson.fromJson(this, type)
    } catch (e: Throwable) {
        e.handler()
        null
    }
}