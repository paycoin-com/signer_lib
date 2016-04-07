package org.bouncycastle.asn1.esf

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers

interface CommitmentTypeIdentifier {
    companion object {
        val proofOfOrigin = PKCSObjectIdentifiers.id_cti_ets_proofOfOrigin
        val proofOfReceipt = PKCSObjectIdentifiers.id_cti_ets_proofOfReceipt
        val proofOfDelivery = PKCSObjectIdentifiers.id_cti_ets_proofOfDelivery
        val proofOfSender = PKCSObjectIdentifiers.id_cti_ets_proofOfSender
        val proofOfApproval = PKCSObjectIdentifiers.id_cti_ets_proofOfApproval
        val proofOfCreation = PKCSObjectIdentifiers.id_cti_ets_proofOfCreation
    }
}
