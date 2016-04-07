/*
 * $Id: d58c359155c8a324a53dc34418f776a1f0f097ed $
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

import java.io.IOException
import java.io.OutputStream
import java.util.*

import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.exceptions.BadPasswordException
import com.itextpdf.text.log.Counter
import com.itextpdf.text.log.CounterFactory
import com.itextpdf.text.pdf.AcroFields.Item

/**

 * @author  psoares
 */
@Deprecated("")
internal open class PdfCopyFieldsImp @Throws(DocumentException::class)
@JvmOverloads constructor(os: OutputStream, pdfVersion: Char = '\0') : PdfWriter(PdfDocument(), os) {
    var readers = ArrayList<PdfReader>()
    var readers2intrefs = HashMap<PdfReader, IntHashtable>()
    var pages2intrefs = HashMap<PdfReader, IntHashtable>()
    var visited = HashMap<PdfReader, IntHashtable>()
    var fields = ArrayList<AcroFields>()
    var file: RandomAccessFileOrArray
    var fieldTree = HashMap<String, Any>()
    var pageRefs = ArrayList<PdfIndirectReference>()
    var pageDics = ArrayList<PdfDictionary>()
    var resources = PdfDictionary()
    var form: PdfDictionary? = null
    var closing = false
    var nd: Document
    private var tabOrder: HashMap<PdfArray, ArrayList<Int>>? = null
    private val calculationOrder = ArrayList<String>()
    private var calculationOrderRefs: ArrayList<Any>? = null
    private var hasSignature: Boolean = false
    private var needAppearances = false
    private val mergedRadioButtons = HashSet<Any>()


    protected override var counter = CounterFactory.getCounter(PdfCopyFields::class.java)

    init {
        pdfDocument.addWriter(this)
        if (pdfVersion.toInt() != 0)
            super.setPdfVersion(pdfVersion)
        nd = Document()
        nd.addDocListener(pdfDocument)
    }

    @Throws(DocumentException::class, IOException::class)
    fun addDocument(reader: PdfReader, pagesToKeep: List<Int>) {
        var reader = reader
        if (!readers2intrefs.containsKey(reader) && reader.isTampered)
            throw DocumentException(MessageLocalization.getComposedMessage("the.document.was.reused"))
        reader = PdfReader(reader)
        reader.selectPages(pagesToKeep)
        if (reader.numberOfPages == 0)
            return
        reader.isTampered = false
        addDocument(reader)
    }

    @Throws(DocumentException::class, IOException::class)
    fun addDocument(reader: PdfReader) {
        var reader = reader
        if (!reader.isOpenedWithFullPermissions)
            throw BadPasswordException(MessageLocalization.getComposedMessage("pdfreader.not.opened.with.owner.password"))
        openDoc()
        if (readers2intrefs.containsKey(reader)) {
            reader = PdfReader(reader)
        } else {
            if (reader.isTampered)
                throw DocumentException(MessageLocalization.getComposedMessage("the.document.was.reused"))
            reader.consolidateNamedDestinations()
            reader.isTampered = true
        }
        reader.shuffleSubsetNames()
        readers2intrefs.put(reader, IntHashtable())
        readers.add(reader)
        val len = reader.numberOfPages
        val refs = IntHashtable()
        for (p in 1..len) {
            refs.put(reader.getPageOrigRef(p).number, 1)
            reader.releasePage(p)
        }
        pages2intrefs.put(reader, refs)
        visited.put(reader, IntHashtable())
        val acro = reader.acroFields
        // when a document with NeedAppearances is encountered, the flag is set
        // in the resulting document.
        val needapp = !acro.isGenerateAppearances
        if (needapp)
            needAppearances = true
        fields.add(acro)
        updateCalculationOrder(reader)
    }

    /**
     * @since    2.1.5; before 2.1.5 the method was private
     */
    protected fun updateCalculationOrder(reader: PdfReader) {
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
            if (calculationOrder.contains(name))
                continue
            calculationOrder.add(name)
        }
    }

    @Throws(IOException::class)
    fun propagate(obj: PdfObject?, refo: PdfIndirectReference?, restricted: Boolean) {
        if (obj == null)
            return
        //        if (refo != null)
        //            addToBody(obj, refo);
        if (obj is PdfIndirectReference)
            return
        when (obj.type()) {
            PdfObject.DICTIONARY, PdfObject.STREAM -> {
                val dic = obj as PdfDictionary?
                for (key in dic.keys) {
                    if (restricted && (key == PdfName.PARENT || key == PdfName.KIDS))
                        continue
                    val ob = dic.get(key)
                    if (ob != null && ob.isIndirect) {
                        val ind = ob as PRIndirectReference?
                        if (!setVisited(ind) && !isPage(ind)) {
                            val ref = getNewReference(ind)
                            propagate(PdfReader.getPdfObjectRelease(ind), ref, restricted)
                        }
                    } else
                        propagate(ob, null, restricted)
                }
            }
            PdfObject.ARRAY -> {
                //PdfArray arr = new PdfArray();
                val it = (obj as PdfArray).listIterator()
                while (it.hasNext()) {
                    val ob = it.next()
                    if (ob != null && ob.isIndirect) {
                        val ind = ob as PRIndirectReference?
                        if (!isVisited(ind) && !isPage(ind)) {
                            val ref = getNewReference(ind)
                            propagate(PdfReader.getPdfObjectRelease(ind), ref, restricted)
                        }
                    } else
                        propagate(ob, null, restricted)
                }
            }
            PdfObject.INDIRECT -> {
                throw RuntimeException(MessageLocalization.getComposedMessage("reference.pointing.to.reference"))
            }
        }
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

    @SuppressWarnings("unchecked")
    @Throws(IOException::class)
    protected fun branchForm(level: HashMap<String, Any>, parent: PdfIndirectReference?, fname: String): PdfArray {
        val arr = PdfArray()
        for ((name, obj) in level) {
            val ind = pdfIndirectReference
            val dic = PdfDictionary()
            if (parent != null)
                dic.put(PdfName.PARENT, parent)
            dic.put(PdfName.T, PdfString(name, PdfObject.TEXT_UNICODE))
            val fname2 = fname + "." + name
            val coidx = calculationOrder.indexOf(fname2)
            if (coidx >= 0)
                calculationOrderRefs!![coidx] = ind
            if (obj is HashMap<Any, Any>) {
                dic.put(PdfName.KIDS, branchForm(obj as HashMap<String, Any>, ind, fname2))
                arr.add(ind)
                addToBody(dic, ind)
            } else {
                val list = obj as ArrayList<Any>
                dic.mergeDifferent(list[0] as PdfDictionary)
                if (list.size == 3) {
                    dic.mergeDifferent(list[2] as PdfDictionary)
                    val page = (list[1] as Int).toInt()
                    val pageDic = pageDics[page - 1]
                    var annots: PdfArray? = pageDic.getAsArray(PdfName.ANNOTS)
                    if (annots == null) {
                        annots = PdfArray()
                        pageDic.put(PdfName.ANNOTS, annots)
                    }
                    val nn = dic.get(iTextTag) as PdfNumber?
                    dic.remove(iTextTag)
                    adjustTabOrder(annots, ind, nn)
                } else {
                    val field = list[0] as PdfDictionary
                    val v = field.getAsName(PdfName.V)
                    val kids = PdfArray()
                    var k = 1
                    while (k < list.size) {
                        val page = (list[k] as Int).toInt()
                        val pageDic = pageDics[page - 1]
                        var annots: PdfArray? = pageDic.getAsArray(PdfName.ANNOTS)
                        if (annots == null) {
                            annots = PdfArray()
                            pageDic.put(PdfName.ANNOTS, annots)
                        }
                        val widget = PdfDictionary()
                        widget.merge(list[k + 1] as PdfDictionary)
                        widget.put(PdfName.PARENT, ind)
                        val nn = widget.get(iTextTag) as PdfNumber?
                        widget.remove(iTextTag)
                        if (PdfCopy.isCheckButton(field)) {
                            val `as` = widget.getAsName(PdfName.AS)
                            if (v != null && `as` != null)
                                widget.put(PdfName.AS, v)
                        } else if (PdfCopy.isRadioButton(field)) {
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
                        val wref = addToBody(widget).indirectReference
                        adjustTabOrder(annots, wref, nn)
                        kids.add(wref)
                        propagate(widget, null, false)
                        k += 2
                    }
                    dic.put(PdfName.KIDS, kids)
                }
                arr.add(ind)
                addToBody(dic, ind)
                propagate(dic, null, false)
            }
        }
        return arr
    }

    protected fun getOffStateName(widget: PdfDictionary): PdfName {
        return PdfName.Off
    }

    @Throws(IOException::class)
    protected fun createAcroForms() {
        if (fieldTree.isEmpty())
            return
        form = PdfDictionary()
        form!!.put(PdfName.DR, resources)
        propagate(resources, null, false)
        if (needAppearances) {
            form!!.put(PdfName.NEEDAPPEARANCES, PdfBoolean.PDFTRUE)
        }
        form!!.put(PdfName.DA, PdfString("/Helv 0 Tf 0 g "))
        tabOrder = HashMap<PdfArray, ArrayList<Int>>()
        calculationOrderRefs = ArrayList<Any>(calculationOrder)
        form!!.put(PdfName.FIELDS, branchForm(fieldTree, null, ""))
        if (hasSignature)
            form!!.put(PdfName.SIGFLAGS, PdfNumber(3))
        val co = PdfArray()
        for (k in calculationOrderRefs!!.indices) {
            val obj = calculationOrderRefs!![k]
            if (obj is PdfIndirectReference)
                co.add(obj)
        }
        if (co.size() > 0)
            form!!.put(PdfName.CO, co)
    }

    override fun close() {
        if (closing) {
            super.close()
            return
        }
        closing = true
        try {
            closeIt()
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    /**
     * Creates the new PDF by merging the fields and forms.
     */
    @Throws(IOException::class)
    protected fun closeIt() {
        for (k in readers.indices) {
            readers[k].removeFields()
        }
        for (r in readers.indices) {
            val reader = readers[r]
            for (page in 1..reader.numberOfPages) {
                pageRefs.add(getNewReference(reader.getPageOrigRef(page)))
                pageDics.add(reader.getPageN(page))
            }
        }
        mergeFields()
        createAcroForms()
        for (r in readers.indices) {
            val reader = readers[r]
            for (page in 1..reader.numberOfPages) {
                val dic = reader.getPageN(page)
                val pageRef = getNewReference(reader.getPageOrigRef(page))
                val parent = root.addPageRef(pageRef)
                dic.put(PdfName.PARENT, parent)
                propagate(dic, pageRef, false)
            }
        }
        for ((reader, t) in readers2intrefs) {
            try {
                file = reader.safeFile
                file.reOpen()
                val keys = t.toOrderedKeys()
                for (k in keys.indices) {
                    val ref = PRIndirectReference(reader, keys[k])
                    addToBody(PdfReader.getPdfObjectRelease(ref), t.get(keys[k]))
                }
            } finally {
                try {
                    file.close()
                    // TODO: Removed - the user should be responsible for closing all PdfReaders.  But, this could cause a lot of memory leaks in code out there that hasn't been properly closing things - maybe add a finalizer to PdfReader that calls PdfReader#close() ??            	
                    //                    reader.close();
                } catch (e: Exception) {
                    // empty on purpose
                }

            }
        }
        pdfDocument.close()
    }

    fun addPageOffsetToField(fd: Map<String, AcroFields.Item>, pageOffset: Int) {
        if (pageOffset == 0)
            return
        for (item in fd.values) {
            for (k in 0..item.size() - 1) {
                val p = item.getPage(k)!!.toInt()
                item.forcePage(k, p + pageOffset)
            }
        }
    }

    fun createWidgets(list: ArrayList<Any>, item: AcroFields.Item) {
        for (k in 0..item.size() - 1) {
            list.add(item.getPage(k))
            val merged = item.getMerged(k)
            val dr = merged.get(PdfName.DR)
            if (dr != null)
                PdfFormField.mergeResources(resources, PdfReader.getPdfObject(dr) as PdfDictionary?)
            val widget = PdfDictionary()
            for (element in merged.keys) {
                if (widgetKeys.containsKey(element))
                    widget.put(element, merged.get(element))
            }
            widget.put(iTextTag, PdfNumber(item.getTabOrder(k)!!.toInt() + 1))
            list.add(widget)
        }
    }

    @SuppressWarnings("unchecked")
    fun mergeField(name: String, item: AcroFields.Item) {
        var map = fieldTree
        val tk = StringTokenizer(name, ".")
        if (!tk.hasMoreTokens())
            return
        while (true) {
            val s = tk.nextToken()
            var obj: Any? = map[s]
            if (tk.hasMoreTokens()) {
                if (obj == null) {
                    obj = HashMap()
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
                        if (fieldKeys.containsKey(element))
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

    fun mergeWithMaster(fd: Map<String, Item>) {
        for (entry in fd.entries) {
            val name = entry.key
            mergeField(name, entry.value)
        }
    }

    internal open fun mergeFields() {
        var pageOffset = 0
        for (k in fields.indices) {
            val fd = fields[k].getFields()
            addPageOffsetToField(fd, pageOffset)
            mergeWithMaster(fd)
            pageOffset += readers[k].numberOfPages
        }
    }

    override fun getPageReference(page: Int): PdfIndirectReference {
        return pageRefs[page - 1]
    }

    override fun getCatalog(rootObj: PdfIndirectReference): PdfDictionary {
        try {
            val cat = pdfDocument.getCatalog(rootObj)
            if (form != null) {
                val ref = addToBody(form).indirectReference
                cat.put(PdfName.ACROFORM, ref)
            }
            return cat
        } catch (e: IOException) {
            throw ExceptionConverter(e)
        }

    }

    protected fun getNewReference(ref: PRIndirectReference): PdfIndirectReference {
        return PdfIndirectReference(0, getNewObjectNumber(ref.reader, ref.number, 0))
    }

    override fun getNewObjectNumber(reader: PdfReader, number: Int, generation: Int): Int {
        val refs = readers2intrefs[reader]
        var n = refs.get(number)
        if (n == 0) {
            n = indirectReferenceNumber
            refs.put(number, n)
        }
        return n
    }


    /**
     * Sets a reference to "visited" in the copy process.
     * @param    ref    the reference that needs to be set to "visited"
     * *
     * @return    true if the reference was set to visited
     */
    protected fun setVisited(ref: PRIndirectReference): Boolean {
        val refs = visited[ref.reader]
        if (refs != null)
            return refs.put(ref.number, 1) != 0
        else
            return false
    }

    /**
     * Checks if a reference has already been "visited" in the copy process.
     * @param    ref    the reference that needs to be checked
     * *
     * @return    true if the reference was already visited
     */
    protected fun isVisited(ref: PRIndirectReference): Boolean {
        val refs = visited[ref.reader]
        if (refs != null)
            return refs.containsKey(ref.number)
        else
            return false
    }

    protected fun isVisited(reader: PdfReader, number: Int, generation: Int): Boolean {
        val refs = readers2intrefs[reader]
        return refs.containsKey(number)
    }

    /**
     * Checks if a reference refers to a page object.
     * @param    ref    the reference that needs to be checked
     * *
     * @return    true is the reference refers to a page object.
     */
    protected fun isPage(ref: PRIndirectReference): Boolean {
        val refs = pages2intrefs[ref.reader]
        if (refs != null)
            return refs.containsKey(ref.number)
        else
            return false
    }

    internal override fun getReaderFile(reader: PdfReader): RandomAccessFileOrArray {
        return file
    }

    fun openDoc() {
        if (!nd.isOpen)
            nd.open()
    }

    companion object {

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
                name = name.substring(0, name.length - 1)
            return name
        }

        protected val widgetKeys = HashMap<PdfName, Int>()
        protected val fieldKeys = HashMap<PdfName, Int>()

        init {
            val one = Integer.valueOf(1)
            widgetKeys.put(PdfName.SUBTYPE, one)
            widgetKeys.put(PdfName.CONTENTS, one)
            widgetKeys.put(PdfName.RECT, one)
            widgetKeys.put(PdfName.NM, one)
            widgetKeys.put(PdfName.M, one)
            widgetKeys.put(PdfName.F, one)
            widgetKeys.put(PdfName.BS, one)
            widgetKeys.put(PdfName.BORDER, one)
            widgetKeys.put(PdfName.AP, one)
            widgetKeys.put(PdfName.AS, one)
            widgetKeys.put(PdfName.C, one)
            widgetKeys.put(PdfName.A, one)
            widgetKeys.put(PdfName.STRUCTPARENT, one)
            widgetKeys.put(PdfName.OC, one)
            widgetKeys.put(PdfName.H, one)
            widgetKeys.put(PdfName.MK, one)
            widgetKeys.put(PdfName.DA, one)
            widgetKeys.put(PdfName.Q, one)
            widgetKeys.put(PdfName.P, one)
            fieldKeys.put(PdfName.AA, one)
            fieldKeys.put(PdfName.FT, one)
            fieldKeys.put(PdfName.TU, one)
            fieldKeys.put(PdfName.TM, one)
            fieldKeys.put(PdfName.FF, one)
            fieldKeys.put(PdfName.V, one)
            fieldKeys.put(PdfName.DV, one)
            fieldKeys.put(PdfName.DS, one)
            fieldKeys.put(PdfName.RV, one)
            fieldKeys.put(PdfName.OPT, one)
            fieldKeys.put(PdfName.MAXLEN, one)
            fieldKeys.put(PdfName.TI, one)
            fieldKeys.put(PdfName.I, one)
            fieldKeys.put(PdfName.LOCK, one)
            fieldKeys.put(PdfName.SV, one)
        }
    }
}
