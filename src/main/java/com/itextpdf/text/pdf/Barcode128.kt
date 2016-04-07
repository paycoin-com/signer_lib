/*
 * $Id: 746a49386995c06e488be2ffde7cdac0e18e3ec3 $
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

import com.itextpdf.text.BaseColor
import com.itextpdf.text.Element
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Rectangle
import com.itextpdf.text.error_messages.MessageLocalization

/**
 * Implements the code 128 and UCC/EAN-128. Other symbologies are allowed in raw mode.
 *
 *
 * The code types allowed are:
 *
 *  * **CODE128** - plain barcode 128.
 *  * **CODE128_UCC** - support for UCC/EAN-128 with a full list of AI.
 *  * **CODE128_RAW** - raw mode. The code attribute has the actual codes from 0
 * to 105 followed by '&#92;uffff' and the human readable text.
 *
 * The default parameters are:
 *
 * x = 0.8f;
 * font = BaseFont.createFont("Helvetica", "winansi", false);
 * size = 8;
 * baseline = size;
 * barHeight = size * 3;
 * textAlignment = Element.ALIGN_CENTER;
 * codeType = CODE128;
 *
 * @author Paulo Soares
 */
class Barcode128 : Barcode() {
    init {
        try {
            x = 0.8f
            font = BaseFont.createFont("Helvetica", "winansi", false)
            size = 8f
            baseline = size
            barHeight = size * 3
            textAlignment = Element.ALIGN_CENTER
            codeType = Barcode.CODE128
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    enum class Barcode128CodeSet {
        A,
        B,
        C,
        AUTO;

        val startSymbol: Char
            get() {
                when (this) {
                    A -> return START_A
                    B -> return START_B
                    C -> return START_C
                    else -> return START_B
                }
            }
    }

    var codeSet = Barcode128CodeSet.AUTO

    /** Gets the maximum area that the barcode and the text, if
     * any, will occupy. The lower left corner is always (0, 0).
     * @return the size the barcode occupies.
     */
    override val barcodeSize: Rectangle
        get() {
            var fontX = 0f
            var fontY = 0f
            var fullCode: String
            if (font != null) {
                if (baseline > 0)
                    fontY = baseline - font.getFontDescriptor(BaseFont.DESCENT, size)
                else
                    fontY = -baseline + size
                if (codeType == Barcode.CODE128_RAW) {
                    val idx = code.indexOf('\uffff')
                    if (idx < 0)
                        fullCode = ""
                    else
                        fullCode = code.substring(idx + 1)
                } else if (codeType == Barcode.CODE128_UCC)
                    fullCode = getHumanReadableUCCEAN(code)
                else
                    fullCode = removeFNC1(code)
                fontX = font.getWidthPoint(if (altText != null) altText else fullCode, size)
            }
            if (codeType == Barcode.CODE128_RAW) {
                val idx = code.indexOf('\uffff')
                if (idx >= 0)
                    fullCode = code.substring(0, idx)
                else
                    fullCode = code
            } else {
                fullCode = getRawText(code, codeType == Barcode.CODE128_UCC, codeSet)
            }
            val len = fullCode.length
            var fullWidth = (len + 2).toFloat() * 11f * x + 2 * x
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
        val fullCode: String
        if (codeType == Barcode.CODE128_RAW) {
            val idx = code.indexOf('\uffff')
            if (idx < 0)
                fullCode = ""
            else
                fullCode = code.substring(idx + 1)
        } else if (codeType == Barcode.CODE128_UCC)
            fullCode = getHumanReadableUCCEAN(code)
        else
            fullCode = removeFNC1(code)
        var fontX = 0f
        if (font != null) {
            fontX = font.getWidthPoint(fullCode = if (altText != null) altText else fullCode, size)
        }
        val bCode: String
        if (codeType == Barcode.CODE128_RAW) {
            val idx = code.indexOf('\uffff')
            if (idx >= 0)
                bCode = code.substring(0, idx)
            else
                bCode = code
        } else {
            bCode = getRawText(code, codeType == Barcode.CODE128_UCC, codeSet)
        }
        val len = bCode.length
        val fullWidth = (len + 2).toFloat() * 11f * x + 2 * x
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
        val bars = getBarsCode128Raw(bCode)
        var print = true
        if (barColor != null)
            cb.setColorFill(barColor)
        for (k in bars.indices) {
            val w = bars[k] * x
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

    /**
     * Sets the code to generate. If it's an UCC code and starts with '(' it will
     * be split by the AI. This code in UCC mode is valid:
     *
     *
     * `(01)00000090311314(10)ABC123(15)060916`
     * @param code the code to generate
     */
    override var code: String
        get() = super.code
        set(code) = if (codeType == Barcode128.CODE128_UCC && code.startsWith("(")) {
            var idx = 0
            val ret = StringBuilder("")
            while (idx >= 0) {
                val end = code.indexOf(')', idx)
                if (end < 0)
                    throw IllegalArgumentException(MessageLocalization.getComposedMessage("badly.formed.ucc.string.1", code))
                var sai = code.substring(idx + 1, end)
                if (sai.length < 2)
                    throw IllegalArgumentException(MessageLocalization.getComposedMessage("ai.too.short.1", sai))
                val ai = Integer.parseInt(sai)
                val len = ais.get(ai)
                if (len == 0)
                    throw IllegalArgumentException(MessageLocalization.getComposedMessage("ai.not.found.1", sai))
                sai = ai.toString()
                if (sai.length == 1)
                    sai = "0" + sai
                idx = code.indexOf('(', end)
                val next = if (idx < 0) code.length else idx
                ret.append(sai).append(code.substring(end + 1, next))
                if (len < 0) {
                    if (idx >= 0)
                        ret.append(FNC1)
                } else if (next - end - 1 + sai.length != len)
                    throw IllegalArgumentException(MessageLocalization.getComposedMessage("invalid.ai.length.1", sai))
            }
            super.code = ret.toString()
        } else
            super.code = code

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
        val bCode: String
        if (codeType == Barcode.CODE128_RAW) {
            val idx = code.indexOf('\uffff')
            if (idx >= 0)
                bCode = code.substring(0, idx)
            else
                bCode = code
        } else {
            bCode = getRawText(code, codeType == Barcode.CODE128_UCC)
        }
        val len = bCode.length
        val fullWidth = (len + 2) * 11 + 2
        val bars = getBarsCode128Raw(bCode)

        var print = true
        var ptr = 0
        val height = barHeight.toInt()
        val pix = IntArray(fullWidth * height)
        for (k in bars.indices) {
            val w = bars[k].toInt()
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
        private val BARS = arrayOf(byteArrayOf(2, 1, 2, 2, 2, 2), byteArrayOf(2, 2, 2, 1, 2, 2), byteArrayOf(2, 2, 2, 2, 2, 1), byteArrayOf(1, 2, 1, 2, 2, 3), byteArrayOf(1, 2, 1, 3, 2, 2), byteArrayOf(1, 3, 1, 2, 2, 2), byteArrayOf(1, 2, 2, 2, 1, 3), byteArrayOf(1, 2, 2, 3, 1, 2), byteArrayOf(1, 3, 2, 2, 1, 2), byteArrayOf(2, 2, 1, 2, 1, 3), byteArrayOf(2, 2, 1, 3, 1, 2), byteArrayOf(2, 3, 1, 2, 1, 2), byteArrayOf(1, 1, 2, 2, 3, 2), byteArrayOf(1, 2, 2, 1, 3, 2), byteArrayOf(1, 2, 2, 2, 3, 1), byteArrayOf(1, 1, 3, 2, 2, 2), byteArrayOf(1, 2, 3, 1, 2, 2), byteArrayOf(1, 2, 3, 2, 2, 1), byteArrayOf(2, 2, 3, 2, 1, 1), byteArrayOf(2, 2, 1, 1, 3, 2), byteArrayOf(2, 2, 1, 2, 3, 1), byteArrayOf(2, 1, 3, 2, 1, 2), byteArrayOf(2, 2, 3, 1, 1, 2), byteArrayOf(3, 1, 2, 1, 3, 1), byteArrayOf(3, 1, 1, 2, 2, 2), byteArrayOf(3, 2, 1, 1, 2, 2), byteArrayOf(3, 2, 1, 2, 2, 1), byteArrayOf(3, 1, 2, 2, 1, 2), byteArrayOf(3, 2, 2, 1, 1, 2), byteArrayOf(3, 2, 2, 2, 1, 1), byteArrayOf(2, 1, 2, 1, 2, 3), byteArrayOf(2, 1, 2, 3, 2, 1), byteArrayOf(2, 3, 2, 1, 2, 1), byteArrayOf(1, 1, 1, 3, 2, 3), byteArrayOf(1, 3, 1, 1, 2, 3), byteArrayOf(1, 3, 1, 3, 2, 1), byteArrayOf(1, 1, 2, 3, 1, 3), byteArrayOf(1, 3, 2, 1, 1, 3), byteArrayOf(1, 3, 2, 3, 1, 1), byteArrayOf(2, 1, 1, 3, 1, 3), byteArrayOf(2, 3, 1, 1, 1, 3), byteArrayOf(2, 3, 1, 3, 1, 1), byteArrayOf(1, 1, 2, 1, 3, 3), byteArrayOf(1, 1, 2, 3, 3, 1), byteArrayOf(1, 3, 2, 1, 3, 1), byteArrayOf(1, 1, 3, 1, 2, 3), byteArrayOf(1, 1, 3, 3, 2, 1), byteArrayOf(1, 3, 3, 1, 2, 1), byteArrayOf(3, 1, 3, 1, 2, 1), byteArrayOf(2, 1, 1, 3, 3, 1), byteArrayOf(2, 3, 1, 1, 3, 1), byteArrayOf(2, 1, 3, 1, 1, 3), byteArrayOf(2, 1, 3, 3, 1, 1), byteArrayOf(2, 1, 3, 1, 3, 1), byteArrayOf(3, 1, 1, 1, 2, 3), byteArrayOf(3, 1, 1, 3, 2, 1), byteArrayOf(3, 3, 1, 1, 2, 1), byteArrayOf(3, 1, 2, 1, 1, 3), byteArrayOf(3, 1, 2, 3, 1, 1), byteArrayOf(3, 3, 2, 1, 1, 1), byteArrayOf(3, 1, 4, 1, 1, 1), byteArrayOf(2, 2, 1, 4, 1, 1), byteArrayOf(4, 3, 1, 1, 1, 1), byteArrayOf(1, 1, 1, 2, 2, 4), byteArrayOf(1, 1, 1, 4, 2, 2), byteArrayOf(1, 2, 1, 1, 2, 4), byteArrayOf(1, 2, 1, 4, 2, 1), byteArrayOf(1, 4, 1, 1, 2, 2), byteArrayOf(1, 4, 1, 2, 2, 1), byteArrayOf(1, 1, 2, 2, 1, 4), byteArrayOf(1, 1, 2, 4, 1, 2), byteArrayOf(1, 2, 2, 1, 1, 4), byteArrayOf(1, 2, 2, 4, 1, 1), byteArrayOf(1, 4, 2, 1, 1, 2), byteArrayOf(1, 4, 2, 2, 1, 1), byteArrayOf(2, 4, 1, 2, 1, 1), byteArrayOf(2, 2, 1, 1, 1, 4), byteArrayOf(4, 1, 3, 1, 1, 1), byteArrayOf(2, 4, 1, 1, 1, 2), byteArrayOf(1, 3, 4, 1, 1, 1), byteArrayOf(1, 1, 1, 2, 4, 2), byteArrayOf(1, 2, 1, 1, 4, 2), byteArrayOf(1, 2, 1, 2, 4, 1), byteArrayOf(1, 1, 4, 2, 1, 2), byteArrayOf(1, 2, 4, 1, 1, 2), byteArrayOf(1, 2, 4, 2, 1, 1), byteArrayOf(4, 1, 1, 2, 1, 2), byteArrayOf(4, 2, 1, 1, 1, 2), byteArrayOf(4, 2, 1, 2, 1, 1), byteArrayOf(2, 1, 2, 1, 4, 1), byteArrayOf(2, 1, 4, 1, 2, 1), byteArrayOf(4, 1, 2, 1, 2, 1), byteArrayOf(1, 1, 1, 1, 4, 3), byteArrayOf(1, 1, 1, 3, 4, 1), byteArrayOf(1, 3, 1, 1, 4, 1), byteArrayOf(1, 1, 4, 1, 1, 3), byteArrayOf(1, 1, 4, 3, 1, 1), byteArrayOf(4, 1, 1, 1, 1, 3), byteArrayOf(4, 1, 1, 3, 1, 1), byteArrayOf(1, 1, 3, 1, 4, 1), byteArrayOf(1, 1, 4, 1, 3, 1), byteArrayOf(3, 1, 1, 1, 4, 1), byteArrayOf(4, 1, 1, 1, 3, 1), byteArrayOf(2, 1, 1, 4, 1, 2), byteArrayOf(2, 1, 1, 2, 1, 4), byteArrayOf(2, 1, 1, 2, 3, 2))

        /** The stop bars.
         */
        private val BARS_STOP = byteArrayOf(2, 3, 3, 1, 1, 1, 2)
        /** The charset code change.
         */
        val CODE_AB_TO_C: Char = 99.toChar()
        /** The charset code change.
         */
        val CODE_AC_TO_B: Char = 100.toChar()
        /** The charset code change.
         */
        val CODE_BC_TO_A: Char = 101.toChar()
        /** The code for UCC/EAN-128.
         */
        val FNC1_INDEX: Char = 102.toChar()
        /** The start code.
         */
        val START_A: Char = 103.toChar()
        /** The start code.
         */
        val START_B: Char = 104.toChar()
        /** The start code.
         */
        val START_C: Char = 105.toChar()

        val FNC1 = '\u00ca'
        val DEL = '\u00c3'
        val FNC3 = '\u00c4'
        val FNC2 = '\u00c5'
        val SHIFT = '\u00c6'
        val CODE_C = '\u00c7'
        val CODE_A = '\u00c8'
        val FNC4 = '\u00c8'
        val STARTA = '\u00cb'
        val STARTB = '\u00cc'
        val STARTC = '\u00cd'

        private val ais = IntHashtable()

        /**
         * Removes the FNC1 codes in the text.
         * @param code the text to clean
         * *
         * @return the cleaned text
         */
        fun removeFNC1(code: String): String {
            val len = code.length
            val buf = StringBuffer(len)
            for (k in 0..len - 1) {
                val c = code[k]
                if (c.toInt() >= 32 && c.toInt() <= 126)
                    buf.append(c)
            }
            return buf.toString()
        }

        /**
         * Gets the human readable text of a sequence of AI.
         * @param code the text
         * *
         * @return the human readable text
         */
        fun getHumanReadableUCCEAN(code: String): String {
            var code = code
            val buf = StringBuffer()
            val fnc1 = FNC1.toString()
            try {
                while (true) {
                    if (code.startsWith(fnc1)) {
                        code = code.substring(1)
                        continue
                    }
                    var n = 0
                    var idlen = 0
                    for (k in 2..4) {
                        if (code.length < k)
                            break
                        if ((n = ais.get(Integer.parseInt(code.substring(0, k)))) != 0) {
                            idlen = k
                            break
                        }
                    }
                    if (idlen == 0)
                        break
                    buf.append('(').append(code.substring(0, idlen)).append(')')
                    code = code.substring(idlen)
                    if (n > 0) {
                        n -= idlen
                        if (code.length <= n)
                            break
                        buf.append(removeFNC1(code.substring(0, n)))
                        code = code.substring(n)
                    } else {
                        val idx = code.indexOf(FNC1.toInt())
                        if (idx < 0)
                            break
                        buf.append(code.substring(0, idx))
                        code = code.substring(idx + 1)
                    }
                }
            } catch (e: Exception) {
                //empty
            }

            buf.append(removeFNC1(code))
            return buf.toString()
        }

        /** Returns true if the next numDigits
         * starting from index textIndex are numeric skipping any FNC1.
         * @param text the text to check
         * *
         * @param textIndex where to check from
         * *
         * @param numDigits the number of digits to check
         * *
         * @return the check result
         */
        internal fun isNextDigits(text: String, textIndex: Int, numDigits: Int): Boolean {
            var textIndex = textIndex
            var numDigits = numDigits
            val len = text.length
            while (textIndex < len && numDigits > 0) {
                if (text[textIndex] == FNC1) {
                    ++textIndex
                    continue
                }
                var n = Math.min(2, numDigits)
                if (textIndex + n > len)
                    return false
                while (n-- > 0) {
                    val c = text[textIndex++]
                    if (c < '0' || c > '9')
                        return false
                    --numDigits
                }
            }
            return numDigits == 0
        }

        /** Packs the digits for charset C also considering FNC1. It assumes that all the parameters
         * are valid.
         * @param text the text to pack
         * *
         * @param textIndex where to pack from
         * *
         * @param numDigits the number of digits to pack. It is always an even number
         * *
         * @return the packed digits, two digits per character
         */
        internal fun getPackedRawDigits(text: String, textIndex: Int, numDigits: Int): String {
            var textIndex = textIndex
            var numDigits = numDigits
            val out = StringBuilder("")
            val start = textIndex
            while (numDigits > 0) {
                if (text[textIndex] == FNC1) {
                    out.append(FNC1_INDEX)
                    ++textIndex
                    continue
                }
                numDigits -= 2
                val c1 = text[textIndex++] - '0'
                val c2 = text[textIndex++] - '0'
                out.append((c1 * 10 + c2).toChar())
            }
            return (textIndex - start).toChar() + out.toString()
        }

        /** Converts the human readable text to the characters needed to
         * create a barcode using the specified code set.
         * @param text the text to convert
         * *
         * @param ucc true if it is an UCC/EAN-128. In this case
         * * the character FNC1 is added
         * *
         * @param codeSet forced code set, or AUTO for optimized barcode.
         * *
         * @return the code ready to be fed to getBarsCode128Raw()
         */
        @JvmOverloads fun getRawText(text: String, ucc: Boolean, codeSet: Barcode128CodeSet = Barcode128CodeSet.AUTO): String {
            var out = ""
            val tLen = text.length
            if (tLen == 0) {
                out += codeSet.startSymbol
                if (ucc)
                    out += FNC1_INDEX
                return out
            }
            var c = 0
            for (k in 0..tLen - 1) {
                c = text[k].toInt()
                if (c > 127 && c != FNC1.toInt())
                    throw RuntimeException(MessageLocalization.getComposedMessage("there.are.illegal.characters.for.barcode.128.in.1", text))
            }
            c = text[0].toInt()
            var currentCode = START_B
            var index = 0
            if ((codeSet == Barcode128CodeSet.AUTO || codeSet == Barcode128CodeSet.C) && isNextDigits(text, index, 2)) {
                currentCode = START_C
                out += currentCode
                if (ucc)
                    out += FNC1_INDEX
                val out2 = getPackedRawDigits(text, index, 2)
                index += out2[0].toInt()
                out += out2.substring(1)
            } else if (c < ' ') {
                currentCode = START_A
                out += currentCode
                if (ucc)
                    out += FNC1_INDEX
                out += (c + 64).toChar()
                ++index
            } else {
                out += currentCode
                if (ucc)
                    out += FNC1_INDEX
                if (c == FNC1.toInt())
                    out += FNC1_INDEX
                else
                    out += (c - ' ').toChar()
                ++index
            }
            if (codeSet != Barcode128CodeSet.AUTO && currentCode != codeSet.startSymbol)
                throw RuntimeException(MessageLocalization.getComposedMessage("there.are.illegal.characters.for.barcode.128.in.1", text))
            while (index < tLen) {
                when (currentCode) {
                    START_A -> {
                        if (codeSet == Barcode128CodeSet.AUTO && isNextDigits(text, index, 4)) {
                            currentCode = START_C
                            out += CODE_AB_TO_C
                            val out2 = getPackedRawDigits(text, index, 4)
                            index += out2[0].toInt()
                            out += out2.substring(1)
                        } else {
                            c = text[index++].toInt()
                            if (c == FNC1.toInt())
                                out += FNC1_INDEX
                            else if (c > '_') {
                                currentCode = START_B
                                out += CODE_AC_TO_B
                                out += (c - ' ').toChar()
                            } else if (c < ' ')
                                out += (c + 64).toChar()
                            else
                                out += (c - ' ').toChar()
                        }
                    }
                    START_B -> {
                        if (codeSet == Barcode128CodeSet.AUTO && isNextDigits(text, index, 4)) {
                            currentCode = START_C
                            out += CODE_AB_TO_C
                            val out2 = getPackedRawDigits(text, index, 4)
                            index += out2[0].toInt()
                            out += out2.substring(1)
                        } else {
                            c = text[index++].toInt()
                            if (c == FNC1.toInt())
                                out += FNC1_INDEX
                            else if (c < ' ') {
                                currentCode = START_A
                                out += CODE_BC_TO_A
                                out += (c + 64).toChar()
                            } else {
                                out += (c - ' ').toChar()
                            }
                        }
                    }
                    START_C -> {
                        if (isNextDigits(text, index, 2)) {
                            val out2 = getPackedRawDigits(text, index, 2)
                            index += out2[0].toInt()
                            out += out2.substring(1)
                        } else {
                            c = text[index++].toInt()
                            if (c == FNC1.toInt())
                                out += FNC1_INDEX
                            else if (c < ' ') {
                                currentCode = START_A
                                out += CODE_BC_TO_A
                                out += (c + 64).toChar()
                            } else {
                                currentCode = START_B
                                out += CODE_AC_TO_B
                                out += (c - ' ').toChar()
                            }
                        }
                    }
                }
                if (codeSet != Barcode128CodeSet.AUTO && currentCode != codeSet.startSymbol)
                    throw RuntimeException(MessageLocalization.getComposedMessage("there.are.illegal.characters.for.barcode.128.in.1", text))
            }
            return out
        }

        /** Generates the bars. The input has the actual barcodes, not
         * the human readable text.
         * @param text the barcode
         * *
         * @return the bars
         */
        fun getBarsCode128Raw(text: String): ByteArray {
            var text = text
            val idx = text.indexOf('\uffff')
            if (idx >= 0)
                text = text.substring(0, idx)
            var chk = text[0].toInt()
            for (k in 1..text.length - 1)
                chk += k * text[k].toInt()
            chk = chk % 103
            text += chk.toChar()
            val bars = ByteArray((text.length + 1) * 6 + 7)
            var k: Int
            k = 0
            while (k < text.length) {
                System.arraycopy(BARS[text[k]], 0, bars, k * 6, 6)
                ++k
            }
            System.arraycopy(BARS_STOP, 0, bars, k * 6, 7)
            return bars
        }

        init {
            ais.put(0, 20)
            ais.put(1, 16)
            ais.put(2, 16)
            ais.put(10, -1)
            ais.put(11, 9)
            ais.put(12, 8)
            ais.put(13, 8)
            ais.put(15, 8)
            ais.put(17, 8)
            ais.put(20, 4)
            ais.put(21, -1)
            ais.put(22, -1)
            ais.put(23, -1)
            ais.put(240, -1)
            ais.put(241, -1)
            ais.put(250, -1)
            ais.put(251, -1)
            ais.put(252, -1)
            ais.put(30, -1)
            for (k in 3100..3699)
                ais.put(k, 10)
            ais.put(37, -1)
            for (k in 3900..3939)
                ais.put(k, -1)
            ais.put(400, -1)
            ais.put(401, -1)
            ais.put(402, 20)
            ais.put(403, -1)
            for (k in 410..415)
                ais.put(k, 16)
            ais.put(420, -1)
            ais.put(421, -1)
            ais.put(422, 6)
            ais.put(423, -1)
            ais.put(424, 6)
            ais.put(425, 6)
            ais.put(426, 6)
            ais.put(7001, 17)
            ais.put(7002, -1)
            for (k in 7030..7039)
                ais.put(k, -1)
            ais.put(8001, 18)
            ais.put(8002, -1)
            ais.put(8003, -1)
            ais.put(8004, -1)
            ais.put(8005, 10)
            ais.put(8006, 22)
            ais.put(8007, -1)
            ais.put(8008, -1)
            ais.put(8018, 22)
            ais.put(8020, -1)
            ais.put(8100, 10)
            ais.put(8101, 14)
            ais.put(8102, 6)
            for (k in 90..99)
                ais.put(k, -1)
        }
    }
}
/** Creates new Barcode128  */
/** Converts the human readable text to the characters needed to
 * create a barcode. Some optimization is done to get the shortest code.
 * @param text the text to convert
 * *
 * @param ucc true if it is an UCC/EAN-128. In this case
 * * the character FNC1 is added
 * *
 * @return the code ready to be fed to getBarsCode128Raw()
 */
