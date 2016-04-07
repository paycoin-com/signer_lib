/*
 * $Id: a553937a7d44d53a2b59c41250ec2596c865210e $
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
package com.itextpdf.text.pdf.internal

import com.itextpdf.text.BaseColor
import com.itextpdf.text.error_messages.MessageLocalization

import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.ExtendedColor
import com.itextpdf.text.pdf.PatternColor
import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfDictionary
import com.itextpdf.text.pdf.PdfGState
import com.itextpdf.text.pdf.PdfImage
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfNumber
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.PdfXConformanceException
import com.itextpdf.text.pdf.ShadingColor
import com.itextpdf.text.pdf.SpotColor
import com.itextpdf.text.pdf.interfaces.PdfXConformance

class PdfXConformanceImp(protected var writer: PdfWriter?) : PdfXConformance {

    /**
     * The value indicating if the PDF has to be in conformance with PDF/X.
     */
    /**
     * @see com.itextpdf.text.pdf.interfaces.PdfXConformance.getPDFXConformance
     */
    /**
     * @see com.itextpdf.text.pdf.interfaces.PdfXConformance.setPDFXConformance
     */
    var pdfxConformance = PdfWriter.PDFXNONE

    /**
     * @see com.itextpdf.text.pdf.interfaces.PdfIsoConformance.isPdfIso
     */
    override val isPdfIso: Boolean
        get() = isPdfX

    /**
     * Checks if the PDF/X Conformance is necessary.
     * @return true if the PDF has to be in conformance with any of the PDF/X specifications
     */
    override val isPdfX: Boolean
        get() = pdfxConformance != PdfWriter.PDFXNONE
    /**
     * Checks if the PDF has to be in conformance with PDF/X-1a:2001
     * @return true of the PDF has to be in conformance with PDF/X-1a:2001
     */
    val isPdfX1A2001: Boolean
        get() = pdfxConformance == PdfWriter.PDFX1A2001
    /**
     * Checks if the PDF has to be in conformance with PDF/X-3:2002
     * @return true of the PDF has to be in conformance with PDF/X-3:2002
     */
    val isPdfX32002: Boolean
        get() = pdfxConformance == PdfWriter.PDFX32002

    /**
     * Business logic that checks if a certain object is in conformance with PDF/X.
     * @param key        the type of PDF ISO conformance that has to be checked
     * *
     * @param obj1        the object that is checked for conformance
     */
    override fun checkPdfIsoConformance(key: Int, obj1: Any) {
        if (writer == null || !writer!!.isPdfX)
            return
        val conf = writer!!.pdfxConformance
        when (key) {
            PdfIsoKeys.PDFISOKEY_COLOR -> when (conf) {
                PdfWriter.PDFX1A2001 -> if (obj1 is ExtendedColor) {
                    when (obj1.type) {
                        ExtendedColor.TYPE_CMYK, ExtendedColor.TYPE_GRAY -> return
                        ExtendedColor.TYPE_RGB -> throw PdfXConformanceException(MessageLocalization.getComposedMessage("colorspace.rgb.is.not.allowed"))
                        ExtendedColor.TYPE_SEPARATION -> {
                            val sc = obj1 as SpotColor
                            checkPdfIsoConformance(PdfIsoKeys.PDFISOKEY_COLOR, sc.pdfSpotColor.alternativeCS)
                        }
                        ExtendedColor.TYPE_SHADING -> {
                            val xc = obj1 as ShadingColor
                            checkPdfIsoConformance(PdfIsoKeys.PDFISOKEY_COLOR, xc.pdfShadingPattern.shading.colorSpace)
                        }
                        ExtendedColor.TYPE_PATTERN -> {
                            val pc = obj1 as PatternColor
                            checkPdfIsoConformance(PdfIsoKeys.PDFISOKEY_COLOR, pc.painter.defaultColor)
                        }
                    }
                } else if (obj1 is BaseColor)
                    throw PdfXConformanceException(MessageLocalization.getComposedMessage("colorspace.rgb.is.not.allowed"))
            }
            PdfIsoKeys.PDFISOKEY_CMYK -> {
            }
            PdfIsoKeys.PDFISOKEY_RGB -> if (conf == PdfWriter.PDFX1A2001)
                throw PdfXConformanceException(MessageLocalization.getComposedMessage("colorspace.rgb.is.not.allowed"))
            PdfIsoKeys.PDFISOKEY_FONT -> if (!(obj1 as BaseFont).isEmbedded)
                throw PdfXConformanceException(MessageLocalization.getComposedMessage("all.the.fonts.must.be.embedded.this.one.isn.t.1", obj1.postscriptFontName))
            PdfIsoKeys.PDFISOKEY_IMAGE -> {
                val image = obj1 as PdfImage
                if (image.get(PdfName.SMASK) != null)
                    throw PdfXConformanceException(MessageLocalization.getComposedMessage("the.smask.key.is.not.allowed.in.images"))
                when (conf) {
                    PdfWriter.PDFX1A2001 -> {
                        val cs = image.get(PdfName.COLORSPACE) ?: return
                        if (cs.isName) {
                            if (PdfName.DEVICERGB == cs)
                                throw PdfXConformanceException(MessageLocalization.getComposedMessage("colorspace.rgb.is.not.allowed"))
                        } else if (cs.isArray) {
                            if (PdfName.CALRGB == (cs as PdfArray).getPdfObject(0))
                                throw PdfXConformanceException(MessageLocalization.getComposedMessage("colorspace.calrgb.is.not.allowed"))
                        }
                    }
                }
            }
            PdfIsoKeys.PDFISOKEY_GSTATE -> {
                val gs = obj1 as PdfDictionary ?: break
                // The example PdfXPdfA threw a NullPointerException because gs was null
                var obj: PdfObject? = gs.get(PdfName.BM)
                if (obj != null && PdfGState.BM_NORMAL != obj && PdfGState.BM_COMPATIBLE != obj)
                    throw PdfXConformanceException(MessageLocalization.getComposedMessage("blend.mode.1.not.allowed", obj.toString()))
                obj = gs.get(PdfName.CA)
                var v = 0.0
                if (obj != null && (v = (obj as PdfNumber).doubleValue()) != 1.0)
                    throw PdfXConformanceException(MessageLocalization.getComposedMessage("transparency.is.not.allowed.ca.eq.1", v.toString()))
                obj = gs.get(PdfName.ca)
                v = 0.0
                if (obj != null && (v = (obj as PdfNumber).doubleValue()) != 1.0)
                    throw PdfXConformanceException(MessageLocalization.getComposedMessage("transparency.is.not.allowed.ca.eq.1", v.toString()))
            }
            PdfIsoKeys.PDFISOKEY_LAYER -> throw PdfXConformanceException(MessageLocalization.getComposedMessage("layers.are.not.allowed"))
        }
    }
}
