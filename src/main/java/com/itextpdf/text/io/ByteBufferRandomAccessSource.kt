/*
 * $Id: ddff03ec8021a68baa81b0074e63e4c8488171d9 $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Kevin Day, Bruno Lowagie, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General License for more
 * details. You should have received a copy of the GNU Affero General License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General License.
 *
 * In accordance with Section 7(b) of the GNU Affero General License, a covered
 * work must retain the producer line in every PDF that is created or
 * manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing a
 * commercial license. Buying such a license is mandatory as soon as you develop
 * commercial activities involving the iText software without disclosing the
 * source code of your own applications. These activities include: offering paid
 * services to customers as an ASP, serving PDFs on the fly in a web
 * application, shipping iText with a closed source product.
 *
 * For more information, please contact iText Software Corp. at this address:
 * sales@itextpdf.com
 */
package com.itextpdf.text.io

import java.io.IOException
import java.lang.reflect.Method
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.security.AccessController
import java.security.PrivilegedAction

/**
 * A RandomAccessSource that is based on an underlying [ByteBuffer].  This class takes steps to ensure that the byte buffer
 * is completely freed from memory during [ByteBufferRandomAccessSource.close]
 * @since 5.3.5
 */
internal class ByteBufferRandomAccessSource
/**
 * Constructs a new [ByteBufferRandomAccessSource] based on the specified ByteBuffer
 * @param byteBuffer the buffer to use as the backing store
 */
(
        /**
         * Internal cache of memory mapped buffers
         */
        private val byteBuffer: ByteBuffer) : RandomAccessSource {

    /**
     * {@inheritDoc}
     *
     *
     * Note: Because ByteBuffers don't support long indexing, the position must be a valid positive int
     * @param position the position to read the byte from - must be less than Integer.MAX_VALUE
     */
    @Throws(IOException::class)
    override fun get(position: Long): Int {
        if (position > Integer.MAX_VALUE)
            throw IllegalArgumentException("Position must be less than Integer.MAX_VALUE")

        try {

            if (position >= byteBuffer.limit())
                return -1

            val b = byteBuffer.get(position.toInt())

            val n = b and 0xff

            return n
        } catch (e: BufferUnderflowException) {
            return -1 // EOF
        }

    }

    /**
     * {@inheritDoc}
     *
     *
     * Note: Because ByteBuffers don't support long indexing, the position must be a valid positive int
     * @param position the position to read the byte from - must be less than Integer.MAX_VALUE
     */
    @Throws(IOException::class)
    override fun get(position: Long, bytes: ByteArray, off: Int, len: Int): Int {
        if (position > Integer.MAX_VALUE)
            throw IllegalArgumentException("Position must be less than Integer.MAX_VALUE")

        if (position >= byteBuffer.limit())
            return -1

        byteBuffer.position(position.toInt())
        val bytesFromThisBuffer = Math.min(len, byteBuffer.remaining())
        byteBuffer.get(bytes, off, bytesFromThisBuffer)

        return bytesFromThisBuffer

    }


    /**
     * {@inheritDoc}
     */
    override fun length(): Long {
        return byteBuffer.limit().toLong()
    }

    /**
     * @see java.io.RandomAccessFile.close
     */
    @Throws(IOException::class)
    override fun close() {
        clean(byteBuffer)
    }

    /**
     * invokes the clean method on the ByteBuffer's cleaner
     * @param buffer ByteBuffer
     * *
     * @return boolean true on success
     */
    private fun clean(buffer: java.nio.ByteBuffer?): Boolean {
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
