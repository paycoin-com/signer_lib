package org.bouncycastle.asn1.smime

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers

interface SMIMEAttributes {
    companion object {
        val smimeCapabilities = PKCSObjectIdentifiers.pkcs_9_at_smimeCapabilities
        val encrypKeyPref = PKCSObjectIdentifiers.id_aa_encrypKeyPref
    }
}
