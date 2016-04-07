package org.bouncycastle.asn1.crmf

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

class POPOSigningKey : ASN1Object {
    var poposkInput: POPOSigningKeyInput? = null
        private set
    var algorithmIdentifier: AlgorithmIdentifier? = null
        private set
    var signature: DERBitString? = null
        private set

    private constructor(seq: ASN1Sequence) {
        var index = 0

        if (seq.getObjectAt(index) is ASN1TaggedObject) {
            val tagObj = seq.getObjectAt(index++) as ASN1TaggedObject
            if (tagObj.tagNo != 0) {
                throw IllegalArgumentException(
                        "Unknown POPOSigningKeyInput tag: " + tagObj.tagNo)
            }
            poposkInput = POPOSigningKeyInput.getInstance(tagObj.`object`)
        }
        algorithmIdentifier = AlgorithmIdentifier.getInstance(seq.getObjectAt(index++))
        signature = DERBitString.getInstance(seq.getObjectAt(index))
    }

    /**
     * Creates a new Proof of Possession object for a signing key.

     * @param poposkIn  the POPOSigningKeyInput structure, or null if the
     * *                  CertTemplate includes both subject and publicKey values.
     * *
     * @param aid       the AlgorithmIdentifier used to sign the proof of possession.
     * *
     * @param signature a signature over the DER-encoded value of poposkIn,
     * *                  or the DER-encoded value of certReq if poposkIn is null.
     */
    constructor(
            poposkIn: POPOSigningKeyInput,
            aid: AlgorithmIdentifier,
            signature: DERBitString) {
        this.poposkInput = poposkIn
        this.algorithmIdentifier = aid
        this.signature = signature
    }

    /**
     *
     * POPOSigningKey ::= SEQUENCE {
     * poposkInput           [0] POPOSigningKeyInput OPTIONAL,
     * algorithmIdentifier   AlgorithmIdentifier,
     * signature             BIT STRING }
     * -- The signature (using "algorithmIdentifier") is on the
     * -- DER-encoded value of poposkInput.  NOTE: If the CertReqMsg
     * -- certReq CertTemplate contains the subject and publicKey values,
     * -- then poposkInput MUST be omitted and the signature MUST be
     * -- computed on the DER-encoded value of CertReqMsg certReq.  If
     * -- the CertReqMsg certReq CertTemplate does not contain the public
     * -- key and subject values, then poposkInput MUST be present and
     * -- MUST be signed.  This strategy ensures that the public key is
     * -- not present in both the poposkInput and CertReqMsg certReq
     * -- CertTemplate fields.
     *

     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (poposkInput != null) {
            v.add(DERTaggedObject(false, 0, poposkInput))
        }

        v.add(algorithmIdentifier)
        v.add(signature)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): POPOSigningKey? {
            if (o is POPOSigningKey) {
                return o
            }

            if (o != null) {
                return POPOSigningKey(ASN1Sequence.getInstance(o))
            }

            return null
        }

        fun getInstance(obj: ASN1TaggedObject, explicit: Boolean): POPOSigningKey {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }
    }
}
