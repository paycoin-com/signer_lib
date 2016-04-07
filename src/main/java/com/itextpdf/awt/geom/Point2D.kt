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

import com.itextpdf.awt.geom.misc.HashCode

abstract class Point2D protected constructor() : Cloneable {

    class Float : Point2D {

        var x: Float = 0.toFloat()
        var y: Float = 0.toFloat()

        constructor() {
        }

        constructor(x: Float, y: Float) {
            this.x = x
            this.y = y
        }

        override fun getX(): Double {
            return x.toDouble()
        }

        override fun getY(): Double {
            return y.toDouble()
        }

        fun setLocation(x: Float, y: Float) {
            this.x = x
            this.y = y
        }

        override fun setLocation(x: Double, y: Double) {
            this.x = x.toFloat()
            this.y = y.toFloat()
        }

        override fun toString(): String {
            return javaClass.name + "[x=" + x + ",y=" + y + "]" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    class Double : Point2D {

        override var x: Double = 0.toDouble()
        override var y: Double = 0.toDouble()

        constructor() {
        }

        constructor(x: Double, y: Double) {
            this.x = x
            this.y = y
        }

        override fun setLocation(x: Double, y: Double) {
            this.x = x
            this.y = y
        }

        override fun toString(): String {
            return javaClass.name + "[x=" + x + ",y=" + y + "]" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    abstract val x: Double

    abstract val y: Double

    abstract fun setLocation(x: Double, y: Double)

    fun setLocation(p: Point2D) {
        setLocation(p.x, p.y)
    }

    fun distanceSq(px: Double, py: Double): Double {
        return Point2D.distanceSq(x, y, px, py)
    }

    fun distanceSq(p: Point2D): Double {
        return Point2D.distanceSq(x, y, p.x, p.y)
    }

    fun distance(px: Double, py: Double): Double {
        return Math.sqrt(distanceSq(px, py))
    }

    fun distance(p: Point2D): Double {
        return Math.sqrt(distanceSq(p))
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
        hash.append(x)
        hash.append(y)
        return hash.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is Point2D) {
            return x == obj.x && y == obj.y
        }
        return false
    }

    companion object {

        fun distanceSq(x1: Double, y1: Double, x2: Double, y2: Double): Double {
            var x2 = x2
            var y2 = y2
            x2 -= x1
            y2 -= y1
            return x2 * x2 + y2 * y2
        }

        fun distance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
            return Math.sqrt(distanceSq(x1, y1, x2, y2))
        }
    }
}

