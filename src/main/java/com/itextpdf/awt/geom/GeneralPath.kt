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

import java.util.NoSuchElementException

import com.itextpdf.awt.geom.gl.Crossing
import com.itextpdf.awt.geom.misc.Messages

class GeneralPath @JvmOverloads constructor(rule: Int = GeneralPath.WIND_NON_ZERO, initialCapacity: Int = GeneralPath.BUFFER_SIZE) : Shape, Cloneable {

    /**
     * The point's types buffer
     */
    internal var types: ByteArray

    /**
     * The points buffer
     */
    internal var points: FloatArray

    /**
     * The point's type buffer size
     */
    internal var typeSize: Int = 0

    /**
     * The points buffer size
     */
    internal var pointSize: Int = 0

    /**
     * The path rule
     */
    // awt.209=Invalid winding rule value
    //$NON-NLS-1$
    var windingRule: Int = 0
        set(rule) {
            if (rule != WIND_EVEN_ODD && rule != WIND_NON_ZERO) {
                throw java.lang.IllegalArgumentException(Messages.getString("awt.209"))
            }
            this.windingRule = rule
        }

    /*
     * GeneralPath path iterator 
     */
    internal inner class Iterator
    /**
     * Constructs a new GeneralPath.Iterator for given general path and transformation
     * @param path - the source GeneralPath object
     * *
     * @param at - the AffineTransform object to apply rectangle path
     */
    @JvmOverloads constructor(
            /**
             * The source GeneralPath object
             */
            var p: GeneralPath,
            /**
             * The path iterator transformation
             */
            var t: AffineTransform? = null) : PathIterator {

        /**
         * The current cursor position in types buffer
         */
        var typeIndex: Int = 0

        /**
         * The current cursor position in points buffer
         */
        var pointIndex: Int = 0

        override val windingRule: Int
            get() = p.windingRule

        override val isDone: Boolean
            get() = typeIndex >= p.typeSize

        override fun next() {
            typeIndex++
        }

        override fun currentSegment(coords: DoubleArray): Int {
            if (isDone) {
                // awt.4B=Iterator out of bounds
                throw NoSuchElementException(Messages.getString("awt.4B")) //$NON-NLS-1$
            }
            val type = p.types[typeIndex].toInt()
            val count = GeneralPath.pointShift[type]
            for (i in 0..count - 1) {
                coords[i] = p.points[pointIndex + i].toDouble()
            }
            if (t != null) {
                t!!.transform(coords, 0, coords, 0, count / 2)
            }
            pointIndex += count
            return type
        }

        override fun currentSegment(coords: FloatArray): Int {
            if (isDone) {
                // awt.4B=Iterator out of bounds
                throw NoSuchElementException(Messages.getString("awt.4B")) //$NON-NLS-1$
            }
            val type = p.types[typeIndex].toInt()
            val count = GeneralPath.pointShift[type]
            System.arraycopy(p.points, pointIndex, coords, 0, count)
            if (t != null) {
                t!!.transform(coords, 0, coords, 0, count / 2)
            }
            pointIndex += count
            return type
        }

    }

    /**
     * Constructs a new GeneralPath.Iterator for given general path
     * @param path - the source GeneralPath object
     */

    init {
        windingRule = rule
        types = ByteArray(initialCapacity)
        points = FloatArray(initialCapacity * 2)
    }

    constructor(shape: Shape) : this(WIND_NON_ZERO, BUFFER_SIZE) {
        val p = shape.getPathIterator(null)
        windingRule = p.windingRule
        append(p, false)
    }

    /**
     * Checks points and types buffer size to add pointCount points. If necessary realloc buffers to enlarge size.
     * @param pointCount - the point count to be added in buffer
     */
    internal fun checkBuf(pointCount: Int, checkMove: Boolean) {
        if (checkMove && typeSize == 0) {
            // awt.20A=First segment should be SEG_MOVETO type
            throw IllegalPathStateException(Messages.getString("awt.20A")) //$NON-NLS-1$
        }
        if (typeSize == types.size) {
            val tmp = ByteArray(typeSize + BUFFER_CAPACITY)
            System.arraycopy(types, 0, tmp, 0, typeSize)
            types = tmp
        }
        if (pointSize + pointCount > points.size) {
            val tmp = FloatArray(pointSize + Math.max(BUFFER_CAPACITY * 2, pointCount))
            System.arraycopy(points, 0, tmp, 0, pointSize)
            points = tmp
        }
    }

    fun moveTo(x: Float, y: Float) {
        if (typeSize > 0 && types[typeSize - 1].toInt() == PathIterator.SEG_MOVETO) {
            points[pointSize - 2] = x
            points[pointSize - 1] = y
        } else {
            checkBuf(2, false)
            types[typeSize++] = PathIterator.SEG_MOVETO.toByte()
            points[pointSize++] = x
            points[pointSize++] = y
        }
    }

    fun lineTo(x: Float, y: Float) {
        checkBuf(2, true)
        types[typeSize++] = PathIterator.SEG_LINETO.toByte()
        points[pointSize++] = x
        points[pointSize++] = y
    }

    fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float) {
        checkBuf(4, true)
        types[typeSize++] = PathIterator.SEG_QUADTO.toByte()
        points[pointSize++] = x1
        points[pointSize++] = y1
        points[pointSize++] = x2
        points[pointSize++] = y2
    }

    fun curveTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        checkBuf(6, true)
        types[typeSize++] = PathIterator.SEG_CUBICTO.toByte()
        points[pointSize++] = x1
        points[pointSize++] = y1
        points[pointSize++] = x2
        points[pointSize++] = y2
        points[pointSize++] = x3
        points[pointSize++] = y3
    }

    fun closePath() {
        if (typeSize == 0 || types[typeSize - 1].toInt() != PathIterator.SEG_CLOSE) {
            checkBuf(0, true)
            types[typeSize++] = PathIterator.SEG_CLOSE.toByte()
        }
    }

    fun append(shape: Shape, connect: Boolean) {
        val p = shape.getPathIterator(null)
        append(p, connect)
    }

    fun append(path: PathIterator, connect: Boolean) {
        var connect = connect
        while (!path.isDone) {
            val coords = FloatArray(6)
            when (path.currentSegment(coords)) {
                PathIterator.SEG_MOVETO -> {
                    if (!connect || typeSize == 0) {
                        moveTo(coords[0], coords[1])
                        break
                    }
                    if (types[typeSize - 1].toInt() != PathIterator.SEG_CLOSE &&
                            points[pointSize - 2] == coords[0] &&
                            points[pointSize - 1] == coords[1]) {
                        break
                    }
                    lineTo(coords[0], coords[1])
                }
            // NO BREAK;
                PathIterator.SEG_LINETO -> lineTo(coords[0], coords[1])
                PathIterator.SEG_QUADTO -> quadTo(coords[0], coords[1], coords[2], coords[3])
                PathIterator.SEG_CUBICTO -> curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5])
                PathIterator.SEG_CLOSE -> closePath()
            }
            path.next()
            connect = false
        }
    }

    val currentPoint: Point2D?
        get() {
            if (typeSize == 0) {
                return null
            }
            var j = pointSize - 2
            if (types[typeSize - 1].toInt() == PathIterator.SEG_CLOSE) {

                for (i in typeSize - 2 downTo 1) {
                    val type = types[i].toInt()
                    if (type == PathIterator.SEG_MOVETO) {
                        break
                    }
                    j -= pointShift[type]
                }
            }
            return Point2D.Float(points[j], points[j + 1])
        }

    fun reset() {
        typeSize = 0
        pointSize = 0
    }

    fun transform(t: AffineTransform) {
        t.transform(points, 0, points, 0, pointSize / 2)
    }

    fun createTransformedShape(t: AffineTransform?): Shape {
        val p = clone() as GeneralPath
        if (t != null) {
            p.transform(t)
        }
        return p
    }

    override val bounds2D: Rectangle2D
        get() {
            var rx1: Float
            var ry1: Float
            var rx2: Float
            var ry2: Float
            if (pointSize == 0) {
                rx1 = ry1 = rx2 = ry2 = 0.0f
            } else {
                var i = pointSize - 1
                ry1 = ry2 = points[i--]
                rx1 = rx2 = points[i--]
                while (i > 0) {
                    val y = points[i--]
                    val x = points[i--]
                    if (x < rx1) {
                        rx1 = x
                    } else if (x > rx2) {
                        rx2 = x
                    }
                    if (y < ry1) {
                        ry1 = y
                    } else if (y > ry2) {
                        ry2 = y
                    }
                }
            }
            return Rectangle2D.Float(rx1, ry1, rx2 - rx1, ry2 - ry1)
        }

    override val bounds: Rectangle
        get() = bounds2D.bounds

    /**
     * Checks cross count according to path rule to define is it point inside shape or not.
     * @param cross - the point cross count
     * *
     * @return true if point is inside path, or false otherwise
     */
    internal fun isInside(cross: Int): Boolean {
        if (windingRule == WIND_NON_ZERO) {
            return Crossing.isInsideNonZero(cross)
        }
        return Crossing.isInsideEvenOdd(cross)
    }

    override fun contains(px: Double, py: Double): Boolean {
        return isInside(Crossing.crossShape(this, px, py))
    }

    override fun contains(rx: Double, ry: Double, rw: Double, rh: Double): Boolean {
        val cross = Crossing.intersectShape(this, rx, ry, rw, rh)
        return cross != Crossing.CROSSING && isInside(cross)
    }

    override fun intersects(rx: Double, ry: Double, rw: Double, rh: Double): Boolean {
        val cross = Crossing.intersectShape(this, rx, ry, rw, rh)
        return cross == Crossing.CROSSING || isInside(cross)
    }

    override fun contains(p: Point2D): Boolean {
        return contains(p.x, p.y)
    }

    override fun contains(r: Rectangle2D): Boolean {
        return contains(r.x, r.y, r.width, r.height)
    }

    override fun intersects(r: Rectangle2D): Boolean {
        return intersects(r.x, r.y, r.width, r.height)
    }

    override fun getPathIterator(t: AffineTransform): PathIterator {
        return Iterator(this, t)
    }

    override fun getPathIterator(t: AffineTransform, flatness: Double): PathIterator {
        return FlatteningPathIterator(getPathIterator(t), flatness)
    }

    public override fun clone(): Any {
        try {
            val p = super.clone() as GeneralPath
            p.types = types.clone()
            p.points = points.clone()
            return p
        } catch (e: CloneNotSupportedException) {
            throw InternalError()
        }

    }

    companion object {

        val WIND_EVEN_ODD = PathIterator.WIND_EVEN_ODD
        val WIND_NON_ZERO = PathIterator.WIND_NON_ZERO

        /**
         * The buffers size
         */
        private val BUFFER_SIZE = 10

        /**
         * The buffers capacity
         */
        private val BUFFER_CAPACITY = 10

        /**
         * The space amount in points buffer for different segmenet's types
         */
        internal var pointShift = intArrayOf(2, // MOVETO
                2, // LINETO
                4, // QUADTO
                6, // CUBICTO
                0) // CLOSE
    }

}

