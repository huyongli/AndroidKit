package com.laohu.kit.util.log

import android.util.Log
import kotlinx.coroutines.CancellationException

fun Throwable.handler() {
    if (this is CancellationException) {
        return
    }
    if (LHLogKit.isEnabled) {
        Log.e(TAG, message, this)
    }
}

typealias LogCallback = (LogLevel, String) -> Unit

enum class LogLevel {
    INFO,
    ERROR,
    DEBUG
}

private const val TAG = "LHKitLog"
object LHLogKit {
    private var enabled = true
    private var tag = TAG
    private var logCallback: LogCallback? = null

    val isEnabled: Boolean
        get() = enabled

    fun init(enable: Boolean = true, tag: String = TAG, callback: LogCallback? = null) {
        this.enabled = enable
        this.tag = tag
        this.logCallback = callback
    }

    fun i(message: String) {
        if (enabled) {
            Log.i(tag, message)
            logCallback?.invoke(LogLevel.INFO, message)
        }
    }

    fun d(message: String) {
        if (enabled) {
            Log.d(tag, message)
            logCallback?.invoke(LogLevel.DEBUG, message)
        }
    }

    fun e(message: String) {
        if (enabled) {
            Log.e(tag, message)
            logCallback?.invoke(LogLevel.ERROR, message)
        }
    }
}