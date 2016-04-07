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

class FlatteningPathIterator @JvmOverloads constructor(
        /**
         * The source PathIterator
         */
        internal var p: PathIterator?, flatness: Double, limit: Int = FlatteningPathIterator.BUFFER_LIMIT) : PathIterator {

    /**
     * The type of current segment to be flat
     */
    internal var bufType: Int = 0

    /**
     * The curve subdivision limit
     */
    var recursionLimit: Int = 0
        internal set

    /**
     * The current points buffer size
     */
    internal var bufSize: Int = 0

    /**
     * The inner cursor position in points buffer
     */
    internal var bufIndex: Int = 0

    /**
     * The current subdivision count
     */
    internal var bufSubdiv: Int = 0

    /**
     * The points buffer
     */
    internal var buf: DoubleArray

    /**
     * The indicator of empty points buffer
     */
    internal var bufEmpty = true

    /**
     * The flatness of new path
     */
    var flatness: Double = 0.toDouble()
        internal set

    /**
     * The square of flatness
     */
    internal var flatness2: Double = 0.toDouble()

    /**
     * The x coordinate of previous path segment
     */
    internal var px: Double = 0.toDouble()

    /**
     * The y coordinate of previous path segment
     */
    internal var py: Double = 0.toDouble()

    /**
     * The tamporary buffer for getting points from PathIterator
     */
    internal var coords = DoubleArray(6)

    init {
        if (flatness < 0.0) {
            // awt.206=Flatness is less then zero
            throw IllegalArgumentException(Messages.getString("awt.206")) //$NON-NLS-1$
        }
        if (limit < 0) {
            // awt.207=Limit is less then zero
            throw IllegalArgumentException(Messages.getString("awt.207")) //$NON-NLS-1$
        }
        if (p == null) {
            // awt.208=Path is null
            throw NullPointerException(Messages.getString("awt.208")) //$NON-NLS-1$
        }
        this.flatness = flatness
        this.flatness2 = flatness * flatness
        this.recursionLimit = limit
        this.bufSize = Math.min(recursionLimit, BUFFER_SIZE)
        this.buf = DoubleArray(bufSize)
        this.bufIndex = bufSize
    }

    override val windingRule: Int
        get() = p.windingRule

    override val isDone: Boolean
        get() = bufEmpty && p.isDone

    /**
     * Calculates flat path points for current segment of the source shape.

     * Line segment is flat by itself. Flatness of quad and cubic curves evaluated by getFlatnessSq() method.
     * Curves subdivided until current flatness is bigger than user defined and subdivision limit isn't exhausted.
     * Single source segment translated to series of buffer points. The less flatness the bigger serries.
     * Every currentSegment() call extract one point from the buffer. When series completed evaluate() takes next source shape segment.
     */
    internal fun evaluate() {
        if (bufEmpty) {
            bufType = p.currentSegment(coords)
        }

        when (bufType) {
            PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> {
                px = coords[0]
                py = coords[1]
            }
            PathIterator.SEG_QUADTO -> {
                if (bufEmpty) {
                    bufIndex -= 6
                    buf[bufIndex + 0] = px
                    buf[bufIndex + 1] = py
                    System.arraycopy(coords, 0, buf, bufIndex + 2, 4)
                    bufSubdiv = 0
                }

                while (bufSubdiv < recursionLimit) {
                    if (QuadCurve2D.getFlatnessSq(buf, bufIndex) < flatness2) {
                        break
                    }

                    // Realloc buffer
                    if (bufIndex <= 4) {
                        val tmp = DoubleArray(bufSize + BUFFER_CAPACITY)
                        System.arraycopy(
                                buf, bufIndex,
                                tmp, bufIndex + BUFFER_CAPACITY,
                                bufSize - bufIndex)
                        buf = tmp
                        bufSize += BUFFER_CAPACITY
                        bufIndex += BUFFER_CAPACITY
                    }

                    QuadCurve2D.subdivide(buf, bufIndex, buf, bufIndex - 4, buf, bufIndex)

                    bufIndex -= 4
                    bufSubdiv++
                }

                bufIndex += 4
                px = buf[bufIndex]
                py = buf[bufIndex + 1]

                bufEmpty = bufIndex == bufSize - 2
                if (bufEmpty) {
                    bufIndex = bufSize
                    bufType = PathIterator.SEG_LINETO
                }
            }
            PathIterator.SEG_CUBICTO -> {
                if (bufEmpty) {
                    bufIndex -= 8
                    buf[bufIndex + 0] = px
                    buf[bufIndex + 1] = py
                    System.arraycopy(coords, 0, buf, bufIndex + 2, 6)
                    bufSubdiv = 0
                }

                while (bufSubdiv < recursionLimit) {
                    if (CubicCurve2D.getFlatnessSq(buf, bufIndex) < flatness2) {
                        break
                    }

                    // Realloc buffer
                    if (bufIndex <= 6) {
                        val tmp = DoubleArray(bufSize + BUFFER_CAPACITY)
                        System.arraycopy(
                                buf, bufIndex,
                                tmp, bufIndex + BUFFER_CAPACITY,
                                bufSize - bufIndex)
                        buf = tmp
                        bufSize += BUFFER_CAPACITY
                        bufIndex += BUFFER_CAPACITY
                    }

                    CubicCurve2D.subdivide(buf, bufIndex, buf, bufIndex - 6, buf, bufIndex)

                    bufIndex -= 6
                    bufSubdiv++
                }

                bufIndex += 6
                px = buf[bufIndex]
                py = buf[bufIndex + 1]

                bufEmpty = bufIndex == bufSize - 2
                if (bufEmpty) {
                    bufIndex = bufSize
                    bufType = PathIterator.SEG_LINETO
                }
            }
        }

    }

    override fun next() {
        if (bufEmpty) {
            p.next()
        }
    }

    override fun currentSegment(coords: FloatArray): Int {
        if (isDone) {
            // awt.4B=Iterator out of bounds
            throw NoSuchElementException(Messages.getString("awt.4Bx")) //$NON-NLS-1$
        }
        evaluate()
        var type = bufType
        if (type != PathIterator.SEG_CLOSE) {
            coords[0] = px.toFloat()
            coords[1] = py.toFloat()
            if (type != PathIterator.SEG_MOVETO) {
                type = PathIterator.SEG_LINETO
            }
        }
        return type
    }

    override fun currentSegment(coords: DoubleArray): Int {
        if (isDone) {
            // awt.4B=Iterator out of bounds
            throw NoSuchElementException(Messages.getString("awt.4B")) //$NON-NLS-1$
        }
        evaluate()
        var type = bufType
        if (type != PathIterator.SEG_CLOSE) {
            coords[0] = px
            coords[1] = py
            if (type != PathIterator.SEG_MOVETO) {
                type = PathIterator.SEG_LINETO
            }
        }
        return type
    }

    companion object {

        /**
         * The default points buffer size
         */
        private val BUFFER_SIZE = 16

        /**
         * The default curve subdivision limit
         */
        private val BUFFER_LIMIT = 16

        /**
         * The points buffer capacity
         */
        private val BUFFER_CAPACITY = 16
    }
}

