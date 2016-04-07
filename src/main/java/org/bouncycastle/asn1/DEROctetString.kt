package org.bouncycastle.asn1

import java.io.IOException

/**
 * Carrier class for a DER encoding OCTET STRING
 */
class DEROctetString : ASN1OctetString {
    /**
     * Base constructor.

     * @param string the octets making up the octet string.
     */
    constructor(
            string: ByteArray) : super(string) {
    }

    /**
     * Constructor from the encoding of an ASN.1 object.

     * @param obj the object to be encoded.
     */
    @Throws(IOException::class)
    constructor(
            obj: ASN1Encodable) : super(obj.toASN1Primitive().getEncoded(ASN1Encoding.DER)) {
    }

    internal override val isConstructed: Boolean
        get() = false

    internal override fun encodedLength(): Int {
        return 1 + StreamUtil.calculateBodyLength(octets.size) + octets.size
    }

    @Throws(IOException::class)
    internal override fun encode(
            out: ASN1OutputStream) {
        out.writeEncoded(BERTags.OCTET_STRING, octets)
    }

    companion object {

        @Throws(IOException::class)
        internal fun encode(
                derOut: DEROutputStream,
                bytes: ByteArray) {
            derOut.writeEncoded(BERTags.OCTET_STRING, bytes)
        }
    }
}
