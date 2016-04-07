package org.bouncycastle.asn1.smime

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers

class SMIMECapability : ASN1Object {

    var capabilityID: ASN1ObjectIdentifier? = null
        private set
    var parameters: ASN1Encodable? = null
        private set

    constructor(
            seq: ASN1Sequence) {
        capabilityID = seq.getObjectAt(0) as ASN1ObjectIdentifier

        if (seq.size() > 1) {
            parameters = seq.getObjectAt(1) as ASN1Primitive
        }
    }

    constructor(
            capabilityID: ASN1ObjectIdentifier,
            parameters: ASN1Encodable) {
        this.capabilityID = capabilityID
        this.parameters = parameters
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * SMIMECapability ::= SEQUENCE {
     * capabilityID OBJECT IDENTIFIER,
     * parameters ANY DEFINED BY capabilityID OPTIONAL
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(capabilityID)

        if (parameters != null) {
            v.add(parameters)
        }

        return DERSequence(v)
    }

    companion object {
        /**
         * general preferences
         */
        val preferSignedData = PKCSObjectIdentifiers.preferSignedData
        val canNotDecryptAny = PKCSObjectIdentifiers.canNotDecryptAny
        val sMIMECapabilitiesVersions = PKCSObjectIdentifiers.sMIMECapabilitiesVersions

        /**
         * encryption algorithms preferences
         */
        val dES_CBC = ASN1ObjectIdentifier("1.3.14.3.2.7")
        val dES_EDE3_CBC = PKCSObjectIdentifiers.des_EDE3_CBC
        val rC2_CBC = PKCSObjectIdentifiers.RC2_CBC
        val aES128_CBC = NISTObjectIdentifiers.id_aes128_CBC
        val aES192_CBC = NISTObjectIdentifiers.id_aes192_CBC
        val aES256_CBC = NISTObjectIdentifiers.id_aes256_CBC

        fun getInstance(
                obj: Any?): SMIMECapability {
            if (obj == null || obj is SMIMECapability) {
                return obj as SMIMECapability?
            }

            if (obj is ASN1Sequence) {
                return SMIMECapability(obj as ASN1Sequence?)
            }

            throw IllegalArgumentException("Invalid SMIMECapability")
        }
    }
}
