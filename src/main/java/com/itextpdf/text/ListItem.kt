/*
 * $Id: 29b08e9bc5d49f4e11c42e0be22c2602663121f7 $
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

import com.itextpdf.text.pdf.PdfName

/**
 * A ListItem is a Paragraph
 * that can be added to a List.
 *
 * Example 1:
 *
 * List list = new List(true, 20);
 * list.add(new ListItem("First line"));
 * list.add(new ListItem("The second line is longer to see what happens once the end of the line is reached. Will it start on a new line?"));
 * list.add(new ListItem("Third line"));
 *

 * The result of this code looks like this:
 *
 *
 * First line
 *
 *
 * The second line is longer to see what happens once the end of the line is reached. Will it start on a new line?
 *
 *
 * Third line
 *
 *

 * Example 2:
 *
 * List overview = new List(false, 10);
 * overview.add(new ListItem("This is an item"));
 * overview.add("This is another item");
 *

 * The result of this code looks like this:
 *
 *
 * This is an item
 *
 *
 * This is another item
 *
 *

 * @see Element

 * @see List

 * @see Paragraph
 */

class ListItem : Paragraph {

    // member variables

    /**
     * this is the symbol that will precede the listitem.
     * @since    5.0	used to be private
     */
    // methods to retrieve information

    /**
     * Returns the listsymbol.

     * @return    a Chunk
     */
    /**
     * Sets the listsymbol.

     * @param    symbol    a Chunk
     */
    var listSymbol: Chunk? = null
        set(symbol) = if (this.listSymbol == null) {
            this.listSymbol = symbol
            if (this.listSymbol!!.font.isStandardFont) {
                this.listSymbol!!.font = font
            }
        }

    private var listBody: ListBody? = null
    private var listLabel: ListLabel? = null

    // constructors

    /**
     * Constructs a ListItem.
     */
    constructor() : super() {
        role = PdfName.LI
    }

    /**
     * Constructs a ListItem with a certain leading.

     * @param    leading        the leading
     */
    constructor(leading: Float) : super(leading) {
        role = PdfName.LI
    }

    /**
     * Constructs a ListItem with a certain Chunk.

     * @param    chunk        a Chunk
     */
    constructor(chunk: Chunk) : super(chunk) {
        role = PdfName.LI
    }

    /**
     * Constructs a ListItem with a certain String.

     * @param    string        a String
     */
    constructor(string: String) : super(string) {
        role = PdfName.LI
    }

    /**
     * Constructs a ListItem with a certain String
     * and a certain Font.

     * @param    string        a String
     * *
     * @param    font        a String
     */
    constructor(string: String, font: Font) : super(string, font) {
        role = PdfName.LI
    }

    /**
     * Constructs a ListItem with a certain Chunk
     * and a certain leading.

     * @param    leading        the leading
     * *
     * @param    chunk        a Chunk
     */
    constructor(leading: Float, chunk: Chunk) : super(leading, chunk) {
        role = PdfName.LI
    }

    /**
     * Constructs a ListItem with a certain String
     * and a certain leading.

     * @param    leading        the leading
     * *
     * @param    string        a String
     */
    constructor(leading: Float, string: String) : super(leading, string) {
        role = PdfName.LI
    }

    /**
     * Constructs a ListItem with a certain leading, String
     * and Font.

     * @param    leading        the leading
     * *
     * @param    string        a String
     * *
     * @param    font        a Font
     */
    constructor(leading: Float, string: String, font: Font) : super(leading, string, font) {
        role = PdfName.LI
    }

    /**
     * Constructs a ListItem with a certain Phrase.

     * @param    phrase        a Phrase
     */
    constructor(phrase: Phrase) : super(phrase) {
        role = PdfName.LI
    }

    // implementation of the Element-methods

    /**
     * Gets the type of the text element.

     * @return    a type
     */
    override fun type(): Int {
        return Element.LISTITEM
    }

    // methods

    override fun cloneShallow(spacingBefore: Boolean): Paragraph {
        val copy = ListItem()
        populateProperties(copy, spacingBefore)
        return copy
    }

    /**
     * Sets the indentation of this paragraph on the left side.

     * @param    indentation        the new indentation
     * *
     * @param autoindent if set to true, indentation is done automagically, the given indentation float is disregarded.
     */
    fun setIndentationLeft(indentation: Float, autoindent: Boolean) {
        if (autoindent) {
            indentationLeft = listSymbol.widthPoint
        } else {
            indentationLeft = indentation
        }
    }

    /**
     * Changes the font of the list symbol to the font of the first chunk
     * in the list item.
     * @since 5.0.6
     */
    fun adjustListSymbolFont() {
        val cks = chunks
        if (!cks.isEmpty() && listSymbol != null)
            listSymbol!!.font = cks[0].font
    }

    fun getListBody(): ListBody {
        if (listBody == null)
            listBody = ListBody(this)
        return listBody
    }

    fun getListLabel(): ListLabel {
        if (listLabel == null)
            listLabel = ListLabel(this)
        return listLabel
    }

    companion object {

        // constants
        private val serialVersionUID = 1970670787169329006L
    }

}
