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

interface PathIterator {

    val windingRule: Int

    val isDone: Boolean

    operator fun next()

    fun currentSegment(coords: FloatArray): Int

    fun currentSegment(coords: DoubleArray): Int

    companion object {

        val WIND_EVEN_ODD = 0
        val WIND_NON_ZERO = 1

        val SEG_MOVETO = 0
        val SEG_LINETO = 1
        val SEG_QUADTO = 2
        val SEG_CUBICTO = 3
        val SEG_CLOSE = 4
    }

}

