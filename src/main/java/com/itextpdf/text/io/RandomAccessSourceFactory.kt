/*
 * $Id: 4cb540e2e32ddb0fc940efec84b4305d4baf231b $
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

import com.itextpdf.text.error_messages.MessageLocalization

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.URL
import java.nio.channels.FileChannel

/**
 * Factory to create [RandomAccessSource] objects based on various types of sources
 * @since 5.3.5
 */

class RandomAccessSourceFactory {

    /**
     * whether the full content of the source should be read into memory at construction
     */
    private var forceRead = false

    /**
     * Whether [RandomAccessFile] should be used instead of a [FileChannel], where applicable
     */
    private var usePlainRandomAccess = false

    /**
     * Whether the underlying file should have a RW lock on it or just an R lock
     */
    private var exclusivelyLockFile = false

    /**
     * Determines whether the full content of the source will be read into memory
     * @param forceRead true if the full content will be read, false otherwise
     * *
     * @return this object (this allows chaining of method calls)
     */
    fun setForceRead(forceRead: Boolean): RandomAccessSourceFactory {
        this.forceRead = forceRead
        return this
    }

    /**
     * Determines whether [RandomAccessFile] should be used as the primary data access mechanism
     * @param usePlainRandomAccess whether [RandomAccessFile] should be used as the primary data access mechanism
     * *
     * @return this object (this allows chaining of method calls)
     */
    fun setUsePlainRandomAccess(usePlainRandomAccess: Boolean): RandomAccessSourceFactory {
        this.usePlainRandomAccess = usePlainRandomAccess
        return this
    }

    fun setExclusivelyLockFile(exclusivelyLockFile: Boolean): RandomAccessSourceFactory {
        this.exclusivelyLockFile = exclusivelyLockFile
        return this
    }

    /**
     * Creates a [RandomAccessSource] based on a byte array
     * @param data the byte array
     * *
     * @return the newly created [RandomAccessSource]
     */
    fun createSource(data: ByteArray): RandomAccessSource {
        return ArrayRandomAccessSource(data)
    }

    @Throws(IOException::class)
    fun createSource(raf: RandomAccessFile): RandomAccessSource {
        return RAFRandomAccessSource(raf)
    }

    /**
     * Creates a [RandomAccessSource] based on a URL.  The data available at the URL is read into memory and used
     * as the source for the [RandomAccessSource]
     * @param url the url to read from
     * *
     * @return the newly created [RandomAccessSource]
     */
    @Throws(IOException::class)
    fun createSource(url: URL): RandomAccessSource {
        val `is` = url.openStream()
        try {
            return createSource(`is`)
        } finally {
            try {
                `is`.close()
            } catch (ioe: IOException) {
            }

        }
    }

    /**
     * Creates a [RandomAccessSource] based on an [InputStream].  The full content of the InputStream is read into memory and used
     * as the source for the [RandomAccessSource]
     * @param is the stream to read from
     * *
     * @return the newly created [RandomAccessSource]
     */
    @Throws(IOException::class)
    fun createSource(`is`: InputStream): RandomAccessSource {
        try {
            return createSource(StreamUtil.inputStreamToArray(`is`))
        } finally {
            try {
                `is`.close()
            } catch (ioe: IOException) {
            }

        }
    }

    /**
     * Creates a [RandomAccessSource] based on a filename string.
     * If the filename describes a URL, a URL based source is created
     * If the filename describes a file on disk, the contents may be read into memory (if forceRead is true), opened using memory mapped file channel (if usePlainRandomAccess is false), or opened using [RandomAccessFile] access (if usePlainRandomAccess is true)
     * This call will automatically failover to using [RandomAccessFile] if the memory map operation fails
     * @param filename the name of the file or resource to create the [RandomAccessSource] for
     * *
     * @return the newly created [RandomAccessSource]
     */
    @Throws(IOException::class)
    fun createBestSource(filename: String): RandomAccessSource {
        val file = File(filename)
        if (!file.canRead()) {
            if (filename.startsWith("file:/")
                    || filename.startsWith("http://")
                    || filename.startsWith("https://")
                    || filename.startsWith("jar:")
                    || filename.startsWith("wsjar:")
                    || filename.startsWith("wsjar:")
                    || filename.startsWith("vfszip:")) {
                return createSource(URL(filename))
            } else {
                return createByReadingToMemory(filename)
            }
        }

        if (forceRead) {
            return createByReadingToMemory(FileInputStream(filename))
        }

        val openMode = if (exclusivelyLockFile) "rw" else "r"

        val raf = RandomAccessFile(file, openMode)

        if (exclusivelyLockFile) {
            raf.channel.lock()
        }

        try {
            // ownership of the RAF passes to whatever source is created by createBestSource.
            return createBestSource(raf)
        } catch (e: IOException) {
            // If creation of the source throws, we must close the RAF we created.
            try {
                raf.close()
            } catch (ignore: IOException) {
            }

            throw e
        } catch (e: RuntimeException) {
            // if we get a runtime exception during opening, we must close the RAF we created
            try {
                raf.close()
            } catch (ignore: IOException) {
            }

            throw e
        }

    }

    /**
     * Creates a [RandomAccessSource] based on a RandomAccessFile.
     * The source will be opened using memory mapped file channel (if usePlainRandomAccess is false), or opened using [RandomAccessFile] access (if usePlainRandomAccess is true)
     * This call will automatically failover to using [RandomAccessFile] if the memory map operation fails
     * If the source couldn't be opened, the RandomAccessFile will be closed
     * @param raf the RandomAccessFile to create a [RandomAccessSource] for
     * *
     * @return the newly created [RandomAccessSource]
     */
    @Throws(IOException::class)
    fun createBestSource(raf: RandomAccessFile): RandomAccessSource {

        if (usePlainRandomAccess) {
            return RAFRandomAccessSource(raf)
        }

        if (raf.length() <= 0)
        // files with zero length can't be mapped and will throw an IllegalArgumentException.  Just open using a simple RAF source.
            return RAFRandomAccessSource(raf)

        try {
            // ownership of the RAF passes to whatever source is created by createBestSource. 
            return createBestSource(raf.channel)
        } catch (e: MapFailedException) {
            return RAFRandomAccessSource(raf)
        }

    }

    /**
     * Creates a [RandomAccessSource] based on memory mapping a file channel.
     * Unless you are explicitly working with a FileChannel already, it is better to use
     * [RandomAccessSourceFactory.createBestSource].
     * If the file is large, it will be opened using a paging strategy.
     * @param filename the name of the file or resource to create the [RandomAccessSource] for
     * *
     * @return the newly created [RandomAccessSource]
     */
    @Throws(IOException::class)
    fun createBestSource(channel: FileChannel): RandomAccessSource {
        if (channel.size() <= PagedChannelRandomAccessSource.DEFAULT_TOTAL_BUFSIZE) {
            // if less than the fully mapped usage of PagedFileChannelRandomAccessSource, just map the whole thing and be done with it
            return GetBufferedRandomAccessSource(FileChannelRandomAccessSource(channel))
        } else {
            return GetBufferedRandomAccessSource(PagedChannelRandomAccessSource(channel))
        }
    }

    @Throws(IOException::class)
    fun createRanged(source: RandomAccessSource, ranges: LongArray): RandomAccessSource {
        val sources = arrayOfNulls<RandomAccessSource>(ranges.size / 2)
        var i = 0
        while (i < ranges.size) {
            sources[i / 2] = WindowRandomAccessSource(source, ranges[i], ranges[i + 1])
            i += 2
        }
        return GroupedRandomAccessSource(sources)
    }

    /**
     * Creates a new [RandomAccessSource] by reading the specified file/resource into memory
     * @param filename the name of the resource to read
     * *
     * @return the newly created [RandomAccessSource]
     * *
     * @throws IOException if reading the underling file or stream fails
     */
    @Throws(IOException::class)
    private fun createByReadingToMemory(filename: String): RandomAccessSource {
        val `is` = StreamUtil.getResourceStream(filename) ?: throw IOException(MessageLocalization.getComposedMessage("1.not.found.as.file.or.resource", filename))
        return createByReadingToMemory(`is`)
    }

    /**
     * Creates a new [RandomAccessSource] by reading the specified file/resource into memory
     * @param is the name of the resource to read
     * *
     * @return the newly created [RandomAccessSource]
     * *
     * @throws IOException if reading the underling file or stream fails
     */
    @Throws(IOException::class)
    private fun createByReadingToMemory(`is`: InputStream): RandomAccessSource {
        try {
            return ArrayRandomAccessSource(StreamUtil.inputStreamToArray(`is`))
        } finally {
            try {
                `is`.close()
            } catch (ioe: IOException) {
            }

        }
    }


}
/**
 * Creates a factory that will give preference to accessing the underling data source using memory mapped files
 */
