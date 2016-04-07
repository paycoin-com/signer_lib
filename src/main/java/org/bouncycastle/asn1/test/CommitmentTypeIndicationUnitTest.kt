package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.esf.CommitmentTypeIdentifier
import org.bouncycastle.asn1.esf.CommitmentTypeIndication
import org.bouncycastle.util.test.SimpleTest

class CommitmentTypeIndicationUnitTest : SimpleTest() {
    override fun getName(): String {
        return "CommitmentTypeIndication"
    }

    @Throws(Exception::class)
    override fun performTest() {
        var cti: CommitmentTypeIndication? = CommitmentTypeIndication(CommitmentTypeIdentifier.proofOfOrigin)

        checkConstruction(cti, CommitmentTypeIdentifier.proofOfOrigin, null)

        val qualifier = DERSequence(ASN1ObjectIdentifier("1.2"))

        cti = CommitmentTypeIndication(CommitmentTypeIdentifier.proofOfOrigin, qualifier)

        checkConstruction(cti, CommitmentTypeIdentifier.proofOfOrigin, qualifier)

        cti = CommitmentTypeIndication.getInstance(null)

        if (cti != null) {
            fail("null getInstance() failed.")
        }

        try {
            CommitmentTypeIndication.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            mv: CommitmentTypeIndication,
            commitmenttTypeId: ASN1ObjectIdentifier,
            qualifier: ASN1Encodable?) {
        var mv = mv
        checkStatement(mv, commitmenttTypeId, qualifier)

        mv = CommitmentTypeIndication.getInstance(mv)

        checkStatement(mv, commitmenttTypeId, qualifier)

        val aIn = ASN1InputStream(mv.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        mv = CommitmentTypeIndication.getInstance(seq)

        checkStatement(mv, commitmenttTypeId, qualifier)
    }

    private fun checkStatement(
            cti: CommitmentTypeIndication,
            commitmentTypeId: ASN1ObjectIdentifier,
            qualifier: ASN1Encodable?) {
        if (!cti.commitmentTypeId!!.equals(commitmentTypeId)) {
            fail("commitmentTypeIds don't match.")
        }

        if (qualifier != null) {
            if (!cti.commitmentTypeQualifier!!.equals(qualifier)) {
                fail("qualifiers don't match.")
            }
        } else if (cti.commitmentTypeQualifier != null) {
            fail("qualifier found when none expected.")
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(CommitmentTypeIndicationUnitTest())
        }
    }
}
