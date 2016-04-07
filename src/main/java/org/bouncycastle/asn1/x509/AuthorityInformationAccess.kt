package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 * The AuthorityInformationAccess object.
 *
 * id-pe-authorityInfoAccess OBJECT IDENTIFIER ::= { id-pe 1 }

 * AuthorityInfoAccessSyntax  ::=
 * SEQUENCE SIZE (1..MAX) OF AccessDescription
 * AccessDescription  ::=  SEQUENCE {
 * accessMethod          OBJECT IDENTIFIER,
 * accessLocation        GeneralName  }

 * id-ad OBJECT IDENTIFIER ::= { id-pkix 48 }
 * id-ad-caIssuers OBJECT IDENTIFIER ::= { id-ad 2 }
 * id-ad-ocsp OBJECT IDENTIFIER ::= { id-ad 1 }
 *
 */
class AuthorityInformationAccess : ASN1Object {
    /**

     * @return the access descriptions contained in this object.
     */
    var accessDescriptions: Array<AccessDescription>? = null
        private set

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() < 1) {
            throw IllegalArgumentException("sequence may not be empty")
        }

        accessDescriptions = arrayOfNulls<AccessDescription>(seq.size())

        for (i in 0..seq.size() - 1) {
            accessDescriptions[i] = AccessDescription.getInstance(seq.getObjectAt(i))
        }
    }

    constructor(
            description: AccessDescription) : this(arrayOf(description)) {
    }

    constructor(
            descriptions: Array<AccessDescription>) {
        this.accessDescriptions = arrayOfNulls<AccessDescription>(descriptions.size)
        System.arraycopy(descriptions, 0, this.accessDescriptions, 0, descriptions.size)
    }

    /**
     * create an AuthorityInformationAccess with the oid and location provided.
     */
    constructor(
            oid: ASN1ObjectIdentifier,
            location: GeneralName) : this(AccessDescription(oid, location)) {
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val vec = ASN1EncodableVector()

        for (i in accessDescriptions!!.indices) {
            vec.add(accessDescriptions!![i])
        }

        return DERSequence(vec)
    }

    override fun toString(): String {
        return "AuthorityInformationAccess: Oid(" + this.accessDescriptions!![0].accessMethod.id + ")"
    }

    companion object {

        fun getInstance(
                obj: Any?): AuthorityInformationAccess? {
            if (obj is AuthorityInformationAccess) {
                return obj
            }

            if (obj != null) {
                return AuthorityInformationAccess(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        fun fromExtensions(extensions: Extensions): AuthorityInformationAccess {
            return AuthorityInformationAccess.getInstance(extensions.getExtensionParsedValue(Extension.authorityInfoAccess))
        }
    }
}
