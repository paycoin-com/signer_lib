/*
 * $Id: 292a64c874f711a8e7655c0bbeb3150d99370a60 $
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
package com.itextpdf.text.pdf.security

import com.itextpdf.text.DocumentException
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfDeveloperExtension
import com.itextpdf.text.pdf.PdfDictionary
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfSignature
import com.itextpdf.text.pdf.PdfSignatureAppearance
import com.itextpdf.text.pdf.PdfString

import java.io.IOException
import java.io.InputStream
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.util.HashMap

/**
 * PAdES-LTV Timestamp
 * @author Paulo Soares
 */
object LtvTimestamp {
    /**
     * Signs a document with a PAdES-LTV Timestamp. The document is closed at the end.
     * @param sap the signature appearance
     * *
     * @param tsa the timestamp generator
     * *
     * @param signatureName the signature name or null to have a name generated
     * * automatically
     * *
     * @throws DocumentException
     * *
     * @throws IOException
     * *
     * @throws GeneralSecurityException
     */
    @Throws(IOException::class, DocumentException::class, GeneralSecurityException::class)
    fun timestamp(sap: PdfSignatureAppearance, tsa: TSAClient, signatureName: String) {
        val contentEstimated = tsa.tokenSizeEstimate
        sap.addDeveloperExtension(PdfDeveloperExtension.ESIC_1_7_EXTENSIONLEVEL5)
        sap.setVisibleSignature(Rectangle(0f, 0f, 0f, 0f), 1, signatureName)

        val dic = PdfSignature(PdfName.ADOBE_PPKLITE, PdfName.ETSI_RFC3161)
        dic.put(PdfName.TYPE, PdfName.DOCTIMESTAMP)
        sap.cryptoDictionary = dic

        val exc = HashMap<PdfName, Int>()
        exc.put(PdfName.CONTENTS, contentEstimated * 2 + 2)
        sap.preClose(exc)
        val data = sap.rangeStream
        val messageDigest = tsa.messageDigest
        val buf = ByteArray(4096)
        var n: Int
        while ((n = data.read(buf)) > 0) {
            messageDigest.update(buf, 0, n)
        }
        val tsImprint = messageDigest.digest()
        val tsToken: ByteArray
        try {
            tsToken = tsa.getTimeStampToken(tsImprint)
        } catch (e: Exception) {
            throw GeneralSecurityException(e)
        }

        if (contentEstimated + 2 < tsToken.size)
            throw IOException("Not enough space")

        val paddedSig = ByteArray(contentEstimated)
        System.arraycopy(tsToken, 0, paddedSig, 0, tsToken.size)

        val dic2 = PdfDictionary()
        dic2.put(PdfName.CONTENTS, PdfString(paddedSig).setHexWriting(true))
        sap.close(dic2)
    }
}
