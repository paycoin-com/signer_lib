package org.bouncycastle.asn1

import java.io.IOException
import java.io.InputStream

/**
 * Parse for DER encoded OCTET STRINGS
 */
class DEROctetStringParser internal constructor(
        private val stream: DefiniteLengthInputStream) : ASN1OctetStringParser {

    /**
     * Return an InputStream representing the contents of the OCTET STRING.

     * @return an InputStream with its source as the OCTET STRING content.
     */
    override val octetStream: InputStream
        get() = stream

    /**
     * Return an in-memory, encodable, representation of the OCTET STRING.

     * @return a DEROctetString.
     * *
     * @throws IOException if there is an issue loading the data.
     */
    override val loadedObject: ASN1Primitive
        @Throws(IOException::class)
        get() = DEROctetString(stream.toByteArray())

    /**
     * Return an DEROctetString representing this parser and its contents.

     * @return an DEROctetString
     */
    override fun toASN1Primitive(): ASN1Primitive {
        try {
            return loadedObject
        } catch (e: IOException) {
            throw ASN1ParsingException("IOException converting stream to byte array: " + e.message, e)
        }

    }
}
