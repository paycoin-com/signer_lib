/*
 * $Id: 7904354419084fe241528ef1d7d883306e871dde $
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
package com.itextpdf.text.html.simpleparser

import java.io.IOException
import java.util.HashMap

import com.itextpdf.text.DocumentException
import com.itextpdf.text.html.HtmlTags

/**
 * This class maps tags such as div and span to their corresponding
 * TagProcessor classes.
 */
@Deprecated("")
@Deprecated("since 5.5.2")
class HTMLTagProcessors : HashMap<String, HTMLTagProcessor>() {

    init {
        put(HtmlTags.A, A)
        put(HtmlTags.B, EM_STRONG_STRIKE_SUP_SUP)
        put(HtmlTags.BODY, DIV)
        put(HtmlTags.BR, BR)
        put(HtmlTags.DIV, DIV)
        put(HtmlTags.EM, EM_STRONG_STRIKE_SUP_SUP)
        put(HtmlTags.FONT, SPAN)
        put(HtmlTags.H1, H)
        put(HtmlTags.H2, H)
        put(HtmlTags.H3, H)
        put(HtmlTags.H4, H)
        put(HtmlTags.H5, H)
        put(HtmlTags.H6, H)
        put(HtmlTags.HR, HR)
        put(HtmlTags.I, EM_STRONG_STRIKE_SUP_SUP)
        put(HtmlTags.IMG, IMG)
        put(HtmlTags.LI, LI)
        put(HtmlTags.OL, UL_OL)
        put(HtmlTags.P, DIV)
        put(HtmlTags.PRE, PRE)
        put(HtmlTags.S, EM_STRONG_STRIKE_SUP_SUP)
        put(HtmlTags.SPAN, SPAN)
        put(HtmlTags.STRIKE, EM_STRONG_STRIKE_SUP_SUP)
        put(HtmlTags.STRONG, EM_STRONG_STRIKE_SUP_SUP)
        put(HtmlTags.SUB, EM_STRONG_STRIKE_SUP_SUP)
        put(HtmlTags.SUP, EM_STRONG_STRIKE_SUP_SUP)
        put(HtmlTags.TABLE, TABLE)
        put(HtmlTags.TD, TD)
        put(HtmlTags.TH, TD)
        put(HtmlTags.TR, TR)
        put(HtmlTags.U, EM_STRONG_STRIKE_SUP_SUP)
        put(HtmlTags.UL, UL_OL)
    }

    companion object {

        /**
         * Object that processes the following tags:
         * i, em, b, strong, s, strike, u, sup, sub
         */
        val EM_STRONG_STRIKE_SUP_SUP: HTMLTagProcessor = object : HTMLTagProcessor {
            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.startElement
             */
            override fun startElement(worker: HTMLWorker, tag: String, attrs: MutableMap<String, String>) {
                var tag = tag
                tag = mapTag(tag)
                attrs.put(tag, null)
                worker.updateChain(tag, attrs)
            }

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.endElement
             */
            override fun endElement(worker: HTMLWorker, tag: String) {
                var tag = tag
                tag = mapTag(tag)
                worker.updateChain(tag)
            }

            /**
             * Maps em to i, strong to b, and strike to s.
             * This is a convention: the style parser expects i, b and s.
             * @param tag the original tag
             * *
             * @return the mapped tag
             */
            private fun mapTag(tag: String): String {
                if (HtmlTags.EM.equals(tag, ignoreCase = true))
                    return HtmlTags.I
                if (HtmlTags.STRONG.equals(tag, ignoreCase = true))
                    return HtmlTags.B
                if (HtmlTags.STRIKE.equals(tag, ignoreCase = true))
                    return HtmlTags.S
                return tag
            }

        }

        /**
         * Object that processes the a tag.
         */
        val A: HTMLTagProcessor = object : HTMLTagProcessor {
            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.startElement
             */
            override fun startElement(worker: HTMLWorker, tag: String, attrs: MutableMap<String, String>) {
                worker.updateChain(tag, attrs)
                worker.flushContent()
            }

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.endElement
             */
            override fun endElement(worker: HTMLWorker, tag: String) {
                worker.processLink()
                worker.updateChain(tag)
            }
        }

        /**
         * Object that processes the br tag.
         */
        val BR: HTMLTagProcessor = object : HTMLTagProcessor {
            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.startElement
             */
            override fun startElement(worker: HTMLWorker, tag: String, attrs: Map<String, String>) {
                worker.newLine()
            }

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.endElement
             */
            override fun endElement(worker: HTMLWorker, tag: String) {
            }

        }

        val UL_OL: HTMLTagProcessor = object : HTMLTagProcessor {

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.startElement
             */
            @Throws(DocumentException::class)
            override fun startElement(worker: HTMLWorker, tag: String, attrs: MutableMap<String, String>) {
                worker.carriageReturn()
                if (worker.isPendingLI)
                    worker.endElement(HtmlTags.LI)
                worker.isSkipText = true
                worker.updateChain(tag, attrs)
                worker.pushToStack(worker.createList(tag))
            }

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.endElement
             */
            @Throws(DocumentException::class)
            override fun endElement(worker: HTMLWorker, tag: String) {
                worker.carriageReturn()
                if (worker.isPendingLI)
                    worker.endElement(HtmlTags.LI)
                worker.isSkipText = false
                worker.updateChain(tag)
                worker.processList()
            }

        }

        val HR: HTMLTagProcessor = object : HTMLTagProcessor {

            @Throws(DocumentException::class)
            override fun startElement(worker: HTMLWorker, tag: String, attrs: Map<String, String>) {
                worker.carriageReturn()
                worker.pushToStack(worker.createLineSeparator(attrs))
            }

            override fun endElement(worker: HTMLWorker, tag: String) {
            }

        }

        val SPAN: HTMLTagProcessor = object : HTMLTagProcessor {

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.startElement
             */
            override fun startElement(worker: HTMLWorker, tag: String, attrs: MutableMap<String, String>) {
                worker.updateChain(tag, attrs)
            }

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.endElement
             */
            override fun endElement(worker: HTMLWorker, tag: String) {
                worker.updateChain(tag)
            }

        }

        val H: HTMLTagProcessor = object : HTMLTagProcessor {

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.startElement
             */
            @Throws(DocumentException::class)
            override fun startElement(worker: HTMLWorker, tag: String, attrs: MutableMap<String, String>) {
                worker.carriageReturn()
                if (!attrs.containsKey(HtmlTags.SIZE)) {
                    val v = 7 - Integer.parseInt(tag.substring(1))
                    attrs.put(HtmlTags.SIZE, Integer.toString(v))
                }
                worker.updateChain(tag, attrs)
            }

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.endElement
             */
            @Throws(DocumentException::class)
            override fun endElement(worker: HTMLWorker, tag: String) {
                worker.carriageReturn()
                worker.updateChain(tag)
            }

        }

        val LI: HTMLTagProcessor = object : HTMLTagProcessor {

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.startElement
             */
            @Throws(DocumentException::class)
            override fun startElement(worker: HTMLWorker, tag: String, attrs: MutableMap<String, String>) {
                worker.carriageReturn()
                if (worker.isPendingLI)
                    worker.endElement(tag)
                worker.isSkipText = false
                worker.isPendingLI = true
                worker.updateChain(tag, attrs)
                worker.pushToStack(worker.createListItem())
            }

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.endElement
             */
            @Throws(DocumentException::class)
            override fun endElement(worker: HTMLWorker, tag: String) {
                worker.carriageReturn()
                worker.isPendingLI = false
                worker.isSkipText = true
                worker.updateChain(tag)
                worker.processListItem()
            }

        }

        val PRE: HTMLTagProcessor = object : HTMLTagProcessor {

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.startElement
             */
            @Throws(DocumentException::class)
            override fun startElement(worker: HTMLWorker, tag: String, attrs: MutableMap<String, String>) {
                worker.carriageReturn()
                if (!attrs.containsKey(HtmlTags.FACE)) {
                    attrs.put(HtmlTags.FACE, "Courier")
                }
                worker.updateChain(tag, attrs)
                worker.isInsidePRE = true
            }

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.endElement
             */
            @Throws(DocumentException::class)
            override fun endElement(worker: HTMLWorker, tag: String) {
                worker.carriageReturn()
                worker.updateChain(tag)
                worker.isInsidePRE = false
            }

        }

        val DIV: HTMLTagProcessor = object : HTMLTagProcessor {

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.startElement
             */
            @Throws(DocumentException::class)
            override fun startElement(worker: HTMLWorker, tag: String, attrs: MutableMap<String, String>) {
                worker.carriageReturn()
                worker.updateChain(tag, attrs)
            }

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.endElement
             */
            @Throws(DocumentException::class)
            override fun endElement(worker: HTMLWorker, tag: String) {
                worker.carriageReturn()
                worker.updateChain(tag)
            }

        }


        val TABLE: HTMLTagProcessor = object : HTMLTagProcessor {

            /**
             * @throws DocumentException
             * *
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.startElement
             */
            @Throws(DocumentException::class)
            override fun startElement(worker: HTMLWorker, tag: String, attrs: MutableMap<String, String>) {
                worker.carriageReturn()
                val table = TableWrapper(attrs)
                worker.pushToStack(table)
                worker.pushTableState()
                worker.isPendingTD = false
                worker.isPendingTR = false
                worker.isSkipText = true
                // Table alignment should not affect children elements, thus remove
                attrs.remove(HtmlTags.ALIGN)
                // In case this is a nested table reset colspan and rowspan
                attrs.put(HtmlTags.COLSPAN, "1")
                attrs.put(HtmlTags.ROWSPAN, "1")
                worker.updateChain(tag, attrs)
            }

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.endElement
             */
            @Throws(DocumentException::class)
            override fun endElement(worker: HTMLWorker, tag: String) {
                worker.carriageReturn()
                if (worker.isPendingTR)
                    worker.endElement(HtmlTags.TR)
                worker.updateChain(tag)
                worker.processTable()
                worker.popTableState()
                worker.isSkipText = false
            }

        }
        val TR: HTMLTagProcessor = object : HTMLTagProcessor {

            /**
             * @throws DocumentException
             * *
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.startElement
             */
            @Throws(DocumentException::class)
            override fun startElement(worker: HTMLWorker, tag: String, attrs: MutableMap<String, String>) {
                worker.carriageReturn()
                if (worker.isPendingTR)
                    worker.endElement(tag)
                worker.isSkipText = true
                worker.isPendingTR = true
                worker.updateChain(tag, attrs)
            }

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.endElement
             */
            @Throws(DocumentException::class)
            override fun endElement(worker: HTMLWorker, tag: String) {
                worker.carriageReturn()
                if (worker.isPendingTD)
                    worker.endElement(HtmlTags.TD)
                worker.isPendingTR = false
                worker.updateChain(tag)
                worker.processRow()
                worker.isSkipText = true
            }

        }
        val TD: HTMLTagProcessor = object : HTMLTagProcessor {

            /**
             * @throws DocumentException
             * *
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.startElement
             */
            @Throws(DocumentException::class)
            override fun startElement(worker: HTMLWorker, tag: String, attrs: MutableMap<String, String>) {
                worker.carriageReturn()
                if (worker.isPendingTD)
                    worker.endElement(tag)
                worker.isSkipText = false
                worker.isPendingTD = true
                worker.updateChain(HtmlTags.TD, attrs)
                worker.pushToStack(worker.createCell(tag))
            }

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.endElement
             */
            @Throws(DocumentException::class)
            override fun endElement(worker: HTMLWorker, tag: String) {
                worker.carriageReturn()
                worker.isPendingTD = false
                worker.updateChain(HtmlTags.TD)
                worker.isSkipText = true
            }

        }

        val IMG: HTMLTagProcessor = object : HTMLTagProcessor {

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.startElement
             */
            @Throws(DocumentException::class, IOException::class)
            override fun startElement(worker: HTMLWorker, tag: String, attrs: MutableMap<String, String>) {
                worker.updateChain(tag, attrs)
                worker.processImage(worker.createImage(attrs), attrs)
                worker.updateChain(tag)
            }

            /**
             * @see com.itextpdf.text.html.simpleparser.HTMLTagProcessors.endElement
             */
            override fun endElement(worker: HTMLWorker, tag: String) {
            }

        }

        /** Serial version UID.  */
        private val serialVersionUID = -959260811961222824L
    }
}
/**
 * Creates a Map containing supported tags.
 */
