package org.bouncycastle.asn1.pkcs

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * PKCS10 Certification request object.
 *
 * CertificationRequest ::= SEQUENCE {
 * certificationRequestInfo  CertificationRequestInfo,
 * signatureAlgorithm        AlgorithmIdentifier{{ SignatureAlgorithms }},
 * signature                 BIT STRING
 * }
 *
 */
open class CertificationRequest : ASN1Object {
    var certificationRequestInfo: CertificationRequestInfo? = null
        protected set
    var signatureAlgorithm: AlgorithmIdentifier? = null
        protected set
    var signature: DERBitString? = null
        protected set

    protected constructor() {
    }

    constructor(
            requestInfo: CertificationRequestInfo,
            algorithm: AlgorithmIdentifier,
            signature: DERBitString) {
        this.certificationRequestInfo = requestInfo
        this.signatureAlgorithm = algorithm
        this.signature = signature
    }

    constructor(
            seq: ASN1Sequence) {
        certificationRequestInfo = CertificationRequestInfo.getInstance(seq.getObjectAt(0))
        signatureAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1))
        signature = seq.getObjectAt(2) as DERBitString
    }

    override fun toASN1Primitive(): ASN1Primitive {
        // Construct the CertificateRequest
        val v = ASN1EncodableVector()

        v.add(certificationRequestInfo)
        v.add(signatureAlgorithm)
        v.add(signature)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): CertificationRequest? {
            if (o is CertificationRequest) {
                return o
            }

            if (o != null) {
                return CertificationRequest(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
