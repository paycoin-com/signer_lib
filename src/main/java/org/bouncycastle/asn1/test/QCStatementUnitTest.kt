package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.x509.qualified.QCStatement
import org.bouncycastle.asn1.x509.qualified.RFC3739QCObjectIdentifiers
import org.bouncycastle.asn1.x509.qualified.SemanticsInformation
import org.bouncycastle.util.test.SimpleTest

class QCStatementUnitTest : SimpleTest() {
    override fun getName(): String {
        return "QCStatement"
    }

    @Throws(Exception::class)
    override fun performTest() {
        var mv: QCStatement? = QCStatement(RFC3739QCObjectIdentifiers.id_qcs_pkixQCSyntax_v1)

        checkConstruction(mv, RFC3739QCObjectIdentifiers.id_qcs_pkixQCSyntax_v1, null)

        val info = SemanticsInformation(ASN1ObjectIdentifier("1.2"))

        mv = QCStatement(RFC3739QCObjectIdentifiers.id_qcs_pkixQCSyntax_v1, info)

        checkConstruction(mv, RFC3739QCObjectIdentifiers.id_qcs_pkixQCSyntax_v1, info)

        mv = QCStatement.getInstance(null)

        if (mv != null) {
            fail("null getInstance() failed.")
        }

        try {
            QCStatement.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            mv: QCStatement,
            statementId: ASN1ObjectIdentifier,
            statementInfo: ASN1Encodable?) {
        var mv = mv
        checkStatement(mv, statementId, statementInfo)

        mv = QCStatement.getInstance(mv)

        checkStatement(mv, statementId, statementInfo)

        val aIn = ASN1InputStream(mv.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        mv = QCStatement.getInstance(seq)

        checkStatement(mv, statementId, statementInfo)
    }

    @Throws(IOException::class)
    private fun checkStatement(
            qcs: QCStatement,
            statementId: ASN1ObjectIdentifier,
            statementInfo: ASN1Encodable?) {
        if (qcs.statementId != statementId) {
            fail("statementIds don't match.")
        }

        if (statementInfo != null) {
            if (qcs.statementInfo != statementInfo) {
                fail("statementInfos don't match.")
            }
        } else if (qcs.statementInfo != null) {
            fail("statementInfo found when none expected.")
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(QCStatementUnitTest())
        }
    }
}
