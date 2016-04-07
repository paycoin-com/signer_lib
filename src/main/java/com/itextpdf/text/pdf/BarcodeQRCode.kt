/*
 * $Id: d9f2880da19aa9a735283384228b4eea079be3ba $
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

import com.itextpdf.text.*
import com.itextpdf.text.pdf.qrcode.EncodeHintType
import com.itextpdf.text.pdf.qrcode.WriterException
import com.itextpdf.text.pdf.qrcode.ByteMatrix
import com.itextpdf.text.pdf.qrcode.QRCodeWriter
import com.itextpdf.text.pdf.codec.CCITTG4Encoder

/**
 * A QRCode implementation based on the zxing code.
 * @author Paulo Soares
 * *
 * @since 5.0.2
 */
class BarcodeQRCode
/**
 * Creates the QR barcode. The barcode is always created with the smallest possible size and is then stretched
 * to the width and height given. Set the width and height to 1 to get an unscaled barcode.
 * @param content the text to be encoded
 * *
 * @param width the barcode width
 * *
 * @param height the barcode height
 * *
 * @param hints modifiers to change the way the barcode is create. They can be EncodeHintType.ERROR_CORRECTION
 * * and EncodeHintType.CHARACTER_SET. For EncodeHintType.ERROR_CORRECTION the values can be ErrorCorrectionLevel.L, M, Q, H.
 * * For EncodeHintType.CHARACTER_SET the values are strings and can be Cp437, Shift_JIS and ISO-8859-1 to ISO-8859-16.
 * * You can also use UTF-8, but correct behaviour is not guaranteed as Unicode is not supported in QRCodes.
 * * The default value is ISO-8859-1.
 * *
 * @throws WriterException
 */
(content: String, width: Int, height: Int, hints: Map<EncodeHintType, Any>) {
    internal var bm: ByteMatrix

    init {
        try {
            val qc = QRCodeWriter()
            bm = qc.encode(content, width, height, hints)
        } catch (ex: WriterException) {
            throw ExceptionConverter(ex)
        }

    }

    private val bitMatrix: ByteArray
        get() {
            val width = bm.width
            val height = bm.height
            val stride = (width + 7) / 8
            val b = ByteArray(stride * height)
            val mt = bm.array
            for (y in 0..height - 1) {
                val line = mt[y]
                for (x in 0..width - 1) {
                    if (line[x].toInt() != 0) {
                        val offset = stride * y + x / 8
                        b[offset] = b[offset] or (0x80 shr x % 8).toByte()
                    }
                }
            }
            return b
        }

    /** Gets an Image with the barcode.
     * @return the barcode Image
     * *
     * @throws BadElementException on error
     */
    val image: Image
        @Throws(BadElementException::class)
        get() {
            val b = bitMatrix
            val g4 = CCITTG4Encoder.compress(b, bm.width, bm.height)
            return Image.getInstance(bm.width, bm.height, false, Image.CCITTG4, Image.CCITT_BLACKIS1, g4, null)
        }

    // AWT related methods (remove this if you port to Android / GAE)

    /** Creates a java.awt.Image.
     * @param foreground the color of the bars
     * *
     * @param background the color of the background
     * *
     * @return the image
     */
    fun createAwtImage(foreground: java.awt.Color, background: java.awt.Color): java.awt.Image {
        val f = foreground.rgb
        val g = background.rgb
        val canvas = java.awt.Canvas()

        val width = bm.width
        val height = bm.height
        val pix = IntArray(width * height)
        val mt = bm.array
        for (y in 0..height - 1) {
            val line = mt[y]
            for (x in 0..width - 1) {
                pix[y * width + x] = if (line[x].toInt() == 0) f else g
            }
        }

        val img = canvas.createImage(java.awt.image.MemoryImageSource(width, height, pix, 0, width))
        return img
    }

    fun placeBarcode(cb: PdfContentByte, foreground: BaseColor, moduleSide: Float) {
        val width = bm.width
        val height = bm.height
        val mt = bm.array

        cb.setColorFill(foreground)

        for (y in 0..height - 1) {
            val line = mt[y]
            for (x in 0..width - 1) {
                if (line[x].toInt() == 0) {
                    cb.rectangle(x * moduleSide, (height - y - 1) * moduleSide, moduleSide, moduleSide)
                }
            }
        }
        cb.fill()
    }

    /** Gets the size of the barcode grid.  */
    val barcodeSize: Rectangle
        get() = Rectangle(0f, 0f, bm.width.toFloat(), bm.height.toFloat())
}
