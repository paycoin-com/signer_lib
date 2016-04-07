/*
 * $Id: 070b3ee323294947025ef9717c25d74a5371974e $
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

import com.itextpdf.text.api.Indentable
import com.itextpdf.text.factories.RomanAlphabetFactory
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.interfaces.IAccessibleElement

import java.util.ArrayList
import java.util.HashMap

/**
 * A List contains several ListItems.
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

 * @see ListItem
 */

open class List : TextElementArray, Indentable, IAccessibleElement {

    // member variables

    /** This is the ArrayList containing the different ListItems.  */
    // methods to retrieve information

    /**
     * Gets all the items in the list.

     * @return    an ArrayList containing ListItems.
     */
    var items = ArrayList<Element>()
        protected set

    /** Indicates if the list has to be numbered.  */
    // getters

    /**
     * Checks if the list is numbered.
     * @return    true if the list is numbered, false otherwise.
     */

    // setters

    /**
     * @param numbered the numbered to set
     */
    var isNumbered = false
    /** Indicates if the listsymbols are numerical or alphabetical.  */
    /**
     * Checks if the list is lettered.
     * @return  true if the list is lettered, false otherwise.
     */
    /**
     * @param lettered the lettered to set
     */
    var isLettered = false
    /** Indicates if the listsymbols are lowercase or uppercase.  */
    /**
     * Checks if the list lettering is lowercase.
     * @return  true if it is lowercase, false otherwise.
     */
    /**
     * @param uppercase the uppercase to set
     */
    var isLowercase = false
    /** Indicates if the indentation has to be set automatically.  */
    /**
     * Checks if the indentation of list items is done automatically.
     * @return the autoindent
     */
    /**
     * @param autoindent the autoindent to set
     */
    var isAutoindent = false
    /** Indicates if the indentation of all the items has to be aligned.  */
    /**
     * Checks if all the listitems should be aligned.
     * @return the alignindent
     */
    /**
     * @param alignindent the alignindent to set
     */
    var isAlignindent = false

    /** This variable indicates the first number of a numbered list.  */
    /**
     * Gets the first number        .
     * @return a number
     */
    /**
     * Sets the number that has to come first in the list.

     * @param    first        a number
     */
    var first = 1
    /** This is the listsymbol of a list that is not numbered.  */
    /**
     * Gets the Chunk containing the symbol.
     * @return a Chunk with a symbol
     */
    var symbol = Chunk("- ")
        protected set
    /**
     * In case you are using numbered/lettered lists, this String is added before the number/letter.
     * @since    iText 2.1.1
     */
    /**
     * Returns the String that is before a number or letter in the list symbol.
     * @return    the String that is before a number or letter in the list symbol
     * *
     * @since    iText 2.1.1
     */
    /**
     * Sets the String that has to be added before a number or letter in the list symbol.
     * @since    iText 2.1.1
     * *
     * @param    preSymbol the String that has to be added before a number or letter in the list symbol.
     */
    var preSymbol = ""
    /**
     * In case you are using numbered/lettered lists, this String is added after the number/letter.
     * @since    iText 2.1.1
     */
    /**
     * Returns the String that is after a number or letter in the list symbol.
     * @return    the String that is after a number or letter in the list symbol
     * *
     * @since    iText 2.1.1
     */
    /**
     * Sets the String that has to be added after a number or letter in the list symbol.
     * @since    iText 2.1.1
     * *
     * @param    postSymbol the String that has to be added after a number or letter in the list symbol.
     */
    var postSymbol = ". "

    /** The indentation of this list on the left side.  */
    /**
     * Gets the indentation of this paragraph on the left side.
     * @return    the indentation
     */
    /**
     * Sets the indentation of this paragraph on the left side.

     * @param    indentation        the new indentation
     */
    override var indentationLeft = 0f
    /** The indentation of this list on the right side.  */
    /**
     * Gets the indentation of this paragraph on the right side.
     * @return    the indentation
     */
    /**
     * Sets the indentation of this paragraph on the right side.

     * @param    indentation        the new indentation
     */
    override var indentationRight = 0f
    /** The indentation of the listitems.  */
    /**
     * Gets the symbol indentation.
     * @return the symbol indentation
     */
    /**
     * @param symbolIndent the symbolIndent to set
     */
    var symbolIndent = 0f

    override var role = PdfName.L
    override var accessibleAttributes: HashMap<PdfName, PdfObject>? = null
        protected set(value: HashMap<PdfName, PdfObject>?) {
            super.accessibleAttributes = value
        }
    override var id: AccessibleElementId? = null
        get() {
            if (id == null)
                id = AccessibleElementId()
            return id
        }

    /**
     * Constructs a List with a specific symbol indentation.
     * @param    symbolIndent    the symbol indentation
     * *
     * @since    iText 2.0.8
     */
    constructor(symbolIndent: Float) {
        this.symbolIndent = symbolIndent
    }

    /**
     * Constructs a List.
     * @param    numbered        a boolean
     * *
     * @param lettered has the list to be 'numbered' with letters
     */
    @JvmOverloads constructor(numbered: Boolean = false, lettered: Boolean = false) {
        this.isNumbered = numbered
        this.isLettered = lettered
        this.isAutoindent = true
        this.isAlignindent = true
    }

    /**
     * Constructs a List.
     *
     * Remark: the parameter symbolIndent is important for instance when
     * generating PDF-documents; it indicates the indentation of the listsymbol.
     * It is not important for HTML-documents.

     * @param    numbered        a boolean
     * *
     * @param    symbolIndent    the indentation that has to be used for the listsymbol
     */
    constructor(numbered: Boolean, symbolIndent: Float) : this(numbered, false, symbolIndent) {
    }

    /**
     * Creates a list
     * @param numbered has the list to be numbered?
     * *
     * @param lettered has the list to be 'numbered' with letters
     * *
     * @param symbolIndent the indentation of the symbol
     */
    constructor(numbered: Boolean, lettered: Boolean, symbolIndent: Float) {
        this.isNumbered = numbered
        this.isLettered = lettered
        this.symbolIndent = symbolIndent
    }

    // implementation of the Element-methods

    /**
     * Processes the element by adding it (or the different parts) to an
     * ElementListener.

     * @param    listener    an ElementListener
     * *
     * @return    true if the element was processed successfully
     */
    override fun process(listener: ElementListener): Boolean {
        try {
            for (element in items) {
                listener.add(element)
            }
            return true
        } catch (de: DocumentException) {
            return false
        }

    }

    /**
     * Gets the type of the text element.

     * @return    a type
     */
    override fun type(): Int {
        return Element.LIST
    }

    /**
     * Gets all the chunks in this element.

     * @return    an ArrayList
     */
    override val chunks: List<Chunk>
        get() {
            val tmp = ArrayList<Chunk>()
            for (element in items) {
                tmp.addAll(element.chunks)
            }
            return tmp
        }

    // methods to set the membervariables

    /**
     * Adds a String to the List.

     * @param   s               the element to add.
     * *
     * @return true if adding the object succeeded
     * *
     * @since 5.0.1
     */
    fun add(s: String?): Boolean {
        if (s != null) {
            return this.add(ListItem(s))
        }
        return false
    }

    /**
     * Adds an Element to the List.

     * @param    o        the element to add.
     * *
     * @return true if adding the object succeeded
     * *
     * @since 5.0.1 (signature changed to use Element)
     */
    override fun add(o: Element): Boolean {
        if (o is ListItem) {
            if (isNumbered || isLettered) {
                val chunk = Chunk(preSymbol, symbol.font)
                chunk.attributes = symbol.attributes
                val index = first + items.size
                if (isLettered)
                    chunk.append(RomanAlphabetFactory.getString(index, isLowercase))
                else
                    chunk.append(index.toString())
                chunk.append(postSymbol)
                o.listSymbol = chunk
            } else {
                o.listSymbol = symbol
            }
            o.setIndentationLeft(symbolIndent, isAutoindent)
            o.indentationRight = 0
            return items.add(o)
        } else if (o is List) {
            o.indentationLeft = o.indentationLeft + symbolIndent
            first--
            return items.add(o)
        }
        return false
    }

    open fun cloneShallow(): List {
        val clone = List()
        populateProperties(clone)
        return clone
    }

    protected fun populateProperties(clone: List) {
        clone.indentationLeft = indentationLeft
        clone.indentationRight = indentationRight
        clone.isAutoindent = isAutoindent
        clone.isAlignindent = isAlignindent
        clone.symbolIndent = symbolIndent
        clone.symbol = symbol
    }

    // extra methods

    /** Makes sure all the items in the list have the same indentation.  */
    fun normalizeIndentation() {
        var max = 0f
        for (o in items) {
            if (o is ListItem) {
                max = Math.max(max, o.indentationLeft)
            }
        }
        for (o in items) {
            if (o is ListItem) {
                o.indentationLeft = max
            }
        }
    }

    /**
     * Sets the listsymbol.

     * @param    symbol        a Chunk
     */
    fun setListSymbol(symbol: Chunk) {
        this.symbol = symbol
    }

    /**
     * Sets the listsymbol.
     *
     * This is a shortcut for setListSymbol(Chunk symbol).

     * @param    symbol        a String
     */
    fun setListSymbol(symbol: String) {
        this.symbol = Chunk(symbol)
    }

    /**
     * Gets the size of the list.

     * @return    a size
     */
    fun size(): Int {
        return items.size
    }

    /**
     * Returns true if the list is empty.

     * @return true if the list is empty
     */
    val isEmpty: Boolean
        get() = items.isEmpty()

    /**
     * Gets the leading of the first listitem.

     * @return    a leading
     */
    val totalLeading: Float
        get() {
            if (items.size < 1) {
                return -1f
            }
            val item = items[0] as ListItem
            return item.totalLeading
        }
    /**
     * @see com.itextpdf.text.Element.isContent
     * @since    iText 2.0.8
     */
    override val isContent: Boolean
        get() = true

    /**
     * @see com.itextpdf.text.Element.isNestable
     * @since    iText 2.0.8
     */
    override val isNestable: Boolean
        get() = true

    val firstItem: ListItem?
        get() {
            val lastElement = if (items.size > 0) items[0] else null
            if (lastElement != null) {
                if (lastElement is ListItem) {
                    return lastElement
                } else if (lastElement is List) {
                    return lastElement.firstItem
                }
            }
            return null
        }

    val lastItem: ListItem?
        get() {
            val lastElement = if (items.size > 0) items[items.size - 1] else null
            if (lastElement != null) {
                if (lastElement is ListItem) {
                    return lastElement
                } else if (lastElement is List) {
                    return lastElement.lastItem
                }
            }
            return null
        }

    override fun getAccessibleAttribute(key: PdfName): PdfObject {
        if (accessibleAttributes != null)
            return accessibleAttributes!![key]
        else
            return null
    }

    override fun setAccessibleAttribute(key: PdfName, value: PdfObject) {
        if (accessibleAttributes == null)
            accessibleAttributes = HashMap<PdfName, PdfObject>()
        accessibleAttributes!!.put(key, value)
    }

    override val isInline: Boolean
        get() = false

    companion object {

        // constants

        /** a possible value for the numbered parameter  */
        val ORDERED = true
        /** a possible value for the numbered parameter  */
        val UNORDERED = false
        /** a possible value for the lettered parameter  */
        val NUMERICAL = false
        /** a possible value for the lettered parameter  */
        val ALPHABETICAL = true
        /** a possible value for the lettered parameter  */
        val UPPERCASE = false
        /** a possible value for the lettered parameter  */
        val LOWERCASE = true
    }
}// constructors
/** Constructs a List.  */
/**
 * Constructs a List.
 * @param    numbered        a boolean
 */
