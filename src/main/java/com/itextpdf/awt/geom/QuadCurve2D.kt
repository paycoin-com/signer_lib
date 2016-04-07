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

abstract class QuadCurve2D protected constructor() : Shape, Cloneable {

    class Float : QuadCurve2D {

        var x1: Float = 0.toFloat()
        var y1: Float = 0.toFloat()
        var ctrlx: Float = 0.toFloat()
        var ctrly: Float = 0.toFloat()
        var x2: Float = 0.toFloat()
        var y2: Float = 0.toFloat()

        constructor() {
        }

        constructor(x1: Float, y1: Float, ctrlx: Float, ctrly: Float, x2: Float, y2: Float) {
            setCurve(x1, y1, ctrlx, ctrly, x2, y2)
        }

        override fun getX1(): Double {
            return x1.toDouble()
        }

        override fun getY1(): Double {
            return y1.toDouble()
        }

        override val ctrlX: Double
            get() = ctrlx.toDouble()

        override val ctrlY: Double
            get() = ctrly.toDouble()

        override fun getX2(): Double {
            return x2.toDouble()
        }

        override fun getY2(): Double {
            return y2.toDouble()
        }

        override val p1: Point2D
            get() = Point2D.Float(x1, y1)

        override val ctrlPt: Point2D
            get() = Point2D.Float(ctrlx, ctrly)

        override val p2: Point2D
            get() = Point2D.Float(x2, y2)

        override fun setCurve(x1: Double, y1: Double, ctrlx: Double, ctrly: Double, x2: Double, y2: Double) {
            this.x1 = x1.toFloat()
            this.y1 = y1.toFloat()
            this.ctrlx = ctrlx.toFloat()
            this.ctrly = ctrly.toFloat()
            this.x2 = x2.toFloat()
            this.y2 = y2.toFloat()
        }

        fun setCurve(x1: Float, y1: Float, ctrlx: Float, ctrly: Float, x2: Float, y2: Float) {
            this.x1 = x1
            this.y1 = y1
            this.ctrlx = ctrlx
            this.ctrly = ctrly
            this.x2 = x2
            this.y2 = y2
        }

        override val bounds2D: Rectangle2D
            get() {
                val rx0 = Math.min(Math.min(x1, x2), ctrlx)
                val ry0 = Math.min(Math.min(y1, y2), ctrly)
                val rx1 = Math.max(Math.max(x1, x2), ctrlx)
                val ry1 = Math.max(Math.max(y1, y2), ctrly)
                return Rectangle2D.Float(rx0, ry0, rx1 - rx0, ry1 - ry0)
            }
    }

    class Double : QuadCurve2D {

        override var x1: Double = 0.toDouble()
        override var y1: Double = 0.toDouble()
        override var ctrlX: Double = 0.toDouble()
        override var ctrlY: Double = 0.toDouble()
        override var x2: Double = 0.toDouble()
        override var y2: Double = 0.toDouble()

        constructor() {
        }

        constructor(x1: Double, y1: Double, ctrlx: Double, ctrly: Double, x2: Double, y2: Double) {
            setCurve(x1, y1, ctrlx, ctrly, x2, y2)
        }

        override val p1: Point2D
            get() = Point2D.Double(x1, y1)

        override val ctrlPt: Point2D
            get() = Point2D.Double(ctrlX, ctrlY)

        override val p2: Point2D
            get() = Point2D.Double(x2, y2)

        override fun setCurve(x1: Double, y1: Double, ctrlx: Double, ctrly: Double, x2: Double, y2: Double) {
            this.x1 = x1
            this.y1 = y1
            this.ctrlX = ctrlx
            this.ctrlY = ctrly
            this.x2 = x2
            this.y2 = y2
        }

        override val bounds2D: Rectangle2D
            get() {
                val rx0 = Math.min(Math.min(x1, x2), ctrlX)
                val ry0 = Math.min(Math.min(y1, y2), ctrlY)
                val rx1 = Math.max(Math.max(x1, x2), ctrlX)
                val ry1 = Math.max(Math.max(y1, y2), ctrlY)
                return Rectangle2D.Double(rx0, ry0, rx1 - rx0, ry1 - ry0)
            }
    }

    /*
     * QuadCurve2D path iterator 
     */
    internal inner class Iterator
    /**
     * Constructs a new QuadCurve2D.Iterator for given line and transformation
     * @param q - the source QuadCurve2D object
     * *
     * @param at - the AffineTransform object to apply rectangle path
     */
    (
            /**
             * The source QuadCurve2D object
             */
            var c: QuadCurve2D,
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
                // awt.4B=Iterator out of bounds
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
                type = PathIterator.SEG_QUADTO
                coords[0] = c.ctrlX
                coords[1] = c.ctrlY
                coords[2] = c.x2
                coords[3] = c.y2
                count = 2
            }
            if (t != null) {
                t!!.transform(coords, 0, coords, 0, count)
            }
            return type
        }

        override fun currentSegment(coords: FloatArray): Int {
            if (isDone) {
                // awt.4B=Iterator out of bounds
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
                type = PathIterator.SEG_QUADTO
                coords[0] = c.ctrlX.toFloat()
                coords[1] = c.ctrlY.toFloat()
                coords[2] = c.x2.toFloat()
                coords[3] = c.y2.toFloat()
                count = 2
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

    abstract val ctrlX: Double

    abstract val ctrlY: Double

    abstract val ctrlPt: Point2D

    abstract val x2: Double

    abstract val y2: Double

    abstract val p2: Point2D

    abstract fun setCurve(x1: Double, y1: Double, ctrlx: Double, ctrly: Double, x2: Double, y2: Double)

    fun setCurve(p1: Point2D, cp: Point2D, p2: Point2D) {
        setCurve(p1.x, p1.y, cp.x, cp.y, p2.x, p2.y)
    }

    fun setCurve(coords: DoubleArray, offset: Int) {
        setCurve(
                coords[offset + 0], coords[offset + 1],
                coords[offset + 2], coords[offset + 3],
                coords[offset + 4], coords[offset + 5])
    }

    fun setCurve(points: Array<Point2D>, offset: Int) {
        setCurve(
                points[offset + 0].x, points[offset + 0].y,
                points[offset + 1].x, points[offset + 1].y,
                points[offset + 2].x, points[offset + 2].y)
    }

    fun setCurve(curve: QuadCurve2D) {
        setCurve(
                curve.x1, curve.y1,
                curve.ctrlX, curve.ctrlY,
                curve.x2, curve.y2)
    }

    val flatnessSq: Double
        get() = Line2D.ptSegDistSq(
                x1, y1,
                x2, y2,
                ctrlX, ctrlY)

    val flatness: Double
        get() = Line2D.ptSegDist(x1, y1, x2, y2, ctrlX, ctrlY)

    fun subdivide(left: QuadCurve2D, right: QuadCurve2D) {
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

    override fun getPathIterator(t: AffineTransform, flatness: Double): PathIterator {
        return FlatteningPathIterator(getPathIterator(t), flatness)
    }

    public override fun clone(): Any {
        try {
            return super.clone()
        } catch (e: CloneNotSupportedException) {
            throw InternalError()
        }

    }

    companion object {

        fun getFlatnessSq(x1: Double, y1: Double, ctrlx: Double, ctrly: Double, x2: Double, y2: Double): Double {
            return Line2D.ptSegDistSq(x1, y1, x2, y2, ctrlx, ctrly)
        }

        fun getFlatnessSq(coords: DoubleArray, offset: Int): Double {
            return Line2D.ptSegDistSq(
                    coords[offset + 0], coords[offset + 1],
                    coords[offset + 4], coords[offset + 5],
                    coords[offset + 2], coords[offset + 3])
        }

        fun getFlatness(x1: Double, y1: Double, ctrlx: Double,
                        ctrly: Double, x2: Double, y2: Double): Double {
            return Line2D.ptSegDist(x1, y1, x2, y2, ctrlx, ctrly)
        }

        fun getFlatness(coords: DoubleArray, offset: Int): Double {
            return Line2D.ptSegDist(
                    coords[offset + 0], coords[offset + 1],
                    coords[offset + 4], coords[offset + 5],
                    coords[offset + 2], coords[offset + 3])
        }

        fun subdivide(src: QuadCurve2D, left: QuadCurve2D?, right: QuadCurve2D?) {
            val x1 = src.x1
            val y1 = src.y1
            var cx = src.ctrlX
            var cy = src.ctrlY
            val x2 = src.x2
            val y2 = src.y2
            val cx1 = (x1 + cx) / 2.0
            val cy1 = (y1 + cy) / 2.0
            val cx2 = (x2 + cx) / 2.0
            val cy2 = (y2 + cy) / 2.0
            cx = (cx1 + cx2) / 2.0
            cy = (cy1 + cy2) / 2.0
            left?.setCurve(x1, y1, cx1, cy1, cx, cy)
            right?.setCurve(cx, cy, cx2, cy2, x2, y2)
        }

        fun subdivide(src: DoubleArray, srcoff: Int, left: DoubleArray?,
                      leftOff: Int, right: DoubleArray?, rightOff: Int) {
            val x1 = src[srcoff + 0]
            val y1 = src[srcoff + 1]
            var cx = src[srcoff + 2]
            var cy = src[srcoff + 3]
            val x2 = src[srcoff + 4]
            val y2 = src[srcoff + 5]
            val cx1 = (x1 + cx) / 2.0
            val cy1 = (y1 + cy) / 2.0
            val cx2 = (x2 + cx) / 2.0
            val cy2 = (y2 + cy) / 2.0
            cx = (cx1 + cx2) / 2.0
            cy = (cy1 + cy2) / 2.0
            if (left != null) {
                left[leftOff + 0] = x1
                left[leftOff + 1] = y1
                left[leftOff + 2] = cx1
                left[leftOff + 3] = cy1
                left[leftOff + 4] = cx
                left[leftOff + 5] = cy
            }
            if (right != null) {
                right[rightOff + 0] = cx
                right[rightOff + 1] = cy
                right[rightOff + 2] = cx2
                right[rightOff + 3] = cy2
                right[rightOff + 4] = x2
                right[rightOff + 5] = y2
            }
        }

        @JvmOverloads fun solveQuadratic(eqn: DoubleArray, res: DoubleArray = eqn): Int {
            return Crossing.solveQuad(eqn, res)
        }
    }

}

