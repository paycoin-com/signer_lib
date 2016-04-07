package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Boolean
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.util.Strings

/**
 *
 * IssuingDistributionPoint ::= SEQUENCE {
 * distributionPoint          [0] DistributionPointName OPTIONAL,
 * onlyContainsUserCerts      [1] BOOLEAN DEFAULT FALSE,
 * onlyContainsCACerts        [2] BOOLEAN DEFAULT FALSE,
 * onlySomeReasons            [3] ReasonFlags OPTIONAL,
 * indirectCRL                [4] BOOLEAN DEFAULT FALSE,
 * onlyContainsAttributeCerts [5] BOOLEAN DEFAULT FALSE }
 *
 */
class IssuingDistributionPoint : ASN1Object {
    /**
     * @return Returns the distributionPoint.
     */
    var distributionPoint: DistributionPointName? = null
        private set

    private var onlyContainsUserCerts: Boolean = false

    private var onlyContainsCACerts: Boolean = false

    /**
     * @return Returns the onlySomeReasons.
     */
    var onlySomeReasons: ReasonFlags? = null
        private set

    var isIndirectCRL: Boolean = false
        private set

    private var onlyContainsAttributeCerts: Boolean = false

    private var seq: ASN1Sequence? = null

    /**
     * Constructor from given details.

     * @param distributionPoint
     * *            May contain an URI as pointer to most current CRL.
     * *
     * @param onlyContainsUserCerts Covers revocation information for end certificates.
     * *
     * @param onlyContainsCACerts Covers revocation information for CA certificates.
     * *
     * *
     * @param onlySomeReasons
     * *            Which revocation reasons does this point cover.
     * *
     * @param indirectCRL
     * *            If `true` then the CRL contains revocation
     * *            information about certificates ssued by other CAs.
     * *
     * @param onlyContainsAttributeCerts Covers revocation information for attribute certificates.
     */
    constructor(
            distributionPoint: DistributionPointName?,
            onlyContainsUserCerts: Boolean,
            onlyContainsCACerts: Boolean,
            onlySomeReasons: ReasonFlags?,
            indirectCRL: Boolean,
            onlyContainsAttributeCerts: Boolean) {
        this.distributionPoint = distributionPoint
        this.isIndirectCRL = indirectCRL
        this.onlyContainsAttributeCerts = onlyContainsAttributeCerts
        this.onlyContainsCACerts = onlyContainsCACerts
        this.onlyContainsUserCerts = onlyContainsUserCerts
        this.onlySomeReasons = onlySomeReasons

        val vec = ASN1EncodableVector()
        if (distributionPoint != null) {
            // CHOICE item so explicitly tagged
            vec.add(DERTaggedObject(true, 0, distributionPoint))
        }
        if (onlyContainsUserCerts) {
            vec.add(DERTaggedObject(false, 1, ASN1Boolean.getInstance(true)))
        }
        if (onlyContainsCACerts) {
            vec.add(DERTaggedObject(false, 2, ASN1Boolean.getInstance(true)))
        }
        if (onlySomeReasons != null) {
            vec.add(DERTaggedObject(false, 3, onlySomeReasons))
        }
        if (indirectCRL) {
            vec.add(DERTaggedObject(false, 4, ASN1Boolean.getInstance(true)))
        }
        if (onlyContainsAttributeCerts) {
            vec.add(DERTaggedObject(false, 5, ASN1Boolean.getInstance(true)))
        }

        seq = DERSequence(vec)
    }

    /**
     * Shorthand Constructor from given details.

     * @param distributionPoint
     * *            May contain an URI as pointer to most current CRL.
     * *
     * @param indirectCRL
     * *            If `true` then the CRL contains revocation
     * *            information about certificates ssued by other CAs.
     * *
     * @param onlyContainsAttributeCerts Covers revocation information for attribute certificates.
     */
    constructor(
            distributionPoint: DistributionPointName,
            indirectCRL: Boolean,
            onlyContainsAttributeCerts: Boolean) : this(distributionPoint, false, false, null, indirectCRL, onlyContainsAttributeCerts) {
    }

    /**
     * Constructor from ASN1Sequence
     */
    private constructor(
            seq: ASN1Sequence) {
        this.seq = seq

        for (i in 0..seq.size() - 1) {
            val o = ASN1TaggedObject.getInstance(seq.getObjectAt(i))

            when (o.tagNo) {
                0 -> // CHOICE so explicit
                    distributionPoint = DistributionPointName.getInstance(o, true)
                1 -> onlyContainsUserCerts = ASN1Boolean.getInstance(o, false).isTrue
                2 -> onlyContainsCACerts = ASN1Boolean.getInstance(o, false).isTrue
                3 -> onlySomeReasons = ReasonFlags(ReasonFlags.getInstance(o, false))
                4 -> isIndirectCRL = ASN1Boolean.getInstance(o, false).isTrue
                5 -> onlyContainsAttributeCerts = ASN1Boolean.getInstance(o, false).isTrue
                else -> throw IllegalArgumentException(
                        "unknown tag in IssuingDistributionPoint")
            }
        }
    }

    fun onlyContainsUserCerts(): Boolean {
        return onlyContainsUserCerts
    }

    fun onlyContainsCACerts(): Boolean {
        return onlyContainsCACerts
    }

    fun onlyContainsAttributeCerts(): Boolean {
        return onlyContainsAttributeCerts
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return seq
    }

    override fun toString(): String {
        val sep = Strings.lineSeparator()
        val buf = StringBuffer()

        buf.append("IssuingDistributionPoint: [")
        buf.append(sep)
        if (distributionPoint != null) {
            appendObject(buf, sep, "distributionPoint", distributionPoint!!.toString())
        }
        if (onlyContainsUserCerts) {
            appendObject(buf, sep, "onlyContainsUserCerts", booleanToString(onlyContainsUserCerts))
        }
        if (onlyContainsCACerts) {
            appendObject(buf, sep, "onlyContainsCACerts", booleanToString(onlyContainsCACerts))
        }
        if (onlySomeReasons != null) {
            appendObject(buf, sep, "onlySomeReasons", onlySomeReasons!!.toString())
        }
        if (onlyContainsAttributeCerts) {
            appendObject(buf, sep, "onlyContainsAttributeCerts", booleanToString(onlyContainsAttributeCerts))
        }
        if (isIndirectCRL) {
            appendObject(buf, sep, "indirectCRL", booleanToString(isIndirectCRL))
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

    private fun booleanToString(value: Boolean): String {
        return if (value) "true" else "false"
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): IssuingDistributionPoint {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): IssuingDistributionPoint? {
            if (obj is IssuingDistributionPoint) {
                return obj
            } else if (obj != null) {
                return IssuingDistributionPoint(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
