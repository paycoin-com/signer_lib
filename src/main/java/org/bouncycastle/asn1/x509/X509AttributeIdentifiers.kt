package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1ObjectIdentifier

interface X509AttributeIdentifiers {
    companion object {

        @Deprecated("use id_at_role")
        val RoleSyntax = ASN1ObjectIdentifier("2.5.4.72")

        val id_pe_ac_auditIdentity = X509ObjectIdentifiers.id_pe.branch("4")
        val id_pe_aaControls = X509ObjectIdentifiers.id_pe.branch("6")
        val id_pe_ac_proxying = X509ObjectIdentifiers.id_pe.branch("10")

        val id_ce_targetInformation = X509ObjectIdentifiers.id_ce.branch("55")

        val id_aca = X509ObjectIdentifiers.id_pkix.branch("10")

        val id_aca_authenticationInfo = id_aca.branch("1")
        val id_aca_accessIdentity = id_aca.branch("2")
        val id_aca_chargingIdentity = id_aca.branch("3")
        val id_aca_group = id_aca.branch("4")
        // { id-aca 5 } is reserved
        val id_aca_encAttrs = id_aca.branch("6")

        val id_at_role = ASN1ObjectIdentifier("2.5.4.72")
        val id_at_clearance = ASN1ObjectIdentifier("2.5.1.5.55")
    }
}
