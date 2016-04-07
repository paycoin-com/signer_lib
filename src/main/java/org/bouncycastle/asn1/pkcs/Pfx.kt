package org.bouncycastle.asn1.pkcs

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.BERSequence

/**
 * the infamous Pfx from PKCS12
 */
class Pfx : ASN1Object, PKCSObjectIdentifiers {
    var authSafe: ContentInfo? = null
        private set
    var macData: MacData? = null
        private set

    private constructor(
            seq: ASN1Sequence) {
        val version = (seq.getObjectAt(0) as ASN1Integer).value
        if (version.toInt() != 3) {
            throw IllegalArgumentException("wrong version for PFX PDU")
        }

        authSafe = ContentInfo.getInstance(seq.getObjectAt(1))

        if (seq.size() == 3) {
            macData = MacData.getInstance(seq.getObjectAt(2))
        }
    }

    constructor(
            contentInfo: ContentInfo,
            macData: MacData) {
        this.authSafe = contentInfo
        this.macData = macData
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(ASN1Integer(3))
        v.add(authSafe)

        if (macData != null) {
            v.add(macData)
        }

        return BERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): Pfx? {
            if (obj is Pfx) {
                return obj
            }

            if (obj != null) {
                return Pfx(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
