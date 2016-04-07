/*
 * $Id: f08672190001a735f22d9c35c66207ead57e3f79 $
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

import com.itextpdf.text.Font.FontFamily
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.HyphenationEvent
import com.itextpdf.text.pdf.PdfName

import java.util.ArrayList

/**
 * A Phrase is a series of Chunks.
 *
 * A Phrase has a main Font, but some chunks
 * within the phrase can have a Font that differs from the
 * main Font. All the Chunks in a Phrase
 * have the same leading.
 *
 * Example:
 *
 * // When no parameters are passed, the default leading = 16
 * Phrase phrase0 = new Phrase();
 * Phrase phrase1 = new Phrase("this is a phrase");
 * // In this example the leading is passed as a parameter
 * Phrase phrase2 = new Phrase(16, "this is a phrase with leading 16");
 * // When a Font is passed (explicitly or embedded in a chunk), the default leading = 1.5 * size of the font
 * Phrase phrase3 = new Phrase("this is a phrase with a red, normal font Courier, size 12", FontFactory.getFont(FontFactory.COURIER, 12, Font.NORMAL, new Color(255, 0, 0)));
 * Phrase phrase4 = new Phrase(new Chunk("this is a phrase"));
 * Phrase phrase5 = new Phrase(18, new Chunk("this is a phrase", FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD, new Color(255, 0, 0)));
 *

 * @see Element

 * @see Chunk

 * @see Paragraph

 * @see Anchor
 */

open class Phrase : ArrayList<Element>, TextElementArray {

    // membervariables
    /** This is the leading of this phrase.  */
    protected var leading = java.lang.Float.NaN

    /** The text leading that is multiplied by the biggest font size in the line.  */
    /**
     * Gets the variable leading
     * @return the leading
     */
    /**
     * Sets the variable leading. The resultant leading will be
     *
     *  * If Phrase is added to the ColumnText: fixedLeading+multipliedLeading*maxFontSize, where maxFontSize is the size of the biggest font in the line; *
     *  * If Phrase is added to the PdfDocument: fixedLeading+multipliedLeading*phraseFontSize, where phraseFontSize is the size of the font applied to the current phrase. *
     *
     * @param multipliedLeading the variable leading
     */
    var multipliedLeading = 0f
        set(multipliedLeading) {
            this.leading = 0f
            this.multipliedLeading = multipliedLeading
        }

    /** This is the font of this phrase.  */
    /**
     * Gets the font of the first Chunk that appears in this Phrase.

     * @return    a Font
     */
    /**
     * Sets the main font of this phrase.
     * @param font    the new font
     */
    var font: Font? = null

    /** Null, unless the Phrase has to be hyphenated.
     * @since    2.1.2
     */
    /**
     * Getter for the hyphenation settings.
     * @return    a HyphenationEvent
     * *
     * @since    2.1.2
     */
    /**
     * Setter for the hyphenation.
     * @param    hyphenation    a HyphenationEvent instance
     * *
     * @since    2.1.2
     */
    var hyphenation: HyphenationEvent? = null

    /**
     * Predefined tab position and properties(alignment, leader and etc.);
     * @since    5.4.1
     */
    /**
     * Getter for the tab stops settings.
     * @return    a HyphenationEvent
     * *
     * @since    5.4.1
     */
    /**
     * Setter for the tab stops.
     * @param    tabSettings tab settings
     * *
     * @since    5.4.1
     */
    var tabSettings: TabSettings? = null

    /**
     * Copy constructor for Phrase.
     * @param phrase the Phrase to copy
     */
    constructor(phrase: Phrase) : super() {
        this.addAll(phrase)
        setLeading(phrase.getLeading(), phrase.multipliedLeading)
        font = phrase.font
        tabSettings = phrase.tabSettings
        hyphenation = phrase.hyphenation
    }

    /**
     * Constructs a Phrase with a certain leading.

     * @param    leading        the leading
     */
    @JvmOverloads constructor(leading: Float = 16f) {
        this.leading = leading
        font = Font()
    }

    /**
     * Constructs a Phrase with a certain Chunk.

     * @param    chunk        a Chunk
     */
    constructor(chunk: Chunk) {
        super.add(chunk)
        font = chunk.font
        hyphenation = chunk.hyphenation
    }

    /**
     * Constructs a Phrase with a certain Chunk
     * and a certain leading.

     * @param    leading    the leading
     * *
     * @param    chunk        a Chunk
     */
    constructor(leading: Float, chunk: Chunk) {
        this.leading = leading
        super.add(chunk)
        font = chunk.font
        hyphenation = chunk.hyphenation
    }

    /**
     * Constructs a Phrase with a certain String.

     * @param    string        a String
     */
    constructor(string: String) : this(java.lang.Float.NaN, string, Font()) {
    }

    /**
     * Constructs a Phrase with a certain String and a certain Font.

     * @param    string        a String
     * *
     * @param    font        a Font
     */
    constructor(string: String, font: Font) : this(java.lang.Float.NaN, string, font) {
    }

    /**
     * Constructs a Phrase with a certain leading, a certain String
     * and a certain Font.

     * @param    leading    the leading
     * *
     * @param    string        a String
     * *
     * @param    font        a Font
     */
    @JvmOverloads constructor(leading: Float, string: String?, font: Font = Font()) {
        this.leading = leading
        this.font = font
        /* bugfix by August Detlefsen */
        if (string != null && string.length != 0) {
            super.add(Chunk(string, font))
        }
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
            for (element in this) {
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
        return Element.PHRASE
    }

    /**
     * Gets all the chunks in this element.

     * @return    an ArrayList
     */
    override val chunks: List<Chunk>
        get() {
            val tmp = ArrayList<Chunk>()
            for (element in this) {
                tmp.addAll(element.chunks)
            }
            return tmp
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

    // overriding some of the ArrayList-methods

    /**
     * Adds a Chunk, an Anchor or another Phrase
     * to this Phrase.

     * @param    index    index at which the specified element is to be inserted
     * *
     * @param    element    an object of type Chunk, Anchor or Phrase
     * *
     * @throws    ClassCastException    when you try to add something that isn't a Chunk, Anchor or Phrase
     * *
     * @since 5.0.1 (signature changed to use Element)
     */
    override fun add(index: Int, element: Element?) {
        if (element == null) return
        when (element.type()) {
            Element.CHUNK -> {
                val chunk = element as Chunk?
                if (!font!!.isStandardFont) {
                    chunk.font = font!!.difference(chunk.font)
                }
                if (hyphenation != null && chunk.hyphenation == null && !chunk.isEmpty) {
                    chunk.setHyphenation(hyphenation)
                }
                super.add(index, chunk)
                return
            }
            Element.PHRASE, Element.PARAGRAPH, Element.MARKED, Element.DIV, Element.ANCHOR, Element.ANNOTATION, Element.PTABLE, Element.LIST, Element.YMARK, Element.WRITABLE_DIRECT -> {
                super.add(index, element)
                return
            }
            else -> throw ClassCastException(MessageLocalization.getComposedMessage("insertion.of.illegal.element.1", element.javaClass.name))
        }
    }

    /**
     * Adds a String to this Phrase.

     * @param   s       a string
     * *
     * @return  a boolean
     * *
     * @since 5.0.1
     */
    fun add(s: String?): Boolean {
        if (s == null) {
            return false
        }
        return super.add(Chunk(s, font))
    }

    /**
     * Adds a Chunk, Anchor or another Phrase
     * to this Phrase.

     * @param   element       an object of type Chunk, Anchor or Phrase
     * *
     * @return  a boolean
     * *
     * @throws  ClassCastException      when you try to add something that isn't a Chunk, Anchor or Phrase
     * *
     * @since 5.0.1 (signature changed to use Element)
     */
    override fun add(element: Element?): Boolean {
        if (element == null) return false
        try {
            // TODO same as in document - change switch to generic adding that works everywhere
            when (element.type()) {
                Element.CHUNK -> return addChunk(element as Chunk?)
                Element.PHRASE, Element.PARAGRAPH -> {
                    val phrase = element as Phrase?
                    var success = true
                    var e: Element
                    for (element2 in phrase) {
                        e = element2 as Element
                        if (e is Chunk) {
                            success = success and addChunk(e)
                        } else {
                            success = success and this.add(e)
                        }
                    }
                    return success
                }
                Element.MARKED, Element.DIV, Element.ANCHOR, Element.ANNOTATION, Element.PTABLE // case added by mr. Karen Vardanyan
                    , Element.LIST, Element.YMARK, Element.WRITABLE_DIRECT -> return super.add(element)
                else -> throw ClassCastException(element.type().toString())
            }
        } catch (cce: ClassCastException) {
            throw ClassCastException(MessageLocalization.getComposedMessage("insertion.of.illegal.element.1", cce.message))
        }

    }

    /**
     * Adds a collection of Chunks
     * to this Phrase.

     * @param    collection    a collection of Chunks, Anchors and Phrases.
     * *
     * @return    true if the action succeeded, false if not.
     * *
     * @throws    ClassCastException    when you try to add something that isn't a Chunk, Anchor or Phrase
     */
    override fun addAll(collection: Collection<Element>): Boolean {
        for (e in collection) {
            this.add(e)
        }
        return true
    }

    /**
     * Adds a Chunk.
     *
     *
     * This method is a hack to solve a problem I had with phrases that were split between chunks
     * in the wrong place.
     * @param chunk a Chunk to add to the Phrase
     * *
     * @return true if adding the Chunk succeeded
     */
    protected fun addChunk(chunk: Chunk): Boolean {
        var f: Font? = chunk.font
        val c = chunk.getContent()
        if (font != null && !font!!.isStandardFont) {
            f = font!!.difference(chunk.font)
        }
        if (size > 0 && !chunk.hasAttributes()) {
            try {
                val previous = get(size - 1) as Chunk
                val previousRole = previous.getRole()
                val chunkRole = chunk.getRole()
                val sameRole: Boolean
                if (previousRole == null || chunkRole == null)
                //Set the value to true if either are null since the overwriting of the role will not matter
                    sameRole = true
                else
                    sameRole = previousRole == chunkRole
                if (sameRole && !previous.hasAttributes() && !chunk.hasAccessibleAttributes() && !previous.hasAccessibleAttributes()
                        && (f == null || f.compareTo(previous.font) == 0)
                        && "" != previous.getContent().trim { it <= ' ' }
                        && "" != c.trim { it <= ' ' }) {
                    previous.append(c)
                    return true
                }
            } catch (cce: ClassCastException) {
            }

        }
        val newChunk = Chunk(c, f)
        newChunk.attributes = chunk.attributes
        newChunk.role = chunk.getRole()
        newChunk.accessibleAttributes = chunk.getAccessibleAttributes()
        if (hyphenation != null && newChunk.hyphenation == null && !newChunk.isEmpty) {
            newChunk.hyphenation = hyphenation
        }
        return super.add(newChunk)
    }

    /**
     * Adds an Element to the Paragraph.

     * @param    object        the object to add.
     */
    protected fun addSpecial(`object`: Element) {
        super.add(`object`)
    }

    // other methods that change the member variables

    /**
     * Sets the leading fixed and variable. The resultant leading will be
     *
     *  * If Phrase is added to the ColumnText: fixedLeading+multipliedLeading*maxFontSize, where maxFontSize is the size of the biggest font in the line; *
     *  * If Phrase is added to the PdfDocument: fixedLeading+multipliedLeading*phraseFontSize, where phraseFontSize is the size of the font applied to the current phrase. *
     *
     * @param fixedLeading the fixed leading
     * *
     * @param multipliedLeading the variable leading
     */
    fun setLeading(fixedLeading: Float, multipliedLeading: Float) {
        this.leading = fixedLeading
        this.multipliedLeading = multipliedLeading
    }

    /**
     * @see com.itextpdf.text.Phrase.setLeading
     */
    fun setLeading(fixedLeading: Float) {
        this.leading = fixedLeading
        this.multipliedLeading = 0f
    }

    // methods to retrieve information

    /**
     * Gets the leading of this phrase.

     * @return    the linespacing
     */
    fun getLeading(): Float {
        if (java.lang.Float.isNaN(leading) && font != null) {
            return font!!.getCalculatedLeading(1.5f)
        }
        return leading
    }

    /**
     * Gets the total leading.
     * This method is based on the assumption that the
     * font of the Paragraph is the font of all the elements
     * that make part of the paragraph. This isn't necessarily
     * true.
     * @return the total leading (fixed and multiplied)
     */
    val totalLeading: Float
        get() {
            val m = if (font == null)
                Font.DEFAULTSIZE * multipliedLeading
            else
                font!!.getCalculatedLeading(multipliedLeading)
            if (m > 0 && !hasLeading()) {
                return m
            }
            return getLeading() + m
        }

    /**
     * Checks you if the leading of this phrase is defined.

     * @return    true if the leading is defined
     */
    fun hasLeading(): Boolean {
        if (java.lang.Float.isNaN(leading)) {
            return false
        }
        return true
    }

    /**
     * Returns the content as a String object.
     * This method differs from toString because toString will return an ArrayList with the toString value of the Chunks in this Phrase.
     * @return the content
     */
    val content: String
        get() {
            val buf = StringBuffer()
            for (c in chunks) {
                buf.append(c.toString())
            }
            return buf.toString()
        }

    /**
     * Checks is this Phrase contains no or 1 empty Chunk.

     * @return    false if the Phrase
     * * contains more than one or more non-emptyChunks.
     */
    override fun isEmpty(): Boolean {
        when (size) {
            0 -> return true
            1 -> {
                val element = get(0)
                if (element.type() == Element.CHUNK && (element as Chunk).isEmpty) {
                    return true
                }
                return false
            }
            else -> return false
        }
    }

    // kept for historical reasons; people should use FontSelector
    // eligible for deprecation, but the methods are mentioned in the book p277.

    /**
     * Constructs a Phrase that can be used in the static getInstance() method.
     * @param    dummy    a dummy parameter
     */
    private constructor(dummy: Boolean) {
    }

    fun trim(): Boolean {
        while (this.size > 0) {
            val firstChunk = this[0]
            if (firstChunk is Chunk && firstChunk.isWhitespace) {
                this.remove(firstChunk)
            } else {
                break
            }
        }
        while (this.size > 0) {
            val lastChunk = this[this.size - 1]
            if (lastChunk is Chunk && lastChunk.isWhitespace) {
                this.remove(lastChunk)
            } else {
                break
            }
        }
        return size > 0
    }

    companion object {

        // constants
        private val serialVersionUID = 2643594602455068231L

        /**
         * Gets a special kind of Phrase that changes some characters into corresponding symbols.
         * @param string
         * *
         * @return a newly constructed Phrase
         */
        fun getInstance(string: String): Phrase {
            return getInstance(16, string, Font())
        }

        /**
         * Gets a special kind of Phrase that changes some characters into corresponding symbols.
         * @param leading
         * *
         * @param string
         * *
         * @param font
         * *
         * @return a newly constructed Phrase
         */
        @JvmOverloads fun getInstance(leading: Int, string: String?, font: Font = Font()): Phrase {
            var string = string
            val p = Phrase(true)
            p.setLeading(leading.toFloat())
            p.font = font
            if (font.family != FontFamily.SYMBOL && font.family != FontFamily.ZAPFDINGBATS && font.baseFont == null) {
                var index: Int
                while ((index = SpecialSymbol.index(string)) > -1) {
                    if (index > 0) {
                        val firstPart = string!!.substring(0, index)
                        p.add(Chunk(firstPart, font))
                        string = string.substring(index)
                    }
                    val symbol = Font(FontFamily.SYMBOL, font.size, font.style, font.color)
                    val buf = StringBuffer()
                    buf.append(SpecialSymbol.getCorrespondingSymbol(string!![0]))
                    string = string.substring(1)
                    while (SpecialSymbol.index(string) == 0) {
                        buf.append(SpecialSymbol.getCorrespondingSymbol(string!![0]))
                        string = string.substring(1)
                    }
                    p.add(Chunk(buf.toString(), symbol))
                }
            }
            if (string != null && string.length != 0) {
                p.add(Chunk(string, font))
            }
            return p
        }
    }

}// constructors
/**
 * Constructs a Phrase without specifying a leading.
 */
/**
 * Constructs a Phrase with a certain leading and a certain String.

 * @param    leading    the leading
 * *
 * @param    string        a String
 */
/**
 * Gets a special kind of Phrase that changes some characters into corresponding symbols.
 * @param leading
 * *
 * @param string
 * *
 * @return a newly constructed Phrase
 */
