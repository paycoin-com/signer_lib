package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers

interface CMSObjectIdentifiers {
    companion object {
        /** PKCS#7: 1.2.840.113549.1.7.1  */
        val data = PKCSObjectIdentifiers.data
        /** PKCS#7: 1.2.840.113549.1.7.2  */
        val signedData = PKCSObjectIdentifiers.signedData
        /** PKCS#7: 1.2.840.113549.1.7.3  */
        val envelopedData = PKCSObjectIdentifiers.envelopedData
        /** PKCS#7: 1.2.840.113549.1.7.4  */
        val signedAndEnvelopedData = PKCSObjectIdentifiers.signedAndEnvelopedData
        /** PKCS#7: 1.2.840.113549.1.7.5  */
        val digestedData = PKCSObjectIdentifiers.digestedData
        /** PKCS#7: 1.2.840.113549.1.7.6  */
        val encryptedData = PKCSObjectIdentifiers.encryptedData
        /** PKCS#9: 1.2.840.113549.1.9.16.1.2 -- smime ct authData  */
        val authenticatedData = PKCSObjectIdentifiers.id_ct_authData
        /** PKCS#9: 1.2.840.113549.1.9.16.1.9 -- smime ct compressedData  */
        val compressedData = PKCSObjectIdentifiers.id_ct_compressedData
        /** PKCS#9: 1.2.840.113549.1.9.16.1.23 -- smime ct authEnvelopedData  */
        val authEnvelopedData = PKCSObjectIdentifiers.id_ct_authEnvelopedData
        /** PKCS#9: 1.2.840.113549.1.9.16.1.31 -- smime ct timestampedData */
        val timestampedData = PKCSObjectIdentifiers.id_ct_timestampedData

        /**
         * The other Revocation Info arc
         *
         *
         *
         * id-ri OBJECT IDENTIFIER ::= { iso(1) identified-organization(3)
         * dod(6) internet(1) security(5) mechanisms(5) pkix(7) ri(16) }
         *
         */
        val id_ri = ASN1ObjectIdentifier("1.3.6.1.5.5.7.16")

        /** 1.3.6.1.5.5.7.16.2  */
        val id_ri_ocsp_response = id_ri.branch("2")
        /** 1.3.6.1.5.5.7.16.4  */
        val id_ri_scvp = id_ri.branch("4")
    }
}
