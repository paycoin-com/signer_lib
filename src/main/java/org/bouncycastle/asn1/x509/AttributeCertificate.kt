package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence

class AttributeCertificate : ASN1Object {
    var acinfo: AttributeCertificateInfo
        internal set
    var signatureAlgorithm: AlgorithmIdentifier
        internal set
    var signatureValue: DERBitString
        internal set

    constructor(
            acinfo: AttributeCertificateInfo,
            signatureAlgorithm: AlgorithmIdentifier,
            signatureValue: DERBitString) {
        this.acinfo = acinfo
        this.signatureAlgorithm = signatureAlgorithm
        this.signatureValue = signatureValue
    }


    @Deprecated("use getInstance() method.")
    constructor(
            seq: ASN1Sequence) {
        if (seq.size() != 3) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        this.acinfo = AttributeCertificateInfo.getInstance(seq.getObjectAt(0))
        this.signatureAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1))
        this.signatureValue = DERBitString.getInstance(seq.getObjectAt(2))
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * AttributeCertificate ::= SEQUENCE {
     * acinfo               AttributeCertificateInfo,
     * signatureAlgorithm   AlgorithmIdentifier,
     * signatureValue       BIT STRING
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(acinfo)
        v.add(signatureAlgorithm)
        v.add(signatureValue)

        return DERSequence(v)
    }

    companion object {

        /**
         * @param obj
         * *
         * @return an AttributeCertificate object
         */
        fun getInstance(obj: Any?): AttributeCertificate? {
            if (obj is AttributeCertificate) {
                return obj
            } else if (obj != null) {
                return AttributeCertificate(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
