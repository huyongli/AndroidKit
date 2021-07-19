package com.laohu.kit.util.crash

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.telephony.TelephonyManager
import android.text.TextUtils
import com.laohu.kit.extensions.asTo
import com.laohu.kit.extensions.versionCode
import com.laohu.kit.extensions.versionName
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

object CrashSnapshot {

    fun snapshot(
        context: Context,
        uncaught: Boolean,
        timestamp: String,
        count: Int
    ): MutableMap<String, String> {
        val info: MutableMap<String, String> = LinkedHashMap()
        info["count: "] = count.toString()
        info["time: "] = timestamp
        info["device: "] = Build.MANUFACTURER + " " + Build.MODEL
        info["android: "] = Build.VERSION.RELEASE
        info["system: "] = Build.DISPLAY
        info["battery: "] = battery(context)
        info["rooted: "] = if (isRooted()) "yes" else "no"
        info["ram: "] = ram(context)
        info["disk: "] = disk()
        info["versionCode: "] = context.versionCode()
        info["versionName: "] = context.versionName()
        info["caught: "] = if (uncaught) "no" else "yes"
        info["network: "] = CrashSnapshot.getNetworkInfo(context)
        return info
    }

    /**
     * 获取手机剩余电量
     * @return
     */
    private fun battery(context: Context): String {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = context.registerReceiver(null, filter)
        val level = intent?.getIntExtra("level", -1) ?: -1
        val scale = intent?.getIntExtra("scale", -1) ?: -1
        return if (scale == -1) {
            "--"
        } else {
            String.format(Locale.US, "%d %%", level * 100 / scale)
        }
    }

    /**
     * 检测手机是否Rooted
     */
    private fun isRooted(): Boolean {
        val tags: Any? = Build.TAGS
        if (tags != null
            && (tags as String).contains("test-keys")
        ) {
            return true
        }
        if (File("/system/app/Superuser.apk").exists()) {
            return true
        }
        return File("/system/xbin/su").exists()
    }

    private fun ram(context: Context): String {
        val total: Long = getTotalMemory()
        val avail: Long = getAvailMemory(context)
        return if (total <= 0) {
            "--"
        } else {
            val ratio = (avail * 100 / total).toFloat()
            String.format(Locale.US, "%.01f%% [%s]", ratio, CrashSnapshot.getSizeWithUnit(total))
        }
    }

    private fun getAvailMemory(context: Context): Long {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return mi.availMem
    }

    private var mTotalMemory: Long = -1

    @Synchronized
    private fun getTotalMemory(): Long {
        if (mTotalMemory == -1L) {
            var total = 0L
            var str: String
            try {
                if (!TextUtils.isEmpty(parseFile(File("/proc/meminfo"), "MemTotal").also { str = it.orEmpty() })) {
                    str = str.uppercase(Locale.US)
                    total = when {
                        str.endsWith("KB") -> {
                            getSize(str, "KB", 1024)
                        }
                        str.endsWith("MB") -> {
                            getSize(str, "MB", 1048576)
                        }
                        str.endsWith("GB") -> {
                            getSize(str, "GB", 1073741824)
                        }
                        else -> {
                            -1
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mTotalMemory = total
        }
        return mTotalMemory
    }

    private fun getSizeWithUnit(size: Long): String? {
        return when {
            size >= 1073741824 -> {
                val i = (size / 1073741824).toFloat()
                String.format(Locale.US, "%.02f GB", i)
            }
            size >= 1048576 -> {
                val i = (size / 1048576).toFloat()
                String.format(Locale.US, "%.02f MB", i)
            }
            else -> {
                val i = (size / 1024).toFloat()
                String.format(Locale.US, "%.02f KB", i)
            }
        }
    }

    private fun parseFile(file: File, filter: String): String? {
        var str: String? = null
        if (file.exists()) {
            var br: BufferedReader? = null
            try {
                br = BufferedReader(FileReader(file), 1024)
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    val pattern = Pattern.compile("\\s*:\\s*")
                    val ret = pattern.split(line.orEmpty(), 2)
                    if (ret.size > 1 && ret[0] == filter) {
                        str = ret[1]
                        break
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                try {
                    br?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return str
    }

    private fun getSize(size: String, unit: String, factor: Int): Long {
        return size.split(unit).toTypedArray()[0].trim { it <= ' ' }.toLong() * factor
    }

    private fun disk(): String {
        val info: LongArray = getSdCardMemory()
        val total = info[0]
        val avail = info[1]
        return if (total <= 0) {
            "--"
        } else {
            val ratio = (avail * 100 / total).toFloat()
            String.format(Locale.US, "%.01f%% [%s]", ratio, getSizeWithUnit(total))
        }
    }

    private fun getSdCardMemory(): LongArray {
        val sdCardInfo = LongArray(2)
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            val sdcardDir = Environment.getExternalStorageDirectory()
            val sf = StatFs(sdcardDir.path)
            if (Build.VERSION.SDK_INT >= 18) {
                val bSize = sf.blockSizeLong
                val bCount = sf.blockCountLong
                val availBlocks = sf.availableBlocksLong
                sdCardInfo[0] = bSize * bCount
                sdCardInfo[1] = bSize * availBlocks
            } else {
                val bSize = sf.blockSize.toLong()
                val bCount = sf.blockCount.toLong()
                val availBlocks = sf.availableBlocks.toLong()
                sdCardInfo[0] = bSize * bCount
                sdCardInfo[1] = bSize * availBlocks
            }
        }
        return sdCardInfo
    }

    fun getNetworkInfo(context: Context): String {
        var info = ""
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE).asTo<ConnectivityManager>() ?: return info
        val activeNetInfo = connectivity.activeNetworkInfo
        if (activeNetInfo != null) {
            info = if (activeNetInfo.type == ConnectivityManager.TYPE_WIFI) {
                activeNetInfo.typeName
            } else {
                val sb = StringBuilder()
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE).asTo<TelephonyManager>()
                sb.append(activeNetInfo.typeName)
                sb.append(" [")
                if (tm != null) {
                    // Result may be unreliable on CDMA networks
                    sb.append(tm.networkOperatorName)
                    sb.append("#")
                }
                sb.append(activeNetInfo.subtypeName)
                sb.append("]")
                sb.toString()
            }
        }
        return info
    }
}