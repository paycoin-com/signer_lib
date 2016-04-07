/*
 * $Id: 16d2874b421cbc38aeeb106c9b2aac57a91fb99c $
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
 * PdfNumber provides two types of numbers, integer and real.
 *
 * Integers may be specified by signed or unsigned constants. Reals may only be
 * in decimal format.
 * This object is described in the 'Portable Document Format Reference Manual
 * version 1.7' section 3.3.2 (page 52-53).

 * @see PdfObject

 * @see BadPdfFormatException
 */
class PdfNumber : PdfObject {

    // CLASS VARIABLES

    /**
     * actual value of this PdfNumber, represented as a
     * double
     */
    private var value: Double = 0.toDouble()

    // CONSTRUCTORS

    /**
     * Constructs a PdfNumber-object.

     * @param content    value of the new PdfNumber-object
     */
    constructor(content: String) : super(PdfObject.NUMBER) {
        try {
            value = java.lang.Double.parseDouble(content.trim { it <= ' ' })
            setContent(content)
        } catch (nfe: NumberFormatException) {
            throw RuntimeException(MessageLocalization.getComposedMessage("1.is.not.a.valid.number.2", content, nfe.toString()))
        }

    }

    /**
     * Constructs a new PdfNumber-object of type integer.

     * @param value    value of the new PdfNumber-object
     */
    constructor(value: Int) : super(PdfObject.NUMBER) {
        this.value = value.toDouble()
        setContent(value.toString())
    }

    /**
     * Constructs a new PdfNumber-object of type long.

     * @param value    value of the new PdfNumber-object
     */
    constructor(value: Long) : super(PdfObject.NUMBER) {
        this.value = value.toDouble()
        setContent(value.toString())
    }

    /**
     * Constructs a new PdfNumber-object of type real.

     * @param value    value of the new PdfNumber-object
     */
    constructor(value: Double) : super(PdfObject.NUMBER) {
        this.value = value
        setContent(ByteBuffer.formatDouble(value))
    }

    /**
     * Constructs a new PdfNumber-object of type real.

     * @param value    value of the new PdfNumber-object
     */
    constructor(value: Float) : this(value.toDouble()) {
    }

    // methods returning the value of this object

    /**
     * Returns the primitive int value of this object.

     * @return The value as int
     */
    fun intValue(): Int {
        return value.toInt()
    }

    /**
     * Returns the primitive long value of this object.

     * @return The value as long
     */
    fun longValue(): Long {
        return value.toLong()
    }

    /**
     * Returns the primitive double value of this object.

     * @return The value as double
     */
    fun doubleValue(): Double {
        return value
    }

    /**
     * Returns the primitive float value of this object.

     * @return The value as float
     */
    fun floatValue(): Float {
        return value.toFloat()
    }

    // other methods

    /**
     * Increments the value of the PdfNumber-object by 1.
     */
    fun increment() {
        value += 1.0
        setContent(ByteBuffer.formatDouble(value))
    }
}
