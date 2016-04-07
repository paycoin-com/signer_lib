/*
 * $Id: bc8bc674f22f48b5ac446cc49452187ce6855e06 $
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
package com.itextpdf.text.pdf.codec

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.SortedMap
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet

import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.RandomAccessFileOrArray

/**
 * Class to read a JBIG2 file at a basic level: understand all the segments,
 * understand what segments belong to which pages, how many pages there are,
 * what the width and height of each page is, and global segments if there
 * are any.  Or: the minimum required to be able to take a normal sequential
 * or random-access organized file, and be able to embed JBIG2 pages as images
 * in a PDF.

 * TODO: the indeterminate-segment-size value of dataLength, else?

 * @since 2.1.5
 */

class JBIG2SegmentReader @Throws(IOException::class)
constructor(private val ra: RandomAccessFileOrArray) {

    private val segments = TreeMap<Int, JBIG2Segment>()
    private val pages = TreeMap<Int, JBIG2Page>()
    private val globals = TreeSet<JBIG2Segment>()
    private var sequential: Boolean = false
    private var number_of_pages_known: Boolean = false
    private var number_of_pages = -1
    private var read = false

    /**
     * Inner class that holds information about a JBIG2 segment.
     * @since    2.1.5
     */
    class JBIG2Segment(val segmentNumber: Int) : Comparable<JBIG2Segment> {
        var dataLength: Long = -1
        var page = -1
        var referredToSegmentNumbers: IntArray? = null
        var segmentRetentionFlags: BooleanArray? = null
        var type = -1
        var deferredNonRetain = false
        var countOfReferredToSegments = -1
        var data: ByteArray? = null
        var headerData: ByteArray? = null
        var page_association_size = false
        var page_association_offset = -1

        override fun compareTo(s: JBIG2Segment): Int {
            return this.segmentNumber - s.segmentNumber
        }


    }

    /**
     * Inner class that holds information about a JBIG2 page.
     * @since    2.1.5
     */
    class JBIG2Page(val page: Int, private val sr: JBIG2SegmentReader) {
        private val segs = TreeMap<Int, JBIG2Segment>()
        var pageBitmapWidth = -1
        var pageBitmapHeight = -1
        /**
         * return as a single byte array the header-data for each segment in segment number
         * order, EMBEDDED organization, but i am putting the needed segments in SEQUENTIAL organization.
         * if for_embedding, skip the segment types that are known to be not for acrobat.
         * @param for_embedding
         * *
         * @return    a byte array
         * *
         * @throws IOException
         */
        @Throws(IOException::class)
        fun getData(for_embedding: Boolean): ByteArray {
            val os = ByteArrayOutputStream()
            for (sn in segs.keys) {
                val s = segs[sn]

                // pdf reference 1.4, section 3.3.6 JBIG2Decode Filter
                // D.3 Embedded organisation
                if (for_embedding && (s.type == END_OF_FILE || s.type == END_OF_PAGE)) {
                    continue
                }

                if (for_embedding) {
                    // change the page association to page 1
                    val headerData_emb = copyByteArray(s.headerData)
                    if (s.page_association_size) {
                        headerData_emb[s.page_association_offset] = 0x0
                        headerData_emb[s.page_association_offset + 1] = 0x0
                        headerData_emb[s.page_association_offset + 2] = 0x0
                        headerData_emb[s.page_association_offset + 3] = 0x1
                    } else {
                        headerData_emb[s.page_association_offset] = 0x1
                    }
                    os.write(headerData_emb)
                } else {
                    os.write(s.headerData)
                }
                os.write(s.data)
            }
            os.close()
            return os.toByteArray()
        }

        fun addSegment(s: JBIG2Segment) {
            segs.put(Integer.valueOf(s.segmentNumber), s)
        }

    }

    @Throws(IOException::class)
    fun read() {
        if (this.read) {
            throw IllegalStateException(MessageLocalization.getComposedMessage("already.attempted.a.read.on.this.jbig2.file"))
        }
        this.read = true

        readFileHeader()
        // Annex D
        if (this.sequential) {
            // D.1
            do {
                val tmp = readHeader()
                readSegment(tmp)
                segments.put(Integer.valueOf(tmp.segmentNumber), tmp)
            } while (this.ra.filePointer < this.ra.length())
        } else {
            // D.2
            var tmp: JBIG2Segment
            do {
                tmp = readHeader()
                segments.put(Integer.valueOf(tmp.segmentNumber), tmp)
            } while (tmp.type != END_OF_FILE)
            val segs = segments.keys.iterator()
            while (segs.hasNext()) {
                readSegment(segments[segs.next()])
            }
        }
    }

    @Throws(IOException::class)
    internal fun readSegment(s: JBIG2Segment) {
        val ptr = ra.filePointer.toInt()

        if (s.dataLength == 0xffffffffL) {
            // TODO figure this bit out, 7.2.7
            return
        }

        val data = ByteArray(s.dataLength.toInt())
        ra.read(data)
        s.data = data

        if (s.type == PAGE_INFORMATION) {
            val last = ra.filePointer.toInt()
            ra.seek(ptr.toLong())
            val page_bitmap_width = ra.readInt()
            val page_bitmap_height = ra.readInt()
            ra.seek(last.toLong())
            val p = pages[Integer.valueOf(s.page)] ?: throw IllegalStateException(MessageLocalization.getComposedMessage("referring.to.widht.height.of.page.we.havent.seen.yet.1", s.page))

            p.pageBitmapWidth = page_bitmap_width
            p.pageBitmapHeight = page_bitmap_height
        }
    }

    @Throws(IOException::class)
    internal fun readHeader(): JBIG2Segment {
        val ptr = ra.filePointer.toInt()
        // 7.2.1
        val segment_number = ra.readInt()
        val s = JBIG2Segment(segment_number)

        // 7.2.3
        val segment_header_flags = ra.read()
        val deferred_non_retain = segment_header_flags and 0x80 == 0x80
        s.deferredNonRetain = deferred_non_retain
        val page_association_size = segment_header_flags and 0x40 == 0x40
        val segment_type = segment_header_flags and 0x3f
        s.type = segment_type

        //7.2.4
        var referred_to_byte0 = ra.read()
        var count_of_referred_to_segments = referred_to_byte0 and 0xE0 shr 5
        var referred_to_segment_numbers: IntArray? = null
        var segment_retention_flags: BooleanArray? = null

        if (count_of_referred_to_segments == 7) {
            // at least five bytes
            ra.seek(ra.filePointer - 1)
            count_of_referred_to_segments = ra.readInt() and 0x1fffffff
            segment_retention_flags = BooleanArray(count_of_referred_to_segments + 1)
            var i = 0
            var referred_to_current_byte = 0
            do {
                val j = i % 8
                if (j == 0) {
                    referred_to_current_byte = ra.read()
                }
                segment_retention_flags[i] = 0x1 shl j and referred_to_current_byte shr j == 0x1
                i++
            } while (i <= count_of_referred_to_segments)

        } else if (count_of_referred_to_segments <= 4) {
            // only one byte
            segment_retention_flags = BooleanArray(count_of_referred_to_segments + 1)
            referred_to_byte0 = referred_to_byte0 and 0x1f
            for (i in 0..count_of_referred_to_segments) {
                segment_retention_flags[i] = 0x1 shl i and referred_to_byte0 shr i == 0x1
            }

        } else if (count_of_referred_to_segments == 5 || count_of_referred_to_segments == 6) {
            throw IllegalStateException(MessageLocalization.getComposedMessage("count.of.referred.to.segments.had.bad.value.in.header.for.segment.1.starting.at.2", segment_number.toString(), ptr.toString()))
        }
        s.segmentRetentionFlags = segment_retention_flags
        s.countOfReferredToSegments = count_of_referred_to_segments

        // 7.2.5
        referred_to_segment_numbers = IntArray(count_of_referred_to_segments + 1)
        for (i in 1..count_of_referred_to_segments) {
            if (segment_number <= 256) {
                referred_to_segment_numbers[i] = ra.read()
            } else if (segment_number <= 65536) {
                referred_to_segment_numbers[i] = ra.readUnsignedShort()
            } else {
                referred_to_segment_numbers[i] = ra.readUnsignedInt().toInt() // TODO wtf ack
            }
        }
        s.referredToSegmentNumbers = referred_to_segment_numbers

        // 7.2.6
        val segment_page_association: Int
        val page_association_offset = ra.filePointer.toInt() - ptr
        if (page_association_size) {
            segment_page_association = ra.readInt()
        } else {
            segment_page_association = ra.read()
        }
        if (segment_page_association < 0) {
            throw IllegalStateException(MessageLocalization.getComposedMessage("page.1.invalid.for.segment.2.starting.at.3", segment_page_association.toString(), segment_number.toString(), ptr.toString()))
        }
        s.page = segment_page_association
        // so we can change the page association at embedding time.
        s.page_association_size = page_association_size
        s.page_association_offset = page_association_offset

        if (segment_page_association > 0 && !pages.containsKey(Integer.valueOf(segment_page_association))) {
            pages.put(Integer.valueOf(segment_page_association), JBIG2Page(segment_page_association, this))
        }
        if (segment_page_association > 0) {
            pages[Integer.valueOf(segment_page_association)].addSegment(s)
        } else {
            globals.add(s)
        }

        // 7.2.7
        val segment_data_length = ra.readUnsignedInt()
        // TODO the 0xffffffff value that might be here, and how to understand those afflicted segments
        s.dataLength = segment_data_length

        val end_ptr = ra.filePointer.toInt()
        ra.seek(ptr.toLong())
        val header_data = ByteArray(end_ptr - ptr)
        ra.read(header_data)
        s.headerData = header_data

        return s
    }

    @Throws(IOException::class)
    internal fun readFileHeader() {
        ra.seek(0)
        val idstring = ByteArray(8)
        ra.read(idstring)

        val refidstring = byteArrayOf(0x97.toByte(), 0x4A, 0x42, 0x32, 0x0D, 0x0A, 0x1A, 0x0A)

        for (i in idstring.indices) {
            if (idstring[i] != refidstring[i]) {
                throw IllegalStateException(MessageLocalization.getComposedMessage("file.header.idstring.not.good.at.byte.1", i))
            }
        }

        val fileheaderflags = ra.read()

        this.sequential = fileheaderflags and 0x1 == 0x1
        this.number_of_pages_known = fileheaderflags and 0x2 == 0x0

        if (fileheaderflags and 0xfc != 0x0) {
            throw IllegalStateException(MessageLocalization.getComposedMessage("file.header.flags.bits.2.7.not.0"))
        }

        if (this.number_of_pages_known) {
            this.number_of_pages = ra.readInt()
        }
    }

    fun numberOfPages(): Int {
        return pages.size
    }

    fun getPageHeight(i: Int): Int {
        return pages[Integer.valueOf(i)].pageBitmapHeight
    }

    fun getPageWidth(i: Int): Int {
        return pages[Integer.valueOf(i)].pageBitmapWidth
    }

    fun getPage(page: Int): JBIG2Page {
        return pages[Integer.valueOf(page)]
    }

    fun getGlobal(for_embedding: Boolean): ByteArray? {
        val os = ByteArrayOutputStream()
        try {
            for (element in globals) {
                if (for_embedding && (element.type == END_OF_FILE || element.type == END_OF_PAGE)) {
                    continue
                }
                os.write(element.headerData)
                os.write(element.data)
            }
            os.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (os.size() <= 0) {
            return null
        }
        return os.toByteArray()
    }

    override fun toString(): String {
        if (this.read) {
            return "Jbig2SegmentReader: number of pages: " + this.numberOfPages()
        } else {
            return "Jbig2SegmentReader in indeterminate state."
        }
    }

    companion object {

        val SYMBOL_DICTIONARY = 0 //see 7.4.2.

        val INTERMEDIATE_TEXT_REGION = 4 //see 7.4.3.
        val IMMEDIATE_TEXT_REGION = 6 //see 7.4.3.
        val IMMEDIATE_LOSSLESS_TEXT_REGION = 7 //see 7.4.3.
        val PATTERN_DICTIONARY = 16 //see 7.4.4.
        val INTERMEDIATE_HALFTONE_REGION = 20 //see 7.4.5.
        val IMMEDIATE_HALFTONE_REGION = 22 //see 7.4.5.
        val IMMEDIATE_LOSSLESS_HALFTONE_REGION = 23 //see 7.4.5.
        val INTERMEDIATE_GENERIC_REGION = 36 //see 7.4.6.
        val IMMEDIATE_GENERIC_REGION = 38 //see 7.4.6.
        val IMMEDIATE_LOSSLESS_GENERIC_REGION = 39 //see 7.4.6.
        val INTERMEDIATE_GENERIC_REFINEMENT_REGION = 40 //see 7.4.7.
        val IMMEDIATE_GENERIC_REFINEMENT_REGION = 42 //see 7.4.7.
        val IMMEDIATE_LOSSLESS_GENERIC_REFINEMENT_REGION = 43 //see 7.4.7.

        val PAGE_INFORMATION = 48 //see 7.4.8.
        val END_OF_PAGE = 49 //see 7.4.9.
        val END_OF_STRIPE = 50 //see 7.4.10.
        val END_OF_FILE = 51 //see 7.4.11.
        val PROFILES = 52 //see 7.4.12.
        val TABLES = 53 //see 7.4.13.
        val EXTENSION = 62 //see 7.4.14.

        fun copyByteArray(b: ByteArray): ByteArray {
            val bc = ByteArray(b.size)
            System.arraycopy(b, 0, bc, 0, b.size)
            return bc
        }
    }
}
