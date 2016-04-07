/*
 * $Id: d2fa73f4c7b8324d68a2feadfdf439b0e56f28ba $
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
package com.itextpdf.text.pdf.events

import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.HashMap
import java.util.TreeMap

import com.itextpdf.text.Chunk
import com.itextpdf.text.Document
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfPageEventHelper
import com.itextpdf.text.pdf.PdfWriter

/**
 * Class for an index.

 * @author Michael Niedermair
 */
class IndexEvents : PdfPageEventHelper() {

    /**
     * keeps the indextag with the pagenumber
     */
    private val indextag = TreeMap<String, Int>()

    /**
     * All the text that is passed to this event, gets registered in the indexentry.

     * @see com.itextpdf.text.pdf.PdfPageEventHelper.onGenericTag
     */
    override fun onGenericTag(writer: PdfWriter, document: Document,
                              rect: Rectangle, text: String) {
        indextag.put(text, Integer.valueOf(writer.pageNumber))
    }

    // --------------------------------------------------------------------
    /**
     * indexcounter
     */
    private var indexcounter: Long = 0

    /**
     * the list for the index entry
     */
    private val indexentry = ArrayList<Entry>()

    /**
     * Create an index entry.

     * @param text  The text for the Chunk.
     * *
     * @param in1   The first level.
     * *
     * @param in2   The second level.
     * *
     * @param in3   The third level.
     * *
     * @return Returns the Chunk.
     */
    @JvmOverloads fun create(text: String, in1: String, in2: String = "",
                             in3: String = ""): Chunk {

        val chunk = Chunk(text)
        val tag = "idx_" + indexcounter++
        chunk.setGenericTag(tag)
        chunk.setLocalDestination(tag)
        val entry = Entry(in1, in2, in3, tag)
        indexentry.add(entry)
        return chunk
    }

    /**
     * Create an index entry.

     * @param text  The text.
     * *
     * @param in1   The first level.
     * *
     * @param in2   The second level.
     * *
     * @param in3   The third level.
     */
    @JvmOverloads fun create(text: Chunk, in1: String, in2: String = "",
                             in3: String = "") {

        val tag = "idx_" + indexcounter++
        text.setGenericTag(tag)
        text.setLocalDestination(tag)
        val entry = Entry(in1, in2, in3, tag)
        indexentry.add(entry)
    }

    /**
     * Comparator for sorting the index
     */
    private var comparator: Comparator<Entry> = Comparator { en1, en2 ->
        var rt = 0
        if (en1.in1 != null && en2.in1 != null) {
            if ((rt = en1.in1.compareTo(en2.in1, ignoreCase = true)) == 0) {
                // in1 equals
                if (en1.in2 != null && en2.in2 != null) {
                    if ((rt = en1.in2.compareTo(en2.in2, ignoreCase = true)) == 0) {
                        // in2 equals
                        if (en1.in3 != null && en2.in3 != null) {
                            rt = en1.in3.compareTo(en2.in3, ignoreCase = true)
                        }
                    }
                }
            }
        }
        rt
    }

    /**
     * Set the comparator.
     * @param aComparator The comparator to set.
     */
    fun setComparator(aComparator: Comparator<Entry>) {
        comparator = aComparator
    }

    /**
     * Returns the sorted list with the entries and the collected page numbers.
     * @return Returns the sorted list with the entries and the collected page numbers.
     */
    // copy to a list and sort it
    val sortedEntries: List<Entry>
        get() {

            val grouped = HashMap<String, Entry>()

            for (i in indexentry.indices) {
                val e = indexentry[i]
                val key = e.key

                val master = grouped[key]
                if (master != null) {
                    master.addPageNumberAndTag(e.pageNumber, e.tag)
                } else {
                    e.addPageNumberAndTag(e.pageNumber, e.tag)
                    grouped.put(key, e)
                }
            }
            val sorted = ArrayList(grouped.values)
            Collections.sort(sorted, comparator)
            return sorted
        }

    // --------------------------------------------------------------------
    /**
     * Class for an index entry.
     *
     *
     * In the first step, only in1, in2,in3 and tag are used.
     * After the collections of the index entries, pagenumbers are used.
     *
     */
    inner class Entry
    /**
     * Create a new object.
     * @param aIn1   The first level.
     * *
     * @param aIn2   The second level.
     * *
     * @param aIn3   The third level.
     * *
     * @param aTag   The tag.
     */
    (
            /**
             * first level
             */
            /**
             * Returns the in1.
             * @return Returns the in1.
             */
            val in1: String,
            /**
             * second level
             */
            /**
             * Returns the in2.
             * @return Returns the in2.
             */
            val in2: String,
            /**
             * third level
             */
            /**
             * Returns the in3.
             * @return Returns the in3.
             */
            val in3: String,
            /**
             * the tag
             */
            /**
             * Returns the tag.
             * @return Returns the tag.
             */
            val tag: String) {

        /**
         * the list of all page numbers.
         */
        private val pagenumbers = ArrayList<Int>()

        /**
         * the list of all tags.
         */
        private val tags = ArrayList<String>()

        /**
         * Returns the pagenumber for this entry.
         * @return Returns the pagenumber for this entry.
         */
        val pageNumber: Int
            get() {
                var rt = -1
                val i = indextag[tag]
                if (i != null) {
                    rt = i.toInt()
                }
                return rt
            }

        /**
         * Add a pagenumber.
         * @param number    The page number.
         * *
         * @param tag
         */
        fun addPageNumberAndTag(number: Int, tag: String) {
            pagenumbers.add(Integer.valueOf(number))
            tags.add(tag)
        }

        /**
         * Returns the key for the map-entry.
         * @return Returns the key for the map-entry.
         */
        val key: String
            get() = "$in1!$in2!$in3"

        /**
         * Returns the pagenumbers.
         * @return Returns the pagenumbers.
         */
        fun getPagenumbers(): List<Int> {
            return pagenumbers
        }

        /**
         * Returns the tags.
         * @return Returns the tags.
         */
        fun getTags(): List<String> {
            return tags
        }

        /**
         * print the entry (only for test)
         * @return the toString implementation of the entry
         */
        override fun toString(): String {
            val buf = StringBuffer()
            buf.append(in1).append(' ')
            buf.append(in2).append(' ')
            buf.append(in3).append(' ')
            for (i in pagenumbers.indices) {
                buf.append(pagenumbers[i]).append(' ')
            }
            return buf.toString()
        }
    }
}
/**
 * Create an index entry.

 * @param text  The text for the Chunk.
 * *
 * @param in1   The first level.
 * *
 * @return Returns the Chunk.
 */
/**
 * Create an index entry.

 * @param text  The text for the Chunk.
 * *
 * @param in1   The first level.
 * *
 * @param in2   The second level.
 * *
 * @return Returns the Chunk.
 */
/**
 * Create an index entry.

 * @param text  The text.
 * *
 * @param in1   The first level.
 */
/**
 * Create an index entry.

 * @param text  The text.
 * *
 * @param in1   The first level.
 * *
 * @param in2   The second level.
 */
