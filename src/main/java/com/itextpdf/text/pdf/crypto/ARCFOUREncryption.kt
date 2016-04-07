/*
 * $Id: 38e7ba50352f203f1d33b249e2dde10111361649 $
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
package com.itextpdf.text.pdf.crypto

class ARCFOUREncryption {
    private val state = ByteArray(256)
    private var x: Int = 0
    private var y: Int = 0

    @JvmOverloads fun prepareARCFOURKey(key: ByteArray, off: Int = 0, len: Int = key.size) {
        var index1 = 0
        var index2 = 0
        for (k in 0..255)
            state[k] = k.toByte()
        x = 0
        y = 0
        var tmp: Byte
        for (k in 0..255) {
            index2 = key[index1 + off].toInt() + state[k].toInt() + index2 and 255
            tmp = state[k]
            state[k] = state[index2]
            state[index2] = tmp
            index1 = (index1 + 1) % len
        }
    }

    fun encryptARCFOUR(dataIn: ByteArray, off: Int, len: Int, dataOut: ByteArray, offOut: Int) {
        val length = len + off
        var tmp: Byte
        for (k in off..length - 1) {
            x = x + 1 and 255
            y = state[x] + y and 255
            tmp = state[x]
            state[x] = state[y]
            state[y] = tmp
            dataOut[k - off + offOut] = (dataIn[k] xor state[state[x] + state[y] and 255]).toByte()
        }
    }

    fun encryptARCFOUR(data: ByteArray, off: Int, len: Int) {
        encryptARCFOUR(data, off, len, data, off)
    }

    fun encryptARCFOUR(dataIn: ByteArray, dataOut: ByteArray) {
        encryptARCFOUR(dataIn, 0, dataIn.size, dataOut, 0)
    }

    fun encryptARCFOUR(data: ByteArray) {
        encryptARCFOUR(data, 0, data.size, data, 0)
    }
}
/** Creates a new instance of ARCFOUREncryption  */
