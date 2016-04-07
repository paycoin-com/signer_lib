/*
 * $Id: 096d2ed9bcceed864cb908c0f87268a51f624af5 $
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

import java.util.HashSet

import com.itextpdf.text.Element
import com.itextpdf.text.BaseColor

/**
 * This class converts a String to the HTML-format of a String.
 *
 * To convert the String, each character is examined:
 *
 * ASCII-characters from 000 till 031 are represented as &amp;#xxx;
 * (with xxx = the value of the character)
 * ASCII-characters from 032 t/m 127 are represented by the character itself, except for:
 *
 * '\n'	becomes &lt;BR&gt;\n
 * &quot; becomes &amp;quot;
 * &amp; becomes &amp;amp;
 * &lt; becomes &amp;lt;
 * &gt; becomes &amp;gt;
 *
 * ASCII-characters from 128 till 255 are represented as &amp;#xxx;
 * (with xxx = the value of the character)
 *
 *
 * Example:
 *
 * String htmlPresentation = HtmlEncoder.encode("Marie-Th&#233;r&#232;se S&#248;rensen");
 *
 * for more info: see O'Reilly; "HTML: The Definitive Guide" (page 164)

 */
@Deprecated("")
@Deprecated("since 5.5.2")
object HtmlEncoder {

    // membervariables
    /**
     * List with the HTML translation of all the characters.
     * @since 5.0.6 (renamed from htmlCode)
     */
    private val HTML_CODE = arrayOfNulls<String>(256)

    init {
        for (i in 0..9) {
            HTML_CODE[i] = "&#00$i;"
        }

        for (i in 10..31) {
            HTML_CODE[i] = "&#0$i;"
        }

        for (i in 32..127) {
            HTML_CODE[i] = i.toChar().toString()
        }

        // Special characters
        HTML_CODE['\t'] = "\t"
        HTML_CODE['\n'] = "<br />\n"
        HTML_CODE['\"'] = "&quot;" // double quote
        HTML_CODE['&'] = "&amp;" // ampersand
        HTML_CODE['<'] = "&lt;" // lower than
        HTML_CODE['>'] = "&gt;" // greater than

        for (i in 128..255) {
            HTML_CODE[i] = "&#$i;"
        }
    }

    // methods

    /**
     * Converts a String to the HTML-format of this String.

     * @param    string    The String to convert
     * *
     * @return    a String
     */
    fun encode(string: String): String {
        val n = string.length
        var character: Char
        val buffer = StringBuffer()
        // loop over all the characters of the String.
        for (i in 0..n - 1) {
            character = string[i]
            // the Htmlcode of these characters are added to a StringBuffer one by one
            if (character.toInt() < 256) {
                buffer.append(HTML_CODE[character])
            } else {
                // Improvement posted by Joachim Eyrich
                buffer.append("&#").append(character.toInt()).append(';')
            }
        }
        return buffer.toString()
    }

    /**
     * Converts a BaseColor into a HTML representation of this BaseColor.

     * @param    color    the BaseColor that has to be converted.
     * *
     * @return    the HTML representation of this BaseColor
     */
    fun encode(color: BaseColor): String {
        val buffer = StringBuffer("#")
        if (color.red < 16) {
            buffer.append('0')
        }
        buffer.append(Integer.toString(color.red, 16))
        if (color.green < 16) {
            buffer.append('0')
        }
        buffer.append(Integer.toString(color.green, 16))
        if (color.blue < 16) {
            buffer.append('0')
        }
        buffer.append(Integer.toString(color.blue, 16))
        return buffer.toString()
    }

    /**
     * Translates the alignment value.

     * @param   alignment   the alignment value
     * *
     * @return  the translated value
     */
    fun getAlignment(alignment: Int): String {
        when (alignment) {
            Element.ALIGN_LEFT -> return HtmlTags.ALIGN_LEFT
            Element.ALIGN_CENTER -> return HtmlTags.ALIGN_CENTER
            Element.ALIGN_RIGHT -> return HtmlTags.ALIGN_RIGHT
            Element.ALIGN_JUSTIFIED, Element.ALIGN_JUSTIFIED_ALL -> return HtmlTags.ALIGN_JUSTIFY
            Element.ALIGN_TOP -> return HtmlTags.ALIGN_TOP
            Element.ALIGN_MIDDLE -> return HtmlTags.ALIGN_MIDDLE
            Element.ALIGN_BOTTOM -> return HtmlTags.ALIGN_BOTTOM
            Element.ALIGN_BASELINE -> return HtmlTags.ALIGN_BASELINE
            else -> return ""
        }
    }

    /**
     * Set containing tags that trigger a new line.
     * @since iText 5.0.6
     */
    private val NEWLINETAGS = HashSet<String>()

    init {
        // Following list are the basic html tags that force new lines
        // List may be extended as we discover them
        NEWLINETAGS.add(HtmlTags.P)
        NEWLINETAGS.add(HtmlTags.BLOCKQUOTE)
        NEWLINETAGS.add(HtmlTags.BR)
    }

    /**
     * Returns true if the tag causes a new line like p, br etc.
     * @since iText 5.0.6
     */
    fun isNewLineTag(tag: String): Boolean {
        return NEWLINETAGS.contains(tag)
    }
}
/**
 * This class will never be constructed.
 */
