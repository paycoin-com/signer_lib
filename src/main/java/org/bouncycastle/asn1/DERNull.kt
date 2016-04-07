package org.bouncycastle.asn1

import java.io.IOException

/**
 * A NULL object.
 */
class DERNull
@Deprecated("use DERNull.INSTANCE")
constructor() : ASN1Null() {

    internal override val isConstructed: Boolean
        get() = false

    internal override fun encodedLength(): Int {
        return 2
    }

    @Throws(IOException::class)
    internal override fun encode(
            out: ASN1OutputStream) {
        out.writeEncoded(BERTags.NULL, zeroBytes)
    }

    companion object {
        val INSTANCE = DERNull()

        private val zeroBytes = ByteArray(0)
    }
}
