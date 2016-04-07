/*
 * Copyright 2008 ZXing authors
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

import java.util.ArrayList

/**
 *
 * Implements Reed-Solomon enbcoding, as the name implies.

 * @author Sean Owen
 * *
 * @author William Rucklidge
 * *
 * @since 5.0.2
 */
class ReedSolomonEncoder(private val field: GF256) {
    private val cachedGenerators: ArrayList<GF256Poly>

    init {
        if (GF256.QR_CODE_FIELD != field) {
            throw IllegalArgumentException("Only QR Code is supported at this time")
        }
        this.cachedGenerators = ArrayList<GF256Poly>()
        cachedGenerators.add(GF256Poly(field, intArrayOf(1)))
    }

    private fun buildGenerator(degree: Int): GF256Poly {
        if (degree >= cachedGenerators.size) {
            var lastGenerator = cachedGenerators[cachedGenerators.size - 1]
            for (d in cachedGenerators.size..degree) {
                val nextGenerator = lastGenerator.multiply(GF256Poly(field, intArrayOf(1, field.exp(d - 1))))
                cachedGenerators.add(nextGenerator)
                lastGenerator = nextGenerator
            }
        }
        return cachedGenerators[degree]
    }

    fun encode(toEncode: IntArray, ecBytes: Int) {
        if (ecBytes == 0) {
            throw IllegalArgumentException("No error correction bytes")
        }
        val dataBytes = toEncode.size - ecBytes
        if (dataBytes <= 0) {
            throw IllegalArgumentException("No data bytes provided")
        }
        val generator = buildGenerator(ecBytes)
        val infoCoefficients = IntArray(dataBytes)
        System.arraycopy(toEncode, 0, infoCoefficients, 0, dataBytes)
        var info = GF256Poly(field, infoCoefficients)
        info = info.multiplyByMonomial(ecBytes, 1)
        val remainder = info.divide(generator)[1]
        val coefficients = remainder.coefficients
        val numZeroCoefficients = ecBytes - coefficients.size
        for (i in 0..numZeroCoefficients - 1) {
            toEncode[dataBytes + i] = 0
        }
        System.arraycopy(coefficients, 0, toEncode, dataBytes + numZeroCoefficients, coefficients.size)
    }

}
