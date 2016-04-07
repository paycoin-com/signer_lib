package org.bouncycastle.asn1.smime

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DERSequence

/**
 * Handler for creating a vector S/MIME Capabilities
 */
class SMIMECapabilityVector {
    private val capabilities = ASN1EncodableVector()

    fun addCapability(
            capability: ASN1ObjectIdentifier) {
        capabilities.add(DERSequence(capability))
    }

    fun addCapability(
            capability: ASN1ObjectIdentifier,
            value: Int) {
        val v = ASN1EncodableVector()

        v.add(capability)
        v.add(ASN1Integer(value.toLong()))

        capabilities.add(DERSequence(v))
    }

    fun addCapability(
            capability: ASN1ObjectIdentifier,
            params: ASN1Encodable) {
        val v = ASN1EncodableVector()

        v.add(capability)
        v.add(params)

        capabilities.add(DERSequence(v))
    }

    fun toASN1EncodableVector(): ASN1EncodableVector {
        return capabilities
    }
}
