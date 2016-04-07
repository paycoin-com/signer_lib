/*
 * $Id: 0673b6647b0f7ad59edda8e1156c81424c725015 $
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
package com.itextpdf.text.pdf.codec.wmf

import com.itextpdf.text.BaseColor
import java.io.IOException
import java.io.InputStream

import com.itextpdf.text.Utilities

class InputMeta(internal var `in`:

                InputStream) {
    var length: Int = 0
        internal set

    @Throws(IOException::class)
    fun readWord(): Int {
        length += 2
        val k1 = `in`.read()
        if (k1 < 0)
            return 0
        return k1 + (`in`.read() shl 8) and 0xffff
    }

    @Throws(IOException::class)
    fun readShort(): Int {
        var k = readWord()
        if (k > 0x7fff)
            k -= 0x10000
        return k
    }

    @Throws(IOException::class)
    fun readInt(): Int {
        length += 4
        val k1 = `in`.read()
        if (k1 < 0)
            return 0
        val k2 = `in`.read() shl 8
        val k3 = `in`.read() shl 16
        return k1 + k2 + k3 + (`in`.read() shl 24)
    }

    @Throws(IOException::class)
    fun readByte(): Int {
        ++length
        return `in`.read() and 0xff
    }

    @Throws(IOException::class)
    fun skip(len: Int) {
        length += len
        Utilities.skip(`in`, len)
    }

    @Throws(IOException::class)
    fun readColor(): BaseColor {
        val red = readByte()
        val green = readByte()
        val blue = readByte()
        readByte()
        return BaseColor(red, green, blue)
    }
}
