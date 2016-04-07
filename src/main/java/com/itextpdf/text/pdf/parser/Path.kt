/*
 * $Id: fca04a216656464d6dc00369f338a6b8ed083514 $
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
import java.util.Arrays

/**
 * Paths define shapes, trajectories, and regions of all sorts. They shall be used
 * to draw lines, define the shapes of filled areas, and specify boundaries for clipping
 * other graphics. A path shall be composed of straight and curved line segments, which
 * may connect to one another or may be disconnected.

 * @since 5.5.6
 */
class Path {

    private val subpaths = ArrayList<Subpath>()
    /**
     * The current point is the trailing endpoint of the segment most recently added to the current path.

     * @return The current point.
     */
    var currentPoint: Point2D? = null
        private set

    constructor() {
    }

    constructor(subpaths: List<Subpath>) {
        addSubpaths(subpaths)
    }

    /**
     * @return A [java.util.List] of subpaths forming this path.
     */
    fun getSubpaths(): List<Subpath> {
        return subpaths
    }

    /**
     * Adds the subpath to this path.

     * @param subpath The subpath to be added to this path.
     */
    fun addSubpath(subpath: Subpath) {
        subpaths.add(subpath)
        currentPoint = subpath.lastPoint
    }

    /**
     * Adds the subpaths to this path.

     * @param subpaths [java.util.List] of subpaths to be added to this path.
     */
    fun addSubpaths(subpaths: List<Subpath>) {
        if (subpaths.size > 0) {
            this.subpaths.addAll(subpaths)
            currentPoint = this.subpaths[subpaths.size - 1].lastPoint
        }
    }

    /**
     * Begins a new subpath by moving the current point to coordinates (x, y).
     */
    fun moveTo(x: Float, y: Float) {
        currentPoint = Point2D.Float(x, y)
        val lastSubpath = lastSubpath

        if (lastSubpath != null && lastSubpath.isSinglePointOpen) {
            lastSubpath.startPoint = currentPoint
        } else {
            subpaths.add(Subpath(currentPoint))
        }
    }

    /**
     * Appends a straight line segment from the current point to the point (x, y).
     */
    fun lineTo(x: Float, y: Float) {
        if (currentPoint == null) {
            throw RuntimeException(START_PATH_ERR_MSG)
        }

        val targetPoint = Point2D.Float(x, y)
        lastSubpath!!.addSegment(Line(currentPoint, targetPoint))
        currentPoint = targetPoint
    }

    /**
     * Appends a cubic Bezier curve to the current path. The curve shall extend from
     * the current point to the point (x3, y3).
     */
    fun curveTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        if (currentPoint == null) {
            throw RuntimeException(START_PATH_ERR_MSG)
        }
        // Numbered in natural order
        val secondPoint = Point2D.Float(x1, y1)
        val thirdPoint = Point2D.Float(x2, y2)
        val fourthPoint = Point2D.Float(x3, y3)

        val controlPoints = ArrayList(Arrays.asList<Point2D>(currentPoint, secondPoint, thirdPoint, fourthPoint))
        lastSubpath!!.addSegment(BezierCurve(controlPoints))

        currentPoint = fourthPoint
    }

    /**
     * Appends a cubic Bezier curve to the current path. The curve shall extend from
     * the current point to the point (x3, y3) with the note that the current
     * point represents two control points.
     */
    fun curveTo(x2: Float, y2: Float, x3: Float, y3: Float) {
        if (currentPoint == null) {
            throw RuntimeException(START_PATH_ERR_MSG)
        }

        curveTo(currentPoint!!.x as Float, currentPoint!!.y as Float, x2, y2, x3, y3)
    }

    /**
     * Appends a cubic Bezier curve to the current path. The curve shall extend from
     * the current point to the point (x3, y3) with the note that the (x3, y3)
     * point represents two control points.
     */
    fun curveFromTo(x1: Float, y1: Float, x3: Float, y3: Float) {
        if (currentPoint == null) {
            throw RuntimeException(START_PATH_ERR_MSG)
        }

        curveTo(x1, y1, x3, y3, x3, y3)
    }

    /**
     * Appends a rectangle to the current path as a complete subpath.
     */
    fun rectangle(x: Float, y: Float, w: Float, h: Float) {
        moveTo(x, y)
        lineTo(x + w, y)
        lineTo(x + w, y + h)
        lineTo(x, y + h)
        closeSubpath()
    }

    /**
     * Closes the current subpath.
     */
    fun closeSubpath() {
        val lastSubpath = lastSubpath
        lastSubpath.isClosed = true

        val startPoint = lastSubpath.startPoint
        moveTo(startPoint.x as Float, startPoint.y as Float)
    }

    /**
     * Closes all subpathes contained in this path.
     */
    fun closeAllSubpaths() {
        for (subpath in subpaths) {
            subpath.isClosed = true
        }
    }

    /**
     * Adds additional line to each closed subpath and makes the subpath unclosed.
     * The line connects the last and the first points of the subpaths.

     * @returns Indices of modified subpaths.
     */
    fun replaceCloseWithLine(): List<Int> {
        val modifiedSubpathsIndices = ArrayList<Int>()
        var i = 0

        /* It could be replaced with "for" cycle, because IList in C# provides effective
             * access by index. In Java List interface has at least one implementation (LinkedList)
             * which is "bad" for access elements by index.
             */
        for (subpath in subpaths) {
            if (subpath.isClosed) {
                subpath.isClosed = false
                subpath.addSegment(Line(subpath.lastPoint, subpath.startPoint))
                modifiedSubpathsIndices.add(i)
            }

            ++i
        }

        return modifiedSubpathsIndices
    }

    /**
     * Path is empty if it contains no subpaths.
     */
    val isEmpty: Boolean
        get() = subpaths.size == 0

    private val lastSubpath: Subpath?
        get() = if (subpaths.size > 0) subpaths[subpaths.size - 1] else null

    companion object {

        private val START_PATH_ERR_MSG = "Path shall start with \"re\" or \"m\" operator"
    }
}
