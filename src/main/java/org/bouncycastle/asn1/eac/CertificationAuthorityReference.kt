package org.bouncycastle.asn1.eac

class CertificationAuthorityReference : CertificateHolderReference {
    constructor(countryCode: String, holderMnemonic: String, sequenceNumber: String) : super(countryCode, holderMnemonic, sequenceNumber) {
    }

    internal constructor(contents: ByteArray) : super(contents) {
    }
}
