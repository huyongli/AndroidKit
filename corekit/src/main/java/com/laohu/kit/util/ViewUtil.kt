package com.laohu.kit.util

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension

fun createShapeDrawable(
    @ColorInt color: Int? = null,
    @ColorInt strokeColor: Int? = null,
    @Dimension strokeWidth: Double? = null,
    @Dimension radius: Double? = null,
    @Dimension topLeftRadius: Double? = null,
    @Dimension topRightRadius: Double? = null,
    @Dimension bottomRightRadius: Double? = null,
    @Dimension bottomLeftRadius: Double? = null
): Drawable {
    assert((strokeColor != null && strokeWidth != null) || (strokeColor == null && strokeWidth == null))

    val drawable = GradientDrawable()
    color?.let {
        drawable.setColor(it)
    }
    strokeColor?.let {
        drawable.setStroke(strokeWidth!!.toInt(), it)
    }
    if (topLeftRadius != null || topRightRadius != null || bottomLeftRadius != null || bottomRightRadius != null) {
        val topLeft = topLeftRadius?.toFloat() ?: 0f
        val topRight = topRightRadius?.toFloat() ?: 0f
        val bottomRight = bottomRightRadius?.toFloat() ?: 0f
        val bottomLeft = bottomLeftRadius?.toFloat() ?: 0f
        drawable.cornerRadii = floatArrayOf(topLeft, topLeft, topRight, topRight, bottomRight, bottomRight, bottomLeft, bottomLeft)
    } else if (radius != null) {
        drawable.cornerRadius = radius.toFloat()
    }
    return drawable
}

fun createStateListDrawable(
    normal: Drawable,
    pressed: Drawable? = null,
    selected: Drawable? = null,
    checked: Drawable? = null,
    enabled: Drawable? = null,
    focused: Drawable? = null
): Drawable {
    val stateDrawable = StateListDrawable()
    pressed?.let {
        stateDrawable.addState(intArrayOf(android.R.attr.state_pressed), it)
    }
    selected?.let {
        stateDrawable.addState(intArrayOf(android.R.attr.state_selected), it)
    }
    checked?.let {
        stateDrawable.addState(intArrayOf(android.R.attr.state_checked), it)
    }
    enabled?.let {
        stateDrawable.addState(intArrayOf(android.R.attr.state_enabled), it)
    }
    focused?.let {
        stateDrawable.addState(intArrayOf(android.R.attr.state_focused), it)
    }
    stateDrawable.addState(intArrayOf(), normal)
    return  stateDrawable
}