/*
 * $Id: 4d21454441e5182cf12c7a62e5e46f58a73e77f4 $
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

import com.itextpdf.text.pdf.BaseFont

/**
 * Contains all the specifications of a font: fontfamily, size, style and color.
 *
 * Example:

 *

 * Paragraph p = new Paragraph("This is a paragraph", new
 * Font(FontFamily.HELVETICA, 18, Font.BOLDITALIC, new BaseColor(0, 0, 255)) );

 *

 *
 */

class Font : Comparable<Font> {

    /**
     * Enum describing the font family

     * @since 5.0.1
     */
    enum class FontFamily {
        COURIER,
        HELVETICA,
        TIMES_ROMAN,
        SYMBOL,
        ZAPFDINGBATS,
        UNDEFINED
    }

    /**
     * FontStyle.

     * @author Balder
     * *
     * @since 5.0.6
     */
    enum class FontStyle private constructor(
            /**
             * @return a string, the text value of this style.
             * *
             * @since 5.0.6
             */
            val value: String) {
        // Created to get rid of dependency of HtmlTags
        NORMAL("normal"),
        BOLD("bold"), ITALIC("italic"), OBLIQUE("oblique"), UNDERLINE("underline"), LINETHROUGH("line-through")

    }

    // membervariables

    /** the value of the fontfamily.  */
    // FAMILY

    /**
     * Gets the family of this font.

     * @return the value of the family
     */
    var family = FontFamily.UNDEFINED
        private set

    /** the value of the fontsize.  */
    // SIZE

    /**
     * Gets the size of this font.

     * @return a size
     */
    /**
     * Sets the size.

     * @param size
     * *            The new size of the font.
     */
    var size = UNDEFINED.toFloat()

    /** the value of the style.  */
    // STYLE

    /**
     * Gets the style of this font.

     * @return a size
     */
    /**
     * Sets the style.

     * @param style
     * *            the style.
     */
    var style = UNDEFINED

    /** the value of the color.  */
    // COLOR

    /**
     * Gets the color of this font.

     * @return a color
     */
    /**
     * Sets the color.

     * @param color
     * *            the new color of the font
     */

    var color: BaseColor? = null

    /** the external font  */
    // BASEFONT

    /**
     * Gets the BaseFont inside this object.

     * @return the BaseFont
     */
    var baseFont: BaseFont? = null
        private set

    // constructors

    /**
     * Copy constructor of a Font

     * @param other
     * *            the font that has to be copied
     */
    constructor(other: Font) {
        this.family = other.family
        this.size = other.size
        this.style = other.style
        this.color = other.color
        this.baseFont = other.baseFont
    }

    /**
     * Constructs a Font.

     * @param family
     * *            the family to which this font belongs
     * *
     * @param size
     * *            the size of this font
     * *
     * @param style
     * *            the style of this font
     * *
     * @param color
     * *            the BaseColor of this font.
     * *
     * @since iText 5.0.1 (first parameter has been replaced with enum)
     */

    @JvmOverloads constructor(family: FontFamily = FontFamily.UNDEFINED, size: Float = UNDEFINED.toFloat(), style: Int = UNDEFINED, color: BaseColor? = null) {
        this.family = family
        this.size = size
        this.style = style
        this.color = color
    }

    /**
     * Constructs a Font.

     * @param bf
     * *            the external font
     * *
     * @param size
     * *            the size of this font
     * *
     * @param style
     * *            the style of this font
     * *
     * @param color
     * *            the BaseColor of this font.
     */

    @JvmOverloads constructor(bf: BaseFont, size: Float = UNDEFINED.toFloat(), style: Int = UNDEFINED, color: BaseColor? = null) {
        this.baseFont = bf
        this.size = size
        this.style = style
        this.color = color
    }

    // implementation of the Comparable interface

    /**
     * Compares this Font with another

     * @param font
     * *            the other Font
     * *
     * @return a value
     */
    override fun compareTo(font: Font): Int {
        if (font == null) {
            return -1
        }
        try {
            // FIXME using equals but equals not implemented!
            if (baseFont != null && baseFont != font.baseFont) {
                return -2
            }
            if (this.family != font.family) {
                return 1
            }
            if (this.size != font.size) {
                return 2
            }
            if (this.style != font.style) {
                return 3
            }
            if (this.color == null) {
                if (font.color == null) {
                    return 0
                }
                return 4
            }
            if (font.color == null) {
                return 4
            }
            if (this.color == font.color) {
                return 0
            }
            return 4
        } catch (cce: ClassCastException) {
            return -3
        }

    }

    /**
     * Gets the familyname as a String.

     * @return the familyname
     */
    val familyname: String
        get() {
            var tmp = "unknown"
            when (family) {
                Font.FontFamily.COURIER -> return FontFactory.COURIER
                Font.FontFamily.HELVETICA -> return FontFactory.HELVETICA
                Font.FontFamily.TIMES_ROMAN -> return FontFactory.TIMES_ROMAN
                Font.FontFamily.SYMBOL -> return FontFactory.SYMBOL
                Font.FontFamily.ZAPFDINGBATS -> return FontFactory.ZAPFDINGBATS
                else -> if (baseFont != null) {
                    val names = baseFont!!.familyFontName
                    for (name in names) {
                        if ("0" == name[2]) {
                            return name[3]
                        }
                        if ("1033" == name[2]) {
                            tmp = name[3]
                        }
                        if ("" == name[2]) {
                            tmp = name[3]
                        }
                    }
                }
            }
            return tmp
        }

    /**
     * Sets the family using a String ("Courier", "Helvetica",
     * "Times New Roman", "Symbol" or "ZapfDingbats").

     * @param family
     * *            A String representing a certain font-family.
     */
    fun setFamily(family: String) {
        this.family = getFamily(family)
    }

    /**
     * Gets the size that can be used with the calculated BaseFont
     * .

     * @return the size that can be used with the calculated BaseFont
     * *
     */
    val calculatedSize: Float
        get() {
            var s = this.size
            if (s == UNDEFINED.toFloat()) {
                s = DEFAULTSIZE.toFloat()
            }
            return s
        }

    /**
     * Gets the leading that can be used with this font.

     * @param multipliedLeading
     * *            a certain multipliedLeading
     * *
     * @return the height of a line
     */
    fun getCalculatedLeading(multipliedLeading: Float): Float {
        return multipliedLeading * calculatedSize
    }

    /**
     * Gets the style that can be used with the calculated BaseFont
     * .

     * @return the style that can be used with the calculated BaseFont
     * *
     */
    val calculatedStyle: Int
        get() {
            var style = this.style
            if (style == UNDEFINED) {
                style = NORMAL
            }
            if (baseFont != null)
                return style
            if (family == FontFamily.SYMBOL || family == FontFamily.ZAPFDINGBATS)
                return style
            else
                return style and BOLDITALIC.inv()
        }

    /**
     * checks if this font is Bold.

     * @return a boolean
     */
    val isBold: Boolean
        get() {
            if (style == UNDEFINED) {
                return false
            }
            return style and BOLD == BOLD
        }

    /**
     * checks if this font is italic.

     * @return a boolean
     */
    val isItalic: Boolean
        get() {
            if (style == UNDEFINED) {
                return false
            }
            return style and ITALIC == ITALIC
        }

    /**
     * checks if this font is underlined.

     * @return a boolean
     */
    val isUnderlined: Boolean
        get() {
            if (style == UNDEFINED) {
                return false
            }
            return style and UNDERLINE == UNDERLINE
        }

    /**
     * checks if the style of this font is STRIKETHRU.

     * @return a boolean
     */
    val isStrikethru: Boolean
        get() {
            if (style == UNDEFINED) {
                return false
            }
            return style and STRIKETHRU == STRIKETHRU
        }

    /**
     * Sets the style using a String containing one or more of the
     * following values: normal, bold, italic, oblique, underline, line-through

     * @param style
     * *            A String representing a certain style.
     */
    fun setStyle(style: String) {
        if (this.style == UNDEFINED)
            this.style = NORMAL
        this.style = this.style or getStyleValue(style)
    }

    /**
     * Sets the color.

     * @param red
     * *            the red-value of the new color
     * *
     * @param green
     * *            the green-value of the new color
     * *
     * @param blue
     * *            the blue-value of the new color
     */
    fun setColor(red: Int, green: Int, blue: Int) {
        this.color = BaseColor(red, green, blue)
    }

    /**
     * Gets the BaseFont this class represents. For the built-in
     * fonts a BaseFont is calculated.

     * @param specialEncoding
     * *            true to use the special encoding for Symbol and
     * *            ZapfDingbats, false to always use Cp1252
     * *
     * *
     * @return the BaseFont this class represents
     */
    fun getCalculatedBaseFont(specialEncoding: Boolean): BaseFont {
        if (baseFont != null)
            return baseFont
        var style = this.style
        if (style == UNDEFINED) {
            style = NORMAL
        }
        var fontName = BaseFont.HELVETICA
        var encoding = BaseFont.WINANSI
        var cfont: BaseFont? = null
        when (family) {
            Font.FontFamily.COURIER -> when (style and BOLDITALIC) {
                BOLD -> fontName = BaseFont.COURIER_BOLD
                ITALIC -> fontName = BaseFont.COURIER_OBLIQUE
                BOLDITALIC -> fontName = BaseFont.COURIER_BOLDOBLIQUE
                else -> // case NORMAL:
                    fontName = BaseFont.COURIER
            }
            Font.FontFamily.TIMES_ROMAN -> when (style and BOLDITALIC) {
                BOLD -> fontName = BaseFont.TIMES_BOLD
                ITALIC -> fontName = BaseFont.TIMES_ITALIC
                BOLDITALIC -> fontName = BaseFont.TIMES_BOLDITALIC
                else, NORMAL -> fontName = BaseFont.TIMES_ROMAN
            }
            Font.FontFamily.SYMBOL -> {
                fontName = BaseFont.SYMBOL
                if (specialEncoding)
                    encoding = BaseFont.SYMBOL
            }
            Font.FontFamily.ZAPFDINGBATS -> {
                fontName = BaseFont.ZAPFDINGBATS
                if (specialEncoding)
                    encoding = BaseFont.ZAPFDINGBATS
            }
            else, Font.FontFamily.HELVETICA -> when (style and BOLDITALIC) {
                BOLD -> fontName = BaseFont.HELVETICA_BOLD
                ITALIC -> fontName = BaseFont.HELVETICA_OBLIQUE
                BOLDITALIC -> fontName = BaseFont.HELVETICA_BOLDOBLIQUE
                else, NORMAL -> fontName = BaseFont.HELVETICA
            }
        }
        try {
            cfont = BaseFont.createFont(fontName, encoding, false)
        } catch (ee: Exception) {
            throw ExceptionConverter(ee)
        }

        return cfont
    }

    // Helper methods

    /**
     * Checks if the properties of this font are undefined or null.
     *
     * If so, the standard should be used.

     * @return a boolean
     */
    val isStandardFont: Boolean
        get() = family == FontFamily.UNDEFINED && size == UNDEFINED.toFloat()
                && style == UNDEFINED && color == null && baseFont == null

    /**
     * Replaces the attributes that are equal to null with the
     * attributes of a given font.

     * @param font the font of a lower element class (ex. this - paragraph font, font - chunk font)
     * *
     * @return a Font
     */
    fun difference(font: Font?): Font {
        if (font == null)
            return this
        // size
        var dSize = font.size
        if (dSize == UNDEFINED.toFloat()) {
            dSize = this.size
        }
        // style
        var dStyle = UNDEFINED
        var style1 = this.style
        var style2 = font.style
        if (style1 != UNDEFINED || style2 != UNDEFINED) {
            if (style1 == UNDEFINED)
                style1 = 0
            if (style2 == UNDEFINED)
                style2 = 0
            dStyle = style1 or style2
        }
        // color
        var dColor = font.color
        if (dColor == null) {
            dColor = this.color
        }
        // family
        if (font.baseFont != null) {
            return Font(font.baseFont, dSize, dStyle, dColor)
        }
        if (font.family != FontFamily.UNDEFINED) {
            return Font(font.family, dSize, dStyle, dColor)
        }
        if (this.baseFont != null) {
            if (dStyle == style1) {
                return Font(this.baseFont, dSize, dStyle, dColor)
            } else {
                return FontFactory.getFont(this.familyname, dSize, dStyle,
                        dColor)
            }
        }
        return Font(this.family, dSize, dStyle, dColor)
    }

    companion object {

        // static membervariables for the different styles

        /** this is a possible style.  */
        val NORMAL = 0

        /** this is a possible style.  */
        val BOLD = 1

        /** this is a possible style.  */
        val ITALIC = 2

        /** this is a possible style.  */
        val UNDERLINE = 4

        /** this is a possible style.  */
        val STRIKETHRU = 8

        /** this is a possible style.  */
        val BOLDITALIC = BOLD or ITALIC

        // static membervariables

        /** the value of an undefined attribute.  */
        val UNDEFINED = -1

        /** the value of the default size.  */
        val DEFAULTSIZE = 12

        /**
         * Translates a String -value of a certain family into the
         * FontFamily enum that is used for this family in this class.

         * @param family
         * *            A String representing a certain font-family
         * *
         * @return the corresponding FontFamily
         * *
         * *
         * @since 5.0.1
         */
        fun getFamily(family: String): FontFamily {
            if (family.equals(FontFactory.COURIER, ignoreCase = true)) {
                return FontFamily.COURIER
            }
            if (family.equals(FontFactory.HELVETICA, ignoreCase = true)) {
                return FontFamily.HELVETICA
            }
            if (family.equals(FontFactory.TIMES_ROMAN, ignoreCase = true)) {
                return FontFamily.TIMES_ROMAN
            }
            if (family.equals(FontFactory.SYMBOL, ignoreCase = true)) {
                return FontFamily.SYMBOL
            }
            if (family.equals(FontFactory.ZAPFDINGBATS, ignoreCase = true)) {
                return FontFamily.ZAPFDINGBATS
            }
            return FontFamily.UNDEFINED
        }

        /**
         * Translates a String -value of a certain style into the index
         * value is used for this style in this class. Supported styles are in
         * [FontStyle] values are checked on [FontStyle.getValue]

         * @param style
         * *            A String
         * *
         * @return the corresponding value
         */
        fun getStyleValue(style: String): Int {
            var s = 0
            if (style.indexOf(FontStyle.NORMAL.value) != -1) {
                s = s or NORMAL
            }
            if (style.indexOf(FontStyle.BOLD.value) != -1) {
                s = s or BOLD
            }
            if (style.indexOf(FontStyle.ITALIC.value) != -1) {
                s = s or ITALIC
            }
            if (style.indexOf(FontStyle.OBLIQUE.value) != -1) {
                s = s or ITALIC
            }
            if (style.indexOf(FontStyle.UNDERLINE.value) != -1) {
                s = s or UNDERLINE
            }
            if (style.indexOf(FontStyle.LINETHROUGH.value) != -1) {
                s = s or STRIKETHRU
            }
            return s
        }
    }

}
/**
 * Constructs a Font.

 * @param bf
 * *            the external font
 * *
 * @param size
 * *            the size of this font
 * *
 * @param style
 * *            the style of this font
 */
/**
 * Constructs a Font.

 * @param bf
 * *            the external font
 * *
 * @param size
 * *            the size of this font
 */
/**
 * Constructs a Font.

 * @param bf
 * *            the external font
 */
/**
 * Constructs a Font.

 * @param family
 * *            the family to which this font belongs
 * *
 * @param size
 * *            the size of this font
 * *
 * @param style
 * *            the style of this font
 * *
 * @since iText 5.0.1 (first parameter has been replaced with enum)
 */
/**
 * Constructs a Font.

 * @param family
 * *            the family to which this font belongs
 * *
 * @param size
 * *            the size of this font
 * *
 * @since iText 5.0.1 (first parameter has been replaced with enum)
 */
/**
 * Constructs a Font.

 * @param family
 * *            the family to which this font belongs
 * *
 * @since iText 5.0.1 (first parameter has been replaced with enum)
 */
/**
 * Constructs a Font.
 */
