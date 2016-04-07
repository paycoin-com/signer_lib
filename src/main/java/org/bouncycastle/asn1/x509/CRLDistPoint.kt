package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.util.Strings

class CRLDistPoint : ASN1Object {
    internal var seq: ASN1Sequence? = null

    private constructor(
            seq: ASN1Sequence) {
        this.seq = seq
    }

    constructor(
            points: Array<DistributionPoint>) {
        val v = ASN1EncodableVector()

        for (i in points.indices) {
            v.add(points[i])
        }

        seq = DERSequence(v)
    }

    /**
     * Return the distribution points making up the sequence.

     * @return DistributionPoint[]
     */
    val distributionPoints: Array<DistributionPoint>
        get() {
            val dp = arrayOfNulls<DistributionPoint>(seq!!.size())

            for (i in 0..seq!!.size() - 1) {
                dp[i] = DistributionPoint.getInstance(seq!!.getObjectAt(i))
            }

            return dp
        }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * CRLDistPoint ::= SEQUENCE SIZE {1..MAX} OF DistributionPoint
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return seq
    }

    override fun toString(): String {
        val buf = StringBuffer()
        val sep = Strings.lineSeparator()

        buf.append("CRLDistPoint:")
        buf.append(sep)
        val dp = distributionPoints
        for (i in dp.indices) {
            buf.append("    ")
            buf.append(dp[i])
            buf.append(sep)
        }
        return buf.toString()
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): CRLDistPoint {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): CRLDistPoint? {
            if (obj is CRLDistPoint) {
                return obj
            } else if (obj != null) {
                return CRLDistPoint(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
