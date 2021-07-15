package com.laohu.kit.extensions

import android.content.res.Resources

/**
 * Convenience property for converting to float
 */
val Int.f: Float
    get() = this.toFloat()

/**
 * Convenience property for converting to Long
 */
val Int.L: Long
    get() = this.toLong()

/**
 * Convenience property for getting a dp value as pixels
 */
val Int.dp: Float
    get() = this * Resources.getSystem().displayMetrics.density

/**
 * Convenience property for getting a sp value as pixels
 */
val Int.sp: Float
    get() = this * Resources.getSystem().displayMetrics.scaledDensity

val Double.dp: Float
    get() = (this * Resources.getSystem().displayMetrics.density).toFloat()