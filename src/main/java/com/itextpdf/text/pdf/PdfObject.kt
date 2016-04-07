/*
 * $Id: 2ab8bf5772c22c32b24335809056f6a2fc61d25c $
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

import com.itextpdf.text.pdf.internal.PdfIsoKeys

import java.io.IOException
import java.io.OutputStream
import java.io.Serializable

/**
 * PdfObject is the abstract superclass of all PDF objects.
 *
 * PDF supports seven basic types of objects: Booleans, numbers, strings, names,
 * arrays, dictionaries and streams. In addition, PDF provides a null object.
 * Objects may be labeled so that they can be referred to by other objects.
 * All these basic PDF objects are described in the 'Portable Document Format
 * Reference Manual version 1.3' Chapter 4 (pages 37-54).

 * @see PdfNull

 * @see PdfBoolean

 * @see PdfNumber

 * @see PdfString

 * @see PdfName

 * @see PdfArray

 * @see PdfDictionary

 * @see PdfStream

 * @see PdfIndirectReference
 */
abstract class PdfObject : Serializable {

    // CLASS VARIABLES

    /** The content of this PdfObject  */
    /**
     * Gets the presentation of this object in a byte array

     * @return a byte array
     */
    var bytes: ByteArray? = null
        protected set

    /** The type of this PdfObject  */
    protected var type: Int = 0

    /** Holds the indirect reference.  */
    /**
     * Get the indirect reference

     * @return A PdfIndirectReference
     */
    /**
     * Set the indirect reference

     * @param indRef New value as a PdfIndirectReference
     */
    var indRef: PRIndirectReference

    // CONSTRUCTORS

    /**
     * Constructs a PdfObject of a certain type
     * without any content.

     * @param type    type of the new PdfObject
     */
    protected constructor(type: Int) {
        this.type = type
    }

    /**
     * Constructs a PdfObject of a certain type
     * with a certain content.

     * @param type     type of the new PdfObject
     * *
     * @param content  content of the new PdfObject as a
     * *   String.
     */
    protected constructor(type: Int, content: String) {
        this.type = type
        bytes = PdfEncodings.convertToBytes(content, null)
    }

    /**
     * Constructs a PdfObject of a certain type
     * with a certain content.

     * @param type   type of the new PdfObject
     * *
     * @param bytes  content of the new PdfObject as an array of
     * *   byte.
     */
    protected constructor(type: Int, bytes: ByteArray) {
        this.bytes = bytes
        this.type = type
    }

    // methods dealing with the content of this object

    /**
     * Writes the PDF representation of this PdfObject as an
     * array of bytes to the writer.

     * @param writer for backwards compatibility
     * *
     * @param os     The OutputStream to write the bytes to.
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    open fun toPdf(writer: PdfWriter, os: OutputStream) {
        if (bytes != null) {
            PdfWriter.checkPdfIsoConformance(writer, PdfIsoKeys.PDFISOKEY_OBJECT, this)
            os.write(bytes)
        }
    }

    /**
     * Returns the String-representation of this
     * PdfObject.

     * @return    a String
     */
    override fun toString(): String {
        if (bytes == null)
            return super.toString()
        return PdfEncodings.convertToString(bytes, null)
    }

    /**
     * Whether this object can be contained in an object stream.

     * PdfObjects of type STREAM OR INDIRECT can not be contained in an
     * object stream.

     * @return true if this object can be in an object stream.
     * *   Otherwise false
     */
    fun canBeInObjStm(): Boolean {
        when (type) {
            NULL, BOOLEAN, NUMBER, STRING, NAME, ARRAY, DICTIONARY -> return true
            STREAM, INDIRECT,
            else -> return false
        }
    }

    /**
     * Returns the length of the actual content of the PdfObject.
     *
     * In some cases, namely for PdfString and PdfStream,
     * this method differs from the method pdfLength because pdfLength
     * returns the length of the PDF representation of the object, not of the actual content
     * as does the method length.
     *
     * Remark: the actual content of an object is in some cases identical to its representation.
     * The following statement is always true: length() &gt;= pdfLength().

     * @return The length as int
     */
    fun length(): Int {
        return toString().length
    }

    /**
     * Changes the content of this PdfObject.

     * @param content    the new content of this PdfObject
     */
    protected fun setContent(content: String) {
        bytes = PdfEncodings.convertToBytes(content, null)
    }

    // methods dealing with the type of this object

    /**
     * Returns the type of this PdfObject.

     * May be either of:
     * - NULL: A PdfNull
     * - BOOLEAN: A PdfBoolean
     * - NUMBER: A PdfNumber
     * - STRING: A PdfString
     * - NAME: A PdfName
     * - ARRAY: A PdfArray
     * - DICTIONARY: A PdfDictionary
     * - STREAM: A PdfStream
     * - INDIRECT: >PdfIndirectObject

     * @return The type
     */
    fun type(): Int {
        return type
    }

    /**
     * Checks if this PdfObject is of the type
     * PdfNull.

     * @return true or false
     */
    val isNull: Boolean
        get() = type == NULL

    /**
     * Checks if this PdfObject is of the type
     * PdfBoolean.

     * @return true or false
     */
    val isBoolean: Boolean
        get() = type == BOOLEAN

    /**
     * Checks if this PdfObject is of the type
     * PdfNumber.

     * @return true or false
     */
    val isNumber: Boolean
        get() = type == NUMBER

    /**
     * Checks if this PdfObject is of the type
     * PdfString.

     * @return true or false
     */
    val isString: Boolean
        get() = type == STRING

    /**
     * Checks if this PdfObject is of the type
     * PdfName.

     * @return true or false
     */
    val isName: Boolean
        get() = type == NAME

    /**
     * Checks if this PdfObject is of the type
     * PdfArray.

     * @return true or false
     */
    val isArray: Boolean
        get() = type == ARRAY

    /**
     * Checks if this PdfObject is of the type
     * PdfDictionary.

     * @return true or false
     */
    val isDictionary: Boolean
        get() = type == DICTIONARY

    /**
     * Checks if this PdfObject is of the type
     * PdfStream.

     * @return true or false
     */
    val isStream: Boolean
        get() = type == STREAM

    /**
     * Checks if this PdfObject is of the type
     * PdfIndirectObject.

     * @return true if this is an indirect object,
     * *   otherwise false
     */
    val isIndirect: Boolean
        get() = type == INDIRECT

    companion object {

        // CONSTANTS

        /** A possible type of PdfObject  */
        val BOOLEAN = 1

        /** A possible type of PdfObject  */
        val NUMBER = 2

        /** A possible type of PdfObject  */
        val STRING = 3

        /** A possible type of PdfObject  */
        val NAME = 4

        /** A possible type of PdfObject  */
        val ARRAY = 5

        /** A possible type of PdfObject  */
        val DICTIONARY = 6

        /** A possible type of PdfObject  */
        val STREAM = 7

        /** A possible type of PdfObject  */
        val NULL = 8

        /** A possible type of PdfObject  */
        val INDIRECT = 10

        /** An empty string used for the PdfNull-object and for an empty PdfString-object.  */
        val NOTHING = ""

        /**
         * This is the default encoding to be used for converting Strings into
         * bytes and vice versa. The default encoding is PdfDocEncoding.
         */
        val TEXT_PDFDOCENCODING = "PDF"

        /** This is the encoding to be used to output text in Unicode.  */
        val TEXT_UNICODE = "UnicodeBig"
    }
}
