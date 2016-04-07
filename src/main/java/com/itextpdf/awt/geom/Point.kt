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

class Point : Point2D, Serializable {

    override var x: Double = 0.toDouble()
    override var y: Double = 0.toDouble()

    constructor() {
        setLocation(0, 0)
    }

    constructor(x: Int, y: Int) {
        setLocation(x, y)
    }

    constructor(x: Double, y: Double) {
        setLocation(x, y)
    }

    constructor(p: Point) {
        setLocation(p.x, p.y)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is Point) {
            return x == obj.x && y == obj.y
        }
        return false
    }

    override fun toString(): String {
        return javaClass.name + "[x=" + x + ",y=" + y + "]" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    var location: Point
        get() = Point(x, y)
        set(p) = setLocation(p.x, p.y)

    fun setLocation(x: Int, y: Int) {
        setLocation(x.toDouble(), y.toDouble())
    }

    override fun setLocation(x: Double, y: Double) {
        this.x = x
        this.y = y
    }

    fun move(x: Int, y: Int) {
        move(x.toDouble(), y.toDouble())
    }

    fun move(x: Double, y: Double) {
        setLocation(x, y)
    }

    fun translate(dx: Int, dy: Int) {
        translate(dx.toDouble(), dy.toDouble())
    }

    fun translate(dx: Double, dy: Double) {
        x += dx
        y += dy
    }

    companion object {

        private val serialVersionUID = -5276940640259749850L
    }

}

