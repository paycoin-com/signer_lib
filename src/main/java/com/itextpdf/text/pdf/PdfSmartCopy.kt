/*
 * $Id: 0982475d5d360d1db07d64d7281991007e5faf2f $
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
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.log.Counter
import com.itextpdf.text.log.CounterFactory
import com.itextpdf.text.log.Logger
import com.itextpdf.text.log.LoggerFactory

import java.io.IOException
import java.io.OutputStream
import java.security.MessageDigest
import java.util.Arrays
import java.util.HashMap

/**
 * PdfSmartCopy has the same functionality as PdfCopy,
 * but when resources (such as fonts, images,...) are
 * encountered, a reference to these resources is saved
 * in a cache, so that they can be reused.
 * This requires more memory, but reduces the file size
 * of the resulting PDF document.
 */

class PdfSmartCopy
/** Creates a PdfSmartCopy instance.  */
@Throws(DocumentException::class)
constructor(document: Document, os: OutputStream) : PdfCopy(document, os) {

    /** the cache with the streams and references.  */
    private val streamMap: HashMap<ByteStore, PdfIndirectReference>? = null
    private val serialized = HashMap<RefKey, Int>()

    protected override var counter = CounterFactory.getCounter(PdfSmartCopy::class.java)

    init {
        this.streamMap = HashMap<ByteStore, PdfIndirectReference>()
    }

    /**
     * Translate a PRIndirectReference to a PdfIndirectReference
     * In addition, translates the object numbers, and copies the
     * referenced object to the output file if it wasn't available
     * in the cache yet. If it's in the cache, the reference to
     * the already used stream is returned.

     * NB: PRIndirectReferences (and PRIndirectObjects) really need to know what
     * file they came from, because each file has its own namespace. The translation
     * we do from their namespace to ours is *at best* heuristic, and guaranteed to
     * fail under some circumstances.
     */
    @Throws(IOException::class, BadPdfFormatException::class)
    override fun copyIndirect(`in`: PRIndirectReference): PdfIndirectReference? {
        val srcObj = PdfReader.getPdfObjectRelease(`in`)
        var streamKey: ByteStore? = null
        var validStream = false
        if (srcObj.isStream) {
            streamKey = ByteStore(srcObj as PRStream, serialized)
            validStream = true
            val streamRef = streamMap!![streamKey]
            if (streamRef != null) {
                return streamRef
            }
        } else if (srcObj.isDictionary) {
            streamKey = ByteStore(srcObj as PdfDictionary, serialized)
            validStream = true
            val streamRef = streamMap!![streamKey]
            if (streamRef != null) {
                return streamRef
            }
        }

        val theRef: PdfIndirectReference
        val key = RefKey(`in`)
        var iRef: PdfCopy.IndirectReferences? = indirects!![key]
        if (iRef != null) {
            theRef = iRef.ref
            if (iRef.copied) {
                return theRef
            }
        } else {
            theRef = body.pdfIndirectReference
            iRef = PdfCopy.IndirectReferences(theRef)
            indirects!!.put(key, iRef)
        }
        if (srcObj.isDictionary) {
            val type = PdfReader.getPdfObjectRelease((srcObj as PdfDictionary).get(PdfName.TYPE))
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

        if (validStream) {
            streamMap!!.put(streamKey, theRef)
        }

        val obj = copyObject(srcObj)
        addToBody(obj, theRef)
        return theRef
    }

    @Throws(IOException::class)
    override fun freeReader(reader: PdfReader) {
        serialized.clear()
        super.freeReader(reader)
    }

    @Throws(IOException::class, BadPdfFormatException::class)
    override fun addPage(iPage: PdfImportedPage) {
        if (currentPdfReaderInstance!!.reader !== reader)
            serialized.clear()
        super.addPage(iPage)
    }

    internal class ByteStore {
        private val b: ByteArray
        private val hash: Int
        private var md5: MessageDigest? = null

        @Throws(IOException::class)
        private fun serObject(obj: PdfObject?, level: Int, bb: ByteBuffer, serialized: HashMap<RefKey, Int>) {
            var obj = obj
            var bb = bb
            if (level <= 0)
                return
            if (obj == null) {
                bb.append("$Lnull")
                return
            }
            var ref: PdfIndirectReference? = null
            var savedBb: ByteBuffer? = null

            if (obj.isIndirect) {
                ref = obj as PdfIndirectReference?
                val key = RefKey(ref)
                if (serialized.containsKey(key)) {
                    bb.append(serialized[key])
                    return
                } else {
                    savedBb = bb
                    bb = ByteBuffer()
                }
            }
            obj = PdfReader.getPdfObject(obj)
            if (obj!!.isStream) {
                bb.append("$B")
                serDic(obj as PdfDictionary?, level - 1, bb, serialized)
                if (level > 0) {
                    md5!!.reset()
                    bb.append(md5!!.digest(PdfReader.getStreamBytesRaw(obj as PRStream?)))
                }
            } else if (obj.isDictionary) {
                serDic(obj as PdfDictionary?, level - 1, bb, serialized)
            } else if (obj.isArray) {
                serArray(obj as PdfArray?, level - 1, bb, serialized)
            } else if (obj.isString) {
                bb.append("$S").append(obj.toString())
            } else if (obj.isName) {
                bb.append("$N").append(obj.toString())
            } else
                bb.append("$L").append(obj.toString())

            if (savedBb != null) {
                val key = RefKey(ref)
                if (!serialized.containsKey(key))
                    serialized.put(key, calculateHash(bb.buffer))
                savedBb.append(bb)
            }
        }

        @Throws(IOException::class)
        private fun serDic(dic: PdfDictionary, level: Int, bb: ByteBuffer, serialized: HashMap<RefKey, Int>) {
            bb.append("$D")
            if (level <= 0)
                return
            val keys = dic.keys.toArray()
            Arrays.sort(keys)
            for (k in keys.indices) {
                if (keys[k] == PdfName.P && (dic.get(keys[k] as PdfName)!!.isIndirect || dic.get(keys[k] as PdfName)!!.isDictionary))
                // ignore recursive call
                    continue
                serObject(keys[k] as PdfObject, level, bb, serialized)
                serObject(dic.get(keys[k] as PdfName), level, bb, serialized)

            }
        }

        @Throws(IOException::class)
        private fun serArray(array: PdfArray, level: Int, bb: ByteBuffer, serialized: HashMap<RefKey, Int>) {
            bb.append("$A")
            if (level <= 0)
                return
            for (k in 0..array.size() - 1) {
                serObject(array.getPdfObject(k), level, bb, serialized)
            }
        }

        @Throws(IOException::class)
        constructor(str: PRStream, serialized: HashMap<RefKey, Int>) {
            try {
                md5 = MessageDigest.getInstance("MD5")
            } catch (e: Exception) {
                throw ExceptionConverter(e)
            }

            val bb = ByteBuffer()
            val level = 100
            serObject(str, level, bb, serialized)
            this.b = bb.toByteArray()
            hash = calculateHash(this.b)
            md5 = null
        }

        @Throws(IOException::class)
        constructor(dict: PdfDictionary, serialized: HashMap<RefKey, Int>) {
            try {
                md5 = MessageDigest.getInstance("MD5")
            } catch (e: Exception) {
                throw ExceptionConverter(e)
            }

            val bb = ByteBuffer()
            val level = 100
            serObject(dict, level, bb, serialized)
            this.b = bb.toByteArray()
            hash = calculateHash(this.b)
            md5 = null
        }

        private fun calculateHash(b: ByteArray): Int {
            var hash = 0
            val len = b.size
            for (k in 0..len - 1)
                hash = hash * 31 + (b[k] and 0xff)
            return hash
        }

        override fun equals(obj: Any?): Boolean {
            if (obj !is ByteStore)
                return false
            if (hashCode() != obj.hashCode())
                return false
            return Arrays.equals(b, obj.b)
        }

        override fun hashCode(): Int {
            return hash
        }
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(PdfSmartCopy::class.java)
    }
}
