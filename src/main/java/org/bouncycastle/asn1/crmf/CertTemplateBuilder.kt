package org.bouncycastle.asn1.crmf

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.asn1.x509.X509Extensions

class CertTemplateBuilder {
    private var version: ASN1Integer? = null
    private var serialNumber: ASN1Integer? = null
    private var signingAlg: AlgorithmIdentifier? = null
    private var issuer: X500Name? = null
    private var validity: OptionalValidity? = null
    private var subject: X500Name? = null
    private var publicKey: SubjectPublicKeyInfo? = null
    private var issuerUID: DERBitString? = null
    private var subjectUID: DERBitString? = null
    private var extensions: Extensions? = null

    /** Sets the X.509 version. Note: for X509v3, use 2 here.  */
    fun setVersion(ver: Int): CertTemplateBuilder {
        version = ASN1Integer(ver.toLong())

        return this
    }

    fun setSerialNumber(ser: ASN1Integer): CertTemplateBuilder {
        serialNumber = ser

        return this
    }

    fun setSigningAlg(aid: AlgorithmIdentifier): CertTemplateBuilder {
        signingAlg = aid

        return this
    }

    fun setIssuer(name: X500Name): CertTemplateBuilder {
        issuer = name

        return this
    }

    fun setValidity(v: OptionalValidity): CertTemplateBuilder {
        validity = v

        return this
    }

    fun setSubject(name: X500Name): CertTemplateBuilder {
        subject = name

        return this
    }

    fun setPublicKey(spki: SubjectPublicKeyInfo): CertTemplateBuilder {
        publicKey = spki

        return this
    }

    /** Sets the issuer unique ID (deprecated in X.509v3)  */
    fun setIssuerUID(uid: DERBitString): CertTemplateBuilder {
        issuerUID = uid

        return this
    }

    /** Sets the subject unique ID (deprecated in X.509v3)  */
    fun setSubjectUID(uid: DERBitString): CertTemplateBuilder {
        subjectUID = uid

        return this
    }


    @Deprecated("use method taking Extensions")
    fun setExtensions(extens: X509Extensions): CertTemplateBuilder {
        return setExtensions(Extensions.getInstance(extens))
    }

    fun setExtensions(extens: Extensions): CertTemplateBuilder {
        extensions = extens

        return this
    }

    /**
     *
     * CertTemplate ::= SEQUENCE {
     * version      [0] Version               OPTIONAL,
     * serialNumber [1] INTEGER               OPTIONAL,
     * signingAlg   [2] AlgorithmIdentifier   OPTIONAL,
     * issuer       [3] Name                  OPTIONAL,
     * validity     [4] OptionalValidity      OPTIONAL,
     * subject      [5] Name                  OPTIONAL,
     * publicKey    [6] SubjectPublicKeyInfo  OPTIONAL,
     * issuerUID    [7] UniqueIdentifier      OPTIONAL,
     * subjectUID   [8] UniqueIdentifier      OPTIONAL,
     * extensions   [9] Extensions            OPTIONAL }
     *
     * @return a basic ASN.1 object representation.
     */
    fun build(): CertTemplate {
        val v = ASN1EncodableVector()

        addOptional(v, 0, false, version)
        addOptional(v, 1, false, serialNumber)
        addOptional(v, 2, false, signingAlg)
        addOptional(v, 3, true, issuer) // CHOICE
        addOptional(v, 4, false, validity)
        addOptional(v, 5, true, subject) // CHOICE
        addOptional(v, 6, false, publicKey)
        addOptional(v, 7, false, issuerUID)
        addOptional(v, 8, false, subjectUID)
        addOptional(v, 9, false, extensions)

        return CertTemplate.getInstance(DERSequence(v))
    }

    private fun addOptional(v: ASN1EncodableVector, tagNo: Int, isExplicit: Boolean, obj: ASN1Encodable?) {
        if (obj != null) {
            v.add(DERTaggedObject(isExplicit, tagNo, obj))
        }
    }
}
