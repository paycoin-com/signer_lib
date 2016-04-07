package org.bouncycastle.asn1.test

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.util.test.SimpleTest
import org.bouncycastle.util.test.TestResult

class ObjectIdentifierTest : SimpleTest() {
    override fun getName(): String {
        return "ObjectIdentifier"
    }

    @Throws(Exception::class)
    override fun performTest() {
        // exercise the object cache
        for (i in 0..1023) {
            for (j in 0..16999) {
                val encoded = ASN1ObjectIdentifier("1.1.$i.$j").encoded

                ASN1ObjectIdentifier.getInstance(encoded)
            }
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            val test = ObjectIdentifierTest()
            val result = test.perform()

            println(result)
        }
    }
}
