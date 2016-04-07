package org.bouncycastle.asn1.dvcs

import java.util.Date

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.cms.ContentInfo

/**
 *
 * DVCSTime ::= CHOICE  {
 * genTime                      GeneralizedTime,
 * timeStampToken               ContentInfo
 * }
 *
 */
class DVCSTime : ASN1Object, ASN1Choice {
    // selectors:

    val genTime: ASN1GeneralizedTime?
    val timeStampToken: ContentInfo?
    private val time: Date? = null

    // constructors:

    constructor(time: Date) : this(ASN1GeneralizedTime(time)) {
    }

    constructor(genTime: ASN1GeneralizedTime) {
        this.genTime = genTime
    }

    constructor(timeStampToken: ContentInfo) {
        this.timeStampToken = timeStampToken
    }

    override fun toASN1Primitive(): ASN1Primitive? {

        if (genTime != null) {
            return genTime
        }

        if (timeStampToken != null) {
            return timeStampToken.toASN1Primitive()
        }

        return null
    }

    override fun toString(): String? {
        if (genTime != null) {
            return genTime.toString()
        }
        if (timeStampToken != null) {
            return timeStampToken.toString()
        }
        return null
    }

    companion object {

        fun getInstance(obj: Any?): DVCSTime? {
            if (obj is DVCSTime) {
                return obj
            } else if (obj is ASN1GeneralizedTime) {
                return DVCSTime(ASN1GeneralizedTime.getInstance(obj))
            } else if (obj != null) {
                return DVCSTime(ContentInfo.getInstance(obj))
            }

            return null
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DVCSTime {
            return getInstance(obj.`object`) // must be explicitly tagged
        }
    }
}
