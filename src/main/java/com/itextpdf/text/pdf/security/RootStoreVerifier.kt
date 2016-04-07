/*
 * $Id: 2b0c4cd54d7e78d27a4d2b2d4d75899de0c52665 $
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

import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.ArrayList
import java.util.Date
import java.util.Enumeration

import com.itextpdf.text.log.Logger
import com.itextpdf.text.log.LoggerFactory

/**
 * Verifies a certificate against a `KeyStore`
 * containing trusted anchors.
 */
open class RootStoreVerifier
/**
 * Creates a RootStoreVerifier in a chain of verifiers.

 * @param verifier
 * *            the next verifier in the chain
 */
(verifier: CertificateVerifier) : CertificateVerifier(verifier) {

    /** A key store against which certificates can be verified.  */
    protected var rootStore: KeyStore? = null

    /**
     * Sets the Key Store against which a certificate can be checked.

     * @param keyStore
     * *            a root store
     */
    fun setRootStore(keyStore: KeyStore) {
        this.rootStore = keyStore
    }

    /**
     * Verifies a single certificate against a key store (if present).

     * @param signCert
     * *            the certificate to verify
     * *
     * @param issuerCert
     * *            the issuer certificate
     * *
     * @param signDate
     * *            the date the certificate needs to be valid
     * *
     * @return a list of `VerificationOK` objects.
     * * The list will be empty if the certificate couldn't be verified.
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    override fun verify(signCert: X509Certificate, issuerCert: X509Certificate?,
                        signDate: Date?): List<VerificationOK> {
        LOGGER.info("Root store verification: " + signCert.subjectDN.name)
        // verify using the CertificateVerifier if root store is missing
        if (rootStore == null)
            return super.verify(signCert, issuerCert, signDate)
        try {
            val result = ArrayList<VerificationOK>()
            // loop over the trusted anchors in the root store
            val aliases = rootStore!!.aliases()
            while (aliases.hasMoreElements()) {
                val alias = aliases.nextElement()
                try {
                    if (!rootStore!!.isCertificateEntry(alias))
                        continue
                    val anchor = rootStore!!.getCertificate(alias) as X509Certificate
                    signCert.verify(anchor.publicKey)
                    LOGGER.info("Certificate verified against root store")
                    result.add(VerificationOK(signCert, this.javaClass, "Certificate verified against root store."))
                    result.addAll(super.verify(signCert, issuerCert, signDate))
                    return result
                } catch (e: GeneralSecurityException) {
                    continue
                }

            }
            result.addAll(super.verify(signCert, issuerCert, signDate))
            return result
        } catch (e: GeneralSecurityException) {
            return super.verify(signCert, issuerCert, signDate)
        }

    }

    companion object {

        /** The Logger instance  */
        protected val LOGGER = LoggerFactory.getLogger(RootStoreVerifier::class.java)
    }
}
