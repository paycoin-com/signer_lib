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

import java.util.HashMap

/**
 * Encapsulates a Character Set ECI, according to "Extended Channel Interpretations" 5.3.1.1
 * of ISO 18004.

 * @author Sean Owen
 * *
 * @since 5.0.2
 */
class CharacterSetECI private constructor(val value: Int, val encodingName: String) {
    companion object {

        private var NAME_TO_ECI: HashMap<String, CharacterSetECI>? = null

        private fun initialize() {
            val n = HashMap<String, CharacterSetECI>(29)
            // TODO figure out if these values are even right!
            addCharacterSet(0, "Cp437", n)
            addCharacterSet(1, arrayOf("ISO8859_1", "ISO-8859-1"), n)
            addCharacterSet(2, "Cp437", n)
            addCharacterSet(3, arrayOf("ISO8859_1", "ISO-8859-1"), n)
            addCharacterSet(4, arrayOf("ISO8859_2", "ISO-8859-2"), n)
            addCharacterSet(5, arrayOf("ISO8859_3", "ISO-8859-3"), n)
            addCharacterSet(6, arrayOf("ISO8859_4", "ISO-8859-4"), n)
            addCharacterSet(7, arrayOf("ISO8859_5", "ISO-8859-5"), n)
            addCharacterSet(8, arrayOf("ISO8859_6", "ISO-8859-6"), n)
            addCharacterSet(9, arrayOf("ISO8859_7", "ISO-8859-7"), n)
            addCharacterSet(10, arrayOf("ISO8859_8", "ISO-8859-8"), n)
            addCharacterSet(11, arrayOf("ISO8859_9", "ISO-8859-9"), n)
            addCharacterSet(12, arrayOf("ISO8859_10", "ISO-8859-10"), n)
            addCharacterSet(13, arrayOf("ISO8859_11", "ISO-8859-11"), n)
            addCharacterSet(15, arrayOf("ISO8859_13", "ISO-8859-13"), n)
            addCharacterSet(16, arrayOf("ISO8859_14", "ISO-8859-14"), n)
            addCharacterSet(17, arrayOf("ISO8859_15", "ISO-8859-15"), n)
            addCharacterSet(18, arrayOf("ISO8859_16", "ISO-8859-16"), n)
            addCharacterSet(20, arrayOf("SJIS", "Shift_JIS"), n)
            NAME_TO_ECI = n
        }

        private fun addCharacterSet(value: Int, encodingName: String, n: HashMap<String, CharacterSetECI>) {
            val eci = CharacterSetECI(value, encodingName)
            n.put(encodingName, eci)
        }

        private fun addCharacterSet(value: Int, encodingNames: Array<String>, n: HashMap<String, CharacterSetECI>) {
            val eci = CharacterSetECI(value, encodingNames[0])
            for (i in encodingNames.indices) {
                n.put(encodingNames[i], eci)
            }
        }

        /**
         * @param name character set ECI encoding name
         * *
         * @return [CharacterSetECI] representing ECI for character encoding, or null if it is legal
         * *   but unsupported
         */
        fun getCharacterSetECIByName(name: String): CharacterSetECI {
            if (NAME_TO_ECI == null) {
                initialize()
            }
            return NAME_TO_ECI!![name]
        }
    }

}