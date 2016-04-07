package org.bouncycastle.asn1.isismtt.x509

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.x500.DirectoryString

/**
 * Some other restriction regarding the usage of this certificate.

 *
 * RestrictionSyntax ::= DirectoryString (SIZE(1..1024))
 *
 */
class Restriction : ASN1Object {
    var restriction: DirectoryString? = null
        private set

    /**
     * Constructor from DirectoryString.
     *
     *
     * The DirectoryString is of type RestrictionSyntax:
     *
     * RestrictionSyntax ::= DirectoryString (SIZE(1..1024))
     *
     *
     * @param restriction A DirectoryString.
     */
    private constructor(restriction: DirectoryString) {
        this.restriction = restriction
    }

    /**
     * Constructor from a given details.

     * @param restriction The describtion of the restriction.
     */
    constructor(restriction: String) {
        this.restriction = DirectoryString(restriction)
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     *
     * Returns:
     *
     * RestrictionSyntax ::= DirectoryString (SIZE(1..1024))
     *

     * @return a DERObject
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return restriction!!.toASN1Primitive()
    }

    companion object {

        fun getInstance(obj: Any?): Restriction? {
            if (obj is Restriction) {
                return obj
            }

            if (obj != null) {
                return Restriction(DirectoryString.getInstance(obj))
            }

            return null
        }
    }
}
