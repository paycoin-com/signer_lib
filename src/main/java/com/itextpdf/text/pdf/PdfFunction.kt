/*
 * $Id: 57ce80f55b55cd829ff80c86d24cde21b6a65f30 $
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

import com.itextpdf.text.ExceptionConverter

/** Implements PDF functions.

 * @author Paulo Soares
 */
class PdfFunction
/** Creates new PdfFunction  */
protected constructor(protected var writer: PdfWriter) {

    protected var reference: PdfIndirectReference? = null

    protected var dictionary: PdfDictionary

    internal fun getReference(): PdfIndirectReference {
        try {
            if (reference == null) {
                reference = writer.addToBody(dictionary).indirectReference
            }
        } catch (ioe: IOException) {
            throw ExceptionConverter(ioe)
        }

        return reference
    }

    companion object {

        fun type0(writer: PdfWriter, domain: FloatArray, range: FloatArray, size: IntArray,
                  bitsPerSample: Int, order: Int, encode: FloatArray?, decode: FloatArray?, stream: ByteArray): PdfFunction {
            val func = PdfFunction(writer)
            func.dictionary = PdfStream(stream)
            (func.dictionary as PdfStream).flateCompress(writer.compressionLevel)
            func.dictionary.put(PdfName.FUNCTIONTYPE, PdfNumber(0))
            func.dictionary.put(PdfName.DOMAIN, PdfArray(domain))
            func.dictionary.put(PdfName.RANGE, PdfArray(range))
            func.dictionary.put(PdfName.SIZE, PdfArray(size))
            func.dictionary.put(PdfName.BITSPERSAMPLE, PdfNumber(bitsPerSample))
            if (order != 1)
                func.dictionary.put(PdfName.ORDER, PdfNumber(order))
            if (encode != null)
                func.dictionary.put(PdfName.ENCODE, PdfArray(encode))
            if (decode != null)
                func.dictionary.put(PdfName.DECODE, PdfArray(decode))
            return func
        }

        fun type2(writer: PdfWriter, domain: FloatArray, range: FloatArray?, c0: FloatArray?, c1: FloatArray?, n: Float): PdfFunction {
            val func = PdfFunction(writer)
            func.dictionary = PdfDictionary()
            func.dictionary.put(PdfName.FUNCTIONTYPE, PdfNumber(2))
            func.dictionary.put(PdfName.DOMAIN, PdfArray(domain))
            if (range != null)
                func.dictionary.put(PdfName.RANGE, PdfArray(range))
            if (c0 != null)
                func.dictionary.put(PdfName.C0, PdfArray(c0))
            if (c1 != null)
                func.dictionary.put(PdfName.C1, PdfArray(c1))
            func.dictionary.put(PdfName.N, PdfNumber(n))
            return func
        }

        fun type3(writer: PdfWriter, domain: FloatArray, range: FloatArray?, functions: Array<PdfFunction>, bounds: FloatArray, encode: FloatArray): PdfFunction {
            val func = PdfFunction(writer)
            func.dictionary = PdfDictionary()
            func.dictionary.put(PdfName.FUNCTIONTYPE, PdfNumber(3))
            func.dictionary.put(PdfName.DOMAIN, PdfArray(domain))
            if (range != null)
                func.dictionary.put(PdfName.RANGE, PdfArray(range))
            val array = PdfArray()
            for (k in functions.indices)
                array.add(functions[k].getReference())
            func.dictionary.put(PdfName.FUNCTIONS, array)
            func.dictionary.put(PdfName.BOUNDS, PdfArray(bounds))
            func.dictionary.put(PdfName.ENCODE, PdfArray(encode))
            return func
        }

        fun type4(writer: PdfWriter, domain: FloatArray, range: FloatArray, postscript: String): PdfFunction {
            val b = ByteArray(postscript.length)
            for (k in b.indices)
                b[k] = postscript[k].toByte()
            val func = PdfFunction(writer)
            func.dictionary = PdfStream(b)
            (func.dictionary as PdfStream).flateCompress(writer.compressionLevel)
            func.dictionary.put(PdfName.FUNCTIONTYPE, PdfNumber(4))
            func.dictionary.put(PdfName.DOMAIN, PdfArray(domain))
            func.dictionary.put(PdfName.RANGE, PdfArray(range))
            return func
        }
    }
}
