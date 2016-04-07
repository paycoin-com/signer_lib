package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.isismtt.x509.NamingAuthority
import org.bouncycastle.asn1.isismtt.x509.ProcurationSyntax
import org.bouncycastle.asn1.isismtt.x509.ProfessionInfo
import org.bouncycastle.asn1.x500.DirectoryString

class ProfessionInfoUnitTest : ASN1UnitTest() {
    override fun getName(): String {
        return "ProfessionInfo"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val auth = NamingAuthority(ASN1ObjectIdentifier("1.2.3"), "url", DirectoryString("fred"))
        val professionItems = arrayOf(DirectoryString("substitution"))
        val professionOids = arrayOf(ASN1ObjectIdentifier("1.2.3"))
        val registrationNumber = "12345"
        val addProfInfo = DEROctetString(ByteArray(20))

        var info: ProfessionInfo? = ProfessionInfo(auth, professionItems, professionOids, registrationNumber, addProfInfo)

        checkConstruction(info, auth, professionItems, professionOids, registrationNumber, addProfInfo)

        info = ProfessionInfo(null, professionItems, professionOids, registrationNumber, addProfInfo)

        checkConstruction(info, null, professionItems, professionOids, registrationNumber, addProfInfo)

        info = ProfessionInfo(auth, professionItems, null, registrationNumber, addProfInfo)

        checkConstruction(info, auth, professionItems, null, registrationNumber, addProfInfo)

        info = ProfessionInfo(auth, professionItems, professionOids, null, addProfInfo)

        checkConstruction(info, auth, professionItems, professionOids, null, addProfInfo)

        info = ProfessionInfo(auth, professionItems, professionOids, registrationNumber, null)

        checkConstruction(info, auth, professionItems, professionOids, registrationNumber, null)

        info = ProfessionInfo.getInstance(null)

        if (info != null) {
            fail("null getInstance() failed.")
        }

        try {
            ProcurationSyntax.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            profInfo: ProfessionInfo,
            auth: NamingAuthority?,
            professionItems: Array<DirectoryString>,
            professionOids: Array<ASN1ObjectIdentifier>?,
            registrationNumber: String?,
            addProfInfo: DEROctetString?) {
        var profInfo = profInfo
        checkValues(profInfo, auth, professionItems, professionOids, registrationNumber, addProfInfo)

        profInfo = ProfessionInfo.getInstance(profInfo)

        checkValues(profInfo, auth, professionItems, professionOids, registrationNumber, addProfInfo)

        val aIn = ASN1InputStream(profInfo.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        profInfo = ProfessionInfo.getInstance(seq)

        checkValues(profInfo, auth, professionItems, professionOids, registrationNumber, addProfInfo)
    }

    private fun checkValues(
            profInfo: ProfessionInfo,
            auth: NamingAuthority,
            professionItems: Array<DirectoryString>,
            professionOids: Array<ASN1ObjectIdentifier>?,
            registrationNumber: String,
            addProfInfo: DEROctetString) {
        checkOptionalField("auth", auth, profInfo.namingAuthority)
        checkMandatoryField("professionItems", professionItems[0], profInfo.getProfessionItems()[0])
        if (professionOids != null) {
            checkOptionalField("professionOids", professionOids[0], profInfo.getProfessionOIDs()[0])
        }
        checkOptionalField("registrationNumber", registrationNumber, profInfo.registrationNumber)
        checkOptionalField("addProfessionInfo", addProfInfo, profInfo.addProfessionInfo)
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(ProfessionInfoUnitTest())
        }
    }
}
