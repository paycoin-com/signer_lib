/*
 * $Id: d920441d783ee1fece171681dd68d9d7b37eca57 $
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
package com.itextpdf.testutils

import com.itextpdf.text.BaseColor
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Meta
import com.itextpdf.text.Rectangle
import com.itextpdf.text.io.RandomAccessSourceFactory
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.parser.ContentByteUtils
import com.itextpdf.text.pdf.parser.ImageRenderInfo
import com.itextpdf.text.pdf.parser.InlineImageInfo
import com.itextpdf.text.pdf.parser.InlineImageUtils
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor
import com.itextpdf.text.pdf.parser.RenderListener
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy
import com.itextpdf.text.pdf.parser.TaggedPdfReaderTool
import com.itextpdf.text.pdf.parser.TextExtractionStrategy
import com.itextpdf.text.pdf.parser.TextRenderInfo
import com.itextpdf.text.xml.XMLUtil
import com.itextpdf.text.xml.xmp.PdfProperties
import com.itextpdf.text.xml.xmp.XmpBasicProperties
import com.itextpdf.xmp.XMPConst
import com.itextpdf.xmp.XMPException
import com.itextpdf.xmp.XMPMeta
import com.itextpdf.xmp.XMPMetaFactory
import com.itextpdf.xmp.XMPUtils
import com.itextpdf.xmp.options.SerializeOptions

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.ArrayList
import java.util.Arrays
import java.util.Comparator
import java.util.HashMap
import java.util.LinkedHashMap
import java.util.Stack
import java.util.StringTokenizer
import java.util.TreeSet

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.SAXException

/**
 * Helper class for tests: uses ghostscript to compare PDFs at a pixel level.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
class CompareTool {

    private inner class ObjectPath {
        protected var baseCmpObject: RefKey? = null
        protected var baseOutObject: RefKey? = null
        protected var path = Stack<PathItem>()
        protected var indirects = Stack<Pair<RefKey>>()

        constructor() {
        }

        protected constructor(baseCmpObject: RefKey, baseOutObject: RefKey) {
            this.baseCmpObject = baseCmpObject
            this.baseOutObject = baseOutObject
        }

        private constructor(baseCmpObject: RefKey, baseOutObject: RefKey, path: Stack<PathItem>) {
            this.baseCmpObject = baseCmpObject
            this.baseOutObject = baseOutObject
            this.path = path
        }

        private inner class Pair<T>(private val first: T, private val second: T) {

            override fun hashCode(): Int {
                return first.hashCode() * 31 + second.hashCode()
            }

            override fun equals(obj: Any?): Boolean {
                return obj is Pair<Any> && first == obj.first && second == obj.second
            }
        }

        private abstract inner class PathItem {
            protected abstract fun toXmlNode(document: Document): Node
        }

        private inner class DictPathItem(internal var key: String) : PathItem() {

            override fun toString(): String {
                return "Dict key: " + key
            }

            override fun hashCode(): Int {
                return key.hashCode()
            }

            override fun equals(obj: Any?): Boolean {
                return obj is DictPathItem && key == obj.key
            }

            override fun toXmlNode(document: Document): Node {
                val element = document.createElement("dictKey")
                element.appendChild(document.createTextNode(key))
                return element
            }
        }

        private inner class ArrayPathItem(internal var index: Int) : PathItem() {

            override fun toString(): String {
                return "Array index: " + index.toString()
            }

            override fun hashCode(): Int {
                return index
            }

            override fun equals(obj: Any?): Boolean {
                return obj is ArrayPathItem && index == obj.index
            }

            override fun toXmlNode(document: Document): Node {
                val element = document.createElement("arrayIndex")
                element.appendChild(document.createTextNode(index.toString()))
                return element
            }
        }

        private inner class OffsetPathItem(internal var offset: Int) : PathItem() {

            override fun toString(): String {
                return "Offset: " + offset.toString()
            }

            override fun hashCode(): Int {
                return offset
            }

            override fun equals(obj: Any?): Boolean {
                return obj is OffsetPathItem && offset == obj.offset
            }

            override fun toXmlNode(document: Document): Node {
                val element = document.createElement("offset")
                element.appendChild(document.createTextNode(offset.toString()))
                return element
            }
        }

        fun resetDirectPath(baseCmpObject: RefKey, baseOutObject: RefKey): ObjectPath {
            val newPath = ObjectPath(baseCmpObject, baseOutObject)
            newPath.indirects = indirects.clone() as Stack<Pair<RefKey>>
            newPath.indirects.add(Pair(baseCmpObject, baseOutObject))
            return newPath
        }

        fun isComparing(baseCmpObject: RefKey, baseOutObject: RefKey): Boolean {
            return indirects.contains(Pair(baseCmpObject, baseOutObject))
        }

        fun pushArrayItemToPath(index: Int) {
            path.add(ArrayPathItem(index))
        }

        fun pushDictItemToPath(key: String) {
            path.add(DictPathItem(key))
        }

        fun pushOffsetToPath(offset: Int) {
            path.add(OffsetPathItem(offset))
        }

        fun pop() {
            path.pop()
        }

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append(String.format("Base cmp object: %s obj. Base out object: %s obj", baseCmpObject, baseOutObject))
            for (pathItem in path) {
                sb.append("\n")
                sb.append(pathItem.toString())
            }
            return sb.toString()
        }

        override fun hashCode(): Int {
            val hashCode1 = if (baseCmpObject != null) baseCmpObject!!.hashCode() else 1
            val hashCode2 = if (baseOutObject != null) baseOutObject!!.hashCode() else 1
            var hashCode = hashCode1 * 31 + hashCode2
            for (pathItem in path) {
                hashCode *= 31
                hashCode += pathItem.hashCode()
            }
            return hashCode
        }

        override fun equals(obj: Any?): Boolean {
            return obj is ObjectPath && baseCmpObject == obj.baseCmpObject && baseOutObject == obj.baseOutObject &&
                    path == obj.path
        }

        protected fun clone(): Any {
            return ObjectPath(baseCmpObject, baseOutObject, path.clone() as Stack<PathItem>)
        }

        fun toXmlNode(document: Document): Node {
            val element = document.createElement("path")
            val baseNode = document.createElement("base")
            baseNode.setAttribute("cmp", baseCmpObject!!.toString() + " obj")
            baseNode.setAttribute("out", baseOutObject!!.toString() + " obj")
            element.appendChild(baseNode)
            for (pathItem in path) {
                element.appendChild(pathItem.toXmlNode(document))
            }
            return element
        }
    }

    protected inner class CompareResult(messageLimit: Int) {
        // LinkedHashMap to retain order. HashMap has different order in Java6/7 and Java8
        protected var differences: MutableMap<ObjectPath, String> = LinkedHashMap()
        protected var messageLimit = 1

        init {
            this.messageLimit = messageLimit
        }

        val isOk: Boolean
            get() = differences.size == 0

        val errorCount: Int
            get() = differences.size

        protected val isMessageLimitReached: Boolean
            get() = differences.size >= messageLimit

        val report: String
            get() {
                val sb = StringBuilder()
                var firstEntry = true
                for (entry in differences.entries) {
                    if (!firstEntry)
                        sb.append("-----------------------------").append("\n")
                    val diffPath = entry.key
                    sb.append(entry.value).append("\n").append(diffPath.toString()).append("\n")
                    firstEntry = false
                }
                return sb.toString()
            }

        protected fun addError(path: ObjectPath, message: String) {
            if (differences.size < messageLimit) {
                differences.put(path.clone() as ObjectPath, message)
            }
        }

        @Throws(ParserConfigurationException::class, TransformerException::class)
        fun writeReportToXml(stream: OutputStream) {
            val xmlReport = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
            val root = xmlReport.createElement("report")
            val errors = xmlReport.createElement("errors")
            errors.setAttribute("count", differences.size.toString())
            root.appendChild(errors)
            for (entry in differences.entries) {
                val errorNode = xmlReport.createElement("error")
                val message = xmlReport.createElement("message")
                message.appendChild(xmlReport.createTextNode(entry.value))
                val path = entry.key.toXmlNode(xmlReport)
                errorNode.appendChild(message)
                errorNode.appendChild(path)
                errors.appendChild(errorNode)
            }
            xmlReport.appendChild(root)

            val tFactory = TransformerFactory.newInstance()
            val transformer = tFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            val source = DOMSource(xmlReport)
            val result = StreamResult(stream)
            transformer.transform(source, result)
        }
    }

    private val gsExec: String?
    private val compareExec: String?
    private val gsParams = " -dNOPAUSE -dBATCH -sDEVICE=png16m -r150 -sOutputFile=<outputfile> <inputfile>"
    private val compareParams = " \"<image1>\" \"<image2>\" \"<difference>\""

    private var cmpPdf: String? = null
    private var cmpPdfName: String? = null
    private var cmpImage: String? = null
    private var outPdf: String? = null
    private var outPdfName: String? = null
    private var outImage: String? = null

    internal var outPages: MutableList<PdfDictionary>
    internal var outPagesRef: MutableList<RefKey>
    internal var cmpPages: MutableList<PdfDictionary>
    internal var cmpPagesRef: MutableList<RefKey>

    private var compareByContentErrorsLimit = 1
    private var generateCompareByContentXmlReport = false
    var xmlReportName = "report"
    private var floatComparisonError = 0.0
    // if false, the error will be relative
    private var absoluteError = true

    init {
        gsExec = System.getProperty("gsExec")
        compareExec = System.getProperty("compareExec")
    }

    @Throws(IOException::class, InterruptedException::class, DocumentException::class)
    private fun compare(outPath: String, differenceImagePrefix: String, ignoredAreas: Map<Int, List<Rectangle>>?,
                        equalPages: List<Int>? = null): String? {
        var outPath = outPath
        if (gsExec == null)
            return undefinedGsPath
        if (!File(gsExec).exists()) {
            return File(gsExec).absolutePath + " does not exist"
        }
        if (!outPath.endsWith("/"))
            outPath = outPath + "/"
        val targetDir = File(outPath)
        var imageFiles: Array<File>
        var cmpImageFiles: Array<File>

        if (!targetDir.exists()) {
            targetDir.mkdirs()
        } else {
            imageFiles = targetDir.listFiles(PngFileFilter())
            for (file in imageFiles) {
                file.delete()
            }
            cmpImageFiles = targetDir.listFiles(CmpPngFileFilter())
            for (file in cmpImageFiles) {
                file.delete()
            }
        }

        val diffFile = File(outPath + differenceImagePrefix)
        if (diffFile.exists()) {
            diffFile.delete()
        }

        if (ignoredAreas != null && !ignoredAreas.isEmpty()) {
            val cmpReader = PdfReader(cmpPdf)
            val outReader = PdfReader(outPdf)
            val outStamper = PdfStamper(outReader, FileOutputStream(outPath + ignoredAreasPrefix + outPdfName))
            val cmpStamper = PdfStamper(cmpReader, FileOutputStream(outPath + ignoredAreasPrefix + cmpPdfName))

            for ((pageNumber, rectangles) in ignoredAreas) {

                if (rectangles != null && !rectangles.isEmpty()) {
                    val outCB = outStamper.getOverContent(pageNumber)
                    val cmpCB = cmpStamper.getOverContent(pageNumber)

                    for (rect in rectangles) {
                        rect.backgroundColor = BaseColor.BLACK
                        outCB.rectangle(rect)
                        cmpCB.rectangle(rect)
                    }
                }
            }

            outStamper.close()
            cmpStamper.close()

            outReader.close()
            cmpReader.close()

            init(outPath + ignoredAreasPrefix + outPdfName, outPath + ignoredAreasPrefix + cmpPdfName)
        }

        if (targetDir.exists()) {
            var gsParams = this.gsParams.replace("<outputfile>", outPath + cmpImage!!).replace("<inputfile>", cmpPdf)
            var p = runProcess(gsExec, gsParams)
            var bri = BufferedReader(InputStreamReader(p.inputStream))
            var bre = BufferedReader(InputStreamReader(p.errorStream))
            var line: String
            while ((line = bri.readLine()) != null) {
                println(line)
            }
            bri.close()
            while ((line = bre.readLine()) != null) {
                println(line)
            }
            bre.close()
            if (p.waitFor() == 0) {
                gsParams = this.gsParams.replace("<outputfile>", outPath + outImage!!).replace("<inputfile>", outPdf)
                p = runProcess(gsExec, gsParams)
                bri = BufferedReader(InputStreamReader(p.inputStream))
                bre = BufferedReader(InputStreamReader(p.errorStream))
                while ((line = bri.readLine()) != null) {
                    println(line)
                }
                bri.close()
                while ((line = bre.readLine()) != null) {
                    println(line)
                }
                bre.close()
                val exitValue = p.waitFor()

                if (exitValue == 0) {
                    imageFiles = targetDir.listFiles(PngFileFilter())
                    cmpImageFiles = targetDir.listFiles(CmpPngFileFilter())
                    var bUnexpectedNumberOfPages = false
                    if (imageFiles.size != cmpImageFiles.size) {
                        bUnexpectedNumberOfPages = true
                    }
                    val cnt = Math.min(imageFiles.size, cmpImageFiles.size)
                    if (cnt < 1) {
                        return "No files for comparing!!!\nThe result or sample pdf file is not processed by GhostScript."
                    }
                    Arrays.sort(imageFiles, ImageNameComparator())
                    Arrays.sort(cmpImageFiles, ImageNameComparator())
                    var differentPagesFail: String? = null
                    for (i in 0..cnt - 1) {
                        if (equalPages != null && equalPages.contains(i))
                            continue
                        print("Comparing page " + Integer.toString(i + 1) + " (" + imageFiles[i].absolutePath + ")...")
                        val is1 = FileInputStream(imageFiles[i])
                        val is2 = FileInputStream(cmpImageFiles[i])
                        val cmpResult = compareStreams(is1, is2)
                        is1.close()
                        is2.close()
                        if (!cmpResult) {
                            if (compareExec != null && File(compareExec).exists()) {
                                val compareParams = this.compareParams.replace("<image1>", imageFiles[i].absolutePath).replace("<image2>", cmpImageFiles[i].absolutePath).replace("<difference>", outPath + differenceImagePrefix + Integer.toString(i + 1) + ".png")
                                p = runProcess(compareExec, compareParams)
                                bre = BufferedReader(InputStreamReader(p.errorStream))
                                while ((line = bre.readLine()) != null) {
                                    println(line)
                                }
                                bre.close()
                                val cmpExitValue = p.waitFor()
                                if (cmpExitValue == 0) {
                                    if (differentPagesFail == null) {
                                        differentPagesFail = differentPages.replace("<filename>", outPdf).replace("<pagenumber>", Integer.toString(i + 1))
                                        differentPagesFail += "\nPlease, examine " + outPath + differenceImagePrefix + Integer.toString(i + 1) + ".png for more details."
                                    } else {
                                        differentPagesFail = "File $outPdf differs.\nPlease, examine difference images for more details."
                                    }
                                } else {
                                    differentPagesFail = differentPages.replace("<filename>", outPdf).replace("<pagenumber>", Integer.toString(i + 1))
                                }
                            } else {
                                differentPagesFail = differentPages.replace("<filename>", outPdf).replace("<pagenumber>", Integer.toString(i + 1))
                                differentPagesFail += "\nYou can optionally specify path to ImageMagick compare tool (e.g. -DcompareExec=\"C:/Program Files/ImageMagick-6.5.4-2/compare.exe\") to visualize differences."
                                break
                            }
                            println(differentPagesFail)
                        } else {
                            println("done.")
                        }
                    }
                    if (differentPagesFail != null) {
                        return differentPagesFail
                    } else {
                        if (bUnexpectedNumberOfPages)
                            return unexpectedNumberOfPages.replace("<filename>", outPdf)
                    }
                } else {
                    return gsFailed.replace("<filename>", outPdf)
                }
            } else {
                return gsFailed.replace("<filename>", cmpPdf)
            }
        } else {
            return cannotOpenTargetDirectory.replace("<filename>", outPdf)
        }

        return null
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun runProcess(execPath: String, params: String): Process {
        val st = StringTokenizer(params)
        val cmdArray = arrayOfNulls<String>(st.countTokens() + 1)
        cmdArray[0] = execPath
        var i = 1
        while (st.hasMoreTokens()) {
            cmdArray[i] = st.nextToken()
            ++i
        }

        val p = Runtime.getRuntime().exec(cmdArray)

        return p
    }

    @Throws(IOException::class, InterruptedException::class, DocumentException::class)
    @JvmOverloads fun compare(outPdf: String, cmpPdf: String, outPath: String, differenceImagePrefix: String, ignoredAreas: Map<Int, List<Rectangle>>? = null): String {
        init(outPdf, cmpPdf)
        return compare(outPath, differenceImagePrefix, ignoredAreas)
    }

    /**
     * Sets the maximum errors count which will be returned as the result of the comparison.
     * @param compareByContentMaxErrorCount the errors count.
     * *
     * @return Returns this.
     */
    fun setCompareByContentErrorsLimit(compareByContentMaxErrorCount: Int): CompareTool {
        this.compareByContentErrorsLimit = compareByContentMaxErrorCount
        return this
    }

    fun setGenerateCompareByContentXmlReport(generateCompareByContentXmlReport: Boolean) {
        this.generateCompareByContentXmlReport = generateCompareByContentXmlReport
    }

    /**
     * Sets the absolute error parameter which will be used in floating point numbers comparison.
     * @param error the epsilon new value.
     * *
     * @return Returns this.
     */
    fun setFloatAbsoluteError(error: Float): CompareTool {
        this.floatComparisonError = error.toDouble()
        this.absoluteError = true
        return this
    }

    /**
     * Sets the relative error parameter which will be used in floating point numbers comparison.
     * @param error the epsilon new value.
     * *
     * @return Returns this.
     */
    fun setFloatRelativeError(error: Float): CompareTool {
        this.floatComparisonError = error.toDouble()
        this.absoluteError = false
        return this
    }

    @Throws(DocumentException::class, InterruptedException::class, IOException::class)
    protected fun compareByContent(outPath: String, differenceImagePrefix: String, ignoredAreas: Map<Int, List<Rectangle>>): String? {
        print("[itext] INFO  Comparing by content..........")
        val outReader = PdfReader(outPdf)
        outPages = ArrayList<PdfDictionary>()
        outPagesRef = ArrayList<RefKey>()
        loadPagesFromReader(outReader, outPages, outPagesRef)

        val cmpReader = PdfReader(cmpPdf)
        cmpPages = ArrayList<PdfDictionary>()
        cmpPagesRef = ArrayList<RefKey>()
        loadPagesFromReader(cmpReader, cmpPages, cmpPagesRef)

        if (outPages.size != cmpPages.size)
            return compare(outPath, differenceImagePrefix, ignoredAreas)

        val compareResult = CompareResult(compareByContentErrorsLimit)
        val equalPages = ArrayList<Int>(cmpPages.size)
        for (i in cmpPages.indices) {
            val currentPath = ObjectPath(cmpPagesRef[i], outPagesRef[i])
            if (compareDictionariesExtended(outPages[i], cmpPages[i], currentPath, compareResult))
                equalPages.add(i)
        }

        val outStructTree = outReader.catalog.get(PdfName.STRUCTTREEROOT)
        val cmpStructTree = cmpReader.catalog.get(PdfName.STRUCTTREEROOT)
        val outStructTreeRef = if (outStructTree == null) null else RefKey(outStructTree as PdfIndirectReference?)
        val cmpStructTreeRef = if (cmpStructTree == null) null else RefKey(cmpStructTree as PdfIndirectReference?)
        compareObjects(outStructTree, cmpStructTree, ObjectPath(outStructTreeRef, cmpStructTreeRef), compareResult)

        val outOcProperties = outReader.catalog.get(PdfName.OCPROPERTIES)
        val cmpOcProperties = cmpReader.catalog.get(PdfName.OCPROPERTIES)
        val outOcPropertiesRef = if (outOcProperties is PdfIndirectReference) RefKey(outOcProperties) else null
        val cmpOcPropertiesRef = if (cmpOcProperties is PdfIndirectReference) RefKey(cmpOcProperties) else null
        compareObjects(outOcProperties, cmpOcProperties, ObjectPath(outOcPropertiesRef, cmpOcPropertiesRef), compareResult)

        outReader.close()
        cmpReader.close()

        if (generateCompareByContentXmlReport) {
            try {
                compareResult.writeReportToXml(FileOutputStream("$outPath/$xmlReportName.xml"))
            } catch (exc: Exception) {
            }

        }

        if (equalPages.size == cmpPages.size && compareResult.isOk) {
            println("OK")
            System.out.flush()
            return null
        } else {
            println("Fail")
            System.out.flush()
            val compareByContentReport = "Compare by content report:\n" + compareResult.report
            println(compareByContentReport)
            System.out.flush()
            val message = compare(outPath, differenceImagePrefix, ignoredAreas, equalPages)
            if (message == null || message.length == 0)
                return "Compare by content fails. No visual differences"
            return message
        }
    }

    @Throws(DocumentException::class, InterruptedException::class, IOException::class)
    @JvmOverloads fun compareByContent(outPdf: String, cmpPdf: String, outPath: String, differenceImagePrefix: String, ignoredAreas: Map<Int, List<Rectangle>>? = null): String {
        init(outPdf, cmpPdf)
        return compareByContent(outPath, differenceImagePrefix, ignoredAreas)
    }

    private fun loadPagesFromReader(reader: PdfReader, pages: MutableList<PdfDictionary>, pagesRef: MutableList<RefKey>) {
        val pagesDict = reader.catalog.get(PdfName.PAGES)
        addPagesFromDict(pagesDict, pages, pagesRef)
    }

    private fun addPagesFromDict(dictRef: PdfObject, pages: MutableList<PdfDictionary>, pagesRef: MutableList<RefKey>) {
        val dict = PdfReader.getPdfObject(dictRef) as PdfDictionary
        if (dict.isPages) {
            val kids = dict.getAsArray(PdfName.KIDS) ?: return
            for (kid in kids) {
                addPagesFromDict(kid, pages, pagesRef)
            }
        } else if (dict.isPage) {
            pages.add(dict)
            pagesRef.add(RefKey(dictRef as PRIndirectReference))
        }
    }

    @Throws(IOException::class)
    private fun compareObjects(outObj: PdfObject, cmpObj: PdfObject, currentPath: ObjectPath?, compareResult: CompareResult?): Boolean {
        var currentPath = currentPath
        val outDirectObj = PdfReader.getPdfObject(outObj)
        val cmpDirectObj = PdfReader.getPdfObject(cmpObj)

        if (cmpDirectObj == null && outDirectObj == null)
            return true

        if (outDirectObj == null) {
            compareResult!!.addError(currentPath, "Expected object was not found.")
            return false
        } else if (cmpDirectObj == null) {
            compareResult!!.addError(currentPath, "Found object which was not expected to be found.")
            return false
        } else if (cmpDirectObj.type() != outDirectObj.type()) {
            compareResult!!.addError(currentPath, String.format("Types do not match. Expected: %s. Found: %s.", cmpDirectObj.javaClass.simpleName, outDirectObj.javaClass.simpleName))
            return false
        }

        if (cmpObj.isIndirect && outObj.isIndirect) {
            if (currentPath!!.isComparing(RefKey(cmpObj as PdfIndirectReference), RefKey(outObj as PdfIndirectReference)))
                return true
            currentPath = currentPath.resetDirectPath(RefKey(cmpObj), RefKey(outObj))
        }

        if (cmpDirectObj.isDictionary && (cmpDirectObj as PdfDictionary).isPage) {
            if (!outDirectObj.isDictionary || !(outDirectObj as PdfDictionary).isPage) {
                if (compareResult != null && currentPath != null)
                    compareResult.addError(currentPath, "Expected a page. Found not a page.")
                return false
            }
            val cmpRefKey = RefKey(cmpObj as PRIndirectReference)
            val outRefKey = RefKey(outObj as PRIndirectReference)
            // References to the same page
            if (cmpPagesRef.contains(cmpRefKey) && cmpPagesRef.indexOf(cmpRefKey) == outPagesRef.indexOf(outRefKey))
                return true
            if (compareResult != null && currentPath != null)
                compareResult.addError(currentPath, String.format("The dictionaries refer to different pages. Expected page number: %s. Found: %s",
                        cmpPagesRef.indexOf(cmpRefKey), outPagesRef.indexOf(outRefKey)))
            return false
        }

        if (cmpDirectObj.isDictionary) {
            if (!compareDictionariesExtended(outDirectObj as PdfDictionary?, cmpDirectObj as PdfDictionary?, currentPath, compareResult))
                return false
        } else if (cmpDirectObj.isStream) {
            if (!compareStreamsExtended(outDirectObj as PRStream?, cmpDirectObj as PRStream?, currentPath, compareResult))
                return false
        } else if (cmpDirectObj.isArray) {
            if (!compareArraysExtended(outDirectObj as PdfArray?, cmpDirectObj as PdfArray?, currentPath, compareResult))
                return false
        } else if (cmpDirectObj.isName) {
            if (!compareNamesExtended(outDirectObj as PdfName?, cmpDirectObj as PdfName?, currentPath, compareResult))
                return false
        } else if (cmpDirectObj.isNumber) {
            if (!compareNumbersExtended(outDirectObj as PdfNumber?, cmpDirectObj as PdfNumber?, currentPath, compareResult))
                return false
        } else if (cmpDirectObj.isString) {
            if (!compareStringsExtended(outDirectObj as PdfString?, cmpDirectObj as PdfString?, currentPath, compareResult))
                return false
        } else if (cmpDirectObj.isBoolean) {
            if (!compareBooleansExtended(outDirectObj as PdfBoolean?, cmpDirectObj as PdfBoolean?, currentPath, compareResult))
                return false
        } else if (cmpDirectObj is PdfLiteral) {
            if (!compareLiteralsExtended(outDirectObj as PdfLiteral?, cmpDirectObj as PdfLiteral?, currentPath, compareResult))
                return false
        } else if (outDirectObj.isNull && cmpDirectObj.isNull) {
        } else {
            throw UnsupportedOperationException()
        }
        return true
    }

    @Throws(IOException::class)
    fun compareDictionaries(outDict: PdfDictionary, cmpDict: PdfDictionary): Boolean {
        return compareDictionariesExtended(outDict, cmpDict, null, null)
    }

    @Throws(IOException::class)
    private fun compareDictionariesExtended(outDict: PdfDictionary?, cmpDict: PdfDictionary?, currentPath: ObjectPath?, compareResult: CompareResult?): Boolean {
        if (cmpDict != null && outDict == null || outDict != null && cmpDict == null) {
            compareResult!!.addError(currentPath, "One of the dictionaries is null, the other is not.")
            return false
        }
        var dictsAreSame = true
        // Iterate through the union of the keys of the cmp and out dictionaries!
        val mergedKeys = TreeSet(cmpDict!!.keys)
        mergedKeys.addAll(outDict!!.keys)

        for (key in mergedKeys) {
            if (key.compareTo(PdfName.PARENT) == 0 || key.compareTo(PdfName.P) == 0) continue
            if (outDict.isStream && cmpDict.isStream && (key == PdfName.FILTER || key == PdfName.LENGTH)) continue
            if (key.compareTo(PdfName.BASEFONT) == 0 || key.compareTo(PdfName.FONTNAME) == 0) {
                val cmpObj = cmpDict.getDirectObject(key)
                if (cmpObj.isName && cmpObj.toString().indexOf('+') > 0) {
                    val outObj = outDict.getDirectObject(key)
                    if (!outObj.isName || outObj.toString().indexOf('+') == -1) {
                        if (compareResult != null && currentPath != null)
                            compareResult.addError(currentPath, String.format("PdfDictionary %s entry: Expected: %s. Found: %s", key.toString(), cmpObj.toString(), outObj.toString()))
                        dictsAreSame = false
                    }
                    val cmpName = cmpObj.toString().substring(cmpObj.toString().indexOf('+'))
                    val outName = outObj.toString().substring(outObj.toString().indexOf('+'))
                    if (cmpName != outName) {
                        if (compareResult != null && currentPath != null)
                            compareResult.addError(currentPath, String.format("PdfDictionary %s entry: Expected: %s. Found: %s", key.toString(), cmpObj.toString(), outObj.toString()))
                        dictsAreSame = false
                    }
                    continue
                }
            }

            if (floatComparisonError != 0.0 && cmpDict.isPage && outDict.isPage && key == PdfName.CONTENTS) {
                if (!compareContentStreamsByParsingExtended(outDict.getDirectObject(key), cmpDict.getDirectObject(key),
                        outDict.getDirectObject(PdfName.RESOURCES) as PdfDictionary, cmpDict.getDirectObject(PdfName.RESOURCES) as PdfDictionary,
                        currentPath, compareResult)) {
                    dictsAreSame = false
                }
                continue
            }

            currentPath?.pushDictItemToPath(key.toString())
            dictsAreSame = compareObjects(outDict.get(key), cmpDict.get(key), currentPath, compareResult) && dictsAreSame
            currentPath?.pop()
            if (!dictsAreSame && (currentPath == null || compareResult == null || compareResult.isMessageLimitReached))
                return false
        }
        return dictsAreSame
    }

    @Throws(IOException::class)
    fun compareContentStreamsByParsing(outObj: PdfObject, cmpObj: PdfObject): Boolean {
        return compareContentStreamsByParsingExtended(outObj, cmpObj, null, null, null, null)
    }

    @Throws(IOException::class)
    fun compareContentStreamsByParsing(outObj: PdfObject, cmpObj: PdfObject, outResources: PdfDictionary, cmpResources: PdfDictionary): Boolean {
        return compareContentStreamsByParsingExtended(outObj, cmpObj, outResources, cmpResources, null, null)
    }

    @Throws(IOException::class)
    private fun compareContentStreamsByParsingExtended(outObj: PdfObject, cmpObj: PdfObject, outResources: PdfDictionary?, cmpResources: PdfDictionary?, currentPath: ObjectPath?, compareResult: CompareResult?): Boolean {
        var outResources = outResources
        var cmpResources = cmpResources
        if (outObj.type() != outObj.type()) {
            compareResult!!.addError(currentPath, String.format(
                    "PdfObject. Types are different. Expected: %s. Found: %s", cmpObj.type(), outObj.type()))
            return false
        }

        if (outObj.isArray) {
            val outArr = outObj as PdfArray
            val cmpArr = cmpObj as PdfArray
            if (cmpArr.size() != outArr.size()) {
                compareResult!!.addError(currentPath, String.format(
                        "PdfArray. Sizes are different. Expected: %s. Found: %s", cmpArr.size(), outArr.size()))
                return false
            }
            for (i in 0..cmpArr.size() - 1) {
                if (!compareContentStreamsByParsingExtended(outArr.getPdfObject(i), cmpArr.getPdfObject(i), outResources, cmpResources, currentPath, compareResult)) {
                    return false
                }
            }
        }

        val cmpTokeniser = PRTokeniser(RandomAccessFileOrArray(
                RandomAccessSourceFactory().createSource(ContentByteUtils.getContentBytesFromContentObject(cmpObj))))
        val outTokeniser = PRTokeniser(RandomAccessFileOrArray(
                RandomAccessSourceFactory().createSource(ContentByteUtils.getContentBytesFromContentObject(outObj))))

        val cmpPs = PdfContentParser(cmpTokeniser)
        val outPs = PdfContentParser(outTokeniser)

        val cmpOperands = ArrayList<PdfObject>()
        val outOperands = ArrayList<PdfObject>()

        while (cmpPs.parse(cmpOperands).size > 0) {
            outPs.parse(outOperands)
            if (cmpOperands.size != outOperands.size) {
                compareResult!!.addError(currentPath, String.format(
                        "PdfObject. Different commands lengths. Expected: %s. Found: %s", cmpOperands.size, outOperands.size))
                return false
            }
            if (cmpOperands.size == 1 && compareLiterals(cmpOperands[0] as PdfLiteral, PdfLiteral("BI")) && compareLiterals(outOperands[0] as PdfLiteral, PdfLiteral("BI"))) {
                val cmpStr = cmpObj as PRStream
                val outStr = outObj as PRStream
                if (null != outStr.getDirectObject(PdfName.RESOURCES) && null != cmpStr.getDirectObject(PdfName.RESOURCES)) {
                    outResources = outStr.getDirectObject(PdfName.RESOURCES) as PdfDictionary
                    cmpResources = cmpStr.getDirectObject(PdfName.RESOURCES) as PdfDictionary
                }
                if (!compareInlineImagesExtended(outPs, cmpPs, outResources, cmpResources, currentPath, compareResult)) {
                    return false
                }
                continue
            }
            for (i in cmpOperands.indices) {
                if (!compareObjects(outOperands[i], cmpOperands[i], currentPath, compareResult)) {
                    return false
                }
            }
        }
        return true
    }

    @Throws(IOException::class)
    private fun compareInlineImagesExtended(outPs: PdfContentParser, cmpPs: PdfContentParser, outDict: PdfDictionary, cmpDict: PdfDictionary, currentPath: ObjectPath, compareResult: CompareResult): Boolean {
        val cmpInfo = InlineImageUtils.parseInlineImage(cmpPs, cmpDict)
        val outInfo = InlineImageUtils.parseInlineImage(outPs, outDict)
        return compareObjects(outInfo.imageDictionary, cmpInfo.imageDictionary, currentPath, compareResult) && Arrays.equals(outInfo.samples, cmpInfo.samples)
    }

    @Throws(IOException::class)
    fun compareStreams(outStream: PRStream, cmpStream: PRStream): Boolean {
        return compareStreamsExtended(outStream, cmpStream, null, null)
    }

    @Throws(IOException::class)
    private fun compareStreamsExtended(outStream: PRStream, cmpStream: PRStream, currentPath: ObjectPath?, compareResult: CompareResult?): Boolean {
        val decodeStreams = PdfName.FLATEDECODE == outStream.get(PdfName.FILTER)
        var outStreamBytes = PdfReader.getStreamBytesRaw(outStream)
        var cmpStreamBytes = PdfReader.getStreamBytesRaw(cmpStream)
        if (decodeStreams) {
            outStreamBytes = PdfReader.decodeBytes(outStreamBytes, outStream)
            cmpStreamBytes = PdfReader.decodeBytes(cmpStreamBytes, cmpStream)
        }
        if (floatComparisonError != 0.0 &&
                PdfName.XOBJECT == cmpStream.getDirectObject(PdfName.TYPE) &&
                PdfName.XOBJECT == outStream.getDirectObject(PdfName.TYPE) &&
                PdfName.FORM == cmpStream.getDirectObject(PdfName.SUBTYPE) &&
                PdfName.FORM == outStream.getDirectObject(PdfName.SUBTYPE)) {
            return compareContentStreamsByParsingExtended(outStream, cmpStream, outStream.getAsDict(PdfName.RESOURCES), cmpStream.getAsDict(PdfName.RESOURCES), currentPath, compareResult) && compareDictionariesExtended(outStream, cmpStream, currentPath, compareResult)
        } else {
            if (Arrays.equals(outStreamBytes, cmpStreamBytes)) {
                return compareDictionariesExtended(outStream, cmpStream, currentPath, compareResult)
            } else {
                if (cmpStreamBytes.size != outStreamBytes.size) {
                    if (compareResult != null && currentPath != null) {
                        compareResult.addError(currentPath, String.format("PRStream. Lengths are different. Expected: %s. Found: %s", cmpStreamBytes.size, outStreamBytes.size))
                    }
                } else {
                    for (i in cmpStreamBytes.indices) {
                        if (cmpStreamBytes[i] != outStreamBytes[i]) {
                            val l = Math.max(0, i - 10)
                            val r = Math.min(cmpStreamBytes.size, i + 10)
                            if (compareResult != null && currentPath != null) {
                                currentPath.pushOffsetToPath(i)
                                compareResult.addError(currentPath, String.format("PRStream. The bytes differ at index %s. Expected: %s (%s). Found: %s (%s)",
                                        i, String(byteArrayOf(cmpStreamBytes[i])), String(cmpStreamBytes, l, r - l).replace("\\n".toRegex(), "\\\\n"),
                                        String(byteArrayOf(outStreamBytes[i])), String(outStreamBytes, l, r - l).replace("\\n".toRegex(), "\\\\n")))
                                currentPath.pop()
                            }
                        }
                    }
                }
                return false
            }
        }
    }

    @Throws(IOException::class)
    fun compareArrays(outArray: PdfArray, cmpArray: PdfArray): Boolean {
        return compareArraysExtended(outArray, cmpArray, null, null)
    }

    @Throws(IOException::class)
    private fun compareArraysExtended(outArray: PdfArray?, cmpArray: PdfArray, currentPath: ObjectPath?, compareResult: CompareResult?): Boolean {
        if (outArray == null) {
            if (compareResult != null && currentPath != null)
                compareResult.addError(currentPath, "Found null. Expected PdfArray.")
            return false
        } else if (outArray.size() != cmpArray.size()) {
            if (compareResult != null && currentPath != null)
                compareResult.addError(currentPath, String.format("PdfArrays. Lengths are different. Expected: %s. Found: %s.", cmpArray.size(), outArray.size()))
            return false
        }
        var arraysAreEqual = true
        for (i in 0..cmpArray.size() - 1) {
            currentPath?.pushArrayItemToPath(i)
            arraysAreEqual = compareObjects(outArray.getPdfObject(i), cmpArray.getPdfObject(i), currentPath, compareResult) && arraysAreEqual
            currentPath?.pop()
            if (!arraysAreEqual && (currentPath == null || compareResult == null || compareResult.isMessageLimitReached))
                return false
        }

        return arraysAreEqual
    }

    fun compareNames(outName: PdfName, cmpName: PdfName): Boolean {
        return cmpName.compareTo(outName) == 0
    }

    private fun compareNamesExtended(outName: PdfName, cmpName: PdfName, currentPath: ObjectPath?, compareResult: CompareResult?): Boolean {
        if (cmpName.compareTo(outName) == 0) {
            return true
        } else {
            if (compareResult != null && currentPath != null)
                compareResult.addError(currentPath, String.format("PdfName. Expected: %s. Found: %s", cmpName.toString(), outName.toString()))
            return false
        }
    }

    fun compareNumbers(outNumber: PdfNumber, cmpNumber: PdfNumber): Boolean {
        var difference = Math.abs(outNumber.doubleValue() - cmpNumber.doubleValue())
        if (!absoluteError && cmpNumber.doubleValue() != 0.0) {
            difference /= cmpNumber.doubleValue()
        }
        return difference <= floatComparisonError
    }

    private fun compareNumbersExtended(outNumber: PdfNumber, cmpNumber: PdfNumber, currentPath: ObjectPath?, compareResult: CompareResult?): Boolean {
        if (compareNumbers(outNumber, cmpNumber)) {
            return true
        } else {
            if (compareResult != null && currentPath != null)
                compareResult.addError(currentPath, String.format("PdfNumber. Expected: %s. Found: %s", cmpNumber, outNumber))
            return false
        }
    }

    fun compareStrings(outString: PdfString, cmpString: PdfString): Boolean {
        return Arrays.equals(cmpString.bytes, outString.bytes)
    }

    private fun compareStringsExtended(outString: PdfString, cmpString: PdfString, currentPath: ObjectPath?, compareResult: CompareResult?): Boolean {
        if (Arrays.equals(cmpString.bytes, outString.bytes)) {
            return true
        } else {
            val cmpStr = cmpString.toUnicodeString()
            val outStr = outString.toUnicodeString()
            if (cmpStr.length != outStr.length) {
                if (compareResult != null && currentPath != null)
                    compareResult.addError(currentPath, String.format("PdfString. Lengths are different. Expected: %s. Found: %s", cmpStr.length, outStr.length))
            } else {
                for (i in 0..cmpStr.length - 1) {
                    if (cmpStr[i] != outStr[i]) {
                        val l = Math.max(0, i - 10)
                        val r = Math.min(cmpStr.length, i + 10)
                        if (compareResult != null && currentPath != null) {
                            currentPath.pushOffsetToPath(i)
                            compareResult.addError(currentPath, String.format("PdfString. Characters differ at position %s. Expected: %s (%s). Found: %s (%s).",
                                    i, Character.toString(cmpStr[i]), cmpStr.substring(l, r).replace("\n", "\\n"),
                                    Character.toString(outStr[i]), outStr.substring(l, r).replace("\n", "\\n")))
                            currentPath.pop()
                        }
                        break
                    }
                }
            }
            return false
        }
    }

    fun compareLiterals(outLiteral: PdfLiteral, cmpLiteral: PdfLiteral): Boolean {
        return Arrays.equals(cmpLiteral.bytes, outLiteral.bytes)
    }

    private fun compareLiteralsExtended(outLiteral: PdfLiteral, cmpLiteral: PdfLiteral, currentPath: ObjectPath?, compareResult: CompareResult?): Boolean {
        if (compareLiterals(outLiteral, cmpLiteral)) {
            return true
        } else {
            if (compareResult != null && currentPath != null)
                compareResult.addError(currentPath, String.format(
                        "PdfLiteral. Expected: %s. Found: %s", cmpLiteral, outLiteral))
            return false
        }
    }

    fun compareBooleans(outBoolean: PdfBoolean, cmpBoolean: PdfBoolean): Boolean {
        return Arrays.equals(cmpBoolean.bytes, outBoolean.bytes)
    }

    private fun compareBooleansExtended(outBoolean: PdfBoolean, cmpBoolean: PdfBoolean, currentPath: ObjectPath?, compareResult: CompareResult?): Boolean {
        if (cmpBoolean.booleanValue() == outBoolean.booleanValue()) {
            return true
        } else {
            if (compareResult != null && currentPath != null)
                compareResult.addError(currentPath, String.format("PdfBoolean. Expected: %s. Found: %s.", cmpBoolean.booleanValue(), outBoolean.booleanValue()))
            return false
        }
    }

    @JvmOverloads fun compareXmp(xmp1: ByteArray, xmp2: ByteArray, ignoreDateAndProducerProperties: Boolean = false): String? {
        var xmp1 = xmp1
        var xmp2 = xmp2
        try {
            if (ignoreDateAndProducerProperties) {
                var xmpMeta = XMPMetaFactory.parseFromBuffer(xmp1)

                XMPUtils.removeProperties(xmpMeta, XMPConst.NS_XMP, XmpBasicProperties.CREATEDATE, true, true)
                XMPUtils.removeProperties(xmpMeta, XMPConst.NS_XMP, XmpBasicProperties.MODIFYDATE, true, true)
                XMPUtils.removeProperties(xmpMeta, XMPConst.NS_XMP, XmpBasicProperties.METADATADATE, true, true)
                XMPUtils.removeProperties(xmpMeta, XMPConst.NS_PDF, PdfProperties.PRODUCER, true, true)

                xmp1 = XMPMetaFactory.serializeToBuffer(xmpMeta, SerializeOptions(SerializeOptions.SORT))

                xmpMeta = XMPMetaFactory.parseFromBuffer(xmp2)
                XMPUtils.removeProperties(xmpMeta, XMPConst.NS_XMP, XmpBasicProperties.CREATEDATE, true, true)
                XMPUtils.removeProperties(xmpMeta, XMPConst.NS_XMP, XmpBasicProperties.MODIFYDATE, true, true)
                XMPUtils.removeProperties(xmpMeta, XMPConst.NS_XMP, XmpBasicProperties.METADATADATE, true, true)
                XMPUtils.removeProperties(xmpMeta, XMPConst.NS_PDF, PdfProperties.PRODUCER, true, true)

                xmp2 = XMPMetaFactory.serializeToBuffer(xmpMeta, SerializeOptions(SerializeOptions.SORT))
            }

            if (!compareXmls(xmp1, xmp2)) {
                return "The XMP packages different!"
            }
        } catch (xmpExc: XMPException) {
            return "XMP parsing failure!"
        } catch (ioExc: IOException) {
            return "XMP parsing failure!"
        } catch (parseExc: ParserConfigurationException) {
            return "XMP parsing failure!"
        } catch (parseExc: SAXException) {
            return "XMP parsing failure!"
        }

        return null
    }

    @JvmOverloads fun compareXmp(outPdf: String, cmpPdf: String, ignoreDateAndProducerProperties: Boolean = false): String {
        init(outPdf, cmpPdf)
        var cmpReader: PdfReader? = null
        var outReader: PdfReader? = null
        try {
            cmpReader = PdfReader(this.cmpPdf)
            outReader = PdfReader(this.outPdf)
            val cmpBytes = cmpReader.metadata
            val outBytes = outReader.metadata
            return compareXmp(cmpBytes, outBytes, ignoreDateAndProducerProperties)
        } catch (e: IOException) {
            return "XMP parsing failure!"
        } finally {
            if (cmpReader != null)
                cmpReader.close()
            if (outReader != null)
                outReader.close()
        }
    }

    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
    fun compareXmls(xml1: ByteArray, xml2: ByteArray): Boolean {
        val dbf = DocumentBuilderFactory.newInstance()
        dbf.isNamespaceAware = true
        dbf.isCoalescing = true
        dbf.isIgnoringElementContentWhitespace = true
        dbf.isIgnoringComments = true
        val db = dbf.newDocumentBuilder()

        val doc1 = db.parse(ByteArrayInputStream(xml1))
        doc1.normalizeDocument()

        val doc2 = db.parse(ByteArrayInputStream(xml2))
        doc2.normalizeDocument()

        return doc2.isEqualNode(doc1)
    }

    @Throws(IOException::class)
    fun compareDocumentInfo(outPdf: String, cmpPdf: String): String {
        print("[itext] INFO  Comparing document info.......")
        var message: String? = null
        val outReader = PdfReader(outPdf)
        val cmpReader = PdfReader(cmpPdf)
        val cmpInfo = convertInfo(cmpReader.info)
        val outInfo = convertInfo(outReader.info)
        for (i in cmpInfo.indices) {
            if (cmpInfo[i] != outInfo[i]) {
                message = "Document info fail"
                break
            }
        }
        outReader.close()
        cmpReader.close()

        if (message == null)
            println("OK")
        else
            println("Fail")
        System.out.flush()
        return message
    }

    private fun linksAreSame(cmpLink: PdfAnnotation.PdfImportedLink, outLink: PdfAnnotation.PdfImportedLink): Boolean {
        // Compare link boxes, page numbers the links refer to, and simple parameters (non-indirect, non-arrays, non-dictionaries)

        if (cmpLink.destinationPage != outLink.destinationPage)
            return false
        if (cmpLink.rect.toString() != outLink.rect.toString())
            return false

        val cmpParams = cmpLink.parameters
        val outParams = outLink.parameters
        if (cmpParams.size != outParams.size)
            return false

        for (cmpEntry in cmpParams.entries) {
            val cmpObj = cmpEntry.value
            if (!outParams.containsKey(cmpEntry.key))
                return false
            val outObj = outParams[cmpEntry.key]
            if (cmpObj.type() != outObj.type())
                return false

            when (cmpObj.type()) {
                PdfObject.NULL, PdfObject.BOOLEAN, PdfObject.NUMBER, PdfObject.STRING, PdfObject.NAME -> if (cmpObj.toString() != outObj.toString())
                    return false
            }
        }

        return true
    }

    @Throws(IOException::class)
    fun compareLinks(outPdf: String, cmpPdf: String): String {
        print("[itext] INFO  Comparing link annotations....")
        var message: String? = null
        val outReader = PdfReader(outPdf)
        val cmpReader = PdfReader(cmpPdf)
        var i = 0
        while (i < outReader.numberOfPages && i < cmpReader.numberOfPages) {
            val outLinks = outReader.getLinks(i + 1)
            val cmpLinks = cmpReader.getLinks(i + 1)
            if (cmpLinks.size != outLinks.size) {
                message = String.format("Different number of links on page %d.", i + 1)
                break
            }
            for (j in cmpLinks.indices) {
                if (!linksAreSame(cmpLinks[j], outLinks[j])) {
                    message = String.format("Different links on page %d.\n%s\n%s", i + 1, cmpLinks[j].toString(), outLinks[j].toString())
                    break
                }
            }
            i++
        }
        outReader.close()
        cmpReader.close()
        if (message == null)
            println("OK")
        else
            println("Fail")
        System.out.flush()
        return message
    }

    @Throws(IOException::class, ParserConfigurationException::class, SAXException::class)
    fun compareTagStructures(outPdf: String, cmpPdf: String): String {
        print("[itext] INFO  Comparing tag structures......")

        val outXml = outPdf.replace(".pdf", ".xml")
        val cmpXml = outPdf.replace(".pdf", ".cmp.xml")

        var message: String? = null
        var reader = PdfReader(outPdf)
        val xmlOut1 = FileOutputStream(outXml)
        CmpTaggedPdfReaderTool().convertToXml(reader, xmlOut1)
        reader.close()
        reader = PdfReader(cmpPdf)
        val xmlOut2 = FileOutputStream(cmpXml)
        CmpTaggedPdfReaderTool().convertToXml(reader, xmlOut2)
        reader.close()
        if (!compareXmls(outXml, cmpXml)) {
            message = "The tag structures are different."
        }
        xmlOut1.close()
        xmlOut2.close()
        if (message == null)
            println("OK")
        else
            println("Fail")
        System.out.flush()
        return message
    }

    private fun convertInfo(info: HashMap<String, String>): Array<String> {
        val convertedInfo = arrayOf("", "", "", "")
        for (entry in info.entries) {
            if (Meta.TITLE.equals(entry.key, ignoreCase = true)) {
                convertedInfo[0] = entry.value
            } else if (Meta.AUTHOR.equals(entry.key, ignoreCase = true)) {
                convertedInfo[1] = entry.value
            } else if (Meta.SUBJECT.equals(entry.key, ignoreCase = true)) {
                convertedInfo[2] = entry.value
            } else if (Meta.KEYWORDS.equals(entry.key, ignoreCase = true)) {
                convertedInfo[3] = entry.value
            }
        }
        return convertedInfo
    }

    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
    fun compareXmls(xml1: String, xml2: String): Boolean {
        val dbf = DocumentBuilderFactory.newInstance()
        dbf.isNamespaceAware = true
        dbf.isCoalescing = true
        dbf.isIgnoringElementContentWhitespace = true
        dbf.isIgnoringComments = true
        val db = dbf.newDocumentBuilder()

        val doc1 = db.parse(File(xml1))
        doc1.normalizeDocument()

        val doc2 = db.parse(File(xml2))
        doc2.normalizeDocument()

        return doc2.isEqualNode(doc1)
    }

    private fun init(outPdf: String, cmpPdf: String) {
        this.outPdf = outPdf
        this.cmpPdf = cmpPdf
        outPdfName = File(outPdf).name
        cmpPdfName = File(cmpPdf).name
        outImage = outPdfName!! + "-%03d.png"
        if (cmpPdfName!!.startsWith("cmp_"))
            cmpImage = cmpPdfName!! + "-%03d.png"
        else
            cmpImage = "cmp_$cmpPdfName-%03d.png"
    }

    @Throws(IOException::class)
    private fun compareStreams(is1: InputStream, is2: InputStream): Boolean {
        val buffer1 = ByteArray(64 * 1024)
        val buffer2 = ByteArray(64 * 1024)
        var len1: Int
        var len2: Int
        while (true) {
            len1 = is1.read(buffer1)
            len2 = is2.read(buffer2)
            if (len1 != len2)
                return false
            if (!Arrays.equals(buffer1, buffer2))
                return false
            if (len1 == -1)
                break
        }
        return true
    }

    internal inner class PngFileFilter : FileFilter {

        override fun accept(pathname: File): Boolean {
            val ap = pathname.absolutePath
            val b1 = ap.endsWith(".png")
            val b2 = ap.contains("cmp_")
            return b1 && !b2 && ap.contains(outPdfName)
        }
    }

    internal inner class CmpPngFileFilter : FileFilter {
        override fun accept(pathname: File): Boolean {
            val ap = pathname.absolutePath
            val b1 = ap.endsWith(".png")
            val b2 = ap.contains("cmp_")
            return b1 && b2 && ap.contains(cmpPdfName)
        }
    }

    internal inner class ImageNameComparator : Comparator<File> {
        override fun compare(f1: File, f2: File): Int {
            val f1Name = f1.absolutePath
            val f2Name = f2.absolutePath
            return f1Name.compareTo(f2Name)
        }
    }

    internal inner class CmpTaggedPdfReaderTool : TaggedPdfReaderTool() {

        var parsedTags: MutableMap<PdfDictionary, Map<Int, String>> = HashMap()

        @Throws(IOException::class)
        override fun parseTag(tag: String, `object`: PdfObject, page: PdfDictionary) {
            if (`object` is PdfNumber) {

                if (!parsedTags.containsKey(page)) {
                    val listener = CmpMarkedContentRenderFilter()

                    val processor = PdfContentStreamProcessor(
                            listener)
                    processor.processContent(PdfReader.getPageContent(page), page.getAsDict(PdfName.RESOURCES))

                    parsedTags.put(page, listener.parsedTagContent)
                }

                var tagContent = ""
                if (parsedTags[page].containsKey(`object`.intValue()))
                    tagContent = parsedTags[page].get(`object`.intValue())

                out.print(XMLUtil.escapeXML(tagContent, true))

            } else {
                super.parseTag(tag, `object`, page)
            }
        }

        @Throws(IOException::class)
        override fun inspectChildDictionary(k: PdfDictionary) {
            inspectChildDictionary(k, true)
        }
    }

    internal inner class CmpMarkedContentRenderFilter : RenderListener {

        var tagsByMcid: MutableMap<Int, TextExtractionStrategy> = HashMap()

        val parsedTagContent: Map<Int, String>
            get() {
                val content = HashMap<Int, String>()
                for (id in tagsByMcid.keys) {
                    content.put(id, tagsByMcid[id].getResultantText())
                }
                return content
            }

        override fun beginTextBlock() {
            for (id in tagsByMcid.keys) {
                tagsByMcid[id].beginTextBlock()
            }
        }

        override fun renderText(renderInfo: TextRenderInfo) {
            val mcid = renderInfo.mcid
            if (mcid != null && tagsByMcid.containsKey(mcid)) {
                tagsByMcid[mcid].renderText(renderInfo)
            } else if (mcid != null) {
                tagsByMcid.put(mcid, SimpleTextExtractionStrategy())
                tagsByMcid[mcid].renderText(renderInfo)
            }
        }

        override fun endTextBlock() {
            for (id in tagsByMcid.keys) {
                tagsByMcid[id].endTextBlock()
            }
        }

        override fun renderImage(renderInfo: ImageRenderInfo) {
        }


    }

    companion object {

        private val cannotOpenTargetDirectory = "Cannot open target directory for <filename>."
        private val gsFailed = "GhostScript failed for <filename>."
        private val unexpectedNumberOfPages = "Unexpected number of pages for <filename>."
        private val differentPages = "File <filename> differs on page <pagenumber>."
        private val undefinedGsPath = "Path to GhostScript is not specified. Please use -DgsExec=<path_to_ghostscript> (e.g. -DgsExec=\"C:/Program Files/gs/gs9.14/bin/gswin32c.exe\")"

        private val ignoredAreasPrefix = "ignored_areas_"
    }


}
