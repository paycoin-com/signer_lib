package org.bouncycastle.asn1.test

import junit.extensions.TestSetup
import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite
import org.bouncycastle.util.test.SimpleTestResult

class AllTests : TestCase() {
    fun testASN1() {
        val tests = RegressionTest.tests

        for (i in tests.indices) {
            val result = tests[i].perform() as SimpleTestResult

            if (!result.isSuccessful) {
                TestCase.Companion.fail(result.toString())
            }
        }
    }

    internal class BCTestSetup(test: Test) : TestSetup(test) {

        override fun setUp() {

        }

        override fun tearDown() {

        }
    }

    companion object {

        @JvmStatic fun main(args: Array<String>) {
            junit.textui.TestRunner.run(suite())
        }

        fun suite(): Test {
            val suite = TestSuite("ASN.1 Tests")

            suite.addTestSuite(AllTests::class.java)
            suite.addTestSuite(GetInstanceTest::class.java)
            suite.addTestSuite(ASN1SequenceParserTest::class.java)
            suite.addTestSuite(OctetStringTest::class.java)
            suite.addTestSuite(ParseTest::class.java)

            return BCTestSetup(suite)
        }
    }
}
