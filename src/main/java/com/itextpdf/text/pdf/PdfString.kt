/*
 * $Id: 177e790c73cc4982059aa0c24edccc724de1afa0 $
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

/**
 * A PdfString-class is the PDF-equivalent of a
 * JAVA-String-object.
 *
 * A string is a sequence of characters delimited by parenthesis.
 * If a string is too long to be conveniently placed on a single line, it may
 * be split across multiple lines by using the backslash character (\) at the
 * end of a line to indicate that the string continues on the following line.
 * Within a string, the backslash character is used as an escape to specify
 * unbalanced parenthesis, non-printing ASCII characters, and the backslash
 * character itself. Use of the \ddd escape sequence is the preferred
 * way to represent characters outside the printable ASCII character set.
 * This object is described in the 'Portable Document Format Reference Manual
 * version 1.7' section 3.2.3 (page 53-56).

 * @see PdfObject

 * @see BadPdfFormatException
 */
open class PdfString : PdfObject {

    // CLASS VARIABLES

    /** The value of this object.  */
    protected var value = PdfObject.NOTHING

    protected var originalValue: String? = null

    /** The encoding.  */
    /**
     * Gets the encoding of this string.

     * @return a String
     */
    var encoding: String? = PdfObject.TEXT_PDFDOCENCODING
        protected set

    protected var objNum = 0

    protected var objGen = 0

    protected var hexWriting = false

    // CONSTRUCTORS

    /**
     * Constructs an empty PdfString-object.
     */
    constructor() : super(PdfObject.STRING) {
    }

    /**
     * Constructs a PdfString-object containing a string in the
     * standard encoding TEXT_PDFDOCENCODING.

     * @param value    the content of the string
     */
    constructor(value: String) : super(PdfObject.STRING) {
        this.value = value
    }

    /**
     * Constructs a PdfString-object containing a string in the
     * specified encoding.

     * @param value    the content of the string
     * *
     * @param encoding an encoding
     */
    constructor(value: String, encoding: String) : super(PdfObject.STRING) {
        this.value = value
        this.encoding = encoding
    }

    /**
     * Constructs a PdfString-object.

     * @param bytes    an array of byte
     */
    constructor(bytes: ByteArray) : super(PdfObject.STRING) {
        value = PdfEncodings.convertToString(bytes, null)
        encoding = PdfObject.NOTHING
    }

    // methods overriding some methods in PdfObject

    /**
     * Writes the PDF representation of this PdfString as an array
     * of byte to the specified OutputStream.

     * @param writer for backwards compatibility
     * *
     * @param os The OutputStream to write the bytes to.
     */
    @Throws(IOException::class)
    override fun toPdf(writer: PdfWriter?, os: OutputStream) {
        PdfWriter.checkPdfIsoConformance(writer, PdfIsoKeys.PDFISOKEY_OBJECT, this)
        var b = bytes
        var crypto: PdfEncryption? = null
        if (writer != null)
            crypto = writer.encryption
        if (crypto != null && !crypto.isEmbeddedFilesOnly)
            b = crypto.encryptByteArray(b)
        if (hexWriting) {
            val buf = ByteBuffer()
            buf.append('<')
            val len = b.size
            for (k in 0..len - 1)
                buf.appendHex(b[k])
            buf.append('>')
            os.write(buf.toByteArray())
        } else
            os.write(StringUtils.escapeString(b))
    }

    /**
     * Returns the String value of this PdfString-object.

     * @return A String
     */
    override fun toString(): String {
        return value
    }

    override var bytes: ByteArray
        get() {
            if (bytes == null) {
                if (encoding != null && encoding == PdfObject.TEXT_UNICODE && PdfEncodings.isPdfDocEncoding(value))
                    bytes = PdfEncodings.convertToBytes(value, PdfObject.TEXT_PDFDOCENCODING)
                else
                    bytes = PdfEncodings.convertToBytes(value, encoding)
            }
            return bytes
        }
        set(value: ByteArray) {
            super.bytes = value
        }

    // other methods

    /**
     * Returns the Unicode String value of this
     * PdfString-object.

     * @return A String
     */
    fun toUnicodeString(): String {
        if (encoding != null && encoding!!.length != 0)
            return value
        bytes
        if (bytes!!.size >= 2 && bytes!![0] == 254.toByte() && bytes!![1] == 255.toByte())
            return PdfEncodings.convertToString(bytes, PdfObject.TEXT_UNICODE)
        else
            return PdfEncodings.convertToString(bytes, PdfObject.TEXT_PDFDOCENCODING)
    }

    internal fun setObjNum(objNum: Int, objGen: Int) {
        this.objNum = objNum
        this.objGen = objGen
    }

    /**
     * Decrypt an encrypted PdfString
     */
    internal fun decrypt(reader: PdfReader) {
        val decrypt = reader.decrypt
        if (decrypt != null) {
            originalValue = value
            decrypt.setHashKey(objNum, objGen)
            bytes = PdfEncodings.convertToBytes(value, null)
            bytes = decrypt.decryptByteArray(bytes)
            value = PdfEncodings.convertToString(bytes, null)
        }
    }

    val originalBytes: ByteArray
        get() {
            if (originalValue == null)
                return bytes
            return PdfEncodings.convertToBytes(originalValue, null)
        }

    fun setHexWriting(hexWriting: Boolean): PdfString {
        this.hexWriting = hexWriting
        return this
    }

    fun isHexWriting(): Boolean {
        return hexWriting
    }
}
