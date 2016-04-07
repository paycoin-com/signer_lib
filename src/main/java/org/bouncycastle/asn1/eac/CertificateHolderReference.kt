package org.bouncycastle.asn1.eac

import java.io.UnsupportedEncodingException

open class CertificateHolderReference {

    var countryCode: String? = null
        private set
    var holderMnemonic: String? = null
        private set
    var sequenceNumber: String? = null
        private set

    constructor(countryCode: String, holderMnemonic: String, sequenceNumber: String) {
        this.countryCode = countryCode
        this.holderMnemonic = holderMnemonic
        this.sequenceNumber = sequenceNumber
    }

    internal constructor(contents: ByteArray) {
        try {
            val concat = String(contents, ReferenceEncoding)

            this.countryCode = concat.substring(0, 2)
            this.holderMnemonic = concat.substring(2, concat.length - 5)

            this.sequenceNumber = concat.substring(concat.length - 5)
        } catch (e: UnsupportedEncodingException) {
            throw IllegalStateException(e.toString())
        }

    }


    val encoded: ByteArray
        get() {
            val ref = countryCode + holderMnemonic + sequenceNumber

            try {
                return ref.toByteArray(charset(ReferenceEncoding))
            } catch (e: UnsupportedEncodingException) {
                throw IllegalStateException(e.toString())
            }

        }

    companion object {
        private val ReferenceEncoding = "ISO-8859-1"
    }
}
