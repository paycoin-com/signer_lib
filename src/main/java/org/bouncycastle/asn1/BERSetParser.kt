package org.bouncycastle.asn1

import java.io.IOException

/**
 * Parser for indefinite-length SETs.
 */
class BERSetParser internal constructor(private val _parser: ASN1StreamParser) : ASN1SetParser {

    /**
     * Read the next object in the SET.

     * @return the next object in the SET, null if there are no more.
     * *
     * @throws IOException if there is an issue reading the underlying stream.
     */
    @Throws(IOException::class)
    override fun readObject(): ASN1Encodable {
        return _parser.readObject()
    }

    /**
     * Return an in-memory, encodable, representation of the SET.

     * @return a BERSet.
     * *
     * @throws IOException if there is an issue loading the data.
     */
    override val loadedObject: ASN1Primitive
        @Throws(IOException::class)
        get() = BERSet(_parser.readVector())

    /**
     * Return an BERSet representing this parser and its contents.

     * @return an BERSet
     */
    override fun toASN1Primitive(): ASN1Primitive {
        try {
            return loadedObject
        } catch (e: IOException) {
            throw ASN1ParsingException(e.message, e)
        }

    }
}