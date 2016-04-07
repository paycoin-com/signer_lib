package org.bouncycastle.asn1

import java.io.IOException
import java.io.InputStream

import org.bouncycastle.util.io.Streams

class BEROctetStringParser internal constructor(
        private val _parser: ASN1StreamParser) : ASN1OctetStringParser {

    override val octetStream: InputStream
        get() = ConstructedOctetStream(_parser)

    override val loadedObject: ASN1Primitive
        @Throws(IOException::class)
        get() = BEROctetString(Streams.readAll(octetStream))

    override fun toASN1Primitive(): ASN1Primitive {
        try {
            return loadedObject
        } catch (e: IOException) {
            throw ASN1ParsingException("IOException converting stream to byte array: " + e.message, e)
        }

    }
}
