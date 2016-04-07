package org.bouncycastle.asn1

import java.io.IOException

/**
 * Parser for indefinite-length SEQUENCEs.
 */
class BERSequenceParser internal constructor(private val _parser: ASN1StreamParser) : ASN1SequenceParser {

    /**
     * Read the next object in the SEQUENCE.

     * @return the next object in the SEQUENCE, null if there are no more.
     * *
     * @throws IOException if there is an issue reading the underlying stream.
     */
    @Throws(IOException::class)
    override fun readObject(): ASN1Encodable {
        return _parser.readObject()
    }

    /**
     * Return an in-memory, encodable, representation of the SEQUENCE.

     * @return a BERSequence.
     * *
     * @throws IOException if there is an issue loading the data.
     */
    override val loadedObject: ASN1Primitive
        @Throws(IOException::class)
        get() = BERSequence(_parser.readVector())

    /**
     * Return an BERSequence representing this parser and its contents.

     * @return an BERSequence
     */
    override fun toASN1Primitive(): ASN1Primitive {
        try {
            return loadedObject
        } catch (e: IOException) {
            throw IllegalStateException(e.message)
        }

    }
}
