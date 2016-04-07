/*
 * $Id: c48e58ca9a2e1504394e85257d6317f6fa5557a6 $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Bruno Lowagie, Paulo Soares, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.text.pdf

import com.itextpdf.text.BaseColor
import com.itextpdf.text.error_messages.MessageLocalization

import java.util.Arrays

class PdfLabColor : ICachedColorSpace {
    internal var whitePoint: FloatArray? = floatArrayOf(0.9505f, 1.0f, 1.0890f)
    internal var blackPoint: FloatArray? = null
    internal var range: FloatArray? = null

    constructor() {
    }

    constructor(whitePoint: FloatArray?) {
        if (whitePoint == null
                || whitePoint.size != 3
                || whitePoint[0] < 0.000001f || whitePoint[2] < 0.000001f
                || whitePoint[1] < 0.999999f || whitePoint[1] > 1.000001f)
            throw RuntimeException(MessageLocalization.getComposedMessage("lab.cs.white.point"))
        this.whitePoint = whitePoint
    }

    constructor(whitePoint: FloatArray, blackPoint: FloatArray) : this(whitePoint) {
        this.blackPoint = blackPoint
    }

    constructor(whitePoint: FloatArray, blackPoint: FloatArray, range: FloatArray) : this(whitePoint, blackPoint) {
        this.range = range
    }

    override fun getPdfObject(writer: PdfWriter): PdfObject {
        val array = PdfArray(PdfName.LAB)
        val dictionary = PdfDictionary()
        if (whitePoint == null
                || whitePoint!!.size != 3
                || whitePoint!![0] < 0.000001f || whitePoint!![2] < 0.000001f
                || whitePoint!![1] < 0.999999f || whitePoint!![1] > 1.000001f)
            throw RuntimeException(MessageLocalization.getComposedMessage("lab.cs.white.point"))
        dictionary.put(PdfName.WHITEPOINT, PdfArray(whitePoint))
        if (blackPoint != null) {
            if (blackPoint!!.size != 3
                    || blackPoint!![0] < -0.000001f || blackPoint!![1] < -0.000001f || blackPoint!![2] < -0.000001f)
                throw RuntimeException(MessageLocalization.getComposedMessage("lab.cs.black.point"))
            dictionary.put(PdfName.BLACKPOINT, PdfArray(blackPoint))
        }
        if (range != null) {
            if (range!!.size != 4 || range!![0] > range!![1] || range!![2] > range!![3])
                throw RuntimeException(MessageLocalization.getComposedMessage("lab.cs.range"))
            dictionary.put(PdfName.RANGE, PdfArray(range))
        }
        array.add(dictionary)
        return array
    }

    fun lab2Rgb(l: Float, a: Float, b: Float): BaseColor {
        val clinear = lab2RgbLinear(l, a, b)
        return BaseColor(clinear[0].toFloat(), clinear[1].toFloat(), clinear[2].toFloat())
    }

    internal fun lab2Cmyk(l: Float, a: Float, b: Float): CMYKColor {
        val clinear = lab2RgbLinear(l, a, b)

        val r = clinear[0]
        val g = clinear[1]
        val bee = clinear[2]
        var computedC = 0.0
        var computedM = 0.0
        var computedY = 0.0
        var computedK = 0.0

        // BLACK
        if (r == 0.0 && g == 0.0 && b == 0f) {
            computedK = 1.0
        } else {
            computedC = 1 - r
            computedM = 1 - g
            computedY = 1 - bee

            val minCMY = Math.min(computedC,
                    Math.min(computedM, computedY))
            computedC = (computedC - minCMY) / (1 - minCMY)
            computedM = (computedM - minCMY) / (1 - minCMY)
            computedY = (computedY - minCMY) / (1 - minCMY)
            computedK = minCMY
        }

        return CMYKColor(computedC.toFloat(), computedM.toFloat(), computedY.toFloat(), computedK.toFloat())
    }

    protected fun lab2RgbLinear(l: Float, a: Float, b: Float): DoubleArray {
        var a = a
        var b = b
        if (range != null && range!!.size == 4) {
            if (a < range!![0])
                a = range!![0]
            if (a > range!![1])
                a = range!![1]
            if (b < range!![2])
                b = range!![2]
            if (b > range!![3])
                b = range!![3]
        }
        val theta = 6.0 / 29.0

        val fy = (l + 16) / 116.0
        val fx = fy + a / 500.0
        val fz = fy - b / 200.0

        val x = if (fx > theta) whitePoint!![0] * (fx * fx * fx) else (fx - 16.0 / 116.0) * 3.0 * (theta * theta) * whitePoint!![0].toDouble()
        val y = if (fy > theta) whitePoint!![1] * (fy * fy * fy) else (fy - 16.0 / 116.0) * 3.0 * (theta * theta) * whitePoint!![1].toDouble()
        val z = if (fz > theta) whitePoint!![2] * (fz * fz * fz) else (fz - 16.0 / 116.0) * 3.0 * (theta * theta) * whitePoint!![2].toDouble()

        val clinear = DoubleArray(3)
        clinear[0] = x * 3.2410 - y * 1.5374 - z * 0.4986 // red
        clinear[1] = -x * 0.9692 + y * 1.8760 - z * 0.0416 // green
        clinear[2] = x * 0.0556 - y * 0.2040 + z * 1.0570 // blue

        for (i in 0..2) {
            clinear[i] = if (clinear[i] <= 0.0031308)
                12.92 * clinear[i]
            else
                (1 + 0.055) * Math.pow(clinear[i], 1.0 / 2.4) - 0.055
            if (clinear[i] < 0)
                clinear[i] = 0.0
            else if (clinear[i] > 1f)
                clinear[i] = 1.0
        }

        return clinear
    }

    fun rgb2lab(baseColor: BaseColor): LabColor {
        val rLinear = (baseColor.red / 255f).toDouble()
        val gLinear = (baseColor.green / 255f).toDouble()
        val bLinear = (baseColor.blue / 255f).toDouble()

        // convert to a sRGB form
        val r = if (rLinear > 0.04045) Math.pow((rLinear + 0.055) / (1 + 0.055), 2.2) else rLinear / 12.92
        val g = if (gLinear > 0.04045) Math.pow((gLinear + 0.055) / (1 + 0.055), 2.2) else gLinear / 12.92
        val b = if (bLinear > 0.04045) Math.pow((bLinear + 0.055) / (1 + 0.055), 2.2) else bLinear / 12.92

        // converts
        val x = r * 0.4124 + g * 0.3576 + b * 0.1805
        val y = r * 0.2126 + g * 0.7152 + b * 0.0722
        val z = r * 0.0193 + g * 0.1192 + b * 0.9505

        val l = Math.round((116.0 * fXyz(y / whitePoint!![1]) - 16) * 1000) / 1000f
        val a = Math.round(500.0 * (fXyz(x / whitePoint!![0]) - fXyz(y / whitePoint!![1])) * 1000) / 1000f
        val bee = Math.round(200.0 * (fXyz(y / whitePoint!![1]) - fXyz(z / whitePoint!![2])) * 1000) / 1000f

        return LabColor(this, l, a, bee)
    }

    private fun fXyz(t: Double): Double {
        return if (t > 0.008856) Math.pow(t, 1.0 / 3.0) else 7.787 * t + 16.0 / 116.0
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is PdfLabColor) return false

        if (!Arrays.equals(blackPoint, o.blackPoint)) return false
        if (!Arrays.equals(range, o.range)) return false
        if (!Arrays.equals(whitePoint, o.whitePoint)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(whitePoint)
        result = 31 * result + if (blackPoint != null) Arrays.hashCode(blackPoint) else 0
        result = 31 * result + if (range != null) Arrays.hashCode(range) else 0
        return result
    }
}
