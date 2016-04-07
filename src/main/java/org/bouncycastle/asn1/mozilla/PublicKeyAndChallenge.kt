package org.bouncycastle.asn1.mozilla

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo

/**
 * This is designed to parse
 * the PublicKeyAndChallenge created by the KEYGEN tag included by
 * Mozilla based browsers.
 *
 * PublicKeyAndChallenge ::= SEQUENCE {
 * spki SubjectPublicKeyInfo,
 * challenge IA5STRING
 * }

 *
 */
class PublicKeyAndChallenge private constructor(private val pkacSeq: ASN1Sequence) : ASN1Object() {
    val subjectPublicKeyInfo: SubjectPublicKeyInfo
    val challenge: DERIA5String

    init {
        subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(pkacSeq.getObjectAt(0))
        challenge = DERIA5String.getInstance(pkacSeq.getObjectAt(1))
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return pkacSeq
    }

    companion object {

        fun getInstance(obj: Any?): PublicKeyAndChallenge? {
            if (obj is PublicKeyAndChallenge) {
                return obj
            } else if (obj != null) {
                return PublicKeyAndChallenge(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
