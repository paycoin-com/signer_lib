package org.bouncycastle.asn1.esf

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers

interface ESFAttributes {
    companion object {
        val sigPolicyId = PKCSObjectIdentifiers.id_aa_ets_sigPolicyId
        val commitmentType = PKCSObjectIdentifiers.id_aa_ets_commitmentType
        val signerLocation = PKCSObjectIdentifiers.id_aa_ets_signerLocation
        val signerAttr = PKCSObjectIdentifiers.id_aa_ets_signerAttr
        val otherSigCert = PKCSObjectIdentifiers.id_aa_ets_otherSigCert
        val contentTimestamp = PKCSObjectIdentifiers.id_aa_ets_contentTimestamp
        val certificateRefs = PKCSObjectIdentifiers.id_aa_ets_certificateRefs
        val revocationRefs = PKCSObjectIdentifiers.id_aa_ets_revocationRefs
        val certValues = PKCSObjectIdentifiers.id_aa_ets_certValues
        val revocationValues = PKCSObjectIdentifiers.id_aa_ets_revocationValues
        val escTimeStamp = PKCSObjectIdentifiers.id_aa_ets_escTimeStamp
        val certCRLTimestamp = PKCSObjectIdentifiers.id_aa_ets_certCRLTimestamp
        val archiveTimestamp = PKCSObjectIdentifiers.id_aa_ets_archiveTimestamp
        val archiveTimestampV2 = PKCSObjectIdentifiers.id_aa.branch("48")
    }
}
