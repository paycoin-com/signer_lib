/*
 * $Id: 294565e31716ed2c0e1259ba8de702747c5ce0ff $
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

import com.itextpdf.text.DocumentException
import com.itextpdf.text.error_messages.MessageLocalization

/**
 * PdfPage is the PDF Page-object.
 *
 * A Page object is a dictionary whose keys describe a single page containing text,
 * graphics, and images. A Page object is a leaf of the Pages tree.
 * This object is described in the 'Portable Document Format Reference Manual version 1.3'
 * section 6.4 (page 73-81)

 * @see PdfPages
 */

class PdfPage// constructors

/**
 * Constructs a PdfPage.

 * @param        mediaBox        a value for the MediaBox key
 * *
 * @param        resources        an indirect reference to a PdfResources-object
 * *
 * @param        rotate            a value for the Rotate key
 * *
 * @throws DocumentException
 */
@Throws(DocumentException::class)
@JvmOverloads internal constructor(
        /** value of the MediaBox key  */
        /**
         * Returns the MediaBox of this Page.

         * @return        a PdfRectangle
         */

        internal var mediaBox: PdfRectangle?, boxSize: HashMap<String, PdfRectangle>, resources: PdfDictionary, rotate: Int = 0) : PdfDictionary(PdfDictionary.PAGE) {
    init {
        if (mediaBox != null && (mediaBox!!.width() > 14400 || mediaBox!!.height() > 14400)) {
            throw DocumentException(MessageLocalization.getComposedMessage("the.page.size.must.be.smaller.than.14400.by.14400.its.1.by.2", mediaBox!!.width(), mediaBox!!.height()))
        }
        put(PdfName.MEDIABOX, mediaBox)
        put(PdfName.RESOURCES, resources)
        if (rotate != 0) {
            put(PdfName.ROTATE, PdfNumber(rotate))
        }
        for (k in boxStrings.indices) {
            val rect = boxSize[boxStrings[k]]
            if (rect != null)
                put(boxNames[k], rect)
        }
    }

    /**
     * Checks if this page element is a tree of pages.
     *
     * This method always returns false.

     * @return    false because this is a single page
     */

    val isParent: Boolean
        get() = false

    // methods

    /**
     * Adds an indirect reference pointing to a PdfContents-object.

     * @param        contents        an indirect reference to a PdfContents-object
     */

    internal fun add(contents: PdfIndirectReference) {
        put(PdfName.CONTENTS, contents)
    }

    /**
     * Rotates the mediabox, but not the text in it.

     * @return        a PdfRectangle
     */

    internal fun rotateMediaBox(): PdfRectangle {
        this.mediaBox = mediaBox.rotate()
        put(PdfName.MEDIABOX, this.mediaBox)
        return this.mediaBox
    }

    companion object {

        private val boxStrings = arrayOf("crop", "trim", "art", "bleed")
        private val boxNames = arrayOf(PdfName.CROPBOX, PdfName.TRIMBOX, PdfName.ARTBOX, PdfName.BLEEDBOX)
        // membervariables

        /** value of the Rotate key for a page in PORTRAIT  */
        val PORTRAIT = PdfNumber(0)

        /** value of the Rotate key for a page in LANDSCAPE  */
        val LANDSCAPE = PdfNumber(90)

        /** value of the Rotate key for a page in INVERTEDPORTRAIT  */
        val INVERTEDPORTRAIT = PdfNumber(180)

        /**	value of the Rotate key for a page in SEASCAPE  */
        val SEASCAPE = PdfNumber(270)
    }
}
/**
 * Constructs a PdfPage.

 * @param        mediaBox        a value for the MediaBox key
 * *
 * @param        resources        an indirect reference to a PdfResources-object
 * *
 * @throws DocumentException
 */
