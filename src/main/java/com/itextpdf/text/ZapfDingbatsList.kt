/*
 * $Id: 8b99efb4a299f1091246a5456fc57bd07ddd8b1f $
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

/**

 * A special-version of LIST which use zapfdingbats-letters.

 * @see com.itextpdf.text.List

 * @author Michael Niedermair and Bruno Lowagie
 */

class ZapfDingbatsList : List {

    /**
     * char-number in zapfdingbats
     */
    /**
     * get the char-number

     * @return    char-number
     */
    /**
     * set the char-number
     * @param zn a char-number
     */
    var charNumber: Int = 0

    /**
     * Creates a ZapfDingbatsList

     * @param zn a char-number
     */
    constructor(zn: Int) : super(true) {
        this.charNumber = zn
        val fontsize = symbol.font.size
        symbol.font = FontFactory.getFont(FontFactory.ZAPFDINGBATS, fontsize, Font.NORMAL)
        postSymbol = " "
    }

    /**
     * Creates a ZapfDingbatsList

     * @param zn a char-number
     * *
     * @param symbolIndent    indent
     */
    constructor(zn: Int, symbolIndent: Int) : super(true, symbolIndent.toFloat()) {
        this.charNumber = zn
        val fontsize = symbol.font.size
        symbol.font = FontFactory.getFont(FontFactory.ZAPFDINGBATS, fontsize, Font.NORMAL)
        postSymbol = " "
    }

    /**
     * Creates a ZapfDingbatList with a colored symbol

     * @param zn a char-number
     * *
     * @param symbolIndent indent
     * *
     * @param zapfDingbatColor color for the ZpafDingbat
     */
    constructor(zn: Int, symbolIndent: Int, zapfDingbatColor: BaseColor) : super(true, symbolIndent.toFloat()) {
        this.charNumber = zn
        val fontsize = symbol.font.size
        symbol.font = FontFactory.getFont(FontFactory.ZAPFDINGBATS, fontsize, Font.NORMAL, zapfDingbatColor)
        postSymbol = " "
    }

    /**
     * Sets the dingbat's color.

     * @param zapfDingbatColor color for the ZapfDingbat
     */
    fun setDingbatColor(zapfDingbatColor: BaseColor) {
        val fontsize = symbol.font.size
        symbol.font = FontFactory.getFont(FontFactory.ZAPFDINGBATS, fontsize, Font.NORMAL, zapfDingbatColor)
    }

    /**
     * Adds an Element to the List.

     * @param    o    the object to add.
     * *
     * @return true if adding the object succeeded
     */
    override fun add(o: Element): Boolean {
        if (o is ListItem) {
            val chunk = Chunk(preSymbol, symbol.font)
            chunk.attributes = symbol.attributes
            chunk.append(charNumber.toChar().toString())
            chunk.append(postSymbol)
            o.listSymbol = chunk
            o.setIndentationLeft(symbolIndent, isAutoindent)
            o.indentationRight = 0
            items.add(o)
        } else if (o is List) {
            o.indentationLeft = o.indentationLeft + symbolIndent
            first--
            return items.add(o)
        }
        return false
    }

    override fun cloneShallow(): List {
        val clone = ZapfDingbatsList(charNumber)
        populateProperties(clone)
        return clone
    }
}
