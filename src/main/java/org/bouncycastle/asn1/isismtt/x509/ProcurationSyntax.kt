package org.bouncycastle.asn1.isismtt.x509

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERPrintableString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x500.DirectoryString
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.IssuerSerial

/**
 * Attribute to indicate that the certificate holder may sign in the name of a
 * third person.
 *
 *
 * ISIS-MTT PROFILE: The corresponding ProcurationSyntax contains either the
 * name of the person who is represented (subcomponent thirdPerson) or a
 * reference to his/her base certificate (in the component signingFor,
 * subcomponent certRef), furthermore the optional components country and
 * typeSubstitution to indicate the country whose laws apply, and respectively
 * the type of procuration (e.g. manager, procuration, custody).
 *
 *
 * ISIS-MTT PROFILE: The GeneralName MUST be of type directoryName and MAY only
 * contain: - RFC3039 attributes, except pseudonym (countryName, commonName,
 * surname, givenName, serialNumber, organizationName, organizationalUnitName,
 * stateOrProvincename, localityName, postalAddress) and - SubjectDirectoryName
 * attributes (title, dateOfBirth, placeOfBirth, gender, countryOfCitizenship,
 * countryOfResidence and NameAtBirth).

 *
 * ProcurationSyntax ::= SEQUENCE {
 * country [1] EXPLICIT PrintableString(SIZE(2)) OPTIONAL,
 * typeOfSubstitution [2] EXPLICIT DirectoryString (SIZE(1..128)) OPTIONAL,
 * signingFor [3] EXPLICIT SigningFor
 * }

 * SigningFor ::= CHOICE
 * {
 * thirdPerson GeneralName,
 * certRef IssuerSerial
 * }
 *

 */
class ProcurationSyntax : ASN1Object {
    var country: String? = null
        private set
    var typeOfSubstitution: DirectoryString? = null
        private set

    var thirdPerson: GeneralName? = null
        private set
    var certRef: IssuerSerial? = null
        private set

    /**
     * Constructor from ASN1Sequence.
     *
     *
     * The sequence is of type ProcurationSyntax:
     *
     * ProcurationSyntax ::= SEQUENCE {
     * country [1] EXPLICIT PrintableString(SIZE(2)) OPTIONAL,
     * typeOfSubstitution [2] EXPLICIT DirectoryString (SIZE(1..128)) OPTIONAL,
     * signingFor [3] EXPLICIT SigningFor
     * }

     * SigningFor ::= CHOICE
     * {
     * thirdPerson GeneralName,
     * certRef IssuerSerial
     * }
     *
     *
     * @param seq The ASN.1 sequence.
     */
    private constructor(seq: ASN1Sequence) {
        if (seq.size() < 1 || seq.size() > 3) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }
        val e = seq.objects

        while (e.hasMoreElements()) {
            val o = ASN1TaggedObject.getInstance(e.nextElement())
            when (o.tagNo) {
                1 -> country = DERPrintableString.getInstance(o, true).string
                2 -> typeOfSubstitution = DirectoryString.getInstance(o, true)
                3 -> {
                    val signingFor = o.`object`
                    if (signingFor is ASN1TaggedObject) {
                        thirdPerson = GeneralName.getInstance(signingFor)
                    } else {
                        certRef = IssuerSerial.getInstance(signingFor)
                    }
                }
                else -> throw IllegalArgumentException("Bad tag number: " + o.tagNo)
            }
        }
    }

    /**
     * Constructor from a given details.
     *
     *
     * Either `generalName` or `certRef` MUST be
     * `null`.

     * @param country            The country code whose laws apply.
     * *
     * @param typeOfSubstitution The type of procuration.
     * *
     * @param certRef            Reference to certificate of the person who is represented.
     */
    constructor(
            country: String,
            typeOfSubstitution: DirectoryString,
            certRef: IssuerSerial) {
        this.country = country
        this.typeOfSubstitution = typeOfSubstitution
        this.thirdPerson = null
        this.certRef = certRef
    }

    /**
     * Constructor from a given details.
     *
     *
     * Either `generalName` or `certRef` MUST be
     * `null`.

     * @param country            The country code whose laws apply.
     * *
     * @param typeOfSubstitution The type of procuration.
     * *
     * @param thirdPerson        The GeneralName of the person who is represented.
     */
    constructor(
            country: String,
            typeOfSubstitution: DirectoryString,
            thirdPerson: GeneralName) {
        this.country = country
        this.typeOfSubstitution = typeOfSubstitution
        this.thirdPerson = thirdPerson
        this.certRef = null
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     *
     * Returns:
     *
     * ProcurationSyntax ::= SEQUENCE {
     * country [1] EXPLICIT PrintableString(SIZE(2)) OPTIONAL,
     * typeOfSubstitution [2] EXPLICIT DirectoryString (SIZE(1..128)) OPTIONAL,
     * signingFor [3] EXPLICIT SigningFor
     * }

     * SigningFor ::= CHOICE
     * {
     * thirdPerson GeneralName,
     * certRef IssuerSerial
     * }
     *

     * @return a DERObject
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val vec = ASN1EncodableVector()
        if (country != null) {
            vec.add(DERTaggedObject(true, 1, DERPrintableString(country, true)))
        }
        if (typeOfSubstitution != null) {
            vec.add(DERTaggedObject(true, 2, typeOfSubstitution))
        }
        if (thirdPerson != null) {
            vec.add(DERTaggedObject(true, 3, thirdPerson))
        } else {
            vec.add(DERTaggedObject(true, 3, certRef))
        }

        return DERSequence(vec)
    }

    companion object {

        fun getInstance(obj: Any?): ProcurationSyntax {
            if (obj == null || obj is ProcurationSyntax) {
                return obj as ProcurationSyntax?
            }

            if (obj is ASN1Sequence) {
                return ProcurationSyntax(obj as ASN1Sequence?)
            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }
    }
}
