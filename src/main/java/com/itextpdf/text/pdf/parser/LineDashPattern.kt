/*
 * $Id: a24c60e63c4300bc8c563d39f64b66617f9c9f9b $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Pavel Mitrofanov, Bruno Lowagie, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General License for more
 * details. You should have received a copy of the GNU Affero General License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General License.
 *
 * In accordance with Section 7(b) of the GNU Affero General License, a covered
 * work must retain the producer line in every PDF that is created or
 * manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing a
 * commercial license. Buying such a license is mandatory as soon as you develop
 * commercial activities involving the iText software without disclosing the
 * source code of your own applications. These activities include: offering paid
 * services to customers as an ASP, serving PDFs on the fly in a web
 * application, shipping iText with a closed source product.
 *
 * For more information, please contact iText Software Corp. at this address:
 * sales@itextpdf.com
 */
package com.itextpdf.text.pdf.parser

import com.itextpdf.text.pdf.PdfArray

/**
 * Represents the line dash pattern. The line dash pattern shall control the pattern
 * of dashes and gaps used to stroke paths. It shall be specified by a dash array and
 * a dash phase.

 * @since 5.5.6
 */
class LineDashPattern
/**
 * Creates new [LineDashPattern] object.
 * @param dashArray The dash array. See [.getDashArray]
 * *
 * @param dashPhase The dash phase. See [.getDashPhase]
 */
(dashArray: PdfArray,
        /**
         * Getter for the dash phase.

         * The dash phase shall specify the distance into the dash pattern at which
         * to start the dash. The elements are expressed in user space units.

         * @return The dash phase.
         */
 /**
  * Setter for the dash phase. See [.getDashArray]
  * @param dashPhase New dash phase.
  */
 var dashPhase: Float) {

    /**
     * Getter for the dash array.

     * The dash array’s elements is number that specify the lengths of
     * alternating dashes and gaps; the numbers are nonnegative. The
     * elements are expressed in user space units.

     * @return The dash array.
     */
    /**
     * Setter for the dash array. See [.getDashArray]
     * @param dashArray New dash array.
     */
    var dashArray: PdfArray? = null

    private var currentIndex: Int = 0
    private var elemOrdinalNumber = 1
    private var currentElem: DashArrayElem? = null

    init {
        this.dashArray = PdfArray(dashArray)
        initFirst(dashPhase)
    }

    /**
     * Calculates and returns the next element which is either gap or dash.
     * @return The next dash array's element.
     */
    operator fun next(): DashArrayElem {
        val ret = currentElem

        if (dashArray!!.size() > 0) {
            currentIndex = (currentIndex + 1) % dashArray!!.size()
            currentElem = DashArrayElem(dashArray!!.getAsNumber(currentIndex).floatValue(),
                    isEven(++elemOrdinalNumber))
        }

        return ret
    }

    /**
     * Resets the dash array so that the [.next] method will start
     * from the beginning of the dash array.
     */
    fun reset() {
        currentIndex = 0
        elemOrdinalNumber = 1
        initFirst(dashPhase)
    }

    /**
     * Checks whether the dashed pattern is solid or not. It's solid when the
     * size of a dash array is even and sum of all the units off in the array
     * is 0.
     * For example: [3 0 4 0 5 0 6 0] (sum is 0), [3 0 4 0 5 1] (sum is 1).
     */
    val isSolid: Boolean
        get() {
            if (dashArray!!.size() % 2 != 0) {
                return false
            }

            var unitsOffSum = 0f

            var i = 1
            while (i < dashArray!!.size()) {
                unitsOffSum += dashArray!!.getAsNumber(i).floatValue()
                i += 2
            }

            return java.lang.Float.compare(unitsOffSum, 0f) == 0
        }

    private fun initFirst(phase: Float) {
        var phase = phase
        if (dashArray!!.size() > 0) {
            while (phase > 0) {
                phase -= dashArray!!.getAsNumber(currentIndex).floatValue()
                currentIndex = (currentIndex + 1) % dashArray!!.size()
                elemOrdinalNumber++
            }

            if (phase < 0) {
                --elemOrdinalNumber
                --currentIndex
                currentElem = DashArrayElem(-phase, isEven(elemOrdinalNumber))
            } else {
                currentElem = DashArrayElem(dashArray!!.getAsNumber(currentIndex).floatValue(),
                        isEven(elemOrdinalNumber))
            }
        }
    }

    private fun isEven(num: Int): Boolean {
        return num % 2 == 0
    }

    inner class DashArrayElem(var `val`: Float, var isGap: Boolean)
}
