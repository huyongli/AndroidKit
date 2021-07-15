package com.laohu.kit.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.laohu.kit.R

private val TAG_KEY_HAVE_SET_OFFSET = R.id.offset_tag

fun Activity?.resetStatusBar() {
    this?.window?.apply {
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        decorView.systemUiVisibility = 0
    }
}

fun Activity?.updateStatusBarColor(@ColorInt color: Int) {
    this?.window?.updateStatusBarColor(color)
}

private fun Window.updateStatusBarColor(@ColorInt color: Int) {
    this.takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP }?.apply {
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        statusBarColor = color

        val isColorDark = ColorUtils.calculateLuminance(color) < 0.5
        if (isColorDark) setStatusBarDarkMode() else setStatusBarLightMode()
    }
}

fun Window.setStatusBarLightMode() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val originSystemUiVisibility = this.decorView.systemUiVisibility
        this.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or originSystemUiVisibility
    }
}

fun Window.setStatusBarDarkMode() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val originSystemUiVisibility = this.decorView.systemUiVisibility
        this.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() and originSystemUiVisibility
    }
}

@SuppressLint("ObsoleteSdkInt")
fun Activity?.layoutBelowStatusBar(vararg needOffsetView: View, fitsSystemWindow: Boolean = false, @ColorInt statusBarColor: Int? = null) {
    if (this == null) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return

    this.window.layoutBelowStatusBar()
    this.window.updateFitsSystemWindow(fitsSystemWindow)

    needOffsetView.forEach { this.layoutViewOffSetStatusBar(it) }
    updateStatusBarModeByColor(statusBarColor)
}

fun Activity?.layoutViewOffSetStatusBar(vararg needOffsetView: View) {
    if (this == null) return
    val statusBarHeight = this.getStatusBarHeight()
    needOffsetView.forEach {
        val haveSetOffset = it.getTag(TAG_KEY_HAVE_SET_OFFSET)
        if (haveSetOffset != null && haveSetOffset as Boolean) {
            return
        }
        val layoutParams = it.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(
            layoutParams.leftMargin, layoutParams.topMargin + statusBarHeight,
            layoutParams.rightMargin, layoutParams.bottomMargin
        )
        it.setTag(TAG_KEY_HAVE_SET_OFFSET, true)
    }
}

fun Activity.updateStatusBarModeByColor(@ColorInt statusBarColor: Int?) {
    statusBarColor?.let {
        val isColorDark = ColorUtils.calculateLuminance(it) < 0.5
        if (isColorDark) window.setStatusBarDarkMode() else window.setStatusBarLightMode()
    }
}

@SuppressLint("ObsoleteSdkInt")
private fun Window.layoutBelowStatusBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val originSystemUiVisibility = this.decorView.systemUiVisibility
        this.decorView.systemUiVisibility =
            originSystemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        this.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        )
    }
    statusBarColor = Color.TRANSPARENT
}

private fun Window.updateFitsSystemWindow(fitsSystemWindow: Boolean) {
    val parent = this.decorView.findViewById<View>(android.R.id.content) as ViewGroup
    var i = 0
    val count = parent.childCount
    while (i < count) {
        val childView = parent.getChildAt(i)
        childView.asTo<ViewGroup>()?.run {
            fitsSystemWindows = fitsSystemWindow
            clipToPadding = fitsSystemWindow
        }
        i++
    }
}

fun Context.getStatusBarHeight(): Int {
    val resourceId = this.resources.getIdentifier("status_bar_height", "dimen", "android")
    return this.resources.getDimensionPixelSize(resourceId)
}

fun Context.getNavigationBarHeight(): Int {
    val resourceId = this.resources.getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceId > 0) {
        return this.resources.getDimensionPixelSize(resourceId)
    }
    return 0
}