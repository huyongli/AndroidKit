package com.laohu.kit.util

import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.UnsupportedEncodingException
import java.security.MessageDigest

object MD5 {
    fun getStringMD5(value: String?): String? {
        return if (value == null || value.trim { it <= ' ' }.isEmpty()) {
            null
        } else try {
            getMD5(value.toByteArray(charset("UTF-8")))
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e.message, e)
        }
    }

    fun getMD5(source: ByteArray?): String {
        return try {
            val md5 = MessageDigest.getInstance("MD5")
            HexDump.toHex(md5.digest(source ?: ByteArray(0)))
        } catch (e: Exception) {
            throw RuntimeException(e.message, e)
        }
    }

    fun getStreamMD5(filePath: String?): String? {
        var hash: String? = null
        val buffer = ByteArray(4096)
        var bufferedInputStream: BufferedInputStream? = null
        try {
            val md5 = MessageDigest.getInstance("MD5")
            bufferedInputStream = BufferedInputStream(FileInputStream(filePath))
            var numRead = 0
            while (bufferedInputStream.read(buffer).also { numRead = it } > 0) {
                md5.update(buffer, 0, numRead)
            }
            bufferedInputStream.close()
            hash = HexDump.toHex(md5.digest())
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                bufferedInputStream?.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return hash
    }
}