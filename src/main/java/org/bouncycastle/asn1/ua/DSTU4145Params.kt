package org.bouncycastle.asn1.ua

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.util.Arrays

class DSTU4145Params : ASN1Object {


    var namedCurve: ASN1ObjectIdentifier? = null
        private set
    val ecBinary: DSTU4145ECBinary
    var dke = defaultDKE
        private set

    constructor(namedCurve: ASN1ObjectIdentifier) {
        this.namedCurve = namedCurve
    }

    constructor(namedCurve: ASN1ObjectIdentifier, dke: ByteArray) {
        this.namedCurve = namedCurve
        this.dke = Arrays.clone(dke)
    }

    constructor(ecbinary: DSTU4145ECBinary) {
        this.ecBinary = ecbinary
    }

    val isNamedCurve: Boolean
        get() = namedCurve != null

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (namedCurve != null) {
            v.add(namedCurve)
        } else {
            v.add(ecBinary)
        }

        if (!org.bouncycastle.util.Arrays.areEqual(dke, defaultDKE)) {
            v.add(DEROctetString(dke))
        }

        return DERSequence(v)
    }

    companion object {
        val defaultDKE = byteArrayOf(0xa9.toByte(), 0xd6.toByte(), 0xeb.toByte(), 0x45, 0xf1.toByte(), 0x3c, 0x70, 0x82.toByte(), 0x80.toByte(), 0xc4.toByte(), 0x96.toByte(), 0x7b, 0x23, 0x1f, 0x5e, 0xad.toByte(), 0xf6.toByte(), 0x58, 0xeb.toByte(), 0xa4.toByte(), 0xc0.toByte(), 0x37, 0x29, 0x1d, 0x38, 0xd9.toByte(), 0x6b, 0xf0.toByte(), 0x25, 0xca.toByte(), 0x4e, 0x17, 0xf8.toByte(), 0xe9.toByte(), 0x72, 0x0d, 0xc6.toByte(), 0x15, 0xb4.toByte(), 0x3a, 0x28, 0x97.toByte(), 0x5f, 0x0b, 0xc1.toByte(), 0xde.toByte(), 0xa3.toByte(), 0x64, 0x38, 0xb5.toByte(), 0x64, 0xea.toByte(), 0x2c, 0x17, 0x9f.toByte(), 0xd0.toByte(), 0x12, 0x3e, 0x6d, 0xb8.toByte(), 0xfa.toByte(), 0xc5.toByte(), 0x79, 0x04)

        fun getInstance(obj: Any?): DSTU4145Params {
            if (obj is DSTU4145Params) {
                return obj
            }

            if (obj != null) {
                val seq = ASN1Sequence.getInstance(obj)
                val params: DSTU4145Params

                if (seq.getObjectAt(0) is ASN1ObjectIdentifier) {
                    params = DSTU4145Params(ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)))
                } else {
                    params = DSTU4145Params(DSTU4145ECBinary.getInstance(seq.getObjectAt(0)))
                }

                if (seq.size() == 2) {
                    params.dke = ASN1OctetString.getInstance(seq.getObjectAt(1)).octets
                    if (params.dke.size != DSTU4145Params.defaultDKE.size) {
                        throw IllegalArgumentException("object parse error")
                    }
                }

                return params
            }

            throw IllegalArgumentException("object parse error")
        }
    }
}
