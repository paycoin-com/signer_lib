package org.bouncycastle.asn1.x509

import java.math.BigInteger
import java.util.Hashtable

import org.bouncycastle.asn1.ASN1Enumerated
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.util.Integers

/**
 * The CRLReason enumeration.
 *
 * CRLReason ::= ENUMERATED {
 * unspecified             (0),
 * keyCompromise           (1),
 * cACompromise            (2),
 * affiliationChanged      (3),
 * superseded              (4),
 * cessationOfOperation    (5),
 * certificateHold         (6),
 * removeFromCRL           (8),
 * privilegeWithdrawn      (9),
 * aACompromise           (10)
 * }
 *
 */
class CRLReason private constructor(
        reason: Int) : ASN1Object() {

    private val value: ASN1Enumerated

    init {
        value = ASN1Enumerated(reason)
    }

    override fun toString(): String {
        val str: String
        val reason = getValue().toInt()
        if (reason < 0 || reason > 10) {
            str = "invalid"
        } else {
            str = reasonString[reason]
        }
        return "CRLReason: " + str
    }

    fun getValue(): BigInteger {
        return value.value
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return value
    }

    companion object {

        @Deprecated("use lower case version")
        val UNSPECIFIED = 0

        @Deprecated("use lower case version")
        val KEY_COMPROMISE = 1

        @Deprecated("use lower case version")
        val CA_COMPROMISE = 2

        @Deprecated("use lower case version")
        val AFFILIATION_CHANGED = 3

        @Deprecated("use lower case version")
        val SUPERSEDED = 4

        @Deprecated("use lower case version")
        val CESSATION_OF_OPERATION = 5

        @Deprecated("use lower case version")
        val CERTIFICATE_HOLD = 6

        @Deprecated("use lower case version")
        val REMOVE_FROM_CRL = 8

        @Deprecated("use lower case version")
        val PRIVILEGE_WITHDRAWN = 9

        @Deprecated("use lower case version")
        val AA_COMPROMISE = 10

        val unspecified = 0
        val keyCompromise = 1
        val cACompromise = 2
        val affiliationChanged = 3
        val superseded = 4
        val cessationOfOperation = 5
        val certificateHold = 6
        // 7 -> unknown
        val removeFromCRL = 8
        val privilegeWithdrawn = 9
        val aACompromise = 10

        private val reasonString = arrayOf("unspecified", "keyCompromise", "cACompromise", "affiliationChanged", "superseded", "cessationOfOperation", "certificateHold", "unknown", "removeFromCRL", "privilegeWithdrawn", "aACompromise")

        private val table = Hashtable()

        fun getInstance(o: Any?): CRLReason? {
            if (o is CRLReason) {
                return o
            } else if (o != null) {
                return lookup(ASN1Enumerated.getInstance(o).value.toInt())
            }

            return null
        }

        fun lookup(value: Int): CRLReason {
            val idx = Integers.valueOf(value)

            if (!table.containsKey(idx)) {
                table.put(idx, CRLReason(value))
            }

            return table.get(idx)
        }
    }
}
