/*
 * $Id: fd84e2e7215024c5f620c57910374db426f37ac4 $
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

import com.itextpdf.text.BaseColor
import com.itextpdf.text.error_messages.MessageLocalization

import java.util.Arrays
import java.util.Locale

class PdfDeviceNColor(spotColors: Array<PdfSpotColor>) : ICachedColorSpace, IPdfSpecialColorSpace {
    var spotColors: Array<PdfSpotColor>
        internal set
    internal var colorantsDetails: Array<ColorDetails>? = null

    init {
        this.spotColors = spotColors
    }

    val numberOfColorants: Int
        get() = spotColors.size

    override fun getColorantDetails(writer: PdfWriter): Array<ColorDetails> {
        if (colorantsDetails == null) {
            colorantsDetails = arrayOfNulls<ColorDetails>(spotColors.size)
            var i = 0
            for (spotColorant in spotColors) {
                colorantsDetails[i] = writer.addSimple(spotColorant)
                i++
            }
        }
        return colorantsDetails
    }

    override fun getPdfObject(writer: PdfWriter): PdfObject {
        val array = PdfArray(PdfName.DEVICEN)

        val colorants = PdfArray()
        val colorantsRanges = FloatArray(spotColors.size * 2)
        val colorantsDict = PdfDictionary()
        var psFunFooter = ""

        val numberOfColorants = spotColors.size
        val CMYK = Array(4) { FloatArray(numberOfColorants) }
        var i = 0
        while (i < numberOfColorants) {
            val spotColorant = spotColors[i]
            colorantsRanges[2 * i] = 0f
            colorantsRanges[2 * i + 1] = 1f
            colorants.add(spotColorant.name)
            if (colorantsDict.get(spotColorant.name) != null)
                throw RuntimeException(MessageLocalization.getComposedMessage("devicen.component.names.shall.be.different"))
            if (colorantsDetails != null)
                colorantsDict.put(spotColorant.name, colorantsDetails!![i].indirectReference)
            else
                colorantsDict.put(spotColorant.name, spotColorant.getPdfObject(writer))
            val color = spotColorant.alternativeCS
            if (color is ExtendedColor) {
                val type = color.type
                when (type) {
                    ExtendedColor.TYPE_GRAY -> {
                        CMYK[0][i] = 0f
                        CMYK[1][i] = 0f
                        CMYK[2][i] = 0f
                        CMYK[3][i] = 1 - (color as GrayColor).gray
                    }
                    ExtendedColor.TYPE_CMYK -> {
                        CMYK[0][i] = (color as CMYKColor).cyan
                        CMYK[1][i] = color.magenta
                        CMYK[2][i] = color.yellow
                        CMYK[3][i] = color.black
                    }
                    ExtendedColor.TYPE_LAB -> {
                        val cmyk = (color as LabColor).toCmyk()
                        CMYK[0][i] = cmyk.cyan
                        CMYK[1][i] = cmyk.magenta
                        CMYK[2][i] = cmyk.yellow
                        CMYK[3][i] = cmyk.black
                    }
                    else -> throw RuntimeException(MessageLocalization.getComposedMessage("only.rgb.gray.and.cmyk.are.supported.as.alternative.color.spaces"))
                }
            } else {
                val r = color.red.toFloat()
                val g = color.green.toFloat()
                val b = color.blue.toFloat()
                var computedC = 0f
                var computedM = 0f
                var computedY = 0f
                var computedK = 0f

                // BLACK
                if (r == 0f && g == 0f && b == 0f) {
                    computedK = 1f
                } else {
                    computedC = 1 - r / 255
                    computedM = 1 - g / 255
                    computedY = 1 - b / 255

                    val minCMY = Math.min(computedC,
                            Math.min(computedM, computedY))
                    computedC = (computedC - minCMY) / (1 - minCMY)
                    computedM = (computedM - minCMY) / (1 - minCMY)
                    computedY = (computedY - minCMY) / (1 - minCMY)
                    computedK = minCMY
                }
                CMYK[0][i] = computedC
                CMYK[1][i] = computedM
                CMYK[2][i] = computedY
                CMYK[3][i] = computedK
            }
            psFunFooter += "pop "
            i++
        }
        array.add(colorants)

        var psFunHeader = String.format(Locale.US, "1.000000 %d 1 roll ", numberOfColorants + 1)
        array.add(PdfName.DEVICECMYK)
        psFunHeader = psFunHeader + psFunHeader + psFunHeader + psFunHeader
        var psFun = ""
        i = numberOfColorants + 4
        while (i > numberOfColorants) {
            psFun += String.format(Locale.US, "%d -1 roll ", i)
            for (j in numberOfColorants downTo 1) {
                psFun += String.format(Locale.US, "%d index %f mul 1.000000 cvr exch sub mul ", j, CMYK[numberOfColorants + 4 - i][numberOfColorants - j])
            }
            psFun += String.format(Locale.US, "1.000000 cvr exch sub %d 1 roll ", i)
            i--
        }

        val func = PdfFunction.type4(writer, colorantsRanges, floatArrayOf(0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f), "{ $psFunHeader$psFun$psFunFooter}")
        array.add(func.getReference())

        val attr = PdfDictionary()
        attr.put(PdfName.SUBTYPE, PdfName.NCHANNEL)
        attr.put(PdfName.COLORANTS, colorantsDict)
        array.add(attr)

        return array
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is PdfDeviceNColor) return false

        if (!Arrays.equals(spotColors, o.spotColors)) return false

        return true
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(spotColors)
    }
}
