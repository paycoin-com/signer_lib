package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.DERBitString

/**
 * The ReasonFlags object.
 *
 * ReasonFlags ::= BIT STRING {
 * unused                  (0),
 * keyCompromise           (1),
 * cACompromise            (2),
 * affiliationChanged      (3),
 * superseded              (4),
 * cessationOfOperation    (5),
 * certificateHold         (6),
 * privilegeWithdrawn      (7),
 * aACompromise            (8) }
 *
 */
class ReasonFlags : DERBitString {

    /**
     * @param reasons - the bitwise OR of the Key Reason flags giving the
     * * allowed uses for the key.
     */
    constructor(
            reasons: Int) : super(ASN1BitString.getBytes(reasons), ASN1BitString.getPadBits(reasons)) {
    }

    constructor(
            reasons: DERBitString) : super(reasons.bytes, reasons.getPadBits()) {
    }

    companion object {

        @Deprecated("use lower case version")
        val UNUSED = 1 shl 7

        @Deprecated("use lower case version")
        val KEY_COMPROMISE = 1 shl 6

        @Deprecated("use lower case version")
        val CA_COMPROMISE = 1 shl 5

        @Deprecated("use lower case version")
        val AFFILIATION_CHANGED = 1 shl 4

        @Deprecated("use lower case version")
        val SUPERSEDED = 1 shl 3

        @Deprecated("use lower case version")
        val CESSATION_OF_OPERATION = 1 shl 2

        @Deprecated("use lower case version")
        val CERTIFICATE_HOLD = 1 shl 1

        @Deprecated("use lower case version")
        val PRIVILEGE_WITHDRAWN = 1 shl 0

        @Deprecated("use lower case version")
        val AA_COMPROMISE = 1 shl 15

        val unused = 1 shl 7
        val keyCompromise = 1 shl 6
        val cACompromise = 1 shl 5
        val affiliationChanged = 1 shl 4
        val superseded = 1 shl 3
        val cessationOfOperation = 1 shl 2
        val certificateHold = 1 shl 1
        val privilegeWithdrawn = 1 shl 0
        val aACompromise = 1 shl 15
    }
}
