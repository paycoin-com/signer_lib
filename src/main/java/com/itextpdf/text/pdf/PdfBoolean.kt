/*
 * $Id: ccb68e60fc055ec684bd1448a6ac6de4f97ded31 $
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
 * PdfBoolean is the boolean object represented by the keywords true or false.
 *
 * This object is described in the 'Portable Document Format Reference Manual version 1.7'
 * section 3.2.1 (page 52).

 * @see PdfObject

 * @see BadPdfFormatException
 */

class PdfBoolean : PdfObject {

    // membervariables

    /** the boolean value of this object  */
    private var value: Boolean = false

    // constructors

    /**
     * Constructs a PdfBoolean-object.

     * @param        value            the value of the new PdfObject
     */

    constructor(value: Boolean) : super(PdfObject.BOOLEAN) {
        if (value) {
            setContent(TRUE)
        } else {
            setContent(FALSE)
        }
        this.value = value
    }

    /**
     * Constructs a PdfBoolean-object.

     * @param        value            the value of the new PdfObject, represented as a String
     * *
     * *
     * @throws        BadPdfFormatException    thrown if the value isn't 'true' or 'false'
     */

    @Throws(BadPdfFormatException::class)
    constructor(value: String) : super(PdfObject.BOOLEAN, value) {
        if (value == TRUE) {
            this.value = true
        } else if (value == FALSE) {
            this.value = false
        } else {
            throw BadPdfFormatException(MessageLocalization.getComposedMessage("the.value.has.to.be.true.of.false.instead.of.1", value))
        }
    }

    // methods returning the value of this object

    /**
     * Returns the primitive value of the PdfBoolean-object.

     * @return        the actual value of the object.
     */

    fun booleanValue(): Boolean {
        return value
    }

    override fun toString(): String {
        return if (value) TRUE else FALSE
    }

    companion object {

        // static membervariables (possible values of a boolean object)
        val PDFTRUE = PdfBoolean(true)
        val PDFFALSE = PdfBoolean(false)
        /** A possible value of PdfBoolean  */
        val TRUE = "true"

        /** A possible value of PdfBoolean  */
        val FALSE = "false"
    }
}
