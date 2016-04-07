/*
 * $Id: ecb99601d11c00974568c79965777e6b606c0e19 $
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

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.reflect.Method
import java.nio.BufferUnderflowException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.security.AccessController
import java.security.PrivilegedAction

/**
 * A [java.nio.MappedByteBuffer] wrapped as a [java.io.RandomAccessFile]

 * @author Joakim Sandstroem
 * * Created on 6.9.2006
 */
class MappedRandomAccessFile
/**
 * Constructs a new MappedRandomAccessFile instance
 * @param filename String
 * *
 * @param mode String r, w or rw
 * *
 * @throws FileNotFoundException
 * *
 * @throws IOException
 */
@Throws(FileNotFoundException::class, IOException::class)
constructor(filename: String, mode: String) {

    /**
     * @since 2.0.8
     */
    var channel: FileChannel? = null
        private set
    private var mappedBuffers: Array<MappedByteBuffer>? = null
    private var size: Long = 0
    /**
     * @see java.io.RandomAccessFile.getFilePointer
     * @return long
     */
    var filePointer: Long = 0
        private set

    init {

        if (mode == "rw")
            init(
                    java.io.RandomAccessFile(filename, mode).channel,
                    FileChannel.MapMode.READ_WRITE)
        else
            init(
                    FileInputStream(filename).channel,
                    FileChannel.MapMode.READ_ONLY)

    }

    /**
     * initializes the channel and mapped bytebuffer
     * @param channel FileChannel
     * *
     * @param mapMode FileChannel.MapMode
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun init(channel: FileChannel, mapMode: FileChannel.MapMode) {

        this.channel = channel


        size = channel.size()
        filePointer = 0
        val requiredBuffers = (size / BUFSIZE).toInt() + if (size % BUFSIZE == 0) 0 else 1
        //System.out.println("This will require " + requiredBuffers + " buffers");

        mappedBuffers = arrayOfNulls<MappedByteBuffer>(requiredBuffers)
        try {
            var index = 0
            var offset: Long = 0
            while (offset < size) {
                val size2 = Math.min(size - offset, BUFSIZE.toLong())
                mappedBuffers[index] = channel.map(mapMode, offset, size2)
                mappedBuffers!![index].load()
                index++
                offset += BUFSIZE.toLong()
            }
            if (index != requiredBuffers) {
                throw Error("Should never happen - $index != $requiredBuffers")
            }
        } catch (e: IOException) {
            close()
            throw e
        } catch (e: RuntimeException) {
            close()
            throw e
        }

    }

    /**
     * @see java.io.RandomAccessFile.read
     * @return int next integer or -1 on EOF
     */
    fun read(): Int {
        try {
            val mapN = (filePointer / BUFSIZE).toInt()
            val offN = (filePointer % BUFSIZE).toInt()

            if (mapN >= mappedBuffers!!.size)
            // we have run out of data to read from
                return -1

            if (offN >= mappedBuffers!![mapN].limit())
                return -1

            val b = mappedBuffers!![mapN].get(offN)
            filePointer++
            val n = b and 0xff

            return n
        } catch (e: BufferUnderflowException) {
            return -1 // EOF
        }

    }

    /**
     * @see java.io.RandomAccessFile.read
     * @param bytes byte[]
     * *
     * @param off int offset
     * *
     * @param len int length
     * *
     * @return int bytes read or -1 on EOF
     */
    fun read(bytes: ByteArray, off: Int, len: Int): Int {
        var off = off
        var mapN = (filePointer / BUFSIZE).toInt()
        var offN = (filePointer % BUFSIZE).toInt()
        var totalRead = 0

        while (totalRead < len) {
            if (mapN >= mappedBuffers!!.size)
            // we have run out of data to read from
                break
            val currentBuffer = mappedBuffers!![mapN]
            if (offN > currentBuffer.limit())
                break
            currentBuffer.position(offN)
            val bytesFromThisBuffer = Math.min(len - totalRead, currentBuffer.remaining())
            currentBuffer.get(bytes, off, bytesFromThisBuffer)
            off += bytesFromThisBuffer
            filePointer += bytesFromThisBuffer.toLong()
            totalRead += bytesFromThisBuffer

            mapN++
            offN = 0

        }
        return if (totalRead == 0) -1 else totalRead
    }

    /**
     * @see java.io.RandomAccessFile.seek
     * @param pos long position
     */
    fun seek(pos: Long) {
        this.filePointer = pos
    }

    /**
     * @see java.io.RandomAccessFile.length
     * @return long length
     */
    fun length(): Long {
        return size
    }

    /**
     * @see java.io.RandomAccessFile.close
     */
    @Throws(IOException::class)
    fun close() {
        for (i in mappedBuffers!!.indices) {
            if (mappedBuffers!![i] != null) {
                clean(mappedBuffers!![i])
                mappedBuffers[i] = null
            }
        }

        if (channel != null)
            channel!!.close()
        channel = null
    }

    /**
     * invokes the close method
     * @see java.lang.Object.finalize
     */
    @Throws(Throwable::class)
    protected fun finalize() {
        close()
        super.finalize()
    }

    companion object {

        private val BUFSIZE = 1 shl 30

        /**
         * invokes the clean method on the ByteBuffer's cleaner
         * @param buffer ByteBuffer
         * *
         * @return boolean true on success
         */
        fun clean(buffer: java.nio.ByteBuffer?): Boolean {
            if (buffer == null || !buffer.isDirect)
                return false

            return AccessController.doPrivileged(PrivilegedAction {
                var success = java.lang.Boolean.FALSE
                try {
                    val getCleanerMethod = buffer.javaClass.getMethod("cleaner", *null as Array<Class<*>>)
                    getCleanerMethod.isAccessible = true
                    val cleaner = getCleanerMethod.invoke(buffer, *null as Array<Any>)
                    val clean = cleaner.javaClass.getMethod("clean", *null as Array<Class<*>>)
                    clean.invoke(cleaner, *null as Array<Any>)
                    success = java.lang.Boolean.TRUE
                } catch (e: Exception) {
                    // This really is a show stopper on windows
                    //e.printStackTrace();
                }

                success
            }).booleanValue()
        }
    }

}
