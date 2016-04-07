/*
 * $Id: ac2dcd8a7826c1d422ed1ed5e9157a4b2562a028 $
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

import com.itextpdf.text.pdf.internal.PdfIsoKeys

import java.io.IOException
import java.io.OutputStream

/** The graphic state dictionary.

 * @author Paulo Soares
 */
class PdfGState : PdfDictionary() {

    /**
     * Sets the flag whether to apply overprint for stroking.
     * @param op
     */
    fun setOverPrintStroking(op: Boolean) {
        put(PdfName.OP, if (op) PdfBoolean.PDFTRUE else PdfBoolean.PDFFALSE)
    }

    /**
     * Sets the flag whether to apply overprint for non stroking painting operations.
     * @param op
     */
    fun setOverPrintNonStroking(op: Boolean) {
        put(PdfName.op, if (op) PdfBoolean.PDFTRUE else PdfBoolean.PDFFALSE)
    }

    /**
     * Sets the flag whether to toggle knockout behavior for overprinted objects.
     * @param opm - accepts 0 or 1
     */
    fun setOverPrintMode(opm: Int) {
        put(PdfName.OPM, PdfNumber(if (opm == 0) 0 else 1))
    }

    /**
     * Sets the current stroking alpha constant, specifying the constant shape or
     * constant opacity value to be used for stroking operations in the transparent
     * imaging model.
     * @param ca
     */
    fun setStrokeOpacity(ca: Float) {
        put(PdfName.CA, PdfNumber(ca))
    }

    /**
     * Sets the current stroking alpha constant, specifying the constant shape or
     * constant opacity value to be used for nonstroking operations in the transparent
     * imaging model.
     * @param ca
     */
    fun setFillOpacity(ca: Float) {
        put(PdfName.ca, PdfNumber(ca))
    }

    /**
     * The alpha source flag specifying whether the current soft mask
     * and alpha constant are to be interpreted as shape values (true)
     * or opacity values (false).
     * @param ais
     */
    fun setAlphaIsShape(ais: Boolean) {
        put(PdfName.AIS, if (ais) PdfBoolean.PDFTRUE else PdfBoolean.PDFFALSE)
    }

    /**
     * Determines the behavior of overlapping glyphs within a text object
     * in the transparent imaging model.
     * @param tk
     */
    fun setTextKnockout(tk: Boolean) {
        put(PdfName.TK, if (tk) PdfBoolean.PDFTRUE else PdfBoolean.PDFFALSE)
    }

    /**
     * The current blend mode to be used in the transparent imaging model.
     * @param bm
     */
    fun setBlendMode(bm: PdfName) {
        put(PdfName.BM, bm)
    }

    /**
     * Set the rendering intent, possible values are: PdfName.ABSOLUTECOLORIMETRIC,
     * PdfName.RELATIVECOLORIMETRIC, PdfName.SATURATION, PdfName.PERCEPTUAL.
     * @param ri
     * *
     * @since 5.0.2
     */
    fun setRenderingIntent(ri: PdfName) {
        put(PdfName.RI, ri)
    }

    @Throws(IOException::class)
    override fun toPdf(writer: PdfWriter, os: OutputStream) {
        PdfWriter.checkPdfIsoConformance(writer, PdfIsoKeys.PDFISOKEY_GSTATE, this)
        super.toPdf(writer, os)
    }

    companion object {
        /** A possible blend mode  */
        val BM_NORMAL = PdfName("Normal")
        /** A possible blend mode  */
        val BM_COMPATIBLE = PdfName("Compatible")
        /** A possible blend mode  */
        val BM_MULTIPLY = PdfName("Multiply")
        /** A possible blend mode  */
        val BM_SCREEN = PdfName("Screen")
        /** A possible blend mode  */
        val BM_OVERLAY = PdfName("Overlay")
        /** A possible blend mode  */
        val BM_DARKEN = PdfName("Darken")
        /** A possible blend mode  */
        val BM_LIGHTEN = PdfName("Lighten")
        /** A possible blend mode  */
        val BM_COLORDODGE = PdfName("ColorDodge")
        /** A possible blend mode  */
        val BM_COLORBURN = PdfName("ColorBurn")
        /** A possible blend mode  */
        val BM_HARDLIGHT = PdfName("HardLight")
        /** A possible blend mode  */
        val BM_SOFTLIGHT = PdfName("SoftLight")
        /** A possible blend mode  */
        val BM_DIFFERENCE = PdfName("Difference")
        /** A possible blend mode  */
        val BM_EXCLUSION = PdfName("Exclusion")
    }
}
