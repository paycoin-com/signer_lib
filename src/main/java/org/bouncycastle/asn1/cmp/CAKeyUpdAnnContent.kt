package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class CAKeyUpdAnnContent : ASN1Object {
    var oldWithNew: CMPCertificate? = null
        private set
    var newWithOld: CMPCertificate? = null
        private set
    var newWithNew: CMPCertificate? = null
        private set

    private constructor(seq: ASN1Sequence) {
        oldWithNew = CMPCertificate.getInstance(seq.getObjectAt(0))
        newWithOld = CMPCertificate.getInstance(seq.getObjectAt(1))
        newWithNew = CMPCertificate.getInstance(seq.getObjectAt(2))
    }

    constructor(oldWithNew: CMPCertificate, newWithOld: CMPCertificate, newWithNew: CMPCertificate) {
        this.oldWithNew = oldWithNew
        this.newWithOld = newWithOld
        this.newWithNew = newWithNew
    }

    /**
     *
     * CAKeyUpdAnnContent ::= SEQUENCE {
     * oldWithNew   CMPCertificate, -- old pub signed with new priv
     * newWithOld   CMPCertificate, -- new pub signed with old priv
     * newWithNew   CMPCertificate  -- new pub signed with new priv
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(oldWithNew)
        v.add(newWithOld)
        v.add(newWithNew)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): CAKeyUpdAnnContent? {
            if (o is CAKeyUpdAnnContent) {
                return o
            }

            if (o != null) {
                return CAKeyUpdAnnContent(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
