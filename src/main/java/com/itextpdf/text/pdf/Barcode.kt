/*
 * $Id: 93509c44f754e809e18068e4dfaaba36eee2bb62 $
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

import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.BaseColor

/** Base class containing properties and methods common to all
 * barcode types.

 * @author Paulo Soares
 */
abstract class Barcode {

    /** The minimum bar width.
     */
    /** Gets the minimum bar width.
     * @return the minimum bar width
     */
    /** Sets the minimum bar width.
     * @param x the minimum bar width
     */
    var x: Float = 0.toFloat()

    /** The bar multiplier for wide bars or the distance between
     * bars for Postnet and Planet.
     */
    /** Gets the bar multiplier for wide bars.
     * @return the bar multiplier for wide bars
     */
    /** Sets the bar multiplier for wide bars.
     * @param n the bar multiplier for wide bars
     */
    var n: Float = 0.toFloat()

    /** The text font. null if no text.
     */
    /** Gets the text font. null if no text.
     * @return the text font. null if no text
     */
    /** Sets the text font.
     * @param font the text font. Set to null to suppress any text
     */
    var font: BaseFont

    /** The size of the text or the height of the shorter bar
     * in Postnet.
     */
    /** Gets the size of the text.
     * @return the size of the text
     */
    /** Sets the size of the text.
     * @param size the size of the text
     */
    var size: Float = 0.toFloat()

    /** If positive, the text distance under the bars. If zero or negative,
     * the text distance above the bars.
     */
    /** Gets the text baseline.
     * If positive, the text distance under the bars. If zero or negative,
     * the text distance above the bars.
     * @return the baseline.
     */
    /** Sets the text baseline.
     * If positive, the text distance under the bars. If zero or negative,
     * the text distance above the bars.
     * @param baseline the baseline.
     */
    var baseline: Float = 0.toFloat()

    /** The height of the bars.
     */
    /** Gets the height of the bars.
     * @return the height of the bars
     */
    /** Sets the height of the bars.
     * @param barHeight the height of the bars
     */
    var barHeight: Float = 0.toFloat()

    /** The text alignment. Can be Element.ALIGN_LEFT,
     * Element.ALIGN_CENTER or Element.ALIGN_RIGHT.
     */
    /** Gets the text alignment. Can be Element.ALIGN_LEFT,
     * Element.ALIGN_CENTER or Element.ALIGN_RIGHT.
     * @return the text alignment
     */
    /** Sets the text alignment. Can be Element.ALIGN_LEFT,
     * Element.ALIGN_CENTER or Element.ALIGN_RIGHT.
     * @param textAlignment the text alignment
     */
    var textAlignment: Int = 0

    /** The optional checksum generation.
     */
    /** Gets the optional checksum generation.
     * @return the optional checksum generation
     */
    /** Setter for property generateChecksum.
     * @param generateChecksum New value of property generateChecksum.
     */
    var isGenerateChecksum: Boolean = false

    /** Shows the generated checksum in the the text.
     */
    /** Gets the property to show the generated checksum in the the text.
     * @return value of property checksumText
     */
    /** Sets the property to show the generated checksum in the the text.
     * @param checksumText new value of property checksumText
     */
    var isChecksumText: Boolean = false

    /** Show the start and stop character '*' in the text for
     * the barcode 39 or 'ABCD' for codabar.
     */
    /** Sets the property to show the start and stop character '*' in the text for
     * the barcode 39.
     * @return value of property startStopText
     */
    /** Gets the property to show the start and stop character '*' in the text for
     * the barcode 39.
     * @param startStopText new value of property startStopText
     */
    var isStartStopText: Boolean = false

    /** Generates extended barcode 39.
     */
    /** Gets the property to generate extended barcode 39.
     * @return value of property extended.
     */
    /** Sets the property to generate extended barcode 39.
     * @param extended new value of property extended
     */
    var isExtended: Boolean = false

    /** The code to generate.
     */
    /** Gets the code to generate.
     * @return the code to generate
     */
    /** Sets the code to generate.
     * @param code the code to generate
     */
    var code = ""

    /** Show the guard bars for barcode EAN.
     */
    /** Gets the property to show the guard bars for barcode EAN.
     * @return value of property guardBars
     */
    /** Sets the property to show the guard bars for barcode EAN.
     * @param guardBars new value of property guardBars
     */
    var isGuardBars: Boolean = false

    /** The code type.
     */
    /** Gets the code type.
     * @return the code type
     */
    /** Sets the code type.
     * @param codeType the code type
     */
    var codeType: Int = 0

    /** The ink spreading.  */
    /** Gets the amount of ink spreading.
     * @return the ink spreading
     */
    /** Sets the amount of ink spreading. This value will be subtracted
     * to the width of each bar. The actual value will depend on the ink
     * and the printing medium.
     * @param inkSpreading the ink spreading
     */
    var inkSpreading = 0f

    /** Gets the maximum area that the barcode and the text, if
     * any, will occupy. The lower left corner is always (0, 0).
     * @return the size the barcode occupies.
     */
    abstract val barcodeSize: Rectangle

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
    abstract fun placeBarcode(cb: PdfContentByte, barColor: BaseColor, textColor: BaseColor): Rectangle

    /** Creates a template with the barcode.
     * @param cb the PdfContentByte to create the template. It
     * * serves no other use
     * *
     * @param barColor the color of the bars. It can be null
     * *
     * @param textColor the color of the text. It can be null
     * *
     * @return the template
     * *
     * @see .placeBarcode
     */
    fun createTemplateWithBarcode(cb: PdfContentByte, barColor: BaseColor, textColor: BaseColor): PdfTemplate {
        val tp = cb.createTemplate(0f, 0f)
        val rect = placeBarcode(tp, barColor, textColor)
        tp.boundingBox = rect
        return tp
    }

    /** Creates an Image with the barcode.
     * @param cb the PdfContentByte to create the Image. It
     * * serves no other use
     * *
     * @param barColor the color of the bars. It can be null
     * *
     * @param textColor the color of the text. It can be null
     * *
     * @return the Image
     * *
     * @see .placeBarcode
     */
    fun createImageWithBarcode(cb: PdfContentByte, barColor: BaseColor, textColor: BaseColor): Image {
        try {
            return Image.getInstance(createTemplateWithBarcode(cb, barColor, textColor))
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    /**
     * The alternate text to be used, if present.
     */
    /**
     * Gets the alternate text.
     * @return the alternate text
     */
    /**
     * Sets the alternate text. If present, this text will be used instead of the
     * text derived from the supplied code.
     * @param altText the alternate text
     */
    var altText: String

    // AWT related methods (remove this if you port to Android / GAE)

    /** Creates a java.awt.Image. This image only
     * contains the bars without any text.
     * @param foreground the color of the bars
     * *
     * @param background the color of the background
     * *
     * @return the image
     */
    abstract fun createAwtImage(foreground: java.awt.Color, background: java.awt.Color): java.awt.Image

    companion object {
        /** A type of barcode  */
        val EAN13 = 1
        /** A type of barcode  */
        val EAN8 = 2
        /** A type of barcode  */
        val UPCA = 3
        /** A type of barcode  */
        val UPCE = 4
        /** A type of barcode  */
        val SUPP2 = 5
        /** A type of barcode  */
        val SUPP5 = 6
        /** A type of barcode  */
        val POSTNET = 7
        /** A type of barcode  */
        val PLANET = 8
        /** A type of barcode  */
        val CODE128 = 9
        /** A type of barcode  */
        val CODE128_UCC = 10
        /** A type of barcode  */
        val CODE128_RAW = 11
        /** A type of barcode  */
        val CODABAR = 12
    }
}
