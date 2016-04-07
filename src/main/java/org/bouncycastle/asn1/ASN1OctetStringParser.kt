package org.bouncycastle.asn1

import java.io.InputStream

/**
 * A basic parser for an OCTET STRING object
 */
interface ASN1OctetStringParser : ASN1Encodable, InMemoryRepresentable {
    /**
     * Return the content of the OCTET STRING as an InputStream.

     * @return an InputStream representing the OCTET STRING's content.
     */
    val octetStream: InputStream
}
