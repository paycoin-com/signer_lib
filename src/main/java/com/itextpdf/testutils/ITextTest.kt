/*
 * $Id: ad973d27fcc6bec6587f3e9a91fa0b3bd1941417 $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Pavel Alay, Bruno Lowagie, et al.
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
package com.itextpdf.testutils

import com.itextpdf.text.log.Logger
import com.itextpdf.text.log.LoggerFactory

import java.io.File
import java.io.IOException

abstract class ITextTest {

    @Throws(Exception::class)
    fun runTest() {
        LOGGER.info("Starting test.")
        val outPdf = outPdf
        if (outPdf == null || outPdf.length == 0)
            throw IOException("outPdf cannot be empty!")
        makePdf(outPdf)
        assertPdf(outPdf)
        comparePdf(outPdf, cmpPdf)
        LOGGER.info("Test complete.")
    }

    @Throws(Exception::class)
    protected abstract fun makePdf(outPdf: String)

    /**
     * Gets the name of the resultant PDF file.
     * This name will be passed to `makePdf`, `assertPdf` and `comparePdf` methods.
     * @return
     */
    protected abstract val outPdf: String?

    @Throws(Exception::class)
    protected fun assertPdf(outPdf: String) {

    }

    @Throws(Exception::class)
    protected fun comparePdf(outPdf: String, cmpPdf: String) {

    }

    /**
     * Gets the name of the compare PDF file.
     * This name will be passed to `comparePdf` method.
     * @return
     */
    protected val cmpPdf: String
        get() = ""

    protected fun deleteDirectory(path: File?) {
        if (path == null)
            return
        if (path.exists()) {
            for (f in path.listFiles()!!) {
                if (f.isDirectory) {
                    deleteDirectory(f)
                    f.delete()
                } else {
                    f.delete()
                }
            }
            path.delete()
        }
    }

    protected fun deleteFiles(path: File?) {
        if (path != null && path.exists()) {
            for (f in path.listFiles()!!) {
                f.delete()
            }
        }
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(ITextTest::class.java.name)
    }
}
