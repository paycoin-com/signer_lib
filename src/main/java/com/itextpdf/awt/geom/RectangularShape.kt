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

abstract class RectangularShape protected constructor() : Shape, Cloneable {

    abstract val x: Double

    abstract val y: Double

    abstract val width: Double

    abstract val height: Double

    abstract val isEmpty: Boolean

    abstract fun setFrame(x: Double, y: Double, w: Double, h: Double)

    val minX: Double
        get() = x

    val minY: Double
        get() = y

    val maxX: Double
        get() = x + width

    val maxY: Double
        get() = y + height

    val centerX: Double
        get() = x + width / 2.0

    val centerY: Double
        get() = y + height / 2.0

    var frame: Rectangle2D
        get() = Rectangle2D.Double(x, y, width, height)
        set(r) = setFrame(r.x, r.y, r.width, r.height)

    fun setFrame(loc: Point2D, size: Dimension2D) {
        setFrame(loc.x, loc.y, size.width, size.height)
    }

    fun setFrameFromDiagonal(x1: Double, y1: Double, x2: Double, y2: Double) {
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
        setFrame(rx, ry, rw, rh)
    }

    fun setFrameFromDiagonal(p1: Point2D, p2: Point2D) {
        setFrameFromDiagonal(p1.x, p1.y, p2.x, p2.y)
    }

    fun setFrameFromCenter(centerX: Double, centerY: Double, cornerX: Double, cornerY: Double) {
        val width = Math.abs(cornerX - centerX)
        val height = Math.abs(cornerY - centerY)
        setFrame(centerX - width, centerY - height, width * 2.0, height * 2.0)
    }

    fun setFrameFromCenter(center: Point2D, corner: Point2D) {
        setFrameFromCenter(center.x, center.y, corner.x, corner.y)
    }

    override fun contains(point: Point2D): Boolean {
        return contains(point.x, point.y)
    }

    override fun intersects(rect: Rectangle2D): Boolean {
        return intersects(rect.x, rect.y, rect.width, rect.height)
    }

    override fun contains(rect: Rectangle2D): Boolean {
        return contains(rect.x, rect.y, rect.width, rect.height)
    }

    override val bounds: Rectangle
        get() {
            val x1 = Math.floor(minX).toInt()
            val y1 = Math.floor(minY).toInt()
            val x2 = Math.ceil(maxX).toInt()
            val y2 = Math.ceil(maxY).toInt()
            return Rectangle(x1.toDouble(), y1.toDouble(), (x2 - x1).toDouble(), (y2 - y1).toDouble())
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

}

