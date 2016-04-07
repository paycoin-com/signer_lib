/*
 * $Id: 2facfebfb986e6857b587cce8969f1362ff5cfe5 $
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
package com.itextpdf.text.pdf

import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.security.XmlLocator
import org.w3c.dom.Document
import org.xml.sax.SAXException

import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerConfigurationException
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Helps to locate xml stream inside PDF document with Xfa form.
 */
class XfaXmlLocator @Throws(DocumentException::class, IOException::class)
constructor(private val stamper: PdfStamper) : XmlLocator {

    init {
        try {
            createXfaForm()
        } catch (e: ParserConfigurationException) {
            throw DocumentException(e)
        } catch (e: SAXException) {
            throw DocumentException(e)
        }

    }

    private var xfaForm: XfaForm? = null
    override var encoding: String? = null

    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
    protected fun createXfaForm() {
        xfaForm = XfaForm(stamper.reader)
    }

    /**
     * Gets Document to sign
     */
    /**
     * Save document as single XML stream in AcroForm.
     * @param document signed document
     * *
     * @throws IOException
     * *
     * @throws DocumentException
     */
    override //Convert Document to byte[] to save to PDF
            //Create PdfStream
    var document: Document
        get() = xfaForm!!.domDocument
        @Throws(IOException::class, DocumentException::class)
        set(document) = try {
            val outputStream = ByteArrayOutputStream()
            val tf = TransformerFactory.newInstance()

            val trans = tf.newTransformer()
            trans.transform(DOMSource(document), StreamResult(outputStream))
            val iref = stamper.writer.addToBody(PdfStream(outputStream.toByteArray())).indirectReference
            stamper.reader.getAcroForm().put(PdfName.XFA, iref)
        } catch (e: TransformerConfigurationException) {
            throw DocumentException(e)
        } catch (e: TransformerException) {
            throw DocumentException(e)
        }
}