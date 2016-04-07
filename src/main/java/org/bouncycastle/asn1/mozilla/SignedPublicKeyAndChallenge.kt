package org.bouncycastle.asn1.mozilla

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 *
 * SignedPublicKeyAndChallenge ::= SEQUENCE {
 * publicKeyAndChallenge PublicKeyAndChallenge,
 * signatureAlgorithm AlgorithmIdentifier,
 * signature BIT STRING
 * }

 *
 */
class SignedPublicKeyAndChallenge private constructor(private val pkacSeq: ASN1Sequence) : ASN1Object() {
    val publicKeyAndChallenge: PublicKeyAndChallenge

    init {
        publicKeyAndChallenge = PublicKeyAndChallenge.getInstance(pkacSeq.getObjectAt(0))
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return pkacSeq
    }

    val signatureAlgorithm: AlgorithmIdentifier
        get() = AlgorithmIdentifier.getInstance(pkacSeq.getObjectAt(1))

    val signature: DERBitString
        get() = DERBitString.getInstance(pkacSeq.getObjectAt(2))

    companion object {

        fun getInstance(obj: Any?): SignedPublicKeyAndChallenge? {
            if (obj is SignedPublicKeyAndChallenge) {
                return obj
            } else if (obj != null) {
                return SignedPublicKeyAndChallenge(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
