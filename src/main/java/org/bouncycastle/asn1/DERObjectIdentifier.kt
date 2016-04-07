package org.bouncycastle.asn1


@Deprecated("Use ASN1ObjectIdentifier instead of this,")
class DERObjectIdentifier : ASN1ObjectIdentifier {
    constructor(identifier: String) : super(identifier) {
    }

    internal constructor(bytes: ByteArray) : super(bytes) {
    }

    internal constructor(oid: ASN1ObjectIdentifier, branch: String) : super(oid, branch) {
    }
}
