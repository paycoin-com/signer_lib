package org.bouncycastle.asn1.microsoft

import org.bouncycastle.asn1.ASN1ObjectIdentifier

/**
 * Microsoft
 *
 *
 * Object identifier base:
 *
 * iso(1) identified-organization(3) dod(6) internet(1) private(4) enterprise(1) microsoft(311)
 *
 * 1.3.6.1.4.1.311
 */
interface MicrosoftObjectIdentifiers {
    companion object {
        /** Base OID: 1.3.6.1.4.1.311  */
        val microsoft = ASN1ObjectIdentifier("1.3.6.1.4.1.311")
        /** OID: 1.3.6.1.4.1.311.20.2  */
        val microsoftCertTemplateV1 = microsoft.branch("20.2")
        /** OID: 1.3.6.1.4.1.311.21.1  */
        val microsoftCaVersion = microsoft.branch("21.1")
        /** OID: 1.3.6.1.4.1.311.21.2  */
        val microsoftPrevCaCertHash = microsoft.branch("21.2")
        /** OID: 1.3.6.1.4.1.311.21.4  */
        val microsoftCrlNextPublish = microsoft.branch("21.4")
        /** OID: 1.3.6.1.4.1.311.21.7  */
        val microsoftCertTemplateV2 = microsoft.branch("21.7")
        /** OID: 1.3.6.1.4.1.311.21.10  */
        val microsoftAppPolicies = microsoft.branch("21.10")
    }
}
