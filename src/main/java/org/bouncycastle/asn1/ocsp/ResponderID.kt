package org.bouncycastle.asn1.ocsp

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x500.X500Name

class ResponderID : ASN1Object, ASN1Choice {
    private var value: ASN1Encodable? = null

    constructor(
            value: ASN1OctetString) {
        this.value = value
    }

    constructor(
            value: X500Name) {
        this.value = value
    }

    val keyHash: ByteArray?
        get() {
            if (this.value is ASN1OctetString) {
                val octetString = this.value as ASN1OctetString?
                return octetString.getOctets()
            }

            return null
        }

    val name: X500Name?
        get() {
            if (this.value is ASN1OctetString) {
                return null
            }

            return X500Name.getInstance(value)
        }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * ResponderID ::= CHOICE {
     * byName          [1] Name,
     * byKey           [2] KeyHash }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        if (value is ASN1OctetString) {
            return DERTaggedObject(true, 2, value)
        }

        return DERTaggedObject(true, 1, value)
    }

    companion object {

        fun getInstance(
                obj: Any): ResponderID {
            if (obj is ResponderID) {
                return obj
            } else if (obj is DEROctetString) {
                return ResponderID(obj)
            } else if (obj is ASN1TaggedObject) {

                if (obj.tagNo == 1) {
                    return ResponderID(X500Name.getInstance(obj, true))
                } else {
                    return ResponderID(ASN1OctetString.getInstance(obj, true))
                }
            }

            return ResponderID(X500Name.getInstance(obj))
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ResponderID {
            return getInstance(obj.`object`) // must be explicitly tagged
        }
    }
}
