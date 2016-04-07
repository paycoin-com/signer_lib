package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

class Challenge : ASN1Object {
    var owf: AlgorithmIdentifier? = null
        private set
    private var witness: ASN1OctetString? = null
    private var challenge: ASN1OctetString? = null

    private constructor(seq: ASN1Sequence) {
        var index = 0

        if (seq.size() == 3) {
            owf = AlgorithmIdentifier.getInstance(seq.getObjectAt(index++))
        }

        witness = ASN1OctetString.getInstance(seq.getObjectAt(index++))
        challenge = ASN1OctetString.getInstance(seq.getObjectAt(index))
    }

    constructor(witness: ByteArray, challenge: ByteArray) : this(null, witness, challenge) {
    }

    constructor(owf: AlgorithmIdentifier?, witness: ByteArray, challenge: ByteArray) {
        this.owf = owf
        this.witness = DEROctetString(witness)
        this.challenge = DEROctetString(challenge)
    }

    fun getWitness(): ByteArray {
        return witness!!.octets
    }

    fun getChallenge(): ByteArray {
        return challenge!!.octets
    }

    /**
     *
     * Challenge ::= SEQUENCE {
     * owf                 AlgorithmIdentifier  OPTIONAL,

     * -- MUST be present in the first Challenge; MAY be omitted in
     * -- any subsequent Challenge in POPODecKeyChallContent (if
     * -- omitted, then the owf used in the immediately preceding
     * -- Challenge is to be used).

     * witness             OCTET STRING,
     * -- the result of applying the one-way function (owf) to a
     * -- randomly-generated INTEGER, A.  [Note that a different
     * -- INTEGER MUST be used for each Challenge.]
     * challenge           OCTET STRING
     * -- the encryption (under the public key for which the cert.
     * -- request is being made) of Rand, where Rand is specified as
     * --   Rand ::= SEQUENCE {
     * --      int      INTEGER,
     * --       - the randomly-generated INTEGER A (above)
     * --      sender   GeneralName
     * --       - the sender's name (as included in PKIHeader)
     * --   }
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        addOptional(v, owf)
        v.add(witness)
        v.add(challenge)

        return DERSequence(v)
    }

    private fun addOptional(v: ASN1EncodableVector, obj: ASN1Encodable?) {
        if (obj != null) {
            v.add(obj)
        }
    }

    companion object {

        fun getInstance(o: Any?): Challenge? {
            if (o is Challenge) {
                return o
            }

            if (o != null) {
                return Challenge(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
