/*
 * $Id: 02748dc5a7c9dd130ef2ba1f30d22ca39c531897 $
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
import java.io.InputStream
import java.net.URL
import java.security.cert.CRL
import java.security.cert.CRLException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.CertificateParsingException
import java.security.cert.X509Certificate

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.x509.CRLDistPoint
import org.bouncycastle.asn1.x509.DistributionPoint
import org.bouncycastle.asn1.x509.DistributionPointName
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames


/**
 * This class contains a series of static methods that
 * allow you to retrieve information from a Certificate.
 */
object CertificateUtil {

    // Certificate Revocation Lists

    /**
     * Gets a CRL from a certificate
     * @param certificate
     * *
     * @return    the CRL or null if there's no CRL available
     * *
     * @throws CertificateException
     * *
     * @throws CRLException
     * *
     * @throws IOException
     */
    @Throws(CertificateException::class, CRLException::class, IOException::class)
    fun getCRL(certificate: X509Certificate): CRL {
        return CertificateUtil.getCRL(CertificateUtil.getCRLURL(certificate))
    }

    /**
     * Gets the URL of the Certificate Revocation List for a Certificate
     * @param certificate    the Certificate
     * *
     * @return    the String where you can check if the certificate was revoked
     * *
     * @throws CertificateParsingException
     * *
     * @throws IOException
     */
    @Throws(CertificateParsingException::class)
    fun getCRLURL(certificate: X509Certificate): String? {
        val obj: ASN1Primitive?
        try {
            obj = getExtensionValue(certificate, Extension.cRLDistributionPoints.id)
        } catch (e: IOException) {
            obj = null
        }

        if (obj == null) {
            return null
        }
        val dist = CRLDistPoint.getInstance(obj)
        val dists = dist.distributionPoints
        for (p in dists) {
            val distributionPointName = p.distributionPoint
            if (DistributionPointName.FULL_NAME != distributionPointName.type) {
                continue
            }
            val generalNames = distributionPointName.name as GeneralNames
            val names = generalNames.names
            for (name in names) {
                if (name.tagNo != GeneralName.uniformResourceIdentifier) {
                    continue
                }
                val derStr = DERIA5String.getInstance(name.toASN1Primitive() as ASN1TaggedObject, false)
                return derStr.string
            }
        }
        return null
    }

    /**
     * Gets the CRL object using a CRL URL.
     * @param url    the URL where to get the CRL
     * *
     * @return    a CRL object
     * *
     * @throws IOException
     * *
     * @throws CertificateException
     * *
     * @throws CRLException
     */
    @Throws(IOException::class, CertificateException::class, CRLException::class)
    fun getCRL(url: String?): CRL? {
        if (url == null)
            return null
        val `is` = URL(url).openStream()
        val cf = CertificateFactory.getInstance("X.509")
        return cf.generateCRL(`is`)
    }

    // Online Certificate Status Protocol

    /**
     * Retrieves the OCSP URL from the given certificate.
     * @param certificate the certificate
     * *
     * @return the URL or null
     * *
     * @throws IOException
     */
    fun getOCSPURL(certificate: X509Certificate): String? {
        val obj: ASN1Primitive?
        try {
            obj = getExtensionValue(certificate, Extension.authorityInfoAccess.id)
            if (obj == null) {
                return null
            }
            val AccessDescriptions = obj as ASN1Sequence?
            for (i in 0..AccessDescriptions.size() - 1) {
                val AccessDescription = AccessDescriptions.getObjectAt(i) as ASN1Sequence
                if (AccessDescription.size() != 2) {
                    continue
                } else if (AccessDescription.getObjectAt(0) is ASN1ObjectIdentifier) {
                    val id = AccessDescription.getObjectAt(0) as ASN1ObjectIdentifier
                    if (SecurityIDs.ID_OCSP == id.id) {
                        val description = AccessDescription.getObjectAt(1) as ASN1Primitive
                        val AccessLocation = getStringFromGeneralName(description)
                        if (AccessLocation == null) {
                            return ""
                        } else {
                            return AccessLocation
                        }
                    }
                }
            }
        } catch (e: IOException) {
            return null
        }

        return null
    }

    // Time Stamp Authority

    /**
     * Gets the URL of the TSA if it's available on the certificate
     * @param certificate    a certificate
     * *
     * @return    a TSA URL
     * *
     * @throws IOException
     */
    fun getTSAURL(certificate: X509Certificate): String? {
        val der = certificate.getExtensionValue(SecurityIDs.ID_TSA) ?: return null
        var asn1obj: ASN1Primitive
        try {
            asn1obj = ASN1Primitive.fromByteArray(der)
            val octets = asn1obj as DEROctetString
            asn1obj = ASN1Primitive.fromByteArray(octets.octets)
            val asn1seq = ASN1Sequence.getInstance(asn1obj)
            return getStringFromGeneralName(asn1seq.getObjectAt(1).toASN1Primitive())
        } catch (e: IOException) {
            return null
        }

    }

    // helper methods

    /**
     * @param certificate    the certificate from which we need the ExtensionValue
     * *
     * @param oid the Object Identifier value for the extension.
     * *
     * @return    the extension value as an ASN1Primitive object
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun getExtensionValue(certificate: X509Certificate, oid: String): ASN1Primitive? {
        val bytes = certificate.getExtensionValue(oid) ?: return null
        var aIn = ASN1InputStream(ByteArrayInputStream(bytes))
        val octs = aIn.readObject() as ASN1OctetString
        aIn = ASN1InputStream(ByteArrayInputStream(octs.octets))
        return aIn.readObject()
    }

    /**
     * Gets a String from an ASN1Primitive
     * @param names    the ASN1Primitive
     * *
     * @return    a human-readable String
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun getStringFromGeneralName(names: ASN1Primitive): String? {
        val taggedObject = names as ASN1TaggedObject
        return String(ASN1OctetString.getInstance(taggedObject, false).octets, "ISO-8859-1")
    }

}
