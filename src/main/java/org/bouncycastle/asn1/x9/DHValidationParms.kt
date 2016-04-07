package org.bouncycastle.asn1.x9

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence


@Deprecated("use ValidationParams")
class DHValidationParms : ASN1Object {
    var seed: DERBitString? = null
        private set
    var pgenCounter: ASN1Integer? = null
        private set

    constructor(seed: DERBitString?, pgenCounter: ASN1Integer?) {
        if (seed == null) {
            throw IllegalArgumentException("'seed' cannot be null")
        }
        if (pgenCounter == null) {
            throw IllegalArgumentException("'pgenCounter' cannot be null")
        }

        this.seed = seed
        this.pgenCounter = pgenCounter
    }

    private constructor(seq: ASN1Sequence) {
        if (seq.size() != 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        this.seed = DERBitString.getInstance(seq.getObjectAt(0))
        this.pgenCounter = ASN1Integer.getInstance(seq.getObjectAt(1))
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()
        v.add(this.seed)
        v.add(this.pgenCounter)
        return DERSequence(v)
    }

    companion object {

        fun getInstance(obj: ASN1TaggedObject, explicit: Boolean): DHValidationParms {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(obj: Any?): DHValidationParms? {
            if (obj is DHValidationParms) {
                return obj
            } else if (obj != null) {
                return DHValidationParms(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
