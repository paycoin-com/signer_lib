/*
 * $Id: 9278844e4d8431d819128ff8b00e226bd41a8ba7 $
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

/** Implements the code 39 and code 39 extended. The default parameters are:
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
 * startStopText = true;
 * extended = false;
 *

 * @author Paulo Soares
 */
class Barcode39 : Barcode() {

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
            isStartStopText = true
            isExtended = false
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
            var fCode = code
            if (isExtended)
                fCode = getCode39Ex(code)
            if (font != null) {
                if (baseline > 0)
                    fontY = baseline - font.getFontDescriptor(BaseFont.DESCENT, size)
                else
                    fontY = -baseline + size
                var fullCode = code
                if (isGenerateChecksum && isChecksumText)
                    fullCode += getChecksum(fCode)
                if (isStartStopText)
                    fullCode = "*$fullCode*"
                fontX = font.getWidthPoint(if (altText != null) altText else fullCode, size)
            }
            var len = fCode.length + 2
            if (isGenerateChecksum)
                ++len
            var fullWidth = len * (6 * x + 3f * x * n) + (len - 1) * x
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
        var fontX = 0f
        var bCode = code
        if (isExtended)
            bCode = getCode39Ex(code)
        if (font != null) {
            if (isGenerateChecksum && isChecksumText)
                fullCode += getChecksum(bCode)
            if (isStartStopText)
                fullCode = "*$fullCode*"
            fontX = font.getWidthPoint(fullCode = if (altText != null) altText else fullCode, size)
        }
        if (isGenerateChecksum)
            bCode += getChecksum(bCode)
        val len = bCode.length + 2
        val fullWidth = len * (6 * x + 3f * x * n) + (len - 1) * x
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
        val bars = getBarsCode39(bCode)
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

        var bCode = code
        if (isExtended)
            bCode = getCode39Ex(code)
        if (isGenerateChecksum)
            bCode += getChecksum(bCode)
        val len = bCode.length + 2
        val nn = n.toInt()
        val fullWidth = len * (6 + 3 * nn) + (len - 1)
        val bars = getBarsCode39(bCode)
        var print = true
        var ptr = 0
        val height = barHeight.toInt()
        val pix = IntArray(fullWidth * height)
        for (k in bars.indices) {
            val w = if (bars[k].toInt() == 0) 1 else nn
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
        private val BARS = arrayOf(byteArrayOf(0, 0, 0, 1, 1, 0, 1, 0, 0), byteArrayOf(1, 0, 0, 1, 0, 0, 0, 0, 1), byteArrayOf(0, 0, 1, 1, 0, 0, 0, 0, 1), byteArrayOf(1, 0, 1, 1, 0, 0, 0, 0, 0), byteArrayOf(0, 0, 0, 1, 1, 0, 0, 0, 1), byteArrayOf(1, 0, 0, 1, 1, 0, 0, 0, 0), byteArrayOf(0, 0, 1, 1, 1, 0, 0, 0, 0), byteArrayOf(0, 0, 0, 1, 0, 0, 1, 0, 1), byteArrayOf(1, 0, 0, 1, 0, 0, 1, 0, 0), byteArrayOf(0, 0, 1, 1, 0, 0, 1, 0, 0), byteArrayOf(1, 0, 0, 0, 0, 1, 0, 0, 1), byteArrayOf(0, 0, 1, 0, 0, 1, 0, 0, 1), byteArrayOf(1, 0, 1, 0, 0, 1, 0, 0, 0), byteArrayOf(0, 0, 0, 0, 1, 1, 0, 0, 1), byteArrayOf(1, 0, 0, 0, 1, 1, 0, 0, 0), byteArrayOf(0, 0, 1, 0, 1, 1, 0, 0, 0), byteArrayOf(0, 0, 0, 0, 0, 1, 1, 0, 1), byteArrayOf(1, 0, 0, 0, 0, 1, 1, 0, 0), byteArrayOf(0, 0, 1, 0, 0, 1, 1, 0, 0), byteArrayOf(0, 0, 0, 0, 1, 1, 1, 0, 0), byteArrayOf(1, 0, 0, 0, 0, 0, 0, 1, 1), byteArrayOf(0, 0, 1, 0, 0, 0, 0, 1, 1), byteArrayOf(1, 0, 1, 0, 0, 0, 0, 1, 0), byteArrayOf(0, 0, 0, 0, 1, 0, 0, 1, 1), byteArrayOf(1, 0, 0, 0, 1, 0, 0, 1, 0), byteArrayOf(0, 0, 1, 0, 1, 0, 0, 1, 0), byteArrayOf(0, 0, 0, 0, 0, 0, 1, 1, 1), byteArrayOf(1, 0, 0, 0, 0, 0, 1, 1, 0), byteArrayOf(0, 0, 1, 0, 0, 0, 1, 1, 0), byteArrayOf(0, 0, 0, 0, 1, 0, 1, 1, 0), byteArrayOf(1, 1, 0, 0, 0, 0, 0, 0, 1), byteArrayOf(0, 1, 1, 0, 0, 0, 0, 0, 1), byteArrayOf(1, 1, 1, 0, 0, 0, 0, 0, 0), byteArrayOf(0, 1, 0, 0, 1, 0, 0, 0, 1), byteArrayOf(1, 1, 0, 0, 1, 0, 0, 0, 0), byteArrayOf(0, 1, 1, 0, 1, 0, 0, 0, 0), byteArrayOf(0, 1, 0, 0, 0, 0, 1, 0, 1), byteArrayOf(1, 1, 0, 0, 0, 0, 1, 0, 0), byteArrayOf(0, 1, 1, 0, 0, 0, 1, 0, 0), byteArrayOf(0, 1, 0, 1, 0, 1, 0, 0, 0), byteArrayOf(0, 1, 0, 1, 0, 0, 0, 1, 0), byteArrayOf(0, 1, 0, 0, 0, 1, 0, 1, 0), byteArrayOf(0, 0, 0, 1, 0, 1, 0, 1, 0), byteArrayOf(0, 1, 0, 0, 1, 0, 1, 0, 0))

        /** The index chars to BARS.
         */
        private val CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%*"

        /** The character combinations to make the code 39 extended.
         */
        private val EXTENDED = "%U$A$B$C$D$E$F$G$H$I$J$K$L$M$N$O$P$Q$R$S$T$U$V$W$X$Y$Z%A%B%C%D%E  /A/B/C/D/E/F/G/H/I/J/K/L - ./O 0 1 2 3 4 5 6 7 8 9/Z%F%G%H%I%J%V A B C D E F G H I J K L M N O P Q R S T U V W X Y Z%K%L%M%N%O%W+A+B+C+D+E+F+G+H+I+J+K+L+M+N+O+P+Q+R+S+T+U+V+W+X+Y+Z%P%Q%R%S%T"

        /** Creates the bars.
         * @param text the text to create the bars. This text does not include the start and
         * * stop characters
         * *
         * @return the bars
         */
        fun getBarsCode39(text: String): ByteArray {
            var text = text
            text = "*$text*"
            val bars = ByteArray(text.length * 10 - 1)
            for (k in 0..text.length - 1) {
                val idx = CHARS.indexOf(text[k].toInt())
                if (idx < 0)
                    throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.character.1.is.illegal.in.code.39", text[k].toInt()))
                System.arraycopy(BARS[idx], 0, bars, k * 10, 9)
            }
            return bars
        }

        /** Converts the extended text into a normal, escaped text,
         * ready to generate bars.
         * @param text the extended text
         * *
         * @return the escaped text
         */
        fun getCode39Ex(text: String): String {
            val out = StringBuilder("")
            for (k in 0..text.length - 1) {
                val c = text[k]
                if (c.toInt() > 127)
                    throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.character.1.is.illegal.in.code.39.extended", c.toInt()))
                val c1 = EXTENDED[c.toInt() * 2]
                val c2 = EXTENDED[c.toInt() * 2 + 1]
                if (c1 != ' ')
                    out.append(c1)
                out.append(c2)
            }
            return out.toString()
        }

        /** Calculates the checksum.
         * @param text the text
         * *
         * @return the checksum
         */
        internal fun getChecksum(text: String): Char {
            var chk = 0
            for (k in 0..text.length - 1) {
                val idx = CHARS.indexOf(text[k].toInt())
                if (idx < 0)
                    throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.character.1.is.illegal.in.code.39", text[k].toInt()))
                chk += idx
            }
            return CHARS[chk % 43]
        }
    }
}
/** Creates a new Barcode39.
 */
