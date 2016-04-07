package org.bouncycastle.asn1.ocsp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

class Signature : ASN1Object {
    var signatureAlgorithm: AlgorithmIdentifier
        internal set
    var signature: DERBitString
        internal set
    var certs: ASN1Sequence? = null
        internal set

    constructor(
            signatureAlgorithm: AlgorithmIdentifier,
            signature: DERBitString) {
        this.signatureAlgorithm = signatureAlgorithm
        this.signature = signature
    }

    constructor(
            signatureAlgorithm: AlgorithmIdentifier,
            signature: DERBitString,
            certs: ASN1Sequence) {
        this.signatureAlgorithm = signatureAlgorithm
        this.signature = signature
        this.certs = certs
    }

    private constructor(
            seq: ASN1Sequence) {
        signatureAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(0))
        signature = seq.getObjectAt(1) as DERBitString

        if (seq.size() == 3) {
            certs = ASN1Sequence.getInstance(
                    seq.getObjectAt(2) as ASN1TaggedObject, true)
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * Signature       ::=     SEQUENCE {
     * signatureAlgorithm      AlgorithmIdentifier,
     * signature               BIT STRING,
     * certs               [0] EXPLICIT SEQUENCE OF Certificate OPTIONAL}
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(signatureAlgorithm)
        v.add(signature)

        if (certs != null) {
            v.add(DERTaggedObject(true, 0, certs))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): Signature {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): Signature? {
            if (obj is Signature) {
                return obj
            } else if (obj != null) {
                return Signature(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
