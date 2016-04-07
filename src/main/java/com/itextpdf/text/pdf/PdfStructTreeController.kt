/*
 * $Id: 0689010dd20beed05f3cce1bc6e3b872d1d02898 $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Bruno Lowagie, Eugene Markovskyi, et al.
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
import com.itextpdf.text.log.LoggerFactory
import com.itextpdf.text.log.Logger

import java.io.IOException

class PdfStructTreeController @Throws(BadPdfFormatException::class)
protected constructor(reader: PdfReader, private val writer: PdfCopy) {

    private var structTreeRoot: PdfDictionary? = null
    private val structureTreeRoot: PdfStructureTreeRoot
    private var parentTree: PdfDictionary? = null
    protected var reader: PdfReader
    private var roleMap: PdfDictionary? = null
    private var sourceRoleMap: PdfDictionary? = null
    private var sourceClassMap: PdfDictionary? = null
    private var nullReference: PdfIndirectReference? = null
    //    private HashSet<Integer> openedDocuments = new HashSet<Integer>();

    enum class returnType {
        BELOW, FOUND, ABOVE, NOTFOUND
    }

    init {
        if (!writer.isTagged)
            throw BadPdfFormatException(MessageLocalization.getComposedMessage("no.structtreeroot.found"))
        structureTreeRoot = writer.getStructureTreeRoot()
        structureTreeRoot.put(PdfName.PARENTTREE, PdfDictionary(PdfName.STRUCTELEM))
        setReader(reader)
    }

    @Throws(BadPdfFormatException::class)
    protected fun setReader(reader: PdfReader) {
        this.reader = reader
        var obj = reader.catalog.get(PdfName.STRUCTTREEROOT)
        obj = getDirectObject(obj)
        if (obj == null || !obj.isDictionary)
            throw BadPdfFormatException(MessageLocalization.getComposedMessage("no.structtreeroot.found"))
        structTreeRoot = obj as PdfDictionary?
        obj = PdfStructTreeController.getDirectObject(structTreeRoot!!.get(PdfName.PARENTTREE))
        if (obj == null || !obj.isDictionary)
            throw BadPdfFormatException(MessageLocalization.getComposedMessage("the.document.does.not.contain.parenttree"))
        parentTree = obj as PdfDictionary?
        sourceRoleMap = null
        sourceClassMap = null
        nullReference = null
    }

    @Throws(BadPdfFormatException::class, IOException::class)
    fun copyStructTreeForPage(sourceArrayNumber: PdfNumber, newArrayNumber: Int) {
        //        int documentHash = getDocumentHash(reader);
        //        if (!openedDocuments.contains(documentHash)) {
        //            openedDocuments.add(documentHash);
        //
        //        }
        if (copyPageMarks(parentTree, sourceArrayNumber, newArrayNumber) == returnType.NOTFOUND) {
            throw BadPdfFormatException(MessageLocalization.getComposedMessage("invalid.structparent"))
        }
    }

    @Throws(BadPdfFormatException::class, IOException::class)
    private fun copyPageMarks(parentTree: PdfDictionary, arrayNumber: PdfNumber, newArrayNumber: Int): returnType {
        val pages = getDirectObject(parentTree.get(PdfName.NUMS)) as PdfArray?
        if (pages == null) {
            val kids = getDirectObject(parentTree.get(PdfName.KIDS)) as PdfArray? ?: return returnType.NOTFOUND
            var cur = kids.size() / 2
            var begin = 0
            while (true) {
                val kidTree = getDirectObject(kids.getPdfObject(cur + begin)) as PdfDictionary?
                when (copyPageMarks(kidTree, arrayNumber, newArrayNumber)) {
                    PdfStructTreeController.returnType.FOUND -> return returnType.FOUND
                    PdfStructTreeController.returnType.ABOVE -> {
                        begin += cur
                        cur /= 2
                        if (cur == 0)
                            cur = 1
                        if (cur + begin == kids.size())
                            return returnType.ABOVE
                    }
                    PdfStructTreeController.returnType.BELOW -> {
                        if (cur + begin == 0)
                            return returnType.BELOW
                        if (cur == 0)
                            return returnType.NOTFOUND
                        cur /= 2
                    }
                    else -> return returnType.NOTFOUND
                }
            }
        } else {
            if (pages.size() == 0)
                return returnType.NOTFOUND
            return findAndCopyMarks(pages, arrayNumber.intValue(), newArrayNumber)
        }
    }

    @Throws(BadPdfFormatException::class, IOException::class)
    private fun findAndCopyMarks(pages: PdfArray, arrayNumber: Int, newArrayNumber: Int): returnType {
        if (pages.getAsNumber(0).intValue() > arrayNumber)
            return returnType.BELOW
        if (pages.getAsNumber(pages.size() - 2).intValue() < arrayNumber)
            return returnType.ABOVE
        var cur = pages.size() / 4
        var begin = 0
        var curNumber: Int

        while (true) {
            curNumber = pages.getAsNumber((begin + cur) * 2).intValue()
            if (curNumber == arrayNumber) {
                var obj = pages.getPdfObject((begin + cur) * 2 + 1)
                val obj1 = obj
                while (obj.isIndirect) obj = PdfReader.getPdfObjectRelease(obj)
                if (obj.isArray) {
                    var firstNotNullKid: PdfObject? = null
                    for (numObj in obj as PdfArray) {
                        if (numObj.isNull) {
                            if (nullReference == null)
                                nullReference = writer.addToBody(PdfNull()).indirectReference
                            structureTreeRoot.setPageMark(newArrayNumber, nullReference)
                        } else {
                            val res = writer.copyObject(numObj, true, false)
                            if (firstNotNullKid == null) firstNotNullKid = res
                            structureTreeRoot.setPageMark(newArrayNumber, res as PdfIndirectReference)
                        }
                    }
                    attachStructTreeRootKids(firstNotNullKid)
                } else if (obj.isDictionary) {
                    val k = getKDict(obj as PdfDictionary) ?: return returnType.NOTFOUND
                    val res = writer.copyObject(obj1, true, false)
                    structureTreeRoot.setAnnotationMark(newArrayNumber, res as PdfIndirectReference)
                } else
                    return returnType.NOTFOUND
                return returnType.FOUND
            }
            if (curNumber < arrayNumber) {
                if (cur == 0)
                    return returnType.NOTFOUND
                begin += cur
                if (cur != 1)
                    cur /= 2
                if (cur + begin == pages.size())
                    return returnType.NOTFOUND
                continue
            }
            if (cur + begin == 0)
                return returnType.BELOW
            if (cur == 0)
                return returnType.NOTFOUND
            cur /= 2
        }
    }

    /**
     * Add kid to structureTreeRoot from structTreeRoot
     */
    @Throws(IOException::class, BadPdfFormatException::class)
    protected fun attachStructTreeRootKids(firstNotNullKid: PdfObject) {
        val structKids = structTreeRoot!!.get(PdfName.K)
        if (structKids == null || !structKids.isArray && !structKids.isIndirect) {
            // incorrect syntax of tags
            addKid(structureTreeRoot, firstNotNullKid)
        } else {
            if (structKids.isIndirect) {
                addKid(structKids)
            } else {
                //structKids.isArray()
                for (kid in structKids as PdfArray?)
                    addKid(kid)
            }
        }
    }

    @Throws(IOException::class, BadPdfFormatException::class)
    private fun addKid(obj: PdfObject) {
        if (!obj.isIndirect) return
        val currRef = obj as PRIndirectReference
        val key = RefKey(currRef)
        if (!writer.indirects!!.containsKey(key)) {
            writer.copyIndirect(currRef, true, false)
        }
        val newKid = writer.indirects!![key].ref

        if (writer.updateRootKids) {
            addKid(structureTreeRoot, newKid)
            writer.structureTreeRootKidsForReaderImported(reader)
        }
    }

    @Throws(BadPdfFormatException::class)
    protected fun addClass(`object`: PdfObject?) {
        var `object` = `object`
        `object` = getDirectObject(`object`)
        if (`object`!!.isDictionary) {
            val curClass = (`object` as PdfDictionary).get(PdfName.C) ?: return
            if (curClass.isArray) {
                val array = curClass as PdfArray?
                for (i in 0..array.size() - 1) {
                    addClass(array.getPdfObject(i))
                }
            } else if (curClass.isName)
                addClass(curClass)
        } else if (`object`.isName) {
            val name = `object` as PdfName?
            if (sourceClassMap == null) {
                `object` = getDirectObject(structTreeRoot!!.get(PdfName.CLASSMAP))
                if (`object` == null || !`object`.isDictionary) {
                    return
                }
                sourceClassMap = `object` as PdfDictionary?
            }
            `object` = getDirectObject(sourceClassMap!!.get(name))
            if (`object` == null) {
                return
            }
            val put = structureTreeRoot.getMappedClass(name)
            if (put != null) {
                if (!compareObjects(put, `object`)) {
                    throw BadPdfFormatException(MessageLocalization.getComposedMessage("conflict.in.classmap", name))
                }
            } else {
                if (`object`.isDictionary)
                    structureTreeRoot.mapClass(name, getDirectDict(`object` as PdfDictionary?))
                else if (`object`.isArray) {
                    structureTreeRoot.mapClass(name, getDirectArray(`object` as PdfArray?))
                }
            }
        }
    }

    @Throws(BadPdfFormatException::class)
    protected fun addRole(structType: PdfName?) {
        if (structType == null) {
            return
        }
        for (name in writer.standardStructElems) {
            if (name == structType)
                return
        }
        if (sourceRoleMap == null) {
            val `object` = getDirectObject(structTreeRoot!!.get(PdfName.ROLEMAP))
            if (`object` == null || !`object`.isDictionary) {
                return
            }
            sourceRoleMap = `object` as PdfDictionary?
        }
        val `object` = sourceRoleMap!!.get(structType)
        if (`object` == null || !`object`.isName) {
            return
        }
        val currentRole: PdfObject
        if (roleMap == null) {
            roleMap = PdfDictionary()
            structureTreeRoot.put(PdfName.ROLEMAP, roleMap)
            roleMap!!.put(structType, `object`)
        } else if ((currentRole = roleMap!!.get(structType)) != null) {
            if (currentRole != `object`) {
                throw BadPdfFormatException(MessageLocalization.getComposedMessage("conflict.in.rolemap", `object`))
            }
        } else {
            roleMap!!.put(structType, `object`)
        }
    }

    protected fun addKid(parent: PdfDictionary, kid: PdfObject) {
        val kidObj = parent.get(PdfName.K)
        val kids: PdfArray
        if (kidObj is PdfArray) {
            kids = kidObj as PdfArray?
        } else {
            kids = PdfArray()
            if (kidObj != null)
                kids.add(kidObj)
        }
        kids.add(kid)
        parent.put(PdfName.K, kids)
    }

    companion object {

        fun checkTagged(reader: PdfReader): Boolean {
            var obj = reader.catalog.get(PdfName.STRUCTTREEROOT)
            obj = getDirectObject(obj)
            if (obj == null || !obj.isDictionary)
                return false
            val structTreeRoot = obj as PdfDictionary?
            obj = PdfStructTreeController.getDirectObject(structTreeRoot.get(PdfName.PARENTTREE))
            if (obj == null || !obj.isDictionary)
                return false
            return true
        }

        fun getDirectObject(`object`: PdfObject?): PdfObject? {
            var `object`: PdfObject? = `object` ?: return null
            while (`object`!!.isIndirect)
                `object` = PdfReader.getPdfObjectRelease(`object`)
            return `object`
        }

        internal fun getKDict(obj: PdfDictionary): PdfDictionary? {
            var k: PdfDictionary? = obj.getAsDict(PdfName.K)
            if (k != null) {
                if (PdfName.OBJR == k.getAsName(PdfName.TYPE)) {
                    return k
                }
            } else {
                val k1 = obj.getAsArray(PdfName.K) ?: return null
                for (i in 0..k1.size() - 1) {
                    k = k1.getAsDict(i)
                    if (k != null) {
                        if (PdfName.OBJR == k.getAsName(PdfName.TYPE)) {
                            return k
                        }
                    }
                }
            }
            return null
        }

        private fun getDirectArray(`in`: PdfArray): PdfArray {
            val out = PdfArray()
            for (i in 0..`in`.size() - 1) {
                val value = getDirectObject(`in`.getPdfObject(i)) ?: continue
                if (value.isArray) {
                    out.add(getDirectArray(value as PdfArray?))
                } else if (value.isDictionary) {
                    out.add(getDirectDict(value as PdfDictionary?))
                } else {
                    out.add(value)
                }
            }
            return out
        }

        private fun getDirectDict(`in`: PdfDictionary): PdfDictionary {
            val out = PdfDictionary()
            for (entry in `in`.hashMap.entries) {
                val value = getDirectObject(entry.value) ?: continue
                if (value.isArray) {
                    out.put(entry.key, getDirectArray(value as PdfArray?))
                } else if (value.isDictionary) {
                    out.put(entry.key, getDirectDict(value as PdfDictionary?))
                } else {
                    out.put(entry.key, value)
                }
            }
            return out
        }

        fun compareObjects(value1: PdfObject, value2: PdfObject?): Boolean {
            var value2 = value2
            value2 = getDirectObject(value2)
            if (value2 == null)
                return false
            if (value1.type() != value2.type())
                return false

            if (value1.isBoolean) {
                if (value1 === value2)
                    return true
                if (value2 is PdfBoolean) {
                    return (value1 as PdfBoolean).booleanValue() == value2.booleanValue()
                }
                return false
            } else if (value1.isName) {
                return value1 == value2
            } else if (value1.isNumber) {
                if (value1 === value2)
                    return true
                if (value2 is PdfNumber) {
                    return (value1 as PdfNumber).doubleValue() == value2.doubleValue()
                }
                return false
            } else if (value1.isNull) {
                if (value1 === value2)
                    return true
                if (value2 is PdfNull)
                    return true
                return false
            } else if (value1.isString) {
                if (value1 === value2)
                    return true
                if (value2 is PdfString) {
                    return value2.value == null && (value1 as PdfString).value == null || (value1 as PdfString).value != null && value1.value == value2.value
                }
                return false
            }
            if (value1.isArray) {
                val array1 = value1 as PdfArray
                val array2 = value2 as PdfArray?
                if (array1.size() != array2.size())
                    return false
                for (i in 0..array1.size() - 1)
                    if (!compareObjects(array1.getPdfObject(i), array2.getPdfObject(i)))
                        return false
                return true
            }
            if (value1.isDictionary) {
                val first = value1 as PdfDictionary
                val second = value2 as PdfDictionary?
                if (first.size() != second.size())
                    return false
                for (name in first.hashMap.keys) {
                    if (!compareObjects(first.get(name), second.get(name)))
                        return false
                }
                return true
            }
            return false
        }
    }

    //    private int getDocumentHash(final PdfReader reader) {
    //        PdfDictionary trailer = reader.trailer;
    //        int hash = trailer.size();
    //        HashMap<String, String> info = reader.getInfo();
    //        PdfArray id = trailer.getAsArray(PdfName.ID);
    //        if (id != null) {
    //            for (PdfObject idPart : id) {
    //                if (idPart instanceof PdfString) {
    //                    hash = hash ^ ((PdfString)idPart).toUnicodeString().hashCode();
    //                }
    //            }
    //        }
    //        for (String key : info.keySet()) {
    //            String value = info.get(key);
    //            if (value != null) {
    //                hash = hash ^ key.hashCode() ^ value.hashCode();
    //            }
    //        }
    //        return hash;
    //    }

}
