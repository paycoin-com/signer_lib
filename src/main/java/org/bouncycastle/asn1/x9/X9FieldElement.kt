package org.bouncycastle.asn1.x9

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.math.ec.ECFieldElement

/**
 * class for processing an FieldElement as a DER object.
 */
class X9FieldElement(f: ECFieldElement) : ASN1Object() {
    var value: ECFieldElement
        protected set

    init {
        this.value = f
    }

    constructor(p: BigInteger, s: ASN1OctetString) : this(ECFieldElement.Fp(p, BigInteger(1, s.octets))) {
    }

    constructor(m: Int, k1: Int, k2: Int, k3: Int, s: ASN1OctetString) : this(ECFieldElement.F2m(m, k1, k2, k3, BigInteger(1, s.octets))) {
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * FieldElement ::= OCTET STRING
     *
     *
     *
     *
     *  1.  if *q* is an odd prime then the field element is
     * processed as an Integer and converted to an octet string
     * according to x 9.62 4.3.1.
     *  1.  if *q* is 2m then the bit string
     * contained in the field element is converted into an octet
     * string with the same ordering padded at the front if necessary.
     *
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val byteCount = converter.getByteLength(value)
        val paddedBigInteger = converter.integerToBytes(value.toBigInteger(), byteCount)

        return DEROctetString(paddedBigInteger)
    }

    companion object {

        private val converter = X9IntegerConverter()
    }
}
