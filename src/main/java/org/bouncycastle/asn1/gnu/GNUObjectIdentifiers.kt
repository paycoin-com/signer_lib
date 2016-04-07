package org.bouncycastle.asn1.gnu

import org.bouncycastle.asn1.ASN1ObjectIdentifier

/**
 * GNU project OID collection
 *
 *
 * { iso(1) identifier-organization(3) dod(6) internet(1) private(4) } == IETF defined things
 */
interface GNUObjectIdentifiers {
    companion object {
        /** 1.3.6.1.4.1.11591.1 -- used by GNU Radius  */
        val GNU = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.1") // GNU Radius
        /** 1.3.6.1.4.1.11591.2 -- used by GNU PG  */
        val GnuPG = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.2") // GnuPG (Ägypten)
        /** 1.3.6.1.4.1.11591.2.1 -- notation  */
        val notation = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.2.1") // notation
        /** 1.3.6.1.4.1.11591.2.1.1 -- pkaAddress  */
        val pkaAddress = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.2.1.1") // pkaAddress
        /** 1.3.6.1.4.1.11591.3 -- GNU Radar  */
        val GnuRadar = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.3") // GNU Radar
        /** 1.3.6.1.4.1.11591.12 -- digestAlgorithm  */
        val digestAlgorithm = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.12") // digestAlgorithm
        /** 1.3.6.1.4.1.11591.12.2 -- TIGER/192  */
        val Tiger_192 = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.12.2") // TIGER/192
        /** 1.3.6.1.4.1.11591.13 -- encryptionAlgorithm  */
        val encryptionAlgorithm = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13") // encryptionAlgorithm
        /** 1.3.6.1.4.1.11591.13.2 -- Serpent  */
        val Serpent = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2") // Serpent
        /** 1.3.6.1.4.1.11591.13.2.1 -- Serpent-128-ECB  */
        val Serpent_128_ECB = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.1") // Serpent-128-ECB
        /** 1.3.6.1.4.1.11591.13.2.2 -- Serpent-128-CBC  */
        val Serpent_128_CBC = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.2") // Serpent-128-CBC
        /** 1.3.6.1.4.1.11591.13.2.3 -- Serpent-128-OFB  */
        val Serpent_128_OFB = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.3") // Serpent-128-OFB
        /** 1.3.6.1.4.1.11591.13.2.4 -- Serpent-128-CFB  */
        val Serpent_128_CFB = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.4") // Serpent-128-CFB
        /** 1.3.6.1.4.1.11591.13.2.21 -- Serpent-192-ECB  */
        val Serpent_192_ECB = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.21") // Serpent-192-ECB
        /** 1.3.6.1.4.1.11591.13.2.22 -- Serpent-192-CCB  */
        val Serpent_192_CBC = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.22") // Serpent-192-CBC
        /** 1.3.6.1.4.1.11591.13.2.23 -- Serpent-192-OFB  */
        val Serpent_192_OFB = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.23") // Serpent-192-OFB
        /** 1.3.6.1.4.1.11591.13.2.24 -- Serpent-192-CFB  */
        val Serpent_192_CFB = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.24") // Serpent-192-CFB
        /** 1.3.6.1.4.1.11591.13.2.41 -- Serpent-256-ECB  */
        val Serpent_256_ECB = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.41") // Serpent-256-ECB
        /** 1.3.6.1.4.1.11591.13.2.42 -- Serpent-256-CBC  */
        val Serpent_256_CBC = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.42") // Serpent-256-CBC
        /** 1.3.6.1.4.1.11591.13.2.43 -- Serpent-256-OFB  */
        val Serpent_256_OFB = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.43") // Serpent-256-OFB
        /** 1.3.6.1.4.1.11591.13.2.44 -- Serpent-256-CFB  */
        val Serpent_256_CFB = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.44") // Serpent-256-CFB

        /** 1.3.6.1.4.1.11591.14 -- CRC algorithms  */
        val CRC = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.14") // CRC algorithms
        /** 1.3.6.1.4.1.11591.14,1 -- CRC32  */
        val CRC32 = ASN1ObjectIdentifier("1.3.6.1.4.1.11591.14.1") // CRC 32
    }
}
