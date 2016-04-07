package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERTaggedObject

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-6.2):
 * Content encryption key delivery mechanisms.
 *
 *
 *
 * RecipientInfo ::= CHOICE {
 * ktri      KeyTransRecipientInfo,
 * kari  [1] KeyAgreeRecipientInfo,
 * kekri [2] KEKRecipientInfo,
 * pwri  [3] PasswordRecipientInfo,
 * ori   [4] OtherRecipientInfo }
 *
 */
class RecipientInfo : ASN1Object, ASN1Choice {
    internal var info: ASN1Encodable

    constructor(
            info: KeyTransRecipientInfo) {
        this.info = info
    }

    constructor(
            info: KeyAgreeRecipientInfo) {
        this.info = DERTaggedObject(false, 1, info)
    }

    constructor(
            info: KEKRecipientInfo) {
        this.info = DERTaggedObject(false, 2, info)
    }

    constructor(
            info: PasswordRecipientInfo) {
        this.info = DERTaggedObject(false, 3, info)
    }

    constructor(
            info: OtherRecipientInfo) {
        this.info = DERTaggedObject(false, 4, info)
    }

    constructor(
            info: ASN1Primitive) {
        this.info = info
    }

    // no syntax version for OtherRecipientInfo
    val version: ASN1Integer
        get() {
            if (info is ASN1TaggedObject) {
                val o = info as ASN1TaggedObject

                when (o.tagNo) {
                    1 -> return KeyAgreeRecipientInfo.getInstance(o, false).version
                    2 -> return getKEKInfo(o).version
                    3 -> return PasswordRecipientInfo.getInstance(o, false).version
                    4 -> return ASN1Integer(0)
                    else -> throw IllegalStateException("unknown tag")
                }
            }

            return KeyTransRecipientInfo.getInstance(info)!!.version
        }

    val isTagged: Boolean
        get() = info is ASN1TaggedObject

    fun getInfo(): ASN1Encodable {
        if (info is ASN1TaggedObject) {
            val o = info as ASN1TaggedObject

            when (o.tagNo) {
                1 -> return KeyAgreeRecipientInfo.getInstance(o, false)
                2 -> return getKEKInfo(o)
                3 -> return PasswordRecipientInfo.getInstance(o, false)
                4 -> return OtherRecipientInfo.getInstance(o, false)
                else -> throw IllegalStateException("unknown tag")
            }
        }

        return KeyTransRecipientInfo.getInstance(info)
    }

    private fun getKEKInfo(o: ASN1TaggedObject): KEKRecipientInfo {
        if (o.isExplicit) {
            // compatibilty with erroneous version
            return KEKRecipientInfo.getInstance(o, true)
        } else {
            return KEKRecipientInfo.getInstance(o, false)
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return info.toASN1Primitive()
    }

    companion object {

        /**
         * Return a RecipientInfo object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [RecipientInfo] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with RecipientInfo structure inside
         *  *  [ASN1TaggedObject][org.bouncycastle.asn1.ASN1TaggedObject.getInstance] input formats with RecipientInfo structure inside
         *

         * @param o the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                o: Any?): RecipientInfo {
            if (o == null || o is RecipientInfo) {
                return o as RecipientInfo?
            } else if (o is ASN1Sequence) {
                return RecipientInfo(o as ASN1Sequence?)
            } else if (o is ASN1TaggedObject) {
                return RecipientInfo(o as ASN1TaggedObject?)
            }

            throw IllegalArgumentException("unknown object in factory: " + o.javaClass.name)
        }
    }
}
