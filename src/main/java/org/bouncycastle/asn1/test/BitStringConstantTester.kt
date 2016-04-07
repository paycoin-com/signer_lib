package org.bouncycastle.asn1.test

object BitStringConstantTester {
    private val bits = intArrayOf(1 shl 7, 1 shl 6, 1 shl 5, 1 shl 4, 1 shl 3, 1 shl 2, 1 shl 1, 1 shl 0, 1 shl 15, 1 shl 14, 1 shl 13, 1 shl 12, 1 shl 11, 1 shl 10, 1 shl 9, 1 shl 8, 1 shl 23, 1 shl 22, 1 shl 21, 1 shl 20, 1 shl 19, 1 shl 18, 1 shl 17, 1 shl 16, 1 shl 31, 1 shl 30, 1 shl 29, 1 shl 28, 1 shl 27, 1 shl 26, 1 shl 25, 1 shl 24)

    fun testFlagValueCorrect(
            bitNo: Int,
            value: Int) {
        if (bits[bitNo] != value) {
            throw IllegalArgumentException("bit value $bitNo wrong")
        }
    }
}
