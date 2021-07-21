package com.laohu.kit.util.crash

import android.content.Context
import android.text.TextUtils
import com.laohu.kit.extensions.orFalse
import com.laohu.kit.util.MD5
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.LineNumberReader
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*

object CrashSaver {
    fun save(
        context: Context,
        throwable: Throwable?,
        uncaught: Boolean,
        appendedSnapshot: Map<String, String>?
    ) {
        if (throwable == null) {
            return
        }
        var writer: Writer? = null
        var printWriter: PrintWriter? = null
        var stackTrace = ""
        try {
            writer = StringWriter()
            printWriter = PrintWriter(writer)
            throwable.printStackTrace(printWriter)
            var cause = throwable.cause
            while (cause != null) {
                cause.printStackTrace(printWriter)
                cause = cause.cause
            }
            stackTrace = writer.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                writer?.close()
                printWriter?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val signature = stackTrace.replace("\\([^\\(]*\\)".toRegex(), "")
        val filename = MD5.getStringMD5(signature)
        if (TextUtils.isEmpty(filename)) {
            return
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val timestamp = sdf.format(Date())
        var mBufferedWriter: BufferedWriter? = null
        try {
            val file = File(context.getExternalFilesDir(null), "crash/$filename.log")
            if (!file.parentFile?.exists().orFalse()) { // 如果文件夹不存在，则先创建文件夹
                file.parentFile?.mkdirs()
            }
            var count = 1
            if (file.exists()) {
                var reader: LineNumberReader? = null
                try {
                    reader = LineNumberReader(FileReader(file))
                    val line = reader.readLine()
                    if (line != null && line.startsWith("count")) {
                        var index = line.indexOf(":")
                        if (index != -1) {
                            var countStr = line.substring(++index)
                            countStr = countStr.trim { it <= ' ' }
                            count = countStr.toInt()
                            count++
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (reader != null) {
                        try {
                            reader.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                file.delete()
            }
            file.createNewFile()
            mBufferedWriter = BufferedWriter(FileWriter(file, true)) // 追加模式写文件
            val snapshot: MutableMap<String, String> =
                CrashSnapshot.snapshot(context, uncaught, timestamp, count)
            if (appendedSnapshot != null) {
                snapshot.putAll(appendedSnapshot)
            }
            val content = buildSnapshotString(snapshot, stackTrace)
            mBufferedWriter.append(content)
            mBufferedWriter.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                mBufferedWriter?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun buildSnapshotString(snapshot: Map<String, String>, trace: String): String {
        val iterator = snapshot.entries.iterator()
        val sb = StringBuilder()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            sb.append(entry.key).append(entry.value)
            sb.append(System.getProperty("line.separator"))
        }
        sb.append(System.getProperty("line.separator"))
        sb.append("**----------------------***")
        sb.append(System.getProperty("line.separator"))
        sb.append(trace)
        sb.append(System.getProperty("line.separator"))
        sb.append(System.getProperty("line.separator"))
        sb.append(System.getProperty("line.separator"))
        return sb.toString()
    }
}