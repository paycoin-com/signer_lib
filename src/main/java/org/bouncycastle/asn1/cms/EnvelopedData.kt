package org.bouncycastle.asn1.cms

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.BERSequence
import org.bouncycastle.asn1.DERTaggedObject

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-6.1) EnvelopedData object.
 *
 * EnvelopedData ::= SEQUENCE {
 * version CMSVersion,
 * originatorInfo [0] IMPLICIT OriginatorInfo OPTIONAL,
 * recipientInfos RecipientInfos,
 * encryptedContentInfo EncryptedContentInfo,
 * unprotectedAttrs [1] IMPLICIT UnprotectedAttributes OPTIONAL
 * }
 *
 */
class EnvelopedData : ASN1Object {
    var version: ASN1Integer? = null
        private set
    var originatorInfo: OriginatorInfo? = null
        private set
    var recipientInfos: ASN1Set? = null
        private set
    var encryptedContentInfo: EncryptedContentInfo? = null
        private set
    var unprotectedAttrs: ASN1Set? = null
        private set

    constructor(
            originatorInfo: OriginatorInfo,
            recipientInfos: ASN1Set,
            encryptedContentInfo: EncryptedContentInfo,
            unprotectedAttrs: ASN1Set) {
        version = ASN1Integer(calculateVersion(originatorInfo, recipientInfos, unprotectedAttrs).toLong())

        this.originatorInfo = originatorInfo
        this.recipientInfos = recipientInfos
        this.encryptedContentInfo = encryptedContentInfo
        this.unprotectedAttrs = unprotectedAttrs
    }

    constructor(
            originatorInfo: OriginatorInfo,
            recipientInfos: ASN1Set,
            encryptedContentInfo: EncryptedContentInfo,
            unprotectedAttrs: Attributes) {
        version = ASN1Integer(calculateVersion(originatorInfo, recipientInfos, ASN1Set.getInstance(unprotectedAttrs)).toLong())

        this.originatorInfo = originatorInfo
        this.recipientInfos = recipientInfos
        this.encryptedContentInfo = encryptedContentInfo
        this.unprotectedAttrs = ASN1Set.getInstance(unprotectedAttrs)
    }


    @Deprecated("use getInstance()")
    constructor(
            seq: ASN1Sequence) {
        var index = 0

        version = seq.getObjectAt(index++) as ASN1Integer

        var tmp: Any = seq.getObjectAt(index++)

        if (tmp is ASN1TaggedObject) {
            originatorInfo = OriginatorInfo.getInstance(tmp, false)
            tmp = seq.getObjectAt(index++)
        }

        recipientInfos = ASN1Set.getInstance(tmp)

        encryptedContentInfo = EncryptedContentInfo.getInstance(seq.getObjectAt(index++))

        if (seq.size() > index) {
            unprotectedAttrs = ASN1Set.getInstance(seq.getObjectAt(index) as ASN1TaggedObject, false)
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(version)

        if (originatorInfo != null) {
            v.add(DERTaggedObject(false, 0, originatorInfo))
        }

        v.add(recipientInfos)
        v.add(encryptedContentInfo)

        if (unprotectedAttrs != null) {
            v.add(DERTaggedObject(false, 1, unprotectedAttrs))
        }

        return BERSequence(v)
    }

    companion object {

        /**
         * Return an EnvelopedData object from a tagged object.

         * @param obj the tagged object holding the object we want.
         * *
         * @param explicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the object held by the
         * *          tagged object cannot be converted.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): EnvelopedData {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        /**
         * Return an EnvelopedData object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [EnvelopedData] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with EnvelopedData structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): EnvelopedData? {
            if (obj is EnvelopedData) {
                return obj
            }

            if (obj != null) {
                return EnvelopedData(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        fun calculateVersion(originatorInfo: OriginatorInfo?, recipientInfos: ASN1Set, unprotectedAttrs: ASN1Set?): Int {
            var version: Int

            if (originatorInfo != null || unprotectedAttrs != null) {
                version = 2
            } else {
                version = 0

                val e = recipientInfos.objects

                while (e.hasMoreElements()) {
                    val ri = RecipientInfo.getInstance(e.nextElement())

                    if (ri.version.value.toInt() != version) {
                        version = 2
                        break
                    }
                }
            }

            return version
        }
    }
}
