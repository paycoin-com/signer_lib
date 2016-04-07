package org.bouncycastle.asn1.sec

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers

/**
 * Certicom object identifiers
 *
 * ellipticCurve OBJECT IDENTIFIER ::= {
 * iso(1) identified-organization(3) certicom(132) curve(0)
 * }
 *
 */
interface SECObjectIdentifiers {
    companion object {
        /** Base OID: 1.3.132.0  */
        val ellipticCurve = ASN1ObjectIdentifier("1.3.132.0")

        /**  sect163k1 OID: 1.3.132.0.1  */
        val sect163k1 = ellipticCurve.branch("1")
        /**  sect163r1 OID: 1.3.132.0.2  */
        val sect163r1 = ellipticCurve.branch("2")
        /**  sect239k1 OID: 1.3.132.0.3  */
        val sect239k1 = ellipticCurve.branch("3")
        /**  sect113r1 OID: 1.3.132.0.4  */
        val sect113r1 = ellipticCurve.branch("4")
        /**  sect113r2 OID: 1.3.132.0.5  */
        val sect113r2 = ellipticCurve.branch("5")
        /**  secp112r1 OID: 1.3.132.0.6  */
        val secp112r1 = ellipticCurve.branch("6")
        /**  secp112r2 OID: 1.3.132.0.7  */
        val secp112r2 = ellipticCurve.branch("7")
        /**  secp160r1 OID: 1.3.132.0.8  */
        val secp160r1 = ellipticCurve.branch("8")
        /**  secp160k1 OID: 1.3.132.0.9  */
        val secp160k1 = ellipticCurve.branch("9")
        /**  secp256k1 OID: 1.3.132.0.10  */
        val secp256k1 = ellipticCurve.branch("10")
        /**  sect163r2 OID: 1.3.132.0.15  */
        val sect163r2 = ellipticCurve.branch("15")
        /**  sect283k1 OID: 1.3.132.0.16  */
        val sect283k1 = ellipticCurve.branch("16")
        /**  sect283r1 OID: 1.3.132.0.17  */
        val sect283r1 = ellipticCurve.branch("17")
        /**  sect131r1 OID: 1.3.132.0.22  */
        val sect131r1 = ellipticCurve.branch("22")
        /**  sect131r2 OID: 1.3.132.0.23  */
        val sect131r2 = ellipticCurve.branch("23")
        /**  sect193r1 OID: 1.3.132.0.24  */
        val sect193r1 = ellipticCurve.branch("24")
        /**  sect193r2 OID: 1.3.132.0.25  */
        val sect193r2 = ellipticCurve.branch("25")
        /**  sect233k1 OID: 1.3.132.0.26  */
        val sect233k1 = ellipticCurve.branch("26")
        /**  sect233r1 OID: 1.3.132.0.27  */
        val sect233r1 = ellipticCurve.branch("27")
        /**  secp128r1 OID: 1.3.132.0.28  */
        val secp128r1 = ellipticCurve.branch("28")
        /**  secp128r2 OID: 1.3.132.0.29  */
        val secp128r2 = ellipticCurve.branch("29")
        /**  secp160r2 OID: 1.3.132.0.30  */
        val secp160r2 = ellipticCurve.branch("30")
        /**  secp192k1 OID: 1.3.132.0.31  */
        val secp192k1 = ellipticCurve.branch("31")
        /**  secp224k1 OID: 1.3.132.0.32  */
        val secp224k1 = ellipticCurve.branch("32")
        /**  secp224r1 OID: 1.3.132.0.33  */
        val secp224r1 = ellipticCurve.branch("33")
        /**  secp384r1 OID: 1.3.132.0.34  */
        val secp384r1 = ellipticCurve.branch("34")
        /**  secp521r1 OID: 1.3.132.0.35  */
        val secp521r1 = ellipticCurve.branch("35")
        /**  sect409k1 OID: 1.3.132.0.36  */
        val sect409k1 = ellipticCurve.branch("36")
        /**  sect409r1 OID: 1.3.132.0.37  */
        val sect409r1 = ellipticCurve.branch("37")
        /**  sect571k1 OID: 1.3.132.0.38  */
        val sect571k1 = ellipticCurve.branch("38")
        /**  sect571r1 OID: 1.3.132.0.39  */
        val sect571r1 = ellipticCurve.branch("39")

        /**  secp192r1 OID: 1.3.132.0.prime192v1  */
        val secp192r1 = X9ObjectIdentifiers.prime192v1
        /**  secp256r1 OID: 1.3.132.0.prime256v1  */
        val secp256r1 = X9ObjectIdentifiers.prime256v1

        val secg_scheme = ASN1ObjectIdentifier("1.3.132.1")

        val dhSinglePass_stdDH_sha224kdf_scheme = secg_scheme.branch("11.0")
        val dhSinglePass_stdDH_sha256kdf_scheme = secg_scheme.branch("11.1")
        val dhSinglePass_stdDH_sha384kdf_scheme = secg_scheme.branch("11.2")
        val dhSinglePass_stdDH_sha512kdf_scheme = secg_scheme.branch("11.3")

        val dhSinglePass_cofactorDH_sha224kdf_scheme = secg_scheme.branch("14.0")
        val dhSinglePass_cofactorDH_sha256kdf_scheme = secg_scheme.branch("14.1")
        val dhSinglePass_cofactorDH_sha384kdf_scheme = secg_scheme.branch("14.2")
        val dhSinglePass_cofactorDH_sha512kdf_scheme = secg_scheme.branch("14.3")

        val mqvSinglePass_sha224kdf_scheme = secg_scheme.branch("15.0")
        val mqvSinglePass_sha256kdf_scheme = secg_scheme.branch("15.1")
        val mqvSinglePass_sha384kdf_scheme = secg_scheme.branch("15.2")
        val mqvSinglePass_sha512kdf_scheme = secg_scheme.branch("15.3")

        val mqvFull_sha224kdf_scheme = secg_scheme.branch("16.0")
        val mqvFull_sha256kdf_scheme = secg_scheme.branch("16.1")
        val mqvFull_sha384kdf_scheme = secg_scheme.branch("16.2")
        val mqvFull_sha512kdf_scheme = secg_scheme.branch("16.3")
    }
}
