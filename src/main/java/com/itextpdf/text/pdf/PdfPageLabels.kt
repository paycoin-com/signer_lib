/*
 * $Id: 884d6bc67d86c9e270b68f7421e83bf8eafd0f82 $
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
import java.util.Arrays
import java.util.HashMap

import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.factories.RomanAlphabetFactory
import com.itextpdf.text.factories.RomanNumberFactory

/** Page labels are used to identify each
 * page visually on the screen or in print.
 * @author  Paulo Soares
 */
class PdfPageLabels {
    /** The sequence of logical pages. Will contain at least a value for page 1
     */
    private val map: HashMap<Int, PdfDictionary>

    init {
        map = HashMap<Int, PdfDictionary>()
        addPageLabel(1, DECIMAL_ARABIC_NUMERALS, null, 1)
    }

    /** Adds or replaces a page label.
     * @param page the real page to start the numbering. First page is 1
     * *
     * @param numberStyle the numbering style such as LOWERCASE_ROMAN_NUMERALS
     * *
     * @param text the text to prefix the number. Can be null or empty
     * *
     * @param firstPage the first logical page number
     */
    @JvmOverloads fun addPageLabel(page: Int, numberStyle: Int, text: String? = null, firstPage: Int = 1) {
        if (page < 1 || firstPage < 1)
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("in.a.page.label.the.page.numbers.must.be.greater.or.equal.to.1"))
        val dic = PdfDictionary()
        if (numberStyle >= 0 && numberStyle < numberingStyle.size)
            dic.put(PdfName.S, numberingStyle[numberStyle])
        if (text != null)
            dic.put(PdfName.P, PdfString(text, PdfObject.TEXT_UNICODE))
        //Not adding the first page by default since 1 is the default value
        if (firstPage != 1)
            dic.put(PdfName.ST, PdfNumber(firstPage))
        map.put(Integer.valueOf(page - 1), dic)
    }

    /** Adds or replaces a page label.
     * @param page the real page to start the numbering. First page is 1
     * *
     * @param numberStyle the numbering style such as LOWERCASE_ROMAN_NUMERALS
     * *
     * @param text the text to prefix the number. Can be null or empty
     * *
     * @param firstPage the first logical page number
     * *
     * @param includeFirstPage If true, the page label will be added to the first page if it is page 1.
     * * 	 If the first page is 1 and this value is false, the value will not be added to the dictionary.
     */
    fun addPageLabel(page: Int, numberStyle: Int, text: String?, firstPage: Int, includeFirstPage: Boolean) {
        if (page < 1 || firstPage < 1)
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("in.a.page.label.the.page.numbers.must.be.greater.or.equal.to.1"))
        val dic = PdfDictionary()
        if (numberStyle >= 0 && numberStyle < numberingStyle.size)
            dic.put(PdfName.S, numberingStyle[numberStyle])
        if (text != null)
            dic.put(PdfName.P, PdfString(text, PdfObject.TEXT_UNICODE))
        if (firstPage != 1 || includeFirstPage)
            dic.put(PdfName.ST, PdfNumber(firstPage))
        map.put(Integer.valueOf(page - 1), dic)
    }

    /** Adds or replaces a page label.
     */
    fun addPageLabel(format: PdfPageLabelFormat) {
        addPageLabel(format.physicalPage, format.numberStyle, format.prefix, format.logicalPage)
    }

    /** Removes a page label. The first page label can not be removed, only changed.
     * @param page the real page to remove
     */
    fun removePageLabel(page: Int) {
        if (page <= 1)
            return
        map.remove(Integer.valueOf(page - 1))
    }

    /** Gets the page label dictionary to insert into the document.
     * @return the page label dictionary
     */
    fun getDictionary(writer: PdfWriter): PdfDictionary {
        try {
            return PdfNumberTree.writeTree(map, writer)
        } catch (e: IOException) {
            throw ExceptionConverter(e)
        }

    }

    class PdfPageLabelFormat
    /** Creates a page label format.
     * @param physicalPage the real page to start the numbering. First page is 1
     * *
     * @param numberStyle the numbering style such as LOWERCASE_ROMAN_NUMERALS
     * *
     * @param prefix the text to prefix the number. Can be null or empty
     * *
     * @param logicalPage the first logical page number
     */
    (var physicalPage: Int, var numberStyle: Int, var prefix: String, var logicalPage: Int) {

        override fun toString(): String {
            return String.format("Physical page %s: style: %s; prefix '%s'; logical page: %s", physicalPage, numberStyle, prefix, logicalPage)
        }
    }

    companion object {

        /** Logical pages will have the form 1,2,3,...
         */
        val DECIMAL_ARABIC_NUMERALS = 0
        /** Logical pages will have the form I,II,III,IV,...
         */
        val UPPERCASE_ROMAN_NUMERALS = 1
        /** Logical pages will have the form i,ii,iii,iv,...
         */
        val LOWERCASE_ROMAN_NUMERALS = 2
        /** Logical pages will have the form of uppercase letters
         * (A to Z for the first 26 pages, AA to ZZ for the next 26, and so on)
         */
        val UPPERCASE_LETTERS = 3
        /** Logical pages will have the form of uppercase letters
         * (a to z for the first 26 pages, aa to zz for the next 26, and so on)
         */
        val LOWERCASE_LETTERS = 4
        /** No logical page numbers are generated but fixed text may
         * still exist
         */
        val EMPTY = 5
        /** Dictionary values to set the logical page styles
         */
        internal var numberingStyle = arrayOf(PdfName.D, PdfName.R, PdfName("r"), PdfName.A, PdfName("a"))

        /**
         * Retrieves the page labels from a PDF as an array of String objects.
         * @param reader a PdfReader object that has the page labels you want to retrieve
         * *
         * @return a String array or `null` if no page labels are present
         */
        fun getPageLabels(reader: PdfReader): Array<String>? {
            val n = reader.numberOfPages

            val dict = reader.catalog
            val labels = PdfReader.getPdfObjectRelease(dict.get(PdfName.PAGELABELS)) as PdfDictionary? ?: return null

            val labelstrings = arrayOfNulls<String>(n)

            val numberTree = PdfNumberTree.readTree(labels)

            var pagecount = 1
            var current: Int?
            var prefix = ""
            var type = 'D'
            for (i in 0..n - 1) {
                current = Integer.valueOf(i)
                if (numberTree.containsKey(current)) {
                    val d = PdfReader.getPdfObjectRelease(numberTree[current]) as PdfDictionary?
                    if (d.contains(PdfName.ST)) {
                        pagecount = (d.get(PdfName.ST) as PdfNumber).intValue()
                    } else {
                        pagecount = 1
                    }
                    if (d.contains(PdfName.P)) {
                        prefix = (d.get(PdfName.P) as PdfString).toUnicodeString()
                    } else {
                        prefix = ""
                    }
                    if (d.contains(PdfName.S)) {
                        type = (d.get(PdfName.S) as PdfName).toString()[1]
                    } else {
                        type = 'e'
                    }
                }
                when (type) {
                    else -> labelstrings[i] = prefix + pagecount
                    'R' -> labelstrings[i] = prefix + RomanNumberFactory.getUpperCaseString(pagecount)
                    'r' -> labelstrings[i] = prefix + RomanNumberFactory.getLowerCaseString(pagecount)
                    'A' -> labelstrings[i] = prefix + RomanAlphabetFactory.getUpperCaseString(pagecount)
                    'a' -> labelstrings[i] = prefix + RomanAlphabetFactory.getLowerCaseString(pagecount)
                    'e' -> labelstrings[i] = prefix
                }
                pagecount++
            }
            return labelstrings
        }

        /**
         * Retrieves the page labels from a PDF as an array of [PdfPageLabelFormat] objects.
         * @param reader a PdfReader object that has the page labels you want to retrieve
         * *
         * @return    a PdfPageLabelEntry array, containing an entry for each format change
         * * or `null` if no page labels are present
         */
        fun getPageLabelFormats(reader: PdfReader): Array<PdfPageLabelFormat>? {
            val dict = reader.catalog
            val labels = PdfReader.getPdfObjectRelease(dict.get(PdfName.PAGELABELS)) as PdfDictionary? ?: return null
            val numberTree = PdfNumberTree.readTree(labels)
            var numbers = arrayOfNulls<Int>(numberTree.size)
            numbers = numberTree.keys.toArray<Int>(numbers)
            Arrays.sort(numbers)
            val formats = arrayOfNulls<PdfPageLabelFormat>(numberTree.size)
            var prefix: String
            var numberStyle: Int
            var pagecount: Int
            for (k in numbers.indices) {
                val key = numbers[k]
                val d = PdfReader.getPdfObjectRelease(numberTree[key]) as PdfDictionary?
                if (d.contains(PdfName.ST)) {
                    pagecount = (d.get(PdfName.ST) as PdfNumber).intValue()
                } else {
                    pagecount = 1
                }
                if (d.contains(PdfName.P)) {
                    prefix = (d.get(PdfName.P) as PdfString).toUnicodeString()
                } else {
                    prefix = ""
                }
                if (d.contains(PdfName.S)) {
                    val type = (d.get(PdfName.S) as PdfName).toString()[1]
                    when (type) {
                        'R' -> numberStyle = UPPERCASE_ROMAN_NUMERALS
                        'r' -> numberStyle = LOWERCASE_ROMAN_NUMERALS
                        'A' -> numberStyle = UPPERCASE_LETTERS
                        'a' -> numberStyle = LOWERCASE_LETTERS
                        else -> numberStyle = DECIMAL_ARABIC_NUMERALS
                    }
                } else {
                    numberStyle = EMPTY
                }
                formats[k] = PdfPageLabelFormat(key!!.toInt() + 1, numberStyle, prefix, pagecount)
            }
            return formats
        }
    }
}
/** Creates a new PdfPageLabel with a default logical page 1
 */
/** Adds or replaces a page label. The first logical page has the default
 * of 1.
 * @param page the real page to start the numbering. First page is 1
 * *
 * @param numberStyle the numbering style such as LOWERCASE_ROMAN_NUMERALS
 * *
 * @param text the text to prefix the number. Can be null or empty
 */
/** Adds or replaces a page label. There is no text prefix and the first
 * logical page has the default of 1.
 * @param page the real page to start the numbering. First page is 1
 * *
 * @param numberStyle the numbering style such as LOWERCASE_ROMAN_NUMERALS
 */
