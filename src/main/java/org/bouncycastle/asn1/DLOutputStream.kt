package org.bouncycastle.asn1

import java.io.IOException
import java.io.OutputStream

/**
 * Stream that outputs encoding based on definite length.
 */
class DLOutputStream(
        os: OutputStream) : ASN1OutputStream(os) {

    @Throws(IOException::class)
    override fun writeObject(
            obj: ASN1Encodable?) {
        if (obj != null) {
            obj.toASN1Primitive().toDLObject().encode(this)
        } else {
            throw IOException("null object detected")
        }
    }
}
