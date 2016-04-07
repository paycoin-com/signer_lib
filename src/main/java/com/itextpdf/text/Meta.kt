/*
 * $Id: ac7125fb172516f1a8e697bd13b9dc6d1f56c3c6 $
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

import java.util.ArrayList

/**
 * This is an Element that contains
 * some meta information about the document.
 *
 * An object of type Meta can not be constructed by the user.
 * User defined meta information should be placed in a Header-object.
 * Meta is reserved for: Subject, Keywords, Author, Title, Producer
 * and Creationdate information.

 * @see Element

 * @see Header
 */

open class Meta : Element {

    // membervariables

    /** This is the type of Meta-information this object contains.  */
    private val type: Int

    /** This is the content of the Meta-information.  */
    private val content: StringBuffer

    // constructors

    /**
     * Constructs a Meta.

     * @param    type        the type of meta-information
     * *
     * @param    content        the content
     */
    internal constructor(type: Int, content: String) {
        this.type = type
        this.content = StringBuffer(content)
    }

    /**
     * Constructs a Meta.

     * @param    tag            the tagname of the meta-information
     * *
     * @param    content        the content
     */
    constructor(tag: String, content: String) {
        this.type = Meta.getType(tag)
        this.content = StringBuffer(content)
    }

    // implementation of the Element-methods

    /**
     * Processes the element by adding it (or the different parts) to a
     * ElementListener.

     * @param    listener        the ElementListener
     * *
     * @return    true if the element was processed successfully
     */
    override fun process(listener: ElementListener): Boolean {
        try {
            return listener.add(this)
        } catch (de: DocumentException) {
            return false
        }

    }

    /**
     * Gets the type of the text element.

     * @return    a type
     */
    override fun type(): Int {
        return type
    }

    /**
     * Gets all the chunks in this element.

     * @return    an ArrayList
     */
    override val chunks: List<Chunk>
        get() = ArrayList<Chunk>()

    /**
     * @see com.itextpdf.text.Element.isContent
     * @since    iText 2.0.8
     */
    override val isContent: Boolean
        get() = false

    /**
     * @see com.itextpdf.text.Element.isNestable
     * @since    iText 2.0.8
     */
    override val isNestable: Boolean
        get() = false

    // methods

    /**
     * appends some text to this Meta.

     * @param    string      a String
     * *
     * @return    a StringBuffer
     */
    fun append(string: String): StringBuffer {
        return content.append(string)
    }

    // methods to retrieve information

    /**
     * Returns the content of the meta information.

     * @return    a String
     */
    fun getContent(): String {
        return content.toString()
    }

    /**
     * Returns the name of the meta information.

     * @return    a String
     */

    open val name: String
        get() {
            when (type) {
                Element.SUBJECT -> return Meta.SUBJECT
                Element.KEYWORDS -> return Meta.KEYWORDS
                Element.AUTHOR -> return Meta.AUTHOR
                Element.TITLE -> return Meta.TITLE
                Element.PRODUCER -> return Meta.PRODUCER
                Element.CREATIONDATE -> return Meta.CREATIONDATE
                else -> return Meta.UNKNOWN
            }
        }

    companion object {

        /**
         * The possible value of an alignment attribute.
         * @since 5.0.6 (moved from ElementTags)
         */
        val UNKNOWN = "unknown"

        /**
         * The possible value of an alignment attribute.
         * @since 5.0.6 (moved from ElementTags)
         */
        val PRODUCER = "producer"

        /**
         * The possible value of an alignment attribute.
         * @since 5.0.6 (moved from ElementTags)
         */
        val CREATIONDATE = "creationdate"

        /**
         * The possible value of an alignment attribute.
         * @since 5.0.6 (moved from ElementTags)
         */
        val AUTHOR = "author"

        /**
         * The possible value of an alignment attribute.
         * @since 5.0.6 (moved from ElementTags)
         */
        val KEYWORDS = "keywords"

        /**
         * The possible value of an alignment attribute.
         * @since 5.0.6 (moved from ElementTags)
         */
        val SUBJECT = "subject"

        /**
         * The possible value of an alignment attribute.
         * @since 5.0.6 (moved from ElementTags)
         */
        val TITLE = "title"

        /**
         * Returns the name of the meta information.

         * @param tag iText tag for meta information
         * *
         * @return    the Element value corresponding with the given tag
         */
        fun getType(tag: String): Int {
            if (Meta.SUBJECT == tag) {
                return Element.SUBJECT
            }
            if (Meta.KEYWORDS == tag) {
                return Element.KEYWORDS
            }
            if (Meta.AUTHOR == tag) {
                return Element.AUTHOR
            }
            if (Meta.TITLE == tag) {
                return Element.TITLE
            }
            if (Meta.PRODUCER == tag) {
                return Element.PRODUCER
            }
            if (Meta.CREATIONDATE == tag) {
                return Element.CREATIONDATE
            }
            return Element.HEADER
        }
    }

}
