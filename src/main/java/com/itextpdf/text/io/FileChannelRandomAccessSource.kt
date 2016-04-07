/*
 * $Id: 94c8363291fc7b6248e2ff6fe580b3d3cdc1288d $
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

/**
 * A RandomAccessSource that is based on an underlying [FileChannel].  The entire channel will be mapped into memory for efficient reads.
 * @since 5.3.5
 */
class FileChannelRandomAccessSource
/**
 * Constructs a new [FileChannelRandomAccessSource] based on the specified FileChannel.  The entire source channel will be mapped into memory.
 * @param channel the channel to use as the backing store
 * *
 * @throws IOException if the channel cannot be opened or mapped
 */
@Throws(IOException::class)
constructor(
        /**
         * The channel this source is based on
         */
        private val channel: FileChannel) : RandomAccessSource {

    /**
     * Tracks the actual mapping
     */
    private val source: MappedChannelRandomAccessSource

    init {
        if (channel.size() == 0)
            throw IOException("File size is 0 bytes")
        source = MappedChannelRandomAccessSource(channel, 0, channel.size())
        source.open()
    }


    /**
     * {@inheritDoc}
     * Cleans the mapped bytebuffers and closes the channel
     */
    @Throws(IOException::class)
    override fun close() {
        source.close()
        channel.close()
    }


    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun get(position: Long): Int {
        return source.get(position)
    }


    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun get(position: Long, bytes: ByteArray, off: Int, len: Int): Int {
        return source.get(position, bytes, off, len)
    }


    /**
     * {@inheritDoc}
     */
    override fun length(): Long {
        return source.length()
    }

}
