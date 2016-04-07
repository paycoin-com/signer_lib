/*
 * $Id: 03f844ad29045874abc0d9c7e6a7f10bb7ad700d $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Kevin Day, Bruno Lowagie, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General License for more
 * details. You should have received a copy of the GNU Affero General License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General License.
 *
 * In accordance with Section 7(b) of the GNU Affero General License, a covered
 * work must retain the producer line in every PDF that is created or
 * manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing a
 * commercial license. Buying such a license is mandatory as soon as you develop
 * commercial activities involving the iText software without disclosing the
 * source code of your own applications. These activities include: offering paid
 * services to customers as an ASP, serving PDFs on the fly in a web
 * application, shipping iText with a closed source product.
 *
 * For more information, please contact iText Software Corp. at this address:
 * sales@itextpdf.com
 */
package com.itextpdf.text.io

import java.io.IOException
import java.nio.channels.FileChannel
import java.util.LinkedList

/**
 * A RandomAccessSource that is based on an underlying [FileChannel].  The channel is mapped into memory using a paging scheme to allow for efficient reads of very large files.
 * As an implementation detail, we use [GroupedRandomAccessSource] functionality, but override to make determination of the underlying
 * mapped page more efficient - and to close each page as another is opened
 * @since 5.3.5
 */
internal class PagedChannelRandomAccessSource
/**
 * Constructs a new [PagedChannelRandomAccessSource] based on the specified FileChannel, with a specific buffer size
 * @param channel the channel to use as the backing store
 * *
 * @param bufferSize the size of the buffers to use
 * *
 * @throws IOException if the channel cannot be opened or mapped
 */
@Throws(IOException::class)
@JvmOverloads constructor(
        /**
         * The channel this source is based on
         */
        private val channel: FileChannel, totalBufferSize: Int = PagedChannelRandomAccessSource.DEFAULT_TOTAL_BUFSIZE, maxOpenBuffers: Int = PagedChannelRandomAccessSource.DEFAULT_MAX_OPEN_BUFFERS) : GroupedRandomAccessSource(PagedChannelRandomAccessSource.buildSources(channel, totalBufferSize / maxOpenBuffers)), RandomAccessSource {

    /**
     * The size of each of the buffers to use when mapping files into memory.  This must be greater than 0 and less than [Integer.MAX_VALUE]
     */
    private val bufferSize: Int

    /**
     * Most recently used list used to hold a number of mapped pages open at a time
     */
    private val mru: MRU<RandomAccessSource>

    init {
        this.bufferSize = totalBufferSize / maxOpenBuffers
        this.mru = MRU<RandomAccessSource>(maxOpenBuffers)
    }

    override
            /**
             * {@inheritDoc}
             */
    fun getStartingSourceIndex(offset: Long): Int {
        return (offset / bufferSize).toInt()
    }

    @Throws(IOException::class)
    override
            /**
             * {@inheritDoc}
             * For now, close the source that is no longer being used.  In the future, we may implement an MRU that allows multiple pages to be opened at a time
             */
    fun sourceReleased(source: RandomAccessSource) {
        val old = mru.enqueue(source)
        old?.close()
    }

    @Throws(IOException::class)
    override
            /**
             * {@inheritDoc}
             * Ensure that the source is mapped.  In the future, we may implement an MRU that allows multiple pages to be opened at a time
             */
    fun sourceInUse(source: RandomAccessSource) {
        (source as MappedChannelRandomAccessSource).open()
    }

    @Throws(IOException::class)
    override
            /**
             * {@inheritDoc}
             * Cleans the mapped bytebuffers and closes the channel
             */
    fun close() {
        super.close()
        channel.close()
    }

    private class MRU<E>
    /**
     * Constructs an MRU with the specified size
     * @param limit the limit
     */
    (
            /**
             * The maximum number of entries held by this MRU
             */
            private val limit: Int) {

        /**
         * Backing list for managing the MRU
         */
        private val queue = LinkedList<E>()

        /**
         * Adds an element to the MRU.  If the element is already in the MRU, it is moved to the top.
         * @param newElement the element to add
         * *
         * @return the element that was removed from the MRU to make room for the new element, or null if no element needed to be removed
         */
        fun enqueue(newElement: E): E? {
            // TODO: this check may not be an effective optimization - the GroupedRandomAccessSource already tracks the 'current' source, so it seems unlikely that we would ever hit this code branch
            if (queue.size > 0 && queue.first === newElement)
                return null

            val it = queue.iterator()
            while (it.hasNext()) {
                val element = it.next()
                if (newElement === element) {
                    it.remove()
                    queue.addFirst(newElement)
                    return null
                }
            }
            queue.addFirst(newElement)

            if (queue.size > limit)
                return queue.removeLast()

            return null
        }
    }

    companion object {
        // these values were selected based on parametric testing with extracting text content from a 2.3GB file.  These settings resulted in the best improvement over
        // the single size MRU case (24% speed improvement)
        val DEFAULT_TOTAL_BUFSIZE = 1 shl 26
        val DEFAULT_MAX_OPEN_BUFFERS = 16

        /**
         * Constructs a set of [MappedChannelRandomAccessSource]s for each page (of size bufferSize) of the underlying channel
         * @param channel the underlying channel
         * *
         * @param bufferSize the size of each page (the last page may be shorter)
         * *
         * @return a list of sources that represent the pages of the channel
         * *
         * @throws IOException if IO fails for any reason
         */
        @Throws(IOException::class)
        private fun buildSources(channel: FileChannel, bufferSize: Int): Array<RandomAccessSource> {
            val size = channel.size()
            if (size <= 0)
                throw IOException("File size must be greater than zero")

            val bufferCount = (size / bufferSize).toInt() + if (size % bufferSize == 0) 0 else 1

            val sources = arrayOfNulls<MappedChannelRandomAccessSource>(bufferCount)
            for (i in 0..bufferCount - 1) {
                val pageOffset = i.toLong() * bufferSize
                val pageLength = Math.min(size - pageOffset, bufferSize.toLong())
                sources[i] = MappedChannelRandomAccessSource(channel, pageOffset, pageLength)
            }
            return sources

        }
    }
}
/**
 * Constructs a new [PagedChannelRandomAccessSource] based on the specified FileChannel, with a default buffer configuration.
 * The default buffer configuration is currently 2^26 total paged bytes, spread across a maximum of 16 active buffers. This arrangement
 * resulted in a 24% speed improvement over the single buffer case in parametric tests extracting text from a 2.3 GB file.
 * @param channel the channel to use as the backing store
 * *
 * @throws IOException if the channel cannot be opened or mapped
 */
