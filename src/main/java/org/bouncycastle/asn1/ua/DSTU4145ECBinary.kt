package org.bouncycastle.asn1.ua

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.math.ec.ECAlgorithms
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.field.PolynomialExtensionField
import org.bouncycastle.util.Arrays

class DSTU4145ECBinary : ASN1Object {
    internal var version = BigInteger.valueOf(0)

    var field: DSTU4145BinaryField
        internal set
    internal var a: ASN1Integer
    internal var b: ASN1OctetString
    internal var n: ASN1Integer
    internal var bp: ASN1OctetString

    constructor(params: ECDomainParameters) {
        val curve = params.curve
        if (!ECAlgorithms.isF2mCurve(curve)) {
            throw IllegalArgumentException("only binary domain is possible")
        }

        // We always use big-endian in parameter encoding

        val field = curve.field as PolynomialExtensionField
        val exponents = field.minimalPolynomial.exponentsPresent
        if (exponents.size == 3) {
            this.field = DSTU4145BinaryField(exponents[2], exponents[1])
        } else if (exponents.size == 5) {
            this.field = DSTU4145BinaryField(exponents[4], exponents[1], exponents[2], exponents[3])
        } else {
            throw IllegalArgumentException("curve must have a trinomial or pentanomial basis")
        }

        a = ASN1Integer(curve.a.toBigInteger())
        b = DEROctetString(curve.b.encoded)
        n = ASN1Integer(params.n)
        bp = DEROctetString(DSTU4145PointEncoder.encodePoint(params.g))
    }

    private constructor(seq: ASN1Sequence) {
        var index = 0

        if (seq.getObjectAt(index) is ASN1TaggedObject) {
            val taggedVersion = seq.getObjectAt(index) as ASN1TaggedObject
            if (taggedVersion.isExplicit && 0 == taggedVersion.tagNo) {
                version = ASN1Integer.getInstance(taggedVersion.loadedObject).value
                index++
            } else {
                throw IllegalArgumentException("object parse error")
            }
        }
        field = DSTU4145BinaryField.getInstance(seq.getObjectAt(index))
        index++
        a = ASN1Integer.getInstance(seq.getObjectAt(index))
        index++
        b = ASN1OctetString.getInstance(seq.getObjectAt(index))
        index++
        n = ASN1Integer.getInstance(seq.getObjectAt(index))
        index++
        bp = ASN1OctetString.getInstance(seq.getObjectAt(index))
    }

    fun getA(): BigInteger {
        return a.value
    }

    fun getB(): ByteArray {
        return Arrays.clone(b.octets)
    }

    fun getN(): BigInteger {
        return n.value
    }

    val g: ByteArray
        get() = Arrays.clone(bp.octets)

    /**
     * ECBinary  ::= SEQUENCE {
     * version          [0] EXPLICIT INTEGER    DEFAULT 0,
     * f     BinaryField,
     * a    INTEGER (0..1),
     * b    OCTET STRING,
     * n    INTEGER,
     * bp    OCTET STRING}
     */
    override fun toASN1Primitive(): ASN1Primitive {

        val v = ASN1EncodableVector()

        if (0 != version.compareTo(BigInteger.valueOf(0))) {
            v.add(DERTaggedObject(true, 0, ASN1Integer(version)))
        }
        v.add(field)
        v.add(a)
        v.add(b)
        v.add(n)
        v.add(bp)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(obj: Any?): DSTU4145ECBinary? {
            if (obj is DSTU4145ECBinary) {
                return obj
            }

            if (obj != null) {
                return DSTU4145ECBinary(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }

}
