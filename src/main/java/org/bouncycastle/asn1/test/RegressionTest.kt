package org.bouncycastle.asn1.test

import org.bouncycastle.util.test.Test
import org.bouncycastle.util.test.TestResult

object RegressionTest {
    var tests = arrayOf(InputStreamTest(), EqualsAndHashCodeTest(), TagTest(), SetTest(), DERUTF8StringTest(), CertificateTest(), GenerationTest(), CMSTest(), OCSPTest(), OIDTest(), PKCS10Test(), PKCS12Test(), X509NameTest(), X500NameTest(), X509ExtensionsTest(), GeneralizedTimeTest(), BitStringTest(), MiscTest(), SMIMETest(), X9Test(), MonetaryValueUnitTest(), BiometricDataUnitTest(), Iso4217CurrencyCodeUnitTest(), SemanticsInformationUnitTest(), QCStatementUnitTest(), TypeOfBiometricDataUnitTest(), SignerLocationUnitTest(), CommitmentTypeQualifierUnitTest(), CommitmentTypeIndicationUnitTest(), EncryptedPrivateKeyInfoTest(), DataGroupHashUnitTest(), LDSSecurityObjectUnitTest(), CscaMasterListTest(), AttributeTableUnitTest(), ReasonFlagsTest(), NetscapeCertTypeTest(), PKIFailureInfoTest(), KeyUsageTest(), StringTest(), UTCTimeTest(), RequestedCertificateUnitTest(), OtherCertIDUnitTest(), OtherSigningCertificateUnitTest(), ContentHintsUnitTest(), CertHashUnitTest(), AdditionalInformationSyntaxUnitTest(), AdmissionSyntaxUnitTest(), AdmissionsUnitTest(), DeclarationOfMajorityUnitTest(), ProcurationSyntaxUnitTest(), ProfessionInfoUnitTest(), RestrictionUnitTest(), NamingAuthorityUnitTest(), MonetaryLimitUnitTest(), NameOrPseudonymUnitTest(), PersonalDataUnitTest(), DERApplicationSpecificTest(), IssuingDistributionPointUnitTest(), TargetInformationTest(), SubjectKeyIdentifierTest(), ESSCertIDv2UnitTest(), ParsingTest(), GeneralNameTest(), ObjectIdentifierTest(), RFC4519Test())

    @JvmStatic fun main(
            args: Array<String>) {
        for (i in tests.indices) {
            val result = tests[i].perform()

            if (result.exception != null) {
                result.exception.printStackTrace()
            }

            println(result)
        }
    }
}

