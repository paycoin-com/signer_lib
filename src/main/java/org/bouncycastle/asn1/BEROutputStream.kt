package org.bouncycastle.asn1

import java.io.IOException
import java.io.OutputStream

class BEROutputStream(
        os: OutputStream) : DEROutputStream(os) {

    @Throws(IOException::class)
    fun writeObject(
            obj: Any?) {
        if (obj == null) {
            writeNull()
        } else if (obj is ASN1Primitive) {
            obj.encode(this)
        } else if (obj is ASN1Encodable) {
            obj.toASN1Primitive().encode(this)
        } else {
            throw IOException("object not BEREncodable")
        }
    }
}
