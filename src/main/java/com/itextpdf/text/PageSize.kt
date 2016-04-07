/*
 * $Id: 62b80dcf70d6a06295956bf4c388ea1ec148abed $
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
package com.itextpdf.text

import java.lang.reflect.Field
import com.itextpdf.text.error_messages.MessageLocalization

/**
 * The PageSize-object contains a number of rectangles representing the most common paper sizes.

 * @see Rectangle
 */

object PageSize {

    // membervariables

    /** This is the letter format  */
    val LETTER: Rectangle = RectangleReadOnly(612f, 792f)

    /** This is the note format  */
    val NOTE: Rectangle = RectangleReadOnly(540f, 720f)

    /** This is the legal format  */
    val LEGAL: Rectangle = RectangleReadOnly(612f, 1008f)

    /** This is the tabloid format  */
    val TABLOID: Rectangle = RectangleReadOnly(792f, 1224f)

    /** This is the executive format  */
    val EXECUTIVE: Rectangle = RectangleReadOnly(522f, 756f)

    /** This is the postcard format  */
    val POSTCARD: Rectangle = RectangleReadOnly(283f, 416f)

    /** This is the a0 format  */
    val A0: Rectangle = RectangleReadOnly(2384f, 3370f)

    /** This is the a1 format  */
    val A1: Rectangle = RectangleReadOnly(1684f, 2384f)

    /** This is the a2 format  */
    val A2: Rectangle = RectangleReadOnly(1191f, 1684f)

    /** This is the a3 format  */
    val A3: Rectangle = RectangleReadOnly(842f, 1191f)

    /** This is the a4 format  */
    val A4: Rectangle = RectangleReadOnly(595f, 842f)

    /** This is the a5 format  */
    val A5: Rectangle = RectangleReadOnly(420f, 595f)

    /** This is the a6 format  */
    val A6: Rectangle = RectangleReadOnly(297f, 420f)

    /** This is the a7 format  */
    val A7: Rectangle = RectangleReadOnly(210f, 297f)

    /** This is the a8 format  */
    val A8: Rectangle = RectangleReadOnly(148f, 210f)

    /** This is the a9 format  */
    val A9: Rectangle = RectangleReadOnly(105f, 148f)

    /** This is the a10 format  */
    val A10: Rectangle = RectangleReadOnly(73f, 105f)

    /** This is the b0 format  */
    val B0: Rectangle = RectangleReadOnly(2834f, 4008f)

    /** This is the b1 format  */
    val B1: Rectangle = RectangleReadOnly(2004f, 2834f)

    /** This is the b2 format  */
    val B2: Rectangle = RectangleReadOnly(1417f, 2004f)

    /** This is the b3 format  */
    val B3: Rectangle = RectangleReadOnly(1000f, 1417f)

    /** This is the b4 format  */
    val B4: Rectangle = RectangleReadOnly(708f, 1000f)

    /** This is the b5 format  */
    val B5: Rectangle = RectangleReadOnly(498f, 708f)

    /** This is the b6 format  */
    val B6: Rectangle = RectangleReadOnly(354f, 498f)

    /** This is the b7 format  */
    val B7: Rectangle = RectangleReadOnly(249f, 354f)

    /** This is the b8 format  */
    val B8: Rectangle = RectangleReadOnly(175f, 249f)

    /** This is the b9 format  */
    val B9: Rectangle = RectangleReadOnly(124f, 175f)

    /** This is the b10 format  */
    val B10: Rectangle = RectangleReadOnly(87f, 124f)

    /** This is the archE format  */
    val ARCH_E: Rectangle = RectangleReadOnly(2592f, 3456f)

    /** This is the archD format  */
    val ARCH_D: Rectangle = RectangleReadOnly(1728f, 2592f)

    /** This is the archC format  */
    val ARCH_C: Rectangle = RectangleReadOnly(1296f, 1728f)

    /** This is the archB format  */
    val ARCH_B: Rectangle = RectangleReadOnly(864f, 1296f)

    /** This is the archA format  */
    val ARCH_A: Rectangle = RectangleReadOnly(648f, 864f)

    /** This is the American Foolscap format  */
    val FLSA: Rectangle = RectangleReadOnly(612f, 936f)

    /** This is the European Foolscap format  */
    val FLSE: Rectangle = RectangleReadOnly(648f, 936f)

    /** This is the halfletter format  */
    val HALFLETTER: Rectangle = RectangleReadOnly(396f, 612f)

    /** This is the 11x17 format  */
    val _11X17: Rectangle = RectangleReadOnly(792f, 1224f)

    /** This is the ISO 7810 ID-1 format (85.60 x 53.98 mm or 3.370 x 2.125 inch)  */
    val ID_1: Rectangle = RectangleReadOnly(242.65f, 153f)

    /** This is the ISO 7810 ID-2 format (A7 rotated)  */
    val ID_2: Rectangle = RectangleReadOnly(297f, 210f)

    /** This is the ISO 7810 ID-3 format (B7 rotated)  */
    val ID_3: Rectangle = RectangleReadOnly(354f, 249f)

    /** This is the ledger format  */
    val LEDGER: Rectangle = RectangleReadOnly(1224f, 792f)

    /** This is the Crown Quarto format  */
    val CROWN_QUARTO: Rectangle = RectangleReadOnly(535f, 697f)

    /** This is the Large Crown Quarto format  */
    val LARGE_CROWN_QUARTO: Rectangle = RectangleReadOnly(569f, 731f)

    /** This is the Demy Quarto format.  */
    val DEMY_QUARTO: Rectangle = RectangleReadOnly(620f, 782f)

    /** This is the Royal Quarto format.  */
    val ROYAL_QUARTO: Rectangle = RectangleReadOnly(671f, 884f)

    /** This is the Crown Octavo format  */
    val CROWN_OCTAVO: Rectangle = RectangleReadOnly(348f, 527f)

    /** This is the Large Crown Octavo format  */
    val LARGE_CROWN_OCTAVO: Rectangle = RectangleReadOnly(365f, 561f)

    /** This is the Demy Octavo format  */
    val DEMY_OCTAVO: Rectangle = RectangleReadOnly(391f, 612f)

    /** This is the Royal Octavo format.  */
    val ROYAL_OCTAVO: Rectangle = RectangleReadOnly(442f, 663f)

    /** This is the small paperback format.  */
    val SMALL_PAPERBACK: Rectangle = RectangleReadOnly(314f, 504f)

    /** This is the Pengiun small paperback format.  */
    val PENGUIN_SMALL_PAPERBACK: Rectangle = RectangleReadOnly(314f, 513f)

    /** This is the Penguin large paperback format.  */
    val PENGUIN_LARGE_PAPERBACK: Rectangle = RectangleReadOnly(365f, 561f)

    // Some extra shortcut values for pages in Landscape

    /**
     * This is the letter format
     * @since iText 5.0.6
     * *
     */
    @Deprecated("")
    val LETTER_LANDSCAPE: Rectangle = RectangleReadOnly(612f, 792f, 90)

    /**
     * This is the legal format
     * @since iText 5.0.6
     * *
     */
    @Deprecated("")
    val LEGAL_LANDSCAPE: Rectangle = RectangleReadOnly(612f, 1008f, 90)

    /**
     * This is the a4 format
     * @since iText 5.0.6
     * *
     */
    @Deprecated("")
    val A4_LANDSCAPE: Rectangle = RectangleReadOnly(595f, 842f, 90)


    /**
     * This method returns a Rectangle based on a String.
     * Possible values are the the names of a constant in this class
     * (for instance "A4", "LETTER",...) or a value like "595 842"
     * @param name the name as defined by the constants of PageSize or a numeric pair string
     * *
     * @return the rectangle
     */
    fun getRectangle(name: String): Rectangle {
        var name = name
        name = name.trim { it <= ' ' }.toUpperCase()
        val pos = name.indexOf(' ')
        if (pos == -1) {
            try {
                val field = PageSize::class.java.getDeclaredField(name.toUpperCase())
                return field.get(null) as Rectangle
            } catch (e: Exception) {
                throw RuntimeException(MessageLocalization.getComposedMessage("can.t.find.page.size.1", name))
            }

        } else {
            try {
                val width = name.substring(0, pos)
                val height = name.substring(pos + 1)
                return Rectangle(java.lang.Float.parseFloat(width), java.lang.Float.parseFloat(height))
            } catch (e: Exception) {
                throw RuntimeException(MessageLocalization.getComposedMessage("1.is.not.a.valid.page.size.format.2", name, e.message))
            }

        }
    }
}
