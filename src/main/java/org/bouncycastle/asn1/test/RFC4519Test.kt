package org.bouncycastle.asn1.test

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.X500NameStyle
import org.bouncycastle.asn1.x500.style.RFC4519Style
import org.bouncycastle.util.Arrays
import org.bouncycastle.util.encoders.Hex
import org.bouncycastle.util.test.SimpleTest

class RFC4519Test : SimpleTest() {

    override fun getName(): String {
        return "RFC4519Test"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val style = RFC4519Style.INSTANCE

        for (i in attributeTypes.indices) {
            if (attributeTypeOIDs[i] != style.attrNameToOID(attributeTypes[i])) {
                fail("mismatch for " + attributeTypes[i])
            }
        }

        val enc = Hex.decode("305e310b300906035504061302415531283026060355040a0c1f546865204c6567696f6e206f662074686520426f756e637920436173746c653125301006035504070c094d656c626f75726e653011060355040b0c0a4173636f742056616c65")

        var n: X500Name = X500Name.getInstance(style, X500Name.getInstance(enc))

        if (n.toString() != "l=Melbourne+ou=Ascot Vale,o=The Legion of the Bouncy Castle,c=AU") {
            fail("Failed composite to string test got: " + n.toString())
        }

        n = X500Name(style, "l=Melbourne+ou=Ascot Vale,o=The Legion of the Bouncy Castle,c=AU")

        if (!Arrays.areEqual(n.encoded, enc)) {
            fail("re-encoding test after parse failed")
        }
    }

    companion object {
        internal var attributeTypes = arrayOf("businessCategory", "c", "cn", "dc", "description", "destinationIndicator", "distinguishedName", "dnQualifier", "enhancedSearchGuide", "facsimileTelephoneNumber", "generationQualifier", "givenName", "houseIdentifier", "initials", "internationalISDNNumber", "l", "member", "name", "o", "ou", "owner", "physicalDeliveryOfficeName", "postalAddress", "postalCode", "postOfficeBox", "preferredDeliveryMethod", "registeredAddress", "roleOccupant", "searchGuide", "seeAlso", "serialNumber", "sn", "st", "street", "telephoneNumber", "teletexTerminalIdentifier", "telexNumber", "title", "uid", "uniqueMember", "userPassword", "x121Address", "x500UniqueIdentifier")

        internal var attributeTypeOIDs = arrayOf(ASN1ObjectIdentifier("2.5.4.15"), ASN1ObjectIdentifier("2.5.4.6"), ASN1ObjectIdentifier("2.5.4.3"), ASN1ObjectIdentifier("0.9.2342.19200300.100.1.25"), ASN1ObjectIdentifier("2.5.4.13"), ASN1ObjectIdentifier("2.5.4.27"), ASN1ObjectIdentifier("2.5.4.49"), ASN1ObjectIdentifier("2.5.4.46"), ASN1ObjectIdentifier("2.5.4.47"), ASN1ObjectIdentifier("2.5.4.23"), ASN1ObjectIdentifier("2.5.4.44"), ASN1ObjectIdentifier("2.5.4.42"), ASN1ObjectIdentifier("2.5.4.51"), ASN1ObjectIdentifier("2.5.4.43"), ASN1ObjectIdentifier("2.5.4.25"), ASN1ObjectIdentifier("2.5.4.7"), ASN1ObjectIdentifier("2.5.4.31"), ASN1ObjectIdentifier("2.5.4.41"), ASN1ObjectIdentifier("2.5.4.10"), ASN1ObjectIdentifier("2.5.4.11"), ASN1ObjectIdentifier("2.5.4.32"), ASN1ObjectIdentifier("2.5.4.19"), ASN1ObjectIdentifier("2.5.4.16"), ASN1ObjectIdentifier("2.5.4.17"), ASN1ObjectIdentifier("2.5.4.18"), ASN1ObjectIdentifier("2.5.4.28"), ASN1ObjectIdentifier("2.5.4.26"), ASN1ObjectIdentifier("2.5.4.33"), ASN1ObjectIdentifier("2.5.4.14"), ASN1ObjectIdentifier("2.5.4.34"), ASN1ObjectIdentifier("2.5.4.5"), ASN1ObjectIdentifier("2.5.4.4"), ASN1ObjectIdentifier("2.5.4.8"), ASN1ObjectIdentifier("2.5.4.9"), ASN1ObjectIdentifier("2.5.4.20"), ASN1ObjectIdentifier("2.5.4.22"), ASN1ObjectIdentifier("2.5.4.21"), ASN1ObjectIdentifier("2.5.4.12"), ASN1ObjectIdentifier("0.9.2342.19200300.100.1.1"), ASN1ObjectIdentifier("2.5.4.50"), ASN1ObjectIdentifier("2.5.4.35"), ASN1ObjectIdentifier("2.5.4.24"), ASN1ObjectIdentifier("2.5.4.45"))


        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(RFC4519Test())
        }
    }
}
