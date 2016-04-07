package org.bouncycastle.asn1.x509.qualified

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.GeneralName

/**
 * The SemanticsInformation object.
 *
 * SemanticsInformation ::= SEQUENCE {
 * semanticsIdentifier        OBJECT IDENTIFIER   OPTIONAL,
 * nameRegistrationAuthorities NameRegistrationAuthorities
 * OPTIONAL }
 * (WITH COMPONENTS {..., semanticsIdentifier PRESENT}|
 * WITH COMPONENTS {..., nameRegistrationAuthorities PRESENT})

 * NameRegistrationAuthorities ::=  SEQUENCE SIZE (1..MAX) OF
 * GeneralName
 *
 */
class SemanticsInformation : ASN1Object {
    var semanticsIdentifier: ASN1ObjectIdentifier? = null
        private set
    var nameRegistrationAuthorities: Array<GeneralName>? = null
        private set

    private constructor(seq: ASN1Sequence) {
        val e = seq.objects
        if (seq.size() < 1) {
            throw IllegalArgumentException("no objects in SemanticsInformation")
        }

        var `object`: Any? = e.nextElement()
        if (`object` is ASN1ObjectIdentifier) {
            semanticsIdentifier = ASN1ObjectIdentifier.getInstance(`object`)
            if (e.hasMoreElements()) {
                `object` = e.nextElement()
            } else {
                `object` = null
            }
        }

        if (`object` != null) {
            val generalNameSeq = ASN1Sequence.getInstance(`object`)
            nameRegistrationAuthorities = arrayOfNulls<GeneralName>(generalNameSeq.size())
            for (i in 0..generalNameSeq.size() - 1) {
                nameRegistrationAuthorities[i] = GeneralName.getInstance(generalNameSeq.getObjectAt(i))
            }
        }
    }

    constructor(
            semanticsIdentifier: ASN1ObjectIdentifier,
            generalNames: Array<GeneralName>) {
        this.semanticsIdentifier = semanticsIdentifier
        this.nameRegistrationAuthorities = generalNames
    }

    constructor(semanticsIdentifier: ASN1ObjectIdentifier) {
        this.semanticsIdentifier = semanticsIdentifier
        this.nameRegistrationAuthorities = null
    }

    constructor(generalNames: Array<GeneralName>) {
        this.semanticsIdentifier = null
        this.nameRegistrationAuthorities = generalNames
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val seq = ASN1EncodableVector()

        if (this.semanticsIdentifier != null) {
            seq.add(semanticsIdentifier)
        }
        if (this.nameRegistrationAuthorities != null) {
            val seqname = ASN1EncodableVector()
            for (i in nameRegistrationAuthorities!!.indices) {
                seqname.add(nameRegistrationAuthorities!![i])
            }
            seq.add(DERSequence(seqname))
        }

        return DERSequence(seq)
    }

    companion object {

        fun getInstance(obj: Any?): SemanticsInformation? {
            if (obj is SemanticsInformation) {
                return obj
            }

            if (obj != null) {
                return SemanticsInformation(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
