package org.bouncycastle.asn1.crmf

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.DERTaggedObject

class ProofOfPossession : ASN1Object, ASN1Choice {

    var type: Int = 0
        private set
    var `object`: ASN1Encodable? = null
        private set

    private constructor(tagged: ASN1TaggedObject) {
        type = tagged.tagNo
        when (type) {
            0 -> `object` = DERNull.INSTANCE
            1 -> `object` = POPOSigningKey.getInstance(tagged, false)
            2, 3 -> `object` = POPOPrivKey.getInstance(tagged, true)
            else -> throw IllegalArgumentException("unknown tag: " + type)
        }
    }

    /** Creates a ProofOfPossession with type raVerified.  */
    constructor() {
        type = TYPE_RA_VERIFIED
        `object` = DERNull.INSTANCE
    }

    /** Creates a ProofOfPossession for a signing key.  */
    constructor(poposk: POPOSigningKey) {
        type = TYPE_SIGNING_KEY
        `object` = poposk
    }

    /**
     * Creates a ProofOfPossession for key encipherment or agreement.
     * @param type one of TYPE_KEY_ENCIPHERMENT or TYPE_KEY_AGREEMENT
     */
    constructor(type: Int, privkey: POPOPrivKey) {
        this.type = type
        `object` = privkey
    }

    /**
     *
     * ProofOfPossession ::= CHOICE {
     * raVerified        [0] NULL,
     * -- used if the RA has already verified that the requester is in
     * -- possession of the private key
     * signature         [1] POPOSigningKey,
     * keyEncipherment   [2] POPOPrivKey,
     * keyAgreement      [3] POPOPrivKey }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return DERTaggedObject(false, type, `object`)
    }

    companion object {
        val TYPE_RA_VERIFIED = 0
        val TYPE_SIGNING_KEY = 1
        val TYPE_KEY_ENCIPHERMENT = 2
        val TYPE_KEY_AGREEMENT = 3

        fun getInstance(o: Any?): ProofOfPossession {
            if (o == null || o is ProofOfPossession) {
                return o as ProofOfPossession?
            }

            if (o is ASN1TaggedObject) {
                return ProofOfPossession(o as ASN1TaggedObject?)
            }

            throw IllegalArgumentException("Invalid object: " + o.javaClass.name)
        }
    }
}
