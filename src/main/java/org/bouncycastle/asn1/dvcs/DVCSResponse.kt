package org.bouncycastle.asn1.dvcs

import java.io.IOException

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERTaggedObject

/**
 *
 * DVCSResponse ::= CHOICE
 * {
 * dvCertInfo         DVCSCertInfo ,
 * dvErrorNote        [0] DVCSErrorNotice
 * }
 *
 */

class DVCSResponse : ASN1Object, ASN1Choice {
    val certInfo: DVCSCertInfo?
    val errorNotice: DVCSErrorNotice

    constructor(dvCertInfo: DVCSCertInfo) {
        this.certInfo = dvCertInfo
    }

    constructor(dvErrorNote: DVCSErrorNotice) {
        this.errorNotice = dvErrorNote
    }

    override fun toASN1Primitive(): ASN1Primitive {
        if (certInfo != null) {
            return certInfo.toASN1Primitive()
        } else {
            return DERTaggedObject(false, 0, errorNotice)
        }
    }

    override fun toString(): String {
        if (certInfo != null) {
            return "DVCSResponse {\ndvCertInfo: " + certInfo.toString() + "}\n"
        } else {
            return "DVCSResponse {\ndvErrorNote: " + errorNotice.toString() + "}\n"
        }
    }

    companion object {

        fun getInstance(obj: Any?): DVCSResponse {
            if (obj == null || obj is DVCSResponse) {
                return obj as DVCSResponse?
            } else {
                if (obj is ByteArray) {
                    try {
                        return getInstance(ASN1Primitive.fromByteArray(obj as ByteArray?))
                    } catch (e: IOException) {
                        throw IllegalArgumentException("failed to construct sequence from byte[]: " + e.message)
                    }

                }
                if (obj is ASN1Sequence) {
                    val dvCertInfo = DVCSCertInfo.getInstance(obj)

                    return DVCSResponse(dvCertInfo)
                }
                if (obj is ASN1TaggedObject) {
                    val t = ASN1TaggedObject.getInstance(obj)
                    val dvErrorNote = DVCSErrorNotice.getInstance(t, false)

                    return DVCSResponse(dvErrorNote)
                }
            }

            throw IllegalArgumentException("Couldn't convert from object to DVCSResponse: " + obj.javaClass.name)
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DVCSResponse {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }
    }
}
