/*
 * $Id: a69e4c9666b32c051fc8321d9502672a56956a4f $
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
package com.itextpdf.text.xml.xmp

import java.io.IOException
import java.io.OutputStream

import com.itextpdf.text.Version
import com.itextpdf.text.pdf.PdfDate
import com.itextpdf.text.pdf.PdfDictionary
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.PdfString
import com.itextpdf.xmp.*
import com.itextpdf.xmp.options.PropertyOptions
import com.itextpdf.xmp.options.SerializeOptions

/**
 * With this class you can create an Xmp Stream that can be used for adding
 * Metadata to a PDF Dictionary. Remark that this class doesn't cover the
 * complete XMP specification.
 */
class XmpWriter
/**
 * Creates an XmpWriter.
 * @param os
 * *
 * @param utfEncoding
 * *
 * @param extraSpace
 * *
 * @throws IOException
 */
@Throws(IOException::class)
@JvmOverloads constructor(protected var outputStream: OutputStream?, utfEncoding: String = XmpWriter.UTF8, extraSpace: Int = 2000) {

    var xmpMeta: XMPMeta
        protected set
    protected var serializeOptions: SerializeOptions

    init {
        serializeOptions = SerializeOptions()
        if (UTF16BE == utfEncoding || UTF16 == utfEncoding)
            serializeOptions.encodeUTF16BE = true
        else if (UTF16LE == utfEncoding)
            serializeOptions.encodeUTF16LE = true
        serializeOptions.padding = extraSpace
        xmpMeta = XMPMetaFactory.create()
        xmpMeta.objectName = XMPConst.TAG_XMPMETA
        xmpMeta.objectName = ""
        try {
            xmpMeta.setProperty(XMPConst.NS_DC, DublinCoreProperties.FORMAT, "application/pdf")
            xmpMeta.setProperty(XMPConst.NS_PDF, PdfProperties.PRODUCER, Version.getInstance().version)
        } catch (xmpExc: XMPException) {
        }

    }

    /**
     * @param os
     * *
     * @param info
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    constructor(os: OutputStream, info: PdfDictionary?) : this(os) {
        if (info != null) {
            var key: PdfName
            var obj: PdfObject?
            var value: String
            for (pdfName in info.keys) {
                key = pdfName
                obj = info[key]
                if (obj == null)
                    continue
                if (!obj.isString)
                    continue
                value = (obj as PdfString).toUnicodeString()
                try {
                    addDocInfoProperty(key, value)
                } catch (xmpExc: XMPException) {
                    throw IOException(xmpExc.message)
                }

            }
        }
    }

    /**
     * @param os
     * *
     * @param info
     * *
     * @throws IOException
     * *
     * @since 5.0.1 (generic type in signature)
     */
    @Throws(IOException::class)
    constructor(os: OutputStream, info: Map<String, String>?) : this(os) {
        if (info != null) {
            var key: String
            var value: String?
            for (entry in info.entries) {
                key = entry.key
                value = entry.value
                if (value == null)
                    continue
                try {
                    addDocInfoProperty(key, value)
                } catch (xmpExc: XMPException) {
                    throw IOException(xmpExc.message)
                }

            }
        }
    }

    /** Sets the XMP to read-only  */
    fun setReadOnly() {
        serializeOptions.readOnlyPacket = true
    }

    /**
     * @param about The about to set.
     */
    fun setAbout(about: String) {
        xmpMeta.objectName = about
    }

    /**
     * Adds an rdf:Description.
     * @param xmlns
     * *
     * @param content
     * *
     * @throws IOException
     */
    @Deprecated("")
    @Throws(IOException::class)
    fun addRdfDescription(xmlns: String, content: String) {
        try {
            val str = "<rdf:RDF xmlns:rdf=\"" + XMPConst.NS_RDF + "\">" +
                    "<rdf:Description rdf:about=\"" + xmpMeta.objectName +
                    "\" " +
                    xmlns +
                    ">" +
                    content +
                    "</rdf:Description></rdf:RDF>\n"
            val extMeta = XMPMetaFactory.parseFromString(str)
            XMPUtils.appendProperties(extMeta, xmpMeta, true, true)
        } catch (xmpExc: XMPException) {
            throw IOException(xmpExc.message)
        }

    }

    /**
     * Adds an rdf:Description.
     * @param s
     * *
     * @throws IOException
     */
    @Deprecated("")
    @Throws(IOException::class)
    fun addRdfDescription(s: XmpSchema) {
        try {
            val str = "<rdf:RDF xmlns:rdf=\"" + XMPConst.NS_RDF + "\">" +
                    "<rdf:Description rdf:about=\"" + xmpMeta.objectName +
                    "\" " +
                    s.xmlns +
                    ">" +
                    s.toString() +
                    "</rdf:Description></rdf:RDF>\n"
            val extMeta = XMPMetaFactory.parseFromString(str)
            XMPUtils.appendProperties(extMeta, xmpMeta, true, true)
        } catch (xmpExc: XMPException) {
            throw IOException(xmpExc.message)
        }

    }

    /**
     * @param schemaNS The namespace URI for the property. Has the same usage as in getProperty.
     * *
     * @param propName The name of the property.
     * *                 Has the same usage as in `getProperty()`.
     * *
     * @param value    the value for the property (only leaf properties have a value).
     * *                 Arrays and non-leaf levels of structs do not have values.
     * *                 Must be `null` if the value is not relevant.
     * *                 The value is automatically detected: Boolean, Integer, Long, Double, XMPDateTime and
     * *                 byte[] are handled, on all other `toString()` is called.
     * *
     * @throws XMPException Wraps all errors and exceptions that may occur.
     */
    @Throws(XMPException::class)
    fun setProperty(schemaNS: String, propName: String, value: Any) {
        xmpMeta.setProperty(schemaNS, propName, value)
    }

    /**
     * Simplifies the construction of an array by not requiring that you pre-create an empty array.
     * The array that is assigned is created automatically if it does not yet exist. Each call to
     * appendArrayItem() appends an item to the array.

     * @param schemaNS  The namespace URI for the array.
     * *
     * @param arrayName The name of the array. May be a general path expression, must not be null or
     * *                  the empty string.
     * *
     * @param value     the value of the array item.
     * *
     * @throws XMPException Wraps all errors and exceptions that may occur.
     */
    @Throws(XMPException::class)
    fun appendArrayItem(schemaNS: String, arrayName: String, value: String) {
        xmpMeta.appendArrayItem(schemaNS, arrayName, PropertyOptions(PropertyOptions.ARRAY), value, null)
    }

    /**
     * Simplifies the construction of an ordered array by not requiring that you pre-create an empty array.
     * The array that is assigned is created automatically if it does not yet exist. Each call to
     * appendArrayItem() appends an item to the array.

     * @param schemaNS  The namespace URI for the array.
     * *
     * @param arrayName The name of the array. May be a general path expression, must not be null or
     * *                  the empty string.
     * *
     * @param value     the value of the array item.
     * *
     * @throws XMPException Wraps all errors and exceptions that may occur.
     */
    @Throws(XMPException::class)
    fun appendOrderedArrayItem(schemaNS: String, arrayName: String, value: String) {
        xmpMeta.appendArrayItem(schemaNS, arrayName, PropertyOptions(PropertyOptions.ARRAY_ORDERED), value, null)
    }

    /**
     * Simplifies the construction of an alternate array by not requiring that you pre-create an empty array.
     * The array that is assigned is created automatically if it does not yet exist. Each call to
     * appendArrayItem() appends an item to the array.

     * @param schemaNS  The namespace URI for the array.
     * *
     * @param arrayName The name of the array. May be a general path expression, must not be null or
     * *                  the empty string.
     * *
     * @param value     the value of the array item.
     * *
     * @throws XMPException Wraps all errors and exceptions that may occur.
     */
    @Throws(XMPException::class)
    fun appendAlternateArrayItem(schemaNS: String, arrayName: String, value: String) {
        xmpMeta.appendArrayItem(schemaNS, arrayName, PropertyOptions(PropertyOptions.ARRAY_ALTERNATE), value, null)
    }

    /**
     * Flushes and closes the XmpWriter.
     * @throws IOException
     */
    @Throws(XMPException::class)
    fun serialize(externalOutputStream: OutputStream) {
        XMPMetaFactory.serialize(xmpMeta, externalOutputStream, serializeOptions)
    }

    /**
     * Flushes and closes the XmpWriter.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun close() {
        if (outputStream == null)
            return
        try {
            XMPMetaFactory.serialize(xmpMeta, outputStream, serializeOptions)
            outputStream = null
        } catch (xmpExc: XMPException) {
            throw IOException(xmpExc.message)
        }

    }

    @Throws(XMPException::class)
    fun addDocInfoProperty(key: Any, value: String) {
        var key = key
        if (key is String)
            key = PdfName(key)
        if (PdfName.TITLE.equals(key)) {
            xmpMeta.setLocalizedText(XMPConst.NS_DC, DublinCoreProperties.TITLE, XMPConst.X_DEFAULT, XMPConst.X_DEFAULT, value)
        } else if (PdfName.AUTHOR == key) {
            xmpMeta.appendArrayItem(XMPConst.NS_DC, DublinCoreProperties.CREATOR, PropertyOptions(PropertyOptions.ARRAY_ORDERED), value, null)
        } else if (PdfName.SUBJECT.equals(key)) {
            xmpMeta.setLocalizedText(XMPConst.NS_DC, DublinCoreProperties.DESCRIPTION, XMPConst.X_DEFAULT, XMPConst.X_DEFAULT, value)
        } else if (PdfName.KEYWORDS.equals(key)) {
            for (v in value.split(",|;".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                if (v.trim { it <= ' ' }.length > 0)
                    xmpMeta.appendArrayItem(XMPConst.NS_DC, DublinCoreProperties.SUBJECT, PropertyOptions(PropertyOptions.ARRAY), v.trim { it <= ' ' }, null)
            xmpMeta.setProperty(XMPConst.NS_PDF, PdfProperties.KEYWORDS, value)
        } else if (PdfName.PRODUCER.equals(key)) {
            xmpMeta.setProperty(XMPConst.NS_PDF, PdfProperties.PRODUCER, value)
        } else if (PdfName.CREATOR == key) {
            xmpMeta.setProperty(XMPConst.NS_XMP, XmpBasicProperties.CREATORTOOL, value)
        } else if (PdfName.CREATIONDATE == key) {
            xmpMeta.setProperty(XMPConst.NS_XMP, XmpBasicProperties.CREATEDATE, PdfDate.getW3CDate(value))
        } else if (PdfName.MODDATE.equals(key)) {
            xmpMeta.setProperty(XMPConst.NS_XMP, XmpBasicProperties.MODIFYDATE, PdfDate.getW3CDate(value))
        }
    }

    companion object {

        /** A possible charset for the XMP.  */
        val UTF8 = "UTF-8"
        /** A possible charset for the XMP.  */
        val UTF16 = "UTF-16"
        /** A possible charset for the XMP.  */
        val UTF16BE = "UTF-16BE"
        /** A possible charset for the XMP.  */
        val UTF16LE = "UTF-16LE"
    }
}
/**
 * Creates an XmpWriter.
 * @param os
 * *
 * @throws IOException
 */
