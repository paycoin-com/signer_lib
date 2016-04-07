/*
 * $Id: 41e5d7c97a1cf9223e5e5b063c842384a0ad1497 $
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
package com.itextpdf.text.pdf.codec.wmf

import com.itextpdf.text.Document
import java.io.IOException
import java.io.UnsupportedEncodingException

import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Font
import com.itextpdf.text.FontFactory
import com.itextpdf.text.pdf.BaseFont

class MetaFont : MetaObject() {

    internal var height: Int = 0
    var angle: Float = 0.toFloat()
        internal set
    internal var bold: Int = 0
    internal var italic: Int = 0
    var isUnderline: Boolean = false
        internal set
    var isStrikeout: Boolean = false
        internal set
    internal var charset: Int = 0
    internal var pitchAndFamily: Int = 0
    internal var faceName = "arial"
    internal var font: BaseFont? = null

    init {
        type = MetaObject.META_FONT
    }

    @Throws(IOException::class)
    fun init(`in`: InputMeta) {
        height = Math.abs(`in`.readShort())
        `in`.skip(2)
        angle = (`in`.readShort() / 1800.0 * Math.PI).toFloat()
        `in`.skip(2)
        bold = if (`in`.readShort() >= BOLDTHRESHOLD) MARKER_BOLD else 0
        italic = if (`in`.readByte() != 0) MARKER_ITALIC else 0
        isUnderline = `in`.readByte() != 0
        isStrikeout = `in`.readByte() != 0
        charset = `in`.readByte()
        `in`.skip(3)
        pitchAndFamily = `in`.readByte()
        val name = ByteArray(nameSize)
        var k: Int
        k = 0
        while (k < nameSize) {
            val c = `in`.readByte()
            if (c == 0) {
                break
            }
            name[k] = c.toByte()
            ++k
        }
        try {
            faceName = String(name, 0, k, "Cp1252")
        } catch (e: UnsupportedEncodingException) {
            faceName = String(name, 0, k)
        }

        faceName = faceName.toLowerCase()
    }

    fun getFont(): BaseFont {
        if (font != null)
            return font
        val ff2 = FontFactory.getFont(faceName, BaseFont.CP1252, true, 10f, if (italic != 0) Font.ITALIC else 0 or if (bold != 0) Font.BOLD else 0)
        font = ff2.baseFont
        if (font != null)
            return font
        val fontName: String
        if (faceName.indexOf("courier") != -1 || faceName.indexOf("terminal") != -1
                || faceName.indexOf("fixedsys") != -1) {
            fontName = fontNames[MARKER_COURIER + italic + bold]
        } else if (faceName.indexOf("ms sans serif") != -1 || faceName.indexOf("arial") != -1
                || faceName.indexOf("system") != -1) {
            fontName = fontNames[MARKER_HELVETICA + italic + bold]
        } else if (faceName.indexOf("arial black") != -1) {
            fontName = fontNames[MARKER_HELVETICA + italic + MARKER_BOLD]
        } else if (faceName.indexOf("times") != -1 || faceName.indexOf("ms serif") != -1
                || faceName.indexOf("roman") != -1) {
            fontName = fontNames[MARKER_TIMES + italic + bold]
        } else if (faceName.indexOf("symbol") != -1) {
            fontName = fontNames[MARKER_SYMBOL]
        } else {
            val pitch = pitchAndFamily and 3
            val family = pitchAndFamily shr 4 and 7
            when (family) {
                FF_MODERN -> fontName = fontNames[MARKER_COURIER + italic + bold]
                FF_ROMAN -> fontName = fontNames[MARKER_TIMES + italic + bold]
                FF_SWISS, FF_SCRIPT, FF_DECORATIVE -> fontName = fontNames[MARKER_HELVETICA + italic + bold]
                else -> {
                    when (pitch) {
                        FIXED_PITCH -> fontName = fontNames[MARKER_COURIER + italic + bold]
                        else -> fontName = fontNames[MARKER_HELVETICA + italic + bold]
                    }
                }
            }
        }
        try {
            font = BaseFont.createFont(fontName, "Cp1252", false)
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

        return font
    }

    fun getFontSize(state: MetaState): Float {
        return Math.abs(state.transformY(height) - state.transformY(0)) * Document.wmfFontCorrection
    }

    companion object {
        internal val fontNames = arrayOf("Courier", "Courier-Bold", "Courier-Oblique", "Courier-BoldOblique", "Helvetica", "Helvetica-Bold", "Helvetica-Oblique", "Helvetica-BoldOblique", "Times-Roman", "Times-Bold", "Times-Italic", "Times-BoldItalic", "Symbol", "ZapfDingbats")

        internal val MARKER_BOLD = 1
        internal val MARKER_ITALIC = 2
        internal val MARKER_COURIER = 0
        internal val MARKER_HELVETICA = 4
        internal val MARKER_TIMES = 8
        internal val MARKER_SYMBOL = 12

        internal val DEFAULT_PITCH = 0
        internal val FIXED_PITCH = 1
        internal val VARIABLE_PITCH = 2
        internal val FF_DONTCARE = 0
        internal val FF_ROMAN = 1
        internal val FF_SWISS = 2
        internal val FF_MODERN = 3
        internal val FF_SCRIPT = 4
        internal val FF_DECORATIVE = 5
        internal val BOLDTHRESHOLD = 600
        internal val nameSize = 32
        internal val ETO_OPAQUE = 2
        internal val ETO_CLIPPED = 4
    }
}
