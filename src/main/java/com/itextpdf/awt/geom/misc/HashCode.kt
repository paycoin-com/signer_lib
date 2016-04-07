/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This code was originally part of the Apache Harmony project.
 *  The Apache Harmony project has been discontinued.
 *  That's why we imported the code into iText.
 */

package com.itextpdf.awt.geom.misc

/**
 * This class is a convenience method to sequentially calculate hash code of the
 * object based on the field values. The result depends on the order of elements
 * appended. The exact formula is the same as for
 * `java.util.List.hashCode`.

 * If you need order independent hash code just summate, multiply or XOR all
 * elements.

 *
 *
 * Suppose we have class:

 * `
 * class Thing {
 * long id;
 * String name;
 * float weight;
 * }
` *

 * The hash code calculation can be expressed in 2 forms.

 *
 *
 * For maximum performance:

 * `
 * public int hashCode() {
 * int hashCode = HashCode.EMPTY_HASH_CODE;
 * hashCode = HashCode.combine(hashCode, id);
 * hashCode = HashCode.combine(hashCode, name);
 * hashCode = HashCode.combine(hashCode, weight);
 * return hashCode;
 * }
` *

 *
 *
 * For convenience: `
 * public int hashCode() {
 * return new HashCode().append(id).append(name).append(weight).hashCode();
 * }
` *

 * @see java.util.List.hashCode
 */
class HashCode {

    private var hashCode = EMPTY_HASH_CODE

    /**
     * Returns accumulated hashCode
     */
    override fun hashCode(): Int {
        return hashCode
    }

    /**
     * Appends value's hashCode to the current hashCode.
     * @param value new element
     * *
     * @return this
     */
    fun append(value: Int): HashCode {
        hashCode = combine(hashCode, value)
        return this
    }

    /**
     * Appends value's hashCode to the current hashCode.
     * @param value new element
     * *
     * @return this
     */
    fun append(value: Long): HashCode {
        hashCode = combine(hashCode, value)
        return this
    }

    /**
     * Appends value's hashCode to the current hashCode.
     * @param value new element
     * *
     * @return this
     */
    fun append(value: Float): HashCode {
        hashCode = combine(hashCode, value)
        return this
    }

    /**
     * Appends value's hashCode to the current hashCode.
     * @param value new element
     * *
     * @return this
     */
    fun append(value: Double): HashCode {
        hashCode = combine(hashCode, value)
        return this
    }

    /**
     * Appends value's hashCode to the current hashCode.
     * @param value new element
     * *
     * @return this
     */
    fun append(value: Boolean): HashCode {
        hashCode = combine(hashCode, value)
        return this
    }

    /**
     * Appends value's hashCode to the current hashCode.
     * @param value new element
     * *
     * @return this
     */
    fun append(value: Any): HashCode {
        hashCode = combine(hashCode, value)
        return this
    }

    companion object {
        /**
         * The hashCode value before any data is appended, equals to 1.
         * @see java.util.List.hashCode
         */
        val EMPTY_HASH_CODE = 1

        /**
         * Combines hashCode of previous elements sequence and value's hashCode.
         * @param hashCode previous hashCode value
         * *
         * @param value new element
         * *
         * @return combined hashCode
         */
        fun combine(hashCode: Int, value: Boolean): Int {
            val v = if (value) 1231 else 1237
            return combine(hashCode, v)
        }

        /**
         * Combines hashCode of previous elements sequence and value's hashCode.
         * @param hashCode previous hashCode value
         * *
         * @param value new element
         * *
         * @return combined hashCode
         */
        fun combine(hashCode: Int, value: Long): Int {
            val v = (value xor value.ushr(32)).toInt()
            return combine(hashCode, v)
        }

        /**
         * Combines hashCode of previous elements sequence and value's hashCode.
         * @param hashCode previous hashCode value
         * *
         * @param value new element
         * *
         * @return combined hashCode
         */
        fun combine(hashCode: Int, value: Float): Int {
            val v = java.lang.Float.floatToIntBits(value)
            return combine(hashCode, v)
        }

        /**
         * Combines hashCode of previous elements sequence and value's hashCode.
         * @param hashCode previous hashCode value
         * *
         * @param value new element
         * *
         * @return combined hashCode
         */
        fun combine(hashCode: Int, value: Double): Int {
            val v = java.lang.Double.doubleToLongBits(value)
            return combine(hashCode, v)
        }

        /**
         * Combines hashCode of previous elements sequence and value's hashCode.
         * @param hashCode previous hashCode value
         * *
         * @param value new element
         * *
         * @return combined hashCode
         */
        fun combine(hashCode: Int, value: Any): Int {
            return combine(hashCode, value.hashCode())
        }

        /**
         * Combines hashCode of previous elements sequence and value's hashCode.
         * @param hashCode previous hashCode value
         * *
         * @param value new element
         * *
         * @return combined hashCode
         */
        fun combine(hashCode: Int, value: Int): Int {
            return 31 * hashCode + value
        }
    }
}
