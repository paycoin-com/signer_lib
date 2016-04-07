/*
 * $Id: 0435affbdd6d6d245497c8cb691ea8d6d5c22cce $
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

class PdfTransition
/**
 * Constructs a Transition.

 * @param  type      type of the transition effect
 * @param  duration  duration of the transition effect
 */
@JvmOverloads constructor(type: Int = PdfTransition.BLINDH, duration: Int = 1) {

    /**
     * duration of the transition effect
     */
    var duration: Int = 0
        protected set
    /**
     * type of the transition effect
     */
    var type: Int = 0
        protected set

    init {
        this.duration = duration
        this.type = type
    }

    val transitionDictionary: PdfDictionary
        get() {
            val trans = PdfDictionary(PdfName.TRANS)
            when (type) {
                SPLITVOUT -> {
                    trans.put(PdfName.S, PdfName.SPLIT)
                    trans.put(PdfName.D, PdfNumber(duration))
                    trans.put(PdfName.DM, PdfName.V)
                    trans.put(PdfName.M, PdfName.O)
                }
                SPLITHOUT -> {
                    trans.put(PdfName.S, PdfName.SPLIT)
                    trans.put(PdfName.D, PdfNumber(duration))
                    trans.put(PdfName.DM, PdfName.H)
                    trans.put(PdfName.M, PdfName.O)
                }
                SPLITVIN -> {
                    trans.put(PdfName.S, PdfName.SPLIT)
                    trans.put(PdfName.D, PdfNumber(duration))
                    trans.put(PdfName.DM, PdfName.V)
                    trans.put(PdfName.M, PdfName.I)
                }
                SPLITHIN -> {
                    trans.put(PdfName.S, PdfName.SPLIT)
                    trans.put(PdfName.D, PdfNumber(duration))
                    trans.put(PdfName.DM, PdfName.H)
                    trans.put(PdfName.M, PdfName.I)
                }
                BLINDV -> {
                    trans.put(PdfName.S, PdfName.BLINDS)
                    trans.put(PdfName.D, PdfNumber(duration))
                    trans.put(PdfName.DM, PdfName.V)
                }
                BLINDH -> {
                    trans.put(PdfName.S, PdfName.BLINDS)
                    trans.put(PdfName.D, PdfNumber(duration))
                    trans.put(PdfName.DM, PdfName.H)
                }
                INBOX -> {
                    trans.put(PdfName.S, PdfName.BOX)
                    trans.put(PdfName.D, PdfNumber(duration))
                    trans.put(PdfName.M, PdfName.I)
                }
                OUTBOX -> {
                    trans.put(PdfName.S, PdfName.BOX)
                    trans.put(PdfName.D, PdfNumber(duration))
                    trans.put(PdfName.M, PdfName.O)
                }
                LRWIPE -> {
                    trans.put(PdfName.S, PdfName.WIPE)
                    trans.put(PdfName.D, PdfNumber(duration))
                    trans.put(PdfName.DI, PdfNumber(0))
                }
                RLWIPE -> {
                    trans.put(PdfName.S, PdfName.WIPE)
                    trans.put(PdfName.D, PdfNumber(duration))
                    trans.put(PdfName.DI, PdfNumber(180))
                }
                BTWIPE -> {
                    trans.put(PdfName.S, PdfName.WIPE)
                    trans.put(PdfName.D, PdfNumber(duration))
                    trans.put(PdfName.DI, PdfNumber(90))
                }
                TBWIPE -> {
                    trans.put(PdfName.S, PdfName.WIPE)
                    trans.put(PdfName.D, PdfNumber(duration))
                    trans.put(PdfName.DI, PdfNumber(270))
                }
                DISSOLVE -> {
                    trans.put(PdfName.S, PdfName.DISSOLVE)
                    trans.put(PdfName.D, PdfNumber(duration))
                }
                LRGLITTER -> {
                    trans.put(PdfName.S, PdfName.GLITTER)
                    trans.put(PdfName.D, PdfNumber(duration))
                    trans.put(PdfName.DI, PdfNumber(0))
                }
                TBGLITTER -> {
                    trans.put(PdfName.S, PdfName.GLITTER)
                    trans.put(PdfName.D, PdfNumber(duration))
                    trans.put(PdfName.DI, PdfNumber(270))
                }
                DGLITTER -> {
                    trans.put(PdfName.S, PdfName.GLITTER)
                    trans.put(PdfName.D, PdfNumber(duration))
                    trans.put(PdfName.DI, PdfNumber(315))
                }
            }
            return trans
        }

    companion object {
        /**
         * Out Vertical Split
         */
        val SPLITVOUT = 1
        /**
         * Out Horizontal Split
         */
        val SPLITHOUT = 2
        /**
         * In Vertical Split
         */
        val SPLITVIN = 3
        /**
         * IN Horizontal Split
         */
        val SPLITHIN = 4
        /**
         * Vertical Blinds
         */
        val BLINDV = 5
        /**
         * Vertical Blinds
         */
        val BLINDH = 6
        /**
         * Inward Box
         */
        val INBOX = 7
        /**
         * Outward Box
         */
        val OUTBOX = 8
        /**
         * Left-Right Wipe
         */
        val LRWIPE = 9
        /**
         * Right-Left Wipe
         */
        val RLWIPE = 10
        /**
         * Bottom-Top Wipe
         */
        val BTWIPE = 11
        /**
         * Top-Bottom Wipe
         */
        val TBWIPE = 12
        /**
         * Dissolve
         */
        val DISSOLVE = 13
        /**
         * Left-Right Glitter
         */
        val LRGLITTER = 14
        /**
         * Top-Bottom Glitter
         */
        val TBGLITTER = 15
        /**
         * Diagonal Glitter
         */
        val DGLITTER = 16
    }
}
/**
 * Constructs a Transition.

 */
/**
 * Constructs a Transition.

 * @param  type      type of the transition effect
 */

