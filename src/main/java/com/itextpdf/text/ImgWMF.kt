/*
 * $Id: 18d4e8035eecb182fb0b2049ee94109ab17481f4 $
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

import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import com.itextpdf.text.error_messages.MessageLocalization

import com.itextpdf.text.pdf.PdfTemplate
import com.itextpdf.text.pdf.codec.wmf.InputMeta
import com.itextpdf.text.pdf.codec.wmf.MetaDo

/**
 * An ImgWMF is the representation of a windows metafile
 * that has to be inserted into the document

 * @see Element

 * @see Image
 */

class ImgWMF : Image {

    // Constructors

    internal constructor(image: Image) : super(image) {
    }

    /**
     * Constructs an ImgWMF-object, using an url.

     * @param url the URL where the image can be found
     * *
     * @throws BadElementException on error
     * *
     * @throws IOException on error
     */

    @Throws(BadElementException::class, IOException::class)
    constructor(url: URL) : super(url) {
        processParameters()
    }

    /**
     * Constructs an ImgWMF-object, using a filename.

     * @param filename a String-representation of the file that contains the image.
     * *
     * @throws BadElementException on error
     * *
     * @throws MalformedURLException on error
     * *
     * @throws IOException on error
     */

    @Throws(BadElementException::class, MalformedURLException::class, IOException::class)
    constructor(filename: String) : this(Utilities.toURL(filename)) {
    }

    /**
     * Constructs an ImgWMF-object from memory.

     * @param img the memory image
     * *
     * @throws BadElementException on error
     * *
     * @throws IOException on error
     */

    @Throws(BadElementException::class, IOException::class)
    constructor(img: ByteArray) : super(null as URL) {
        rawData = img
        originalData = img
        processParameters()
    }

    /**
     * This method checks if the image is a valid WMF and processes some parameters.
     * @throws BadElementException
     * *
     * @throws IOException
     */

    @Throws(BadElementException::class, IOException::class)
    private fun processParameters() {
        type = Element.IMGTEMPLATE
        originalType = Image.ORIGINAL_WMF
        var `is`: InputStream? = null
        try {
            val errorID: String
            if (rawData == null) {
                `is` = url.openStream()
                errorID = url.toString()
            } else {
                `is` = java.io.ByteArrayInputStream(rawData)
                errorID = "Byte array"
            }
            val `in` = InputMeta(`is`)
            if (`in`.readInt() != 0x9AC6CDD7.toInt()) {
                throw BadElementException(MessageLocalization.getComposedMessage("1.is.not.a.valid.placeable.windows.metafile", errorID))
            }
            `in`.readWord()
            val left = `in`.readShort()
            var top = `in`.readShort()
            var right = `in`.readShort()
            val bottom = `in`.readShort()
            val inch = `in`.readWord()
            dpiX = 72
            dpiY = 72
            scaledHeight = (bottom - top).toFloat() / inch * 72f
            top = scaledHeight
            scaledWidth = (right - left).toFloat() / inch * 72f
            right = scaledWidth
        } finally {
            if (`is` != null) {
                `is`.close()
            }
            plainWidth = width
            plainHeight = height
        }
    }

    /** Reads the WMF into a template.
     * @param template the template to read to
     * *
     * @throws IOException on error
     * *
     * @throws DocumentException on error
     */
    @Throws(IOException::class, DocumentException::class)
    fun readWMF(template: PdfTemplate) {
        templateData = template
        template.width = width
        template.height = height
        var `is`: InputStream? = null
        try {
            if (rawData == null) {
                `is` = url.openStream()
            } else {
                `is` = java.io.ByteArrayInputStream(rawData)
            }
            val meta = MetaDo(`is`, template)
            meta.readAll()
        } finally {
            if (`is` != null) {
                `is`.close()
            }
        }
    }
}
