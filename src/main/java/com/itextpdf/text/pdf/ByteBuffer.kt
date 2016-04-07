/*
 * $Id: 0e30bceaec800dd928fff8e7de5a4a96fa91c334 $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Bruno Lowagie, Paulo Soares, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.text.pdf

import java.io.IOException
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import com.itextpdf.text.error_messages.MessageLocalization

import com.itextpdf.text.DocWriter

/**
 * Acts like a StringBuffer but works with byte arrays.
 * Floating point is converted to a format suitable to the PDF.
 * @author Paulo Soares
 */

class ByteBuffer
/**
 * Creates a byte buffer with a certain capacity.
 * @param size the initial capacity
 */
@JvmOverloads constructor(size: Int = 128) : OutputStream() {
    /** The count of bytes in the buffer.  */
    protected var count: Int = 0

    /** The buffer where the bytes are stored.  */
    var buffer: ByteArray
        protected set

    init {
        var size = size
        if (size < 1)
            size = 128
        buffer = ByteArray(size)
    }

    /**
     * Appends an int. The size of the array will grow by one.
     * @param b the int to be appended
     * *
     * @return a reference to this ByteBuffer object
     */
    fun append_i(b: Int): ByteBuffer {
        val newcount = count + 1
        if (newcount > buffer.size) {
            val newbuf = ByteArray(Math.max(buffer.size shl 1, newcount))
            System.arraycopy(buffer, 0, newbuf, 0, count)
            buffer = newbuf
        }
        buffer[count] = b.toByte()
        count = newcount
        return this
    }

    /**
     * Appends the subarray of the byte array. The buffer will grow by
     * len bytes.
     * @param b the array to be appended
     * *
     * @param off the offset to the start of the array
     * *
     * @param len the length of bytes to append
     * *
     * @return a reference to this ByteBuffer object
     */
    @JvmOverloads fun append(b: ByteArray, off: Int = 0, len: Int = b.size): ByteBuffer {
        if (off < 0 || off > b.size || len < 0 ||
                off + len > b.size || off + len < 0 || len == 0)
            return this
        val newcount = count + len
        if (newcount > buffer.size) {
            val newbuf = ByteArray(Math.max(buffer.size shl 1, newcount))
            System.arraycopy(buffer, 0, newbuf, 0, count)
            buffer = newbuf
        }
        System.arraycopy(b, off, buffer, count, len)
        count = newcount
        return this
    }

    /**
     * Appends a String to the buffer. The String is
     * converted according to the encoding ISO-8859-1.
     * @param str the String to be appended
     * *
     * @return a reference to this ByteBuffer object
     */
    fun append(str: String?): ByteBuffer {
        if (str != null)
            return append(DocWriter.getISOBytes(str))
        return this
    }

    /**
     * Appends a char to the buffer. The char is
     * converted according to the encoding ISO-8859-1.
     * @param c the char to be appended
     * *
     * @return a reference to this ByteBuffer object
     */
    fun append(c: Char): ByteBuffer {
        return append_i(c.toInt())
    }

    /**
     * Appends another ByteBuffer to this buffer.
     * @param buf the ByteBuffer to be appended
     * *
     * @return a reference to this ByteBuffer object
     */
    fun append(buf: ByteBuffer): ByteBuffer {
        return append(buf.buffer, 0, buf.count)
    }

    /**
     * Appends the string representation of an int.
     * @param i the int to be appended
     * *
     * @return a reference to this ByteBuffer object
     */
    fun append(i: Int): ByteBuffer {
        return append(i.toDouble())
    }

    /**
     * Appends the string representation of a long.
     * @param i the long to be appended
     * *
     * @return a reference to this ByteBuffer object
     */
    fun append(i: Long): ByteBuffer {
        return append(java.lang.Long.toString(i))
    }

    fun append(b: Byte): ByteBuffer {
        return append_i(b.toInt())
    }

    fun appendHex(b: Byte): ByteBuffer {
        append(bytes[b shr 4 and 0x0f])
        return append(bytes[b and 0x0f])
    }

    /**
     * Appends a string representation of a float according
     * to the Pdf conventions.
     * @param i the float to be appended
     * *
     * @return a reference to this ByteBuffer object
     */
    fun append(i: Float): ByteBuffer {
        return append(i.toDouble())
    }

    /**
     * Appends a string representation of a double according
     * to the Pdf conventions.
     * @param d the double to be appended
     * *
     * @return a reference to this ByteBuffer object
     */
    fun append(d: Double): ByteBuffer {
        append(formatDouble(d, this))
        return this
    }

    /**
     * Sets the size to zero.
     */
    fun reset() {
        count = 0
    }

    /**
     * Creates a newly allocated byte array. Its size is the current
     * size of this output stream and the valid contents of the buffer
     * have been copied into it.

     * @return  the current contents of this output stream, as a byte array.
     */
    fun toByteArray(): ByteArray {
        val newbuf = ByteArray(count)
        System.arraycopy(buffer, 0, newbuf, 0, count)
        return newbuf
    }

    /**
     * Returns the current size of the buffer.

     * @return the value of the `count` field, which is the number of valid bytes in this byte buffer.
     */
    fun size(): Int {
        return count
    }

    fun setSize(size: Int) {
        if (size > count || size < 0)
            throw IndexOutOfBoundsException(MessageLocalization.getComposedMessage("the.new.size.must.be.positive.and.lt.eq.of.the.current.size"))
        count = size
    }

    /**
     * Converts the buffer's contents into a string, translating bytes into
     * characters according to the platform's default character encoding.

     * @return String translated from the buffer's contents.
     */
    override fun toString(): String {
        return String(buffer, 0, count)
    }

    /**
     * Converts the buffer's contents into a string, translating bytes into
     * characters according to the specified character encoding.

     * @param   enc  a character-encoding name.
     * *
     * @return String translated from the buffer's contents.
     * *
     * @throws UnsupportedEncodingException
     * *         If the named encoding is not supported.
     */
    @Throws(UnsupportedEncodingException::class)
    fun toString(enc: String): String {
        return String(buffer, 0, count, enc)
    }

    /**
     * Writes the complete contents of this byte buffer output to
     * the specified output stream argument, as if by calling the output
     * stream's write method using `out.write(buf, 0, count)`.

     * @param      out   the output stream to which to write the data.
     * *
     * @exception  IOException  if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun writeTo(out: OutputStream) {
        out.write(buffer, 0, count)
    }

    @Throws(IOException::class)
    override fun write(b: Int) {
        append(b.toByte())
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        append(b, off, len)
    }

    companion object {

        private var byteCacheSize = 0

        private var byteCache = arrayOfNulls<ByteArray>(byteCacheSize)
        val ZERO = '0'.toByte()
        private val chars = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        private val bytes = byteArrayOf(48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102)
        /**
         * If true always output floating point numbers with 6 decimal digits.
         * If false uses the faster, although less precise, representation.
         */
        var HIGH_PRECISION = false
        private val dfs = DecimalFormatSymbols(Locale.US)

        /**
         * Sets the cache size.
         *
         * This can only be used to increment the size.
         * If the size that is passed through is smaller than the current size, nothing happens.

         * @param   size    the size of the cache
         */

        fun setCacheSize(size: Int) {
            var size = size
            if (size > 3276700) size = 3276700
            if (size <= byteCacheSize) return
            val tmpCache = arrayOfNulls<ByteArray>(size)
            System.arraycopy(byteCache, 0, tmpCache, 0, byteCacheSize)
            byteCache = tmpCache
            byteCacheSize = size
        }

        /**
         * You can fill the cache in advance if you want to.

         * @param   decimals
         */

        fun fillCache(decimals: Int) {
            var step = 1
            when (decimals) {
                0 -> step = 100
                1 -> step = 10
            }
            var i = 1
            while (i < byteCacheSize) {
                if (byteCache[i] != null) {
                    i += step
                    continue
                }
                byteCache[i] = convertToBytes(i)
                i += step
            }
        }

        /**
         * Converts an double (multiplied by 100 and cast to an int) into an array of bytes.

         * @param   i   the int
         * *
         * @return  a byte array
         */

        private fun convertToBytes(i: Int): ByteArray {
            var size = Math.floor(Math.log(i.toDouble()) / Math.log(10.0)).toInt()
            if (i % 100 != 0) {
                size += 2
            }
            if (i % 10 != 0) {
                size++
            }
            if (i < 100) {
                size++
                if (i < 10) {
                    size++
                }
            }
            size--
            val cache = ByteArray(size)
            size--
            if (i < 100) {
                cache[0] = '0'.toByte()
            }
            if (i % 10 != 0) {
                cache[size--] = bytes[i % 10]
            }
            if (i % 100 != 0) {
                cache[size--] = bytes[i / 10 % 10]
                cache[size--] = '.'.toByte()
            }
            size = Math.floor(Math.log(i.toDouble()) / Math.log(10.0)).toInt() - 1
            var add = 0
            while (add < size) {
                cache[add] = bytes[i / Math.pow(10.0, (size - add + 1).toDouble()).toInt() % 10]
                add++
            }
            return cache
        }

        /**
         * Outputs a double into a format suitable for the PDF.
         * @param d a double
         * *
         * @param buf a ByteBuffer
         * *
         * @return the String representation of the double if
         * * buf is null. If buf is not null,
         * * then the double is appended directly to the buffer and this methods returns null.
         */
        @JvmOverloads fun formatDouble(d: Double, buf: ByteBuffer? = null): String? {
            var d = d
            if (HIGH_PRECISION) {
                val dn = DecimalFormat("0.######", dfs)
                val sform = dn.format(d)
                if (buf == null)
                    return sform
                else {
                    buf.append(sform)
                    return null
                }
            }
            var negative = false
            if (Math.abs(d) < 0.000015) {
                if (buf != null) {
                    buf.append(ZERO)
                    return null
                } else {
                    return "0"
                }
            }
            if (d < 0) {
                negative = true
                d = -d
            }
            if (d < 1.0) {
                d += 0.000005
                if (d >= 1) {
                    if (negative) {
                        if (buf != null) {
                            buf.append('-'.toByte())
                            buf.append('1'.toByte())
                            return null
                        } else {
                            return "-1"
                        }
                    } else {
                        if (buf != null) {
                            buf.append('1'.toByte())
                            return null
                        } else {
                            return "1"
                        }
                    }
                }
                if (buf != null) {
                    val v = (d * 100000).toInt()

                    if (negative) buf.append('-'.toByte())
                    buf.append('0'.toByte())
                    buf.append('.'.toByte())

                    buf.append((v / 10000 + ZERO).toByte())
                    if (v % 10000 != 0) {
                        buf.append((v / 1000 % 10 + ZERO).toByte())
                        if (v % 1000 != 0) {
                            buf.append((v / 100 % 10 + ZERO).toByte())
                            if (v % 100 != 0) {
                                buf.append((v / 10 % 10 + ZERO).toByte())
                                if (v % 10 != 0) {
                                    buf.append((v % 10 + ZERO).toByte())
                                }
                            }
                        }
                    }
                    return null
                } else {
                    var x = 100000
                    val v = (d * x).toInt()

                    val res = StringBuilder()
                    if (negative) res.append('-')
                    res.append("0.")

                    while (v < x / 10) {
                        res.append('0')
                        x /= 10
                    }
                    res.append(v)
                    var cut = res.length - 1
                    while (res[cut] == '0') {
                        --cut
                    }
                    res.setLength(cut + 1)
                    return res.toString()
                }
            } else if (d <= 32767) {
                d += 0.005
                val v = (d * 100).toInt()

                if (v < byteCacheSize && byteCache[v] != null) {
                    if (buf != null) {
                        if (negative) buf.append('-'.toByte())
                        buf.append(byteCache[v])
                        return null
                    } else {
                        var tmp = PdfEncodings.convertToString(byteCache[v], null)
                        if (negative) tmp = "-" + tmp
                        return tmp
                    }
                }
                if (buf != null) {
                    if (v < byteCacheSize) {
                        //create the cachebyte[]
                        val cache: ByteArray
                        var size = 0
                        if (v >= 1000000) {
                            //the original number is >=10000, we need 5 more bytes
                            size += 5
                        } else if (v >= 100000) {
                            //the original number is >=1000, we need 4 more bytes
                            size += 4
                        } else if (v >= 10000) {
                            //the original number is >=100, we need 3 more bytes
                            size += 3
                        } else if (v >= 1000) {
                            //the original number is >=10, we need 2 more bytes
                            size += 2
                        } else if (v >= 100) {
                            //the original number is >=1, we need 1 more bytes
                            size += 1
                        }

                        //now we must check if we have a decimal number
                        if (v % 100 != 0) {
                            //yes, do not forget the "."
                            size += 2
                        }
                        if (v % 10 != 0) {
                            size++
                        }
                        cache = ByteArray(size)
                        var add = 0
                        if (v >= 1000000) {
                            cache[add++] = bytes[v / 1000000]
                        }
                        if (v >= 100000) {
                            cache[add++] = bytes[v / 100000 % 10]
                        }
                        if (v >= 10000) {
                            cache[add++] = bytes[v / 10000 % 10]
                        }
                        if (v >= 1000) {
                            cache[add++] = bytes[v / 1000 % 10]
                        }
                        if (v >= 100) {
                            cache[add++] = bytes[v / 100 % 10]
                        }

                        if (v % 100 != 0) {
                            cache[add++] = '.'.toByte()
                            cache[add++] = bytes[v / 10 % 10]
                            if (v % 10 != 0) {
                                cache[add++] = bytes[v % 10]
                            }
                        }
                        byteCache[v] = cache
                    }

                    if (negative) buf.append('-'.toByte())
                    if (v >= 1000000) {
                        buf.append(bytes[v / 1000000])
                    }
                    if (v >= 100000) {
                        buf.append(bytes[v / 100000 % 10])
                    }
                    if (v >= 10000) {
                        buf.append(bytes[v / 10000 % 10])
                    }
                    if (v >= 1000) {
                        buf.append(bytes[v / 1000 % 10])
                    }
                    if (v >= 100) {
                        buf.append(bytes[v / 100 % 10])
                    }

                    if (v % 100 != 0) {
                        buf.append('.'.toByte())
                        buf.append(bytes[v / 10 % 10])
                        if (v % 10 != 0) {
                            buf.append(bytes[v % 10])
                        }
                    }
                    return null
                } else {
                    val res = StringBuilder()
                    if (negative) res.append('-')
                    if (v >= 1000000) {
                        res.append(chars[v / 1000000])
                    }
                    if (v >= 100000) {
                        res.append(chars[v / 100000 % 10])
                    }
                    if (v >= 10000) {
                        res.append(chars[v / 10000 % 10])
                    }
                    if (v >= 1000) {
                        res.append(chars[v / 1000 % 10])
                    }
                    if (v >= 100) {
                        res.append(chars[v / 100 % 10])
                    }

                    if (v % 100 != 0) {
                        res.append('.')
                        res.append(chars[v / 10 % 10])
                        if (v % 10 != 0) {
                            res.append(chars[v % 10])
                        }
                    }
                    return res.toString()
                }
            } else {
                d += 0.5
                val v = d.toLong()
                if (negative)
                    return "-" + java.lang.Long.toString(v)
                else
                    return java.lang.Long.toString(v)
            }
        }
    }
}
/** Creates new ByteBuffer with capacity 128  */
/**
 * Appends an array of bytes.
 * @param b the array to be appended
 * *
 * @return a reference to this ByteBuffer object
 */
/**
 * Outputs a double into a format suitable for the PDF.
 * @param d a double
 * *
 * @return the String representation of the double
 */
