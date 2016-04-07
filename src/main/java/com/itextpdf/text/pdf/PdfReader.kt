/*
 * $Id: 7252cf7ac4501aff4a3b7e6f92ccf5b92d1b73a3 $
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
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.PageSize
import com.itextpdf.text.Rectangle
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.exceptions.BadPasswordException
import com.itextpdf.text.exceptions.InvalidPdfException
import com.itextpdf.text.exceptions.UnsupportedPdfException
import com.itextpdf.text.io.RandomAccessSource
import com.itextpdf.text.io.RandomAccessSourceFactory
import com.itextpdf.text.io.WindowRandomAccessSource
import com.itextpdf.text.log.*
import com.itextpdf.text.pdf.PRTokeniser.TokenType
import com.itextpdf.text.pdf.interfaces.PdfViewerPreferences
import com.itextpdf.text.pdf.internal.PdfViewerPreferencesImp
import com.itextpdf.text.pdf.security.ExternalDecryptionProcess
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cms.CMSEnvelopedData
import org.bouncycastle.cms.RecipientInformation

import java.io.*
import java.net.URL
import java.security.Key
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.cert.Certificate
import java.util.*
import java.util.zip.InflaterInputStream

/**
   * Reads a PDF document.
   * @author Paulo Soares
  * * 
 * @author Kazuya Ujihara
 */
open class PdfReader:PdfViewerPreferences {
protected var tokens:PRTokeniser
// Each xref pair is a position
    // type 0 -> -1, 0
    // type 1 -> offset, 0
    // type 2 -> index, obj num
    protected var xref:LongArray? = null
protected var objStmMark:HashMap<Int, IntHashtable>? = null
protected var objStmToOffset:LongHashtable? = null
/**
       * Getter for property newXrefType.
       * @return Value of property newXrefType.
 */
     var isNewXrefType:Boolean = false
protected set
protected var xrefObj:ArrayList<PdfObject>
internal var rootPages:PdfDictionary? = null
/**
       * Gets the trailer dictionary
       * @return the trailer dictionary
 */
     var trailer:PdfDictionary? = null
protected set
/**
       * Returns the document's catalog. This dictionary is not a copy,
       * any changes will be reflected in the catalog.
       * @return the document's catalog
 */
     var catalog:PdfDictionary? = null
protected set
protected var pageRefs:PageRefs
protected var acroForm:PRAcroForm? = null
protected var acroFormParsed = false
/**
       * Returns true if the PDF is encrypted.
       * @return true if the PDF is encrypted
 */
     var isEncrypted = false
protected set
/** Checks if the document had errors and was rebuilt.
       * @return true if rebuilt.
 */
     var isRebuilt = false
protected set
protected var freeXref:Int = 0
/** Checks if the document was changed.
       * @return true if the document was changed,
      * * false otherwise
 */
    /**
       * Sets the tampered state. A tampered PdfReader cannot be reused in PdfStamper.
       * @param tampered the tampered state
 */
     var isTampered = false
set(tampered) {
this.isTampered = tampered
pageRefs.keepPages()
}
/**
       * Gets the byte address of the last xref table.
       * @return the byte address of the last xref table
 */
     var lastXref:Long = 0
protected set
/**
       * Gets the byte address of the %%EOF marker.
       * @return the byte address of the %%EOF marker
 */
     var eofPos:Long = 0
protected set
/**
       * Gets the PDF version. Only the last version char is returned. For example
       * version 1.4 is returned as '4'.
       * @return the PDF version
 */
     var pdfVersion:Char = ' '
protected set
internal var decrypt:

PdfEncryption? = null
protected set
protected var password:ByteArray? = null //added by ujihara for decryption
protected var certificateKey:Key? = null //added by Aiken Sam for certificate decryption
protected var certificate:Certificate? = null //added by Aiken Sam for certificate decryption
protected var certificateKeyProvider:String? = null //added by Aiken Sam for certificate decryption
protected var externalDecryptionProcess:ExternalDecryptionProcess? = null
private var ownerPasswordUsed:Boolean = false
protected var strings:ArrayList<PdfString>? = ArrayList()
protected var sharedStreams = true
protected var consolidateNamedDestinations = false
protected var remoteToLocalNamedDestinations = false
protected var rValue:Int = 0
/**
       * Gets the encryption permissions. It can be used directly in
       * PdfWriter.setEncryption().
       * @return the encryption permissions
 */
     var permissions:Long = 0
protected set
private var objNum:Int = 0
private var objGen:Int = 0
/**
       * Getter for property fileLength.
       * @return Value of property fileLength.
 */
     var fileLength:Long = 0
private set
/**
       * Getter for property hybridXref.
       * @return Value of property hybridXref.
 */
     var isHybridXref:Boolean = false
private set
private var lastXrefPartial = -1
private var partial:Boolean = false

private var cryptoRef:PRIndirectReference? = null
private val viewerPreferences = PdfViewerPreferencesImp()
private var encryptionError:Boolean = false

/**
       * Holds value of property appendable.
      */
    /**
       * Getter for property appendable.
       * @return Value of property appendable.
 */
    /**
       * Setter for property appendable.
       * @param appendable New value of property appendable.
 */
     var isAppendable:Boolean = false
set(appendable) {
this.isAppendable = appendable
if (appendable)
getPdfObject(trailer!!.get(PdfName.ROOT))
}
protected open val counter:Counter
get() =COUNTER

/**
       * Constructs a new PdfReader.  This is the master constructor.
       * @param byteSource source of bytes for the reader
      * * 
 * @param partialRead if true, the reader is opened in partial mode (PDF is parsed on demand), if false, the entire PDF is parsed into memory as the reader opens
      * * 
 * @param ownerPassword the password or null if no password is required
      * * 
 * @param certificate the certificate or null if no certificate is required
      * * 
 * @param certificateKey the key or null if no certificate key is required
      * * 
 * @param certificateKeyProvider the name of the key provider, or null if no key is required
      * * 
 * @param externalDecryptionProcess
      * * 
 * @param closeSourceOnConstructorError if true, the byteSource will be closed if there is an error during construction of this reader
 */
    @Throws(IOException::class)
private constructor(byteSource:RandomAccessSource, partialRead:Boolean, ownerPassword:ByteArray?, certificate:Certificate?, certificateKey:Key?, certificateKeyProvider:String?, externalDecryptionProcess:ExternalDecryptionProcess?, closeSourceOnConstructorError:Boolean) {
this.certificate = certificate
this.certificateKey = certificateKey
this.certificateKeyProvider = certificateKeyProvider
this.externalDecryptionProcess = externalDecryptionProcess
this.password = ownerPassword
this.partial = partialRead
try
{

tokens = getOffsetTokeniser(byteSource)

if (partialRead)
{
readPdfPartial()
}
else
{
readPdf()
}
}
catch (e:IOException) {
if (closeSourceOnConstructorError)
byteSource.close()
throw e
}

counter.read(fileLength)
}


/**
       * Reads and parses a PDF document.
       * @param filename the file name of the document
      * * 
 * @param ownerPassword the password to read the document
      * * 
 * @param partial indicates if the reader needs to read the document only partially
      * * 
 * @throws IOException on error
 */
    @Throws(IOException::class)
@JvmOverloads  constructor(filename:String, ownerPassword:ByteArray = null as ByteArray, partial:Boolean = false) : this(
RandomAccessSourceFactory().setForceRead(false).setUsePlainRandomAccess(Document.plainRandomAccess).createBestSource(filename), 
partial, 
ownerPassword, 
null, 
null, 
null, 
null, 
true) {}

/**
       * Reads and parses a PDF document.
       * @param pdfIn the byte array with the document
      * * 
 * @param ownerPassword the password to read the document
      * * 
 * @throws IOException on error
 */
    @Throws(IOException::class)
@JvmOverloads  constructor(pdfIn:ByteArray, ownerPassword:ByteArray? = null) : this(
RandomAccessSourceFactory().createSource(pdfIn), 
false, 
ownerPassword, 
null, 
null, 
null, 
null, 
true) {

}

/**
       * Reads and parses a PDF document.
       * @param filename the file name of the document
      * * 
 * @param certificate the certificate to read the document
      * * 
 * @param certificateKey the private key of the certificate
      * * 
 * @param certificateKeyProvider the security provider for certificateKey
      * * 
 * @throws IOException on error
 */
    @Throws(IOException::class)
 constructor(filename:String, certificate:Certificate, certificateKey:Key, certificateKeyProvider:String) : this(
RandomAccessSourceFactory().setForceRead(false).setUsePlainRandomAccess(Document.plainRandomAccess).createBestSource(filename), 
false, 
null, 
certificate, 
certificateKey, 
certificateKeyProvider, 
null, 
true) {

}


/**
       * Reads and parses a PDF document.
       * @param filename the file name of the document
      * * 
 * @param certificate
      * * 
 * @param externalDecryptionProcess
      * * 
 * @throws IOException on error
 */
    @Throws(IOException::class)
 constructor(filename:String, certificate:Certificate, externalDecryptionProcess:ExternalDecryptionProcess) : this(
RandomAccessSourceFactory().setForceRead(false).setUsePlainRandomAccess(Document.plainRandomAccess).createBestSource(filename), 
false, 
null, 
certificate, 
null, 
null, 
externalDecryptionProcess, 
true) {

}

/**
       * Reads and parses a PDF document.
     
       * @param pdfIn the document as a byte array
      * * 
 * @param certificate
      * * 
 * @param externalDecryptionProcess
      * * 
 * @throws IOException on error
 */
    @Throws(IOException::class)
 constructor(pdfIn:ByteArray, certificate:Certificate, externalDecryptionProcess:ExternalDecryptionProcess) : this(
RandomAccessSourceFactory().setForceRead(false).setUsePlainRandomAccess(Document.plainRandomAccess).createSource(pdfIn), 
false, 
null, 
certificate, 
null, 
null, 
externalDecryptionProcess, 
true) {

}

/**
       * Reads and parses a PDF document.
     
       * @param inputStream the PDF file
      * * 
 * @param certificate
      * * 
 * @param externalDecryptionProcess
      * * 
 * @throws IOException on error
 */
    @Throws(IOException::class)
 constructor(inputStream:InputStream, certificate:Certificate, externalDecryptionProcess:ExternalDecryptionProcess) : this(RandomAccessSourceFactory().setForceRead(false).setUsePlainRandomAccess(Document.plainRandomAccess).createSource(inputStream), 
false, 
null, 
certificate, 
null, 
null, 
externalDecryptionProcess, 
true) {}

/**
       * Reads and parses a PDF document.
       * @param url the URL of the document
      * * 
 * @param ownerPassword the password to read the document
      * * 
 * @throws IOException on error
 */
    @Throws(IOException::class)
@JvmOverloads  constructor(url:URL, ownerPassword:ByteArray? = null) : this(
RandomAccessSourceFactory().createSource(url), 
false, 
ownerPassword, 
null, 
null, 
null, 
null, 
true) {

}

/**
       * Reads and parses a PDF document.
       * @param is the InputStream containing the document. The stream is read to the
      * * end but is not closed
      * * 
 * @param ownerPassword the password to read the document
      * * 
 * @throws IOException on error
 */
    @Throws(IOException::class)
@JvmOverloads  constructor(`is`:InputStream, ownerPassword:ByteArray? = null) : this(
RandomAccessSourceFactory().createSource(`is`), 
false, 
ownerPassword, 
null, 
null, 
null, 
null, 
false) {

}

/**
       * Reads and parses a pdf document.
       * @param raf the document location
      * * 
 * @param ownerPassword the password or null for no password
      * * 
 * @param partial indicates if the reader needs to read the document only partially. See [PdfReader.PdfReader]
      * * 
 * @throws IOException on error
 */
    @Throws(IOException::class)
@JvmOverloads  constructor(raf:RandomAccessFileOrArray, ownerPassword:ByteArray, partial:Boolean = true) : this(
raf.byteSource, 
partial, 
ownerPassword, 
null, 
null, 
null, 
null, 
false) {}

/** Creates an independent duplicate.
       * @param reader the PdfReader to duplicate
 */
     constructor(reader:PdfReader) {
this.isAppendable = reader.isAppendable
this.consolidateNamedDestinations = reader.consolidateNamedDestinations
this.isEncrypted = reader.isEncrypted
this.isRebuilt = reader.isRebuilt
this.sharedStreams = reader.sharedStreams
this.isTampered = reader.isTampered
this.password = reader.password
this.pdfVersion = reader.pdfVersion
this.eofPos = reader.eofPos
this.freeXref = reader.freeXref
this.lastXref = reader.lastXref
this.isNewXrefType = reader.isNewXrefType
this.tokens = PRTokeniser(reader.tokens.safeFile)
if (reader.decrypt != null)
this.decrypt = PdfEncryption(reader.decrypt)
this.permissions = reader.permissions
this.rValue = reader.rValue
this.xrefObj = ArrayList(reader.xrefObj)
for (k in reader.xrefObj.indices)
{
this.xrefObj.set(k, duplicatePdfObject(reader.xrefObj[k], this))
}
this.pageRefs = PageRefs(reader.pageRefs, this)
this.trailer = duplicatePdfObject(reader.trailer, this) as PdfDictionary?
this.catalog = trailer!!.getAsDict(PdfName.ROOT)
this.rootPages = catalog!!.getAsDict(PdfName.PAGES)
this.fileLength = reader.fileLength
this.partial = reader.partial
this.isHybridXref = reader.isHybridXref
this.objStmToOffset = reader.objStmToOffset
this.xref = reader.xref
this.cryptoRef = duplicatePdfObject(reader.cryptoRef, this) as PRIndirectReference?
this.ownerPasswordUsed = reader.ownerPasswordUsed
}

/** Gets a new file instance of the original PDF
       * document.
       * @return a new file instance of the original PDF document
 */
     val safeFile:RandomAccessFileOrArray
get() =tokens.safeFile

protected fun getPdfReaderInstance(writer:PdfWriter):PdfReaderInstance {
return PdfReaderInstance(this, writer)
}

/** Gets the number of pages in the document.
       * @return the number of pages in the document
 */
     val numberOfPages:Int
get() =pageRefs.size()

/**
       * Returns the document's acroform, if it has one.
       * @return the document's acroform
 */
     fun getAcroForm():PRAcroForm {
if (!acroFormParsed)
{
acroFormParsed = true
val form = catalog!!.get(PdfName.ACROFORM)
if (form != null)
{
try
{
acroForm = PRAcroForm(this)
acroForm!!.readAcroForm(getPdfObject(form) as PdfDictionary?)
}
catch (e:Exception) {
acroForm = null
}

}
}
return acroForm
}
/**
       * Gets the page rotation. This value can be 0, 90, 180 or 270.
       * @param index the page number. The first page is 1
      * * 
 * @return the page rotation
 */
     fun getPageRotation(index:Int):Int {
return getPageRotation(pageRefs.getPageNRelease(index))
}

internal fun getPageRotation(page:PdfDictionary):Int {
val rotate = page.getAsNumber(PdfName.ROTATE)
if (rotate == null)
return 0
else
{
var n = rotate.intValue()
n %= 360
return if (n < 0) n + 360 else n
}
}
/** Gets the page size, taking rotation into account. This
       * is a Rectangle with the value of the /MediaBox and the /Rotate key.
       * @param index the page number. The first page is 1
      * * 
 * @return a Rectangle
 */
     fun getPageSizeWithRotation(index:Int):Rectangle {
return getPageSizeWithRotation(pageRefs.getPageNRelease(index))
}

/**
       * Gets the rotated page from a page dictionary.
       * @param page the page dictionary
      * * 
 * @return the rotated page
 */
     fun getPageSizeWithRotation(page:PdfDictionary):Rectangle {
var rect = getPageSize(page)
var rotation = getPageRotation(page)
while (rotation > 0)
{
rect = rect.rotate()
rotation -= 90
}
return rect
}

/** Gets the page size without taking rotation into account. This
       * is the value of the /MediaBox key.
       * @param index the page number. The first page is 1
      * * 
 * @return the page size
 */
     fun getPageSize(index:Int):Rectangle {
return getPageSize(pageRefs.getPageNRelease(index))
}

/**
       * Gets the page from a page dictionary
       * @param page the page dictionary
      * * 
 * @return the page
 */
     fun getPageSize(page:PdfDictionary):Rectangle {
val mediaBox = page.getAsArray(PdfName.MEDIABOX)
return getNormalizedRectangle(mediaBox)
}

/** Gets the crop box without taking rotation into account. This
       * is the value of the /CropBox key. The crop box is the part
       * of the document to be displayed or printed. It usually is the same
       * as the media box but may be smaller. If the page doesn't have a crop
       * box the page size will be returned.
       * @param index the page number. The first page is 1
      * * 
 * @return the crop box
 */
     fun getCropBox(index:Int):Rectangle {
val page = pageRefs.getPageNRelease(index)
val cropBox = getPdfObjectRelease(page.get(PdfName.CROPBOX)) as PdfArray? ?: return getPageSize(page)
return getNormalizedRectangle(cropBox)
}

/** Gets the box size. Allowed names are: "crop", "trim", "art", "bleed" and "media".
       * @param index the page number. The first page is 1
      * * 
 * @param boxName the box name
      * * 
 * @return the box rectangle or null
 */
     fun getBoxSize(index:Int, boxName:String):Rectangle? {
val page = pageRefs.getPageNRelease(index)
var box:PdfArray? = null
if (boxName == "trim")
box = getPdfObjectRelease(page.get(PdfName.TRIMBOX)) as PdfArray?
else if (boxName == "art")
box = getPdfObjectRelease(page.get(PdfName.ARTBOX)) as PdfArray?
else if (boxName == "bleed")
box = getPdfObjectRelease(page.get(PdfName.BLEEDBOX)) as PdfArray?
else if (boxName == "crop")
box = getPdfObjectRelease(page.get(PdfName.CROPBOX)) as PdfArray?
else if (boxName == "media")
box = getPdfObjectRelease(page.get(PdfName.MEDIABOX)) as PdfArray?
if (box == null)
return null
return getNormalizedRectangle(box)
}

/**
       * Returns the content of the document information dictionary as a HashMap
       * of String.
       * @return content of the document information dictionary
 */
     val info:HashMap<String, String>
get() {
val map = HashMap<String, String>()
val info = trailer!!.getAsDict(PdfName.INFO) ?: return map
for (element in info.keys)
{
val obj = getPdfObject(info.get(element)) ?: continue
var value = obj.toString()
when (obj.type()) {
PdfObject.STRING -> {
value = (obj as PdfString).toUnicodeString()
}
PdfObject.NAME -> {
value = PdfName.decodeName(value)
}
}
map.put(PdfName.decodeName(element.toString()), value)
}
return map
}

/**
       * Checks if the PDF is a tagged PDF.
      */
     val isTagged:Boolean
get() {
val markInfo = catalog!!.getAsDict(PdfName.MARKINFO) ?: return false
if (PdfBoolean.PDFTRUE == markInfo.getAsBoolean(PdfName.MARKED))
{
return catalog!!.getAsDict(PdfName.STRUCTTREEROOT) != null
}
else
{
return false
}
}

/**
       * Parses the entire PDF
      */
    @Throws(IOException::class)
protected open fun readPdf() {
fileLength = tokens.file.length()
pdfVersion = tokens.checkPdfHeader()
try
{
readXref()
}
catch (e:Exception) {
try
{
isRebuilt = true
rebuildXref()
lastXref = -1
}
catch (ne:Exception) {
throw InvalidPdfException(MessageLocalization.getComposedMessage("rebuild.failed.1.original.message.2", ne.message, e.message))
}

}

try
{
readDocObj()
}
catch (e:Exception) {
if (e is BadPasswordException)
throw BadPasswordException(e.message)
if (isRebuilt || encryptionError)
throw InvalidPdfException(e.message)
isRebuilt = true
isEncrypted = false
try
{
rebuildXref()
lastXref = -1
readDocObj()
}
catch (ne:Exception) {
throw InvalidPdfException(MessageLocalization.getComposedMessage("rebuild.failed.1.original.message.2", ne.message, e.message))
}

}

strings!!.clear()
readPages()
//eliminateSharedStreams();
        removeUnusedObjects()

}

@Throws(IOException::class)
protected fun readPdfPartial() {
fileLength = tokens.file.length()
pdfVersion = tokens.checkPdfHeader()
try
{
readXref()
}
catch (e:Exception) {
try
{
isRebuilt = true
rebuildXref()
lastXref = -1
}
catch (ne:Exception) {
throw InvalidPdfException(
MessageLocalization.getComposedMessage(
"rebuild.failed.1.original.message.2", 
ne.message, e.message), ne)
}

}

readDocObjPartial()
readPages()
}

private fun equalsArray(ar1:ByteArray, ar2:ByteArray, size:Int):Boolean {
for (k in 0..size - 1)
{
if (ar1[k] != ar2[k])
return false
}
return true
}

/**
       * @throws IOException
 */
    @SuppressWarnings("unchecked")
@Throws(IOException::class)
private fun readDecryptedDocObj() {
if (isEncrypted)
return 
val encDic = trailer!!.get(PdfName.ENCRYPT)
if (encDic == null || encDic.toString() == "null")
return 
encryptionError = true
var encryptionKey:ByteArray? = null
isEncrypted = true
val enc = getPdfObject(encDic) as PdfDictionary?

var s:String
var o:PdfObject

val documentIDs = trailer!!.getAsArray(PdfName.ID)
var documentID:ByteArray? = null
if (documentIDs != null)
{
o = documentIDs.getPdfObject(0)
strings!!.remove(o)
s = o.toString()
documentID = com.itextpdf.text.DocWriter.getISOBytes(s)
if (documentIDs.size() > 1)
strings!!.remove(documentIDs.getPdfObject(1))
}
// just in case we have a broken producer
        if (documentID == null)
documentID = ByteArray(0)
var uValue:ByteArray? = null
var oValue:ByteArray? = null
var cryptoMode = PdfWriter.STANDARD_ENCRYPTION_40
var lengthValue = 0

val filter = getPdfObjectRelease(enc.get(PdfName.FILTER))

if (filter == PdfName.STANDARD)
{
s = enc.get(PdfName.U)!!.toString()
strings!!.remove(enc.get(PdfName.U))
uValue = com.itextpdf.text.DocWriter.getISOBytes(s)
s = enc.get(PdfName.O)!!.toString()
strings!!.remove(enc.get(PdfName.O))
oValue = com.itextpdf.text.DocWriter.getISOBytes(s)
if (enc.contains(PdfName.OE))
strings!!.remove(enc.get(PdfName.OE))
if (enc.contains(PdfName.UE))
strings!!.remove(enc.get(PdfName.UE))
if (enc.contains(PdfName.PERMS))
strings!!.remove(enc.get(PdfName.PERMS))

o = enc.get(PdfName.P)
if (!o.isNumber)
throw InvalidPdfException(MessageLocalization.getComposedMessage("illegal.p.value"))
permissions = (o as PdfNumber).longValue()

o = enc.get(PdfName.R)
if (!o.isNumber)
throw InvalidPdfException(MessageLocalization.getComposedMessage("illegal.r.value"))
rValue = (o as PdfNumber).intValue()

when (rValue) {
2 -> cryptoMode = PdfWriter.STANDARD_ENCRYPTION_40
3 -> {
o = enc.get(PdfName.LENGTH)
if (!o.isNumber)
throw InvalidPdfException(MessageLocalization.getComposedMessage("illegal.length.value"))
lengthValue = (o as PdfNumber).intValue()
if (lengthValue > 128 || lengthValue < 40 || lengthValue % 8 != 0)
throw InvalidPdfException(MessageLocalization.getComposedMessage("illegal.length.value"))
cryptoMode = PdfWriter.STANDARD_ENCRYPTION_128
}
4 -> {
var dic:PdfDictionary? = enc.get(PdfName.CF) as PdfDictionary? ?: throw InvalidPdfException(MessageLocalization.getComposedMessage("cf.not.found.encryption"))
dic = dic.get(PdfName.STDCF) as PdfDictionary?
if (dic == null)
throw InvalidPdfException(MessageLocalization.getComposedMessage("stdcf.not.found.encryption"))
if (PdfName.V2 == dic.get(PdfName.CFM))
cryptoMode = PdfWriter.STANDARD_ENCRYPTION_128
else if (PdfName.AESV2 == dic.get(PdfName.CFM))
cryptoMode = PdfWriter.ENCRYPTION_AES_128
else
throw UnsupportedPdfException(MessageLocalization.getComposedMessage("no.compatible.encryption.found"))
val em = enc.get(PdfName.ENCRYPTMETADATA)
if (em != null && em.toString() == "false")
cryptoMode = cryptoMode or PdfWriter.DO_NOT_ENCRYPT_METADATA
}
5 -> {
cryptoMode = PdfWriter.ENCRYPTION_AES_256
val em5 = enc.get(PdfName.ENCRYPTMETADATA)
if (em5 != null && em5.toString() == "false")
cryptoMode = cryptoMode or PdfWriter.DO_NOT_ENCRYPT_METADATA
}
else -> throw UnsupportedPdfException(MessageLocalization.getComposedMessage("unknown.encryption.type.r.eq.1", rValue))
}
}
else if (filter == PdfName.PUBSEC)
{
var foundRecipient = false
var envelopedData:ByteArray? = null
var recipients:PdfArray? = null

o = enc.get(PdfName.V)
if (!o.isNumber)
throw InvalidPdfException(MessageLocalization.getComposedMessage("illegal.v.value"))
val vValue = (o as PdfNumber).intValue()
when (vValue) {
1 -> {
cryptoMode = PdfWriter.STANDARD_ENCRYPTION_40
lengthValue = 40
recipients = enc.get(PdfName.RECIPIENTS) as PdfArray?
}
2 -> {
o = enc.get(PdfName.LENGTH)
if (!o.isNumber)
throw InvalidPdfException(MessageLocalization.getComposedMessage("illegal.length.value"))
lengthValue = (o as PdfNumber).intValue()
if (lengthValue > 128 || lengthValue < 40 || lengthValue % 8 != 0)
throw InvalidPdfException(MessageLocalization.getComposedMessage("illegal.length.value"))
cryptoMode = PdfWriter.STANDARD_ENCRYPTION_128
recipients = enc.get(PdfName.RECIPIENTS) as PdfArray?
}
4, 5 -> {
var dic:PdfDictionary? = enc.get(PdfName.CF) as PdfDictionary? ?: throw InvalidPdfException(MessageLocalization.getComposedMessage("cf.not.found.encryption"))
dic = dic.get(PdfName.DEFAULTCRYPTFILTER) as PdfDictionary?
if (dic == null)
throw InvalidPdfException(MessageLocalization.getComposedMessage("defaultcryptfilter.not.found.encryption"))
if (PdfName.V2 == dic.get(PdfName.CFM))
{
cryptoMode = PdfWriter.STANDARD_ENCRYPTION_128
lengthValue = 128
}
else if (PdfName.AESV2 == dic.get(PdfName.CFM))
{
cryptoMode = PdfWriter.ENCRYPTION_AES_128
lengthValue = 128
}
else if (PdfName.AESV3 == dic.get(PdfName.CFM))
{
cryptoMode = PdfWriter.ENCRYPTION_AES_256
lengthValue = 256
}
else
throw UnsupportedPdfException(MessageLocalization.getComposedMessage("no.compatible.encryption.found"))
val em = dic.get(PdfName.ENCRYPTMETADATA)
if (em != null && em.toString() == "false")
cryptoMode = cryptoMode or PdfWriter.DO_NOT_ENCRYPT_METADATA

recipients = dic.get(PdfName.RECIPIENTS) as PdfArray?
}
else -> throw UnsupportedPdfException(MessageLocalization.getComposedMessage("unknown.encryption.type.v.eq.1", vValue))
}
val certHolder:X509CertificateHolder
try
{
certHolder = X509CertificateHolder(certificate!!.encoded)
}
catch (f:Exception) {
throw ExceptionConverter(f)
}

if (externalDecryptionProcess == null)
{
for (i in 0..recipients!!.size() - 1)
{
val recipient = recipients.getPdfObject(i)
strings!!.remove(recipient)

var data:CMSEnvelopedData? = null
try
{
data = CMSEnvelopedData(recipient.bytes)

val recipientCertificatesIt = data.recipientInfos.recipients.iterator()

while (recipientCertificatesIt.hasNext())
{
val recipientInfo = recipientCertificatesIt.next()

if (recipientInfo.rid.match(certHolder) && !foundRecipient)
{
envelopedData = PdfEncryptor.getContent(recipientInfo, certificateKey as PrivateKey?, certificateKeyProvider)
foundRecipient = true
}
}

}
catch (f:Exception) {
throw ExceptionConverter(f)
}

}
}
else
{
for (i in 0..recipients!!.size() - 1)
{
val recipient = recipients.getPdfObject(i)
strings!!.remove(recipient)

var data:CMSEnvelopedData? = null
try
{
data = CMSEnvelopedData(recipient.bytes)

val recipientInfo = data.recipientInfos.get(externalDecryptionProcess!!.cmsRecipientId)

if (recipientInfo != null)
{
envelopedData = recipientInfo.getContent(externalDecryptionProcess!!.cmsRecipient)
foundRecipient = true
}
}
catch (f:Exception) {
throw ExceptionConverter(f)
}

}
}

if (!foundRecipient || envelopedData == null)
{
throw UnsupportedPdfException(MessageLocalization.getComposedMessage("bad.certificate.and.key"))
}

var md:MessageDigest? = null

try
{
if (cryptoMode and PdfWriter.ENCRYPTION_MASK == PdfWriter.ENCRYPTION_AES_256)
md = MessageDigest.getInstance("SHA-256")
else
md = MessageDigest.getInstance("SHA-1")
md!!.update(envelopedData, 0, 20)
for (i in 0..recipients.size() - 1)
{
val encodedRecipient = recipients.getPdfObject(i).bytes
md.update(encodedRecipient)
}
if (cryptoMode and PdfWriter.DO_NOT_ENCRYPT_METADATA != 0)
md.update(byteArrayOf(255.toByte(), 255.toByte(), 255.toByte(), 255.toByte()))
encryptionKey = md.digest()
}
catch (f:Exception) {
throw ExceptionConverter(f)
}

}


decrypt = PdfEncryption()
decrypt!!.setCryptoMode(cryptoMode, lengthValue)

if (filter == PdfName.STANDARD)
{
if (rValue == 5)
{
ownerPasswordUsed = decrypt!!.readKey(enc, password)
decrypt!!.documentID = documentID
permissions = decrypt!!.permissions
}
else
{
//check by owner password
                decrypt!!.setupByOwnerPassword(documentID, password, uValue, oValue, permissions)
if (!equalsArray(uValue, decrypt!!.userKey, if (rValue == 3 || rValue == 4) 16 else 32))
{
//check by user password
                    decrypt!!.setupByUserPassword(documentID, password, oValue, permissions)
if (!equalsArray(uValue, decrypt!!.userKey, if (rValue == 3 || rValue == 4) 16 else 32))
{
throw BadPasswordException(MessageLocalization.getComposedMessage("bad.user.password"))
}
}
else
ownerPasswordUsed = true
}
}
else if (filter == PdfName.PUBSEC)
{
if (cryptoMode and PdfWriter.ENCRYPTION_MASK == PdfWriter.ENCRYPTION_AES_256)
decrypt!!.setKey(encryptionKey)
else
decrypt!!.setupByEncryptionKey(encryptionKey, lengthValue)
ownerPasswordUsed = true
}

for (k in strings!!.indices)
{
val str = strings!![k]
str.decrypt(this)
}

if (encDic.isIndirect)
{
cryptoRef = encDic as PRIndirectReference?
xrefObj.set(cryptoRef!!.number, null)
}
encryptionError = false
}

/**
       * @param idx
      * * 
 * @return a PdfObject
 */
     fun getPdfObjectRelease(idx:Int):PdfObject? {
val obj = getPdfObject(idx)
releaseLastXrefPartial()
return obj
}

/**
       * @param idx
      * * 
 * @return aPdfObject
 */
     fun getPdfObject(idx:Int):PdfObject? {
try
{
lastXrefPartial = -1
if (idx < 0 || idx >= xrefObj.size)
return null
var obj:PdfObject? = xrefObj[idx]
if (!partial || obj != null)
return obj
if (idx * 2 >= xref!!.size)
return null
obj = readSingleObject(idx)
lastXrefPartial = -1
if (obj != null)
lastXrefPartial = idx
return obj
}
catch (e:Exception) {
throw ExceptionConverter(e)
}

}

/**
     
      */
     fun resetLastXrefPartial() {
lastXrefPartial = -1
}

/**
     
      */
     fun releaseLastXrefPartial() {
if (partial && lastXrefPartial != -1)
{
xrefObj.set(lastXrefPartial, null)
lastXrefPartial = -1
}
}

private fun setXrefPartialObject(idx:Int, obj:PdfObject) {
if (!partial || idx < 0)
return 
xrefObj[idx] = obj
}

/**
       * @param obj
      * * 
 * @return an indirect reference
 */
     fun addPdfObject(obj:PdfObject):PRIndirectReference {
xrefObj.add(obj)
return PRIndirectReference(this, xrefObj.size - 1)
}

@Throws(IOException::class)
protected fun readPages() {
catalog = trailer!!.getAsDict(PdfName.ROOT)
if (catalog == null)
{
throw InvalidPdfException(MessageLocalization.getComposedMessage("the.document.has.no.catalog.object"))
}
rootPages = catalog!!.getAsDict(PdfName.PAGES)
if (rootPages == null || PdfName.PAGES != rootPages!!.get(PdfName.TYPE))
{
if (debugmode)
{
if (LOGGER.isLogging(Level.ERROR))
{
LOGGER.error(MessageLocalization.getComposedMessage("the.document.has.no.page.root"))
}
}
else
{
throw InvalidPdfException(MessageLocalization.getComposedMessage("the.document.has.no.page.root"))
}
}
pageRefs = PageRefs(this)
}

@Throws(IOException::class)
protected fun readDocObjPartial() {
xrefObj = ArrayList<PdfObject>(xref!!.size / 2)
xrefObj.addAll(Collections.nCopies<PdfObject>(xref!!.size / 2, null))
readDecryptedDocObj()
if (objStmToOffset != null)
{
val keys = objStmToOffset!!.keys
for (k in keys.indices)
{
val n = keys[k]
objStmToOffset!!.put(n, xref!![(n * 2).toInt()])
xref[(n * 2).toInt()] = -1
}
}
}

@Throws(IOException::class)
protected fun readSingleObject(k:Int):PdfObject? {
strings!!.clear()
val k2 = k * 2
var pos = xref!![k2]
if (pos < 0)
return null
if (xref!![k2 + 1] > 0)
pos = objStmToOffset!!.get(xref!![k2 + 1])
if (pos == 0)
return null
tokens.seek(pos)
tokens.nextValidToken()
if (tokens.tokenType != TokenType.NUMBER)
tokens.throwError(MessageLocalization.getComposedMessage("invalid.object.number"))
objNum = tokens.intValue()
tokens.nextValidToken()
if (tokens.tokenType != TokenType.NUMBER)
tokens.throwError(MessageLocalization.getComposedMessage("invalid.generation.number"))
objGen = tokens.intValue()
tokens.nextValidToken()
if (tokens.stringValue != "obj")
tokens.throwError(MessageLocalization.getComposedMessage("token.obj.expected"))
var obj:PdfObject?
try
{
obj = readPRObject()
for (j in strings!!.indices)
{
val str = strings!![j]
str.decrypt(this)
}
if (obj.isStream)
{
checkPRStreamLength(obj as PRStream?)
}
}
catch (e:IOException) {
if (debugmode)
{
if (LOGGER.isLogging(Level.ERROR))
LOGGER.error(e.message, e)
obj = null
}
else
throw e
}

if (xref!![k2 + 1] > 0)
{
obj = readOneObjStm(obj as PRStream?, xref!![k2].toInt())
}
xrefObj.set(k, obj)
return obj
}

@Throws(IOException::class)
protected fun readOneObjStm(stream:PRStream, idx:Int):PdfObject {
var idx = idx
val first = stream.getAsNumber(PdfName.FIRST).intValue()
val b = getStreamBytes(stream, tokens.file)
val saveTokens = tokens
tokens = PRTokeniser(RandomAccessFileOrArray(RandomAccessSourceFactory().createSource(b)))
try
{
var address = 0
var ok = true
++idx
for (k in 0..idx - 1)
{
ok = tokens.nextToken()
if (!ok)
break
if (tokens.tokenType != TokenType.NUMBER)
{
ok = false
break
}
ok = tokens.nextToken()
if (!ok)
break
if (tokens.tokenType != TokenType.NUMBER)
{
ok = false
break
}
address = tokens.intValue() + first
}
if (!ok)
throw InvalidPdfException(MessageLocalization.getComposedMessage("error.reading.objstm"))
tokens.seek(address.toLong())
tokens.nextToken()
val obj:PdfObject
if (tokens.tokenType == PRTokeniser.TokenType.NUMBER)
{
obj = PdfNumber(tokens.stringValue)
}
else
{
tokens.seek(address.toLong())
obj = readPRObject()
}
return obj
//return readPRObject();
        }

finally
{
tokens = saveTokens
}
}

/**
       * @return the percentage of the cross reference table that has been read
 */
     fun dumpPerc():Double {
var total = 0
for (k in xrefObj.indices)
{
if (xrefObj[k] != null)
++total
}
return total * 100.0 / xrefObj.size
}

@Throws(IOException::class)
protected fun readDocObj() {
val streams = ArrayList<PRStream>()
xrefObj = ArrayList<PdfObject>(xref!!.size / 2)
xrefObj.addAll(Collections.nCopies<PdfObject>(xref!!.size / 2, null))
run{
            var k = 2
            while (k < xref!!.size) {
                val pos = xref!![k]
                if (pos <= 0 || xref!![k + 1] > 0) {
                    k += 2
                    continue
                }
                tokens.seek(pos)
                tokens.nextValidToken()
                if (tokens.tokenType != TokenType.NUMBER)
                    tokens.throwError(MessageLocalization.getComposedMessage("invalid.object.number"))
                objNum = tokens.intValue()
                tokens.nextValidToken()
                if (tokens.tokenType != TokenType.NUMBER)
                    tokens.throwError(MessageLocalization.getComposedMessage("invalid.generation.number"))
                objGen = tokens.intValue()
                tokens.nextValidToken()
                if (tokens.stringValue != "obj")
                    tokens.throwError(MessageLocalization.getComposedMessage("token.obj.expected"))
                val obj: PdfObject?
                try {
                    obj = readPRObject()
                    if (obj.isStream) {
                        streams.add(obj as PRStream?)
                    }
                } catch (e: IOException) {
                    if (debugmode) {
                        if (LOGGER.isLogging(Level.ERROR))
                            LOGGER.error(e.message, e)
                        obj = null
                    } else
                        throw e
                }

                xrefObj.set(k / 2, obj)
                k += 2
            }
        }
for (k in streams.indices)
{
checkPRStreamLength(streams[k])
}
readDecryptedDocObj()
if (objStmMark != null)
{
for (entry in objStmMark!!.entries)
{
val n = entry.key.toInt()
val h = entry.value
readObjStm(xrefObj[n] as PRStream, h)
xrefObj.set(n, null)
}
objStmMark = null
}
xref = null
}

@Throws(IOException::class)
private fun checkPRStreamLength(stream:PRStream) {
val fileLength = tokens.length()
val start = stream.offset
var calc = false
var streamLength:Long = 0
val obj = getPdfObjectRelease(stream.get(PdfName.LENGTH))
if (obj != null && obj.type() == PdfObject.NUMBER)
{
streamLength = (obj as PdfNumber).intValue().toLong()
if (streamLength + start > fileLength - 20)
calc = true
else
{
tokens.seek(start + streamLength)
val line = tokens.readString(20)
if (!line.startsWith("\nendstream") && 
!line.startsWith("\r\nendstream") && 
!line.startsWith("\rendstream") && 
!line.startsWith("endstream"))
calc = true
}
}
else
calc = true
if (calc)
{
val tline = ByteArray(16)
tokens.seek(start)
var pos:Long
while (true)
{
pos = tokens.filePointer
if (!tokens.readLineSegment(tline, false))
// added boolean because of mailing list issue (17 Feb. 2014)
                    break
if (equalsn(tline, endstream))
{
streamLength = pos - start
break
}
if (equalsn(tline, endobj))
{
tokens.seek(pos - 16)
val s = tokens.readString(16)
val index = s.indexOf("endstream")
if (index >= 0)
pos = pos - 16 + index
streamLength = pos - start
break
}
}
tokens.seek(pos - 2)
if (tokens.read() == 13)
streamLength--
tokens.seek(pos - 1)
if (tokens.read() == 10)
streamLength--

if (streamLength < 0)
{
streamLength = 0
}
}
stream.length = streamLength.toInt()
}

@Throws(IOException::class)
protected fun readObjStm(stream:PRStream?, map:IntHashtable) {
if (stream == null) return 
val first = stream.getAsNumber(PdfName.FIRST).intValue()
val n = stream.getAsNumber(PdfName.N).intValue()
val b = getStreamBytes(stream, tokens.file)
val saveTokens = tokens
tokens = PRTokeniser(RandomAccessFileOrArray(RandomAccessSourceFactory().createSource(b)))
try
{
val address = IntArray(n)
val objNumber = IntArray(n)
var ok = true
for (k in 0..n - 1)
{
ok = tokens.nextToken()
if (!ok)
break
if (tokens.tokenType != TokenType.NUMBER)
{
ok = false
break
}
objNumber[k] = tokens.intValue()
ok = tokens.nextToken()
if (!ok)
break
if (tokens.tokenType != TokenType.NUMBER)
{
ok = false
break
}
address[k] = tokens.intValue() + first
}
if (!ok)
throw InvalidPdfException(MessageLocalization.getComposedMessage("error.reading.objstm"))
for (k in 0..n - 1)
{
if (map.containsKey(k))
{
tokens.seek(address[k].toLong())
tokens.nextToken()
val obj:PdfObject
if (tokens.tokenType == PRTokeniser.TokenType.NUMBER)
{
obj = PdfNumber(tokens.stringValue)
}
else
{
tokens.seek(address[k].toLong())
obj = readPRObject()
}
xrefObj[objNumber[k]] = obj
}
}
}

finally
{
tokens = saveTokens
}
}

private fun ensureXrefSize(size:Int) {
if (size == 0)
return 
if (xref == null)
xref = LongArray(size)
else
{
if (xref!!.size < size)
{
val xref2 = LongArray(size)
System.arraycopy(xref, 0, xref2, 0, xref!!.size)
xref = xref2
}
}
}

@Throws(IOException::class)
protected fun readXref() {
isHybridXref = false
isNewXrefType = false
tokens.seek(tokens.startxref)
tokens.nextToken()
if (tokens.stringValue != "startxref")
throw InvalidPdfException(MessageLocalization.getComposedMessage("startxref.not.found"))
tokens.nextToken()
if (tokens.tokenType != TokenType.NUMBER)
throw InvalidPdfException(MessageLocalization.getComposedMessage("startxref.is.not.followed.by.a.number"))
var startxref = tokens.longValue()
lastXref = startxref
eofPos = tokens.filePointer
try
{
if (readXRefStream(startxref))
{
isNewXrefType = true
return 
}
}
catch (e:Exception) {}

xref = null
tokens.seek(startxref)
trailer = readXrefSection()
var trailer2:PdfDictionary = trailer
while (true)
{
val prev = trailer2.get(PdfName.PREV) as PdfNumber? ?: break
if (prev.longValue() == startxref)
throw InvalidPdfException(MessageLocalization.getComposedMessage("trailer.prev.entry.points.to.its.own.cross.reference.section"))
startxref = prev.longValue()
tokens.seek(startxref)
trailer2 = readXrefSection()
}
}

@Throws(IOException::class)
protected fun readXrefSection():PdfDictionary {
tokens.nextValidToken()
if (tokens.stringValue != "xref")
tokens.throwError(MessageLocalization.getComposedMessage("xref.subsection.not.found"))
var start = 0
var end = 0
var pos:Long = 0
var gen = 0
while (true)
{
tokens.nextValidToken()
if (tokens.stringValue == "trailer")
break
if (tokens.tokenType != TokenType.NUMBER)
tokens.throwError(MessageLocalization.getComposedMessage("object.number.of.the.first.object.in.this.xref.subsection.not.found"))
start = tokens.intValue()
tokens.nextValidToken()
if (tokens.tokenType != TokenType.NUMBER)
tokens.throwError(MessageLocalization.getComposedMessage("number.of.entries.in.this.xref.subsection.not.found"))
end = tokens.intValue() + start
if (start == 1)
{ // fix incorrect start number
val back = tokens.filePointer
tokens.nextValidToken()
pos = tokens.longValue()
tokens.nextValidToken()
gen = tokens.intValue()
if (pos == 0 && gen == PdfWriter.GENERATION_MAX)
{
--start
--end
}
tokens.seek(back)
}
ensureXrefSize(end * 2)
for (k in start..end - 1)
{
tokens.nextValidToken()
pos = tokens.longValue()
tokens.nextValidToken()
gen = tokens.intValue()
tokens.nextValidToken()
val p = k * 2
if (tokens.stringValue == "n")
{
if (xref!![p] == 0 && xref!![p + 1] == 0)
{
//                        if (pos == 0)
//                            tokens.throwError(MessageLocalization.getComposedMessage("file.position.0.cross.reference.entry.in.this.xref.subsection"));
                        xref[p] = pos
}
}
else if (tokens.stringValue == "f")
{
if (xref!![p] == 0 && xref!![p + 1] == 0)
xref[p] = -1
}
else
tokens.throwError(MessageLocalization.getComposedMessage("invalid.cross.reference.entry.in.this.xref.subsection"))
}
}
val trailer = readPRObject() as PdfDictionary
val xrefSize = trailer.get(PdfName.SIZE) as PdfNumber?
ensureXrefSize(xrefSize.intValue() * 2)
val xrs = trailer.get(PdfName.XREFSTM)
if (xrs != null && xrs.isNumber)
{
val loc = (xrs as PdfNumber).intValue()
try
{
readXRefStream(loc.toLong())
isNewXrefType = true
isHybridXref = true
}
catch (e:IOException) {
xref = null
throw e
}

}
return trailer
}

@Throws(IOException::class)
protected fun readXRefStream(ptr:Long):Boolean {
tokens.seek(ptr)
var thisStream = 0
if (!tokens.nextToken())
return false
if (tokens.tokenType != TokenType.NUMBER)
return false
thisStream = tokens.intValue()
if (!tokens.nextToken() || tokens.tokenType != TokenType.NUMBER)
return false
if (!tokens.nextToken() || tokens.stringValue != "obj")
return false
val `object` = readPRObject()
var stm:PRStream? = null
if (`object`.isStream)
{
stm = `object` as PRStream
if (PdfName.XREF != stm.get(PdfName.TYPE))
return false
}
else
return false
if (trailer == null)
{
trailer = PdfDictionary()
trailer!!.putAll(stm)
}
stm.length = (stm.get(PdfName.LENGTH) as PdfNumber).intValue()
val size = (stm.get(PdfName.SIZE) as PdfNumber).intValue()
val index:PdfArray
var obj = stm.get(PdfName.INDEX)
if (obj == null)
{
index = PdfArray()
index.add(intArrayOf(0, size))
}
else
index = obj as PdfArray?
val w = stm.get(PdfName.W) as PdfArray?
var prev:Long = -1
obj = stm.get(PdfName.PREV)
if (obj != null)
prev = (obj as PdfNumber).longValue()
// Each xref pair is a position
        // type 0 -> -1, 0
        // type 1 -> offset, 0
        // type 2 -> index, obj num
        ensureXrefSize(size * 2)
if (objStmMark == null && !partial)
objStmMark = HashMap<Int, IntHashtable>()
if (objStmToOffset == null && partial)
objStmToOffset = LongHashtable()
val b = getStreamBytes(stm, tokens.file)
var bptr = 0
val wc = IntArray(3)
for (k in 0..2)
wc[k] = w.getAsNumber(k).intValue()
var idx = 0
while (idx < index.size())
{
var start = index.getAsNumber(idx).intValue()
var length = index.getAsNumber(idx + 1).intValue()
ensureXrefSize((start + length) * 2)
while (length-- > 0)
{
var type = 1
if (wc[0] > 0)
{
type = 0
for (k in 0..wc[0] - 1)
type = (type shl 8) + (b[bptr++] and 0xff)
}
var field2:Long = 0
for (k in 0..wc[1] - 1)
field2 = (field2 shl 8) + (b[bptr++] and 0xff)
var field3 = 0
for (k in 0..wc[2] - 1)
field3 = (field3 shl 8) + (b[bptr++] and 0xff)
val base = start * 2
if (xref!![base] == 0 && xref!![base + 1] == 0)
{
when (type) {
0 -> xref[base] = -1
1 -> xref[base] = field2
2 -> {
xref[base] = field3.toLong()
xref[base + 1] = field2
if (partial)
{
objStmToOffset!!.put(field2, 0)
}
else
{
val on = Integer.valueOf(field2.toInt())
var seq:IntHashtable? = objStmMark!![on]
if (seq == null)
{
seq = IntHashtable()
seq.put(field3, 1)
objStmMark!!.put(on, seq)
}
else
seq.put(field3, 1)
}
}
}
}
++start
}
idx += 2
}
thisStream *= 2
if (thisStream + 1 < xref!!.size && xref!![thisStream] == 0 && xref!![thisStream + 1] == 0)
xref[thisStream] = -1

if (prev == -1)
return true
return readXRefStream(prev)
}

@Throws(IOException::class)
protected fun rebuildXref() {
isHybridXref = false
isNewXrefType = false
tokens.seek(0)
var xr = arrayOfNulls<LongArray>(1024)
var top:Long = 0
trailer = null
val line = ByteArray(64)
while (true)
{
var pos = tokens.filePointer
if (!tokens.readLineSegment(line, true))
// added boolean because of mailing list issue (17 Feb. 2014)
                break
if (line[0] == 't')
{
if (!PdfEncodings.convertToString(line, null).startsWith("trailer"))
continue
tokens.seek(pos)
tokens.nextToken()
pos = tokens.filePointer
try
{
val dic = readPRObject() as PdfDictionary
if (dic.get(PdfName.ROOT) != null)
trailer = dic
else
tokens.seek(pos)
}
catch (e:Exception) {
tokens.seek(pos)
}

}
else if (line[0] >= '0' && line[0] <= '9')
{
val obj = PRTokeniser.checkObjectStart(line) ?: continue
val num = obj[0]
val gen = obj[1]
if (num >= xr.size)
{
val newLength = num * 2
val xr2 = arrayOfNulls<LongArray>(newLength.toInt())
System.arraycopy(xr, 0, xr2, 0, top.toInt())
xr = xr2
}
if (num >= top)
top = num + 1
if (xr[num.toInt()] == null || gen >= xr[num.toInt()][1])
{
obj[0] = pos
xr[num.toInt()] = obj
}
}
}
if (trailer == null)
throw InvalidPdfException(MessageLocalization.getComposedMessage("trailer.not.found"))
xref = LongArray((top * 2).toInt())
for (k in 0..top - 1)
{
val obj = xr[k]
if (obj != null)
xref[k * 2] = obj[0]
}
}

@Throws(IOException::class)
protected fun readDictionary():PdfDictionary {
val dic = PdfDictionary()
while (true)
{
tokens.nextValidToken()
if (tokens.tokenType == TokenType.END_DIC)
break
if (tokens.tokenType != TokenType.NAME)
tokens.throwError(MessageLocalization.getComposedMessage("dictionary.key.1.is.not.a.name", tokens.stringValue))
val name = PdfName(tokens.stringValue, false)
val obj = readPRObject()
val type = obj.type()
if (-type == TokenType.END_DIC.ordinal)
tokens.throwError(MessageLocalization.getComposedMessage("unexpected.gt.gt"))
if (-type == TokenType.END_ARRAY.ordinal)
tokens.throwError(MessageLocalization.getComposedMessage("unexpected.close.bracket"))
dic.put(name, obj)
}
return dic
}

@Throws(IOException::class)
protected fun readArray():PdfArray {
val array = PdfArray()
while (true)
{
val obj = readPRObject()
val type = obj.type()
if (-type == TokenType.END_ARRAY.ordinal)
break
if (-type == TokenType.END_DIC.ordinal)
tokens.throwError(MessageLocalization.getComposedMessage("unexpected.gt.gt"))
array.add(obj)
}
return array
}

// Track how deeply nested the current object is, so
    // we know when to return an individual null or boolean, or
    // reuse one of the static ones.
    private var readDepth = 0

@Throws(IOException::class)
protected fun readPRObject():PdfObject {
tokens.nextValidToken()
val type = tokens.tokenType
when (type) {
PRTokeniser.TokenType.START_DIC -> {
run{
            ++readDepth
            val dic = readDictionary()
            --readDepth
            val pos = tokens.filePointer
            // be careful in the trailer. May not be a "next" token.
            var hasNext: Boolean
            do {
                hasNext = tokens.nextToken()
            } while (hasNext && tokens.tokenType == TokenType.COMMENT)

            if (hasNext && tokens.stringValue == "stream") {
                //skip whitespaces
                var ch: Int
                do {
                    ch = tokens.read()
                } while (ch == 32 || ch == 9 || ch == 0 || ch == 12)
                if (ch != '\n')
                    ch = tokens.read()
                if (ch != '\n')
                    tokens.backOnePosition(ch)
                val stream = PRStream(this, tokens.filePointer)
                stream.putAll(dic)
                // crypto handling
                stream.setObjNum(objNum, objGen)

                return stream
            } else {
                tokens.seek(pos)
                return dic
            }
        }
run{
            ++readDepth
            val arr = readArray()
            --readDepth
            return arr
        }
}
PRTokeniser.TokenType.START_ARRAY -> {
++readDepth
val arr = readArray()
--readDepth
return arr
}
PRTokeniser.TokenType.NUMBER -> return PdfNumber(tokens.stringValue)
PRTokeniser.TokenType.STRING -> {
val str = PdfString(tokens.stringValue, null).setHexWriting(tokens.isHexString)
// crypto handling
                str.setObjNum(objNum, objGen)
if (strings != null)
strings!!.add(str)

return str
}
PRTokeniser.TokenType.NAME -> {
run{
            val cachedName = PdfName.staticNames[tokens.stringValue]
            if (readDepth > 0 && cachedName != null) {
                return cachedName
            } else {
                // an indirect name (how odd...), or a non-standard one
                return PdfName(tokens.stringValue, false)
            }
        }
val num = tokens.reference
val ref = PRIndirectReference(this, num, tokens.generation)
return ref
}
PRTokeniser.TokenType.REF -> {
val num = tokens.reference
val ref = PRIndirectReference(this, num, tokens.generation)
return ref
}
PRTokeniser.TokenType.ENDOFFILE -> throw IOException(MessageLocalization.getComposedMessage("unexpected.end.of.file"))
else -> {
val sv = tokens.stringValue
if ("null" == sv)
{
if (readDepth == 0)
{
return PdfNull()
} //else
return PdfNull.PDFNULL
}
else if ("true" == sv)
{
if (readDepth == 0)
{
return PdfBoolean(true)
} //else
return PdfBoolean.PDFTRUE
}
else if ("false" == sv)
{
if (readDepth == 0)
{
return PdfBoolean(false)
} //else
return PdfBoolean.PDFFALSE
}
return PdfLiteral(-type.ordinal, tokens.stringValue)
}
}
}

/** Gets the dictionary that represents a page.
       * @param pageNum the page number. 1 is the first
      * * 
 * @return the page dictionary
 */
     fun getPageN(pageNum:Int):PdfDictionary? {
val dic = pageRefs.getPageN(pageNum) ?: return null
if (isAppendable)
dic.indRef = pageRefs.getPageOrigRef(pageNum)
return dic
}

/**
       * @param pageNum
      * * 
 * @return a Dictionary object
 */
     fun getPageNRelease(pageNum:Int):PdfDictionary? {
val dic = getPageN(pageNum)
pageRefs.releasePage(pageNum)
return dic
}

/**
       * @param pageNum
 */
     fun releasePage(pageNum:Int) {
pageRefs.releasePage(pageNum)
}

/**
     
      */
     fun resetReleasePage() {
pageRefs.resetReleasePage()
}

/** Gets the page reference to this page.
       * @param pageNum the page number. 1 is the first
      * * 
 * @return the page reference
 */
     fun getPageOrigRef(pageNum:Int):PRIndirectReference {
return pageRefs.getPageOrigRef(pageNum)
}

/** Gets the contents of the page.
       * @param pageNum the page number. 1 is the first
      * * 
 * @param file the location of the PDF document
      * * 
 * @throws IOException on error
      * * 
 * @return the content
 */
    @Throws(IOException::class)
 fun getPageContent(pageNum:Int, file:RandomAccessFileOrArray):ByteArray? {
val page = getPageNRelease(pageNum) ?: return null
val contents = getPdfObjectRelease(page.get(PdfName.CONTENTS)) ?: return ByteArray(0)
var bout:ByteArrayOutputStream? = null
if (contents.isStream)
{
return getStreamBytes(contents as PRStream?, file)
}
else if (contents.isArray)
{
val array = contents as PdfArray?
bout = ByteArrayOutputStream()
for (k in 0..array.size() - 1)
{
val item = getPdfObjectRelease(array.getPdfObject(k))
if (item == null || !item.isStream)
continue
val b = getStreamBytes(item as PRStream?, file)
bout.write(b)
if (k != array.size() - 1)
bout.write('\n')
}
return bout.toByteArray()
}
else
return ByteArray(0)
}

/**
       * Retrieve the given page's resource dictionary
       * @param pageNum 1-based page number from which to retrieve the resource dictionary
      * * 
 * @return The page's resources, or 'null' if the page has none.
      * * 
 * @since 5.1
 */
     fun getPageResources(pageNum:Int):PdfDictionary {
return getPageResources(getPageN(pageNum))
}

/**
       * Retrieve the given page's resource dictionary
       * @param pageDict the given page
      * * 
 * @return The page's resources, or 'null' if the page has none.
      * * 
 * @since 5.1
 */
     fun getPageResources(pageDict:PdfDictionary):PdfDictionary {
return pageDict.getAsDict(PdfName.RESOURCES)
}

/** Gets the contents of the page.
       * @param pageNum the page number. 1 is the first
      * * 
 * @throws IOException on error
      * * 
 * @return the content
 */
    @Throws(IOException::class)
 fun getPageContent(pageNum:Int):ByteArray {
val rf = safeFile
try
{
rf.reOpen()
return getPageContent(pageNum, rf)
}

finally
{
try
{
rf.close()
}
catch (e:Exception) {}

}
}

protected fun killXref(obj:PdfObject?) {
var obj:PdfObject? = obj ?: return
if (obj is PdfIndirectReference && !obj.isIndirect)
return 
when (obj.type()) {
PdfObject.INDIRECT -> {
val xr = (obj as PRIndirectReference).number
obj = xrefObj[xr]
xrefObj.set(xr, null)
freeXref = xr
killXref(obj)
}
PdfObject.ARRAY -> {
val t = obj as PdfArray?
for (i in 0..t.size() - 1)
killXref(t.getPdfObject(i))
}
PdfObject.STREAM, PdfObject.DICTIONARY -> {
val dic = obj as PdfDictionary?
for (element in dic.keys)
{
killXref(dic.get(element as PdfName))
}
}
}
}

/** Sets the contents of the page.
       * @param content the new page content
      * * 
 * @param pageNum the page number. 1 is the first
      * * 
 * @param compressionLevel the compressionLevel
      * * 
 * @param killOldXRefRecursively if true, old contents will be deeply removed from the pdf (i.e. if it was an array,
      * *                               all its entries will also be removed). Use careful when a content stream may be reused.
      * *                               If false, old contents will not be removed and will stay in the document if not manually deleted.
      * * 
 * @since	5.5.7	(the method already existed without param killOldXRefRecursively)
 */
    @JvmOverloads  fun setPageContent(pageNum:Int, content:ByteArray, compressionLevel:Int = PdfStream.DEFAULT_COMPRESSION, killOldXRefRecursively:Boolean = false) {
val page = getPageN(pageNum) ?: return
val contents = page.get(PdfName.CONTENTS)
freeXref = -1
if (killOldXRefRecursively)
{
killXref(contents)
}
if (freeXref == -1)
{
xrefObj.add(null)
freeXref = xrefObj.size - 1
}
page.put(PdfName.CONTENTS, PRIndirectReference(this, freeXref))
xrefObj[freeXref] = PRStream(this, content, compressionLevel)
}

/** Eliminates shared streams if they exist.  */
     fun eliminateSharedStreams() {
if (!sharedStreams)
return 
sharedStreams = false
if (pageRefs.size() == 1)
return 
val newRefs = ArrayList<PRIndirectReference>()
val newStreams = ArrayList<PRStream>()
val visited = IntHashtable()
for (k in 1..pageRefs.size())
{
val page = pageRefs.getPageN(k) ?: continue
val contents = getPdfObject(page.get(PdfName.CONTENTS)) ?: continue
if (contents.isStream)
{
val ref = page.get(PdfName.CONTENTS) as PRIndirectReference?
if (visited.containsKey(ref.number))
{
// need to duplicate
                    newRefs.add(ref)
newStreams.add(PRStream(contents as PRStream?, null))
}
else
visited.put(ref.number, 1)
}
else if (contents.isArray)
{
val array = contents as PdfArray?
for (j in 0..array.size() - 1)
{
val ref = array.getPdfObject(j) as PRIndirectReference
if (visited.containsKey(ref.number))
{
// need to duplicate
                        newRefs.add(ref)
newStreams.add(PRStream(getPdfObject(ref) as PRStream?, null))
}
else
visited.put(ref.number, 1)
}
}
}
if (newStreams.isEmpty())
return 
for (k in newStreams.indices)
{
xrefObj.add(newStreams[k])
val ref = newRefs[k]
ref.setNumber(xrefObj.size - 1, 0)
}
}

/** Gets the XML metadata.
       * @throws IOException on error
      * * 
 * @return the XML metadata
 */
    // empty on purpose
 val metadata:ByteArray?
@Throws(IOException::class)
get() {
val obj = getPdfObject(catalog!!.get(PdfName.METADATA))
if (obj !is PRStream)
return null
val rf = safeFile
var b:ByteArray? = null
try
{
rf.reOpen()
b = getStreamBytes(obj, rf)
}

finally
{
try
{
rf.close()
}
catch (e:Exception) {}

}
return b
}

/**
       * Gets the number of xref objects.
       * @return the number of xref objects
 */
     val xrefSize:Int
get() =xrefObj.size

/**
       * Returns true if the PDF has a 128 bit key encryption.
       * @return true if the PDF has a 128 bit key encryption
 */
     val is128Key:Boolean
get() =rValue == 3

/** Finds all the font subsets and changes the prefixes to some
       * random values.
       * @return the number of font subsets altered
 */
     fun shuffleSubsetNames():Int {
var total = 0
for (k in 1..xrefObj.size - 1)
{
val obj = getPdfObjectRelease(k)
if (obj == null || !obj.isDictionary)
continue
val dic = obj as PdfDictionary?
if (!existsName(dic, PdfName.TYPE, PdfName.FONT))
continue
if (existsName(dic, PdfName.SUBTYPE, PdfName.TYPE1) 
|| existsName(dic, PdfName.SUBTYPE, PdfName.MMTYPE1) 
|| existsName(dic, PdfName.SUBTYPE, PdfName.TRUETYPE))
{
val s = getSubsetPrefix(dic) ?: continue
val ns = BaseFont.createSubsetPrefix() + s.substring(7)
val newName = PdfName(ns)
dic.put(PdfName.BASEFONT, newName)
setXrefPartialObject(k, dic)
++total
val fd = dic.getAsDict(PdfName.FONTDESCRIPTOR) ?: continue
fd.put(PdfName.FONTNAME, newName)
}
else if (existsName(dic, PdfName.SUBTYPE, PdfName.TYPE0))
{
val s = getSubsetPrefix(dic)
val arr = dic.getAsArray(PdfName.DESCENDANTFONTS) ?: continue
if (arr.isEmpty)
continue
val desc = arr.getAsDict(0)
val sde = getSubsetPrefix(desc) ?: continue
val ns = BaseFont.createSubsetPrefix()
if (s != null)
dic.put(PdfName.BASEFONT, PdfName(ns + s.substring(7)))
setXrefPartialObject(k, dic)
val newName = PdfName(ns + sde.substring(7))
desc.put(PdfName.BASEFONT, newName)
++total
val fd = desc.getAsDict(PdfName.FONTDESCRIPTOR) ?: continue
fd.put(PdfName.FONTNAME, newName)
}
}
return total
}

/** Finds all the fonts not subset but embedded and marks them as subset.
       * @return the number of fonts altered
 */
     fun createFakeFontSubsets():Int {
var total = 0
for (k in 1..xrefObj.size - 1)
{
val obj = getPdfObjectRelease(k)
if (obj == null || !obj.isDictionary)
continue
val dic = obj as PdfDictionary?
if (!existsName(dic, PdfName.TYPE, PdfName.FONT))
continue
if (existsName(dic, PdfName.SUBTYPE, PdfName.TYPE1) 
|| existsName(dic, PdfName.SUBTYPE, PdfName.MMTYPE1) 
|| existsName(dic, PdfName.SUBTYPE, PdfName.TRUETYPE))
{
var s = getSubsetPrefix(dic)
if (s != null)
continue
s = getFontName(dic)
if (s == null)
continue
val ns = BaseFont.createSubsetPrefix() + s
var fd:PdfDictionary? = getPdfObjectRelease(dic.get(PdfName.FONTDESCRIPTOR)) as PdfDictionary? ?: continue
if (fd.get(PdfName.FONTFILE) == null && fd.get(PdfName.FONTFILE2) == null 
&& fd.get(PdfName.FONTFILE3) == null)
continue
fd = dic.getAsDict(PdfName.FONTDESCRIPTOR)
val newName = PdfName(ns)
dic.put(PdfName.BASEFONT, newName)
fd!!.put(PdfName.FONTNAME, newName)
setXrefPartialObject(k, dic)
++total
}
}
return total
}

/**
       * Gets all the named destinations as an HashMap. The key is the name
       * and the value is the destinations array.
       * @return gets all the named destinations
 */
     val namedDestination:HashMap<Any, PdfObject>
get() =getNamedDestination(false)

/**
       * Gets all the named destinations as an HashMap. The key is the name
       * and the value is the destinations array.
       * @param	keepNames	true if you want the keys to be real PdfNames instead of Strings
      * * 
 * @return gets all the named destinations
      * * 
 * @since	2.1.6
 */
     fun getNamedDestination(keepNames:Boolean):HashMap<Any, PdfObject> {
val names = getNamedDestinationFromNames(keepNames)
names.putAll(namedDestinationFromStrings)
return names
}

/**
       * Gets the named destinations from the /Dests key in the catalog as an HashMap. The key is the name
       * and the value is the destinations array.
       * @return gets the named destinations
      * * 
 * @since 5.0.1 (generic type in signature)
 */
     val namedDestinationFromNames:HashMap<String, PdfObject>
@SuppressWarnings("unchecked")
get() =HashMap(getNamedDestinationFromNames(false))

/**
       * Gets the named destinations from the /Dests key in the catalog as an HashMap. The key is the name
       * and the value is the destinations array.
       * @param	keepNames	true if you want the keys to be real PdfNames instead of Strings
      * * 
 * @return gets the named destinations
      * * 
 * @since	2.1.6
 */
     fun getNamedDestinationFromNames(keepNames:Boolean):HashMap<Any, PdfObject> {
val names = HashMap<Any, PdfObject>()
if (catalog!!.get(PdfName.DESTS) != null)
{
val dic = getPdfObjectRelease(catalog!!.get(PdfName.DESTS)) as PdfDictionary? ?: return names
val keys = dic.keys
for (key in keys)
{
val arr = getNameArray(dic.get(key)) ?: continue
if (keepNames)
{
names.put(key, arr)
}
else
{
val name = PdfName.decodeName(key.toString())
names.put(name, arr)
}
}
}
return names
}

/**
       * Gets the named destinations from the /Names key in the catalog as an HashMap. The key is the name
       * and the value is the destinations array.
       * @return gets the named destinations
 */
     val namedDestinationFromStrings:HashMap<String, PdfObject>
get() {
if (catalog!!.get(PdfName.NAMES) != null)
{
var dic:PdfDictionary? = getPdfObjectRelease(catalog!!.get(PdfName.NAMES)) as PdfDictionary?
if (dic != null)
{
dic = getPdfObjectRelease(dic.get(PdfName.DESTS)) as PdfDictionary?
if (dic != null)
{
val names = PdfNameTree.readTree(dic)
val it = names.entries.iterator()
while (it.hasNext())
{
val entry = it.next()
val arr = getNameArray(entry.value)
if (arr != null)
entry.setValue(arr)
else
it.remove()
}
return names
}
}
}
return HashMap()
}

/**
       * Removes all the fields from the document.
      */
     fun removeFields() {
pageRefs.resetReleasePage()
for (k in 1..pageRefs.size())
{
val page = pageRefs.getPageN(k)
val annots = page.getAsArray(PdfName.ANNOTS)
if (annots == null)
{
pageRefs.releasePage(k)
continue
}
var j = 0
while (j < annots.size())
{
val obj = getPdfObjectRelease(annots.getPdfObject(j))
if (obj == null || !obj.isDictionary)
{
++j
continue
}
val annot = obj as PdfDictionary?
if (PdfName.WIDGET == annot.get(PdfName.SUBTYPE))
annots.remove(j--)
++j
}
if (annots.isEmpty)
page.remove(PdfName.ANNOTS)
else
pageRefs.releasePage(k)
}
catalog!!.remove(PdfName.ACROFORM)
pageRefs.resetReleasePage()
}

/**
       * Removes all the annotations and fields from the document.
      */
     fun removeAnnotations() {
pageRefs.resetReleasePage()
for (k in 1..pageRefs.size())
{
val page = pageRefs.getPageN(k)
if (page.get(PdfName.ANNOTS) == null)
pageRefs.releasePage(k)
else
page.remove(PdfName.ANNOTS)
}
catalog!!.remove(PdfName.ACROFORM)
pageRefs.resetReleasePage()
}

/**
       * Retrieves links for a certain page.
       * @param page the page to inspect
      * * 
 * @return a list of links
 */
     fun getLinks(page:Int):ArrayList<PdfAnnotation.PdfImportedLink> {
pageRefs.resetReleasePage()
val result = ArrayList<PdfAnnotation.PdfImportedLink>()
val pageDic = pageRefs.getPageN(page)
if (pageDic.get(PdfName.ANNOTS) != null)
{
val annots = pageDic.getAsArray(PdfName.ANNOTS)
for (j in 0..annots.size() - 1)
{
val annot = getPdfObjectRelease(annots.getPdfObject(j)) as PdfDictionary?

if (PdfName.LINK == annot.get(PdfName.SUBTYPE))
{
result.add(PdfAnnotation.PdfImportedLink(annot))
}
}
}
pageRefs.releasePage(page)
pageRefs.resetReleasePage()
return result
}

private fun iterateBookmarks(outlineRef:PdfObject?, names:HashMap<Any, PdfObject>) {
var outlineRef = outlineRef
while (outlineRef != null)
{
replaceNamedDestination(outlineRef, names)
val outline = getPdfObjectRelease(outlineRef) as PdfDictionary?
val first = outline.get(PdfName.FIRST)
if (first != null)
{
iterateBookmarks(first, names)
}
outlineRef = outline.get(PdfName.NEXT)
}
}

/**
       * Replaces remote named links with local destinations that have the same name.
       * @since	5.0
 */
     fun makeRemoteNamedDestinationsLocal() {
if (remoteToLocalNamedDestinations)
return 
remoteToLocalNamedDestinations = true
val names = getNamedDestination(true)
if (names.isEmpty())
return 
for (k in 1..pageRefs.size())
{
val page = pageRefs.getPageN(k)
val annotsRef:PdfObject
val annots = getPdfObject(annotsRef = page.get(PdfName.ANNOTS)) as PdfArray?
val annotIdx = lastXrefPartial
releaseLastXrefPartial()
if (annots == null)
{
pageRefs.releasePage(k)
continue
}
var commitAnnots = false
for (an in 0..annots.size() - 1)
{
val objRef = annots.getPdfObject(an)
if (convertNamedDestination(objRef, names) && !objRef.isIndirect)
commitAnnots = true
}
if (commitAnnots)
setXrefPartialObject(annotIdx, annots)
if (!commitAnnots || annotsRef.isIndirect)
pageRefs.releasePage(k)
}
}

/**
       * Converts a remote named destination GoToR with a local named destination
       * if there's a corresponding name.
       * @param	obj	an annotation that needs to be screened for links to external named destinations.
      * * 
 * @param	names	a map with names of local named destinations
      * * 
 * @since	iText 5.0
 */
    private fun convertNamedDestination(obj:PdfObject?, names:HashMap<Any, PdfObject>):Boolean {
var obj = obj
obj = getPdfObject(obj)
val objIdx = lastXrefPartial
releaseLastXrefPartial()
if (obj != null && obj.isDictionary)
{
val ob2 = getPdfObject((obj as PdfDictionary).get(PdfName.A))
if (ob2 != null)
{
val obj2Idx = lastXrefPartial
releaseLastXrefPartial()
val dic = ob2 as PdfDictionary?
val type = getPdfObjectRelease(dic.get(PdfName.S)) as PdfName?
if (PdfName.GOTOR == type)
{
val ob3 = getPdfObjectRelease(dic.get(PdfName.D))
var name:Any? = null
if (ob3 != null)
{
if (ob3.isName)
name = ob3
else if (ob3.isString)
name = ob3.toString()
val dest = names[name] as PdfArray
if (dest != null)
{
dic.remove(PdfName.F)
dic.remove(PdfName.NEWWINDOW)
dic.put(PdfName.S, PdfName.GOTO)
setXrefPartialObject(obj2Idx, ob2)
setXrefPartialObject(objIdx, obj)
return true
}
}
}
}
}
return false
}

/** Replaces all the local named links with the actual destinations.  */
     fun consolidateNamedDestinations() {
if (consolidateNamedDestinations)
return 
consolidateNamedDestinations = true
val names = getNamedDestination(true)
if (names.isEmpty())
return 
for (k in 1..pageRefs.size())
{
val page = pageRefs.getPageN(k)
val annotsRef:PdfObject
val annots = getPdfObject(annotsRef = page.get(PdfName.ANNOTS)) as PdfArray?
val annotIdx = lastXrefPartial
releaseLastXrefPartial()
if (annots == null)
{
pageRefs.releasePage(k)
continue
}
var commitAnnots = false
for (an in 0..annots.size() - 1)
{
val objRef = annots.getPdfObject(an)
if (replaceNamedDestination(objRef, names) && !objRef.isIndirect)
commitAnnots = true
}
if (commitAnnots)
setXrefPartialObject(annotIdx, annots)
if (!commitAnnots || annotsRef.isIndirect)
pageRefs.releasePage(k)
}
val outlines = getPdfObjectRelease(catalog!!.get(PdfName.OUTLINES)) as PdfDictionary? ?: return
iterateBookmarks(outlines.get(PdfName.FIRST), names)
}

private fun replaceNamedDestination(obj:PdfObject?, names:HashMap<Any, PdfObject>):Boolean {
var obj = obj
obj = getPdfObject(obj)
val objIdx = lastXrefPartial
releaseLastXrefPartial()
if (obj != null && obj.isDictionary)
{
var ob2 = getPdfObjectRelease((obj as PdfDictionary).get(PdfName.DEST))
var name:Any? = null
if (ob2 != null)
{
if (ob2.isName)
name = ob2
else if (ob2.isString)
name = ob2.toString()
val dest = names[name] as PdfArray
if (dest != null)
{
obj.put(PdfName.DEST, dest)
setXrefPartialObject(objIdx, obj)
return true
}
}
else if ((ob2 = getPdfObject(obj.get(PdfName.A))) != null)
{
val obj2Idx = lastXrefPartial
releaseLastXrefPartial()
val type = getPdfObjectRelease(ob2.get(PdfName.S)) as PdfName?
if (PdfName.GOTO == type)
{
val ob3 = getPdfObjectRelease(ob2.get(PdfName.D))
if (ob3 != null)
{
if (ob3.isName)
name = ob3
else if (ob3.isString)
name = ob3.toString()
}
val dest = names[name] as PdfArray
if (dest != null)
{
ob2.put(PdfName.D, dest)
setXrefPartialObject(obj2Idx, ob2)
setXrefPartialObject(objIdx, obj)
return true
}
}
}
}
return false
}

/**
       * Closes the reader, and any underlying stream or data source used to create the reader
      */
     fun close() {
try
{
tokens.close()
}
catch (e:IOException) {
throw ExceptionConverter(e)
}

}

@SuppressWarnings("unchecked")
protected fun removeUnusedNode(obj:PdfObject, hits:BooleanArray) {
var obj = obj
val state = Stack<Any>()
state.push(obj)
while (!state.empty())
{
val current = state.pop() ?: continue
var ar:ArrayList<PdfObject>? = null
var dic:PdfDictionary? = null
var keys:Array<PdfName>? = null
var objs:Array<Any>? = null
var idx = 0
if (current is PdfObject)
{
obj = current as PdfObject?
when (obj.type()) {
PdfObject.DICTIONARY, PdfObject.STREAM -> {
dic = obj as PdfDictionary
keys = arrayOfNulls<PdfName>(dic.size())
dic.keys.toArray<PdfName>(keys)
}
PdfObject.ARRAY -> ar = (obj as PdfArray).arrayList
PdfObject.INDIRECT -> {
val ref = obj as PRIndirectReference
val num = ref.number
if (!hits[num])
{
hits[num] = true
state.push(getPdfObjectRelease(ref))
}
continue
}
else -> continue
}
}
else
{
objs = current as Array<Any>?
if (objs!![0] is ArrayList<Any>)
{
ar = objs[0] as ArrayList<PdfObject>
idx = (objs[1] as Int).toInt()
}
else
{
keys = objs[0] as Array<PdfName>
dic = objs[1] as PdfDictionary
idx = (objs[2] as Int).toInt()
}
}
if (ar != null)
{
for (k in idx..ar.size - 1)
{
val v = ar[k]
if (v.isIndirect)
{
val num = (v as PRIndirectReference).number
if (num >= xrefObj.size || !partial && xrefObj[num] == null)
{
ar[k] = PdfNull.PDFNULL
continue
}
}
if (objs == null)
state.push(arrayOf<Any>(ar, Integer.valueOf(k + 1)))
else
{
objs[1] = Integer.valueOf(k + 1)
state.push(objs)
}
state.push(v)
break
}
}
else
{
for (k in idx..keys!!.size - 1)
{
val key = keys[k]
val v = dic!!.get(key)
if (v.isIndirect)
{
val num = (v as PRIndirectReference).number
if (num < 0 || num >= xrefObj.size || !partial && xrefObj[num] == null)
{
dic.put(key, PdfNull.PDFNULL)
continue
}
}
if (objs == null)
state.push(arrayOf<Any>(keys, dic, Integer.valueOf(k + 1)))
else
{
objs[2] = Integer.valueOf(k + 1)
state.push(objs)
}
state.push(v)
break
}
}
}
}

/**
       * Removes all the unreachable objects.
       * @return the number of indirect objects removed
 */
     fun removeUnusedObjects():Int {
val hits = BooleanArray(xrefObj.size)
removeUnusedNode(trailer, hits)
var total = 0
if (partial)
{
for (k in 1..hits.size - 1)
{
if (!hits[k])
{
xref[k * 2] = -1
xref[k * 2 + 1] = 0
xrefObj.set(k, null)
++total
}
}
}
else
{
for (k in 1..hits.size - 1)
{
if (!hits[k])
{
xrefObj.set(k, null)
++total
}
}
}
return total
}

/** Gets a read-only version of AcroFields.
       * @return a read-only version of AcroFields
 */
     val acroFields:AcroFields
get() =AcroFields(this, null)

/**
       * Gets the global document JavaScript.
       * @param file the document file
      * * 
 * @throws IOException on error
      * * 
 * @return the global document JavaScript
 */
    @Throws(IOException::class)
 fun getJavaScript(file:RandomAccessFileOrArray):String? {
val names = getPdfObjectRelease(catalog!!.get(PdfName.NAMES)) as PdfDictionary? ?: return null
val js = getPdfObjectRelease(names.get(PdfName.JAVASCRIPT)) as PdfDictionary? ?: return null
val jscript = PdfNameTree.readTree(js)
var sortedNames = arrayOfNulls<String>(jscript.size)
sortedNames = jscript.keys.toArray<String>(sortedNames)
Arrays.sort(sortedNames)
val buf = StringBuffer()
for (k in sortedNames.indices)
{
val j = getPdfObjectRelease(jscript[sortedNames[k]]) as PdfDictionary? ?: continue
val obj = getPdfObjectRelease(j.get(PdfName.JS))
if (obj != null)
{
if (obj.isString)
buf.append((obj as PdfString).toUnicodeString()).append('\n')
else if (obj.isStream)
{
val bytes = getStreamBytes(obj as PRStream?, file)
if (bytes.size >= 2 && bytes[0] == 254.toByte() && bytes[1] == 255.toByte())
buf.append(PdfEncodings.convertToString(bytes, PdfObject.TEXT_UNICODE))
else
buf.append(PdfEncodings.convertToString(bytes, PdfObject.TEXT_PDFDOCENCODING))
buf.append('\n')
}
}
}
return buf.toString()
}

/**
       * Gets the global document JavaScript.
       * @throws IOException on error
      * * 
 * @return the global document JavaScript
 */
     val javaScript:String
@Throws(IOException::class)
get() {
val rf = safeFile
try
{
rf.reOpen()
return getJavaScript(rf)
}

finally
{
try
{
rf.close()
}
catch (e:Exception) {}

}
}

/**
       * Selects the pages to keep in the document. The pages are described as
       * ranges. The page ordering can be changed but
       * no page repetitions are allowed. Note that it may be very slow in partial mode.
       * @param ranges the comma separated ranges as described in [SequenceList]
 */
     fun selectPages(ranges:String) {
selectPages(SequenceList.expand(ranges, numberOfPages))
}

/**
       * Selects the pages to keep in the document. The pages are described as a
       * List of Integer. The page ordering can be changed but
       * no page repetitions are allowed. Note that it may be very slow in partial mode.
       * @param pagesToKeep the pages to keep in the document
 */
     fun selectPages(pagesToKeep:List<Int>) {
selectPages(pagesToKeep, true)
}

/**
       * Selects the pages to keep in the document. The pages are described as a
       * List of Integer. The page ordering can be changed but
       * no page repetitions are allowed. Note that it may be very slow in partial mode.
       * @param pagesToKeep the pages to keep in the document
      * * 
 * @param removeUnused indicate if to remove unsed objects. @see removeUnusedObjects
 */
    protected fun selectPages(pagesToKeep:List<Int>, removeUnused:Boolean) {
pageRefs.selectPages(pagesToKeep)
if (removeUnused) removeUnusedObjects()
}

/** Sets the viewer preferences as the sum of several constants.
       * @param preferences the viewer preferences
      * * 
 * @see PdfViewerPreferences.setViewerPreferences
 */
    override fun setViewerPreferences(preferences:Int) {
this.viewerPreferences.setViewerPreferences(preferences)
setViewerPreferences(this.viewerPreferences)
}

/** Adds a viewer preference
       * @param key a key for a viewer preference
      * * 
 * @param value	a value for the viewer preference
      * * 
 * @see PdfViewerPreferences.addViewerPreference
 */
     fun addViewerPreference(key:PdfName, value:PdfObject) {
this.viewerPreferences.addViewerPreference(key, value)
setViewerPreferences(this.viewerPreferences)
}

 fun setViewerPreferences(vp:PdfViewerPreferencesImp) {
vp.addToCatalog(catalog)
}

/**
       * Returns a bitset representing the PageMode and PageLayout viewer preferences.
       * Doesn't return any information about the ViewerPreferences dictionary.
       * @return an int that contains the Viewer Preferences.
 */
     val simpleViewerPreferences:Int
get() =PdfViewerPreferencesImp.getViewerPreferences(catalog).getPageLayoutAndMode()

internal class PageRefs {
private val reader:PdfReader
/** ArrayList with the indirect references to every page. Element 0 = page 1; 1 = page 2;... Not used for partial reading.  */
        private var refsn:ArrayList<PRIndirectReference>? = null
/** The number of pages, updated only in case of partial reading.  */
        private var sizep:Int = 0
/** intHashtable that does the same thing as refsn in case of partial reading: major difference: not all the pages are read.  */
        private var refsp:IntHashtable? = null
/** Page number of the last page that was read (partial reading only)  */
        private var lastPageRead = -1
/** stack to which pages dictionaries are pushed to keep track of the current page attributes  */
        private var pageInh:ArrayList<PdfDictionary>? = null
private var keepPages:Boolean = false
/**
           * Keeps track of all pages nodes to avoid circular references.
          */
        private val pagesNodes = HashSet<PdfObject>()

@Throws(IOException::class)
private constructor(reader:PdfReader) {
this.reader = reader
if (reader.partial)
{
refsp = IntHashtable()
val npages = PdfReader.getPdfObjectRelease(reader.rootPages!!.get(PdfName.COUNT)) as PdfNumber?
sizep = npages.intValue()
}
else
{
readPages()
}
}

 constructor(other:PageRefs, reader:PdfReader) {
this.reader = reader
this.sizep = other.sizep
if (other.refsn != null)
{
refsn = ArrayList(other.refsn)
for (k in refsn!!.indices)
{
refsn!!.set(k, duplicatePdfObject(refsn!![k], reader) as PRIndirectReference?)
}
}
else
this.refsp = other.refsp!!.clone() as IntHashtable
}

 fun size():Int {
if (refsn != null)
return refsn!!.size
else
return sizep
}

@Throws(IOException::class)
 fun readPages() {
if (refsn != null)
return 
refsp = null
refsn = ArrayList<PRIndirectReference>()
pageInh = ArrayList<PdfDictionary>()
iteratePages(reader.catalog!!.get(PdfName.PAGES) as PRIndirectReference?)
pageInh = null
reader.rootPages!!.put(PdfName.COUNT, PdfNumber(refsn!!.size))
}

@Throws(IOException::class)
 fun reReadPages() {
refsn = null
readPages()
}

/** Gets the dictionary that represents a page.
           * @param pageNum the page number. 1 is the first
          * * 
 * @return the page dictionary
 */
         fun getPageN(pageNum:Int):PdfDictionary? {
val ref = getPageOrigRef(pageNum)
return PdfReader.getPdfObject(ref) as PdfDictionary?
}

/**
           * @param pageNum
          * * 
 * @return a dictionary object
 */
         fun getPageNRelease(pageNum:Int):PdfDictionary {
val page = getPageN(pageNum)
releasePage(pageNum)
return page
}

/**
           * @param pageNum
          * * 
 * @return an indirect reference
 */
         fun getPageOrigRefRelease(pageNum:Int):PRIndirectReference {
val ref = getPageOrigRef(pageNum)
releasePage(pageNum)
return ref
}

/**
           * Gets the page reference to this page.
           * @param pageNum the page number. 1 is the first
          * * 
 * @return the page reference
 */
         fun getPageOrigRef(pageNum:Int):PRIndirectReference? {
var pageNum = pageNum
try
{
--pageNum
if (pageNum < 0 || pageNum >= size())
return null
if (refsn != null)
return refsn!![pageNum]
else
{
val n = refsp!!.get(pageNum)
if (n == 0)
{
val ref = getSinglePage(pageNum)
if (reader.lastXrefPartial == -1)
lastPageRead = -1
else
lastPageRead = pageNum
reader.lastXrefPartial = -1
refsp!!.put(pageNum, ref.number)
if (keepPages)
lastPageRead = -1
return ref
}
else
{
if (lastPageRead != pageNum)
lastPageRead = -1
if (keepPages)
lastPageRead = -1
return PRIndirectReference(reader, n)
}
}
}
catch (e:Exception) {
throw ExceptionConverter(e)
}

}

 fun keepPages() {
if (refsp == null || keepPages)
return 
keepPages = true
refsp!!.clear()
}

/**
           * @param pageNum
 */
         fun releasePage(pageNum:Int) {
var pageNum = pageNum
if (refsp == null)
return 
--pageNum
if (pageNum < 0 || pageNum >= size())
return 
if (pageNum != lastPageRead)
return 
lastPageRead = -1
reader.lastXrefPartial = refsp!!.get(pageNum)
reader.releaseLastXrefPartial()
refsp!!.remove(pageNum)
}

/**
         
          */
         fun resetReleasePage() {
if (refsp == null)
return 
lastPageRead = -1
}

 fun insertPage(pageNum:Int, ref:PRIndirectReference) {
var pageNum = pageNum
--pageNum
if (refsn != null)
{
if (pageNum >= refsn!!.size)
refsn!!.add(ref)
else
refsn!!.add(pageNum, ref)
}
else
{
++sizep
lastPageRead = -1
if (pageNum >= size())
{
refsp!!.put(size(), ref.number)
}
else
{
val refs2 = IntHashtable((refsp!!.size() + 1) * 2)
val it = refsp!!.entryIterator
while (it.hasNext())
{
val entry = it.next()
val p = entry.key
refs2.put(if (p >= pageNum) p + 1 else p, entry.value)
}
refs2.put(pageNum, ref.number)
refsp = refs2
}
}
}

/**
           * Adds a PdfDictionary to the pageInh stack to keep track of the page attributes.
           * @param nodePages	a Pages dictionary
 */
        private fun pushPageAttributes(nodePages:PdfDictionary) {
val dic = PdfDictionary()
if (!pageInh!!.isEmpty())
{
dic.putAll(pageInh!![pageInh!!.size - 1])
}
for (k in pageInhCandidates.indices)
{
val obj = nodePages.get(pageInhCandidates[k])
if (obj != null)
dic.put(pageInhCandidates[k], obj)
}
pageInh!!.add(dic)
}

/**
           * Removes the last PdfDictionary that was pushed to the pageInh stack.
          */
        private fun popPageAttributes() {
pageInh!!.removeAt(pageInh!!.size - 1)
}

@Throws(IOException::class)
private fun iteratePages(rpage:PRIndirectReference) {
if (!pagesNodes.add(getPdfObject(rpage)))
throw InvalidPdfException(MessageLocalization.getComposedMessage("illegal.pages.tree"))

val page = getPdfObject(rpage) as PdfDictionary? ?: return
val kidsPR = page.getAsArray(PdfName.KIDS)
// reference to a leaf
            if (kidsPR == null)
{
page.put(PdfName.TYPE, PdfName.PAGE)
val dic = pageInh!![pageInh!!.size - 1]
var key:PdfName
for (element in dic.keys)
{
key = element as PdfName
if (page.get(key) == null)
page.put(key, dic.get(key))
}
if (page.get(PdfName.MEDIABOX) == null)
{
val arr = PdfArray(floatArrayOf(0f, 0f, PageSize.LETTER.right, PageSize.LETTER.top))
page.put(PdfName.MEDIABOX, arr)
}
refsn!!.add(rpage)
}
else
{
page.put(PdfName.TYPE, PdfName.PAGES)
pushPageAttributes(page)
for (k in 0..kidsPR.size() - 1)
{
val obj = kidsPR.getPdfObject(k)
if (!obj.isIndirect)
{
while (k < kidsPR.size())
kidsPR.remove(k)
break
}
iteratePages(obj as PRIndirectReference)
}
popPageAttributes()
}// reference to a branch
}

protected fun getSinglePage(n:Int):PRIndirectReference {
val acc = PdfDictionary()
var top:PdfDictionary = reader.rootPages
var base = 0
while (true)
{
for (k in pageInhCandidates.indices)
{
val obj = top.get(pageInhCandidates[k])
if (obj != null)
acc.put(pageInhCandidates[k], obj)
}
val kids = PdfReader.getPdfObjectRelease(top.get(PdfName.KIDS)) as PdfArray?
val it = kids.listIterator()
while (it.hasNext())
{
val ref = it.next() as PRIndirectReference
val dic = getPdfObject(ref) as PdfDictionary?
val last = reader.lastXrefPartial
val count = getPdfObjectRelease(dic.get(PdfName.COUNT))
reader.lastXrefPartial = last
var acn = 1
if (count != null && count.type() == PdfObject.NUMBER)
acn = (count as PdfNumber).intValue()
if (n < base + acn)
{
if (count == null)
{
dic.mergeDifferent(acc)
return ref
}
reader.releaseLastXrefPartial()
top = dic
break
}
reader.releaseLastXrefPartial()
base += acn
}
}
}

private fun selectPages(pagesToKeep:List<Int>) {
val pg = IntHashtable()
val finalPages = ArrayList<Int>()
val psize = size()
for (pi in pagesToKeep)
{
val p = pi.toInt()
if (p >= 1 && p <= psize && pg.put(p, 1) == 0)
finalPages.add(pi)
}
if (reader.partial)
{
for (k in 1..psize)
{
getPageOrigRef(k)
resetReleasePage()
}
}
val parent = reader.catalog!!.get(PdfName.PAGES) as PRIndirectReference?
val topPages = PdfReader.getPdfObject(parent) as PdfDictionary?
val newPageRefs = ArrayList<PRIndirectReference>(finalPages.size)
val kids = PdfArray()
for (k in finalPages.indices)
{
val p = finalPages[k].toInt()
val pref = getPageOrigRef(p)
resetReleasePage()
kids.add(pref)
newPageRefs.add(pref)
getPageN(p)!!.put(PdfName.PARENT, parent)
}
val af = reader.acroFields
val removeFields = af.getFields().size > 0
for (k in 1..psize)
{
if (!pg.containsKey(k))
{
if (removeFields)
af.removeFieldsFromPage(k)
val pref = getPageOrigRef(k)
val nref = pref.number
reader.xrefObj.set(nref, null)
if (reader.partial)
{
reader.xref[nref * 2] = -1
reader.xref[nref * 2 + 1] = 0
}
}
}
topPages.put(PdfName.COUNT, PdfNumber(finalPages.size))
topPages.put(PdfName.KIDS, kids)
refsp = null
refsn = newPageRefs
}
}

internal fun getCryptoRef():PdfIndirectReference? {
if (cryptoRef == null)
return null
return PdfIndirectReference(0, cryptoRef!!.number, cryptoRef!!.generation)
}

/**
       * Checks if this PDF has usage rights enabled.
      
       * @return `true` if usage rights are present; `false` otherwise
 */
     fun hasUsageRights():Boolean {
val perms = catalog!!.getAsDict(PdfName.PERMS) ?: return false
return perms.contains(PdfName.UR) || perms.contains(PdfName.UR3)
}

/**
       * Removes any usage rights that this PDF may have. Only Adobe can grant usage rights
       * and any PDF modification with iText will invalidate them. Invalidated usage rights may
       * confuse Acrobat and it's advisable to remove them altogether.
      */
     fun removeUsageRights() {
val perms = catalog!!.getAsDict(PdfName.PERMS) ?: return
perms.remove(PdfName.UR)
perms.remove(PdfName.UR3)
if (perms.size() == 0)
catalog!!.remove(PdfName.PERMS)
}

/**
       * Gets the certification level for this document. The return values can be `PdfSignatureAppearance.NOT_CERTIFIED`,
       * `PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED`,
       * `PdfSignatureAppearance.CERTIFIED_FORM_FILLING` and
       * `PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS`.
       * 
 * 
       * No signature validation is made, use the methods available for that in AcroFields.
       * 
       * @return gets the certification level for this document
 */
     val certificationLevel:Int
get() {
var dic:PdfDictionary? = catalog!!.getAsDict(PdfName.PERMS) ?: return PdfSignatureAppearance.NOT_CERTIFIED
dic = dic.getAsDict(PdfName.DOCMDP)
if (dic == null)
return PdfSignatureAppearance.NOT_CERTIFIED
val arr = dic.getAsArray(PdfName.REFERENCE)
if (arr == null || arr.size() == 0)
return PdfSignatureAppearance.NOT_CERTIFIED
dic = arr.getAsDict(0)
if (dic == null)
return PdfSignatureAppearance.NOT_CERTIFIED
dic = dic.getAsDict(PdfName.TRANSFORMPARAMS)
if (dic == null)
return PdfSignatureAppearance.NOT_CERTIFIED
val p = dic.getAsNumber(PdfName.P) ?: return PdfSignatureAppearance.NOT_CERTIFIED
return p.intValue()
}

/**
       * Checks if the document was opened with the owner password so that the end application
       * can decide what level of access restrictions to apply. If the document is not encrypted
       * it will return true.
       * @return true if the document was opened with the owner password or if it's not encrypted,
      * * false if the document was opened with the user password
 */
     val isOpenedWithFullPermissions:Boolean
get() =!isEncrypted || ownerPasswordUsed || unethicalreading

/**
       * @return the crypto mode, or -1 of none
 */
     val cryptoMode:Int
get() {
if (decrypt == null)
return -1
else
return decrypt!!.cryptoMode
}

/**
       * @return true if the metadata is encrypted.
 */
     val isMetadataEncrypted:Boolean
get() {
if (decrypt == null)
return false
else
return decrypt!!.isMetadataEncrypted
}

/**
       * @return byte array of computed user password, or null if not encrypted or no ownerPassword is used.
 */
     fun computeUserPassword():ByteArray? {
if (!isEncrypted || !ownerPasswordUsed) return null
return decrypt!!.computeUserPassword(password)
}

companion object {

/**
	   * The iText developers are not responsible if you decide to change the
	   * value of this static parameter.
	   * @since 5.0.2
 */
	 var unethicalreading = false

 var debugmode = false
private val LOGGER = LoggerFactory.getLogger(PdfReader::class.java)

internal val pageInhCandidates = arrayOf(PdfName.MEDIABOX, PdfName.ROTATE, PdfName.RESOURCES, PdfName.CROPBOX)

internal val endstream = PdfEncodings.convertToBytes("endstream", null)
internal val endobj = PdfEncodings.convertToBytes("endobj", null)

protected var COUNTER = CounterFactory.getCounter(PdfReader::class.java)

/**
       * Utility method that checks the provided byte source to see if it has junk bytes at the beginning.  If junk bytes
       * are found, construct a tokeniser that ignores the junk.  Otherwise, construct a tokeniser for the byte source as it is
       * @param byteSource the source to check
      * * 
 * @return a tokeniser that is guaranteed to start at the PDF header
      * * 
 * @throws IOException if there is a problem reading the byte source
 */
    @Throws(IOException::class)
private fun getOffsetTokeniser(byteSource:RandomAccessSource):PRTokeniser {
var tok = PRTokeniser(RandomAccessFileOrArray(byteSource))
val offset = tok.headerOffset
if (offset != 0)
{
val offsetSource = WindowRandomAccessSource(byteSource, offset)
tok = PRTokeniser(RandomAccessFileOrArray(offsetSource))
}
return tok
}

/** Normalizes a Rectangle so that llx and lly are smaller than urx and ury.
       * @param box the original rectangle
      * * 
 * @return a normalized Rectangle
 */
     fun getNormalizedRectangle(box:PdfArray):Rectangle {
val llx = (getPdfObjectRelease(box.getPdfObject(0)) as PdfNumber).floatValue()
val lly = (getPdfObjectRelease(box.getPdfObject(1)) as PdfNumber).floatValue()
val urx = (getPdfObjectRelease(box.getPdfObject(2)) as PdfNumber).floatValue()
val ury = (getPdfObjectRelease(box.getPdfObject(3)) as PdfNumber).floatValue()
return Rectangle(Math.min(llx, urx), Math.min(lly, ury), 
Math.max(llx, urx), Math.max(lly, ury))
}

/**
       * @param obj
      * * 
 * @return a PdfObject
 */
     fun getPdfObjectRelease(obj:PdfObject):PdfObject? {
val obj2 = getPdfObject(obj)
releaseLastXrefPartial(obj)
return obj2
}


/**
       * Reads a PdfObject resolving an indirect reference
       * if needed.
       * @param obj the PdfObject to read
      * * 
 * @return the resolved PdfObject
 */
     fun getPdfObject(obj:PdfObject?):PdfObject? {
var obj:PdfObject? = obj ?: return null
if (!obj.isIndirect)
return obj
try
{
val ref = obj as PRIndirectReference?
val idx = ref.number
val appendable = ref.reader.isAppendable
obj = ref.reader.getPdfObject(idx)
if (obj == null)
{
return null
}
else
{
if (appendable)
{
when (obj.type()) {
PdfObject.NULL -> obj = PdfNull()
PdfObject.BOOLEAN -> obj = PdfBoolean((obj as PdfBoolean).booleanValue())
PdfObject.NAME -> obj = PdfName(obj.bytes)
}
obj.indRef = ref
}
return obj
}
}
catch (e:Exception) {
throw ExceptionConverter(e)
}

}

/**
       * Reads a PdfObject resolving an indirect reference
       * if needed. If the reader was opened in partial mode the object will be released
       * to save memory.
       * @param obj the PdfObject to read
      * * 
 * @param parent
      * * 
 * @return a PdfObject
 */
     fun getPdfObjectRelease(obj:PdfObject, parent:PdfObject):PdfObject {
val obj2 = getPdfObject(obj, parent)
releaseLastXrefPartial(obj)
return obj2
}

/**
       * @param obj
      * * 
 * @param parent
      * * 
 * @return a PdfObject
 */
     fun getPdfObject(obj:PdfObject?, parent:PdfObject?):PdfObject? {
var obj:PdfObject? = obj ?: return null
if (!obj.isIndirect)
{
var ref:PRIndirectReference? = null
if (parent != null && (ref = parent.indRef) != null && ref!!.reader.isAppendable)
{
when (obj.type()) {
PdfObject.NULL -> obj = PdfNull()
PdfObject.BOOLEAN -> obj = PdfBoolean((obj as PdfBoolean).booleanValue())
PdfObject.NAME -> obj = PdfName(obj.bytes)
}
obj.indRef = ref
}
return obj
}
return getPdfObject(obj)
}

/**
       * @param obj
 */
     fun releaseLastXrefPartial(obj:PdfObject?) {
if (obj == null)
return 
if (!obj.isIndirect)
return 
if (obj !is PRIndirectReference)
return 

val reader = obj.reader
if (reader.partial && reader.lastXrefPartial != -1 && reader.lastXrefPartial == obj.number)
{
reader.xrefObj[reader.lastXrefPartial] = null
}
reader.lastXrefPartial = -1
}

/**
       * Eliminates the reference to the object freeing the memory used by it and clearing
       * the xref entry.
       * @param obj the object. If it's an indirect reference it will be eliminated
      * * 
 * @return the object or the already erased dereferenced object
 */
     fun killIndirect(obj:PdfObject?):PdfObject? {
if (obj == null || obj.isNull)
return null
val ret = getPdfObjectRelease(obj)
if (obj.isIndirect)
{
val ref = obj as PRIndirectReference?
val reader = ref.reader
val n = ref.number
reader.xrefObj[n] = null
if (reader.partial)
reader.xref[n * 2] = -1
}
return ret
}

/** Decodes a stream that has the FlateDecode filter.
       * @param in the input data
      * * 
 * @return the decoded data
 */
     fun FlateDecode(`in`:ByteArray):ByteArray {
val b = FlateDecode(`in`, true) ?: return FlateDecode(`in`, false)
return b
}

/**
       * @param in
      * * 
 * @param dicPar
      * * 
 * @return a byte array
 */
     fun decodePredictor(`in`:ByteArray, dicPar:PdfObject?):ByteArray {
if (dicPar == null || !dicPar.isDictionary)
return `in`
val dic = dicPar as PdfDictionary?
var obj = getPdfObject(dic.get(PdfName.PREDICTOR))
if (obj == null || !obj.isNumber)
return `in`
val predictor = (obj as PdfNumber).intValue()
if (predictor < 10 && predictor != 2)
return `in`
var width = 1
obj = getPdfObject(dic.get(PdfName.COLUMNS))
if (obj != null && obj.isNumber)
width = (obj as PdfNumber).intValue()
var colors = 1
obj = getPdfObject(dic.get(PdfName.COLORS))
if (obj != null && obj.isNumber)
colors = (obj as PdfNumber).intValue()
var bpc = 8
obj = getPdfObject(dic.get(PdfName.BITSPERCOMPONENT))
if (obj != null && obj.isNumber)
bpc = (obj as PdfNumber).intValue()
val dataStream = DataInputStream(ByteArrayInputStream(`in`))
val fout = ByteArrayOutputStream(`in`.size)
val bytesPerPixel = colors * bpc / 8
val bytesPerRow = (colors * width * bpc + 7) / 8
var curr = ByteArray(bytesPerRow)
var prior = ByteArray(bytesPerRow)
if (predictor == 2)
{
if (bpc == 8)
{
val numRows = `in`.size / bytesPerRow
for (row in 0..numRows - 1)
{
val rowStart = row * bytesPerRow
for (col in 0 + bytesPerPixel..bytesPerRow - 1)
{
`in`[rowStart + col] = (`in`[rowStart + col] + `in`[rowStart + col - bytesPerPixel]).toByte()
}
}
}
return `in`
}
// Decode the (sub)image row-by-row
        while (true)
{
// Read the filter type byte and a row of data
            var filter = 0
try
{
filter = dataStream.read()
if (filter < 0)
{
return fout.toByteArray()
}
dataStream.readFully(curr, 0, bytesPerRow)
}
catch (e:Exception) {
return fout.toByteArray()
}

when (filter) {
0 //PNG_FILTER_NONE
 -> {}
1 //PNG_FILTER_SUB
 -> for (i in bytesPerPixel..bytesPerRow - 1)
{
curr[i] += curr[i - bytesPerPixel]
}
2 //PNG_FILTER_UP
 -> for (i in 0..bytesPerRow - 1)
{
curr[i] += prior[i]
}
3 //PNG_FILTER_AVERAGE
 -> {
for (i in 0..bytesPerPixel - 1)
{
curr[i] += (prior[i] / 2).toByte()
}
for (i in bytesPerPixel..bytesPerRow - 1)
{
curr[i] += (((curr[i - bytesPerPixel] and 0xff) + (prior[i] and 0xff)) / 2).toByte()
}
}
4 //PNG_FILTER_PAETH
 -> {
for (i in 0..bytesPerPixel - 1)
{
curr[i] += prior[i]
}

for (i in bytesPerPixel..bytesPerRow - 1)
{
val a = curr[i - bytesPerPixel] and 0xff
val b = prior[i] and 0xff
val c = prior[i - bytesPerPixel] and 0xff

val p = a + b - c
val pa = Math.abs(p - a)
val pb = Math.abs(p - b)
val pc = Math.abs(p - c)

val ret:Int

if (pa <= pb && pa <= pc)
{
ret = a
}
else if (pb <= pc)
{
ret = b
}
else
{
ret = c
}
curr[i] += ret.toByte()
}
}
else -> // Error -- unknown filter type
                    throw RuntimeException(MessageLocalization.getComposedMessage("png.filter.unknown"))
}
try
{
fout.write(curr)
}
catch (ioe:IOException) {// Never happens
            }

// Swap curr and prior
            val tmp = prior
prior = curr
curr = tmp
}
}

/** A helper to FlateDecode.
       * @param in the input data
      * * 
 * @param strict true to read a correct stream. false
      * * to try to read a corrupted stream
      * * 
 * @return the decoded data
 */
     fun FlateDecode(`in`:ByteArray, strict:Boolean):ByteArray? {
val stream = ByteArrayInputStream(`in`)
val zip = InflaterInputStream(stream)
val out = ByteArrayOutputStream()
val b = ByteArray(if (strict) 4092 else 1)
try
{
var n:Int
while ((n = zip.read(b)) >= 0)
{
out.write(b, 0, n)
}
zip.close()
out.close()
return out.toByteArray()
}
catch (e:Exception) {
if (strict)
return null
return out.toByteArray()
}

}

/** Decodes a stream that has the ASCIIHexDecode filter.
       * @param in the input data
      * * 
 * @return the decoded data
 */
     fun ASCIIHexDecode(`in`:ByteArray):ByteArray {
val out = ByteArrayOutputStream()
var first = true
var n1 = 0
for (k in `in`.indices)
{
val ch = `in`[k] and 0xff
if (ch == '>')
break
if (PRTokeniser.isWhitespace(ch))
continue
val n = PRTokeniser.getHex(ch)
if (n == -1)
throw RuntimeException(MessageLocalization.getComposedMessage("illegal.character.in.asciihexdecode"))
if (first)
n1 = n
else
out.write(((n1 shl 4) + n).toByte().toInt())
first = !first
}
if (!first)
out.write((n1 shl 4).toByte().toInt())
return out.toByteArray()
}

/** Decodes a stream that has the ASCII85Decode filter.
       * @param in the input data
      * * 
 * @return the decoded data
 */
     fun ASCII85Decode(`in`:ByteArray):ByteArray {
val out = ByteArrayOutputStream()
var state = 0
val chn = IntArray(5)
for (k in `in`.indices)
{
val ch = `in`[k] and 0xff
if (ch == '~')
break
if (PRTokeniser.isWhitespace(ch))
continue
if (ch == 'z' && state == 0)
{
out.write(0)
out.write(0)
out.write(0)
out.write(0)
continue
}
if (ch < '!' || ch > 'u')
throw RuntimeException(MessageLocalization.getComposedMessage("illegal.character.in.ascii85decode"))
chn[state] = ch - '!'
++state
if (state == 5)
{
state = 0
var r = 0
for (j in 0..4)
r = r * 85 + chn[j]
out.write((r shr 24).toByte().toInt())
out.write((r shr 16).toByte().toInt())
out.write((r shr 8).toByte().toInt())
out.write(r.toByte().toInt())
}
}
var r = 0
// We'll ignore the next two lines for the sake of perpetuating broken PDFs
//        if (state == 1)
//            throw new RuntimeException(MessageLocalization.getComposedMessage("illegal.length.in.ascii85decode"));
        if (state == 2)
{
r = chn[0] * 85 * 85 * 85 * 85 + chn[1] * 85 * 85 * 85 + 85 * 85 * 85 + 85 * 85 + 85
out.write((r shr 24).toByte().toInt())
}
else if (state == 3)
{
r = chn[0] * 85 * 85 * 85 * 85 + chn[1] * 85 * 85 * 85 + chn[2] * 85 * 85 + 85 * 85 + 85
out.write((r shr 24).toByte().toInt())
out.write((r shr 16).toByte().toInt())
}
else if (state == 4)
{
r = chn[0] * 85 * 85 * 85 * 85 + chn[1] * 85 * 85 * 85 + chn[2] * 85 * 85 + chn[3] * 85 + 85
out.write((r shr 24).toByte().toInt())
out.write((r shr 16).toByte().toInt())
out.write((r shr 8).toByte().toInt())
}
return out.toByteArray()
}

/** Decodes a stream that has the LZWDecode filter.
       * @param in the input data
      * * 
 * @return the decoded data
 */
     fun LZWDecode(`in`:ByteArray):ByteArray {
val out = ByteArrayOutputStream()
val lzw = LZWDecoder()
lzw.decode(`in`, out)
return out.toByteArray()
}

/** Gets the content from the page dictionary.
       * @param page the page dictionary
      * * 
 * @throws IOException on error
      * * 
 * @return the content
      * * 
 * @since 5.0.6
 */
    @Throws(IOException::class)
 fun getPageContent(page:PdfDictionary?):ByteArray? {
if (page == null)
return null
var rf:RandomAccessFileOrArray? = null
try
{
val contents = getPdfObjectRelease(page.get(PdfName.CONTENTS)) ?: return ByteArray(0)
if (contents.isStream)
{
if (rf == null)
{
rf = (contents as PRStream).reader.safeFile
rf!!.reOpen()
}
return getStreamBytes(contents as PRStream?, rf)
}
else if (contents.isArray)
{
val array = contents as PdfArray?
val bout = ByteArrayOutputStream()
for (k in 0..array.size() - 1)
{
val item = getPdfObjectRelease(array.getPdfObject(k))
if (item == null || !item.isStream)
continue
if (rf == null)
{
rf = (item as PRStream).reader.safeFile
rf!!.reOpen()
}
val b = getStreamBytes(item as PRStream?, rf)
bout.write(b)
if (k != array.size() - 1)
bout.write('\n')
}
return bout.toByteArray()
}
else
return ByteArray(0)
}

finally
{
try
{
if (rf != null)
rf.close()
}
catch (e:Exception) {}

}
}

/**
       * Decode a byte[] applying the filters specified in the provided dictionary using the provided filter handlers.
       * @param b the bytes to decode
      * * 
 * @param streamDictionary the dictionary that contains filter information
      * * 
 * @param filterHandlers the map used to look up a handler for each type of filter
      * * 
 * @return the decoded bytes
      * * 
 * @throws IOException if there are any problems decoding the bytes
      * * 
 * @since 5.0.4
 */
    @Throws(IOException::class)
@JvmOverloads  fun decodeBytes(b:ByteArray, streamDictionary:PdfDictionary, filterHandlers:Map<PdfName, FilterHandlers.FilterHandler> = FilterHandlers.defaultFilterHandlers):ByteArray {
var b = b
val filter = getPdfObjectRelease(streamDictionary.get(PdfName.FILTER))

var filters = ArrayList<PdfObject>()
if (filter != null)
{
if (filter.isName)
filters.add(filter)
else if (filter.isArray)
filters = (filter as PdfArray).arrayList
}
var dp = ArrayList<PdfObject>()
var dpo = getPdfObjectRelease(streamDictionary.get(PdfName.DECODEPARMS))
if (dpo == null || !dpo.isDictionary && !dpo.isArray)
dpo = getPdfObjectRelease(streamDictionary.get(PdfName.DP))
if (dpo != null)
{
if (dpo.isDictionary)
dp.add(dpo)
else if (dpo.isArray)
dp = (dpo as PdfArray).arrayList
}
for (j in filters.indices)
{
val filterName = filters[j] as PdfName
val filterHandler = filterHandlers[filterName] ?: throw UnsupportedPdfException(MessageLocalization.getComposedMessage("the.filter.1.is.not.supported", filterName))

val decodeParams:PdfDictionary?
if (j < dp.size)
{
val dpEntry = getPdfObject(dp[j])
if (dpEntry is PdfDictionary)
{
decodeParams = dpEntry as PdfDictionary?
}
else if (dpEntry == null || dpEntry is PdfNull || 
dpEntry is PdfLiteral && Arrays.equals("null".toByteArray(), dpEntry.bytes))
{
decodeParams = null
}
else
{
throw UnsupportedPdfException(MessageLocalization.getComposedMessage("the.decode.parameter.type.1.is.not.supported", dpEntry.javaClass.toString()))
}

}
else
{
decodeParams = null
}
b = filterHandler.decode(b, filterName, decodeParams, streamDictionary)
}
return b
}

/** Get the content from a stream applying the required filters.
       * @param stream the stream
      * * 
 * @param file the location where the stream is
      * * 
 * @throws IOException on error
      * * 
 * @return the stream content
 */
    @Throws(IOException::class)
 fun getStreamBytes(stream:PRStream, file:RandomAccessFileOrArray):ByteArray {
val b = getStreamBytesRaw(stream, file)
return decodeBytes(b, stream)
}

/** Get the content from a stream applying the required filters.
       * @param stream the stream
      * * 
 * @throws IOException on error
      * * 
 * @return the stream content
 */
    @Throws(IOException::class)
 fun getStreamBytes(stream:PRStream):ByteArray {
val rf = stream.reader.safeFile
try
{
rf.reOpen()
return getStreamBytes(stream, rf)
}

finally
{
try
{
rf.close()
}
catch (e:Exception) {}

}
}

/** Get the content from a stream as it is without applying any filter.
       * @param stream the stream
      * * 
 * @param file the location where the stream is
      * * 
 * @throws IOException on error
      * * 
 * @return the stream content
 */
    @Throws(IOException::class)
 fun getStreamBytesRaw(stream:PRStream, file:RandomAccessFileOrArray):ByteArray {
val reader = stream.reader
var b:ByteArray
if (stream.offset < 0)
b = stream.bytes
else
{
b = ByteArray(stream.length)
file.seek(stream.offset)
file.readFully(b)
val decrypt = reader.decrypt
if (decrypt != null)
{
val filter = getPdfObjectRelease(stream.get(PdfName.FILTER))
var filters = ArrayList<PdfObject>()
if (filter != null)
{
if (filter.isName)
filters.add(filter)
else if (filter.isArray)
filters = (filter as PdfArray).arrayList
}
var skip = false
for (k in filters.indices)
{
val obj = getPdfObjectRelease(filters[k])
if (obj != null && obj.toString() == "/Crypt")
{
skip = true
break
}
}
if (!skip)
{
decrypt.setHashKey(stream.objNum, stream.objGen)
b = decrypt.decryptByteArray(b)
}
}
}
return b
}

/** Get the content from a stream as it is without applying any filter.
       * @param stream the stream
      * * 
 * @throws IOException on error
      * * 
 * @return the stream content
 */
    @Throws(IOException::class)
 fun getStreamBytesRaw(stream:PRStream):ByteArray {
val rf = stream.reader.safeFile
try
{
rf.reOpen()
return getStreamBytesRaw(stream, rf)
}

finally
{
try
{
rf.close()
}
catch (e:Exception) {}

}
}

internal fun equalsn(a1:ByteArray, a2:ByteArray):Boolean {
val length = a2.size
for (k in 0..length - 1)
{
if (a1[k] != a2[k])
return false
}
return true
}

internal fun existsName(dic:PdfDictionary, key:PdfName, value:PdfName):Boolean {
val type = getPdfObjectRelease(dic.get(key))
if (type == null || !type.isName)
return false
val name = type as PdfName?
return name == value
}

internal fun getFontName(dic:PdfDictionary?):String? {
if (dic == null)
return null
val type = getPdfObjectRelease(dic.get(PdfName.BASEFONT))
if (type == null || !type.isName)
return null
return PdfName.decodeName(type.toString())
}

internal fun getSubsetPrefix(dic:PdfDictionary?):String? {
if (dic == null)
return null
val s = getFontName(dic) ?: return null
if (s.length < 8 || s[6] != '+')
return null
for (k in 0..5)
{
val c = s[k]
if (c < 'A' || c > 'Z')
return null
}
return s
}

private fun getNameArray(obj:PdfObject?):PdfArray? {
var obj:PdfObject? = obj ?: return null
obj = getPdfObjectRelease(obj)
if (obj == null)
return null
if (obj.isArray)
return obj as PdfArray?
else if (obj.isDictionary)
{
val arr2 = getPdfObjectRelease((obj as PdfDictionary).get(PdfName.D))
if (arr2 != null && arr2.isArray)
return arr2 as PdfArray?
}
return null
}

protected fun duplicatePdfDictionary(original:PdfDictionary, copy:PdfDictionary?, newReader:PdfReader):PdfDictionary {
var copy = copy
if (copy == null)
copy = PdfDictionary(original.size())
for (element in original.keys)
{
copy.put(element, duplicatePdfObject(original.get(element), newReader))
}
return copy
}

protected fun duplicatePdfObject(original:PdfObject?, newReader:PdfReader):PdfObject? {
if (original == null)
return null
when (original.type()) {
PdfObject.DICTIONARY -> {
return duplicatePdfDictionary(original as PdfDictionary?, null, newReader)
}
PdfObject.STREAM -> {
val org = original as PRStream?
val stream = PRStream(org, null, newReader)
duplicatePdfDictionary(org, stream, newReader)
return stream
}
PdfObject.ARRAY -> {
val originalArray = original as PdfArray?
val arr = PdfArray(originalArray.size())
val it = originalArray.listIterator()
while (it.hasNext())
{
arr.add(duplicatePdfObject(it.next(), newReader))
}
return arr
}
PdfObject.INDIRECT -> {
val org = original as PRIndirectReference?
return PRIndirectReference(newReader, org.number, org.generation)
}
else -> return original
}
}
}
}/**
       * Reads and parses a PDF document.
       * @param filename the file name of the document
      * * 
 * @throws IOException on error
 *//**
       * Reads and parses a PDF document.
       * @param filename the file name of the document
      * * 
 * @param ownerPassword the password to read the document
      * * 
 * @throws IOException on error
 *//**
       * Reads and parses a PDF document.
       * @param pdfIn the byte array with the document
      * * 
 * @throws IOException on error
 *//**
       * Reads and parses a PDF document.
       * @param url the URL of the document
      * * 
 * @throws IOException on error
 *//**
       * Reads and parses a PDF document.
       * @param is the InputStream containing the document. The stream is read to the
      * * end but is not closed
      * * 
 * @throws IOException on error
 *//**
       * Reads and parses a pdf document. Contrary to the other constructors only the xref is read
       * into memory. The reader is said to be working in "partial" mode as only parts of the pdf
       * are read as needed.
       * @param raf the document location
      * * 
 * @param ownerPassword the password or null for no password
      * * 
 * @throws IOException on error
 *//** Sets the contents of the page.
       * @param content the new page content
      * * 
 * @param pageNum the page number. 1 is the first
 *//** Sets the contents of the page.
       * @param content the new page content
      * * 
 * @param pageNum the page number. 1 is the first
      * * 
 * @param compressionLevel the compressionLevel
      * * 
 * @since	2.1.3	(the method already existed without param compressionLevel)
 *//**
       * Decode a byte[] applying the filters specified in the provided dictionary using default filter handlers.
       * @param b the bytes to decode
      * * 
 * @param streamDictionary the dictionary that contains filter information
      * * 
 * @return the decoded bytes
      * * 
 * @throws IOException if there are any problems decoding the bytes
      * * 
 * @since 5.0.4
 */
