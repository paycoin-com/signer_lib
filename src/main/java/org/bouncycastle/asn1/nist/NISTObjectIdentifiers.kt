package org.bouncycastle.asn1.nist

import org.bouncycastle.asn1.ASN1ObjectIdentifier

/**

 * NIST:
 * iso/itu(2) joint-assign(16) us(840) organization(1) gov(101) csor(3)
 */
interface NISTObjectIdentifiers {
    companion object {
        //
        // nistalgorithms(4)
        //
        /** 2.16.840.1.101.3.4 -- algorithms  */
        val nistAlgorithm = ASN1ObjectIdentifier("2.16.840.1.101.3.4")

        /** 2.16.840.1.101.3.4.2  */
        val hashAlgs = nistAlgorithm.branch("2")

        /** 2.16.840.1.101.3.4.2.1  */
        val id_sha256 = hashAlgs.branch("1")
        /** 2.16.840.1.101.3.4.2.2  */
        val id_sha384 = hashAlgs.branch("2")
        /** 2.16.840.1.101.3.4.2.3  */
        val id_sha512 = hashAlgs.branch("3")
        /** 2.16.840.1.101.3.4.2.4  */
        val id_sha224 = hashAlgs.branch("4")
        /** 2.16.840.1.101.3.4.2.5  */
        val id_sha512_224 = hashAlgs.branch("5")
        /** 2.16.840.1.101.3.4.2.6  */
        val id_sha512_256 = hashAlgs.branch("6")

        /** 2.16.840.1.101.3.4.2.7  */
        val id_sha3_224 = hashAlgs.branch("7")
        /** 2.16.840.1.101.3.4.2.8  */
        val id_sha3_256 = hashAlgs.branch("8")
        /** 2.16.840.1.101.3.4.2.9  */
        val id_sha3_384 = hashAlgs.branch("9")
        /** 2.16.840.1.101.3.4.2.10  */
        val id_sha3_512 = hashAlgs.branch("10")
        /** 2.16.840.1.101.3.4.2.11  */
        val id_shake128 = hashAlgs.branch("11")
        /** 2.16.840.1.101.3.4.2.12  */
        val id_shake256 = hashAlgs.branch("12")

        /** 2.16.840.1.101.3.4.1  */
        val aes = nistAlgorithm.branch("1")

        /** 2.16.840.1.101.3.4.1.1  */
        val id_aes128_ECB = aes.branch("1")
        /** 2.16.840.1.101.3.4.1.2  */
        val id_aes128_CBC = aes.branch("2")
        /** 2.16.840.1.101.3.4.1.3  */
        val id_aes128_OFB = aes.branch("3")
        /** 2.16.840.1.101.3.4.1.4  */
        val id_aes128_CFB = aes.branch("4")
        /** 2.16.840.1.101.3.4.1.5  */
        val id_aes128_wrap = aes.branch("5")
        /** 2.16.840.1.101.3.4.1.6  */
        val id_aes128_GCM = aes.branch("6")
        /** 2.16.840.1.101.3.4.1.7  */
        val id_aes128_CCM = aes.branch("7")

        /** 2.16.840.1.101.3.4.1.21  */
        val id_aes192_ECB = aes.branch("21")
        /** 2.16.840.1.101.3.4.1.22  */
        val id_aes192_CBC = aes.branch("22")
        /** 2.16.840.1.101.3.4.1.23  */
        val id_aes192_OFB = aes.branch("23")
        /** 2.16.840.1.101.3.4.1.24  */
        val id_aes192_CFB = aes.branch("24")
        /** 2.16.840.1.101.3.4.1.25  */
        val id_aes192_wrap = aes.branch("25")
        /** 2.16.840.1.101.3.4.1.26  */
        val id_aes192_GCM = aes.branch("26")
        /** 2.16.840.1.101.3.4.1.27  */
        val id_aes192_CCM = aes.branch("27")

        /** 2.16.840.1.101.3.4.1.41  */
        val id_aes256_ECB = aes.branch("41")
        /** 2.16.840.1.101.3.4.1.42  */
        val id_aes256_CBC = aes.branch("42")
        /** 2.16.840.1.101.3.4.1.43  */
        val id_aes256_OFB = aes.branch("43")
        /** 2.16.840.1.101.3.4.1.44  */
        val id_aes256_CFB = aes.branch("44")
        /** 2.16.840.1.101.3.4.1.45  */
        val id_aes256_wrap = aes.branch("45")
        /** 2.16.840.1.101.3.4.1.46  */
        val id_aes256_GCM = aes.branch("46")
        /** 2.16.840.1.101.3.4.1.47  */
        val id_aes256_CCM = aes.branch("47")

        //
        // signatures
        //
        /** 2.16.840.1.101.3.4.3  */
        val id_dsa_with_sha2 = nistAlgorithm.branch("3")

        /** 2.16.840.1.101.3.4.3.1  */
        val dsa_with_sha224 = id_dsa_with_sha2.branch("1")
        /** 2.16.840.1.101.3.4.3.2  */
        val dsa_with_sha256 = id_dsa_with_sha2.branch("2")
        /** 2.16.840.1.101.3.4.3.3  */
        val dsa_with_sha384 = id_dsa_with_sha2.branch("3")
        /** 2.16.840.1.101.3.4.3.4  */
        val dsa_with_sha512 = id_dsa_with_sha2.branch("4")
    }
}
