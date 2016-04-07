/*
 * $Id: e5603f990a1b38fe5c55e10f9f6587532bcb818c $
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

import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.ArrayList

import com.itextpdf.text.error_messages.MessageLocalization

/**
 * An Jpeg2000 is the representation of a graphic element (JPEG)
 * that has to be inserted into the document

 * @see Element

 * @see Image
 */

class Jpeg2000 : Image {

    internal var inp: InputStream? = null
    internal var boxLength: Int = 0
    internal var boxType: Int = 0
    var numOfComps: Int = 0
        internal set
    var colorSpecBoxes: ArrayList<ColorSpecBox>? = null
        internal set
    /**
     * @return `true` if the image is JP2, `false` if a codestream.
     */
    var isJp2 = false
        internal set
    var bpcBoxData: ByteArray? = null
        internal set

    // Constructors

    internal constructor(image: Image) : super(image) {
        if (image is Jpeg2000) {
            numOfComps = image.numOfComps
            if (colorSpecBoxes != null)
                colorSpecBoxes = image.colorSpecBoxes!!.clone() as ArrayList<ColorSpecBox>
            isJp2 = image.isJp2
            if (bpcBoxData != null)
                bpcBoxData = image.bpcBoxData!!.clone()

        }
    }

    /**
     * Constructs a Jpeg2000-object, using an url.

     * @param        url            the URL where the image can be found
     * *
     * @throws BadElementException
     * *
     * @throws IOException
     */
    @Throws(BadElementException::class, IOException::class)
    constructor(url: URL) : super(url) {
        processParameters()
    }

    /**
     * Constructs a Jpeg2000-object from memory.

     * @param        img        the memory image
     * *
     * @throws BadElementException
     * *
     * @throws IOException
     */

    @Throws(BadElementException::class, IOException::class)
    constructor(img: ByteArray) : super(null as URL) {
        rawData = img
        originalData = img
        processParameters()
    }

    /**
     * Constructs a Jpeg2000-object from memory.

     * @param        img            the memory image.
     * *
     * @param        width        the width you want the image to have
     * *
     * @param        height        the height you want the image to have
     * *
     * @throws BadElementException
     * *
     * @throws IOException
     */

    @Throws(BadElementException::class, IOException::class)
    constructor(img: ByteArray, width: Float, height: Float) : this(img) {
        scaledWidth = width
        scaledHeight = height
    }

    @Throws(IOException::class)
    private fun cio_read(n: Int): Int {
        var v = 0
        for (i in n - 1 downTo 0) {
            v += inp!!.read() shl (i shl 3)
        }
        return v
    }

    @Throws(IOException::class)
    fun jp2_read_boxhdr() {
        boxLength = cio_read(4)
        boxType = cio_read(4)
        if (boxLength == 1) {
            if (cio_read(4) != 0) {
                throw IOException(MessageLocalization.getComposedMessage("cannot.handle.box.sizes.higher.than.2.32"))
            }
            boxLength = cio_read(4)
            if (boxLength == 0)
                throw IOException(MessageLocalization.getComposedMessage("unsupported.box.size.eq.eq.0"))
        } else if (boxLength == 0) {
            throw ZeroBoxSizeException(MessageLocalization.getComposedMessage("unsupported.box.size.eq.eq.0"))
        }
    }

    /**
     * This method checks if the image is a valid JPEG and processes some parameters.
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processParameters() {
        type = Element.JPEG2000
        originalType = Image.ORIGINAL_JPEG2000
        inp = null
        try {
            if (rawData == null) {
                inp = url.openStream()
            } else {
                inp = java.io.ByteArrayInputStream(rawData)
            }
            boxLength = cio_read(4)
            if (boxLength == 0x0000000c) {
                isJp2 = true
                boxType = cio_read(4)
                if (JP2_JP != boxType) {
                    throw IOException(MessageLocalization.getComposedMessage("expected.jp.marker"))
                }
                if (0x0d0a870a != cio_read(4)) {
                    throw IOException(MessageLocalization.getComposedMessage("error.with.jp.marker"))
                }

                jp2_read_boxhdr()
                if (JP2_FTYP != boxType) {
                    throw IOException(MessageLocalization.getComposedMessage("expected.ftyp.marker"))
                }
                Utilities.skip(inp, boxLength - 8)
                jp2_read_boxhdr()
                do {
                    if (JP2_JP2H != boxType) {
                        if (boxType == JP2_JP2C) {
                            throw IOException(MessageLocalization.getComposedMessage("expected.jp2h.marker"))
                        }
                        Utilities.skip(inp, boxLength - 8)
                        jp2_read_boxhdr()
                    }
                } while (JP2_JP2H != boxType)
                jp2_read_boxhdr()
                if (JP2_IHDR != boxType) {
                    throw IOException(MessageLocalization.getComposedMessage("expected.ihdr.marker"))
                }
                scaledHeight = cio_read(4).toFloat()
                top = scaledHeight
                scaledWidth = cio_read(4).toFloat()
                right = scaledWidth
                numOfComps = cio_read(2)
                bpc = -1
                bpc = cio_read(1)

                Utilities.skip(inp, 3)

                jp2_read_boxhdr()
                if (boxType == JP2_BPCC) {
                    bpcBoxData = ByteArray(boxLength - 8)
                    inp!!.read(bpcBoxData, 0, boxLength - 8)
                } else if (boxType == JP2_COLR) {
                    do {
                        if (colorSpecBoxes == null)
                            colorSpecBoxes = ArrayList<ColorSpecBox>()
                        colorSpecBoxes!!.add(jp2_read_colr())
                        try {
                            jp2_read_boxhdr()
                        } catch (ioe: ZeroBoxSizeException) {
                            //Probably we have reached the contiguous codestream box which is the last in jpeg2000 and has no length.
                        }

                    } while (JP2_COLR == boxType)
                }
            } else if (boxLength == 0xff4fff51.toInt()) {
                Utilities.skip(inp, 4)
                val x1 = cio_read(4)
                val y1 = cio_read(4)
                val x0 = cio_read(4)
                val y0 = cio_read(4)
                Utilities.skip(inp, 16)
                colorspace = cio_read(2)
                bpc = 8
                scaledHeight = (y1 - y0).toFloat()
                top = scaledHeight
                scaledWidth = (x1 - x0).toFloat()
                right = scaledWidth
            } else {
                throw IOException(MessageLocalization.getComposedMessage("not.a.valid.jpeg2000.file"))
            }
        } finally {
            if (inp != null) {
                try {
                    inp!!.close()
                } catch (e: Exception) {
                }

                inp = null
            }
        }
        plainWidth = width
        plainHeight = height
    }

    @Throws(IOException::class)
    private fun jp2_read_colr(): ColorSpecBox {
        var readBytes = 8
        val colr = ColorSpecBox()
        for (i in 0..2) {
            colr.add(cio_read(1))
            readBytes++
        }
        if (colr.meth == 1) {
            colr.add(cio_read(4))
            readBytes += 4
        } else {
            colr.add(0)
        }

        if (boxLength - readBytes > 0) {
            val colorProfile = ByteArray(boxLength - readBytes)
            inp!!.read(colorProfile, 0, boxLength - readBytes)
            colr.colorProfile = colorProfile
        }
        return colr
    }

    class ColorSpecBox : ArrayList<Int>() {
        var colorProfile: ByteArray? = null
            internal set(colorProfile) {
                this.colorProfile = colorProfile
            }

        val meth: Int
            get() = get(0).toInt()

        val prec: Int
            get() = get(1).toInt()

        val approx: Int
            get() = get(2).toInt()

        val enumCs: Int
            get() = get(3).toInt()
    }

    private inner class ZeroBoxSizeException : IOException {
        constructor() : super() {
        }

        constructor(s: String) : super(s) {
        }
    }

    companion object {

        // public static final membervariables

        val JP2_JP = 0x6a502020
        val JP2_IHDR = 0x69686472
        val JPIP_JPIP = 0x6a706970

        val JP2_FTYP = 0x66747970
        val JP2_JP2H = 0x6a703268
        val JP2_COLR = 0x636f6c72
        val JP2_JP2C = 0x6a703263
        val JP2_URL = 0x75726c20
        val JP2_DBTL = 0x6474626c
        val JP2_BPCC = 0x62706363
        val JP2_JP2 = 0x6a703220
    }

}
