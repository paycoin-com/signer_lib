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

import com.itextpdf.awt.geom.misc.Messages

abstract class Line2D protected constructor() : Shape, Cloneable {

    class Float : Line2D {

        var x1: Float = 0.toFloat()
        var y1: Float = 0.toFloat()
        var x2: Float = 0.toFloat()
        var y2: Float = 0.toFloat()

        constructor() {
        }

        constructor(x1: Float, y1: Float, x2: Float, y2: Float) {
            setLine(x1, y1, x2, y2)
        }

        constructor(p1: Point2D, p2: Point2D) {
            setLine(p1, p2)
        }

        override fun getX1(): Double {
            return x1.toDouble()
        }

        override fun getY1(): Double {
            return y1.toDouble()
        }

        override fun getX2(): Double {
            return x2.toDouble()
        }

        override fun getY2(): Double {
            return y2.toDouble()
        }

        override val p1: Point2D
            get() = Point2D.Float(x1, y1)

        override val p2: Point2D
            get() = Point2D.Float(x2, y2)

        override fun setLine(x1: Double, y1: Double, x2: Double, y2: Double) {
            this.x1 = x1.toFloat()
            this.y1 = y1.toFloat()
            this.x2 = x2.toFloat()
            this.y2 = y2.toFloat()
        }

        fun setLine(x1: Float, y1: Float, x2: Float, y2: Float) {
            this.x1 = x1
            this.y1 = y1
            this.x2 = x2
            this.y2 = y2
        }

        override val bounds2D: Rectangle2D
            get() {
                val rx: Float
                val ry: Float
                val rw: Float
                val rh: Float
                if (x1 < x2) {
                    rx = x1
                    rw = x2 - x1
                } else {
                    rx = x2
                    rw = x1 - x2
                }
                if (y1 < y2) {
                    ry = y1
                    rh = y2 - y1
                } else {
                    ry = y2
                    rh = y1 - y2
                }
                return Rectangle2D.Float(rx, ry, rw, rh)
            }
    }

    class Double : Line2D {

        override var x1: Double = 0.toDouble()
        override var y1: Double = 0.toDouble()
        override var x2: Double = 0.toDouble()
        override var y2: Double = 0.toDouble()

        constructor() {
        }

        constructor(x1: Double, y1: Double, x2: Double, y2: Double) {
            setLine(x1, y1, x2, y2)
        }

        constructor(p1: Point2D, p2: Point2D) {
            setLine(p1, p2)
        }

        override val p1: Point2D
            get() = Point2D.Double(x1, y1)

        override val p2: Point2D
            get() = Point2D.Double(x2, y2)

        override fun setLine(x1: Double, y1: Double, x2: Double, y2: Double) {
            this.x1 = x1
            this.y1 = y1
            this.x2 = x2
            this.y2 = y2
        }

        override val bounds2D: Rectangle2D
            get() {
                val rx: Double
                val ry: Double
                val rw: Double
                val rh: Double
                if (x1 < x2) {
                    rx = x1
                    rw = x2 - x1
                } else {
                    rx = x2
                    rw = x1 - x2
                }
                if (y1 < y2) {
                    ry = y1
                    rh = y2 - y1
                } else {
                    ry = y2
                    rh = y1 - y2
                }
                return Rectangle2D.Double(rx, ry, rw, rh)
            }
    }

    /*
     * Line2D path iterator 
     */
    internal inner class Iterator
    /**
     * Constructs a new Line2D.Iterator for given line and transformation
     * @param l - the source Line2D object
     * *
     * @param at - the AffineTransform object to apply rectangle path
     */
    (l: Line2D,
     /**
      * The path iterator transformation
      */
     var t: AffineTransform?) : PathIterator {

        /**
         * The x coordinate of the start line point
         */
        var x1: Double = 0.toDouble()

        /**
         * The y coordinate of the start line point
         */
        var y1: Double = 0.toDouble()

        /**
         * The x coordinate of the end line point
         */
        var x2: Double = 0.toDouble()

        /**
         * The y coordinate of the end line point
         */
        var y2: Double = 0.toDouble()

        /**
         * The current segmenet index
         */
        var index: Int = 0

        init {
            this.x1 = l.x1
            this.y1 = l.y1
            this.x2 = l.x2
            this.y2 = l.y2
        }

        override val windingRule: Int
            get() = PathIterator.WIND_NON_ZERO

        override val isDone: Boolean
            get() = index > 1

        override fun next() {
            index++
        }

        override fun currentSegment(coords: DoubleArray): Int {
            if (isDone) {
                // awt.4B=Iterator out of bounds
                throw NoSuchElementException(Messages.getString("awt.4B")) //$NON-NLS-1$
            }
            val type: Int
            if (index == 0) {
                type = PathIterator.SEG_MOVETO
                coords[0] = x1
                coords[1] = y1
            } else {
                type = PathIterator.SEG_LINETO
                coords[0] = x2
                coords[1] = y2
            }
            if (t != null) {
                t!!.transform(coords, 0, coords, 0, 1)
            }
            return type
        }

        override fun currentSegment(coords: FloatArray): Int {
            if (isDone) {
                // awt.4B=Iterator out of bounds
                throw NoSuchElementException(Messages.getString("awt.4B")) //$NON-NLS-1$
            }
            val type: Int
            if (index == 0) {
                type = PathIterator.SEG_MOVETO
                coords[0] = x1.toFloat()
                coords[1] = y1.toFloat()
            } else {
                type = PathIterator.SEG_LINETO
                coords[0] = x2.toFloat()
                coords[1] = y2.toFloat()
            }
            if (t != null) {
                t!!.transform(coords, 0, coords, 0, 1)
            }
            return type
        }

    }

    abstract val x1: Double

    abstract val y1: Double

    abstract val x2: Double

    abstract val y2: Double

    abstract val p1: Point2D

    abstract val p2: Point2D

    abstract fun setLine(x1: Double, y1: Double, x2: Double, y2: Double)

    fun setLine(p1: Point2D, p2: Point2D) {
        setLine(p1.x, p1.y, p2.x, p2.y)
    }

    fun setLine(line: Line2D) {
        setLine(line.x1, line.y1, line.x2, line.y2)
    }

    override val bounds: Rectangle
        get() = bounds2D.bounds

    fun relativeCCW(px: Double, py: Double): Int {
        return relativeCCW(x1, y1, x2, y2, px, py)
    }

    fun relativeCCW(p: Point2D): Int {
        return relativeCCW(x1, y1, x2, y2, p.x, p.y)
    }

    fun intersectsLine(x1: Double, y1: Double, x2: Double, y2: Double): Boolean {
        return linesIntersect(x1, y1, x2, y2, x1, y1, x2, y2)
    }

    fun intersectsLine(l: Line2D): Boolean {
        return linesIntersect(l.x1, l.y1, l.x2, l.y2, x1, y1, x2, y2)
    }

    fun ptSegDistSq(px: Double, py: Double): Double {
        return ptSegDistSq(x1, y1, x2, y2, px, py)
    }

    fun ptSegDistSq(p: Point2D): Double {
        return ptSegDistSq(x1, y1, x2, y2, p.x, p.y)
    }

    fun ptSegDist(px: Double, py: Double): Double {
        return ptSegDist(x1, y1, x2, y2, px, py)
    }

    fun ptSegDist(p: Point2D): Double {
        return ptSegDist(x1, y1, x2, y2, p.x, p.y)
    }

    fun ptLineDistSq(px: Double, py: Double): Double {
        return ptLineDistSq(x1, y1, x2, y2, px, py)
    }

    fun ptLineDistSq(p: Point2D): Double {
        return ptLineDistSq(x1, y1, x2, y2, p.x, p.y)
    }

    fun ptLineDist(px: Double, py: Double): Double {
        return ptLineDist(x1, y1, x2, y2, px, py)
    }

    fun ptLineDist(p: Point2D): Double {
        return ptLineDist(x1, y1, x2, y2, p.x, p.y)
    }

    override fun contains(px: Double, py: Double): Boolean {
        return false
    }

    override fun contains(p: Point2D): Boolean {
        return false
    }

    override fun contains(r: Rectangle2D): Boolean {
        return false
    }

    override fun contains(rx: Double, ry: Double, rw: Double, rh: Double): Boolean {
        return false
    }

    override fun intersects(rx: Double, ry: Double, rw: Double, rh: Double): Boolean {
        return intersects(Rectangle2D.Double(rx, ry, rw, rh))
    }

    override fun intersects(r: Rectangle2D): Boolean {
        return r.intersectsLine(x1, y1, x2, y2)
    }

    override fun getPathIterator(at: AffineTransform): PathIterator {
        return Iterator(this, at)
    }

    override fun getPathIterator(at: AffineTransform, flatness: Double): PathIterator {
        return Iterator(this, at)
    }

    public override fun clone(): Any {
        try {
            return super.clone()
        } catch (e: CloneNotSupportedException) {
            throw InternalError()
        }

    }

    companion object {

        fun relativeCCW(x1: Double, y1: Double, x2: Double, y2: Double, px: Double, py: Double): Int {
            var x2 = x2
            var y2 = y2
            var px = px
            var py = py
            /*
         * A = (x2-x1, y2-y1) P = (px-x1, py-y1)
         */
            x2 -= x1
            y2 -= y1
            px -= x1
            py -= y1
            var t = px * y2 - py * x2 // PxA
            if (t == 0.0) {
                t = px * x2 + py * y2 // P*A
                if (t > 0.0) {
                    px -= x2 // B-A
                    py -= y2
                    t = px * x2 + py * y2 // (P-A)*A
                    if (t < 0.0) {
                        t = 0.0
                    }
                }
            }

            return if (t < 0.0) -1 else if (t > 0.0) 1 else 0
        }

        fun linesIntersect(x1: Double, y1: Double, x2: Double,
                           y2: Double, x3: Double, y3: Double, x4: Double, y4: Double): Boolean {
            var x2 = x2
            var y2 = y2
            var x3 = x3
            var y3 = y3
            var x4 = x4
            var y4 = y4
            /*
         * A = (x2-x1, y2-y1) B = (x3-x1, y3-y1) C = (x4-x1, y4-y1) D = (x4-x3,
         * y4-y3) = C-B E = (x1-x3, y1-y3) = -B F = (x2-x3, y2-y3) = A-B
         *
         * Result is ((AxB) * (AxC) <=0) and ((DxE) * (DxF) <= 0)
         *
         * DxE = (C-B)x(-B) = BxB-CxB = BxC DxF = (C-B)x(A-B) = CxA-CxB-BxA+BxB =
         * AxB+BxC-AxC
         */

            x2 -= x1 // A
            y2 -= y1
            x3 -= x1 // B
            y3 -= y1
            x4 -= x1 // C
            y4 -= y1

            val AvB = x2 * y3 - x3 * y2
            val AvC = x2 * y4 - x4 * y2

            // Online
            if (AvB == 0.0 && AvC == 0.0) {
                if (x2 != 0.0) {
                    return x4 * x3 <= 0.0 || x3 * x2 >= 0.0 && if (x2 > 0.0) x3 <= x2 || x4 <= x2 else x3 >= x2 || x4 >= x2
                }
                if (y2 != 0.0) {
                    return y4 * y3 <= 0.0 || y3 * y2 >= 0.0 && if (y2 > 0.0) y3 <= y2 || y4 <= y2 else y3 >= y2 || y4 >= y2
                }
                return false
            }

            val BvC = x3 * y4 - x4 * y3

            return AvB * AvC <= 0.0 && BvC * (AvB + BvC - AvC) <= 0.0
        }

        fun ptSegDistSq(x1: Double, y1: Double, x2: Double, y2: Double, px: Double, py: Double): Double {
            var x2 = x2
            var y2 = y2
            var px = px
            var py = py
            /*
         * A = (x2 - x1, y2 - y1) P = (px - x1, py - y1)
         */
            x2 -= x1 // A = (x2, y2)
            y2 -= y1
            px -= x1 // P = (px, py)
            py -= y1
            var dist: Double
            if (px * x2 + py * y2 <= 0.0) {
                // P*A
                dist = px * px + py * py
            } else {
                px = x2 - px // P = A - P = (x2 - px, y2 - py)
                py = y2 - py
                if (px * x2 + py * y2 <= 0.0) {
                    // P*A
                    dist = px * px + py * py
                } else {
                    dist = px * y2 - py * x2
                    dist = dist * dist / (x2 * x2 + y2 * y2) // pxA/|A|
                }
            }
            if (dist < 0) {
                dist = 0.0
            }
            return dist
        }

        fun ptSegDist(x1: Double, y1: Double, x2: Double, y2: Double, px: Double, py: Double): Double {
            return Math.sqrt(ptSegDistSq(x1, y1, x2, y2, px, py))
        }

        fun ptLineDistSq(x1: Double, y1: Double, x2: Double, y2: Double, px: Double, py: Double): Double {
            var x2 = x2
            var y2 = y2
            var px = px
            var py = py
            x2 -= x1
            y2 -= y1
            px -= x1
            py -= y1
            val s = px * y2 - py * x2
            return s * s / (x2 * x2 + y2 * y2)
        }

        fun ptLineDist(x1: Double, y1: Double, x2: Double, y2: Double, px: Double, py: Double): Double {
            return Math.sqrt(ptLineDistSq(x1, y1, x2, y2, px, py))
        }
    }

}