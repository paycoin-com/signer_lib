/*
 * $Id: edbd3988125f4102178feff4135ba739a0423468 $
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

import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.security.GeneralSecurityException
import java.security.Security
import java.security.cert.CertificateEncodingException
import java.security.cert.X509Certificate

import org.bouncycastle.asn1.ocsp.OCSPResponseStatus

import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.bouncycastle.cert.ocsp.*
import org.bouncycastle.operator.OperatorException
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder

import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.io.StreamUtil
import com.itextpdf.text.log.Level
import com.itextpdf.text.log.Logger
import com.itextpdf.text.log.LoggerFactory
import com.itextpdf.text.pdf.PdfEncryption

/**
 * OcspClient implementation using BouncyCastle.

 * @author Paulo Soarees
 */
class OcspClientBouncyCastle : OcspClient {

    private val verifier: OCSPVerifier?

    /**
     * Create default implemention of `OcspClient`.
     * Note, if you use this constructor, OCSP response will not be verified.
     */
    @Deprecated("")
    constructor() {
        verifier = null
    }

    /**
     * Create `OcspClient`
     * @param verifier will be used for response verification. {@see OCSPVerifier}.
     */
    constructor(verifier: OCSPVerifier) {
        this.verifier = verifier
    }

    /**
     * Gets OCSP response. If {@see OCSPVerifier} was setted, the response will be checked.
     */
    fun getBasicOCSPResp(checkCert: X509Certificate, rootCert: X509Certificate, url: String): BasicOCSPResp? {
        try {
            val ocspResponse = getOcspResponse(checkCert, rootCert, url) ?: return null
            if (ocspResponse.status != OCSPResponseStatus.SUCCESSFUL) {
                return null
            }
            val basicResponse = ocspResponse.responseObject as BasicOCSPResp
            verifier?.isValidResponse(basicResponse, rootCert)
            return basicResponse
        } catch (ex: Exception) {
            if (LOGGER.isLogging(Level.ERROR))
                LOGGER.error(ex.message)
        }

        return null
    }

    /**
     * Gets an encoded byte array with OCSP validation. The method should not throw an exception.

     * @param checkCert to certificate to check
     * *
     * @param rootCert  the parent certificate
     * *
     * @param url       to get the verification. It it's null it will be taken
     * *                  from the check cert or from other implementation specific source
     * *
     * @return a byte array with the validation or null if the validation could not be obtained
     */
    override fun getEncoded(checkCert: X509Certificate, rootCert: X509Certificate, url: String): ByteArray? {
        try {
            val basicResponse = getBasicOCSPResp(checkCert, rootCert, url)
            if (basicResponse != null) {
                val responses = basicResponse.responses
                if (responses.size == 1) {
                    val resp = responses[0]
                    val status = resp.certStatus
                    if (status === CertificateStatus.GOOD) {
                        return basicResponse.encoded
                    } else if (status is RevokedStatus) {
                        throw IOException(MessageLocalization.getComposedMessage("ocsp.status.is.revoked"))
                    } else {
                        throw IOException(MessageLocalization.getComposedMessage("ocsp.status.is.unknown"))
                    }
                }
            }
        } catch (ex: Exception) {
            if (LOGGER.isLogging(Level.ERROR))
                LOGGER.error(ex.message)
        }

        return null
    }

    @Throws(GeneralSecurityException::class, OCSPException::class, IOException::class, OperatorException::class)
    private fun getOcspResponse(checkCert: X509Certificate?, rootCert: X509Certificate?, url: String?): OCSPResp? {
        var url = url
        if (checkCert == null || rootCert == null)
            return null
        if (url == null) {
            url = CertificateUtil.getOCSPURL(checkCert)
        }
        if (url == null)
            return null
        LOGGER.info("Getting OCSP from " + url)
        val request = generateOCSPRequest(rootCert, checkCert.serialNumber)
        val array = request.encoded
        val urlt = URL(url)
        val con = urlt.openConnection() as HttpURLConnection
        con.setRequestProperty("Content-Type", "application/ocsp-request")
        con.setRequestProperty("Accept", "application/ocsp-response")
        con.doOutput = true
        val out = con.outputStream
        val dataOut = DataOutputStream(BufferedOutputStream(out))
        dataOut.write(array)
        dataOut.flush()
        dataOut.close()
        if (con.responseCode / 100 != 2) {
            throw IOException(MessageLocalization.getComposedMessage("invalid.http.response.1", con.responseCode))
        }
        //Get Response
        val `in` = con.content as InputStream
        return OCSPResp(StreamUtil.inputStreamToArray(`in`))
    }

    companion object {

        /**
         * The Logger instance
         */
        private val LOGGER = LoggerFactory.getLogger(OcspClientBouncyCastle::class.java)


        /**
         * Generates an OCSP request using BouncyCastle.

         * @param issuerCert   certificate of the issues
         * *
         * @param serialNumber serial number
         * *
         * @return an OCSP request
         * *
         * @throws OCSPException
         * *
         * @throws IOException
         */
        @Throws(OCSPException::class, IOException::class, OperatorException::class, CertificateEncodingException::class)
        private fun generateOCSPRequest(issuerCert: X509Certificate, serialNumber: BigInteger): OCSPReq {
            //Add provider BC
            Security.addProvider(org.bouncycastle.jce.provider.BouncyCastleProvider())

            // Generate the id for the certificate we are looking for
            val id = CertificateID(
                    JcaDigestCalculatorProviderBuilder().build().get(CertificateID.HASH_SHA1),
                    JcaX509CertificateHolder(issuerCert), serialNumber)

            // basic request generation with nonce
            val gen = OCSPReqBuilder()
            gen.addRequest(id)

            val ext = Extension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce, false, DEROctetString(DEROctetString(PdfEncryption.createDocumentId()).encoded))
            gen.setRequestExtensions(Extensions(arrayOf(ext)))
            return gen.build()
        }
    }
}
