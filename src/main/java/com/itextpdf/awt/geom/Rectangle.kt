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

import java.io.Serializable

class Rectangle : Rectangle2D, Shape, Serializable {

    override var x: Double = 0.toDouble()
    override var y: Double = 0.toDouble()
    override var width: Double = 0.toDouble()
    override var height: Double = 0.toDouble()

    constructor() {
        setBounds(0, 0, 0, 0)
    }

    constructor(p: Point) {
        setBounds(p.x, p.y, 0.0, 0.0)
    }

    constructor(p: Point, d: Dimension) {
        setBounds(p.x, p.y, d.width, d.height)
    }

    constructor(x: Double, y: Double, width: Double, height: Double) {
        setBounds(x, y, width, height)
    }

    constructor(width: Int, height: Int) {
        setBounds(0, 0, width, height)
    }

    constructor(r: Rectangle) {
        setBounds(r.x, r.y, r.width, r.height)
    }

    constructor(r: com.itextpdf.text.Rectangle) {
        r.normalize()
        setBounds(r.left.toDouble(), r.bottom.toDouble(), r.width.toDouble(), r.height.toDouble())
    }

    constructor(d: Dimension) {
        setBounds(0.0, 0.0, d.width, d.height)
    }

    override val isEmpty: Boolean
        get() = width <= 0 || height <= 0

    var size: Dimension
        get() = Dimension(width, height)
        set(d) {
            setSize(d.width, d.height)
        }

    fun setSize(mx: Int, my: Int) {
        setSize(mx.toDouble(), my.toDouble())
    }

    fun setSize(width: Double, height: Double) {
        this.width = width
        this.height = height
    }

    var location: Point
        get() = Point(x, y)
        set(p) {
            setLocation(p.x, p.y)
        }

    fun setLocation(mx: Int, my: Int) {
        setLocation(mx.toDouble(), my.toDouble())
    }

    fun setLocation(x: Double, y: Double) {
        this.x = x
        this.y = y
    }

    override fun setRect(x: Double, y: Double, width: Double, height: Double) {
        val x1 = Math.floor(x).toInt()
        val y1 = Math.floor(y).toInt()
        val x2 = Math.ceil(x + width).toInt()
        val y2 = Math.ceil(y + height).toInt()
        setBounds(x1, y1, x2 - x1, y2 - y1)
    }

    override var bounds: Rectangle
        get() = Rectangle(x, y, width, height)
        set(r) = setBounds(r.x, r.y, r.width, r.height)

    override val bounds2D: Rectangle2D
        get() = bounds

    fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        setBounds(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
    }

    fun setBounds(x: Double, y: Double, width: Double, height: Double) {
        this.x = x
        this.y = y
        this.height = height
        this.width = width
    }

    fun grow(mx: Int, my: Int) {
        translate(mx.toDouble(), my.toDouble())
    }

    fun grow(dx: Double, dy: Double) {
        x -= dx
        y -= dy
        width += dx + dx
        height += dy + dy
    }

    fun translate(mx: Int, my: Int) {
        translate(mx.toDouble(), my.toDouble())
    }

    fun translate(mx: Double, my: Double) {
        x += mx
        y += my
    }

    fun add(px: Int, py: Int) {
        add(px.toDouble(), py.toDouble())
    }

    override fun add(px: Double, py: Double) {
        val x1 = Math.min(x, px)
        val x2 = Math.max(x + width, px)
        val y1 = Math.min(y, py)
        val y2 = Math.max(y + height, py)
        setBounds(x1, y1, x2 - x1, y2 - y1)
    }

    fun add(p: Point) {
        add(p.x, p.y)
    }

    fun add(r: Rectangle) {
        val x1 = Math.min(x, r.x)
        val x2 = Math.max(x + width, r.x + r.width)
        val y1 = Math.min(y, r.y)
        val y2 = Math.max(y + height, r.y + r.height)
        setBounds(x1, y1, x2 - x1, y2 - y1)
    }

    fun contains(px: Int, py: Int): Boolean {
        return contains(px.toDouble(), py.toDouble())
    }

    override fun contains(px: Double, py: Double): Boolean {
        var px = px
        var py = py
        if (isEmpty) {
            return false
        }
        if (px < x || py < y) {
            return false
        }
        px -= x
        py -= y
        return px < width && py < height
    }

    operator fun contains(p: Point): Boolean {
        return contains(p.x, p.y)
    }

    fun contains(rx: Int, ry: Int, rw: Int, rh: Int): Boolean {
        return contains(rx, ry) && contains(rx + rw - 1, ry + rh - 1)
    }

    override fun contains(rx: Double, ry: Double, rw: Double, rh: Double): Boolean {
        return contains(rx, ry) && contains(rx + rw - 0.01, ry + rh - 0.01)
    }

    operator fun contains(r: Rectangle): Boolean {
        return contains(r.x, r.y, r.width, r.height)
    }

    override fun createIntersection(r: Rectangle2D): Rectangle2D {
        if (r is Rectangle) {
            return intersection(r)
        }
        val dst = Rectangle2D.Double()
        Rectangle2D.intersect(this, r, dst)
        return dst
    }

    fun intersection(r: Rectangle): Rectangle {
        val x1 = Math.max(x, r.x)
        val y1 = Math.max(y, r.y)
        val x2 = Math.min(x + width, r.x + r.width)
        val y2 = Math.min(y + height, r.y + r.height)
        return Rectangle(x1, y1, x2 - x1, y2 - y1)
    }

    fun intersects(r: Rectangle): Boolean {
        return !intersection(r).isEmpty
    }

    override fun outcode(px: Double, py: Double): Int {
        var code = 0

        if (width <= 0) {
            code = code or (Rectangle2D.OUT_LEFT or Rectangle2D.OUT_RIGHT)
        } else if (px < x) {
            code = code or Rectangle2D.OUT_LEFT
        } else if (px > x + width) {
            code = code or Rectangle2D.OUT_RIGHT
        }

        if (height <= 0) {
            code = code or (Rectangle2D.OUT_TOP or Rectangle2D.OUT_BOTTOM)
        } else if (py < y) {
            code = code or Rectangle2D.OUT_TOP
        } else if (py > y + height) {
            code = code or Rectangle2D.OUT_BOTTOM
        }

        return code
    }

    override fun createUnion(r: Rectangle2D): Rectangle2D {
        if (r is Rectangle) {
            return union(r)
        }
        val dst = Rectangle2D.Double()
        Rectangle2D.union(this, r, dst)
        return dst
    }

    fun union(r: Rectangle): Rectangle {
        val dst = Rectangle(this)
        dst.add(r)
        return dst
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is Rectangle) {
            return obj.x == x && obj.y == y && obj.width == width && obj.height == height
        }
        return false
    }

    override fun toString(): String {
        // The output format based on 1.5 release behaviour. It could be obtained in the following way
        // System.out.println(new Rectangle().toString())
        return javaClass.name + "[x=" + x + ",y=" + y + //$NON-NLS-1$ //$NON-NLS-2$

                ",width=" + width + ",height=" + height + "]" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    companion object {

        private val serialVersionUID = -4345857070255674764L
    }

}

