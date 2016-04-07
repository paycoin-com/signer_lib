/*
 * $Id: 6e6f2a26db82b38176ed5b6772463c655230f3c4 $
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
import com.itextpdf.text.io.RASInputStream
import com.itextpdf.text.io.RandomAccessSource
import com.itextpdf.text.io.RandomAccessSourceFactory
import com.itextpdf.text.io.StreamUtil
import com.itextpdf.text.log.Logger
import com.itextpdf.text.log.LoggerFactory
import com.itextpdf.text.pdf.AcroFields
import com.itextpdf.text.pdf.ByteBuffer
import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfDate
import com.itextpdf.text.pdf.PdfDeveloperExtension
import com.itextpdf.text.pdf.PdfDictionary
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfSignature
import com.itextpdf.text.pdf.PdfSignatureAppearance
import com.itextpdf.text.pdf.PdfString

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.ArrayList
import java.util.Calendar
import java.util.HashMap

/**
 * Class that signs your PDF.
 * @author Paulo Soares
 */
object MakeSignature {

    /** The Logger instance.  */
    private val LOGGER = LoggerFactory.getLogger(MakeSignature::class.java)

    enum class CryptoStandard {
        CMS, CADES
    }

    /**
     * Signs the document using the detached mode, CMS or CAdES equivalent.
     * @param sap the PdfSignatureAppearance
     * *
     * @param externalSignature the interface providing the actual signing
     * *
     * @param chain the certificate chain
     * *
     * @param crlList the CRL list
     * *
     * @param ocspClient the OCSP client
     * *
     * @param tsaClient the Timestamp client
     * *
     * @param externalDigest an implementation that provides the digest
     * *
     * @param estimatedSize the reserved size for the signature. It will be estimated if 0
     * *
     * @param sigtype Either Signature.CMS or Signature.CADES
     * *
     * @throws DocumentException
     * *
     * @throws IOException
     * *
     * @throws GeneralSecurityException
     * *
     * @throws NoSuchAlgorithmException
     * *
     * @throws Exception
     */
    @Throws(IOException::class, DocumentException::class, GeneralSecurityException::class)
    fun signDetached(sap: PdfSignatureAppearance, externalDigest: ExternalDigest, externalSignature: ExternalSignature, chain: Array<Certificate>, crlList: Collection<CrlClient>, ocspClient: OcspClient?,
                     tsaClient: TSAClient?, estimatedSize: Int, sigtype: CryptoStandard) {
        var estimatedSize = estimatedSize
        var crlBytes: Collection<ByteArray>? = null
        var i = 0
        while (crlBytes == null && i < chain.size)
            crlBytes = processCrl(chain[i++], crlList)
        if (estimatedSize == 0) {
            estimatedSize = 8192
            if (crlBytes != null) {
                for (element in crlBytes) {
                    estimatedSize += element.size + 10
                }
            }
            if (ocspClient != null)
                estimatedSize += 4192
            if (tsaClient != null)
                estimatedSize += 4192
        }
        sap.certificate = chain[0]
        if (sigtype == CryptoStandard.CADES) {
            sap.addDeveloperExtension(PdfDeveloperExtension.ESIC_1_7_EXTENSIONLEVEL2)
        }
        val dic = PdfSignature(PdfName.ADOBE_PPKLITE, if (sigtype == CryptoStandard.CADES) PdfName.ETSI_CADES_DETACHED else PdfName.ADBE_PKCS7_DETACHED)
        dic.setReason(sap.reason)
        dic.setLocation(sap.location)
        dic.setSignatureCreator(sap.signatureCreator)
        dic.setContact(sap.contact)
        dic.setDate(PdfDate(sap.signDate)) // time-stamp will over-rule this
        sap.cryptoDictionary = dic

        val exc = HashMap<PdfName, Int>()
        exc.put(PdfName.CONTENTS, estimatedSize * 2 + 2)
        sap.preClose(exc)

        val hashAlgorithm = externalSignature.hashAlgorithm
        val sgn = PdfPKCS7(null, chain, hashAlgorithm, null, externalDigest, false)
        val data = sap.rangeStream
        val hash = DigestAlgorithms.digest(data, externalDigest.getMessageDigest(hashAlgorithm))
        var ocsp: ByteArray? = null
        if (chain.size >= 2 && ocspClient != null) {
            ocsp = ocspClient.getEncoded(chain[0] as X509Certificate, chain[1] as X509Certificate, null)
        }
        val sh = sgn.getAuthenticatedAttributeBytes(hash, ocsp, crlBytes, sigtype)
        val extSignature = externalSignature.sign(sh)
        sgn.setExternalDigest(extSignature, null, externalSignature.encryptionAlgorithm)

        val encodedSig = sgn.getEncodedPKCS7(hash, tsaClient, ocsp, crlBytes, sigtype)

        if (estimatedSize < encodedSig.size)
            throw IOException("Not enough space")

        val paddedSig = ByteArray(estimatedSize)
        System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.size)

        val dic2 = PdfDictionary()
        dic2.put(PdfName.CONTENTS, PdfString(paddedSig).setHexWriting(true))
        sap.close(dic2)
    }

    /**
     * Processes a CRL list.
     * @param cert    a Certificate if one of the CrlList implementations needs to retrieve the CRL URL from it.
     * *
     * @param crlList    a list of CrlClient implementations
     * *
     * @return    a collection of CRL bytes that can be embedded in a PDF.
     */
    fun processCrl(cert: Certificate, crlList: Collection<CrlClient>?): Collection<ByteArray>? {
        if (crlList == null)
            return null
        val crlBytes = ArrayList<ByteArray>()
        for (cc in crlList) {
            if (cc == null)
                continue
            LOGGER.info("Processing " + cc.javaClass.name)
            val b = cc.getEncoded(cert as X509Certificate, null) ?: continue
            crlBytes.addAll(b)
        }
        if (crlBytes.isEmpty())
            return null
        else
            return crlBytes
    }

    /**
     * Sign the document using an external container, usually a PKCS7. The signature is fully composed
     * externally, iText will just put the container inside the document.
     * @param sap the PdfSignatureAppearance
     * *
     * @param externalSignatureContainer the interface providing the actual signing
     * *
     * @param estimatedSize the reserved size for the signature
     * *
     * @throws GeneralSecurityException
     * *
     * @throws IOException
     * *
     * @throws DocumentException
     */
    @Throws(GeneralSecurityException::class, IOException::class, DocumentException::class)
    fun signExternalContainer(sap: PdfSignatureAppearance, externalSignatureContainer: ExternalSignatureContainer, estimatedSize: Int) {
        val dic = PdfSignature(null, null)
        dic.setReason(sap.reason)
        dic.setLocation(sap.location)
        dic.setSignatureCreator(sap.signatureCreator)
        dic.setContact(sap.contact)
        dic.setDate(PdfDate(sap.signDate)) // time-stamp will over-rule this
        externalSignatureContainer.modifySigningDictionary(dic)
        sap.cryptoDictionary = dic

        val exc = HashMap<PdfName, Int>()
        exc.put(PdfName.CONTENTS, estimatedSize * 2 + 2)
        sap.preClose(exc)

        val data = sap.rangeStream
        val encodedSig = externalSignatureContainer.sign(data)

        if (estimatedSize < encodedSig.size)
            throw IOException("Not enough space")

        val paddedSig = ByteArray(estimatedSize)
        System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.size)

        val dic2 = PdfDictionary()
        dic2.put(PdfName.CONTENTS, PdfString(paddedSig).setHexWriting(true))
        sap.close(dic2)
    }

    /**
     * Signs a PDF where space was already reserved.
     * @param reader the original PDF
     * *
     * @param fieldName the field to sign. It must be the last field
     * *
     * @param outs the output PDF
     * *
     * @param externalSignatureContainer the signature container doing the actual signing. Only the
     * * method ExternalSignatureContainer.sign is used
     * *
     * @throws DocumentException
     * *
     * @throws IOException
     * *
     * @throws GeneralSecurityException
     */
    @Throws(DocumentException::class, IOException::class, GeneralSecurityException::class)
    fun signDeferred(reader: PdfReader, fieldName: String, outs: OutputStream, externalSignatureContainer: ExternalSignatureContainer) {
        val af = reader.acroFields
        val v = af.getSignatureDictionary(fieldName) ?: throw DocumentException("No field")
        if (!af.signatureCoversWholeDocument(fieldName))
            throw DocumentException("Not the last signature")
        val b = v.getAsArray(PdfName.BYTERANGE)
        val gaps = b.asLongArray()
        if (b.size() != 4 || gaps[0] != 0)
            throw DocumentException("Single exclusion space supported")
        val readerSource = reader.safeFile.createSourceView()
        val rg = RASInputStream(RandomAccessSourceFactory().createRanged(readerSource, gaps))
        val signedContent = externalSignatureContainer.sign(rg)
        var spaceAvailable = (gaps[2] - gaps[1]).toInt() - 2
        if (spaceAvailable and 1 != 0)
            throw DocumentException("Gap is not a multiple of 2")
        spaceAvailable /= 2
        if (spaceAvailable < signedContent.size)
            throw DocumentException("Not enough space")
        StreamUtil.CopyBytes(readerSource, 0, gaps[1] + 1, outs)
        val bb = ByteBuffer(spaceAvailable * 2)
        for (bi in signedContent) {
            bb.appendHex(bi)
        }
        val remain = (spaceAvailable - signedContent.size) * 2
        for (k in 0..remain - 1) {
            bb.append(48.toByte())
        }
        bb.writeTo(outs)
        StreamUtil.CopyBytes(readerSource, gaps[2] - 1, gaps[3] + 1, outs)
    }
}
