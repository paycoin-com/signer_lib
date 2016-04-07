package org.bouncycastle.asn1.esf

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

/**
 *
 * CrlOcspRef ::= SEQUENCE {
 * crlids [0] CRLListID OPTIONAL,
 * ocspids [1] OcspListID OPTIONAL,
 * otherRev [2] OtherRevRefs OPTIONAL
 * }
 *
 */
class CrlOcspRef : ASN1Object {

    var crlids: CrlListID? = null
        private set
    var ocspids: OcspListID? = null
        private set
    var otherRev: OtherRevRefs? = null
        private set

    private constructor(seq: ASN1Sequence) {
        val e = seq.objects
        while (e.hasMoreElements()) {
            val o = e.nextElement() as DERTaggedObject
            when (o.tagNo) {
                0 -> this.crlids = CrlListID.getInstance(o.`object`)
                1 -> this.ocspids = OcspListID.getInstance(o.`object`)
                2 -> this.otherRev = OtherRevRefs.getInstance(o.`object`)
                else -> throw IllegalArgumentException("illegal tag")
            }
        }
    }

    constructor(crlids: CrlListID, ocspids: OcspListID,
                otherRev: OtherRevRefs) {
        this.crlids = crlids
        this.ocspids = ocspids
        this.otherRev = otherRev
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()
        if (null != this.crlids) {
            v.add(DERTaggedObject(true, 0, this.crlids!!.toASN1Primitive()))
        }
        if (null != this.ocspids) {
            v.add(DERTaggedObject(true, 1, this.ocspids!!.toASN1Primitive()))
        }
        if (null != this.otherRev) {
            v.add(DERTaggedObject(true, 2, this.otherRev!!.toASN1Primitive()))
        }
        return DERSequence(v)
    }

    companion object {

        fun getInstance(obj: Any?): CrlOcspRef? {
            if (obj is CrlOcspRef) {
                return obj
            } else if (obj != null) {
                return CrlOcspRef(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
