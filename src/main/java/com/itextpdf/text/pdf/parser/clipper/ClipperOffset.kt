/*
 * $Id: 7b36d61246e86841ef634042728f5d854aad3b3a $
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

import java.util.ArrayList
import java.util.Collections

import com.itextpdf.text.pdf.parser.clipper.Clipper.ClipType
import com.itextpdf.text.pdf.parser.clipper.Clipper.EndType
import com.itextpdf.text.pdf.parser.clipper.Clipper.JoinType
import com.itextpdf.text.pdf.parser.clipper.Clipper.PolyFillType
import com.itextpdf.text.pdf.parser.clipper.Clipper.PolyType
import com.itextpdf.text.pdf.parser.clipper.Point.DoublePoint
import com.itextpdf.text.pdf.parser.clipper.Point.LongPoint

class ClipperOffset @JvmOverloads constructor(private val miterLimit: Double = 2.0, private val arcTolerance: Double = ClipperOffset.DEFAULT_ARC_TOLERANCE) {

    private var destPolys: Paths? = null
    private var srcPoly: Path? = null
    private var destPoly: Path? = null

    private val normals: MutableList<DoublePoint>
    private var delta: Double = 0.toDouble()
    private var inA: Double = 0.toDouble()
    private var sin: Double = 0.toDouble()
    private var cos: Double = 0.toDouble()

    private var miterLim: Double = 0.toDouble()
    private var stepsPerRad: Double = 0.toDouble()
    private var lowest: LongPoint? = null

    private val polyNodes: PolyNode

    init {
        lowest = LongPoint()
        lowest!!.setX(-1L)
        polyNodes = PolyNode()
        normals = ArrayList<DoublePoint>()
    }

    fun addPath(path: Path, joinType: JoinType, endType: EndType) {
        var highI = path.size - 1
        if (highI < 0) {
            return
        }
        val newNode = PolyNode()
        newNode.joinType = joinType
        newNode.endType = endType

        //strip duplicate points from path and also get index to the lowest point ...
        if (endType == EndType.CLOSED_LINE || endType == EndType.CLOSED_POLYGON) {
            while (highI > 0 && path[0] === path[highI]) {
                highI--
            }
        }

        newNode.polygon.add(path[0])
        var j = 0
        var k = 0
        for (i in 1..highI) {
            if (newNode.polygon[j] !== path[i]) {
                j++
                newNode.polygon.add(path[i])
                if (path[i].y > newNode.polygon[k].y || path[i].y == newNode.polygon[k].y && path[i].x < newNode.polygon[k].x) {
                    k = j
                }
            }
        }
        if (endType == EndType.CLOSED_POLYGON && j < 2) {
            return
        }

        polyNodes.addChild(newNode)

        //if this path's lowest pt is lower than all the others then update m_lowest
        if (endType != EndType.CLOSED_POLYGON) {
            return
        }
        if (lowest!!.x < 0) {
            lowest = LongPoint((polyNodes.childCount - 1).toLong(), k.toLong())
        } else {
            val ip = polyNodes.getChilds()[lowest!!.x.toInt()].polygon[lowest!!.y.toInt()]
            if (newNode.polygon[k].y > ip.y || newNode.polygon[k].y == ip.y && newNode.polygon[k].x < ip.x) {
                lowest = LongPoint((polyNodes.childCount - 1).toLong(), k.toLong())
            }
        }
    }

    fun addPaths(paths: Paths, joinType: JoinType, endType: EndType) {
        for (p in paths) {
            addPath(p, joinType, endType)
        }
    }

    fun clear() {
        polyNodes.getChilds().clear()
        lowest!!.setX(-1L)
    }

    private fun doMiter(j: Int, k: Int, r: Double) {
        val q = delta / r
        destPoly!!.add(LongPoint(Math.round(srcPoly!![j].x + (normals[k].x + normals[j].x) * q), Math.round(srcPoly!![j].y + (normals[k].y + normals[j].y) * q)))
    }

    private fun doOffset(delta: Double) {
        destPolys = Paths()
        this.delta = delta

        //if Zero offset, just copy any CLOSED polygons to m_p and return ...
        if (nearZero(delta)) {
            for (i in 0..polyNodes.childCount - 1) {
                val node = polyNodes.getChilds()[i]
                if (node.endType == EndType.CLOSED_POLYGON) {
                    destPolys!!.add(node.polygon)
                }
            }
            return
        }

        //see offset_triginometry3.svg in the documentation folder ...
        if (miterLimit > 2) {
            miterLim = 2 / (miterLimit * miterLimit)
        } else {
            miterLim = 0.5
        }

        val y: Double
        if (arcTolerance <= 0.0) {
            y = DEFAULT_ARC_TOLERANCE
        } else if (arcTolerance > Math.abs(delta) * DEFAULT_ARC_TOLERANCE) {
            y = Math.abs(delta) * DEFAULT_ARC_TOLERANCE
        } else {
            y = arcTolerance
        }
        //see offset_triginometry2.svg in the documentation folder ...
        val steps = Math.PI / Math.acos(1 - y / Math.abs(delta))
        sin = Math.sin(TWO_PI / steps)
        cos = Math.cos(TWO_PI / steps)
        stepsPerRad = steps / TWO_PI
        if (delta < 0.0) {
            sin = -sin
        }

        for (i in 0..polyNodes.childCount - 1) {
            val node = polyNodes.getChilds()[i]
            srcPoly = node.polygon

            val len = srcPoly!!.size

            if (len == 0 || delta <= 0 && (len < 3 || node.endType != EndType.CLOSED_POLYGON)) {
                continue
            }

            destPoly = Path()

            if (len == 1) {
                if (node.joinType == JoinType.ROUND) {
                    var X = 1.0
                    var Y = 0.0
                    var j = 1
                    while (j <= steps) {
                        destPoly!!.add(LongPoint(Math.round(srcPoly!![0].x + X * delta), Math.round(srcPoly!![0].y + Y * delta)))
                        val X2 = X
                        X = X * cos - sin * Y
                        Y = X2 * sin + Y * cos
                        j++
                    }
                } else {
                    var X = -1.0
                    var Y = -1.0
                    for (j in 0..3) {
                        destPoly!!.add(LongPoint(Math.round(srcPoly!![0].x + X * delta), Math.round(srcPoly!![0].y + Y * delta)))
                        if (X < 0) {
                            X = 1.0
                        } else if (Y < 0) {
                            Y = 1.0
                        } else {
                            X = -1.0
                        }
                    }
                }
                destPolys!!.add(destPoly)
                continue
            }

            //build m_normals ...
            normals.clear()
            for (j in 0..len - 1 - 1) {
                normals.add(Point.getUnitNormal(srcPoly!![j], srcPoly!![j + 1]))
            }
            if (node.endType == EndType.CLOSED_LINE || node.endType == EndType.CLOSED_POLYGON) {
                normals.add(Point.getUnitNormal(srcPoly!![len - 1], srcPoly!![0]))
            } else {
                normals.add(DoublePoint(normals[len - 2]))
            }

            if (node.endType == EndType.CLOSED_POLYGON) {
                val k = intArrayOf(len - 1)
                for (j in 0..len - 1) {
                    offsetPoint(j, k, node.joinType)
                }
                destPolys!!.add(destPoly)
            } else if (node.endType == EndType.CLOSED_LINE) {
                val k = intArrayOf(len - 1)
                for (j in 0..len - 1) {
                    offsetPoint(j, k, node.joinType)
                }
                destPolys!!.add(destPoly)
                destPoly = Path()
                //re-build m_normals ...
                val n = normals[len - 1]
                for (j in len - 1 downTo 1) {
                    normals[j] = DoublePoint(-normals[j - 1].x, -normals[j - 1].y)
                }
                normals[0] = DoublePoint(-n.x, -n.y, 0.0)
                k[0] = 0
                for (j in len - 1 downTo 0) {
                    offsetPoint(j, k, node.joinType)
                }
                destPolys!!.add(destPoly)
            } else {
                val k = IntArray(1)
                for (j in 1..len - 1 - 1) {
                    offsetPoint(j, k, node.joinType)
                }

                var pt1: LongPoint
                if (node.endType == EndType.OPEN_BUTT) {
                    val j = len - 1
                    pt1 = LongPoint(Math.round(srcPoly!![j].x + normals[j].x * delta), Math.round(srcPoly!![j].y + normals[j].y * delta), 0)
                    destPoly!!.add(pt1)
                    pt1 = LongPoint(Math.round(srcPoly!![j].x - normals[j].x * delta), Math.round(srcPoly!![j].y - normals[j].y * delta), 0)
                    destPoly!!.add(pt1)
                } else {
                    val j = len - 1
                    k[0] = len - 2
                    inA = 0.0
                    normals[j] = DoublePoint(-normals[j].x, -normals[j].y)
                    if (node.endType == EndType.OPEN_SQUARE) {
                        doSquare(j, k[0], true)
                    } else {
                        doRound(j, k[0])
                    }
                }

                //re-build m_normals ...
                for (j in len - 1 downTo 1) {
                    normals[j] = DoublePoint(-normals[j - 1].x, -normals[j - 1].y)
                }

                normals[0] = DoublePoint(-normals[1].x, -normals[1].y)

                k[0] = len - 1
                for (j in k[0] - 1 downTo 1) {
                    offsetPoint(j, k, node.joinType)
                }

                if (node.endType == EndType.OPEN_BUTT) {
                    pt1 = LongPoint(Math.round(srcPoly!![0].x - normals[0].x * delta), Math.round(srcPoly!![0].y - normals[0].y * delta))
                    destPoly!!.add(pt1)
                    pt1 = LongPoint(Math.round(srcPoly!![0].x + normals[0].x * delta), Math.round(srcPoly!![0].y + normals[0].y * delta))
                    destPoly!!.add(pt1)
                } else {
                    k[0] = 1
                    inA = 0.0
                    if (node.endType == EndType.OPEN_SQUARE) {
                        doSquare(0, 1, true)
                    } else {
                        doRound(0, 1)
                    }
                }
                destPolys!!.add(destPoly)
            }
        }
    }

    private fun doRound(j: Int, k: Int) {
        val a = Math.atan2(inA, normals[k].x * normals[j].x + normals[k].y * normals[j].y)
        val steps = Math.max(Math.round(stepsPerRad * Math.abs(a)).toInt(), 1)

        var X = normals[k].x
        var Y = normals[k].y
        var X2: Double
        for (i in 0..steps - 1) {
            destPoly!!.add(LongPoint(Math.round(srcPoly!![j].x + X * delta), Math.round(srcPoly!![j].y + Y * delta)))
            X2 = X
            X = X * cos - sin * Y
            Y = X2 * sin + Y * cos
        }
        destPoly!!.add(LongPoint(Math.round(srcPoly!![j].x + normals[j].x * delta), Math.round(srcPoly!![j].y + normals[j].y * delta)))
    }

    private fun doSquare(j: Int, k: Int, addExtra: Boolean) {
        val nkx = normals[k].x
        val nky = normals[k].y
        val njx = normals[j].x
        val njy = normals[j].y
        val sjx = srcPoly!![j].x.toDouble()
        val sjy = srcPoly!![j].y.toDouble()
        val dx = Math.tan(Math.atan2(inA, nkx * njx + nky * njy) / 4)
        destPoly!!.add(LongPoint(Math.round(sjx + delta * (nkx - if (addExtra) nky * dx else 0)), Math.round(sjy + delta * (nky + if (addExtra) nkx * dx else 0)), 0))
        destPoly!!.add(LongPoint(Math.round(sjx + delta * (njx + if (addExtra) njy * dx else 0)), Math.round(sjy + delta * (njy - if (addExtra) njx * dx else 0)), 0))
    }

    //------------------------------------------------------------------------------

    fun execute(solution: Paths, delta: Double) {
        solution.clear()
        fixOrientations()
        doOffset(delta)
        //now clean up 'corners' ...
        val clpr = DefaultClipper(Clipper.REVERSE_SOLUTION)
        clpr.addPaths(destPolys, PolyType.SUBJECT, true)
        if (delta > 0) {
            clpr.execute(ClipType.UNION, solution, PolyFillType.POSITIVE, PolyFillType.POSITIVE)
        } else {
            val r = destPolys!!.bounds
            val outer = Path(4)

            outer.add(LongPoint(r.left - 10, r.bottom + 10, 0))
            outer.add(LongPoint(r.right + 10, r.bottom + 10, 0))
            outer.add(LongPoint(r.right + 10, r.top - 10, 0))
            outer.add(LongPoint(r.left - 10, r.top - 10, 0))

            clpr.addPath(outer, PolyType.SUBJECT, true)

            clpr.execute(ClipType.UNION, solution, PolyFillType.NEGATIVE, PolyFillType.NEGATIVE)
            if (solution.size > 0) {
                solution.removeAt(0)
            }
        }
    }

    //------------------------------------------------------------------------------

    fun execute(solution: PolyTree, delta: Double) {
        solution.Clear()
        fixOrientations()
        doOffset(delta)

        //now clean up 'corners' ...
        val clpr = DefaultClipper(Clipper.REVERSE_SOLUTION)
        clpr.addPaths(destPolys, PolyType.SUBJECT, true)
        if (delta > 0) {
            clpr.execute(ClipType.UNION, solution, PolyFillType.POSITIVE, PolyFillType.POSITIVE)
        } else {
            val r = destPolys!!.bounds
            val outer = Path(4)

            outer.add(LongPoint(r.left - 10, r.bottom + 10, 0))
            outer.add(LongPoint(r.right + 10, r.bottom + 10, 0))
            outer.add(LongPoint(r.right + 10, r.top - 10, 0))
            outer.add(LongPoint(r.left - 10, r.top - 10, 0))

            clpr.addPath(outer, PolyType.SUBJECT, true)

            clpr.execute(ClipType.UNION, solution, PolyFillType.NEGATIVE, PolyFillType.NEGATIVE)
            //remove the outer PolyNode rectangle ...
            if (solution.childCount == 1 && solution.getChilds()[0].childCount > 0) {
                val outerNode = solution.getChilds()[0]
                solution.getChilds()[0] = outerNode.getChilds()[0]
                solution.getChilds()[0].parent = solution
                for (i in 1..outerNode.childCount - 1) {
                    solution.addChild(outerNode.getChilds()[i])
                }
            } else {
                solution.Clear()
            }
        }
    }

    //------------------------------------------------------------------------------

    private fun fixOrientations() {
        //fixup orientations of all closed paths if the orientation of the
        //closed path with the lowermost vertex is wrong ...
        if (lowest!!.x >= 0 && !polyNodes.childs[lowest!!.x.toInt()].polygon.orientation()) {
            for (i in 0..polyNodes.childCount - 1) {
                val node = polyNodes.childs[i]
                if (node.endType == EndType.CLOSED_POLYGON || node.endType == EndType.CLOSED_LINE && node.polygon.orientation()) {
                    Collections.reverse(node.polygon)

                }
            }
        } else {
            for (i in 0..polyNodes.childCount - 1) {
                val node = polyNodes.childs[i]
                if (node.endType == EndType.CLOSED_LINE && !node.polygon.orientation()) {
                    Collections.reverse(node.polygon)
                }
            }
        }
    }

    private fun offsetPoint(j: Int, kV: IntArray, jointype: JoinType) {
        //cross product ...
        val k = kV[0]
        val nkx = normals[k].x
        val nky = normals[k].y
        val njy = normals[j].y
        val njx = normals[j].x
        val sjx = srcPoly!![j].x
        val sjy = srcPoly!![j].y
        inA = nkx * njy - njx * nky

        if (Math.abs(inA * delta) < 1.0) {
            //dot product ...

            val cosA = nkx * njx + njy * nky
            if (cosA > 0)
            // angle ==> 0 degrees
            {
                destPoly!!.add(LongPoint(Math.round(sjx + nkx * delta), Math.round(sjy + nky * delta), 0))
                return
            }
            //else angle ==> 180 degrees
        } else if (inA > 1.0) {
            inA = 1.0
        } else if (inA < -1.0) {
            inA = -1.0
        }

        if (inA * delta < 0) {
            destPoly!!.add(LongPoint(Math.round(sjx + nkx * delta), Math.round(sjy + nky * delta)))
            destPoly!!.add(srcPoly!![j])
            destPoly!!.add(LongPoint(Math.round(sjx + njx * delta), Math.round(sjy + njy * delta)))
        } else {
            when (jointype) {
                Clipper.JoinType.MITER -> {
                    val r = 1.0 + njx * nkx + njy * nky
                    if (r >= miterLim) {
                        doMiter(j, k, r)
                    } else {
                        doSquare(j, k, false)
                    }
                }
                Clipper.JoinType.BEVEL -> doSquare(j, k, false)
                Clipper.JoinType.ROUND -> doRound(j, k)
            }
        }
        kV[0] = j
    }

    companion object {
        private fun nearZero(`val`: Double): Boolean {
            return `val` > -TOLERANCE && `val` < TOLERANCE
        }

        private val TWO_PI = Math.PI * 2

        private val DEFAULT_ARC_TOLERANCE = 0.25

        private val TOLERANCE = 1.0E-20
    }
    //------------------------------------------------------------------------------
}