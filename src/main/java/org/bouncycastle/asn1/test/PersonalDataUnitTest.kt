package org.bouncycastle.asn1.test

import java.io.IOException
import java.math.BigInteger

import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.x500.DirectoryString
import org.bouncycastle.asn1.x509.sigi.NameOrPseudonym
import org.bouncycastle.asn1.x509.sigi.PersonalData

class PersonalDataUnitTest : ASN1UnitTest() {
    override fun getName(): String {
        return "PersonalData"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val nameOrPseudonym = NameOrPseudonym("pseudonym")
        val nameDistinguisher = BigInteger.valueOf(10)
        val dateOfBirth = ASN1GeneralizedTime("20070315173729Z")
        val placeOfBirth = DirectoryString("placeOfBirth")
        val gender = "M"
        val postalAddress = DirectoryString("address")

        var data: PersonalData? = PersonalData(nameOrPseudonym, nameDistinguisher, dateOfBirth, placeOfBirth, gender, postalAddress)

        checkConstruction(data, nameOrPseudonym, nameDistinguisher, dateOfBirth, placeOfBirth, gender, postalAddress)

        data = PersonalData(nameOrPseudonym, null, dateOfBirth, placeOfBirth, gender, postalAddress)

        checkConstruction(data, nameOrPseudonym, null, dateOfBirth, placeOfBirth, gender, postalAddress)

        data = PersonalData(nameOrPseudonym, nameDistinguisher, null, placeOfBirth, gender, postalAddress)

        checkConstruction(data, nameOrPseudonym, nameDistinguisher, null, placeOfBirth, gender, postalAddress)

        data = PersonalData(nameOrPseudonym, nameDistinguisher, dateOfBirth, null, gender, postalAddress)

        checkConstruction(data, nameOrPseudonym, nameDistinguisher, dateOfBirth, null, gender, postalAddress)

        data = PersonalData(nameOrPseudonym, nameDistinguisher, dateOfBirth, placeOfBirth, null, postalAddress)

        checkConstruction(data, nameOrPseudonym, nameDistinguisher, dateOfBirth, placeOfBirth, null, postalAddress)

        data = PersonalData(nameOrPseudonym, nameDistinguisher, dateOfBirth, placeOfBirth, gender, null)

        checkConstruction(data, nameOrPseudonym, nameDistinguisher, dateOfBirth, placeOfBirth, gender, null)

        data = PersonalData.getInstance(null)

        if (data != null) {
            fail("null getInstance() failed.")
        }

        try {
            PersonalData.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            data: PersonalData,
            nameOrPseudonym: NameOrPseudonym,
            nameDistinguisher: BigInteger?,
            dateOfBirth: ASN1GeneralizedTime?,
            placeOfBirth: DirectoryString?,
            gender: String?,
            postalAddress: DirectoryString?) {
        var data = data
        checkValues(data, nameOrPseudonym, nameDistinguisher, dateOfBirth, placeOfBirth, gender, postalAddress)

        data = PersonalData.getInstance(data)

        checkValues(data, nameOrPseudonym, nameDistinguisher, dateOfBirth, placeOfBirth, gender, postalAddress)

        val aIn = ASN1InputStream(data.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        data = PersonalData.getInstance(seq)

        checkValues(data, nameOrPseudonym, nameDistinguisher, dateOfBirth, placeOfBirth, gender, postalAddress)
    }

    private fun checkValues(
            data: PersonalData,
            nameOrPseudonym: NameOrPseudonym,
            nameDistinguisher: BigInteger,
            dateOfBirth: ASN1GeneralizedTime,
            placeOfBirth: DirectoryString,
            gender: String,
            postalAddress: DirectoryString) {
        checkMandatoryField("nameOrPseudonym", nameOrPseudonym, data.nameOrPseudonym)
        checkOptionalField("nameDistinguisher", nameDistinguisher, data.nameDistinguisher)
        checkOptionalField("dateOfBirth", dateOfBirth, data.dateOfBirth)
        checkOptionalField("placeOfBirth", placeOfBirth, data.placeOfBirth)
        checkOptionalField("gender", gender, data.gender)
        checkOptionalField("postalAddress", postalAddress, data.postalAddress)
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(PersonalDataUnitTest())
        }
    }
}
