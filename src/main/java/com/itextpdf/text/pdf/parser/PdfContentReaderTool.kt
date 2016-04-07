/*
 * $Id: 7b3fd6af7fdafbe36268e360d24e2651911b35aa $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Kevin Day, Bruno Lowagie, Paulo Soares, et al.
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
package com.itextpdf.text.pdf.parser

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.PrintWriter
import java.util.ArrayList

import com.itextpdf.text.pdf.PdfDictionary
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStream
import com.itextpdf.text.pdf.RandomAccessFileOrArray

/**
 * Tool that parses the content of a PDF document.
 * @since    2.1.4
 */
object PdfContentReaderTool {

    /**
     * Shows the detail of a dictionary.
     * @param dic    the dictionary of which you want the detail
     * *
     * @param depth    the depth of the current dictionary (for nested dictionaries)
     * *
     * @return    a String representation of the dictionary
     */
    @JvmOverloads fun getDictionaryDetail(dic: PdfDictionary, depth: Int = 0): String {
        val builder = StringBuffer()
        builder.append('(')
        val subDictionaries = ArrayList<PdfName>()
        for (key in dic.keys) {
            val `val` = dic.getDirectObject(key)
            if (`val`.isDictionary)
                subDictionaries.add(key)
            builder.append(key)
            builder.append('=')
            builder.append(`val`)
            builder.append(", ")
        }
        if (builder.length >= 2)
            builder.setLength(builder.length - 2)
        builder.append(')')
        for (pdfSubDictionaryName in subDictionaries) {
            builder.append('\n')
            for (i in 0..depth + 1 - 1) {
                builder.append('\t')
            }
            builder.append("Subdictionary ")
            builder.append(pdfSubDictionaryName)
            builder.append(" = ")
            builder.append(getDictionaryDetail(dic.getAsDict(pdfSubDictionaryName), depth + 1))
        }
        return builder.toString()
    }

    /**
     * Displays a summary of the entries in the XObject dictionary for the stream
     * @param resourceDic the resource dictionary for the stream
     * *
     * @return a string with the summary of the entries
     * *
     * @throws IOException
     * *
     * @since 5.0.2
     */
    @Throws(IOException::class)
    fun getXObjectDetail(resourceDic: PdfDictionary): String {
        val sb = StringBuilder()

        val xobjects = resourceDic.getAsDict(PdfName.XOBJECT) ?: return "No XObjects"
        for (entryName in xobjects.keys) {
            val xobjectStream = xobjects.getAsStream(entryName)

            sb.append("------ " + entryName + " - subtype = " + xobjectStream.get(PdfName.SUBTYPE) + " = " + xobjectStream.getAsNumber(PdfName.LENGTH) + " bytes ------\n")

            if (xobjectStream.get(PdfName.SUBTYPE) != PdfName.IMAGE) {

                val contentBytes = ContentByteUtils.getContentBytesFromContentObject(xobjectStream)

                val `is` = ByteArrayInputStream(contentBytes)
                var ch: Int
                while ((ch = `is`.read()) != -1) {
                    sb.append(ch.toChar())
                }

                sb.append("------ " + entryName + " - subtype = " + xobjectStream.get(PdfName.SUBTYPE) + "End of Content" + "------\n")
            }
        }

        return sb.toString()
    }

    /**
     * Writes information about a specific page from PdfReader to the specified output stream.
     * @since 2.1.5
     * *
     * @param reader    the PdfReader to read the page content from
     * *
     * @param pageNum   the page number to read
     * *
     * @param out       the output stream to send the content to
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun listContentStreamForPage(reader: PdfReader, pageNum: Int, out: PrintWriter) {
        out.println("==============Page $pageNum====================")
        out.println("- - - - - Dictionary - - - - - -")
        val pageDictionary = reader.getPageN(pageNum)
        out.println(getDictionaryDetail(pageDictionary))

        out.println("- - - - - XObject Summary - - - - - -")
        out.println(getXObjectDetail(pageDictionary.getAsDict(PdfName.RESOURCES)))

        out.println("- - - - - Content Stream - - - - - -")
        val f = reader.safeFile

        val contentBytes = reader.getPageContent(pageNum, f)
        f.close()

        out.flush()

        val `is` = ByteArrayInputStream(contentBytes)
        var ch: Int
        while ((ch = `is`.read()) != -1) {
            out.print(ch.toChar())
        }

        out.flush()

        out.println("- - - - - Text Extraction - - - - - -")
        val extractedText = PdfTextExtractor.getTextFromPage(reader, pageNum, LocationTextExtractionStrategy())
        if (extractedText.length != 0)
            out.println(extractedText)
        else
            out.println("No text found on page " + pageNum)

        out.println()

    }

    /**
     * Writes information about each page in a PDF file to the specified output stream.
     * @since 2.1.5
     * *
     * @param pdfFile    a File instance referring to a PDF file
     * *
     * @param out       the output stream to send the content to
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun listContentStream(pdfFile: File, out: PrintWriter) {
        val reader = PdfReader(pdfFile.canonicalPath)

        val maxPageNum = reader.numberOfPages

        for (pageNum in 1..maxPageNum) {
            listContentStreamForPage(reader, pageNum, out)
        }

    }

    /**
     * Writes information about the specified page in a PDF file to the specified output stream.
     * @since 2.1.5
     * *
     * @param pdfFile   a File instance referring to a PDF file
     * *
     * @param pageNum   the page number to read
     * *
     * @param out       the output stream to send the content to
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun listContentStream(pdfFile: File, pageNum: Int, out: PrintWriter) {
        val reader = PdfReader(pdfFile.canonicalPath)

        listContentStreamForPage(reader, pageNum, out)
    }

    /**
     * Writes information about each page in a PDF file to the specified file, or System.out.
     * @param args
     */
    @JvmStatic fun main(args: Array<String>) {
        try {
            if (args.size < 1 || args.size > 3) {
                println("Usage:  PdfContentReaderTool <pdf file> [<output file>|stdout] [<page num>]")
                return
            }

            var writer = PrintWriter(System.out)
            if (args.size >= 2) {
                if (args[1].compareTo("stdout", ignoreCase = true) != 0) {
                    println("Writing PDF content to " + args[1])
                    writer = PrintWriter(FileOutputStream(File(args[1])))
                }
            }

            var pageNum = -1
            if (args.size >= 3) {
                pageNum = Integer.parseInt(args[2])
            }

            if (pageNum == -1) {
                listContentStream(File(args[0]), writer)
            } else {
                listContentStream(File(args[0]), pageNum, writer)
            }
            writer.flush()

            if (args.size >= 2) {
                writer.close()
                println("Finished writing content to " + args[1])
            }
        } catch (e: Exception) {
            e.printStackTrace(System.err)
        }

    }

}
/**
 * Shows the detail of a dictionary.
 * This is similar to the PdfLister functionality.
 * @param dic    the dictionary of which you want the detail
 * *
 * @return    a String representation of the dictionary
 */
