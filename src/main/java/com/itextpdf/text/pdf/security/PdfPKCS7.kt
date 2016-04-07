/*
 * $Id: dfe1dc268d2b4204724ced987c3a9fb7b54b9133 $
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
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.GeneralSecurityException
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException
import java.security.cert.CRL
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509CRL
import java.security.cert.X509Certificate
import java.util.ArrayList
import java.util.Arrays
import java.util.Calendar
import java.util.Date
import java.util.Enumeration
import java.util.GregorianCalendar
import java.util.HashSet

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Encoding
import org.bouncycastle.asn1.ASN1Enumerated
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1OutputStream
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERSet
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.DERUTCTime
import org.bouncycastle.asn1.cms.Attribute
import org.bouncycastle.asn1.cms.AttributeTable
import org.bouncycastle.asn1.cms.ContentInfo
import org.bouncycastle.asn1.ess.ESSCertID
import org.bouncycastle.asn1.ess.ESSCertIDv2
import org.bouncycastle.asn1.ess.SigningCertificate
import org.bouncycastle.asn1.ess.SigningCertificateV2
import org.bouncycastle.asn1.ocsp.BasicOCSPResponse
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.tsp.MessageImprint
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.bouncycastle.cert.ocsp.BasicOCSPResp
import org.bouncycastle.cert.ocsp.CertificateID
import org.bouncycastle.cert.ocsp.SingleResp
import org.bouncycastle.jce.X509Principal
import org.bouncycastle.jce.provider.X509CertParser
import org.bouncycastle.operator.DigestCalculator
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import org.bouncycastle.tsp.TimeStampToken
import org.bouncycastle.tsp.TimeStampTokenInfo

import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard

/**
 * This class does all the processing related to signing
 * and verifying a PKCS#7 signature.
 */
class PdfPKCS7 {

    // Constructors for creating new signatures

    /**
     * Assembles all the elements needed to create a signature, except for the data.
     * @param privKey the private key
     * *
     * @param certChain the certificate chain
     * *
     * @param interfaceDigest the interface digest
     * *
     * @param hashAlgorithm the hash algorithm
     * *
     * @param provider the provider or `null` for the default provider
     * *
     * @param hasRSAdata true if the sub-filter is adbe.pkcs7.sha1
     * *
     * @throws InvalidKeyException on error
     * *
     * @throws NoSuchProviderException on error
     * *
     * @throws NoSuchAlgorithmException on error
     */
    @Throws(InvalidKeyException::class, NoSuchProviderException::class, NoSuchAlgorithmException::class)
    constructor(privKey: PrivateKey?, certChain: Array<Certificate>,
                hashAlgorithm: String, provider: String, interfaceDigest: ExternalDigest, hasRSAdata: Boolean) {
        this.provider = provider
        this.interfaceDigest = interfaceDigest
        // message digest
        digestAlgorithmOid = DigestAlgorithms.getAllowedDigests(hashAlgorithm)
        if (digestAlgorithmOid == null)
            throw NoSuchAlgorithmException(MessageLocalization.getComposedMessage("unknown.hash.algorithm.1", hashAlgorithm))

        // Copy the certificates
        signingCertificate = certChain[0] as X509Certificate
        certs = ArrayList<Certificate>()
        for (element in certChain) {
            certs!!.add(element)
        }


        // initialize and add the digest algorithms.
        digestalgos = HashSet<String>()
        digestalgos!!.add(digestAlgorithmOid)

        // find the signing algorithm (RSA or DSA)
        if (privKey != null) {
            digestEncryptionAlgorithmOid = privKey.algorithm
            if (digestEncryptionAlgorithmOid == "RSA") {
                digestEncryptionAlgorithmOid = SecurityIDs.ID_RSA
            } else if (digestEncryptionAlgorithmOid == "DSA") {
                digestEncryptionAlgorithmOid = SecurityIDs.ID_DSA
            } else {
                throw NoSuchAlgorithmException(MessageLocalization.getComposedMessage("unknown.key.algorithm.1", digestEncryptionAlgorithmOid))
            }
        }

        // initialize the RSA data
        if (hasRSAdata) {
            RSAdata = ByteArray(0)
            messageDigest = DigestAlgorithms.getMessageDigest(hashAlgorithm, provider)
        }

        // initialize the Signature object
        if (privKey != null) {
            sig = initSignature(privKey)
        }
    }

    // Constructors for validating existing signatures

    /**
     * Use this constructor if you want to verify a signature using the sub-filter adbe.x509.rsa_sha1.
     * @param contentsKey the /Contents key
     * *
     * @param certsKey the /Cert key
     * *
     * @param provider the provider or `null` for the default provider
     */
    @SuppressWarnings("unchecked")
    constructor(contentsKey: ByteArray, certsKey: ByteArray, provider: String?) {
        try {
            this.provider = provider
            val cr = X509CertParser()
            cr.engineInit(ByteArrayInputStream(certsKey))
            certs = cr.engineReadAll()
            signCerts = certs
            signingCertificate = certs!!.iterator().next() as X509Certificate
            crls = ArrayList<CRL>()

            val `in` = ASN1InputStream(ByteArrayInputStream(contentsKey))
            digest = (`in`.readObject() as ASN1OctetString).octets

            if (provider == null) {
                sig = Signature.getInstance("SHA1withRSA")
            } else {
                sig = Signature.getInstance("SHA1withRSA", provider)
            }

            sig!!.initVerify(signingCertificate!!.publicKey)

            // setting the oid to SHA1withRSA
            digestAlgorithmOid = "1.2.840.10040.4.3"
            digestEncryptionAlgorithmOid = "1.3.36.3.3.1.2"
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    /**
     * Use this constructor if you want to verify a signature.
     * @param contentsKey the /Contents key
     * *
     * @param filterSubtype the filtersubtype
     * *
     * @param provider the provider or `null` for the default provider
     */
    @SuppressWarnings("unchecked")
    constructor(contentsKey: ByteArray, filterSubtype: PdfName, provider: String) {
        this.filterSubtype = filterSubtype
        isTsp = PdfName.ETSI_RFC3161 == filterSubtype
        isCades = PdfName.ETSI_CADES_DETACHED == filterSubtype
        try {
            this.provider = provider
            val din = ASN1InputStream(ByteArrayInputStream(contentsKey))

            //
            // Basic checks to make sure it's a PKCS#7 SignedData Object
            //
            val pkcs: ASN1Primitive

            try {
                pkcs = din.readObject()
            } catch (e: IOException) {
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("can.t.decode.pkcs7signeddata.object"))
            }

            if (pkcs !is ASN1Sequence) {
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("not.a.valid.pkcs.7.object.not.a.sequence"))
            }
            val objId = pkcs.getObjectAt(0) as ASN1ObjectIdentifier
            if (objId.id != SecurityIDs.ID_PKCS7_SIGNED_DATA)
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("not.a.valid.pkcs.7.object.not.signed.data"))
            val content = (pkcs.getObjectAt(1) as ASN1TaggedObject).`object` as ASN1Sequence
            // the positions that we care are:
            //     0 - version
            //     1 - digestAlgorithms
            //     2 - possible ID_PKCS7_DATA
            //     (the certificates and crls are taken out by other means)
            //     last - signerInfos

            // the version
            version = (content.getObjectAt(0) as ASN1Integer).value.toInt()

            // the digestAlgorithms
            digestalgos = HashSet<String>()
            val e = (content.getObjectAt(1) as ASN1Set).objects
            while (e.hasMoreElements()) {
                val s = e.nextElement()
                val o = s.getObjectAt(0) as ASN1ObjectIdentifier
                digestalgos!!.add(o.id)
            }

            // the possible ID_PKCS7_DATA
            val rsaData = content.getObjectAt(2) as ASN1Sequence
            if (rsaData.size() > 1) {
                val rsaDataContent = (rsaData.getObjectAt(1) as ASN1TaggedObject).`object` as ASN1OctetString
                RSAdata = rsaDataContent.octets
            }

            var next = 3
            while (content.getObjectAt(next) is ASN1TaggedObject)
                ++next


            // the certificates
            /*
			This should work, but that's not always the case because of a bug in BouncyCastle:
*/
            val cr = X509CertParser()
            cr.engineInit(ByteArrayInputStream(contentsKey))
            certs = cr.engineReadAll()
            /*    
            The following workaround was provided by Alfonso Massa, but it doesn't always work either.

            ASN1Set certSet = null;
            ASN1Set crlSet = null;
            while (content.getObjectAt(next) instanceof ASN1TaggedObject) {
                ASN1TaggedObject tagged = (ASN1TaggedObject)content.getObjectAt(next);

                switch (tagged.getTagNo()) {
                case 0:
                    certSet = ASN1Set.getInstance(tagged, false);
                    break;
                case 1:
                    crlSet = ASN1Set.getInstance(tagged, false);
                    break;
                default:
                    throw new IllegalArgumentException("unknown tag value " + tagged.getTagNo());
                }
                ++next;
            }
            certs = new ArrayList<Certificate>(certSet.size());

            CertificateFactory certFact = CertificateFactory.getInstance("X.509", new BouncyCastleProvider());
            for (Enumeration en = certSet.getObjects(); en.hasMoreElements();) {
                ASN1Primitive obj = ((ASN1Encodable)en.nextElement()).toASN1Primitive();
                if (obj instanceof ASN1Sequence) {
    	            ByteArrayInputStream stream = new ByteArrayInputStream(obj.getEncoded());
    	            X509Certificate x509Certificate = (X509Certificate)certFact.generateCertificate(stream);
    	            stream.close();
    				certs.add(x509Certificate);
                }
            }
*/
            // the signerInfos
            val signerInfos = content.getObjectAt(next) as ASN1Set
            if (signerInfos.size() != 1)
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("this.pkcs.7.object.has.multiple.signerinfos.only.one.is.supported.at.this.time"))
            val signerInfo = signerInfos.getObjectAt(0) as ASN1Sequence
            // the positions that we care are
            //     0 - version
            //     1 - the signing certificate issuer and serial number
            //     2 - the digest algorithm
            //     3 or 4 - digestEncryptionAlgorithm
            //     4 or 5 - encryptedDigest
            signingInfoVersion = (signerInfo.getObjectAt(0) as ASN1Integer).value.toInt()
            // Get the signing certificate
            val issuerAndSerialNumber = signerInfo.getObjectAt(1) as ASN1Sequence
            val issuer = X509Principal(issuerAndSerialNumber.getObjectAt(0).toASN1Primitive().encoded)
            val serialNumber = (issuerAndSerialNumber.getObjectAt(1) as ASN1Integer).value
            for (element in certs!!) {
                val cert = element as X509Certificate
                if (cert.issuerDN == issuer && serialNumber == cert.serialNumber) {
                    signingCertificate = cert
                    break
                }
            }
            if (signingCertificate == null) {
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("can.t.find.signing.certificate.with.serial.1",
                        issuer.name + " / " + serialNumber.toString(16)))
            }
            signCertificateChain()
            digestAlgorithmOid = ((signerInfo.getObjectAt(2) as ASN1Sequence).getObjectAt(0) as ASN1ObjectIdentifier).id
            next = 3
            var foundCades = false
            if (signerInfo.getObjectAt(next) is ASN1TaggedObject) {
                val tagsig = signerInfo.getObjectAt(next) as ASN1TaggedObject
                val sseq = ASN1Set.getInstance(tagsig, false)
                sigAttr = sseq.encoded
                // maybe not necessary, but we use the following line as fallback:
                sigAttrDer = sseq.getEncoded(ASN1Encoding.DER)

                for (k in 0..sseq.size() - 1) {
                    val seq2 = sseq.getObjectAt(k) as ASN1Sequence
                    val idSeq2 = (seq2.getObjectAt(0) as ASN1ObjectIdentifier).id
                    if (idSeq2 == SecurityIDs.ID_MESSAGE_DIGEST) {
                        val set = seq2.getObjectAt(1) as ASN1Set
                        digestAttr = (set.getObjectAt(0) as ASN1OctetString).octets
                    } else if (idSeq2 == SecurityIDs.ID_ADBE_REVOCATION) {
                        val setout = seq2.getObjectAt(1) as ASN1Set
                        val seqout = setout.getObjectAt(0) as ASN1Sequence
                        for (j in 0..seqout.size() - 1) {
                            val tg = seqout.getObjectAt(j) as ASN1TaggedObject
                            if (tg.tagNo == 0) {
                                val seqin = tg.`object` as ASN1Sequence
                                findCRL(seqin)
                            }
                            if (tg.tagNo == 1) {
                                val seqin = tg.`object` as ASN1Sequence
                                findOcsp(seqin)
                            }
                        }
                    } else if (isCades && idSeq2 == SecurityIDs.ID_AA_SIGNING_CERTIFICATE_V1) {
                        val setout = seq2.getObjectAt(1) as ASN1Set
                        val seqout = setout.getObjectAt(0) as ASN1Sequence
                        val sv2 = SigningCertificate.getInstance(seqout)
                        val cerv2m = sv2.certs
                        val cerv2 = cerv2m[0]
                        val enc2 = signingCertificate!!.encoded
                        val m2 = BouncyCastleDigest().getMessageDigest("SHA-1")
                        val signCertHash = m2.digest(enc2)
                        val hs2 = cerv2.certHash
                        if (!Arrays.equals(signCertHash, hs2))
                            throw IllegalArgumentException("Signing certificate doesn't match the ESS information.")
                        foundCades = true
                    } else if (isCades && idSeq2 == SecurityIDs.ID_AA_SIGNING_CERTIFICATE_V2) {
                        val setout = seq2.getObjectAt(1) as ASN1Set
                        val seqout = setout.getObjectAt(0) as ASN1Sequence
                        val sv2 = SigningCertificateV2.getInstance(seqout)
                        val cerv2m = sv2.certs
                        val cerv2 = cerv2m[0]
                        val ai2 = cerv2.hashAlgorithm
                        val enc2 = signingCertificate!!.encoded
                        val m2 = BouncyCastleDigest().getMessageDigest(DigestAlgorithms.getDigest(ai2.algorithm.id))
                        val signCertHash = m2.digest(enc2)
                        val hs2 = cerv2.certHash
                        if (!Arrays.equals(signCertHash, hs2))
                            throw IllegalArgumentException("Signing certificate doesn't match the ESS information.")
                        foundCades = true
                    }
                }
                if (digestAttr == null)
                    throw IllegalArgumentException(MessageLocalization.getComposedMessage("authenticated.attribute.is.missing.the.digest"))
                ++next
            }
            if (isCades && !foundCades)
                throw IllegalArgumentException("CAdES ESS information missing.")
            digestEncryptionAlgorithmOid = ((signerInfo.getObjectAt(next++) as ASN1Sequence).getObjectAt(0) as ASN1ObjectIdentifier).id
            digest = (signerInfo.getObjectAt(next++) as ASN1OctetString).octets
            if (next < signerInfo.size() && signerInfo.getObjectAt(next) is ASN1TaggedObject) {
                val taggedObject = signerInfo.getObjectAt(next) as ASN1TaggedObject
                val unat = ASN1Set.getInstance(taggedObject, false)
                val attble = AttributeTable(unat)
                val ts = attble.get(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken)
                if (ts != null && ts.attrValues.size() > 0) {
                    val attributeValues = ts.attrValues
                    val tokenSequence = ASN1Sequence.getInstance(attributeValues.getObjectAt(0))
                    val contentInfo = ContentInfo(tokenSequence)
                    this.timeStampToken = TimeStampToken(contentInfo)
                }
            }
            if (isTsp) {
                val contentInfoTsp = ContentInfo(pkcs)
                this.timeStampToken = TimeStampToken(contentInfoTsp)
                val info = timeStampToken!!.timeStampInfo
                val algOID = info.messageImprintAlgOID.id
                messageDigest = DigestAlgorithms.getMessageDigestFromOid(algOID, null)
            } else {
                if (RSAdata != null || digestAttr != null) {
                    if (PdfName.ADBE_PKCS7_SHA1 == filterSubtype) {
                        messageDigest = DigestAlgorithms.getMessageDigest("SHA1", provider)
                    } else {
                        messageDigest = DigestAlgorithms.getMessageDigest(hashAlgorithm, provider)
                    }
                    encContDigest = DigestAlgorithms.getMessageDigest(hashAlgorithm, provider)
                }
                sig = initSignature(signingCertificate!!.publicKey)
            }
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    // Encryption provider

    /** The encryption provider, e.g. "BC" if you use BouncyCastle.  */
    private var provider: String? = null

    // Signature info

    /** Holds value of property signName.  */
    /**
     * Getter for property sigName.
     * @return Value of property sigName.
     */
    /**
     * Setter for property sigName.
     * @param signName New value of property sigName.
     */
    var signName: String? = null

    /** Holds value of property reason.  */
    /**
     * Getter for property reason.
     * @return Value of property reason.
     */
    /**
     * Setter for property reason.
     * @param reason New value of property reason.
     */
    var reason: String? = null

    /** Holds value of property location.  */
    /**
     * Getter for property location.
     * @return Value of property location.
     */
    /**
     * Setter for property location.
     * @param location New value of property location.
     */
    var location: String? = null

    /** Holds value of property signDate.  */
    /**
     * Getter for property signDate.
     * @return Value of property signDate.
     */
    /**
     * Setter for property signDate.
     * @param signDate New value of property signDate.
     */
    var signDate: Calendar? = null
        get() {
            val dt = timeStampDate
            if (dt == null)
                return this.signDate
            else
                return dt
        }

    // version info

    /** Version of the PKCS#7 object  */
    /**
     * Get the version of the PKCS#7 object.
     * @return the version of the PKCS#7 object.
     */
    var version = 1
        private set

    /** Version of the PKCS#7 "SignerInfo" object.  */
    /**
     * Get the version of the PKCS#7 "SignerInfo" object.
     * @return the version of the PKCS#7 "SignerInfo" object.
     */
    var signingInfoVersion = 1
        private set

    // Message digest algorithm

    /** The ID of the digest algorithm, e.g. "2.16.840.1.101.3.4.2.1".  */
    /**
     * Getter for the ID of the digest algorithm, e.g. "2.16.840.1.101.3.4.2.1"
     */
    var digestAlgorithmOid: String? = null
        private set

    /** The object that will create the digest  */
    private var messageDigest: MessageDigest? = null

    /** The digest algorithms  */
    private var digestalgos: MutableSet<String>? = null

    /** The digest attributes  */
    private var digestAttr: ByteArray? = null

    /**
     * Returns the filter subtype.
     */
    val filterSubtype: PdfName

    /**
     * Returns the name of the digest algorithm, e.g. "SHA256".
     * @return the digest algorithm name, e.g. "SHA256"
     */
    val hashAlgorithm: String
        get() = DigestAlgorithms.getDigest(digestAlgorithmOid)

    // Encryption algorithm

    /** The encryption algorithm.  */
    /**
     * Getter for the digest encryption algorithm
     */
    var digestEncryptionAlgorithmOid: String? = null
        private set

    /**
     * Get the algorithm used to calculate the message digest, e.g. "SHA1withRSA".
     * @return the algorithm used to calculate the message digest
     */
    val digestAlgorithm: String
        get() = hashAlgorithm + "with" + encryptionAlgorithm

    /*
     *	DIGITAL SIGNATURE CREATION
     */

    private val interfaceDigest: ExternalDigest
    // The signature is created externally

    /** The signed digest if created outside this class  */
    private var externalDigest: ByteArray? = null

    /** External RSA data  */
    private var externalRSAdata: ByteArray? = null

    /**
     * Sets the digest/signature to an external calculated value.
     * @param digest the digest. This is the actual signature
     * *
     * @param RSAdata the extra data that goes into the data tag in PKCS#7
     * *
     * @param digestEncryptionAlgorithm the encryption algorithm. It may must be null if the digest
     * * is also null. If the digest is not null
     * * then it may be "RSA" or "DSA"
     */
    fun setExternalDigest(digest: ByteArray, RSAdata: ByteArray, digestEncryptionAlgorithm: String?) {
        externalDigest = digest
        externalRSAdata = RSAdata
        if (digestEncryptionAlgorithm != null) {
            if (digestEncryptionAlgorithm == "RSA") {
                this.digestEncryptionAlgorithmOid = SecurityIDs.ID_RSA
            } else if (digestEncryptionAlgorithm == "DSA") {
                this.digestEncryptionAlgorithmOid = SecurityIDs.ID_DSA
            } else if (digestEncryptionAlgorithm == "ECDSA") {
                this.digestEncryptionAlgorithmOid = SecurityIDs.ID_ECDSA
            } else
                throw ExceptionConverter(NoSuchAlgorithmException(MessageLocalization.getComposedMessage("unknown.key.algorithm.1", digestEncryptionAlgorithm)))
        }
    }

    // The signature is created internally

    /** Class from the Java SDK that provides the functionality of a digital signature algorithm.  */
    private var sig: Signature? = null

    /** The signed digest as calculated by this class (or extracted from an existing PDF)  */
    private var digest: ByteArray? = null

    /** The RSA data  */
    private var RSAdata: ByteArray? = null

    // Signing functionality.

    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class, InvalidKeyException::class)
    private fun initSignature(key: PrivateKey): Signature {
        val signature: Signature
        if (provider == null)
            signature = Signature.getInstance(digestAlgorithm)
        else
            signature = Signature.getInstance(digestAlgorithm, provider)
        signature.initSign(key)
        return signature
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class, InvalidKeyException::class)
    private fun initSignature(key: PublicKey): Signature {
        var digestAlgorithm = digestAlgorithm
        if (PdfName.ADBE_X509_RSA_SHA1 == filterSubtype)
            digestAlgorithm = "SHA1withRSA"
        val signature: Signature
        if (provider == null)
            signature = Signature.getInstance(digestAlgorithm)
        else
            signature = Signature.getInstance(digestAlgorithm, provider)

        signature.initVerify(key)
        return signature
    }

    /**
     * Update the digest with the specified bytes.
     * This method is used both for signing and verifying
     * @param buf the data buffer
     * *
     * @param off the offset in the data buffer
     * *
     * @param len the data length
     * *
     * @throws SignatureException on error
     */
    @Throws(SignatureException::class)
    fun update(buf: ByteArray, off: Int, len: Int) {
        if (RSAdata != null || digestAttr != null || isTsp)
            messageDigest!!.update(buf, off, len)
        else
            sig!!.update(buf, off, len)
    }

    // adbe.x509.rsa_sha1 (PKCS#1)

    /**
     * Gets the bytes for the PKCS#1 object.
     * @return a byte array
     */
    val encodedPKCS1: ByteArray
        get() {
            try {
                if (externalDigest != null)
                    digest = externalDigest
                else
                    digest = sig!!.sign()
                val bOut = ByteArrayOutputStream()

                val dout = ASN1OutputStream(bOut)
                dout.writeObject(DEROctetString(digest))
                dout.close()

                return bOut.toByteArray()
            } catch (e: Exception) {
                throw ExceptionConverter(e)
            }

        }

    // other subfilters (PKCS#7)

    /**
     * Gets the bytes for the PKCS7SignedData object.
     * @return the bytes for the PKCS7SignedData object
     */
    val encodedPKCS7: ByteArray
        get() = getEncodedPKCS7(null, null, null, null, CryptoStandard.CMS)

    /**
     * Gets the bytes for the PKCS7SignedData object. Optionally the authenticatedAttributes
     * in the signerInfo can also be set, OR a time-stamp-authority client
     * may be provided.
     * @param secondDigest the digest in the authenticatedAttributes
     * *
     * @param tsaClient TSAClient - null or an optional time stamp authority client
     * *
     * @return byte[] the bytes for the PKCS7SignedData object
     * *
     * @since    2.1.6
     */
    @JvmOverloads fun getEncodedPKCS7(secondDigest: ByteArray?, tsaClient: TSAClient? = null, ocsp: ByteArray? = null, crlBytes: Collection<ByteArray>? = null, sigtype: CryptoStandard = CryptoStandard.CMS): ByteArray {
        try {
            if (externalDigest != null) {
                digest = externalDigest
                if (RSAdata != null)
                    RSAdata = externalRSAdata
            } else if (externalRSAdata != null && RSAdata != null) {
                RSAdata = externalRSAdata
                sig!!.update(RSAdata)
                digest = sig!!.sign()
            } else {
                if (RSAdata != null) {
                    RSAdata = messageDigest!!.digest()
                    sig!!.update(RSAdata)
                }
                digest = sig!!.sign()
            }

            // Create the set of Hash algorithms
            val digestAlgorithms = ASN1EncodableVector()
            for (element in digestalgos!!) {
                val algos = ASN1EncodableVector()
                algos.add(ASN1ObjectIdentifier(element))
                algos.add(DERNull.INSTANCE)
                digestAlgorithms.add(DERSequence(algos))
            }

            // Create the contentInfo.
            var v = ASN1EncodableVector()
            v.add(ASN1ObjectIdentifier(SecurityIDs.ID_PKCS7_DATA))
            if (RSAdata != null)
                v.add(DERTaggedObject(0, DEROctetString(RSAdata)))
            val contentinfo = DERSequence(v)

            // Get all the certificates
            //
            v = ASN1EncodableVector()
            for (element in certs!!) {
                val tempstream = ASN1InputStream(ByteArrayInputStream((element as X509Certificate).encoded))
                v.add(tempstream.readObject())
            }

            val dercertificates = DERSet(v)

            // Create signerinfo structure.
            //
            val signerinfo = ASN1EncodableVector()

            // Add the signerInfo version
            //
            signerinfo.add(ASN1Integer(signingInfoVersion.toLong()))

            v = ASN1EncodableVector()
            v.add(CertificateInfo.getIssuer(signingCertificate!!.tbsCertificate))
            v.add(ASN1Integer(signingCertificate!!.serialNumber))
            signerinfo.add(DERSequence(v))

            // Add the digestAlgorithm
            v = ASN1EncodableVector()
            v.add(ASN1ObjectIdentifier(digestAlgorithmOid))
            v.add(DERNull())
            signerinfo.add(DERSequence(v))

            // add the authenticated attribute if present
            if (secondDigest != null) {
                signerinfo.add(DERTaggedObject(false, 0, getAuthenticatedAttributeSet(secondDigest, ocsp, crlBytes, sigtype)))
            }
            // Add the digestEncryptionAlgorithm
            v = ASN1EncodableVector()
            v.add(ASN1ObjectIdentifier(digestEncryptionAlgorithmOid))
            v.add(DERNull())
            signerinfo.add(DERSequence(v))

            // Add the digest
            signerinfo.add(DEROctetString(digest))

            // When requested, go get and add the timestamp. May throw an exception.
            // Added by Martin Brunecky, 07/12/2007 folowing Aiken Sam, 2006-11-15
            // Sam found Adobe expects time-stamped SHA1-1 of the encrypted digest
            if (tsaClient != null) {
                val tsImprint = tsaClient.messageDigest.digest(digest)
                val tsToken = tsaClient.getTimeStampToken(tsImprint)
                if (tsToken != null) {
                    val unauthAttributes = buildUnauthenticatedAttributes(tsToken)
                    if (unauthAttributes != null) {
                        signerinfo.add(DERTaggedObject(false, 1, DERSet(unauthAttributes)))
                    }
                }
            }

            // Finally build the body out of all the components above
            val body = ASN1EncodableVector()
            body.add(ASN1Integer(version.toLong()))
            body.add(DERSet(digestAlgorithms))
            body.add(contentinfo)
            body.add(DERTaggedObject(false, 0, dercertificates))

            // Only allow one signerInfo
            body.add(DERSet(DERSequence(signerinfo)))

            // Now we have the body, wrap it in it's PKCS7Signed shell
            // and return it
            //
            val whole = ASN1EncodableVector()
            whole.add(ASN1ObjectIdentifier(SecurityIDs.ID_PKCS7_SIGNED_DATA))
            whole.add(DERTaggedObject(0, DERSequence(body)))

            val bOut = ByteArrayOutputStream()

            val dout = ASN1OutputStream(bOut)
            dout.writeObject(DERSequence(whole))
            dout.close()

            return bOut.toByteArray()
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    /**
     * Added by Aiken Sam, 2006-11-15, modifed by Martin Brunecky 07/12/2007
     * to start with the timeStampToken (signedData 1.2.840.113549.1.7.2).
     * Token is the TSA response without response status, which is usually
     * handled by the (vendor supplied) TSA request/response interface).
     * @param timeStampToken byte[] - time stamp token, DER encoded signedData
     * *
     * @return ASN1EncodableVector
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun buildUnauthenticatedAttributes(timeStampToken: ByteArray?): ASN1EncodableVector? {
        if (timeStampToken == null)
            return null

        // @todo: move this together with the rest of the defintions
        val ID_TIME_STAMP_TOKEN = "1.2.840.113549.1.9.16.2.14" // RFC 3161 id-aa-timeStampToken

        val tempstream = ASN1InputStream(ByteArrayInputStream(timeStampToken))
        val unauthAttributes = ASN1EncodableVector()

        val v = ASN1EncodableVector()
        v.add(ASN1ObjectIdentifier(ID_TIME_STAMP_TOKEN)) // id-aa-timeStampToken
        val seq = tempstream.readObject() as ASN1Sequence
        v.add(DERSet(seq))

        unauthAttributes.add(DERSequence(v))
        return unauthAttributes
    }

    // Authenticated attributes

    /**
     * When using authenticatedAttributes the authentication process is different.
     * The document digest is generated and put inside the attribute. The signing is done over the DER encoded
     * authenticatedAttributes. This method provides that encoding and the parameters must be
     * exactly the same as in [.getEncodedPKCS7].
     *
     *
     * A simple example:
     *
     *
     *
     * Calendar cal = Calendar.getInstance();
     * PdfPKCS7 pk7 = new PdfPKCS7(key, chain, null, "SHA1", null, false);
     * MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
     * byte buf[] = new byte[8192];
     * int n;
     * InputStream inp = sap.getRangeStream();
     * while ((n = inp.read(buf)) &gt; 0) {
     * messageDigest.update(buf, 0, n);
     * }
     * byte hash[] = messageDigest.digest();
     * byte sh[] = pk7.getAuthenticatedAttributeBytes(hash, cal);
     * pk7.update(sh, 0, sh.length);
     * byte sg[] = pk7.getEncodedPKCS7(hash, cal);
     *
     * @param secondDigest the content digest
     * *
     * @return the byte array representation of the authenticatedAttributes ready to be signed
     */
    fun getAuthenticatedAttributeBytes(secondDigest: ByteArray, ocsp: ByteArray, crlBytes: Collection<ByteArray>, sigtype: CryptoStandard): ByteArray {
        try {
            return getAuthenticatedAttributeSet(secondDigest, ocsp, crlBytes, sigtype).getEncoded(ASN1Encoding.DER)
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    /**
     * This method provides that encoding and the parameters must be
     * exactly the same as in [.getEncodedPKCS7].

     * @param secondDigest the content digest
     * *
     * @return the byte array representation of the authenticatedAttributes ready to be signed
     */
    private fun getAuthenticatedAttributeSet(secondDigest: ByteArray, ocsp: ByteArray?, crlBytes: Collection<ByteArray>?, sigtype: CryptoStandard): DERSet {
        try {
            val attribute = ASN1EncodableVector()
            var v = ASN1EncodableVector()
            v.add(ASN1ObjectIdentifier(SecurityIDs.ID_CONTENT_TYPE))
            v.add(DERSet(ASN1ObjectIdentifier(SecurityIDs.ID_PKCS7_DATA)))
            attribute.add(DERSequence(v))
            v = ASN1EncodableVector()
            v.add(ASN1ObjectIdentifier(SecurityIDs.ID_MESSAGE_DIGEST))
            v.add(DERSet(DEROctetString(secondDigest)))
            attribute.add(DERSequence(v))
            var haveCrl = false
            if (crlBytes != null) {
                for (bCrl in crlBytes) {
                    if (bCrl != null) {
                        haveCrl = true
                        break
                    }
                }
            }
            if (ocsp != null || haveCrl) {
                v = ASN1EncodableVector()
                v.add(ASN1ObjectIdentifier(SecurityIDs.ID_ADBE_REVOCATION))

                val revocationV = ASN1EncodableVector()

                if (haveCrl) {
                    val v2 = ASN1EncodableVector()
                    for (bCrl in crlBytes!!) {
                        if (bCrl == null)
                            continue
                        val t = ASN1InputStream(ByteArrayInputStream(bCrl))
                        v2.add(t.readObject())
                    }
                    revocationV.add(DERTaggedObject(true, 0, DERSequence(v2)))
                }

                if (ocsp != null) {
                    val doctet = DEROctetString(ocsp)
                    val vo1 = ASN1EncodableVector()
                    val v2 = ASN1EncodableVector()
                    v2.add(OCSPObjectIdentifiers.id_pkix_ocsp_basic)
                    v2.add(doctet)
                    val den = ASN1Enumerated(0)
                    val v3 = ASN1EncodableVector()
                    v3.add(den)
                    v3.add(DERTaggedObject(true, 0, DERSequence(v2)))
                    vo1.add(DERSequence(v3))
                    revocationV.add(DERTaggedObject(true, 1, DERSequence(vo1)))
                }

                v.add(DERSet(DERSequence(revocationV)))
                attribute.add(DERSequence(v))
            }
            if (sigtype == CryptoStandard.CADES) {
                v = ASN1EncodableVector()
                v.add(ASN1ObjectIdentifier(SecurityIDs.ID_AA_SIGNING_CERTIFICATE_V2))

                val aaV2 = ASN1EncodableVector()
                val sha256Oid = DigestAlgorithms.getAllowedDigests(DigestAlgorithms.SHA256)

                // If we look into X.690-0207, clause 11.5, we can see that using DER all the components of a sequence having
                // default values shall not be included. According to RFC 5035, 5.4.1.1, definition of ESSCertIDv2, default
                // AlgorithmIdentifier is sha256.
                if (sha256Oid != digestAlgorithmOid) {
                    val algoId = AlgorithmIdentifier(ASN1ObjectIdentifier(digestAlgorithmOid))
                    aaV2.add(algoId)
                }

                val md = interfaceDigest.getMessageDigest(hashAlgorithm)
                val dig = md.digest(signingCertificate!!.encoded)
                aaV2.add(DEROctetString(dig))

                v.add(DERSet(DERSequence(DERSequence(DERSequence(aaV2)))))
                attribute.add(DERSequence(v))
            }

            return DERSet(attribute)
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    /*
     *	DIGITAL SIGNATURE VERIFICATION
     */

    /** Signature attributes  */
    private var sigAttr: ByteArray? = null
    /** Signature attributes (maybe not necessary, but we use it as fallback)  */
    private var sigAttrDer: ByteArray? = null

    /** encrypted digest  */
    private var encContDigest: MessageDigest? = null // Stefan Santesson

    /** Indicates if a signature has already been verified  */
    private var verified: Boolean = false

    /** The result of the verification  */
    private var verifyResult: Boolean = false


    // verification

    /**
     * Verify the digest.
     * @throws SignatureException on error
     * *
     * @return true if the signature checks out, false otherwise
     * *
     * @throws java.security.GeneralSecurityException
     */
    @Throws(GeneralSecurityException::class)
    fun verify(): Boolean {
        if (verified)
            return verifyResult
        if (isTsp) {
            val info = timeStampToken!!.timeStampInfo
            val imprint = info.toASN1Structure().messageImprint
            val md = messageDigest!!.digest()
            val imphashed = imprint.hashedMessage
            verifyResult = Arrays.equals(md, imphashed)
        } else {
            if (sigAttr != null || sigAttrDer != null) {
                val msgDigestBytes = messageDigest!!.digest()
                var verifyRSAdata = true
                // Stefan Santesson fixed a bug, keeping the code backward compatible
                var encContDigestCompare = false
                if (RSAdata != null) {
                    verifyRSAdata = Arrays.equals(msgDigestBytes, RSAdata)
                    encContDigest!!.update(RSAdata)
                    encContDigestCompare = Arrays.equals(encContDigest!!.digest(), digestAttr)
                }
                val absentEncContDigestCompare = Arrays.equals(msgDigestBytes, digestAttr)
                val concludingDigestCompare = absentEncContDigestCompare || encContDigestCompare
                val sigVerify = verifySigAttributes(sigAttr) || verifySigAttributes(sigAttrDer)
                verifyResult = concludingDigestCompare && sigVerify && verifyRSAdata
            } else {
                if (RSAdata != null)
                    sig!!.update(messageDigest!!.digest())
                verifyResult = sig!!.verify(digest)
            }
        }
        verified = true
        return verifyResult
    }

    @Throws(GeneralSecurityException::class)
    private fun verifySigAttributes(attr: ByteArray): Boolean {
        val signature = initSignature(signingCertificate!!.publicKey)
        signature.update(attr)
        return signature.verify(digest)
    }

    /**
     * Checks if the timestamp refers to this document.
     * @return true if it checks false otherwise
     * *
     * @throws GeneralSecurityException on error
     * *
     * @since    2.1.6
     */
    @Throws(GeneralSecurityException::class)
    fun verifyTimestampImprint(): Boolean {
        if (timeStampToken == null)
            return false
        val info = timeStampToken!!.timeStampInfo
        val imprint = info.toASN1Structure().messageImprint
        val algOID = info.messageImprintAlgOID.id
        val md = BouncyCastleDigest().getMessageDigest(DigestAlgorithms.getDigest(algOID)).digest(digest)
        val imphashed = imprint.hashedMessage
        val res = Arrays.equals(md, imphashed)
        return res
    }

    // Certificates

    /** All the X.509 certificates in no particular order.  */
    private var certs: MutableCollection<Certificate>? = null

    /** All the X.509 certificates used for the main signature.  */
    private var signCerts: Collection<Certificate>? = null

    /** The X.509 certificate that is used to sign the digest.  */
    /**
     * Get the X.509 certificate actually used to sign the digest.
     * @return the X.509 certificate actually used to sign the digest
     */
    var signingCertificate: X509Certificate? = null
        private set

    /**
     * Get all the X.509 certificates associated with this PKCS#7 object in no particular order.
     * Other certificates, from OCSP for example, will also be included.
     * @return the X.509 certificates associated with this PKCS#7 object
     */
    val certificates: Array<Certificate>
        get() = certs!!.toArray<X509Certificate>(arrayOfNulls<X509Certificate>(certs!!.size))

    /**
     * Get the X.509 sign certificate chain associated with this PKCS#7 object.
     * Only the certificates used for the main signature will be returned, with
     * the signing certificate first.
     * @return the X.509 certificates associated with this PKCS#7 object
     * *
     * @since    2.1.6
     */
    val signCertificateChain: Array<Certificate>
        get() = signCerts!!.toArray<X509Certificate>(arrayOfNulls<X509Certificate>(signCerts!!.size))

    /**
     * Helper method that creates the collection of certificates
     * used for the main signature based on the complete list
     * of certificates and the sign certificate.
     */
    private fun signCertificateChain() {
        val cc = ArrayList<Certificate>()
        cc.add(signingCertificate)
        val oc = ArrayList(certs)
        run {
            var k = 0
            while (k < oc.size) {
                if (signingCertificate == oc[k]) {
                    oc.removeAt(k)
                    --k
                    ++k
                    continue
                }
                ++k
            }
        }
        var found = true
        while (found) {
            val v = cc[cc.size - 1] as X509Certificate
            found = false
            for (k in oc.indices) {
                val issuer = oc[k] as X509Certificate
                try {
                    if (provider == null)
                        v.verify(issuer.publicKey)
                    else
                        v.verify(issuer.publicKey, provider)
                    found = true
                    cc.add(oc[k])
                    oc.removeAt(k)
                    break
                } catch (e: Exception) {
                }

            }
        }
        signCerts = cc
    }

    // Certificate Revocation Lists

    private var crls: MutableCollection<CRL>? = null

    /**
     * Get the X.509 certificate revocation lists associated with this PKCS#7 object
     * @return the X.509 certificate revocation lists associated with this PKCS#7 object
     */
    val crLs: Collection<CRL>
        get() = crls

    /**
     * Helper method that tries to construct the CRLs.
     */
    private fun findCRL(seq: ASN1Sequence) {
        try {
            crls = ArrayList<CRL>()
            for (k in 0..seq.size() - 1) {
                val ar = ByteArrayInputStream(seq.getObjectAt(k).toASN1Primitive().getEncoded(ASN1Encoding.DER))
                val cf = CertificateFactory.getInstance("X.509")
                val crl = cf.generateCRL(ar) as X509CRL
                crls!!.add(crl)
            }
        } catch (ex: Exception) {
            // ignore
        }

    }

    // Online Certificate Status Protocol

    /** BouncyCastle BasicOCSPResp  */
    /**
     * Gets the OCSP basic response if there is one.
     * @return the OCSP basic response or null
     * *
     * @since    2.1.6
     */
    var ocsp: BasicOCSPResp? = null
        private set

    /**
     * Checks if OCSP revocation refers to the document signing certificate.
     * @return true if it checks, false otherwise
     * *
     * @since    2.1.6
     */
    val isRevocationValid: Boolean
        get() {
            if (ocsp == null)
                return false
            if (signCerts!!.size < 2)
                return false
            try {
                val cs = signCertificateChain as Array<X509Certificate>
                val sr = ocsp!!.responses[0]
                val cid = sr.certID
                val digestalg = JcaDigestCalculatorProviderBuilder().build().get(AlgorithmIdentifier(cid.hashAlgOID, DERNull.INSTANCE))
                val sigcer = signingCertificate
                val isscer = cs[1]
                val tis = CertificateID(
                        digestalg, JcaX509CertificateHolder(isscer), sigcer.getSerialNumber())
                return tis == cid
            } catch (ex: Exception) {
            }

            return false
        }

    /**
     * Helper method that creates the BasicOCSPResp object.
     * @param seq
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun findOcsp(seq: ASN1Sequence) {
        var seq = seq
        ocsp = null
        var ret = false
        while (true) {
            if (seq.getObjectAt(0) is ASN1ObjectIdentifier && (seq.getObjectAt(0) as ASN1ObjectIdentifier).id == OCSPObjectIdentifiers.id_pkix_ocsp_basic.id) {
                break
            }
            ret = true
            for (k in 0..seq.size() - 1) {
                if (seq.getObjectAt(k) is ASN1Sequence) {
                    seq = seq.getObjectAt(0) as ASN1Sequence
                    ret = false
                    break
                }
                if (seq.getObjectAt(k) is ASN1TaggedObject) {
                    val tag = seq.getObjectAt(k) as ASN1TaggedObject
                    if (tag.`object` is ASN1Sequence) {
                        seq = tag.`object` as ASN1Sequence
                        ret = false
                        break
                    } else
                        return
                }
            }
            if (ret)
                return
        }
        val os = seq.getObjectAt(1) as ASN1OctetString
        val inp = ASN1InputStream(os.octets)
        val resp = BasicOCSPResponse.getInstance(inp.readObject())
        ocsp = BasicOCSPResp(resp)
    }

    // Time Stamps

    /** True if there's a PAdES LTV time stamp.  */
    /**
     * Check if it's a PAdES-LTV time stamp.
     * @return true if it's a PAdES-LTV time stamp, false otherwise
     */
    val isTsp: Boolean

    /** True if it's a CAdES signature type.  */
    private val isCades: Boolean

    /** BouncyCastle TimeStampToken.  */
    /**
     * Gets the timestamp token if there is one.
     * @return the timestamp token or null
     * *
     * @since    2.1.6
     */
    var timeStampToken: TimeStampToken? = null
        private set

    /**
     * Gets the timestamp date
     * @return    a date
     * *
     * @since    2.1.6
     */
    val timeStampDate: Calendar?
        get() {
            if (timeStampToken == null)
                return null
            val cal = GregorianCalendar()
            val date = timeStampToken!!.timeStampInfo.genTime
            cal.time = date
            return cal
        }

    /**
     * Returns the encryption algorithm
     * @return    the name of an encryption algorithm
     */
    val encryptionAlgorithm: String
        get() {
            var encryptAlgo: String? = EncryptionAlgorithms.getAlgorithm(digestEncryptionAlgorithmOid)
            if (encryptAlgo == null)
                encryptAlgo = digestEncryptionAlgorithmOid
            return encryptAlgo
        }
}
/**
 * Gets the bytes for the PKCS7SignedData object. Optionally the authenticatedAttributes
 * in the signerInfo can also be set. If either of the parameters is null, none will be used.
 * @param secondDigest the digest in the authenticatedAttributes
 * *
 * @return the bytes for the PKCS7SignedData object
 */
