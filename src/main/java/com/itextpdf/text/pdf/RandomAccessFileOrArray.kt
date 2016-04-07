/*
 * $Id: c23d87eb80847c87515f17a1692b8e84066512da $
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

import com.itextpdf.text.Document
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.io.IndependentRandomAccessSource
import com.itextpdf.text.io.RandomAccessSource
import com.itextpdf.text.io.RandomAccessSourceFactory

import java.io.DataInput
import java.io.DataInputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.net.URL

/** Intended to be layered on top of a low level RandomAccessSource object.  Provides
 * functionality useful during parsing:
 *
 *  * tracks current position in the file
 *  * allows single byte pushback
 *  * allows reading of multi-byte data structures (int, long, String) for both Big and Little Endian representations
 *  * allows creation of independent 'views' of the underlying data source
 *

 * @author Paulo Soares, Kevin Day
 */
class RandomAccessFileOrArray
/**
 * Creates a RandomAccessFileOrArray that wraps the specified byte source.  The byte source will be closed when
 * this RandomAccessFileOrArray is closed.
 * @param byteSource the byte source to wrap
 */
(
        /**
         * The source that backs this object
         */
        protected //TODO: I'm only putting this in here for backwards compatability with PdfReader(RAFOA, byte[]).  Once we get rid of the
        //PdfReader constructor, we can get rid of this method as well
        val byteSource: RandomAccessSource) : DataInput {

    /**
     * The physical location in the underlying byte source.
     */
    private var byteSourcePosition: Long = 0

    /**
     * the pushed  back byte, if any
     */
    private var back: Byte = 0
    /**
     * Whether there is a pushed back byte
     */
    private var isBack = false

    /**
     * @param filename
     * *
     * @throws IOException
     */
    @Deprecated("")
    @Deprecated("use {@link RandomAccessFileOrArray#RandomAccessFileOrArray(RandomAccessSource)} instead\n      ")
    @Throws(IOException::class)
    constructor(filename: String) : this(RandomAccessSourceFactory().setForceRead(false).setUsePlainRandomAccess(Document.plainRandomAccess).createBestSource(filename)) {

    }

    /**
     * Creates an independent view of the specified source.  Closing the new object will not close the source.
     * Closing the source will have adverse effect on the behavior of the new view.
     * @param source the source for the new independent view
     */
    @Deprecated("")
    @Deprecated("use {@link RandomAccessFileOrArray#createView()} instead\n      ")
    constructor(source: RandomAccessFileOrArray) : this(IndependentRandomAccessSource(source.byteSource)) {
    }

    /**
     * Creates an independent view of this object (with it's own file pointer and pushback queue).  Closing the new object will not close this object.
     * Closing this object will have adverse effect on the view.
     * @return the new view
     */
    fun createView(): RandomAccessFileOrArray {
        return RandomAccessFileOrArray(IndependentRandomAccessSource(byteSource))
    }

    fun createSourceView(): RandomAccessSource {
        return IndependentRandomAccessSource(byteSource)
    }

    /**
     * Constructs a new RandomAccessFileOrArrayObject
     * @param filename the file to open (can be a file system file or one of the following url strings: file://, http://, https://, jar:, wsjar:, vfszip:
     * *
     * @param forceRead if true, the entire file will be read into memory
     * *
     * @param plainRandomAccess if true, a regular RandomAccessFile is used to access the file contents.  If false, a memory mapped file will be used, unless the file cannot be mapped into memory, in which case regular RandomAccessFile will be used
     * *
     * @throws IOException if there is a failure opening or reading the file
     * *
     */
    @Deprecated("")
    @Deprecated("use {@link RandomAccessSourceFactory#createBestSource(String)} and {@link RandomAccessFileOrArray#RandomAccessFileOrArray(RandomAccessSource)} instead")
    @Throws(IOException::class)
    constructor(filename: String, forceRead: Boolean, plainRandomAccess: Boolean) : this(RandomAccessSourceFactory().setForceRead(forceRead).setUsePlainRandomAccess(plainRandomAccess).createBestSource(filename)) {
    }

    /**
     * @param url
     * *
     * @throws IOException
     * *
     */
    @Deprecated("")
    @Deprecated("use {@link RandomAccessSourceFactory#createSource(URL)} and {@link RandomAccessFileOrArray#RandomAccessFileOrArray(RandomAccessSource)} instead")
    @Throws(IOException::class)
    constructor(url: URL) : this(RandomAccessSourceFactory().createSource(url)) {
    }

    /**
     * @param is
     * *
     * @throws IOException
     * *
     */
    @Deprecated("")
    @Deprecated("use {@link RandomAccessSourceFactory#createSource(InputStream)} and {@link RandomAccessFileOrArray#RandomAccessFileOrArray(RandomAccessSource)} instead")
    @Throws(IOException::class)
    constructor(`is`: InputStream) : this(RandomAccessSourceFactory().createSource(`is`)) {
    }


    /**
     * @param arrayIn byte[]
     * *
     * @throws IOException
     * *
     */
    @Deprecated("")
    @Deprecated("use {@link RandomAccessSourceFactory#createSource(byte[])} and {@link RandomAccessFileOrArray#RandomAccessFileOrArray(RandomAccessSource)} instead")
    constructor(arrayIn: ByteArray) : this(RandomAccessSourceFactory().createSource(arrayIn)) {
    }

    /**
     * Pushes a byte back.  The next get() will return this byte instead of the value from the underlying data source
     * @param b the byte to push
     */
    fun pushBack(b: Byte) {
        back = b
        isBack = true
    }

    /**
     * Reads a single byte
     * @return the byte, or -1 if EOF is reached
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun read(): Int {
        if (isBack) {
            isBack = false
            return back and 0xff
        }

        return byteSource[byteSourcePosition++]
    }

    @Throws(IOException::class)
    @JvmOverloads fun read(b: ByteArray, off: Int = 0, len: Int = b.size): Int {
        var off = off
        var len = len
        if (len == 0)
            return 0
        var count = 0
        if (isBack && len > 0) {
            isBack = false
            b[off++] = back
            --len
            count++
        }
        if (len > 0) {
            val byteSourceCount = byteSource[byteSourcePosition, b, off, len]
            if (byteSourceCount > 0) {
                count += byteSourceCount
                byteSourcePosition += byteSourceCount.toLong()
            }
        }
        if (count == 0)
            return -1
        return count
    }

    @Throws(IOException::class)
    override fun readFully(b: ByteArray) {
        readFully(b, 0, b.size)
    }

    @Throws(IOException::class)
    override fun readFully(b: ByteArray, off: Int, len: Int) {
        var n = 0
        do {
            val count = read(b, off + n, len - n)
            if (count < 0)
                throw EOFException()
            n += count
        } while (n < len)
    }

    @Throws(IOException::class)
    fun skip(n: Long): Long {
        var n = n
        if (n <= 0) {
            return 0
        }
        var adj = 0
        if (isBack) {
            isBack = false
            if (n == 1) {
                return 1
            } else {
                --n
                adj = 1
            }
        }
        val pos: Long
        val len: Long
        var newpos: Long

        pos = filePointer
        len = length()
        newpos = pos + n
        if (newpos > len) {
            newpos = len
        }
        seek(newpos)

        /* return the actual number of bytes skipped */
        return newpos - pos + adj
    }

    @Throws(IOException::class)
    override fun skipBytes(n: Int): Int {
        return skip(n.toLong()).toInt()
    }

    @Deprecated("")
    @Throws(IOException::class)
            //TODO: remove all references to this call, then remove this method
    fun reOpen() {
        seek(0)
    }


    @Throws(IOException::class)
    fun close() {
        isBack = false

        byteSource.close()
    }

    @Throws(IOException::class)
    fun length(): Long {
        return byteSource.length()
    }

    @Throws(IOException::class)
    fun seek(pos: Long) {
        byteSourcePosition = pos
        isBack = false
    }

    //TODO: consider changing method name to getPosition or something like that - might not be worth making a breaking change, though
    val filePointer: Long
        @Throws(IOException::class)
        get() = byteSourcePosition - if (isBack) 1 else 0

    @Throws(IOException::class)
    override fun readBoolean(): Boolean {
        val ch = this.read()
        if (ch < 0)
            throw EOFException()
        return ch != 0
    }

    @Throws(IOException::class)
    override fun readByte(): Byte {
        val ch = this.read()
        if (ch < 0)
            throw EOFException()
        return ch.toByte()
    }

    @Throws(IOException::class)
    override fun readUnsignedByte(): Int {
        val ch = this.read()
        if (ch < 0)
            throw EOFException()
        return ch
    }

    @Throws(IOException::class)
    override fun readShort(): Short {
        val ch1 = this.read()
        val ch2 = this.read()
        if (ch1 or ch2 < 0)
            throw EOFException()
        return ((ch1 shl 8) + ch2).toShort()
    }

    /**
     * Reads a signed 16-bit number from this stream in little-endian order.
     * The method reads two
     * bytes from this stream, starting at the current stream pointer.
     * If the two bytes read, in order, are
     * `b1` and `b2`, where each of the two values is
     * between `0` and `255`, inclusive, then the
     * result is equal to:
     *
     * (short)((b2 &lt;&lt; 8) | b1)
     *
     *
     *
     * This method blocks until the two bytes are read, the end of the
     * stream is detected, or an exception is thrown.

     * @return     the next two bytes of this stream, interpreted as a signed
     * *             16-bit number.
     * *
     * @exception  EOFException  if this stream reaches the end before reading
     * *               two bytes.
     * *
     * @exception  IOException   if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun readShortLE(): Short {
        val ch1 = this.read()
        val ch2 = this.read()
        if (ch1 or ch2 < 0)
            throw EOFException()
        return ((ch2 shl 8) + (ch1 shl 0)).toShort()
    }

    @Throws(IOException::class)
    override fun readUnsignedShort(): Int {
        val ch1 = this.read()
        val ch2 = this.read()
        if (ch1 or ch2 < 0)
            throw EOFException()
        return (ch1 shl 8) + ch2
    }

    /**
     * Reads an unsigned 16-bit number from this stream in little-endian order.
     * This method reads
     * two bytes from the stream, starting at the current stream pointer.
     * If the bytes read, in order, are
     * `b1` and `b2`, where
     * `0&nbsp;&lt;=&nbsp;b1, b2&nbsp;&lt;=&nbsp;255`,
     * then the result is equal to:
     *
     * (b2 &lt;&lt; 8) | b1
     *
     *
     *
     * This method blocks until the two bytes are read, the end of the
     * stream is detected, or an exception is thrown.

     * @return     the next two bytes of this stream, interpreted as an
     * *             unsigned 16-bit integer.
     * *
     * @exception  EOFException  if this stream reaches the end before reading
     * *               two bytes.
     * *
     * @exception  IOException   if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun readUnsignedShortLE(): Int {
        val ch1 = this.read()
        val ch2 = this.read()
        if (ch1 or ch2 < 0)
            throw EOFException()
        return (ch2 shl 8) + (ch1 shl 0)
    }

    @Throws(IOException::class)
    override fun readChar(): Char {
        val ch1 = this.read()
        val ch2 = this.read()
        if (ch1 or ch2 < 0)
            throw EOFException()
        return ((ch1 shl 8) + ch2).toChar()
    }

    /**
     * Reads a Unicode character from this stream in little-endian order.
     * This method reads two
     * bytes from the stream, starting at the current stream pointer.
     * If the bytes read, in order, are
     * `b1` and `b2`, where
     * `0&nbsp;&lt;=&nbsp;b1,&nbsp;b2&nbsp;&lt;=&nbsp;255`,
     * then the result is equal to:
     *
     * (char)((b2 &lt;&lt; 8) | b1)
     *
     *
     *
     * This method blocks until the two bytes are read, the end of the
     * stream is detected, or an exception is thrown.

     * @return     the next two bytes of this stream as a Unicode character.
     * *
     * @exception  EOFException  if this stream reaches the end before reading
     * *               two bytes.
     * *
     * @exception  IOException   if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun readCharLE(): Char {
        val ch1 = this.read()
        val ch2 = this.read()
        if (ch1 or ch2 < 0)
            throw EOFException()
        return ((ch2 shl 8) + (ch1 shl 0)).toChar()
    }

    @Throws(IOException::class)
    override fun readInt(): Int {
        val ch1 = this.read()
        val ch2 = this.read()
        val ch3 = this.read()
        val ch4 = this.read()
        if (ch1 or ch2 or ch3 or ch4 < 0)
            throw EOFException()
        return (ch1 shl 24) + (ch2 shl 16) + (ch3 shl 8) + ch4
    }

    /**
     * Reads a signed 32-bit integer from this stream in little-endian order.
     * This method reads 4
     * bytes from the stream, starting at the current stream pointer.
     * If the bytes read, in order, are `b1`,
     * `b2`, `b3`, and `b4`, where
     * `0&nbsp;&lt;=&nbsp;b1, b2, b3, b4&nbsp;&lt;=&nbsp;255`,
     * then the result is equal to:
     *
     * (b4 &lt;&lt; 24) | (b3 &lt;&lt; 16) + (b2 &lt;&lt; 8) + b1
     *
     *
     *
     * This method blocks until the four bytes are read, the end of the
     * stream is detected, or an exception is thrown.

     * @return     the next four bytes of this stream, interpreted as an
     * *             `int`.
     * *
     * @exception  EOFException  if this stream reaches the end before reading
     * *               four bytes.
     * *
     * @exception  IOException   if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun readIntLE(): Int {
        val ch1 = this.read()
        val ch2 = this.read()
        val ch3 = this.read()
        val ch4 = this.read()
        if (ch1 or ch2 or ch3 or ch4 < 0)
            throw EOFException()
        return (ch4 shl 24) + (ch3 shl 16) + (ch2 shl 8) + (ch1 shl 0)
    }

    /**
     * Reads an unsigned 32-bit integer from this stream. This method reads 4
     * bytes from the stream, starting at the current stream pointer.
     * If the bytes read, in order, are `b1`,
     * `b2`, `b3`, and `b4`, where
     * `0&nbsp;&lt;=&nbsp;b1, b2, b3, b4&nbsp;&lt;=&nbsp;255`,
     * then the result is equal to:
     *
     * (b1 &lt;&lt; 24) | (b2 &lt;&lt; 16) + (b3 &lt;&lt; 8) + b4
     *
     *
     *
     * This method blocks until the four bytes are read, the end of the
     * stream is detected, or an exception is thrown.

     * @return     the next four bytes of this stream, interpreted as a
     * *             `long`.
     * *
     * @exception  EOFException  if this stream reaches the end before reading
     * *               four bytes.
     * *
     * @exception  IOException   if an I/O error occurs.
     */
    @Throws(IOException::class)
    fun readUnsignedInt(): Long {
        val ch1 = this.read().toLong()
        val ch2 = this.read().toLong()
        val ch3 = this.read().toLong()
        val ch4 = this.read().toLong()
        if (ch1 or ch2 or ch3 or ch4 < 0)
            throw EOFException()
        return (ch1 shl 24) + (ch2 shl 16) + (ch3 shl 8) + (ch4 shl 0)
    }

    @Throws(IOException::class)
    fun readUnsignedIntLE(): Long {
        val ch1 = this.read().toLong()
        val ch2 = this.read().toLong()
        val ch3 = this.read().toLong()
        val ch4 = this.read().toLong()
        if (ch1 or ch2 or ch3 or ch4 < 0)
            throw EOFException()
        return (ch4 shl 24) + (ch3 shl 16) + (ch2 shl 8) + (ch1 shl 0)
    }

    @Throws(IOException::class)
    override fun readLong(): Long {
        return (readInt().toLong() shl 32) + (readInt() and 0xFFFFFFFFL)
    }

    @Throws(IOException::class)
    fun readLongLE(): Long {
        val i1 = readIntLE()
        val i2 = readIntLE()
        return (i2.toLong() shl 32) + (i1 and 0xFFFFFFFFL)
    }

    @Throws(IOException::class)
    override fun readFloat(): Float {
        return java.lang.Float.intBitsToFloat(readInt())
    }

    @Throws(IOException::class)
    fun readFloatLE(): Float {
        return java.lang.Float.intBitsToFloat(readIntLE())
    }

    @Throws(IOException::class)
    override fun readDouble(): Double {
        return java.lang.Double.longBitsToDouble(readLong())
    }

    @Throws(IOException::class)
    fun readDoubleLE(): Double {
        return java.lang.Double.longBitsToDouble(readLongLE())
    }

    @Throws(IOException::class)
    override fun readLine(): String? {
        val input = StringBuilder()
        var c = -1
        var eol = false

        while (!eol) {
            when (c = read()) {
                -1, '\n' -> eol = true
                '\r' -> {
                    eol = true
                    val cur = filePointer
                    if (read() != '\n') {
                        seek(cur)
                    }
                }
                else -> input.append(c.toChar())
            }
        }

        if (c == -1 && input.length == 0) {
            return null
        }
        return input.toString()
    }

    @Throws(IOException::class)
    override fun readUTF(): String {
        return DataInputStream.readUTF(this)
    }

    /** Reads a String from the font file as bytes using the given
     * encoding.
     * @param length the length of bytes to read
     * *
     * @param encoding the given encoding
     * *
     * @return the String read
     * *
     * @throws IOException the font file could not be read
     */
    @Throws(IOException::class)
    fun readString(length: Int, encoding: String): String {
        val buf = ByteArray(length)
        readFully(buf)
        try {
            return String(buf, encoding)
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

}
