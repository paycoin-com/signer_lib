package org.bouncycastle.asn1

import java.io.IOException
import java.io.OutputStream

/**
 * Stream that outputs encoding based on distinguished encoding rules.
 */
open class DEROutputStream(
        os: OutputStream) : ASN1OutputStream(os) {

    @Throws(IOException::class)
    override fun writeObject(
            obj: ASN1Encodable?) {
        if (obj != null) {
            obj.toASN1Primitive().toDERObject().encode(this)
        } else {
            throw IOException("null object detected")
        }
    }

    internal override val derSubStream: ASN1OutputStream
        get() = this

    internal override val dlSubStream: ASN1OutputStream
        get() = this
}
