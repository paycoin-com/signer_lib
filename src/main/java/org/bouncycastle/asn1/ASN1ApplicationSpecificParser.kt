package org.bouncycastle.asn1

import java.io.IOException

/**
 * Interface to parse ASN.1 application specific objects.
 */
interface ASN1ApplicationSpecificParser : ASN1Encodable, InMemoryRepresentable {
    /**
     * Read the next object in the parser.

     * @return an ASN1Encodable
     * *
     * @throws IOException on a parsing or decoding error.
     */
    @Throws(IOException::class)
    fun readObject(): ASN1Encodable
}
