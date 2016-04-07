/*
 * $Id: 6ee5f602fc8d75e1e01034a977cca06ead1a2c0b $
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

import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.HashMap
import java.util.Stack

import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.xml.simpleparser.SimpleXMLDocHandler
import com.itextpdf.text.xml.simpleparser.SimpleXMLParser

/**
 * Reads a XFDF.
 * @author Leonard Rosenthol (leonardr@pdfsages.com)
 */
class XfdfReader : SimpleXMLDocHandler {
    // stuff used during parsing to handle state
    private var foundRoot = false
    private val fieldNames = Stack<String>()
    private val fieldValues = Stack<String>()

    // storage for the field list and their values
    /** Gets all the fields. The map is keyed by the fully qualified
     * field name and the value is a merged PdfDictionary
     * with the field content.
     * @return all the fields
     */
    var fields: HashMap<String, String>
        internal set
    /**
     * Storage for field values if there's more than one value for a field.
     * @since    2.1.4
     */
    protected var listFields: HashMap<String, List<String>>

    // storage for the path to referenced PDF, if any
    /** Gets the PDF file specification contained in the FDF.
     * @return the PDF file specification contained in the FDF
     */
    var fileSpec: String
        internal set

    /**
     * Reads an XFDF form.
     * @param filename the file name of the form
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class)
    constructor(filename: String) {
        var fin: FileInputStream? = null
        try {
            fin = FileInputStream(filename)
            SimpleXMLParser.parse(this, fin)
        } finally {
            try {
                if (fin != null) {
                    fin.close()
                }
            } catch (e: Exception) {
            }

        }
    }

    /**
     * Reads an XFDF form.
     * @param xfdfIn the byte array with the form
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class)
    constructor(xfdfIn: ByteArray) : this(ByteArrayInputStream(xfdfIn)) {
    }

    /**
     * Reads an XFDF form.
     * @param is an InputStream to read the form
     * *
     * @throws IOException on error
     * *
     * @since 5.0.1
     */
    @Throws(IOException::class)
    constructor(`is`: InputStream) {
        SimpleXMLParser.parse(this, `is`)
    }

    /** Gets the field value.
     * @param name the fully qualified field name
     * *
     * @return the field's value
     */
    fun getField(name: String): String {
        return fields[name]
    }

    /** Gets the field value or null if the field does not
     * exist or has no value defined.
     * @param name the fully qualified field name
     * *
     * @return the field value or null
     */
    fun getFieldValue(name: String): String? {
        val field = fields[name]
        if (field == null)
            return null
        else
            return field
    }

    /**
     * Gets the field values for a list or null if the field does not
     * exist or has no value defined.
     * @param name the fully qualified field name
     * *
     * @return the field values or null
     * *
     * @since    2.1.4
     */
    fun getListValues(name: String): List<String> {
        return listFields[name]
    }

    /**
     * Called when a start tag is found.
     * @param tag the tag name
     * *
     * @param h the tag's attributes
     */
    override fun startElement(tag: String, h: Map<String, String>) {
        if (!foundRoot) {
            if (tag != "xfdf")
                throw RuntimeException(MessageLocalization.getComposedMessage("root.element.is.not.xfdf.1", tag))
            else
                foundRoot = true
        }

        if (tag == "xfdf") {

        } else if (tag == "f") {
            fileSpec = h["href"]
        } else if (tag == "fields") {
            fields = HashMap<String, String>()        // init it!
            listFields = HashMap<String, List<String>>()
        } else if (tag == "field") {
            val fName = h["name"]
            fieldNames.push(fName)
        } else if (tag == "value") {
            fieldValues.push("")
        }
    }

    /**
     * Called when an end tag is found.
     * @param tag the tag name
     */
    override fun endElement(tag: String) {
        if (tag == "value") {
            var fName = ""
            for (k in fieldNames.indices) {
                fName += "." + fieldNames.elementAt(k)
            }
            if (fName.startsWith("."))
                fName = fName.substring(1)
            val fVal = fieldValues.pop()
            val old = fields.put(fName, fVal)
            if (old != null) {
                var l: MutableList<String>? = listFields[fName]
                if (l == null) {
                    l = ArrayList<String>()
                    l.add(old)
                }
                l.add(fVal)
                listFields.put(fName, l)
            }
        } else if (tag == "field") {
            if (!fieldNames.isEmpty())
                fieldNames.pop()
        }
    }

    /**
     * Called when the document starts to be parsed.
     */
    override fun startDocument() {
        fileSpec = ""
    }

    /**
     * Called after the document is parsed.
     */
    override fun endDocument() {

    }

    /**
     * Called when a text element is found.
     * @param str the text element, probably a fragment.
     */
    override fun text(str: String) {
        if (fieldNames.isEmpty() || fieldValues.isEmpty())
            return

        var `val` = fieldValues.pop()
        `val` += str
        fieldValues.push(`val`)
    }
}
