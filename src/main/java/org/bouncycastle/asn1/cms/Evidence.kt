package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERTaggedObject

/**
 * [RFC 5544](http://tools.ietf.org/html/rfc5544):
 * Binding Documents with Time-Stamps; Evidence object.
 *
 *
 *
 * Evidence ::= CHOICE {
 * tstEvidence    [0] TimeStampTokenEvidence,   -- see RFC 3161
 * ersEvidence    [1] EvidenceRecord,           -- see RFC 4998
 * otherEvidence  [2] OtherEvidence
 * }
 *
 */
class Evidence : ASN1Object, ASN1Choice {
    var tstEvidence: TimeStampTokenEvidence? = null
        private set

    constructor(tstEvidence: TimeStampTokenEvidence) {
        this.tstEvidence = tstEvidence
    }

    private constructor(tagged: ASN1TaggedObject) {
        if (tagged.tagNo == 0) {
            this.tstEvidence = TimeStampTokenEvidence.getInstance(tagged, false)
        }
    }

    override fun toASN1Primitive(): ASN1Primitive? {
        if (tstEvidence != null) {
            return DERTaggedObject(false, 0, tstEvidence)
        }

        return null
    }

    companion object {

        /**
         * Return an Evidence object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  [Evidence] object
         *  *  [ASN1TaggedObject][org.bouncycastle.asn1.ASN1TaggedObject.getInstance] input formats with Evidence data inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(obj: Any?): Evidence {
            if (obj == null || obj is Evidence) {
                return obj as Evidence?
            } else if (obj is ASN1TaggedObject) {
                return Evidence(ASN1TaggedObject.getInstance(obj))
            }

            throw IllegalArgumentException("unknown object in getInstance")
        }
    }
}
