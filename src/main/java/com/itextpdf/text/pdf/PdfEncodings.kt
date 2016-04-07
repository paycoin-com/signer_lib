/*
 * $Id: 88f575c256bf7bb6d18ee0de6851694c506539e4 $
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
import java.io.UnsupportedEncodingException
import java.util.HashMap

import com.itextpdf.text.ExceptionConverter
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetEncoder
import java.nio.charset.CodingErrorAction

/** Supports fast encodings for winansi and PDFDocEncoding.
 * Supports conversions from CJK encodings to CID.
 * Supports custom encodings.
 * @author Paulo Soares
 */
object PdfEncodings {
    internal val winansiByteToChar = charArrayOf(0.toChar(), 1.toChar(), 2.toChar(), 3.toChar(), 4.toChar(), 5.toChar(), 6.toChar(), 7.toChar(), 8.toChar(), 9.toChar(), 10.toChar(), 11.toChar(), 12.toChar(), 13.toChar(), 14.toChar(), 15.toChar(), 16.toChar(), 17.toChar(), 18.toChar(), 19.toChar(), 20.toChar(), 21.toChar(), 22.toChar(), 23.toChar(), 24.toChar(), 25.toChar(), 26.toChar(), 27.toChar(), 28.toChar(), 29.toChar(), 30.toChar(), 31.toChar(), 32.toChar(), 33.toChar(), 34.toChar(), 35.toChar(), 36.toChar(), 37.toChar(), 38.toChar(), 39.toChar(), 40.toChar(), 41.toChar(), 42.toChar(), 43.toChar(), 44.toChar(), 45.toChar(), 46.toChar(), 47.toChar(), 48.toChar(), 49.toChar(), 50.toChar(), 51.toChar(), 52.toChar(), 53.toChar(), 54.toChar(), 55.toChar(), 56.toChar(), 57.toChar(), 58.toChar(), 59.toChar(), 60.toChar(), 61.toChar(), 62.toChar(), 63.toChar(), 64.toChar(), 65.toChar(), 66.toChar(), 67.toChar(), 68.toChar(), 69.toChar(), 70.toChar(), 71.toChar(), 72.toChar(), 73.toChar(), 74.toChar(), 75.toChar(), 76.toChar(), 77.toChar(), 78.toChar(), 79.toChar(), 80.toChar(), 81.toChar(), 82.toChar(), 83.toChar(), 84.toChar(), 85.toChar(), 86.toChar(), 87.toChar(), 88.toChar(), 89.toChar(), 90.toChar(), 91.toChar(), 92.toChar(), 93.toChar(), 94.toChar(), 95.toChar(), 96.toChar(), 97.toChar(), 98.toChar(), 99.toChar(), 100.toChar(), 101.toChar(), 102.toChar(), 103.toChar(), 104.toChar(), 105.toChar(), 106.toChar(), 107.toChar(), 108.toChar(), 109.toChar(), 110.toChar(), 111.toChar(), 112.toChar(), 113.toChar(), 114.toChar(), 115.toChar(), 116.toChar(), 117.toChar(), 118.toChar(), 119.toChar(), 120.toChar(), 121.toChar(), 122.toChar(), 123.toChar(), 124.toChar(), 125.toChar(), 126.toChar(), 127.toChar(), 8364.toChar(), 65533.toChar(), 8218.toChar(), 402.toChar(), 8222.toChar(), 8230.toChar(), 8224.toChar(), 8225.toChar(), 710.toChar(), 8240.toChar(), 352.toChar(), 8249.toChar(), 338.toChar(), 65533.toChar(), 381.toChar(), 65533.toChar(), 65533.toChar(), 8216.toChar(), 8217.toChar(), 8220.toChar(), 8221.toChar(), 8226.toChar(), 8211.toChar(), 8212.toChar(), 732.toChar(), 8482.toChar(), 353.toChar(), 8250.toChar(), 339.toChar(), 65533.toChar(), 382.toChar(), 376.toChar(), 160.toChar(), 161.toChar(), 162.toChar(), 163.toChar(), 164.toChar(), 165.toChar(), 166.toChar(), 167.toChar(), 168.toChar(), 169.toChar(), 170.toChar(), 171.toChar(), 172.toChar(), 173.toChar(), 174.toChar(), 175.toChar(), 176.toChar(), 177.toChar(), 178.toChar(), 179.toChar(), 180.toChar(), 181.toChar(), 182.toChar(), 183.toChar(), 184.toChar(), 185.toChar(), 186.toChar(), 187.toChar(), 188.toChar(), 189.toChar(), 190.toChar(), 191.toChar(), 192.toChar(), 193.toChar(), 194.toChar(), 195.toChar(), 196.toChar(), 197.toChar(), 198.toChar(), 199.toChar(), 200.toChar(), 201.toChar(), 202.toChar(), 203.toChar(), 204.toChar(), 205.toChar(), 206.toChar(), 207.toChar(), 208.toChar(), 209.toChar(), 210.toChar(), 211.toChar(), 212.toChar(), 213.toChar(), 214.toChar(), 215.toChar(), 216.toChar(), 217.toChar(), 218.toChar(), 219.toChar(), 220.toChar(), 221.toChar(), 222.toChar(), 223.toChar(), 224.toChar(), 225.toChar(), 226.toChar(), 227.toChar(), 228.toChar(), 229.toChar(), 230.toChar(), 231.toChar(), 232.toChar(), 233.toChar(), 234.toChar(), 235.toChar(), 236.toChar(), 237.toChar(), 238.toChar(), 239.toChar(), 240.toChar(), 241.toChar(), 242.toChar(), 243.toChar(), 244.toChar(), 245.toChar(), 246.toChar(), 247.toChar(), 248.toChar(), 249.toChar(), 250.toChar(), 251.toChar(), 252.toChar(), 253.toChar(), 254.toChar(), 255.toChar())

    internal val pdfEncodingByteToChar = charArrayOf(0.toChar(), 1.toChar(), 2.toChar(), 3.toChar(), 4.toChar(), 5.toChar(), 6.toChar(), 7.toChar(), 8.toChar(), 9.toChar(), 10.toChar(), 11.toChar(), 12.toChar(), 13.toChar(), 14.toChar(), 15.toChar(), 16.toChar(), 17.toChar(), 18.toChar(), 19.toChar(), 20.toChar(), 21.toChar(), 22.toChar(), 23.toChar(), 24.toChar(), 25.toChar(), 26.toChar(), 27.toChar(), 28.toChar(), 29.toChar(), 30.toChar(), 31.toChar(), 32.toChar(), 33.toChar(), 34.toChar(), 35.toChar(), 36.toChar(), 37.toChar(), 38.toChar(), 39.toChar(), 40.toChar(), 41.toChar(), 42.toChar(), 43.toChar(), 44.toChar(), 45.toChar(), 46.toChar(), 47.toChar(), 48.toChar(), 49.toChar(), 50.toChar(), 51.toChar(), 52.toChar(), 53.toChar(), 54.toChar(), 55.toChar(), 56.toChar(), 57.toChar(), 58.toChar(), 59.toChar(), 60.toChar(), 61.toChar(), 62.toChar(), 63.toChar(), 64.toChar(), 65.toChar(), 66.toChar(), 67.toChar(), 68.toChar(), 69.toChar(), 70.toChar(), 71.toChar(), 72.toChar(), 73.toChar(), 74.toChar(), 75.toChar(), 76.toChar(), 77.toChar(), 78.toChar(), 79.toChar(), 80.toChar(), 81.toChar(), 82.toChar(), 83.toChar(), 84.toChar(), 85.toChar(), 86.toChar(), 87.toChar(), 88.toChar(), 89.toChar(), 90.toChar(), 91.toChar(), 92.toChar(), 93.toChar(), 94.toChar(), 95.toChar(), 96.toChar(), 97.toChar(), 98.toChar(), 99.toChar(), 100.toChar(), 101.toChar(), 102.toChar(), 103.toChar(), 104.toChar(), 105.toChar(), 106.toChar(), 107.toChar(), 108.toChar(), 109.toChar(), 110.toChar(), 111.toChar(), 112.toChar(), 113.toChar(), 114.toChar(), 115.toChar(), 116.toChar(), 117.toChar(), 118.toChar(), 119.toChar(), 120.toChar(), 121.toChar(), 122.toChar(), 123.toChar(), 124.toChar(), 125.toChar(), 126.toChar(), 127.toChar(), 0x2022.toChar(), 0x2020.toChar(), 0x2021.toChar(), 0x2026.toChar(), 0x2014.toChar(), 0x2013.toChar(), 0x0192.toChar(), 0x2044.toChar(), 0x2039.toChar(), 0x203a.toChar(), 0x2212.toChar(), 0x2030.toChar(), 0x201e.toChar(), 0x201c.toChar(), 0x201d.toChar(), 0x2018.toChar(), 0x2019.toChar(), 0x201a.toChar(), 0x2122.toChar(), 0xfb01.toChar(), 0xfb02.toChar(), 0x0141.toChar(), 0x0152.toChar(), 0x0160.toChar(), 0x0178.toChar(), 0x017d.toChar(), 0x0131.toChar(), 0x0142.toChar(), 0x0153.toChar(), 0x0161.toChar(), 0x017e.toChar(), 65533.toChar(), 0x20ac.toChar(), 161.toChar(), 162.toChar(), 163.toChar(), 164.toChar(), 165.toChar(), 166.toChar(), 167.toChar(), 168.toChar(), 169.toChar(), 170.toChar(), 171.toChar(), 172.toChar(), 173.toChar(), 174.toChar(), 175.toChar(), 176.toChar(), 177.toChar(), 178.toChar(), 179.toChar(), 180.toChar(), 181.toChar(), 182.toChar(), 183.toChar(), 184.toChar(), 185.toChar(), 186.toChar(), 187.toChar(), 188.toChar(), 189.toChar(), 190.toChar(), 191.toChar(), 192.toChar(), 193.toChar(), 194.toChar(), 195.toChar(), 196.toChar(), 197.toChar(), 198.toChar(), 199.toChar(), 200.toChar(), 201.toChar(), 202.toChar(), 203.toChar(), 204.toChar(), 205.toChar(), 206.toChar(), 207.toChar(), 208.toChar(), 209.toChar(), 210.toChar(), 211.toChar(), 212.toChar(), 213.toChar(), 214.toChar(), 215.toChar(), 216.toChar(), 217.toChar(), 218.toChar(), 219.toChar(), 220.toChar(), 221.toChar(), 222.toChar(), 223.toChar(), 224.toChar(), 225.toChar(), 226.toChar(), 227.toChar(), 228.toChar(), 229.toChar(), 230.toChar(), 231.toChar(), 232.toChar(), 233.toChar(), 234.toChar(), 235.toChar(), 236.toChar(), 237.toChar(), 238.toChar(), 239.toChar(), 240.toChar(), 241.toChar(), 242.toChar(), 243.toChar(), 244.toChar(), 245.toChar(), 246.toChar(), 247.toChar(), 248.toChar(), 249.toChar(), 250.toChar(), 251.toChar(), 252.toChar(), 253.toChar(), 254.toChar(), 255.toChar())

    internal val winansi = IntHashtable()

    internal val pdfEncoding = IntHashtable()

    internal var extraEncodings = HashMap<String, ExtraEncoding>()

    init {
        for (k in 128..160) {
            val c = winansiByteToChar[k]
            if (c.toInt() != 65533)
                winansi.put(c.toInt(), k)
        }

        for (k in 128..160) {
            val c = pdfEncodingByteToChar[k]
            if (c.toInt() != 65533)
                pdfEncoding.put(c.toInt(), k)
        }

        addExtraEncoding("Wingdings", WingdingsConversion())
        addExtraEncoding("Symbol", SymbolConversion(true))
        addExtraEncoding("ZapfDingbats", SymbolConversion(false))
        addExtraEncoding("SymbolTT", SymbolTTConversion())
        addExtraEncoding("Cp437", Cp437Conversion())
    }

    /** Converts a String to a byte array according
     * to the font's encoding.
     * @return an array of byte representing the conversion according to the font's encoding
     * *
     * @param encoding the encoding
     * *
     * @param text the String to be converted
     */
    fun convertToBytes(text: String?, encoding: String?): ByteArray {
        if (text == null)
            return ByteArray(0)
        if (encoding == null || encoding.length == 0) {
            val len = text.length
            val b = ByteArray(len)
            for (k in 0..len - 1)
                b[k] = text[k].toByte()
            return b
        }
        val extra = extraEncodings[encoding.toLowerCase()]
        if (extra != null) {
            val b = extra.charToByte(text, encoding)
            if (b != null)
                return b
        }
        var hash: IntHashtable? = null
        if (encoding == BaseFont.WINANSI)
            hash = winansi
        else if (encoding == PdfObject.TEXT_PDFDOCENCODING)
            hash = pdfEncoding
        if (hash != null) {
            val cc = text.toCharArray()
            val len = cc.size
            var ptr = 0
            val b = ByteArray(len)
            var c = 0
            for (k in 0..len - 1) {
                val char1 = cc[k]
                if (char1.toInt() < 128 || char1.toInt() > 160 && char1.toInt() <= 255)
                    c = char1.toInt()
                else
                    c = hash.get(char1.toInt())
                if (c != 0)
                    b[ptr++] = c.toByte()
            }
            if (ptr == len)
                return b
            val b2 = ByteArray(ptr)
            System.arraycopy(b, 0, b2, 0, ptr)
            return b2
        }
        if (encoding == PdfObject.TEXT_UNICODE) {
            // workaround for jdk 1.2.2 bug
            val cc = text.toCharArray()
            val len = cc.size
            val b = ByteArray(cc.size * 2 + 2)
            b[0] = -2
            b[1] = -1
            var bptr = 2
            for (k in 0..len - 1) {
                val c = cc[k]
                b[bptr++] = (c.toInt() shr 8).toByte()
                b[bptr++] = (c.toInt() and 0xff).toByte()
            }
            return b
        }
        try {
            val cc = Charset.forName(encoding)
            val ce = cc.newEncoder()
            ce.onUnmappableCharacter(CodingErrorAction.IGNORE)
            val cb = CharBuffer.wrap(text.toCharArray())
            val bb = ce.encode(cb)
            bb.rewind()
            val lim = bb.limit()
            val br = ByteArray(lim)
            bb.get(br)
            return br
        } catch (e: IOException) {
            throw ExceptionConverter(e)
        }

    }

    /** Converts a String to a byte array according
     * to the font's encoding.
     * @return an array of byte representing the conversion according to the font's encoding
     * *
     * @param encoding the encoding
     * *
     * @param char1 the char to be converted
     */
    fun convertToBytes(char1: Char, encoding: String?): ByteArray {
        if (encoding == null || encoding.length == 0)
            return byteArrayOf(char1.toByte())
        val extra = extraEncodings[encoding.toLowerCase()]
        if (extra != null) {
            val b = extra.charToByte(char1, encoding)
            if (b != null)
                return b
        }
        var hash: IntHashtable? = null
        if (encoding == BaseFont.WINANSI)
            hash = winansi
        else if (encoding == PdfObject.TEXT_PDFDOCENCODING)
            hash = pdfEncoding
        if (hash != null) {
            var c = 0
            if (char1.toInt() < 128 || char1.toInt() > 160 && char1.toInt() <= 255)
                c = char1.toInt()
            else
                c = hash.get(char1.toInt())
            if (c != 0)
                return byteArrayOf(c.toByte())
            else
                return ByteArray(0)
        }
        if (encoding == PdfObject.TEXT_UNICODE) {
            // workaround for jdk 1.2.2 bug
            val b = ByteArray(4)
            b[0] = -2
            b[1] = -1
            b[2] = (char1.toInt() shr 8).toByte()
            b[3] = (char1.toInt() and 0xff).toByte()
            return b
        }
        try {
            val cc = Charset.forName(encoding)
            val ce = cc.newEncoder()
            ce.onUnmappableCharacter(CodingErrorAction.IGNORE)
            val cb = CharBuffer.wrap(charArrayOf(char1))
            val bb = ce.encode(cb)
            bb.rewind()
            val lim = bb.limit()
            val br = ByteArray(lim)
            bb.get(br)
            return br
        } catch (e: IOException) {
            throw ExceptionConverter(e)
        }

    }

    /** Converts a byte array to a String according
     * to the some encoding.
     * @param bytes the bytes to convert
     * *
     * @param encoding the encoding
     * *
     * @return the converted String
     */
    fun convertToString(bytes: ByteArray?, encoding: String?): String {
        if (bytes == null)
            return PdfObject.NOTHING
        if (encoding == null || encoding.length == 0) {
            val c = CharArray(bytes.size)
            for (k in bytes.indices)
                c[k] = (bytes[k] and 0xff).toChar()
            return String(c)
        }
        val extra = extraEncodings[encoding.toLowerCase()]
        if (extra != null) {
            val text = extra.byteToChar(bytes, encoding)
            if (text != null)
                return text
        }
        var ch: CharArray? = null
        if (encoding == BaseFont.WINANSI)
            ch = winansiByteToChar
        else if (encoding == PdfObject.TEXT_PDFDOCENCODING)
            ch = pdfEncodingByteToChar
        if (ch != null) {
            val len = bytes.size
            val c = CharArray(len)
            for (k in 0..len - 1) {
                c[k] = ch[bytes[k] and 0xff]
            }
            return String(c)
        }
        try {
            return String(bytes, encoding)
        } catch (e: UnsupportedEncodingException) {
            throw ExceptionConverter(e)
        }

    }

    /** Checks is text only has PdfDocEncoding characters.
     * @param text the String to test
     * *
     * @return true if only PdfDocEncoding characters are present
     */
    fun isPdfDocEncoding(text: String?): Boolean {
        if (text == null)
            return true
        val len = text.length
        for (k in 0..len - 1) {
            val char1 = text[k]
            if (char1.toInt() < 128 || char1.toInt() > 160 && char1.toInt() <= 255)
                continue
            if (!pdfEncoding.containsKey(char1.toInt()))
                return false
        }
        return true
    }

    /** Adds an extra encoding.
     * @param name the name of the encoding. The encoding recognition is case insensitive
     * *
     * @param enc the conversion class
     */
    @SuppressWarnings("unchecked")
    fun addExtraEncoding(name: String, enc: ExtraEncoding) {
        synchronized (extraEncodings) { // This serializes concurrent updates
            val newEncodings = extraEncodings.clone() as HashMap<String, ExtraEncoding>
            newEncodings.put(name.toLowerCase(), enc)
            extraEncodings = newEncodings  // This swap does not require synchronization with reader
        }
    }

    private class WingdingsConversion : ExtraEncoding {

        override fun charToByte(char1: Char, encoding: String): ByteArray {
            if (char1 == ' ')
                return byteArrayOf(char1.toByte())
            else if (char1 >= '\u2701' && char1 <= '\u27BE') {
                val v = table[char1.toInt() - 0x2700]
                if (v.toInt() != 0)
                    return byteArrayOf(v)
            }
            return ByteArray(0)
        }

        override fun charToByte(text: String, encoding: String): ByteArray {
            val cc = text.toCharArray()
            val b = ByteArray(cc.size)
            var ptr = 0
            val len = cc.size
            for (k in 0..len - 1) {
                val c = cc[k]
                if (c == ' ')
                    b[ptr++] = c.toByte()
                else if (c >= '\u2701' && c <= '\u27BE') {
                    val v = table[c.toInt() - 0x2700]
                    if (v.toInt() != 0)
                        b[ptr++] = v
                }
            }
            if (ptr == len)
                return b
            val b2 = ByteArray(ptr)
            System.arraycopy(b, 0, b2, 0, ptr)
            return b2
        }

        override fun byteToChar(b: ByteArray, encoding: String): String? {
            return null
        }

        companion object {

            private val table = byteArrayOf(0, 35, 34, 0, 0, 0, 41, 62, 81, 42, 0, 0, 65, 63, 0, 0, 0, 0, 0, -4, 0, 0, 0, -5, 0, 0, 0, 0, 0, 0, 86, 0, 88, 89, 0, 0, 0, 0, 0, 0, 0, 0, -75, 0, 0, 0, 0, 0, -74, 0, 0, 0, -83, -81, -84, 0, 0, 0, 0, 0, 0, 0, 0, 124, 123, 0, 0, 0, 84, 0, 0, 0, 0, 0, 0, 0, 0, -90, 0, 0, 0, 113, 114, 0, 0, 0, 117, 0, 0, 0, 0, 0, 0, 125, 126, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -116, -115, -114, -113, -112, -111, -110, -109, -108, -107, -127, -126, -125, -124, -123, -122, -121, -120, -119, -118, -116, -115, -114, -113, -112, -111, -110, -109, -108, -107, -24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -24, -40, 0, 0, -60, -58, 0, 0, -16, 0, 0, 0, 0, 0, 0, 0, 0, 0, -36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        }
    }

    private class Cp437Conversion : ExtraEncoding {

        override fun charToByte(text: String, encoding: String): ByteArray {
            val cc = text.toCharArray()
            val b = ByteArray(cc.size)
            var ptr = 0
            val len = cc.size
            for (k in 0..len - 1) {
                val c = cc[k]
                if (c.toInt() < 128)
                    b[ptr++] = c.toByte()
                else {
                    val v = c2b.get(c.toInt()).toByte()
                    if (v.toInt() != 0)
                        b[ptr++] = v
                }
            }
            if (ptr == len)
                return b
            val b2 = ByteArray(ptr)
            System.arraycopy(b, 0, b2, 0, ptr)
            return b2
        }

        override fun charToByte(char1: Char, encoding: String): ByteArray {
            if (char1.toInt() < 128)
                return byteArrayOf(char1.toByte())
            else {
                val v = c2b.get(char1.toInt()).toByte()
                if (v.toInt() != 0)
                    return byteArrayOf(v)
                else
                    return ByteArray(0)
            }
        }

        override fun byteToChar(b: ByteArray, encoding: String): String {
            val len = b.size
            val cc = CharArray(len)
            var ptr = 0
            for (k in 0..len - 1) {
                val c = b[k] and 0xff
                if (c < ' ')
                    continue
                if (c < 128)
                    cc[ptr++] = c.toChar()
                else {
                    val v = table[c - 128]
                    cc[ptr++] = v
                }
            }
            return String(cc, 0, ptr)
        }

        companion object {
            private val c2b = IntHashtable()

            private val table = charArrayOf('\u00C7', '\u00FC', '\u00E9', '\u00E2', '\u00E4', '\u00E0', '\u00E5', '\u00E7', '\u00EA', '\u00EB', '\u00E8', '\u00EF', '\u00EE', '\u00EC', '\u00C4', '\u00C5', '\u00C9', '\u00E6', '\u00C6', '\u00F4', '\u00F6', '\u00F2', '\u00FB', '\u00F9', '\u00FF', '\u00D6', '\u00DC', '\u00A2', '\u00A3', '\u00A5', '\u20A7', '\u0192', '\u00E1', '\u00ED', '\u00F3', '\u00FA', '\u00F1', '\u00D1', '\u00AA', '\u00BA', '\u00BF', '\u2310', '\u00AC', '\u00BD', '\u00BC', '\u00A1', '\u00AB', '\u00BB', '\u2591', '\u2592', '\u2593', '\u2502', '\u2524', '\u2561', '\u2562', '\u2556', '\u2555', '\u2563', '\u2551', '\u2557', '\u255D', '\u255C', '\u255B', '\u2510', '\u2514', '\u2534', '\u252C', '\u251C', '\u2500', '\u253C', '\u255E', '\u255F', '\u255A', '\u2554', '\u2569', '\u2566', '\u2560', '\u2550', '\u256C', '\u2567', '\u2568', '\u2564', '\u2565', '\u2559', '\u2558', '\u2552', '\u2553', '\u256B', '\u256A', '\u2518', '\u250C', '\u2588', '\u2584', '\u258C', '\u2590', '\u2580', '\u03B1', '\u00DF', '\u0393', '\u03C0', '\u03A3', '\u03C3', '\u00B5', '\u03C4', '\u03A6', '\u0398', '\u03A9', '\u03B4', '\u221E', '\u03C6', '\u03B5', '\u2229', '\u2261', '\u00B1', '\u2265', '\u2264', '\u2320', '\u2321', '\u00F7', '\u2248', '\u00B0', '\u2219', '\u00B7', '\u221A', '\u207F', '\u00B2', '\u25A0', '\u00A0')

            init {
                for (k in table.indices)
                    c2b.put(table[k].toInt(), k + 128)
            }
        }
    }

    private class SymbolConversion internal constructor(symbol: Boolean) : ExtraEncoding {
        private var translation: IntHashtable? = null
        private val byteToChar: CharArray

        init {
            if (symbol) {
                translation = t1
                byteToChar = table1
            } else {
                translation = t2
                byteToChar = table2
            }
        }

        override fun charToByte(text: String, encoding: String): ByteArray {
            val cc = text.toCharArray()
            val b = ByteArray(cc.size)
            var ptr = 0
            val len = cc.size
            for (k in 0..len - 1) {
                val c = cc[k]
                val v = translation!!.get(c.toInt()).toByte()
                if (v.toInt() != 0)
                    b[ptr++] = v
            }
            if (ptr == len)
                return b
            val b2 = ByteArray(ptr)
            System.arraycopy(b, 0, b2, 0, ptr)
            return b2
        }

        override fun charToByte(char1: Char, encoding: String): ByteArray {
            val v = translation!!.get(char1.toInt()).toByte()
            if (v.toInt() != 0)
                return byteArrayOf(v)
            else
                return ByteArray(0)
        }

        override fun byteToChar(b: ByteArray, encoding: String): String {
            val len = b.size
            val cc = CharArray(len)
            var ptr = 0
            for (k in 0..len - 1) {
                val c = b[k] and 0xff
                val v = byteToChar[c]
                cc[ptr++] = v
            }
            return String(cc, 0, ptr)
        }

        companion object {

            private val t1 = IntHashtable()
            private val t2 = IntHashtable()

            private val table1 = charArrayOf('\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', ' ', '!', '\u2200', '#', '\u2203', '%', '&', '\u220b', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '\u2245', '\u0391', '\u0392', '\u03a7', '\u0394', '\u0395', '\u03a6', '\u0393', '\u0397', '\u0399', '\u03d1', '\u039a', '\u039b', '\u039c', '\u039d', '\u039f', '\u03a0', '\u0398', '\u03a1', '\u03a3', '\u03a4', '\u03a5', '\u03c2', '\u03a9', '\u039e', '\u03a8', '\u0396', '[', '\u2234', ']', '\u22a5', '_', '\u0305', '\u03b1', '\u03b2', '\u03c7', '\u03b4', '\u03b5', '\u03d5', '\u03b3', '\u03b7', '\u03b9', '\u03c6', '\u03ba', '\u03bb', '\u03bc', '\u03bd', '\u03bf', '\u03c0', '\u03b8', '\u03c1', '\u03c3', '\u03c4', '\u03c5', '\u03d6', '\u03c9', '\u03be', '\u03c8', '\u03b6', '{', '|', '}', '~', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\u20ac', '\u03d2', '\u2032', '\u2264', '\u2044', '\u221e', '\u0192', '\u2663', '\u2666', '\u2665', '\u2660', '\u2194', '\u2190', '\u2191', '\u2192', '\u2193', '\u00b0', '\u00b1', '\u2033', '\u2265', '\u00d7', '\u221d', '\u2202', '\u2022', '\u00f7', '\u2260', '\u2261', '\u2248', '\u2026', '\u2502', '\u2500', '\u21b5', '\u2135', '\u2111', '\u211c', '\u2118', '\u2297', '\u2295', '\u2205', '\u2229', '\u222a', '\u2283', '\u2287', '\u2284', '\u2282', '\u2286', '\u2208', '\u2209', '\u2220', '\u2207', '\u00ae', '\u00a9', '\u2122', '\u220f', '\u221a', '\u22c5', '\u00ac', '\u2227', '\u2228', '\u21d4', '\u21d0', '\u21d1', '\u21d2', '\u21d3', '\u25ca', '\u2329', '\0', '\0', '\0', '\u2211', '\u239b', '\u239c', '\u239d', '\u23a1', '\u23a2', '\u23a3', '\u23a7', '\u23a8', '\u23a9', '\u23aa', '\0', '\u232a', '\u222b', '\u2320', '\u23ae', '\u2321', '\u239e', '\u239f', '\u23a0', '\u23a4', '\u23a5', '\u23a6', '\u23ab', '\u23ac', '\u23ad', '\0')

            private val table2 = charArrayOf('\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\u0020', '\u2701', '\u2702', '\u2703', '\u2704', '\u260e', '\u2706', '\u2707', '\u2708', '\u2709', '\u261b', '\u261e', '\u270C', '\u270D', '\u270E', '\u270F', '\u2710', '\u2711', '\u2712', '\u2713', '\u2714', '\u2715', '\u2716', '\u2717', '\u2718', '\u2719', '\u271A', '\u271B', '\u271C', '\u271D', '\u271E', '\u271F', '\u2720', '\u2721', '\u2722', '\u2723', '\u2724', '\u2725', '\u2726', '\u2727', '\u2605', '\u2729', '\u272A', '\u272B', '\u272C', '\u272D', '\u272E', '\u272F', '\u2730', '\u2731', '\u2732', '\u2733', '\u2734', '\u2735', '\u2736', '\u2737', '\u2738', '\u2739', '\u273A', '\u273B', '\u273C', '\u273D', '\u273E', '\u273F', '\u2740', '\u2741', '\u2742', '\u2743', '\u2744', '\u2745', '\u2746', '\u2747', '\u2748', '\u2749', '\u274A', '\u274B', '\u25cf', '\u274D', '\u25a0', '\u274F', '\u2750', '\u2751', '\u2752', '\u25b2', '\u25bc', '\u25c6', '\u2756', '\u25d7', '\u2758', '\u2759', '\u275A', '\u275B', '\u275C', '\u275D', '\u275E', '\u0000', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\u0000', '\u2761', '\u2762', '\u2763', '\u2764', '\u2765', '\u2766', '\u2767', '\u2663', '\u2666', '\u2665', '\u2660', '\u2460', '\u2461', '\u2462', '\u2463', '\u2464', '\u2465', '\u2466', '\u2467', '\u2468', '\u2469', '\u2776', '\u2777', '\u2778', '\u2779', '\u277A', '\u277B', '\u277C', '\u277D', '\u277E', '\u277F', '\u2780', '\u2781', '\u2782', '\u2783', '\u2784', '\u2785', '\u2786', '\u2787', '\u2788', '\u2789', '\u278A', '\u278B', '\u278C', '\u278D', '\u278E', '\u278F', '\u2790', '\u2791', '\u2792', '\u2793', '\u2794', '\u2192', '\u2194', '\u2195', '\u2798', '\u2799', '\u279A', '\u279B', '\u279C', '\u279D', '\u279E', '\u279F', '\u27A0', '\u27A1', '\u27A2', '\u27A3', '\u27A4', '\u27A5', '\u27A6', '\u27A7', '\u27A8', '\u27A9', '\u27AA', '\u27AB', '\u27AC', '\u27AD', '\u27AE', '\u27AF', '\u0000', '\u27B1', '\u27B2', '\u27B3', '\u27B4', '\u27B5', '\u27B6', '\u27B7', '\u27B8', '\u27B9', '\u27BA', '\u27BB', '\u27BC', '\u27BD', '\u27BE', '\u0000')

            init {
                for (k in 0..255) {
                    val v = table1[k].toInt()
                    if (v != 0)
                        t1.put(v, k)
                }
                for (k in 0..255) {
                    val v = table2[k].toInt()
                    if (v != 0)
                        t2.put(v, k)
                }
            }
        }
    }

    private class SymbolTTConversion : ExtraEncoding {

        override fun charToByte(char1: Char, encoding: String): ByteArray {
            if (char1.toInt() and 0xff00 == 0 || char1.toInt() and 0xff00 == 0xf000)
                return byteArrayOf(char1.toByte())
            else
                return ByteArray(0)
        }

        override fun charToByte(text: String, encoding: String): ByteArray {
            val ch = text.toCharArray()
            val b = ByteArray(ch.size)
            var ptr = 0
            val len = ch.size
            for (k in 0..len - 1) {
                val c = ch[k]
                if (c.toInt() and 0xff00 == 0 || c.toInt() and 0xff00 == 0xf000)
                    b[ptr++] = c.toByte()
            }
            if (ptr == len)
                return b
            val b2 = ByteArray(ptr)
            System.arraycopy(b, 0, b2, 0, ptr)
            return b2
        }

        override fun byteToChar(b: ByteArray, encoding: String): String? {
            return null
        }

    }
}
