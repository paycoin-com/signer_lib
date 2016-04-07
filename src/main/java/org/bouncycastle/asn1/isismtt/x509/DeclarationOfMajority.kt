package org.bouncycastle.asn1.isismtt.x509

import org.bouncycastle.asn1.ASN1Boolean
import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERPrintableString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

/**
 * A declaration of majority.

 *
 * DeclarationOfMajoritySyntax ::= CHOICE
 * {
 * notYoungerThan [0] IMPLICIT INTEGER,
 * fullAgeAtCountry [1] IMPLICIT SEQUENCE
 * {
 * fullAge BOOLEAN DEFAULT TRUE,
 * country PrintableString (SIZE(2))
 * }
 * dateOfBirth [2] IMPLICIT GeneralizedTime
 * }
 *
 *
 *
 * fullAgeAtCountry indicates the majority of the owner with respect to the laws
 * of a specific country.
 */
class DeclarationOfMajority : ASN1Object, ASN1Choice {

    private var declaration: ASN1TaggedObject? = null

    constructor(notYoungerThan: Int) {
        declaration = DERTaggedObject(false, 0, ASN1Integer(notYoungerThan.toLong()))
    }

    constructor(fullAge: Boolean, country: String) {
        if (country.length > 2) {
            throw IllegalArgumentException("country can only be 2 characters")
        }

        if (fullAge) {
            declaration = DERTaggedObject(false, 1, DERSequence(DERPrintableString(country, true)))
        } else {
            val v = ASN1EncodableVector()

            v.add(ASN1Boolean.FALSE)
            v.add(DERPrintableString(country, true))

            declaration = DERTaggedObject(false, 1, DERSequence(v))
        }
    }

    constructor(dateOfBirth: ASN1GeneralizedTime) {
        declaration = DERTaggedObject(false, 2, dateOfBirth)
    }

    private constructor(o: ASN1TaggedObject) {
        if (o.tagNo > 2) {
            throw IllegalArgumentException("Bad tag number: " + o.tagNo)
        }
        declaration = o
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     *
     * Returns:
     *
     * DeclarationOfMajoritySyntax ::= CHOICE
     * {
     * notYoungerThan [0] IMPLICIT INTEGER,
     * fullAgeAtCountry [1] IMPLICIT SEQUENCE
     * {
     * fullAge BOOLEAN DEFAULT TRUE,
     * country PrintableString (SIZE(2))
     * }
     * dateOfBirth [2] IMPLICIT GeneralizedTime
     * }
     *

     * @return a DERObject
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return declaration
    }

    val type: Int
        get() = declaration!!.tagNo

    /**
     * @return notYoungerThan if that's what we are, -1 otherwise
     */
    fun notYoungerThan(): Int {
        if (declaration!!.tagNo != 0) {
            return -1
        }

        return ASN1Integer.getInstance(declaration, false).value.toInt()
    }

    fun fullAgeAtCountry(): ASN1Sequence? {
        if (declaration!!.tagNo != 1) {
            return null
        }

        return ASN1Sequence.getInstance(declaration, false)
    }

    fun getDateOfBirth(): ASN1GeneralizedTime? {
        if (declaration!!.tagNo != 2) {
            return null
        }

        return ASN1GeneralizedTime.getInstance(declaration, false)
    }

    companion object {
        val notYoungerThan = 0
        val fullAgeAtCountry = 1
        val dateOfBirth = 2

        fun getInstance(obj: Any?): DeclarationOfMajority {
            if (obj == null || obj is DeclarationOfMajority) {
                return obj as DeclarationOfMajority?
            }

            if (obj is ASN1TaggedObject) {
                return DeclarationOfMajority(obj as ASN1TaggedObject?)
            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }
    }
}
