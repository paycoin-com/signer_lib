/*
 * $Id: 8488294d31fd366d07e07ce17021e6326345e81d $
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

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.util.ArrayList

import com.itextpdf.text.BaseColor
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Image
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.codec.BmpImage

class MetaDo(`in`: InputStream, var cb: PdfContentByte) {
    var `in`: InputMeta
    internal var left: Int = 0
    internal var top: Int = 0
    internal var right: Int = 0
    internal var bottom: Int = 0
    internal var inch: Int = 0
    internal var state = MetaState()

    init {
        this.`in` = InputMeta(`in`)
    }

    @Throws(IOException::class, DocumentException::class)
    fun readAll() {
        if (`in`.readInt() != 0x9AC6CDD7.toInt()) {
            throw DocumentException(MessageLocalization.getComposedMessage("not.a.placeable.windows.metafile"))
        }
        `in`.readWord()
        left = `in`.readShort()
        top = `in`.readShort()
        right = `in`.readShort()
        bottom = `in`.readShort()
        inch = `in`.readWord()
        state.setScalingX((right - left).toFloat() / inch.toFloat() * 72f)
        state.setScalingY((bottom - top).toFloat() / inch.toFloat() * 72f)
        state.setOffsetWx(left)
        state.setOffsetWy(top)
        state.setExtentWx(right - left)
        state.setExtentWy(bottom - top)
        `in`.readInt()
        `in`.readWord()
        `in`.skip(18)

        var tsize: Int
        var function: Int
        cb.setLineCap(1)
        cb.setLineJoin(1)
        while (true) {
            val lenMarker = `in`.length
            tsize = `in`.readInt()
            if (tsize < 3)
                break
            function = `in`.readWord()
            when (function) {
                0 -> {
                }
                META_CREATEPALETTE, META_CREATEREGION, META_DIBCREATEPATTERNBRUSH -> state.addMetaObject(MetaObject())
                META_CREATEPENINDIRECT -> {
                    val pen = MetaPen()
                    pen.init(`in`)
                    state.addMetaObject(pen)
                }
                META_CREATEBRUSHINDIRECT -> {
                    val brush = MetaBrush()
                    brush.init(`in`)
                    state.addMetaObject(brush)
                }
                META_CREATEFONTINDIRECT -> {
                    val font = MetaFont()
                    font.init(`in`)
                    state.addMetaObject(font)
                }
                META_SELECTOBJECT -> {
                    val idx = `in`.readWord()
                    state.selectMetaObject(idx, cb)
                }
                META_DELETEOBJECT -> {
                    val idx = `in`.readWord()
                    state.deleteMetaObject(idx)
                }
                META_SAVEDC -> state.saveState(cb)
                META_RESTOREDC -> {
                    val idx = `in`.readShort()
                    state.restoreState(idx, cb)
                }
                META_SETWINDOWORG -> {
                    state.setOffsetWy(`in`.readShort())
                    state.setOffsetWx(`in`.readShort())
                }
                META_SETWINDOWEXT -> {
                    state.setExtentWy(`in`.readShort())
                    state.setExtentWx(`in`.readShort())
                }
                META_MOVETO -> {
                    val y = `in`.readShort()
                    val p = Point(`in`.readShort(), y)
                    state.currentPoint = p
                }
                META_LINETO -> {
                    val y = `in`.readShort()
                    val x = `in`.readShort()
                    val p = state.currentPoint
                    cb.moveTo(state.transformX(p.x), state.transformY(p.y))
                    cb.lineTo(state.transformX(x), state.transformY(y))
                    cb.stroke()
                    state.currentPoint = Point(x, y)
                }
                META_POLYLINE -> {
                    state.setLineJoinPolygon(cb)
                    val len = `in`.readWord()
                    var x = `in`.readShort()
                    var y = `in`.readShort()
                    cb.moveTo(state.transformX(x), state.transformY(y))
                    for (k in 1..len - 1) {
                        x = `in`.readShort()
                        y = `in`.readShort()
                        cb.lineTo(state.transformX(x), state.transformY(y))
                    }
                    cb.stroke()
                }
                META_POLYGON -> {
                    if (isNullStrokeFill(false))
                        break
                    val len = `in`.readWord()
                    val sx = `in`.readShort()
                    val sy = `in`.readShort()
                    cb.moveTo(state.transformX(sx), state.transformY(sy))
                    for (k in 1..len - 1) {
                        val x = `in`.readShort()
                        val y = `in`.readShort()
                        cb.lineTo(state.transformX(x), state.transformY(y))
                    }
                    cb.lineTo(state.transformX(sx), state.transformY(sy))
                    strokeAndFill()
                }
                META_POLYPOLYGON -> {
                    if (isNullStrokeFill(false))
                        break
                    val numPoly = `in`.readWord()
                    val lens = IntArray(numPoly)
                    for (k in lens.indices)
                        lens[k] = `in`.readWord()
                    for (j in lens.indices) {
                        val len = lens[j]
                        val sx = `in`.readShort()
                        val sy = `in`.readShort()
                        cb.moveTo(state.transformX(sx), state.transformY(sy))
                        for (k in 1..len - 1) {
                            val x = `in`.readShort()
                            val y = `in`.readShort()
                            cb.lineTo(state.transformX(x), state.transformY(y))
                        }
                        cb.lineTo(state.transformX(sx), state.transformY(sy))
                    }
                    strokeAndFill()
                }
                META_ELLIPSE -> {
                    if (isNullStrokeFill(state.lineNeutral))
                        break
                    val b = `in`.readShort()
                    val r = `in`.readShort()
                    val t = `in`.readShort()
                    val l = `in`.readShort()
                    cb.arc(state.transformX(l), state.transformY(b), state.transformX(r), state.transformY(t), 0f, 360f)
                    strokeAndFill()
                }
                META_ARC -> {
                    if (isNullStrokeFill(state.lineNeutral))
                        break
                    val yend = state.transformY(`in`.readShort())
                    val xend = state.transformX(`in`.readShort())
                    val ystart = state.transformY(`in`.readShort())
                    val xstart = state.transformX(`in`.readShort())
                    val b = state.transformY(`in`.readShort())
                    val r = state.transformX(`in`.readShort())
                    val t = state.transformY(`in`.readShort())
                    val l = state.transformX(`in`.readShort())
                    val cx = (r + l) / 2
                    val cy = (t + b) / 2
                    val arc1 = getArc(cx, cy, xstart, ystart)
                    var arc2 = getArc(cx, cy, xend, yend)
                    arc2 -= arc1
                    if (arc2 <= 0)
                        arc2 += 360f
                    cb.arc(l, b, r, t, arc1, arc2)
                    cb.stroke()
                }
                META_PIE -> {
                    if (isNullStrokeFill(state.lineNeutral))
                        break
                    val yend = state.transformY(`in`.readShort())
                    val xend = state.transformX(`in`.readShort())
                    val ystart = state.transformY(`in`.readShort())
                    val xstart = state.transformX(`in`.readShort())
                    val b = state.transformY(`in`.readShort())
                    val r = state.transformX(`in`.readShort())
                    val t = state.transformY(`in`.readShort())
                    val l = state.transformX(`in`.readShort())
                    val cx = (r + l) / 2
                    val cy = (t + b) / 2
                    val arc1 = getArc(cx, cy, xstart, ystart).toDouble()
                    var arc2 = getArc(cx, cy, xend, yend).toDouble()
                    arc2 -= arc1
                    if (arc2 <= 0)
                        arc2 += 360.0
                    val ar = PdfContentByte.bezierArc(l.toDouble(), b.toDouble(), r.toDouble(), t.toDouble(), arc1, arc2)
                    if (ar.isEmpty())
                        break
                    var pt = ar[0]
                    cb.moveTo(cx, cy)
                    cb.lineTo(pt[0], pt[1])
                    for (k in ar.indices) {
                        pt = ar[k]
                        cb.curveTo(pt[2], pt[3], pt[4], pt[5], pt[6], pt[7])
                    }
                    cb.lineTo(cx, cy)
                    strokeAndFill()
                }
                META_CHORD -> {
                    if (isNullStrokeFill(state.lineNeutral))
                        break
                    val yend = state.transformY(`in`.readShort())
                    val xend = state.transformX(`in`.readShort())
                    val ystart = state.transformY(`in`.readShort())
                    val xstart = state.transformX(`in`.readShort())
                    val b = state.transformY(`in`.readShort())
                    val r = state.transformX(`in`.readShort())
                    val t = state.transformY(`in`.readShort())
                    val l = state.transformX(`in`.readShort())
                    var cx = ((r + l) / 2).toDouble()
                    var cy = ((t + b) / 2).toDouble()
                    val arc1 = getArc(cx, cy, xstart.toDouble(), ystart.toDouble())
                    var arc2 = getArc(cx, cy, xend.toDouble(), yend.toDouble())
                    arc2 -= arc1
                    if (arc2 <= 0)
                        arc2 += 360.0
                    val ar = PdfContentByte.bezierArc(l.toDouble(), b.toDouble(), r.toDouble(), t.toDouble(), arc1, arc2)
                    if (ar.isEmpty())
                        break
                    var pt = ar[0]
                    cx = pt[0]
                    cy = pt[1]
                    cb.moveTo(cx, cy)
                    for (k in ar.indices) {
                        pt = ar[k]
                        cb.curveTo(pt[2], pt[3], pt[4], pt[5], pt[6], pt[7])
                    }
                    cb.lineTo(cx, cy)
                    strokeAndFill()
                }
                META_RECTANGLE -> {
                    if (isNullStrokeFill(true))
                        break
                    val b = state.transformY(`in`.readShort())
                    val r = state.transformX(`in`.readShort())
                    val t = state.transformY(`in`.readShort())
                    val l = state.transformX(`in`.readShort())
                    cb.rectangle(l, b, r - l, t - b)
                    strokeAndFill()
                }
                META_ROUNDRECT -> {
                    if (isNullStrokeFill(true))
                        break
                    val h = state.transformY(0) - state.transformY(`in`.readShort())
                    val w = state.transformX(`in`.readShort()) - state.transformX(0)
                    val b = state.transformY(`in`.readShort())
                    val r = state.transformX(`in`.readShort())
                    val t = state.transformY(`in`.readShort())
                    val l = state.transformX(`in`.readShort())
                    cb.roundRectangle(l, b, r - l, t - b, (h + w) / 4)
                    strokeAndFill()
                }
                META_INTERSECTCLIPRECT -> {
                    val b = state.transformY(`in`.readShort())
                    val r = state.transformX(`in`.readShort())
                    val t = state.transformY(`in`.readShort())
                    val l = state.transformX(`in`.readShort())
                    cb.rectangle(l, b, r - l, t - b)
                    cb.eoClip()
                    cb.newPath()
                }
                META_EXTTEXTOUT -> {
                    val y = `in`.readShort()
                    val x = `in`.readShort()
                    val count = `in`.readWord()
                    val flag = `in`.readWord()
                    var x1 = 0
                    var y1 = 0
                    var x2 = 0
                    var y2 = 0
                    if (flag and (MetaFont.ETO_CLIPPED or MetaFont.ETO_OPAQUE) != 0) {
                        x1 = `in`.readShort()
                        y1 = `in`.readShort()
                        x2 = `in`.readShort()
                        y2 = `in`.readShort()
                    }
                    val text = ByteArray(count)
                    var k: Int
                    k = 0
                    while (k < count) {
                        val c = `in`.readByte().toByte()
                        if (c.toInt() == 0)
                            break
                        text[k] = c
                        ++k
                    }
                    val s: String
                    try {
                        s = String(text, 0, k, "Cp1252")
                    } catch (e: UnsupportedEncodingException) {
                        s = String(text, 0, k)
                    }

                    outputText(x, y, flag, x1, y1, x2, y2, s)
                }
                META_TEXTOUT -> {
                    var count = `in`.readWord()
                    val text = ByteArray(count)
                    var k: Int
                    k = 0
                    while (k < count) {
                        val c = `in`.readByte().toByte()
                        if (c.toInt() == 0)
                            break
                        text[k] = c
                        ++k
                    }
                    val s: String
                    try {
                        s = String(text, 0, k, "Cp1252")
                    } catch (e: UnsupportedEncodingException) {
                        s = String(text, 0, k)
                    }

                    count = count + 1 and 0xfffe
                    `in`.skip(count - k)
                    val y = `in`.readShort()
                    val x = `in`.readShort()
                    outputText(x, y, 0, 0, 0, 0, 0, s)
                }
                META_SETBKCOLOR -> state.currentBackgroundColor = `in`.readColor()
                META_SETTEXTCOLOR -> state.currentTextColor = `in`.readColor()
                META_SETTEXTALIGN -> state.textAlign = `in`.readWord()
                META_SETBKMODE -> state.backgroundMode = `in`.readWord()
                META_SETPOLYFILLMODE -> state.polyFillMode = `in`.readWord()
                META_SETPIXEL -> {
                    val color = `in`.readColor()
                    val y = `in`.readShort()
                    val x = `in`.readShort()
                    cb.saveState()
                    cb.setColorFill(color)
                    cb.rectangle(state.transformX(x), state.transformY(y), .2f, .2f)
                    cb.fill()
                    cb.restoreState()
                }
                META_DIBSTRETCHBLT, META_STRETCHDIB -> {
                    val rop = `in`.readInt()
                    if (function == META_STRETCHDIB) {
                        /*int usage = */ `in`.readWord()
                    }
                    val srcHeight = `in`.readShort()
                    val srcWidth = `in`.readShort()
                    val ySrc = `in`.readShort()
                    val xSrc = `in`.readShort()
                    val destHeight = state.transformY(`in`.readShort()) - state.transformY(0)
                    val destWidth = state.transformX(`in`.readShort()) - state.transformX(0)
                    val yDest = state.transformY(`in`.readShort())
                    val xDest = state.transformX(`in`.readShort())
                    val b = ByteArray(tsize * 2 - (`in`.length - lenMarker))
                    for (k in b.indices)
                        b[k] = `in`.readByte().toByte()
                    try {
                        val inb = ByteArrayInputStream(b)
                        val bmp = BmpImage.getImage(inb, true, b.size)
                        cb.saveState()
                        cb.rectangle(xDest, yDest, destWidth, destHeight)
                        cb.clip()
                        cb.newPath()
                        bmp.scaleAbsolute(destWidth * bmp.width / srcWidth, -destHeight * bmp.height / srcHeight)
                        bmp.setAbsolutePosition(xDest - destWidth * xSrc / srcWidth, yDest + destHeight * ySrc / srcHeight - bmp.scaledHeight)
                        cb.addImage(bmp)
                        cb.restoreState()
                    } catch (e: Exception) {
                        // empty on purpose
                    }

                }
            }
            `in`.skip(tsize * 2 - (`in`.length - lenMarker))
        }
        state.cleanup(cb)
    }

    fun outputText(x: Int, y: Int, flag: Int, x1: Int, y1: Int, x2: Int, y2: Int, text: String) {
        val font = state.currentFont
        val refX = state.transformX(x)
        val refY = state.transformY(y)
        val angle = state.transformAngle(font.angle)
        val sin = Math.sin(angle.toDouble()).toFloat()
        val cos = Math.cos(angle.toDouble()).toFloat()
        val fontSize = font.getFontSize(state)
        val bf = font.getFont()
        val align = state.textAlign
        val textWidth = bf.getWidthPoint(text, fontSize)
        var tx = 0f
        var ty = 0f
        val descender = bf.getFontDescriptor(BaseFont.DESCENT, fontSize)
        val ury = bf.getFontDescriptor(BaseFont.BBOXURY, fontSize)
        cb.saveState()
        cb.concatCTM(cos, sin, -sin, cos, refX, refY)
        if (align and MetaState.TA_CENTER == MetaState.TA_CENTER)
            tx = -textWidth / 2
        else if (align and MetaState.TA_RIGHT == MetaState.TA_RIGHT)
            tx = -textWidth
        if (align and MetaState.TA_BASELINE == MetaState.TA_BASELINE)
            ty = 0f
        else if (align and MetaState.TA_BOTTOM == MetaState.TA_BOTTOM)
            ty = -descender
        else
            ty = -ury
        var textColor: BaseColor
        if (state.backgroundMode == MetaState.OPAQUE) {
            textColor = state.currentBackgroundColor
            cb.setColorFill(textColor)
            cb.rectangle(tx, ty + descender, textWidth, ury - descender)
            cb.fill()
        }
        textColor = state.currentTextColor
        cb.setColorFill(textColor)
        cb.beginText()
        cb.setFontAndSize(bf, fontSize)
        cb.setTextMatrix(tx, ty)
        cb.showText(text)
        cb.endText()
        if (font.isUnderline) {
            cb.rectangle(tx, ty - fontSize / 4, textWidth, fontSize / 15)
            cb.fill()
        }
        if (font.isStrikeout) {
            cb.rectangle(tx, ty + fontSize / 3, textWidth, fontSize / 15)
            cb.fill()
        }
        cb.restoreState()
    }

    fun isNullStrokeFill(isRectangle: Boolean): Boolean {
        val pen = state.currentPen
        val brush = state.currentBrush
        val noPen = pen.style == MetaPen.PS_NULL
        val style = brush.style
        val isBrush = style == MetaBrush.BS_SOLID || style == MetaBrush.BS_HATCHED && state.backgroundMode == MetaState.OPAQUE
        val result = noPen && !isBrush
        if (!noPen) {
            if (isRectangle)
                state.setLineJoinRectangle(cb)
            else
                state.setLineJoinPolygon(cb)
        }
        return result
    }

    fun strokeAndFill() {
        val pen = state.currentPen
        val brush = state.currentBrush
        val penStyle = pen.style
        val brushStyle = brush.style
        if (penStyle == MetaPen.PS_NULL) {
            cb.closePath()
            if (state.polyFillMode == MetaState.ALTERNATE) {
                cb.eoFill()
            } else {
                cb.fill()
            }
        } else {
            val isBrush = brushStyle == MetaBrush.BS_SOLID || brushStyle == MetaBrush.BS_HATCHED && state.backgroundMode == MetaState.OPAQUE
            if (isBrush) {
                if (state.polyFillMode == MetaState.ALTERNATE)
                    cb.closePathEoFillStroke()
                else
                    cb.closePathFillStroke()
            } else {
                cb.closePathStroke()
            }
        }
    }

    companion object {

        val META_SETBKCOLOR = 0x0201
        val META_SETBKMODE = 0x0102
        val META_SETMAPMODE = 0x0103
        val META_SETROP2 = 0x0104
        val META_SETRELABS = 0x0105
        val META_SETPOLYFILLMODE = 0x0106
        val META_SETSTRETCHBLTMODE = 0x0107
        val META_SETTEXTCHAREXTRA = 0x0108
        val META_SETTEXTCOLOR = 0x0209
        val META_SETTEXTJUSTIFICATION = 0x020A
        val META_SETWINDOWORG = 0x020B
        val META_SETWINDOWEXT = 0x020C
        val META_SETVIEWPORTORG = 0x020D
        val META_SETVIEWPORTEXT = 0x020E
        val META_OFFSETWINDOWORG = 0x020F
        val META_SCALEWINDOWEXT = 0x0410
        val META_OFFSETVIEWPORTORG = 0x0211
        val META_SCALEVIEWPORTEXT = 0x0412
        val META_LINETO = 0x0213
        val META_MOVETO = 0x0214
        val META_EXCLUDECLIPRECT = 0x0415
        val META_INTERSECTCLIPRECT = 0x0416
        val META_ARC = 0x0817
        val META_ELLIPSE = 0x0418
        val META_FLOODFILL = 0x0419
        val META_PIE = 0x081A
        val META_RECTANGLE = 0x041B
        val META_ROUNDRECT = 0x061C
        val META_PATBLT = 0x061D
        val META_SAVEDC = 0x001E
        val META_SETPIXEL = 0x041F
        val META_OFFSETCLIPRGN = 0x0220
        val META_TEXTOUT = 0x0521
        val META_BITBLT = 0x0922
        val META_STRETCHBLT = 0x0B23
        val META_POLYGON = 0x0324
        val META_POLYLINE = 0x0325
        val META_ESCAPE = 0x0626
        val META_RESTOREDC = 0x0127
        val META_FILLREGION = 0x0228
        val META_FRAMEREGION = 0x0429
        val META_INVERTREGION = 0x012A
        val META_PAINTREGION = 0x012B
        val META_SELECTCLIPREGION = 0x012C
        val META_SELECTOBJECT = 0x012D
        val META_SETTEXTALIGN = 0x012E
        val META_CHORD = 0x0830
        val META_SETMAPPERFLAGS = 0x0231
        val META_EXTTEXTOUT = 0x0a32
        val META_SETDIBTODEV = 0x0d33
        val META_SELECTPALETTE = 0x0234
        val META_REALIZEPALETTE = 0x0035
        val META_ANIMATEPALETTE = 0x0436
        val META_SETPALENTRIES = 0x0037
        val META_POLYPOLYGON = 0x0538
        val META_RESIZEPALETTE = 0x0139
        val META_DIBBITBLT = 0x0940
        val META_DIBSTRETCHBLT = 0x0b41
        val META_DIBCREATEPATTERNBRUSH = 0x0142
        val META_STRETCHDIB = 0x0f43
        val META_EXTFLOODFILL = 0x0548
        val META_DELETEOBJECT = 0x01f0
        val META_CREATEPALETTE = 0x00f7
        val META_CREATEPATTERNBRUSH = 0x01F9
        val META_CREATEPENINDIRECT = 0x02FA
        val META_CREATEFONTINDIRECT = 0x02FB
        val META_CREATEBRUSHINDIRECT = 0x02FC
        val META_CREATEREGION = 0x06FF

        internal fun getArc(xCenter: Float, yCenter: Float, xDot: Float, yDot: Float): Float {
            return getArc(xCenter.toDouble(), yCenter.toDouble(), xDot.toDouble(), yDot.toDouble()).toFloat()
        }

        internal fun getArc(xCenter: Double, yCenter: Double, xDot: Double, yDot: Double): Double {
            var s = Math.atan2(yDot - yCenter, xDot - xCenter)
            if (s < 0)
                s += Math.PI * 2
            return (s / Math.PI * 180).toFloat().toDouble()
        }

        @Throws(IOException::class)
        fun wrapBMP(image: Image): ByteArray {
            if (image.originalType != Image.ORIGINAL_BMP)
                throw IOException(MessageLocalization.getComposedMessage("only.bmp.can.be.wrapped.in.wmf"))
            val imgIn: InputStream
            var data: ByteArray? = null
            if (image.originalData == null) {
                imgIn = image.url.openStream()
                val out = ByteArrayOutputStream()
                var b = 0
                while ((b = imgIn.read()) != -1)
                    out.write(b)
                imgIn.close()
                data = out.toByteArray()
            } else
                data = image.originalData
            val sizeBmpWords = (data!!.size - 14 + 1).ushr(1)
            val os = ByteArrayOutputStream()
            // write metafile header
            writeWord(os, 1)
            writeWord(os, 9)
            writeWord(os, 0x0300)
            writeDWord(os, 9 + 4 + 5 + 5 + 13 + sizeBmpWords + 3) // total metafile size
            writeWord(os, 1)
            writeDWord(os, 14 + sizeBmpWords) // max record size
            writeWord(os, 0)
            // write records
            writeDWord(os, 4)
            writeWord(os, META_SETMAPMODE)
            writeWord(os, 8)

            writeDWord(os, 5)
            writeWord(os, META_SETWINDOWORG)
            writeWord(os, 0)
            writeWord(os, 0)

            writeDWord(os, 5)
            writeWord(os, META_SETWINDOWEXT)
            writeWord(os, image.height.toInt())
            writeWord(os, image.width.toInt())

            writeDWord(os, 13 + sizeBmpWords)
            writeWord(os, META_DIBSTRETCHBLT)
            writeDWord(os, 0x00cc0020)
            writeWord(os, image.height.toInt())
            writeWord(os, image.width.toInt())
            writeWord(os, 0)
            writeWord(os, 0)
            writeWord(os, image.height.toInt())
            writeWord(os, image.width.toInt())
            writeWord(os, 0)
            writeWord(os, 0)
            os.write(data, 14, data.size - 14)
            if (data.size and 1 == 1)
                os.write(0)
            //        writeDWord(os, 14 + sizeBmpWords);
            //        writeWord(os, META_STRETCHDIB);
            //        writeDWord(os, 0x00cc0020);
            //        writeWord(os, 0);
            //        writeWord(os, (int)image.height());
            //        writeWord(os, (int)image.width());
            //        writeWord(os, 0);
            //        writeWord(os, 0);
            //        writeWord(os, (int)image.height());
            //        writeWord(os, (int)image.width());
            //        writeWord(os, 0);
            //        writeWord(os, 0);
            //        os.write(data, 14, data.length - 14);
            //        if ((data.length & 1) == 1)
            //            os.write(0);

            writeDWord(os, 3)
            writeWord(os, 0)
            os.close()
            return os.toByteArray()
        }

        @Throws(IOException::class)
        fun writeWord(os: OutputStream, v: Int) {
            os.write(v and 0xff)
            os.write(v.ushr(8) and 0xff)
        }

        @Throws(IOException::class)
        fun writeDWord(os: OutputStream, v: Int) {
            writeWord(os, v and 0xffff)
            writeWord(os, v.ushr(16) and 0xffff)
        }
    }
}
