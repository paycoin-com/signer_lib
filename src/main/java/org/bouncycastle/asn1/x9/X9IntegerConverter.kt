package org.bouncycastle.asn1.x9

import java.math.BigInteger

import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.ECFieldElement

class X9IntegerConverter {
    fun getByteLength(
            c: ECCurve): Int {
        return (c.fieldSize + 7) / 8
    }

    fun getByteLength(
            fe: ECFieldElement): Int {
        return (fe.fieldSize + 7) / 8
    }

    fun integerToBytes(
            s: BigInteger,
            qLength: Int): ByteArray {
        val bytes = s.toByteArray()

        if (qLength < bytes.size) {
            val tmp = ByteArray(qLength)

            System.arraycopy(bytes, bytes.size - tmp.size, tmp, 0, tmp.size)

            return tmp
        } else if (qLength > bytes.size) {
            val tmp = ByteArray(qLength)

            System.arraycopy(bytes, 0, tmp, tmp.size - bytes.size, bytes.size)

            return tmp
        }

        return bytes
    }
}
