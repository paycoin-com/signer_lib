package org.bouncycastle.asn1

import java.io.IOException
import java.util.Enumeration

internal class LazyConstructionEnumeration(encoded: ByteArray) : Enumeration<Any> {
    private val aIn: ASN1InputStream
    private var nextObj: Any? = null

    init {
        aIn = ASN1InputStream(encoded, true)
        nextObj = readObject()
    }

    override fun hasMoreElements(): Boolean {
        return nextObj != null
    }

    override fun nextElement(): Any {
        val o = nextObj

        nextObj = readObject()

        return o
    }

    private fun readObject(): Any {
        try {
            return aIn.readObject()
        } catch (e: IOException) {
            throw ASN1ParsingException("malformed DER construction: " + e, e)
        }

    }
}
