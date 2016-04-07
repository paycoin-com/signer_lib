package org.bouncycastle.asn1.pkcs

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.BERSequence
import org.bouncycastle.asn1.DLSequence

class AuthenticatedSafe : ASN1Object {
    var contentInfo: Array<ContentInfo>? = null
        private set
    private var isBer = true

    private constructor(
            seq: ASN1Sequence) {
        contentInfo = arrayOfNulls<ContentInfo>(seq.size())

        for (i in contentInfo!!.indices) {
            contentInfo[i] = ContentInfo.getInstance(seq.getObjectAt(i))
        }

        isBer = seq is BERSequence
    }

    constructor(
            info: Array<ContentInfo>) {
        this.contentInfo = info
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        for (i in contentInfo!!.indices) {
            v.add(contentInfo!![i])
        }

        if (isBer) {
            return BERSequence(v)
        } else {
            return DLSequence(v)
        }
    }

    companion object {

        fun getInstance(
                o: Any?): AuthenticatedSafe? {
            if (o is AuthenticatedSafe) {
                return o
            }

            if (o != null) {
                return AuthenticatedSafe(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
