package org.bouncycastle.asn1.pkcs

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DLSequence
import org.bouncycastle.asn1.DLTaggedObject

class SafeBag : ASN1Object {
    var bagId: ASN1ObjectIdentifier? = null
        private set
    var bagValue: ASN1Encodable? = null
        private set
    var bagAttributes: ASN1Set? = null
        private set

    constructor(
            oid: ASN1ObjectIdentifier,
            obj: ASN1Encodable) {
        this.bagId = oid
        this.bagValue = obj
        this.bagAttributes = null
    }

    constructor(
            oid: ASN1ObjectIdentifier,
            obj: ASN1Encodable,
            bagAttributes: ASN1Set) {
        this.bagId = oid
        this.bagValue = obj
        this.bagAttributes = bagAttributes
    }

    private constructor(
            seq: ASN1Sequence) {
        this.bagId = seq.getObjectAt(0) as ASN1ObjectIdentifier
        this.bagValue = (seq.getObjectAt(1) as ASN1TaggedObject).`object`
        if (seq.size() == 3) {
            this.bagAttributes = seq.getObjectAt(2) as ASN1Set
        }
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(bagId)
        v.add(DLTaggedObject(true, 0, bagValue))

        if (bagAttributes != null) {
            v.add(bagAttributes)
        }

        return DLSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): SafeBag? {
            if (obj is SafeBag) {
                return obj
            }

            if (obj != null) {
                return SafeBag(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
