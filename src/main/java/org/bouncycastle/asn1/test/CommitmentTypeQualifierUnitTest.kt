package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.esf.CommitmentTypeIdentifier
import org.bouncycastle.asn1.esf.CommitmentTypeQualifier
import org.bouncycastle.util.test.SimpleTest

class CommitmentTypeQualifierUnitTest : SimpleTest() {
    override fun getName(): String {
        return "CommitmentTypeQualifier"
    }

    @Throws(Exception::class)
    override fun performTest() {
        var ctq: CommitmentTypeQualifier? = CommitmentTypeQualifier(CommitmentTypeIdentifier.proofOfOrigin)

        checkConstruction(ctq, CommitmentTypeIdentifier.proofOfOrigin, null)

        val info = ASN1ObjectIdentifier("1.2")

        ctq = CommitmentTypeQualifier(CommitmentTypeIdentifier.proofOfOrigin, info)

        checkConstruction(ctq, CommitmentTypeIdentifier.proofOfOrigin, info)

        ctq = CommitmentTypeQualifier.getInstance(null)

        if (ctq != null) {
            fail("null getInstance() failed.")
        }

        try {
            CommitmentTypeQualifier.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            mv: CommitmentTypeQualifier,
            commitmenttTypeId: ASN1ObjectIdentifier,
            qualifier: ASN1Encodable?) {
        var mv = mv
        checkStatement(mv, commitmenttTypeId, qualifier)

        mv = CommitmentTypeQualifier.getInstance(mv)

        checkStatement(mv, commitmenttTypeId, qualifier)

        val aIn = ASN1InputStream(mv.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        mv = CommitmentTypeQualifier.getInstance(seq)

        checkStatement(mv, commitmenttTypeId, qualifier)
    }

    private fun checkStatement(
            ctq: CommitmentTypeQualifier,
            commitmentTypeId: ASN1ObjectIdentifier,
            qualifier: ASN1Encodable?) {
        if (!ctq.commitmentTypeIdentifier!!.equals(commitmentTypeId)) {
            fail("commitmentTypeIds don't match.")
        }

        if (qualifier != null) {
            if (!ctq.qualifier!!.equals(qualifier)) {
                fail("qualifiers don't match.")
            }
        } else if (ctq.qualifier != null) {
            fail("qualifier found when none expected.")
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(CommitmentTypeQualifierUnitTest())
        }
    }
}
