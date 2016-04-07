package org.bouncycastle.asn1

import java.io.IOException

class DERSequenceParser internal constructor(private val _parser: ASN1StreamParser) : ASN1SequenceParser {

    @Throws(IOException::class)
    override fun readObject(): ASN1Encodable {
        return _parser.readObject()
    }

    override val loadedObject: ASN1Primitive
        @Throws(IOException::class)
        get() = DERSequence(_parser.readVector())

    override fun toASN1Primitive(): ASN1Primitive {
        try {
            return loadedObject
        } catch (e: IOException) {
            throw IllegalStateException(e.message)
        }

    }
}
