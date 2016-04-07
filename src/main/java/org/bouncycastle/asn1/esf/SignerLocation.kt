package org.bouncycastle.asn1.esf

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.x500.DirectoryString

/**
 * Signer-Location attribute (RFC3126).

 *
 * SignerLocation ::= SEQUENCE {
 * countryName        [0] DirectoryString OPTIONAL,
 * localityName       [1] DirectoryString OPTIONAL,
 * postalAddress      [2] PostalAddress OPTIONAL }

 * PostalAddress ::= SEQUENCE SIZE(1..6) OF DirectoryString
 *
 */
class SignerLocation : ASN1Object {
    var countryName: DERUTF8String? = null
        private set
    var localityName: DERUTF8String? = null
        private set
    var postalAddress: ASN1Sequence? = null
        private set

    private constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        while (e.hasMoreElements()) {
            val o = e.nextElement() as DERTaggedObject

            when (o.tagNo) {
                0 -> {
                    val countryNameDirectoryString = DirectoryString.getInstance(o, true)
                    this.countryName = DERUTF8String(countryNameDirectoryString.string)
                }
                1 -> {
                    val localityNameDirectoryString = DirectoryString.getInstance(o, true)
                    this.localityName = DERUTF8String(localityNameDirectoryString.string)
                }
                2 -> {
                    if (o.isExplicit) {
                        this.postalAddress = ASN1Sequence.getInstance(o, true)
                    } else
                    // handle erroneous implicitly tagged sequences
                    {
                        this.postalAddress = ASN1Sequence.getInstance(o, false)
                    }
                    if (postalAddress != null && postalAddress!!.size() > 6) {
                        throw IllegalArgumentException("postal address must contain less than 6 strings")
                    }
                }
                else -> throw IllegalArgumentException("illegal tag")
            }
        }
    }

    constructor(
            countryName: DERUTF8String?,
            localityName: DERUTF8String?,
            postalAddress: ASN1Sequence?) {
        if (postalAddress != null && postalAddress.size() > 6) {
            throw IllegalArgumentException("postal address must contain less than 6 strings")
        }

        if (countryName != null) {
            this.countryName = DERUTF8String.getInstance(countryName.toASN1Primitive())
        }

        if (localityName != null) {
            this.localityName = DERUTF8String.getInstance(localityName.toASN1Primitive())
        }

        if (postalAddress != null) {
            this.postalAddress = ASN1Sequence.getInstance(postalAddress.toASN1Primitive())
        }
    }

    /**
     *
     * SignerLocation ::= SEQUENCE {
     * countryName        [0] DirectoryString OPTIONAL,
     * localityName       [1] DirectoryString OPTIONAL,
     * postalAddress      [2] PostalAddress OPTIONAL }

     * PostalAddress ::= SEQUENCE SIZE(1..6) OF DirectoryString

     * DirectoryString ::= CHOICE {
     * teletexString           TeletexString (SIZE (1..MAX)),
     * printableString         PrintableString (SIZE (1..MAX)),
     * universalString         UniversalString (SIZE (1..MAX)),
     * utf8String              UTF8String (SIZE (1.. MAX)),
     * bmpString               BMPString (SIZE (1..MAX)) }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (countryName != null) {
            v.add(DERTaggedObject(true, 0, countryName))
        }

        if (localityName != null) {
            v.add(DERTaggedObject(true, 1, localityName))
        }

        if (postalAddress != null) {
            v.add(DERTaggedObject(true, 2, postalAddress))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): SignerLocation {
            if (obj == null || obj is SignerLocation) {
                return obj as SignerLocation?
            }

            return SignerLocation(ASN1Sequence.getInstance(obj))
        }
    }
}
