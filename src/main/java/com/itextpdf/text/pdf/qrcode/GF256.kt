/*
 * Copyright 2007 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itextpdf.text.pdf.qrcode

/**
 *
 * This class contains utility methods for performing mathematical operations over
 * the Galois Field GF(256). Operations use a given primitive polynomial in calculations.

 *
 * Throughout this package, elements of GF(256) are represented as an `int`
 * for convenience and speed (but at the cost of memory).
 * Only the bottom 8 bits are really used.

 * @author Sean Owen
 * *
 * @since 5.0.2
 */
class GF256
/**
 * Create a representation of GF(256) using the given primitive polynomial.

 * @param primitive irreducible polynomial whose coefficients are represented by
 * *  the bits of an int, where the least-significant bit represents the constant
 * *  coefficient
 */
private constructor(primitive: Int) {

    private val expTable: IntArray
    private val logTable: IntArray
    internal val zero:

            GF256Poly
    internal val one:

            GF256Poly

    init {
        expTable = IntArray(256)
        logTable = IntArray(256)
        var x = 1
        for (i in 0..255) {
            expTable[i] = x
            x = x shl 1 // x = x * 2; we're assuming the generator alpha is 2
            if (x >= 0x100) {
                x = x xor primitive
            }
        }
        for (i in 0..254) {
            logTable[expTable[i]] = i
        }
        // logTable[0] == 0 but this should never be used
        zero = GF256Poly(this, intArrayOf(0))
        one = GF256Poly(this, intArrayOf(1))
    }

    /**
     * @return the monomial representing coefficient * x^degree
     */
    internal fun buildMonomial(degree: Int, coefficient: Int): GF256Poly {
        if (degree < 0) {
            throw IllegalArgumentException()
        }
        if (coefficient == 0) {
            return zero
        }
        val coefficients = IntArray(degree + 1)
        coefficients[0] = coefficient
        return GF256Poly(this, coefficients)
    }

    /**
     * @return 2 to the power of a in GF(256)
     */
    internal fun exp(a: Int): Int {
        return expTable[a]
    }

    /**
     * @return base 2 log of a in GF(256)
     */
    internal fun log(a: Int): Int {
        if (a == 0) {
            throw IllegalArgumentException()
        }
        return logTable[a]
    }

    /**
     * @return multiplicative inverse of a
     */
    internal fun inverse(a: Int): Int {
        if (a == 0) {
            throw ArithmeticException()
        }
        return expTable[255 - logTable[a]]
    }

    /**
     * @param a
     * *
     * @param b
     * *
     * @return product of a and b in GF(256)
     */
    internal fun multiply(a: Int, b: Int): Int {
        if (a == 0 || b == 0) {
            return 0
        }
        if (a == 1) {
            return b
        }
        if (b == 1) {
            return a
        }
        return expTable[(logTable[a] + logTable[b]) % 255]
    }

    companion object {

        val QR_CODE_FIELD = GF256(0x011D) // x^8 + x^4 + x^3 + x^2 + 1
        val DATA_MATRIX_FIELD = GF256(0x012D) // x^8 + x^5 + x^3 + x^2 + 1

        /**
         * Implements both addition and subtraction -- they are the same in GF(256).

         * @return sum/difference of a and b
         */
        internal fun addOrSubtract(a: Int, b: Int): Int {
            return a xor b
        }
    }

}
