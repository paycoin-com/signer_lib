/*
 * $Id: 094d68b7713b43ff69fc0512b24782c14b80eedd $
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

import java.io.ByteArrayInputStream
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509CRL
import java.security.cert.X509Certificate
import java.util.ArrayList
import java.util.Calendar
import java.util.Date

import org.bouncycastle.cert.ocsp.BasicOCSPResp
import org.bouncycastle.cert.ocsp.OCSPException
import org.bouncycastle.cert.ocsp.OCSPResp

import com.itextpdf.text.log.Logger
import com.itextpdf.text.log.LoggerFactory
import com.itextpdf.text.pdf.AcroFields
import com.itextpdf.text.pdf.PRStream
import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfDictionary
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.security.LtvVerification.CertificateOption

/**
 * Verifies the signatures in an LTV document.
 */
class LtvVerifier
/**
 * Creates a VerificationData object for a PdfReader
 * @param reader    a reader for the document we want to verify.
 * *
 * @throws GeneralSecurityException
 */
@Throws(GeneralSecurityException::class)
constructor(
        /** A reader object for the revision that is being verified.  */
        protected var reader: PdfReader) : RootStoreVerifier(null) {

    /** Do we need to check all certificate, or only the signing certificate?  */
    protected var option = CertificateOption.SIGNING_CERTIFICATE
    /** Verify root.  */
    protected var verifyRootCertificate = true
    /** The fields in the revision that is being verified.  */
    protected var fields: AcroFields
    /** The date the revision was signed, or `null` for the highest revision.  */
    protected var signDate: Date
    /** The signature that covers the revision.  */
    protected var signatureName: String
    /** The PdfPKCS7 object for the signature.  */
    protected var pkcs7: PdfPKCS7? = null
    /** Indicates if we're working with the latest revision.  */
    protected var latestRevision = true
    /** The document security store for the revision that is being verified  */
    protected var dss: PdfDictionary? = null

    init {
        this.fields = reader.acroFields
        val names = fields.signatureNames
        signatureName = names[names.size - 1]
        this.signDate = Date()
        pkcs7 = coversWholeDocument()
        LOGGER.info(String.format("Checking %ssignature %s", if (pkcs7!!.isTsp) "document-level timestamp " else "", signatureName))
    }

    /**
     * Sets an extra verifier.
     * @param verifier the verifier to set
     */
    fun setVerifier(verifier: CertificateVerifier) {
        this.verifier = verifier
    }

    /**
     * Sets the certificate option.
     * @param    option    Either CertificateOption.SIGNING_CERTIFICATE (default) or CertificateOption.WHOLE_CHAIN
     */
    fun setCertificateOption(option: CertificateOption) {
        this.option = option
    }

    /**
     * Set the verifyRootCertificate to false if you can't verify the root certificate.
     */
    fun setVerifyRootCertificate(verifyRootCertificate: Boolean) {
        this.verifyRootCertificate = verifyRootCertificate
    }

    /**
     * Checks if the signature covers the whole document
     * and throws an exception if the document was altered
     * @return a PdfPKCS7 object
     * *
     * @throws GeneralSecurityException
     */
    @Throws(GeneralSecurityException::class)
    protected fun coversWholeDocument(): PdfPKCS7 {
        val pkcs7 = fields.verifySignature(signatureName)
        if (fields.signatureCoversWholeDocument(signatureName)) {
            LOGGER.info("The timestamp covers whole document.")
        } else {
            throw VerificationException(null, "Signature doesn't cover whole document.")
        }
        if (pkcs7.verify()) {
            LOGGER.info("The signed document has not been modified.")
            return pkcs7
        } else {
            throw VerificationException(null, "The document was altered after the final signature was applied.")
        }
    }

    /**
     * Verifies all the document-level timestamps and all the signatures in the document.
     * @throws IOException
     * *
     * @throws GeneralSecurityException
     */
    @Throws(IOException::class, GeneralSecurityException::class)
    fun verify(result: MutableList<VerificationOK>?): List<VerificationOK> {
        var result = result
        if (result == null)
            result = ArrayList<VerificationOK>()
        while (pkcs7 != null) {
            result.addAll(verifySignature())
        }
        return result
    }

    /**
     * Verifies a document level timestamp.
     * @throws GeneralSecurityException
     * *
     * @throws IOException
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun verifySignature(): List<VerificationOK> {
        LOGGER.info("Verifying signature.")
        val result = ArrayList<VerificationOK>()
        // Get the certificate chain
        val chain = pkcs7!!.signCertificateChain
        verifyChain(chain)
        // how many certificates in the chain do we need to check?
        var total = 1
        if (CertificateOption.WHOLE_CHAIN == option) {
            total = chain.size
        }
        // loop over the certificates
        var signCert: X509Certificate
        var issuerCert: X509Certificate?
        var i = 0
        while (i < total) {
            // the certificate to check
            signCert = chain[i++] as X509Certificate
            // its issuer
            issuerCert = null
            if (i < chain.size)
                issuerCert = chain[i] as X509Certificate
            // now lets verify the certificate
            LOGGER.info(signCert.subjectDN.name)
            val list = verify(signCert, issuerCert, signDate)
            if (list.size == 0) {
                try {
                    signCert.verify(signCert.publicKey)
                    if (latestRevision && chain.size > 1) {
                        list.add(VerificationOK(signCert, this.javaClass, "Root certificate in final revision"))
                    }
                    if (list.size == 0 && verifyRootCertificate) {
                        throw GeneralSecurityException()
                    } else if (chain.size > 1)
                        list.add(VerificationOK(signCert, this.javaClass, "Root certificate passed without checking"))
                } catch (e: GeneralSecurityException) {
                    throw VerificationException(signCert, "Couldn't verify with CRL or OCSP or trusted anchor")
                }

            }
            result.addAll(list)
        }
        // go to the previous revision
        switchToPreviousRevision()
        return result
    }

    /**
     * Checks the certificates in a certificate chain:
     * are they valid on a specific date, and
     * do they chain up correctly?
     * @param chain
     * *
     * @throws GeneralSecurityException
     */
    @Throws(GeneralSecurityException::class)
    fun verifyChain(chain: Array<Certificate>) {
        // Loop over the certificates in the chain
        for (i in chain.indices) {
            val cert = chain[i] as X509Certificate
            // check if the certificate was/is valid
            cert.checkValidity(signDate)
            // check if the previous certificate was issued by this certificate
            if (i > 0)
                chain[i - 1].verify(chain[i].publicKey)
        }
        LOGGER.info("All certificates are valid on " + signDate.toString())
    }

    /**
     * Verifies certificates against a list of CRLs and OCSP responses.
     * @param signingCert
     * *
     * @param issuerCert
     * *
     * @return a list of `VerificationOK` objects.
     * * The list will be empty if the certificate couldn't be verified.
     * *
     * @throws GeneralSecurityException
     * *
     * @throws IOException
     * *
     * @see com.itextpdf.text.pdf.security.RootStoreVerifier.verify
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    override fun verify(signCert: X509Certificate, issuerCert: X509Certificate?, signDate: Date?): MutableList<VerificationOK> {
        // we'll verify agains the rootstore (if present)
        val rootStoreVerifier = RootStoreVerifier(verifier)
        rootStoreVerifier.setRootStore(rootStore)
        // We'll verify against a list of CRLs
        val crlVerifier = CRLVerifier(rootStoreVerifier, crLsFromDSS)
        crlVerifier.setRootStore(rootStore)
        crlVerifier.setOnlineCheckingAllowed(latestRevision || onlineCheckingAllowed)
        // We'll verify against a list of OCSPs
        val ocspVerifier = OCSPVerifier(crlVerifier, ocspResponsesFromDSS)
        ocspVerifier.setRootStore(rootStore)
        ocspVerifier.setOnlineCheckingAllowed(latestRevision || onlineCheckingAllowed)
        // We verify the chain
        return ocspVerifier.verify(signCert, issuerCert, signDate)
    }

    /**
     * Switches to the previous revision.
     * @throws IOException
     * *
     * @throws GeneralSecurityException
     */
    @Throws(IOException::class, GeneralSecurityException::class)
    fun switchToPreviousRevision() {
        LOGGER.info("Switching to previous revision.")
        latestRevision = false
        dss = reader.catalog.getAsDict(PdfName.DSS)
        var cal = pkcs7!!.timeStampDate
        if (cal == null)
            cal = pkcs7!!.signDate
        // TODO: get date from signature
        signDate = cal!!.time
        var names: List<String> = fields.signatureNames
        if (names.size > 1) {
            signatureName = names[names.size - 2]
            reader = PdfReader(fields.extractRevision(signatureName))
            this.fields = reader.acroFields
            names = fields.signatureNames
            signatureName = names[names.size - 1]
            pkcs7 = coversWholeDocument()
            LOGGER.info(String.format("Checking %ssignature %s", if (pkcs7!!.isTsp) "document-level timestamp " else "", signatureName))
        } else {
            LOGGER.info("No signatures in revision")
            pkcs7 = null
        }
    }

    /**
     * Gets a list of X509CRL objects from a Document Security Store.
     * @return    a list of CRLs
     * *
     * @throws GeneralSecurityException
     * *
     * @throws IOException
     */
    val crLsFromDSS: List<X509CRL>
        @Throws(GeneralSecurityException::class, IOException::class)
        get() {
            val crls = ArrayList<X509CRL>()
            if (dss == null)
                return crls
            val crlarray = dss!!.getAsArray(PdfName.CRLS) ?: return crls
            val cf = CertificateFactory.getInstance("X.509")
            for (i in 0..crlarray.size() - 1) {
                val stream = crlarray.getAsStream(i) as PRStream
                val crl = cf.generateCRL(ByteArrayInputStream(PdfReader.getStreamBytes(stream))) as X509CRL
                crls.add(crl)
            }
            return crls
        }

    /**
     * Gets OCSP responses from the Document Security Store.
     * @return    a list of BasicOCSPResp objects
     * *
     * @throws IOException
     * *
     * @throws GeneralSecurityException
     */
    val ocspResponsesFromDSS: List<BasicOCSPResp>
        @Throws(IOException::class, GeneralSecurityException::class)
        get() {
            val ocsps = ArrayList<BasicOCSPResp>()
            if (dss == null)
                return ocsps
            val ocsparray = dss!!.getAsArray(PdfName.OCSPS) ?: return ocsps
            for (i in 0..ocsparray.size() - 1) {
                val stream = ocsparray.getAsStream(i) as PRStream
                val ocspResponse = OCSPResp(PdfReader.getStreamBytes(stream))
                if (ocspResponse.status == 0)
                    try {
                        ocsps.add(ocspResponse.responseObject as BasicOCSPResp)
                    } catch (e: OCSPException) {
                        throw GeneralSecurityException(e)
                    }

            }
            return ocsps
        }

    companion object {
        /** The Logger instance  */
        protected val LOGGER = LoggerFactory.getLogger(LtvVerifier::class.java)
    }
}
