/*
 * $Id: 492457d98e1673b9b5d74fc95f9f40f3d815a684 $
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

import com.itextpdf.text.Rectangle
import com.itextpdf.text.BaseColor

/** Implements the Postnet and Planet barcodes. The default parameters are:
 *
 * n = 72f / 22f; // distance between bars
 * x = 0.02f * 72f; // bar width
 * barHeight = 0.125f * 72f; // height of the tall bars
 * size = 0.05f * 72f; // height of the short bars
 * codeType = POSTNET; // type of code
 *

 * @author Paulo Soares
 */
class BarcodePostnet : Barcode() {

    init {
        n = 72f / 22f // distance between bars
        x = 0.02f * 72f // bar width
        barHeight = 0.125f * 72f // height of the tall bars
        size = 0.05f * 72f // height of the short bars
        codeType = Barcode.POSTNET // type of code
    }

    /** Gets the maximum area that the barcode and the text, if
     * any, will occupy. The lower left corner is always (0, 0).
     * @return the size the barcode occupies.
     */
    override val barcodeSize: Rectangle
        get() {
            val width = ((code.length + 1) * 5 + 1) * n + x
            return Rectangle(width, barHeight)
        }

    /** Places the barcode in a PdfContentByte. The
     * barcode is always placed at coordinates (0, 0). Use the
     * translation matrix to move it elsewhere.
     *
     *
     * The bars and text are written in the following colors:
     *
     *
     *
     *
     * barColor
     * textColor
     * Result
     *
     *
     * null
     * null
     * bars and text painted with current fill color
     *
     *
     * barColor
     * null
     * bars and text painted with barColor
     *
     *
     * null
     * textColor
     * bars painted with current colortext painted with textColor
     *
     *
     * barColor
     * textColor
     * bars painted with barColortext painted with textColor
     *
     *
     * @param cb the PdfContentByte where the barcode will be placed
     * *
     * @param barColor the color of the bars. It can be null
     * *
     * @param textColor the color of the text. It can be null
     * *
     * @return the dimensions the barcode occupies
     */
    override fun placeBarcode(cb: PdfContentByte, barColor: BaseColor?, textColor: BaseColor): Rectangle {
        if (barColor != null)
            cb.setColorFill(barColor)
        val bars = getBarsPostnet(code)
        var flip: Byte = 1
        if (codeType == Barcode.PLANET) {
            flip = 0
            bars[0] = 0
            bars[bars.size - 1] = 0
        }
        var startX = 0f
        for (k in bars.indices) {
            cb.rectangle(startX, 0f, x - inkSpreading, if (bars[k] == flip) barHeight else size)
            startX += n
        }
        cb.fill()
        return barcodeSize
    }

    // AWT related methods (remove this if you port to Android / GAE)

    /** Creates a java.awt.Image. This image only
     * contains the bars without any text.
     * @param foreground the color of the bars
     * *
     * @param background the color of the background
     * *
     * @return the image
     */
    override fun createAwtImage(foreground: java.awt.Color, background: java.awt.Color): java.awt.Image {
        val f = foreground.rgb
        val g = background.rgb
        val canvas = java.awt.Canvas()
        var barWidth = x.toInt()
        if (barWidth <= 0)
            barWidth = 1
        var barDistance = n.toInt()
        if (barDistance <= barWidth)
            barDistance = barWidth + 1
        var barShort = size.toInt()
        if (barShort <= 0)
            barShort = 1
        var barTall = barHeight.toInt()
        if (barTall <= barShort)
            barTall = barShort + 1
        val width = ((code.length + 1) * 5 + 1) * barDistance + barWidth
        val pix = IntArray(width * barTall)
        val bars = getBarsPostnet(code)
        var flip: Byte = 1
        if (codeType == Barcode.PLANET) {
            flip = 0
            bars[0] = 0
            bars[bars.size - 1] = 0
        }
        var idx = 0
        for (k in bars.indices) {
            val dot = bars[k] == flip
            for (j in 0..barDistance - 1) {
                pix[idx + j] = if (dot && j < barWidth) f else g
            }
            idx += barDistance
        }
        val limit = width * (barTall - barShort)
        run {
            var k = width
            while (k < limit) {
                System.arraycopy(pix, 0, pix, k, width)
                k += width
            }
        }
        idx = limit
        for (k in bars.indices) {
            for (j in 0..barDistance - 1) {
                pix[idx + j] = if (j < barWidth) f else g
            }
            idx += barDistance
        }
        var k = limit + width
        while (k < pix.size) {
            System.arraycopy(pix, limit, pix, k, width)
            k += width
        }
        val img = canvas.createImage(java.awt.image.MemoryImageSource(width, barTall, pix, 0, width))

        return img
    }

    companion object {

        /** The bars for each character.
         */
        private val BARS = arrayOf(byteArrayOf(1, 1, 0, 0, 0), byteArrayOf(0, 0, 0, 1, 1), byteArrayOf(0, 0, 1, 0, 1), byteArrayOf(0, 0, 1, 1, 0), byteArrayOf(0, 1, 0, 0, 1), byteArrayOf(0, 1, 0, 1, 0), byteArrayOf(0, 1, 1, 0, 0), byteArrayOf(1, 0, 0, 0, 1), byteArrayOf(1, 0, 0, 1, 0), byteArrayOf(1, 0, 1, 0, 0))

        /** Creates the bars for Postnet.
         * @param text the code to be created without checksum
         * *
         * @return the bars
         */
        fun getBarsPostnet(text: String): ByteArray {
            var text = text
            var total = 0
            for (k in text.length - 1 downTo 0) {
                val n = text[k] - '0'
                total += n
            }
            text += ((10 - total % 10) % 10 + '0').toChar()
            val bars = ByteArray(text.length * 5 + 2)
            bars[0] = 1
            bars[bars.size - 1] = 1
            for (k in 0..text.length - 1) {
                val c = text[k] - '0'
                System.arraycopy(BARS[c], 0, bars, k * 5 + 1, 5)
            }
            return bars
        }
    }
}
/** Creates new BarcodePostnet  */
