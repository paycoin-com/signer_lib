package org.bouncycastle.asn1.tsp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

class MessageImprint : ASN1Object {
    var hashAlgorithm: AlgorithmIdentifier
        internal set
    var hashedMessage: ByteArray
        internal set

    private constructor(
            seq: ASN1Sequence) {
        this.hashAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(0))
        this.hashedMessage = ASN1OctetString.getInstance(seq.getObjectAt(1)).octets
    }

    constructor(
            hashAlgorithm: AlgorithmIdentifier,
            hashedMessage: ByteArray) {
        this.hashAlgorithm = hashAlgorithm
        this.hashedMessage = hashedMessage
    }

    /**
     *
     * MessageImprint ::= SEQUENCE  {
     * hashAlgorithm                AlgorithmIdentifier,
     * hashedMessage                OCTET STRING  }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(hashAlgorithm)
        v.add(DEROctetString(hashedMessage))

        return DERSequence(v)
    }

    companion object {

        /**
         * @param o
         * *
         * @return a MessageImprint object.
         */
        fun getInstance(o: Any?): MessageImprint? {
            if (o is MessageImprint) {
                return o
            }

            if (o != null) {
                return MessageImprint(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
