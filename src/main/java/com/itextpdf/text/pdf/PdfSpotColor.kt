/*
 * $Id: 9244131336a2b884bd6b9f2ac92dd247abbb8155 $
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

/**
 * A PdfSpotColor defines a ColorSpace

 * @see PdfDictionary
 */

class PdfSpotColor
/**
 * Constructs a new PdfSpotColor.

 * @param        name        a String value
 * *
 * @param        altcs        an alternative colorspace value
 */
(name: String,
        /** The alternative color space  */
 /**
  * Gets the alternative ColorSpace.
  * @return a BaseColor
  */
 var alternativeCS: BaseColor) : ICachedColorSpace, IPdfSpecialColorSpace {

    /**	The color name  */
    var name: PdfName
    // constructors

    var altColorDetails: ColorDetails? = null

    init {
        this.name = PdfName(name)
    }

    override fun getColorantDetails(writer: PdfWriter): Array<ColorDetails> {
        if (altColorDetails == null && this.alternativeCS is ExtendedColor && (this.alternativeCS as ExtendedColor).type == ExtendedColor.TYPE_LAB) {
            altColorDetails = writer.addSimple((alternativeCS as LabColor).labColorSpace)
        }
        return arrayOf<ColorDetails>(altColorDetails)
    }


    @Deprecated("")
    protected fun getSpotObject(writer: PdfWriter): PdfObject {
        return getPdfObject(writer)
    }

    override fun getPdfObject(writer: PdfWriter): PdfObject {
        val array = PdfArray(PdfName.SEPARATION)
        array.add(name)
        var func: PdfFunction? = null
        if (alternativeCS is ExtendedColor) {
            val type = (alternativeCS as ExtendedColor).type
            when (type) {
                ExtendedColor.TYPE_GRAY -> {
                    array.add(PdfName.DEVICEGRAY)
                    func = PdfFunction.type2(writer, floatArrayOf(0f, 1f), null, floatArrayOf(1f), floatArrayOf((alternativeCS as GrayColor).gray), 1f)
                }
                ExtendedColor.TYPE_CMYK -> {
                    array.add(PdfName.DEVICECMYK)
                    val cmyk = alternativeCS as CMYKColor
                    func = PdfFunction.type2(writer, floatArrayOf(0f, 1f), null, floatArrayOf(0f, 0f, 0f, 0f),
                            floatArrayOf(cmyk.cyan, cmyk.magenta, cmyk.yellow, cmyk.black), 1f)
                }
                ExtendedColor.TYPE_LAB -> {
                    val lab = alternativeCS as LabColor
                    if (altColorDetails != null)
                        array.add(altColorDetails!!.indirectReference)
                    else
                        array.add(lab.labColorSpace.getPdfObject(writer))
                    func = PdfFunction.type2(writer, floatArrayOf(0f, 1f), null, floatArrayOf(100f, 0f, 0f),
                            floatArrayOf(lab.l, lab.a, lab.b), 1f)
                }
                else -> throw RuntimeException(MessageLocalization.getComposedMessage("only.rgb.gray.and.cmyk.are.supported.as.alternative.color.spaces"))
            }
        } else {
            array.add(PdfName.DEVICERGB)
            func = PdfFunction.type2(writer, floatArrayOf(0f, 1f), null, floatArrayOf(1f, 1f, 1f),
                    floatArrayOf(alternativeCS.red.toFloat() / 255, alternativeCS.green.toFloat() / 255, alternativeCS.blue.toFloat() / 255), 1f)
        }
        array.add(func!!.getReference())
        return array
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is PdfSpotColor) return false

        if (alternativeCS != o.alternativeCS) return false
        if (name != o.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + alternativeCS.hashCode()
        return result
    }
}
