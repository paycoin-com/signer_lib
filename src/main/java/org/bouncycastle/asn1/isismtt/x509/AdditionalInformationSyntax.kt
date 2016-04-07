package org.bouncycastle.asn1.isismtt.x509

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.x500.DirectoryString

/**
 * Some other information of non-restrictive nature regarding the usage of this
 * certificate.

 *
 * AdditionalInformationSyntax ::= DirectoryString (SIZE(1..2048))
 *
 */
class AdditionalInformationSyntax private constructor(val information: DirectoryString) : ASN1Object() {

    /**
     * Constructor from a given details.

     * @param information The description of the information.
     */
    constructor(information: String) : this(DirectoryString(information)) {
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     *
     * Returns:
     *
     * AdditionalInformationSyntax ::= DirectoryString (SIZE(1..2048))
     *

     * @return a DERObject
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return information.toASN1Primitive()
    }

    companion object {

        fun getInstance(obj: Any?): AdditionalInformationSyntax? {
            if (obj is AdditionalInformationSyntax) {
                return obj
            }

            if (obj != null) {
                return AdditionalInformationSyntax(DirectoryString.getInstance(obj))
            }

            return null
        }
    }
}
