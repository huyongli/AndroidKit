package com.laohu.kit.extensions

import android.content.Context
import java.lang.RuntimeException
import java.math.BigDecimal

val <T> T.exhaustive: T
    get() = this

inline fun <reified T> Any.asTo(): T? {
    return this as? T
}

fun Boolean?.orFalse() = this ?: false

fun String?.isNotNullOrEmpty() = !this.isNullOrEmpty()

fun Int?.orZero() = this ?: 0

fun Long?.orZero() = this ?: 0L

fun BigDecimal?.orZero() = this ?: 0.toBigDecimal()

fun Any?.toText(context: Context?): String {
    return when {
        this == null -> ""
        this is Int -> {
            if (context == null) {
                throw RuntimeException("context is null")
            }
            context.getString(this)
        }
        this is String -> this
        else -> throw RuntimeException("Type not supported")
    }
}