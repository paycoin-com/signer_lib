package org.bouncycastle.asn1.dvcs

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.GeneralName

/**
 *
 * DVCSRequest ::= SEQUENCE  {
 * requestInformation         DVCSRequestInformation,
 * data                       Data,
 * transactionIdentifier      GeneralName OPTIONAL
 * }
 *
 */

class DVCSRequest : ASN1Object {

    var requestInformation: DVCSRequestInformation? = null
        private set
    var data: Data? = null
        private set
    var transactionIdentifier: GeneralName? = null
        private set

    @JvmOverloads constructor(requestInformation: DVCSRequestInformation, data: Data, transactionIdentifier: GeneralName? = null) {
        this.requestInformation = requestInformation
        this.data = data
        this.transactionIdentifier = transactionIdentifier
    }

    private constructor(seq: ASN1Sequence) {
        requestInformation = DVCSRequestInformation.getInstance(seq.getObjectAt(0))
        data = Data.getInstance(seq.getObjectAt(1))
        if (seq.size() > 2) {
            transactionIdentifier = GeneralName.getInstance(seq.getObjectAt(2))
        }
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()
        v.add(requestInformation)
        v.add(data)
        if (transactionIdentifier != null) {
            v.add(transactionIdentifier)
        }
        return DERSequence(v)
    }

    override fun toString(): String {
        return "DVCSRequest {\n" +
                "requestInformation: " + requestInformation + "\n" +
                "data: " + data + "\n" +
                (if (transactionIdentifier != null) "transactionIdentifier: " + transactionIdentifier + "\n" else "") +
                "}\n"
    }

    companion object {

        fun getInstance(obj: Any?): DVCSRequest? {
            if (obj is DVCSRequest) {
                return obj
            } else if (obj != null) {
                return DVCSRequest(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DVCSRequest {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }
    }
}
