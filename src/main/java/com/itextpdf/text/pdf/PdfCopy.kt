/*
 * $Id: 51dc6461dda35cb95248fe4ff8446e433d202e12 $
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

import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Rectangle
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.exceptions.BadPasswordException
import com.itextpdf.text.log.Counter
import com.itextpdf.text.log.CounterFactory
import com.itextpdf.text.log.Logger
import com.itextpdf.text.log.LoggerFactory

import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashMap
import java.util.StringTokenizer

/**
 * Make copies of PDF documents. Documents can be edited after reading and
 * before writing them out.
 * @author Mark Thompson
 */

open class PdfCopy
/**
 * Constructor
 * @param document document
 * *
 * @param os outputstream
 */
@Throws(DocumentException::class)
constructor(document: Document, os: OutputStream) : PdfWriter(PdfDocument(), os) {

    /**
     * This class holds information about indirect references, since they are
     * renumbered by iText.
     */
    internal class IndirectReferences(var ref: PdfIndirectReference) {
        var copied: Boolean = false

        init {
            copied = false
        }

        fun setCopied() {
            copied = true
        }

        fun setNotCopied() {
            copied = false
        }

        override fun toString(): String {
            var ext = ""
            if (copied) ext += " Copied"
            return ref + ext
        }
    }

    protected override val counter: Counter
        get() = COUNTER
    protected var indirects: HashMap<RefKey, IndirectReferences>? = null
    protected var indirectMap: HashMap<PdfReader, HashMap<RefKey, IndirectReferences>>
    protected var parentObjects: HashMap<PdfObject, PdfObject>
    protected var disableIndirects: HashSet<PdfObject>
    protected var reader: PdfReader
    protected var namePtr = intArrayOf(0)
    /** Holds value of property rotateContents.  */
    /** Getter for property rotateContents.
     * @return Value of property rotateContents.
     */
    /** Setter for property rotateContents.
     * @param rotateContents New value of property rotateContents.
     */
    var isRotateContents = true
    protected var fieldArray: PdfArray? = null
    protected var fieldTemplates: HashSet<PdfTemplate>? = null
    private var structTreeController: PdfStructTreeController? = null
    private var currentStructArrayNumber = 0
    //remember to avoid coping
    protected var structTreeRootReference: PRIndirectReference? = null
    //to remove unused objects
    protected var indirectObjects: LinkedHashMap<RefKey, PdfIndirectObject>? = null
    //PdfIndirectObjects, that generate PdfWriter.addToBody(PdfObject) method, already saved to PdfBody
    protected var savedObjects: ArrayList<PdfIndirectObject>
    //imported pages from getImportedPage(PdfReader, int, boolean)
    protected var importedPages: ArrayList<ImportedPage>
    //for correct update of kids in StructTreeRootController
    protected var updateRootKids = false

    protected var mergeFields = false
    private var needAppearances = false
    private var hasSignature: Boolean = false
    private var acroForm: PdfIndirectReference? = null
    private var tabOrder: HashMap<PdfArray, ArrayList<Int>>? = null
    private var calculationOrderRefs: ArrayList<Any>? = null
    private var resources: PdfDictionary? = null
    protected var fields: ArrayList<AcroFields>
    private var calculationOrder: ArrayList<String>? = null
    private var fieldTree: HashMap<String, Any>? = null
    private var unmergedMap: HashMap<Int, PdfIndirectObject>? = null
    private var unmergedIndirectRefsMap: HashMap<RefKey, PdfIndirectObject>? = null
    private var mergedMap: HashMap<Int, PdfIndirectObject>? = null
    private var mergedSet: HashSet<PdfIndirectObject>? = null
    private var mergeFieldsInternalCall = false
    private val mergedRadioButtons = HashSet<Any>()
    private val mergedTextFields = HashMap<Any, PdfString>()

    private val readersWithImportedStructureTreeRootKids = HashSet<PdfReader>()

    protected class ImportedPage internal constructor(internal var reader: PdfReader, internal var pageNumber: Int, keepFields: Boolean) {
        internal var mergedFields: PdfArray
        internal var annotsIndirectReference: PdfIndirectReference

        init {
            if (keepFields) {
                mergedFields = PdfArray()
            }
        }

        override fun equals(o: Any?): Boolean {
            if (o !is ImportedPage) return false
            return this.pageNumber == o.pageNumber && this.reader == o.reader
        }

        override fun toString(): String {
            return Integer.toString(pageNumber)
        }
    }

    init {
        document.addDocListener(pdfDocument)
        pdfDocument.addWriter(this)
        indirectMap = HashMap<PdfReader, HashMap<RefKey, IndirectReferences>>()
        parentObjects = HashMap<PdfObject, PdfObject>()
        disableIndirects = HashSet<PdfObject>()

        indirectObjects = LinkedHashMap<RefKey, PdfIndirectObject>()
        savedObjects = ArrayList<PdfIndirectObject>()
        importedPages = ArrayList<ImportedPage>()
    }

    /**
     * Setting page events isn't possible with Pdf(Smart)Copy.
     * Use the PageStamp class if you want to add content to copied pages.
     * @see com.itextpdf.text.pdf.PdfWriter.setPageEvent
     */
    override var pageEvent: PdfPageEvent?
        get() = super.pageEvent
        set(event) = throw UnsupportedOperationException()

    fun setMergeFields() {
        this.mergeFields = true
        resources = PdfDictionary()
        fields = ArrayList<AcroFields>()
        calculationOrder = ArrayList<String>()
        fieldTree = LinkedHashMap<String, Any>()
        unmergedMap = HashMap<Int, PdfIndirectObject>()
        unmergedIndirectRefsMap = HashMap<RefKey, PdfIndirectObject>()
        mergedMap = HashMap<Int, PdfIndirectObject>()
        mergedSet = HashSet<PdfIndirectObject>()
    }

    /**
     * Grabs a page from the input document
     * @param reader the reader of the document
     * *
     * @param pageNumber which page to get
     * *
     * @return the page
     */
    override fun getImportedPage(reader: PdfReader, pageNumber: Int): PdfImportedPage {
        if (mergeFields && !mergeFieldsInternalCall) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("1.method.cannot.be.used.in.mergeFields.mode.please.use.addDocument", "getImportedPage"))
        }
        if (mergeFields) {
            val newPage = ImportedPage(reader, pageNumber, mergeFields)
            importedPages.add(newPage)
        }
        if (structTreeController != null)
            structTreeController!!.reader = null
        disableIndirects.clear()
        parentObjects.clear()
        return getImportedPageImpl(reader, pageNumber)
    }

    @Throws(BadPdfFormatException::class)
    fun getImportedPage(reader: PdfReader, pageNumber: Int, keepTaggedPdfStructure: Boolean): PdfImportedPage {
        if (mergeFields && !mergeFieldsInternalCall) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("1.method.cannot.be.used.in.mergeFields.mode.please.use.addDocument", "getImportedPage"))
        }
        updateRootKids = false
        if (!keepTaggedPdfStructure) {
            if (mergeFields) {
                val newPage = ImportedPage(reader, pageNumber, mergeFields)
                importedPages.add(newPage)
            }
            return getImportedPageImpl(reader, pageNumber)
        }
        if (structTreeController != null) {
            if (reader !== structTreeController!!.reader)
                structTreeController!!.setReader(reader)
        } else {
            structTreeController = PdfStructTreeController(reader, this)
        }
        val newPage = ImportedPage(reader, pageNumber, mergeFields)
        when (checkStructureTreeRootKids(newPage)) {
            -1 //-1 - clear , update
            -> {
                clearIndirects(reader)
                updateRootKids = true
            }
            0 //0 - not clear, not update
            -> updateRootKids = false
            1 //1 - not clear, update
            -> updateRootKids = true
        }
        importedPages.add(newPage)

        disableIndirects.clear()
        parentObjects.clear()
        return getImportedPageImpl(reader, pageNumber)
    }

    private fun clearIndirects(reader: PdfReader) {
        val currIndirects = indirectMap[reader]
        val forDelete = ArrayList<RefKey>()
        for (entry in currIndirects.entries) {
            val iRef = entry.value.ref
            val key = RefKey(iRef)
            val iobj = indirectObjects!![key]
            if (iobj == null) {
                forDelete.add(entry.key)
            } else if (iobj.`object`!!.isArray || iobj.`object`!!.isDictionary || iobj.`object`!!.isStream) {
                forDelete.add(entry.key)
            }
        }

        for (key in forDelete)
            currIndirects.remove(key)
    }

    //0 - not clear, not update
    //-1 - clear , update
    //1 - not clear, update
    private fun checkStructureTreeRootKids(newPage: ImportedPage): Int {
        //start of document;
        if (importedPages.size == 0) return 1
        var readerExist = false
        for (page in importedPages) {
            if (page.reader == newPage.reader) {
                readerExist = true
                break
            }
        }

        //add new reader;
        if (!readerExist) return 1

        val lastPage = importedPages[importedPages.size - 1]
        val equalReader = lastPage.reader == newPage.reader
        //reader exist, correct order;
        if (equalReader && newPage.pageNumber > lastPage.pageNumber) {
            if (readersWithImportedStructureTreeRootKids.contains(newPage.reader))
                return 0
            else
                return 1
        }
        //reader exist, incorrect order;
        return -1
    }

    protected fun structureTreeRootKidsForReaderImported(reader: PdfReader) {
        readersWithImportedStructureTreeRootKids.add(reader)
    }

    protected fun fixStructureTreeRoot(activeKeys: HashSet<RefKey>, activeClassMaps: HashSet<PdfName>) {
        val newClassMap = HashMap<PdfName, PdfObject>(activeClassMaps.size)
        for (key in activeClassMaps) {
            val cm = structureTreeRoot!!.classes!![key]
            if (cm != null) newClassMap.put(key, cm)
        }

        structureTreeRoot!!.classes = newClassMap

        val kids = structureTreeRoot!!.getAsArray(PdfName.K)
        if (kids != null) {
            var i = 0
            while (i < kids.size()) {
                val iref = kids.getPdfObject(i) as PdfIndirectReference
                val key = RefKey(iref)
                if (!activeKeys.contains(key)) kids.remove(i--)
                ++i
            }
        }
    }

    protected fun getImportedPageImpl(reader: PdfReader, pageNumber: Int): PdfImportedPage {
        if (currentPdfReaderInstance != null) {
            if (currentPdfReaderInstance!!.reader !== reader) {

                //  TODO: Removed - the user should be responsible for closing all PdfReaders.  But, this could cause a lot of memory leaks in code out there that hasn't been properly closing things - maybe add a finalizer to PdfReader that calls PdfReader#close() ??
                //             	  try {
                //                    currentPdfReaderInstance.getReader().close();
                //                    currentPdfReaderInstance.getReaderFile().close();
                //                }
                //                catch (IOException ioe) {
                //                    // empty on purpose
                //                }
                currentPdfReaderInstance = super.getPdfReaderInstance(reader)
            }
        } else {
            currentPdfReaderInstance = super.getPdfReaderInstance(reader)
        }

        //currentPdfReaderInstance.setOutputToPdf(false);
        return currentPdfReaderInstance!!.getImportedPage(pageNumber)
    }

    /**
     * Translate a PRIndirectReference to a PdfIndirectReference
     * In addition, translates the object numbers, and copies the
     * referenced object to the output file.
     * NB: PRIndirectReferences (and PRIndirectObjects) really need to know what
     * file they came from, because each file has its own namespace. The translation
     * we do from their namespace to ours is *at best* heuristic, and guaranteed to
     * fail under some circumstances.
     */
    @Throws(IOException::class, BadPdfFormatException::class)
    protected fun copyIndirect(`in`: PRIndirectReference, keepStructure: Boolean, directRootKids: Boolean): PdfIndirectReference? {
        val theRef: PdfIndirectReference
        val key = RefKey(`in`)
        var iRef: IndirectReferences? = indirects!![key]
        val obj = PdfReader.getPdfObjectRelease(`in`)
        if (keepStructure && directRootKids)
            if (obj is PdfDictionary) {
                if (obj.contains(PdfName.PG))
                    return null
            }

        if (iRef != null) {
            theRef = iRef.ref
            if (iRef.copied) {
                return theRef
            }
        } else {
            theRef = body.pdfIndirectReference
            iRef = IndirectReferences(theRef)
            indirects!!.put(key, iRef)
        }

        if (obj != null && obj.isDictionary) {
            val type = PdfReader.getPdfObjectRelease((obj as PdfDictionary).get(PdfName.TYPE))
            if (type != null) {
                if (PdfName.PAGE == type) {
                    return theRef
                }
                if (PdfName.CATALOG == type) {
                    LOGGER.warn(MessageLocalization.getComposedMessage("make.copy.of.catalog.dictionary.is.forbidden"))
                    return null
                }
            }
        }
        iRef.setCopied()
        if (obj != null) parentObjects.put(obj, `in`)
        val res = copyObject(obj, keepStructure, directRootKids)
        if (disableIndirects.contains(obj))
            iRef.setNotCopied()
        if (res != null) {
            addToBody(res, theRef)
            return theRef
        } else {
            indirects!!.remove(key)
            return null
        }

    }

    /**
     * Translate a PRIndirectReference to a PdfIndirectReference
     * In addition, translates the object numbers, and copies the
     * referenced object to the output file.
     * NB: PRIndirectReferences (and PRIndirectObjects) really need to know what
     * file they came from, because each file has its own namespace. The translation
     * we do from their namespace to ours is *at best* heuristic, and guaranteed to
     * fail under some circumstances.
     */
    @Throws(IOException::class, BadPdfFormatException::class)
    protected open fun copyIndirect(`in`: PRIndirectReference): PdfIndirectReference {
        return copyIndirect(`in`, false, false)
    }

    /**
     * Translate a PRDictionary to a PdfDictionary. Also translate all of the
     * objects contained in it.
     */
    @Throws(IOException::class, BadPdfFormatException::class)
    @JvmOverloads protected fun copyDictionary(`in`: PdfDictionary, keepStruct: Boolean = false, directRootKids: Boolean = false): PdfDictionary? {
        val out = PdfDictionary(`in`.size())
        val type = PdfReader.getPdfObjectRelease(`in`.get(PdfName.TYPE))

        if (keepStruct) {
            if (directRootKids && `in`.contains(PdfName.PG)) {
                var curr: PdfObject = `in`
                disableIndirects.add(curr)
                while (parentObjects.containsKey(curr) && !disableIndirects.contains(curr)) {
                    curr = parentObjects[curr]
                    disableIndirects.add(curr)
                }
                return null
            }

            val structType = `in`.getAsName(PdfName.S)
            structTreeController!!.addRole(structType)
            structTreeController!!.addClass(`in`)
        }
        if (structTreeController != null && structTreeController!!.reader != null && (`in`.contains(PdfName.STRUCTPARENTS) || `in`.contains(PdfName.STRUCTPARENT))) {
            var key = PdfName.STRUCTPARENT
            if (`in`.contains(PdfName.STRUCTPARENTS)) {
                key = PdfName.STRUCTPARENTS
            }
            val value = `in`.get(key)
            out.put(key, PdfNumber(currentStructArrayNumber))
            structTreeController!!.copyStructTreeForPage(value as PdfNumber, currentStructArrayNumber++)
        }
        for (element in `in`.keys) {
            val value = `in`.get(element)
            if (structTreeController != null && structTreeController!!.reader != null && (element == PdfName.STRUCTPARENTS || element == PdfName.STRUCTPARENT)) {
                continue
            }
            if (PdfName.PAGE == type) {
                if (element != PdfName.B && element != PdfName.PARENT) {
                    parentObjects.put(value, `in`)
                    val res = copyObject(value, keepStruct, directRootKids)
                    if (res != null)
                        out.put(element, res)
                }
            } else {
                val res: PdfObject?
                if (isTagged && value.isIndirect && isStructTreeRootReference(value as PRIndirectReference)) {
                    res = structureTreeRoot!!.reference
                } else {
                    res = copyObject(value, keepStruct, directRootKids)
                }
                if (res != null)
                    out.put(element, res)
            }
        }

        return out
    }

    /**
     * Translate a PRStream to a PdfStream. The data part copies itself.
     */
    @Throws(IOException::class, BadPdfFormatException::class)
    protected fun copyStream(`in`: PRStream): PdfStream {
        val out = PRStream(`in`, null)

        for (element in `in`.keys) {
            val value = `in`.get(element)
            parentObjects.put(value, `in`)
            val res = copyObject(value)
            if (res != null)
                out.put(element, res)
        }

        return out
    }

    /**
     * Translate a PRArray to a PdfArray. Also translate all of the objects contained
     * in it
     */
    @Throws(IOException::class, BadPdfFormatException::class)
    @JvmOverloads protected fun copyArray(`in`: PdfArray, keepStruct: Boolean = false, directRootKids: Boolean = false): PdfArray {
        val out = PdfArray(`in`.size())

        val i = `in`.listIterator()
        while (i.hasNext()) {
            val value = i.next()
            parentObjects.put(value, `in`)
            val res = copyObject(value, keepStruct, directRootKids)
            if (res != null)
                out.add(res)
        }
        return out
    }

    /**
     * Translate a PR-object to a Pdf-object
     */
    @Throws(IOException::class, BadPdfFormatException::class)
    @JvmOverloads protected fun copyObject(`in`: PdfObject?, keepStruct: Boolean = false, directRootKids: Boolean = false): PdfObject? {
        if (`in` == null)
            return PdfNull.PDFNULL
        when (`in`.type) {
            PdfObject.DICTIONARY -> return copyDictionary(`in` as PdfDictionary?, keepStruct, directRootKids)
            PdfObject.INDIRECT -> {
                if (!keepStruct && !directRootKids)
                // fix for PdfSmartCopy
                    return copyIndirect(`in` as PRIndirectReference?)
                else
                    return copyIndirect(`in` as PRIndirectReference?, keepStruct, directRootKids)
                return copyArray(`in` as PdfArray?, keepStruct, directRootKids)
            }
            PdfObject.ARRAY -> return copyArray(`in` as PdfArray?, keepStruct, directRootKids)
            PdfObject.NUMBER, PdfObject.NAME, PdfObject.STRING, PdfObject.NULL, PdfObject.BOOLEAN, 0//PdfIndirectReference
            -> return `in`
            PdfObject.STREAM -> return copyStream(`in` as PRStream?)
        //                return in;
            else -> {
                if (`in`.type < 0) {
                    val lit = (`in` as PdfLiteral).toString()
                    if (lit == "true" || lit == "false") {
                        return PdfBoolean(lit)
                    }
                    return PdfLiteral(lit)
                }
                println("CANNOT COPY type " + `in`.type)
                return null
            }
        }
    }

    /**
     * convenience method. Given an imported page, set our "globals"
     */
    protected fun setFromIPage(iPage: PdfImportedPage): Int {
        val pageNum = iPage.pageNumber
        val inst = currentPdfReaderInstance = iPage.pdfReaderInstance
        reader = inst.reader
        setFromReader(reader)
        return pageNum
    }

    /**
     * convenience method. Given a reader, set our "globals"
     */
    protected fun setFromReader(reader: PdfReader) {
        this.reader = reader
        indirects = indirectMap[reader]
        if (indirects == null) {
            indirects = HashMap<RefKey, IndirectReferences>()
            indirectMap.put(reader, indirects)
        }
    }

    /**
     * Add an imported page to our output
     * @param iPage an imported page
     * *
     * @throws IOException, BadPdfFormatException
     */
    @Throws(IOException::class, BadPdfFormatException::class)
    open fun addPage(iPage: PdfImportedPage) {
        if (mergeFields && !mergeFieldsInternalCall) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("1.method.cannot.be.used.in.mergeFields.mode.please.use.addDocument", "addPage"))
        }

        val pageNum = setFromIPage(iPage)
        val thePage = reader.getPageN(pageNum)
        val origRef = reader.getPageOrigRef(pageNum)
        reader.releasePage(pageNum)
        val key = RefKey(origRef)
        val pageRef: PdfIndirectReference
        var iRef: IndirectReferences? = indirects!![key]
        if (iRef != null && !iRef.copied) {
            pageReferences.add(iRef.ref)
            iRef.setCopied()
        }
        pageRef = currentPage
        if (iRef == null) {
            iRef = IndirectReferences(pageRef)
            indirects!!.put(key, iRef)
        }
        iRef.setCopied()
        if (isTagged)
            structTreeRootReference = reader.catalog.get(PdfName.STRUCTTREEROOT) as PRIndirectReference?
        val newPage = copyDictionary(thePage)
        if (mergeFields) {
            val importedPage = importedPages[importedPages.size - 1]
            importedPage.annotsIndirectReference = body.pdfIndirectReference
            newPage.put(PdfName.ANNOTS, importedPage.annotsIndirectReference)
        }
        root.addPage(newPage)
        iPage.setCopied()
        ++currentPageNumber
        pdfDocument.setPageCount(currentPageNumber)
        structTreeRootReference = null
    }

    /**
     * Adds a blank page.
     * @param    rect The page dimension
     * *
     * @param    rotation The rotation angle in degrees
     * *
     * @throws DocumentException
     * *
     * @since    2.1.5
     */
    @Throws(DocumentException::class)
    fun addPage(rect: Rectangle, rotation: Int) {
        if (mergeFields && !mergeFieldsInternalCall) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("1.method.cannot.be.used.in.mergeFields.mode.please.use.addDocument", "addPage"))
        }
        val mediabox = PdfRectangle(rect, rotation)
        val resources = PageResources()
        val page = PdfPage(mediabox, HashMap<String, PdfRectangle>(), resources.resources, 0)
        page.put(PdfName.TABS, tabs)
        root.addPage(page)
        ++currentPageNumber
        pdfDocument.setPageCount(currentPageNumber)
    }

    @Throws(DocumentException::class, IOException::class)
    fun addDocument(reader: PdfReader, pagesToKeep: List<Int>) {
        if (indirectMap.containsKey(reader)) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("document.1.has.already.been.added", reader.toString()))
        }
        reader.selectPages(pagesToKeep, false)
        addDocument(reader)
    }

    /**
     * Copy document fields to a destination document.
     * @param reader a document where fields are copied from.
     * *
     * @throws DocumentException
     * *
     * @throws IOException
     */
    @Throws(DocumentException::class, IOException::class)
    fun copyDocumentFields(reader: PdfReader) {
        if (!document.isOpen) {
            throw DocumentException(MessageLocalization.getComposedMessage("the.document.is.not.open.yet.you.can.only.add.meta.information"))
        }

        if (indirectMap.containsKey(reader)) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("document.1.has.already.been.added", reader.toString()))
        }

        if (!reader.isOpenedWithFullPermissions)
            throw BadPasswordException(MessageLocalization.getComposedMessage("pdfreader.not.opened.with.owner.password"))

        if (!mergeFields)
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("1.method.can.be.only.used.in.mergeFields.mode.please.use.addDocument", "copyDocumentFields"))

        indirects = HashMap<RefKey, IndirectReferences>()
        indirectMap.put(reader, indirects)

        reader.consolidateNamedDestinations()
        reader.shuffleSubsetNames()
        if (isTagged && PdfStructTreeController.checkTagged(reader)) {
            structTreeRootReference = reader.catalog.get(PdfName.STRUCTTREEROOT) as PRIndirectReference?
            if (structTreeController != null) {
                if (reader !== structTreeController!!.reader)
                    structTreeController!!.setReader(reader)
            } else {
                structTreeController = PdfStructTreeController(reader, this)
            }
        }

        val annotationsToBeCopied = ArrayList<PdfObject>()

        for (i in 1..reader.numberOfPages) {
            val page = reader.getPageNRelease(i)
            if (page != null && page.contains(PdfName.ANNOTS)) {
                val annots = page.getAsArray(PdfName.ANNOTS)
                if (annots != null && annots.size() > 0) {
                    if (importedPages.size < i)
                        throw DocumentException(MessageLocalization.getComposedMessage("there.are.not.enough.imported.pages.for.copied.fields"))
                    indirectMap[reader].put(RefKey(reader.pageRefs.getPageOrigRef(i)), IndirectReferences(pageReferences[i - 1]))
                    for (j in 0..annots.size() - 1) {
                        val annot = annots.getAsDict(j)
                        if (annot != null) {
                            annot.put(annotId, PdfNumber(++annotIdCnt))
                            annotationsToBeCopied.add(annots.getPdfObject(j))
                        }
                    }
                }
            }
        }

        for (annot in annotationsToBeCopied) {
            copyObject(annot)
        }

        if (isTagged && structTreeController != null)
            structTreeController!!.attachStructTreeRootKids(null)

        val acro = reader.acroFields
        val needapp = !acro.isGenerateAppearances
        if (needapp)
            needAppearances = true
        fields.add(acro)
        updateCalculationOrder(reader)
        structTreeRootReference = null
    }

    @Throws(DocumentException::class, IOException::class)
    fun addDocument(reader: PdfReader) {
        if (!document.isOpen) {
            throw DocumentException(MessageLocalization.getComposedMessage("the.document.is.not.open.yet.you.can.only.add.meta.information"))
        }
        if (indirectMap.containsKey(reader)) {
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("document.1.has.already.been.added", reader.toString()))
        }
        if (!reader.isOpenedWithFullPermissions)
            throw BadPasswordException(MessageLocalization.getComposedMessage("pdfreader.not.opened.with.owner.password"))
        if (mergeFields) {
            reader.consolidateNamedDestinations()
            reader.shuffleSubsetNames()
            for (i in 1..reader.numberOfPages) {
                val page = reader.getPageNRelease(i)
                if (page != null && page.contains(PdfName.ANNOTS)) {
                    val annots = page.getAsArray(PdfName.ANNOTS)
                    if (annots != null) {
                        for (j in 0..annots.size() - 1) {
                            val annot = annots.getAsDict(j)
                            annot?.put(annotId, PdfNumber(++annotIdCnt))
                        }
                    }
                }
            }
            val acro = reader.acroFields
            // when a document with NeedAppearances is encountered, the flag is set
            // in the resulting document.
            val needapp = !acro.isGenerateAppearances
            if (needapp)
                needAppearances = true
            fields.add(acro)
            updateCalculationOrder(reader)
        }
        val tagged = this.isTagged && PdfStructTreeController.checkTagged(reader)
        mergeFieldsInternalCall = true
        for (i in 1..reader.numberOfPages) {
            addPage(getImportedPage(reader, i, tagged))
        }
        mergeFieldsInternalCall = false
    }

    @Throws(IOException::class)
    override fun addToBody(`object`: PdfObject, ref: PdfIndirectReference): PdfIndirectObject {
        return this.addToBody(`object`, ref, false)
    }

    @Throws(IOException::class)
    override fun addToBody(`object`: PdfObject, ref: PdfIndirectReference, formBranching: Boolean): PdfIndirectObject {
        if (formBranching) {
            updateReferences(`object`)
        }
        val iobj: PdfIndirectObject
        if ((isTagged || mergeFields) && indirectObjects != null && (`object`.isArray || `object`.isDictionary || `object`.isStream || `object`.isNull)) {
            val key = RefKey(ref)
            var obj: PdfIndirectObject? = indirectObjects!![key]
            if (obj == null) {
                obj = PdfIndirectObject(ref, `object`, this)
                indirectObjects!!.put(key, obj)
            }
            iobj = obj
        } else {
            iobj = super.addToBody(`object`, ref)
        }
        if (mergeFields && `object`.isDictionary) {
            val annotId = (`object` as PdfDictionary).getAsNumber(PdfCopy.annotId)
            if (annotId != null) {
                if (formBranching) {
                    mergedMap!!.put(annotId.intValue(), iobj)
                    mergedSet!!.add(iobj)
                } else {
                    unmergedMap!!.put(annotId.intValue(), iobj)
                    unmergedIndirectRefsMap!!.put(RefKey(iobj.number, iobj.generation), iobj)
                }
            }
        }
        return iobj
    }

    override fun cacheObject(iobj: PdfIndirectObject) {
        if ((isTagged || mergeFields) && indirectObjects != null) {
            savedObjects.add(iobj)
            val key = RefKey(iobj.number, iobj.generation)
            if (!indirectObjects!!.containsKey(key)) indirectObjects!!.put(key, iobj)
        }
    }

    @Throws(IOException::class)
    override fun flushTaggedObjects() {
        try {
            fixTaggedStructure()
        } catch (ex: ClassCastException) {
        } finally {
            flushIndirectObjects()
        }
    }

    @Throws(IOException::class, BadPdfFormatException::class)
    override fun flushAcroFields() {
        if (mergeFields) {
            try {
                //save annotations that appear just at page level (comments, popups)
                for (page in importedPages) {
                    val pageDict = page.reader.getPageN(page.pageNumber)
                    if (pageDict != null) {
                        val pageFields = pageDict.getAsArray(PdfName.ANNOTS)
                        if (pageFields == null || pageFields.size() == 0)
                            continue
                        for (items in page.reader.acroFields.getFields().values) {
                            for (ref in items.widget_refs) {
                                pageFields.arrayList.remove(ref)
                            }
                        }
                        indirects = indirectMap[page.reader]
                        for (ref in pageFields.arrayList)
                            page.mergedFields.add(copyObject(ref))
                    }
                }
                //ok, remove old fields and build create new one
                for (reader in indirectMap.keys) {
                    reader.removeFields()
                }
                mergeFields()
                createAcroForms()

            } catch (ex: ClassCastException) {
            } finally {
                if (!isTagged)
                    flushIndirectObjects()
            }
        }
    }


    @Throws(IOException::class)
    protected fun fixTaggedStructure() {
        val numTree = structureTreeRoot!!.numTree
        val activeKeys = HashSet<RefKey>()
        val actives = ArrayList<PdfIndirectReference>()
        var pageRefIndex = 0

        if (mergeFields && acroForm != null) {
            actives.add(acroForm)
            activeKeys.add(RefKey(acroForm))
        }
        for (page in pageReferences) {
            actives.add(page)
            activeKeys.add(RefKey(page))
        }

        //from end, because some objects can appear on several pages because of MCR (out16.pdf)
        for (i in numTree.size - 1 downTo 0) {
            val currNum = numTree[i] ?: continue
            val numKey = RefKey(currNum)
            val obj = indirectObjects!![numKey].`object`
            if (obj.isDictionary) {
                var addActiveKeys = false
                if (pageReferences.contains((obj as PdfDictionary).get(PdfName.PG))) {
                    addActiveKeys = true
                } else {
                    val k = PdfStructTreeController.getKDict(obj)
                    if (k != null && pageReferences.contains(k.get(PdfName.PG))) {
                        addActiveKeys = true
                    }
                }
                if (addActiveKeys) {
                    activeKeys.add(numKey)
                    actives.add(currNum)
                } else {
                    numTree.remove(i)
                }
            } else if (obj.isArray) {
                activeKeys.add(numKey)
                actives.add(currNum)
                val currNums = obj as PdfArray
                val currPage = pageReferences[pageRefIndex++]
                actives.add(currPage)
                activeKeys.add(RefKey(currPage))
                var prevKid: PdfIndirectReference? = null
                for (j in 0..currNums.size() - 1) {
                    val currKid = currNums.getDirectObject(j) as PdfIndirectReference?
                    if (currKid == prevKid) continue
                    val kidKey = RefKey(currKid)
                    activeKeys.add(kidKey)
                    actives.add(currKid)

                    val iobj = indirectObjects!![kidKey]
                    if (iobj.`object`!!.isDictionary) {
                        val dict = iobj.`object` as PdfDictionary?
                        val pg = dict.get(PdfName.PG) as PdfIndirectReference?
                        //if pg is real page - do nothing, else set correct pg and remove first MCID if exists
                        if (pg != null && !pageReferences.contains(pg) && pg != currPage) {
                            dict.put(PdfName.PG, currPage)
                            val kids = dict.getAsArray(PdfName.K)
                            if (kids != null) {
                                val firstKid = kids.getDirectObject(0)
                                if (firstKid.isNumber) kids.remove(0)
                            }
                        }
                    }
                    prevKid = currKid
                }
            }
        }

        val activeClassMaps = HashSet<PdfName>()
        //collect all active objects from current active set (include kids, classmap, attributes)
        findActives(actives, activeKeys, activeClassMaps)
        //find parents of active objects
        val newRefs = findActiveParents(activeKeys)
        //find new objects with incorrect Pg; if find, set Pg from first correct kid. This correct kid must be.
        fixPgKey(newRefs, activeKeys)
        //remove unused kids of StructTreeRoot and remove unused objects from class map
        fixStructureTreeRoot(activeKeys, activeClassMaps)

        for (entry in indirectObjects!!.entries) {
            if (!activeKeys.contains(entry.key)) {
                entry.setValue(null)
            } else {
                if (entry.value.`object`!!.isArray) {
                    removeInactiveReferences(entry.value.`object` as PdfArray?, activeKeys)
                } else if (entry.value.`object`!!.isDictionary) {
                    val kids = (entry.value.`object` as PdfDictionary).get(PdfName.K)
                    if (kids != null && kids.isArray)
                        removeInactiveReferences(kids as PdfArray?, activeKeys)
                }
            }
        }
    }

    private fun removeInactiveReferences(array: PdfArray, activeKeys: HashSet<RefKey>) {
        var i = 0
        while (i < array.size()) {
            val obj = array.getPdfObject(i)
            if (obj.type() == 0 && !activeKeys.contains(RefKey(obj as PdfIndirectReference)) || obj.isDictionary && containsInactivePg(obj as PdfDictionary, activeKeys))
                array.remove(i--)
            ++i
        }
    }

    private fun containsInactivePg(dict: PdfDictionary, activeKeys: HashSet<RefKey>): Boolean {
        val pg = dict.get(PdfName.PG)
        if (pg != null && !activeKeys.contains(RefKey(pg as PdfIndirectReference?)))
            return true
        return false
    }

    //return new found objects
    private fun findActiveParents(activeKeys: HashSet<RefKey>): ArrayList<PdfIndirectReference> {
        val newRefs = ArrayList<PdfIndirectReference>()
        val tmpActiveKeys = ArrayList(activeKeys)
        for (i in tmpActiveKeys.indices) {
            val iobj = indirectObjects!![tmpActiveKeys[i]]
            if (iobj == null || !iobj.`object`!!.isDictionary) continue
            val parent = (iobj.`object` as PdfDictionary).get(PdfName.P)
            if (parent != null && parent.type() == 0) {
                val key = RefKey(parent as PdfIndirectReference?)
                if (!activeKeys.contains(key)) {
                    activeKeys.add(key)
                    tmpActiveKeys.add(key)
                    newRefs.add(parent as PdfIndirectReference?)
                }
            }
        }
        return newRefs
    }

    private fun fixPgKey(newRefs: ArrayList<PdfIndirectReference>, activeKeys: HashSet<RefKey>) {
        for (iref in newRefs) {
            val iobj = indirectObjects!![RefKey(iref)]
            if (iobj == null || !iobj.`object`!!.isDictionary) continue
            val dict = iobj.`object` as PdfDictionary?
            val pg = dict.get(PdfName.PG)
            if (pg == null || activeKeys.contains(RefKey(pg as PdfIndirectReference?))) continue
            val kids = dict.getAsArray(PdfName.K) ?: continue
            var i = 0
            while (i < kids.size()) {
                val obj = kids.getPdfObject(i)
                if (obj.type() != 0) {
                    kids.remove(i--)
                } else {
                    val kid = indirectObjects!![RefKey(obj as PdfIndirectReference)]
                    if (kid != null && kid.`object`!!.isDictionary) {
                        val kidPg = (kid.`object` as PdfDictionary).get(PdfName.PG)
                        if (kidPg != null && activeKeys.contains(RefKey(kidPg as PdfIndirectReference?))) {
                            dict.put(PdfName.PG, kidPg)
                            break
                        }
                    }
                }
                ++i
            }
        }
    }

    private fun findActives(actives: ArrayList<PdfIndirectReference>, activeKeys: HashSet<RefKey>, activeClassMaps: HashSet<PdfName>) {
        //collect all active objects from current active set (include kids, classmap, attributes)
        for (i in actives.indices) {
            val key = RefKey(actives[i])
            val iobj = indirectObjects!![key]
            if (iobj == null || iobj.`object` == null) continue
            when (iobj.`object`!!.type()) {
                0//PdfIndirectReference
                -> findActivesFromReference(iobj.`object` as PdfIndirectReference?, actives, activeKeys)
                PdfObject.ARRAY -> findActivesFromArray(iobj.`object` as PdfArray?, actives, activeKeys, activeClassMaps)
                PdfObject.DICTIONARY, PdfObject.STREAM -> findActivesFromDict(iobj.`object` as PdfDictionary?, actives, activeKeys, activeClassMaps)
            }
        }
    }

    private fun findActivesFromReference(iref: PdfIndirectReference, actives: ArrayList<PdfIndirectReference>, activeKeys: HashSet<RefKey>) {
        val key = RefKey(iref)
        val iobj = indirectObjects!![key]
        if (iobj != null && iobj.`object`!!.isDictionary && containsInactivePg(iobj.`object` as PdfDictionary?, activeKeys)) return

        if (!activeKeys.contains(key)) {
            activeKeys.add(key)
            actives.add(iref)
        }
    }

    private fun findActivesFromArray(array: PdfArray, actives: ArrayList<PdfIndirectReference>, activeKeys: HashSet<RefKey>, activeClassMaps: HashSet<PdfName>) {
        for (obj in array) {
            when (obj.type()) {
                0//PdfIndirectReference
                -> findActivesFromReference(obj as PdfIndirectReference, actives, activeKeys)
                PdfObject.ARRAY -> findActivesFromArray(obj as PdfArray, actives, activeKeys, activeClassMaps)
                PdfObject.DICTIONARY, PdfObject.STREAM -> findActivesFromDict(obj as PdfDictionary, actives, activeKeys, activeClassMaps)
            }
        }
    }

    private fun findActivesFromDict(dict: PdfDictionary, actives: ArrayList<PdfIndirectReference>, activeKeys: HashSet<RefKey>, activeClassMaps: HashSet<PdfName>) {
        if (containsInactivePg(dict, activeKeys)) return
        for (key in dict.keys) {
            val obj = dict.get(key)
            if (key == PdfName.P)
                continue
            else if (key == PdfName.C) {
                //classmap
                if (obj.isArray) {
                    for (cm in obj as PdfArray) {
                        if (cm.isName) activeClassMaps.add(cm as PdfName)
                    }
                } else if (obj.isName) activeClassMaps.add(obj as PdfName)
                continue
            }
            when (obj.type()) {
                0//PdfIndirectReference
                -> findActivesFromReference(obj as PdfIndirectReference, actives, activeKeys)
                PdfObject.ARRAY -> findActivesFromArray(obj as PdfArray, actives, activeKeys, activeClassMaps)
                PdfObject.DICTIONARY, PdfObject.STREAM -> findActivesFromDict(obj as PdfDictionary, actives, activeKeys, activeClassMaps)
            }
        }
    }

    @Throws(IOException::class)
    protected fun flushIndirectObjects() {
        for (iobj in savedObjects)
            indirectObjects!!.remove(RefKey(iobj.number, iobj.generation))
        val inactives = HashSet<RefKey>()
        for (entry in indirectObjects!!.entries) {
            if (entry.value != null)
                writeObjectToBody(entry.value)
            else
                inactives.add(entry.key)
        }
        val pdfCrossReferences = ArrayList(body.xrefs)
        for (cr in pdfCrossReferences) {
            val key = RefKey(cr.refnum, 0)
            if (inactives.contains(key))
                body.xrefs.remove(cr)
        }
        indirectObjects = null
    }

    @Throws(IOException::class)
    private fun writeObjectToBody(`object`: PdfIndirectObject) {
        var skipWriting = false
        if (mergeFields) {
            updateAnnotationReferences(`object`.`object`)
            if (`object`.`object`!!.isDictionary || `object`.`object`!!.isStream) {
                val dictionary = `object`.`object` as PdfDictionary?
                if (unmergedIndirectRefsMap!!.containsKey(RefKey(`object`.number, `object`.generation))) {
                    val annotId = dictionary.getAsNumber(PdfCopy.annotId)
                    if (annotId != null && mergedMap!!.containsKey(annotId.intValue()))
                        skipWriting = true
                }
                if (mergedSet!!.contains(`object`)) {
                    val annotId = dictionary.getAsNumber(PdfCopy.annotId)
                    if (annotId != null) {
                        val unmerged = unmergedMap!![annotId.intValue()]
                        if (unmerged != null && unmerged.`object`!!.isDictionary) {
                            val structParent = (unmerged.`object` as PdfDictionary).getAsNumber(PdfName.STRUCTPARENT)
                            if (structParent != null) {
                                dictionary.put(PdfName.STRUCTPARENT, structParent)
                            }
                        }
                    }
                }
            }
        }
        if (!skipWriting) {
            var dictionary: PdfDictionary? = null
            var annotId: PdfNumber? = null
            if (mergeFields && `object`.`object`!!.isDictionary) {
                dictionary = `object`.`object` as PdfDictionary?
                annotId = dictionary!!.getAsNumber(PdfCopy.annotId)
                if (annotId != null)
                    dictionary.remove(PdfCopy.annotId)
            }
            body.add(`object`.`object`, `object`.number, `object`.generation, true)
            if (annotId != null) {
                dictionary!!.put(PdfCopy.annotId, annotId)
            }
        }
    }

    private fun updateAnnotationReferences(obj: PdfObject) {
        if (obj.isArray) {
            val array = obj as PdfArray
            for (i in 0..array.size() - 1) {
                val o = array.getPdfObject(i)
                if (o != null && o.type() == 0) {
                    val entry = unmergedIndirectRefsMap!![RefKey(o as PdfIndirectReference?)]
                    if (entry != null) {
                        if (entry.`object`!!.isDictionary) {
                            val annotId = (entry.`object` as PdfDictionary).getAsNumber(PdfCopy.annotId)
                            if (annotId != null) {
                                val merged = mergedMap!![annotId.intValue()]
                                if (merged != null) {
                                    array.set(i, merged.indirectReference)
                                }
                            }
                        }
                    }
                } else {
                    updateAnnotationReferences(o)
                }
            }
        } else if (obj.isDictionary || obj.isStream) {
            val dictionary = obj as PdfDictionary
            for (key in dictionary.keys) {
                val o = dictionary.get(key)
                if (o != null && o.type() == 0) {
                    val entry = unmergedIndirectRefsMap!![RefKey(o as PdfIndirectReference?)]
                    if (entry != null) {
                        if (entry.`object`!!.isDictionary) {
                            val annotId = (entry.`object` as PdfDictionary).getAsNumber(PdfCopy.annotId)
                            if (annotId != null) {
                                val merged = mergedMap!![annotId.intValue()]
                                if (merged != null) {
                                    dictionary.put(key, merged.indirectReference)
                                }
                            }
                        }
                    }
                } else {
                    updateAnnotationReferences(o)
                }
            }
        }
    }

    private fun updateCalculationOrder(reader: PdfReader) {
        val catalog = reader.catalog
        val acro = catalog.getAsDict(PdfName.ACROFORM) ?: return
        val co = acro.getAsArray(PdfName.CO)
        if (co == null || co.size() == 0)
            return
        val af = reader.acroFields
        for (k in 0..co.size() - 1) {
            val obj = co.getPdfObject(k)
            if (obj == null || !obj.isIndirect)
                continue
            var name = getCOName(reader, obj as PRIndirectReference?)
            if (af.getFieldItem(name) == null)
                continue
            name = "." + name
            if (calculationOrder!!.contains(name))
                continue
            calculationOrder!!.add(name)
        }
    }

    private fun mergeFields() {
        var pageOffset = 0
        for (k in fields.indices) {
            val af = fields[k]
            val fd = af.getFields()
            if (pageOffset < importedPages.size && importedPages[pageOffset].reader === af.reader) {
                addPageOffsetToField(fd, pageOffset)
                pageOffset += af.reader.numberOfPages
            }
            mergeWithMaster(fd)
        }
    }

    private fun addPageOffsetToField(fd: Map<String, AcroFields.Item>, pageOffset: Int) {
        if (pageOffset == 0)
            return
        for (item in fd.values) {
            for (k in 0..item.size() - 1) {
                val p = item.getPage(k)!!.toInt()
                item.forcePage(k, p + pageOffset)
            }
        }
    }

    private fun mergeWithMaster(fd: Map<String, AcroFields.Item>) {
        for (entry in fd.entries) {
            val name = entry.key
            mergeField(name, entry.value)
        }
    }

    @SuppressWarnings("unchecked")
    private fun mergeField(name: String, item: AcroFields.Item) {
        var map: HashMap<String, Any> = fieldTree
        val tk = StringTokenizer(name, ".")
        if (!tk.hasMoreTokens())
            return
        while (true) {
            val s = tk.nextToken()
            var obj: Any? = map[s]
            if (tk.hasMoreTokens()) {
                if (obj == null) {
                    obj = LinkedHashMap<String, Any>()
                    map.put(s, obj)
                    map = obj as HashMap<String, Any>?
                    continue
                } else if (obj is HashMap<Any, Any>)
                    map = obj as HashMap<String, Any>?
                else
                    return
            } else {
                if (obj is HashMap<Any, Any>)
                    return
                val merged = item.getMerged(0)
                if (obj == null) {
                    val field = PdfDictionary()
                    if (PdfName.SIG == merged.get(PdfName.FT))
                        hasSignature = true
                    for (element in merged.keys) {
                        if (fieldKeys.contains(element))
                            field.put(element, merged.get(element))
                    }
                    val list = ArrayList<Any>()
                    list.add(field)
                    createWidgets(list, item)
                    map.put(s, list)
                } else {
                    val list = obj as ArrayList<Any>?
                    val field = list.get(0) as PdfDictionary
                    val type1 = field.get(PdfName.FT) as PdfName?
                    val type2 = merged.get(PdfName.FT) as PdfName?
                    if (type1 == null || type1 != type2)
                        return
                    var flag1 = 0
                    val f1 = field.get(PdfName.FF)
                    if (f1 != null && f1.isNumber)
                        flag1 = (f1 as PdfNumber).intValue()
                    var flag2 = 0
                    val f2 = merged.get(PdfName.FF)
                    if (f2 != null && f2.isNumber)
                        flag2 = (f2 as PdfNumber).intValue()
                    if (type1 == PdfName.BTN) {
                        if (flag1 xor flag2 and PdfFormField.FF_PUSHBUTTON != 0)
                            return
                        if (flag1 and PdfFormField.FF_PUSHBUTTON == 0 && flag1 xor flag2 and PdfFormField.FF_RADIO != 0)
                            return
                    } else if (type1 == PdfName.CH) {
                        if (flag1 xor flag2 and PdfFormField.FF_COMBO != 0)
                            return
                    }
                    createWidgets(list, item)
                }
                return
            }
        }
    }

    private fun createWidgets(list: ArrayList<Any>, item: AcroFields.Item) {
        for (k in 0..item.size() - 1) {
            list.add(item.getPage(k))
            val merged = item.getMerged(k)
            val dr = merged.get(PdfName.DR)
            if (dr != null)
                PdfFormField.mergeResources(resources, PdfReader.getPdfObject(dr) as PdfDictionary?)
            val widget = PdfDictionary()
            for (element in merged.keys) {
                if (widgetKeys.contains(element))
                    widget.put(element, merged.get(element))
            }
            widget.put(iTextTag, PdfNumber(item.getTabOrder(k)!!.toInt() + 1))
            list.add(widget)
        }
    }

    @Throws(IOException::class)
    private fun propagate(obj: PdfObject?): PdfObject {
        var obj = obj
        if (obj == null) {
            return PdfNull()
        } else if (obj.isArray) {
            val a = obj as PdfArray?
            for (i in 0..a.size() - 1) {
                a.set(i, propagate(a.getPdfObject(i)))
            }
            return a
        } else if (obj.isDictionary || obj.isStream) {
            val d = obj as PdfDictionary?
            for (key in d.keys) {
                d.put(key, propagate(d.get(key)))
            }
            return d
        } else if (obj.isIndirect) {
            obj = PdfReader.getPdfObject(obj)
            return addToBody(propagate(obj)).indirectReference
        } else
            return obj
    }

    @Throws(IOException::class, BadPdfFormatException::class)
    private fun createAcroForms() {
        if (fieldTree!!.isEmpty()) {
            //write annotations that appear just at page level (comments, popups)
            for (importedPage in importedPages) {
                if (importedPage.mergedFields.size() > 0)
                    addToBody(importedPage.mergedFields, importedPage.annotsIndirectReference)
            }
            return
        }
        val form = PdfDictionary()
        form.put(PdfName.DR, propagate(resources))

        if (needAppearances) {
            form.put(PdfName.NEEDAPPEARANCES, PdfBoolean.PDFTRUE)
        }
        form.put(PdfName.DA, PdfString("/Helv 0 Tf 0 g "))
        tabOrder = HashMap<PdfArray, ArrayList<Int>>()
        calculationOrderRefs = ArrayList<Any>(calculationOrder)
        form.put(PdfName.FIELDS, branchForm(fieldTree, null, ""))
        if (hasSignature)
            form.put(PdfName.SIGFLAGS, PdfNumber(3))
        val co = PdfArray()
        for (k in calculationOrderRefs!!.indices) {
            val obj = calculationOrderRefs!![k]
            if (obj is PdfIndirectReference)
                co.add(obj)
        }
        if (co.size() > 0)
            form.put(PdfName.CO, co)
        this.acroForm = addToBody(form).indirectReference
        for (importedPage in importedPages) {
            addToBody(importedPage.mergedFields, importedPage.annotsIndirectReference)
        }
    }

    private fun updateReferences(obj: PdfObject) {
        if (obj.isDictionary || obj.isStream) {
            val dictionary = obj as PdfDictionary
            for (key in dictionary.keys) {
                val o = dictionary.get(key)
                if (o.isIndirect) {
                    val reader = (o as PRIndirectReference).reader
                    val indirects = indirectMap[reader]
                    val indRef = indirects[RefKey(o)]
                    if (indRef != null) {
                        dictionary.put(key, indRef.ref)
                    }
                } else {
                    updateReferences(o)
                }
            }
        } else if (obj.isArray) {
            val array = obj as PdfArray
            for (i in 0..array.size() - 1) {
                val o = array.getPdfObject(i)
                if (o.isIndirect) {
                    val reader = (o as PRIndirectReference).reader
                    val indirects = indirectMap[reader]
                    val indRef = indirects[RefKey(o)]
                    if (indRef != null) {
                        array.set(i, indRef.ref)
                    }
                } else {
                    updateReferences(o)
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Throws(IOException::class, BadPdfFormatException::class)
    private fun branchForm(level: HashMap<String, Any>, parent: PdfIndirectReference?, fname: String): PdfArray {
        val arr = PdfArray()
        for ((name, obj) in level) {
            val ind = pdfIndirectReference
            val dic = PdfDictionary()
            if (parent != null)
                dic.put(PdfName.PARENT, parent)
            dic.put(PdfName.T, PdfString(name, PdfObject.TEXT_UNICODE))
            val fname2 = fname + "." + name
            val coidx = calculationOrder!!.indexOf(fname2)
            if (coidx >= 0)
                calculationOrderRefs!![coidx] = ind
            if (obj is HashMap<Any, Any>) {
                dic.put(PdfName.KIDS, branchForm(obj as HashMap<String, Any>, ind, fname2))
                arr.add(ind)
                addToBody(dic, ind, true)
            } else {
                val list = obj as ArrayList<Any>
                dic.mergeDifferent(list[0] as PdfDictionary)
                if (list.size == 3) {
                    dic.mergeDifferent(list[2] as PdfDictionary)
                    val page = (list[1] as Int).toInt()
                    val annots = importedPages[page - 1].mergedFields
                    val nn = dic.get(iTextTag) as PdfNumber?
                    dic.remove(iTextTag)
                    dic.put(PdfName.TYPE, PdfName.ANNOT)
                    adjustTabOrder(annots, ind, nn)
                } else {
                    val field = list[0] as PdfDictionary
                    val kids = PdfArray()
                    var k = 1
                    while (k < list.size) {
                        val page = (list[k] as Int).toInt()
                        val annots = importedPages[page - 1].mergedFields
                        val widget = PdfDictionary()
                        widget.merge(list[k + 1] as PdfDictionary)
                        widget.put(PdfName.PARENT, ind)
                        val nn = widget.get(iTextTag) as PdfNumber?
                        widget.remove(iTextTag)
                        if (PdfCopy.isTextField(field)) {
                            val v = field.getAsString(PdfName.V)
                            val ap = widget.getDirectObject(PdfName.AP)
                            if (v != null && ap != null) {
                                if (!mergedTextFields.containsKey(list)) {
                                    mergedTextFields.put(list, v)
                                } else {
                                    try {
                                        val tx = TextField(this, null, null)
                                        fields[0].decodeGenericDictionary(widget, tx)
                                        var box = PdfReader.getNormalizedRectangle(widget.getAsArray(PdfName.RECT))
                                        if (tx.rotation == 90 || tx.rotation == 270)
                                            box = box.rotate()
                                        tx.box = box
                                        tx.text = mergedTextFields[list].toUnicodeString()
                                        val app = tx.appearance
                                        (ap as PdfDictionary).put(PdfName.N, app.indirectReference)
                                    } catch (e: DocumentException) {
                                        //do nothing
                                    }

                                }
                            }
                        } else if (PdfCopy.isCheckButton(field)) {
                            val v = field.getAsName(PdfName.V)
                            val `as` = widget.getAsName(PdfName.AS)
                            if (v != null && `as` != null)
                                widget.put(PdfName.AS, v)
                        } else if (PdfCopy.isRadioButton(field)) {
                            val v = field.getAsName(PdfName.V)
                            val `as` = widget.getAsName(PdfName.AS)
                            if (v != null && `as` != null && `as` != getOffStateName(widget)) {
                                if (!mergedRadioButtons.contains(list)) {
                                    mergedRadioButtons.add(list)
                                    widget.put(PdfName.AS, v)
                                } else {
                                    widget.put(PdfName.AS, getOffStateName(widget))
                                }
                            }
                        }
                        widget.put(PdfName.TYPE, PdfName.ANNOT)
                        val wref = addToBody(widget, pdfIndirectReference, true).indirectReference
                        adjustTabOrder(annots, wref, nn)
                        kids.add(wref)
                        k += 2
                    }
                    dic.put(PdfName.KIDS, kids)
                }
                arr.add(ind)
                addToBody(dic, ind, true)
            }
        }
        return arr
    }

    private fun adjustTabOrder(annots: PdfArray, ind: PdfIndirectReference, nn: PdfNumber) {
        val v = nn.intValue()
        var t: ArrayList<Int>? = tabOrder!![annots]
        if (t == null) {
            t = ArrayList<Int>()
            val size = annots.size() - 1
            for (k in 0..size - 1) {
                t.add(zero)
            }
            t.add(Integer.valueOf(v))
            tabOrder!!.put(annots, t)
            annots.add(ind)
        } else {
            var size = t.size - 1
            for (k in size downTo 0) {
                if (t[k].toInt() <= v) {
                    t.add(k + 1, Integer.valueOf(v))
                    annots.add(k + 1, ind)
                    size = -2
                    break
                }
            }
            if (size != -2) {
                t.add(0, Integer.valueOf(v))
                annots.add(0, ind)
            }
        }
    }

    /*
     * the getCatalog method is part of PdfWriter.
     * we wrap this so that we can extend it
     */
    override fun getCatalog(rootObj: PdfIndirectReference): PdfDictionary {
        try {
            val theCat = pdfDocument.getCatalog(rootObj)
            buildStructTreeRootForTagged(theCat)
            if (fieldArray != null) {
                addFieldResources(theCat)
            } else if (mergeFields && acroForm != null) {
                theCat.put(PdfName.ACROFORM, acroForm)
            }
            return theCat
        } catch (e: IOException) {
            throw ExceptionConverter(e)
        }

    }

    protected fun isStructTreeRootReference(prRef: PdfIndirectReference?): Boolean {
        if (prRef == null || structTreeRootReference == null)
            return false
        return prRef.number == structTreeRootReference!!.number && prRef.generation == structTreeRootReference!!.generation
    }

    @Throws(IOException::class)
    private fun addFieldResources(catalog: PdfDictionary) {
        if (fieldArray == null)
            return
        val acroForm = PdfDictionary()
        catalog.put(PdfName.ACROFORM, acroForm)
        acroForm.put(PdfName.FIELDS, fieldArray)
        acroForm.put(PdfName.DA, PdfString("/Helv 0 Tf 0 g "))
        if (fieldTemplates!!.isEmpty())
            return
        val dr = PdfDictionary()
        acroForm.put(PdfName.DR, dr)
        for (template in fieldTemplates!!) {
            PdfFormField.mergeResources(dr, template.resources as PdfDictionary)
        }
        // if (dr.get(PdfName.ENCODING) == null) dr.put(PdfName.ENCODING, PdfName.WIN_ANSI_ENCODING);
        var fonts: PdfDictionary? = dr.getAsDict(PdfName.FONT)
        if (fonts == null) {
            fonts = PdfDictionary()
            dr.put(PdfName.FONT, fonts)
        }
        if (!fonts.contains(PdfName.HELV)) {
            val dic = PdfDictionary(PdfName.FONT)
            dic.put(PdfName.BASEFONT, PdfName.HELVETICA)
            dic.put(PdfName.ENCODING, PdfName.WIN_ANSI_ENCODING)
            dic.put(PdfName.NAME, PdfName.HELV)
            dic.put(PdfName.SUBTYPE, PdfName.TYPE1)
            fonts.put(PdfName.HELV, addToBody(dic).indirectReference)
        }
        if (!fonts.contains(PdfName.ZADB)) {
            val dic = PdfDictionary(PdfName.FONT)
            dic.put(PdfName.BASEFONT, PdfName.ZAPFDINGBATS)
            dic.put(PdfName.NAME, PdfName.ZADB)
            dic.put(PdfName.SUBTYPE, PdfName.TYPE1)
            fonts.put(PdfName.ZADB, addToBody(dic).indirectReference)
        }
    }

    /**
     * Signals that the Document was closed and that no other
     * Elements will be added.
     *
     * The pages-tree is built and written to the outputstream.
     * A Catalog is constructed, as well as an Info-object,
     * the reference table is composed and everything is written
     * to the outputstream embedded in a Trailer.
     */

    override fun close() {
        if (open) {
            pdfDocument.close()
            super.close()
            //  Users are responsible for closing PdfReader
            //            if (ri != null) {
            //                try {
            //                    ri.getReader().close();
            //                    ri.getReaderFile().close();
            //                }
            //                catch (IOException ioe) {
            //                    // empty on purpose
            //                }
            //            }
        }
    }

    fun add(outline: PdfOutline): PdfIndirectReference? {
        return null
    }

    override fun addAnnotation(annot: PdfAnnotation) {
    }

    @Throws(PdfException::class)
    internal override fun add(page: PdfPage, contents: PdfContents): PdfIndirectReference? {
        return null
    }

    @Throws(IOException::class)
    override fun freeReader(reader: PdfReader) {
        if (mergeFields)
            throw UnsupportedOperationException(MessageLocalization.getComposedMessage("it.is.not.possible.to.free.reader.in.merge.fields.mode"))
        val array = reader.trailer!!.getAsArray(PdfName.ID)
        if (array != null)
            originalFileID = array.getAsString(0).bytes
        indirectMap.remove(reader)
        //  TODO: Removed - the user should be responsible for closing all PdfReaders.  But, this could cause a lot of memory leaks in code out there that hasn't been properly closing things - maybe add a finalizer to PdfReader that calls PdfReader#close() ??
        //        if (currentPdfReaderInstance != null) {
        //            if (currentPdfReaderInstance.getReader() == reader) {
        //                try {
        //                    currentPdfReaderInstance.getReader().close();
        //                    currentPdfReaderInstance.getReaderFile().close();
        //                }
        //                catch (IOException ioe) {
        //                    // empty on purpose
        //                }
        currentPdfReaderInstance = null
        //            }
        //        }
        super.freeReader(reader)
    }

    protected fun getOffStateName(widget: PdfDictionary): PdfName {
        return PdfName.Off
    }

    /**
     * Create a page stamp. New content and annotations, including new fields, are allowed.
     * The fields added cannot have parents in another pages. This method modifies the PdfReader instance.
     *
     *
     * The general usage to stamp something in a page is:
     *
     *
     *
     * PdfImportedPage page = copy.getImportedPage(reader, 1);
     * PdfCopy.PageStamp ps = copy.createPageStamp(page);
     * ps.addAnnotation(PdfAnnotation.createText(copy, new Rectangle(50, 180, 70, 200), "Hello", "No Thanks", true, "Comment"));
     * PdfContentByte under = ps.getUnderContent();
     * under.addImage(img);
     * PdfContentByte over = ps.getOverContent();
     * over.beginText();
     * over.setFontAndSize(bf, 18);
     * over.setTextMatrix(30, 30);
     * over.showText("total page " + totalPage);
     * over.endText();
     * ps.alterContents();
     * copy.addPage(page);
     *
     * @param iPage an imported page
     * *
     * @return the PageStamp
     */
    fun createPageStamp(iPage: PdfImportedPage): PageStamp {
        val pageNum = iPage.pageNumber
        val reader = iPage.pdfReaderInstance.reader
        if (isTagged)
            throw RuntimeException(MessageLocalization.getComposedMessage("creating.page.stamp.not.allowed.for.tagged.reader"))
        val pageN = reader.getPageN(pageNum)
        return PageStamp(reader, pageN, this)
    }

    class PageStamp internal constructor(internal var reader: PdfReader, internal var pageN:

    PdfDictionary, internal var cstp: PdfCopy) {
        internal var under: PdfCopy.StampContent? = null
        internal var over: PdfCopy.StampContent? = null
        internal var pageResources: PageResources? = null

        val underContent: PdfContentByte
            get() {
                if (under == null) {
                    if (pageResources == null) {
                        pageResources = PageResources()
                        val resources = pageN.getAsDict(PdfName.RESOURCES)
                        pageResources!!.setOriginalResources(resources, cstp.namePtr)
                    }
                    under = PdfCopy.StampContent(cstp, pageResources)
                }
                return under
            }

        val overContent: PdfContentByte
            get() {
                if (over == null) {
                    if (pageResources == null) {
                        pageResources = PageResources()
                        val resources = pageN.getAsDict(PdfName.RESOURCES)
                        pageResources!!.setOriginalResources(resources, cstp.namePtr)
                    }
                    over = PdfCopy.StampContent(cstp, pageResources)
                }
                return over
            }

        @Throws(IOException::class)
        fun alterContents() {
            if (over == null && under == null)
                return
            var ar: PdfArray? = null
            val content = PdfReader.getPdfObject(pageN.get(PdfName.CONTENTS), pageN)
            if (content == null) {
                ar = PdfArray()
                pageN.put(PdfName.CONTENTS, ar)
            } else if (content.isArray) {
                ar = content as PdfArray?
            } else if (content.isStream) {
                ar = PdfArray()
                ar.add(pageN.get(PdfName.CONTENTS))
                pageN.put(PdfName.CONTENTS, ar)
            } else {
                ar = PdfArray()
                pageN.put(PdfName.CONTENTS, ar)
            }
            val out = ByteBuffer()
            if (under != null) {
                out.append(PdfContents.SAVESTATE)
                applyRotation(pageN, out)
                out.append(under!!.internalBuffer)
                out.append(PdfContents.RESTORESTATE)
            }
            if (over != null)
                out.append(PdfContents.SAVESTATE)
            var stream = PdfStream(out.toByteArray())
            stream.flateCompress(cstp.compressionLevel)
            val ref1 = cstp.addToBody(stream).indirectReference
            ar!!.addFirst(ref1)
            out.reset()
            if (over != null) {
                out.append(' ')
                out.append(PdfContents.RESTORESTATE)
                out.append(PdfContents.SAVESTATE)
                applyRotation(pageN, out)
                out.append(over!!.internalBuffer)
                out.append(PdfContents.RESTORESTATE)
                stream = PdfStream(out.toByteArray())
                stream.flateCompress(cstp.compressionLevel)
                ar.add(cstp.addToBody(stream).indirectReference)
            }
            pageN.put(PdfName.RESOURCES, pageResources!!.resources)
        }

        internal fun applyRotation(pageN: PdfDictionary, out: ByteBuffer) {
            if (!cstp.isRotateContents)
                return
            val page = reader.getPageSizeWithRotation(pageN)
            val rotation = page.rotation
            when (rotation) {
                90 -> {
                    out.append(PdfContents.ROTATE90)
                    out.append(page.top)
                    out.append(' ').append('0').append(PdfContents.ROTATEFINAL)
                }
                180 -> {
                    out.append(PdfContents.ROTATE180)
                    out.append(page.right)
                    out.append(' ')
                    out.append(page.top)
                    out.append(PdfContents.ROTATEFINAL)
                }
                270 -> {
                    out.append(PdfContents.ROTATE270)
                    out.append('0').append(' ')
                    out.append(page.right)
                    out.append(PdfContents.ROTATEFINAL)
                }
            }
        }

        private fun addDocumentField(ref: PdfIndirectReference) {
            if (cstp.fieldArray == null)
                cstp.fieldArray = PdfArray()
            cstp.fieldArray!!.add(ref)
        }

        private fun expandFields(field: PdfFormField, allAnnots: ArrayList<PdfAnnotation>) {
            allAnnots.add(field)
            val kids = field.kids
            if (kids != null) {
                for (f in kids)
                    expandFields(f, allAnnots)
            }
        }

        fun addAnnotation(annot: PdfAnnotation) {
            var annot = annot
            try {
                val allAnnots = ArrayList<PdfAnnotation>()
                if (annot.isForm) {
                    val field = annot as PdfFormField
                    if (field.parent != null)
                        return
                    expandFields(field, allAnnots)
                    if (cstp.fieldTemplates == null)
                        cstp.fieldTemplates = HashSet<PdfTemplate>()
                } else
                    allAnnots.add(annot)
                for (k in allAnnots.indices) {
                    annot = allAnnots[k]
                    if (annot.isForm) {
                        if (!annot.isUsed) {
                            val templates = annot.templates
                            if (templates != null)
                                cstp.fieldTemplates!!.addAll(templates)
                        }
                        val field = annot as PdfFormField
                        if (field.parent == null)
                            addDocumentField(field.indirectReference)
                    }
                    if (annot.isAnnotation) {
                        val pdfobj = PdfReader.getPdfObject(pageN.get(PdfName.ANNOTS), pageN)
                        var annots: PdfArray? = null
                        if (pdfobj == null || !pdfobj.isArray) {
                            annots = PdfArray()
                            pageN.put(PdfName.ANNOTS, annots)
                        } else
                            annots = pdfobj as PdfArray?
                        annots!!.add(annot.indirectReference)
                        if (!annot.isUsed) {
                            val rect = annot.get(PdfName.RECT) as PdfRectangle?
                            if (rect != null && (rect.left() != 0f || rect.right() != 0f || rect.top() != 0f || rect.bottom() != 0f)) {
                                val rotation = reader.getPageRotation(pageN)
                                val pageSize = reader.getPageSizeWithRotation(pageN)
                                when (rotation) {
                                    90 -> annot.put(PdfName.RECT, PdfRectangle(
                                            pageSize.top - rect.bottom(),
                                            rect.left(),
                                            pageSize.top - rect.top(),
                                            rect.right()))
                                    180 -> annot.put(PdfName.RECT, PdfRectangle(
                                            pageSize.right - rect.left(),
                                            pageSize.top - rect.bottom(),
                                            pageSize.right - rect.right(),
                                            pageSize.top - rect.top()))
                                    270 -> annot.put(PdfName.RECT, PdfRectangle(
                                            rect.bottom(),
                                            pageSize.right - rect.left(),
                                            rect.top(),
                                            pageSize.right - rect.right()))
                                }
                            }
                        }
                    }
                    if (!annot.isUsed) {
                        annot.setUsed()
                        cstp.addToBody(annot, annot.indirectReference)
                    }
                }
            } catch (e: IOException) {
                throw ExceptionConverter(e)
            }

        }
    }

    class StampContent
    /** Creates a new instance of StampContent  */
    internal constructor(writer: PdfWriter, internal override var pageResources: PageResources) : PdfContentByte(writer) {

        /**
         * Gets a duplicate of this PdfContentByte. All
         * the members are copied by reference but the buffer stays different.

         * @return a copy of this PdfContentByte
         */
        override val duplicate: PdfContentByte
            get() = PdfCopy.StampContent(pdfWriter, pageResources)
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(PdfCopy::class.java)

        protected var COUNTER = CounterFactory.getCounter(PdfCopy::class.java)

        private val annotId = PdfName("iTextAnnotId")
        private var annotIdCnt = 0
        private val iTextTag = PdfName("_iTextTag_")
        private val zero = Integer.valueOf(0)

        private fun getCOName(reader: PdfReader, ref: PRIndirectReference?): String {
            var ref = ref
            var name = ""
            while (ref != null) {
                val obj = PdfReader.getPdfObject(ref)
                if (obj == null || obj.type() != PdfObject.DICTIONARY)
                    break
                val dic = obj as PdfDictionary?
                val t = dic.getAsString(PdfName.T)
                if (t != null) {
                    name = t.toUnicodeString() + "." + name
                }
                ref = dic.get(PdfName.PARENT) as PRIndirectReference?
            }
            if (name.endsWith("."))
                name = name.substring(0, name.length - 2)
            return name
        }

        protected val widgetKeys = HashSet<PdfName>()
        protected val fieldKeys = HashSet<PdfName>()

        init {
            widgetKeys.add(PdfName.SUBTYPE)
            widgetKeys.add(PdfName.CONTENTS)
            widgetKeys.add(PdfName.RECT)
            widgetKeys.add(PdfName.NM)
            widgetKeys.add(PdfName.M)
            widgetKeys.add(PdfName.F)
            widgetKeys.add(PdfName.BS)
            widgetKeys.add(PdfName.BORDER)
            widgetKeys.add(PdfName.AP)
            widgetKeys.add(PdfName.AS)
            widgetKeys.add(PdfName.C)
            widgetKeys.add(PdfName.A)
            widgetKeys.add(PdfName.STRUCTPARENT)
            widgetKeys.add(PdfName.OC)
            widgetKeys.add(PdfName.H)
            widgetKeys.add(PdfName.MK)
            widgetKeys.add(PdfName.DA)
            widgetKeys.add(PdfName.Q)
            widgetKeys.add(PdfName.P)
            widgetKeys.add(PdfName.TYPE)
            widgetKeys.add(annotId)
            fieldKeys.add(PdfName.AA)
            fieldKeys.add(PdfName.FT)
            fieldKeys.add(PdfName.TU)
            fieldKeys.add(PdfName.TM)
            fieldKeys.add(PdfName.FF)
            fieldKeys.add(PdfName.V)
            fieldKeys.add(PdfName.DV)
            fieldKeys.add(PdfName.DS)
            fieldKeys.add(PdfName.RV)
            fieldKeys.add(PdfName.OPT)
            fieldKeys.add(PdfName.MAXLEN)
            fieldKeys.add(PdfName.TI)
            fieldKeys.add(PdfName.I)
            fieldKeys.add(PdfName.LOCK)
            fieldKeys.add(PdfName.SV)
        }

        internal fun getFlags(field: PdfDictionary): Int? {
            val type = field.getAsName(PdfName.FT)
            if (PdfName.BTN != type)
                return null
            val flags = field.getAsNumber(PdfName.FF) ?: return null
            return flags.intValue()
        }

        internal fun isCheckButton(field: PdfDictionary): Boolean {
            val flags = getFlags(field)
            return flags == null || flags.toInt() and PdfFormField.FF_PUSHBUTTON == 0 && flags.toInt() and PdfFormField.FF_RADIO == 0
        }

        internal fun isRadioButton(field: PdfDictionary): Boolean {
            val flags = getFlags(field)
            return flags != null && flags.toInt() and PdfFormField.FF_PUSHBUTTON == 0 && flags.toInt() and PdfFormField.FF_RADIO != 0
        }

        internal fun isTextField(field: PdfDictionary): Boolean {
            val type = field.getAsName(PdfName.FT)
            return PdfName.TX == type
        }
    }
}
/**
 * Translate a PRDictionary to a PdfDictionary. Also translate all of the
 * objects contained in it.
 */
/**
 * Translate a PRArray to a PdfArray. Also translate all of the objects contained
 * in it
 */
/**
 * Translate a PR-object to a Pdf-object
 */
