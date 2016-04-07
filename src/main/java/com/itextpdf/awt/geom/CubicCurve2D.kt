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

abstract class CubicCurve2D protected constructor() : Shape, Cloneable {

    class Float : CubicCurve2D {

        var x1: Float = 0.toFloat()
        var y1: Float = 0.toFloat()
        var ctrlx1: Float = 0.toFloat()
        var ctrly1: Float = 0.toFloat()
        var ctrlx2: Float = 0.toFloat()
        var ctrly2: Float = 0.toFloat()
        var x2: Float = 0.toFloat()
        var y2: Float = 0.toFloat()

        constructor() {
        }

        constructor(x1: Float, y1: Float, ctrlx1: Float, ctrly1: Float, ctrlx2: Float, ctrly2: Float, x2: Float, y2: Float) {
            setCurve(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2)
        }

        override fun getX1(): Double {
            return x1.toDouble()
        }

        override fun getY1(): Double {
            return y1.toDouble()
        }

        override val ctrlX1: Double
            get() = ctrlx1.toDouble()

        override val ctrlY1: Double
            get() = ctrly1.toDouble()

        override val ctrlX2: Double
            get() = ctrlx2.toDouble()

        override val ctrlY2: Double
            get() = ctrly2.toDouble()

        override fun getX2(): Double {
            return x2.toDouble()
        }

        override fun getY2(): Double {
            return y2.toDouble()
        }

        override val p1: Point2D
            get() = Point2D.Float(x1, y1)

        override val ctrlP1: Point2D
            get() = Point2D.Float(ctrlx1, ctrly1)

        override val ctrlP2: Point2D
            get() = Point2D.Float(ctrlx2, ctrly2)

        override val p2: Point2D
            get() = Point2D.Float(x2, y2)

        override fun setCurve(x1: Double, y1: Double, ctrlx1: Double, ctrly1: Double,
                              ctrlx2: Double, ctrly2: Double, x2: Double, y2: Double) {
            this.x1 = x1.toFloat()
            this.y1 = y1.toFloat()
            this.ctrlx1 = ctrlx1.toFloat()
            this.ctrly1 = ctrly1.toFloat()
            this.ctrlx2 = ctrlx2.toFloat()
            this.ctrly2 = ctrly2.toFloat()
            this.x2 = x2.toFloat()
            this.y2 = y2.toFloat()
        }

        fun setCurve(x1: Float, y1: Float, ctrlx1: Float, ctrly1: Float,
                     ctrlx2: Float, ctrly2: Float, x2: Float, y2: Float) {
            this.x1 = x1
            this.y1 = y1
            this.ctrlx1 = ctrlx1
            this.ctrly1 = ctrly1
            this.ctrlx2 = ctrlx2
            this.ctrly2 = ctrly2
            this.x2 = x2
            this.y2 = y2
        }

        override val bounds2D: Rectangle2D
            get() {
                val rx1 = Math.min(Math.min(x1, x2), Math.min(ctrlx1, ctrlx2))
                val ry1 = Math.min(Math.min(y1, y2), Math.min(ctrly1, ctrly2))
                val rx2 = Math.max(Math.max(x1, x2), Math.max(ctrlx1, ctrlx2))
                val ry2 = Math.max(Math.max(y1, y2), Math.max(ctrly1, ctrly2))
                return Rectangle2D.Float(rx1, ry1, rx2 - rx1, ry2 - ry1)
            }
    }

    class Double : CubicCurve2D {

        override var x1: Double = 0.toDouble()
        override var y1: Double = 0.toDouble()
        override var ctrlX1: Double = 0.toDouble()
        override var ctrlY1: Double = 0.toDouble()
        override var ctrlX2: Double = 0.toDouble()
        override var ctrlY2: Double = 0.toDouble()
        override var x2: Double = 0.toDouble()
        override var y2: Double = 0.toDouble()

        constructor() {
        }

        constructor(x1: Double, y1: Double, ctrlx1: Double, ctrly1: Double,
                    ctrlx2: Double, ctrly2: Double, x2: Double, y2: Double) {
            setCurve(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2)
        }

        override val p1: Point2D
            get() = Point2D.Double(x1, y1)

        override val ctrlP1: Point2D
            get() = Point2D.Double(ctrlX1, ctrlY1)

        override val ctrlP2: Point2D
            get() = Point2D.Double(ctrlX2, ctrlY2)

        override val p2: Point2D
            get() = Point2D.Double(x2, y2)

        override fun setCurve(x1: Double, y1: Double, ctrlx1: Double, ctrly1: Double,
                              ctrlx2: Double, ctrly2: Double, x2: Double, y2: Double) {
            this.x1 = x1
            this.y1 = y1
            this.ctrlX1 = ctrlx1
            this.ctrlY1 = ctrly1
            this.ctrlX2 = ctrlx2
            this.ctrlY2 = ctrly2
            this.x2 = x2
            this.y2 = y2
        }

        override val bounds2D: Rectangle2D
            get() {
                val rx1 = Math.min(Math.min(x1, x2), Math.min(ctrlX1, ctrlX2))
                val ry1 = Math.min(Math.min(y1, y2), Math.min(ctrlY1, ctrlY2))
                val rx2 = Math.max(Math.max(x1, x2), Math.max(ctrlX1, ctrlX2))
                val ry2 = Math.max(Math.max(y1, y2), Math.max(ctrlY1, ctrlY2))
                return Rectangle2D.Double(rx1, ry1, rx2 - rx1, ry2 - ry1)
            }
    }

    /*
     * CubicCurve2D path iterator 
     */
    internal inner class Iterator
    /**
     * Constructs a new CubicCurve2D.Iterator for given line and transformation
     * @param c - the source CubicCurve2D object
     * *
     * @param at - the AffineTransform object to apply rectangle path
     */
    (
            /**
             * The source CubicCurve2D object
             */
            var c: CubicCurve2D,
            /**
             * The path iterator transformation
             */
            var t: AffineTransform?) : PathIterator {

        /**
         * The current segmenet index
         */
        var index: Int = 0

        override val windingRule: Int
            get() = PathIterator.WIND_NON_ZERO

        override val isDone: Boolean
            get() = index > 1

        override fun next() {
            index++
        }

        override fun currentSegment(coords: DoubleArray): Int {
            if (isDone) {
                throw NoSuchElementException(Messages.getString("awt.4B")) //$NON-NLS-1$
            }
            val type: Int
            val count: Int
            if (index == 0) {
                type = PathIterator.SEG_MOVETO
                coords[0] = c.x1
                coords[1] = c.y1
                count = 1
            } else {
                type = PathIterator.SEG_CUBICTO
                coords[0] = c.ctrlX1
                coords[1] = c.ctrlY1
                coords[2] = c.ctrlX2
                coords[3] = c.ctrlY2
                coords[4] = c.x2
                coords[5] = c.y2
                count = 3
            }
            if (t != null) {
                t!!.transform(coords, 0, coords, 0, count)
            }
            return type
        }

        override fun currentSegment(coords: FloatArray): Int {
            if (isDone) {
                throw NoSuchElementException(Messages.getString("awt.4B")) //$NON-NLS-1$
            }
            val type: Int
            val count: Int
            if (index == 0) {
                type = PathIterator.SEG_MOVETO
                coords[0] = c.x1.toFloat()
                coords[1] = c.y1.toFloat()
                count = 1
            } else {
                type = PathIterator.SEG_CUBICTO
                coords[0] = c.ctrlX1.toFloat()
                coords[1] = c.ctrlY1.toFloat()
                coords[2] = c.ctrlX2.toFloat()
                coords[3] = c.ctrlY2.toFloat()
                coords[4] = c.x2.toFloat()
                coords[5] = c.y2.toFloat()
                count = 3
            }
            if (t != null) {
                t!!.transform(coords, 0, coords, 0, count)
            }
            return type
        }

    }

    abstract val x1: Double

    abstract val y1: Double

    abstract val p1: Point2D

    abstract val ctrlX1: Double

    abstract val ctrlY1: Double

    abstract val ctrlP1: Point2D

    abstract val ctrlX2: Double

    abstract val ctrlY2: Double

    abstract val ctrlP2: Point2D

    abstract val x2: Double

    abstract val y2: Double

    abstract val p2: Point2D

    abstract fun setCurve(x1: Double, y1: Double, ctrlx1: Double, ctrly1: Double,
                          ctrlx2: Double, ctrly2: Double, x2: Double, y2: Double)

    fun setCurve(p1: Point2D, cp1: Point2D, cp2: Point2D, p2: Point2D) {
        setCurve(
                p1.x, p1.y,
                cp1.x, cp1.y,
                cp2.x, cp2.y,
                p2.x, p2.y)
    }

    fun setCurve(coords: DoubleArray, offset: Int) {
        setCurve(
                coords[offset + 0], coords[offset + 1],
                coords[offset + 2], coords[offset + 3],
                coords[offset + 4], coords[offset + 5],
                coords[offset + 6], coords[offset + 7])
    }

    fun setCurve(points: Array<Point2D>, offset: Int) {
        setCurve(
                points[offset + 0].x, points[offset + 0].y,
                points[offset + 1].x, points[offset + 1].y,
                points[offset + 2].x, points[offset + 2].y,
                points[offset + 3].x, points[offset + 3].y)
    }

    fun setCurve(curve: CubicCurve2D) {
        setCurve(
                curve.x1, curve.y1,
                curve.ctrlX1, curve.ctrlY1,
                curve.ctrlX2, curve.ctrlY2,
                curve.x2, curve.y2)
    }

    val flatnessSq: Double
        get() = getFlatnessSq(
                x1, y1,
                ctrlX1, ctrlY1,
                ctrlX2, ctrlY2,
                x2, y2)

    val flatness: Double
        get() = getFlatness(
                x1, y1,
                ctrlX1, ctrlY1,
                ctrlX2, ctrlY2,
                x2, y2)

    fun subdivide(left: CubicCurve2D, right: CubicCurve2D) {
        subdivide(this, left, right)
    }

    override fun contains(px: Double, py: Double): Boolean {
        return Crossing.isInsideEvenOdd(Crossing.crossShape(this, px, py))
    }

    override fun contains(rx: Double, ry: Double, rw: Double, rh: Double): Boolean {
        val cross = Crossing.intersectShape(this, rx, ry, rw, rh)
        return cross != Crossing.CROSSING && Crossing.isInsideEvenOdd(cross)
    }

    override fun intersects(rx: Double, ry: Double, rw: Double, rh: Double): Boolean {
        val cross = Crossing.intersectShape(this, rx, ry, rw, rh)
        return cross == Crossing.CROSSING || Crossing.isInsideEvenOdd(cross)
    }

    override fun contains(p: Point2D): Boolean {
        return contains(p.x, p.y)
    }

    override fun intersects(r: Rectangle2D): Boolean {
        return intersects(r.x, r.y, r.width, r.height)
    }

    override fun contains(r: Rectangle2D): Boolean {
        return contains(r.x, r.y, r.width, r.height)
    }

    override val bounds: Rectangle
        get() = bounds2D.bounds

    override fun getPathIterator(t: AffineTransform): PathIterator {
        return Iterator(this, t)
    }

    override fun getPathIterator(at: AffineTransform, flatness: Double): PathIterator {
        return FlatteningPathIterator(getPathIterator(at), flatness)
    }

    public override fun clone(): Any {
        try {
            return super.clone()
        } catch (e: CloneNotSupportedException) {
            throw InternalError()
        }

    }

    companion object {

        fun getFlatnessSq(x1: Double, y1: Double, ctrlx1: Double, ctrly1: Double,
                          ctrlx2: Double, ctrly2: Double, x2: Double, y2: Double): Double {
            return Math.max(
                    Line2D.ptSegDistSq(x1, y1, x2, y2, ctrlx1, ctrly1),
                    Line2D.ptSegDistSq(x1, y1, x2, y2, ctrlx2, ctrly2))
        }

        fun getFlatnessSq(coords: DoubleArray, offset: Int): Double {
            return getFlatnessSq(
                    coords[offset + 0], coords[offset + 1],
                    coords[offset + 2], coords[offset + 3],
                    coords[offset + 4], coords[offset + 5],
                    coords[offset + 6], coords[offset + 7])
        }

        fun getFlatness(x1: Double, y1: Double, ctrlx1: Double, ctrly1: Double,
                        ctrlx2: Double, ctrly2: Double, x2: Double, y2: Double): Double {
            return Math.sqrt(getFlatnessSq(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2))
        }

        fun getFlatness(coords: DoubleArray, offset: Int): Double {
            return getFlatness(
                    coords[offset + 0], coords[offset + 1],
                    coords[offset + 2], coords[offset + 3],
                    coords[offset + 4], coords[offset + 5],
                    coords[offset + 6], coords[offset + 7])
        }

        fun subdivide(src: CubicCurve2D, left: CubicCurve2D?, right: CubicCurve2D?) {
            val x1 = src.x1
            val y1 = src.y1
            var cx1 = src.ctrlX1
            var cy1 = src.ctrlY1
            var cx2 = src.ctrlX2
            var cy2 = src.ctrlY2
            val x2 = src.x2
            val y2 = src.y2
            var cx = (cx1 + cx2) / 2.0
            var cy = (cy1 + cy2) / 2.0
            cx1 = (x1 + cx1) / 2.0
            cy1 = (y1 + cy1) / 2.0
            cx2 = (x2 + cx2) / 2.0
            cy2 = (y2 + cy2) / 2.0
            val ax = (cx1 + cx) / 2.0
            val ay = (cy1 + cy) / 2.0
            val bx = (cx2 + cx) / 2.0
            val by = (cy2 + cy) / 2.0
            cx = (ax + bx) / 2.0
            cy = (ay + by) / 2.0
            left?.setCurve(x1, y1, cx1, cy1, ax, ay, cx, cy)
            right?.setCurve(cx, cy, bx, by, cx2, cy2, x2, y2)
        }

        fun subdivide(src: DoubleArray, srcOff: Int, left: DoubleArray?, leftOff: Int, right: DoubleArray?, rightOff: Int) {
            val x1 = src[srcOff + 0]
            val y1 = src[srcOff + 1]
            var cx1 = src[srcOff + 2]
            var cy1 = src[srcOff + 3]
            var cx2 = src[srcOff + 4]
            var cy2 = src[srcOff + 5]
            val x2 = src[srcOff + 6]
            val y2 = src[srcOff + 7]
            var cx = (cx1 + cx2) / 2.0
            var cy = (cy1 + cy2) / 2.0
            cx1 = (x1 + cx1) / 2.0
            cy1 = (y1 + cy1) / 2.0
            cx2 = (x2 + cx2) / 2.0
            cy2 = (y2 + cy2) / 2.0
            val ax = (cx1 + cx) / 2.0
            val ay = (cy1 + cy) / 2.0
            val bx = (cx2 + cx) / 2.0
            val by = (cy2 + cy) / 2.0
            cx = (ax + bx) / 2.0
            cy = (ay + by) / 2.0
            if (left != null) {
                left[leftOff + 0] = x1
                left[leftOff + 1] = y1
                left[leftOff + 2] = cx1
                left[leftOff + 3] = cy1
                left[leftOff + 4] = ax
                left[leftOff + 5] = ay
                left[leftOff + 6] = cx
                left[leftOff + 7] = cy
            }
            if (right != null) {
                right[rightOff + 0] = cx
                right[rightOff + 1] = cy
                right[rightOff + 2] = bx
                right[rightOff + 3] = by
                right[rightOff + 4] = cx2
                right[rightOff + 5] = cy2
                right[rightOff + 6] = x2
                right[rightOff + 7] = y2
            }
        }

        @JvmOverloads fun solveCubic(eqn: DoubleArray, res: DoubleArray = eqn): Int {
            return Crossing.solveCubic(eqn, res)
        }
    }
}