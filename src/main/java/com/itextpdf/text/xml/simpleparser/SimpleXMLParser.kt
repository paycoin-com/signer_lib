/*
 * $Id: efa03372d65bee0338742df34ef98788cac19119 $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2015 iText Group NV
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

/* The code to recognize the encoding in this class and in the convenience class IanaEncodings was taken from Apache Xerces published under the following license:
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Part of this code is based on the Quick-and-Dirty XML parser by Steven Brandt.
 * The code for the Quick-and-Dirty parser was published in JavaWorld (java tip 128).
 * Steven Brandt and JavaWorld gave permission to use the code for free.
 * (Bruno Lowagie and Paulo Soares chose to use it under the AGPL in conformance
 * with the rest of the code).
 * The original code can be found on this url: <A HREF="http://www.javaworld.com/javatips/jw-javatip128_p.html">http://www.javaworld.com/javatips/jw-javatip128_p.html</A>.
 * It was substantially refactored by Bruno Lowagie.
 *
 * The method 'private static String getEncodingName(byte[] b4)' was found
 * in org.apache.xerces.impl.XMLEntityManager, originaly published by the
 * Apache Software Foundation under the Apache Software License; now being
 * used in iText under the MPL.
 */
package com.itextpdf.text.xml.simpleparser

import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.util.HashMap
import java.util.Stack

import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.xml.XMLUtil
import com.itextpdf.text.xml.simpleparser.handler.HTMLNewLineHandler
import com.itextpdf.text.xml.simpleparser.handler.NeverNewLineHandler

/**
 * A simple XML.  This parser is, like the SAX parser,
 * an event based parser, but with much less functionality.
 *
 *
 * The parser can:
 *
 *
 *
 *  * It recognizes the encoding used
 *  * It recognizes all the elements' start tags and end tags
 *  * It lists attributes, where attribute values can be enclosed in single or double quotes
 *  * It recognizes the `&lt;[CDATA[ ... ]]&gt;` construct
 *  * It recognizes the standard entities: &amp;amp;, &amp;lt;, &amp;gt;, &amp;quot;, and &amp;apos;, as well as numeric entities
 *  * It maps lines ending in `\r\n` and `\r` to `\n` on input, in accordance with the XML Specification, Section 2.11
 *
 *
 *
 */
class SimpleXMLParser
/**
 * Creates a Simple XML parser object.
 * Call go(BufferedReader) immediately after creation.
 */
private constructor(
        /** The handler to which we are going to forward document content  */
        private val doc: SimpleXMLDocHandler,
        /** The handler to which we are going to forward comments.  */
        private val comment: SimpleXMLDocHandlerComment?,
        /** Are we parsing HTML?  */
        private val html: Boolean) {

    /** the state stack  */
    private val stack: Stack<Int>
    /** The current character.  */
    private var character = 0
    /** The previous character.  */
    private var previousCharacter = -1
    /** the line we are currently reading  */
    private var lines = 1
    /** the column where the current character occurs  */
    private var columns = 0
    /** was the last character equivalent to a newline?  */
    private var eol = false
    /**
     * A boolean indicating if the next character should be taken into account
     * if it's a space character. When nospace is false, the previous character
     * wasn't whitespace.
     * @since 2.1.5
     */
    private var nowhite = false
    /** the current state  */
    private var state: Int = 0
    /** current text (whatever is encountered between tags)  */
    private val text = StringBuffer()
    /** current entity (whatever is encountered between & and ;)  */
    private val entity = StringBuffer()
    /** current tagname  */
    private var tag: String? = null
    /** current attributes  */
    private var attributes: HashMap<String, String>? = null
    /** Keeps track of the number of tags that are open.  */
    private var nested = 0
    /** the quote character that was used to open the quote.  */
    private var quoteCharacter: Int = '"'
    /** the attribute key.  */
    private var attributekey: String? = null
    /** the attribute value.  */
    private var attributevalue: String? = null
    private var newLineHandler: NewLineHandler? = null

    init {
        if (html) {
            this.newLineHandler = HTMLNewLineHandler()
        } else {
            this.newLineHandler = NeverNewLineHandler()
        }
        stack = Stack<Int>()
        state = if (html) TEXT else UNKNOWN
    }

    /**
     * Does the actual parsing. Perform this immediately
     * after creating the parser object.
     */
    @Throws(IOException::class)
    private fun go(r: Reader) {
        val reader: BufferedReader
        if (r is BufferedReader)
            reader = r
        else
            reader = BufferedReader(r)
        doc.startDocument()
        while (true) {
            // read a new character
            if (previousCharacter == -1) {
                character = reader.read()
            } else {
                character = previousCharacter
                previousCharacter = -1
            }// or re-examine the previous character

            // the end of the file was reached
            if (character == -1) {
                if (html) {
                    if (html && state == TEXT)
                        flush()
                    doc.endDocument()
                } else {
                    throwException(MessageLocalization.getComposedMessage("missing.end.tag"))
                }
                return
            }

            // dealing with  \n and \r
            if (character == '\n' && eol) {
                eol = false
                continue
            } else if (eol) {
                eol = false
            } else if (character == '\n') {
                lines++
                columns = 0
            } else if (character == '\r') {
                eol = true
                character = '\n'
                lines++
                columns = 0
            } else {
                columns++
            }

            when (state) {
            // we are in an unknown state before there's actual content
                UNKNOWN -> if (character == '<') {
                    saveState(TEXT)
                    state = TAG_ENCOUNTERED
                }
            // we can encounter any content
                TEXT -> if (character == '<') {
                    flush()
                    saveState(state)
                    state = TAG_ENCOUNTERED
                } else if (character == '&') {
                    saveState(state)
                    entity.setLength(0)
                    state = ENTITY
                    nowhite = true
                } else if (character == ' ') {
                    if (html && nowhite) {
                        text.append(' ')
                        nowhite = false
                    } else {
                        if (nowhite) {
                            text.append(character.toChar())
                        }
                        nowhite = false
                    }
                } else if (Character.isWhitespace(character.toChar())) {
                    if (html) {
                        // totally ignore other whitespace
                    } else {
                        if (nowhite) {
                            text.append(character.toChar())
                        }
                        nowhite = false
                    }
                } else {
                    text.append(character.toChar())
                    nowhite = true
                }
            // we have just seen a < and are wondering what we are looking at
            // <foo>, </foo>, <!-- ... --->, etc.
                TAG_ENCOUNTERED -> {
                    initTag()
                    if (character == '/') {
                        state = IN_CLOSETAG
                    } else if (character == '?') {
                        restoreState()
                        state = PI
                    } else {
                        text.append(character.toChar())
                        state = EXAMIN_TAG
                    }
                }
            // we are processing something like this <foo ... >.
            // It could still be a <!-- ... --> or something.
                EXAMIN_TAG -> if (character == '>') {
                    doTag()
                    processTag(true)
                    initTag()
                    state = restoreState()
                } else if (character == '/') {
                    state = SINGLE_TAG
                } else if (character == '-' && text.toString() == "!-") {
                    flush()
                    state = COMMENT
                } else if (character == '[' && text.toString() == "![CDATA") {
                    flush()
                    state = CDATA
                } else if (character == 'E' && text.toString() == "!DOCTYP") {
                    flush()
                    state = PI
                } else if (Character.isWhitespace(character.toChar())) {
                    doTag()
                    state = TAG_EXAMINED
                } else {
                    text.append(character.toChar())
                }
            // we know the name of the tag now.
                TAG_EXAMINED -> if (character == '>') {
                    processTag(true)
                    initTag()
                    state = restoreState()
                } else if (character == '/') {
                    state = SINGLE_TAG
                } else if (Character.isWhitespace(character.toChar())) {
                    // empty
                } else {
                    text.append(character.toChar())
                    state = ATTRIBUTE_KEY
                }

            // we are processing a closing tag: e.g. </foo>
                IN_CLOSETAG -> if (character == '>') {
                    doTag()
                    processTag(false)
                    if (!html && nested == 0) return
                    state = restoreState()
                } else {
                    if (!Character.isWhitespace(character.toChar()))
                        text.append(character.toChar())
                }

            // we have just seen something like this: <foo a="b"/
            // and are looking for the final >.
                SINGLE_TAG -> {
                    if (character != '>')
                        throwException(MessageLocalization.getComposedMessage("expected.gt.for.tag.lt.1.gt", tag))
                    doTag()
                    processTag(true)
                    processTag(false)
                    initTag()
                    if (!html && nested == 0) {
                        doc.endDocument()
                        return
                    }
                    state = restoreState()
                }

            // we are processing CDATA
                CDATA -> if (character == '>' && text.toString().endsWith("]]")) {
                    text.setLength(text.length - 2)
                    flush()
                    state = restoreState()
                } else
                    text.append(character.toChar())

            // we are processing a comment.  We are inside
            // the <!-- .... --> looking for the -->.
                COMMENT -> if (character == '>' && text.toString().endsWith("--")) {
                    text.setLength(text.length - 2)
                    flush()
                    state = restoreState()
                } else
                    text.append(character.toChar())

            // We are inside one of these <? ... ?> or one of these <!DOCTYPE ... >
                PI -> if (character == '>') {
                    state = restoreState()
                    if (state == TEXT) state = UNKNOWN
                }

            // we are processing an entity, e.g. &lt;, &#187;, etc.
                ENTITY -> if (character == ';') {
                    state = restoreState()
                    val cent = entity.toString()
                    entity.setLength(0)
                    val ce = EntitiesToUnicode.decodeEntity(cent)
                    if (ce == '\0')
                        text.append('&').append(cent).append(';')
                    else
                        text.append(ce)
                } else if (character != '#' && (character < '0' || character > '9') && (character < 'a' || character > 'z')
                        && (character < 'A' || character > 'Z') || entity.length >= 7) {
                    state = restoreState()
                    previousCharacter = character
                    text.append('&').append(entity.toString())
                    entity.setLength(0)
                } else {
                    entity.append(character.toChar())
                }
            // We are processing the quoted right-hand side of an element's attribute.
                QUOTE -> if (html && quoteCharacter == ' ' && character == '>') {
                    flush()
                    processTag(true)
                    initTag()
                    state = restoreState()
                } else if (html && quoteCharacter == ' ' && Character.isWhitespace(character.toChar())) {
                    flush()
                    state = TAG_EXAMINED
                } else if (html && quoteCharacter == ' ') {
                    text.append(character.toChar())
                } else if (character == quoteCharacter) {
                    flush()
                    state = TAG_EXAMINED
                } else if (" \r\n\u0009".indexOf(character) >= 0) {
                    text.append(' ')
                } else if (character == '&') {
                    saveState(state)
                    state = ENTITY
                    entity.setLength(0)
                } else {
                    text.append(character.toChar())
                }

                ATTRIBUTE_KEY -> if (Character.isWhitespace(character.toChar())) {
                    flush()
                    state = ATTRIBUTE_EQUAL
                } else if (character == '=') {
                    flush()
                    state = ATTRIBUTE_VALUE
                } else if (html && character == '>') {
                    text.setLength(0)
                    processTag(true)
                    initTag()
                    state = restoreState()
                } else {
                    text.append(character.toChar())
                }

                ATTRIBUTE_EQUAL -> if (character == '=') {
                    state = ATTRIBUTE_VALUE
                } else if (Character.isWhitespace(character.toChar())) {
                    // empty
                } else if (html && character == '>') {
                    text.setLength(0)
                    processTag(true)
                    initTag()
                    state = restoreState()
                } else if (html && character == '/') {
                    flush()
                    state = SINGLE_TAG
                } else if (html) {
                    flush()
                    text.append(character.toChar())
                    state = ATTRIBUTE_KEY
                } else {
                    throwException(MessageLocalization.getComposedMessage("error.in.attribute.processing"))
                }

                ATTRIBUTE_VALUE -> if (character == '"' || character == '\'') {
                    quoteCharacter = character
                    state = QUOTE
                } else if (Character.isWhitespace(character.toChar())) {
                    // empty
                } else if (html && character == '>') {
                    flush()
                    processTag(true)
                    initTag()
                    state = restoreState()
                } else if (html) {
                    text.append(character.toChar())
                    quoteCharacter = ' '
                    state = QUOTE
                } else {
                    throwException(MessageLocalization.getComposedMessage("error.in.attribute.processing"))
                }
            }
        }
    }

    /**
     * Gets a state from the stack
     * @return the previous state
     */
    private fun restoreState(): Int {
        if (!stack.empty())
            return stack.pop().toInt()
        else
            return UNKNOWN
    }

    /**
     * Adds a state to the stack.
     * @param    s    a state to add to the stack
     */
    private fun saveState(s: Int) {
        stack.push(Integer.valueOf(s))
    }

    /**
     * Flushes the text that is currently in the buffer.
     * The text can be ignored, added to the document
     * as content or as comment,... depending on the current state.
     */
    private fun flush() {
        when (state) {
            TEXT, CDATA -> if (text.length > 0) {
                doc.text(text.toString())
            }
            COMMENT -> comment?.comment(text.toString())
            ATTRIBUTE_KEY -> {
                attributekey = text.toString()
                if (html)
                    attributekey = attributekey!!.toLowerCase()
            }
            QUOTE, ATTRIBUTE_VALUE -> {
                attributevalue = text.toString()
                attributes!!.put(attributekey, attributevalue)
            }
        }// do nothing
        text.setLength(0)
    }

    /**
     * Initialized the tag name and attributes.
     */
    private fun initTag() {
        tag = null
        attributes = HashMap<String, String>()
    }

    /** Sets the name of the tag.  */
    private fun doTag() {
        if (tag == null)
            tag = text.toString()
        if (html)
            tag = tag!!.toLowerCase()
        text.setLength(0)
    }

    /**
     * processes the tag.
     * @param start    if true we are dealing with a tag that has just been opened; if false we are closing a tag.
     */
    private fun processTag(start: Boolean) {
        if (start) {
            nested++
            doc.startElement(tag, attributes)
        } else {
            // White spaces following new lines need to be ignored in HTML
            if (newLineHandler!!.isNewLineTag(tag)) {
                nowhite = false
            }
            nested--
            doc.endElement(tag)
        }
    }

    /** Throws an exception  */
    @Throws(IOException::class)
    private fun throwException(s: String) {
        throw IOException(MessageLocalization.getComposedMessage("1.near.line.2.column.3", s, lines.toString(), columns.toString()))
    }

    companion object {
        /** possible states  */
        private val UNKNOWN = 0
        private val TEXT = 1
        private val TAG_ENCOUNTERED = 2
        private val EXAMIN_TAG = 3
        private val TAG_EXAMINED = 4
        private val IN_CLOSETAG = 5
        private val SINGLE_TAG = 6
        private val CDATA = 7
        private val COMMENT = 8
        private val PI = 9
        private val ENTITY = 10
        private val QUOTE = 11
        private val ATTRIBUTE_KEY = 12
        private val ATTRIBUTE_EQUAL = 13
        private val ATTRIBUTE_VALUE = 14

        /**
         * Parses the XML document firing the events to the handler.
         * @param doc the document handler
         * *
         * @param comment the comment handler
         * *
         * @param r the document. The encoding is already resolved. The reader is not closed
         * *
         * @param html
         * *
         * @throws IOException on error
         */
        @Throws(IOException::class)
        fun parse(doc: SimpleXMLDocHandler, comment: SimpleXMLDocHandlerComment?, r: Reader, html: Boolean) {
            val parser = SimpleXMLParser(doc, comment, html)
            parser.go(r)
        }

        /**
         * Parses the XML document firing the events to the handler.
         * @param doc the document handler
         * *
         * @param in the document. The encoding is deduced from the stream. The stream is not closed
         * *
         * @throws IOException on error
         */
        @Throws(IOException::class)
        fun parse(doc: SimpleXMLDocHandler, `in`: InputStream) {
            val b4 = ByteArray(4)
            val count = `in`.read(b4)
            if (count != 4)
                throw IOException(MessageLocalization.getComposedMessage("insufficient.length"))
            var encoding = XMLUtil.getEncodingName(b4)
            var decl: String? = null
            if (encoding == "UTF-8") {
                val sb = StringBuffer()
                var c: Int
                while ((c = `in`.read()) != -1) {
                    if (c == '>')
                        break
                    sb.append(c.toChar())
                }
                decl = sb.toString()
            } else if (encoding == "CP037") {
                val bi = ByteArrayOutputStream()
                var c: Int
                while ((c = `in`.read()) != -1) {
                    if (c == 0x6e)
                    // that's '>' in ebcdic
                        break
                    bi.write(c)
                }
                decl = String(bi.toByteArray(), "CP037")
            }
            if (decl != null) {
                decl = getDeclaredEncoding(decl)
                if (decl != null)
                    encoding = decl
            }
            parse(doc, InputStreamReader(`in`, IanaEncodings.getJavaEncoding(encoding)))
        }

        private fun getDeclaredEncoding(decl: String?): String? {
            if (decl == null)
                return null
            val idx = decl.indexOf("encoding")
            if (idx < 0)
                return null
            val idx1 = decl.indexOf('"', idx)
            val idx2 = decl.indexOf('\'', idx)
            if (idx1 == idx2)
                return null
            if (idx1 < 0 && idx2 > 0 || idx2 > 0 && idx2 < idx1) {
                val idx3 = decl.indexOf('\'', idx2 + 1)
                if (idx3 < 0)
                    return null
                return decl.substring(idx2 + 1, idx3)
            }
            if (idx2 < 0 && idx1 > 0 || idx1 > 0 && idx1 < idx2) {
                val idx3 = decl.indexOf('"', idx1 + 1)
                if (idx3 < 0)
                    return null
                return decl.substring(idx1 + 1, idx3)
            }
            return null
        }

        /**
         * @param doc
         * *
         * @param r
         * *
         * @throws IOException
         */
        @Throws(IOException::class)
        fun parse(doc: SimpleXMLDocHandler, r: Reader) {
            parse(doc, null, r, false)
        }

        /**
         * Escapes a string with the appropriated XML codes.

         * @param s
         * *            the string to be escaped
         * *
         * @param onlyASCII
         * *            codes above 127 will always be escaped with &amp;#nn; if
         * *            true
         * *
         * @return the escaped string
         * *
         */
        @Deprecated("")
        @Deprecated("moved to {@link XMLUtil#escapeXML(String, boolean)}, left\n\t              here for the sake of backwards compatibility")
        fun escapeXML(s: String, onlyASCII: Boolean): String {
            return XMLUtil.escapeXML(s, onlyASCII)
        }
    }

}
