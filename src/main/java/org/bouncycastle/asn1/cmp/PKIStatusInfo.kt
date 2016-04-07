package org.bouncycastle.asn1.cmp

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence

class PKIStatusInfo : ASN1Object {
    internal var status: ASN1Integer
    var statusString: PKIFreeText? = null
        internal set
    var failInfo: DERBitString? = null
        internal set

    private constructor(
            seq: ASN1Sequence) {
        this.status = ASN1Integer.getInstance(seq.getObjectAt(0))

        this.statusString = null
        this.failInfo = null

        if (seq.size() > 2) {
            this.statusString = PKIFreeText.getInstance(seq.getObjectAt(1))
            this.failInfo = DERBitString.getInstance(seq.getObjectAt(2))
        } else if (seq.size() > 1) {
            val obj = seq.getObjectAt(1)
            if (obj is DERBitString) {
                this.failInfo = DERBitString.getInstance(obj)
            } else {
                this.statusString = PKIFreeText.getInstance(obj)
            }
        }
    }

    /**
     * @param status
     */
    constructor(status: PKIStatus) {
        this.status = ASN1Integer.getInstance(status.toASN1Primitive())
    }

    /**

     * @param status
     * *
     * @param statusString
     */
    constructor(
            status: PKIStatus,
            statusString: PKIFreeText) {
        this.status = ASN1Integer.getInstance(status.toASN1Primitive())
        this.statusString = statusString
    }

    constructor(
            status: PKIStatus,
            statusString: PKIFreeText,
            failInfo: PKIFailureInfo) {
        this.status = ASN1Integer.getInstance(status.toASN1Primitive())
        this.statusString = statusString
        this.failInfo = failInfo
    }

    fun getStatus(): BigInteger {
        return status.value
    }

    /**
     *
     * PKIStatusInfo ::= SEQUENCE {
     * status        PKIStatus,                (INTEGER)
     * statusString  PKIFreeText     OPTIONAL,
     * failInfo      PKIFailureInfo  OPTIONAL  (BIT STRING)
     * }

     * PKIStatus:
     * granted                (0), -- you got exactly what you asked for
     * grantedWithMods        (1), -- you got something like what you asked for
     * rejection              (2), -- you don't get it, more information elsewhere in the message
     * waiting                (3), -- the request body part has not yet been processed, expect to hear more later
     * revocationWarning      (4), -- this message contains a warning that a revocation is imminent
     * revocationNotification (5), -- notification that a revocation has occurred
     * keyUpdateWarning       (6)  -- update already done for the oldCertId specified in CertReqMsg

     * PKIFailureInfo:
     * badAlg           (0), -- unrecognized or unsupported Algorithm Identifier
     * badMessageCheck  (1), -- integrity check failed (e.g., signature did not verify)
     * badRequest       (2), -- transaction not permitted or supported
     * badTime          (3), -- messageTime was not sufficiently close to the system time, as defined by local policy
     * badCertId        (4), -- no certificate could be found matching the provided criteria
     * badDataFormat    (5), -- the data submitted has the wrong format
     * wrongAuthority   (6), -- the authority indicated in the request is different from the one creating the response token
     * incorrectData    (7), -- the requester's data is incorrect (for notary services)
     * missingTimeStamp (8), -- when the timestamp is missing but should be there (by policy)
     * badPOP           (9)  -- the proof-of-possession failed

     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(status)

        if (statusString != null) {
            v.add(statusString)
        }

        if (failInfo != null) {
            v.add(failInfo)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): PKIStatusInfo {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): PKIStatusInfo? {
            if (obj is PKIStatusInfo) {
                return obj
            } else if (obj != null) {
                return PKIStatusInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
