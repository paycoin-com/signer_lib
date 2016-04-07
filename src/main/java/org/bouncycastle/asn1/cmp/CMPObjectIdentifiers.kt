package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1ObjectIdentifier

interface CMPObjectIdentifiers {
    companion object {
        // RFC 4210

        /** id-PasswordBasedMac OBJECT IDENTIFIER ::= {1 2 840 113533 7 66 13}  */
        val passwordBasedMac = ASN1ObjectIdentifier("1.2.840.113533.7.66.13")

        /** id-DHBasedMac OBJECT IDENTIFIER ::= {1 2 840 113533 7 66 30}  */
        val dhBasedMac = ASN1ObjectIdentifier("1.2.840.113533.7.66.30")

        // Example InfoTypeAndValue contents include, but are not limited
        // to, the following (un-comment in this ASN.1 module and use as
        // appropriate for a given environment):
        //
        //   id-it-caProtEncCert    OBJECT IDENTIFIER ::= {id-it 1}
        //      CAProtEncCertValue      ::= CMPCertificate
        //   id-it-signKeyPairTypes OBJECT IDENTIFIER ::= {id-it 2}
        //      SignKeyPairTypesValue   ::= SEQUENCE OF AlgorithmIdentifier
        //   id-it-encKeyPairTypes  OBJECT IDENTIFIER ::= {id-it 3}
        //      EncKeyPairTypesValue    ::= SEQUENCE OF AlgorithmIdentifier
        //   id-it-preferredSymmAlg OBJECT IDENTIFIER ::= {id-it 4}
        //      PreferredSymmAlgValue   ::= AlgorithmIdentifier
        //   id-it-caKeyUpdateInfo  OBJECT IDENTIFIER ::= {id-it 5}
        //      CAKeyUpdateInfoValue    ::= CAKeyUpdAnnContent
        //   id-it-currentCRL       OBJECT IDENTIFIER ::= {id-it 6}
        //      CurrentCRLValue         ::= CertificateList
        //   id-it-unsupportedOIDs  OBJECT IDENTIFIER ::= {id-it 7}
        //      UnsupportedOIDsValue    ::= SEQUENCE OF OBJECT IDENTIFIER
        //   id-it-keyPairParamReq  OBJECT IDENTIFIER ::= {id-it 10}
        //      KeyPairParamReqValue    ::= OBJECT IDENTIFIER
        //   id-it-keyPairParamRep  OBJECT IDENTIFIER ::= {id-it 11}
        //      KeyPairParamRepValue    ::= AlgorithmIdentifer
        //   id-it-revPassphrase    OBJECT IDENTIFIER ::= {id-it 12}
        //      RevPassphraseValue      ::= EncryptedValue
        //   id-it-implicitConfirm  OBJECT IDENTIFIER ::= {id-it 13}
        //      ImplicitConfirmValue    ::= NULL
        //   id-it-confirmWaitTime  OBJECT IDENTIFIER ::= {id-it 14}
        //      ConfirmWaitTimeValue    ::= GeneralizedTime
        //   id-it-origPKIMessage   OBJECT IDENTIFIER ::= {id-it 15}
        //      OrigPKIMessageValue     ::= PKIMessages
        //   id-it-suppLangTags     OBJECT IDENTIFIER ::= {id-it 16}
        //      SuppLangTagsValue       ::= SEQUENCE OF UTF8String
        //
        // where
        //
        //   id-pkix OBJECT IDENTIFIER ::= {
        //      iso(1) identified-organization(3)
        //      dod(6) internet(1) security(5) mechanisms(5) pkix(7)}
        // and
        //   id-it   OBJECT IDENTIFIER ::= {id-pkix 4}

        /** RFC 4120: it-id: PKIX.4 = 1.3.6.1.5.5.7.4  */

        /** RFC 4120: 1.3.6.1.5.5.7.4.1  */
        val it_caProtEncCert = ASN1ObjectIdentifier("1.3.6.1.5.5.7.4.1")
        /** RFC 4120: 1.3.6.1.5.5.7.4.2  */
        val it_signKeyPairTypes = ASN1ObjectIdentifier("1.3.6.1.5.5.7.4.2")
        /** RFC 4120: 1.3.6.1.5.5.7.4.3  */
        val it_encKeyPairTypes = ASN1ObjectIdentifier("1.3.6.1.5.5.7.4.3")
        /** RFC 4120: 1.3.6.1.5.5.7.4.4  */
        val it_preferredSymAlg = ASN1ObjectIdentifier("1.3.6.1.5.5.7.4.4")
        /** RFC 4120: 1.3.6.1.5.5.7.4.5  */
        val it_caKeyUpdateInfo = ASN1ObjectIdentifier("1.3.6.1.5.5.7.4.5")
        /** RFC 4120: 1.3.6.1.5.5.7.4.6  */
        val it_currentCRL = ASN1ObjectIdentifier("1.3.6.1.5.5.7.4.6")
        /** RFC 4120: 1.3.6.1.5.5.7.4.7  */
        val it_unsupportedOIDs = ASN1ObjectIdentifier("1.3.6.1.5.5.7.4.7")
        /** RFC 4120: 1.3.6.1.5.5.7.4.10  */
        val it_keyPairParamReq = ASN1ObjectIdentifier("1.3.6.1.5.5.7.4.10")
        /** RFC 4120: 1.3.6.1.5.5.7.4.11  */
        val it_keyPairParamRep = ASN1ObjectIdentifier("1.3.6.1.5.5.7.4.11")
        /** RFC 4120: 1.3.6.1.5.5.7.4.12  */
        val it_revPassphrase = ASN1ObjectIdentifier("1.3.6.1.5.5.7.4.12")
        /** RFC 4120: 1.3.6.1.5.5.7.4.13  */
        val it_implicitConfirm = ASN1ObjectIdentifier("1.3.6.1.5.5.7.4.13")
        /** RFC 4120: 1.3.6.1.5.5.7.4.14  */
        val it_confirmWaitTime = ASN1ObjectIdentifier("1.3.6.1.5.5.7.4.14")
        /** RFC 4120: 1.3.6.1.5.5.7.4.15  */
        val it_origPKIMessage = ASN1ObjectIdentifier("1.3.6.1.5.5.7.4.15")
        /** RFC 4120: 1.3.6.1.5.5.7.4.16  */
        val it_suppLangTags = ASN1ObjectIdentifier("1.3.6.1.5.5.7.4.16")

        // RFC 4211

        // id-pkix  OBJECT IDENTIFIER  ::= { iso(1) identified-organization(3)
        //     dod(6) internet(1) security(5) mechanisms(5) pkix(7) }
        //
        // arc for Internet X.509 PKI protocols and their components
        // id-pkip  OBJECT IDENTIFIER :: { id-pkix pkip(5) }
        //
        // arc for Registration Controls in CRMF
        // id-regCtrl  OBJECT IDENTIFIER ::= { id-pkip regCtrl(1) }
        //
        // arc for Registration Info in CRMF
        // id-regInfo       OBJECT IDENTIFIER ::= { id-pkip id-regInfo(2) }

        /** RFC 4211: it-pkip: PKIX.5 = 1.3.6.1.5.5.7.5  */
        val id_pkip = ASN1ObjectIdentifier("1.3.6.1.5.5.7.5")

        /** RFC 4211: it-regCtrl: 1.3.6.1.5.5.7.5.1  */
        val id_regCtrl = ASN1ObjectIdentifier("1.3.6.1.5.5.7.5.1")
        /** RFC 4211: it-regInfo: 1.3.6.1.5.5.7.5.2  */
        val id_regInfo = ASN1ObjectIdentifier("1.3.6.1.5.5.7.5.2")


        /** 1.3.6.1.5.5.7.5.1.1  */
        val regCtrl_regToken = ASN1ObjectIdentifier("1.3.6.1.5.5.7.5.1.1")
        /** 1.3.6.1.5.5.7.5.1.2  */
        val regCtrl_authenticator = ASN1ObjectIdentifier("1.3.6.1.5.5.7.5.1.2")
        /** 1.3.6.1.5.5.7.5.1.3  */
        val regCtrl_pkiPublicationInfo = ASN1ObjectIdentifier("1.3.6.1.5.5.7.5.1.3")
        /** 1.3.6.1.5.5.7.5.1.4  */
        val regCtrl_pkiArchiveOptions = ASN1ObjectIdentifier("1.3.6.1.5.5.7.5.1.4")
        /** 1.3.6.1.5.5.7.5.1.5  */
        val regCtrl_oldCertID = ASN1ObjectIdentifier("1.3.6.1.5.5.7.5.1.5")
        /** 1.3.6.1.5.5.7.5.1.6  */
        val regCtrl_protocolEncrKey = ASN1ObjectIdentifier("1.3.6.1.5.5.7.5.1.6")

        /** From RFC4210:
         * id-regCtrl-altCertTemplate OBJECT IDENTIFIER ::= {id-regCtrl 7}; 1.3.6.1.5.5.7.1.7  */
        val regCtrl_altCertTemplate = ASN1ObjectIdentifier("1.3.6.1.5.5.7.5.1.7")

        /** RFC 4211: it-regInfo-utf8Pairs: 1.3.6.1.5.5.7.5.2.1  */
        val regInfo_utf8Pairs = ASN1ObjectIdentifier("1.3.6.1.5.5.7.5.2.1")
        /** RFC 4211: it-regInfo-certReq: 1.3.6.1.5.5.7.5.2.1  */
        val regInfo_certReq = ASN1ObjectIdentifier("1.3.6.1.5.5.7.5.2.2")

        /**
         * 1.2.840.113549.1.9.16.1.21
         *
         *
         * id-ct   OBJECT IDENTIFIER ::= { id-smime  1 }  -- content types
         *
         *
         * id-ct-encKeyWithID OBJECT IDENTIFIER ::= {id-ct 21}
         */
        val ct_encKeyWithID = ASN1ObjectIdentifier("1.2.840.113549.1.9.16.1.21")
    }

}
