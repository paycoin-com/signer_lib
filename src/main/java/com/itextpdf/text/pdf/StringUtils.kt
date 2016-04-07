/*
 * $Id: 796bcdef224c546d4ae793c199ebbc18eccf82fe $
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

import com.itextpdf.text.DocWriter

object StringUtils {

    private val r = DocWriter.getISOBytes("\\r")
    private val n = DocWriter.getISOBytes("\\n")
    private val t = DocWriter.getISOBytes("\\t")
    private val b = DocWriter.getISOBytes("\\b")
    private val f = DocWriter.getISOBytes("\\f")

    /**
     * Escapes a byte array according to the PDF conventions.

     * @param bytes the byte array to escape
     * *
     * @return an escaped byte array
     */
    fun escapeString(bytes: ByteArray): ByteArray {
        val content = ByteBuffer()
        escapeString(bytes, content)
        return content.toByteArray()
    }

    /**
     * Escapes a byte array according to the PDF conventions.

     * @param bytes the byte array to escape
     * *
     * @param content the content
     */
    fun escapeString(bytes: ByteArray, content: ByteBuffer) {
        content.append_i('(')
        for (k in bytes.indices) {
            val c = bytes[k]
            when (c) {
                '\r' -> content.append(r)
                '\n' -> content.append(n)
                '\t' -> content.append(t)
                '\b' -> content.append(b)
                '\f' -> content.append(f)
                '(', ')', '\\' -> content.append_i('\\').append_i(c.toInt())
                else -> content.append_i(c.toInt())
            }
        }
        content.append_i(')')
    }


    /**
     * Converts an array of unsigned 16bit numbers to an array of bytes.
     * The input values are presented as chars for convenience.

     * @param chars the array of 16bit numbers that should be converted
     * *
     * @return the resulting byte array, twice as large as the input
     */
    fun convertCharsToBytes(chars: CharArray): ByteArray {
        val result = ByteArray(chars.size * 2)
        for (i in chars.indices) {
            result[2 * i] = (chars[i].toInt() / 256).toByte()
            result[2 * i + 1] = (chars[i].toInt() % 256).toByte()
        }
        return result
    }
}
