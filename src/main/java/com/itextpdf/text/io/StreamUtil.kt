/*
 * $Id: 4d7369764da9380f26b6e8ad31b8d7a4c7a719af $
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

import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Utility class with commonly used stream operations
 * @since 5.3.5
 */
object StreamUtil {

    /**
     * Reads the full content of a stream and returns them in a byte array
     * @param is the stream to read
     * *
     * @return a byte array containing all of the bytes from the stream
     * *
     * @throws IOException if there is a problem reading from the input stream
     */
    @Throws(IOException::class)
    fun inputStreamToArray(`is`: InputStream): ByteArray {
        val b = ByteArray(8192)
        val out = ByteArrayOutputStream()
        while (true) {
            val read = `is`.read(b)
            if (read < 1)
                break
            out.write(b, 0, read)
        }
        out.close()
        return out.toByteArray()
    }

    @Throws(IOException::class)
    fun CopyBytes(source: RandomAccessSource, start: Long, length: Long, outs: OutputStream) {
        var length = length
        if (length <= 0)
            return
        var idx = start
        val buf = ByteArray(8192)
        while (length > 0) {
            val n = source.get(idx, buf, 0, Math.min(buf.size.toLong(), length).toInt()).toLong()
            if (n <= 0)
                throw EOFException()
            outs.write(buf, 0, n.toInt())
            idx += n
            length -= n
        }
    }

    /**
     * Gets the resource's inputstream
     * .
     * @param key the full name of the resource
     * *
     * @param loader the ClassLoader to load the resource or null to try the ones available
     * *
     * @return the InputStream to get the resource or
     * * null if not found
     */
    @JvmOverloads fun getResourceStream(key: String, loader: ClassLoader? = null): InputStream {
        var key = key
        if (key.startsWith("/"))
            key = key.substring(1)
        var `is`: InputStream? = null
        if (loader != null) {
            `is` = loader.getResourceAsStream(key)
            if (`is` != null)
                return `is`
        }
        // Try to use Context Class Loader to load the properties file.
        try {
            val contextClassLoader = Thread.currentThread().contextClassLoader
            if (contextClassLoader != null) {
                `is` = contextClassLoader.getResourceAsStream(key)
            }
        } catch (e: Throwable) {
            // empty body
        }

        if (`is` == null) {
            `is` = StreamUtil::class.java.getResourceAsStream("/" + key)
        }
        if (`is` == null) {
            `is` = ClassLoader.getSystemResourceAsStream(key)
        }
        return `is`
    }
}
/**
 * Gets the resource's inputstream.
 * @param key the full name of the resource
 * *
 * @return the InputStream to get the resource or
 * * null if not found
 */
