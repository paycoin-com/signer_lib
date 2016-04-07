package org.bouncycastle.asn1.x509

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

class NameConstraints : ASN1Object {
    var permittedSubtrees: Array<GeneralSubtree>? = null
        private set
    var excludedSubtrees: Array<GeneralSubtree>? = null
        private set

    private constructor(seq: ASN1Sequence) {
        val e = seq.objects
        while (e.hasMoreElements()) {
            val o = ASN1TaggedObject.getInstance(e.nextElement())
            when (o.tagNo) {
                0 -> permittedSubtrees = createArray(ASN1Sequence.getInstance(o, false))
                1 -> excludedSubtrees = createArray(ASN1Sequence.getInstance(o, false))
            }
        }
    }

    /**
     * Constructor from a given details.

     *
     *
     * permitted and excluded are arrays of GeneralSubtree objects.

     * @param permitted
     * *            Permitted subtrees
     * *
     * @param excluded
     * *            Excludes subtrees
     */
    constructor(
            permitted: Array<GeneralSubtree>?,
            excluded: Array<GeneralSubtree>?) {
        if (permitted != null) {
            this.permittedSubtrees = permitted
        }

        if (excluded != null) {
            this.excludedSubtrees = excluded
        }
    }

    private fun createArray(subtree: ASN1Sequence): Array<GeneralSubtree> {
        val ar = arrayOfNulls<GeneralSubtree>(subtree.size())

        for (i in ar.indices) {
            ar[i] = GeneralSubtree.getInstance(subtree.getObjectAt(i))
        }

        return ar
    }

    /*
     * NameConstraints ::= SEQUENCE { permittedSubtrees [0] GeneralSubtrees
     * OPTIONAL, excludedSubtrees [1] GeneralSubtrees OPTIONAL }
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (permittedSubtrees != null) {
            v.add(DERTaggedObject(false, 0, DERSequence(permittedSubtrees)))
        }

        if (excludedSubtrees != null) {
            v.add(DERTaggedObject(false, 1, DERSequence(excludedSubtrees)))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(obj: Any?): NameConstraints? {
            if (obj is NameConstraints) {
                return obj
            }
            if (obj != null) {
                return NameConstraints(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
