package org.bouncycastle.asn1.anssi

import org.bouncycastle.asn1.ASN1ObjectIdentifier

/**
 * Object Identifiers belong to the French Agency, ANSSI.
 */
interface ANSSIObjectIdentifiers {
    companion object {
        val FRP256v1 = ASN1ObjectIdentifier("1.2.250.1.223.101.256.1")
    }
}
