package org.bouncycastle.asn1

import java.io.IOException

/**
 * A parser for indefinite-length application specific objects.
 */
class BERApplicationSpecificParser internal constructor(private val tag: Int, private val parser: ASN1StreamParser) : ASN1ApplicationSpecificParser {

    /**
     * Return the object contained in this application specific object,
     * @return the contained object.
     * *
     * @throws IOException if the underlying stream cannot be read, or does not contain an ASN.1 encoding.
     */
    @Throws(IOException::class)
    override fun readObject(): ASN1Encodable {
        return parser.readObject()
    }

    /**
     * Return an in-memory, encodable, representation of the application specific object.

     * @return a BERApplicationSpecific.
     * *
     * @throws IOException if there is an issue loading the data.
     */
    override val loadedObject: ASN1Primitive
        @Throws(IOException::class)
        get() = BERApplicationSpecific(tag, parser.readVector())

    /**
     * Return a BERApplicationSpecific representing this parser and its contents.

     * @return a BERApplicationSpecific
     */
    override fun toASN1Primitive(): ASN1Primitive {
        try {
            return loadedObject
        } catch (e: IOException) {
            throw ASN1ParsingException(e.message, e)
        }

    }
}
