/*
 * $Id: 8901d3069b7b4ed954484487ff20715139d60e6a $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Kevin Day, Bruno Lowagie, Paulo Soares, et al.
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
package com.itextpdf.text.pdf.parser

import java.io.UnsupportedEncodingException
import java.util.ArrayList

import com.itextpdf.text.BaseColor
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.DocumentFont
import com.itextpdf.text.pdf.PdfString

/**
 * Provides information and calculations needed by render listeners
 * to display/evaluate text render operations.
 *
 * This is passed between the [PdfContentStreamProcessor] and
 * [RenderListener] objects as text rendering operations are
 * discovered
 */
class TextRenderInfo {

    /**
     * @return original PDF string
     */
    val pdfString: PdfString
    private var text: String? = null
    private val textToUserSpaceTransformMatrix: Matrix
    private val gs: GraphicsState
    private var unscaledWidth: Float? = null
    private var fontMatrix: DoubleArray? = null
    /**
     * Array containing marked content info for the text.
     * @since 5.0.2
     */
    private val markedContentInfos: Collection<MarkedContentInfo>

    /**
     * Creates a new TextRenderInfo object
     * @param string the PDF string that should be displayed
     * *
     * @param gs the graphics state (note: at this time, this is not immutable, so don't cache it)
     * *
     * @param textMatrix the text matrix at the time of the render operation
     * *
     * @param markedContentInfo the marked content sequence, if available
     */
    internal constructor(string: PdfString, gs: GraphicsState, textMatrix: Matrix, markedContentInfo: Collection<MarkedContentInfo>) {
        this.pdfString = string
        this.textToUserSpaceTransformMatrix = textMatrix.multiply(gs.ctm)
        this.gs = gs
        this.markedContentInfos = ArrayList(markedContentInfo)
        this.fontMatrix = gs.font!!.fontMatrix
    }

    /**
     * Used for creating sub-TextRenderInfos for each individual character
     * @param parent the parent TextRenderInfo
     * *
     * @param string the content of a TextRenderInfo
     * *
     * @param horizontalOffset the unscaled horizontal offset of the character that this TextRenderInfo represents
     * *
     * @since 5.3.3
     */
    private constructor(parent: TextRenderInfo, string: PdfString, horizontalOffset: Float) {
        this.pdfString = string
        this.textToUserSpaceTransformMatrix = Matrix(horizontalOffset, 0f).multiply(parent.textToUserSpaceTransformMatrix)
        this.gs = parent.gs
        this.markedContentInfos = parent.markedContentInfos
        this.fontMatrix = gs.font!!.fontMatrix
    }

    /**
     * @return the text to render
     */
    fun getText(): String {
        if (text == null)
            text = decode(pdfString)
        return text
    }

    /**
     * Checks if the text belongs to a marked content sequence
     * with a given mcid.
     * @param mcid a marked content id
     * *
     * @param checkTheTopmostLevelOnly indicates whether to check the topmost level of marked content stack only
     * *
     * @return true if the text is marked with this id
     * *
     * @since 5.3.5
     */
    @JvmOverloads fun hasMcid(mcid: Int, checkTheTopmostLevelOnly: Boolean = false): Boolean {
        if (checkTheTopmostLevelOnly) {
            if (markedContentInfos is ArrayList<Any>) {
                val infoMcid = mcid
                return if (infoMcid != null) infoMcid === mcid else false
            }
        } else {
            for (info in markedContentInfos) {
                if (info.hasMcid())
                    if (info.mcid == mcid)
                        return true
            }
        }
        return false
    }

    /**
     * @return the marked content associated with the TextRenderInfo instance.
     */
    val mcid: Int?
        get() {
            if (markedContentInfos is ArrayList<Any>) {
                val mci = markedContentInfos as ArrayList<MarkedContentInfo>
                val info = if (mci.size > 0) mci[mci.size - 1] else null
                return if (info != null && info.hasMcid()) info.mcid else null
            }
            return null
        }

    /**
     * @return the unscaled (i.e. in Text space) width of the text
     */
    internal fun getUnscaledWidth(): Float {
        if (unscaledWidth == null)
            unscaledWidth = java.lang.Float.valueOf(getPdfStringWidth(pdfString, false))
        return unscaledWidth!!
    }

    /**
     * Gets the baseline for the text (i.e. the line that the text 'sits' on)
     * This value includes the Rise of the draw operation - see [.getRise] for the amount added by Rise
     * @return the baseline line segment
     * *
     * @since 5.0.2
     */
    val baseline: LineSegment
        get() = getUnscaledBaselineWithOffset(0 + gs.rise).transformBy(textToUserSpaceTransformMatrix)

    val unscaledBaseline: LineSegment
        get() = getUnscaledBaselineWithOffset(0 + gs.rise)

    /**
     * Gets the ascentline for the text (i.e. the line that represents the topmost extent that a string of the current font could have)
     * This value includes the Rise of the draw operation - see [.getRise] for the amount added by Rise
     * @return the ascentline line segment
     * *
     * @since 5.0.2
     */
    val ascentLine: LineSegment
        get() {
            val ascent = gs.font.getFontDescriptor(BaseFont.ASCENT, gs.fontSize)
            return getUnscaledBaselineWithOffset(ascent + gs.rise).transformBy(textToUserSpaceTransformMatrix)
        }

    /**
     * Gets the descentline for the text (i.e. the line that represents the bottom most extent that a string of the current font could have).
     * This value includes the Rise of the draw operation - see [.getRise] for the amount added by Rise
     * @return the descentline line segment
     * *
     * @since 5.0.2
     */
    // per getFontDescription() API, descent is returned as a negative number, so we apply that as a normal vertical offset
    val descentLine: LineSegment
        get() {
            val descent = gs.font.getFontDescriptor(BaseFont.DESCENT, gs.fontSize)
            return getUnscaledBaselineWithOffset(descent + gs.rise).transformBy(textToUserSpaceTransformMatrix)
        }

    private fun getUnscaledBaselineWithOffset(yOffset: Float): LineSegment {
        // we need to correct the width so we don't have an extra character and word spaces at the end.  The extra character and word spaces
        // are important for tracking relative text coordinate systems, but should not be part of the baseline
        val unicodeStr = pdfString.toUnicodeString()

        val correctedUnscaledWidth = getUnscaledWidth() - (gs.characterSpacing + if (unicodeStr.length > 0 && unicodeStr[unicodeStr.length - 1] == ' ') gs.wordSpacing else 0) * gs.horizontalScaling

        return LineSegment(Vector(0f, yOffset, 1f), Vector(correctedUnscaledWidth, yOffset, 1f))
    }

    /**
     * Getter for the font
     * @return the font
     * *
     * @since iText 5.0.2
     */
    val font: DocumentFont
        get() = gs.font

    // removing - this shouldn't be needed now that we are exposing getCharacterRenderInfos()
    //	/**
    //	 * @return The character spacing width, in user space units (Tc value, scaled to user space)
    //	 * @since 5.3.3
    //	 */
    //	public float getCharacterSpacing(){
    //		return convertWidthFromTextSpaceToUserSpace(gs.characterSpacing);
    //	}
    //	
    //	/**
    //	 * @return The word spacing width, in user space units (Tw value, scaled to user space)
    //	 * @since 5.3.3
    //	 */
    //	public float getWordSpacing(){
    //		return convertWidthFromTextSpaceToUserSpace(gs.wordSpacing);
    //	}

    /**
     * The rise represents how far above the nominal baseline the text should be rendered.  The [.getBaseline], [.getAscentLine] and [.getDescentLine] methods already include Rise.
     * This method is exposed to allow listeners to determine if an explicit rise was involved in the computation of the baseline (this might be useful, for example, for identifying superscript rendering)
     * @return The Rise for the text draw operation, in user space units (Ts value, scaled to user space)
     * *
     * @since 5.3.3
     */
    // optimize the common case
    val rise: Float
        get() {
            if (gs.rise == 0f) return 0f

            return convertHeightFromTextSpaceToUserSpace(gs.rise)
        }

    /**

     * @param width the width, in text space
     * *
     * @return the width in user space
     * *
     * @since 5.3.3
     */
    private fun convertWidthFromTextSpaceToUserSpace(width: Float): Float {
        val textSpace = LineSegment(Vector(0f, 0f, 1f), Vector(width, 0f, 1f))
        val userSpace = textSpace.transformBy(textToUserSpaceTransformMatrix)
        return userSpace.length
    }

    /**

     * @param height the height, in text space
     * *
     * @return the height in user space
     * *
     * @since 5.3.3
     */
    private fun convertHeightFromTextSpaceToUserSpace(height: Float): Float {
        val textSpace = LineSegment(Vector(0f, 0f, 1f), Vector(0f, height, 1f))
        val userSpace = textSpace.transformBy(textToUserSpaceTransformMatrix)
        return userSpace.length
    }

    /**
     * @return The width, in user space units, of a single space character in the current font
     */
    val singleSpaceWidth: Float
        get() = convertWidthFromTextSpaceToUserSpace(unscaledFontSpaceWidth)

    /**
     * @return the text render mode that should be used for the text.  From the
     * * PDF specification, this means:
     * *
     * *    * 0 = Fill text
     * *    * 1 = Stroke text
     * *    * 2 = Fill, then stroke text
     * *    * 3 = Invisible
     * *    * 4 = Fill text and add to path for clipping
     * *    * 5 = Stroke text and add to path for clipping
     * *    * 6 = Fill, then stroke text and add to path for clipping
     * *    * 7 = Add text to padd for clipping
     * *
     * *
     * @since iText 5.0.1
     */
    val textRenderMode: Int
        get() = gs.renderMode

    /**
     * @return the current fill color.
     */
    val fillColor: BaseColor
        get() = gs.fillColor


    /**
     * @return the current stroke color.
     */
    val strokeColor: BaseColor
        get() = gs.strokeColor

    /**
     * Calculates the width of a space character.  If the font does not define
     * a width for a standard space character \u0020, we also attempt to use
     * the width of \u00A0 (a non-breaking space in many fonts)
     * @return the width of a single space character in text space units
     */
    private val unscaledFontSpaceWidth: Float
        get() {
            var charToUse = ' '
            if (gs.font!!.getWidth(charToUse.toInt()) == 0)
                charToUse = '\u00A0'
            return getStringWidth(charToUse.toString())
        }

    /**
     * Gets the width of a String in text space units
     * @param string    the string that needs measuring
     * *
     * @return          the width of a String in text space units
     */
    private fun getStringWidth(string: String): Float {
        var totalWidth = 0f
        for (i in 0..string.length - 1) {
            val c = string[i]
            val w = gs.font!!.getWidth(c.toInt()) / 1000.0f
            val wordSpacing = if (c.toInt() == 32) gs.wordSpacing else 0f
            totalWidth += (w * gs.fontSize + gs.characterSpacing + wordSpacing) * gs.horizontalScaling
        }
        return totalWidth
    }

    /**
     * Gets the width of a PDF string in text space units
     * @param string        the string that needs measuring
     * *
     * @return  the width of a String in text space units
     */
    private fun getPdfStringWidth(string: PdfString, singleCharString: Boolean): Float {
        if (singleCharString) {
            val widthAndWordSpacing = getWidthAndWordSpacing(string, singleCharString)
            return (widthAndWordSpacing[0] * gs.fontSize + gs.characterSpacing + widthAndWordSpacing[1]) * gs.horizontalScaling
        } else {
            var totalWidth = 0f
            for (str in splitString(string)) {
                totalWidth += getPdfStringWidth(str, true)
            }
            return totalWidth
        }
    }

    /**
     * Provides detail useful if a listener needs access to the position of each individual glyph in the text render operation
     * @return  A list of [TextRenderInfo] objects that represent each glyph used in the draw operation. The next effect is if there was a separate Tj opertion for each character in the rendered string
     * *
     * @since   5.3.3
     */
    val characterRenderInfos: List<TextRenderInfo>
        get() {
            val rslt = ArrayList<TextRenderInfo>(pdfString.length())
            val strings = splitString(pdfString)
            var totalWidth = 0f
            for (i in strings.indices) {
                val widthAndWordSpacing = getWidthAndWordSpacing(strings[i], true)
                val subInfo = TextRenderInfo(this, strings[i], totalWidth)
                rslt.add(subInfo)
                totalWidth += (widthAndWordSpacing[0] * gs.fontSize + gs.characterSpacing + widthAndWordSpacing[1]) * gs.horizontalScaling
            }
            for (tri in rslt)
                tri.getUnscaledWidth()
            return rslt
        }

    /**
     * Calculates width and word spacing of a single character PDF string.
     * @param string            a character to calculate width.
     * *
     * @param singleCharString  true if PDF string represents single character, false otherwise.
     * *
     * @return                  array of 2 items: first item is a character width, second item is a calculated word spacing.
     */
    private fun getWidthAndWordSpacing(string: PdfString, singleCharString: Boolean): FloatArray {
        if (singleCharString == false)
            throw UnsupportedOperationException()
        val result = FloatArray(2)
        val decoded = decode(string)
        result[0] = (gs.font!!.getWidth(getCharCode(decoded)) * fontMatrix!![0]).toFloat()
        result[1] = if (decoded == " ") gs.wordSpacing else 0
        return result
    }

    /**
     * Decodes a PdfString (which will contain glyph ids encoded in the font's encoding)
     * based on the active font, and determine the unicode equivalent
     * @param in    the String that needs to be encoded
     * *
     * @return        the encoded String
     */
    private fun decode(`in`: PdfString): String {
        val bytes = `in`.bytes
        return gs.font!!.decode(bytes, 0, bytes.size)
    }

    /**
     * Converts a single character string to char code.

     * @param string single character string to convert to.
     * *
     * @return char code.
     */
    private fun getCharCode(string: String): Int {
        try {
            val b = string.toByteArray(charset("UTF-16BE"))
            var value = 0
            for (i in 0..b.size - 1 - 1) {
                value += b[i] and 0xff
                value = value shl 8
            }
            if (b.size > 0) {
                value += b[b.size - 1] and 0xff
            }
            return value
        } catch (e: UnsupportedEncodingException) {
        }

        return 0
    }

    /**
     * Split PDF string into array of single character PDF strings.
     * @param string    PDF string to be splitted.
     * *
     * @return          splitted PDF string.
     */
    private fun splitString(string: PdfString): Array<PdfString> {
        val strings = ArrayList<PdfString>()
        val stringValue = string.toString()
        var i = 0
        while (i < stringValue.length) {
            var newString = PdfString(stringValue.substring(i, i + 1), string.encoding)
            val text = decode(newString)
            if (text.length == 0 && i < stringValue.length - 1) {
                newString = PdfString(stringValue.substring(i, i + 2), string.encoding)
                i++
            }
            strings.add(newString)
            i++
        }
        return strings.toArray<PdfString>(arrayOfNulls<PdfString>(strings.size))
    }

}
/**
 * Checks if the text belongs to a marked content sequence
 * with a given mcid.
 * @param mcid a marked content id
 * *
 * @return true if the text is marked with this id
 * *
 * @since 5.0.2
 */
