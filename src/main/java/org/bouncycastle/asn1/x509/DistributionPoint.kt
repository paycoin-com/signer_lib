package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.util.Strings

/**
 * The DistributionPoint object.
 *
 * DistributionPoint ::= SEQUENCE {
 * distributionPoint [0] DistributionPointName OPTIONAL,
 * reasons           [1] ReasonFlags OPTIONAL,
 * cRLIssuer         [2] GeneralNames OPTIONAL
 * }
 *
 */
class DistributionPoint : ASN1Object {
    var distributionPoint: DistributionPointName? = null
        internal set
    var reasons: ReasonFlags? = null
        internal set
    var crlIssuer: GeneralNames? = null
        internal set

    constructor(
            seq: ASN1Sequence) {
        for (i in 0..seq.size() - 1) {
            val t = ASN1TaggedObject.getInstance(seq.getObjectAt(i))
            when (t.tagNo) {
                0 -> distributionPoint = DistributionPointName.getInstance(t, true)
                1 -> reasons = ReasonFlags(DERBitString.getInstance(t, false))
                2 -> crlIssuer = GeneralNames.getInstance(t, false)
            }
        }
    }

    constructor(
            distributionPoint: DistributionPointName,
            reasons: ReasonFlags,
            cRLIssuer: GeneralNames) {
        this.distributionPoint = distributionPoint
        this.reasons = reasons
        this.crlIssuer = cRLIssuer
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (distributionPoint != null) {
            //
            // as this is a CHOICE it must be explicitly tagged
            //
            v.add(DERTaggedObject(0, distributionPoint))
        }

        if (reasons != null) {
            v.add(DERTaggedObject(false, 1, reasons))
        }

        if (crlIssuer != null) {
            v.add(DERTaggedObject(false, 2, crlIssuer))
        }

        return DERSequence(v)
    }

    override fun toString(): String {
        val sep = Strings.lineSeparator()
        val buf = StringBuffer()
        buf.append("DistributionPoint: [")
        buf.append(sep)
        if (distributionPoint != null) {
            appendObject(buf, sep, "distributionPoint", distributionPoint!!.toString())
        }
        if (reasons != null) {
            appendObject(buf, sep, "reasons", reasons!!.toString())
        }
        if (crlIssuer != null) {
            appendObject(buf, sep, "cRLIssuer", crlIssuer!!.toString())
        }
        buf.append("]")
        buf.append(sep)
        return buf.toString()
    }

    private fun appendObject(buf: StringBuffer, sep: String, name: String, value: String) {
        val indent = "    "

        buf.append(indent)
        buf.append(name)
        buf.append(":")
        buf.append(sep)
        buf.append(indent)
        buf.append(indent)
        buf.append(value)
        buf.append(sep)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DistributionPoint {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): DistributionPoint {
            if (obj == null || obj is DistributionPoint) {
                return obj as DistributionPoint?
            }

            if (obj is ASN1Sequence) {
                return DistributionPoint(obj as ASN1Sequence?)
            }

            throw IllegalArgumentException("Invalid DistributionPoint: " + obj.javaClass.name)
        }
    }
}
