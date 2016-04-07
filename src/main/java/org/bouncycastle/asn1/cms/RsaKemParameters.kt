package org.bouncycastle.asn1.cms

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * RFC 5990 RSA KEM parameters class.
 *
 * RsaKemParameters ::= SEQUENCE {
 * keyDerivationFunction  KeyDerivationFunction,
 * keyLength              KeyLength
 * }

 * KeyDerivationFunction ::= AlgorithmIdentifier
 * KeyLength ::= INTEGER (1..MAX)
 *
 */
class RsaKemParameters : ASN1Object {
    val keyDerivationFunction: AlgorithmIdentifier
    val keyLength: BigInteger

    private constructor(sequence: ASN1Sequence) {
        if (sequence.size() != 2) {
            throw IllegalArgumentException("ASN.1 SEQUENCE should be of length 2")
        }
        this.keyDerivationFunction = AlgorithmIdentifier.getInstance(sequence.getObjectAt(0))
        this.keyLength = ASN1Integer.getInstance(sequence.getObjectAt(1)).value
    }

    /**
     * Base constructor.

     * @param keyDerivationFunction algorithm ID describing the key derivation function.
     * *
     * @param keyLength length of key to be derived (in bytes).
     */
    constructor(keyDerivationFunction: AlgorithmIdentifier, keyLength: Int) {
        this.keyDerivationFunction = keyDerivationFunction
        this.keyLength = BigInteger.valueOf(keyLength.toLong())
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(keyDerivationFunction)
        v.add(ASN1Integer(keyLength))

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                o: Any?): RsaKemParameters? {
            if (o is RsaKemParameters) {
                return o
            } else if (o != null) {
                return RsaKemParameters(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
