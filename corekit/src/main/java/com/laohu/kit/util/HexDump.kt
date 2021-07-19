package com.laohu.kit.util

object HexDump {
    private val m_hexCodes = charArrayOf(
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    )
    private val m_shifts = intArrayOf(
        60, 56, 52, 48, 44, 40, 36, 32, 28,
        24, 20, 16, 12, 8, 4, 0
    )

    private fun toHex(value: Long, digitNum: Int): String {
        val result = StringBuilder(digitNum)
        for (j in 0 until digitNum) {
            val index = (value shr m_shifts[j + (16 - digitNum)] and 15).toInt()
            result.append(m_hexCodes[index])
        }
        return result.toString()
    }

    fun toHex(value: Byte): String {
        return toHex(value.toLong(), 2)
    }

    fun toHex(value: Short): String {
        return toHex(value.toLong(), 4)
    }

    fun toHex(value: Int): String {
        return toHex(value.toLong(), 8)
    }

    fun toHex(value: Long): String {
        return toHex(value, 16)
    }

    @JvmOverloads
    fun toHex(
        value: ByteArray, offset: Int = 0,
        length: Int = value.size
    ): String {
        val retVal = StringBuilder()
        val end = offset + length
        for (x in offset until end) retVal.append(toHex(value[x]))
        return retVal.toString()
    }

    fun restoreBytes(hex: String): ByteArray? {
        val bytes = ByteArray(hex.length / 2)
        for (i in bytes.indices) {
            val c1 = charToNumber(hex[2 * i])
            val c2 = charToNumber(hex[2 * i + 1])
            if (c1 == -1 || c2 == -1) {
                return null
            }
            bytes[i] = ((c1 shl 4) + c2).toByte()
        }
        return bytes
    }

    private fun charToNumber(c: Char): Int {
        return when (c) {
            in '0'..'9' -> {
                c - '0'
            }
            in 'a'..'f' -> {
                c - 'a' + 0xa
            }
            in 'A'..'F' -> {
                c - 'A' + 0xA
            }
            else -> {
                -1
            }
        }
    }
}