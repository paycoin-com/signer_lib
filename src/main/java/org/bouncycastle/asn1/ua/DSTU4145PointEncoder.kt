package org.bouncycastle.asn1.ua

import java.math.BigInteger
import java.util.Random

import org.bouncycastle.math.ec.ECConstants
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.ECFieldElement
import org.bouncycastle.math.ec.ECPoint

/**
 * DSTU4145 encodes points somewhat differently than X9.62
 * It compresses the point to the size of the field element
 */
object DSTU4145PointEncoder {
    private fun trace(fe: ECFieldElement): ECFieldElement {
        var t = fe
        for (i in 1..fe.fieldSize - 1) {
            t = t.square().add(fe)
        }
        return t
    }

    /**
     * Solves a quadratic equation `z2 + z = beta`(X9.62
     * D.1.6) The other solution is `z + 1`.

     * @param beta The value to solve the quadratic equation for.
     * *
     * @return the solution for `z2 + z = beta` or
     * *         `null` if no solution exists.
     */
    private fun solveQuadraticEquation(curve: ECCurve, beta: ECFieldElement): ECFieldElement? {
        if (beta.isZero) {
            return beta
        }

        val zeroElement = curve.fromBigInteger(ECConstants.ZERO)

        var z: ECFieldElement? = null
        var gamma: ECFieldElement? = null

        val rand = Random()
        val m = beta.fieldSize
        do {
            val t = curve.fromBigInteger(BigInteger(m, rand))
            z = zeroElement
            var w = beta
            for (i in 1..m - 1) {
                val w2 = w.square()
                z = z!!.square().add(w2.multiply(t))
                w = w2.add(beta)
            }
            if (!w.isZero) {
                return null
            }
            gamma = z!!.square().add(z)
        } while (gamma!!.isZero)

        return z
    }

    fun encodePoint(Q: ECPoint): ByteArray {
        var Q = Q
        /*if (!Q.isCompressed())
              Q=new ECPoint.F2m(Q.getCurve(),Q.getX(),Q.getY(),true);

          byte[] bytes=Q.getEncoded();

          if (bytes[0]==0x02)
              bytes[bytes.length-1]&=0xFE;
          else if (bytes[0]==0x02)
              bytes[bytes.length-1]|=0x01;

          return Arrays.copyOfRange(bytes, 1, bytes.length);*/

        Q = Q.normalize()

        val x = Q.affineXCoord

        val bytes = x.encoded

        if (!x.isZero) {
            val z = Q.affineYCoord.divide(x)
            if (trace(z).isOne) {
                bytes[bytes.size - 1] = bytes[bytes.size - 1] or 0x01
            } else {
                bytes[bytes.size - 1] = bytes[bytes.size - 1] and 0xFE
            }
        }

        return bytes
    }

    fun decodePoint(curve: ECCurve, bytes: ByteArray): ECPoint {
        /*byte[] bp_enc=new byte[bytes.length+1];
          if (0==(bytes[bytes.length-1]&0x1))
              bp_enc[0]=0x02;
          else
              bp_enc[0]=0x03;
          System.arraycopy(bytes, 0, bp_enc, 1, bytes.length);
          if (!trace(curve.fromBigInteger(new BigInteger(1, bytes))).equals(curve.getA().toBigInteger()))
              bp_enc[bp_enc.length-1]^=0x01;

          return curve.decodePoint(bp_enc);*/

        val k = curve.fromBigInteger(BigInteger.valueOf((bytes[bytes.size - 1] and 0x1).toLong()))

        var xp = curve.fromBigInteger(BigInteger(1, bytes))
        if (trace(xp) != curve.a) {
            xp = xp.addOne()
        }

        var yp: ECFieldElement? = null
        if (xp.isZero) {
            yp = curve.b.sqrt()
        } else {
            val beta = xp.square().invert().multiply(curve.b).add(curve.a).add(xp)
            var z = solveQuadraticEquation(curve, beta)
            if (z != null) {
                if (trace(z) != k) {
                    z = z.addOne()
                }
                yp = xp.multiply(z)
            }
        }

        if (yp == null) {
            throw IllegalArgumentException("Invalid point compression")
        }

        return curve.createPoint(xp.toBigInteger(), yp.toBigInteger())
    }
}
