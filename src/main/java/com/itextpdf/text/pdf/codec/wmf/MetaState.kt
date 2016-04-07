/*
 * $Id: 5167f28e7bd6a06d14d47926561964fc0a604ef1 $
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
package com.itextpdf.text.pdf.codec.wmf

import java.util.ArrayList
import java.util.Stack

import com.itextpdf.text.BaseColor
import com.itextpdf.text.pdf.PdfContentByte

class MetaState {

    var savedStates: Stack<MetaState>
    var MetaObjects: ArrayList<MetaObject>
    var currentPoint: Point
    var currentPen: MetaPen
    var currentBrush: MetaBrush
    var currentFont: MetaFont
    /** Getter for property currentBackgroundColor.
     * @return Value of property currentBackgroundColor.
     */
    /** Setter for property currentBackgroundColor.
     * @param currentBackgroundColor New value of property currentBackgroundColor.
     */
    var currentBackgroundColor = BaseColor.WHITE
    /** Getter for property currentTextColor.
     * @return Value of property currentTextColor.
     */
    /** Setter for property currentTextColor.
     * @param currentTextColor New value of property currentTextColor.
     */
    var currentTextColor = BaseColor.BLACK
    /** Getter for property backgroundMode.
     * @return Value of property backgroundMode.
     */
    /** Setter for property backgroundMode.
     * @param backgroundMode New value of property backgroundMode.
     */
    var backgroundMode = OPAQUE
    /** Getter for property polyFillMode.
     * @return Value of property polyFillMode.
     */
    /** Setter for property polyFillMode.
     * @param polyFillMode New value of property polyFillMode.
     */
    var polyFillMode = ALTERNATE
    var lineJoin = 1
    /** Getter for property textAlign.
     * @return Value of property textAlign.
     */
    /** Setter for property textAlign.
     * @param textAlign New value of property textAlign.
     */
    var textAlign: Int = 0
    var offsetWx: Int = 0
    var offsetWy: Int = 0
    var extentWx: Int = 0
    var extentWy: Int = 0
    var scalingX: Float = 0.toFloat()
    var scalingY: Float = 0.toFloat()


    /** Creates new MetaState  */
    constructor() {
        savedStates = Stack<MetaState>()
        MetaObjects = ArrayList<MetaObject>()
        currentPoint = Point(0, 0)
        currentPen = MetaPen()
        currentBrush = MetaBrush()
        currentFont = MetaFont()
    }

    constructor(state: MetaState) {
        setMetaState(state)
    }

    fun setMetaState(state: MetaState) {
        savedStates = state.savedStates
        MetaObjects = state.MetaObjects
        currentPoint = state.currentPoint
        currentPen = state.currentPen
        currentBrush = state.currentBrush
        currentFont = state.currentFont
        currentBackgroundColor = state.currentBackgroundColor
        currentTextColor = state.currentTextColor
        backgroundMode = state.backgroundMode
        polyFillMode = state.polyFillMode
        textAlign = state.textAlign
        lineJoin = state.lineJoin
        offsetWx = state.offsetWx
        offsetWy = state.offsetWy
        extentWx = state.extentWx
        extentWy = state.extentWy
        scalingX = state.scalingX
        scalingY = state.scalingY
    }

    fun addMetaObject(`object`: MetaObject) {
        for (k in MetaObjects.indices) {
            if (MetaObjects[k] == null) {
                MetaObjects[k] = `object`
                return
            }
        }
        MetaObjects.add(`object`)
    }

    fun selectMetaObject(index: Int, cb: PdfContentByte) {
        val obj = MetaObjects[index] ?: return
        val style: Int
        when (obj.type) {
            MetaObject.META_BRUSH -> {
                currentBrush = obj as MetaBrush?
                style = currentBrush.style
                if (style == MetaBrush.BS_SOLID) {
                    val color = currentBrush.color
                    cb.setColorFill(color)
                } else if (style == MetaBrush.BS_HATCHED) {
                    val color = currentBackgroundColor
                    cb.setColorFill(color)
                }
            }
            MetaObject.META_PEN -> {
                currentPen = obj as MetaPen?
                style = currentPen.style
                if (style != MetaPen.PS_NULL) {
                    val color = currentPen.color
                    cb.setColorStroke(color)
                    cb.setLineWidth(Math.abs(currentPen.penWidth * scalingX / extentWx))
                    when (style) {
                        MetaPen.PS_DASH -> cb.setLineDash(18f, 6f, 0f)
                        MetaPen.PS_DASHDOT -> cb.setLiteral("[9 6 3 6]0 d\n")
                        MetaPen.PS_DASHDOTDOT -> cb.setLiteral("[9 3 3 3 3 3]0 d\n")
                        MetaPen.PS_DOT -> cb.setLineDash(3f, 0f)
                        else -> cb.setLineDash(0f)
                    }
                }
            }
            MetaObject.META_FONT -> {
                currentFont = obj as MetaFont?
            }
        }
    }

    fun deleteMetaObject(index: Int) {
        MetaObjects.set(index, null)
    }

    fun saveState(cb: PdfContentByte) {
        cb.saveState()
        val state = MetaState(this)
        savedStates.push(state)
    }

    fun restoreState(index: Int, cb: PdfContentByte) {
        var pops: Int
        if (index < 0)
            pops = Math.min(-index, savedStates.size)
        else
            pops = Math.max(savedStates.size - index, 0)
        if (pops == 0)
            return
        var state: MetaState? = null
        while (pops-- != 0) {
            cb.restoreState()
            state = savedStates.pop()
        }
        setMetaState(state)
    }

    fun cleanup(cb: PdfContentByte) {
        var k = savedStates.size
        while (k-- > 0)
            cb.restoreState()
    }

    fun transformX(x: Int): Float {
        return (x.toFloat() - offsetWx) * scalingX / extentWx
    }

    fun transformY(y: Int): Float {
        return (1f - (y.toFloat() - offsetWy) / extentWy) * scalingY
    }

    fun setScalingX(scalingX: Float) {
        this.scalingX = scalingX
    }

    fun setScalingY(scalingY: Float) {
        this.scalingY = scalingY
    }

    fun setOffsetWx(offsetWx: Int) {
        this.offsetWx = offsetWx
    }

    fun setOffsetWy(offsetWy: Int) {
        this.offsetWy = offsetWy
    }

    fun setExtentWx(extentWx: Int) {
        this.extentWx = extentWx
    }

    fun setExtentWy(extentWy: Int) {
        this.extentWy = extentWy
    }

    fun transformAngle(angle: Float): Float {
        val ta = if (scalingY < 0) -angle else angle
        return (if (scalingX < 0) Math.PI - ta else ta).toFloat()
    }

    fun setLineJoinRectangle(cb: PdfContentByte) {
        if (lineJoin != 0) {
            lineJoin = 0
            cb.setLineJoin(0)
        }
    }

    fun setLineJoinPolygon(cb: PdfContentByte) {
        if (lineJoin == 0) {
            lineJoin = 1
            cb.setLineJoin(1)
        }
    }

    val lineNeutral: Boolean
        get() = lineJoin == 0

    companion object {

        val TA_NOUPDATECP = 0
        val TA_UPDATECP = 1
        val TA_LEFT = 0
        val TA_RIGHT = 2
        val TA_CENTER = 6
        val TA_TOP = 0
        val TA_BOTTOM = 8
        val TA_BASELINE = 24

        val TRANSPARENT = 1
        val OPAQUE = 2

        val ALTERNATE = 1
        val WINDING = 2
    }

}
