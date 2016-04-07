/*
 * $Id: 965d2d015069c416210c73173b3fe5154ea20d6a $
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

/**
 * Represents a line.

 * @since 5.5.6
 */
class Line
/**
 * Constructs a new line based on the given coordinates.
 */
@JvmOverloads constructor(x1: Float = 0f, y1: Float = 0f, x2: Float = 0f, y2: Float = 0f) : Shape {

    private val p1: Point2D
    private val p2: Point2D

    init {
        p1 = Point2D.Float(x1, y1)
        p2 = Point2D.Float(x2, y2)
    }

    /**
     * Constructs a new line based on the given coordinates.
     */
    constructor(p1: Point2D, p2: Point2D) : this(p1.x as Float, p1.y as Float, p2.x as Float, p2.y as Float) {
    }

    override val basePoints: List<Point2D>
        get() {
            val basePoints = ArrayList<Point2D>(2)
            basePoints.add(p1)
            basePoints.add(p2)

            return basePoints
        }
}
/**
 * Constructs a new zero-length line starting at zero.
 */
