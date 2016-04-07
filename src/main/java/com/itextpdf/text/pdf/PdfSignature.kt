/*
 * $Id: e0a34cda0b76010fcbbb6a40e94ee6361c2362d1 $
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

import com.itextpdf.text.pdf.security.PdfSignatureBuildProperties

/**
 * Implements the signature dictionary.

 * @author Paulo Soares
 */
class PdfSignature
/** Creates new PdfSignature  */
(filter: PdfName, subFilter: PdfName) : PdfDictionary(PdfName.SIG) {

    init {
        put(PdfName.FILTER, filter)
        put(PdfName.SUBFILTER, subFilter)
    }

    fun setByteRange(range: IntArray) {
        val array = PdfArray()
        for (k in range.indices)
            array.add(PdfNumber(range[k]))
        put(PdfName.BYTERANGE, array)
    }

    fun setContents(contents: ByteArray) {
        put(PdfName.CONTENTS, PdfString(contents).setHexWriting(true))
    }

    fun setCert(cert: ByteArray) {
        put(PdfName.CERT, PdfString(cert))
    }

    fun setName(name: String) {
        put(PdfName.NAME, PdfString(name, PdfObject.TEXT_UNICODE))
    }

    fun setDate(date: PdfDate) {
        put(PdfName.M, date)
    }

    fun setLocation(name: String) {
        put(PdfName.LOCATION, PdfString(name, PdfObject.TEXT_UNICODE))
    }

    fun setReason(name: String) {
        put(PdfName.REASON, PdfString(name, PdfObject.TEXT_UNICODE))
    }

    /**
     * Sets the signature creator name in the
     * [PdfSignatureBuildProperties] dictionary.

     * @param name
     */
    fun setSignatureCreator(name: String?) {
        if (name != null) {
            pdfSignatureBuildProperties.setSignatureCreator(name)
        }
    }

    /**
     * Gets the [PdfSignatureBuildProperties] instance if it exists, if
     * not it adds a new one and returns this.

     * @return [PdfSignatureBuildProperties]
     */
    internal val pdfSignatureBuildProperties: PdfSignatureBuildProperties
        get() {
            var buildPropDic: PdfSignatureBuildProperties? = getAsDict(PdfName.PROP_BUILD) as PdfSignatureBuildProperties
            if (buildPropDic == null) {
                buildPropDic = PdfSignatureBuildProperties()
                put(PdfName.PROP_BUILD, buildPropDic)
            }
            return buildPropDic
        }

    fun setContact(name: String) {
        put(PdfName.CONTACTINFO, PdfString(name, PdfObject.TEXT_UNICODE))
    }
}
