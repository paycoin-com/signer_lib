/*
 * $Id: cf3b3006d415baf40d8b673d282c6200fa885c12 $
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
package com.itextpdf.text.pdf.internal

import java.io.IOException

import com.itextpdf.text.DocWriter
import com.itextpdf.text.pdf.OutputStreamCounter
import com.itextpdf.text.pdf.PdfDeveloperExtension
import com.itextpdf.text.pdf.PdfDictionary
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.interfaces.PdfVersion

/**
 * Stores the PDF version information,
 * knows how to write a PDF Header,
 * and how to add the version to the catalog (if necessary).
 */

class PdfVersionImp : PdfVersion {

    /** Indicates if the header was already written.  */
    protected var headerWasWritten = false
    /** Indicates if we are working in append mode.  */
    protected var appendmode = false
    /** The version that was or will be written to the header.  */
    protected var header_version = PdfWriter.VERSION_1_4
    /** The version that will be written to the catalog.  */
    protected var catalog_version: PdfName? = null
    /** The version that user can use to get the actual version of PDF document  */
    var version = PdfWriter.VERSION_1_4
        protected set

    /**
     * The extensions dictionary.
     * @since    2.1.6
     */
    protected var extensions: PdfDictionary? = null

    /**
     * @see com.itextpdf.text.pdf.interfaces.PdfVersion.setPdfVersion
     */
    override fun setPdfVersion(version: Char) {
        this.version = version
        if (headerWasWritten || appendmode) {
            setPdfVersion(getVersionAsName(version))
        } else {
            this.header_version = version
        }
    }

    /**
     * @see com.itextpdf.text.pdf.interfaces.PdfVersion.setAtLeastPdfVersion
     */
    override fun setAtLeastPdfVersion(version: Char) {
        if (version > header_version) {
            setPdfVersion(version)
        }
    }

    /**
     * @see com.itextpdf.text.pdf.interfaces.PdfVersion.setPdfVersion
     */
    fun setPdfVersion(version: PdfName) {
        if (catalog_version == null || catalog_version!!.compareTo(version) < 0) {
            this.catalog_version = version
        }
    }

    /**
     * Sets the append mode.
     */
    fun setAppendmode(appendmode: Boolean) {
        this.appendmode = appendmode
    }

    /**
     * Writes the header to the OutputStreamCounter.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun writeHeader(os: OutputStreamCounter) {
        if (appendmode) {
            os.write(HEADER[0])
        } else {
            os.write(HEADER[1])
            os.write(getVersionAsByteArray(header_version))
            os.write(HEADER[2])
            headerWasWritten = true
        }
    }

    /**
     * Returns the PDF version as a name.
     * @param version    the version character.
     */
    fun getVersionAsName(version: Char): PdfName {
        when (version) {
            PdfWriter.VERSION_1_2 -> return PdfWriter.PDF_VERSION_1_2
            PdfWriter.VERSION_1_3 -> return PdfWriter.PDF_VERSION_1_3
            PdfWriter.VERSION_1_4 -> return PdfWriter.PDF_VERSION_1_4
            PdfWriter.VERSION_1_5 -> return PdfWriter.PDF_VERSION_1_5
            PdfWriter.VERSION_1_6 -> return PdfWriter.PDF_VERSION_1_6
            PdfWriter.VERSION_1_7 -> return PdfWriter.PDF_VERSION_1_7
            else -> return PdfWriter.PDF_VERSION_1_4
        }
    }

    /**
     * Returns the version as a byte[].
     * @param version the version character
     */
    fun getVersionAsByteArray(version: Char): ByteArray {
        return DocWriter.getISOBytes(getVersionAsName(version).toString().substring(1))
    }

    /** Adds the version to the Catalog dictionary.  */
    fun addToCatalog(catalog: PdfDictionary) {
        if (catalog_version != null) {
            catalog.put(PdfName.VERSION, catalog_version)
        }
        if (extensions != null) {
            catalog.put(PdfName.EXTENSIONS, extensions)
        }
    }

    /**
     * @see com.itextpdf.text.pdf.interfaces.PdfVersion.addDeveloperExtension
     * @since    2.1.6
     */
    fun addDeveloperExtension(de: PdfDeveloperExtension) {
        if (extensions == null) {
            extensions = PdfDictionary()
        } else {
            val extension = extensions!!.getAsDict(de.prefix)
            if (extension != null) {
                var diff = de.baseversion.compareTo(extension.getAsName(PdfName.BASEVERSION))
                if (diff < 0)
                    return
                diff = de.extensionLevel - extension.getAsNumber(PdfName.EXTENSIONLEVEL).intValue()
                if (diff <= 0)
                    return
            }
        }
        extensions!!.put(de.prefix, de.developerExtensions)
    }

    companion object {

        /** Contains different strings that are part of the header.  */
        val HEADER = arrayOf(DocWriter.getISOBytes("\n"), DocWriter.getISOBytes("%PDF-"), DocWriter.getISOBytes("\n%\u00e2\u00e3\u00cf\u00d3\n"))
    }

}
