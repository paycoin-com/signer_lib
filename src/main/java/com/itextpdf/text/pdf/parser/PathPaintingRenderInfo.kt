/*
 * $Id: 2dc661a26d83fa04d42bbdcc98abb19bfd067a52 $
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

/**
 * Contains information relating to painting current path.

 * @since 5.5.6
 */
class PathPaintingRenderInfo
/**
 * @param operation One of the possible combinations of [.STROKE] and [.FILL] values or [.NO_OP]
 * *
 * @param rule      Either [.NONZERO_WINDING_RULE] or [.EVEN_ODD_RULE].
 * *
 * @param gs        The graphics state.
 */
(
        /**
         * @return int value which is either [.NO_OP] or one of possible
         * * combinations of [.STROKE] and [.FILL]
         */
        val operation: Int,
        /**
         * @return Either [.NONZERO_WINDING_RULE] or [.EVEN_ODD_RULE].
         */
        val rule: Int, private val gs: GraphicsState) {

    /**
     * If the operation is [.NO_OP] then the rule is ignored,
     * otherwise [.NONZERO_WINDING_RULE] is used by default.

     * See [.PathPaintingRenderInfo]
     */
    constructor(operation: Int, gs: GraphicsState) : this(operation, NONZERO_WINDING_RULE, gs) {
    }

    /**
     * @return Current transformation matrix.
     */
    val ctm: Matrix
        get() = gs.ctm

    val lineWidth: Float
        get() = gs.lineWidth

    val lineCapStyle: Int
        get() = gs.lineCapStyle

    val lineJoinStyle: Int
        get() = gs.lineJoinStyle

    val miterLimit: Float
        get() = gs.miterLimit

    val lineDashPattern: LineDashPattern
        get() = gs.lineDashPattern

    companion object {

        /**
         * The nonzero winding number rule determines whether a given point is inside a path by
         * conceptually drawing a ray from that point to infinity in any direction and then examining
         * the places where a segment of the path crosses the ray. Starting with a count of 0, the rule
         * adds 1 each time a path segment crosses the ray from left to right and subtracts 1 each time a
         * segment crosses from right to left. After counting all the crossings, if the result is 0, the
         * point is outside the path; otherwise, it is inside.

         * For more details see PDF spec.
         */
        val NONZERO_WINDING_RULE = 1

        /**
         * The even-odd rule determines whether a point is inside a path by drawing a ray from that point in
         * any direction and simply counting the number of path segments that cross the ray, regardless of
         * direction. If this number is odd, the point is inside; if even, the point is outside.

         * For more details see PDF spec.
         */
        val EVEN_ODD_RULE = 2

        /**
         * End the path object without filling or stroking it. This operator shall be a path-painting no-op,
         * used primarily for the side effect of changing the current clipping path
         */
        val NO_OP = 0

        /**
         * Value specifying stroke operation to perform on the current path.
         */
        val STROKE = 1

        /**
         * Value specifying fill operation to perform on the current path. When the fill operation
         * is performed it should use either nonzero winding or even-odd rule.
         */
        val FILL = 2
    }
}
