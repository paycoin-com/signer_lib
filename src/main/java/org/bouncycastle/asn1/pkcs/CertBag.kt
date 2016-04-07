package org.bouncycastle.asn1.pkcs

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

class CertBag : ASN1Object {
    var certId: ASN1ObjectIdentifier? = null
        private set
    var certValue: ASN1Encodable? = null
        private set

    private constructor(
            seq: ASN1Sequence) {
        this.certId = seq.getObjectAt(0) as ASN1ObjectIdentifier
        this.certValue = (seq.getObjectAt(1) as DERTaggedObject).`object`
    }

    constructor(
            certId: ASN1ObjectIdentifier,
            certValue: ASN1Encodable) {
        this.certId = certId
        this.certValue = certValue
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(certId)
        v.add(DERTaggedObject(0, certValue))

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): CertBag? {
            if (o is CertBag) {
                return o
            } else if (o != null) {
                return CertBag(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
