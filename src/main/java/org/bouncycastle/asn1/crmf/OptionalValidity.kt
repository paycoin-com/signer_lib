package org.bouncycastle.asn1.crmf

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.Time

class OptionalValidity : ASN1Object {
    var notBefore: Time? = null
        private set
    var notAfter: Time? = null
        private set

    private constructor(seq: ASN1Sequence) {
        val en = seq.objects
        while (en.hasMoreElements()) {
            val tObj = en.nextElement() as ASN1TaggedObject

            if (tObj.tagNo == 0) {
                notBefore = Time.getInstance(tObj, true)
            } else {
                notAfter = Time.getInstance(tObj, true)
            }
        }
    }

    constructor(notBefore: Time?, notAfter: Time?) {
        if (notBefore == null && notAfter == null) {
            throw IllegalArgumentException("at least one of notBefore/notAfter must not be null.")
        }

        this.notBefore = notBefore
        this.notAfter = notAfter
    }

    /**
     *
     * OptionalValidity ::= SEQUENCE {
     * notBefore  [0] Time OPTIONAL,
     * notAfter   [1] Time OPTIONAL } --at least one MUST be present
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (notBefore != null) {
            v.add(DERTaggedObject(true, 0, notBefore))
        }

        if (notAfter != null) {
            v.add(DERTaggedObject(true, 1, notAfter))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): OptionalValidity? {
            if (o is OptionalValidity) {
                return o
            }

            if (o != null) {
                return OptionalValidity(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
