package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1ObjectIdentifier

/**
 * PolicyQualifierId, used in the CertificatePolicies
 * X509V3 extension.

 *
 * id-qt          OBJECT IDENTIFIER ::=  { id-pkix 2 }
 * id-qt-cps      OBJECT IDENTIFIER ::=  { id-qt 1 }
 * id-qt-unotice  OBJECT IDENTIFIER ::=  { id-qt 2 }
 * PolicyQualifierId ::=
 * OBJECT IDENTIFIER (id-qt-cps | id-qt-unotice)
 *
 */
class PolicyQualifierId private constructor(id: String) : ASN1ObjectIdentifier(id) {
    companion object {
        private val id_qt = "1.3.6.1.5.5.7.2"

        val id_qt_cps = PolicyQualifierId(id_qt + ".1")
        val id_qt_unotice = PolicyQualifierId(id_qt + ".2")
    }
}
