/*
 * $Id: 66f4f7ac00f825b3ca3b4b2065ce07764bbcc21a $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Bruno Lowagie, Paulo Soares, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.text.pdf.parser

import com.itextpdf.awt.geom.Point2D

import java.util.ArrayList
import java.util.HashSet

/**
 * As subpath is a part of a path comprising a sequence of connected segments.

 * @since 5.5.6
 */
class Subpath {

    /**
     * @return The point this subpath starts at.
     */
    /**
     * Sets the start point of the subpath.
     * @param startPoint
     */
    var startPoint: Point2D? = null
        set(startPoint) = setStartPoint(startPoint.x as Float, startPoint.y as Float)
    private val segments = ArrayList<Shape>()
    /**
     * Returns a boolean value indicating whether the subpath must be closed or not.
     * Ignore this value if the subpath is a rectangle because in this case it is already closed
     * (of course if you paint the path using re operator)

     * @return boolean value indicating whether the path must be closed or not.
     * *
     * @since 5.5.6
     */
    /**
     * See [.isClosed]
     */
    var isClosed: Boolean = false

    constructor() {
    }

    /**
     * Copy constuctor.
     * @param subpath
     */
    constructor(subpath: Subpath) {
        this.startPoint = subpath.startPoint
        this.segments.addAll(subpath.getSegments())
        this.isClosed = subpath.isClosed
    }

    /**
     * Constructs a new subpath starting at the given point.
     */
    constructor(startPoint: Point2D) : this(startPoint.x as Float, startPoint.y as Float) {
    }

    /**
     * Constructs a new subpath starting at the given point.
     */
    constructor(startPointX: Float, startPointY: Float) {
        this.startPoint = Point2D.Float(startPointX, startPointY)
    }

    /**
     * Sets the start point of the subpath.
     * @param x
     * *
     * @param y
     */
    fun setStartPoint(x: Float, y: Float) {
        this.startPoint = Point2D.Float(x, y)
    }

    /**
     * @return The last point of the subpath.
     */
    val lastPoint: Point2D
        get() {
            var lastPoint: Point2D = startPoint

            if (segments.size > 0 && !isClosed) {
                val shape = segments[segments.size - 1]
                lastPoint = shape.basePoints[shape.basePoints.size - 1]
            }

            return lastPoint
        }

    /**
     * Adds a segment to the subpath.
     * Note: each new segment shall start at the end of the previous segment.
     * @param segment new segment.
     */
    fun addSegment(segment: Shape) {
        if (isClosed) {
            return
        }

        if (isSinglePointOpen) {
            startPoint = segment.basePoints[0]
        }

        segments.add(segment)
    }

    /**
     * @return [java.util.List] comprising all the segments
     * *         the subpath made on.
     */
    fun getSegments(): List<Shape> {
        return segments
    }

    /**
     * Checks whether subpath is empty or not.
     * @return true if the subpath is empty, false otherwise.
     */
    val isEmpty: Boolean
        get() = startPoint == null

    /**
     * @return true if this subpath contains only one point and it is not closed,
     * *         false otherwise
     */
    val isSinglePointOpen: Boolean
        get() = segments.size == 0 && !isClosed

    val isSinglePointClosed: Boolean
        get() = segments.size == 0 && isClosed

    /**
     * Returns a boolean indicating whether the subpath is degenerate or not.
     * A degenerate subpath is the subpath consisting of a single-point closed path or of
     * two or more points at the same coordinates.

     * @return boolean value indicating whether the path is degenerate or not.
     * *
     * @since 5.5.6
     */
    // The first segment of a subpath always starts at startPoint, so...
    // the second clause is for case when we have single point
    val isDegenerate: Boolean
        get() {
            if (segments.size > 0 && isClosed) {
                return false
            }

            for (segment in segments) {
                val points = HashSet(segment.basePoints)
                if (points.size != 1) {
                    return false
                }
            }
            return segments.size > 0 || isClosed
        }

    /**
     * @return [java.util.List] containing points of piecewise linear approximation
     * *         for this subpath.
     * *
     * @since 5.5.6
     */
    val piecewiseLinearApproximation: List<Point2D>
        get() {
            val result = ArrayList<Point2D>()

            if (segments.size == 0) {
                return result
            }

            if (segments[0] is BezierCurve) {
                result.addAll((segments[0] as BezierCurve).piecewiseLinearApproximation)
            } else {
                result.addAll(segments[0].basePoints)
            }

            for (i in 1..segments.size - 1) {
                var segApprox: List<Point2D>

                if (segments[i] is BezierCurve) {
                    segApprox = (segments[i] as BezierCurve).piecewiseLinearApproximation
                    segApprox = segApprox.subList(1, segApprox.size)
                } else {
                    segApprox = segments[i].basePoints
                    segApprox = segApprox.subList(1, segApprox.size)
                }

                result.addAll(segApprox)
            }

            return result
        }
}
