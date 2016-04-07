/*
 * $Id: 0a11369e5fea510739fc28d7c108d6cc0c7dc0be $
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

import com.itextpdf.text.DocumentException
import com.itextpdf.text.Image
import com.itextpdf.text.error_messages.MessageLocalization

/**
 * The content where Type3 glyphs are written to.
 */
class Type3Glyph : PdfContentByte {

    internal override var pageResources:

            PageResources? = null
        private set(value: PageResources?) {
            super.pageResources = value
        }
    private var colorized: Boolean = false

    private constructor() : super(null) {
    }

    internal constructor(writer: PdfWriter, pageResources: PageResources, wx: Float, llx: Float, lly: Float, urx: Float, ury: Float, colorized: Boolean) : super(writer) {
        this.pageResources = pageResources
        this.colorized = colorized
        if (colorized) {
            internalBuffer.append(wx).append(" 0 d0\n")
        } else {
            internalBuffer.append(wx).append(" 0 ").append(llx).append(' ').append(lly).append(' ').append(urx).append(' ').append(ury).append(" d1\n")
        }
    }

    @Throws(DocumentException::class)
    override fun addImage(image: Image, a: Float, b: Float, c: Float, d: Float, e: Float, f: Float, inlineImage: Boolean) {
        if (!colorized && (!image.isMask || !(image.bpc == 1 || image.bpc > 0xff)))
            throw DocumentException(MessageLocalization.getComposedMessage("not.colorized.typed3.fonts.only.accept.mask.images"))
        super.addImage(image, a, b, c, d, e, f, inlineImage)
    }

    override val duplicate: PdfContentByte
        get() {
            val dup = Type3Glyph()
            dup.pdfWriter = pdfWriter
            dup.pdfDocument = pdfDocument
            dup.pageResources = pageResources
            dup.colorized = colorized
            return dup
        }

}
