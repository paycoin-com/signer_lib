package org.bouncycastle.asn1.pkcs

import org.bouncycastle.asn1.ASN1ObjectIdentifier

/**
 * pkcs-1 OBJECT IDENTIFIER ::=
 *
 *
 * { iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1) 1 }

 */
interface PKCSObjectIdentifiers {
    companion object {
        /** PKCS#1: 1.2.840.113549.1.1  */
        val pkcs_1 = ASN1ObjectIdentifier("1.2.840.113549.1.1")
        /** PKCS#1: 1.2.840.113549.1.1.1  */
        val rsaEncryption = pkcs_1.branch("1")
        /** PKCS#1: 1.2.840.113549.1.1.2  */
        val md2WithRSAEncryption = pkcs_1.branch("2")
        /** PKCS#1: 1.2.840.113549.1.1.3  */
        val md4WithRSAEncryption = pkcs_1.branch("3")
        /** PKCS#1: 1.2.840.113549.1.1.4  */
        val md5WithRSAEncryption = pkcs_1.branch("4")
        /** PKCS#1: 1.2.840.113549.1.1.5  */
        val sha1WithRSAEncryption = pkcs_1.branch("5")
        /** PKCS#1: 1.2.840.113549.1.1.6  */
        val srsaOAEPEncryptionSET = pkcs_1.branch("6")
        /** PKCS#1: 1.2.840.113549.1.1.7  */
        val id_RSAES_OAEP = pkcs_1.branch("7")
        /** PKCS#1: 1.2.840.113549.1.1.8  */
        val id_mgf1 = pkcs_1.branch("8")
        /** PKCS#1: 1.2.840.113549.1.1.9  */
        val id_pSpecified = pkcs_1.branch("9")
        /** PKCS#1: 1.2.840.113549.1.1.10  */
        val id_RSASSA_PSS = pkcs_1.branch("10")
        /** PKCS#1: 1.2.840.113549.1.1.11  */
        val sha256WithRSAEncryption = pkcs_1.branch("11")
        /** PKCS#1: 1.2.840.113549.1.1.12  */
        val sha384WithRSAEncryption = pkcs_1.branch("12")
        /** PKCS#1: 1.2.840.113549.1.1.13  */
        val sha512WithRSAEncryption = pkcs_1.branch("13")
        /** PKCS#1: 1.2.840.113549.1.1.14  */
        val sha224WithRSAEncryption = pkcs_1.branch("14")

        //
        // pkcs-3 OBJECT IDENTIFIER ::= {
        //       iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1) 3 }
        //
        /** PKCS#3: 1.2.840.113549.1.3  */
        val pkcs_3 = ASN1ObjectIdentifier("1.2.840.113549.1.3")
        /** PKCS#3: 1.2.840.113549.1.3.1  */
        val dhKeyAgreement = pkcs_3.branch("1")

        //
        // pkcs-5 OBJECT IDENTIFIER ::= {
        //       iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1) 5 }
        //
        /** PKCS#5: 1.2.840.113549.1.5  */
        val pkcs_5 = ASN1ObjectIdentifier("1.2.840.113549.1.5")

        /** PKCS#5: 1.2.840.113549.1.5.1  */
        val pbeWithMD2AndDES_CBC = pkcs_5.branch("1")
        /** PKCS#5: 1.2.840.113549.1.5.4  */
        val pbeWithMD2AndRC2_CBC = pkcs_5.branch("4")
        /** PKCS#5: 1.2.840.113549.1.5.3  */
        val pbeWithMD5AndDES_CBC = pkcs_5.branch("3")
        /** PKCS#5: 1.2.840.113549.1.5.6  */
        val pbeWithMD5AndRC2_CBC = pkcs_5.branch("6")
        /** PKCS#5: 1.2.840.113549.1.5.10  */
        val pbeWithSHA1AndDES_CBC = pkcs_5.branch("10")
        /** PKCS#5: 1.2.840.113549.1.5.11  */
        val pbeWithSHA1AndRC2_CBC = pkcs_5.branch("11")
        /** PKCS#5: 1.2.840.113549.1.5.13  */
        val id_PBES2 = pkcs_5.branch("13")
        /** PKCS#5: 1.2.840.113549.1.5.12  */
        val id_PBKDF2 = pkcs_5.branch("12")

        //
        // encryptionAlgorithm OBJECT IDENTIFIER ::= {
        //       iso(1) member-body(2) us(840) rsadsi(113549) 3 }
        //
        /**  1.2.840.113549.3  */
        val encryptionAlgorithm = ASN1ObjectIdentifier("1.2.840.113549.3")

        /**  1.2.840.113549.3.7  */
        val des_EDE3_CBC = encryptionAlgorithm.branch("7")
        /**  1.2.840.113549.3.2  */
        val RC2_CBC = encryptionAlgorithm.branch("2")
        /**  1.2.840.113549.3.4  */
        val rc4 = encryptionAlgorithm.branch("4")

        //
        // object identifiers for digests
        //
        /**  1.2.840.113549.2  */
        val digestAlgorithm = ASN1ObjectIdentifier("1.2.840.113549.2")
        //
        // md2 OBJECT IDENTIFIER ::=
        //      {iso(1) member-body(2) US(840) rsadsi(113549) digestAlgorithm(2) 2}
        //
        /**  1.2.840.113549.2.2  */
        val md2 = digestAlgorithm.branch("2")

        //
        // md4 OBJECT IDENTIFIER ::=
        //      {iso(1) member-body(2) US(840) rsadsi(113549) digestAlgorithm(2) 4}
        //
        /**  1.2.840.113549.2.4  */
        val md4 = digestAlgorithm.branch("4")

        //
        // md5 OBJECT IDENTIFIER ::=
        //      {iso(1) member-body(2) US(840) rsadsi(113549) digestAlgorithm(2) 5}
        //
        /**  1.2.840.113549.2.5  */
        val md5 = digestAlgorithm.branch("5")

        /**  1.2.840.113549.2.7  */
        val id_hmacWithSHA1 = digestAlgorithm.branch("7").intern()
        /**  1.2.840.113549.2.8  */
        val id_hmacWithSHA224 = digestAlgorithm.branch("8").intern()
        /**  1.2.840.113549.2.9  */
        val id_hmacWithSHA256 = digestAlgorithm.branch("9").intern()
        /**  1.2.840.113549.2.10  */
        val id_hmacWithSHA384 = digestAlgorithm.branch("10").intern()
        /**  1.2.840.113549.2.11  */
        val id_hmacWithSHA512 = digestAlgorithm.branch("11").intern()

        //
        // pkcs-7 OBJECT IDENTIFIER ::= {
        //       iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1) 7 }
        //
        /** pkcs#7: 1.2.840.113549.1.7  */
        val pkcs_7 = ASN1ObjectIdentifier("1.2.840.113549.1.7").intern()
        /** PKCS#7: 1.2.840.113549.1.7.1  */
        val data = ASN1ObjectIdentifier("1.2.840.113549.1.7.1").intern()
        /** PKCS#7: 1.2.840.113549.1.7.2  */
        val signedData = ASN1ObjectIdentifier("1.2.840.113549.1.7.2").intern()
        /** PKCS#7: 1.2.840.113549.1.7.3  */
        val envelopedData = ASN1ObjectIdentifier("1.2.840.113549.1.7.3").intern()
        /** PKCS#7: 1.2.840.113549.1.7.4  */
        val signedAndEnvelopedData = ASN1ObjectIdentifier("1.2.840.113549.1.7.4").intern()
        /** PKCS#7: 1.2.840.113549.1.7.5  */
        val digestedData = ASN1ObjectIdentifier("1.2.840.113549.1.7.5").intern()
        /** PKCS#7: 1.2.840.113549.1.7.76  */
        val encryptedData = ASN1ObjectIdentifier("1.2.840.113549.1.7.6").intern()

        //
        // pkcs-9 OBJECT IDENTIFIER ::= {
        //       iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1) 9 }
        //
        /** PKCS#9: 1.2.840.113549.1.9  */
        val pkcs_9 = ASN1ObjectIdentifier("1.2.840.113549.1.9")

        /** PKCS#9: 1.2.840.113549.1.9.1  */
        val pkcs_9_at_emailAddress = pkcs_9.branch("1").intern()
        /** PKCS#9: 1.2.840.113549.1.9.2  */
        val pkcs_9_at_unstructuredName = pkcs_9.branch("2").intern()
        /** PKCS#9: 1.2.840.113549.1.9.3  */
        val pkcs_9_at_contentType = pkcs_9.branch("3").intern()
        /** PKCS#9: 1.2.840.113549.1.9.4  */
        val pkcs_9_at_messageDigest = pkcs_9.branch("4").intern()
        /** PKCS#9: 1.2.840.113549.1.9.5  */
        val pkcs_9_at_signingTime = pkcs_9.branch("5").intern()
        /** PKCS#9: 1.2.840.113549.1.9.6  */
        val pkcs_9_at_counterSignature = pkcs_9.branch("6").intern()
        /** PKCS#9: 1.2.840.113549.1.9.7  */
        val pkcs_9_at_challengePassword = pkcs_9.branch("7").intern()
        /** PKCS#9: 1.2.840.113549.1.9.8  */
        val pkcs_9_at_unstructuredAddress = pkcs_9.branch("8").intern()
        /** PKCS#9: 1.2.840.113549.1.9.9  */
        val pkcs_9_at_extendedCertificateAttributes = pkcs_9.branch("9").intern()

        /** PKCS#9: 1.2.840.113549.1.9.13  */
        val pkcs_9_at_signingDescription = pkcs_9.branch("13").intern()
        /** PKCS#9: 1.2.840.113549.1.9.14  */
        val pkcs_9_at_extensionRequest = pkcs_9.branch("14").intern()
        /** PKCS#9: 1.2.840.113549.1.9.15  */
        val pkcs_9_at_smimeCapabilities = pkcs_9.branch("15").intern()
        /** PKCS#9: 1.2.840.113549.1.9.16  */
        val id_smime = pkcs_9.branch("16").intern()

        /** PKCS#9: 1.2.840.113549.1.9.20  */
        val pkcs_9_at_friendlyName = pkcs_9.branch("20").intern()
        /** PKCS#9: 1.2.840.113549.1.9.21  */
        val pkcs_9_at_localKeyId = pkcs_9.branch("21").intern()

        /** PKCS#9: 1.2.840.113549.1.9.22.1
         */
        @Deprecated("use x509Certificate instead ")
        val x509certType = pkcs_9.branch("22.1")

        /** PKCS#9: 1.2.840.113549.1.9.22  */
        val certTypes = pkcs_9.branch("22")
        /** PKCS#9: 1.2.840.113549.1.9.22.1  */
        val x509Certificate = certTypes.branch("1").intern()
        /** PKCS#9: 1.2.840.113549.1.9.22.2  */
        val sdsiCertificate = certTypes.branch("2").intern()

        /** PKCS#9: 1.2.840.113549.1.9.23  */
        val crlTypes = pkcs_9.branch("23")
        /** PKCS#9: 1.2.840.113549.1.9.23.1  */
        val x509Crl = crlTypes.branch("1").intern()

        /** RFC 6211 -  id-aa-cmsAlgorithmProtect OBJECT IDENTIFIER ::= {
         * iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1)
         * pkcs9(9) 52 }   */
        val id_aa_cmsAlgorithmProtect = pkcs_9.branch("52").intern()

        //
        // SMIME capability sub oids.
        //
        /** PKCS#9: 1.2.840.113549.1.9.15.1 -- smime capability  */
        val preferSignedData = pkcs_9.branch("15.1")
        /** PKCS#9: 1.2.840.113549.1.9.15.2 -- smime capability   */
        val canNotDecryptAny = pkcs_9.branch("15.2")
        /** PKCS#9: 1.2.840.113549.1.9.15.3 -- smime capability   */
        val sMIMECapabilitiesVersions = pkcs_9.branch("15.3")

        //
        // id-ct OBJECT IDENTIFIER ::= {iso(1) member-body(2) usa(840)
        // rsadsi(113549) pkcs(1) pkcs-9(9) smime(16) ct(1)}
        //
        /** PKCS#9: 1.2.840.113549.1.9.16.1 -- smime ct  */
        val id_ct = ASN1ObjectIdentifier("1.2.840.113549.1.9.16.1")

        /** PKCS#9: 1.2.840.113549.1.9.16.1.2 -- smime ct authData  */
        val id_ct_authData = id_ct.branch("2")
        /** PKCS#9: 1.2.840.113549.1.9.16.1.4 -- smime ct TSTInfo */
        val id_ct_TSTInfo = id_ct.branch("4")
        /** PKCS#9: 1.2.840.113549.1.9.16.1.9 -- smime ct compressedData  */
        val id_ct_compressedData = id_ct.branch("9")
        /** PKCS#9: 1.2.840.113549.1.9.16.1.23 -- smime ct authEnvelopedData  */
        val id_ct_authEnvelopedData = id_ct.branch("23")
        /** PKCS#9: 1.2.840.113549.1.9.16.1.31 -- smime ct timestampedData */
        val id_ct_timestampedData = id_ct.branch("31")


        /** S/MIME: Algorithm Identifiers ; 1.2.840.113549.1.9.16.3  */
        val id_alg = id_smime.branch("3")
        /** PKCS#9: 1.2.840.113549.1.9.16.3.9  */
        val id_alg_PWRI_KEK = id_alg.branch("9")
        /**
         *
         * -- RSA-KEM Key Transport Algorithm

         * id-rsa-kem OID ::= {
         * iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1)
         * pkcs-9(9) smime(16) alg(3) 14
         * }
         *
         */
        val id_rsa_KEM = id_alg.branch("14")

        //
        // id-cti OBJECT IDENTIFIER ::= {iso(1) member-body(2) usa(840)
        // rsadsi(113549) pkcs(1) pkcs-9(9) smime(16) cti(6)}
        //
        /** PKCS#9: 1.2.840.113549.1.9.16.6 -- smime cti  */
        val id_cti = ASN1ObjectIdentifier("1.2.840.113549.1.9.16.6")

        /** PKCS#9: 1.2.840.113549.1.9.16.6.1 -- smime cti proofOfOrigin  */
        val id_cti_ets_proofOfOrigin = id_cti.branch("1")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2 -- smime cti proofOfReceipt */
        val id_cti_ets_proofOfReceipt = id_cti.branch("2")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.3 -- smime cti proofOfDelivery  */
        val id_cti_ets_proofOfDelivery = id_cti.branch("3")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.4 -- smime cti proofOfSender  */
        val id_cti_ets_proofOfSender = id_cti.branch("4")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.5 -- smime cti proofOfApproval  */
        val id_cti_ets_proofOfApproval = id_cti.branch("5")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.6 -- smime cti proofOfCreation  */
        val id_cti_ets_proofOfCreation = id_cti.branch("6")

        //
        // id-aa OBJECT IDENTIFIER ::= {iso(1) member-body(2) usa(840)
        // rsadsi(113549) pkcs(1) pkcs-9(9) smime(16) attributes(2)}
        //
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2 - smime attributes  */
        val id_aa = ASN1ObjectIdentifier("1.2.840.113549.1.9.16.2")


        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.1 -- smime attribute receiptRequest  */
        val id_aa_receiptRequest = id_aa.branch("1")

        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.4 - See [RFC 2634](http://tools.ietf.org/html/rfc2634)  */
        val id_aa_contentHint = id_aa.branch("4") // See RFC 2634
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.5  */
        val id_aa_msgSigDigest = id_aa.branch("5")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.10  */
        val id_aa_contentReference = id_aa.branch("10")
        /*
     * id-aa-encrypKeyPref OBJECT IDENTIFIER ::= {id-aa 11}
     * 
     */
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.11  */
        val id_aa_encrypKeyPref = id_aa.branch("11")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.12  */
        val id_aa_signingCertificate = id_aa.branch("12")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.47  */
        val id_aa_signingCertificateV2 = id_aa.branch("47")

        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.7 - See [RFC 2634](http://tools.ietf.org/html/rfc2634)  */
        val id_aa_contentIdentifier = id_aa.branch("7") // See RFC 2634

        /*
     * RFC 3126
     */
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.14 - [RFC 3126](http://tools.ietf.org/html/rfc3126)  */
        val id_aa_signatureTimeStampToken = id_aa.branch("14")

        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.15 - [RFC 3126](http://tools.ietf.org/html/rfc3126)  */
        val id_aa_ets_sigPolicyId = id_aa.branch("15")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.16 - [RFC 3126](http://tools.ietf.org/html/rfc3126)  */
        val id_aa_ets_commitmentType = id_aa.branch("16")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.17 - [RFC 3126](http://tools.ietf.org/html/rfc3126)  */
        val id_aa_ets_signerLocation = id_aa.branch("17")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.18 - [RFC 3126](http://tools.ietf.org/html/rfc3126)  */
        val id_aa_ets_signerAttr = id_aa.branch("18")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.19 - [RFC 3126](http://tools.ietf.org/html/rfc3126)  */
        val id_aa_ets_otherSigCert = id_aa.branch("19")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.20 - [RFC 3126](http://tools.ietf.org/html/rfc3126)  */
        val id_aa_ets_contentTimestamp = id_aa.branch("20")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.21 - [RFC 3126](http://tools.ietf.org/html/rfc3126)  */
        val id_aa_ets_certificateRefs = id_aa.branch("21")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.22 - [RFC 3126](http://tools.ietf.org/html/rfc3126)  */
        val id_aa_ets_revocationRefs = id_aa.branch("22")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.23 - [RFC 3126](http://tools.ietf.org/html/rfc3126)  */
        val id_aa_ets_certValues = id_aa.branch("23")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.24 - [RFC 3126](http://tools.ietf.org/html/rfc3126)  */
        val id_aa_ets_revocationValues = id_aa.branch("24")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.25 - [RFC 3126](http://tools.ietf.org/html/rfc3126)  */
        val id_aa_ets_escTimeStamp = id_aa.branch("25")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.26 - [RFC 3126](http://tools.ietf.org/html/rfc3126)  */
        val id_aa_ets_certCRLTimestamp = id_aa.branch("26")
        /** PKCS#9: 1.2.840.113549.1.9.16.6.2.27 - [RFC 3126](http://tools.ietf.org/html/rfc3126)  */
        val id_aa_ets_archiveTimestamp = id_aa.branch("27")


        @Deprecated("use id_aa_ets_sigPolicyId instead ")
        val id_aa_sigPolicyId = id_aa_ets_sigPolicyId

        @Deprecated("use id_aa_ets_commitmentType instead ")
        val id_aa_commitmentType = id_aa_ets_commitmentType

        @Deprecated("use id_aa_ets_signerLocation instead ")
        val id_aa_signerLocation = id_aa_ets_signerLocation

        @Deprecated("use id_aa_ets_otherSigCert instead ")
        val id_aa_otherSigCert = id_aa_ets_otherSigCert

        /**
         * id-spq OBJECT IDENTIFIER ::= {iso(1) member-body(2) usa(840)
         * rsadsi(113549) pkcs(1) pkcs-9(9) smime(16) id-spq(5)};
         *
         *
         * 1.2.840.113549.1.9.16.5
         */
        val id_spq = "1.2.840.113549.1.9.16.5"

        /** SMIME SPQ URI:     1.2.840.113549.1.9.16.5.1  */
        val id_spq_ets_uri = ASN1ObjectIdentifier(id_spq + ".1")
        /** SMIME SPQ UNOTICE: 1.2.840.113549.1.9.16.5.2  */
        val id_spq_ets_unotice = ASN1ObjectIdentifier(id_spq + ".2")

        //
        // pkcs-12 OBJECT IDENTIFIER ::= {
        //       iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1) 12 }
        //
        /** PKCS#12: 1.2.840.113549.1.12  */
        val pkcs_12 = ASN1ObjectIdentifier("1.2.840.113549.1.12")
        /** PKCS#12: 1.2.840.113549.1.12.10.1  */
        val bagtypes = pkcs_12.branch("10.1")

        /** PKCS#12: 1.2.840.113549.1.12.10.1.1  */
        val keyBag = bagtypes.branch("1")
        /** PKCS#12: 1.2.840.113549.1.12.10.1.2  */
        val pkcs8ShroudedKeyBag = bagtypes.branch("2")
        /** PKCS#12: 1.2.840.113549.1.12.10.1.3  */
        val certBag = bagtypes.branch("3")
        /** PKCS#12: 1.2.840.113549.1.12.10.1.4  */
        val crlBag = bagtypes.branch("4")
        /** PKCS#12: 1.2.840.113549.1.12.10.1.5  */
        val secretBag = bagtypes.branch("5")
        /** PKCS#12: 1.2.840.113549.1.12.10.1.6  */
        val safeContentsBag = bagtypes.branch("6")

        /** PKCS#12: 1.2.840.113549.1.12.1  */
        val pkcs_12PbeIds = pkcs_12.branch("1")

        /** PKCS#12: 1.2.840.113549.1.12.1.1  */
        val pbeWithSHAAnd128BitRC4 = pkcs_12PbeIds.branch("1")
        /** PKCS#12: 1.2.840.113549.1.12.1.2  */
        val pbeWithSHAAnd40BitRC4 = pkcs_12PbeIds.branch("2")
        /** PKCS#12: 1.2.840.113549.1.12.1.3  */
        val pbeWithSHAAnd3_KeyTripleDES_CBC = pkcs_12PbeIds.branch("3")
        /** PKCS#12: 1.2.840.113549.1.12.1.4  */
        val pbeWithSHAAnd2_KeyTripleDES_CBC = pkcs_12PbeIds.branch("4")
        /** PKCS#12: 1.2.840.113549.1.12.1.5  */
        val pbeWithSHAAnd128BitRC2_CBC = pkcs_12PbeIds.branch("5")
        /** PKCS#12: 1.2.840.113549.1.12.1.6  */
        val pbeWithSHAAnd40BitRC2_CBC = pkcs_12PbeIds.branch("6")

        /**
         * PKCS#12: 1.2.840.113549.1.12.1.6
         */
        @Deprecated("use pbeWithSHAAnd40BitRC2_CBC")
        val pbewithSHAAnd40BitRC2_CBC = pkcs_12PbeIds.branch("6")

        /** PKCS#9: 1.2.840.113549.1.9.16.3.6  */
        val id_alg_CMS3DESwrap = ASN1ObjectIdentifier("1.2.840.113549.1.9.16.3.6")
        /** PKCS#9: 1.2.840.113549.1.9.16.3.7  */
        val id_alg_CMSRC2wrap = ASN1ObjectIdentifier("1.2.840.113549.1.9.16.3.7")
        /** PKCS#9: 1.2.840.113549.1.9.16.3.5  */
        val id_alg_ESDH = ASN1ObjectIdentifier("1.2.840.113549.1.9.16.3.5")
        /** PKCS#9: 1.2.840.113549.1.9.16.3.10  */
        val id_alg_SSDH = ASN1ObjectIdentifier("1.2.840.113549.1.9.16.3.10")
    }
}

