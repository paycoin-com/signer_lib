package org.bouncycastle.asn1

import java.io.IOException

class DERSetParser internal constructor(private val _parser: ASN1StreamParser) : ASN1SetParser {

    @Throws(IOException::class)
    override fun readObject(): ASN1Encodable {
        return _parser.readObject()
    }

    override val loadedObject: ASN1Primitive
        @Throws(IOException::class)
        get() = DERSet(_parser.readVector(), false)

    override fun toASN1Primitive(): ASN1Primitive {
        try {
            return loadedObject
        } catch (e: IOException) {
            throw ASN1ParsingException(e.message, e)
        }

    }
}
