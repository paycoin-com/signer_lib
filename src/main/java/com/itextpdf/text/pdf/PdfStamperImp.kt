/*
 * $Id: 8dd316884a91e0d64ba596d311bbfdaf3c86da63 $
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

import com.itextpdf.awt.geom.Point
import com.itextpdf.text.DocumentException
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.Version
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.exceptions.BadPasswordException
import com.itextpdf.text.log.Counter
import com.itextpdf.text.log.CounterFactory
import com.itextpdf.text.pdf.AcroFields.Item
import com.itextpdf.text.pdf.collection.PdfCollection
import com.itextpdf.text.pdf.interfaces.PdfViewerPreferences
import com.itextpdf.text.pdf.internal.PdfIsoKeys
import com.itextpdf.text.pdf.internal.PdfViewerPreferencesImp
import com.itextpdf.text.xml.xmp.PdfProperties
import com.itextpdf.text.xml.xmp.XmpBasicProperties
import com.itextpdf.text.xml.xmp.XmpWriter
import com.itextpdf.xmp.XMPException
import com.itextpdf.xmp.XMPMeta
import com.itextpdf.xmp.XMPMetaFactory
import com.itextpdf.xmp.options.SerializeOptions

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.HashMap
import java.util.HashSet

internal class PdfStamperImp
/**
 * Creates new PdfStamperImp.

 * @param reader     the read PDF
 * *
 * @param os         the output destination
 * *
 * @param pdfVersion the new pdf version or '\0' to keep the same version as the original
 * *                   document
 * *
 * @param append
 * *
 * @throws DocumentException on error
 * *
 * @throws IOException
 */
@Throws(DocumentException::class, IOException::class)
protected constructor(var pdfReader: PdfReader, os: OutputStream, pdfVersion: Char, append: Boolean) : PdfWriter(PdfDocument(), os) {
    var readers2intrefs = HashMap<PdfReader, IntHashtable>()
    var readers2file = HashMap<PdfReader, RandomAccessFileOrArray>()
    protected var file: RandomAccessFileOrArray
    var myXref = IntHashtable()
    /**
     * Integer(page number) -> PageStamp
     */
    var pagesToContent = HashMap<PdfDictionary, PageStamp>()
    protected var closed = false
    /**
     * Holds value of property rotateContents.
     */
    /**
     * Getter for property rotateContents.

     * @return Value of property rotateContents.
     */
    /**
     * Setter for property rotateContents.

     * @param rotateContents New value of property rotateContents.
     */
    var isRotateContents = true
    protected var acroFields: AcroFields? = null
    protected var flat = false
    protected var flatFreeText = false
    protected var flatannotations = false
    protected var namePtr = intArrayOf(0)
    protected var partialFlattening = HashSet<String>()
    protected var useVp = false
    protected var viewerPreferences = PdfViewerPreferencesImp()
    protected var fieldTemplates = HashSet<PdfTemplate>()
    protected var fieldsAdded = false
    protected var sigFlags = 0
    /**
     * Getter for property append.

     * @return Value of property append.
     */
    var isAppend: Boolean = false
        protected set
    protected var marked: IntHashtable
    protected var initialXrefSize: Int = 0
    protected var openAction: PdfAction? = null
    protected var namedDestinations = HashMap<Any, PdfObject>()

    protected override var counter = CounterFactory.getCounter(PdfStamper::class.java)

    /* Flag which defines if PdfLayer objects from existing pdf have been already read.
     * If no new layers were registered and user didn't fetched layers explicitly via getPdfLayers() method
     * then original layers are never read - they are simply copied to the new document with whole original catalog. */
    private var originalLayersAreRead = false

    private val DEFAULT_MATRIX = doubleArrayOf(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)

    init {
        if (!pdfReader.isOpenedWithFullPermissions)
            throw BadPasswordException(MessageLocalization.getComposedMessage("pdfreader.not.opened.with.owner.password"))
        if (pdfReader.isTampered)
            throw DocumentException(MessageLocalization.getComposedMessage("the.original.document.was.reused.read.it.again.from.file"))
        pdfReader.isTampered = true
        file = pdfReader.safeFile
        this.isAppend = append
        if (pdfReader.isEncrypted && (append || PdfReader.unethicalreading)) {
            encryption = PdfEncryption(pdfReader.decrypt)
        }
        if (append) {
            if (pdfReader.isRebuilt)
                throw DocumentException(MessageLocalization.getComposedMessage("append.mode.requires.a.document.without.errors.even.if.recovery.was.possible"))
            this.pdfVersion.setAppendmode(true)
            if (pdfVersion.toInt() == 0) {
                this.pdfVersion.setPdfVersion(pdfReader.pdfVersion)
            } else {
                this.pdfVersion.setPdfVersion(pdfVersion)
            }
            val buf = ByteArray(8192)
            var n: Int
            while ((n = file.read(buf)) > 0)
                this.os.write(buf, 0, n)
            prevxref = pdfReader.lastXref
            pdfReader.isAppendable = true
        } else {
            if (pdfVersion.toInt() == 0)
                super.setPdfVersion(pdfReader.pdfVersion)
            else
                super.setPdfVersion(pdfVersion)
        }

        if (pdfReader.isTagged) {
            this.setTagged()
        }

        super.open()
        pdfDocument.addWriter(this)
        if (append) {
            body.setRefnum(pdfReader.xrefSize)
            marked = IntHashtable()
            if (pdfReader.isNewXrefType)
                isFullCompression = true
            if (pdfReader.isHybridXref)
                isFullCompression = false
        }
        initialXrefSize = pdfReader.xrefSize
        readColorProfile()
    }

    protected fun readColorProfile() {
        val outputIntents = pdfReader.catalog.getAsArray(PdfName.OUTPUTINTENTS)
        if (outputIntents != null && (outputIntents as PdfArray).size() > 0) {
            var iccProfileStream: PdfStream? = null
            for (i in 0..outputIntents.size() - 1) {
                val outputIntentDictionary = outputIntents.getAsDict(i)
                if (outputIntentDictionary != null) {
                    iccProfileStream = outputIntentDictionary.getAsStream(PdfName.DESTOUTPUTPROFILE)
                    if (iccProfileStream != null)
                        break
                }
            }

            if (iccProfileStream is PRStream) {
                try {
                    colorProfile = ICC_Profile.getInstance(PdfReader.getStreamBytes(iccProfileStream as PRStream?))
                } catch (exc: IOException) {
                    throw ExceptionConverter(exc)
                }

            }
        }
    }

    protected fun setViewerPreferences() {
        pdfReader.setViewerPreferences(viewerPreferences)
        markUsed(pdfReader.trailer.get(PdfName.ROOT))
    }

    @Throws(IOException::class)
    protected fun close(moreInfo: Map<String, String>?) {
        if (closed) {
            return
        }
        if (useVp) {
            setViewerPreferences()
        }
        if (flat) {
            flatFields()
        }
        if (flatFreeText) {
            flatFreeTextFields()
        }
        if (flatannotations) {
            flattenAnnotations()
        }
        addFieldResources()
        val catalog = pdfReader.catalog
        pdfVersion.addToCatalog(catalog)
        val acroForm = PdfReader.getPdfObject(catalog.get(PdfName.ACROFORM), pdfReader.catalog) as PdfDictionary?
        if (acroFields != null && acroFields!!.xfa!!.isChanged) {
            markUsed(acroForm)
            if (!flat) {
                acroFields!!.xfa!!.setXfa(this)
            }
        }
        if (sigFlags != 0) {
            if (acroForm != null) {
                acroForm.put(PdfName.SIGFLAGS, PdfNumber(sigFlags))
                markUsed(acroForm)
                markUsed(catalog)
            }
        }
        closed = true
        addSharedObjectsToBody()
        setOutlines()
        setJavaScript()
        addFileAttachments()
        // [C11] Output Intents
        if (extraCatalog != null) {
            catalog.mergeDifferent(extraCatalog)
        }
        if (openAction != null) {
            catalog.put(PdfName.OPENACTION, openAction)
        }
        if (pdfDocument.pageLabels != null) {
            catalog.put(PdfName.PAGELABELS, pdfDocument.pageLabels!!.getDictionary(this))
        }
        // OCG
        if (!documentOCG.isEmpty()) {
            fillOCProperties(false)
            val ocdict = catalog.getAsDict(PdfName.OCPROPERTIES)
            if (ocdict == null) {
                pdfReader.catalog.put(PdfName.OCPROPERTIES, OCProperties)
            } else {
                ocdict.put(PdfName.OCGS, OCProperties!!.get(PdfName.OCGS))
                var ddict: PdfDictionary? = ocdict.getAsDict(PdfName.D)
                if (ddict == null) {
                    ddict = PdfDictionary()
                    ocdict.put(PdfName.D, ddict)
                }
                ddict.put(PdfName.ORDER, OCProperties!!.getAsDict(PdfName.D).get(PdfName.ORDER))
                ddict.put(PdfName.RBGROUPS, OCProperties!!.getAsDict(PdfName.D).get(PdfName.RBGROUPS))
                ddict.put(PdfName.OFF, OCProperties!!.getAsDict(PdfName.D).get(PdfName.OFF))
                ddict.put(PdfName.AS, OCProperties!!.getAsDict(PdfName.D).get(PdfName.AS))
            }
            PdfWriter.checkPdfIsoConformance(this, PdfIsoKeys.PDFISOKEY_LAYER, OCProperties)
        }
        // metadata
        var skipInfo = -1
        val iInfo = pdfReader.trailer.getAsIndirectObject(PdfName.INFO)
        if (iInfo != null) {
            skipInfo = iInfo.number
        }
        val oldInfo = pdfReader.trailer.getAsDict(PdfName.INFO)
        var producer: String? = null
        if (oldInfo != null && oldInfo.get(PdfName.PRODUCER) != null) {
            producer = oldInfo.getAsString(PdfName.PRODUCER).toUnicodeString()
        }
        val version = Version.getInstance()
        if (producer == null || version.version.indexOf(version.product) == -1) {
            producer = version.version
        } else {
            val idx = producer.indexOf("; modified using")
            val buf: StringBuffer
            if (idx == -1)
                buf = StringBuffer(producer)
            else
                buf = StringBuffer(producer.substring(0, idx))
            buf.append("; modified using ")
            buf.append(version.version)
            producer = buf.toString()
        }
        var info: PdfIndirectReference? = null
        val newInfo = PdfDictionary()
        if (oldInfo != null) {
            for (element in oldInfo.keys) {
                val value = PdfReader.getPdfObject(oldInfo.get(element))
                newInfo.put(element, value)
            }
        }
        if (moreInfo != null) {
            for ((key, value) in moreInfo) {
                val keyName = PdfName(key)
                if (value == null)
                    newInfo.remove(keyName)
                else
                    newInfo.put(keyName, PdfString(value, PdfObject.TEXT_UNICODE))
            }
        }
        val date = PdfDate()
        newInfo.put(PdfName.MODDATE, date)
        newInfo.put(PdfName.PRODUCER, PdfString(producer, PdfObject.TEXT_UNICODE))
        if (isAppend) {
            if (iInfo == null) {
                info = addToBody(newInfo, false).indirectReference
            } else {
                info = addToBody(newInfo, iInfo.number, false).indirectReference
            }
        } else {
            info = addToBody(newInfo, false).indirectReference
        }
        // XMP
        var altMetadata: ByteArray? = null
        val xmpo = PdfReader.getPdfObject(catalog.get(PdfName.METADATA))
        if (xmpo != null && xmpo.isStream) {
            altMetadata = PdfReader.getStreamBytesRaw(xmpo as PRStream?)
            PdfReader.killIndirect(catalog.get(PdfName.METADATA))
        }
        var xmp: PdfStream? = null
        if (xmpMetadata != null) {
            altMetadata = xmpMetadata
        } else if (xmpWriter != null) {
            try {
                val baos = ByteArrayOutputStream()
                PdfProperties.setProducer(xmpWriter!!.xmpMeta, producer)
                XmpBasicProperties.setModDate(xmpWriter!!.xmpMeta, date.w3CDate)
                XmpBasicProperties.setMetaDataDate(xmpWriter!!.xmpMeta, date.w3CDate)
                xmpWriter!!.serialize(baos)
                xmpWriter!!.close()
                xmp = PdfStream(baos.toByteArray())
            } catch (exc: XMPException) {
                xmpWriter = null
            }

        }
        if (xmp == null && altMetadata != null) {
            try {
                val baos = ByteArrayOutputStream()
                if (moreInfo == null || xmpMetadata != null) {
                    val xmpMeta = XMPMetaFactory.parseFromBuffer(altMetadata)

                    PdfProperties.setProducer(xmpMeta, producer)
                    XmpBasicProperties.setModDate(xmpMeta, date.w3CDate)
                    XmpBasicProperties.setMetaDataDate(xmpMeta, date.w3CDate)

                    val serializeOptions = SerializeOptions()
                    serializeOptions.padding = 2000
                    XMPMetaFactory.serialize(xmpMeta, baos, serializeOptions)
                } else {
                    val xmpw = createXmpWriter(baos, newInfo)
                    xmpw.close()
                }
                xmp = PdfStream(baos.toByteArray())
            } catch (e: XMPException) {
                xmp = PdfStream(altMetadata)
            } catch (e: IOException) {
                xmp = PdfStream(altMetadata)
            }

        }
        if (xmp != null) {
            xmp.put(PdfName.TYPE, PdfName.METADATA)
            xmp.put(PdfName.SUBTYPE, PdfName.XML)
            if (encryption != null && !encryption!!.isMetadataEncrypted) {
                val ar = PdfArray()
                ar.add(PdfName.CRYPT)
                xmp.put(PdfName.FILTER, ar)
            }
            if (isAppend && xmpo != null) {
                body.add(xmp, xmpo.indRef)
            } else {
                catalog.put(PdfName.METADATA, body.add(xmp).indirectReference)
                markUsed(catalog)
            }
        }

        if (!namedDestinations.isEmpty())
            updateNamedDestinations()
        close(info, skipInfo)
    }

    @Throws(IOException::class)
    protected fun close(info: PdfIndirectReference, skipInfo: Int) {
        alterContents()
        val rootN = (pdfReader.trailer!!.get(PdfName.ROOT) as PRIndirectReference).number
        if (isAppend) {
            val keys = marked.keys
            for (k in keys.indices) {
                val j = keys[k]
                val obj = pdfReader.getPdfObjectRelease(j)
                if (obj != null && skipInfo != j && j < initialXrefSize) {
                    addToBody(obj, obj.indRef, j != rootN)
                }
            }
            for (k in initialXrefSize..pdfReader.xrefSize - 1) {
                val obj = pdfReader.getPdfObject(k)
                if (obj != null) {
                    addToBody(obj, getNewObjectNumber(pdfReader, k, 0))
                }
            }
        } else {
            for (k in 1..pdfReader.xrefSize - 1) {
                val obj = pdfReader.getPdfObjectRelease(k)
                if (obj != null && skipInfo != k) {
                    addToBody(obj, getNewObjectNumber(pdfReader, k, 0), k != rootN)
                }
            }
        }

        var encryption: PdfIndirectReference? = null
        var fileID: PdfObject? = null
        if (this.encryption != null) {
            if (isAppend) {
                encryption = pdfReader.cryptoRef
            } else {
                val encryptionObject = addToBody(this.encryption!!.encryptionDictionary, false)
                encryption = encryptionObject.indirectReference
            }
            fileID = this.encryption!!.getFileID(true)
        } else {
            val IDs = pdfReader.trailer!!.getAsArray(PdfName.ID)
            if (IDs != null && IDs.getAsString(0) != null) {
                fileID = PdfEncryption.createInfoId(IDs.getAsString(0).bytes, true)
            } else {
                fileID = PdfEncryption.createInfoId(PdfEncryption.createDocumentId(), true)
            }
        }
        val iRoot = pdfReader.trailer!!.get(PdfName.ROOT) as PRIndirectReference?
        val root = PdfIndirectReference(0, getNewObjectNumber(pdfReader, iRoot.number, 0))
        // write the cross-reference table of the body
        body.writeCrossReferenceTable(os, root, info, encryption, fileID, prevxref)
        if (isFullCompression) {
            PdfWriter.writeKeyInfo(os)
            os.write(DocWriter.getISOBytes("startxref\n"))
            os.write(DocWriter.getISOBytes(body.offset().toString()))
            os.write(DocWriter.getISOBytes("\n%%EOF\n"))
        } else {
            val trailer = PdfWriter.PdfTrailer(body.size(),
                    body.offset(),
                    root,
                    info,
                    encryption,
                    fileID, prevxref)
            trailer.toPdf(this, os)
        }
        os.flush()
        if (isCloseStream)
            os.close()
        counter.written(os.counter)
    }

    fun applyRotation(pageN: PdfDictionary, out: ByteBuffer) {
        if (!isRotateContents)
            return
        val page = pdfReader.getPageSizeWithRotation(pageN)
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

    @Throws(IOException::class)
    protected fun alterContents() {
        for (element in pagesToContent.values) {
            val pageN = element.pageN
            markUsed(pageN)
            var ar: PdfArray? = null
            val content = PdfReader.getPdfObject(pageN.get(PdfName.CONTENTS), pageN)
            if (content == null) {
                ar = PdfArray()
                pageN.put(PdfName.CONTENTS, ar)
            } else if (content.isArray) {
                ar = PdfArray(content as PdfArray?)
                pageN.put(PdfName.CONTENTS, ar)
            } else if (content.isStream) {
                ar = PdfArray()
                ar.add(pageN.get(PdfName.CONTENTS))
                pageN.put(PdfName.CONTENTS, ar)
            } else {
                ar = PdfArray()
                pageN.put(PdfName.CONTENTS, ar)
            }
            val out = ByteBuffer()
            if (element.under != null) {
                out.append(PdfContents.SAVESTATE)
                applyRotation(pageN, out)
                out.append(element.under!!.internalBuffer)
                out.append(PdfContents.RESTORESTATE)
            }
            if (element.over != null)
                out.append(PdfContents.SAVESTATE)
            var stream = PdfStream(out.toByteArray())
            stream.flateCompress(compressionLevel)
            ar.addFirst(addToBody(stream).indirectReference)
            out.reset()
            if (element.over != null) {
                out.append(' ')
                out.append(PdfContents.RESTORESTATE)
                val buf = element.over!!.internalBuffer
                out.append(buf.buffer, 0, element.replacePoint)
                out.append(PdfContents.SAVESTATE)
                applyRotation(pageN, out)
                out.append(buf.buffer, element.replacePoint, buf.size() - element.replacePoint)
                out.append(PdfContents.RESTORESTATE)
                stream = PdfStream(out.toByteArray())
                stream.flateCompress(compressionLevel)
                ar.add(addToBody(stream).indirectReference)
            }
            alterResources(element)
        }
    }

    fun alterResources(ps: PageStamp) {
        ps.pageN.put(PdfName.RESOURCES, ps.pageResources.resources)
    }

    override fun getNewObjectNumber(reader: PdfReader, number: Int, generation: Int): Int {
        val ref = readers2intrefs[reader]
        if (ref != null) {
            var n = ref.get(number)
            if (n == 0) {
                n = indirectReferenceNumber
                ref.put(number, n)
            }
            return n
        }
        if (currentPdfReaderInstance == null) {
            if (isAppend && number < initialXrefSize)
                return number
            var n = myXref.get(number)
            if (n == 0) {
                n = indirectReferenceNumber
                myXref.put(number, n)
            }
            return n
        } else
            return currentPdfReaderInstance!!.getNewObjectNumber(number, generation)
    }

    internal override fun getReaderFile(reader: PdfReader): RandomAccessFileOrArray {
        if (readers2intrefs.containsKey(reader)) {
            val raf = readers2file[reader]
            if (raf != null)
                return raf
            return reader.safeFile
        }
        if (currentPdfReaderInstance == null)
            return file
        else
            return currentPdfReaderInstance!!.readerFile
    }

    /**
     * @param reader
     * *
     * @param openFile
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun registerReader(reader: PdfReader, openFile: Boolean) {
        if (readers2intrefs.containsKey(reader))
            return
        readers2intrefs.put(reader, IntHashtable())
        if (openFile) {
            val raf = reader.safeFile
            readers2file.put(reader, raf)
            raf.reOpen()
        }
    }

    /**
     * @param reader
     */
    fun unRegisterReader(reader: PdfReader) {
        if (!readers2intrefs.containsKey(reader))
            return
        readers2intrefs.remove(reader)
        val raf = readers2file[reader] ?: return
        readers2file.remove(reader)
        try {
            raf.close()
        } catch (e: Exception) {
        }

    }

    /**
     * @param fdf
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun addComments(fdf: FdfReader) {
        if (readers2intrefs.containsKey(fdf))
            return
        var catalog: PdfDictionary? = fdf.catalog
        catalog = catalog!!.getAsDict(PdfName.FDF)
        if (catalog == null)
            return
        val annots = catalog.getAsArray(PdfName.ANNOTS)
        if (annots == null || annots.size() == 0)
            return
        registerReader(fdf, false)
        val hits = IntHashtable()
        val irt = HashMap<String, PdfObject>()
        val an = ArrayList<PdfObject>()
        for (k in 0..annots.size() - 1) {
            val obj = annots.getPdfObject(k)
            val annot = PdfReader.getPdfObject(obj) as PdfDictionary?
            val page = annot.getAsNumber(PdfName.PAGE)
            if (page == null || page.intValue() >= pdfReader.numberOfPages)
                continue
            findAllObjects(fdf, obj, hits)
            an.add(obj)
            if (obj.type() == PdfObject.INDIRECT) {
                val nm = PdfReader.getPdfObject(annot.get(PdfName.NM))
                if (nm != null && nm.type() == PdfObject.STRING)
                    irt.put(nm.toString(), obj)
            }
        }
        val arhits = hits.keys
        for (k in arhits.indices) {
            val n = arhits[k]
            var obj: PdfObject = fdf.getPdfObject(n)
            if (obj.type() == PdfObject.DICTIONARY) {
                val str = PdfReader.getPdfObject((obj as PdfDictionary).get(PdfName.IRT))
                if (str != null && str.type() == PdfObject.STRING) {
                    val i = irt[str.toString()]
                    if (i != null) {
                        val dic2 = PdfDictionary()
                        dic2.merge(obj)
                        dic2.put(PdfName.IRT, i)
                        obj = dic2
                    }
                }
            }
            addToBody(obj, getNewObjectNumber(fdf, n, 0))
        }
        for (k in an.indices) {
            val obj = an[k]
            val annot = PdfReader.getPdfObject(obj) as PdfDictionary?
            val page = annot.getAsNumber(PdfName.PAGE)
            val dic = pdfReader.getPageN(page.intValue() + 1)
            var annotsp: PdfArray? = PdfReader.getPdfObject(dic.get(PdfName.ANNOTS), dic) as PdfArray?
            if (annotsp == null) {
                annotsp = PdfArray()
                dic.put(PdfName.ANNOTS, annotsp)
                markUsed(dic)
            }
            markUsed(annotsp)
            annotsp.add(obj)
        }
    }

    fun getPageStamp(pageNum: Int): PageStamp {
        val pageN = pdfReader.getPageN(pageNum)
        var ps: PageStamp? = pagesToContent[pageN]
        if (ps == null) {
            ps = PageStamp(this, pdfReader, pageN)
            pagesToContent.put(pageN, ps)
        }
        return ps
    }

    fun getUnderContent(pageNum: Int): PdfContentByte? {
        if (pageNum < 1 || pageNum > pdfReader.numberOfPages)
            return null
        val ps = getPageStamp(pageNum)
        if (ps.under == null)
            ps.under = StampContent(this, ps)
        return ps.under
    }

    fun getOverContent(pageNum: Int): PdfContentByte? {
        if (pageNum < 1 || pageNum > pdfReader.numberOfPages)
            return null
        val ps = getPageStamp(pageNum)
        if (ps.over == null)
            ps.over = StampContent(this, ps)
        return ps.over
    }

    fun correctAcroFieldPages(page: Int) {
        if (acroFields == null)
            return
        if (page > pdfReader.numberOfPages)
            return
        val fields = acroFields!!.getFields()
        for (item in fields.values) {
            for (k in 0..item.size() - 1) {
                val p = item.getPage(k)!!.toInt()
                if (p >= page)
                    item.forcePage(k, p + 1)
            }
        }
    }

    fun replacePage(r: PdfReader, pageImported: Int, pageReplaced: Int) {
        val pageN = pdfReader.getPageN(pageReplaced)
        if (pagesToContent.containsKey(pageN))
            throw IllegalStateException(MessageLocalization.getComposedMessage("this.page.cannot.be.replaced.new.content.was.already.added"))
        val p = getImportedPage(r, pageImported)
        val dic2 = pdfReader.getPageNRelease(pageReplaced)
        dic2.remove(PdfName.RESOURCES)
        dic2.remove(PdfName.CONTENTS)
        moveRectangle(dic2, r, pageImported, PdfName.MEDIABOX, "media")
        moveRectangle(dic2, r, pageImported, PdfName.CROPBOX, "crop")
        moveRectangle(dic2, r, pageImported, PdfName.TRIMBOX, "trim")
        moveRectangle(dic2, r, pageImported, PdfName.ARTBOX, "art")
        moveRectangle(dic2, r, pageImported, PdfName.BLEEDBOX, "bleed")
        dic2.put(PdfName.ROTATE, PdfNumber(r.getPageRotation(pageImported)))
        val cb = getOverContent(pageReplaced)
        cb.addTemplate(p, 0f, 0f)
        val ps = pagesToContent[pageN]
        ps.replacePoint = ps.over!!.internalBuffer.size()
    }

    fun insertPage(pageNumber: Int, mediabox: Rectangle) {
        var pageNumber = pageNumber
        val media = Rectangle(mediabox)
        val rotation = media.rotation % 360
        val page = PdfDictionary(PdfName.PAGE)
        page.put(PdfName.RESOURCES, PdfDictionary())
        page.put(PdfName.ROTATE, PdfNumber(rotation))
        page.put(PdfName.MEDIABOX, PdfRectangle(media, rotation))
        val pref = pdfReader.addPdfObject(page)
        var parent: PdfDictionary?
        var parentRef: PRIndirectReference
        if (pageNumber > pdfReader.numberOfPages) {
            val lastPage = pdfReader.getPageNRelease(pdfReader.numberOfPages)
            parentRef = lastPage.get(PdfName.PARENT) as PRIndirectReference?
            parentRef = PRIndirectReference(pdfReader, parentRef.number)
            parent = PdfReader.getPdfObject(parentRef) as PdfDictionary?
            val kids = PdfReader.getPdfObject(parent!!.get(PdfName.KIDS), parent) as PdfArray?
            kids.add(pref)
            markUsed(kids)
            pdfReader.pageRefs.insertPage(pageNumber, pref)
        } else {
            if (pageNumber < 1)
                pageNumber = 1
            val firstPage = pdfReader.getPageN(pageNumber)
            val firstPageRef = pdfReader.getPageOrigRef(pageNumber)
            pdfReader.releasePage(pageNumber)
            parentRef = firstPage.get(PdfName.PARENT) as PRIndirectReference?
            parentRef = PRIndirectReference(pdfReader, parentRef.number)
            parent = PdfReader.getPdfObject(parentRef) as PdfDictionary?
            val kids = PdfReader.getPdfObject(parent!!.get(PdfName.KIDS), parent) as PdfArray?
            val len = kids.size()
            val num = firstPageRef.number
            for (k in 0..len - 1) {
                val cur = kids.getPdfObject(k) as PRIndirectReference
                if (num == cur.number) {
                    kids.add(k, pref)
                    break
                }
            }
            if (len == kids.size())
                throw RuntimeException(MessageLocalization.getComposedMessage("internal.inconsistence"))
            markUsed(kids)
            pdfReader.pageRefs.insertPage(pageNumber, pref)
            correctAcroFieldPages(pageNumber)
        }
        page.put(PdfName.PARENT, parentRef)
        while (parent != null) {
            markUsed(parent)
            val count = PdfReader.getPdfObjectRelease(parent.get(PdfName.COUNT)) as PdfNumber?
            parent.put(PdfName.COUNT, PdfNumber(count.intValue() + 1))
            parent = parent.getAsDict(PdfName.PARENT)
        }
    }

    val isContentWritten: Boolean
        get() = body.size() > 1

    fun getAcroFields(): AcroFields {
        if (acroFields == null) {
            acroFields = AcroFields(pdfReader, this)
        }
        return acroFields
    }

    fun setFormFlattening(flat: Boolean) {
        this.flat = flat
    }

    fun setFreeTextFlattening(flat: Boolean) {
        this.flatFreeText = flat
    }

    fun partialFormFlattening(name: String): Boolean {
        getAcroFields()
        if (acroFields!!.xfa!!.isXfaPresent)
            throw UnsupportedOperationException(MessageLocalization.getComposedMessage("partial.form.flattening.is.not.supported.with.xfa.forms"))
        if (!acroFields!!.getFields().containsKey(name))
            return false
        partialFlattening.add(name)
        return true
    }

    protected fun flatFields() {
        if (isAppend)
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("field.flattening.is.not.supported.in.append.mode"))
        getAcroFields()
        val fields = acroFields!!.getFields()
        if (fieldsAdded && partialFlattening.isEmpty()) {
            for (s in fields.keys) {
                partialFlattening.add(s)
            }
        }
        val acroForm = pdfReader.catalog.getAsDict(PdfName.ACROFORM)
        var acroFds: PdfArray? = null
        if (acroForm != null) {
            acroFds = PdfReader.getPdfObject(acroForm.get(PdfName.FIELDS), acroForm) as PdfArray?
        }
        for ((name, item) in fields) {
            if (!partialFlattening.isEmpty() && !partialFlattening.contains(name))
                continue
            for (k in 0..item.size() - 1) {
                val merged = item.getMerged(k)
                val ff = merged.getAsNumber(PdfName.F)
                var flags = 0
                if (ff != null)
                    flags = ff.intValue()
                val page = item.getPage(k)!!.toInt()
                if (page < 1)
                    continue
                var appDic: PdfDictionary? = merged.getAsDict(PdfName.AP)
                var as_n: PdfObject? = null
                if (appDic != null) {
                    as_n = appDic.getAsStream(PdfName.N)
                    if (as_n == null)
                        as_n = appDic.getAsDict(PdfName.N)
                }
                if (acroFields!!.isGenerateAppearances) {
                    if (appDic == null || as_n == null) {
                        try {
                            acroFields!!.regenerateField(name)
                            appDic = acroFields!!.getFieldItem(name)!!.getMerged(k).getAsDict(PdfName.AP)
                        } catch (e: IOException) {
                        } catch (e: DocumentException) {
                        }
                        // if we can't create appearances for some reason, we'll just continue
                    } else if (as_n.isStream) {
                        val stream = as_n as PdfStream?
                        val bbox = stream.getAsArray(PdfName.BBOX)
                        val rect = merged.getAsArray(PdfName.RECT)
                        if (bbox != null && rect != null) {
                            val rectWidth = rect.getAsNumber(2).floatValue() - rect.getAsNumber(0).floatValue()
                            val bboxWidth = bbox.getAsNumber(2).floatValue() - bbox.getAsNumber(0).floatValue()
                            val rectHeight = rect.getAsNumber(3).floatValue() - rect.getAsNumber(1).floatValue()
                            val bboxHeight = bbox.getAsNumber(3).floatValue() - bbox.getAsNumber(1).floatValue()
                            val widthCoef = Math.abs(if (bboxWidth != 0f) rectWidth / bboxWidth else java.lang.Float.MAX_VALUE)
                            val heightCoef = Math.abs(if (bboxHeight != 0f) rectHeight / bboxHeight else java.lang.Float.MAX_VALUE)

                            if (widthCoef != 1f || heightCoef != 1f) {
                                val array = NumberArray(widthCoef, 0, 0, heightCoef, 0, 0)
                                stream.put(PdfName.MATRIX, array)
                                markUsed(stream)
                            }
                        }
                    }
                } else if (appDic != null && as_n != null) {
                    val bbox = (as_n as PdfDictionary).getAsArray(PdfName.BBOX)
                    val rect = merged.getAsArray(PdfName.RECT)
                    if (bbox != null && rect != null) {
                        val widthDiff = bbox.getAsNumber(2).floatValue() - bbox.getAsNumber(0).floatValue() - (rect.getAsNumber(2).floatValue() - rect.getAsNumber(0).floatValue())
                        val heightDiff = bbox.getAsNumber(3).floatValue() - bbox.getAsNumber(1).floatValue() - (rect.getAsNumber(3).floatValue() - rect.getAsNumber(1).floatValue())
                        if (Math.abs(widthDiff) > 1 || Math.abs(heightDiff) > 1) {
                            try {
                                //simulate Adobe behavior.
                                acroFields!!.isGenerateAppearances = true
                                acroFields!!.regenerateField(name)
                                acroFields!!.isGenerateAppearances = false
                                appDic = acroFields!!.getFieldItem(name)!!.getMerged(k).getAsDict(PdfName.AP)
                            } catch (e: IOException) {
                            } catch (e: DocumentException) {
                            }
                            // if we can't create appearances for some reason, we'll just continue
                        }
                    }
                }

                if (appDic != null && flags and PdfFormField.FLAGS_PRINT != 0 && flags and PdfFormField.FLAGS_HIDDEN == 0) {
                    val obj = appDic.get(PdfName.N)
                    var app: PdfAppearance? = null
                    if (obj != null) {
                        var objReal = PdfReader.getPdfObject(obj)
                        if (obj is PdfIndirectReference && !obj.isIndirect)
                            app = PdfAppearance(obj as PdfIndirectReference?)
                        else if (objReal is PdfStream) {
                            (objReal as PdfDictionary).put(PdfName.SUBTYPE, PdfName.FORM)
                            app = PdfAppearance(obj as PdfIndirectReference?)
                        } else {
                            if (objReal != null && objReal.isDictionary) {
                                val `as` = merged.getAsName(PdfName.AS)
                                if (`as` != null) {
                                    val iref = (objReal as PdfDictionary).get(`as`) as PdfIndirectReference?
                                    if (iref != null) {
                                        app = PdfAppearance(iref)
                                        if (iref.isIndirect) {
                                            objReal = PdfReader.getPdfObject(iref)
                                            (objReal as PdfDictionary).put(PdfName.SUBTYPE, PdfName.FORM)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (app != null) {
                        val box = PdfReader.getNormalizedRectangle(merged.getAsArray(PdfName.RECT))
                        val cb = getOverContent(page)
                        cb.setLiteral("Q ")
                        cb.addTemplate(app, box.left, box.bottom)
                        cb.setLiteral("q ")
                    }
                }
                if (partialFlattening.isEmpty())
                    continue
                val pageDic = pdfReader.getPageN(page)
                val annots = pageDic.getAsArray(PdfName.ANNOTS) ?: continue
                var idx = 0
                while (idx < annots.size()) {
                    val ran = annots.getPdfObject(idx)
                    if (!ran.isIndirect) {
                        ++idx
                        continue
                    }
                    val ran2 = item.getWidgetRef(k)
                    if (!ran2.isIndirect) {
                        ++idx
                        continue
                    }
                    if ((ran as PRIndirectReference).number == (ran2 as PRIndirectReference).number) {
                        annots.remove(idx--)
                        while (true) {
                            val wd = PdfReader.getPdfObject(ran2) as PdfDictionary?
                            val parentRef = wd.get(PdfName.PARENT) as PRIndirectReference?
                            PdfReader.killIndirect(ran2)
                            if (parentRef == null) {
                                // reached AcroForm
                                var fr = 0
                                while (fr < acroFds!!.size()) {
                                    val h = acroFds.getPdfObject(fr)
                                    if (h.isIndirect && (h as PRIndirectReference).number == ran2.number) {
                                        acroFds.remove(fr)
                                        --fr
                                    }
                                    ++fr
                                }
                                break
                            }
                            val parent = PdfReader.getPdfObject(parentRef) as PdfDictionary?
                            val kids = parent.getAsArray(PdfName.KIDS)
                            var fr = 0
                            while (fr < kids.size()) {
                                val h = kids.getPdfObject(fr)
                                if (h.isIndirect && (h as PRIndirectReference).number == ran2.number) {
                                    kids.remove(fr)
                                    --fr
                                }
                                ++fr
                            }
                            if (!kids.isEmpty)
                                break
                            ran2 = parentRef
                        }
                    }
                    ++idx
                }
                if (annots.isEmpty) {
                    PdfReader.killIndirect(pageDic.get(PdfName.ANNOTS))
                    pageDic.remove(PdfName.ANNOTS)
                }
            }
        }
        if (!fieldsAdded && partialFlattening.isEmpty()) {
            for (page in 1..pdfReader.numberOfPages) {
                val pageDic = pdfReader.getPageN(page)
                val annots = pageDic.getAsArray(PdfName.ANNOTS) ?: continue
                var idx = 0
                while (idx < annots.size()) {
                    val annoto = annots.getDirectObject(idx)
                    if (annoto is PdfIndirectReference && !annoto.isIndirect) {
                        ++idx
                        continue
                    }
                    if (!annoto.isDictionary || PdfName.WIDGET == (annoto as PdfDictionary).get(PdfName.SUBTYPE)) {
                        annots.remove(idx)
                        --idx
                    }
                    ++idx
                }
                if (annots.isEmpty) {
                    PdfReader.killIndirect(pageDic.get(PdfName.ANNOTS))
                    pageDic.remove(PdfName.ANNOTS)
                }
            }
            eliminateAcroformObjects()
        }
    }

    fun eliminateAcroformObjects() {
        val acro = pdfReader.catalog.get(PdfName.ACROFORM) ?: return
        val acrodic = PdfReader.getPdfObject(acro) as PdfDictionary?
        pdfReader.killXref(acrodic.get(PdfName.XFA))
        acrodic.remove(PdfName.XFA)
        val iFields = acrodic.get(PdfName.FIELDS)
        if (iFields != null) {
            val kids = PdfDictionary()
            kids.put(PdfName.KIDS, iFields)
            sweepKids(kids)
            PdfReader.killIndirect(iFields)
            acrodic.put(PdfName.FIELDS, PdfArray())
        }
        acrodic.remove(PdfName.SIGFLAGS)
        acrodic.remove(PdfName.NEEDAPPEARANCES)
        acrodic.remove(PdfName.DR)
        //        PdfReader.killIndirect(acro);
        //        reader.getCatalog().remove(PdfName.ACROFORM);
    }

    fun sweepKids(obj: PdfObject) {
        val oo = PdfReader.killIndirect(obj)
        if (oo == null || !oo.isDictionary)
            return
        val dic = oo as PdfDictionary?
        val kids = PdfReader.killIndirect(dic.get(PdfName.KIDS)) as PdfArray? ?: return
        for (k in 0..kids.size() - 1) {
            sweepKids(kids.getPdfObject(k))
        }
    }

    /**
     * If true, annotations with an appearance stream will be flattened.

     * @param flatAnnotations boolean
     * *
     * @since 5.5.3
     */
    fun setFlatAnnotations(flatAnnotations: Boolean) {
        this.flatannotations = flatAnnotations
    }

    /**
     * If setFlatAnnotations is set to true, iText will flatten all annotations with an appearance stream

     * @since 5.5.3
     */
    protected fun flattenAnnotations() {
        flattenAnnotations(false)
    }

    private fun flattenAnnotations(flattenFreeTextAnnotations: Boolean) {
        if (isAppend) {
            if (flattenFreeTextAnnotations) {
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("freetext.flattening.is.not.supported.in.append.mode"))
            } else {
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("annotation.flattening.is.not.supported.in.append.mode"))
            }
        }

        for (page in 1..pdfReader.numberOfPages) {
            val pageDic = pdfReader.getPageN(page)
            val annots = pageDic.getAsArray(PdfName.ANNOTS) ?: continue

            var idx = 0
            while (idx < annots.size()) {
                val annoto = annots.getDirectObject(idx)
                if (annoto is PdfIndirectReference && !annoto.isIndirect) {
                    ++idx
                    continue
                }
                if (annoto !is PdfDictionary) {
                    ++idx
                    continue
                }
                if (flattenFreeTextAnnotations) {
                    if (annoto.get(PdfName.SUBTYPE) != PdfName.FREETEXT) {
                        ++idx
                        continue
                    }
                } else {
                    if (annoto.get(PdfName.SUBTYPE) == PdfName.WIDGET) {
                        // skip widgets
                        continue
                    }
                }

                val ff = annoto.getAsNumber(PdfName.F)
                val flags = if (ff != null) ff.intValue() else 0

                if (flags and PdfFormField.FLAGS_PRINT != 0 && flags and PdfFormField.FLAGS_HIDDEN == 0) {
                    val obj1 = annoto.get(PdfName.AP)
                    if (obj1 == null) {
                        ++idx
                        continue
                    }
                    val appDic = if (obj1 is PdfIndirectReference)
                        PdfReader.getPdfObject(obj1) as PdfDictionary?
                    else
                        obj1 as PdfDictionary?
                    val obj = appDic.get(PdfName.N)
                    val objDict = appDic.getAsStream(PdfName.N)
                    var app: PdfAppearance? = null
                    var objReal: PdfObject = PdfReader.getPdfObject(obj)

                    if (obj is PdfIndirectReference && !obj.isIndirect) {
                        app = PdfAppearance(obj)
                    } else if (objReal is PdfStream) {
                        (objReal as PdfDictionary).put(PdfName.SUBTYPE, PdfName.FORM)
                        app = PdfAppearance(obj as PdfIndirectReference)
                    } else {
                        if (objReal.isDictionary) {
                            val as_p = appDic.getAsName(PdfName.AS)
                            if (as_p != null) {
                                val iref = (objReal as PdfDictionary).get(as_p) as PdfIndirectReference?
                                if (iref != null) {
                                    app = PdfAppearance(iref)
                                    if (iref.isIndirect) {
                                        objReal = PdfReader.getPdfObject(iref)
                                        (objReal as PdfDictionary).put(PdfName.SUBTYPE, PdfName.FORM)
                                    }
                                }
                            }
                        }
                    }
                    if (app != null) {
                        val rect = PdfReader.getNormalizedRectangle(annoto.getAsArray(PdfName.RECT))
                        val bBox = PdfReader.getNormalizedRectangle(objDict.getAsArray(PdfName.BBOX))
                        val cb = getOverContent(page)
                        cb.setLiteral("Q ")
                        if (objDict.getAsArray(PdfName.MATRIX) != null && !Arrays.equals(DEFAULT_MATRIX, objDict.getAsArray(PdfName.MATRIX).asDoubleArray())) {
                            val matrix = objDict.getAsArray(PdfName.MATRIX).asDoubleArray()
                            val transformBBox = transformBBoxByMatrix(bBox, matrix)
                            cb.addTemplate(app, rect.width / transformBBox.width, 0f, 0f, rect.height / transformBBox.height, rect.left, rect.bottom)
                        } else {
                            //Changed so that when the annotation has a difference scale than the xObject in the appearance dictionary, the image is consistent between
                            //the input and the flattened document.  When the annotation is rotated or skewed, it will still be flattened incorrectly.
                            cb.addTemplate(app, rect.width / bBox.width, 0f, 0f, rect.height / bBox.height, rect.left, rect.bottom)
                        }
                        cb.setLiteral("q ")

                        annots.remove(idx)
                        --idx
                    }
                }
                ++idx
            }

            if (annots.isEmpty) {
                PdfReader.killIndirect(pageDic.get(PdfName.ANNOTS))
                pageDic.remove(PdfName.ANNOTS)
            }
        }
    }

    /*
    * The transformation BBOX between two coordinate systems can be
    * represented by a 3-by-3 transformation matrix and create new BBOX based min(x,y) and
     * max(x,y) coordinate pairs
    * */
    private fun transformBBoxByMatrix(bBox: Rectangle, matrix: DoubleArray): Rectangle {
        val xArr = ArrayList()
        val yArr = ArrayList()
        val p1 = transformPoint(bBox.left.toDouble(), bBox.bottom.toDouble(), matrix)
        xArr.add(p1.x)
        yArr.add(p1.y)
        val p2 = transformPoint(bBox.right.toDouble(), bBox.top.toDouble(), matrix)
        xArr.add(p2.x)
        yArr.add(p2.y)
        val p3 = transformPoint(bBox.left.toDouble(), bBox.top.toDouble(), matrix)
        xArr.add(p3.x)
        yArr.add(p3.y)
        val p4 = transformPoint(bBox.right.toDouble(), bBox.bottom.toDouble(), matrix)
        xArr.add(p4.x)
        yArr.add(p4.y)

        return Rectangle((Collections.min(xArr) as Double).toFloat(),
                (Collections.min(yArr) as Double).toFloat(),
                (Collections.max(xArr) as Double).toFloat(),
                (Collections.max(yArr) as Double).toFloat())
    }

    /*
    *  transform point by algorithm
    *  x′ = a*x + c×y + e
    *  y' = b*x + d*y + f
    *  [ a b c d e f ] transformation matrix values
    * */
    private fun transformPoint(x: Double, y: Double, matrix: DoubleArray): Point {
        val point = Point()
        point.x = matrix[0] * x + matrix[2] * y + matrix[4]
        point.y = matrix[1] * x + matrix[3] * y + matrix[5]
        return point
    }

    protected fun flatFreeTextFields() {
        flattenAnnotations(true)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfWriter.getPageReference
     */
    override fun getPageReference(page: Int): PdfIndirectReference {
        val ref = pdfReader.getPageOrigRef(page) ?: throw IllegalArgumentException(MessageLocalization.getComposedMessage("invalid.page.number.1", page))
        return ref
    }

    /**
     * @see com.itextpdf.text.pdf.PdfWriter.addAnnotation
     */
    override fun addAnnotation(annot: PdfAnnotation) {
        throw RuntimeException(MessageLocalization.getComposedMessage("unsupported.in.this.context.use.pdfstamper.addannotation"))
    }

    fun addDocumentField(ref: PdfIndirectReference) {
        val catalog = pdfReader.catalog
        var acroForm: PdfDictionary? = PdfReader.getPdfObject(catalog.get(PdfName.ACROFORM), catalog) as PdfDictionary?
        if (acroForm == null) {
            acroForm = PdfDictionary()
            catalog.put(PdfName.ACROFORM, acroForm)
            markUsed(catalog)
        }
        var fields: PdfArray? = PdfReader.getPdfObject(acroForm.get(PdfName.FIELDS), acroForm) as PdfArray?
        if (fields == null) {
            fields = PdfArray()
            acroForm.put(PdfName.FIELDS, fields)
            markUsed(acroForm)
        }
        if (!acroForm.contains(PdfName.DA)) {
            acroForm.put(PdfName.DA, PdfString("/Helv 0 Tf 0 g "))
            markUsed(acroForm)
        }
        fields.add(ref)
        markUsed(fields)
    }

    @Throws(IOException::class)
    protected fun addFieldResources() {
        if (fieldTemplates.isEmpty())
            return
        val catalog = pdfReader.catalog
        var acroForm: PdfDictionary? = PdfReader.getPdfObject(catalog.get(PdfName.ACROFORM), catalog) as PdfDictionary?
        if (acroForm == null) {
            acroForm = PdfDictionary()
            catalog.put(PdfName.ACROFORM, acroForm)
            markUsed(catalog)
        }
        var dr: PdfDictionary? = PdfReader.getPdfObject(acroForm.get(PdfName.DR), acroForm) as PdfDictionary?
        if (dr == null) {
            dr = PdfDictionary()
            acroForm.put(PdfName.DR, dr)
            markUsed(acroForm)
        }
        markUsed(dr)
        for (template in fieldTemplates) {
            PdfFormField.mergeResources(dr, template.resources as PdfDictionary, this)
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
        if (acroForm.get(PdfName.DA) == null) {
            acroForm.put(PdfName.DA, PdfString("/Helv 0 Tf 0 g "))
            markUsed(acroForm)
        }
    }

    fun expandFields(field: PdfFormField, allAnnots: ArrayList<PdfAnnotation>) {
        allAnnots.add(field)
        val kids = field.kids
        if (kids != null) {
            for (k in kids.indices)
                expandFields(kids[k], allAnnots)
        }
    }

    fun addAnnotation(annot: PdfAnnotation, pageN: PdfDictionary) {
        var annot = annot
        var pageN = pageN
        try {
            val allAnnots = ArrayList<PdfAnnotation>()
            if (annot.isForm) {
                fieldsAdded = true
                getAcroFields()
                val field = annot as PdfFormField
                if (field.parent != null)
                    return
                expandFields(field, allAnnots)
            } else
                allAnnots.add(annot)
            for (k in allAnnots.indices) {
                annot = allAnnots[k]
                if (annot.placeInPage > 0)
                    pageN = pdfReader.getPageN(annot.placeInPage)
                if (annot.isForm) {
                    if (!annot.isUsed) {
                        val templates = annot.templates
                        if (templates != null)
                            fieldTemplates.addAll(templates)
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
                        markUsed(pageN)
                    } else
                        annots = pdfobj as PdfArray?
                    annots!!.add(annot.indirectReference)
                    markUsed(annots)
                    if (!annot.isUsed) {
                        val rect = annot.get(PdfName.RECT) as PdfRectangle?
                        if (rect != null && (rect.left() != 0f || rect.right() != 0f || rect.top() != 0f || rect.bottom() != 0f)) {
                            val rotation = pdfReader.getPageRotation(pageN)
                            val pageSize = pdfReader.getPageSizeWithRotation(pageN)
                            when (rotation) {
                                90 -> annot.put(PdfName.RECT, PdfRectangle(
                                        pageSize.top - rect.top(),
                                        rect.right(),
                                        pageSize.top - rect.bottom(),
                                        rect.left()))
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
                    addToBody(annot, annot.indirectReference)
                }
            }
        } catch (e: IOException) {
            throw ExceptionConverter(e)
        }

    }

    internal override fun addAnnotation(annot: PdfAnnotation, page: Int) {
        if (annot.isAnnotation)
            annot.setPage(page)
        addAnnotation(annot, pdfReader.getPageN(page))
    }

    private fun outlineTravel(outline: PRIndirectReference?) {
        var outline = outline
        while (outline != null) {
            val outlineR = PdfReader.getPdfObjectRelease(outline) as PdfDictionary?
            val first = outlineR.get(PdfName.FIRST) as PRIndirectReference?
            if (first != null) {
                outlineTravel(first)
            }
            PdfReader.killIndirect(outlineR.get(PdfName.DEST))
            PdfReader.killIndirect(outlineR.get(PdfName.A))
            PdfReader.killIndirect(outline)
            outline = outlineR.get(PdfName.NEXT) as PRIndirectReference?
        }
    }

    fun deleteOutlines() {
        val catalog = pdfReader.catalog
        val obj = catalog.get(PdfName.OUTLINES) ?: return
        if (obj is PRIndirectReference) {
            outlineTravel(obj)
            PdfReader.killIndirect(obj)
        }
        catalog.remove(PdfName.OUTLINES)
        markUsed(catalog)
    }

    @Throws(IOException::class)
    protected fun setJavaScript() {
        val djs = pdfDocument.documentLevelJS
        if (djs.isEmpty())
            return
        val catalog = pdfReader.catalog
        var names: PdfDictionary? = PdfReader.getPdfObject(catalog.get(PdfName.NAMES), catalog) as PdfDictionary?
        if (names == null) {
            names = PdfDictionary()
            catalog.put(PdfName.NAMES, names)
            markUsed(catalog)
        }
        markUsed(names)
        val tree = PdfNameTree.writeTree(djs, this)
        names.put(PdfName.JAVASCRIPT, addToBody(tree).indirectReference)
    }

    @Throws(IOException::class)
    protected fun addFileAttachments() {
        val fs = pdfDocument.documentFileAttachment
        if (fs.isEmpty())
            return
        val catalog = pdfReader.catalog
        var names: PdfDictionary? = PdfReader.getPdfObject(catalog.get(PdfName.NAMES), catalog) as PdfDictionary?
        if (names == null) {
            names = PdfDictionary()
            catalog.put(PdfName.NAMES, names)
            markUsed(catalog)
        }
        markUsed(names)
        val old = PdfNameTree.readTree(PdfReader.getPdfObjectRelease(names.get(PdfName.EMBEDDEDFILES)) as PdfDictionary?)
        for (entry in fs.entries) {
            val name = entry.key
            var k = 0
            val nn = StringBuilder(name)
            while (old.containsKey(nn.toString())) {
                ++k
                nn.append(" ").append(k)
            }
            old.put(nn.toString(), entry.value)
        }
        val tree = PdfNameTree.writeTree(old, this)
        // Remove old EmbeddedFiles object if preset
        val oldEmbeddedFiles = names.get(PdfName.EMBEDDEDFILES)
        if (oldEmbeddedFiles != null) {
            PdfReader.killIndirect(oldEmbeddedFiles)
        }

        // Add new EmbeddedFiles object
        names.put(PdfName.EMBEDDEDFILES, addToBody(tree).indirectReference)
    }

    /**
     * Adds or replaces the Collection Dictionary in the Catalog.

     * @param collection the new collection dictionary.
     */
    fun makePackage(collection: PdfCollection) {
        val catalog = pdfReader.catalog
        catalog.put(PdfName.COLLECTION, collection)
    }

    @Throws(IOException::class)
    protected fun setOutlines() {
        if (newBookmarks == null)
            return
        deleteOutlines()
        if (newBookmarks!!.isEmpty())
            return
        val catalog = pdfReader.catalog
        val namedAsNames = catalog.get(PdfName.DESTS) != null
        writeOutlines(catalog, namedAsNames)
        markUsed(catalog)
    }

    /**
     * Sets the viewer preferences.

     * @param preferences the viewer preferences
     * *
     * @see PdfWriter.setViewerPreferences
     */
    override fun setViewerPreferences(preferences: Int) {
        useVp = true
        this.viewerPreferences.setViewerPreferences(preferences)
    }

    /**
     * Adds a viewer preference

     * @param key   a key for a viewer preference
     * *
     * @param value the value for the viewer preference
     * *
     * @see PdfViewerPreferences.addViewerPreference
     */
    override fun addViewerPreference(key: PdfName, value: PdfObject) {
        useVp = true
        this.viewerPreferences.addViewerPreference(key, value)
    }

    /**
     * Set the signature flags.

     * @param f the flags. This flags are ORed with current ones
     */
    override fun setSigFlags(f: Int) {
        sigFlags = sigFlags or f
    }

    /**
     * Always throws an `UnsupportedOperationException`.

     * @param actionType ignore
     * *
     * @param action     ignore
     * *
     * @throws PdfException ignore
     * *
     * @see PdfStamper.setPageAction
     */
    @Throws(PdfException::class)
    override fun setPageAction(actionType: PdfName, action: PdfAction) {
        throw UnsupportedOperationException(MessageLocalization.getComposedMessage("use.setpageaction.pdfname.actiontype.pdfaction.action.int.page"))
    }

    /**
     * Sets the open and close page additional action.

     * @param actionType the action type. It can be PdfWriter.PAGE_OPEN
     * *                   or PdfWriter.PAGE_CLOSE
     * *
     * @param action     the action to perform
     * *
     * @param page       the page where the action will be applied. The first page is 1
     * *
     * @throws PdfException if the action type is invalid
     */
    @Throws(PdfException::class)
    fun setPageAction(actionType: PdfName, action: PdfAction, page: Int) {
        if (actionType != PdfWriter.PAGE_OPEN && actionType != PdfWriter.PAGE_CLOSE)
            throw PdfException(MessageLocalization.getComposedMessage("invalid.page.additional.action.type.1", actionType.toString()))
        val pg = pdfReader.getPageN(page)
        var aa: PdfDictionary? = PdfReader.getPdfObject(pg.get(PdfName.AA), pg) as PdfDictionary?
        if (aa == null) {
            aa = PdfDictionary()
            pg.put(PdfName.AA, aa)
            markUsed(pg)
        }
        aa.put(actionType, action)
        markUsed(aa)
    }

    /**
     * Always throws an `UnsupportedOperationException`.

     * @param seconds ignore
     */
    override fun setDuration(seconds: Int) {
        throw UnsupportedOperationException(MessageLocalization.getComposedMessage("use.setpageaction.pdfname.actiontype.pdfaction.action.int.page"))
    }

    /**
     * Always throws an `UnsupportedOperationException`.

     * @param transition ignore
     */
    override fun setTransition(transition: PdfTransition) {
        throw UnsupportedOperationException(MessageLocalization.getComposedMessage("use.setpageaction.pdfname.actiontype.pdfaction.action.int.page"))
    }

    /**
     * Sets the display duration for the page (for presentations)

     * @param seconds the number of seconds to display the page. A negative value removes the entry
     * *
     * @param page    the page where the duration will be applied. The first page is 1
     */
    fun setDuration(seconds: Int, page: Int) {
        val pg = pdfReader.getPageN(page)
        if (seconds < 0)
            pg.remove(PdfName.DUR)
        else
            pg.put(PdfName.DUR, PdfNumber(seconds))
        markUsed(pg)
    }

    /**
     * Sets the transition for the page

     * @param transition the transition object. A `null` removes the transition
     * *
     * @param page       the page where the transition will be applied. The first page is 1
     */
    fun setTransition(transition: PdfTransition?, page: Int) {
        val pg = pdfReader.getPageN(page)
        if (transition == null)
            pg.remove(PdfName.TRANS)
        else
            pg.put(PdfName.TRANS, transition.transitionDictionary)
        markUsed(pg)
    }

    protected fun markUsed(obj: PdfObject?) {
        if (isAppend && obj != null) {
            var ref: PRIndirectReference? = null
            if (obj.type() == PdfObject.INDIRECT)
                ref = obj as PRIndirectReference?
            else
                ref = obj.indRef
            if (ref != null)
                marked.put(ref.number, 1)
        }
    }

    protected fun markUsed(num: Int) {
        if (isAppend)
            marked.put(num, 1)
    }

    /**
     * Additional-actions defining the actions to be taken in
     * response to various trigger events affecting the document
     * as a whole. The actions types allowed are: DOCUMENT_CLOSE,
     * WILL_SAVE, DID_SAVE, WILL_PRINT
     * and DID_PRINT.

     * @param actionType the action type
     * *
     * @param action     the action to execute in response to the trigger
     * *
     * @throws PdfException on invalid action type
     */
    @Throws(PdfException::class)
    override fun setAdditionalAction(actionType: PdfName, action: PdfAction?) {
        if (!(actionType == PdfWriter.DOCUMENT_CLOSE ||
                actionType == PdfWriter.WILL_SAVE ||
                actionType == PdfWriter.DID_SAVE ||
                actionType == PdfWriter.WILL_PRINT ||
                actionType == PdfWriter.DID_PRINT)) {
            throw PdfException(MessageLocalization.getComposedMessage("invalid.additional.action.type.1", actionType.toString()))
        }
        var aa: PdfDictionary? = pdfReader.catalog.getAsDict(PdfName.AA)
        if (aa == null) {
            if (action == null)
                return
            aa = PdfDictionary()
            pdfReader.catalog.put(PdfName.AA, aa)
        }
        markUsed(aa)
        if (action == null)
            aa.remove(actionType)
        else
            aa.put(actionType, action)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfWriter.setOpenAction
     */
    override fun setOpenAction(action: PdfAction) {
        openAction = action
    }

    /**
     * @see com.itextpdf.text.pdf.PdfWriter.setOpenAction
     */
    override fun setOpenAction(name: String) {
        throw UnsupportedOperationException(MessageLocalization.getComposedMessage("open.actions.by.name.are.not.supported"))
    }

    /**
     * @see com.itextpdf.text.pdf.PdfWriter.setThumbnail
     */
    override fun setThumbnail(image: com.itextpdf.text.Image) {
        throw UnsupportedOperationException(MessageLocalization.getComposedMessage("use.pdfstamper.setthumbnail"))
    }

    @Throws(PdfException::class, DocumentException::class)
    fun setThumbnail(image: Image, page: Int) {
        val thumb = getImageReference(addDirectImageSimple(image))
        pdfReader.resetReleasePage()
        val dic = pdfReader.getPageN(page)
        dic.put(PdfName.THUMB, thumb)
        pdfReader.resetReleasePage()
    }

    override fun getDirectContentUnder(): PdfContentByte {
        throw UnsupportedOperationException(MessageLocalization.getComposedMessage("use.pdfstamper.getundercontent.or.pdfstamper.getovercontent"))
    }

    override fun getDirectContent(): PdfContentByte {
        throw UnsupportedOperationException(MessageLocalization.getComposedMessage("use.pdfstamper.getundercontent.or.pdfstamper.getovercontent"))
    }

    /**
     * Reads the OCProperties dictionary from the catalog of the existing document
     * and fills the documentOCG, documentOCGorder and OCGRadioGroup variables in PdfWriter.
     * Note that the original OCProperties of the existing document can contain more information.

     * @since 2.1.2
     */
    protected fun readOCProperties() {
        if (!documentOCG.isEmpty()) {
            return
        }
        val dict = pdfReader.catalog.getAsDict(PdfName.OCPROPERTIES) ?: return
        val ocgs = dict.getAsArray(PdfName.OCGS)
        var ref: PdfIndirectReference
        var layer: PdfLayer
        val ocgmap = HashMap<String, PdfLayer>()
        run {
            val i = ocgs.listIterator()
            while (i.hasNext()) {
                ref = i.next() as PdfIndirectReference
                layer = PdfLayer(null)
                layer.ref = ref
                layer.isOnPanel = false
                layer.merge(PdfReader.getPdfObject(ref) as PdfDictionary?)
                ocgmap.put(ref.toString(), layer)
            }
        }
        val d = dict.getAsDict(PdfName.D)
        val off = d.getAsArray(PdfName.OFF)
        if (off != null) {
            val i = off.listIterator()
            while (i.hasNext()) {
                ref = i.next() as PdfIndirectReference
                layer = ocgmap[ref.toString()]
                layer.isOn = false
            }
        }
        val order = d.getAsArray(PdfName.ORDER)
        if (order != null) {
            addOrder(null, order, ocgmap)
        }
        documentOCG.addAll(ocgmap.values)
        OCGRadioGroup = d.getAsArray(PdfName.RBGROUPS)
        if (OCGRadioGroup == null)
            OCGRadioGroup = PdfArray()
        OCGLocked = d.getAsArray(PdfName.LOCKED)
        if (OCGLocked == null)
            OCGLocked = PdfArray()
    }

    /**
     * Recursive method to reconstruct the documentOCGorder variable in the writer.

     * @param parent a parent PdfLayer (can be null)
     * *
     * @param arr    an array possibly containing children for the parent PdfLayer
     * *
     * @param ocgmap a HashMap with indirect reference Strings as keys and PdfLayer objects as values.
     * *
     * @since 2.1.2
     */
    private fun addOrder(parent: PdfLayer?, arr: PdfArray, ocgmap: Map<String, PdfLayer>) {
        var obj: PdfObject
        var layer: PdfLayer?
        var i = 0
        while (i < arr.size()) {
            obj = arr.getPdfObject(i)
            if (obj.isIndirect) {
                layer = ocgmap[obj.toString()]
                if (layer != null) {
                    layer.isOnPanel = true
                    registerLayer(layer)
                    parent?.addChild(layer)
                    if (arr.size() > i + 1 && arr.getPdfObject(i + 1).isArray) {
                        i++
                        addOrder(layer, arr.getPdfObject(i) as PdfArray, ocgmap)
                    }
                }
            } else if (obj.isArray) {
                val sub = obj as PdfArray
                if (sub.isEmpty) return
                obj = sub.getPdfObject(0)
                if (obj.isString) {
                    layer = PdfLayer(obj.toString())
                    layer.isOnPanel = true
                    registerLayer(layer)
                    parent?.addChild(layer)
                    val array = PdfArray()
                    val j = sub.listIterator()
                    while (j.hasNext()) {
                        array.add(j.next())
                    }
                    addOrder(layer, array, ocgmap)
                } else {
                    addOrder(parent, obj as PdfArray, ocgmap)
                }
            }
            i++
        }
    }

    /**
     * Gets the PdfLayer objects in an existing document as a Map
     * with the names/titles of the layers as keys.

     * @return a Map with all the PdfLayers in the document (and the name/title of the layer as key)
     * *
     * @since 2.1.2
     */
    val pdfLayers: Map<String, PdfLayer>
        get() {
            if (!originalLayersAreRead) {
                originalLayersAreRead = true
                readOCProperties()
            }
            val map = HashMap<String, PdfLayer>()
            var layer: PdfLayer
            var key: String
            for (pdfOCG in documentOCG) {
                layer = pdfOCG as PdfLayer
                if (layer.title == null) {
                    key = layer.getAsString(PdfName.NAME).toString()
                } else {
                    key = layer.title
                }
                if (map.containsKey(key)) {
                    var seq = 2
                    var tmp = "$key($seq)"
                    while (map.containsKey(tmp)) {
                        seq++
                        tmp = "$key($seq)"
                    }
                    key = tmp
                }
                map.put(key, layer)
            }
            return map
        }

    internal override fun registerLayer(layer: PdfOCG) {
        if (!originalLayersAreRead) {
            originalLayersAreRead = true
            readOCProperties()
        }
        super.registerLayer(layer)
    }

    override fun createXmpMetadata() {
        try {
            xmpWriter = createXmpWriter(null, pdfReader.info)
            xmpMetadata = null
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }

    }

    @Throws(IOException::class)
    protected fun updateNamedDestinations() {

        var dic: PdfDictionary? = pdfReader.catalog.getAsDict(PdfName.NAMES)
        if (dic != null)
            dic = dic.getAsDict(PdfName.DESTS)
        if (dic == null) {
            dic = pdfReader.catalog.getAsDict(PdfName.DESTS)
        }
        if (dic == null) {
            dic = PdfDictionary()
            val dests = PdfDictionary()
            dic.put(PdfName.NAMES, PdfArray())
            dests.put(PdfName.DESTS, dic)
            pdfReader.catalog.put(PdfName.NAMES, dests)
        }

        val names = getLastChildInNameTree(dic)

        for (name in namedDestinations.keys) {
            names.add(PdfString(name.toString()))
            names.add(addToBody(namedDestinations[name], pdfIndirectReference).indirectReference)
        }
    }

    private fun getLastChildInNameTree(dic: PdfDictionary): PdfArray {

        val names: PdfArray

        val childNode = dic.getAsArray(PdfName.KIDS)
        if (childNode != null) {
            val lastKid = childNode.getAsDict(childNode.size() - 1)
            names = getLastChildInNameTree(lastKid)
        } else {
            names = dic.getAsArray(PdfName.NAMES)
        }

        return names
    }

    internal class PageStamp(stamper: PdfStamperImp, reader: PdfReader, var pageN:

    PdfDictionary) {
        var under: StampContent? = null
        var over: StampContent? = null
        var pageResources: PageResources
        var replacePoint = 0

        init {
            pageResources = PageResources()
            val resources = pageN.getAsDict(PdfName.RESOURCES)
            pageResources.setOriginalResources(resources, stamper.namePtr)
        }
    }

    companion object {

        fun findAllObjects(reader: PdfReader, obj: PdfObject?, hits: IntHashtable) {
            if (obj == null)
                return
            when (obj.type()) {
                PdfObject.INDIRECT -> {
                    val iref = obj as PRIndirectReference?
                    if (reader !== iref.reader)
                        return
                    if (hits.containsKey(iref.number))
                        return
                    hits.put(iref.number, 1)
                    findAllObjects(reader, PdfReader.getPdfObject(obj), hits)
                    return
                }
                PdfObject.ARRAY -> {
                    val a = obj as PdfArray?
                    for (k in 0..a.size() - 1) {
                        findAllObjects(reader, a.getPdfObject(k), hits)
                    }
                    return
                }
                PdfObject.DICTIONARY, PdfObject.STREAM -> {
                    val dic = obj as PdfDictionary?
                    for (element in dic.keys) {
                        findAllObjects(reader, dic.get(element), hits)
                    }
                    return
                }
            }
        }

        private fun moveRectangle(dic2: PdfDictionary, r: PdfReader, pageImported: Int, key: PdfName, name: String) {
            val m = r.getBoxSize(pageImported, name)
            if (m == null)
                dic2.remove(key)
            else
                dic2.put(key, PdfRectangle(m))
        }
    }
}
