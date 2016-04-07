package org.bouncycastle.asn1.x9

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.util.Arrays

/**
 * class for describing an ECPoint as a DER object.
 */
class X9ECPoint : ASN1Object {
    private val encoding: ASN1OctetString

    private val c: ECCurve
    private var p: ECPoint? = null

    @JvmOverloads constructor(
            p: ECPoint,
            compressed: Boolean = false) {
        this.p = p.normalize()
        this.encoding = DEROctetString(p.getEncoded(compressed))
    }

    constructor(
            c: ECCurve,
            encoding: ByteArray) {
        this.c = c
        this.encoding = DEROctetString(Arrays.clone(encoding))
    }

    constructor(
            c: ECCurve,
            s: ASN1OctetString) : this(c, s.octets) {
    }

    val pointEncoding: ByteArray
        get() = Arrays.clone(encoding.octets)

    val point: ECPoint
        get() {
            if (p == null) {
                p = c.decodePoint(encoding.octets).normalize()
            }

            return p
        }

    val isPointCompressed: Boolean
        get() {
            val octets = encoding.octets
            return octets != null && octets.size > 0 && (octets[0].toInt() == 2 || octets[0].toInt() == 3)
        }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * ECPoint ::= OCTET STRING
     *
     *
     *
     * Octet string produced using ECPoint.getEncoded().
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return encoding
    }
}
