package org.bouncycastle.asn1.eac

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERTaggedObject

class UnsignedInteger : ASN1Object {
    var tagNo: Int = 0
        private set
    var value: BigInteger? = null
        private set

    constructor(tagNo: Int, value: BigInteger) {
        this.tagNo = tagNo
        this.value = value
    }

    private constructor(obj: ASN1TaggedObject) {
        this.tagNo = obj.tagNo
        this.value = BigInteger(1, ASN1OctetString.getInstance(obj, false).octets)
    }

    private fun convertValue(): ByteArray {
        val v = value!!.toByteArray()

        if (v[0].toInt() == 0) {
            val tmp = ByteArray(v.size - 1)

            System.arraycopy(v, 1, tmp, 0, tmp.size)

            return tmp
        }

        return v
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return DERTaggedObject(false, tagNo, DEROctetString(convertValue()))
    }

    companion object {

        fun getInstance(obj: Any?): UnsignedInteger? {
            if (obj is UnsignedInteger) {
                return obj
            }
            if (obj != null) {
                return UnsignedInteger(ASN1TaggedObject.getInstance(obj))
            }

            return null
        }
    }
}
