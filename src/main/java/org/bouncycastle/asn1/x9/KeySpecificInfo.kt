package org.bouncycastle.asn1.x9

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 * ASN.1 def for Diffie-Hellman key exchange KeySpecificInfo structure. See
 * RFC 2631, or X9.42, for further details.
 *
 * KeySpecificInfo ::= SEQUENCE {
 * algorithm OBJECT IDENTIFIER,
 * counter OCTET STRING SIZE (4..4)
 * }
 *
 */
class KeySpecificInfo : ASN1Object {
    /**
     * The object identifier for the CEK wrapping algorithm.

     * @return CEK wrapping algorithm OID.
     */
    var algorithm: ASN1ObjectIdentifier? = null
        private set
    /**
     * The initial counter value for key derivation.

     * @return initial counter value as a 4 byte octet string (big endian).
     */
    var counter: ASN1OctetString? = null
        private set

    /**
     * Base constructor.

     * @param algorithm  algorithm identifier for the CEK.
     * *
     * @param counter initial counter value for key derivation.
     */
    constructor(
            algorithm: ASN1ObjectIdentifier,
            counter: ASN1OctetString) {
        this.algorithm = algorithm
        this.counter = counter
    }

    private constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        algorithm = e.nextElement() as ASN1ObjectIdentifier
        counter = e.nextElement() as ASN1OctetString
    }

    /**
     * Return an ASN.1 primitive representation of this object.

     * @return a DERSequence containing the parameter values.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(algorithm)
        v.add(counter)

        return DERSequence(v)
    }

    companion object {

        /**
         * Return a KeySpecificInfo object from the passed in object.

         * @param obj an object for conversion or a byte[].
         * *
         * @return a KeySpecificInfo
         */
        fun getInstance(obj: Any?): KeySpecificInfo? {
            if (obj is KeySpecificInfo) {
                return obj
            } else if (obj != null) {
                return KeySpecificInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}