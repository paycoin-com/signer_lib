package org.bouncycastle.asn1.iana

import org.bouncycastle.asn1.ASN1ObjectIdentifier

/**
 * IANA:
 * { iso(1) identifier-organization(3) dod(6) internet(1) } == IETF defined things
 */
interface IANAObjectIdentifiers {
    companion object {

        /** { iso(1) identifier-organization(3) dod(6) internet(1) } == IETF defined things  */
        val internet = ASN1ObjectIdentifier("1.3.6.1")
        /** 1.3.6.1.1: Internet directory: X.500  */
        val directory = internet.branch("1")
        /** 1.3.6.1.2: Internet management  */
        val mgmt = internet.branch("2")
        /** 1.3.6.1.3:  */
        val experimental = internet.branch("3")
        /** 1.3.6.1.4:  */
        val _private = internet.branch("4")
        /** 1.3.6.1.5: Security services  */
        val security = internet.branch("5")
        /** 1.3.6.1.6: SNMPv2 -- never really used  */
        val SNMPv2 = internet.branch("6")
        /** 1.3.6.1.7: mail -- never really used  */
        val mail = internet.branch("7")


        // id-SHA1 OBJECT IDENTIFIER ::=    
        // {iso(1) identified-organization(3) dod(6) internet(1) security(5) mechanisms(5) ipsec(8) isakmpOakley(1)}
        //


        /** IANA security mechanisms; 1.3.6.1.5.5  */
        val security_mechanisms = security.branch("5")
        /** IANA security nametypes;  1.3.6.1.5.6  */
        val security_nametypes = security.branch("6")

        /** PKIX base OID:            1.3.6.1.5.6.6  */
        val pkix = security_mechanisms.branch("6")


        /** IPSEC base OID:                        1.3.6.1.5.5.8  */
        val ipsec = security_mechanisms.branch("8")
        /** IPSEC ISAKMP-Oakley OID:               1.3.6.1.5.5.8.1  */
        val isakmpOakley = ipsec.branch("1")

        /** IPSEC ISAKMP-Oakley hmacMD5 OID:       1.3.6.1.5.5.8.1.1  */
        val hmacMD5 = isakmpOakley.branch("1")
        /** IPSEC ISAKMP-Oakley hmacSHA1 OID:      1.3.6.1.5.5.8.1.2  */
        val hmacSHA1 = isakmpOakley.branch("2")

        /** IPSEC ISAKMP-Oakley hmacTIGER OID:     1.3.6.1.5.5.8.1.3  */
        val hmacTIGER = isakmpOakley.branch("3")

        /** IPSEC ISAKMP-Oakley hmacRIPEMD160 OID: 1.3.6.1.5.5.8.1.4  */
        val hmacRIPEMD160 = isakmpOakley.branch("4")
    }

}
