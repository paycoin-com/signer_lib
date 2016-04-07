/*
 * $Id: fe6883918e3fa82c3cc4087d66a0f08a56651ef0 $
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

import com.itextpdf.text.error_messages.MessageLocalization

import com.itextpdf.text.Element
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Rectangle
import com.itextpdf.text.BaseColor

/** Implements the code codabar. The default parameters are:
 *
 * x = 0.8f;
 * n = 2;
 * font = BaseFont.createFont("Helvetica", "winansi", false);
 * size = 8;
 * baseline = size;
 * barHeight = size * 3;
 * textAlignment = Element.ALIGN_CENTER;
 * generateChecksum = false;
 * checksumText = false;
 * startStopText = false;
 *

 * @author Paulo Soares
 */
class BarcodeCodabar : Barcode() {
    init {
        try {
            x = 0.8f
            n = 2f
            font = BaseFont.createFont("Helvetica", "winansi", false)
            size = 8f
            baseline = size
            barHeight = size * 3
            textAlignment = Element.ALIGN_CENTER
            isGenerateChecksum = false
            isChecksumText = false
            isStartStopText = false
            codeType = Barcode.CODABAR
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    /** Gets the maximum area that the barcode and the text, if
     * any, will occupy. The lower left corner is always (0, 0).
     * @return the size the barcode occupies.
     */
    override val barcodeSize: Rectangle
        get() {
            var fontX = 0f
            var fontY = 0f
            var text = code
            if (isGenerateChecksum && isChecksumText)
                text = calculateChecksum(code)
            if (!isStartStopText)
                text = text.substring(1, text.length - 1)
            if (font != null) {
                if (baseline > 0)
                    fontY = baseline - font.getFontDescriptor(BaseFont.DESCENT, size)
                else
                    fontY = -baseline + size
                fontX = font.getWidthPoint(if (altText != null) altText else text, size)
            }
            text = code
            if (isGenerateChecksum)
                text = calculateChecksum(code)
            val bars = getBarsCodabar(text)
            var wide = 0
            for (k in bars.indices) {
                wide += bars[k].toInt()
            }
            val narrow = bars.size - wide
            var fullWidth = x * (narrow + wide * n)
            fullWidth = Math.max(fullWidth, fontX)
            val fullHeight = barHeight + fontY
            return Rectangle(fullWidth, fullHeight)
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
    override fun placeBarcode(cb: PdfContentByte, barColor: BaseColor?, textColor: BaseColor?): Rectangle {
        var fullCode = code
        if (isGenerateChecksum && isChecksumText)
            fullCode = calculateChecksum(code)
        if (!isStartStopText)
            fullCode = fullCode.substring(1, fullCode.length - 1)
        var fontX = 0f
        if (font != null) {
            fontX = font.getWidthPoint(fullCode = if (altText != null) altText else fullCode, size)
        }
        val bars = getBarsCodabar(if (isGenerateChecksum) calculateChecksum(code) else code)
        var wide = 0
        for (k in bars.indices) {
            wide += bars[k].toInt()
        }
        val narrow = bars.size - wide
        val fullWidth = x * (narrow + wide * n)
        var barStartX = 0f
        var textStartX = 0f
        when (textAlignment) {
            Element.ALIGN_LEFT -> {
            }
            Element.ALIGN_RIGHT -> if (fontX > fullWidth)
                barStartX = fontX - fullWidth
            else
                textStartX = fullWidth - fontX
            else -> if (fontX > fullWidth)
                barStartX = (fontX - fullWidth) / 2
            else
                textStartX = (fullWidth - fontX) / 2
        }
        var barStartY = 0f
        var textStartY = 0f
        if (font != null) {
            if (baseline <= 0)
                textStartY = barHeight - baseline
            else {
                textStartY = -font.getFontDescriptor(BaseFont.DESCENT, size)
                barStartY = textStartY + baseline
            }
        }
        var print = true
        if (barColor != null)
            cb.setColorFill(barColor)
        for (k in bars.indices) {
            val w = if (bars[k].toInt() == 0) x else x * n
            if (print)
                cb.rectangle(barStartX, barStartY, w - inkSpreading, barHeight)
            print = !print
            barStartX += w
        }
        cb.fill()
        if (font != null) {
            if (textColor != null)
                cb.setColorFill(textColor)
            cb.beginText()
            cb.setFontAndSize(font, size)
            cb.setTextMatrix(textStartX, textStartY)
            cb.showText(fullCode)
            cb.endText()
        }
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

        var fullCode = code
        if (isGenerateChecksum && isChecksumText)
            fullCode = calculateChecksum(code)
        if (!isStartStopText)
            fullCode = fullCode.substring(1, fullCode.length - 1)
        val bars = getBarsCodabar(if (isGenerateChecksum) calculateChecksum(code) else code)
        var wide = 0
        for (k in bars.indices) {
            wide += bars[k].toInt()
        }
        val narrow = bars.size - wide
        val fullWidth = narrow + wide * n.toInt()
        var print = true
        var ptr = 0
        val height = barHeight.toInt()
        val pix = IntArray(fullWidth * height)
        for (k in bars.indices) {
            val w = if (bars[k].toInt() == 0) 1 else n.toInt()
            var c = g
            if (print)
                c = f
            print = !print
            for (j in 0..w - 1)
                pix[ptr++] = c
        }
        var k = fullWidth
        while (k < pix.size) {
            System.arraycopy(pix, 0, pix, k, fullWidth)
            k += fullWidth
        }
        val img = canvas.createImage(java.awt.image.MemoryImageSource(fullWidth, height, pix, 0, fullWidth))

        return img
    }

    companion object {

        /** The bars to generate the code.
         */
        private val BARS = arrayOf(byteArrayOf(0, 0, 0, 0, 0, 1, 1), // 0
                byteArrayOf(0, 0, 0, 0, 1, 1, 0), // 1
                byteArrayOf(0, 0, 0, 1, 0, 0, 1), // 2
                byteArrayOf(1, 1, 0, 0, 0, 0, 0), // 3
                byteArrayOf(0, 0, 1, 0, 0, 1, 0), // 4
                byteArrayOf(1, 0, 0, 0, 0, 1, 0), // 5
                byteArrayOf(0, 1, 0, 0, 0, 0, 1), // 6
                byteArrayOf(0, 1, 0, 0, 1, 0, 0), // 7
                byteArrayOf(0, 1, 1, 0, 0, 0, 0), // 8
                byteArrayOf(1, 0, 0, 1, 0, 0, 0), // 9
                byteArrayOf(0, 0, 0, 1, 1, 0, 0), // -
                byteArrayOf(0, 0, 1, 1, 0, 0, 0), // $
                byteArrayOf(1, 0, 0, 0, 1, 0, 1), // :
                byteArrayOf(1, 0, 1, 0, 0, 0, 1), // /
                byteArrayOf(1, 0, 1, 0, 1, 0, 0), // .
                byteArrayOf(0, 0, 1, 0, 1, 0, 1), // +
                byteArrayOf(0, 0, 1, 1, 0, 1, 0), // a
                byteArrayOf(0, 1, 0, 1, 0, 0, 1), // b
                byteArrayOf(0, 0, 0, 1, 0, 1, 1), // c
                byteArrayOf(0, 0, 0, 1, 1, 1, 0)  // d
        )

        /** The index chars to BARS.
         */
        private val CHARS = "0123456789-$:/.+ABCD"

        private val START_STOP_IDX = 16

        /** Creates the bars.
         * @param text the text to create the bars
         * *
         * @return the bars
         */
        fun getBarsCodabar(text: String): ByteArray {
            var text = text
            text = text.toUpperCase()
            val len = text.length
            if (len < 2)
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("codabar.must.have.at.least.a.start.and.stop.character"))
            if (CHARS.indexOf(text[0].toInt()) < START_STOP_IDX || CHARS.indexOf(text[len - 1].toInt()) < START_STOP_IDX)
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("codabar.must.have.one.of.abcd.as.start.stop.character"))
            val bars = ByteArray(text.length * 8 - 1)
            for (k in 0..len - 1) {
                val idx = CHARS.indexOf(text[k].toInt())
                if (idx >= START_STOP_IDX && k > 0 && k < len - 1)
                    throw IllegalArgumentException(MessageLocalization.getComposedMessage("in.codabar.start.stop.characters.are.only.allowed.at.the.extremes"))
                if (idx < 0)
                    throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.character.1.is.illegal.in.codabar", text[k].toInt()))
                System.arraycopy(BARS[idx], 0, bars, k * 8, 7)
            }
            return bars
        }

        fun calculateChecksum(code: String): String {
            if (code.length < 2)
                return code
            val text = code.toUpperCase()
            var sum = 0
            val len = text.length
            for (k in 0..len - 1)
                sum += CHARS.indexOf(text[k].toInt())
            sum = (sum + 15) / 16 * 16 - sum
            return code.substring(0, len - 1) + CHARS[sum] + code.substring(len - 1)
        }
    }
}
/** Creates a new BarcodeCodabar.
 */
