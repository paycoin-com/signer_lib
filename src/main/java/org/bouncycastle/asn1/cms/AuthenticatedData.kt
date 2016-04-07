package org.bouncycastle.asn1.cms

import java.util.Enumeration

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
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-9.1) section 9.1:
 * The AuthenticatedData carries AuthAttributes and other data
 * which define what really is being signed.
 *
 * AuthenticatedData ::= SEQUENCE {
 * version CMSVersion,
 * originatorInfo [0] IMPLICIT OriginatorInfo OPTIONAL,
 * recipientInfos RecipientInfos,
 * macAlgorithm MessageAuthenticationCodeAlgorithm,
 * digestAlgorithm [1] DigestAlgorithmIdentifier OPTIONAL,
 * encapContentInfo EncapsulatedContentInfo,
 * authAttrs [2] IMPLICIT AuthAttributes OPTIONAL,
 * mac MessageAuthenticationCode,
 * unauthAttrs [3] IMPLICIT UnauthAttributes OPTIONAL }

 * AuthAttributes ::= SET SIZE (1..MAX) OF Attribute

 * UnauthAttributes ::= SET SIZE (1..MAX) OF Attribute

 * MessageAuthenticationCode ::= OCTET STRING
 *
 */
class AuthenticatedData : ASN1Object {
    var version: ASN1Integer? = null
        private set
    var originatorInfo: OriginatorInfo? = null
        private set
    var recipientInfos: ASN1Set? = null
        private set
    var macAlgorithm: AlgorithmIdentifier? = null
        private set
    var digestAlgorithm: AlgorithmIdentifier? = null
        private set
    var encapsulatedContentInfo: ContentInfo? = null
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
            macAlgorithm: AlgorithmIdentifier,
            digestAlgorithm: AlgorithmIdentifier?,
            encapsulatedContent: ContentInfo,
            authAttrs: ASN1Set?,
            mac: ASN1OctetString,
            unauthAttrs: ASN1Set) {
        if (digestAlgorithm != null || authAttrs != null) {
            if (digestAlgorithm == null || authAttrs == null) {
                throw IllegalArgumentException("digestAlgorithm and authAttrs must be set together")
            }
        }

        version = ASN1Integer(calculateVersion(originatorInfo).toLong())

        this.originatorInfo = originatorInfo
        this.macAlgorithm = macAlgorithm
        this.digestAlgorithm = digestAlgorithm
        this.recipientInfos = recipientInfos
        this.encapsulatedContentInfo = encapsulatedContent
        this.authAttrs = authAttrs
        this.mac = mac
        this.unauthAttrs = unauthAttrs
    }

    private constructor(
            seq: ASN1Sequence) {
        var index = 0

        version = seq.getObjectAt(index++) as ASN1Integer

        var tmp: Any = seq.getObjectAt(index++)

        if (tmp is ASN1TaggedObject) {
            originatorInfo = OriginatorInfo.getInstance(tmp, false)
            tmp = seq.getObjectAt(index++)
        }

        recipientInfos = ASN1Set.getInstance(tmp)
        macAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(index++))

        tmp = seq.getObjectAt(index++)

        if (tmp is ASN1TaggedObject) {
            digestAlgorithm = AlgorithmIdentifier.getInstance(tmp, false)
            tmp = seq.getObjectAt(index++)
        }

        encapsulatedContentInfo = ContentInfo.getInstance(tmp)

        tmp = seq.getObjectAt(index++)

        if (tmp is ASN1TaggedObject) {
            authAttrs = ASN1Set.getInstance(tmp, false)
            tmp = seq.getObjectAt(index++)
        }

        mac = ASN1OctetString.getInstance(tmp)

        if (seq.size() > index) {
            unauthAttrs = ASN1Set.getInstance(seq.getObjectAt(index) as ASN1TaggedObject, false)
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
        v.add(macAlgorithm)

        if (digestAlgorithm != null) {
            v.add(DERTaggedObject(false, 1, digestAlgorithm))
        }

        v.add(encapsulatedContentInfo)

        if (authAttrs != null) {
            v.add(DERTaggedObject(false, 2, authAttrs))
        }

        v.add(mac)

        if (unauthAttrs != null) {
            v.add(DERTaggedObject(false, 3, unauthAttrs))
        }

        return BERSequence(v)
    }

    companion object {

        /**
         * Return an AuthenticatedData object from a tagged object.

         * @param obj      the tagged object holding the object we want.
         * *
         * @param explicit true if the object is meant to be explicitly
         * *                 tagged false otherwise.
         * *
         * @return a reference that can be assigned to AuthenticatedData (may be null)
         * *
         * @throws IllegalArgumentException if the object held by the
         * *                                  tagged object cannot be converted.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): AuthenticatedData {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        /**
         * Return an AuthenticatedData object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [AuthenticatedData] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with AuthenticatedData structure inside
         *

         * @param obj the object we want converted.
         * *
         * @return a reference that can be assigned to AuthenticatedData (may be null)
         * *
         * @throws IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): AuthenticatedData? {
            if (obj is AuthenticatedData) {
                return obj
            } else if (obj != null) {
                return AuthenticatedData(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        fun calculateVersion(origInfo: OriginatorInfo?): Int {
            if (origInfo == null) {
                return 0
            } else {
                var ver = 0

                run {
                    val e = origInfo.certificates.objects
                    while (e.hasMoreElements()) {
                        val obj = e.nextElement()

                        if (obj is ASN1TaggedObject) {

                            if (obj.tagNo == 2) {
                                ver = 1
                            } else if (obj.tagNo == 3) {
                                ver = 3
                                break
                            }
                        }
                    }
                }

                if (origInfo.crLs != null) {
                    val e = origInfo.crLs.objects
                    while (e.hasMoreElements()) {
                        val obj = e.nextElement()

                        if (obj is ASN1TaggedObject) {

                            if (obj.tagNo == 1) {
                                ver = 3
                                break
                            }
                        }
                    }
                }

                return ver
            }
        }
    }
}
