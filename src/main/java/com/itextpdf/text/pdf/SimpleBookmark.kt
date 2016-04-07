/*
 * $Id: 90321b6050f982c263a694fdc4d904c46e49decd $
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

import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.Writer
import java.util.ArrayList
import java.util.HashMap
import java.util.Stack
import java.util.StringTokenizer

import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.xml.XMLUtil
import com.itextpdf.text.xml.simpleparser.IanaEncodings
import com.itextpdf.text.xml.simpleparser.SimpleXMLDocHandler
import com.itextpdf.text.xml.simpleparser.SimpleXMLParser

/**
 * Bookmark processing in a simple way. It has some limitations, mainly the only
 * action types supported are GoTo, GoToR, URI and Launch.
 *
 *
 * The list structure is composed by a number of HashMap, keyed by strings, one HashMap
 * for each bookmark.
 * The element values are all strings with the exception of the key "Kids" that has
 * another list for the child bookmarks.
 *
 *
 * All the bookmarks have a "Title" with the
 * bookmark title and optionally a "Style" that can be "bold", "italic" or a
 * combination of both. They can also have a "Color" key with a value of three
 * floats with values in the range 0.0 to 1.0 separated by spaces.
 * The key "Open" can have the values "true" or "false" and
 * signals the open status of the children. It's "true" by default.
 *
 *
 * The actions and the parameters can be:
 *
 *  * "Action" = "GoTo" - "Page" | "Named"
 *
 *  * "Page" = "3 XYZ 70 400 null" - page number followed by a destination (/XYZ is also accepted)
 *  * "Named" = "named_destination"
 *
 *  * "Action" = "GoToR" - "Page" | "Named" | "NamedN", "File", ["NewWindow"]
 *
 *  * "Page" = "3 XYZ 70 400 null" - page number followed by a destination (/XYZ is also accepted)
 *  * "Named" = "named_destination_as_a_string"
 *  * "NamedN" = "named_destination_as_a_name"
 *  * "File" - "the_file_to_open"
 *  * "NewWindow" - "true" or "false"
 *
 *  * "Action" = "URI" - "URI"
 *
 *  * "URI" = "http://sf.net" - URI to jump to
 *
 *  * "Action" = "Launch" - "File"
 *
 *  * "File" - "the_file_to_open_or_execute"
 *
 * @author Paulo Soares
 */
class SimpleBookmark
/** Creates a new instance of SimpleBookmark  */
private constructor() : SimpleXMLDocHandler {

    private var topList: ArrayList<HashMap<String, Any>>? = null
    private val attr = Stack<HashMap<String, Any>>()

    override fun endDocument() {
    }

    @SuppressWarnings("unchecked")
    override fun endElement(tag: String) {
        if (tag == "Bookmark") {
            if (attr.isEmpty())
                return
            else
                throw RuntimeException(MessageLocalization.getComposedMessage("bookmark.end.tag.out.of.place"))
        }
        if (tag != "Title")
            throw RuntimeException(MessageLocalization.getComposedMessage("invalid.end.tag.1", tag))
        val attributes = attr.pop()
        val title = attributes["Title"] as String
        attributes.put("Title", title.trim { it <= ' ' })
        var named: String? = attributes["Named"] as String
        if (named != null)
            attributes.put("Named", SimpleNamedDestination.unEscapeBinaryString(named))
        named = attributes["NamedN"] as String
        if (named != null)
            attributes.put("NamedN", SimpleNamedDestination.unEscapeBinaryString(named))
        if (attr.isEmpty())
            topList!!.add(attributes)
        else {
            val parent = attr.peek()
            var kids: MutableList<HashMap<String, Any>>? = parent["Kids"] as List<HashMap<String, Any>>
            if (kids == null) {
                kids = ArrayList<HashMap<String, Any>>()
                parent.put("Kids", kids)
            }
            kids.add(attributes)
        }
    }

    override fun startDocument() {
    }

    override fun startElement(tag: String, h: Map<String, String>) {
        if (topList == null) {
            if (tag == "Bookmark") {
                topList = ArrayList<HashMap<String, Any>>()
                return
            } else
                throw RuntimeException(MessageLocalization.getComposedMessage("root.element.is.not.bookmark.1", tag))
        }
        if (tag != "Title")
            throw RuntimeException(MessageLocalization.getComposedMessage("tag.1.not.allowed", tag))
        val attributes = HashMap<String, Any>(h)
        attributes.put("Title", "")
        attributes.remove("Kids")
        attr.push(attributes)
    }

    override fun text(str: String) {
        if (attr.isEmpty())
            return
        val attributes = attr.peek()
        var title = attributes["Title"] as String
        title += str
        attributes.put("Title", title)
    }

    companion object {

        private fun bookmarkDepth(reader: PdfReader, outline: PdfDictionary?, pages: IntHashtable, processCurrentOutlineOnly: Boolean): List<HashMap<String, Any>> {
            var outline = outline
            val list = ArrayList<HashMap<String, Any>>()
            while (outline != null) {
                val map = HashMap<String, Any>()
                val title = PdfReader.getPdfObjectRelease(outline.get(PdfName.TITLE)) as PdfString?
                map.put("Title", title.toUnicodeString())
                val color = PdfReader.getPdfObjectRelease(outline.get(PdfName.C)) as PdfArray?
                if (color != null && color.size() == 3) {
                    val out = ByteBuffer()
                    out.append(color.getAsNumber(0).floatValue()).append(' ')
                    out.append(color.getAsNumber(1).floatValue()).append(' ')
                    out.append(color.getAsNumber(2).floatValue())
                    map.put("Color", PdfEncodings.convertToString(out.toByteArray(), null))
                }
                val style = PdfReader.getPdfObjectRelease(outline.get(PdfName.F)) as PdfNumber?
                if (style != null) {
                    val f = style.intValue()
                    var s = ""
                    if (f and 1 != 0)
                        s += "italic "
                    if (f and 2 != 0)
                        s += "bold "
                    s = s.trim { it <= ' ' }
                    if (s.length != 0)
                        map.put("Style", s)
                }
                val count = PdfReader.getPdfObjectRelease(outline.get(PdfName.COUNT)) as PdfNumber?
                if (count != null && count.intValue() < 0)
                    map.put("Open", "false")
                try {
                    var dest = PdfReader.getPdfObjectRelease(outline.get(PdfName.DEST))
                    if (dest != null) {
                        mapGotoBookmark(map, dest, pages) //changed by ujihara 2004-06-13
                    } else {
                        val action = PdfReader.getPdfObjectRelease(outline.get(PdfName.A)) as PdfDictionary?
                        if (action != null) {
                            if (PdfName.GOTO == PdfReader.getPdfObjectRelease(action.get(PdfName.S))) {
                                dest = PdfReader.getPdfObjectRelease(action.get(PdfName.D))
                                if (dest != null) {
                                    mapGotoBookmark(map, dest, pages)
                                }
                            } else if (PdfName.URI == PdfReader.getPdfObjectRelease(action.get(PdfName.S))) {
                                map.put("Action", "URI")
                                map.put("URI", (PdfReader.getPdfObjectRelease(action.get(PdfName.URI)) as PdfString).toUnicodeString())
                            } else if (PdfName.JAVASCRIPT == PdfReader.getPdfObjectRelease(action.get(PdfName.S))) {
                                map.put("Action", "JS")
                                map.put("Code", PdfReader.getPdfObjectRelease(action.get(PdfName.JS))!!.toString())
                            } else if (PdfName.GOTOR == PdfReader.getPdfObjectRelease(action.get(PdfName.S))) {
                                dest = PdfReader.getPdfObjectRelease(action.get(PdfName.D))
                                if (dest != null) {
                                    if (dest.isString)
                                        map.put("Named", dest.toString())
                                    else if (dest.isName)
                                        map.put("NamedN", PdfName.decodeName(dest.toString()))
                                    else if (dest.isArray) {
                                        val arr = dest as PdfArray?
                                        val s = StringBuffer()
                                        s.append(arr.getPdfObject(0).toString())
                                        s.append(' ').append(arr.getPdfObject(1).toString())
                                        for (k in 2..arr.size() - 1)
                                            s.append(' ').append(arr.getPdfObject(k).toString())
                                        map.put("Page", s.toString())
                                    }
                                }
                                map.put("Action", "GoToR")
                                var file = PdfReader.getPdfObjectRelease(action.get(PdfName.F))
                                if (file != null) {
                                    if (file.isString)
                                        map.put("File", (file as PdfString).toUnicodeString())
                                    else if (file.isDictionary) {
                                        file = PdfReader.getPdfObject((file as PdfDictionary).get(PdfName.F))
                                        if (file!!.isString)
                                            map.put("File", (file as PdfString).toUnicodeString())
                                    }
                                }
                                val newWindow = PdfReader.getPdfObjectRelease(action.get(PdfName.NEWWINDOW))
                                if (newWindow != null)
                                    map.put("NewWindow", newWindow.toString())
                            } else if (PdfName.LAUNCH == PdfReader.getPdfObjectRelease(action.get(PdfName.S))) {
                                map.put("Action", "Launch")
                                var file = PdfReader.getPdfObjectRelease(action.get(PdfName.F))
                                if (file == null)
                                    file = PdfReader.getPdfObjectRelease(action.get(PdfName.WIN))
                                if (file != null) {
                                    if (file.isString)
                                        map.put("File", (file as PdfString).toUnicodeString())
                                    else if (file.isDictionary) {
                                        file = PdfReader.getPdfObjectRelease((file as PdfDictionary).get(PdfName.F))
                                        if (file!!.isString)
                                            map.put("File", (file as PdfString).toUnicodeString())
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    //empty on purpose
                }

                val first = PdfReader.getPdfObjectRelease(outline.get(PdfName.FIRST)) as PdfDictionary?
                if (first != null) {
                    map.put("Kids", bookmarkDepth(reader, first, pages, false))
                }
                list.add(map)
                if (!processCurrentOutlineOnly)
                    outline = PdfReader.getPdfObjectRelease(outline.get(PdfName.NEXT)) as PdfDictionary?
                else
                    outline = null
            }
            return list
        }

        private fun mapGotoBookmark(map: HashMap<String, Any>, dest: PdfObject, pages: IntHashtable) {
            if (dest.isString)
                map.put("Named", dest.toString())
            else if (dest.isName)
                map.put("Named", PdfName.decodeName(dest.toString()))
            else if (dest.isArray)
                map.put("Page", makeBookmarkParam(dest as PdfArray, pages)) //changed by ujihara 2004-06-13
            map.put("Action", "GoTo")
        }

        private fun makeBookmarkParam(dest: PdfArray, pages: IntHashtable): String {
            val s = StringBuffer()
            val obj = dest.getPdfObject(0)
            if (obj.isNumber)
                s.append((obj as PdfNumber).intValue() + 1)
            else
                s.append(pages.get(getNumber(obj as PdfIndirectReference))) //changed by ujihara 2004-06-13
            s.append(' ').append(dest.getPdfObject(1).toString().substring(1))
            for (k in 2..dest.size() - 1)
                s.append(' ').append(dest.getPdfObject(k).toString())
            return s.toString()
        }

        /**
         * Gets number of indirect. If type of directed indirect is PAGES, it refers PAGE object through KIDS.
         * (Contributed by Kazuya Ujihara)
         * @param indirect
         * * 2004-06-13
         */
        private fun getNumber(indirect: PdfIndirectReference): Int {
            var indirect = indirect
            val pdfObj = PdfReader.getPdfObjectRelease(indirect) as PdfDictionary?
            if (pdfObj.contains(PdfName.TYPE) && pdfObj.get(PdfName.TYPE) == PdfName.PAGES && pdfObj.contains(PdfName.KIDS)) {
                val kids = pdfObj.get(PdfName.KIDS) as PdfArray?
                indirect = kids.getPdfObject(0) as PdfIndirectReference
            }
            return indirect.number
        }

        /**
         * Gets a List with the bookmarks. It returns null if
         * the document doesn't have any bookmarks.
         * @param reader the document
         * *
         * @return a List with the bookmarks or null if the
         * * document doesn't have any
         */
        fun getBookmark(reader: PdfReader): List<HashMap<String, Any>>? {
            val catalog = reader.catalog
            val obj = PdfReader.getPdfObjectRelease(catalog.get(PdfName.OUTLINES))
            if (obj == null || !obj.isDictionary)
                return null
            val outlines = obj as PdfDictionary?
            return SimpleBookmark.getBookmark(reader, outlines, false)
        }

        /**
         * Gets a List with the bookmarks that are children of outline. It returns null if
         * the document doesn't have any bookmarks.
         * @param reader the document
         * *
         * @param outline the outline dictionary to get bookmarks from
         * *
         * @param includeRoot indicates if to include outline parameter itself into returned list of bookmarks
         * *
         * @return a List with the bookmarks or null if the
         * * document doesn't have any
         */
        fun getBookmark(reader: PdfReader, outline: PdfDictionary?, includeRoot: Boolean): List<HashMap<String, Any>>? {
            val catalog = reader.catalog
            if (outline == null)
                return null
            val pages = IntHashtable()
            val numPages = reader.numberOfPages
            for (k in 1..numPages) {
                pages.put(reader.getPageOrigRef(k).number, k)
                reader.releasePage(k)
            }
            if (includeRoot)
                return bookmarkDepth(reader, outline, pages, true)
            else
                return bookmarkDepth(reader, PdfReader.getPdfObjectRelease(outline.get(PdfName.FIRST)) as PdfDictionary?, pages, false)
        }

        /**
         * Removes the bookmark entries for a number of page ranges. The page ranges
         * consists of a number of pairs with the start/end page range. The page numbers
         * are inclusive.
         * @param list the bookmarks
         * *
         * @param pageRange the page ranges, always in pairs.
         */
        @SuppressWarnings("unchecked")
        fun eliminatePages(list: List<HashMap<String, Any>>?, pageRange: IntArray) {
            if (list == null)
                return
            val it = list.listIterator()
            while (it.hasNext()) {
                val map = it.next()
                var hit = false
                if ("GoTo" == map["Action"]) {
                    var page: String? = map["Page"] as String
                    if (page != null) {
                        page = page.trim { it <= ' ' }
                        val idx = page.indexOf(' ')
                        val pageNum: Int
                        if (idx < 0)
                            pageNum = Integer.parseInt(page)
                        else
                            pageNum = Integer.parseInt(page.substring(0, idx))
                        val len = pageRange.size and 0xfffffffe.toInt()
                        var k = 0
                        while (k < len) {
                            if (pageNum >= pageRange[k] && pageNum <= pageRange[k + 1]) {
                                hit = true
                                break
                            }
                            k += 2
                        }
                    }
                }
                var kids: List<HashMap<String, Any>>? = map["Kids"] as List<HashMap<String, Any>>
                if (kids != null) {
                    eliminatePages(kids, pageRange)
                    if (kids.isEmpty()) {
                        map.remove("Kids")
                        kids = null
                    }
                }
                if (hit) {
                    if (kids == null)
                        it.remove()
                    else {
                        map.remove("Action")
                        map.remove("Page")
                        map.remove("Named")
                    }
                }
            }
        }

        /**
         * For the pages in range add the pageShift to the page number.
         * The page ranges
         * consists of a number of pairs with the start/end page range. The page numbers
         * are inclusive.
         * @param list the bookmarks
         * *
         * @param pageShift the number to add to the pages in range
         * *
         * @param pageRange the page ranges, always in pairs. It can be null
         * * to include all the pages
         */
        @SuppressWarnings("unchecked")
        fun shiftPageNumbers(list: List<HashMap<String, Any>>?, pageShift: Int, pageRange: IntArray?) {
            if (list == null)
                return
            val it = list.listIterator()
            while (it.hasNext()) {
                val map = it.next()
                if ("GoTo" == map["Action"]) {
                    var page: String? = map["Page"] as String
                    if (page != null) {
                        page = page.trim { it <= ' ' }
                        val idx = page.indexOf(' ')
                        val pageNum: Int
                        if (idx < 0)
                            pageNum = Integer.parseInt(page)
                        else
                            pageNum = Integer.parseInt(page.substring(0, idx))
                        var hit = false
                        if (pageRange == null)
                            hit = true
                        else {
                            val len = pageRange.size and 0xfffffffe.toInt()
                            var k = 0
                            while (k < len) {
                                if (pageNum >= pageRange[k] && pageNum <= pageRange[k + 1]) {
                                    hit = true
                                    break
                                }
                                k += 2
                            }
                        }
                        if (hit) {
                            if (idx < 0)
                                page = Integer.toString(pageNum + pageShift)
                            else
                                page = pageNum + pageShift + page.substring(idx)
                        }
                        map.put("Page", page)
                    }
                }
                val kids = map["Kids"] as List<HashMap<String, Any>>
                if (kids != null)
                    shiftPageNumbers(kids, pageShift, pageRange)
            }
        }

        internal fun createOutlineAction(outline: PdfDictionary, map: HashMap<String, Any>, writer: PdfWriter, namedAsNames: Boolean) {
            try {
                val action = map["Action"] as String
                if ("GoTo" == action) {
                    var p: String
                    if ((p = map["Named"] as String) != null) {
                        if (namedAsNames)
                            outline.put(PdfName.DEST, PdfName(p))
                        else
                            outline.put(PdfName.DEST, PdfString(p, null))
                    } else if ((p = map["Page"] as String) != null) {
                        val ar = PdfArray()
                        val tk = StringTokenizer(p)
                        val n = Integer.parseInt(tk.nextToken())
                        ar.add(writer.getPageReference(n))
                        if (!tk.hasMoreTokens()) {
                            ar.add(PdfName.XYZ)
                            ar.add(floatArrayOf(0f, 10000f, 0f))
                        } else {
                            var fn = tk.nextToken()
                            if (fn.startsWith("/"))
                                fn = fn.substring(1)
                            ar.add(PdfName(fn))
                            var k = 0
                            while (k < 4 && tk.hasMoreTokens()) {
                                fn = tk.nextToken()
                                if (fn == "null")
                                    ar.add(PdfNull.PDFNULL)
                                else
                                    ar.add(PdfNumber(fn))
                                ++k
                            }
                        }
                        outline.put(PdfName.DEST, ar)
                    }
                } else if ("GoToR" == action) {
                    var p: String
                    val dic = PdfDictionary()
                    if ((p = map["Named"] as String) != null)
                        dic.put(PdfName.D, PdfString(p, null))
                    else if ((p = map["NamedN"] as String) != null)
                        dic.put(PdfName.D, PdfName(p))
                    else if ((p = map["Page"] as String) != null) {
                        val ar = PdfArray()
                        val tk = StringTokenizer(p)
                        ar.add(PdfNumber(tk.nextToken()))
                        if (!tk.hasMoreTokens()) {
                            ar.add(PdfName.XYZ)
                            ar.add(floatArrayOf(0f, 10000f, 0f))
                        } else {
                            var fn = tk.nextToken()
                            if (fn.startsWith("/"))
                                fn = fn.substring(1)
                            ar.add(PdfName(fn))
                            var k = 0
                            while (k < 4 && tk.hasMoreTokens()) {
                                fn = tk.nextToken()
                                if (fn == "null")
                                    ar.add(PdfNull.PDFNULL)
                                else
                                    ar.add(PdfNumber(fn))
                                ++k
                            }
                        }
                        dic.put(PdfName.D, ar)
                    }
                    val file = map["File"] as String
                    if (dic.size() > 0 && file != null) {
                        dic.put(PdfName.S, PdfName.GOTOR)
                        dic.put(PdfName.F, PdfString(file))
                        val nw = map["NewWindow"] as String
                        if (nw != null) {
                            if (nw == "true")
                                dic.put(PdfName.NEWWINDOW, PdfBoolean.PDFTRUE)
                            else if (nw == "false")
                                dic.put(PdfName.NEWWINDOW, PdfBoolean.PDFFALSE)
                        }
                        outline.put(PdfName.A, dic)
                    }
                } else if ("URI" == action) {
                    val uri = map["URI"] as String
                    if (uri != null) {
                        val dic = PdfDictionary()
                        dic.put(PdfName.S, PdfName.URI)
                        dic.put(PdfName.URI, PdfString(uri))
                        outline.put(PdfName.A, dic)
                    }
                } else if ("JS" == action) {
                    val code = map["Code"] as String
                    if (code != null) {
                        outline.put(PdfName.A, PdfAction.javaScript(code, writer))
                    }
                } else if ("Launch" == action) {
                    val file = map["File"] as String
                    if (file != null) {
                        val dic = PdfDictionary()
                        dic.put(PdfName.S, PdfName.LAUNCH)
                        dic.put(PdfName.F, PdfString(file))
                        outline.put(PdfName.A, dic)
                    }
                }
            } catch (e: Exception) {
                // empty on purpose
            }

        }

        @SuppressWarnings("unchecked")
        @Throws(IOException::class)
        fun iterateOutlines(writer: PdfWriter, parent: PdfIndirectReference, kids: List<HashMap<String, Any>>, namedAsNames: Boolean): Array<Any> {
            val refs = arrayOfNulls<PdfIndirectReference>(kids.size)
            for (k in refs.indices)
                refs[k] = writer.pdfIndirectReference
            var ptr = 0
            var count = 0
            val it = kids.listIterator()
            while (it.hasNext()) {
                val map = it.next()
                var lower: Array<Any>? = null
                val subKid = map["Kids"] as List<HashMap<String, Any>>
                if (subKid != null && !subKid.isEmpty())
                    lower = iterateOutlines(writer, refs[ptr], subKid, namedAsNames)
                val outline = PdfDictionary()
                ++count
                if (lower != null) {
                    outline.put(PdfName.FIRST, lower[0] as PdfIndirectReference)
                    outline.put(PdfName.LAST, lower[1] as PdfIndirectReference)
                    val n = (lower[2] as Int).toInt()
                    if ("false" == map["Open"]) {
                        outline.put(PdfName.COUNT, PdfNumber(-n))
                    } else {
                        outline.put(PdfName.COUNT, PdfNumber(n))
                        count += n
                    }
                }
                outline.put(PdfName.PARENT, parent)
                if (ptr > 0)
                    outline.put(PdfName.PREV, refs[ptr - 1])
                if (ptr < refs.size - 1)
                    outline.put(PdfName.NEXT, refs[ptr + 1])
                outline.put(PdfName.TITLE, PdfString(map["Title"] as String, PdfObject.TEXT_UNICODE))
                val color = map["Color"] as String
                if (color != null) {
                    try {
                        val arr = PdfArray()
                        val tk = StringTokenizer(color)
                        for (k in 0..2) {
                            var f = java.lang.Float.parseFloat(tk.nextToken())
                            if (f < 0) f = 0f
                            if (f > 1) f = 1f
                            arr.add(PdfNumber(f))
                        }
                        outline.put(PdfName.C, arr)
                    } catch (e: Exception) {
                    }
                    //in case it's malformed
                }
                var style: String? = map["Style"] as String
                if (style != null) {
                    style = style.toLowerCase()
                    var bits = 0
                    if (style.indexOf("italic") >= 0)
                        bits = bits or 1
                    if (style.indexOf("bold") >= 0)
                        bits = bits or 2
                    if (bits != 0)
                        outline.put(PdfName.F, PdfNumber(bits))
                }
                createOutlineAction(outline, map, writer, namedAsNames)
                writer.addToBody(outline, refs[ptr])
                ++ptr
            }
            return arrayOf<Any>(refs[0], refs[refs.size - 1], Integer.valueOf(count))
        }

        /**
         * Exports the bookmarks to XML. Only of use if the generation is to be include in
         * some other XML document.
         * @param list the bookmarks
         * *
         * @param out the export destination. The writer is not closed
         * *
         * @param indent the indentation level. Pretty printing significant only. Use -1 for no indents.
         * *
         * @param onlyASCII codes above 127 will always be escaped with &amp;#nn; if true,
         * * whatever the encoding
         * *
         * @throws IOException on error
         * *
         * @since 5.0.1 (generic type in signature)
         */
        @SuppressWarnings("unchecked")
        @Throws(IOException::class)
        fun exportToXMLNode(list: List<HashMap<String, Any>>, out: Writer, indent: Int, onlyASCII: Boolean) {
            var dep = ""
            if (indent != -1) {
                for (k in 0..indent - 1)
                    dep += "  "
            }
            for (map in list) {
                var title: String? = null
                out.write(dep)
                out.write("<Title ")
                var kids: List<HashMap<String, Any>>? = null
                for (entry in map.entries) {
                    val key = entry.key
                    if (key == "Title") {
                        title = entry.value as String
                        continue
                    } else if (key == "Kids") {
                        kids = entry.value as List<HashMap<String, Any>>
                        continue
                    } else {
                        out.write(key)
                        out.write("=\"")
                        var value = entry.value as String
                        if (key == "Named" || key == "NamedN")
                            value = SimpleNamedDestination.escapeBinaryString(value)
                        out.write(XMLUtil.escapeXML(value, onlyASCII))
                        out.write("\" ")
                    }
                }
                out.write(">")
                if (title == null)
                    title = ""
                out.write(XMLUtil.escapeXML(title, onlyASCII))
                if (kids != null) {
                    out.write("\n")
                    exportToXMLNode(kids, out, if (indent == -1) indent else indent + 1, onlyASCII)
                    out.write(dep)
                }
                out.write("</Title>\n")
            }
        }

        /**
         * Exports the bookmarks to XML. The DTD for this XML is:
         *
         *
         *
         * &lt;?xml version='1.0' encoding='UTF-8'?&gt;
         * &lt;!ELEMENT Title (#PCDATA|Title)*&gt;
         * &lt;!ATTLIST Title
         * Action CDATA #IMPLIED
         * Open CDATA #IMPLIED
         * Page CDATA #IMPLIED
         * URI CDATA #IMPLIED
         * File CDATA #IMPLIED
         * Named CDATA #IMPLIED
         * NamedN CDATA #IMPLIED
         * NewWindow CDATA #IMPLIED
         * Style CDATA #IMPLIED
         * Color CDATA #IMPLIED
         * &gt;
         * &lt;!ELEMENT Bookmark (Title)*&gt;
         *
         * @param list the bookmarks
         * *
         * @param out the export destination. The stream is not closed
         * *
         * @param encoding the encoding according to IANA conventions
         * *
         * @param onlyASCII codes above 127 will always be escaped with &amp;#nn; if true,
         * * whatever the encoding
         * *
         * @throws IOException on error
         * *
         * @since 5.0.1 (generic type in signature)
         */
        @Throws(IOException::class)
        fun exportToXML(list: List<HashMap<String, Any>>, out: OutputStream, encoding: String, onlyASCII: Boolean) {
            val jenc = IanaEncodings.getJavaEncoding(encoding)
            val wrt = BufferedWriter(OutputStreamWriter(out, jenc))
            exportToXML(list, wrt, encoding, onlyASCII)
        }

        /**
         * Exports the bookmarks to XML.
         * @param list the bookmarks
         * *
         * @param wrt the export destination. The writer is not closed
         * *
         * @param encoding the encoding according to IANA conventions
         * *
         * @param onlyASCII codes above 127 will always be escaped with &amp;#nn; if true,
         * * whatever the encoding
         * *
         * @throws IOException on error
         * *
         * @since 5.0.1 (generic type in signature)
         */
        @Throws(IOException::class)
        fun exportToXML(list: List<HashMap<String, Any>>, wrt: Writer, encoding: String, onlyASCII: Boolean) {
            wrt.write("<?xml version=\"1.0\" encoding=\"")
            wrt.write(XMLUtil.escapeXML(encoding, onlyASCII))
            wrt.write("\"?>\n<Bookmark>\n")
            exportToXMLNode(list, wrt, 1, onlyASCII)
            wrt.write("</Bookmark>\n")
            wrt.flush()
        }

        /**
         * Import the bookmarks from XML.
         * @param in the XML source. The stream is not closed
         * *
         * @throws IOException on error
         * *
         * @return the bookmarks
         */
        @Throws(IOException::class)
        fun importFromXML(`in`: InputStream): List<HashMap<String, Any>> {
            val book = SimpleBookmark()
            SimpleXMLParser.parse(book, `in`)
            return book.topList
        }

        /**
         * Import the bookmarks from XML.
         * @param in the XML source. The reader is not closed
         * *
         * @throws IOException on error
         * *
         * @return the bookmarks
         */
        @Throws(IOException::class)
        fun importFromXML(`in`: Reader): List<HashMap<String, Any>> {
            val book = SimpleBookmark()
            SimpleXMLParser.parse(book, `in`)
            return book.topList
        }
    }
}
