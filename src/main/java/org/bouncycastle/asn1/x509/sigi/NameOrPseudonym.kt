package org.bouncycastle.asn1.x509.sigi

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x500.DirectoryString

/**
 * Structure for a name or pseudonym.

 *
 * NameOrPseudonym ::= CHOICE {
 * surAndGivenName SEQUENCE {
 * surName DirectoryString,
 * givenName SEQUENCE OF DirectoryString
 * },
 * pseudonym DirectoryString
 * }
 *

 * @see org.bouncycastle.asn1.x509.sigi.PersonalData
 */
class NameOrPseudonym : ASN1Object, ASN1Choice {
    val pseudonym: DirectoryString?

    var surname: DirectoryString? = null
        private set

    private var givenName: ASN1Sequence? = null

    /**
     * Constructor from DirectoryString.
     *
     *
     * The sequence is of type NameOrPseudonym:
     *
     * NameOrPseudonym ::= CHOICE {
     * surAndGivenName SEQUENCE {
     * surName DirectoryString,
     * givenName SEQUENCE OF DirectoryString
     * },
     * pseudonym DirectoryString
     * }
     *
     * @param pseudonym pseudonym value to use.
     */
    constructor(pseudonym: DirectoryString) {
        this.pseudonym = pseudonym
    }

    /**
     * Constructor from ASN1Sequence.
     *
     *
     * The sequence is of type NameOrPseudonym:
     *
     * NameOrPseudonym ::= CHOICE {
     * surAndGivenName SEQUENCE {
     * surName DirectoryString,
     * givenName SEQUENCE OF DirectoryString
     * },
     * pseudonym DirectoryString
     * }
     *
     *
     * @param seq The ASN.1 sequence.
     */
    private constructor(seq: ASN1Sequence) {
        if (seq.size() != 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        if (seq.getObjectAt(0) !is ASN1String) {
            throw IllegalArgumentException("Bad object encountered: " + seq.getObjectAt(0).javaClass)
        }

        surname = DirectoryString.getInstance(seq.getObjectAt(0))
        givenName = ASN1Sequence.getInstance(seq.getObjectAt(1))
    }

    /**
     * Constructor from a given details.

     * @param pseudonym The pseudonym.
     */
    constructor(pseudonym: String) : this(DirectoryString(pseudonym)) {
    }

    /**
     * Constructor from a given details.

     * @param surname   The surname.
     * *
     * @param givenName A sequence of directory strings making up the givenName
     */
    constructor(surname: DirectoryString, givenName: ASN1Sequence) {
        this.surname = surname
        this.givenName = givenName
    }

    fun getGivenName(): Array<DirectoryString> {
        val items = arrayOfNulls<DirectoryString>(givenName!!.size())
        var count = 0
        val e = givenName!!.objects
        while (e.hasMoreElements()) {
            items[count++] = DirectoryString.getInstance(e.nextElement())
        }
        return items
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     *
     * Returns:
     *
     * NameOrPseudonym ::= CHOICE {
     * surAndGivenName SEQUENCE {
     * surName DirectoryString,
     * givenName SEQUENCE OF DirectoryString
     * },
     * pseudonym DirectoryString
     * }
     *

     * @return a DERObject
     */
    override fun toASN1Primitive(): ASN1Primitive {
        if (pseudonym != null) {
            return pseudonym.toASN1Primitive()
        } else {
            val vec1 = ASN1EncodableVector()
            vec1.add(surname)
            vec1.add(givenName)
            return DERSequence(vec1)
        }
    }

    companion object {

        fun getInstance(obj: Any?): NameOrPseudonym {
            if (obj == null || obj is NameOrPseudonym) {
                return obj as NameOrPseudonym?
            }

            if (obj is ASN1String) {
                return NameOrPseudonym(DirectoryString.getInstance(obj))
            }

            if (obj is ASN1Sequence) {
                return NameOrPseudonym(obj as ASN1Sequence?)
            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }
    }
}
