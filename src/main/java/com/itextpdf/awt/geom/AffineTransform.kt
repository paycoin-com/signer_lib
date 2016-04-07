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
/**
 * @author Denis M. Kishenko
 */
package com.itextpdf.awt.geom

import java.io.IOException
import java.io.Serializable

import com.itextpdf.awt.geom.misc.HashCode
import com.itextpdf.awt.geom.misc.Messages

class AffineTransform : Cloneable, Serializable {

    /**
     * The values of transformation matrix
     */
    var scaleX: Double = 0.toDouble()
        internal set
    var shearY: Double = 0.toDouble()
        internal set
    var shearX: Double = 0.toDouble()
        internal set
    var scaleY: Double = 0.toDouble()
        internal set
    var translateX: Double = 0.toDouble()
        internal set
    var translateY: Double = 0.toDouble()
        internal set

    /**
     * The transformation `type`
     */
    @Transient internal var type: Int = 0

    constructor() {
        type = TYPE_IDENTITY
        scaleX = scaleY = 1.0
        shearY = shearX = translateX = translateY = 0.0
    }

    constructor(t: AffineTransform) {
        this.type = t.type
        this.scaleX = t.scaleX
        this.shearY = t.shearY
        this.shearX = t.shearX
        this.scaleY = t.scaleY
        this.translateX = t.translateX
        this.translateY = t.translateY
    }

    constructor(m00: Float, m10: Float, m01: Float, m11: Float, m02: Float, m12: Float) {
        this.type = TYPE_UNKNOWN
        this.scaleX = m00.toDouble()
        this.shearY = m10.toDouble()
        this.shearX = m01.toDouble()
        this.scaleY = m11.toDouble()
        this.translateX = m02.toDouble()
        this.translateY = m12.toDouble()
    }

    constructor(m00: Double, m10: Double, m01: Double, m11: Double, m02: Double, m12: Double) {
        this.type = TYPE_UNKNOWN
        this.scaleX = m00
        this.shearY = m10
        this.shearX = m01
        this.scaleY = m11
        this.translateX = m02
        this.translateY = m12
    }

    constructor(matrix: FloatArray) {
        this.type = TYPE_UNKNOWN
        scaleX = matrix[0].toDouble()
        shearY = matrix[1].toDouble()
        shearX = matrix[2].toDouble()
        scaleY = matrix[3].toDouble()
        if (matrix.size > 4) {
            translateX = matrix[4].toDouble()
            translateY = matrix[5].toDouble()
        }
    }

    constructor(matrix: DoubleArray) {
        this.type = TYPE_UNKNOWN
        scaleX = matrix[0]
        shearY = matrix[1]
        shearX = matrix[2]
        scaleY = matrix[3]
        if (matrix.size > 4) {
            translateX = matrix[4]
            translateY = matrix[5]
        }
    }

    /*
     * Method returns type of affine transformation.
     * 
     * Transform matrix is
     *   m00 m01 m02
     *   m10 m11 m12
     * 
     * According analytic geometry new basis vectors are (m00, m01) and (m10, m11), 
     * translation vector is (m02, m12). Original basis vectors are (1, 0) and (0, 1). 
     * Type transformations classification:  
     *   TYPE_IDENTITY - new basis equals original one and zero translation
     *   TYPE_TRANSLATION - translation vector isn't zero  
     *   TYPE_UNIFORM_SCALE - vectors length of new basis equals
     *   TYPE_GENERAL_SCALE - vectors length of new basis doesn't equal 
     *   TYPE_FLIP - new basis vector orientation differ from original one
     *   TYPE_QUADRANT_ROTATION - new basis is rotated by 90, 180, 270, or 360 degrees     
     *   TYPE_GENERAL_ROTATION - new basis is rotated by arbitrary angle
     *   TYPE_GENERAL_TRANSFORM - transformation can't be inversed
     */
    fun getType(): Int {
        if (type != TYPE_UNKNOWN) {
            return type
        }

        var type = 0

        if (scaleX * shearX + shearY * scaleY != 0.0) {
            type = type or TYPE_GENERAL_TRANSFORM
            return type
        }

        if (translateX != 0.0 || translateY != 0.0) {
            type = type or TYPE_TRANSLATION
        } else if (scaleX == 1.0 && scaleY == 1.0 && shearX == 0.0 && shearY == 0.0) {
            type = TYPE_IDENTITY
            return type
        }

        if (scaleX * scaleY - shearX * shearY < 0.0) {
            type = type or TYPE_FLIP
        }

        val dx = scaleX * scaleX + shearY * shearY
        val dy = shearX * shearX + scaleY * scaleY
        if (dx != dy) {
            type = type or TYPE_GENERAL_SCALE
        } else if (dx != 1.0) {
            type = type or TYPE_UNIFORM_SCALE
        }

        if (scaleX == 0.0 && scaleY == 0.0 || shearY == 0.0 && shearX == 0.0 && (scaleX < 0.0 || scaleY < 0.0)) {
            type = type or TYPE_QUADRANT_ROTATION
        } else if (shearX != 0.0 || shearY != 0.0) {
            type = type or TYPE_GENERAL_ROTATION
        }

        return type
    }

    val isIdentity: Boolean
        get() = getType() == TYPE_IDENTITY

    fun getMatrix(matrix: DoubleArray) {
        matrix[0] = scaleX
        matrix[1] = shearY
        matrix[2] = shearX
        matrix[3] = scaleY
        if (matrix.size > 4) {
            matrix[4] = translateX
            matrix[5] = translateY
        }
    }

    val determinant: Double
        get() = scaleX * scaleY - shearX * shearY

    fun setTransform(m00: Double, m10: Double, m01: Double, m11: Double, m02: Double, m12: Double) {
        this.type = TYPE_UNKNOWN
        this.scaleX = m00
        this.shearY = m10
        this.shearX = m01
        this.scaleY = m11
        this.translateX = m02
        this.translateY = m12
    }

    fun setTransform(t: AffineTransform) {
        type = t.type
        setTransform(t.scaleX, t.shearY, t.shearX, t.scaleY, t.translateX, t.translateY)
    }

    fun setToIdentity() {
        type = TYPE_IDENTITY
        scaleX = scaleY = 1.0
        shearY = shearX = translateX = translateY = 0.0
    }

    fun setToTranslation(mx: Double, my: Double) {
        scaleX = scaleY = 1.0
        shearX = shearY = 0.0
        translateX = mx
        translateY = my
        if (mx == 0.0 && my == 0.0) {
            type = TYPE_IDENTITY
        } else {
            type = TYPE_TRANSLATION
        }
    }

    fun setToScale(scx: Double, scy: Double) {
        scaleX = scx
        scaleY = scy
        shearY = shearX = translateX = translateY = 0.0
        if (scx != 1.0 || scy != 1.0) {
            type = TYPE_UNKNOWN
        } else {
            type = TYPE_IDENTITY
        }
    }

    fun setToShear(shx: Double, shy: Double) {
        scaleX = scaleY = 1.0
        translateX = translateY = 0.0
        shearX = shx
        shearY = shy
        if (shx != 0.0 || shy != 0.0) {
            type = TYPE_UNKNOWN
        } else {
            type = TYPE_IDENTITY
        }
    }

    fun setToRotation(angle: Double) {
        var sin = Math.sin(angle)
        var cos = Math.cos(angle)
        if (Math.abs(cos) < ZERO) {
            cos = 0.0
            sin = if (sin > 0.0) 1.0 else -1.0
        } else if (Math.abs(sin) < ZERO) {
            sin = 0.0
            cos = if (cos > 0.0) 1.0 else -1.0
        }
        scaleX = scaleY = cos
        shearX = -sin
        shearY = sin
        translateX = translateY = 0.0
        type = TYPE_UNKNOWN
    }

    fun setToRotation(angle: Double, px: Double, py: Double) {
        setToRotation(angle)
        translateX = px * (1.0 - scaleX) + py * shearY
        translateY = py * (1.0 - scaleX) - px * shearY
        type = TYPE_UNKNOWN
    }

    fun translate(mx: Double, my: Double) {
        concatenate(AffineTransform.getTranslateInstance(mx, my))
    }

    fun scale(scx: Double, scy: Double) {
        concatenate(AffineTransform.getScaleInstance(scx, scy))
    }

    fun shear(shx: Double, shy: Double) {
        concatenate(AffineTransform.getShearInstance(shx, shy))
    }

    fun rotate(angle: Double) {
        concatenate(AffineTransform.getRotateInstance(angle))
    }

    fun rotate(angle: Double, px: Double, py: Double) {
        concatenate(AffineTransform.getRotateInstance(angle, px, py))
    }

    /**
     * Multiply matrix of two AffineTransform objects
     * @param t1 - the AffineTransform object is a multiplicand
     * *
     * @param t2 - the AffineTransform object is a multiplier
     * *
     * @return an AffineTransform object that is a result of t1 multiplied by matrix t2.
     */
    internal fun multiply(t1: AffineTransform, t2: AffineTransform): AffineTransform {
        return AffineTransform(
                t1.scaleX * t2.scaleX + t1.shearY * t2.shearX, // m00
                t1.scaleX * t2.shearY + t1.shearY * t2.scaleY, // m01
                t1.shearX * t2.scaleX + t1.scaleY * t2.shearX, // m10
                t1.shearX * t2.shearY + t1.scaleY * t2.scaleY, // m11
                t1.translateX * t2.scaleX + t1.translateY * t2.shearX + t2.translateX, // m02
                t1.translateX * t2.shearY + t1.translateY * t2.scaleY + t2.translateY)// m12
    }

    fun concatenate(t: AffineTransform) {
        setTransform(multiply(t, this))
    }

    fun preConcatenate(t: AffineTransform) {
        setTransform(multiply(this, t))
    }

    @Throws(NoninvertibleTransformException::class)
    fun createInverse(): AffineTransform {
        val det = determinant
        if (Math.abs(det) < ZERO) {
            // awt.204=Determinant is zero
            throw NoninvertibleTransformException(Messages.getString("awt.204")) //$NON-NLS-1$
        }
        return AffineTransform(
                scaleY / det, // m00
                -shearY / det, // m10
                -shearX / det, // m01
                scaleX / det, // m11
                (shearX * translateY - scaleY * translateX) / det, // m02
                (shearY * translateX - scaleX * translateY) / det  // m12
        )
    }

    fun transform(src: Point2D, dst: Point2D?): Point2D {
        var dst = dst
        if (dst == null) {
            if (src is Point2D.Double) {
                dst = Point2D.Double()
            } else {
                dst = Point2D.Float()
            }
        }

        val x = src.x
        val y = src.y

        dst.setLocation(x * scaleX + y * shearX + translateX, x * shearY + y * scaleY + translateY)
        return dst
    }

    fun transform(src: Array<Point2D>, srcOff: Int, dst: Array<Point2D>, dstOff: Int, length: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        var length = length
        while (--length >= 0) {
            val srcPoint = src[srcOff++]
            val x = srcPoint.x
            val y = srcPoint.y
            var dstPoint: Point2D? = dst[dstOff]
            if (dstPoint == null) {
                if (srcPoint is Point2D.Double) {
                    dstPoint = Point2D.Double()
                } else {
                    dstPoint = Point2D.Float()
                }
            }
            dstPoint.setLocation(x * scaleX + y * shearX + translateX, x * shearY + y * scaleY + translateY)
            dst[dstOff++] = dstPoint
        }
    }

    fun transform(src: DoubleArray, srcOff: Int, dst: DoubleArray, dstOff: Int, length: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        var length = length
        var step = 2
        if (src == dst && srcOff < dstOff && dstOff < srcOff + length * 2) {
            srcOff = srcOff + length * 2 - 2
            dstOff = dstOff + length * 2 - 2
            step = -2
        }
        while (--length >= 0) {
            val x = src[srcOff + 0]
            val y = src[srcOff + 1]
            dst[dstOff + 0] = x * scaleX + y * shearX + translateX
            dst[dstOff + 1] = x * shearY + y * scaleY + translateY
            srcOff += step
            dstOff += step
        }
    }

    fun transform(src: FloatArray, srcOff: Int, dst: FloatArray, dstOff: Int, length: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        var length = length
        var step = 2
        if (src == dst && srcOff < dstOff && dstOff < srcOff + length * 2) {
            srcOff = srcOff + length * 2 - 2
            dstOff = dstOff + length * 2 - 2
            step = -2
        }
        while (--length >= 0) {
            val x = src[srcOff + 0]
            val y = src[srcOff + 1]
            dst[dstOff + 0] = (x * scaleX + y * shearX + translateX).toFloat()
            dst[dstOff + 1] = (x * shearY + y * scaleY + translateY).toFloat()
            srcOff += step
            dstOff += step
        }
    }

    fun transform(src: FloatArray, srcOff: Int, dst: DoubleArray, dstOff: Int, length: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        var length = length
        while (--length >= 0) {
            val x = src[srcOff++]
            val y = src[srcOff++]
            dst[dstOff++] = x * scaleX + y * shearX + translateX
            dst[dstOff++] = x * shearY + y * scaleY + translateY
        }
    }

    fun transform(src: DoubleArray, srcOff: Int, dst: FloatArray, dstOff: Int, length: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        var length = length
        while (--length >= 0) {
            val x = src[srcOff++]
            val y = src[srcOff++]
            dst[dstOff++] = (x * scaleX + y * shearX + translateX).toFloat()
            dst[dstOff++] = (x * shearY + y * scaleY + translateY).toFloat()
        }
    }

    fun deltaTransform(src: Point2D, dst: Point2D?): Point2D {
        var dst = dst
        if (dst == null) {
            if (src is Point2D.Double) {
                dst = Point2D.Double()
            } else {
                dst = Point2D.Float()
            }
        }

        val x = src.x
        val y = src.y

        dst.setLocation(x * scaleX + y * shearX, x * shearY + y * scaleY)
        return dst
    }

    fun deltaTransform(src: DoubleArray, srcOff: Int, dst: DoubleArray, dstOff: Int, length: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        var length = length
        while (--length >= 0) {
            val x = src[srcOff++]
            val y = src[srcOff++]
            dst[dstOff++] = x * scaleX + y * shearX
            dst[dstOff++] = x * shearY + y * scaleY
        }
    }

    @Throws(NoninvertibleTransformException::class)
    fun inverseTransform(src: Point2D, dst: Point2D?): Point2D {
        var dst = dst
        val det = determinant
        if (Math.abs(det) < ZERO) {
            // awt.204=Determinant is zero
            throw NoninvertibleTransformException(Messages.getString("awt.204")) //$NON-NLS-1$
        }

        if (dst == null) {
            if (src is Point2D.Double) {
                dst = Point2D.Double()
            } else {
                dst = Point2D.Float()
            }
        }

        val x = src.x - translateX
        val y = src.y - translateY

        dst.setLocation((x * scaleY - y * shearX) / det, (y * scaleX - x * shearY) / det)
        return dst
    }

    @Throws(NoninvertibleTransformException::class)
    fun inverseTransform(src: DoubleArray, srcOff: Int, dst: DoubleArray, dstOff: Int, length: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        var length = length
        val det = determinant
        if (Math.abs(det) < ZERO) {
            // awt.204=Determinant is zero
            throw NoninvertibleTransformException(Messages.getString("awt.204")) //$NON-NLS-1$
        }

        while (--length >= 0) {
            val x = src[srcOff++] - translateX
            val y = src[srcOff++] - translateY
            dst[dstOff++] = (x * scaleY - y * shearX) / det
            dst[dstOff++] = (y * scaleX - x * shearY) / det
        }
    }

    @Throws(NoninvertibleTransformException::class)
    fun inverseTransform(src: FloatArray, srcOff: Int, dst: FloatArray, dstOff: Int, length: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        var length = length
        val det = determinant.toFloat()
        if (Math.abs(det) < ZERO) {
            // awt.204=Determinant is zero
            throw NoninvertibleTransformException(Messages.getString("awt.204")) //$NON-NLS-1$
        }

        while (--length >= 0) {
            val x = src[srcOff++] - translateX.toFloat()
            val y = src[srcOff++] - translateY.toFloat()
            dst[dstOff++] = (x * scaleY.toFloat() - y * shearX.toFloat()) / det
            dst[dstOff++] = (y * scaleX.toFloat() - x * shearY.toFloat()) / det
        }
    }

    fun createTransformedShape(src: Shape?): Shape? {
        if (src == null) {
            return null
        }
        if (src is GeneralPath) {
            return src.createTransformedShape(this)
        }
        val path = src.getPathIterator(this)
        val dst = GeneralPath(path.windingRule)
        dst.append(path, false)
        return dst
    }

    override fun toString(): String {
        return javaClass.name +
                "[[" + scaleX + ", " + shearX + ", " + translateX + "], [" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        +shearY + ", " + scaleY + ", " + translateY + "]]" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public override fun clone(): Any {
        try {
            return super.clone()
        } catch (e: CloneNotSupportedException) {
            throw InternalError()
        }

    }

    override fun hashCode(): Int {
        val hash = HashCode()
        hash.append(scaleX)
        hash.append(shearX)
        hash.append(translateX)
        hash.append(shearY)
        hash.append(scaleY)
        hash.append(translateY)
        return hash.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is AffineTransform) {
            return scaleX == obj.scaleX && shearX == obj.shearX &&
                    translateX == obj.translateX && shearY == obj.shearY &&
                    scaleY == obj.scaleY && translateY == obj.translateY
        }
        return false
    }


    /**
     * Write AffineTrasform object to the output steam.
     * @param stream - the output stream
     * *
     * @throws IOException - if there are I/O errors while writing to the output strem
     */
    @Throws(IOException::class)
    private fun writeObject(stream: java.io.ObjectOutputStream) {
        stream.defaultWriteObject()
    }


    /**
     * Read AffineTransform object from the input stream
     * @param stream - the input steam
     * *
     * @throws IOException - if there are I/O errors while reading from the input strem
     * *
     * @throws ClassNotFoundException - if class could not be found
     */
    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(stream: java.io.ObjectInputStream) {
        stream.defaultReadObject()
        type = TYPE_UNKNOWN
    }

    companion object {

        private val serialVersionUID = 1330973210523860834L

        val TYPE_IDENTITY = 0
        val TYPE_TRANSLATION = 1
        val TYPE_UNIFORM_SCALE = 2
        val TYPE_GENERAL_SCALE = 4
        val TYPE_QUADRANT_ROTATION = 8
        val TYPE_GENERAL_ROTATION = 16
        val TYPE_GENERAL_TRANSFORM = 32
        val TYPE_FLIP = 64
        val TYPE_MASK_SCALE = TYPE_UNIFORM_SCALE or TYPE_GENERAL_SCALE
        val TYPE_MASK_ROTATION = TYPE_QUADRANT_ROTATION or TYPE_GENERAL_ROTATION

        /**
         * The `TYPE_UNKNOWN` is an initial type value
         */
        internal val TYPE_UNKNOWN = -1

        /**
         * The min value equivalent to zero. If absolute value less then ZERO it considered as zero.
         */
        internal val ZERO = 1E-10.0

        fun getTranslateInstance(mx: Double, my: Double): AffineTransform {
            val t = AffineTransform()
            t.setToTranslation(mx, my)
            return t
        }

        fun getScaleInstance(scx: Double, scY: Double): AffineTransform {
            val t = AffineTransform()
            t.setToScale(scx, scY)
            return t
        }

        fun getShearInstance(shx: Double, shy: Double): AffineTransform {
            val m = AffineTransform()
            m.setToShear(shx, shy)
            return m
        }

        fun getRotateInstance(angle: Double): AffineTransform {
            val t = AffineTransform()
            t.setToRotation(angle)
            return t
        }

        fun getRotateInstance(angle: Double, x: Double, y: Double): AffineTransform {
            val t = AffineTransform()
            t.setToRotation(angle, x, y)
            return t
        }
    }

}

