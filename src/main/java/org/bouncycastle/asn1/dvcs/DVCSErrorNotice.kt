package org.bouncycastle.asn1.dvcs

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.cmp.PKIStatusInfo
import org.bouncycastle.asn1.x509.GeneralName

/**
 *
 * DVCSErrorNotice ::= SEQUENCE {
 * transactionStatus           PKIStatusInfo ,
 * transactionIdentifier       GeneralName OPTIONAL
 * }
 *
 */
class DVCSErrorNotice : ASN1Object {
    var transactionStatus: PKIStatusInfo? = null
        private set
    var transactionIdentifier: GeneralName? = null
        private set

    @JvmOverloads constructor(status: PKIStatusInfo, transactionIdentifier: GeneralName? = null) {
        this.transactionStatus = status
        this.transactionIdentifier = transactionIdentifier
    }

    private constructor(seq: ASN1Sequence) {
        this.transactionStatus = PKIStatusInfo.getInstance(seq.getObjectAt(0))
        if (seq.size() > 1) {
            this.transactionIdentifier = GeneralName.getInstance(seq.getObjectAt(1))
        }
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()
        v.add(transactionStatus)
        if (transactionIdentifier != null) {
            v.add(transactionIdentifier)
        }
        return DERSequence(v)
    }

    override fun toString(): String {
        return "DVCSErrorNotice {\n" +
                "transactionStatus: " + transactionStatus + "\n" +
                (if (transactionIdentifier != null) "transactionIdentifier: " + transactionIdentifier + "\n" else "") +
                "}\n"
    }

    companion object {

        fun getInstance(obj: Any?): DVCSErrorNotice? {
            if (obj is DVCSErrorNotice) {
                return obj
            } else if (obj != null) {
                return DVCSErrorNotice(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DVCSErrorNotice {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }
    }
}
