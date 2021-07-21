package com.laohu.kit.util.log

import android.annotation.SuppressLint
import android.content.Context
import com.laohu.kit.extensions.orFalse
import com.laohu.kit.extensions.orZero
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

data class LogText(
    val clazz: Class<*>,
    val text: String,
    val date: Date
) {
    @SuppressLint("SimpleDateFormat")
    override fun toString(): String {
        val format = SimpleDateFormat("MM-dd HH:mm:ss.SSS")
        val time = format.format(date)
        return "$time: [${clazz.simpleName}] $text"
    }
}

@SuppressLint("SimpleDateFormat")
object LHKitLogWriter {
    private const val LOG_DIR = "AppLog"
    private var enabled: Boolean = true
    private val loggerExecutor: Executor = Executors.newSingleThreadExecutor()
    private val logFileName: String by lazy {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        format.format(Date())
    }
    private lateinit var logFile: File

    val isEnabled: Boolean
        get() = enabled

    fun init(context: Context, enable: Boolean = true, keepLatestLogFileNum: Int = 5) {
        this.enabled = enable
        logFile = File(context.getExternalFilesDir(null), "$LOG_DIR/$logFileName.log")
        deleteUnnecessaryLogFiles(context, keepLatestLogFileNum)
    }

    private fun deleteUnnecessaryLogFiles(context: Context, keepLatestLogFileNum: Int) {
        val dir = File(context.getExternalFilesDir(null), LOG_DIR)
        if (!dir.exists() || !dir.isDirectory) {
            return
        }
        loggerExecutor.execute {
            val fileCount = dir.listFiles()?.size.orZero()
            val endIndex = if (fileCount > (keepLatestLogFileNum - 1))
                fileCount - (keepLatestLogFileNum - 1)
            else 0
            dir.listFiles()?.toList()?.sortedBy { it.name }?.subList(0, endIndex)?.forEach {
                it.delete()
            }
        }
    }

    private fun verifyAndCreateFileIfNeed() {
        val parent = logFile.parentFile
        if (!parent?.exists().orFalse()) {
            parent?.mkdirs()
        }
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
    }

    fun log(clazz: Class<*>, text: String) {
        if (!enabled) {
            return
        }
        writeLog(LogText(clazz, text, Date()))
    }

    private fun writeLog(log: LogText) {
        try {
            loggerExecutor.execute {
                try {
                    verifyAndCreateFileIfNeed()
                    val content = "$log${System.getProperty("line.separator")}"
                    LHLogKit.d(log.toString())
                    val bufferWriter = BufferedWriter(FileWriter(logFile, true))
                    bufferWriter.write(content)
                    bufferWriter.flush()
                    bufferWriter.close()
                } catch (throwable: Throwable) {
                    throwable.handler()
                }
            }
        } catch (throwable: Throwable) {
            throwable.handler()
        }
    }
}