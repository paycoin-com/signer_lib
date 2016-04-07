/*
 * $Id: 3ffab3cc45af756f37b743ef4be798efd260d999 $
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

import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList

/**
 * An Anchor can be a reference or a destination of a reference.
 *
 * An Anchor is a special kind of Phrase.
 * It is constructed in the same way.
 *
 * Example:
 *
 * Anchor anchor = new Anchor("this is a link");
 * anchor.setName("LINK");
 * anchor.setReference("http://www.lowagie.com");
 *

 * @see Element

 * @see Phrase
 */

class Anchor : Phrase {

    // membervariables

    /** This is the name of the Anchor.  */
    // methods to retrieve information

    /**
     * Returns the name of this Anchor.

     * @return    a name
     */
    // methods

    /**
     * Sets the name of this Anchor.

     * @param    name        a new name
     */
    var name: String? = null

    /** This is the reference of the Anchor.  */
    /**
     * Gets the reference of this Anchor.

     * @return    a reference
     */
    /**
     * Sets the reference of this Anchor.

     * @param    reference        a new reference
     */
    var reference: String? = null

    // constructors

    /**
     * Constructs an Anchor without specifying a leading.
     */
    constructor() : super(16f) {
    }

    /**
     * Constructs an Anchor with a certain leading.

     * @param    leading        the leading
     */

    constructor(leading: Float) : super(leading) {
    }

    /**
     * Constructs an Anchor with a certain Chunk.

     * @param    chunk        a Chunk
     */
    constructor(chunk: Chunk) : super(chunk) {
    }

    /**
     * Constructs an Anchor with a certain String.

     * @param    string        a String
     */
    constructor(string: String) : super(string) {
    }

    /**
     * Constructs an Anchor with a certain String
     * and a certain Font.

     * @param    string        a String
     * *
     * @param    font        a Font
     */
    constructor(string: String, font: Font) : super(string, font) {
    }

    /**
     * Constructs an Anchor with a certain Chunk
     * and a certain leading.

     * @param    leading        the leading
     * *
     * @param    chunk        a Chunk
     */
    constructor(leading: Float, chunk: Chunk) : super(leading, chunk) {
    }

    /**
     * Constructs an Anchor with a certain leading
     * and a certain String.

     * @param    leading        the leading
     * *
     * @param    string        a String
     */
    constructor(leading: Float, string: String) : super(leading, string) {
    }

    /**
     * Constructs an Anchor with a certain leading,
     * a certain String and a certain Font.

     * @param    leading        the leading
     * *
     * @param    string        a String
     * *
     * @param    font        a Font
     */
    constructor(leading: Float, string: String, font: Font) : super(leading, string, font) {
    }

    /**
     * Constructs an Anchor with a certain Phrase.

     * @param    phrase        a Phrase
     */
    constructor(phrase: Phrase) : super(phrase) {
        if (phrase is Anchor) {
            name = phrase.name
            reference = phrase.reference
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
            var chunk: Chunk
            val i = chunks.iterator()
            val localDestination = reference != null && reference!!.startsWith("#")
            var notGotoOK = true
            while (i.hasNext()) {
                chunk = i.next()
                if (name != null && notGotoOK && !chunk.isEmpty) {
                    chunk.setLocalDestination(name)
                    notGotoOK = false
                }
                if (localDestination) {
                    chunk.setLocalGoto(reference!!.substring(1))
                }
                listener.add(chunk)
            }
            return true
        } catch (de: DocumentException) {
            return false
        }

    }

    /**
     * Gets all the chunks in this element.

     * @return    an ArrayList
     */
    override val chunks: List<Chunk>
        get() {
            val localDestination = reference != null && reference!!.startsWith("#")
            var notGotoOK = true
            val tmp = ArrayList<Chunk>()
            val i = iterator()
            var element: Element
            while (i.hasNext()) {
                element = i.next() as Element
                if (element is Chunk) {
                    notGotoOK = applyAnchor(element, notGotoOK, localDestination)
                    tmp.add(element)
                } else {
                    for (c in element.chunks) {
                        notGotoOK = applyAnchor(c, notGotoOK, localDestination)
                        tmp.add(c)
                    }
                }
            }
            return tmp
        }

    /**
     * Applies the properties of the Anchor to a Chunk.
     * @param chunk            the Chunk (part of the Anchor)
     * *
     * @param notGotoOK        if true, this chunk will determine the local destination
     * *
     * @param localDestination    true if the chunk is a local goto and the reference a local destination
     * *
     * @return    the value of notGotoOK or false, if a previous Chunk was used to determine the local destination
     */
    protected fun applyAnchor(chunk: Chunk, notGotoOK: Boolean, localDestination: Boolean): Boolean {
        var notGotoOK = notGotoOK
        if (name != null && notGotoOK && !chunk.isEmpty) {
            chunk.setLocalDestination(name)
            notGotoOK = false
        }
        if (localDestination) {
            chunk.setLocalGoto(reference!!.substring(1))
        } else if (reference != null)
            chunk.setAnchor(reference)
        return notGotoOK
    }

    /**
     * Gets the type of the text element.

     * @return    a type
     */
    override fun type(): Int {
        return Element.ANCHOR
    }

    /**
     * Gets the reference of this Anchor.

     * @return    an URL
     */
    val url: URL?
        get() {
            try {
                return URL(reference)
            } catch (mue: MalformedURLException) {
                return null
            }

        }

    companion object {

        // constant
        private val serialVersionUID = -852278536049236911L
    }

}
