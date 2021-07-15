package com.laohu.kit.extensions

import android.graphics.Color

/**
 * @param alpha     0.0 ~ 1.0
 */
fun Int.withAlpha(alpha: Float): Int {
    val red = Color.red(this)
    val green = Color.green(this)
    val blue = Color.blue(this)
    val alpha = (Color.alpha(this) * alpha).toInt()
    return Color.argb(alpha, red, green, blue)
}