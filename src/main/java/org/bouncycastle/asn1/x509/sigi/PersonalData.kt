package org.bouncycastle.asn1.x509.sigi

import java.math.BigInteger
import java.util.Enumeration

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
import org.bouncycastle.asn1.x500.DirectoryString

/**
 * Contains personal data for the otherName field in the subjectAltNames
 * extension.

 *
 * PersonalData ::= SEQUENCE {
 * nameOrPseudonym NameOrPseudonym,
 * nameDistinguisher [0] INTEGER OPTIONAL,
 * dateOfBirth [1] GeneralizedTime OPTIONAL,
 * placeOfBirth [2] DirectoryString OPTIONAL,
 * gender [3] PrintableString OPTIONAL,
 * postalAddress [4] DirectoryString OPTIONAL
 * }
 *

 * @see org.bouncycastle.asn1.x509.sigi.NameOrPseudonym

 * @see org.bouncycastle.asn1.x509.sigi.SigIObjectIdentifiers
 */
class PersonalData : ASN1Object {
    var nameOrPseudonym: NameOrPseudonym? = null
        private set
    var nameDistinguisher: BigInteger? = null
        private set
    var dateOfBirth: ASN1GeneralizedTime? = null
        private set
    var placeOfBirth: DirectoryString? = null
        private set
    var gender: String? = null
        private set
    var postalAddress: DirectoryString? = null
        private set

    /**
     * Constructor from ASN1Sequence.
     *
     *
     * The sequence is of type NameOrPseudonym:
     *
     * PersonalData ::= SEQUENCE {
     * nameOrPseudonym NameOrPseudonym,
     * nameDistinguisher [0] INTEGER OPTIONAL,
     * dateOfBirth [1] GeneralizedTime OPTIONAL,
     * placeOfBirth [2] DirectoryString OPTIONAL,
     * gender [3] PrintableString OPTIONAL,
     * postalAddress [4] DirectoryString OPTIONAL
     * }
     *
     *
     * @param seq The ASN.1 sequence.
     */
    private constructor(seq: ASN1Sequence) {
        if (seq.size() < 1) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        val e = seq.objects

        nameOrPseudonym = NameOrPseudonym.getInstance(e.nextElement())

        while (e.hasMoreElements()) {
            val o = ASN1TaggedObject.getInstance(e.nextElement())
            val tag = o.tagNo
            when (tag) {
                0 -> nameDistinguisher = ASN1Integer.getInstance(o, false).value
                1 -> dateOfBirth = ASN1GeneralizedTime.getInstance(o, false)
                2 -> placeOfBirth = DirectoryString.getInstance(o, true)
                3 -> gender = DERPrintableString.getInstance(o, false).string
                4 -> postalAddress = DirectoryString.getInstance(o, true)
                else -> throw IllegalArgumentException("Bad tag number: " + o.tagNo)
            }
        }
    }

    /**
     * Constructor from a given details.

     * @param nameOrPseudonym   Name or pseudonym.
     * *
     * @param nameDistinguisher Name distinguisher.
     * *
     * @param dateOfBirth       Date of birth.
     * *
     * @param placeOfBirth      Place of birth.
     * *
     * @param gender            Gender.
     * *
     * @param postalAddress     Postal Address.
     */
    constructor(nameOrPseudonym: NameOrPseudonym,
                nameDistinguisher: BigInteger, dateOfBirth: ASN1GeneralizedTime,
                placeOfBirth: DirectoryString, gender: String, postalAddress: DirectoryString) {
        this.nameOrPseudonym = nameOrPseudonym
        this.dateOfBirth = dateOfBirth
        this.gender = gender
        this.nameDistinguisher = nameDistinguisher
        this.postalAddress = postalAddress
        this.placeOfBirth = placeOfBirth
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     *
     * Returns:
     *
     * PersonalData ::= SEQUENCE {
     * nameOrPseudonym NameOrPseudonym,
     * nameDistinguisher [0] INTEGER OPTIONAL,
     * dateOfBirth [1] GeneralizedTime OPTIONAL,
     * placeOfBirth [2] DirectoryString OPTIONAL,
     * gender [3] PrintableString OPTIONAL,
     * postalAddress [4] DirectoryString OPTIONAL
     * }
     *

     * @return a DERObject
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val vec = ASN1EncodableVector()
        vec.add(nameOrPseudonym)
        if (nameDistinguisher != null) {
            vec.add(DERTaggedObject(false, 0, ASN1Integer(nameDistinguisher)))
        }
        if (dateOfBirth != null) {
            vec.add(DERTaggedObject(false, 1, dateOfBirth))
        }
        if (placeOfBirth != null) {
            vec.add(DERTaggedObject(true, 2, placeOfBirth))
        }
        if (gender != null) {
            vec.add(DERTaggedObject(false, 3, DERPrintableString(gender, true)))
        }
        if (postalAddress != null) {
            vec.add(DERTaggedObject(true, 4, postalAddress))
        }
        return DERSequence(vec)
    }

    companion object {

        fun getInstance(obj: Any?): PersonalData {
            if (obj == null || obj is PersonalData) {
                return obj as PersonalData?
            }

            if (obj is ASN1Sequence) {
                return PersonalData(obj as ASN1Sequence?)
            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }
    }
}
