/*
 * $Id: 73660d1cc10319088a45f21745c91c9ec239b7c2 $
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

import java.io.IOException
import java.io.OutputStream

import com.itextpdf.text.DocWriter

/**
 * PdfIndirectObject is the Pdf indirect object.
 *
 * An indirect object is an object that has been labeled so that it can be referenced by
 * other objects. Any type of PdfObject may be labeled as an indirect object.
 * An indirect object consists of an object identifier, a direct object, and the endobj
 * keyword. The object identifier consists of an integer object number, an integer
 * generation number, and the obj keyword.
 * This object is described in the 'Portable Document Format Reference Manual version 1.7'
 * section 3.2.9 (page 63-65).

 * @see PdfObject

 * @see PdfIndirectReference
 */

class PdfIndirectObject
/**
 * Constructs a PdfIndirectObject.

 * @param        number            the object number
 * *
 * @param        generation        the generation number
 * *
 * @param        object            the direct object
 */
internal constructor(// membervariables

        /** The object number  */
        protected var number: Int, generation: Int, protected var `object`: PdfObject?, protected var writer: PdfWriter?) {

    /** the generation number  */
    protected var generation = 0

    // constructors

    /**
     * Constructs a PdfIndirectObject.

     * @param        number            the object number
     * *
     * @param        object            the direct object
     */

    protected constructor(number: Int, `object`: PdfObject, writer: PdfWriter) : this(number, 0, `object`, writer) {
    }

    internal constructor(ref: PdfIndirectReference, `object`: PdfObject, writer: PdfWriter) : this(ref.number, ref.generation, `object`, writer) {
    }

    init {
        this.generation = generation
        var crypto: PdfEncryption? = null
        if (writer != null)
            crypto = writer!!.encryption
        if (crypto != null) {
            crypto.setHashKey(number, generation)
        }
    }

    // methods

    /**
     * Return the length of this PdfIndirectObject.

     * @return        the length of the PDF-representation of this indirect object.
     */

    //    public int length() {
    //        if (isStream)
    //            return bytes.size() + SIZEOBJ + stream.getStreamLength(writer);
    //        else
    //            return bytes.size();
    //    }


    /**
     * Returns a PdfIndirectReference to this PdfIndirectObject.

     * @return        a PdfIndirectReference
     */

    val indirectReference: PdfIndirectReference
        get() = PdfIndirectReference(`object`!!.type(), number, generation)

    /**
     * Writes efficiently to a stream

     * @param os the stream to write to
     * *
     * @throws IOException on write error
     */
    @Throws(IOException::class)
    protected fun writeTo(os: OutputStream) {
        os.write(DocWriter.getISOBytes(number.toString()))
        os.write(' ')
        os.write(DocWriter.getISOBytes(generation.toString()))
        os.write(STARTOBJ)
        `object`!!.toPdf(writer, os)
        os.write(ENDOBJ)
    }

    override fun toString(): String {
        return StringBuffer().append(number).append(' ').append(generation).append(" R: ").append(if (`object` != null) `object`!!.toString() else "null").toString()
    }

    companion object {

        internal val STARTOBJ = DocWriter.getISOBytes(" obj\n")
        internal val ENDOBJ = DocWriter.getISOBytes("\nendobj\n")
        internal val SIZEOBJ = STARTOBJ.size + ENDOBJ.size
    }
}
