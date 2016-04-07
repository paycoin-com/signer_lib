package org.bouncycastle.asn1.x509.qualified

import org.bouncycastle.asn1.ASN1ObjectIdentifier

interface RFC3739QCObjectIdentifiers {
    companion object {
        /** OID: 1.3.6.1.5.5.7.11.1  */
        val id_qcs_pkixQCSyntax_v1 = ASN1ObjectIdentifier("1.3.6.1.5.5.7.11.1")
        /** OID: 1.3.6.1.5.5.7.11.2  */
        val id_qcs_pkixQCSyntax_v2 = ASN1ObjectIdentifier("1.3.6.1.5.5.7.11.2")
    }
}
