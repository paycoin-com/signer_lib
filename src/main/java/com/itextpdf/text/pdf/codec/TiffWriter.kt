/*
 * $Id: 37bdbb7094d881c6da709a67364e06c563c56734 $
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

import java.io.OutputStream
import java.io.IOException
import java.util.TreeMap

/**
 * Exports images as TIFF.
 * @since 5.0.3
 */
class TiffWriter {
    private val ifd = TreeMap<Int, FieldBase>()

    fun addField(field: FieldBase) {
        ifd.put(Integer.valueOf(field.tag), field)
    }

    val ifdSize: Int
        get() = 6 + ifd.size * 12

    @Throws(IOException::class)
    fun writeFile(stream: OutputStream) {
        stream.write(0x4d)
        stream.write(0x4d)
        stream.write(0)
        stream.write(42)
        writeLong(8, stream)
        writeShort(ifd.size, stream)
        var offset = 8 + ifdSize
        for (field in ifd.values) {
            val size = field.valueSize
            if (size > 4) {
                field.setOffset(offset)
                offset += size
            }
            field.writeField(stream)
        }
        writeLong(0, stream)
        for (field in ifd.values) {
            field.writeValue(stream)
        }
    }

    /**
     * Inner class class containing information about a field.
     * @since 5.0.3
     */
    abstract class FieldBase protected constructor(val tag: Int, private val fieldType: Int, private val count: Int) {
        protected var data: ByteArray
        private var offset: Int = 0

        val valueSize: Int
            get() = data.size + 1 and 0xfffffffe.toInt()

        fun setOffset(offset: Int) {
            this.offset = offset
        }

        @Throws(IOException::class)
        fun writeField(stream: OutputStream) {
            writeShort(tag, stream)
            writeShort(fieldType, stream)
            writeLong(count, stream)
            if (data.size <= 4) {
                stream.write(data)
                for (k in data.size..3) {
                    stream.write(0)
                }
            } else {
                writeLong(offset, stream)
            }
        }

        @Throws(IOException::class)
        fun writeValue(stream: OutputStream) {
            if (data.size <= 4)
                return
            stream.write(data)
            if (data.size and 1 == 1)
                stream.write(0)
        }
    }

    /**
     * Inner class containing info about a field.
     * @since 5.0.3
     */
    class FieldShort : FieldBase {
        constructor(tag: Int, value: Int) : super(tag, 3, 1) {
            data = ByteArray(2)
            data[0] = (value shr 8).toByte()
            data[1] = value.toByte()
        }

        constructor(tag: Int, values: IntArray) : super(tag, 3, values.size) {
            data = ByteArray(values.size * 2)
            var ptr = 0
            for (value in values) {
                data[ptr++] = (value shr 8).toByte()
                data[ptr++] = value.toByte()
            }
        }
    }

    /**
     * Inner class containing info about a field.
     * @since 5.0.3
     */
    class FieldLong : FieldBase {
        constructor(tag: Int, value: Int) : super(tag, 4, 1) {
            data = ByteArray(4)
            data[0] = (value shr 24).toByte()
            data[1] = (value shr 16).toByte()
            data[2] = (value shr 8).toByte()
            data[3] = value.toByte()
        }

        constructor(tag: Int, values: IntArray) : super(tag, 4, values.size) {
            data = ByteArray(values.size * 4)
            var ptr = 0
            for (value in values) {
                data[ptr++] = (value shr 24).toByte()
                data[ptr++] = (value shr 16).toByte()
                data[ptr++] = (value shr 8).toByte()
                data[ptr++] = value.toByte()
            }
        }
    }

    /**
     * Inner class containing info about a field.
     * @since 5.0.3
     */
    class FieldRational(tag: Int, values: Array<IntArray>) : FieldBase(tag, 5, values.size) {
        constructor(tag: Int, value: IntArray) : this(tag, arrayOf(value)) {
        }

        init {
            data = ByteArray(values.size * 8)
            var ptr = 0
            for (value in values) {
                data[ptr++] = (value[0] shr 24).toByte()
                data[ptr++] = (value[0] shr 16).toByte()
                data[ptr++] = (value[0] shr 8).toByte()
                data[ptr++] = value[0].toByte()
                data[ptr++] = (value[1] shr 24).toByte()
                data[ptr++] = (value[1] shr 16).toByte()
                data[ptr++] = (value[1] shr 8).toByte()
                data[ptr++] = value[1].toByte()
            }
        }
    }

    /**
     * Inner class containing info about a field.
     * @since 5.0.3
     */
    class FieldByte(tag: Int, values: ByteArray) : FieldBase(tag, 1, values.size) {
        init {
            data = values
        }
    }

    /**
     * Inner class containing info about a field.
     * @since 5.0.3
     */
    class FieldUndefined(tag: Int, values: ByteArray) : FieldBase(tag, 7, values.size) {
        init {
            data = values
        }
    }

    /**
     * Inner class containing info about a field.
     * @since 5.0.3
     */
    class FieldImage(values: ByteArray) : FieldBase(TIFFConstants.TIFFTAG_STRIPOFFSETS, 4, 1) {
        init {
            data = values
        }
    }

    /**
     * Inner class containing info about an ASCII field.
     * @since 5.0.3
     */
    class FieldAscii(tag: Int, values: String) : FieldBase(tag, 2, values.toByteArray().size + 1) {
        init {
            val b = values.toByteArray()
            data = ByteArray(b.size + 1)
            System.arraycopy(b, 0, data, 0, b.size)
        }
    }

    companion object {

        @Throws(IOException::class)
        fun writeShort(v: Int, stream: OutputStream) {
            stream.write(v shr 8 and 0xff)
            stream.write(v and 0xff)
        }

        @Throws(IOException::class)
        fun writeLong(v: Int, stream: OutputStream) {
            stream.write(v shr 24 and 0xff)
            stream.write(v shr 16 and 0xff)
            stream.write(v shr 8 and 0xff)
            stream.write(v and 0xff)
        }

        @Throws(IOException::class)
        fun compressLZW(stream: OutputStream, predictor: Int, b: ByteArray, height: Int, samplesPerPixel: Int, stride: Int) {

            val lzwCompressor = LZWCompressor(stream, 8, true)
            val usePredictor = predictor == TIFFConstants.PREDICTOR_HORIZONTAL_DIFFERENCING

            if (!usePredictor) {
                lzwCompressor.compress(b, 0, b.size)
            } else {
                var off = 0
                val rowBuf = if (usePredictor) ByteArray(stride) else null
                for (i in 0..height - 1) {
                    System.arraycopy(b, off, rowBuf, 0, stride)
                    for (j in stride - 1 downTo samplesPerPixel) {
                        rowBuf[j] -= rowBuf!![j - samplesPerPixel]
                    }
                    lzwCompressor.compress(rowBuf, 0, stride)
                    off += stride
                }
            }

            lzwCompressor.flush()
        }
    }
}
