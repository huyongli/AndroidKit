package com.laohu.kit.extensions

import android.app.Activity
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageInfo
import android.graphics.Point
import android.net.ConnectivityManager
import android.os.Build
import android.os.Process
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import java.io.File


fun Context.getDrawableCompat(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)!!

fun Context.getColorCompat(@ColorRes id: Int) = ContextCompat.getColor(this, id)

fun Context.getDimenValue(@DimenRes resId: Int) = this.resources.getDimensionPixelSize(resId)

fun Context.getRealScreenWidth(): Int {
    val wm = applicationContext?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        wm.defaultDisplay.getRealSize(point)
    } else {
        wm.defaultDisplay.getSize(point)
    }
    return point.x
}

fun Context.getRealScreenHeight(): Int {
    val wm = applicationContext?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        wm.defaultDisplay.getRealSize(point)
    } else {
        wm.defaultDisplay.getSize(point)
    }
    return point.y
}

/**
 * 屏幕可用高度
 */
fun Context.getScreenHeight(): Int {
    val wm = applicationContext?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val dm = DisplayMetrics()
    wm.defaultDisplay.getMetrics(dm)
    return dm.heightPixels
}

/**
 * 屏幕可用宽度
 */
fun Context.getScreenWidth(): Int {
    val wm = applicationContext?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val dm = DisplayMetrics()
    wm.defaultDisplay.getMetrics(dm)
    return dm.widthPixels
}

fun Context.versionName(): String {
    val packageManager = this.packageManager
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    val versionName = packageInfo.versionName
    return if (versionName.startsWith("v", true)) {
        versionName.substring(1)
    } else {
        versionName
    }
}

fun Context.getActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> (this.baseContext as ContextWrapper).getActivity()
        else -> null
    }
}

fun Context.hideSoftKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

val Context.imageCacheDir: File
    get() = externalMediaDirs.singleOrNull() ?: externalCacheDir ?: cacheDir

fun Context.copyToClipboard(value: String) {
    val cm: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("", value)
    cm.setPrimaryClip(clipData)
}

fun Context.getAppProcessName(): String {
    var currentProcessName = ""
    val pid = Process.myPid()
    val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    if (manager?.runningAppProcesses != null) {
        for (processInfo in manager.runningAppProcesses) {
            if (processInfo.pid == pid) {
                currentProcessName = processInfo.processName
                break
            }
        }
    }

    return currentProcessName
}

fun Context.isNetworkConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE).asTo<ConnectivityManager>()
    val activeNetwork = connectivityManager?.activeNetworkInfo
    return activeNetwork?.isConnected.orFalse()
}

fun Context.isAppsExist(packageName: String): Boolean {
    var packageInfo: PackageInfo? = null
    try {
        packageInfo = packageManager.getPackageInfo(packageName, 0)
    } catch (t: Throwable) {
    }
    return packageInfo != null
}

fun Context?.getClipboardText(): String? {
    val  clipboardManager = this?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    return if (clipboardManager?.hasPrimaryClip().orFalse() && clipboardManager?.primaryClip?.itemCount.orZero() > 0) {
        clipboardManager?.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()
    } else {
        null
    }
}

fun Context?.clearClipboard() {
    val clipboardManager = this?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    try {
        clipboardManager?.setPrimaryClip(ClipData.newPlainText(null, ""))
    } catch (throwable: Throwable) {

    }
}