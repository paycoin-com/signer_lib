/*
 * $Id: 19eb6d5e8a96ea76b8aa142608d8d5f87eb98513 $
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

import java.net.URL
import java.util.ArrayList
import java.util.HashMap

/**
 * An Annotation is a little note that can be added to a page on
 * a document.

 * @see Element

 * @see Anchor
 */

class Annotation : Element {

    /** This is the type of annotation.  */
    protected var annotationtype: Int = 0

    /** This is the title of the Annotation.  */
    protected var annotationAttributes = HashMap<String, Any>()

    /** This is the lower left x-value  */
    protected var llx = java.lang.Float.NaN

    /** This is the lower left y-value  */
    protected var lly = java.lang.Float.NaN

    /** This is the upper right x-value  */
    protected var urx = java.lang.Float.NaN

    /** This is the upper right y-value  */
    protected var ury = java.lang.Float.NaN

    // constructors

    /**
     * Constructs an Annotation with a certain title and some
     * text.

     * @param llx
     * *            lower left x coordinate
     * *
     * @param lly
     * *            lower left y coordinate
     * *
     * @param urx
     * *            upper right x coordinate
     * *
     * @param ury
     * *            upper right y coordinate
     */
    private constructor(llx: Float, lly: Float, urx: Float, ury: Float) {
        this.llx = llx
        this.lly = lly
        this.urx = urx
        this.ury = ury
    }

    /**
     * Copy constructor.
     * @param an the annotation to create a new Annotation from
     */
    constructor(an: Annotation) {
        annotationtype = an.annotationtype
        annotationAttributes = an.annotationAttributes
        llx = an.llx
        lly = an.lly
        urx = an.urx
        ury = an.ury
    }

    /**
     * Constructs an Annotation with a certain title and some
     * text.

     * @param title
     * *            the title of the annotation
     * *
     * @param text
     * *            the content of the annotation
     */
    constructor(title: String, text: String) {
        annotationtype = TEXT
        annotationAttributes.put(TITLE, title)
        annotationAttributes.put(CONTENT, text)
    }

    /**
     * Constructs an Annotation with a certain title and some
     * text.

     * @param title
     * *            the title of the annotation
     * *
     * @param text
     * *            the content of the annotation
     * *
     * @param llx
     * *            the lower left x-value
     * *
     * @param lly
     * *            the lower left y-value
     * *
     * @param urx
     * *            the upper right x-value
     * *
     * @param ury
     * *            the upper right y-value
     */
    constructor(title: String, text: String, llx: Float, lly: Float,
                urx: Float, ury: Float) : this(llx, lly, urx, ury) {
        annotationtype = TEXT
        annotationAttributes.put(TITLE, title)
        annotationAttributes.put(CONTENT, text)
    }

    /**
     * Constructs an Annotation.

     * @param llx
     * *            the lower left x-value
     * *
     * @param lly
     * *            the lower left y-value
     * *
     * @param urx
     * *            the upper right x-value
     * *
     * @param ury
     * *            the upper right y-value
     * *
     * @param url
     * *            the external reference
     */
    constructor(llx: Float, lly: Float, urx: Float, ury: Float, url: URL) : this(llx, lly, urx, ury) {
        annotationtype = URL_NET
        annotationAttributes.put(URL, url)
    }

    /**
     * Constructs an Annotation.

     * @param llx
     * *            the lower left x-value
     * *
     * @param lly
     * *            the lower left y-value
     * *
     * @param urx
     * *            the upper right x-value
     * *
     * @param ury
     * *            the upper right y-value
     * *
     * @param url
     * *            the external reference
     */
    constructor(llx: Float, lly: Float, urx: Float, ury: Float, url: String) : this(llx, lly, urx, ury) {
        annotationtype = URL_AS_STRING
        annotationAttributes.put(FILE, url)
    }

    /**
     * Constructs an Annotation.

     * @param llx
     * *            the lower left x-value
     * *
     * @param lly
     * *            the lower left y-value
     * *
     * @param urx
     * *            the upper right x-value
     * *
     * @param ury
     * *            the upper right y-value
     * *
     * @param file
     * *            an external PDF file
     * *
     * @param dest
     * *            the destination in this file
     */
    constructor(llx: Float, lly: Float, urx: Float, ury: Float, file: String,
                dest: String) : this(llx, lly, urx, ury) {
        annotationtype = FILE_DEST
        annotationAttributes.put(FILE, file)
        annotationAttributes.put(DESTINATION, dest)
    }

    /**
     * Creates a Screen annotation to embed media clips

     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     * *
     * @param moviePath
     * *            path to the media clip file
     * *
     * @param mimeType
     * *            mime type of the media
     * *
     * @param showOnDisplay
     * *            if true play on display of the page
     */
    constructor(llx: Float, lly: Float, urx: Float, ury: Float,
                moviePath: String, mimeType: String, showOnDisplay: Boolean) : this(llx, lly, urx, ury) {
        annotationtype = SCREEN
        annotationAttributes.put(FILE, moviePath)
        annotationAttributes.put(MIMETYPE, mimeType)
        annotationAttributes.put(PARAMETERS, booleanArrayOf(false /* embedded */, showOnDisplay))
    }

    /**
     * Constructs an Annotation.

     * @param llx
     * *            the lower left x-value
     * *
     * @param lly
     * *            the lower left y-value
     * *
     * @param urx
     * *            the upper right x-value
     * *
     * @param ury
     * *            the upper right y-value
     * *
     * @param file
     * *            an external PDF file
     * *
     * @param page
     * *            a page number in this file
     */
    constructor(llx: Float, lly: Float, urx: Float, ury: Float, file: String,
                page: Int) : this(llx, lly, urx, ury) {
        annotationtype = FILE_PAGE
        annotationAttributes.put(FILE, file)
        annotationAttributes.put(PAGE, Integer.valueOf(page))
    }

    /**
     * Constructs an Annotation.

     * @param llx
     * *            the lower left x-value
     * *
     * @param lly
     * *            the lower left y-value
     * *
     * @param urx
     * *            the upper right x-value
     * *
     * @param ury
     * *            the upper right y-value
     * *
     * @param named
     * *            a named destination in this file
     */
    constructor(llx: Float, lly: Float, urx: Float, ury: Float, named: Int) : this(llx, lly, urx, ury) {
        annotationtype = NAMED_DEST
        annotationAttributes.put(NAMED, Integer.valueOf(named))
    }

    /**
     * Constructs an Annotation.

     * @param llx
     * *            the lower left x-value
     * *
     * @param lly
     * *            the lower left y-value
     * *
     * @param urx
     * *            the upper right x-value
     * *
     * @param ury
     * *            the upper right y-value
     * *
     * @param application
     * *            an external application
     * *
     * @param parameters
     * *            parameters to pass to this application
     * *
     * @param operation
     * *            the operation to pass to this application
     * *
     * @param defaultdir
     * *            the default directory to run this application in
     */
    constructor(llx: Float, lly: Float, urx: Float, ury: Float,
                application: String, parameters: String, operation: String,
                defaultdir: String) : this(llx, lly, urx, ury) {
        annotationtype = LAUNCH
        annotationAttributes.put(APPLICATION, application)
        annotationAttributes.put(PARAMETERS, parameters)
        annotationAttributes.put(OPERATION, operation)
        annotationAttributes.put(DEFAULTDIR, defaultdir)
    }

    // implementation of the Element-methods

    /**
     * Gets the type of the text element.

     * @return a type
     */
    override fun type(): Int {
        return Element.ANNOTATION
    }

    /**
     * Processes the element by adding it (or the different parts) to an
     * ElementListener.

     * @param listener
     * *            an ElementListener
     * *
     * @return true if the element was processed successfully
     */
    override fun process(listener: ElementListener): Boolean {
        try {
            return listener.add(this)
        } catch (de: DocumentException) {
            return false
        }

    }

    /**
     * Gets all the chunks in this element.

     * @return an ArrayList
     */

    override val chunks: List<Chunk>
        get() = ArrayList<Chunk>()

    // methods

    /**
     * Sets the dimensions of this annotation.

     * @param llx
     * *            the lower left x-value
     * *
     * @param lly
     * *            the lower left y-value
     * *
     * @param urx
     * *            the upper right x-value
     * *
     * @param ury
     * *            the upper right y-value
     */
    fun setDimensions(llx: Float, lly: Float, urx: Float, ury: Float) {
        this.llx = llx
        this.lly = lly
        this.urx = urx
        this.ury = ury
    }

    // methods to retrieve information

    /**
     * Returns the lower left x-value.

     * @return a value
     */
    fun llx(): Float {
        return llx
    }

    /**
     * Returns the lower left y-value.

     * @return a value
     */
    fun lly(): Float {
        return lly
    }

    /**
     * Returns the upper right x-value.

     * @return a value
     */
    fun urx(): Float {
        return urx
    }

    /**
     * Returns the upper right y-value.

     * @return a value
     */
    fun ury(): Float {
        return ury
    }

    /**
     * Returns the lower left x-value.

     * @param def
     * *            the default value
     * *
     * @return a value
     */
    fun llx(def: Float): Float {
        if (java.lang.Float.isNaN(llx))
            return def
        return llx
    }

    /**
     * Returns the lower left y-value.

     * @param def
     * *            the default value
     * *
     * @return a value
     */
    fun lly(def: Float): Float {
        if (java.lang.Float.isNaN(lly))
            return def
        return lly
    }

    /**
     * Returns the upper right x-value.

     * @param def
     * *            the default value
     * *
     * @return a value
     */
    fun urx(def: Float): Float {
        if (java.lang.Float.isNaN(urx))
            return def
        return urx
    }

    /**
     * Returns the upper right y-value.

     * @param def
     * *            the default value
     * *
     * @return a value
     */
    fun ury(def: Float): Float {
        if (java.lang.Float.isNaN(ury))
            return def
        return ury
    }

    /**
     * Returns the type of this Annotation.

     * @return a type
     */
    fun annotationType(): Int {
        return annotationtype
    }

    /**
     * Returns the title of this Annotation.

     * @return a name
     */
    fun title(): String {
        var s: String? = annotationAttributes[TITLE] as String
        if (s == null)
            s = ""
        return s
    }

    /**
     * Gets the content of this Annotation.

     * @return a reference
     */
    fun content(): String {
        var s: String? = annotationAttributes[CONTENT] as String
        if (s == null)
            s = ""
        return s
    }

    /**
     * Gets the content of this Annotation.

     * @return a reference
     */
    fun attributes(): HashMap<String, Any> {
        return annotationAttributes
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

    companion object {

        // membervariables

        /** This is a possible annotation type.  */
        val TEXT = 0

        /** This is a possible annotation type.  */
        val URL_NET = 1

        /** This is a possible annotation type.  */
        val URL_AS_STRING = 2

        /** This is a possible annotation type.  */
        val FILE_DEST = 3

        /** This is a possible annotation type.  */
        val FILE_PAGE = 4

        /** This is a possible annotation type.  */
        val NAMED_DEST = 5

        /** This is a possible annotation type.  */
        val LAUNCH = 6

        /** This is a possible annotation type.  */
        val SCREEN = 7

        /** This is a possible attribute.  */
        val TITLE = "title"

        /** This is a possible attribute.  */
        val CONTENT = "content"

        /** This is a possible attribute.  */
        val URL = "url"

        /** This is a possible attribute.  */
        val FILE = "file"

        /** This is a possible attribute.  */
        val DESTINATION = "destination"

        /** This is a possible attribute.  */
        val PAGE = "page"

        /** This is a possible attribute.  */
        val NAMED = "named"

        /** This is a possible attribute.  */
        val APPLICATION = "application"

        /** This is a possible attribute.  */
        val PARAMETERS = "parameters"

        /** This is a possible attribute.  */
        val OPERATION = "operation"

        /** This is a possible attribute.  */
        val DEFAULTDIR = "defaultdir"

        /** This is a possible attribute.  */
        val LLX = "llx"

        /** This is a possible attribute.  */
        val LLY = "lly"

        /** This is a possible attribute.  */
        val URX = "urx"

        /** This is a possible attribute.  */
        val URY = "ury"

        /** This is a possible attribute.  */
        val MIMETYPE = "mime"
    }

}
