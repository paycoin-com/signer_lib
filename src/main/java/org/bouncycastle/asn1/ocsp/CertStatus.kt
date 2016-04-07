package org.bouncycastle.asn1.ocsp

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.DERTaggedObject

class CertStatus : ASN1Object, ASN1Choice {
    var tagNo: Int = 0
        private set
    var status: ASN1Encodable? = null
        private set

    /**
     * create a CertStatus object with a tag of zero.
     */
    constructor() {
        tagNo = 0
        status = DERNull.INSTANCE
    }

    constructor(
            info: RevokedInfo) {
        tagNo = 1
        status = info
    }

    constructor(
            tagNo: Int,
            value: ASN1Encodable) {
        this.tagNo = tagNo
        this.status = value
    }

    constructor(
            choice: ASN1TaggedObject) {
        this.tagNo = choice.tagNo

        when (choice.tagNo) {
            0 -> status = DERNull.INSTANCE
            1 -> status = RevokedInfo.getInstance(choice, false)
            2 -> status = DERNull.INSTANCE
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * CertStatus ::= CHOICE {
     * good        [0]     IMPLICIT NULL,
     * revoked     [1]     IMPLICIT RevokedInfo,
     * unknown     [2]     IMPLICIT UnknownInfo }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return DERTaggedObject(false, tagNo, status)
    }

    companion object {

        fun getInstance(
                obj: Any?): CertStatus {
            if (obj == null || obj is CertStatus) {
                return obj as CertStatus?
            } else if (obj is ASN1TaggedObject) {
                return CertStatus(obj as ASN1TaggedObject?)
            }

            throw IllegalArgumentException("unknown object in factory: " + obj.javaClass.name)
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): CertStatus {
            return getInstance(obj.`object`) // must be explicitly tagged
        }
    }
}
