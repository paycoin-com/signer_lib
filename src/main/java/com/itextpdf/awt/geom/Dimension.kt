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

import com.itextpdf.awt.geom.misc.HashCode


class Dimension : Dimension2D, Serializable {

    override var width: Double = 0.toDouble()
    override var height: Double = 0.toDouble()

    constructor(d: Dimension) : this(d.width, d.height) {
    }

    constructor(width: Double, height: Double) {
        setSize(width, height)
    }

    @JvmOverloads constructor(width: Int = 0, height: Int = 0) {
        setSize(width, height)
    }

    override fun hashCode(): Int {
        val hash = HashCode()
        hash.append(width)
        hash.append(height)
        return hash.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is Dimension) {
            return obj.width == width && obj.height == height
        }
        return false
    }

    override fun toString(): String {
        // The output format based on 1.5 release behaviour. It could be obtained in the following way
        // System.out.println(new Dimension().toString())
        return javaClass.name + "[width=" + width + ",height=" + height + "]" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    fun setSize(width: Int, height: Int) {
        this.width = width.toDouble()
        this.height = height.toDouble()
    }

    override fun setSize(width: Double, height: Double) {
        setSize(Math.ceil(width).toInt(), Math.ceil(height).toInt())
    }

    var size: Dimension
        get() = Dimension(width, height)
        set(d) = setSize(d.width, d.height)

    companion object {

        private val serialVersionUID = 4723952579491349524L
    }

}

