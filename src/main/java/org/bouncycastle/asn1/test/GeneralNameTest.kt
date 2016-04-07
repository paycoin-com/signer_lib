package org.bouncycastle.asn1.test

import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.asn1.x509.GeneralNamesBuilder
import org.bouncycastle.util.Arrays
import org.bouncycastle.util.encoders.Hex
import org.bouncycastle.util.test.SimpleTest

class GeneralNameTest : SimpleTest() {

    override fun getName(): String {
        return "GeneralName"
    }

    @Throws(Exception::class)
    override fun performTest() {
        var nm = GeneralName(GeneralName.iPAddress, "10.9.8.0")
        if (!Arrays.areEqual(nm.encoded, ipv4)) {
            fail("ipv4 encoding failed")
        }

        nm = GeneralName(GeneralName.iPAddress, "10.9.8.0/255.255.255.0")
        if (!Arrays.areEqual(nm.encoded, ipv4WithMask1)) {
            fail("ipv4 with netmask 1 encoding failed")
        }

        nm = GeneralName(GeneralName.iPAddress, "10.9.8.0/24")
        if (!Arrays.areEqual(nm.encoded, ipv4WithMask1)) {
            fail("ipv4 with netmask 2 encoding failed")
        }

        nm = GeneralName(GeneralName.iPAddress, "10.9.8.0/255.255.128.0")
        if (!Arrays.areEqual(nm.encoded, ipv4WithMask2)) {
            fail("ipv4 with netmask 3a encoding failed")
        }

        nm = GeneralName(GeneralName.iPAddress, "10.9.8.0/17")
        if (!Arrays.areEqual(nm.encoded, ipv4WithMask2)) {
            fail("ipv4 with netmask 3b encoding failed")
        }

        nm = GeneralName(GeneralName.iPAddress, "10.9.8.0/255.255.192.0")
        if (!Arrays.areEqual(nm.encoded, ipv4WithMask3)) {
            fail("ipv4 with netmask 3a encoding failed")
        }

        nm = GeneralName(GeneralName.iPAddress, "10.9.8.0/18")
        if (!Arrays.areEqual(nm.encoded, ipv4WithMask3)) {
            fail("ipv4 with netmask 3b encoding failed")
        }

        nm = GeneralName(GeneralName.iPAddress, "2001:0db8:85a3:08d3:1319:8a2e:0370:7334")
        if (!Arrays.areEqual(nm.encoded, ipv6a)) {
            fail("ipv6 with netmask encoding failed")
        }

        nm = GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::1319:8a2e:0370:7334")
        if (!Arrays.areEqual(nm.encoded, ipv6b)) {
            fail("ipv6b encoding failed")
        }

        nm = GeneralName(GeneralName.iPAddress, "::1")
        if (!Arrays.areEqual(nm.encoded, ipv6c)) {
            fail("ipv6c failed")
        }

        nm = GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::8a2e:0370:7334")
        if (!Arrays.areEqual(nm.encoded, ipv6d)) {
            fail("ipv6d failed")
        }

        nm = GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::8a2e:10.9.8.0")
        if (!Arrays.areEqual(nm.encoded, ipv6e)) {
            fail("ipv6e failed")
        }

        nm = GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::8a2e:10.9.8.0/ffff:ffff:ffff::0000")
        if (!Arrays.areEqual(nm.encoded, ipv6f)) {
            fail("ipv6f failed")
        }

        nm = GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::8a2e:10.9.8.0/128")
        if (!Arrays.areEqual(nm.encoded, ipv6g)) {
            fail("ipv6g failed")
        }

        nm = GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::/48")
        if (!Arrays.areEqual(nm.encoded, ipv6h)) {
            fail("ipv6h failed")
        }

        nm = GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::/47")
        if (!Arrays.areEqual(nm.encoded, ipv6i)) {
            fail("ipv6i failed")
        }

        nm = GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::/49")
        if (!Arrays.areEqual(nm.encoded, ipv6j)) {
            fail("ipv6j failed")
        }

        var genNamesBuilder = GeneralNamesBuilder()

        val name1 = GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::8a2e:0370:7334")

        genNamesBuilder.addName(name1)

        if (genNamesBuilder.build() != GeneralNames(name1)) {
            fail("single build failed")
        }

        val nm1 = GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::/48")
        val nm2 = GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::/47")
        val nm3 = GeneralName(GeneralName.iPAddress, "2001:0db8:85a3::/49")

        genNamesBuilder = GeneralNamesBuilder()

        genNamesBuilder.addName(name1)

        genNamesBuilder.addNames(GeneralNames(arrayOf(nm1, nm2)))

        genNamesBuilder.addName(nm3)

        if (genNamesBuilder.build() != GeneralNames(arrayOf(name1, nm1, nm2, nm3))) {
            fail("multi build failed")
        }
    }

    companion object {
        private val ipv4 = Hex.decode("87040a090800")
        private val ipv4WithMask1 = Hex.decode("87080a090800ffffff00")
        private val ipv4WithMask2 = Hex.decode("87080a090800ffff8000")
        private val ipv4WithMask3 = Hex.decode("87080a090800ffffc000")

        private val ipv6a = Hex.decode("871020010db885a308d313198a2e03707334")
        private val ipv6b = Hex.decode("871020010db885a3000013198a2e03707334")
        private val ipv6c = Hex.decode("871000000000000000000000000000000001")
        private val ipv6d = Hex.decode("871020010db885a3000000008a2e03707334")
        private val ipv6e = Hex.decode("871020010db885a3000000008a2e0a090800")
        private val ipv6f = Hex.decode("872020010db885a3000000008a2e0a090800ffffffffffff00000000000000000000")
        private val ipv6g = Hex.decode("872020010db885a3000000008a2e0a090800ffffffffffffffffffffffffffffffff")
        private val ipv6h = Hex.decode("872020010db885a300000000000000000000ffffffffffff00000000000000000000")
        private val ipv6i = Hex.decode("872020010db885a300000000000000000000fffffffffffe00000000000000000000")
        private val ipv6j = Hex.decode("872020010db885a300000000000000000000ffffffffffff80000000000000000000")

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(GeneralNameTest())
        }
    }
}
