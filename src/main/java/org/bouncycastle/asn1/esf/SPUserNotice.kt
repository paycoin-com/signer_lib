package org.bouncycastle.asn1.esf

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.DisplayText
import org.bouncycastle.asn1.x509.NoticeReference

class SPUserNotice : ASN1Object {
    var noticeRef: NoticeReference? = null
        private set
    var explicitText: DisplayText? = null
        private set

    private constructor(
            seq: ASN1Sequence) {
        val e = seq.objects
        while (e.hasMoreElements()) {
            val `object` = e.nextElement() as ASN1Encodable
            if (`object` is DisplayText || `object` is ASN1String) {
                explicitText = DisplayText.getInstance(`object`)
            } else if (`object` is NoticeReference || `object` is ASN1Sequence) {
                noticeRef = NoticeReference.getInstance(`object`)
            } else {
                throw IllegalArgumentException("Invalid element in 'SPUserNotice': " + `object`.javaClass.name)
            }
        }
    }

    constructor(
            noticeRef: NoticeReference,
            explicitText: DisplayText) {
        this.noticeRef = noticeRef
        this.explicitText = explicitText
    }

    /**
     *
     * SPUserNotice ::= SEQUENCE {
     * noticeRef NoticeReference OPTIONAL,
     * explicitText DisplayText OPTIONAL }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (noticeRef != null) {
            v.add(noticeRef)
        }

        if (explicitText != null) {
            v.add(explicitText)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): SPUserNotice? {
            if (obj is SPUserNotice) {
                return obj
            } else if (obj != null) {
                return SPUserNotice(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
