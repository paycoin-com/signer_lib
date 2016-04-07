/*
 * $Id: caf48059e2d5ccbe1d1c1384e1e163395894f508 $
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

import java.util.StringTokenizer

/**
 * A PdfDestination is a reference to a location in a PDF file.
 */

class PdfDestination : PdfArray {

    // member variables

    /** Is the indirect reference to a page already added?  */
    private var status = false

    // constructors

    constructor(d: PdfDestination) : super(d) {
        this.status = d.status
    }

    /**
     * Constructs a new PdfDestination.
     *
     * If type equals FITB, the bounding box of a page
     * will fit the window of the Reader. Otherwise the type will be set to
     * FIT so that the entire page will fit to the window.

     * @param        type        The destination type
     */

    constructor(type: Int) : super() {
        if (type == FITB) {
            add(PdfName.FITB)
        } else {
            add(PdfName.FIT)
        }
    }

    /**
     * Constructs a new PdfDestination.
     *
     * If type equals FITBH / FITBV,
     * the width / height of the bounding box of a page will fit the window
     * of the Reader. The parameter will specify the y / x coordinate of the
     * top / left edge of the window. If the type equals FITH
     * or FITV the width / height of the entire page will fit
     * the window and the parameter will specify the y / x coordinate of the
     * top / left edge. In all other cases the type will be set to FITH.

     * @param        type        the destination type
     * *
     * @param        parameter    a parameter to combined with the destination type
     */

    constructor(type: Int, parameter: Float) : super(PdfNumber(parameter)) {
        when (type) {
            else -> addFirst(PdfName.FITH)
            FITV -> addFirst(PdfName.FITV)
            FITBH -> addFirst(PdfName.FITBH)
            FITBV -> addFirst(PdfName.FITBV)
        }
    }

    /** Constructs a new PdfDestination.
     *
     * Display the page, with the coordinates (left, top) positioned
     * at the top-left corner of the window and the contents of the page magnified
     * by the factor zoom. A negative value for any of the parameters left or top, or a
     * zoom value of 0 specifies that the current value of that parameter is to be retained unchanged.
     * @param type must be a PdfDestination.XYZ
     * *
     * @param left the left value. Negative to place a null
     * *
     * @param top the top value. Negative to place a null
     * *
     * @param zoom The zoom factor. A value of 0 keeps the current value
     */

    constructor(type: Int, left: Float, top: Float, zoom: Float) : super(PdfName.XYZ) {
        if (left < 0)
            add(PdfNull.PDFNULL)
        else
            add(PdfNumber(left))
        if (top < 0)
            add(PdfNull.PDFNULL)
        else
            add(PdfNumber(top))
        add(PdfNumber(zoom))
    }

    /** Constructs a new PdfDestination.
     *
     * Display the page, with its contents magnified just enough
     * to fit the rectangle specified by the coordinates left, bottom, right, and top
     * entirely within the window both horizontally and vertically. If the required
     * horizontal and vertical magnification factors are different, use the smaller of
     * the two, centering the rectangle within the window in the other dimension.

     * @param type must be PdfDestination.FITR
     * *
     * @param left a parameter
     * *
     * @param bottom a parameter
     * *
     * @param right a parameter
     * *
     * @param top a parameter
     * *
     * @since iText0.38
     */

    constructor(type: Int, left: Float, bottom: Float, right: Float, top: Float) : super(PdfName.FITR) {
        add(PdfNumber(left))
        add(PdfNumber(bottom))
        add(PdfNumber(right))
        add(PdfNumber(top))
    }

    /**
     * Creates a PdfDestination based on a String.
     * Valid Strings are for instance the values returned by SimpleNamedDestination:
     * "Fit", "XYZ 36 806 0",...
     * @param    dest    a String notation of a destination.
     * *
     * @since    iText 5.0
     */
    constructor(dest: String) : super() {
        val tokens = StringTokenizer(dest)
        if (tokens.hasMoreTokens()) {
            add(PdfName(tokens.nextToken()))
        }
        while (tokens.hasMoreTokens()) {
            val token = tokens.nextToken()
            if ("null" == token)
                add(PdfNull())
            else {
                try {
                    add(PdfNumber(token))
                } catch (e: RuntimeException) {
                    add(PdfNull())
                }

            }
        }
    }

    // methods

    /**
     * Checks if an indirect reference to a page has been added.

     * @return    true or false
     */

    fun hasPage(): Boolean {
        return status
    }

    /** Adds the indirect reference of the destination page.

     * @param page    an indirect reference
     * *
     * @return true if the page reference was added
     */

    fun addPage(page: PdfIndirectReference): Boolean {
        if (!status) {
            addFirst(page)
            status = true
            return true
        }
        return false
    }

    companion object {

        // public static final member-variables

        /** This is a possible destination type  */
        val XYZ = 0

        /** This is a possible destination type  */
        val FIT = 1

        /** This is a possible destination type  */
        val FITH = 2

        /** This is a possible destination type  */
        val FITV = 3

        /** This is a possible destination type  */
        val FITR = 4

        /** This is a possible destination type  */
        val FITB = 5

        /** This is a possible destination type  */
        val FITBH = 6

        /** This is a possible destination type  */
        val FITBV = 7
    }
}
