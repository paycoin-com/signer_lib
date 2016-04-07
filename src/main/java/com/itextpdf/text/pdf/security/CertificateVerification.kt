/*
 * $Id: fd966092c3dd6cdc1ca8d12953cc0b91e40423d7 $
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

import java.security.KeyStore
import java.security.cert.CRL
import java.security.cert.Certificate
import java.security.cert.CertificateParsingException
import java.security.cert.X509Certificate
import java.util.ArrayList
import java.util.Calendar
import java.util.Enumeration
import java.util.GregorianCalendar

import org.bouncycastle.cert.ocsp.BasicOCSPResp
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder
import org.bouncycastle.tsp.TimeStampToken

/**
 * This class consists of some methods that allow you to verify certificates.
 */
object CertificateVerification {

    /**
     * Verifies a single certificate.
     * @param cert the certificate to verify
     * *
     * @param crls the certificate revocation list or null
     * *
     * @param calendar the date or null for the current date
     * *
     * @return a String with the error description or null
     * * if no error
     */
    fun verifyCertificate(cert: X509Certificate, crls: Collection<CRL>?, calendar: Calendar?): String? {
        var calendar = calendar
        if (calendar == null)
            calendar = GregorianCalendar()
        if (cert.hasUnsupportedCriticalExtension()) {
            for (oid in cert.criticalExtensionOIDs) {
                // KEY USAGE and DIGITAL SIGNING is ALLOWED
                if ("2.5.29.15" == oid && cert.keyUsage[0]) {
                    continue
                }
                try {
                    // EXTENDED KEY USAGE and TIMESTAMPING is ALLOWED
                    if ("2.5.29.37" == oid && cert.extendedKeyUsage.contains("1.3.6.1.5.5.7.3.8")) {
                        continue
                    }
                } catch (e: CertificateParsingException) {
                    // DO NOTHING;
                }

                return "Has unsupported critical extension"
            }
        }
        try {
            cert.checkValidity(calendar.time)
        } catch (e: Exception) {
            return e.message
        }

        if (crls != null) {
            for (crl in crls) {
                if (crl.isRevoked(cert))
                    return "Certificate revoked"
            }
        }
        return null
    }

    /**
     * Verifies a certificate chain against a KeyStore.
     * @param certs the certificate chain
     * *
     * @param keystore the KeyStore
     * *
     * @param crls the certificate revocation list or null
     * *
     * @param calendar the date or null for the current date
     * *
     * @return null if the certificate chain could be validated or a
     * * Object[]{cert,error} where cert is the
     * * failed certificate and error is the error message
     */
    fun verifyCertificates(certs: Array<Certificate>, keystore: KeyStore, crls: Collection<CRL>?, calendar: Calendar?): List<VerificationException> {
        var calendar = calendar
        val result = ArrayList<VerificationException>()
        if (calendar == null)
            calendar = GregorianCalendar()
        for (k in certs.indices) {
            val cert = certs[k] as X509Certificate
            val err = verifyCertificate(cert, crls, calendar)
            if (err != null)
                result.add(VerificationException(cert, err))
            try {
                val aliases = keystore.aliases()
                while (aliases.hasMoreElements()) {
                    try {
                        val alias = aliases.nextElement()
                        if (!keystore.isCertificateEntry(alias))
                            continue
                        val certStoreX509 = keystore.getCertificate(alias) as X509Certificate
                        if (verifyCertificate(certStoreX509, crls, calendar) != null)
                            continue
                        try {
                            cert.verify(certStoreX509.publicKey)
                            return result
                        } catch (e: Exception) {
                            continue
                        }

                    } catch (ex: Exception) {
                    }

                }
            } catch (e: Exception) {
            }

            var j: Int
            j = 0
            while (j < certs.size) {
                if (j == k) {
                    ++j
                    continue
                }
                val certNext = certs[j] as X509Certificate
                try {
                    cert.verify(certNext.publicKey)
                    break
                } catch (e: Exception) {
                }

                ++j
            }
            if (j == certs.size) {
                result.add(VerificationException(cert, "Cannot be verified against the KeyStore or the certificate chain"))
            }
        }
        if (result.size == 0)
            result.add(VerificationException(null, "Invalid state. Possible circular certificate chain"))
        return result
    }

    /**
     * Verifies a certificate chain against a KeyStore.
     * @param certs the certificate chain
     * *
     * @param keystore the KeyStore
     * *
     * @param calendar the date or null for the current date
     * *
     * @return null if the certificate chain could be validated or a
     * * Object[]{cert,error} where cert is the
     * * failed certificate and error is the error message
     */
    fun verifyCertificates(certs: Array<Certificate>, keystore: KeyStore, calendar: Calendar): List<VerificationException> {
        return verifyCertificates(certs, keystore, null, calendar)
    }

    /**
     * Verifies an OCSP response against a KeyStore.
     * @param ocsp the OCSP response
     * *
     * @param keystore the KeyStore
     * *
     * @param provider the provider or null to use the BouncyCastle provider
     * *
     * @return true is a certificate was found
     */
    fun verifyOcspCertificates(ocsp: BasicOCSPResp, keystore: KeyStore, provider: String?): Boolean {
        var provider = provider
        if (provider == null)
            provider = "BC"
        try {
            val aliases = keystore.aliases()
            while (aliases.hasMoreElements()) {
                try {
                    val alias = aliases.nextElement()
                    if (!keystore.isCertificateEntry(alias))
                        continue
                    val certStoreX509 = keystore.getCertificate(alias) as X509Certificate
                    if (ocsp.isSignatureValid(JcaContentVerifierProviderBuilder().setProvider(provider).build(certStoreX509.publicKey)))
                        return true
                } catch (ex: Exception) {
                }

            }
        } catch (e: Exception) {
        }

        return false
    }

    /**
     * Verifies a time stamp against a KeyStore.
     * @param ts the time stamp
     * *
     * @param keystore the KeyStore
     * *
     * @param provider the provider or null to use the BouncyCastle provider
     * *
     * @return true is a certificate was found
     */
    fun verifyTimestampCertificates(ts: TimeStampToken, keystore: KeyStore, provider: String?): Boolean {
        var provider = provider
        if (provider == null)
            provider = "BC"
        try {
            val aliases = keystore.aliases()
            while (aliases.hasMoreElements()) {
                try {
                    val alias = aliases.nextElement()
                    if (!keystore.isCertificateEntry(alias))
                        continue
                    val certStoreX509 = keystore.getCertificate(alias) as X509Certificate
                    ts.isSignatureValid(JcaSimpleSignerInfoVerifierBuilder().setProvider(provider).build(certStoreX509))
                    return true
                } catch (ex: Exception) {
                }

            }
        } catch (e: Exception) {
        }

        return false
    }

}
