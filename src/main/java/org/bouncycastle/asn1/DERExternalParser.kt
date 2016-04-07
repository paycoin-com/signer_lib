package org.bouncycastle.asn1

import java.io.IOException

/**
 * Parser DER EXTERNAL tagged objects.
 */
class DERExternalParser
/**
 * Base constructor.

 * @param parser the underlying parser to read the DER EXTERNAL from.
 */
(private val _parser: ASN1StreamParser) : ASN1Encodable, InMemoryRepresentable {

    @Throws(IOException::class)
    fun readObject(): ASN1Encodable {
        return _parser.readObject()
    }

    /**
     * Return an in-memory, encodable, representation of the EXTERNAL object.

     * @return a DERExternal.
     * *
     * @throws IOException if there is an issue loading the data.
     */
    override val loadedObject: ASN1Primitive
        @Throws(IOException::class)
        get() {
            try {
                return DERExternal(_parser.readVector())
            } catch (e: IllegalArgumentException) {
                throw ASN1Exception(e.message, e)
            }

        }

    /**
     * Return an DERExternal representing this parser and its contents.

     * @return an DERExternal
     */
    override fun toASN1Primitive(): ASN1Primitive {
        try {
            return loadedObject
        } catch (ioe: IOException) {
            throw ASN1ParsingException("unable to get DER object", ioe)
        } catch (ioe: IllegalArgumentException) {
            throw ASN1ParsingException("unable to get DER object", ioe)
        }

    }
}