package org.bouncycastle.asn1.ess

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DEROctetString

class ContentIdentifier
/**
 * Create from OCTET STRING whose octets represent the identifier.
 */
private constructor(
        value: ASN1OctetString) : ASN1Object() {
    var value: ASN1OctetString
        internal set

    init {
        this.value = value
    }

    /**
     * Create from byte array representing the identifier.
     */
    constructor(
            value: ByteArray) : this(DEROctetString(value)) {
    }

    /**
     * The definition of ContentIdentifier is
     *
     * ContentIdentifier ::=  OCTET STRING
     *
     * id-aa-contentIdentifier OBJECT IDENTIFIER ::= { iso(1)
     * member-body(2) us(840) rsadsi(113549) pkcs(1) pkcs9(9)
     * smime(16) id-aa(2) 7 }
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return value
    }

    companion object {

        fun getInstance(o: Any?): ContentIdentifier? {
            if (o is ContentIdentifier) {
                return o
            } else if (o != null) {
                return ContentIdentifier(ASN1OctetString.getInstance(o))
            }

            return null
        }
    }
}
