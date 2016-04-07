/*
 * $Id: dce847fbc6cde87bb98bead55152a52e41ec2917 $
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

import java.util.Arrays
import com.itextpdf.text.error_messages.MessageLocalization

import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Rectangle
import com.itextpdf.text.BaseColor

/** Generates barcodes in several formats: EAN13, EAN8, UPCA, UPCE,
 * supplemental 2 and 5. The default parameters are:
 *
 * x = 0.8f;
 * font = BaseFont.createFont("Helvetica", "winansi", false);
 * size = 8;
 * baseline = size;
 * barHeight = size * 3;
 * guardBars = true;
 * codeType = EAN13;
 * code = "";
 *

 * @author Paulo Soares
 */
class BarcodeEAN : Barcode() {

    init {
        try {
            x = 0.8f
            font = BaseFont.createFont("Helvetica", "winansi", false)
            size = 8f
            baseline = size
            barHeight = size * 3
            isGuardBars = true
            codeType = Barcode.EAN13
            code = ""
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
            var width = 0f
            var height = barHeight
            if (font != null) {
                if (baseline <= 0)
                    height += -baseline + size
                else
                    height += baseline - font.getFontDescriptor(BaseFont.DESCENT, size)
            }
            when (codeType) {
                Barcode.EAN13 -> {
                    width = x * (11 + 12 * 7)
                    if (font != null) {
                        width += font.getWidthPoint(code[0].toInt(), size)
                    }
                }
                Barcode.EAN8 -> width = x * (11 + 8 * 7)
                Barcode.UPCA -> {
                    width = x * (11 + 12 * 7)
                    if (font != null) {
                        width += font.getWidthPoint(code[0].toInt(), size) + font.getWidthPoint(code[11].toInt(), size)
                    }
                }
                Barcode.UPCE -> {
                    width = x * (9 + 6 * 7)
                    if (font != null) {
                        width += font.getWidthPoint(code[0].toInt(), size) + font.getWidthPoint(code[7].toInt(), size)
                    }
                }
                Barcode.SUPP2 -> width = x * (6 + 2 * 7)
                Barcode.SUPP5 -> width = x * (4 + 5 * 7 + 4 * 2)
                else -> throw RuntimeException(MessageLocalization.getComposedMessage("invalid.code.type"))
            }
            return Rectangle(width, height)
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
        val rect = barcodeSize
        var barStartX = 0f
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
        when (codeType) {
            Barcode.EAN13, Barcode.UPCA, Barcode.UPCE -> if (font != null)
                barStartX += font.getWidthPoint(code[0].toInt(), size)
        }
        var bars: ByteArray? = null
        var guard = GUARD_EMPTY
        when (codeType) {
            Barcode.EAN13 -> {
                bars = getBarsEAN13(code)
                guard = GUARD_EAN13
            }
            Barcode.EAN8 -> {
                bars = getBarsEAN8(code)
                guard = GUARD_EAN8
            }
            Barcode.UPCA -> {
                bars = getBarsEAN13("0" + code)
                guard = GUARD_UPCA
            }
            Barcode.UPCE -> {
                bars = getBarsUPCE(code)
                guard = GUARD_UPCE
            }
            Barcode.SUPP2 -> bars = getBarsSupplemental2(code)
            Barcode.SUPP5 -> bars = getBarsSupplemental5(code)
        }
        val keepBarX = barStartX
        var print = true
        var gd = 0f
        if (font != null && baseline > 0 && isGuardBars) {
            gd = baseline / 2
        }
        if (barColor != null)
            cb.setColorFill(barColor)
        for (k in bars!!.indices) {
            val w = bars[k] * x
            if (print) {
                if (Arrays.binarySearch(guard, k) >= 0)
                    cb.rectangle(barStartX, barStartY - gd, w - inkSpreading, barHeight + gd)
                else
                    cb.rectangle(barStartX, barStartY, w - inkSpreading, barHeight)
            }
            print = !print
            barStartX += w
        }
        cb.fill()
        if (font != null) {
            if (textColor != null)
                cb.setColorFill(textColor)
            cb.beginText()
            cb.setFontAndSize(font, size)
            when (codeType) {
                Barcode.EAN13 -> {
                    cb.setTextMatrix(0f, textStartY)
                    cb.showText(code.substring(0, 1))
                    for (k in 1..12) {
                        val c = code.substring(k, k + 1)
                        val len = font.getWidthPoint(c, size)
                        val pX = keepBarX + TEXTPOS_EAN13[k - 1] * x - len / 2
                        cb.setTextMatrix(pX, textStartY)
                        cb.showText(c)
                    }
                }
                Barcode.EAN8 -> for (k in 0..7) {
                    val c = code.substring(k, k + 1)
                    val len = font.getWidthPoint(c, size)
                    val pX = TEXTPOS_EAN8[k] * x - len / 2
                    cb.setTextMatrix(pX, textStartY)
                    cb.showText(c)
                }
                Barcode.UPCA -> {
                    cb.setTextMatrix(0f, textStartY)
                    cb.showText(code.substring(0, 1))
                    for (k in 1..10) {
                        val c = code.substring(k, k + 1)
                        val len = font.getWidthPoint(c, size)
                        val pX = keepBarX + TEXTPOS_EAN13[k] * x - len / 2
                        cb.setTextMatrix(pX, textStartY)
                        cb.showText(c)
                    }
                    cb.setTextMatrix(keepBarX + x * (11 + 12 * 7), textStartY)
                    cb.showText(code.substring(11, 12))
                }
                Barcode.UPCE -> {
                    cb.setTextMatrix(0f, textStartY)
                    cb.showText(code.substring(0, 1))
                    for (k in 1..6) {
                        val c = code.substring(k, k + 1)
                        val len = font.getWidthPoint(c, size)
                        val pX = keepBarX + TEXTPOS_EAN13[k - 1] * x - len / 2
                        cb.setTextMatrix(pX, textStartY)
                        cb.showText(c)
                    }
                    cb.setTextMatrix(keepBarX + x * (9 + 6 * 7), textStartY)
                    cb.showText(code.substring(7, 8))
                }
                Barcode.SUPP2, Barcode.SUPP5 -> for (k in 0..code.length - 1) {
                    val c = code.substring(k, k + 1)
                    val len = font.getWidthPoint(c, size)
                    val pX = (7.5f + 9 * k) * x - len / 2
                    cb.setTextMatrix(pX, textStartY)
                    cb.showText(c)
                }
            }
            cb.endText()
        }
        return rect
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

        var width = 0
        var bars: ByteArray? = null
        when (codeType) {
            Barcode.EAN13 -> {
                bars = getBarsEAN13(code)
                width = 11 + 12 * 7
            }
            Barcode.EAN8 -> {
                bars = getBarsEAN8(code)
                width = 11 + 8 * 7
            }
            Barcode.UPCA -> {
                bars = getBarsEAN13("0" + code)
                width = 11 + 12 * 7
            }
            Barcode.UPCE -> {
                bars = getBarsUPCE(code)
                width = 9 + 6 * 7
            }
            Barcode.SUPP2 -> {
                bars = getBarsSupplemental2(code)
                width = 6 + 2 * 7
            }
            Barcode.SUPP5 -> {
                bars = getBarsSupplemental5(code)
                width = 4 + 5 * 7 + 4 * 2
            }
            else -> throw RuntimeException(MessageLocalization.getComposedMessage("invalid.code.type"))
        }

        var print = true
        var ptr = 0
        val height = barHeight.toInt()
        val pix = IntArray(width * height)
        for (k in bars.indices) {
            val w = bars[k].toInt()
            var c = g
            if (print)
                c = f
            print = !print
            for (j in 0..w - 1)
                pix[ptr++] = c
        }
        var k = width
        while (k < pix.size) {
            System.arraycopy(pix, 0, pix, k, width)
            k += width
        }
        val img = canvas.createImage(java.awt.image.MemoryImageSource(width, height, pix, 0, width))

        return img
    }

    companion object {

        /** The bar positions that are guard bars. */
        private val GUARD_EMPTY = intArrayOf()
        /** The bar positions that are guard bars. */
        private val GUARD_UPCA = intArrayOf(0, 2, 4, 6, 28, 30, 52, 54, 56, 58)
        /** The bar positions that are guard bars. */
        private val GUARD_EAN13 = intArrayOf(0, 2, 28, 30, 56, 58)
        /** The bar positions that are guard bars. */
        private val GUARD_EAN8 = intArrayOf(0, 2, 20, 22, 40, 42)
        /** The bar positions that are guard bars. */
        private val GUARD_UPCE = intArrayOf(0, 2, 28, 30, 32)
        /** The x coordinates to place the text. */
        private val TEXTPOS_EAN13 = floatArrayOf(6.5f, 13.5f, 20.5f, 27.5f, 34.5f, 41.5f, 53.5f, 60.5f, 67.5f, 74.5f, 81.5f, 88.5f)
        /** The x coordinates to place the text. */
        private val TEXTPOS_EAN8 = floatArrayOf(6.5f, 13.5f, 20.5f, 27.5f, 39.5f, 46.5f, 53.5f, 60.5f)
        /** The basic bar widths. */
        private val BARS = arrayOf(byteArrayOf(3, 2, 1, 1), // 0
                byteArrayOf(2, 2, 2, 1), // 1
                byteArrayOf(2, 1, 2, 2), // 2
                byteArrayOf(1, 4, 1, 1), // 3
                byteArrayOf(1, 1, 3, 2), // 4
                byteArrayOf(1, 2, 3, 1), // 5
                byteArrayOf(1, 1, 1, 4), // 6
                byteArrayOf(1, 3, 1, 2), // 7
                byteArrayOf(1, 2, 1, 3), // 8
                byteArrayOf(3, 1, 1, 2)  // 9
        )

        /** The total number of bars for EAN13. */
        private val TOTALBARS_EAN13 = 11 + 12 * 4
        /** The total number of bars for EAN8. */
        private val TOTALBARS_EAN8 = 11 + 8 * 4
        /** The total number of bars for UPCE. */
        private val TOTALBARS_UPCE = 9 + 6 * 4
        /** The total number of bars for supplemental 2. */
        private val TOTALBARS_SUPP2 = 13
        /** The total number of bars for supplemental 5. */
        private val TOTALBARS_SUPP5 = 31
        /** Marker for odd parity. */
        private val ODD = 0
        /** Marker for even parity. */
        private val EVEN = 1

        /** Sequence of parities to be used with EAN13. */
        private val PARITY13 = arrayOf(byteArrayOf(ODD.toByte(), ODD.toByte(), ODD.toByte(), ODD.toByte(), ODD.toByte(), ODD.toByte()), // 0
                byteArrayOf(ODD.toByte(), ODD.toByte(), EVEN.toByte(), ODD.toByte(), EVEN.toByte(), EVEN.toByte()), // 1
                byteArrayOf(ODD.toByte(), ODD.toByte(), EVEN.toByte(), EVEN.toByte(), ODD.toByte(), EVEN.toByte()), // 2
                byteArrayOf(ODD.toByte(), ODD.toByte(), EVEN.toByte(), EVEN.toByte(), EVEN.toByte(), ODD.toByte()), // 3
                byteArrayOf(ODD.toByte(), EVEN.toByte(), ODD.toByte(), ODD.toByte(), EVEN.toByte(), EVEN.toByte()), // 4
                byteArrayOf(ODD.toByte(), EVEN.toByte(), EVEN.toByte(), ODD.toByte(), ODD.toByte(), EVEN.toByte()), // 5
                byteArrayOf(ODD.toByte(), EVEN.toByte(), EVEN.toByte(), EVEN.toByte(), ODD.toByte(), ODD.toByte()), // 6
                byteArrayOf(ODD.toByte(), EVEN.toByte(), ODD.toByte(), EVEN.toByte(), ODD.toByte(), EVEN.toByte()), // 7
                byteArrayOf(ODD.toByte(), EVEN.toByte(), ODD.toByte(), EVEN.toByte(), EVEN.toByte(), ODD.toByte()), // 8
                byteArrayOf(ODD.toByte(), EVEN.toByte(), EVEN.toByte(), ODD.toByte(), EVEN.toByte(), ODD.toByte())   // 9
        )

        /** Sequence of parities to be used with supplemental 2. */
        private val PARITY2 = arrayOf(byteArrayOf(ODD.toByte(), ODD.toByte()), // 0
                byteArrayOf(ODD.toByte(), EVEN.toByte()), // 1
                byteArrayOf(EVEN.toByte(), ODD.toByte()), // 2
                byteArrayOf(EVEN.toByte(), EVEN.toByte())   // 3
        )

        /** Sequence of parities to be used with supplemental 2. */
        private val PARITY5 = arrayOf(byteArrayOf(EVEN.toByte(), EVEN.toByte(), ODD.toByte(), ODD.toByte(), ODD.toByte()), // 0
                byteArrayOf(EVEN.toByte(), ODD.toByte(), EVEN.toByte(), ODD.toByte(), ODD.toByte()), // 1
                byteArrayOf(EVEN.toByte(), ODD.toByte(), ODD.toByte(), EVEN.toByte(), ODD.toByte()), // 2
                byteArrayOf(EVEN.toByte(), ODD.toByte(), ODD.toByte(), ODD.toByte(), EVEN.toByte()), // 3
                byteArrayOf(ODD.toByte(), EVEN.toByte(), EVEN.toByte(), ODD.toByte(), ODD.toByte()), // 4
                byteArrayOf(ODD.toByte(), ODD.toByte(), EVEN.toByte(), EVEN.toByte(), ODD.toByte()), // 5
                byteArrayOf(ODD.toByte(), ODD.toByte(), ODD.toByte(), EVEN.toByte(), EVEN.toByte()), // 6
                byteArrayOf(ODD.toByte(), EVEN.toByte(), ODD.toByte(), EVEN.toByte(), ODD.toByte()), // 7
                byteArrayOf(ODD.toByte(), EVEN.toByte(), ODD.toByte(), ODD.toByte(), EVEN.toByte()), // 8
                byteArrayOf(ODD.toByte(), ODD.toByte(), EVEN.toByte(), ODD.toByte(), EVEN.toByte())  // 9
        )

        /** Sequence of parities to be used with UPCE. */
        private val PARITYE = arrayOf(byteArrayOf(EVEN.toByte(), EVEN.toByte(), EVEN.toByte(), ODD.toByte(), ODD.toByte(), ODD.toByte()), // 0
                byteArrayOf(EVEN.toByte(), EVEN.toByte(), ODD.toByte(), EVEN.toByte(), ODD.toByte(), ODD.toByte()), // 1
                byteArrayOf(EVEN.toByte(), EVEN.toByte(), ODD.toByte(), ODD.toByte(), EVEN.toByte(), ODD.toByte()), // 2
                byteArrayOf(EVEN.toByte(), EVEN.toByte(), ODD.toByte(), ODD.toByte(), ODD.toByte(), EVEN.toByte()), // 3
                byteArrayOf(EVEN.toByte(), ODD.toByte(), EVEN.toByte(), EVEN.toByte(), ODD.toByte(), ODD.toByte()), // 4
                byteArrayOf(EVEN.toByte(), ODD.toByte(), ODD.toByte(), EVEN.toByte(), EVEN.toByte(), ODD.toByte()), // 5
                byteArrayOf(EVEN.toByte(), ODD.toByte(), ODD.toByte(), ODD.toByte(), EVEN.toByte(), EVEN.toByte()), // 6
                byteArrayOf(EVEN.toByte(), ODD.toByte(), EVEN.toByte(), ODD.toByte(), EVEN.toByte(), ODD.toByte()), // 7
                byteArrayOf(EVEN.toByte(), ODD.toByte(), EVEN.toByte(), ODD.toByte(), ODD.toByte(), EVEN.toByte()), // 8
                byteArrayOf(EVEN.toByte(), ODD.toByte(), ODD.toByte(), EVEN.toByte(), ODD.toByte(), EVEN.toByte())  // 9
        )

        /** Calculates the EAN parity character.
         * @param code the code
         * *
         * @return the parity character
         */
        fun calculateEANParity(code: String): Int {
            var mul = 3
            var total = 0
            for (k in code.length - 1 downTo 0) {
                val n = code[k] - '0'
                total += mul * n
                mul = mul xor 2
            }
            return (10 - total % 10) % 10
        }

        /** Converts an UPCA code into an UPCE code. If the code can not
         * be converted a null is returned.
         * @param text the code to convert. It must have 12 numeric characters
         * *
         * @return the 8 converted digits or null if the
         * * code could not be converted
         */
        fun convertUPCAtoUPCE(text: String): String? {
            if (text.length != 12 || !(text.startsWith("0") || text.startsWith("1")))
                return null
            if (text.substring(3, 6) == "000" || text.substring(3, 6) == "100"
                    || text.substring(3, 6) == "200") {
                if (text.substring(6, 8) == "00")
                    return text.substring(0, 1) + text.substring(1, 3) + text.substring(8, 11) + text.substring(3, 4) + text.substring(11)
            } else if (text.substring(4, 6) == "00") {
                if (text.substring(6, 9) == "000")
                    return text.substring(0, 1) + text.substring(1, 4) + text.substring(9, 11) + "3" + text.substring(11)
            } else if (text.substring(5, 6) == "0") {
                if (text.substring(6, 10) == "0000")
                    return text.substring(0, 1) + text.substring(1, 5) + text.substring(10, 11) + "4" + text.substring(11)
            } else if (text[10] >= '5') {
                if (text.substring(6, 10) == "0000")
                    return text.substring(0, 1) + text.substring(1, 6) + text.substring(10, 11) + text.substring(11)
            }
            return null
        }

        /** Creates the bars for the barcode EAN13 and UPCA.
         * @param _code the text with 13 digits
         * *
         * @return the barcode
         */
        fun getBarsEAN13(_code: String): ByteArray {
            val code = IntArray(_code.length)
            for (k in code.indices)
                code[k] = _code[k] - '0'
            val bars = ByteArray(TOTALBARS_EAN13)
            var pb = 0
            bars[pb++] = 1
            bars[pb++] = 1
            bars[pb++] = 1
            val sequence = PARITY13[code[0]]
            for (k in sequence.indices) {
                val c = code[k + 1]
                val stripes = BARS[c]
                if (sequence[k].toInt() == ODD) {
                    bars[pb++] = stripes[0]
                    bars[pb++] = stripes[1]
                    bars[pb++] = stripes[2]
                    bars[pb++] = stripes[3]
                } else {
                    bars[pb++] = stripes[3]
                    bars[pb++] = stripes[2]
                    bars[pb++] = stripes[1]
                    bars[pb++] = stripes[0]
                }
            }
            bars[pb++] = 1
            bars[pb++] = 1
            bars[pb++] = 1
            bars[pb++] = 1
            bars[pb++] = 1
            for (k in 7..12) {
                val c = code[k]
                val stripes = BARS[c]
                bars[pb++] = stripes[0]
                bars[pb++] = stripes[1]
                bars[pb++] = stripes[2]
                bars[pb++] = stripes[3]
            }
            bars[pb++] = 1
            bars[pb++] = 1
            bars[pb++] = 1
            return bars
        }

        /** Creates the bars for the barcode EAN8.
         * @param _code the text with 8 digits
         * *
         * @return the barcode
         */
        fun getBarsEAN8(_code: String): ByteArray {
            val code = IntArray(_code.length)
            for (k in code.indices)
                code[k] = _code[k] - '0'
            val bars = ByteArray(TOTALBARS_EAN8)
            var pb = 0
            bars[pb++] = 1
            bars[pb++] = 1
            bars[pb++] = 1
            for (k in 0..3) {
                val c = code[k]
                val stripes = BARS[c]
                bars[pb++] = stripes[0]
                bars[pb++] = stripes[1]
                bars[pb++] = stripes[2]
                bars[pb++] = stripes[3]
            }
            bars[pb++] = 1
            bars[pb++] = 1
            bars[pb++] = 1
            bars[pb++] = 1
            bars[pb++] = 1
            for (k in 4..7) {
                val c = code[k]
                val stripes = BARS[c]
                bars[pb++] = stripes[0]
                bars[pb++] = stripes[1]
                bars[pb++] = stripes[2]
                bars[pb++] = stripes[3]
            }
            bars[pb++] = 1
            bars[pb++] = 1
            bars[pb++] = 1
            return bars
        }

        /** Creates the bars for the barcode UPCE.
         * @param _code the text with 8 digits
         * *
         * @return the barcode
         */
        fun getBarsUPCE(_code: String): ByteArray {
            val code = IntArray(_code.length)
            for (k in code.indices)
                code[k] = _code[k] - '0'
            val bars = ByteArray(TOTALBARS_UPCE)
            val flip = code[0] != 0
            var pb = 0
            bars[pb++] = 1
            bars[pb++] = 1
            bars[pb++] = 1
            val sequence = PARITYE[code[code.size - 1]]
            for (k in 1..code.size - 1 - 1) {
                val c = code[k]
                val stripes = BARS[c]
                if (sequence[k - 1].toInt() == (if (flip) EVEN else ODD)) {
                    bars[pb++] = stripes[0]
                    bars[pb++] = stripes[1]
                    bars[pb++] = stripes[2]
                    bars[pb++] = stripes[3]
                } else {
                    bars[pb++] = stripes[3]
                    bars[pb++] = stripes[2]
                    bars[pb++] = stripes[1]
                    bars[pb++] = stripes[0]
                }
            }
            bars[pb++] = 1
            bars[pb++] = 1
            bars[pb++] = 1
            bars[pb++] = 1
            bars[pb++] = 1
            bars[pb++] = 1
            return bars
        }

        /** Creates the bars for the barcode supplemental 2.
         * @param _code the text with 2 digits
         * *
         * @return the barcode
         */
        fun getBarsSupplemental2(_code: String): ByteArray {
            val code = IntArray(2)
            for (k in code.indices)
                code[k] = _code[k] - '0'
            val bars = ByteArray(TOTALBARS_SUPP2)
            var pb = 0
            val parity = (code[0] * 10 + code[1]) % 4
            bars[pb++] = 1
            bars[pb++] = 1
            bars[pb++] = 2
            val sequence = PARITY2[parity]
            for (k in sequence.indices) {
                if (k == 1) {
                    bars[pb++] = 1
                    bars[pb++] = 1
                }
                val c = code[k]
                val stripes = BARS[c]
                if (sequence[k].toInt() == ODD) {
                    bars[pb++] = stripes[0]
                    bars[pb++] = stripes[1]
                    bars[pb++] = stripes[2]
                    bars[pb++] = stripes[3]
                } else {
                    bars[pb++] = stripes[3]
                    bars[pb++] = stripes[2]
                    bars[pb++] = stripes[1]
                    bars[pb++] = stripes[0]
                }
            }
            return bars
        }

        /** Creates the bars for the barcode supplemental 5.
         * @param _code the text with 5 digits
         * *
         * @return the barcode
         */
        fun getBarsSupplemental5(_code: String): ByteArray {
            val code = IntArray(5)
            for (k in code.indices)
                code[k] = _code[k] - '0'
            val bars = ByteArray(TOTALBARS_SUPP5)
            var pb = 0
            val parity = ((code[0] + code[2] + code[4]) * 3 + (code[1] + code[3]) * 9) % 10
            bars[pb++] = 1
            bars[pb++] = 1
            bars[pb++] = 2
            val sequence = PARITY5[parity]
            for (k in sequence.indices) {
                if (k != 0) {
                    bars[pb++] = 1
                    bars[pb++] = 1
                }
                val c = code[k]
                val stripes = BARS[c]
                if (sequence[k].toInt() == ODD) {
                    bars[pb++] = stripes[0]
                    bars[pb++] = stripes[1]
                    bars[pb++] = stripes[2]
                    bars[pb++] = stripes[3]
                } else {
                    bars[pb++] = stripes[3]
                    bars[pb++] = stripes[2]
                    bars[pb++] = stripes[1]
                    bars[pb++] = stripes[0]
                }
            }
            return bars
        }
    }
}
/** Creates new BarcodeEAN  */
