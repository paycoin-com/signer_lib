package org.bouncycastle.asn1

import java.io.IOException

/**
 * A basic parser for a SEQUENCE object
 */
interface ASN1SequenceParser : ASN1Encodable, InMemoryRepresentable {
    /**
     * Read the next object from the underlying object representing a SEQUENCE.

     * @throws IOException for bad input stream.
     * *
     * @return the next object, null if we are at the end.
     */
    @Throws(IOException::class)
    fun readObject(): ASN1Encodable
}
