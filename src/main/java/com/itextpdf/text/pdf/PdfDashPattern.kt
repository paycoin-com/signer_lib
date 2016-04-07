/*
 * $Id: 035b986c7b6385cc520cb78db2098a8ee6e61f9a $
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
import java.io.OutputStream

/**
 * A PdfDashPattern defines a dash pattern as described in
 * the PDF Reference Manual version 1.3 p 325 (section 8.4.3).

 * @see PdfArray
 */

class PdfDashPattern : PdfArray {

    // membervariables

    /** This is the length of a dash.  */
    private var dash = -1f

    /** This is the length of a gap.  */
    private var gap = -1f

    /** This is the phase.  */
    private val phase = -1f

    // constructors

    /**
     * Constructs a new PdfDashPattern.
     */

    constructor() : super() {
    }

    /**
     * Constructs a new PdfDashPattern.
     */

    constructor(dash: Float) : super(PdfNumber(dash)) {
        this.dash = dash
    }

    /**
     * Constructs a new PdfDashPattern.
     */

    constructor(dash: Float, gap: Float) : super(PdfNumber(dash)) {
        add(PdfNumber(gap))
        this.dash = dash
        this.gap = gap
    }

    /**
     * Constructs a new PdfDashPattern.
     */

    constructor(dash: Float, gap: Float, phase: Float) : super(PdfNumber(dash)) {
        add(PdfNumber(gap))
        this.dash = dash
        this.gap = gap
        this.phase = phase
    }

    fun add(n: Float) {
        add(PdfNumber(n))
    }

    /**
     * Returns the PDF representation of this PdfArray.
     */

    @Throws(IOException::class)
    override fun toPdf(writer: PdfWriter, os: OutputStream) {
        os.write('[')

        if (dash >= 0) {
            PdfNumber(dash).toPdf(writer, os)
            if (gap >= 0) {
                os.write(' ')
                PdfNumber(gap).toPdf(writer, os)
            }
        }
        os.write(']')
        if (phase >= 0) {
            os.write(' ')
            PdfNumber(phase).toPdf(writer, os)
        }
    }
}
