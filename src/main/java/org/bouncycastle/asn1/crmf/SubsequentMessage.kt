package org.bouncycastle.asn1.crmf

import org.bouncycastle.asn1.ASN1Integer

class SubsequentMessage private constructor(value: Int) : ASN1Integer(value) {
    companion object {
        val encrCert = SubsequentMessage(0)
        val challengeResp = SubsequentMessage(1)

        fun valueOf(value: Int): SubsequentMessage {
            if (value == 0) {
                return encrCert
            }
            if (value == 1) {
                return challengeResp
            }

            throw IllegalArgumentException("unknown value: " + value)
        }
    }
}
