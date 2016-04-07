/*
 * $Id: 373be7f431fb5f5b13dfa4e14d4c4c12a8745e20 $
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
package com.itextpdf.text.html

import java.util.Properties
import java.util.StringTokenizer
import java.util.HashMap
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Element

/**
 * A class that contains some utilities to parse HTML attributes and content.
 * @since 5.0.6 (some of these methods used to be in the Markup class)
 * *
 */
@Deprecated("")
@Deprecated("since 5.5.2")
object HtmlUtilities {

    /**
     * a default value for font-size
     * @since 2.1.3
     */
    val DEFAULT_FONT_SIZE = 12f

    private val sizes = HashMap<String, Float>()

    init {
        sizes.put("xx-small", 4)
        sizes.put("x-small", 6)
        sizes.put("small", 8)
        sizes.put("medium", 10)
        sizes.put("large", 13)
        sizes.put("x-large", 18)
        sizes.put("xx-large", 26)
    }

    /**
     * New method contributed by: Lubos Strapko

     * @since 2.1.3
     */
    @JvmOverloads fun parseLength(string: String?, actualFontSize: Float = DEFAULT_FONT_SIZE): Float {
        var string: String? = string ?: return 0f
        val fl = sizes[string.toLowerCase()]
        if (fl != null)
            return fl.toFloat()
        var pos = 0
        val length = string.length
        var ok = true
        while (ok && pos < length) {
            when (string[pos]) {
                '+', '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' -> pos++
                else -> ok = false
            }
        }
        if (pos == 0)
            return 0f
        if (pos == length)
            return java.lang.Float.parseFloat(string + "f")
        val f = java.lang.Float.parseFloat(string.substring(0, pos) + "f")
        string = string.substring(pos)
        // inches
        if (string.startsWith("in")) {
            return f * 72f
        }
        // centimeters
        if (string.startsWith("cm")) {
            return f / 2.54f * 72f
        }
        // millimeters
        if (string.startsWith("mm")) {
            return f / 25.4f * 72f
        }
        // picas
        if (string.startsWith("pc")) {
            return f * 12f
        }
        // 1em is equal to the current font size
        if (string.startsWith("em")) {
            return f * actualFontSize
        }
        // one ex is the x-height of a font (x-height is usually about half the
        // font-size)
        if (string.startsWith("ex")) {
            return f * actualFontSize / 2
        }
        // default: we assume the length was measured in points
        return f
    }

    /**
     * Converts a BaseColor into a HTML representation of this
     * BaseColor.

     * @param s
     * *            the BaseColor that has to be converted.
     * *
     * @return the HTML representation of this BaseColor
     */

    fun decodeColor(s: String?): BaseColor? {
        var s: String? = s ?: return null
        s = s.toLowerCase().trim { it <= ' ' }
        try {
            return WebColors.getRGBColor(s)
        } catch (iae: IllegalArgumentException) {
            return null
        }

    }

    /**
     * This method parses a String with attributes and returns a Properties
     * object.

     * @param string
     * *            a String of this form: 'key1="value1"; key2="value2";...
     * *            keyN="valueN" '
     * *
     * @return a Properties object
     */
    fun parseAttributes(string: String?): Properties {
        val result = Properties()
        if (string == null)
            return result
        val keyValuePairs = StringTokenizer(string, ";")
        var keyValuePair: StringTokenizer
        var key: String
        var value: String
        while (keyValuePairs.hasMoreTokens()) {
            keyValuePair = StringTokenizer(keyValuePairs.nextToken(), ":")
            if (keyValuePair.hasMoreTokens())
                key = keyValuePair.nextToken().trim { it <= ' ' }
            else
                continue
            if (keyValuePair.hasMoreTokens())
                value = keyValuePair.nextToken().trim { it <= ' ' }
            else
                continue
            if (value.startsWith("\""))
                value = value.substring(1)
            if (value.endsWith("\""))
                value = value.substring(0, value.length - 1)
            result.setProperty(key.toLowerCase(), value)
        }
        return result
    }

    /**
     * Removes the comments sections of a String.

     * @param string
     * *            the original String
     * *
     * @param startComment
     * *            the String that marks the start of a Comment section
     * *
     * @param endComment
     * *            the String that marks the end of a Comment section.
     * *
     * @return the String stripped of its comment section
     */
    fun removeComment(string: String, startComment: String,
                      endComment: String): String {
        val result = StringBuffer()
        var pos = 0
        val end = endComment.length
        var start = string.indexOf(startComment, pos)
        while (start > -1) {
            result.append(string.substring(pos, start))
            pos = string.indexOf(endComment, start) + end
            start = string.indexOf(startComment, pos)
        }
        result.append(string.substring(pos))
        return result.toString()
    }

    /**
     * Helper class that reduces the white space in a String
     * @param content content containing whitespace
     * *
     * @return the content without all unnecessary whitespace
     */
    fun eliminateWhiteSpace(content: String): String {
        // multiple spaces are reduced to one,
        // newlines are treated as spaces,
        // tabs, carriage returns are ignored.
        val buf = StringBuffer()
        val len = content.length
        var character: Char
        var newline = false
        for (i in 0..len - 1) {
            when (character = content[i]) {
                ' ' -> if (!newline) {
                    buf.append(character)
                }
                '\n' -> if (i > 0) {
                    newline = true
                    buf.append(' ')
                }
                '\r' -> {
                }
                '\t' -> {
                }
                else -> {
                    newline = false
                    buf.append(character)
                }
            }
        }
        return buf.toString()
    }

    /**
     * A series of predefined font sizes.
     * @since 5.0.6 (renamed)
     */
    val FONTSIZES = intArrayOf(8, 10, 12, 14, 18, 24, 36)

    /**
     * Picks a font size from a series of predefined font sizes.
     * @param value        the new value of a font, expressed as an index
     * *
     * @param previous    the previous value of the font size
     * *
     * @return    a new font size.
     */
    fun getIndexedFontSize(value: String, previous: String?): Int {
        var previous = previous
        // the font is expressed as an index in a series of predefined font sizes
        var sIndex = 0
        // the font is defined as a relative size
        if (value.startsWith("+") || value.startsWith("-")) {
            // fetch the previous value
            if (previous == null)
                previous = "12"
            val c = java.lang.Float.parseFloat(previous).toInt()
            // look for the nearest font size in the predefined series
            for (k in FONTSIZES.indices.reversed()) {
                if (c >= FONTSIZES[k]) {
                    sIndex = k
                    break
                }
            }
            // retrieve the difference
            val diff = Integer.parseInt(if (value.startsWith("+"))
                value.substring(1)
            else
                value)
            // apply the difference
            sIndex += diff
        } else {
            try {
                sIndex = Integer.parseInt(value) - 1
            } catch (nfe: NumberFormatException) {
                sIndex = 0
            }

        }// the font is defined as an index
        if (sIndex < 0)
            sIndex = 0
        else if (sIndex >= FONTSIZES.size)
            sIndex = FONTSIZES.size - 1
        return FONTSIZES[sIndex]
    }

    /**
     * Translates a String value to an alignment value.
     * (written by Norman Richards, integrated into iText by Bruno)
     * @param    alignment a String (one of the ALIGN_ constants of this class)
     * *
     * @return    an alignment value (one of the ALIGN_ constants of the Element interface)
     */
    fun alignmentValue(alignment: String?): Int {
        if (alignment == null) return Element.ALIGN_UNDEFINED
        if (HtmlTags.ALIGN_CENTER.equals(alignment, ignoreCase = true)) {
            return Element.ALIGN_CENTER
        }
        if (HtmlTags.ALIGN_LEFT.equals(alignment, ignoreCase = true)) {
            return Element.ALIGN_LEFT
        }
        if (HtmlTags.ALIGN_RIGHT.equals(alignment, ignoreCase = true)) {
            return Element.ALIGN_RIGHT
        }
        if (HtmlTags.ALIGN_JUSTIFY.equals(alignment, ignoreCase = true)) {
            return Element.ALIGN_JUSTIFIED
        }
        if (HtmlTags.ALIGN_JUSTIFIED_ALL.equals(alignment, ignoreCase = true)) {
            return Element.ALIGN_JUSTIFIED_ALL
        }
        if (HtmlTags.ALIGN_TOP.equals(alignment, ignoreCase = true)) {
            return Element.ALIGN_TOP
        }
        if (HtmlTags.ALIGN_MIDDLE.equals(alignment, ignoreCase = true)) {
            return Element.ALIGN_MIDDLE
        }
        if (HtmlTags.ALIGN_BOTTOM.equals(alignment, ignoreCase = true)) {
            return Element.ALIGN_BOTTOM
        }
        if (HtmlTags.ALIGN_BASELINE.equals(alignment, ignoreCase = true)) {
            return Element.ALIGN_BASELINE
        }
        return Element.ALIGN_UNDEFINED
    }
}
/**
 * Parses a length.

 * @param string
 * *            a length in the form of an optional + or -, followed by a
 * *            number and a unit.
 * *
 * @return a float
 */
