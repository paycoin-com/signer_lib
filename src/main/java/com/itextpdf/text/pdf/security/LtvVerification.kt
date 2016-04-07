/*
 * $Id: 2254fbccd3483230661fdd224708a119843bd6d8 $
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
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Enumerated
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers

import com.itextpdf.text.Utilities
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.log.Logger
import com.itextpdf.text.log.LoggerFactory
import com.itextpdf.text.pdf.AcroFields
import com.itextpdf.text.pdf.PRIndirectReference
import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfDeveloperExtension
import com.itextpdf.text.pdf.PdfDictionary
import com.itextpdf.text.pdf.PdfIndirectReference
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfStream
import com.itextpdf.text.pdf.PdfString
import com.itextpdf.text.pdf.PdfWriter

/**
 * Add verification according to PAdES-LTV (part 4)
 * @author Paulo Soares
 */
class LtvVerification
/**
 * The verification constructor. This class should only be created with
 * PdfStamper.getLtvVerification() otherwise the information will not be
 * added to the Pdf.
 * @param stp the PdfStamper to apply the validation to
 */
(private val stp: PdfStamper) {

    private val LOGGER = LoggerFactory.getLogger(LtvVerification::class.java)
    private val writer: PdfWriter
    private val reader: PdfReader
    private val acroFields: AcroFields
    private val validated = HashMap<PdfName, ValidationData>()
    private var used = false

    /**
     * What type of verification to include
     */
    enum class Level {
        /**
         * Include only OCSP
         */
        OCSP,
        /**
         * Include only CRL
         */
        CRL,
        /**
         * Include both OCSP and CRL
         */
        OCSP_CRL,
        /**
         * Include CRL only if OCSP can't be read
         */
        OCSP_OPTIONAL_CRL
    }

    /**
     * Options for how many certificates to include
     */
    enum class CertificateOption {
        /**
         * Include verification just for the signing certificate
         */
        SIGNING_CERTIFICATE,
        /**
         * Include verification for the whole chain of certificates
         */
        WHOLE_CHAIN
    }

    /**
     * Certificate inclusion in the DSS and VRI dictionaries in the CERT and CERTS
     * keys
     */
    enum class CertificateInclusion {
        /**
         * Include certificates in the DSS and VRI dictionaries
         */
        YES,
        /**
         * Do not include certificates in the DSS and VRI dictionaries
         */
        NO
    }

    init {
        writer = stp.writer
        reader = stp.reader
        acroFields = stp.acroFields
    }

    /**
     * Add verification for a particular signature
     * @param signatureName the signature to validate (it may be a timestamp)
     * *
     * @param ocsp the interface to get the OCSP
     * *
     * @param crl the interface to get the CRL
     * *
     * @param certOption
     * *
     * @param level the validation options to include
     * *
     * @param certInclude
     * *
     * @return true if a validation was generated, false otherwise
     * *
     * @throws GeneralSecurityException
     * *
     * @throws IOException
     */
    @Throws(IOException::class, GeneralSecurityException::class)
    fun addVerification(signatureName: String, ocsp: OcspClient?, crl: CrlClient?, certOption: CertificateOption, level: Level, certInclude: CertificateInclusion): Boolean {
        if (used)
            throw IllegalStateException(MessageLocalization.getComposedMessage("verification.already.output"))
        val pk = acroFields.verifySignature(signatureName)
        LOGGER.info("Adding verification for " + signatureName)
        val xc = pk.certificates
        var cert: X509Certificate
        val signingCert = pk.signingCertificate
        val vd = ValidationData()
        for (k in xc.indices) {
            cert = xc[k] as X509Certificate
            LOGGER.info("Certificate: " + cert.subjectDN)
            if (certOption == CertificateOption.SIGNING_CERTIFICATE && cert != signingCert) {
                continue
            }
            var ocspEnc: ByteArray? = null
            if (ocsp != null && level != Level.CRL) {
                ocspEnc = ocsp.getEncoded(cert, getParent(cert, xc), null)
                if (ocspEnc != null) {
                    vd.ocsps.add(buildOCSPResponse(ocspEnc))
                    LOGGER.info("OCSP added")
                }
            }
            if (crl != null && (level == Level.CRL || level == Level.OCSP_CRL || level == Level.OCSP_OPTIONAL_CRL && ocspEnc == null)) {
                val cims = crl.getEncoded(cert, null)
                if (cims != null) {
                    for (cim in cims) {
                        var dup = false
                        for (b in vd.crls) {
                            if (Arrays.equals(b, cim)) {
                                dup = true
                                break
                            }
                        }
                        if (!dup) {
                            vd.crls.add(cim)
                            LOGGER.info("CRL added")
                        }
                    }
                }
            }
            if (certInclude == CertificateInclusion.YES) {
                vd.certs.add(cert.encoded)
            }
        }
        if (vd.crls.isEmpty() && vd.ocsps.isEmpty())
            return false
        validated.put(getSignatureHashKey(signatureName), vd)
        return true
    }

    /**
     * Returns the issuing certificate for a child certificate.
     * @param cert    the certificate for which we search the parent
     * *
     * @param certs    an array with certificates that contains the parent
     * *
     * @return    the partent certificate
     */
    private fun getParent(cert: X509Certificate, certs: Array<Certificate>): X509Certificate? {
        var parent: X509Certificate
        for (i in certs.indices) {
            parent = certs[i] as X509Certificate
            if (cert.issuerDN != parent.subjectDN)
                continue
            try {
                cert.verify(parent.publicKey)
                return parent
            } catch (e: Exception) {
                // do nothing
            }

        }
        return null
    }

    /**

     * Alternative addVerification.
     * I assume that inputs are deduplicated.

     * @throws IOException
     * *
     * @throws GeneralSecurityException
     */
    @Throws(IOException::class, GeneralSecurityException::class)
    fun addVerification(signatureName: String, ocsps: Collection<ByteArray>?, crls: Collection<ByteArray>?, certs: Collection<ByteArray>?): Boolean {
        if (used)
            throw IllegalStateException(MessageLocalization.getComposedMessage("verification.already.output"))
        val vd = ValidationData()
        if (ocsps != null) {
            for (ocsp in ocsps) {
                vd.ocsps.add(buildOCSPResponse(ocsp))
            }
        }
        if (crls != null) {
            for (crl in crls) {
                vd.crls.add(crl)
            }
        }
        if (certs != null) {
            for (cert in certs) {
                vd.certs.add(cert)
            }
        }
        validated.put(getSignatureHashKey(signatureName), vd)
        return true
    }

    @Throws(IOException::class)
    private fun buildOCSPResponse(BasicOCSPResponse: ByteArray): ByteArray {
        val doctet = DEROctetString(BasicOCSPResponse)
        val v2 = ASN1EncodableVector()
        v2.add(OCSPObjectIdentifiers.id_pkix_ocsp_basic)
        v2.add(doctet)
        val den = ASN1Enumerated(0)
        val v3 = ASN1EncodableVector()
        v3.add(den)
        v3.add(DERTaggedObject(true, 0, DERSequence(v2)))
        val seq = DERSequence(v3)
        return seq.encoded
    }

    @Throws(NoSuchAlgorithmException::class, IOException::class)
    private fun getSignatureHashKey(signatureName: String): PdfName {
        val dic = acroFields.getSignatureDictionary(signatureName)
        val contents = dic.getAsString(PdfName.CONTENTS)
        var bc = contents.originalBytes
        var bt: ByteArray? = null
        if (PdfName.ETSI_RFC3161 == PdfReader.getPdfObject(dic.get(PdfName.SUBFILTER))) {
            val din = ASN1InputStream(ByteArrayInputStream(bc))
            val pkcs = din.readObject()
            bc = pkcs.encoded
        }
        bt = hashBytesSha1(bc)
        return PdfName(Utilities.convertToHex(bt))
    }

    @Throws(NoSuchAlgorithmException::class)
    private fun hashBytesSha1(b: ByteArray): ByteArray {
        val sh = MessageDigest.getInstance("SHA1")
        return sh.digest(b)
    }

    /**
     * Merges the validation with any validation already in the document or creates
     * a new one.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun merge() {
        if (used || validated.isEmpty())
            return
        used = true
        val catalog = reader.catalog
        val dss = catalog.get(PdfName.DSS)
        if (dss == null)
            createDss()
        else
            updateDss()
    }

    @Throws(IOException::class)
    private fun updateDss() {
        val catalog = reader.catalog
        stp.markUsed(catalog)
        val dss = catalog.getAsDict(PdfName.DSS)
        var ocsps: PdfArray? = dss.getAsArray(PdfName.OCSPS)
        var crls: PdfArray? = dss.getAsArray(PdfName.CRLS)
        var certs: PdfArray? = dss.getAsArray(PdfName.CERTS)
        dss.remove(PdfName.OCSPS)
        dss.remove(PdfName.CRLS)
        dss.remove(PdfName.CERTS)
        val vrim = dss.getAsDict(PdfName.VRI)
        //delete old validations
        if (vrim != null) {
            for (n in vrim.keys) {
                if (validated.containsKey(n)) {
                    val vri = vrim.getAsDict(n)
                    if (vri != null) {
                        deleteOldReferences(ocsps, vri.getAsArray(PdfName.OCSP))
                        deleteOldReferences(crls, vri.getAsArray(PdfName.CRL))
                        deleteOldReferences(certs, vri.getAsArray(PdfName.CERT))
                    }
                }
            }
        }
        if (ocsps == null)
            ocsps = PdfArray()
        if (crls == null)
            crls = PdfArray()
        if (certs == null)
            certs = PdfArray()
        outputDss(dss, vrim, ocsps, crls, certs)
    }

    private fun deleteOldReferences(all: PdfArray?, toDelete: PdfArray?) {
        if (all == null || toDelete == null)
            return
        for (pi in toDelete) {
            if (!pi.isIndirect)
                continue
            val pir = pi as PRIndirectReference
            var k = 0
            while (k < all.size()) {
                val po = all.getPdfObject(k)
                if (!po.isIndirect) {
                    ++k
                    continue
                }
                val pod = po as PRIndirectReference
                if (pir.number == pod.number) {
                    all.remove(k)
                    --k
                }
                ++k
            }
        }
    }

    @Throws(IOException::class)
    private fun createDss() {
        outputDss(PdfDictionary(), PdfDictionary(), PdfArray(), PdfArray(), PdfArray())
    }

    @Throws(IOException::class)
    private fun outputDss(dss: PdfDictionary, vrim: PdfDictionary, ocsps: PdfArray, crls: PdfArray, certs: PdfArray) {
        writer.addDeveloperExtension(PdfDeveloperExtension.ESIC_1_7_EXTENSIONLEVEL5)
        val catalog = reader.catalog
        stp.markUsed(catalog)
        for (vkey in validated.keys) {
            val ocsp = PdfArray()
            val crl = PdfArray()
            val cert = PdfArray()
            val vri = PdfDictionary()
            for (b in validated[vkey].crls) {
                val ps = PdfStream(b)
                ps.flateCompress()
                val iref = writer.addToBody(ps, false).indirectReference
                crl.add(iref)
                crls.add(iref)
            }
            for (b in validated[vkey].ocsps) {
                val ps = PdfStream(b)
                ps.flateCompress()
                val iref = writer.addToBody(ps, false).indirectReference
                ocsp.add(iref)
                ocsps.add(iref)
            }
            for (b in validated[vkey].certs) {
                val ps = PdfStream(b)
                ps.flateCompress()
                val iref = writer.addToBody(ps, false).indirectReference
                cert.add(iref)
                certs.add(iref)
            }
            if (ocsp.size() > 0)
                vri.put(PdfName.OCSP, writer.addToBody(ocsp, false).indirectReference)
            if (crl.size() > 0)
                vri.put(PdfName.CRL, writer.addToBody(crl, false).indirectReference)
            if (cert.size() > 0)
                vri.put(PdfName.CERT, writer.addToBody(cert, false).indirectReference)
            vrim.put(vkey, writer.addToBody(vri, false).indirectReference)
        }
        dss.put(PdfName.VRI, writer.addToBody(vrim, false).indirectReference)
        if (ocsps.size() > 0)
            dss.put(PdfName.OCSPS, writer.addToBody(ocsps, false).indirectReference)
        if (crls.size() > 0)
            dss.put(PdfName.CRLS, writer.addToBody(crls, false).indirectReference)
        if (certs.size() > 0)
            dss.put(PdfName.CERTS, writer.addToBody(certs, false).indirectReference)
        catalog.put(PdfName.DSS, writer.addToBody(dss, false).indirectReference)
    }

    private class ValidationData {
        var crls: MutableList<ByteArray> = ArrayList()
        var ocsps: MutableList<ByteArray> = ArrayList()
        var certs: MutableList<ByteArray> = ArrayList()
    }
}
