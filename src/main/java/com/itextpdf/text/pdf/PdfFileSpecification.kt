/*
 * $Id: 7435b4ae908cee06b77810a86707c4a864c7d40d $
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

import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.io.StreamUtil
import com.itextpdf.text.pdf.collection.PdfCollectionItem
import com.itextpdf.text.pdf.internal.PdfIsoKeys

import java.io.*
import java.net.URL

/** Specifies a file or an URL. The file can be extern or embedded.

 * @author Paulo Soares
 */
class PdfFileSpecification : PdfDictionary(PdfName.FILESPEC) {
    protected var writer: PdfWriter
    protected var ref: PdfIndirectReference? = null

    /**
     * Gets the indirect reference to this file specification.
     * Multiple invocations will retrieve the same value.
     * @throws IOException on error
     * *
     * @return the indirect reference
     */
    val reference: PdfIndirectReference
        @Throws(IOException::class)
        get() {
            if (ref != null)
                return ref
            ref = writer.addToBody(this).indirectReference
            return ref
        }

    /**
     * Sets the file name (the key /F) string as an hex representation
     * to support multi byte file names. The name must have the slash and
     * backslash escaped according to the file specification rules
     * @param fileName the file name as a byte array
     */
    fun setMultiByteFileName(fileName: ByteArray) {
        put(PdfName.F, PdfString(fileName).setHexWriting(true))
    }

    /**
     * Adds the unicode file name (the key /UF). This entry was introduced
     * in PDF 1.7. The filename must have the slash and backslash escaped
     * according to the file specification rules.
     * @param filename    the filename
     * *
     * @param unicode    if true, the filename is UTF-16BE encoded; otherwise PDFDocEncoding is used;
     */
    fun setUnicodeFileName(filename: String, unicode: Boolean) {
        put(PdfName.UF, PdfString(filename, if (unicode) PdfObject.TEXT_UNICODE else PdfObject.TEXT_PDFDOCENCODING))
    }

    /**
     * Sets a flag that indicates whether an external file referenced by the file
     * specification is volatile. If the value is true, applications should never
     * cache a copy of the file.
     * @param volatile_file    if true, the external file should not be cached
     */
    fun setVolatile(volatile_file: Boolean) {
        put(PdfName.V, PdfBoolean(volatile_file))
    }

    /**
     * Adds a description for the file that is specified here.
     * @param description    some text
     * *
     * @param unicode        if true, the text is added as a unicode string
     */
    fun addDescription(description: String, unicode: Boolean) {
        put(PdfName.DESC, PdfString(description, if (unicode) PdfObject.TEXT_UNICODE else PdfObject.TEXT_PDFDOCENCODING))
    }

    /**
     * Adds the Collection item dictionary.
     */
    fun addCollectionItem(ci: PdfCollectionItem) {
        put(PdfName.CI, ci)
    }

    @Throws(IOException::class)
    override fun toPdf(writer: PdfWriter, os: OutputStream) {
        PdfWriter.checkPdfIsoConformance(writer, PdfIsoKeys.PDFISOKEY_FILESPEC, this)
        super.toPdf(writer, os)
    }

    companion object {

        /**
         * Creates a file specification of type URL.
         * @param writer the PdfWriter
         * *
         * @param url the URL
         * *
         * @return the file specification
         */
        fun url(writer: PdfWriter, url: String): PdfFileSpecification {
            val fs = PdfFileSpecification()
            fs.writer = writer
            fs.put(PdfName.FS, PdfName.URL)
            fs.put(PdfName.F, PdfString(url))
            return fs
        }

        /**
         * Creates a file specification with the file embedded. The file may
         * come from the file system or from a byte array. The data is flate compressed.
         * @param writer the PdfWriter
         * *
         * @param filePath the file path
         * *
         * @param fileDisplay the file information that is presented to the user
         * *
         * @param fileStore the byte array with the file. If it is not null
         * * it takes precedence over filePath
         * *
         * @param compressionLevel    the compression level to be used for compressing the file
         * * it takes precedence over filePath
         * *
         * @throws IOException on error
         * *
         * @return the file specification
         * *
         * @since    2.1.3
         */
        @Throws(IOException::class)
        @JvmOverloads fun fileEmbedded(writer: PdfWriter, filePath: String, fileDisplay: String, fileStore: ByteArray, compressionLevel: Int = PdfStream.BEST_COMPRESSION): PdfFileSpecification {
            return fileEmbedded(writer, filePath, fileDisplay, fileStore, null, null, compressionLevel)
        }


        /**
         * Creates a file specification with the file embedded. The file may
         * come from the file system or from a byte array.
         * @param writer the PdfWriter
         * *
         * @param filePath the file path
         * *
         * @param fileDisplay the file information that is presented to the user
         * *
         * @param fileStore the byte array with the file. If it is not null
         * * it takes precedence over filePath
         * *
         * @param compress sets the compression on the data. Multimedia content will benefit little
         * * from compression
         * *
         * @throws IOException on error
         * *
         * @return the file specification
         */
        @Throws(IOException::class)
        fun fileEmbedded(writer: PdfWriter, filePath: String, fileDisplay: String, fileStore: ByteArray, compress: Boolean): PdfFileSpecification {
            return fileEmbedded(writer, filePath, fileDisplay, fileStore, null, null, if (compress) PdfStream.BEST_COMPRESSION else PdfStream.NO_COMPRESSION)
        }

        /**
         * Creates a file specification with the file embedded. The file may
         * come from the file system or from a byte array.
         * @param writer the PdfWriter
         * *
         * @param filePath the file path
         * *
         * @param fileDisplay the file information that is presented to the user
         * *
         * @param fileStore the byte array with the file. If it is not null
         * * it takes precedence over filePath
         * *
         * @param compress sets the compression on the data. Multimedia content will benefit little
         * * from compression
         * *
         * @param mimeType the optional mimeType
         * *
         * @param fileParameter the optional extra file parameters such as the creation or modification date
         * *
         * @throws IOException on error
         * *
         * @return the file specification
         */
        @Throws(IOException::class)
        fun fileEmbedded(writer: PdfWriter, filePath: String, fileDisplay: String, fileStore: ByteArray, compress: Boolean, mimeType: String, fileParameter: PdfDictionary): PdfFileSpecification {
            return fileEmbedded(writer, filePath, fileDisplay, fileStore, mimeType, fileParameter, if (compress) PdfStream.BEST_COMPRESSION else PdfStream.NO_COMPRESSION)
        }

        /**
         * Creates a file specification with the file embedded. The file may
         * come from the file system or from a byte array.
         * @param writer the PdfWriter
         * *
         * @param filePath the file path
         * *
         * @param fileDisplay the file information that is presented to the user
         * *
         * @param fileStore the byte array with the file. If it is not null
         * * it takes precedence over filePath
         * *
         * @param mimeType the optional mimeType
         * *
         * @param fileParameter the optional extra file parameters such as the creation or modification date
         * *
         * @param compressionLevel the level of compression
         * *
         * @throws IOException on error
         * *
         * @return the file specification
         * *
         * @since    2.1.3
         */
        @Throws(IOException::class)
        fun fileEmbedded(writer: PdfWriter, filePath: String, fileDisplay: String, fileStore: ByteArray?, mimeType: String?, fileParameter: PdfDictionary?, compressionLevel: Int): PdfFileSpecification {
            val fs = PdfFileSpecification()
            fs.writer = writer
            fs.put(PdfName.F, PdfString(fileDisplay))
            fs.setUnicodeFileName(fileDisplay, false)
            val stream: PdfEFStream
            var `in`: InputStream? = null
            val ref: PdfIndirectReference
            var refFileLength: PdfIndirectReference? = null
            try {
                if (fileStore == null) {
                    refFileLength = writer.pdfIndirectReference
                    val file = File(filePath)
                    if (file.canRead()) {
                        `in` = FileInputStream(filePath)
                    } else {
                        if (filePath.startsWith("file:/") || filePath.startsWith("http://") || filePath.startsWith("https://") || filePath.startsWith("jar:")) {
                            `in` = URL(filePath).openStream()
                        } else {
                            `in` = StreamUtil.getResourceStream(filePath)
                            if (`in` == null)
                                throw IOException(MessageLocalization.getComposedMessage("1.not.found.as.file.or.resource", filePath))
                        }
                    }
                    stream = PdfEFStream(`in`, writer)
                } else {
                    stream = PdfEFStream(fileStore)
                }
                stream.put(PdfName.TYPE, PdfName.EMBEDDEDFILE)
                stream.flateCompress(compressionLevel)
                val param = PdfDictionary()
                if (fileParameter != null) {
                    param.merge(fileParameter)
                }
                if (!param.contains(PdfName.MODDATE)) {
                    param.put(PdfName.MODDATE, PdfDate())
                }
                if (fileStore == null) {
                    stream.put(PdfName.PARAMS, refFileLength)
                } else {
                    param.put(PdfName.SIZE, PdfNumber(stream.rawLength))
                    stream.put(PdfName.PARAMS, param)
                }

                if (mimeType != null)
                    stream.put(PdfName.SUBTYPE, PdfName(mimeType))

                ref = writer.addToBody(stream).indirectReference
                if (fileStore == null) {
                    stream.writeLength()
                    param.put(PdfName.SIZE, PdfNumber(stream.rawLength))
                    writer.addToBody(param, refFileLength)
                }
            } finally {
                if (`in` != null)
                    try {
                        `in`.close()
                    } catch (e: Exception) {
                    }

            }
            val f = PdfDictionary()
            f.put(PdfName.F, ref)
            f.put(PdfName.UF, ref)
            fs.put(PdfName.EF, f)
            return fs
        }

        /**
         * Creates a file specification for an external file.
         * @param writer the PdfWriter
         * *
         * @param filePath the file path
         * *
         * @return the file specification
         */
        fun fileExtern(writer: PdfWriter, filePath: String): PdfFileSpecification {
            val fs = PdfFileSpecification()
            fs.writer = writer
            fs.put(PdfName.F, PdfString(filePath))
            fs.setUnicodeFileName(filePath, false)
            return fs
        }
    }

}
/** Creates a new instance of PdfFileSpecification. The static methods are preferred.  */
/**
 * Creates a file specification with the file embedded. The file may
 * come from the file system or from a byte array. The data is flate compressed.
 * @param writer the PdfWriter
 * *
 * @param filePath the file path
 * *
 * @param fileDisplay the file information that is presented to the user
 * *
 * @param fileStore the byte array with the file. If it is not null
 * * it takes precedence over filePath
 * *
 * @throws IOException on error
 * *
 * @return the file specification
 */
