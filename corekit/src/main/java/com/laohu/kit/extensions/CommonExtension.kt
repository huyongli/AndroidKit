package com.laohu.kit.extensions

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