/*
 * $Id: 31186532f52feaabc4ed911bd28ebc4880bede11 $
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

import com.itextpdf.text.error_messages.MessageLocalization

import com.itextpdf.text.Rectangle
import com.itextpdf.text.BaseColor

/** This class takes 2 barcodes, an EAN/UPC and a supplemental
 * and creates a single barcode with both combined in the
 * expected layout. The UPC/EAN should have a positive text
 * baseline and the supplemental a negative one (in the supplemental
 * the text is on the top of the barcode.
 *
 *
 * The default parameters are:
 *
 * n = 8; // horizontal distance between the two barcodes
 *

 * @author Paulo Soares
 */
class BarcodeEANSUPP
/** Creates new combined barcode.
 * @param ean the EAN/UPC barcode
 * *
 * @param supp the supplemental barcode
 */
(
        /** The barcode with the EAN/UPC.
         */
        protected var ean: Barcode,
        /** The barcode with the supplemental.
         */
        protected var supp: Barcode) : Barcode() {

    init {
        n = 8f // horizontal distance between the two barcodes
    }

    /** Gets the maximum area that the barcode and the text, if
     * any, will occupy. The lower left corner is always (0, 0).
     * @return the size the barcode occupies.
     */
    override val barcodeSize: Rectangle
        get() {
            val rect = ean.barcodeSize
            rect.right = rect.width + supp.barcodeSize.width + n
            return rect
        }

    /** Places the barcode in a PdfContentByte. The
     * barcode is always placed at coordinates (0, 0). Use the
     * translation matrix to move it elsewhere.
     *
     *
     * The bars and text are written in the following colors:
     *
     *
     *
     *
     * barColor
     * textColor
     * Result
     *
     *
     * null
     * null
     * bars and text painted with current fill color
     *
     *
     * barColor
     * null
     * bars and text painted with barColor
     *
     *
     * null
     * textColor
     * bars painted with current colortext painted with textColor
     *
     *
     * barColor
     * textColor
     * bars painted with barColortext painted with textColor
     *
     *
     * @param cb the PdfContentByte where the barcode will be placed
     * *
     * @param barColor the color of the bars. It can be null
     * *
     * @param textColor the color of the text. It can be null
     * *
     * @return the dimensions the barcode occupies
     */
    override fun placeBarcode(cb: PdfContentByte, barColor: BaseColor, textColor: BaseColor): Rectangle {
        if (supp.font != null)
            supp.barHeight = ean.barHeight + supp.baseline - supp.font.getFontDescriptor(BaseFont.CAPHEIGHT, supp.size)
        else
            supp.barHeight = ean.barHeight
        val eanR = ean.barcodeSize
        cb.saveState()
        ean.placeBarcode(cb, barColor, textColor)
        cb.restoreState()
        cb.saveState()
        cb.concatCTM(1f, 0f, 0f, 1f, eanR.width + n, eanR.height - ean.barHeight)
        supp.placeBarcode(cb, barColor, textColor)
        cb.restoreState()
        return barcodeSize
    }

    // AWT related methods (remove this if you port to Android / GAE)

    /** Creates a java.awt.Image. This image only
     * contains the bars without any text.
     * @param foreground the color of the bars
     * *
     * @param background the color of the background
     * *
     * @return the image
     */
    override fun createAwtImage(foreground: java.awt.Color, background: java.awt.Color): java.awt.Image {
        throw UnsupportedOperationException(MessageLocalization.getComposedMessage("the.two.barcodes.must.be.composed.externally"))
    }
}
