package org.bouncycastle.asn1.test

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.util.test.SimpleTest

import java.math.BigInteger

abstract class ASN1UnitTest : SimpleTest() {
    protected fun checkMandatoryField(name: String, expected: ASN1Encodable, present: ASN1Encodable) {
        if (expected != present) {
            fail(name + " field doesn't match.")
        }
    }

    protected fun checkMandatoryField(name: String, expected: String, present: String) {
        if (expected != present) {
            fail(name + " field doesn't match.")
        }
    }

    protected fun checkMandatoryField(name: String, expected: ByteArray, present: ByteArray) {
        if (!areEqual(expected, present)) {
            fail(name + " field doesn't match.")
        }
    }

    protected fun checkMandatoryField(name: String, expected: Int, present: Int) {
        if (expected != present) {
            fail(name + " field doesn't match.")
        }
    }

    protected fun checkOptionalField(name: String, expected: ASN1Encodable?, present: ASN1Encodable?) {
        if (expected != null) {
            if (expected != present) {
                fail(name + " field doesn't match.")
            }
        } else if (present != null) {
            fail(name + " field found when none expected.")
        }
    }

    protected fun checkOptionalField(name: String, expected: String?, present: String?) {
        if (expected != null) {
            if (expected != present) {
                fail(name + " field doesn't match.")
            }
        } else if (present != null) {
            fail(name + " field found when none expected.")
        }
    }

    protected fun checkOptionalField(name: String, expected: BigInteger?, present: BigInteger?) {
        if (expected != null) {
            if (expected != present) {
                fail(name + " field doesn't match.")
            }
        } else if (present != null) {
            fail(name + " field found when none expected.")
        }
    }


}
