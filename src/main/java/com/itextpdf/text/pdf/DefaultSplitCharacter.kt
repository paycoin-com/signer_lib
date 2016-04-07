/*
 * $Id: 9ae46e5fc0ece0feb35fee1c11a4c7a2d6abca87 $
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

import com.itextpdf.text.SplitCharacter

/**
 *
 *
 * The default class that is used to determine whether or not a character
 * is a split character.
 *
 *
 * You can add an array of characters or a single character on which iText
 * should split the chunk. If custom characters have been set, iText will ignore
 * the default characters this class uses to split chunks.

 * @since 2.1.2
 */
class DefaultSplitCharacter : SplitCharacter {

    protected var characters: CharArray? = null

    /**
     * Default constructor, has no custom characters to check.
     */
    constructor() {
        // empty body
    }

    /**
     * Constructor with one splittable character.

     * @param character char
     */
    constructor(character: Char) : this(charArrayOf(character)) {
    }

    /**
     * Constructor with an array of splittable characters

     * @param characters char[]
     */
    constructor(characters: CharArray) {
        this.characters = characters
    }

    /**
     *
     *
     * Checks if a character can be used to split a PdfString.
     *
     *
     *
     *
     * The default behavior is that every character less than or equal to SPACE, the character '-'
     * and some specific unicode ranges are 'splitCharacters'.
     *
     *
     *
     * If custom splittable characters are set using the specified constructors,
     * then this class will ignore the default characters described in the
     * previous paragraph.
     *

     * @param start   start position in the array
     * *
     * @param current current position in the array
     * *
     * @param end     end position in the array
     * *
     * @param ck      chunk array
     * *
     * @param cc      the character array that has to be checked
     * *
     * @return true if the character can be used to split a string, false otherwise
     */
    override fun isSplitCharacter(start: Int, current: Int, end: Int, cc: CharArray, ck: Array<PdfChunk>): Boolean {
        val c = getCurrentCharacter(current, cc, ck)

        if (characters != null) {
            for (i in characters!!.indices) {
                if (c == characters!![i]) {
                    return true
                }
            }
            return false
        }

        if (c <= ' ' || c == '-' || c == '\u2010') {
            return true
        }
        if (c.toInt() < 0x2002)
            return false
        return c.toInt() >= 0x2002 && c.toInt() <= 0x200b
                || c.toInt() >= 0x2e80 && c.toInt() < 0xd7a0
                || c.toInt() >= 0xf900 && c.toInt() < 0xfb00
                || c.toInt() >= 0xfe30 && c.toInt() < 0xfe50
                || c.toInt() >= 0xff61 && c.toInt() < 0xffa0
    }

    /**
     * Returns the current character

     * @param current current position in the array
     * *
     * @param ck      chunk array
     * *
     * @param cc      the character array that has to be checked
     * *
     * @return the current character
     */
    protected fun getCurrentCharacter(current: Int, cc: CharArray, ck: Array<PdfChunk>?): Char {
        if (ck == null) {
            return cc[current]
        }
        return ck[Math.min(current, ck.size - 1)].getUnicodeEquivalent(cc[current].toInt()).toChar()
    }

    companion object {

        /**
         * An instance of the default SplitCharacter.
         */
        val DEFAULT: SplitCharacter = DefaultSplitCharacter()
    }
}
