package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 * `UserNotice` class, used in
 * `CertificatePolicies` X509 extensions (in policy
 * qualifiers).
 *
 * UserNotice ::= SEQUENCE {
 * noticeRef        NoticeReference OPTIONAL,
 * explicitText     DisplayText OPTIONAL}

 *

 * @see PolicyQualifierId

 * @see PolicyInformation
 */
class UserNotice : ASN1Object {
    var noticeRef: NoticeReference? = null
        private set
    var explicitText: DisplayText? = null
        private set

    /**
     * Creates a new `UserNotice` instance.

     * @param noticeRef a `NoticeReference` value
     * *
     * @param explicitText a `DisplayText` value
     */
    constructor(
            noticeRef: NoticeReference,
            explicitText: DisplayText) {
        this.noticeRef = noticeRef
        this.explicitText = explicitText
    }

    /**
     * Creates a new `UserNotice` instance.

     * @param noticeRef a `NoticeReference` value
     * *
     * @param str the explicitText field as a String.
     */
    constructor(
            noticeRef: NoticeReference,
            str: String) : this(noticeRef, DisplayText(str)) {
    }

    /**
     * Creates a new `UserNotice` instance.
     *
     * Useful from reconstructing a `UserNotice` instance
     * from its encodable/encoded form.

     * @param as an `ASN1Sequence` value obtained from either
     * * calling @{link toASN1Primitive()} for a `UserNotice`
     * * instance or from parsing it from a DER-encoded stream.
     */
    private constructor(
            `as`: ASN1Sequence) {
        if (`as`.size() == 2) {
            noticeRef = NoticeReference.getInstance(`as`.getObjectAt(0))
            explicitText = DisplayText.getInstance(`as`.getObjectAt(1))
        } else if (`as`.size() == 1) {
            if (`as`.getObjectAt(0).toASN1Primitive() is ASN1Sequence) {
                noticeRef = NoticeReference.getInstance(`as`.getObjectAt(0))
            } else {
                explicitText = DisplayText.getInstance(`as`.getObjectAt(0))
            }
        } else {
            throw IllegalArgumentException("Bad sequence size: " + `as`.size())
        }
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val av = ASN1EncodableVector()

        if (noticeRef != null) {
            av.add(noticeRef)
        }

        if (explicitText != null) {
            av.add(explicitText)
        }

        return DERSequence(av)
    }

    companion object {

        fun getInstance(
                obj: Any?): UserNotice? {
            if (obj is UserNotice) {
                return obj
            }

            if (obj != null) {
                return UserNotice(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
