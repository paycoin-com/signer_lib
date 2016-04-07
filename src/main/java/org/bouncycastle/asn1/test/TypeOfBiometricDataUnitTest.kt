package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.x509.qualified.TypeOfBiometricData
import org.bouncycastle.util.test.SimpleTest

class TypeOfBiometricDataUnitTest : SimpleTest() {
    override fun getName(): String {
        return "TypeOfBiometricData"
    }

    @Throws(Exception::class)
    override fun performTest() {
        //
        // predefined
        //
        checkPredefinedType(TypeOfBiometricData.PICTURE)

        checkPredefinedType(TypeOfBiometricData.HANDWRITTEN_SIGNATURE)

        //
        // non-predefined
        //
        val localType = ASN1ObjectIdentifier("1.1")

        var type: TypeOfBiometricData? = TypeOfBiometricData(localType)

        checkNonPredefined(type, localType)

        type = TypeOfBiometricData.getInstance(type)

        checkNonPredefined(type, localType)

        val obj = type!!.toASN1Primitive()

        type = TypeOfBiometricData.getInstance(obj)

        checkNonPredefined(type, localType)

        type = TypeOfBiometricData.getInstance(null)

        if (type != null) {
            fail("null getInstance() failed.")
        }

        try {
            TypeOfBiometricData.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

        try {
            TypeOfBiometricData(100)

            fail("constructor failed to detect bad predefined type.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

        if (TypeOfBiometricData.PICTURE != 0) {
            fail("predefined picture should be 0")
        }

        if (TypeOfBiometricData.HANDWRITTEN_SIGNATURE != 1) {
            fail("predefined handwritten signature should be 1")
        }
    }

    @Throws(IOException::class)
    private fun checkPredefinedType(
            predefinedType: Int) {
        var type = TypeOfBiometricData(predefinedType)

        checkPredefined(type, predefinedType)

        type = TypeOfBiometricData.getInstance(type)

        checkPredefined(type, predefinedType)

        val aIn = ASN1InputStream(type.toASN1Primitive().encoded)

        val obj = aIn.readObject()

        type = TypeOfBiometricData.getInstance(obj)

        checkPredefined(type, predefinedType)
    }

    private fun checkPredefined(
            type: TypeOfBiometricData,
            value: Int) {
        if (!type.isPredefined) {
            fail("predefined type expected but not found.")
        }

        if (type.predefinedBiometricType != value) {
            fail("predefined type does not match.")
        }
    }

    private fun checkNonPredefined(
            type: TypeOfBiometricData,
            value: ASN1ObjectIdentifier) {
        if (type.isPredefined) {
            fail("predefined type found when not expected.")
        }

        if (type.biometricDataOid != value) {
            fail("data oid does not match.")
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(TypeOfBiometricDataUnitTest())
        }
    }
}
