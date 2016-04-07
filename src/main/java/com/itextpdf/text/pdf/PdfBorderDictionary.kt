/*
 * $Id: 5fc813ff93116749556bac090f92e6cedd768e6e $
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

/**
 * A PdfBorderDictionary define the appearance of a Border (Annotations).

 * @see PdfDictionary
 */

class PdfBorderDictionary// constructors

/**
 * Constructs a PdfBorderDictionary.
 */
@JvmOverloads constructor(borderWidth: Float, borderStyle: Int, dashes: PdfDashPattern? = null) : PdfDictionary() {
    init {
        put(PdfName.W, PdfNumber(borderWidth))
        when (borderStyle) {
            STYLE_SOLID -> put(PdfName.S, PdfName.S)
            STYLE_DASHED -> {
                if (dashes != null)
                    put(PdfName.D, dashes)
                put(PdfName.S, PdfName.D)
            }
            STYLE_BEVELED -> put(PdfName.S, PdfName.B)
            STYLE_INSET -> put(PdfName.S, PdfName.I)
            STYLE_UNDERLINE -> put(PdfName.S, PdfName.U)
            else -> throw IllegalArgumentException(MessageLocalization.getComposedMessage("invalid.border.style"))
        }
    }

    companion object {

        val STYLE_SOLID = 0
        val STYLE_DASHED = 1
        val STYLE_BEVELED = 2
        val STYLE_INSET = 3
        val STYLE_UNDERLINE = 4
    }
}
