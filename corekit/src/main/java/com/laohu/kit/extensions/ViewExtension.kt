package com.laohu.kit.extensions

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

fun View.showSelf() {
    if (this.visibility != View.VISIBLE) {
        this.visibility = View.VISIBLE
    }
}

fun View.showSelfAndParent() {
    var parent = this.parent.asTo<View>()
    while (parent != null) {
        parent.showSelf()
        parent = parent.parent.asTo<View>()
    }
    this.showSelf()
}

fun View.hideSelf() {
    if (this.visibility != View.GONE) {
        this.visibility = View.GONE
    }
}

fun View.hideHolderSelf() {
    if (this.visibility != View.INVISIBLE) {
        this.visibility = View.INVISIBLE
    }
}

fun View.getDimenValue(@DimenRes resId: Int) = this.context.getDimenValue(resId)

fun View.getColorCompat(@ColorRes id: Int) = ContextCompat.getColor(this.context, id)

fun View.getString(@StringRes id: Int) = this.context.getString(id)

fun View.getInt(@IntegerRes id: Int) = this.context.resources.getInteger(id)

fun View.safeParent(level: Int = 1): ViewParent? {
    if (level < 1) return null
    var parent: ViewParent? = this.parent
    var curLevel = 1
    while (curLevel < level) {
        parent = parent?.parent
        curLevel++
    }
    return parent
}

fun View.focus() {
    setFocusable(true)
    setFocusableInTouchMode(true)
    requestFocus()
}

fun View.unFocus() {
    setFocusable(false)
    setFocusableInTouchMode(false)
}

fun View.showKeyBoard() {
    val imm = context
        .applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.hideKeyBoard() {
    val imm = context.applicationContext
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.addListenerForHideKeyBoardWhenScroll() {
    setOnTouchListener(OnHideKeyBoardTouchEventListener(context))
}

private class OnHideKeyBoardTouchEventListener(context: Context) : View.OnTouchListener {
    private var scrollViewTouchY: Float = 0f
    private val touchSlop: Int = android.view.ViewConfiguration.get(context).scaledTouchSlop

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v == null || event == null) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                scrollViewTouchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (kotlin.math.abs(event.y - scrollViewTouchY) > touchSlop) {
                    v.hideKeyBoard()
                }
            }
        }
        return false
    }
}

fun View?.isTouchInBounds(x: Float, y: Float, width: Int? = null, height: Int? = null): Boolean {
    if (this == null) {
        return false
    }
    val location = IntArray(2)
    this.getLocationOnScreen(location);
    val left = location[0]
    val top = location[1]
    val right = left + (width ?: this.measuredWidth)
    val bottom = top + (height ?: this.measuredHeight)
    if (y >= top && y <= bottom && x >= left && x <= right) {
        return true
    }
    return false
}