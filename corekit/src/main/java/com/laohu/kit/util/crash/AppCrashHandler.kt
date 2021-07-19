package com.laohu.kit.util.crash

import android.content.Context

typealias CrashSnapshotExtrasFactory = () -> Map<String, String>?

object AppCrashHandler {
    private var uncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    fun init(context: Context, snapshotExtras: CrashSnapshotExtrasFactory? = null) {
        // get default
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

        // install
        Thread.setDefaultUncaughtExceptionHandler { thread, ex -> // save log
            saveException(context, ex, true, snapshotExtras)
            // uncaught
            uncaughtExceptionHandler?.uncaughtException(thread, ex)
        }
    }

    fun saveException(context: Context, throwable: Throwable?, uncaught: Boolean, snapshotExtras: CrashSnapshotExtrasFactory? = null) {
        CrashSaver.save(context, throwable, uncaught, snapshotExtras?.invoke())
    }

    fun setUncaughtExceptionHandler(handler: Thread.UncaughtExceptionHandler?) {
        handler?.let {
            uncaughtExceptionHandler = it
        }
    }
}