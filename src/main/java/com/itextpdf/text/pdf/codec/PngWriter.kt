/*
 * $Id: 396a766f4d71a3e598c0428e6316527475df2a01 $
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

package com.itextpdf.text.pdf.codec

import com.itextpdf.text.DocWriter
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.zip.DeflaterOutputStream

/**
 * Writes a PNG image.

 * @author  Paulo Soares
 * *
 * @since 5.0.3
 */
class PngWriter @Throws(IOException::class)
constructor(private val outp: OutputStream) {

    init {
        outp.write(PNG_SIGNTURE)
    }

    @Throws(IOException::class)
    fun writeHeader(width: Int, height: Int, bitDepth: Int, colorType: Int) {
        val ms = ByteArrayOutputStream()
        outputInt(width, ms)
        outputInt(height, ms)
        ms.write(bitDepth)
        ms.write(colorType)
        ms.write(0)
        ms.write(0)
        ms.write(0)
        writeChunk(IHDR, ms.toByteArray())
    }

    @Throws(IOException::class)
    fun writeEnd() {
        writeChunk(IEND, ByteArray(0))
    }

    @Throws(IOException::class)
    fun writeData(data: ByteArray, stride: Int) {
        val stream = ByteArrayOutputStream()
        val zip = DeflaterOutputStream(stream)
        var k: Int
        k = 0
        while (k < data.size - stride) {
            zip.write(0)
            zip.write(data, k, stride)
            k += stride
        }
        val remaining = data.size - k
        if (remaining > 0) {
            zip.write(0)
            zip.write(data, k, remaining)
        }
        zip.close()
        writeChunk(IDAT, stream.toByteArray())
    }

    @Throws(IOException::class)
    fun writePalette(data: ByteArray) {
        writeChunk(PLTE, data)
    }

    @Throws(IOException::class)
    fun writeIccProfile(data: ByteArray) {
        val stream = ByteArrayOutputStream()
        stream.write('I'.toByte().toInt())
        stream.write('C'.toByte().toInt())
        stream.write('C'.toByte().toInt())
        stream.write(0)
        stream.write(0)
        val zip = DeflaterOutputStream(stream)
        zip.write(data)
        zip.close()
        writeChunk(iCCP, stream.toByteArray())
    }

    @Throws(IOException::class)
    fun outputInt(n: Int) {
        outputInt(n, outp)
    }

    @Throws(IOException::class)
    fun writeChunk(chunkType: ByteArray, data: ByteArray) {
        outputInt(data.size)
        outp.write(chunkType, 0, 4)
        outp.write(data)
        var c = update_crc(0xffffffff.toInt(), chunkType, 0, chunkType.size)
        c = update_crc(c, data, 0, data.size) xor 0xffffffff.toInt()
        outputInt(c)
    }

    companion object {
        private val PNG_SIGNTURE = byteArrayOf(137.toByte(), 80, 78, 71, 13, 10, 26, 10)

        private val IHDR = DocWriter.getISOBytes("IHDR")
        private val PLTE = DocWriter.getISOBytes("PLTE")
        private val IDAT = DocWriter.getISOBytes("IDAT")
        private val IEND = DocWriter.getISOBytes("IEND")
        private val iCCP = DocWriter.getISOBytes("iCCP")

        private var crc_table: IntArray? = null

        private fun make_crc_table() {
            if (crc_table != null)
                return
            val crc2 = IntArray(256)
            for (n in 0..255) {
                var c = n
                for (k in 0..7) {
                    if (c and 1 != 0)
                        c = 0xedb88320.toInt() xor c.ushr(1)
                    else
                        c = c.ushr(1)
                }
                crc2[n] = c
            }
            crc_table = crc2
        }

        private fun update_crc(crc: Int, buf: ByteArray, offset: Int, len: Int): Int {
            var c = crc

            if (crc_table == null)
                make_crc_table()
            for (n in 0..len - 1) {
                c = crc_table!![c xor buf[n + offset] and 0xff] xor c.ushr(8)
            }
            return c
        }

        private fun crc(buf: ByteArray, offset: Int, len: Int): Int {
            return update_crc(0xffffffff.toInt(), buf, offset, len) xor 0xffffffff.toInt()
        }

        private fun crc(buf: ByteArray): Int {
            return update_crc(0xffffffff.toInt(), buf, 0, buf.size) xor 0xffffffff.toInt()
        }

        @Throws(IOException::class)
        fun outputInt(n: Int, s: OutputStream) {
            s.write((n shr 24).toByte().toInt())
            s.write((n shr 16).toByte().toInt())
            s.write((n shr 8).toByte().toInt())
            s.write(n.toByte().toInt())
        }
    }
}
