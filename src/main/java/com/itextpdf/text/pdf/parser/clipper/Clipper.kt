/*
 * $Id: 33f03c04604a83220be42c8aae68880538ed9472 $
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
package com.itextpdf.text.pdf.parser.clipper

import com.itextpdf.text.pdf.parser.clipper.Point.LongPoint

interface Clipper {
    enum class ClipType {
        INTERSECTION, UNION, DIFFERENCE, XOR
    }

    enum class Direction {
        RIGHT_TO_LEFT, LEFT_TO_RIGHT
    }

    enum class EndType {
        CLOSED_POLYGON, CLOSED_LINE, OPEN_BUTT, OPEN_SQUARE, OPEN_ROUND
    }

    enum class JoinType {
        BEVEL, ROUND, MITER
    }

    enum class PolyFillType {
        EVEN_ODD, NON_ZERO, POSITIVE, NEGATIVE
    }

    enum class PolyType {
        SUBJECT, CLIP
    }

    interface ZFillCallback {
        fun zFill(bot1: LongPoint, top1: LongPoint, bot2: LongPoint, top2: LongPoint, pt: LongPoint)
    }

    fun addPath(pg: Path, polyType: PolyType, Closed: Boolean): Boolean

    fun addPaths(ppg: Paths, polyType: PolyType, closed: Boolean): Boolean

    fun clear()

    fun execute(clipType: ClipType, solution: Paths): Boolean

    fun execute(clipType: ClipType, solution: Paths, subjFillType: PolyFillType, clipFillType: PolyFillType): Boolean

    fun execute(clipType: ClipType, polytree: PolyTree): Boolean

    fun execute(clipType: ClipType, polytree: PolyTree, subjFillType: PolyFillType, clipFillType: PolyFillType): Boolean

    companion object {

        //InitOptions that can be passed to the constructor ...
        val REVERSE_SOLUTION = 1

        val STRICTLY_SIMPLE = 2

        val PRESERVE_COLINEAR = 4
    }
}
