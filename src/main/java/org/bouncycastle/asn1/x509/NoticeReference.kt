package org.bouncycastle.asn1.x509

import java.math.BigInteger
import java.util.Enumeration
import java.util.Vector

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 * `NoticeReference` class, used in
 * `CertificatePolicies` X509 V3 extensions
 * (in policy qualifiers).

 *
 * NoticeReference ::= SEQUENCE {
 * organization     DisplayText,
 * noticeNumbers    SEQUENCE OF INTEGER }

 *

 * @see PolicyQualifierInfo

 * @see PolicyInformation
 */
class NoticeReference : ASN1Object {
    var organization: DisplayText? = null
        private set
    private var noticeNumbers: ASN1Sequence? = null

    /**
     * Creates a new `NoticeReference` instance.

     * @param organization a `String` value
     * *
     * @param numbers a `Vector` value
     */
    constructor(
            organization: String,
            numbers: Vector<Any>) : this(organization, convertVector(numbers)) {
    }

    /**
     * Creates a new `NoticeReference` instance.

     * @param organization a `String` value
     * *
     * @param noticeNumbers an `ASN1EncodableVector` value
     */
    constructor(
            organization: String,
            noticeNumbers: ASN1EncodableVector) : this(DisplayText(organization), noticeNumbers) {
    }

    /**
     * Creates a new `NoticeReference` instance.

     * @param organization displayText
     * *
     * @param noticeNumbers an `ASN1EncodableVector` value
     */
    constructor(
            organization: DisplayText,
            noticeNumbers: ASN1EncodableVector) {
        this.organization = organization
        this.noticeNumbers = DERSequence(noticeNumbers)
    }

    /**
     * Creates a new `NoticeReference` instance.
     *
     * Useful for reconstructing a `NoticeReference`
     * instance from its encodable/encoded form.

     * @param as an `ASN1Sequence` value obtained from either
     * * calling @{link toASN1Primitive()} for a `NoticeReference`
     * * instance or from parsing it from a DER-encoded stream.
     */
    private constructor(
            `as`: ASN1Sequence) {
        if (`as`.size() != 2) {
            throw IllegalArgumentException("Bad sequence size: " + `as`.size())
        }

        organization = DisplayText.getInstance(`as`.getObjectAt(0))
        noticeNumbers = ASN1Sequence.getInstance(`as`.getObjectAt(1))
    }

    fun getNoticeNumbers(): Array<ASN1Integer> {
        val tmp = arrayOfNulls<ASN1Integer>(noticeNumbers!!.size())

        for (i in 0..noticeNumbers!!.size() - 1) {
            tmp[i] = ASN1Integer.getInstance(noticeNumbers!!.getObjectAt(i))
        }

        return tmp
    }

    /**
     * Describe `toASN1Object` method here.

     * @return a `ASN1Primitive` value
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val av = ASN1EncodableVector()
        av.add(organization)
        av.add(noticeNumbers)
        return DERSequence(av)
    }

    companion object {

        private fun convertVector(numbers: Vector<Any>): ASN1EncodableVector {
            val av = ASN1EncodableVector()

            val it = numbers.elements()

            while (it.hasMoreElements()) {
                val o = it.nextElement()
                val di: ASN1Integer

                if (o is BigInteger) {
                    di = ASN1Integer(o)
                } else if (o is Int) {
                    di = ASN1Integer(o.toInt().toLong())
                } else {
                    throw IllegalArgumentException()
                }

                av.add(di)
            }
            return av
        }

        fun getInstance(
                `as`: Any?): NoticeReference? {
            if (`as` is NoticeReference) {
                return `as`
            } else if (`as` != null) {
                return NoticeReference(ASN1Sequence.getInstance(`as`))
            }

            return null
        }
    }
}
