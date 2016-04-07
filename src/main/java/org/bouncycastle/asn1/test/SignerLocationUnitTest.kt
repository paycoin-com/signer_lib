package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.esf.SignerLocation
import org.bouncycastle.util.test.SimpleTest

class SignerLocationUnitTest : SimpleTest() {
    override fun getName(): String {
        return "SignerLocation"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val countryName = DERUTF8String("Australia")

        var sl: SignerLocation? = SignerLocation(countryName, null, null)

        checkConstruction(sl, countryName, null, null)

        val localityName = DERUTF8String("Melbourne")

        sl = SignerLocation(null, localityName, null)

        checkConstruction(sl, null, localityName, null)

        sl = SignerLocation(countryName, localityName, null)

        checkConstruction(sl, countryName, localityName, null)

        var v = ASN1EncodableVector()

        v.add(DERUTF8String("line 1"))
        v.add(DERUTF8String("line 2"))

        var postalAddress: ASN1Sequence = DERSequence(v)

        sl = SignerLocation(null, null, postalAddress)

        checkConstruction(sl, null, null, postalAddress)

        sl = SignerLocation(countryName, null, postalAddress)

        checkConstruction(sl, countryName, null, postalAddress)

        sl = SignerLocation(countryName, localityName, postalAddress)

        checkConstruction(sl, countryName, localityName, postalAddress)

        sl = SignerLocation.getInstance(null)

        if (sl != null) {
            fail("null getInstance() failed.")
        }

        try {
            SignerLocation.getInstance(Object())

            fail("getInstance() failed to detect bad object.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

        //
        // out of range postal address
        //
        v = ASN1EncodableVector()

        v.add(DERUTF8String("line 1"))
        v.add(DERUTF8String("line 2"))
        v.add(DERUTF8String("line 3"))
        v.add(DERUTF8String("line 4"))
        v.add(DERUTF8String("line 5"))
        v.add(DERUTF8String("line 6"))
        v.add(DERUTF8String("line 7"))

        postalAddress = DERSequence(v)

        try {
            SignerLocation(null, null, postalAddress)

            fail("constructor failed to detect bad postalAddress.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

        try {
            SignerLocation.getInstance(DERSequence(DERTaggedObject(2, postalAddress)))

            fail("sequence constructor failed to detect bad postalAddress.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

        try {
            SignerLocation.getInstance(DERSequence(DERTaggedObject(5, postalAddress)))

            fail("sequence constructor failed to detect bad tag.")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }

    @Throws(IOException::class)
    private fun checkConstruction(
            sl: SignerLocation,
            countryName: DERUTF8String?,
            localityName: DERUTF8String?,
            postalAddress: ASN1Sequence?) {
        var sl = sl
        checkValues(sl, countryName, localityName, postalAddress)

        sl = SignerLocation.getInstance(sl)

        checkValues(sl, countryName, localityName, postalAddress)

        val aIn = ASN1InputStream(sl.toASN1Primitive().encoded)

        val seq = aIn.readObject() as ASN1Sequence

        sl = SignerLocation.getInstance(seq)

        checkValues(sl, countryName, localityName, postalAddress)
    }

    private fun checkValues(
            sl: SignerLocation,
            countryName: DERUTF8String?,
            localityName: DERUTF8String?,
            postalAddress: ASN1Sequence?) {
        if (countryName != null) {
            if (countryName != sl.countryName) {
                fail("countryNames don't match.")
            }
        } else if (sl.countryName != null) {
            fail("countryName found when none expected.")
        }

        if (localityName != null) {
            if (localityName != sl.localityName) {
                fail("localityNames don't match.")
            }
        } else if (sl.localityName != null) {
            fail("localityName found when none expected.")
        }

        if (postalAddress != null) {
            if (postalAddress != sl.postalAddress) {
                fail("postalAddresses don't match.")
            }
        } else if (sl.postalAddress != null) {
            fail("postalAddress found when none expected.")
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(SignerLocationUnitTest())
        }
    }
}
