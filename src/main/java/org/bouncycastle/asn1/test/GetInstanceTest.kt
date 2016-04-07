package org.bouncycastle.asn1.test

import java.lang.reflect.Method
import java.math.BigInteger
import java.util.Date
import java.util.Vector

import junit.framework.TestCase
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Enumerated
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.ASN1UTCTime
import org.bouncycastle.asn1.DERBMPString
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERGeneralString
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.DERNumericString
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERPrintableString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERSet
import org.bouncycastle.asn1.DERT61String
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.DERUniversalString
import org.bouncycastle.asn1.DERVisibleString
import org.bouncycastle.asn1.cmp.CAKeyUpdAnnContent
import org.bouncycastle.asn1.cmp.CMPCertificate
import org.bouncycastle.asn1.cmp.CRLAnnContent
import org.bouncycastle.asn1.cmp.CertConfirmContent
import org.bouncycastle.asn1.cmp.CertOrEncCert
import org.bouncycastle.asn1.cmp.CertRepMessage
import org.bouncycastle.asn1.cmp.CertResponse
import org.bouncycastle.asn1.cmp.CertifiedKeyPair
import org.bouncycastle.asn1.cmp.Challenge
import org.bouncycastle.asn1.cmp.ErrorMsgContent
import org.bouncycastle.asn1.cmp.GenMsgContent
import org.bouncycastle.asn1.cmp.GenRepContent
import org.bouncycastle.asn1.cmp.InfoTypeAndValue
import org.bouncycastle.asn1.cmp.KeyRecRepContent
import org.bouncycastle.asn1.cmp.OOBCertHash
import org.bouncycastle.asn1.cmp.PBMParameter
import org.bouncycastle.asn1.cmp.PKIBody
import org.bouncycastle.asn1.cmp.PKIConfirmContent
import org.bouncycastle.asn1.cmp.PKIFailureInfo
import org.bouncycastle.asn1.cmp.PKIFreeText
import org.bouncycastle.asn1.cmp.PKIHeader
import org.bouncycastle.asn1.cmp.PKIMessage
import org.bouncycastle.asn1.cmp.PKIMessages
import org.bouncycastle.asn1.cmp.PKIStatus
import org.bouncycastle.asn1.cmp.PKIStatusInfo
import org.bouncycastle.asn1.cmp.POPODecKeyChallContent
import org.bouncycastle.asn1.cmp.POPODecKeyRespContent
import org.bouncycastle.asn1.cmp.PollRepContent
import org.bouncycastle.asn1.cmp.PollReqContent
import org.bouncycastle.asn1.cmp.ProtectedPart
import org.bouncycastle.asn1.cmp.RevAnnContent
import org.bouncycastle.asn1.cmp.RevDetails
import org.bouncycastle.asn1.cmp.RevRepContent
import org.bouncycastle.asn1.cmp.RevReqContent
import org.bouncycastle.asn1.cms.Attribute
import org.bouncycastle.asn1.cms.Attributes
import org.bouncycastle.asn1.cms.AuthEnvelopedData
import org.bouncycastle.asn1.cms.AuthenticatedData
import org.bouncycastle.asn1.cms.CompressedData
import org.bouncycastle.asn1.cms.ContentInfo
import org.bouncycastle.asn1.cms.EncryptedContentInfo
import org.bouncycastle.asn1.cms.EncryptedData
import org.bouncycastle.asn1.cms.EnvelopedData
import org.bouncycastle.asn1.cms.Evidence
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber
import org.bouncycastle.asn1.cms.KEKIdentifier
import org.bouncycastle.asn1.cms.KEKRecipientInfo
import org.bouncycastle.asn1.cms.KeyAgreeRecipientIdentifier
import org.bouncycastle.asn1.cms.KeyAgreeRecipientInfo
import org.bouncycastle.asn1.cms.KeyTransRecipientInfo
import org.bouncycastle.asn1.cms.MetaData
import org.bouncycastle.asn1.cms.OriginatorIdentifierOrKey
import org.bouncycastle.asn1.cms.OriginatorInfo
import org.bouncycastle.asn1.cms.OriginatorPublicKey
import org.bouncycastle.asn1.cms.OtherKeyAttribute
import org.bouncycastle.asn1.cms.OtherRecipientInfo
import org.bouncycastle.asn1.cms.PasswordRecipientInfo
import org.bouncycastle.asn1.cms.RecipientEncryptedKey
import org.bouncycastle.asn1.cms.RecipientIdentifier
import org.bouncycastle.asn1.cms.RecipientInfo
import org.bouncycastle.asn1.cms.RecipientKeyIdentifier
import org.bouncycastle.asn1.cms.SignerIdentifier
import org.bouncycastle.asn1.cms.SignerInfo
import org.bouncycastle.asn1.cms.TimeStampAndCRL
import org.bouncycastle.asn1.cms.TimeStampTokenEvidence
import org.bouncycastle.asn1.cms.TimeStampedData
import org.bouncycastle.asn1.cms.ecc.MQVuserKeyingMaterial
import org.bouncycastle.asn1.crmf.AttributeTypeAndValue
import org.bouncycastle.asn1.crmf.CertId
import org.bouncycastle.asn1.crmf.CertReqMessages
import org.bouncycastle.asn1.crmf.CertReqMsg
import org.bouncycastle.asn1.crmf.CertRequest
import org.bouncycastle.asn1.crmf.CertTemplate
import org.bouncycastle.asn1.crmf.Controls
import org.bouncycastle.asn1.crmf.EncKeyWithID
import org.bouncycastle.asn1.crmf.EncryptedKey
import org.bouncycastle.asn1.crmf.EncryptedValue
import org.bouncycastle.asn1.crmf.OptionalValidity
import org.bouncycastle.asn1.crmf.PKIArchiveOptions
import org.bouncycastle.asn1.crmf.PKIPublicationInfo
import org.bouncycastle.asn1.crmf.PKMACValue
import org.bouncycastle.asn1.crmf.POPOPrivKey
import org.bouncycastle.asn1.crmf.POPOSigningKey
import org.bouncycastle.asn1.crmf.POPOSigningKeyInput
import org.bouncycastle.asn1.crmf.ProofOfPossession
import org.bouncycastle.asn1.crmf.SinglePubInfo
import org.bouncycastle.asn1.cryptopro.ECGOST3410ParamSetParameters
import org.bouncycastle.asn1.cryptopro.GOST28147Parameters
import org.bouncycastle.asn1.cryptopro.GOST3410ParamSetParameters
import org.bouncycastle.asn1.cryptopro.GOST3410PublicKeyAlgParameters
import org.bouncycastle.asn1.eac.CVCertificate
import org.bouncycastle.asn1.eac.CVCertificateRequest
import org.bouncycastle.asn1.eac.CertificateBody
import org.bouncycastle.asn1.eac.PublicKeyDataObject
import org.bouncycastle.asn1.eac.RSAPublicKey
import org.bouncycastle.asn1.eac.UnsignedInteger
import org.bouncycastle.asn1.esf.CommitmentTypeIndication
import org.bouncycastle.asn1.esf.CommitmentTypeQualifier
import org.bouncycastle.asn1.esf.CompleteRevocationRefs
import org.bouncycastle.asn1.esf.CrlIdentifier
import org.bouncycastle.asn1.esf.CrlListID
import org.bouncycastle.asn1.esf.CrlOcspRef
import org.bouncycastle.asn1.esf.CrlValidatedID
import org.bouncycastle.asn1.esf.OcspIdentifier
import org.bouncycastle.asn1.esf.OcspListID
import org.bouncycastle.asn1.esf.OcspResponsesID
import org.bouncycastle.asn1.esf.OtherHash
import org.bouncycastle.asn1.esf.OtherHashAlgAndValue
import org.bouncycastle.asn1.esf.OtherRevRefs
import org.bouncycastle.asn1.esf.OtherRevVals
import org.bouncycastle.asn1.esf.RevocationValues
import org.bouncycastle.asn1.esf.SPUserNotice
import org.bouncycastle.asn1.esf.SPuri
import org.bouncycastle.asn1.esf.SigPolicyQualifierInfo
import org.bouncycastle.asn1.esf.SigPolicyQualifiers
import org.bouncycastle.asn1.esf.SignaturePolicyId
import org.bouncycastle.asn1.esf.SignaturePolicyIdentifier
import org.bouncycastle.asn1.esf.SignerAttribute
import org.bouncycastle.asn1.esf.SignerLocation
import org.bouncycastle.asn1.ess.ContentHints
import org.bouncycastle.asn1.ess.ContentIdentifier
import org.bouncycastle.asn1.ess.ESSCertID
import org.bouncycastle.asn1.ess.ESSCertIDv2
import org.bouncycastle.asn1.ess.OtherCertID
import org.bouncycastle.asn1.ess.OtherSigningCertificate
import org.bouncycastle.asn1.ess.SigningCertificate
import org.bouncycastle.asn1.ess.SigningCertificateV2
import org.bouncycastle.asn1.icao.CscaMasterList
import org.bouncycastle.asn1.icao.DataGroupHash
import org.bouncycastle.asn1.icao.LDSSecurityObject
import org.bouncycastle.asn1.icao.LDSVersionInfo
import org.bouncycastle.asn1.isismtt.ocsp.CertHash
import org.bouncycastle.asn1.isismtt.ocsp.RequestedCertificate
import org.bouncycastle.asn1.isismtt.x509.AdditionalInformationSyntax
import org.bouncycastle.asn1.isismtt.x509.AdmissionSyntax
import org.bouncycastle.asn1.isismtt.x509.Admissions
import org.bouncycastle.asn1.isismtt.x509.DeclarationOfMajority
import org.bouncycastle.asn1.isismtt.x509.MonetaryLimit
import org.bouncycastle.asn1.isismtt.x509.NamingAuthority
import org.bouncycastle.asn1.isismtt.x509.ProcurationSyntax
import org.bouncycastle.asn1.isismtt.x509.ProfessionInfo
import org.bouncycastle.asn1.isismtt.x509.Restriction
import org.bouncycastle.asn1.misc.CAST5CBCParameters
import org.bouncycastle.asn1.misc.IDEACBCPar
import org.bouncycastle.asn1.mozilla.PublicKeyAndChallenge
import org.bouncycastle.asn1.ocsp.BasicOCSPResponse
import org.bouncycastle.asn1.ocsp.CertID
import org.bouncycastle.asn1.ocsp.CertStatus
import org.bouncycastle.asn1.ocsp.CrlID
import org.bouncycastle.asn1.ocsp.OCSPRequest
import org.bouncycastle.asn1.ocsp.OCSPResponse
import org.bouncycastle.asn1.ocsp.OCSPResponseStatus
import org.bouncycastle.asn1.ocsp.Request
import org.bouncycastle.asn1.ocsp.ResponderID
import org.bouncycastle.asn1.ocsp.ResponseBytes
import org.bouncycastle.asn1.ocsp.ResponseData
import org.bouncycastle.asn1.ocsp.RevokedInfo
import org.bouncycastle.asn1.ocsp.Signature
import org.bouncycastle.asn1.ocsp.SingleResponse
import org.bouncycastle.asn1.ocsp.TBSRequest
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers
import org.bouncycastle.asn1.pkcs.AuthenticatedSafe
import org.bouncycastle.asn1.pkcs.CertificationRequest
import org.bouncycastle.asn1.pkcs.CertificationRequestInfo
import org.bouncycastle.asn1.pkcs.DHParameter
import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo
import org.bouncycastle.asn1.pkcs.MacData
import org.bouncycastle.asn1.pkcs.PBEParameter
import org.bouncycastle.asn1.pkcs.PBES2Parameters
import org.bouncycastle.asn1.pkcs.PBKDF2Params
import org.bouncycastle.asn1.pkcs.PKCS12PBEParams
import org.bouncycastle.asn1.pkcs.Pfx
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.asn1.pkcs.RC2CBCParameter
import org.bouncycastle.asn1.pkcs.RSAESOAEPparams
import org.bouncycastle.asn1.pkcs.RSAPrivateKey
import org.bouncycastle.asn1.pkcs.RSASSAPSSparams
import org.bouncycastle.asn1.pkcs.SafeBag
import org.bouncycastle.asn1.pkcs.SignedData
import org.bouncycastle.asn1.sec.ECPrivateKey
import org.bouncycastle.asn1.smime.SMIMECapabilities
import org.bouncycastle.asn1.smime.SMIMECapability
import org.bouncycastle.asn1.tsp.Accuracy
import org.bouncycastle.asn1.tsp.MessageImprint
import org.bouncycastle.asn1.tsp.TSTInfo
import org.bouncycastle.asn1.tsp.TimeStampReq
import org.bouncycastle.asn1.tsp.TimeStampResp
import org.bouncycastle.asn1.x500.DirectoryString
import org.bouncycastle.asn1.x500.RDN
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AccessDescription
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.AttCertIssuer
import org.bouncycastle.asn1.x509.AttCertValidityPeriod
import org.bouncycastle.asn1.x509.AttributeCertificate
import org.bouncycastle.asn1.x509.AttributeCertificateInfo
import org.bouncycastle.asn1.x509.AuthorityInformationAccess
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.CRLDistPoint
import org.bouncycastle.asn1.x509.CRLNumber
import org.bouncycastle.asn1.x509.CRLReason
import org.bouncycastle.asn1.x509.Certificate
import org.bouncycastle.asn1.x509.CertificateList
import org.bouncycastle.asn1.x509.CertificatePair
import org.bouncycastle.asn1.x509.CertificatePolicies
import org.bouncycastle.asn1.x509.DSAParameter
import org.bouncycastle.asn1.x509.DigestInfo
import org.bouncycastle.asn1.x509.DisplayText
import org.bouncycastle.asn1.x509.DistributionPoint
import org.bouncycastle.asn1.x509.DistributionPointName
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.asn1.x509.GeneralSubtree
import org.bouncycastle.asn1.x509.Holder
import org.bouncycastle.asn1.x509.IetfAttrSyntax
import org.bouncycastle.asn1.x509.IssuerSerial
import org.bouncycastle.asn1.x509.IssuingDistributionPoint
import org.bouncycastle.asn1.x509.NameConstraints
import org.bouncycastle.asn1.x509.NoticeReference
import org.bouncycastle.asn1.x509.ObjectDigestInfo
import org.bouncycastle.asn1.x509.PolicyInformation
import org.bouncycastle.asn1.x509.PolicyMappings
import org.bouncycastle.asn1.x509.PolicyQualifierInfo
import org.bouncycastle.asn1.x509.PrivateKeyUsagePeriod
import org.bouncycastle.asn1.x509.RoleSyntax
import org.bouncycastle.asn1.x509.SubjectDirectoryAttributes
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.asn1.x509.TBSCertList
import org.bouncycastle.asn1.x509.TBSCertificate
import org.bouncycastle.asn1.x509.TBSCertificateStructure
import org.bouncycastle.asn1.x509.Target
import org.bouncycastle.asn1.x509.TargetInformation
import org.bouncycastle.asn1.x509.Targets
import org.bouncycastle.asn1.x509.Time
import org.bouncycastle.asn1.x509.UserNotice
import org.bouncycastle.asn1.x509.V2Form
import org.bouncycastle.asn1.x509.X509CertificateStructure
import org.bouncycastle.asn1.x509.X509Extensions
import org.bouncycastle.asn1.x509.qualified.BiometricData
import org.bouncycastle.asn1.x509.qualified.Iso4217CurrencyCode
import org.bouncycastle.asn1.x509.qualified.MonetaryValue
import org.bouncycastle.asn1.x509.qualified.QCStatement
import org.bouncycastle.asn1.x509.qualified.SemanticsInformation
import org.bouncycastle.asn1.x509.qualified.TypeOfBiometricData
import org.bouncycastle.asn1.x509.sigi.NameOrPseudonym
import org.bouncycastle.asn1.x509.sigi.PersonalData
import org.bouncycastle.asn1.x9.DHDomainParameters
import org.bouncycastle.asn1.x9.DHPublicKey
import org.bouncycastle.asn1.x9.DHValidationParms
import org.bouncycastle.asn1.x9.X962Parameters
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.util.Integers
import org.bouncycastle.util.encoders.Base64

class GetInstanceTest : TestCase() {

    internal var cert1 = Base64.decode(
            "MIIDXjCCAsegAwIBAgIBBzANBgkqhkiG9w0BAQQFADCBtzELMAkGA1UEBhMCQVUx"
                    + "ETAPBgNVBAgTCFZpY3RvcmlhMRgwFgYDVQQHEw9Tb3V0aCBNZWxib3VybmUxGjAY"
                    + "BgNVBAoTEUNvbm5lY3QgNCBQdHkgTHRkMR4wHAYDVQQLExVDZXJ0aWZpY2F0ZSBB"
                    + "dXRob3JpdHkxFTATBgNVBAMTDENvbm5lY3QgNCBDQTEoMCYGCSqGSIb3DQEJARYZ"
                    + "d2VibWFzdGVyQGNvbm5lY3Q0LmNvbS5hdTAeFw0wMDA2MDIwNzU2MjFaFw0wMTA2"
                    + "MDIwNzU2MjFaMIG4MQswCQYDVQQGEwJBVTERMA8GA1UECBMIVmljdG9yaWExGDAW"
                    + "BgNVBAcTD1NvdXRoIE1lbGJvdXJuZTEaMBgGA1UEChMRQ29ubmVjdCA0IFB0eSBM"
                    + "dGQxFzAVBgNVBAsTDldlYnNlcnZlciBUZWFtMR0wGwYDVQQDExR3d3cyLmNvbm5l"
                    + "Y3Q0LmNvbS5hdTEoMCYGCSqGSIb3DQEJARYZd2VibWFzdGVyQGNvbm5lY3Q0LmNv"
                    + "bS5hdTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEArvDxclKAhyv7Q/Wmr2re"
                    + "Gw4XL9Cnh9e+6VgWy2AWNy/MVeXdlxzd7QAuc1eOWQkGQEiLPy5XQtTY+sBUJ3AO"
                    + "Rvd2fEVJIcjf29ey7bYua9J/vz5MG2KYo9/WCHIwqD9mmG9g0xLcfwq/s8ZJBswE"
                    + "7sb85VU+h94PTvsWOsWuKaECAwEAAaN3MHUwJAYDVR0RBB0wG4EZd2VibWFzdGVy"
                    + "QGNvbm5lY3Q0LmNvbS5hdTA6BglghkgBhvhCAQ0ELRYrbW9kX3NzbCBnZW5lcmF0"
                    + "ZWQgY3VzdG9tIHNlcnZlciBjZXJ0aWZpY2F0ZTARBglghkgBhvhCAQEEBAMCBkAw"
                    + "DQYJKoZIhvcNAQEEBQADgYEAotccfKpwSsIxM1Hae8DR7M/Rw8dg/RqOWx45HNVL"
                    + "iBS4/3N/TO195yeQKbfmzbAA2jbPVvIvGgTxPgO1MP4ZgvgRhasaa0qCJCkWvpM4"
                    + "yQf33vOiYQbpv4rTwzU8AmRlBG45WdjyNIigGV+oRc61aKCTnLq7zB8N3z1TF/bF"
                    + "5/8=")

    private val v2CertList = Base64.decode(
            "MIICjTCCAfowDQYJKoZIhvcNAQECBQAwXzELMAkGA1UEBhMCVVMxIDAeBgNVBAoT"
                    + "F1JTQSBEYXRhIFNlY3VyaXR5LCBJbmMuMS4wLAYDVQQLEyVTZWN1cmUgU2VydmVy"
                    + "IENlcnRpZmljYXRpb24gQXV0aG9yaXR5Fw05NTA1MDIwMjEyMjZaFw05NTA2MDEw"
                    + "MDAxNDlaMIIBaDAWAgUCQQAABBcNOTUwMjAxMTcyNDI2WjAWAgUCQQAACRcNOTUw"
                    + "MjEwMDIxNjM5WjAWAgUCQQAADxcNOTUwMjI0MDAxMjQ5WjAWAgUCQQAADBcNOTUw"
                    + "MjI1MDA0NjQ0WjAWAgUCQQAAGxcNOTUwMzEzMTg0MDQ5WjAWAgUCQQAAFhcNOTUw"
                    + "MzE1MTkxNjU0WjAWAgUCQQAAGhcNOTUwMzE1MTk0MDQxWjAWAgUCQQAAHxcNOTUw"
                    + "MzI0MTk0NDMzWjAWAgUCcgAABRcNOTUwMzI5MjAwNzExWjAWAgUCcgAAERcNOTUw"
                    + "MzMwMDIzNDI2WjAWAgUCQQAAIBcNOTUwNDA3MDExMzIxWjAWAgUCcgAAHhcNOTUw"
                    + "NDA4MDAwMjU5WjAWAgUCcgAAQRcNOTUwNDI4MTcxNzI0WjAWAgUCcgAAOBcNOTUw"
                    + "NDI4MTcyNzIxWjAWAgUCcgAATBcNOTUwNTAyMDIxMjI2WjANBgkqhkiG9w0BAQIF"
                    + "AAN+AHqOEJXSDejYy0UwxxrH/9+N2z5xu/if0J6qQmK92W0hW158wpJg+ovV3+wQ"
                    + "wvIEPRL2rocL0tKfAsVq1IawSJzSNgxG0lrcla3MrJBnZ4GaZDu4FutZh72MR3Gt"
                    + "JaAL3iTJHJD55kK2D/VoyY1djlsPuNh6AEgdVwFAyp0v")

    @Throws(Exception::class)
    private fun doFullGetInstanceTest(clazz: Class<Any>, o1: ASN1Object) {
        var m: Method

        try {
            m = clazz.getMethod("getInstance", Any::class.java)
        } catch (e: NoSuchMethodException) {
            TestCase.Companion.fail("no getInstance method found")
            return
        }

        var o2: ASN1Object? = m.invoke(clazz, *NULL_ARGS) as ASN1Object
        if (o2 != null) {
            TestCase.Companion.fail(clazz.name + " null failed")
        }

        o2 = m.invoke(clazz, o1) as ASN1Object

        if (o1 != o2 || !clazz.isInstance(o2)) {
            TestCase.Companion.fail(clazz.name + " equality failed")
        }

        o2 = m.invoke(clazz, *o1.encoded) as ASN1Object
        if (o1 != o2 || !clazz.isInstance(o2)) {
            TestCase.Companion.fail(clazz.name + " encoded equality failed")
        }

        o2 = m.invoke(clazz, o1.toASN1Primitive()) as ASN1Object
        if (o1 != o2 || !clazz.isInstance(o2)) {
            TestCase.Companion.fail(clazz.name + " sequence equality failed")
        }

        try {
            m = clazz.getMethod("getInstance", ASN1TaggedObject::class.java, java.lang.Boolean.TYPE)
        } catch (e: NoSuchMethodException) {
            return
        }

        var t: ASN1TaggedObject = DERTaggedObject(true, 0, o1)
        o2 = m.invoke(clazz, t, true) as ASN1Object
        if (o1 != o2 || !clazz.isInstance(o2)) {
            TestCase.Companion.fail(clazz.name + " tag equality failed")
        }

        t = DERTaggedObject(true, 0, o1.toASN1Primitive())
        o2 = m.invoke(clazz, t, true) as ASN1Object
        if (o1 != o2 || !clazz.isInstance(o2)) {
            TestCase.Companion.fail(clazz.name + " tag equality failed")
        }

        t = ASN1TaggedObject.getInstance(t.getEncoded())
        o2 = m.invoke(clazz, t, true) as ASN1Object
        if (o1 != o2 || !clazz.isInstance(o2)) {
            TestCase.Companion.fail(clazz.name + " tag equality failed")
        }

        t = DERTaggedObject(false, 0, o1)
        o2 = m.invoke(clazz, t, false) as ASN1Object
        if (o1 != o2 || !clazz.isInstance(o2)) {
            TestCase.Companion.fail(clazz.name + " tag equality failed")
        }

        t = DERTaggedObject(false, 0, o1.toASN1Primitive())
        o2 = m.invoke(clazz, t, false) as ASN1Object
        if (o1 != o2 || !clazz.isInstance(o2)) {
            TestCase.Companion.fail(clazz.name + " tag equality failed")
        }

        t = ASN1TaggedObject.getInstance(t.getEncoded())
        o2 = m.invoke(clazz, t, false) as ASN1Object
        if (o1 != o2 || !clazz.isInstance(o2)) {
            TestCase.Companion.fail(clazz.name + " tag equality failed")
        }
    }

    @Throws(Exception::class)
    fun testGetInstance() {
        doFullGetInstanceTest(DERPrintableString::class.java, DERPrintableString("hello world"))
        doFullGetInstanceTest(DERBMPString::class.java, DERBMPString("hello world"))
        doFullGetInstanceTest(DERUTF8String::class.java, DERUTF8String("hello world"))
        doFullGetInstanceTest(DERUniversalString::class.java, DERUniversalString(ByteArray(20)))
        doFullGetInstanceTest(DERIA5String::class.java, DERIA5String("hello world"))
        doFullGetInstanceTest(DERGeneralString::class.java, DERGeneralString("hello world"))
        doFullGetInstanceTest(DERNumericString::class.java, DERNumericString("hello world"))
        doFullGetInstanceTest(DERNumericString::class.java, DERNumericString("99999", true))
        doFullGetInstanceTest(DERT61String::class.java, DERT61String("hello world"))
        doFullGetInstanceTest(DERVisibleString::class.java, DERVisibleString("hello world"))

        doFullGetInstanceTest(ASN1Integer::class.java, ASN1Integer(1))
        doFullGetInstanceTest(ASN1GeneralizedTime::class.java, ASN1GeneralizedTime(Date()))
        doFullGetInstanceTest(ASN1UTCTime::class.java, ASN1UTCTime(Date()))
        doFullGetInstanceTest(ASN1Enumerated::class.java, ASN1Enumerated(1))

        val cmpCert = CMPCertificate(Certificate.getInstance(cert1))
        val crl = CertificateList.getInstance(v2CertList)
        val attributeCert = AttributeCertificate.getInstance(attrCert)

        doFullGetInstanceTest(CAKeyUpdAnnContent::class.java, CAKeyUpdAnnContent(cmpCert, cmpCert, cmpCert))

        CertConfirmContent.getInstance(null)
        CertifiedKeyPair.getInstance(null)
        CertOrEncCert.getInstance(null)
        CertRepMessage.getInstance(null)
        doFullGetInstanceTest(CertResponse::class.java, CertResponse(ASN1Integer(1), PKIStatusInfo(PKIStatus.granted)))
        doFullGetInstanceTest(org.bouncycastle.asn1.cmp.CertStatus::class.java, org.bouncycastle.asn1.cmp.CertStatus(ByteArray(10), BigInteger.valueOf(1), PKIStatusInfo(PKIStatus.granted)))
        doFullGetInstanceTest(Challenge::class.java, Challenge(AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE), ByteArray(10), ByteArray(10)))

        doFullGetInstanceTest(CMPCertificate::class.java, cmpCert)
        doFullGetInstanceTest(CRLAnnContent::class.java, CRLAnnContent(crl))
        doFullGetInstanceTest(ErrorMsgContent::class.java, ErrorMsgContent(PKIStatusInfo(PKIStatus.granted), ASN1Integer(1), PKIFreeText("fred")))
        GenMsgContent.getInstance(null)
        GenRepContent.getInstance(null)
        InfoTypeAndValue.getInstance(null)
        KeyRecRepContent.getInstance(null)
        OOBCertHash.getInstance(null)
        PBMParameter.getInstance(null)
        PKIBody.getInstance(null)
        PKIConfirmContent.getInstance(null)
        PKIFreeText.getInstance(null)
        doFullGetInstanceTest(PKIFreeText::class.java, PKIFreeText("hello world"))
        doFullGetInstanceTest(PKIFreeText::class.java, PKIFreeText(arrayOf("hello", "world")))
        doFullGetInstanceTest(PKIFreeText::class.java, PKIFreeText(arrayOf(DERUTF8String("hello"), DERUTF8String("world"))))
        PKIHeader.getInstance(null)
        PKIMessage.getInstance(null)
        PKIMessages.getInstance(null)
        doFullGetInstanceTest(PKIStatusInfo::class.java, PKIStatusInfo(PKIStatus.rejection, PKIFreeText("hello world"), PKIFailureInfo(PKIFailureInfo.badAlg)))
        doFullGetInstanceTest(PKIStatusInfo::class.java, PKIStatusInfo(PKIStatus.granted, PKIFreeText("hello world")))
        PKIStatus.getInstance(null)
        PollRepContent.getInstance(null)
        PollReqContent.getInstance(null)
        POPODecKeyChallContent.getInstance(null)
        POPODecKeyRespContent.getInstance(null)
        ProtectedPart.getInstance(null)
        RevAnnContent.getInstance(null)
        RevDetails.getInstance(null)
        RevRepContent.getInstance(null)
        RevReqContent.getInstance(null)
        Attribute.getInstance(null)
        Attributes.getInstance(null)
        AuthenticatedData.getInstance(null)
        AuthenticatedData.getInstance(null)
        AuthEnvelopedData.getInstance(null)
        AuthEnvelopedData.getInstance(null)
        CompressedData.getInstance(null)
        CompressedData.getInstance(null)
        ContentInfo.getInstance(null)
        EncryptedContentInfo.getInstance(null)
        EncryptedData.getInstance(null)
        EnvelopedData.getInstance(null)
        EnvelopedData.getInstance(null)
        Evidence.getInstance(null)
        IssuerAndSerialNumber.getInstance(null)
        KEKIdentifier.getInstance(null)
        KEKIdentifier.getInstance(null)
        KEKRecipientInfo.getInstance(null)
        KEKRecipientInfo.getInstance(null)
        KeyAgreeRecipientIdentifier.getInstance(null)
        KeyAgreeRecipientIdentifier.getInstance(null)
        KeyAgreeRecipientInfo.getInstance(null)
        KeyAgreeRecipientInfo.getInstance(null)
        KeyTransRecipientInfo.getInstance(null)
        MetaData.getInstance(null)
        OriginatorIdentifierOrKey.getInstance(null)
        OriginatorIdentifierOrKey.getInstance(null)
        OriginatorInfo.getInstance(null)
        OriginatorInfo.getInstance(null)
        OriginatorPublicKey.getInstance(null)
        OriginatorPublicKey.getInstance(null)
        OtherKeyAttribute.getInstance(null)
        OtherRecipientInfo.getInstance(null)
        OtherRecipientInfo.getInstance(null)
        PasswordRecipientInfo.getInstance(null)
        PasswordRecipientInfo.getInstance(null)
        RecipientEncryptedKey.getInstance(null)
        RecipientIdentifier.getInstance(null)
        RecipientInfo.getInstance(null)
        RecipientKeyIdentifier.getInstance(null)
        RecipientKeyIdentifier.getInstance(null)
        SignedData.getInstance(null)
        SignerIdentifier.getInstance(null)
        SignerInfo.getInstance(null)
        Time.getInstance(null)
        Time.getInstance(null)
        TimeStampAndCRL.getInstance(null)
        TimeStampedData.getInstance(null)
        TimeStampTokenEvidence.getInstance(null)
        AttributeTypeAndValue.getInstance(null)

        doFullGetInstanceTest(CertId::class.java, CertId(GeneralName(X500Name("CN=Test")), BigInteger.valueOf(1)))


        CertReqMessages.getInstance(null)
        CertReqMsg.getInstance(null)
        CertRequest.getInstance(null)
        CertTemplate.getInstance(null)
        Controls.getInstance(null)
        EncKeyWithID.getInstance(null)
        EncryptedKey.getInstance(null)
        EncryptedValue.getInstance(null)
        OptionalValidity.getInstance(null)
        PKIArchiveOptions.getInstance(null)
        PKIPublicationInfo.getInstance(null)
        PKMACValue.getInstance(null)
        PKMACValue.getInstance(null)
        POPOPrivKey.getInstance(null)
        POPOSigningKeyInput.getInstance(null)
        POPOSigningKey.getInstance(null)
        POPOSigningKey.getInstance(null)
        ProofOfPossession.getInstance(null)
        SinglePubInfo.getInstance(null)
        ECGOST3410ParamSetParameters.getInstance(null)
        ECGOST3410ParamSetParameters.getInstance(null)
        GOST28147Parameters.getInstance(null)
        GOST28147Parameters.getInstance(null)
        GOST3410ParamSetParameters.getInstance(null)
        GOST3410ParamSetParameters.getInstance(null)
        GOST3410PublicKeyAlgParameters.getInstance(null)
        GOST3410PublicKeyAlgParameters.getInstance(null)
        CertificateBody.getInstance(null)
        CVCertificate.getInstance(null)
        CVCertificateRequest.getInstance(null)
        PublicKeyDataObject.getInstance(null)
        UnsignedInteger.getInstance(null)
        CommitmentTypeIndication.getInstance(null)
        CommitmentTypeQualifier.getInstance(null)

        val ocspIdentifier = OcspIdentifier(ResponderID(X500Name("CN=Test")), ASN1GeneralizedTime(Date()))
        val crlListID = CrlListID(arrayOf(CrlValidatedID(OtherHash(ByteArray(20)))))
        val ocspListID = OcspListID(arrayOf(OcspResponsesID(ocspIdentifier)))
        val otherRevRefs = OtherRevRefs(ASN1ObjectIdentifier("1.2.1"), DERSequence())
        val otherRevVals = OtherRevVals(ASN1ObjectIdentifier("1.2.1"), DERSequence())
        val crlOcspRef = CrlOcspRef(crlListID, ocspListID, otherRevRefs)
        doFullGetInstanceTest(CompleteRevocationRefs::class.java, CompleteRevocationRefs(arrayOf(crlOcspRef, crlOcspRef)))

        doFullGetInstanceTest(CrlIdentifier::class.java, CrlIdentifier(X500Name("CN=Test"), ASN1UTCTime(Date()), BigInteger.valueOf(1)))


        doFullGetInstanceTest(CrlListID::class.java, crlListID)
        doFullGetInstanceTest(CrlOcspRef::class.java, crlOcspRef)
        doFullGetInstanceTest(CrlValidatedID::class.java, CrlValidatedID(OtherHash(ByteArray(20))))
        doFullGetInstanceTest(OcspIdentifier::class.java, ocspIdentifier)
        doFullGetInstanceTest(OcspListID::class.java, ocspListID)
        doFullGetInstanceTest(OcspResponsesID::class.java, OcspResponsesID(ocspIdentifier))

        val otherHashAlgAndValue = OtherHashAlgAndValue(AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE), DEROctetString(ByteArray(10)))
        doFullGetInstanceTest(OtherHashAlgAndValue::class.java, otherHashAlgAndValue)
        OtherHash.getInstance(null)
        doFullGetInstanceTest(OtherRevRefs::class.java, otherRevRefs)
        doFullGetInstanceTest(OtherRevVals::class.java, otherRevVals)
        doFullGetInstanceTest(RevocationValues::class.java, RevocationValues(arrayOf(crl), null, otherRevVals))

        val signaturePolicyId = SignaturePolicyId(ASN1ObjectIdentifier("1.2.1"), otherHashAlgAndValue)
        doFullGetInstanceTest(SignaturePolicyIdentifier::class.java, SignaturePolicyIdentifier())
        doFullGetInstanceTest(SignaturePolicyIdentifier::class.java, SignaturePolicyIdentifier(signaturePolicyId))
        doFullGetInstanceTest(SignaturePolicyId::class.java, signaturePolicyId)
        doFullGetInstanceTest(SignerAttribute::class.java, SignerAttribute(arrayOf(org.bouncycastle.asn1.x509.Attribute(ASN1ObjectIdentifier("1.2.1"), DERSet()))))
        doFullGetInstanceTest(SignerAttribute::class.java, SignerAttribute(attributeCert))

        val postalAddr = ASN1EncodableVector()

        postalAddr.add(DERUTF8String("line 1"))
        postalAddr.add(DERUTF8String("line 2"))

        doFullGetInstanceTest(SignerLocation::class.java, SignerLocation(DERUTF8String("AU"), DERUTF8String("Melbourne"), DERSequence(postalAddr)))
        doFullGetInstanceTest(SigPolicyQualifierInfo::class.java, SigPolicyQualifierInfo(ASN1ObjectIdentifier("1.2.1"), DERSequence()))
        SigPolicyQualifiers.getInstance(null)
        SPuri.getInstance(null)
        val v = Vector()

        v.add(Integers.valueOf(1))
        v.add(BigInteger.valueOf(2))
        val noticeReference = NoticeReference("BC", v)
        doFullGetInstanceTest(SPUserNotice::class.java, SPUserNotice(noticeReference, DisplayText("hello world")))
        ContentHints.getInstance(null)
        ContentIdentifier.getInstance(null)
        ESSCertID.getInstance(null)
        ESSCertIDv2.getInstance(null)
        OtherCertID.getInstance(null)
        OtherSigningCertificate.getInstance(null)
        SigningCertificate.getInstance(null)
        SigningCertificateV2.getInstance(null)
        CscaMasterList.getInstance(null)
        DataGroupHash.getInstance(null)
        LDSSecurityObject.getInstance(null)
        LDSVersionInfo.getInstance(null)
        CAST5CBCParameters.getInstance(null)
        IDEACBCPar.getInstance(null)
        PublicKeyAndChallenge.getInstance(null)
        BasicOCSPResponse.getInstance(null)
        BasicOCSPResponse.getInstance(null)

        doFullGetInstanceTest(CertID::class.java, CertID(AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE), DEROctetString(ByteArray(1)), DEROctetString(ByteArray(1)), ASN1Integer(1)))

        CertStatus.getInstance(null)
        CertStatus.getInstance(null)
        CrlID.getInstance(null)
        OCSPRequest.getInstance(null)
        OCSPRequest.getInstance(null)
        OCSPResponse.getInstance(null)
        OCSPResponse.getInstance(null)
        OCSPResponseStatus.getInstance(null)
        Request.getInstance(null)
        Request.getInstance(null)
        ResponderID.getInstance(null)
        ResponderID.getInstance(null)
        ResponseBytes.getInstance(null)
        ResponseBytes.getInstance(null)
        ResponseData.getInstance(null)
        ResponseData.getInstance(null)
        RevokedInfo.getInstance(null)
        RevokedInfo.getInstance(null)
        Signature.getInstance(null)
        Signature.getInstance(null)
        SingleResponse.getInstance(null)
        SingleResponse.getInstance(null)
        TBSRequest.getInstance(null)
        TBSRequest.getInstance(null)
        Attribute.getInstance(null)
        AuthenticatedSafe.getInstance(null)
        CertificationRequestInfo.getInstance(null)
        CertificationRequest.getInstance(null)
        ContentInfo.getInstance(null)
        DHParameter.getInstance(null)
        EncryptedData.getInstance(null)
        EncryptedPrivateKeyInfo.getInstance(null)
        AlgorithmIdentifier.getInstance(null)
        IssuerAndSerialNumber.getInstance(null)
        MacData.getInstance(null)
        PBEParameter.getInstance(null)
        PBES2Parameters.getInstance(null)
        PBKDF2Params.getInstance(null)
        Pfx.getInstance(null)
        PKCS12PBEParams.getInstance(null)
        PrivateKeyInfo.getInstance(null)
        PrivateKeyInfo.getInstance(null)
        RC2CBCParameter.getInstance(null)
        RSAESOAEPparams.getInstance(null)
        RSAPrivateKey.getInstance(null)
        RSAPrivateKey.getInstance(null)
        RSAPublicKey.getInstance(null)
        RSAPublicKey.getInstance(null)
        RSASSAPSSparams.getInstance(null)
        SafeBag.getInstance(null)
        SignedData.getInstance(null)
        SignerInfo.getInstance(null)
        ECPrivateKey.getInstance(null)
        SMIMECapabilities.getInstance(null)
        SMIMECapability.getInstance(null)
        Accuracy.getInstance(null)
        MessageImprint.getInstance(null)
        TimeStampReq.getInstance(null)
        TimeStampResp.getInstance(null)
        TSTInfo.getInstance(null)
        AttributeTypeAndValue.getInstance(null)
        DirectoryString.getInstance(null)
        DirectoryString.getInstance(null)
        RDN.getInstance(null)
        X500Name.getInstance(null)
        X500Name.getInstance(null)
        AccessDescription.getInstance(null)
        AlgorithmIdentifier.getInstance(null)
        AlgorithmIdentifier.getInstance(null)
        AttCertIssuer.getInstance(null)
        AttCertIssuer.getInstance(null)
        AttCertValidityPeriod.getInstance(null)
        AttributeCertificateInfo.getInstance(null)
        AttributeCertificateInfo.getInstance(null)
        AttributeCertificate.getInstance(null)
        Attribute.getInstance(null)
        AuthorityInformationAccess.getInstance(null)
        AuthorityKeyIdentifier.getInstance(null)
        AuthorityKeyIdentifier.getInstance(null)
        BasicConstraints.getInstance(null)
        BasicConstraints.getInstance(null)
        Certificate.getInstance(null)
        Certificate.getInstance(null)
        CertificateList.getInstance(null)
        CertificateList.getInstance(null)
        CertificatePair.getInstance(null)
        CertificatePolicies.getInstance(null)
        CertificatePolicies.getInstance(null)
        CRLDistPoint.getInstance(null)
        CRLDistPoint.getInstance(null)
        CRLNumber.getInstance(null)
        CRLReason.getInstance(null)
        DigestInfo.getInstance(null)
        DigestInfo.getInstance(null)
        DisplayText.getInstance(null)
        DisplayText.getInstance(null)
        DistributionPoint.getInstance(null)
        DistributionPoint.getInstance(null)
        DistributionPointName.getInstance(null)
        DistributionPointName.getInstance(null)
        DSAParameter.getInstance(null)
        DSAParameter.getInstance(null)
        ExtendedKeyUsage.getInstance(null)
        ExtendedKeyUsage.getInstance(null)
        Extensions.getInstance(null)
        Extensions.getInstance(null)
        GeneralName.getInstance(null)
        GeneralName.getInstance(null)
        GeneralNames.getInstance(null)
        GeneralNames.getInstance(null)

        val generalSubtree = GeneralSubtree(GeneralName(X500Name("CN=Test")))
        val algOid = ASN1ObjectIdentifier("1.2.1")
        val objectDigestInfo = ObjectDigestInfo(ObjectDigestInfo.otherObjectDigest, algOid, AlgorithmIdentifier(algOid), ByteArray(20))

        doFullGetInstanceTest(GeneralSubtree::class.java, generalSubtree)
        doFullGetInstanceTest(Holder::class.java, Holder(objectDigestInfo))
        IetfAttrSyntax.getInstance(null)
        IssuerSerial.getInstance(null)
        IssuerSerial.getInstance(null)
        IssuingDistributionPoint.getInstance(null)
        IssuingDistributionPoint.getInstance(null)
        DERBitString.getInstance(null)

        v.clear()
        v.add(generalSubtree)

        doFullGetInstanceTest(NameConstraints::class.java, NameConstraints(null, null))
        doFullGetInstanceTest(NoticeReference::class.java, noticeReference)
        doFullGetInstanceTest(ObjectDigestInfo::class.java, objectDigestInfo)

        PolicyInformation.getInstance(null)
        PolicyMappings.getInstance(null)
        PolicyQualifierInfo.getInstance(null)
        PrivateKeyUsagePeriod.getInstance(null)
        doFullGetInstanceTest(RoleSyntax::class.java, RoleSyntax(GeneralNames(GeneralName(X500Name("CN=Test"))), GeneralName(GeneralName.uniformResourceIdentifier, "http://bc")))
        org.bouncycastle.asn1.pkcs.RSAPublicKey.getInstance(null)
        RSAPublicKey.getInstance(null)
        SubjectDirectoryAttributes.getInstance(null)
        SubjectKeyIdentifier.getInstance(null)
        SubjectKeyIdentifier.getInstance(null)
        SubjectPublicKeyInfo.getInstance(null)
        SubjectPublicKeyInfo.getInstance(null)
        TargetInformation.getInstance(null)
        Target.getInstance(null)
        Targets.getInstance(null)
        TBSCertificate.getInstance(null)
        TBSCertificate.getInstance(null)
        TBSCertificateStructure.getInstance(null)
        TBSCertificateStructure.getInstance(null)
        TBSCertList.CRLEntry.getInstance(null)
        TBSCertList.getInstance(null)
        TBSCertList.getInstance(null)
        Time.getInstance(null)
        Time.getInstance(null)
        doFullGetInstanceTest(UserNotice::class.java, UserNotice(noticeReference, "hello world"))
        V2Form.getInstance(null)
        V2Form.getInstance(null)
        X509CertificateStructure.getInstance(null)
        X509CertificateStructure.getInstance(null)
        X509Extensions.getInstance(null)
        X509Extensions.getInstance(null)
        X500Name.getInstance(null)
        X500Name.getInstance(null)
        DHDomainParameters.getInstance(null)
        DHDomainParameters.getInstance(null)
        DHPublicKey.getInstance(null)
        DHPublicKey.getInstance(null)
        DHValidationParms.getInstance(null)
        DHValidationParms.getInstance(null)
        X962Parameters.getInstance(null)
        X962Parameters.getInstance(null)
        X9ECParameters.getInstance(null)
        MQVuserKeyingMaterial.getInstance(null)
        MQVuserKeyingMaterial.getInstance(null)
        CertHash.getInstance(null)
        RequestedCertificate.getInstance(null)
        RequestedCertificate.getInstance(null)
        AdditionalInformationSyntax.getInstance(null)
        Admissions.getInstance(null)
        AdmissionSyntax.getInstance(null)
        DeclarationOfMajority.getInstance(null)
        MonetaryLimit.getInstance(null)
        NamingAuthority.getInstance(null)
        NamingAuthority.getInstance(null)
        ProcurationSyntax.getInstance(null)
        ProfessionInfo.getInstance(null)
        Restriction.getInstance(null)
        BiometricData.getInstance(null)
        Iso4217CurrencyCode.getInstance(null)
        MonetaryValue.getInstance(null)
        QCStatement.getInstance(null)
        SemanticsInformation.getInstance(null)
        TypeOfBiometricData.getInstance(null)
        NameOrPseudonym.getInstance(null)
        PersonalData.getInstance(null)
    }

    override var name: String?
        get() = "GetInstanceNullTest"
        set(value: String?) {
            super.name = value
        }

    companion object {
        var attrCert = Base64.decode(
                "MIIHQDCCBqkCAQEwgZChgY2kgYowgYcxHDAaBgkqhkiG9w0BCQEWDW1sb3JjaEB2"
                        + "dC5lZHUxHjAcBgNVBAMTFU1hcmt1cyBMb3JjaCAobWxvcmNoKTEbMBkGA1UECxMS"
                        + "VmlyZ2luaWEgVGVjaCBVc2VyMRAwDgYDVQQLEwdDbGFzcyAyMQswCQYDVQQKEwJ2"
                        + "dDELMAkGA1UEBhMCVVMwgYmkgYYwgYMxGzAZBgkqhkiG9w0BCQEWDHNzaGFoQHZ0"
                        + "LmVkdTEbMBkGA1UEAxMSU3VtaXQgU2hhaCAoc3NoYWgpMRswGQYDVQQLExJWaXJn"
                        + "aW5pYSBUZWNoIFVzZXIxEDAOBgNVBAsTB0NsYXNzIDExCzAJBgNVBAoTAnZ0MQsw"
                        + "CQYDVQQGEwJVUzANBgkqhkiG9w0BAQQFAAIBBTAiGA8yMDAzMDcxODE2MDgwMloY"
                        + "DzIwMDMwNzI1MTYwODAyWjCCBU0wggVJBgorBgEEAbRoCAEBMYIFORaCBTU8UnVs"
                        + "ZSBSdWxlSWQ9IkZpbGUtUHJpdmlsZWdlLVJ1bGUiIEVmZmVjdD0iUGVybWl0Ij4K"
                        + "IDxUYXJnZXQ+CiAgPFN1YmplY3RzPgogICA8U3ViamVjdD4KICAgIDxTdWJqZWN0"
                        + "TWF0Y2ggTWF0Y2hJZD0idXJuOm9hc2lzOm5hbWVzOnRjOnhhY21sOjEuMDpmdW5j"
                        + "dGlvbjpzdHJpbmctZXF1YWwiPgogICAgIDxBdHRyaWJ1dGVWYWx1ZSBEYXRhVHlw"
                        + "ZT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEjc3RyaW5nIj4KICAg"
                        + "ICAgIENOPU1hcmt1cyBMb3JjaDwvQXR0cmlidXRlVmFsdWU+CiAgICAgPFN1Ympl"
                        + "Y3RBdHRyaWJ1dGVEZXNpZ25hdG9yIEF0dHJpYnV0ZUlkPSJ1cm46b2FzaXM6bmFt"
                        + "ZXM6dGM6eGFjbWw6MS4wOnN1YmplY3Q6c3ViamVjdC1pZCIgRGF0YVR5cGU9Imh0"
                        + "dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hI3N0cmluZyIgLz4gCiAgICA8"
                        + "L1N1YmplY3RNYXRjaD4KICAgPC9TdWJqZWN0PgogIDwvU3ViamVjdHM+CiAgPFJl"
                        + "c291cmNlcz4KICAgPFJlc291cmNlPgogICAgPFJlc291cmNlTWF0Y2ggTWF0Y2hJ"
                        + "ZD0idXJuOm9hc2lzOm5hbWVzOnRjOnhhY21sOjEuMDpmdW5jdGlvbjpzdHJpbmct"
                        + "ZXF1YWwiPgogICAgIDxBdHRyaWJ1dGVWYWx1ZSBEYXRhVHlwZT0iaHR0cDovL3d3"
                        + "dy53My5vcmcvMjAwMS9YTUxTY2hlbWEjYW55VVJJIj4KICAgICAgaHR0cDovL3p1"
                        + "bmkuY3MudnQuZWR1PC9BdHRyaWJ1dGVWYWx1ZT4KICAgICA8UmVzb3VyY2VBdHRy"
                        + "aWJ1dGVEZXNpZ25hdG9yIEF0dHJpYnV0ZUlkPSJ1cm46b2FzaXM6bmFtZXM6dGM6"
                        + "eGFjbWw6MS4wOnJlc291cmNlOnJlc291cmNlLWlkIiBEYXRhVHlwZT0iaHR0cDov"
                        + "L3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEjYW55VVJJIiAvPiAKICAgIDwvUmVz"
                        + "b3VyY2VNYXRjaD4KICAgPC9SZXNvdXJjZT4KICA8L1Jlc291cmNlcz4KICA8QWN0"
                        + "aW9ucz4KICAgPEFjdGlvbj4KICAgIDxBY3Rpb25NYXRjaCBNYXRjaElkPSJ1cm46"
                        + "b2FzaXM6bmFtZXM6dGM6eGFjbWw6MS4wOmZ1bmN0aW9uOnN0cmluZy1lcXVhbCI+"
                        + "CiAgICAgPEF0dHJpYnV0ZVZhbHVlIERhdGFUeXBlPSJodHRwOi8vd3d3LnczLm9y"
                        + "Zy8yMDAxL1hNTFNjaGVtYSNzdHJpbmciPgpEZWxlZ2F0ZSBBY2Nlc3MgICAgIDwv"
                        + "QXR0cmlidXRlVmFsdWU+CgkgIDxBY3Rpb25BdHRyaWJ1dGVEZXNpZ25hdG9yIEF0"
                        + "dHJpYnV0ZUlkPSJ1cm46b2FzaXM6bmFtZXM6dGM6eGFjbWw6MS4wOmFjdGlvbjph"
                        + "Y3Rpb24taWQiIERhdGFUeXBlPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNj"
                        + "aGVtYSNzdHJpbmciIC8+IAogICAgPC9BY3Rpb25NYXRjaD4KICAgPC9BY3Rpb24+"
                        + "CiAgPC9BY3Rpb25zPgogPC9UYXJnZXQ+CjwvUnVsZT4KMA0GCSqGSIb3DQEBBAUA"
                        + "A4GBAGiJSM48XsY90HlYxGmGVSmNR6ZW2As+bot3KAfiCIkUIOAqhcphBS23egTr"
                        + "6asYwy151HshbPNYz+Cgeqs45KkVzh7bL/0e1r8sDVIaaGIkjHK3CqBABnfSayr3"
                        + "Rd1yBoDdEv8Qb+3eEPH6ab9021AsLEnJ6LWTmybbOpMNZ3tv")

        private val NULL_ARGS = arrayOf<Any>(null)
    }
}
