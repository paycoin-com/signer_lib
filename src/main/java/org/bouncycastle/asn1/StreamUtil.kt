package org.bouncycastle.asn1

import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel

internal object StreamUtil {
    private val MAX_MEMORY = Runtime.getRuntime().maxMemory()

    /**
     * Find out possible longest length...

     * @param in input stream of interest
     * *
     * @return length calculation or MAX_VALUE.
     */
    fun findLimit(`in`: InputStream): Int {
        if (`in` is LimitedInputStream) {
            return `in`.remaining
        } else if (`in` is ASN1InputStream) {
            return `in`.limit
        } else if (`in` is ByteArrayInputStream) {
            return `in`.available()
        } else if (`in` is FileInputStream) {
            try {
                val channel = `in`.channel
                val size = if (channel != null) channel.size() else Integer.MAX_VALUE

                if (size < Integer.MAX_VALUE) {
                    return size.toInt()
                }
            } catch (e: IOException) {
                // ignore - they'll find out soon enough!
            }

        }

        if (MAX_MEMORY > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE
        }

        return MAX_MEMORY.toInt()
    }

    fun calculateBodyLength(
            length: Int): Int {
        var count = 1

        if (length > 127) {
            var size = 1
            var `val` = length

            while ((`val` = `val` ushr 8) != 0) {
                size++
            }

            var i = (size - 1) * 8
            while (i >= 0) {
                count++
                i -= 8
            }
        }

        return count
    }

    @Throws(IOException::class)
    fun calculateTagLength(tagNo: Int): Int {
        var tagNo = tagNo
        var length = 1

        if (tagNo >= 31) {
            if (tagNo < 128) {
                length++
            } else {
                val stack = ByteArray(5)
                var pos = stack.size

                stack[--pos] = (tagNo and 0x7F).toByte()

                do {
                    tagNo = tagNo shr 7
                    stack[--pos] = (tagNo and 0x7F or 0x80).toByte()
                } while (tagNo > 127)

                length += stack.size - pos
            }
        }

        return length
    }
}
