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

import com.itextpdf.awt.geom.misc.HashCode
import com.itextpdf.awt.geom.misc.Messages

abstract class Rectangle2D protected constructor() : RectangularShape() {

    class Float : Rectangle2D {

        var x: Float = 0.toFloat()
        var y: Float = 0.toFloat()
        var width: Float = 0.toFloat()
        var height: Float = 0.toFloat()

        constructor() {
        }

        constructor(x: Float, y: Float, width: Float, height: Float) {
            setRect(x, y, width, height)
        }

        override fun getX(): Double {
            return x.toDouble()
        }

        override fun getY(): Double {
            return y.toDouble()
        }

        override fun getWidth(): Double {
            return width.toDouble()
        }

        override fun getHeight(): Double {
            return height.toDouble()
        }

        override val isEmpty: Boolean
            get() = width <= 0.0f || height <= 0.0f

        fun setRect(x: Float, y: Float, width: Float, height: Float) {
            this.x = x
            this.y = y
            this.width = width
            this.height = height
        }

        override fun setRect(x: Double, y: Double, width: Double, height: Double) {
            this.x = x.toFloat()
            this.y = y.toFloat()
            this.width = width.toFloat()
            this.height = height.toFloat()
        }

        override fun setRect(r: Rectangle2D) {
            this.x = r.x.toFloat()
            this.y = r.y.toFloat()
            this.width = r.width.toFloat()
            this.height = r.height.toFloat()
        }

        override fun outcode(px: Double, py: Double): Int {
            var code = 0

            if (width <= 0.0f) {
                code = code or (OUT_LEFT or OUT_RIGHT)
            } else if (px < x) {
                code = code or OUT_LEFT
            } else if (px > x + width) {
                code = code or OUT_RIGHT
            }

            if (height <= 0.0f) {
                code = code or (OUT_TOP or OUT_BOTTOM)
            } else if (py < y) {
                code = code or OUT_TOP
            } else if (py > y + height) {
                code = code or OUT_BOTTOM
            }

            return code
        }

        override val bounds2D: Rectangle2D
            get() = Float(x, y, width, height)

        override fun createIntersection(r: Rectangle2D): Rectangle2D {
            val dst: Rectangle2D
            if (r is Double) {
                dst = Rectangle2D.Double()
            } else {
                dst = Rectangle2D.Float()
            }
            Rectangle2D.intersect(this, r, dst)
            return dst
        }

        override fun createUnion(r: Rectangle2D): Rectangle2D {
            val dst: Rectangle2D
            if (r is Double) {
                dst = Rectangle2D.Double()
            } else {
                dst = Rectangle2D.Float()
            }
            Rectangle2D.union(this, r, dst)
            return dst
        }

        override fun toString(): String {
            // The output format based on 1.5 release behaviour. It could be obtained in the following way
            // System.out.println(new Rectangle2D.Float().toString())
            return javaClass.name + "[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + "]" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }
    }

    class Double : Rectangle2D {

        override var x: Double = 0.toDouble()
        override var y: Double = 0.toDouble()
        override var width: Double = 0.toDouble()
        override var height: Double = 0.toDouble()

        constructor() {
        }

        constructor(x: Double, y: Double, width: Double, height: Double) {
            setRect(x, y, width, height)
        }

        override val isEmpty: Boolean
            get() = width <= 0.0 || height <= 0.0

        override fun setRect(x: Double, y: Double, width: Double, height: Double) {
            this.x = x
            this.y = y
            this.width = width
            this.height = height
        }

        override fun setRect(r: Rectangle2D) {
            this.x = r.x
            this.y = r.y
            this.width = r.width
            this.height = r.height
        }

        override fun outcode(px: Double, py: Double): Int {
            var code = 0

            if (width <= 0.0) {
                code = code or (OUT_LEFT or OUT_RIGHT)
            } else if (px < x) {
                code = code or OUT_LEFT
            } else if (px > x + width) {
                code = code or OUT_RIGHT
            }

            if (height <= 0.0) {
                code = code or (OUT_TOP or OUT_BOTTOM)
            } else if (py < y) {
                code = code or OUT_TOP
            } else if (py > y + height) {
                code = code or OUT_BOTTOM
            }

            return code
        }

        override val bounds2D: Rectangle2D
            get() = Double(x, y, width, height)

        override fun createIntersection(r: Rectangle2D): Rectangle2D {
            val dst = Rectangle2D.Double()
            Rectangle2D.intersect(this, r, dst)
            return dst
        }

        override fun createUnion(r: Rectangle2D): Rectangle2D {
            val dest = Rectangle2D.Double()
            Rectangle2D.union(this, r, dest)
            return dest
        }

        override fun toString(): String {
            // The output format based on 1.5 release behaviour. It could be obtained in the following way
            // System.out.println(new Rectangle2D.Double().toString())
            return javaClass.name + "[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + "]" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }
    }

    /*
     * Rectangle2D path iterator 
     */
    internal inner class Iterator
    /**
     * Constructs a new Rectangle2D.Iterator for given rectangle and transformation
     * @param r - the source Rectangle2D object
     * *
     * @param at - the AffineTransform object to apply rectangle path
     */
    (r: Rectangle2D,
     /**
      * The path iterator transformation
      */
     var t: AffineTransform?) : PathIterator {

        /**
         * The x coordinate of left-upper rectangle corner
         */
        var x: Double = 0.toDouble()

        /**
         * The y coordinate of left-upper rectangle corner
         */
        var y: Double = 0.toDouble()


        /**
         * The width of rectangle
         */
        var width: Double = 0.toDouble()

        /**
         * The height of rectangle
         */
        var height: Double = 0.toDouble()

        /**
         * The current segmenet index
         */
        var index: Int = 0

        init {
            this.x = r.x
            this.y = r.y
            this.width = r.width
            this.height = r.height
            if (width < 0.0 || height < 0.0) {
                index = 6
            }
        }

        override val windingRule: Int
            get() = PathIterator.WIND_NON_ZERO

        override val isDone: Boolean
            get() = index > 5

        override fun next() {
            index++
        }

        override fun currentSegment(coords: DoubleArray): Int {
            if (isDone) {
                throw NoSuchElementException(Messages.getString("awt.4B")) //$NON-NLS-1$
            }
            if (index == 5) {
                return PathIterator.SEG_CLOSE
            }
            val type: Int
            if (index == 0) {
                type = PathIterator.SEG_MOVETO
                coords[0] = x
                coords[1] = y
            } else {
                type = PathIterator.SEG_LINETO
                when (index) {
                    1 -> {
                        coords[0] = x + width
                        coords[1] = y
                    }
                    2 -> {
                        coords[0] = x + width
                        coords[1] = y + height
                    }
                    3 -> {
                        coords[0] = x
                        coords[1] = y + height
                    }
                    4 -> {
                        coords[0] = x
                        coords[1] = y
                    }
                }
            }
            if (t != null) {
                t!!.transform(coords, 0, coords, 0, 1)
            }
            return type
        }

        override fun currentSegment(coords: FloatArray): Int {
            if (isDone) {
                throw NoSuchElementException(Messages.getString("awt.4B")) //$NON-NLS-1$
            }
            if (index == 5) {
                return PathIterator.SEG_CLOSE
            }
            val type: Int
            if (index == 0) {
                coords[0] = x.toFloat()
                coords[1] = y.toFloat()
                type = PathIterator.SEG_MOVETO
            } else {
                type = PathIterator.SEG_LINETO
                when (index) {
                    1 -> {
                        coords[0] = (x + width).toFloat()
                        coords[1] = y.toFloat()
                    }
                    2 -> {
                        coords[0] = (x + width).toFloat()
                        coords[1] = (y + height).toFloat()
                    }
                    3 -> {
                        coords[0] = x.toFloat()
                        coords[1] = (y + height).toFloat()
                    }
                    4 -> {
                        coords[0] = x.toFloat()
                        coords[1] = y.toFloat()
                    }
                }
            }
            if (t != null) {
                t!!.transform(coords, 0, coords, 0, 1)
            }
            return type
        }

    }

    abstract fun setRect(x: Double, y: Double, width: Double, height: Double)

    abstract fun outcode(x: Double, y: Double): Int

    abstract fun createIntersection(r: Rectangle2D): Rectangle2D

    abstract fun createUnion(r: Rectangle2D): Rectangle2D

    open fun setRect(r: Rectangle2D) {
        setRect(r.x, r.y, r.width, r.height)
    }

    override fun setFrame(x: Double, y: Double, width: Double, height: Double) {
        setRect(x, y, width, height)
    }

    override val bounds2D: Rectangle2D
        get() = clone() as Rectangle2D

    fun intersectsLine(x1: Double, y1: Double, x2: Double, y2: Double): Boolean {
        val rx1 = x
        val ry1 = y
        val rx2 = rx1 + width
        val ry2 = ry1 + height
        return rx1 <= x1 && x1 <= rx2 && ry1 <= y1 && y1 <= ry2 ||
                rx1 <= x2 && x2 <= rx2 && ry1 <= y2 && y2 <= ry2 ||
                Line2D.linesIntersect(rx1, ry1, rx2, ry2, x1, y1, x2, y2) ||
                Line2D.linesIntersect(rx2, ry1, rx1, ry2, x1, y1, x2, y2)
    }

    fun intersectsLine(l: Line2D): Boolean {
        return intersectsLine(l.x1, l.y1, l.x2, l.y2)
    }

    fun outcode(p: Point2D): Int {
        return outcode(p.x, p.y)
    }

    override fun contains(x: Double, y: Double): Boolean {
        if (isEmpty) {
            return false
        }

        val x1 = x
        val y1 = y
        val x2 = x1 + width
        val y2 = y1 + height

        return x1 <= x && x < x2 &&
                y1 <= y && y < y2
    }

    override fun intersects(x: Double, y: Double, width: Double, height: Double): Boolean {
        if (isEmpty || width <= 0.0 || height <= 0.0) {
            return false
        }

        val x1 = x
        val y1 = y
        val x2 = x1 + width
        val y2 = y1 + height

        return x + width > x1 && x < x2 &&
                y + height > y1 && y < y2
    }

    override fun contains(x: Double, y: Double, width: Double, height: Double): Boolean {
        if (isEmpty || width <= 0.0 || height <= 0.0) {
            return false
        }

        val x1 = x
        val y1 = y
        val x2 = x1 + width
        val y2 = y1 + height

        return x1 <= x && x + width <= x2 &&
                y1 <= y && y + height <= y2
    }

    open fun add(x: Double, y: Double) {
        val x1 = Math.min(minX, x)
        val y1 = Math.min(minY, y)
        val x2 = Math.max(maxX, x)
        val y2 = Math.max(maxY, y)
        setRect(x1, y1, x2 - x1, y2 - y1)
    }

    fun add(p: Point2D) {
        add(p.x, p.y)
    }

    fun add(r: Rectangle2D) {
        union(this, r, this)
    }

    override fun getPathIterator(t: AffineTransform): PathIterator {
        return Iterator(this, t)
    }

    override fun getPathIterator(t: AffineTransform, flatness: Double): PathIterator {
        return Iterator(this, t)
    }

    override fun hashCode(): Int {
        val hash = HashCode()
        hash.append(x)
        hash.append(y)
        hash.append(width)
        hash.append(height)
        return hash.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is Rectangle2D) {
            return x == obj.x &&
                    y == obj.y &&
                    width == obj.width &&
                    height == obj.height
        }
        return false
    }

    companion object {

        val OUT_LEFT = 1
        val OUT_TOP = 2
        val OUT_RIGHT = 4
        val OUT_BOTTOM = 8

        fun intersect(src1: Rectangle2D, src2: Rectangle2D, dst: Rectangle2D) {
            val x1 = Math.max(src1.minX, src2.minX)
            val y1 = Math.max(src1.minY, src2.minY)
            val x2 = Math.min(src1.maxX, src2.maxX)
            val y2 = Math.min(src1.maxY, src2.maxY)
            dst.setFrame(x1, y1, x2 - x1, y2 - y1)
        }

        fun union(src1: Rectangle2D, src2: Rectangle2D, dst: Rectangle2D) {
            val x1 = Math.min(src1.minX, src2.minX)
            val y1 = Math.min(src1.minY, src2.minY)
            val x2 = Math.max(src1.maxX, src2.maxX)
            val y2 = Math.max(src1.maxY, src2.maxY)
            dst.setFrame(x1, y1, x2 - x1, y2 - y1)
        }
    }

}

