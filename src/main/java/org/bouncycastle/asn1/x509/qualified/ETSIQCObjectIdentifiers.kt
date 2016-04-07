package org.bouncycastle.asn1.x509.qualified

import org.bouncycastle.asn1.ASN1ObjectIdentifier

interface ETSIQCObjectIdentifiers {
    companion object {
        val id_etsi_qcs_QcCompliance = ASN1ObjectIdentifier("0.4.0.1862.1.1")
        val id_etsi_qcs_LimiteValue = ASN1ObjectIdentifier("0.4.0.1862.1.2")
        val id_etsi_qcs_RetentionPeriod = ASN1ObjectIdentifier("0.4.0.1862.1.3")
        val id_etsi_qcs_QcSSCD = ASN1ObjectIdentifier("0.4.0.1862.1.4")
    }
}
