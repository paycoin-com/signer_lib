/*
 * $Id: 6f72d6c79e2f79af87af1044cbece91896811b17 $
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

import java.io.IOException
import java.util.ArrayList

import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.PRTokeniser.TokenType

/**
 * Parses the page or template content.
 * @author Paulo Soares
 */
class PdfContentParser
/**
 * Creates a new instance of PdfContentParser
 * @param tokeniser the tokeniser with the content
 */
(
        /**
         * Holds value of property tokeniser.
         */
        /**
         * Gets the tokeniser.
         * @return the tokeniser.
         */
        /**
         * Sets the tokeniser.
         * @param tokeniser the tokeniser
         */
        var tokeniser: PRTokeniser?) {

    /**
     * Parses a single command from the content. Each command is output as an array of arguments
     * having the command itself as the last element. The returned array will be empty if the
     * end of content was reached.
     * @param ls an ArrayList to use. It will be cleared before using. If it's
     * * null will create a new ArrayList
     * *
     * @return the same ArrayList given as argument or a new one
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class)
    fun parse(ls: ArrayList<PdfObject>?): ArrayList<PdfObject> {
        var ls = ls
        if (ls == null)
            ls = ArrayList<PdfObject>()
        else
            ls.clear()
        var ob: PdfObject? = null
        while ((ob = readPRObject()) != null) {
            ls.add(ob)
            if (ob!!.type() == COMMAND_TYPE)
                break
        }
        return ls
    }

    /**
     * Reads a dictionary. The tokeniser must be positioned past the "&lt;&lt;" token.
     * @return the dictionary
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class)
    fun readDictionary(): PdfDictionary {
        val dic = PdfDictionary()
        while (true) {
            if (!nextValidToken())
                throw IOException(MessageLocalization.getComposedMessage("unexpected.end.of.file"))
            if (tokeniser!!.tokenType == TokenType.END_DIC)
                break
            if (tokeniser!!.tokenType == TokenType.OTHER && "def" == tokeniser!!.stringValue)
                continue
            if (tokeniser!!.tokenType != TokenType.NAME)
                throw IOException(MessageLocalization.getComposedMessage("dictionary.key.1.is.not.a.name", tokeniser!!.stringValue))
            val name = PdfName(tokeniser!!.stringValue, false)
            val obj = readPRObject()
            val type = obj.type()
            if (-type == TokenType.END_DIC.ordinal)
                throw IOException(MessageLocalization.getComposedMessage("unexpected.gt.gt"))
            if (-type == TokenType.END_ARRAY.ordinal)
                throw IOException(MessageLocalization.getComposedMessage("unexpected.close.bracket"))
            dic.put(name, obj)
        }
        return dic
    }

    /**
     * Reads an array. The tokeniser must be positioned past the "[" token.
     * @return an array
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class)
    fun readArray(): PdfArray {
        val array = PdfArray()
        while (true) {
            val obj = readPRObject()
            val type = obj.type()
            if (-type == TokenType.END_ARRAY.ordinal)
                break
            if (-type == TokenType.END_DIC.ordinal)
                throw IOException(MessageLocalization.getComposedMessage("unexpected.gt.gt"))
            array.add(obj)
        }
        return array
    }

    /**
     * Reads a pdf object.
     * @return the pdf object
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class)
    fun readPRObject(): PdfObject? {
        if (!nextValidToken())
            return null
        val type = tokeniser!!.tokenType
        when (type) {
            PRTokeniser.TokenType.START_DIC -> {
                val dic = readDictionary()
                return dic
            }
            PRTokeniser.TokenType.START_ARRAY -> return readArray()
            PRTokeniser.TokenType.STRING -> {
                val str = PdfString(tokeniser!!.stringValue, null).setHexWriting(tokeniser!!.isHexString)
                return str
            }
            PRTokeniser.TokenType.NAME -> return PdfName(tokeniser!!.stringValue, false)
            PRTokeniser.TokenType.NUMBER -> return PdfNumber(tokeniser!!.stringValue)
            PRTokeniser.TokenType.OTHER -> return PdfLiteral(COMMAND_TYPE, tokeniser!!.stringValue)
            else -> return PdfLiteral(-type.ordinal, tokeniser!!.stringValue)
        }
    }

    /**
     * Reads the next token skipping over the comments.
     * @return true if a token was read, false if the end of content was reached
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class)
    fun nextValidToken(): Boolean {
        while (tokeniser!!.nextToken()) {
            if (tokeniser!!.tokenType == TokenType.COMMENT)
                continue
            return true
        }
        return false
    }

    companion object {

        /**
         * Commands have this type.
         */
        val COMMAND_TYPE = 200
    }
}
