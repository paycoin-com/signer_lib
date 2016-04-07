package org.bouncycastle.asn1

/**
 * Supported encoding formats.
 */
interface ASN1Encoding {
    companion object {
        /**
         * DER - distinguished encoding rules.
         */
        val DER = "DER"

        /**
         * DL - definite length encoding.
         */
        val DL = "DL"

        /**
         * BER - basic encoding rules.
         */
        val BER = "BER"
    }
}
