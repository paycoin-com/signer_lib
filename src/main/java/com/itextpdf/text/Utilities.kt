/*
 * $Id: db5d2076ca1da9887a3e739c66e6749b7e6b1314 $
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
package com.itextpdf.text

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.util.Collections
import java.util.Hashtable
import java.util.Properties

import com.itextpdf.text.pdf.ByteBuffer
import com.itextpdf.text.pdf.PRTokeniser
import com.itextpdf.text.pdf.PdfEncodings

/**
 * A collection of convenience methods that were present in many different iText
 * classes.
 */

object Utilities {

    /**
     * Gets the keys of a Hashtable.
     * Marked as deprecated, not used anywhere anymore.
     * @param  type for the key
     * *
     * @param  type for the value
     * *
     * @param table
     * *            a Hashtable
     * *
     * @return the keyset of a Hashtable (or an empty set if table is null)
     */
    @Deprecated("")
    fun <K, V> getKeySet(table: Hashtable<K, V>?): Set<K> {
        return if (table == null) emptySet<K>() else table.keys
    }

    /**
     * Utility method to extend an array.

     * @param original
     * *            the original array or null
     * *
     * @param item
     * *            the item to be added to the array
     * *
     * @return a new array with the item appended
     */
    fun addToArray(original: Array<Array<Any>>?, item: Array<Any>): Array<Array<Any>> {
        var original = original
        if (original == null) {
            original = arrayOfNulls<Array<Any>>(1)
            original[0] = item
            return original
        } else {
            val original2 = arrayOfNulls<Array<Any>>(original.size + 1)
            System.arraycopy(original, 0, original2, 0, original.size)
            original2[original.size] = item
            return original2
        }
    }

    /**
     * Checks for a true/false value of a key in a Properties object.
     * @param attributes
     * *
     * @param key
     * *
     * @return a true/false value of a key in a Properties object
     */
    fun checkTrueOrFalse(attributes: Properties, key: String): Boolean {
        return "true".equals(attributes.getProperty(key), ignoreCase = true)
    }

    /**
     * Unescapes an URL. All the "%xx" are replaced by the 'xx' hex char value.
     * @param src the url to unescape
     * *
     * @return the unescaped value
     */
    fun unEscapeURL(src: String): String {
        val bf = StringBuffer()
        val s = src.toCharArray()
        var k = 0
        while (k < s.size) {
            val c = s[k]
            if (c == '%') {
                if (k + 2 >= s.size) {
                    bf.append(c)
                    ++k
                    continue
                }
                val a0 = PRTokeniser.getHex(s[k + 1])
                val a1 = PRTokeniser.getHex(s[k + 2])
                if (a0 < 0 || a1 < 0) {
                    bf.append(c)
                    ++k
                    continue
                }
                bf.append((a0 * 16 + a1).toChar())
                k += 2
            } else
                bf.append(c)
            ++k
        }
        return bf.toString()
    }

    /**
     * This method makes a valid URL from a given filename.
     *
     * This method makes the conversion of this library from the JAVA 2 platform
     * to a JDK1.1.x-version easier.

     * @param filename
     * *            a given filename
     * *
     * @return a valid URL
     * *
     * @throws MalformedURLException
     */
    @Throws(MalformedURLException::class)
    fun toURL(filename: String): URL {
        try {
            return URL(filename)
        } catch (e: Exception) {
            return File(filename).toURI().toURL()
        }

    }

    /**
     * This method is an alternative for the InputStream.skip()
     * -method that doesn't seem to work properly for big values of size
     * .

     * @param is
     * *            the InputStream
     * *
     * @param size
     * *            the number of bytes to skip
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun skip(`is`: InputStream, size: Int) {
        var size = size
        var n: Long
        while (size > 0) {
            n = `is`.skip(size.toLong())
            if (n <= 0)
                break
            size -= n.toInt()
        }
    }

    /**
     * Measurement conversion from millimeters to points.
     * @param    value    a value in millimeters
     * *
     * @return    a value in points
     * *
     * @since    2.1.2
     */
    fun millimetersToPoints(value: Float): Float {
        return inchesToPoints(millimetersToInches(value))
    }

    /**
     * Measurement conversion from millimeters to inches.
     * @param    value    a value in millimeters
     * *
     * @return    a value in inches
     * *
     * @since    2.1.2
     */
    fun millimetersToInches(value: Float): Float {
        return value / 25.4f
    }

    /**
     * Measurement conversion from points to millimeters.
     * @param    value    a value in points
     * *
     * @return    a value in millimeters
     * *
     * @since    2.1.2
     */
    fun pointsToMillimeters(value: Float): Float {
        return inchesToMillimeters(pointsToInches(value))
    }

    /**
     * Measurement conversion from points to inches.
     * @param    value    a value in points
     * *
     * @return    a value in inches
     * *
     * @since    2.1.2
     */
    fun pointsToInches(value: Float): Float {
        return value / 72f
    }

    /**
     * Measurement conversion from inches to millimeters.
     * @param    value    a value in inches
     * *
     * @return    a value in millimeters
     * *
     * @since    2.1.2
     */
    fun inchesToMillimeters(value: Float): Float {
        return value * 25.4f
    }

    /**
     * Measurement conversion from inches to points.
     * @param    value    a value in inches
     * *
     * @return    a value in points
     * *
     * @since    2.1.2
     */
    fun inchesToPoints(value: Float): Float {
        return value * 72f
    }

    /**
     * Check if the value of a character belongs to a certain interval
     * that indicates it's the higher part of a surrogate pair.
     * @param c    the character
     * *
     * @return    true if the character belongs to the interval
     * *
     * @since    2.1.2
     */
    fun isSurrogateHigh(c: Char): Boolean {
        return c >= '\ud800' && c <= '\udbff'
    }

    /**
     * Check if the value of a character belongs to a certain interval
     * that indicates it's the lower part of a surrogate pair.
     * @param c    the character
     * *
     * @return    true if the character belongs to the interval
     * *
     * @since    2.1.2
     */
    fun isSurrogateLow(c: Char): Boolean {
        return c >= '\udc00' && c <= '\udfff'
    }

    /**
     * Checks if two subsequent characters in a String are
     * are the higher and the lower character in a surrogate
     * pair (and therefore eligible for conversion to a UTF 32 character).
     * @param text    the String with the high and low surrogate characters
     * *
     * @param idx    the index of the 'high' character in the pair
     * *
     * @return    true if the characters are surrogate pairs
     * *
     * @since    2.1.2
     */
    fun isSurrogatePair(text: String, idx: Int): Boolean {
        if (idx < 0 || idx > text.length - 2)
            return false
        return isSurrogateHigh(text[idx]) && isSurrogateLow(text[idx + 1])
    }

    /**
     * Checks if two subsequent characters in a character array are
     * are the higher and the lower character in a surrogate
     * pair (and therefore eligible for conversion to a UTF 32 character).
     * @param text    the character array with the high and low surrogate characters
     * *
     * @param idx    the index of the 'high' character in the pair
     * *
     * @return    true if the characters are surrogate pairs
     * *
     * @since    2.1.2
     */
    fun isSurrogatePair(text: CharArray, idx: Int): Boolean {
        if (idx < 0 || idx > text.size - 2)
            return false
        return isSurrogateHigh(text[idx]) && isSurrogateLow(text[idx + 1])
    }

    /**
     * Returns the code point of a UTF32 character corresponding with
     * a high and a low surrogate value.
     * @param highSurrogate    the high surrogate value
     * *
     * @param lowSurrogate    the low surrogate value
     * *
     * @return    a code point value
     * *
     * @since    2.1.2
     */
    fun convertToUtf32(highSurrogate: Char, lowSurrogate: Char): Int {
        return (highSurrogate.toInt() - 0xd800) * 0x400 + lowSurrogate.toInt() - 0xdc00 + 0x10000
    }

    /**
     * Converts a unicode character in a character array to a UTF 32 code point value.
     * @param text    a character array that has the unicode character(s)
     * *
     * @param idx    the index of the 'high' character
     * *
     * @return    the code point value
     * *
     * @since    2.1.2
     */
    fun convertToUtf32(text: CharArray, idx: Int): Int {
        return (text[idx].toInt() - 0xd800) * 0x400 + text[idx + 1].toInt() - 0xdc00 + 0x10000
    }

    /**
     * Converts a unicode character in a String to a UTF32 code point value
     * @param text    a String that has the unicode character(s)
     * *
     * @param idx    the index of the 'high' character
     * *
     * @return    the codepoint value
     * *
     * @since    2.1.2
     */
    fun convertToUtf32(text: String, idx: Int): Int {
        return (text[idx].toInt() - 0xd800) * 0x400 + text[idx + 1].toInt() - 0xdc00 + 0x10000
    }

    /**
     * Converts a UTF32 code point value to a String with the corresponding character(s).
     * @param codePoint    a Unicode value
     * *
     * @return    the corresponding characters in a String
     * *
     * @since    2.1.2
     */
    fun convertFromUtf32(codePoint: Int): String {
        var codePoint = codePoint
        if (codePoint < 0x10000)
            return Character.toString(codePoint.toChar())
        codePoint -= 0x10000
        return String(charArrayOf((codePoint / 0x400 + 0xd800).toChar(), (codePoint % 0x400 + 0xdc00).toChar()))
    }

    /**
     * Reads the contents of a file to a String.
     * @param    path    the path to the file
     * *
     * @return    a String with the contents of the file
     * *
     * @throws IOException
     * *
     * @since    iText 5.0.0
     */
    @Throws(IOException::class)
    fun readFileToString(path: String): String {
        return readFileToString(File(path))
    }

    /**
     * Reads the contents of a file to a String.
     * @param    file    a file
     * *
     * @return    a String with the contents of the file
     * *
     * @throws IOException if file was not found or could not be read.
     * *
     * @since    iText 5.0.0
     */
    @Throws(IOException::class)
    fun readFileToString(file: File): String {
        val jsBytes = ByteArray(file.length().toInt())
        val f = FileInputStream(file)
        f.read(jsBytes)
        return String(jsBytes)
    }

    /**
     * Converts an array of bytes to a String of hexadecimal values
     * @param bytes    a byte array
     * *
     * @return    the same bytes expressed as hexadecimal values
     */
    fun convertToHex(bytes: ByteArray): String {
        val buf = ByteBuffer()
        for (b in bytes) {
            buf.appendHex(b)
        }
        return PdfEncodings.convertToString(buf.toByteArray(), null).toUpperCase()
    }

    /**
     * Copies the specified range of the specified array into a new array.
     * The initial index of the range (from) must lie between zero
     * and original.length, inclusive.  The value at
     * original[from] is placed into the initial element of the copy
     * (unless from == original.length or from == to).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (to), which must be greater than or equal to from,
     * may be greater than original.length, in which case
     * '\\u000' is placed in all elements of the copy whose index is
     * greater than or equal to original.length - from.  The length
     * of the returned array will be to - from.

     * @param original the array from which a range is to be copied
     * *
     * @param from the initial index of the range to be copied, inclusive
     * *
     * @param to the final index of the range to be copied, exclusive.
     * *     (This index may lie outside the array.)
     * *
     * @return a new array containing the specified range from the original array,
     * *     truncated or padded with null characters to obtain the required length
     * *
     * @throws ArrayIndexOutOfBoundsException if `from &lt; 0`
     * *     or `from &gt; original.length`
     * *
     * @throws IllegalArgumentException if from &gt; to
     * *
     * @throws NullPointerException if original is null
     * *
     * @since 1.6
     */
    fun copyOfRange(original: CharArray, from: Int, to: Int): CharArray {
        val newLength = to - from
        if (newLength < 0)
            throw IllegalArgumentException(from + " > " + to)
        val copy = CharArray(newLength)
        System.arraycopy(original, from, copy, 0,
                Math.min(original.size - from, newLength))
        return copy
    }

}
