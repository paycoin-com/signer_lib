package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.BERSequence
import org.bouncycastle.asn1.DERTaggedObject

/**
 * [RFC 5083](http://tools.ietf.org/html/rfc5083):

 * CMS AuthEnveloped Data object.
 *
 *
 * ASN.1:
 *
 * id-ct-authEnvelopedData OBJECT IDENTIFIER ::= { iso(1)
 * member-body(2) us(840) rsadsi(113549) pkcs(1) pkcs-9(9)
 * smime(16) ct(1) 23 }

 * AuthEnvelopedData ::= SEQUENCE {
 * version CMSVersion,
 * originatorInfo [0] IMPLICIT OriginatorInfo OPTIONAL,
 * recipientInfos RecipientInfos,
 * authEncryptedContentInfo EncryptedContentInfo,
 * authAttrs [1] IMPLICIT AuthAttributes OPTIONAL,
 * mac MessageAuthenticationCode,
 * unauthAttrs [2] IMPLICIT UnauthAttributes OPTIONAL }
 *
 */
class AuthEnvelopedData : ASN1Object {
    var version: ASN1Integer? = null
        private set
    var originatorInfo: OriginatorInfo? = null
        private set
    var recipientInfos: ASN1Set? = null
        private set
    var authEncryptedContentInfo: EncryptedContentInfo? = null
        private set
    var authAttrs: ASN1Set? = null
        private set
    var mac: ASN1OctetString? = null
        private set
    var unauthAttrs: ASN1Set? = null
        private set

    constructor(
            originatorInfo: OriginatorInfo,
            recipientInfos: ASN1Set,
            authEncryptedContentInfo: EncryptedContentInfo,
            authAttrs: ASN1Set?,
            mac: ASN1OctetString,
            unauthAttrs: ASN1Set) {
        // "It MUST be set to 0."
        this.version = ASN1Integer(0)

        this.originatorInfo = originatorInfo

        // "There MUST be at least one element in the collection."
        this.recipientInfos = recipientInfos
        if (this.recipientInfos!!.size() == 0) {
            throw IllegalArgumentException("AuthEnvelopedData requires at least 1 RecipientInfo")
        }

        this.authEncryptedContentInfo = authEncryptedContentInfo

        // "The authAttrs MUST be present if the content type carried in
        // EncryptedContentInfo is not id-data."
        this.authAttrs = authAttrs
        if (authEncryptedContentInfo.contentType != CMSObjectIdentifiers.data) {
            if (authAttrs == null || authAttrs.size() == 0) {
                throw IllegalArgumentException("authAttrs must be present with non-data content")
            }
        }

        this.mac = mac

        this.unauthAttrs = unauthAttrs
    }

    /**
     * Constructs AuthEnvelopedData by parsing supplied ASN1Sequence
     *
     *
     * @param seq An ASN1Sequence with AuthEnvelopedData
     */
    private constructor(
            seq: ASN1Sequence) {
        var index = 0

        // "It MUST be set to 0."
        var tmp = seq.getObjectAt(index++).toASN1Primitive()
        version = tmp as ASN1Integer
        if (this.version!!.value.toInt() != 0) {
            throw IllegalArgumentException("AuthEnvelopedData version number must be 0")
        }

        tmp = seq.getObjectAt(index++).toASN1Primitive()
        if (tmp is ASN1TaggedObject) {
            originatorInfo = OriginatorInfo.getInstance(tmp, false)
            tmp = seq.getObjectAt(index++).toASN1Primitive()
        }

        // "There MUST be at least one element in the collection."
        recipientInfos = ASN1Set.getInstance(tmp)
        if (this.recipientInfos!!.size() == 0) {
            throw IllegalArgumentException("AuthEnvelopedData requires at least 1 RecipientInfo")
        }

        tmp = seq.getObjectAt(index++).toASN1Primitive()
        authEncryptedContentInfo = EncryptedContentInfo.getInstance(tmp)

        tmp = seq.getObjectAt(index++).toASN1Primitive()
        if (tmp is ASN1TaggedObject) {
            authAttrs = ASN1Set.getInstance(tmp, false)
            tmp = seq.getObjectAt(index++).toASN1Primitive()
        } else {
            // "The authAttrs MUST be present if the content type carried in
            // EncryptedContentInfo is not id-data."
            if (authEncryptedContentInfo!!.contentType != CMSObjectIdentifiers.data) {
                if (authAttrs == null || authAttrs!!.size() == 0) {
                    throw IllegalArgumentException("authAttrs must be present with non-data content")
                }
            }
        }

        mac = ASN1OctetString.getInstance(tmp)

        if (seq.size() > index) {
            tmp = seq.getObjectAt(index).toASN1Primitive()
            unauthAttrs = ASN1Set.getInstance(tmp as ASN1TaggedObject, false)
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
        v.add(authEncryptedContentInfo)

        // "authAttrs optionally contains the authenticated attributes."
        if (authAttrs != null) {
            // "AuthAttributes MUST be DER encoded, even if the rest of the
            // AuthEnvelopedData structure is BER encoded."
            v.add(DERTaggedObject(false, 1, authAttrs))
        }

        v.add(mac)

        // "unauthAttrs optionally contains the unauthenticated attributes."
        if (unauthAttrs != null) {
            v.add(DERTaggedObject(false, 2, unauthAttrs))
        }

        return BERSequence(v)
    }

    companion object {

        /**
         * Return an AuthEnvelopedData object from a tagged object.
         *
         *
         * Accepted inputs:
         *
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats
         *


         * @param obj      the tagged object holding the object we want.
         * *
         * @param explicit true if the object is meant to be explicitly
         * *                 tagged false otherwise.
         * *
         * @return a reference that can be assigned to AuthEnvelopedData (may be null)
         * *
         * @throws IllegalArgumentException if the object held by the
         * *                                  tagged object cannot be converted.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): AuthEnvelopedData {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        /**
         * Return an AuthEnvelopedData object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [AuthEnvelopedData] object
         *  *  [org.bouncycastle.asn1.ASN1Sequence][ASN1Sequence] input formats with AuthEnvelopedData structure inside
         *

         * @param obj The object we want converted.
         * *
         * @return a reference that can be assigned to AuthEnvelopedData (may be null)
         * *
         * @throws IllegalArgumentException if the object cannot be converted, or was null.
         */
        fun getInstance(
                obj: Any?): AuthEnvelopedData {
            if (obj == null || obj is AuthEnvelopedData) {
                return obj as AuthEnvelopedData?
            }

            if (obj is ASN1Sequence) {
                return AuthEnvelopedData(obj as ASN1Sequence?)
            }

            throw IllegalArgumentException("Invalid AuthEnvelopedData: " + obj.javaClass.name)
        }
    }
}
