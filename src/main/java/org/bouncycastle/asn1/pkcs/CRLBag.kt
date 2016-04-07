package org.bouncycastle.asn1.pkcs

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

/**
 * CRL Bag for PKCS#12
 */
class CRLBag : ASN1Object {
    var crlId: ASN1ObjectIdentifier? = null
        private set
    var crlValue: ASN1Encodable? = null
        private set

    private constructor(
            seq: ASN1Sequence) {
        this.crlId = seq.getObjectAt(0) as ASN1ObjectIdentifier
        this.crlValue = (seq.getObjectAt(1) as DERTaggedObject).`object`
    }

    constructor(
            crlId: ASN1ObjectIdentifier,
            crlValue: ASN1Encodable) {
        this.crlId = crlId
        this.crlValue = crlValue
    }

    /**
     *
     * CRLBag ::= SEQUENCE {
     * crlId  BAG-TYPE.&amp;id ({CRLTypes}),
     * crlValue  [0] EXPLICIT BAG-TYPE.&amp;Type ({CRLTypes}{&#64;crlId})
     * }

     * x509CRL BAG-TYPE ::= {OCTET STRING IDENTIFIED BY {certTypes 1}
     * -- DER-encoded X.509 CRL stored in OCTET STRING

     * CRLTypes BAG-TYPE ::= {
     * x509CRL,
     * ... -- For future extensions
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(crlId)
        v.add(DERTaggedObject(0, crlValue))

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): CRLBag? {
            if (o is CRLBag) {
                return o
            } else if (o != null) {
                return CRLBag(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
