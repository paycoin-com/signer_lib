/*
 * $Id: ec6686d9bab8415cbee6052f4849ec88b196d07d $
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

import java.util.HashMap

import com.itextpdf.text.Rectangle

/**
 * Implements the appearance stream to be used with form fields..
 */

class PdfAppearance : PdfTemplate {

    /**
     * Creates a PdfAppearance.
     */

    internal constructor() : super() {
        separator = ' '
    }

    internal constructor(iref: PdfIndirectReference) {
        thisReference = iref
    }

    /**
     * Creates new PdfTemplate

     * @param wr the PdfWriter
     */

    internal constructor(wr: PdfWriter) : super(wr) {
        separator = ' '
    }

    /**
     * Set the font and the size for the subsequent text writing.

     * @param bf the font
     * *
     * @param size the font size in points
     */
    override fun setFontAndSize(bf: BaseFont, size: Float) {
        checkWriter()
        state.size = size
        if (bf.fontType == BaseFont.FONT_TYPE_DOCUMENT) {
            state.fontDetails = FontDetails(null, (bf as DocumentFont).indirectReference, bf)
        } else
            state.fontDetails = pdfWriter!!.addSimple(bf)
        var psn: PdfName? = stdFieldFontNames[bf.postscriptFontName]
        if (psn == null) {
            if (bf.isSubset && bf.fontType == BaseFont.FONT_TYPE_TTUNI)
                psn = state.fontDetails!!.fontName
            else {
                psn = PdfName(bf.postscriptFontName)
                state.fontDetails!!.isSubset = false
            }
        }
        val prs = pageResources
        //        PdfName name = state.fontDetails.getFontName();
        prs.addFont(psn, state.fontDetails!!.indirectReference)
        internalBuffer.append(psn!!.bytes).append(' ').append(size).append(" Tf").append_i(separator)
    }

    override val duplicate: PdfContentByte
        get() {
            val tpl = PdfAppearance()
            tpl.pdfWriter = pdfWriter
            tpl.pdfDocument = pdfDocument
            tpl.thisReference = thisReference
            tpl.pageResources = pageResources
            tpl.boundingBox = Rectangle(boundingBox)
            tpl.group = group
            tpl.layer = layer
            if (matrix != null) {
                tpl.matrix = PdfArray(matrix)
            }
            tpl.separator = separator
            return tpl
        }

    companion object {

        val stdFieldFontNames = HashMap<String, PdfName>()

        init {
            stdFieldFontNames.put("Courier-BoldOblique", PdfName("CoBO"))
            stdFieldFontNames.put("Courier-Bold", PdfName("CoBo"))
            stdFieldFontNames.put("Courier-Oblique", PdfName("CoOb"))
            stdFieldFontNames.put("Courier", PdfName("Cour"))
            stdFieldFontNames.put("Helvetica-BoldOblique", PdfName("HeBO"))
            stdFieldFontNames.put("Helvetica-Bold", PdfName("HeBo"))
            stdFieldFontNames.put("Helvetica-Oblique", PdfName("HeOb"))
            stdFieldFontNames.put("Helvetica", PdfName.HELV)
            stdFieldFontNames.put("Symbol", PdfName("Symb"))
            stdFieldFontNames.put("Times-BoldItalic", PdfName("TiBI"))
            stdFieldFontNames.put("Times-Bold", PdfName("TiBo"))
            stdFieldFontNames.put("Times-Italic", PdfName("TiIt"))
            stdFieldFontNames.put("Times-Roman", PdfName("TiRo"))
            stdFieldFontNames.put("ZapfDingbats", PdfName.ZADB)
            stdFieldFontNames.put("HYSMyeongJo-Medium", PdfName("HySm"))
            stdFieldFontNames.put("HYGoThic-Medium", PdfName("HyGo"))
            stdFieldFontNames.put("HeiseiKakuGo-W5", PdfName("KaGo"))
            stdFieldFontNames.put("HeiseiMin-W3", PdfName("KaMi"))
            stdFieldFontNames.put("MHei-Medium", PdfName("MHei"))
            stdFieldFontNames.put("MSung-Light", PdfName("MSun"))
            stdFieldFontNames.put("STSong-Light", PdfName("STSo"))
            stdFieldFontNames.put("MSungStd-Light", PdfName("MSun"))
            stdFieldFontNames.put("STSongStd-Light", PdfName("STSo"))
            stdFieldFontNames.put("HYSMyeongJoStd-Medium", PdfName("HySm"))
            stdFieldFontNames.put("KozMinPro-Regular", PdfName("KaMi"))
        }

        /**
         * Creates a new appearance to be used with form fields.

         * @param writer the PdfWriter to use
         * *
         * @param width the bounding box width
         * *
         * @param height the bounding box height
         * *
         * @return the appearance created
         */
        fun createAppearance(writer: PdfWriter, width: Float, height: Float): PdfAppearance {
            return createAppearance(writer, width, height, null)
        }

        internal fun createAppearance(writer: PdfWriter, width: Float, height: Float, forcedName: PdfName?): PdfAppearance {
            val template = PdfAppearance(writer)
            template.width = width
            template.height = height
            writer.addDirectTemplateSimple(template, forcedName)
            return template
        }
    }
}
