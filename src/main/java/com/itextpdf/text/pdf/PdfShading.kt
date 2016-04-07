/*
 * $Id: 9ef9d84d0ec895bfc61e2427188378822e220092 $
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
package com.itextpdf.text.pdf

import java.io.IOException
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.BaseColor

/** Implements the shading dictionary (or stream).

 * @author Paulo Soares
 */
class PdfShading
/** Creates new PdfShading  */
protected constructor(writer: PdfWriter) {

    protected var shading: PdfDictionary

    internal var writer:

            PdfWriter
        protected set

    protected var shadingType: Int = 0

    internal var colorDetails:

            ColorDetails
        protected set

    internal var shadingName:

            PdfName
        protected set

    protected var shadingReference: PdfIndirectReference? = null

    var colorSpace: BaseColor? = null
        protected set(color) {
            colorSpace = color
            val type = ExtendedColor.getType(color)
            var colorSpace: PdfObject? = null
            when (type) {
                ExtendedColor.TYPE_GRAY -> {
                    colorSpace = PdfName.DEVICEGRAY
                }
                ExtendedColor.TYPE_CMYK -> {
                    colorSpace = PdfName.DEVICECMYK
                }
                ExtendedColor.TYPE_SEPARATION -> {
                    val spot = color as SpotColor
                    colorDetails = writer.addSimple(spot.pdfSpotColor)
                    colorSpace = colorDetails.indirectReference
                }
                ExtendedColor.TYPE_DEVICEN -> {
                    val deviceNColor = color as DeviceNColor
                    colorDetails = writer.addSimple(deviceNColor.pdfDeviceNColor)
                    colorSpace = colorDetails.indirectReference
                }
                ExtendedColor.TYPE_PATTERN, ExtendedColor.TYPE_SHADING -> {
                    run { throwColorSpaceError() }
                    colorSpace = PdfName.DEVICERGB
                }
                else -> colorSpace = PdfName.DEVICERGB
            }
            shading.put(PdfName.COLORSPACE, colorSpace)
        }

    /** Holds value of property bBox.  */
    var bBox: FloatArray? = null
        set(bBox) {
            if (bBox.size != 4)
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("bbox.must.be.a.4.element.array"))
            this.bBox = bBox
        }

    /** Holds value of property antiAlias.  */
    var isAntiAlias = false

    init {
        this.writer = writer
    }

    internal fun getShadingReference(): PdfIndirectReference {
        if (shadingReference == null)
            shadingReference = writer.pdfIndirectReference
        return shadingReference
    }

    internal fun setName(number: Int) {
        shadingName = PdfName("Sh" + number)
    }

    @Throws(IOException::class)
    fun addToBody() {
        if (bBox != null)
            shading.put(PdfName.BBOX, PdfArray(bBox))
        if (isAntiAlias)
            shading.put(PdfName.ANTIALIAS, PdfBoolean.PDFTRUE)
        writer.addToBody(shading, getShadingReference())
    }

    companion object {

        fun throwColorSpaceError() {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("a.tiling.or.shading.pattern.cannot.be.used.as.a.color.space.in.a.shading.pattern"))
        }

        fun checkCompatibleColors(c1: BaseColor, c2: BaseColor) {
            val type1 = ExtendedColor.getType(c1)
            val type2 = ExtendedColor.getType(c2)
            if (type1 != type2)
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("both.colors.must.be.of.the.same.type"))
            if (type1 == ExtendedColor.TYPE_SEPARATION && (c1 as SpotColor).pdfSpotColor !== (c2 as SpotColor).pdfSpotColor)
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.spot.color.must.be.the.same.only.the.tint.can.vary"))
            if (type1 == ExtendedColor.TYPE_PATTERN || type1 == ExtendedColor.TYPE_SHADING)
                throwColorSpaceError()
        }

        fun getColorArray(color: BaseColor): FloatArray? {
            val type = ExtendedColor.getType(color)
            when (type) {
                ExtendedColor.TYPE_GRAY -> {
                    return floatArrayOf((color as GrayColor).gray)
                }
                ExtendedColor.TYPE_CMYK -> {
                    val cmyk = color as CMYKColor
                    return floatArrayOf(cmyk.cyan, cmyk.magenta, cmyk.yellow, cmyk.black)
                }
                ExtendedColor.TYPE_SEPARATION -> {
                    return floatArrayOf((color as SpotColor).tint)
                }
                ExtendedColor.TYPE_DEVICEN -> {
                    return (color as DeviceNColor).tints
                }
                ExtendedColor.TYPE_RGB -> {
                    return floatArrayOf(color.red / 255f, color.green / 255f, color.blue / 255f)
                }
            }
            throwColorSpaceError()
            return null
        }

        fun type1(writer: PdfWriter, colorSpace: BaseColor, domain: FloatArray?, tMatrix: FloatArray?, function: PdfFunction): PdfShading {
            val sp = PdfShading(writer)
            sp.shading = PdfDictionary()
            sp.shadingType = 1
            sp.shading.put(PdfName.SHADINGTYPE, PdfNumber(sp.shadingType))
            sp.colorSpace = colorSpace
            if (domain != null)
                sp.shading.put(PdfName.DOMAIN, PdfArray(domain))
            if (tMatrix != null)
                sp.shading.put(PdfName.MATRIX, PdfArray(tMatrix))
            sp.shading.put(PdfName.FUNCTION, function.getReference())
            return sp
        }

        fun type2(writer: PdfWriter, colorSpace: BaseColor, coords: FloatArray, domain: FloatArray?, function: PdfFunction, extend: BooleanArray?): PdfShading {
            val sp = PdfShading(writer)
            sp.shading = PdfDictionary()
            sp.shadingType = 2
            sp.shading.put(PdfName.SHADINGTYPE, PdfNumber(sp.shadingType))
            sp.colorSpace = colorSpace
            sp.shading.put(PdfName.COORDS, PdfArray(coords))
            if (domain != null)
                sp.shading.put(PdfName.DOMAIN, PdfArray(domain))
            sp.shading.put(PdfName.FUNCTION, function.getReference())
            if (extend != null && (extend[0] || extend[1])) {
                val array = PdfArray(if (extend[0]) PdfBoolean.PDFTRUE else PdfBoolean.PDFFALSE)
                array.add(if (extend[1]) PdfBoolean.PDFTRUE else PdfBoolean.PDFFALSE)
                sp.shading.put(PdfName.EXTEND, array)
            }
            return sp
        }

        fun type3(writer: PdfWriter, colorSpace: BaseColor, coords: FloatArray, domain: FloatArray?, function: PdfFunction, extend: BooleanArray): PdfShading {
            val sp = type2(writer, colorSpace, coords, domain, function, extend)
            sp.shadingType = 3
            sp.shading.put(PdfName.SHADINGTYPE, PdfNumber(sp.shadingType))
            return sp
        }

        @JvmOverloads fun simpleAxial(writer: PdfWriter, x0: Float, y0: Float, x1: Float, y1: Float, startColor: BaseColor, endColor: BaseColor, extendStart: Boolean = true, extendEnd: Boolean = true): PdfShading {
            checkCompatibleColors(startColor, endColor)
            val function = PdfFunction.type2(writer, floatArrayOf(0f, 1f), null, getColorArray(startColor),
                    getColorArray(endColor), 1f)
            return type2(writer, startColor, floatArrayOf(x0, y0, x1, y1), null, function, booleanArrayOf(extendStart, extendEnd))
        }

        @JvmOverloads fun simpleRadial(writer: PdfWriter, x0: Float, y0: Float, r0: Float, x1: Float, y1: Float, r1: Float, startColor: BaseColor, endColor: BaseColor, extendStart: Boolean = true, extendEnd: Boolean = true): PdfShading {
            checkCompatibleColors(startColor, endColor)
            val function = PdfFunction.type2(writer, floatArrayOf(0f, 1f), null, getColorArray(startColor),
                    getColorArray(endColor), 1f)
            return type3(writer, startColor, floatArrayOf(x0, y0, r0, x1, y1, r1), null, function, booleanArrayOf(extendStart, extendEnd))
        }
    }

}
