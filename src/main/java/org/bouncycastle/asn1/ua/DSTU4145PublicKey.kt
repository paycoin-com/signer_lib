package org.bouncycastle.asn1.ua

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.math.ec.ECPoint

class DSTU4145PublicKey : ASN1Object {

    private var pubKey: ASN1OctetString? = null

    constructor(pubKey: ECPoint) {
        // We always use big-endian in parameter encoding
        this.pubKey = DEROctetString(DSTU4145PointEncoder.encodePoint(pubKey))
    }

    private constructor(ocStr: ASN1OctetString) {
        pubKey = ocStr
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return pubKey
    }

    companion object {

        fun getInstance(obj: Any?): DSTU4145PublicKey? {
            if (obj is DSTU4145PublicKey) {
                return obj
            }

            if (obj != null) {
                return DSTU4145PublicKey(ASN1OctetString.getInstance(obj))
            }

            return null
        }
    }

}
