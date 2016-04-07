package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

class V2Form : ASN1Object {
    var issuerName: GeneralNames? = null
        internal set
    var baseCertificateID: IssuerSerial? = null
        internal set
    var objectDigestInfo: ObjectDigestInfo? = null
        internal set

    constructor(
            issuerName: GeneralNames,
            objectDigestInfo: ObjectDigestInfo) : this(issuerName, null, objectDigestInfo) {
    }

    @JvmOverloads constructor(
            issuerName: GeneralNames,
            baseCertificateID: IssuerSerial? = null,
            objectDigestInfo: ObjectDigestInfo? = null) {
        this.issuerName = issuerName
        this.baseCertificateID = baseCertificateID
        this.objectDigestInfo = objectDigestInfo
    }


    @Deprecated("use getInstance().")
    constructor(
            seq: ASN1Sequence) {
        if (seq.size() > 3) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        var index = 0

        if (seq.getObjectAt(0) !is ASN1TaggedObject) {
            index++
            this.issuerName = GeneralNames.getInstance(seq.getObjectAt(0))
        }

        for (i in index..seq.size() - 1) {
            val o = ASN1TaggedObject.getInstance(seq.getObjectAt(i))
            if (o.tagNo == 0) {
                baseCertificateID = IssuerSerial.getInstance(o, false)
            } else if (o.tagNo == 1) {
                objectDigestInfo = ObjectDigestInfo.getInstance(o, false)
            } else {
                throw IllegalArgumentException("Bad tag number: " + o.tagNo)
            }
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * V2Form ::= SEQUENCE {
     * issuerName            GeneralNames  OPTIONAL,
     * baseCertificateID     [0] IssuerSerial  OPTIONAL,
     * objectDigestInfo      [1] ObjectDigestInfo  OPTIONAL
     * -- issuerName MUST be present in this profile
     * -- baseCertificateID and objectDigestInfo MUST NOT
     * -- be present in this profile
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (issuerName != null) {
            v.add(issuerName)
        }

        if (baseCertificateID != null) {
            v.add(DERTaggedObject(false, 0, baseCertificateID))
        }

        if (objectDigestInfo != null) {
            v.add(DERTaggedObject(false, 1, objectDigestInfo))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): V2Form {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): V2Form? {
            if (obj is V2Form) {
                return obj
            } else if (obj != null) {
                return V2Form(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
