package org.bouncycastle.asn1.x509

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

/**
 *
 * PrivateKeyUsagePeriod ::= SEQUENCE {
 * notBefore       [0]     GeneralizedTime OPTIONAL,
 * notAfter        [1]     GeneralizedTime OPTIONAL }
 *
 */
class PrivateKeyUsagePeriod private constructor(seq: ASN1Sequence) : ASN1Object() {

    var notBefore: ASN1GeneralizedTime? = null
        private set
    var notAfter: ASN1GeneralizedTime? = null
        private set

    init {
        val en = seq.objects
        while (en.hasMoreElements()) {
            val tObj = en.nextElement() as ASN1TaggedObject

            if (tObj.tagNo == 0) {
                notBefore = ASN1GeneralizedTime.getInstance(tObj, false)
            } else if (tObj.tagNo == 1) {
                notAfter = ASN1GeneralizedTime.getInstance(tObj, false)
            }
        }
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (notBefore != null) {
            v.add(DERTaggedObject(false, 0, notBefore))
        }
        if (notAfter != null) {
            v.add(DERTaggedObject(false, 1, notAfter))
        }

        return DERSequence(v)
    }

    companion object {
        fun getInstance(obj: Any?): PrivateKeyUsagePeriod? {
            if (obj is PrivateKeyUsagePeriod) {
                return obj
            }

            if (obj != null) {
                return PrivateKeyUsagePeriod(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
