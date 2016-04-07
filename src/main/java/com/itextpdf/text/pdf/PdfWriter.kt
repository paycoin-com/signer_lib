/*
 * $Id: 950bbd342c59f672ea72821289066de2bcd7b0d9 $
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

import com.itextpdf.text.*
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.io.TempFileCache
import com.itextpdf.text.log.Counter
import com.itextpdf.text.log.CounterFactory
import com.itextpdf.text.pdf.collection.PdfCollection
import com.itextpdf.text.pdf.events.PdfPageEventForwarder
import com.itextpdf.text.pdf.interfaces.*
import com.itextpdf.text.pdf.internal.PdfIsoKeys
import com.itextpdf.text.pdf.internal.PdfVersionImp
import com.itextpdf.text.pdf.internal.PdfXConformanceImp
import com.itextpdf.text.xml.xmp.PdfProperties
import com.itextpdf.text.xml.xmp.XmpWriter
import com.itextpdf.xmp.XMPConst
import com.itextpdf.xmp.XMPException
import com.itextpdf.xmp.options.PropertyOptions

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.security.cert.Certificate
import java.util.*

/**
   * A DocWriter class for PDF.
   * 
   * When this PdfWriter is added
   * to a certain PdfDocument, the PDF representation of every Element
   * added to this Document will be written to the outputstream.
  */

open class PdfWriter:DocWriter, PdfViewerPreferences, PdfEncryptionSettings, PdfVersion, PdfDocumentActions, PdfPageActions, PdfRunDirection, PdfAnnotations {

// INNER CLASSES

    /**
       * This class generates the structure of a PDF document.
       * 
       * This class covers the third section of Chapter 5 in the 'Portable Document Format
       * Reference Manual version 1.3' (page 55-60). It contains the body of a PDF document
       * (section 5.14) and it can also generate a Cross-reference Table (section 5.15).
     
       * @see PdfWriter
      
 * @see PdfObject
      
 * @see PdfIndirectObject
 */

     class PdfBody// constructors

        /**
           * Constructs a new PdfBody.
           * @param writer
 */
         protected constructor(protected val writer:PdfWriter) {

// inner classes

        /**
           * PdfCrossReference is an entry in the PDF Cross-Reference table.
          */

         class PdfCrossReference:Comparable<PdfCrossReference> {

// membervariables
            private val type:Int

/**	Byte offset in the PDF file.  */
            private val offset:Long

 val refnum:Int
/**	generation of the object.  */
            private val generation:Int

// constructors
            /**
               * Constructs a cross-reference element for a PdfIndirectObject.
               * @param refnum
              * * 
 * @param	offset		byte offset of the object
              * * 
 * @param	generation	generation number of the object
 */

             constructor(refnum:Int, offset:Long, generation:Int) {
type = 0
this.offset = offset
this.refnum = refnum
this.generation = generation
}

/**
               * Constructs a cross-reference element for a PdfIndirectObject.
               * @param refnum
              * * 
 * @param	offset		byte offset of the object
 */

             constructor(refnum:Int, offset:Long) {
type = 1
this.offset = offset
this.refnum = refnum
this.generation = 0
}

 constructor(type:Int, refnum:Int, offset:Long, generation:Int) {
this.type = type
this.offset = offset
this.refnum = refnum
this.generation = generation
}

/**
               * Returns the PDF representation of this PdfObject.
               * @param os
              * * 
 * @throws IOException
 */

            @Throws(IOException::class)
 fun toPdf(os:OutputStream) {
val off = StringBuffer("0000000000").append(offset)
off.delete(0, off.length - 10)
val gen = StringBuffer("00000").append(generation)
gen.delete(0, gen.length - 5)

off.append(' ').append(gen).append(if (generation == GENERATION_MAX) " f \n" else " n \n")
os.write(DocWriter.getISOBytes(off.toString()))
}

/**
               * Writes PDF syntax to the OutputStream
               * @param midSize
              * * 
 * @param os
              * * 
 * @throws IOException
 */
            @Throws(IOException::class)
 fun toPdf(midSize:Int, os:OutputStream) {
var midSize = midSize
os.write(type.toByte().toInt())
while (--midSize >= 0)
os.write((offset.ushr(8 * midSize) and 0xff).toByte().toInt())
os.write((generation.ushr(8) and 0xff).toByte().toInt())
os.write((generation and 0xff).toByte().toInt())
}

/**
               * @see java.lang.Comparable.compareTo
 */
            override fun compareTo(other:PdfCrossReference):Int {
return if (refnum < other.refnum) -1 else if (refnum == other.refnum) 0 else 1
}

/**
               * @see java.lang.Object.equals
 */
            override fun equals(obj:Any?):Boolean {
if (obj is PdfCrossReference)
{
return refnum == obj.refnum
}
else
return false
}

/**
               * @see java.lang.Object.hashCode
 */
            override fun hashCode():Int {
return refnum
}

}

// membervariables

        /** array containing the cross-reference table of the normal objects.  */
        protected val xrefs:TreeSet<PdfCrossReference>
protected var refnum:Int = 0
/** the current byte position in the body.  */
        protected var position:Long = 0
protected var index:ByteBuffer? = null
protected var streamObjects:ByteBuffer? = null
protected var currentObjNum:Int = 0
protected var numObj = 0
init{
xrefs = TreeSet<PdfCrossReference>()
xrefs.add(PdfCrossReference(0, 0, GENERATION_MAX))
position = writer.os.counter
refnum = 1
}

// methods

        internal fun setRefnum(refnum:Int) {
this.refnum = refnum
}

@Throws(IOException::class)
protected fun addToObjStm(obj:PdfObject, nObj:Int):PdfWriter.PdfBody.PdfCrossReference {
if (numObj >= OBJSINSTREAM)
flushObjStm()
if (index == null)
{
index = ByteBuffer()
streamObjects = ByteBuffer()
currentObjNum = indirectReferenceNumber
numObj = 0
}
val p = streamObjects!!.size()
val idx = numObj++
val enc = writer.encryption
writer.encryption = null
obj.toPdf(writer, streamObjects)
writer.encryption = enc
streamObjects!!.append(' ')
index!!.append(nObj).append(' ').append(p).append(' ')
return PdfWriter.PdfBody.PdfCrossReference(2, nObj, currentObjNum.toLong(), idx)
}

@Throws(IOException::class)
 fun flushObjStm() {
if (numObj == 0)
return 
val first = index!!.size()
index!!.append(streamObjects)
val stream = PdfStream(index!!.toByteArray())
stream.flateCompress(writer.compressionLevel)
stream.put(PdfName.TYPE, PdfName.OBJSTM)
stream.put(PdfName.N, PdfNumber(numObj))
stream.put(PdfName.FIRST, PdfNumber(first))
add(stream, currentObjNum)
index = null
streamObjects = null
numObj = 0
}

@Throws(IOException::class)
internal fun add(`object`:PdfObject, inObjStm:Boolean):PdfIndirectObject {
return add(`object`, indirectReferenceNumber, 0, inObjStm)
}

/**
           * Gets a PdfIndirectReference for an object that will be created in the future.
           * @return a PdfIndirectReference
 */

         val pdfIndirectReference:PdfIndirectReference
get() =PdfIndirectReference(0, indirectReferenceNumber)

protected val indirectReferenceNumber:Int
get() {
val n = refnum++
xrefs.add(PdfCrossReference(n, 0, GENERATION_MAX))
return n
}

@Throws(IOException::class)
@JvmOverloads internal fun add(`object`:PdfObject, ref:PdfIndirectReference, inObjStm:Boolean = true):PdfIndirectObject {
return add(`object`, ref.number, ref.generation, inObjStm)
}

@Throws(IOException::class)
@JvmOverloads internal fun add(`object`:PdfObject, refNumber:Int = indirectReferenceNumber):PdfIndirectObject {
return add(`object`, refNumber, 0, true) // to false
}

@Throws(IOException::class)
protected fun add(`object`:PdfObject, refNumber:Int, generation:Int, inObjStm:Boolean):PdfIndirectObject {
if (inObjStm && `object`.canBeInObjStm() && writer.isFullCompression)
{
val pxref = addToObjStm(`object`, refNumber)
val indirect = PdfIndirectObject(refNumber, `object`, writer)
if (!xrefs.add(pxref))
{
xrefs.remove(pxref)
xrefs.add(pxref)
}
return indirect
}
else
{
val indirect:PdfIndirectObject
if (writer.isFullCompression)
{
indirect = PdfIndirectObject(refNumber, `object`, writer)
write(indirect, refNumber)
}
else
{
indirect = PdfIndirectObject(refNumber, generation, `object`, writer)
write(indirect, refNumber, generation)
}
return indirect
}
}

@Throws(IOException::class)
protected fun write(indirect:PdfIndirectObject, refNumber:Int) {
val pxref = PdfCrossReference(refNumber, position)
if (!xrefs.add(pxref))
{
xrefs.remove(pxref)
xrefs.add(pxref)
}
indirect.writeTo(writer.os)
position = writer.os.counter
}

@Throws(IOException::class)
protected fun write(indirect:PdfIndirectObject, refNumber:Int, generation:Int) {
val pxref = PdfCrossReference(refNumber, position, generation)
if (!xrefs.add(pxref))
{
xrefs.remove(pxref)
xrefs.add(pxref)
}
indirect.writeTo(writer.os)
position = writer.os.counter
}

/**
           * Returns the offset of the Cross-Reference table.
         
           * @return		an offset
 */

         fun offset():Long {
return position
}

/**
           * Returns the total number of objects contained in the CrossReferenceTable of this Body.
         
           * @return	a number of objects
 */

         fun size():Int {
return Math.max(xrefs.last().refnum + 1, refnum)
}

/**
           * Returns the CrossReferenceTable of the Body.
           * @param os
          * * 
 * @param root
          * * 
 * @param info
          * * 
 * @param encryption
          * * 
 * @param fileID
          * * 
 * @param prevxref
          * * 
 * @throws IOException
 */

        @Throws(IOException::class)
 fun writeCrossReferenceTable(os:OutputStream, root:PdfIndirectReference, info:PdfIndirectReference?, encryption:PdfIndirectReference?, fileID:PdfObject?, prevxref:Long) {
var refNumber = 0
if (writer.isFullCompression)
{
flushObjStm()
refNumber = indirectReferenceNumber
xrefs.add(PdfCrossReference(refNumber, position))
}
var entry = xrefs.first()
var first = entry.refnum
var len = 0
val sections = ArrayList<Int>()
for (pdfCrossReference in xrefs)
{
entry = pdfCrossReference
if (first + len == entry.refnum)
++len
else
{
sections.add(Integer.valueOf(first))
sections.add(Integer.valueOf(len))
first = entry.refnum
len = 1
}
}
sections.add(Integer.valueOf(first))
sections.add(Integer.valueOf(len))
if (writer.isFullCompression)
{
var mid = 5
var mask = 0xff00000000L
while (mid > 1)
{
if (mask and position != 0)
break
mask = mask ushr 8
--mid
}
var buf:ByteBuffer? = ByteBuffer()

for (element in xrefs)
{
entry = element
entry.toPdf(mid, buf)
}
val xr = PdfStream(buf!!.toByteArray())
buf = null
xr.flateCompress(writer.compressionLevel)
xr.put(PdfName.SIZE, PdfNumber(size()))
xr.put(PdfName.ROOT, root)
if (info != null)
{
xr.put(PdfName.INFO, info)
}
if (encryption != null)
xr.put(PdfName.ENCRYPT, encryption)
if (fileID != null)
xr.put(PdfName.ID, fileID)
xr.put(PdfName.W, PdfArray(intArrayOf(1, mid, 2)))
xr.put(PdfName.TYPE, PdfName.XREF)
val idx = PdfArray()
for (k in sections.indices)
idx.add(PdfNumber(sections[k].toInt()))
xr.put(PdfName.INDEX, idx)
if (prevxref > 0)
xr.put(PdfName.PREV, PdfNumber(prevxref))
val enc = writer.encryption
writer.encryption = null
val indirect = PdfIndirectObject(refNumber, xr, writer)
indirect.writeTo(writer.os)
writer.encryption = enc
}
else
{
os.write(DocWriter.getISOBytes("xref\n"))
val i = xrefs.iterator()
var k = 0
while (k < sections.size)
{
first = sections[k].toInt()
len = sections[k + 1].toInt()
os.write(DocWriter.getISOBytes(first.toString()))
os.write(DocWriter.getISOBytes(" "))
os.write(DocWriter.getISOBytes(len.toString()))
os.write('\n')
while (len-- > 0)
{
entry = i.next()
entry.toPdf(os)
}
k += 2
}
}
}

companion object {

private val OBJSINSTREAM = 200
}
}/**
           * Adds a PdfObject to the body.
           * 
           * This methods creates a PdfIndirectObject with a
           * certain number, containing the given PdfObject.
           * It also adds a PdfCrossReference for this object
           * to an ArrayList that will be used to build the
           * Cross-reference Table.
         
           * @param		object			a PdfObject
          * * 
 * @return		a PdfIndirectObject
          * * 
 * @throws IOException
 *//**
           * Adds a PdfObject to the body given an already existing
           * PdfIndirectReference.
           * 
           * This methods creates a PdfIndirectObject with the number given by
           * ref, containing the given PdfObject.
           * It also adds a PdfCrossReference for this object
           * to an ArrayList that will be used to build the
           * Cross-reference Table.
         
           * @param		object			a PdfObject
          * * 
 * @param		ref		        a PdfIndirectReference
          * * 
 * @return		a PdfIndirectObject
          * * 
 * @throws IOException
 */

/**
       * PdfTrailer is the PDF Trailer object.
       * 
       * This object is described in the 'Portable Document Format Reference Manual version 1.3'
       * section 5.16 (page 59-60).
      */

     class PdfTrailer// constructors

        /**
           * Constructs a PDF-Trailer.
         
           * @param		size		the number of entries in the PdfCrossReferenceTable
          * * 
 * @param		offset		offset of the PdfCrossReferenceTable
          * * 
 * @param		root		an indirect reference to the root of the PDF document
          * * 
 * @param		info		an indirect reference to the info object of the PDF document
          * * 
 * @param encryption
          * * 
 * @param fileID
          * * 
 * @param prevxref
 */

        (size:Int, // membervariables

        internal var offset:Long, root:PdfIndirectReference, info:PdfIndirectReference?, encryption:PdfIndirectReference?, fileID:PdfObject?, prevxref:Long):PdfDictionary() {
init{
put(PdfName.SIZE, PdfNumber(size))
put(PdfName.ROOT, root)
if (info != null)
{
put(PdfName.INFO, info)
}
if (encryption != null)
put(PdfName.ENCRYPT, encryption)
if (fileID != null)
put(PdfName.ID, fileID)
if (prevxref > 0)
put(PdfName.PREV, PdfNumber(prevxref))
}

/**
           * Returns the PDF representation of this PdfObject.
           * @param writer
          * * 
 * @param os
          * * 
 * @throws IOException
 */
        @Throws(IOException::class)
override fun toPdf(writer:PdfWriter, os:OutputStream) {
PdfWriter.checkPdfIsoConformance(writer, PdfIsoKeys.PDFISOKEY_TRAILER, this)
os.write(DocWriter.getISOBytes("trailer\n"))
super.toPdf(null, os)
os.write('\n')
writeKeyInfo(os)
os.write(DocWriter.getISOBytes("startxref\n"))
os.write(DocWriter.getISOBytes(offset.toString()))
os.write(DocWriter.getISOBytes("\n%%EOF\n"))
}
}
protected open val counter:Counter
get() =COUNTER

//	Construct a PdfWriter instance

    /**
       * Constructs a PdfWriter.
      */
    protected constructor() {}

/**
       * Constructs a PdfWriter.
       * 
       * Remark: a PdfWriter can only be constructed by calling the method
       * getInstance(Document document, OutputStream os).
     
       * @param	document	The PdfDocument that has to be written
      * * 
 * @param	os			The OutputStream the writer has to write to.
 */

    protected constructor(document:PdfDocument, os:OutputStream) : super(document, os) {
pdfDocument = document
directContentUnder = PdfContentByte(this)
directContent = directContentUnder.duplicate
}

//	the PdfDocument instance

    /** the pdfdocument object.  */
    /**
       * Gets the PdfDocument associated with this writer.
       * @return the PdfDocument
 */

    internal var pdfDocument:PdfDocument
protected set

/**
       * Use this method to get the info dictionary if you want to
       * change it directly (add keys and values to the info dictionary).
       * @return the info dictionary
 */
     val info:PdfDictionary
get() =pdfDocument.info

/**
       * Use this method to get the current vertical page position.
       * @param ensureNewLine Tells whether a new line shall be enforced. This may cause side effects
      * *   for elements that do not terminate the lines they've started because those lines will get
      * *   terminated.
      * * 
 * @return The current vertical page position.
 */
     fun getVerticalPosition(ensureNewLine:Boolean):Float {
return pdfDocument.getVerticalPosition(ensureNewLine)
}

/**
       * Sets the initial leading for the PDF document.
       * This has to be done before the document is opened.
       * @param	leading	the initial leading
      * * 
 * @since	2.1.6
      * * 
 * @throws	DocumentException	if you try setting the leading after the document was opened.
 */
    @Throws(DocumentException::class)
 fun setInitialLeading(leading:Float) {
if (open)
throw DocumentException(MessageLocalization.getComposedMessage("you.can.t.set.the.initial.leading.if.the.document.is.already.open"))
pdfDocument.leading = leading
}

//	the PdfDirectContentByte instances

/*
 * You should see Direct Content as a canvas on which you can draw
 * graphics and text. One canvas goes on top of the page (getDirectContent),
 * the other goes underneath (getDirectContentUnder).
 * You can always the same object throughout your document,
 * even if you have moved to a new page. Whatever you add on
 * the canvas will be displayed on top or under the current page.
 */

    /** The direct content in this document.  */
    protected var directContent:PdfContentByte

/** The direct content under in this document.  */
    protected var directContentUnder:PdfContentByte

/**
       * Use this method to get the direct content for this document.
       * There is only one direct content, multiple calls to this method
       * will allways retrieve the same object.
       * @return the direct content
 */

    open fun getDirectContent():PdfContentByte {
if (!open)
throw RuntimeException(MessageLocalization.getComposedMessage("the.document.is.not.open"))
return directContent
}

/**
       * Use this method to get the direct content under for this document.
       * There is only one direct content, multiple calls to this method
       * will always retrieve the same object.
       * @return the direct content
 */

    open fun getDirectContentUnder():PdfContentByte {
if (!open)
throw RuntimeException(MessageLocalization.getComposedMessage("the.document.is.not.open"))
return directContentUnder
}

/**
       * Resets all the direct contents to empty.
       * This happens when a new page is started.
      */
    internal fun resetContent() {
directContent.reset()
directContentUnder.reset()
}

//	PDF body

/*
 * A PDF file has 4 parts: a header, a body, a cross-reference table, and a trailer.
 * The body contains all the PDF objects that make up the PDF document.
 * Each element gets a reference (a set of numbers) and the byte position of
 * every object is stored in the cross-reference table.
 * Use these methods only if you know what you're doing.
 */

    /** body of the PDF document  */
    protected var body:PdfBody

 var colorProfile:ICC_Profile
protected set

/**
       * Adds the local destinations to the body of the document.
       * @param desto the HashMap containing the destinations
      * * 
 * @throws IOException on error
 */

    @Throws(IOException::class)
internal fun addLocalDestinations(desto:TreeMap<String, PdfDocument.Destination>) {
for ((name, dest) in desto)
{
val destination = dest.destination
if (dest.reference == null)
dest.reference = pdfIndirectReference
if (destination == null)
addToBody(PdfString("invalid_" + name), dest.reference)
else
addToBody(destination, dest.reference)
}
}

/**
       * Use this method to add a PDF object to the PDF body.
       * Use this method only if you know what you're doing!
       * @param object
      * * 
 * @return a PdfIndirectObject
      * * 
 * @throws IOException
 */
    @Throws(IOException::class)
 fun addToBody(`object`:PdfObject):PdfIndirectObject {
val iobj = body.add(`object`)
cacheObject(iobj)
return iobj
}

/**
       * Use this method to add a PDF object to the PDF body.
       * Use this method only if you know what you're doing!
       * @param object
      * * 
 * @param inObjStm
      * * 
 * @return a PdfIndirectObject
      * * 
 * @throws IOException
 */
    @Throws(IOException::class)
 fun addToBody(`object`:PdfObject, inObjStm:Boolean):PdfIndirectObject {
val iobj = body.add(`object`, inObjStm)
cacheObject(iobj)
return iobj
}

/**
       * Use this method to add a PDF object to the PDF body.
       * Use this method only if you know what you're doing!
       * @param object
      * * 
 * @param ref
      * * 
 * @return a PdfIndirectObject
      * * 
 * @throws IOException
 */
    @Throws(IOException::class)
open fun addToBody(`object`:PdfObject, ref:PdfIndirectReference):PdfIndirectObject {
val iobj = body.add(`object`, ref)
cacheObject(iobj)
return iobj
}

/**
       * Use this method to add a PDF object to the PDF body.
       * Use this method only if you know what you're doing!
       * @param object
      * * 
 * @param ref
      * * 
 * @param inObjStm
      * * 
 * @return a PdfIndirectObject
      * * 
 * @throws IOException
 */
    @Throws(IOException::class)
open fun addToBody(`object`:PdfObject, ref:PdfIndirectReference, inObjStm:Boolean):PdfIndirectObject {
val iobj = body.add(`object`, ref, inObjStm)
cacheObject(iobj)
return iobj
}

/**
       * Use this method to add a PDF object to the PDF body.
       * Use this method only if you know what you're doing!
       * @param object
      * * 
 * @param refNumber
      * * 
 * @return a PdfIndirectObject
      * * 
 * @throws IOException
 */
    @Throws(IOException::class)
 fun addToBody(`object`:PdfObject, refNumber:Int):PdfIndirectObject {
val iobj = body.add(`object`, refNumber)
cacheObject(iobj)
return iobj
}

/**
       * Use this method to add a PDF object to the PDF body.
       * Use this method only if you know what you're doing!
       * @param object
      * * 
 * @param refNumber
      * * 
 * @param inObjStm
      * * 
 * @return a PdfIndirectObject
      * * 
 * @throws IOException
 */
    @Throws(IOException::class)
 fun addToBody(`object`:PdfObject, refNumber:Int, inObjStm:Boolean):PdfIndirectObject {
val iobj = body.add(`object`, refNumber, 0, inObjStm)
cacheObject(iobj)
return iobj
}

/**
       * Use this method for caching objects.
       * @param iobj @see PdfIndirectObject
 */
    protected open fun cacheObject(iobj:PdfIndirectObject) {}

/**
       * Use this to get an PdfIndirectReference for an object that
       * will be created in the future.
       * Use this method only if you know what you're doing!
       * @return the PdfIndirectReference
 */

     val pdfIndirectReference:PdfIndirectReference
get() =body.pdfIndirectReference

protected val indirectReferenceNumber:Int
get() =body.indirectReferenceNumber

/**
       * Returns the outputStreamCounter.
       * @return the outputStreamCounter
 */
     val os:OutputStreamCounter
get() =os


//	PDF Catalog

/*
 * The Catalog is also called the root object of the document.
 * Whereas the Cross-Reference maps the objects number with the
 * byte offset so that the viewer can find the objects, the
 * Catalog tells the viewer the numbers of the objects needed
 * to render the document.
 */

    protected open fun getCatalog(rootObj:PdfIndirectReference):PdfDictionary {
val catalog = pdfDocument.getCatalog(rootObj)
// [F12] tagged PDF
        buildStructTreeRootForTagged(catalog)
// [F13] OCG
        if (!documentOCG.isEmpty())
{
fillOCProperties(false)
catalog.put(PdfName.OCPROPERTIES, OCProperties)
}
return catalog
}

protected fun buildStructTreeRootForTagged(catalog:PdfDictionary) {
if (isTagged)
{
try
{
getStructureTreeRoot().buildTree()
for (elementId in pdfDocument.structElements)
{
val element = pdfDocument.getStructElement(elementId, false)
addToBody(element, element.reference)
}

}
catch (e:Exception) {
throw ExceptionConverter(e)
}

catalog.put(PdfName.STRUCTTREEROOT, structureTreeRoot!!.reference)
val mi = PdfDictionary()
mi.put(PdfName.MARKED, PdfBoolean.PDFTRUE)
if (isUserProperties)
mi.put(PdfName.USERPROPERTIES, PdfBoolean.PDFTRUE)
catalog.put(PdfName.MARKINFO, mi)
}
}

/** Holds value of property extraCatalog this is used for Output Intents.  */
    protected var extraCatalog:PdfDictionary? = null

/**
       * Sets extra keys to the catalog.
       * @return the catalog to change
 */
     fun getExtraCatalog():PdfDictionary {
if (extraCatalog == null)
extraCatalog = PdfDictionary()
return this.extraCatalog
}

//	PdfPages

/*
 * The page root keeps the complete page tree of the document.
 * There's an entry in the Catalog that refers to the root
 * of the page tree, the page tree contains the references
 * to pages and other page trees.
 */

    /** The root of the page tree.  */
    protected var root = PdfPages(this)
/** The PdfIndirectReference to the pages.  */
    protected var pageReferences = ArrayList<PdfIndirectReference>()
/** The current page number.  */
     var currentPageNumber = 1
protected set
/**
       * The value of the Tabs entry in the page dictionary.
       * @since	2.1.5
 */
    /**
       * Returns the value to be used for the Tabs entry in the page tree.
       * @return the Tabs PdfName
      * * 
 * @since	2.1.5
 */
    /**
       * Sets the value for the Tabs entry in the page tree.
       * @param	tabs	Can be PdfName.R, PdfName.C or PdfName.S.
      * * Since the Adobe Extensions Level 3, it can also be PdfName.A
      * * or PdfName.W
      * * 
 * @since	2.1.5
 */
     var tabs:PdfName? = null

/**
       * Additional page dictionary entries.
       * @since 5.1.0
 */
    /**
       * Gets the additional pageDictEntries.
       * @return the page dictionary entries
      * * 
 * @since 5.1.0
 */
     var pageDictEntries = PdfDictionary()
protected set

/**
       * Adds an additional entry for the page dictionary.
       * @param key the key
      * * 
 * @param object the PdfObject for the given key
      * * 
 * @since 5.1.0
 */
     fun addPageDictEntry(key:PdfName, `object`:PdfObject) {
pageDictEntries.put(key, `object`)
}

/**
       * Resets the additional pageDictEntries.
       * @since 5.1.0
 */
     fun resetPageDictEntries() {
pageDictEntries = PdfDictionary()
}

/**
       * Use this method to make sure the page tree has a linear structure
       * (every leave is attached directly to the root).
       * Use this method to allow page reordering with method reorderPages.
      */
      fun setLinearPageMode() {
root.setLinearMode(null)
}

/**
       * Use this method to reorder the pages in the document.
       * A null argument value only returns the number of pages to process.
       * It is advisable to issue a Document.newPage() before using this method.
       * @return the total number of pages
      * * 
 * @param order an array with the new page sequence. It must have the
      * * same size as the number of pages.
      * * 
 * @throws DocumentException if all the pages are not present in the array
 */
    @Throws(DocumentException::class)
 fun reorderPages(order:IntArray):Int {
return root.reorderPages(order)
}

/**
       * Use this method to get a reference to a page existing or not.
       * If the page does not exist yet the reference will be created
       * in advance. If on closing the document, a page number greater
       * than the total number of pages was requested, an exception
       * is thrown.
       * @param page the page number. The first page is 1
      * * 
 * @return the reference to the page
 */
    open fun getPageReference(page:Int):PdfIndirectReference {
var page = page
--page
if (page < 0)
throw IndexOutOfBoundsException(MessageLocalization.getComposedMessage("the.page.number.must.be.gt.eq.1"))
var ref:PdfIndirectReference?
if (page < pageReferences.size)
{
ref = pageReferences[page]
if (ref == null)
{
ref = body.pdfIndirectReference
pageReferences[page] = ref
}
}
else
{
val empty = page - pageReferences.size
for (k in 0..empty - 1)
pageReferences.add(null)
ref = body.pdfIndirectReference
pageReferences.add(ref)
}
return ref
}

/**
       * Gets the pagenumber of this document.
       * This number can be different from the real pagenumber,
       * if you have (re)set the page number previously.
       * @return a page number
 */

     val pageNumber:Int
get() =pdfDocument.pageNumber

internal val currentPage:PdfIndirectReference
get() =getPageReference(currentPageNumber)

/**
       * Sets the Viewport for the next page.
       * @param vp an array consisting of Viewport dictionaries.
      * * 
 * @since 5.1.0
 */
     fun setPageViewport(vp:PdfArray) {
addPageDictEntry(PdfName.VP, vp)
}

/**
       * Adds some PdfContents to this Writer.
       * 
       * The document has to be open before you can begin to add content
       * to the body of the document.
     
       * @return a PdfIndirectReference
      * * 
 * @param page the PdfPage to add
      * * 
 * @param contents the PdfContents of the page
      * * 
 * @throws PdfException on error
 */

    @Throws(PdfException::class)
internal open fun add(page:PdfPage, contents:PdfContents):PdfIndirectReference? {
if (!open)
{
throw PdfException(MessageLocalization.getComposedMessage("the.document.is.not.open"))
}
val `object`:PdfIndirectObject
try
{
`object` = addToBody(contents)
}
catch (ioe:IOException) {
throw ExceptionConverter(ioe)
}

page.add(`object`.indirectReference)
// [U5]
        if (group != null)
{
page.put(PdfName.GROUP, group)
group = null
}
else if (isRgbTransparencyBlending)
{
val pp = PdfDictionary()
pp.put(PdfName.TYPE, PdfName.GROUP)
pp.put(PdfName.S, PdfName.TRANSPARENCY)
pp.put(PdfName.CS, PdfName.DEVICERGB)
page.put(PdfName.GROUP, pp)
}
root.addPage(page)
currentPageNumber++
return null
}

//	page events

/*
 * Page events are specific for iText, not for PDF.
 * Upon specific events (for instance when a page starts
 * or ends), the corresponding method in the page event
 * implementation that is added to the writer is invoked.
 */

    /** The PdfPageEvent for this document.  */
    /**
       * Gets the PdfPageEvent for this document or null
       * if none is set.
       * @return the PdfPageEvent for this document or null
      * * if none is set
 */

    /**
       * Sets the PdfPageEvent for this document.
       * @param event the PdfPageEvent for this document
 */

    open var pageEvent:PdfPageEvent? = null
set(event) =if (event == null)
this.pageEvent = null
else if (this.pageEvent == null)
this.pageEvent = event
else if (this.pageEvent is PdfPageEventForwarder)
(this.pageEvent as PdfPageEventForwarder).addPageEvent(event)
else
{
val forward = PdfPageEventForwarder()
forward.addPageEvent(this.pageEvent)
forward.addPageEvent(event)
this.pageEvent = forward
}

//	Open and Close methods + method that create the PDF

    /** A number referring to the previous Cross-Reference Table.  */
    protected var prevxref:Long = 0
/** The original file ID (if present).  */
    protected var originalFileID:ByteArray? = null

/**
       * Signals that the Document has been opened and that
       * Elements can be added.
       * 
       * When this method is called, the PDF-document header is
       * written to the outputstream.
       * @see com.itextpdf.text.DocWriter.open
 */
    override fun open() {
super.open()
try
{
pdfVersion.writeHeader(os)
body = PdfBody(this)
if (isPdfX && (pdfIsoConformance as PdfXConformanceImp).isPdfX32002)
{
val sec = PdfDictionary()
sec.put(PdfName.GAMMA, PdfArray(floatArrayOf(2.2f, 2.2f, 2.2f)))
sec.put(PdfName.MATRIX, PdfArray(floatArrayOf(0.4124f, 0.2126f, 0.0193f, 0.3576f, 0.7152f, 0.1192f, 0.1805f, 0.0722f, 0.9505f)))
sec.put(PdfName.WHITEPOINT, PdfArray(floatArrayOf(0.9505f, 1f, 1.089f)))
val arr = PdfArray(PdfName.CALRGB)
arr.add(sec)
setDefaultColorspace(PdfName.DEFAULTRGB, addToBody(arr).indirectReference)
}
}
catch (ioe:IOException) {
throw ExceptionConverter(ioe)
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
       * @see com.itextpdf.text.DocWriter.close
 */
    override fun close() {
if (open)
{
if (currentPageNumber - 1 != pageReferences.size)
throw RuntimeException("The page " + pageReferences.size + 
" was requested but the document has only " + (currentPageNumber - 1) + " pages.")
pdfDocument.close()
try
{
addSharedObjectsToBody()
for (layer in documentOCG)
{
addToBody(layer.pdfObject, layer.ref)
}
// add the root to the body
                val rootRef = root.writePageTree()
// make the catalog-object and add it to the body
                val catalog = getCatalog(rootRef)
if (!documentOCG.isEmpty())
PdfWriter.checkPdfIsoConformance(this, PdfIsoKeys.PDFISOKEY_LAYER, OCProperties)
// [C9] if there is XMP data to add: add it
                if (xmpMetadata == null && xmpWriter != null)
{
try
{
val baos = ByteArrayOutputStream()
xmpWriter!!.serialize(baos)
xmpWriter!!.close()
xmpMetadata = baos.toByteArray()
}
catch (exc:IOException) {
xmpWriter = null
}
catch (exc:XMPException) {
xmpWriter = null
}

}
if (xmpMetadata != null)
{
val xmp = PdfStream(xmpMetadata)
xmp.put(PdfName.TYPE, PdfName.METADATA)
xmp.put(PdfName.SUBTYPE, PdfName.XML)
if (encryption != null && !encryption!!.isMetadataEncrypted)
{
val ar = PdfArray()
ar.add(PdfName.CRYPT)
xmp.put(PdfName.FILTER, ar)
}
catalog.put(PdfName.METADATA, body.add(xmp).indirectReference)
}
// [C10] make pdfx conformant
                if (isPdfX)
{
completeInfoDictionary(info)
completeExtraCatalog(getExtraCatalog())
}
// [C11] Output Intents
                if (extraCatalog != null)
{
catalog.mergeDifferent(extraCatalog)
}

writeOutlines(catalog, false)

// add the Catalog to the body
                val indirectCatalog = addToBody(catalog, false)
// add the info-object to the body
                val infoObj = addToBody(info, false)

// [F1] encryption
                var encryption:PdfIndirectReference? = null
var fileID:PdfObject? = null
body.flushObjStm()
val isModified = originalFileID != null
if (this.encryption != null)
{
val encryptionObject = addToBody(this.encryption!!.encryptionDictionary, false)
encryption = encryptionObject.indirectReference
fileID = this.encryption!!.getFileID(isModified)
}
else
{
fileID = PdfEncryption.createInfoId(if (isModified) originalFileID else PdfEncryption.createDocumentId(), isModified)
}

// write the cross-reference table of the body
                body.writeCrossReferenceTable(os, indirectCatalog.indirectReference, 
infoObj.indirectReference, encryption, fileID, prevxref)

// make the trailer
                // [F2] full compression
                if (isFullCompression)
{
writeKeyInfo(os)
os.write(DocWriter.getISOBytes("startxref\n"))
os.write(DocWriter.getISOBytes(body.offset().toString()))
os.write(DocWriter.getISOBytes("\n%%EOF\n"))
}
else
{
val trailer = PdfTrailer(body.size(), 
body.offset(), 
indirectCatalog.indirectReference, 
infoObj.indirectReference, 
encryption, 
fileID, prevxref)
trailer.toPdf(this, os)
}
}
catch (ioe:IOException) {
throw ExceptionConverter(ioe)
}
finally
{
super.close()
}
}
counter.written(os.counter)
}

@Throws(IOException::class)
protected fun addXFormsToBody() {
for (objs in formXObjects.values)
{
val template = objs[1] as PdfTemplate
if (template != null && template.indirectReference is PRIndirectReference)
continue
if (template != null && template.type == PdfTemplate.TYPE_TEMPLATE)
{
addToBody(template.getFormXObject(compressionLevel), template.indirectReference)
}
}
}

@Throws(IOException::class)
protected fun addSharedObjectsToBody() {
// [F3] add the fonts
        for (details in documentFonts.values)
{
details.writeFont(this)
}
// [F4] add the form XObjects
        addXFormsToBody()
// [F5] add all the dependencies in the imported pages
        for (element in readerInstances.values)
{
currentPdfReaderInstance = element
currentPdfReaderInstance!!.writeAllPages()
}
currentPdfReaderInstance = null
// [F6] add the spotcolors
        for (color in documentColors.values)
{
addToBody(color.getPdfObject(this), color.indirectReference)
}
// [F7] add the pattern
        for (pat in documentPatterns.keys)
{
addToBody(pat.getPattern(compressionLevel), pat.indirectReference)
}
// [F8] add the shading patterns
        for (shadingPattern in documentShadingPatterns)
{
shadingPattern.addToBody()
}
// [F9] add the shadings
        for (shading in documentShadings)
{
shading.addToBody()
}
// [F10] add the extgstate
        for ((gstate, obj) in documentExtGState)
{
addToBody(gstate, obj[1] as PdfIndirectReference)
}
// [F11] add the properties
        for ((prop, obj) in documentProperties)
{
if (prop is PdfLayerMembership)
{
addToBody(prop.pdfObject, prop.ref)
}
else if (prop is PdfDictionary && prop !is PdfLayer)
{
addToBody(prop, obj[1] as PdfIndirectReference)
}
}
}

// Root data for the PDF document (used when composing the Catalog)

//  [C1] Outlines (bookmarks)

     /**
        * Use this method to get the root outline
        * and construct bookmarks.
        * @return the root outline
 */

      val rootOutline:PdfOutline
get() =directContent.rootOutline

protected var newBookmarks:List<HashMap<String, Any>>? = null

/**
       * Sets the bookmarks. The list structure is defined in
       * [SimpleBookmark].
       * @param outlines the bookmarks or null to remove any
 */
     fun setOutlines(outlines:List<HashMap<String, Any>>) {
newBookmarks = outlines
}

@Throws(IOException::class)
protected fun writeOutlines(catalog:PdfDictionary, namedAsNames:Boolean) {
if (newBookmarks == null || newBookmarks!!.isEmpty())
return 
val top = PdfDictionary()
val topRef = pdfIndirectReference
val kids = SimpleBookmark.iterateOutlines(this, topRef, newBookmarks, namedAsNames)
top.put(PdfName.FIRST, kids[0] as PdfIndirectReference)
top.put(PdfName.LAST, kids[1] as PdfIndirectReference)
top.put(PdfName.COUNT, PdfNumber((kids[2] as Int).toInt()))
addToBody(top, topRef)
catalog.put(PdfName.OUTLINES, topRef)
}

/** Stores the version information for the header and the catalog.  */
    /**
	   * Returns the version information.
	   * @return the PdfVersion
 */
	internal var pdfVersion = PdfVersionImp()
protected set

/** @see com.itextpdf.text.pdf.interfaces.PdfVersion.setPdfVersion
 */
    override fun setPdfVersion(version:Char) {
pdfVersion.setPdfVersion(version)
}

/** @see com.itextpdf.text.pdf.interfaces.PdfVersion.setAtLeastPdfVersion
 */
    override fun setAtLeastPdfVersion(version:Char) {
pdfVersion.setAtLeastPdfVersion(version)
}

/** @see com.itextpdf.text.pdf.interfaces.PdfVersion.setPdfVersion
 */
	 fun setPdfVersion(version:PdfName) {
pdfVersion.setPdfVersion(version)
}

/**
	   * @see com.itextpdf.text.pdf.interfaces.PdfVersion.addDeveloperExtension
 * @since	2.1.6
 */
	 fun addDeveloperExtension(de:PdfDeveloperExtension) {
pdfVersion.addDeveloperExtension(de)
}

/** @see com.itextpdf.text.pdf.interfaces.PdfViewerPreferences.setViewerPreferences
 */
    override fun setViewerPreferences(preferences:Int) {
pdfDocument.setViewerPreferences(preferences)
}

/** @see com.itextpdf.text.pdf.interfaces.PdfViewerPreferences.addViewerPreference
 */
    open fun addViewerPreference(key:PdfName, value:PdfObject) {
pdfDocument.addViewerPreference(key, value)
}

//  [C4] Page labels

    /**
       * Use this method to add page labels
       * @param pageLabels the page labels
 */
     fun setPageLabels(pageLabels:PdfPageLabels) {
pdfDocument.pageLabels = pageLabels
}

//  [C5] named objects: named destinations, javascript, embedded files

    /**
       * Adds named destinations in bulk.
       * Valid keys and values of the map can be found in the map
       * that is created by SimpleNamedDestination.
       * @param	map	a map with strings as keys for the names,
      * * 			and structured strings as values for the destinations
      * * 
 * @param	page_offset	number of pages that has to be added to
      * * 			the page numbers in the destinations (useful if you
      * *          use this method in combination with PdfCopy).
      * * 
 * @since	iText 5.0
 */
     fun addNamedDestinations(map:Map<String, String>, page_offset:Int) {
var page:Int
var dest:String
var destination:PdfDestination
for (entry in map.entries)
{
dest = entry.value
page = Integer.parseInt(dest.substring(0, dest.indexOf(" ")))
destination = PdfDestination(dest.substring(dest.indexOf(" ") + 1))
addNamedDestination(entry.key, page + page_offset, destination)
}
}

/**
       * Adds one named destination.
       * @param	name	the name for the destination
      * * 
 * @param	page	the page number where you want to jump to
      * * 
 * @param	dest	an explicit destination
      * * 
 * @since	iText 5.0
 */
     fun addNamedDestination(name:String, page:Int, dest:PdfDestination) {
val d = PdfDestination(dest)
d.addPage(getPageReference(page))
pdfDocument.localDestination(name, d)
}

/**
        * Use this method to add a JavaScript action at the document level.
        * When the document opens, all this JavaScript runs.
        * @param js The JavaScript action
 */
      fun addJavaScript(js:PdfAction) {
pdfDocument.addJavaScript(js)
}

/**
        * Use this method to add a JavaScript action at the document level.
        * When the document opens, all this JavaScript runs.
        * @param code the JavaScript code
       * * 
 * @param unicode select JavaScript unicode. Note that the internal
       * * Acrobat JavaScript engine does not support unicode,
       * * so this may or may not work for you
 */
     @JvmOverloads  fun addJavaScript(code:String, unicode:Boolean = false) {
addJavaScript(PdfAction.javaScript(code, this, unicode))
}
/**
        * Use this method to add a JavaScript action at the document level.
        * When the document opens, all this JavaScript runs.
        * @param name	The name of the JS Action in the name tree
       * * 
 * @param js The JavaScript action
 */
      fun addJavaScript(name:String, js:PdfAction) {
pdfDocument.addJavaScript(name, js)
}

/**
        * Use this method to add a JavaScript action at the document level.
        * When the document opens, all this JavaScript runs.
        * @param name	The name of the JS Action in the name tree
       * * 
 * @param code the JavaScript code
       * * 
 * @param unicode select JavaScript unicode. Note that the internal
       * * Acrobat JavaScript engine does not support unicode,
       * * so this may or may not work for you
 */
     @JvmOverloads  fun addJavaScript(name:String, code:String, unicode:Boolean = false) {
addJavaScript(name, PdfAction.javaScript(code, this, unicode))
}

/**
        * Use this method to add a file attachment at the document level.
        * @param description the file description
       * * 
 * @param fileStore an array with the file. If it's null
       * * the file will be read from the disk
       * * 
 * @param file the path to the file. It will only be used if
       * * fileStore is not null
       * * 
 * @param fileDisplay the actual file name stored in the pdf
       * * 
 * @throws IOException on error
 */
     @Throws(IOException::class)
 fun addFileAttachment(description:String, fileStore:ByteArray, file:String, fileDisplay:String) {
addFileAttachment(description, PdfFileSpecification.fileEmbedded(this, file, fileDisplay, fileStore))
}

/**
        * Use this method to add a file attachment at the document level.
        * @param description the file description
       * * 
 * @param fs the file specification
      * * 
 * @throws IOException if the file attachment could not be added to the document
 */
     @Throws(IOException::class)
 fun addFileAttachment(description:String?, fs:PdfFileSpecification) {
pdfDocument.addFileAttachment(description, fs)
}

/**
        * Use this method to add a file attachment at the document level.
        * @param fs the file specification
      * * 
 * @throws IOException if the file attachment could not be added to the document
 */
     @Throws(IOException::class)
 fun addFileAttachment(fs:PdfFileSpecification) {
addFileAttachment(null, fs)
}

/** @see com.itextpdf.text.pdf.interfaces.PdfDocumentActions.setOpenAction
 */
    override fun setOpenAction(name:String) {
pdfDocument.setOpenAction(name)
}

/** @see com.itextpdf.text.pdf.interfaces.PdfDocumentActions.setOpenAction
 */
    open fun setOpenAction(action:PdfAction) {
pdfDocument.setOpenAction(action)
}

/** @see com.itextpdf.text.pdf.interfaces.PdfDocumentActions.setAdditionalAction
 */
    @Throws(DocumentException::class)
open fun setAdditionalAction(actionType:PdfName, action:PdfAction) {
if (!(actionType == DOCUMENT_CLOSE || 
actionType == WILL_SAVE || 
actionType == DID_SAVE || 
actionType == WILL_PRINT || 
actionType == DID_PRINT))
{
throw DocumentException(MessageLocalization.getComposedMessage("invalid.additional.action.type.1", actionType.toString()))
}
pdfDocument.addAdditionalAction(actionType, action)
}

//  [C7] portable collections

    /**
       * Use this method to add the Collection dictionary.
       * @param collection a dictionary of type PdfCollection
 */
     fun setCollection(collection:PdfCollection) {
setAtLeastPdfVersion(VERSION_1_7)
pdfDocument.setCollection(collection)
}

/** @see com.itextpdf.text.pdf.interfaces.PdfAnnotations.getAcroForm
 */
    override val acroForm:PdfAcroForm
get() =pdfDocument.acroForm

/** @see com.itextpdf.text.pdf.interfaces.PdfAnnotations.addAnnotation
 */
    open fun addAnnotation(annot:PdfAnnotation) {
pdfDocument.addAnnotation(annot)
}

internal open fun addAnnotation(annot:PdfAnnotation, page:Int) {
addAnnotation(annot)
}

/** @see com.itextpdf.text.pdf.interfaces.PdfAnnotations.addCalculationOrder
 */
     fun addCalculationOrder(annot:PdfFormField) {
pdfDocument.addCalculationOrder(annot)
}

/** @see com.itextpdf.text.pdf.interfaces.PdfAnnotations.setSigFlags
 */
    override fun setSigFlags(f:Int) {
pdfDocument.setSigFlags(f)
}

 fun setLanguage(language:String) {
pdfDocument.setLanguage(language)
}

//  [C9] Metadata

    /** XMP Metadata for the document.  */
    protected var xmpMetadata:ByteArray? = null

/**
       * Use this method to set the XMP Metadata.
       * @param xmpMetadata The xmpMetadata to set.
 */
     fun setXmpMetadata(xmpMetadata:ByteArray) {
this.xmpMetadata = xmpMetadata
}

/**
       * Use this method to set the XMP Metadata for each page.
       * @param xmpMetadata The xmpMetadata to set.
      * * 
 * @throws IOException
 */
    @Throws(IOException::class)
 fun setPageXmpMetadata(xmpMetadata:ByteArray) {
pdfDocument.setXmpMetadata(xmpMetadata)
}

 var xmpWriter:XmpWriter? = null
protected set

/**
       * Use this method to creates XMP Metadata based
       * on the metadata in the PdfDocument.
       * @since 5.4.4 just creates XmpWriter instance which will be serialized in close.
 */
    open fun createXmpMetadata() {
try
{
xmpWriter = createXmpWriter(null, pdfDocument.info)
if (isTagged)
{
try
{
xmpWriter!!.xmpMeta.setPropertyInteger(XMPConst.NS_PDFUA_ID, PdfProperties.PART, 1, PropertyOptions(PropertyOptions.SEPARATE_NODE))
}
catch (e:XMPException) {
throw ExceptionConverter(e)
}

}
xmpMetadata = null
}
catch (ioe:IOException) {
ioe.printStackTrace()
}

}

/** Stores the PDF ISO conformance.  */
	protected var pdfIsoConformance = initPdfIsoConformance()

protected fun initPdfIsoConformance():PdfIsoConformance {
return PdfXConformanceImp(this)
}

/** @see com.itextpdf.text.pdf.interfaces.PdfXConformance.getPDFXConformance
 */
    /** @see com.itextpdf.text.pdf.interfaces.PdfXConformance.setPDFXConformance
 */
     var pdfxConformance:Int
get() {
if (pdfIsoConformance is PdfXConformanceImp)
return (pdfIsoConformance as PdfXConformance).getPDFXConformance()
else
return PDFXNONE
}
set(pdfx) {
if (pdfIsoConformance !is PdfXConformanceImp)
return 
if ((pdfIsoConformance as PdfXConformance).getPDFXConformance() === pdfx)
return 
if (pdfDocument.isOpen)
throw PdfXConformanceException(MessageLocalization.getComposedMessage("pdfx.conformance.can.only.be.set.before.opening.the.document"))
if (encryption != null)
throw PdfXConformanceException(MessageLocalization.getComposedMessage("a.pdfx.conforming.document.cannot.be.encrypted"))
if (pdfx != PDFXNONE)
setPdfVersion(VERSION_1_3)
(pdfIsoConformance as PdfXConformance).setPDFXConformance(pdfx)
}

/** @see com.itextpdf.text.pdf.interfaces.PdfXConformance.isPdfX
 */
     val isPdfX:Boolean
get() {
if (pdfIsoConformance is PdfXConformanceImp)
return (pdfIsoConformance as PdfXConformance).isPdfX
else
return false
}

/**
       * Checks if any PDF ISO conformance is necessary.
       * @return `true` if the PDF has to be in conformance with any of the PDF ISO specifications
 */
     val isPdfIso:Boolean
get() =pdfIsoConformance.isPdfIso

//  [C11] Output intents
    /**
       * Sets the values of the output intent dictionary. Null values are allowed to
       * suppress any key.
     
       * @param outputConditionIdentifier a value
      * * 
 * @param outputCondition           a value
      * * 
 * @param registryName              a value
      * * 
 * @param info                      a value
      * * 
 * @param colorProfile              a value
      * * 
 * @since 2.1.5
      * * 
 * @throws IOException on error
 */
    @Throws(IOException::class)
 fun setOutputIntents(outputConditionIdentifier:String?, outputCondition:String?, registryName:String?, info:String?, colorProfile:ICC_Profile?) {
PdfWriter.checkPdfIsoConformance(this, PdfIsoKeys.PDFISOKEY_OUTPUTINTENT, colorProfile)
getExtraCatalog()
val out = PdfDictionary(PdfName.OUTPUTINTENT)
if (outputCondition != null)
out.put(PdfName.OUTPUTCONDITION, PdfString(outputCondition, PdfObject.TEXT_UNICODE))
if (outputConditionIdentifier != null)
out.put(PdfName.OUTPUTCONDITIONIDENTIFIER, PdfString(outputConditionIdentifier, PdfObject.TEXT_UNICODE))
if (registryName != null)
out.put(PdfName.REGISTRYNAME, PdfString(registryName, PdfObject.TEXT_UNICODE))
if (info != null)
out.put(PdfName.INFO, PdfString(info, PdfObject.TEXT_UNICODE))
if (colorProfile != null)
{
val stream = PdfICCBased(colorProfile, compressionLevel)
out.put(PdfName.DESTOUTPUTPROFILE, addToBody(stream).indirectReference)
}

out.put(PdfName.S, PdfName.GTS_PDFX)

extraCatalog!!.put(PdfName.OUTPUTINTENTS, PdfArray(out))
this.colorProfile = colorProfile
}

/**
       * Sets the values of the output intent dictionary. Null values are allowed to
       * suppress any key.
     
       * Prefer the ICC_Profile-based version of this method.
       * @param outputConditionIdentifier a value
      * * 
 * @param outputCondition           a value, "PDFA/A" to force GTS_PDFA1, otherwise cued by pdfxConformance.
      * * 
 * @param registryName              a value
      * * 
 * @param info                      a value
      * * 
 * @param destOutputProfile         a value
      * * 
 * @since 1.x
      * *
      * * 
 * @throws IOException
 */
    @Throws(IOException::class)
 fun setOutputIntents(outputConditionIdentifier:String, outputCondition:String, registryName:String, info:String, destOutputProfile:ByteArray?) {
val colorProfile = if (destOutputProfile == null) null else ICC_Profile.getInstance(destOutputProfile)
setOutputIntents(outputConditionIdentifier, outputCondition, registryName, info, colorProfile)
}


/**
       * Use this method to copy the output intent dictionary
       * from another document to this one.
       * @param reader the other document
      * * 
 * @param checkExistence true to just check for the existence of a valid output intent
      * * dictionary, false to insert the dictionary if it exists
      * * 
 * @throws IOException on error
      * * 
 * @return true if the output intent dictionary exists, false
      * * otherwise
 */
    @Throws(IOException::class)
 fun setOutputIntents(reader:PdfReader, checkExistence:Boolean):Boolean {
val catalog = reader.catalog
val outs = catalog.getAsArray(PdfName.OUTPUTINTENTS) ?: return false
if (outs.isEmpty)
return false
val out = outs.getAsDict(0)
val obj = PdfReader.getPdfObject(out.get(PdfName.S))
if (obj == null || PdfName.GTS_PDFX != obj)
return false
if (checkExistence)
return true
val stream = PdfReader.getPdfObject(out.get(PdfName.DESTOUTPUTPROFILE)) as PRStream?
var destProfile:ByteArray? = null
if (stream != null)
{
destProfile = PdfReader.getStreamBytes(stream)
}
setOutputIntents(getNameString(out, PdfName.OUTPUTCONDITIONIDENTIFIER), getNameString(out, PdfName.OUTPUTCONDITION), 
getNameString(out, PdfName.REGISTRYNAME), getNameString(out, PdfName.INFO), destProfile)
return true
}

/** Contains the business logic for cryptography.  */
    internal var encryption:PdfEncryption? = null
protected set

/** @see com.itextpdf.text.pdf.interfaces.PdfEncryptionSettings.setEncryption
 */
    @Throws(DocumentException::class)
override fun setEncryption(userPassword:ByteArray, ownerPassword:ByteArray, permissions:Int, encryptionType:Int) {
if (pdfDocument.isOpen)
throw DocumentException(MessageLocalization.getComposedMessage("encryption.can.only.be.added.before.opening.the.document"))
encryption = PdfEncryption()
encryption!!.setCryptoMode(encryptionType, 0)
encryption!!.setupAllKeys(userPassword, ownerPassword, permissions)
}

/** @see com.itextpdf.text.pdf.interfaces.PdfEncryptionSettings.setEncryption
 */
    @Throws(DocumentException::class)
 fun setEncryption(certs:Array<Certificate>?, permissions:IntArray, encryptionType:Int) {
if (pdfDocument.isOpen)
throw DocumentException(MessageLocalization.getComposedMessage("encryption.can.only.be.added.before.opening.the.document"))
encryption = PdfEncryption()
if (certs != null)
{
for (i in certs.indices)
{
encryption!!.addRecipient(certs[i], permissions[i])
}
}
encryption!!.setCryptoMode(encryptionType, 0)
encryption!!.encryptionDictionary
}

/**
       * Sets the encryption options for this document. The userPassword and the
        * ownerPassword can be null or have zero length. In this case the ownerPassword
        * is replaced by a random string. The open permissions for the document can be
        * AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
        * AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
        * The permissions can be combined by ORing them.
       * @param userPassword the user password. Can be null or empty
      * * 
 * @param ownerPassword the owner password. Can be null or empty
      * * 
 * @param permissions the user permissions
      * * 
 * @param strength128Bits `true` for 128 bit key length, `false` for 40 bit key length
      * * 
 * @throws DocumentException if the document is already open
      * * 
 */
    @Deprecated("")
@Deprecated("As of iText 2.0.3, replaced by (@link #setEncryption(byte[], byte[], int, int)}. Scheduled for removal at or after 2.2.0")
@Throws(DocumentException::class)
 fun setEncryption(userPassword:ByteArray, ownerPassword:ByteArray, permissions:Int, strength128Bits:Boolean) {
setEncryption(userPassword, ownerPassword, permissions, if (strength128Bits) STANDARD_ENCRYPTION_128 else STANDARD_ENCRYPTION_40)
}

/**
       * Sets the encryption options for this document. The userPassword and the
        * ownerPassword can be null or have zero length. In this case the ownerPassword
        * is replaced by a random string. The open permissions for the document can be
        * AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
        * AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
        * The permissions can be combined by ORing them.
       * @param strength `true` for 128 bit key length, `false` for 40 bit key length
      * * 
 * @param userPassword the user password. Can be null or empty
      * * 
 * @param ownerPassword the owner password. Can be null or empty
      * * 
 * @param permissions the user permissions
      * * 
 * @throws DocumentException if the document is already open
      * * 
 */
    @Deprecated("")
@Deprecated("As of iText 2.0.3, replaced by (@link #setEncryption(byte[], byte[], int, int)}. Scheduled for removal at or after 2.2.0")
@Throws(DocumentException::class)
 fun setEncryption(strength:Boolean, userPassword:String, ownerPassword:String, permissions:Int) {
setEncryption(DocWriter.getISOBytes(userPassword), DocWriter.getISOBytes(ownerPassword), permissions, if (strength) STANDARD_ENCRYPTION_128 else STANDARD_ENCRYPTION_40)
}

/**
       * Sets the encryption options for this document. The userPassword and the
        * ownerPassword can be null or have zero length. In this case the ownerPassword
        * is replaced by a random string. The open permissions for the document can be
        * AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
        * AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
        * The permissions can be combined by ORing them.
       * @param encryptionType the type of encryption. It can be one of STANDARD_ENCRYPTION_40, STANDARD_ENCRYPTION_128 or ENCRYPTION_AES128.
      * * Optionally DO_NOT_ENCRYPT_METADATA can be ored to output the metadata in cleartext
      * * 
 * @param userPassword the user password. Can be null or empty
      * * 
 * @param ownerPassword the owner password. Can be null or empty
      * * 
 * @param permissions the user permissions
      * * 
 * @throws DocumentException if the document is already open
      * * 
 */
    @Deprecated("")
@Deprecated("As of iText 2.0.3, replaced by (@link #setEncryption(byte[], byte[], int, int)}. Scheduled for removal at or after 2.2.0")
@Throws(DocumentException::class)
 fun setEncryption(encryptionType:Int, userPassword:String, ownerPassword:String, permissions:Int) {
setEncryption(DocWriter.getISOBytes(userPassword), DocWriter.getISOBytes(ownerPassword), permissions, encryptionType)
}

//  [F2] compression

    /** Holds value of property fullCompression.  */
    /**
       * Use this method to find out if 1.5 compression is on.
       * @return the 1.5 compression status
 */
     var isFullCompression = false
protected set

/**
       * Use this method to set the document's compression to the
       * PDF 1.5 mode with object streams and xref streams.
       * It can be set at any time but once set it can't be unset.
      */
    @Throws(DocumentException::class)
 fun setFullCompression() {
if (open)
throw DocumentException(MessageLocalization.getComposedMessage("you.can.t.set.the.full.compression.if.the.document.is.already.open"))
this.isFullCompression = true
setAtLeastPdfVersion(VERSION_1_5)
}

/**
       * The compression level of the content streams.
       * @since 2.1.3
 */
    /**
       * Returns the compression level used for streams written by this writer.
       * @return the compression level (0 = best speed, 9 = best compression, -1 is default)
      * * 
 * @since 2.1.3
 */
    /**
       * Sets the compression level to be used for streams written by this writer.
       * @param compressionLevel a value between 0 (best speed) and 9 (best compression)
      * * 
 * @since 2.1.3
 */
     var compressionLevel = PdfStream.DEFAULT_COMPRESSION
set(compressionLevel) =if (compressionLevel < PdfStream.NO_COMPRESSION || compressionLevel > PdfStream.BEST_COMPRESSION)
this.compressionLevel = PdfStream.DEFAULT_COMPRESSION
else
this.compressionLevel = compressionLevel

//  [F3] adding fonts

    /** The fonts of this document  */
    protected var documentFonts = LinkedHashMap<BaseFont, FontDetails>()

/** The font number counter for the fonts in the document.  */
    protected var fontNumber = 1

/**
       * Adds a BaseFont to the document but not to the page resources.
       * It is used for templates.
       * @param bf the BaseFont to add
      * * 
 * @return an Object[] where position 0 is a PdfName
      * * and position 1 is an PdfIndirectReference
 */

    internal fun addSimple(bf:BaseFont):FontDetails {
var ret:FontDetails? = documentFonts[bf]
if (ret == null)
{
PdfWriter.checkPdfIsoConformance(this, PdfIsoKeys.PDFISOKEY_FONT, bf)
if (bf.fontType == BaseFont.FONT_TYPE_DOCUMENT)
{
ret = FontDetails(PdfName("F" + fontNumber++), (bf as DocumentFont).indirectReference, bf)
}
else
{
ret = FontDetails(PdfName("F" + fontNumber++), body.pdfIndirectReference, bf)
}
documentFonts.put(bf, ret)
}
return ret
}

internal fun eliminateFontSubset(fonts:PdfDictionary) {
for (element in documentFonts.values)
{
if (fonts.get(element.fontName) != null)
element.isSubset = false
}
}

//  [F4] adding (and releasing) form XObjects

    /** The form XObjects in this document. The key is the xref and the value
         * is Object[]{PdfName, template}. */
    protected var formXObjects = HashMap<PdfIndirectReference, Array<Any>>()

/** The name counter for the form XObjects name.  */
    protected var formXObjectsCounter = 1

/**
       * Adds a template to the document but not to the page resources.
       * @param template the template to add
      * * 
 * @param forcedName the template name, rather than a generated one. Can be null
      * * 
 * @return the PdfName for this template
 */

    internal fun addDirectTemplateSimple(template:PdfTemplate?, forcedName:PdfName?):PdfName {
var template = template
val ref = template!!.indirectReference
val obj = formXObjects[ref]
var name:PdfName? = null
try
{
if (obj == null)
{
if (forcedName == null)
{
name = PdfName("Xf" + formXObjectsCounter)
++formXObjectsCounter
}
else
name = forcedName
if (template.type == PdfTemplate.TYPE_IMPORTED)
{
// If we got here from PdfCopy we'll have to fill importedPages
                    val ip = template as PdfImportedPage?
val r = ip.pdfReaderInstance.reader
if (!readerInstances.containsKey(r))
{
readerInstances.put(r, ip.pdfReaderInstance)
}
template = null
}
formXObjects.put(ref, arrayOf<Any>(name, template))
}
else
name = obj[0] as PdfName
}
catch (e:Exception) {
throw ExceptionConverter(e)
}

return name
}

/**
       * Use this method to releases the memory used by a template.
       * This method writes the template to the output.
       * The template can still be added to any content
       * but changes to the template itself won't have any effect.
       * @param tp the template to release
      * * 
 * @throws IOException on error
 */
    @Throws(IOException::class)
 fun releaseTemplate(tp:PdfTemplate) {
val ref = tp.indirectReference
val objs = formXObjects[ref]
if (objs == null || objs[1] == null)
return 
val template = objs[1] as PdfTemplate
if (template.indirectReference is PRIndirectReference)
return 
if (template.type == PdfTemplate.TYPE_TEMPLATE)
{
addToBody(template.getFormXObject(compressionLevel), template.indirectReference)
objs[1] = null
}
}

//  [F5] adding pages imported form other PDF documents

    /**
       * Instances of PdfReader/PdfReaderInstance that are used to import pages.
       * @since 5.0.3
 */
    protected var readerInstances = HashMap<PdfReader, PdfReaderInstance>()

/**
       * Use this method to get a page from other PDF document.
       * The page can be used as any other PdfTemplate.
       * Note that calling this method more than once with the same parameters
       * will retrieve the same object.
       * @param reader the PDF document where the page is
      * * 
 * @param pageNumber the page number. The first page is 1
      * * 
 * @return the template representing the imported page
 */
    open fun getImportedPage(reader:PdfReader, pageNumber:Int):PdfImportedPage {
return getPdfReaderInstance(reader).getImportedPage(pageNumber)
}

/**
       * Returns the PdfReaderInstance associated with the specified reader.
       * Multiple calls with the same reader object will return the same
       * PdfReaderInstance.
       * @param reader the PDF reader that you want an instance for
      * * 
 * @return the instance for the provided reader
      * * 
 * @since 5.0.3
 */
    protected fun getPdfReaderInstance(reader:PdfReader):PdfReaderInstance {
var inst:PdfReaderInstance? = readerInstances[reader]
if (inst == null)
{
inst = reader.getPdfReaderInstance(this)
readerInstances.put(reader, inst)
}
return inst
}

/**
       * Use this method to writes the reader to the document
       * and free the memory used by it.
       * The main use is when concatenating multiple documents
       * to keep the memory usage restricted to the current
       * appending document.
       * @param reader the PdfReader to free
      * * 
 * @throws IOException on error
 */
    @Throws(IOException::class)
open fun freeReader(reader:PdfReader) {
currentPdfReaderInstance = readerInstances[reader]
if (currentPdfReaderInstance == null)
return 
currentPdfReaderInstance!!.writeAllPages()
currentPdfReaderInstance = null
readerInstances.remove(reader)
}

/**
       * Use this method to gets the current document size.
       * This size only includes the data already written
       * to the output stream, it does not include templates or fonts.
       * It is useful if used with freeReader()
       * when concatenating many documents and an idea of
       * the current size is needed.
       * @return the approximate size without fonts or templates
 */
     val currentDocumentSize:Long
get() =body.offset() + (body.size() * 20).toLong() + 0x48

protected var currentPdfReaderInstance:PdfReaderInstance? = null

protected open fun getNewObjectNumber(reader:PdfReader, number:Int, generation:Int):Int {
if (currentPdfReaderInstance == null || currentPdfReaderInstance!!.reader !== reader)
{
currentPdfReaderInstance = getPdfReaderInstance(reader)
}
return currentPdfReaderInstance!!.getNewObjectNumber(number, generation)
}

internal open fun getReaderFile(reader:PdfReader):RandomAccessFileOrArray {
return currentPdfReaderInstance!!.readerFile
}

//  [F6] spot colors

    /** The colors of this document  */
    protected var documentColors = HashMap<ICachedColorSpace, ColorDetails>()

/** The color number counter for the colors in the document.  */
    protected var colorNumber = 1

internal val colorspaceName:PdfName
get() =PdfName("CS" + colorNumber++)

/**
       * Adds a SpotColor to the document but not to the page resources.
       * @param spc the SpotColor to add
      * * 
 * @return an Object[] where position 0 is a PdfName
      * * and position 1 is an PdfIndirectReference
 */
    internal fun addSimple(spc:ICachedColorSpace):ColorDetails {
var ret:ColorDetails? = documentColors[spc]
if (ret == null)
{
ret = ColorDetails(colorspaceName, body.pdfIndirectReference, spc)
if (spc is IPdfSpecialColorSpace)
{
spc.getColorantDetails(this)
}
documentColors.put(spc, ret)
}
return ret
}

//  [F7] document patterns

    /** The patterns of this document  */
    protected var documentPatterns = HashMap<PdfPatternPainter, PdfName>()

/** The pattern number counter for the colors in the document.  */
    protected var patternNumber = 1

internal fun addSimplePattern(painter:PdfPatternPainter):PdfName {
var name:PdfName? = documentPatterns[painter]
try
{
if (name == null)
{
name = PdfName("P" + patternNumber)
++patternNumber
documentPatterns.put(painter, name)
}
}
catch (e:Exception) {
throw ExceptionConverter(e)
}

return name
}

//  [F8] shading patterns

    protected var documentShadingPatterns = HashSet<PdfShadingPattern>()

internal fun addSimpleShadingPattern(shading:PdfShadingPattern) {
if (!documentShadingPatterns.contains(shading))
{
shading.setName(patternNumber)
++patternNumber
documentShadingPatterns.add(shading)
addSimpleShading(shading.shading)
}
}

//  [F9] document shadings

    protected var documentShadings = HashSet<PdfShading>()

internal fun addSimpleShading(shading:PdfShading) {
if (!documentShadings.contains(shading))
{
documentShadings.add(shading)
shading.setName(documentShadings.size)
}
}

// [F10] extended graphics state (for instance for transparency)

    protected var documentExtGState = HashMap<PdfDictionary, Array<PdfObject>>()

internal fun addSimpleExtGState(gstate:PdfDictionary):Array<PdfObject> {
if (!documentExtGState.containsKey(gstate))
{
documentExtGState.put(gstate, arrayOf(PdfName("GS" + (documentExtGState.size + 1)), pdfIndirectReference))
}
return documentExtGState[gstate]
}

//  [F11] adding properties (OCG, marked content)

    protected var documentProperties = HashMap<Any, Array<PdfObject>>()
internal fun addSimpleProperty(prop:Any, refi:PdfIndirectReference):Array<PdfObject> {
if (!documentProperties.containsKey(prop))
{
if (prop is PdfOCG)
PdfWriter.checkPdfIsoConformance(this, PdfIsoKeys.PDFISOKEY_LAYER, prop)
documentProperties.put(prop, arrayOf(PdfName("Pr" + (documentProperties.size + 1)), refi))
}
return documentProperties[prop]
}

internal fun propertyExists(prop:Any):Boolean {
return documentProperties.containsKey(prop)
}

/**
       * Check if the document is marked for tagging.
       * @return true if the document is marked for tagging
 */
     var isTagged = false
protected set
protected var taggingMode = markInlineElementsOnly
protected var structureTreeRoot:PdfStructureTreeRoot? = null

@JvmOverloads  fun setTagged(taggingMode:Int = markInlineElementsOnly) {
if (open)
throw IllegalArgumentException(MessageLocalization.getComposedMessage("tagging.must.be.set.before.opening.the.document"))
isTagged = true
this.taggingMode = taggingMode
}

 fun needToBeMarkedInContent(element:IAccessibleElement):Boolean {
if (taggingMode and markInlineElementsOnly != 0)
{
if (element.isInline || PdfName.ARTIFACT == element.role)
{
return true
}
return false
}
return true
}

 fun checkElementRole(element:IAccessibleElement, parent:IAccessibleElement?) {
if (parent != null && (parent.role == null || PdfName.ARTIFACT == parent.role))
element.role = null
else if (taggingMode and markInlineElementsOnly != 0)
{
if (element.isInline && element.role == null && (parent == null || !parent.isInline))
throw IllegalArgumentException(MessageLocalization.getComposedMessage("inline.elements.with.role.null.are.not.allowed"))
}
}

/**
       * Fix structure of tagged document: remove unused objects, remove unused items from class map,
       * fix xref table due to removed objects.
      */
    @Throws(IOException::class)
protected open fun flushTaggedObjects() {}

@Throws(IOException::class, BadPdfFormatException::class)
protected open fun flushAcroFields() {}

/**
       * Gets the structure tree root. If the document is not marked for tagging it will return null.
       * @return the structure tree root
 */
     fun getStructureTreeRoot():PdfStructureTreeRoot {
if (isTagged && structureTreeRoot == null)
structureTreeRoot = PdfStructureTreeRoot(this)
return structureTreeRoot
}

//  [F13] Optional Content Groups
    /** A hashSet containing all the PdfLayer objects.  */
    protected var documentOCG = LinkedHashSet<PdfOCG>()
/** An array list used to define the order of an OCG tree.  */
    protected var documentOCGorder = ArrayList<PdfOCG>()
/** The OCProperties in a catalog dictionary.  */
    protected var OCProperties:PdfOCProperties? = null
/** The RBGroups array in an OCG dictionary  */
    protected var OCGRadioGroup = PdfArray()
/**
       * The locked array in an OCG dictionary
       * @since   2.1.2
 */
    protected var OCGLocked = PdfArray()

/**
       * Use this method to get the Optional Content Properties Dictionary.
       * Each call fills the dictionary with the current layer state.
       * It's advisable to only call this method right before close
       * and do any modifications at that time.
       * @return the Optional Content Properties Dictionary
 */
     val ocProperties:PdfOCProperties
get() {
fillOCProperties(true)
return OCProperties
}

/**
       * Use this method to set a collection of optional content groups
       * whose states are intended to follow a "radio button" paradigm.
       * That is, the state of at most one optional content group
       * in the array should be ON at a time: if one group is turned
       * ON, all others must be turned OFF.
       * @param group the radio group
 */
     fun addOCGRadioGroup(group:ArrayList<PdfLayer>) {
val ar = PdfArray()
for (k in group.indices)
{
val layer = group[k]
if (layer.title == null)
ar.add(layer.ref)
}
if (ar.size() == 0)
return 
OCGRadioGroup.add(ar)
}

/**
       * Use this method to lock an optional content group.
       * The state of a locked group cannot be changed through the user interface
       * of a viewer application. Producers can use this entry to prevent the visibility
       * of content that depends on these groups from being changed by users.
       * @param layer	the layer that needs to be added to the array of locked OCGs
      * * 
 * @since	2.1.2
 */
     fun lockLayer(layer:PdfLayer) {
OCGLocked.add(layer.ref)
}

private fun addASEvent(event:PdfName, category:PdfName) {
val arr = PdfArray()
for (element in documentOCG)
{
val layer = element as PdfLayer
val usage = layer.getAsDict(PdfName.USAGE)
if (usage != null && usage.get(category) != null)
arr.add(layer.ref)
}
if (arr.size() == 0)
return 
val d = OCProperties!!.getAsDict(PdfName.D)
var arras:PdfArray? = d.getAsArray(PdfName.AS)
if (arras == null)
{
arras = PdfArray()
d.put(PdfName.AS, arras)
}
val `as` = PdfDictionary()
`as`.put(PdfName.EVENT, event)
`as`.put(PdfName.CATEGORY, PdfArray(category))
`as`.put(PdfName.OCGS, arr)
arras.add(`as`)
}

/**
       * @param erase true to erase the [PdfName.OCGS] and [PdfName.D] from the OCProperties first.
      * * 
 * @since 2.1.2
 */
    protected fun fillOCProperties(erase:Boolean) {
if (OCProperties == null)
OCProperties = PdfOCProperties()
if (erase)
{
OCProperties!!.remove(PdfName.OCGS)
OCProperties!!.remove(PdfName.D)
}
if (OCProperties!!.get(PdfName.OCGS) == null)
{
val gr = PdfArray()
for (element in documentOCG)
{
val layer = element as PdfLayer
gr.add(layer.ref)
}
OCProperties!!.put(PdfName.OCGS, gr)
}
if (OCProperties!!.get(PdfName.D) != null)
return 
val docOrder = ArrayList(documentOCGorder)
val it = docOrder.iterator()
while (it.hasNext())
{
val layer = it.next() as PdfLayer
if (layer.parent != null)
it.remove()
}
val order = PdfArray()
for (element in docOrder)
{
val layer = element as PdfLayer
getOCGOrder(order, layer)
}
val d = PdfDictionary()
OCProperties!!.put(PdfName.D, d)
d.put(PdfName.ORDER, order)
if (docOrder.size > 0 && docOrder[0] is PdfLayer)
{
val l = docOrder[0] as PdfLayer
val name = l.getAsString(PdfName.NAME)
if (name != null)
{
d.put(PdfName.NAME, name)
}
}
val gr = PdfArray()
for (element in documentOCG)
{
val layer = element as PdfLayer
if (!layer.isOn)
gr.add(layer.ref)
}
if (gr.size() > 0)
d.put(PdfName.OFF, gr)
if (OCGRadioGroup.size() > 0)
d.put(PdfName.RBGROUPS, OCGRadioGroup)
if (OCGLocked.size() > 0)
d.put(PdfName.LOCKED, OCGLocked)
addASEvent(PdfName.VIEW, PdfName.ZOOM)
addASEvent(PdfName.VIEW, PdfName.VIEW)
addASEvent(PdfName.PRINT, PdfName.PRINT)
addASEvent(PdfName.EXPORT, PdfName.EXPORT)
d.put(PdfName.LISTMODE, PdfName.VISIBLEPAGES)
}

internal open fun registerLayer(layer:PdfOCG) {
PdfWriter.checkPdfIsoConformance(this, PdfIsoKeys.PDFISOKEY_LAYER, layer)
if (layer is PdfLayer)
{
if (layer.title == null)
{
if (!documentOCG.contains(layer))
{
documentOCG.add(layer)
documentOCGorder.add(layer)
}
}
else
{
documentOCGorder.add(layer)
}
}
else
throw IllegalArgumentException(MessageLocalization.getComposedMessage("only.pdflayer.is.accepted"))
}

//  User methods to change aspects of the page

//  [U1] page size

    /**
       * Use this method to get the size of the media box.
       * @return a Rectangle
 */
     val pageSize:Rectangle
get() =pdfDocument.getPageSize()

/**
       * Use this method to set the crop box.
       * The crop box should not be rotated even if the page is rotated.
       * This change only takes effect in the next page.
       * @param crop the crop box
 */
     fun setCropBoxSize(crop:Rectangle) {
pdfDocument.setCropBoxSize(crop)
}

/**
       * Use this method to set the page box sizes.
       * Allowed names are: "crop", "trim", "art" and "bleed".
       * @param boxName the box size
      * * 
 * @param size the size
 */
     fun setBoxSize(boxName:String, size:Rectangle) {
pdfDocument.setBoxSize(boxName, size)
}

/**
       * Use this method to get the size of a trim, art, crop or bleed box,
       * or null if not defined.
       * @param boxName crop, trim, art or bleed
 */
     fun getBoxSize(boxName:String):Rectangle {
return pdfDocument.getBoxSize(boxName)
}

/**
       * Returns the intersection between the crop, trim art or bleed box and the parameter intersectingRectangle.
       * This method returns null when
       * - there is no intersection
       * - any of the above boxes are not defined
       * - the parameter intersectingRectangle is null
     
       * @param boxName crop, trim, art, bleed
      * * 
 * @param intersectingRectangle the rectangle that intersects the rectangle associated to the boxName
      * * 
 * @return the intersection of the two rectangles
 */
     fun getBoxSize(boxName:String, intersectingRectangle:Rectangle?):Rectangle? {
val pdfRectangle = pdfDocument.getBoxSize(boxName)

if (pdfRectangle == null || intersectingRectangle == null)
{ // no intersection
return null
}

val boxRect = com.itextpdf.awt.geom.Rectangle(pdfRectangle)
val intRect = com.itextpdf.awt.geom.Rectangle(intersectingRectangle)
val outRect = boxRect.intersection(intRect)

if (outRect.isEmpty)
{ // no intersection
return null
}

val output = Rectangle(outRect.x as Float, outRect.y as Float, (outRect.x + outRect.width).toFloat(), (outRect.y + outRect.height).toFloat())
output.normalize()
return output
}

/**
       * Checks if a newPage() will actually generate a new page.
       * @return true if a new page will be generated, false otherwise
      * * 
 * @since 5.0.0
 */
    //  [U2] take care of empty pages

    /**
       * Use this method to make sure a page is added,
       * even if it's empty. If you use setPageEmpty(false),
       * invoking newPage() after a blank page will add a newPage.
       * setPageEmpty(true) won't have any effect.
       * @param pageEmpty the state
 */
     var isPageEmpty:Boolean
get() =pdfDocument.isPageEmpty
set(pageEmpty) {
if (pageEmpty)
return 
pdfDocument.isPageEmpty = pageEmpty
}

/** @see com.itextpdf.text.pdf.interfaces.PdfPageActions.setPageAction
 */
    @Throws(DocumentException::class)
open fun setPageAction(actionType:PdfName, action:PdfAction) {
if (actionType != PAGE_OPEN && actionType != PAGE_CLOSE)
throw DocumentException(MessageLocalization.getComposedMessage("invalid.page.additional.action.type.1", actionType.toString()))
pdfDocument.setPageAction(actionType, action)
}

/** @see com.itextpdf.text.pdf.interfaces.PdfPageActions.setDuration
 */
    override fun setDuration(seconds:Int) {
pdfDocument.setDuration(seconds)
}

/** @see com.itextpdf.text.pdf.interfaces.PdfPageActions.setTransition
 */
    open fun setTransition(transition:PdfTransition) {
pdfDocument.setTransition(transition)
}

//  [U4] Thumbnail image

    /**
       * Use this method to set the thumbnail image for the current page.
       * @param image the image
      * * 
 * @throws PdfException on error
      * * 
 * @throws DocumentException or error
 */
    @Throws(PdfException::class, DocumentException::class)
open fun setThumbnail(image:Image) {
pdfDocument.setThumbnail(image)
}

//  [U5] Transparency groups

    /**
       * A group attributes dictionary specifying the attributes
       * of the page's page group for use in the transparent
       * imaging model
      */
    /**
       * Use this method to get the group dictionary.
       * @return Value of property group.
 */
    /**
       * Use this method to set the group dictionary.
       * @param group New value of property group.
 */
     var group:PdfDictionary? = null

/**
       * The ratio between the extra word spacing and the extra character spacing.
       * Extra word spacing will grow ratio times more than extra character spacing.
      */
    /**
       * Use this method to gets the space/character extra spacing ratio
       * for fully justified text.
       * @return the space/character extra spacing ratio
 */
    /**
       * Use this method to set the ratio between the extra word spacing and
       * the extra character spacing when the text is fully justified.
       * Extra word spacing will grow spaceCharRatio times more
       * than extra character spacing. If the ratio is PdfWriter.NO_SPACE_CHAR_RATIO
       * then the extra character spacing will be zero.
       * @param spaceCharRatio the ratio between the extra word spacing and the extra character spacing
 */
     var spaceCharRatio = SPACE_CHAR_RATIO_DEFAULT
set(spaceCharRatio) =if (spaceCharRatio < 0.001f)
this.spaceCharRatio = 0.001f
else
this.spaceCharRatio = spaceCharRatio

/**
       * Use this method to set the run direction.
       * @return the run direction
 */
    /**
       * Use this method to set the run direction.
       * This is only used as a placeholder as it does not affect anything.
       * @param runDirection the run direction
 */
    override var runDirection = RUN_DIRECTION_NO_BIDI
set(runDirection) {
if (runDirection < RUN_DIRECTION_NO_BIDI || runDirection > RUN_DIRECTION_RTL)
throw RuntimeException(MessageLocalization.getComposedMessage("invalid.run.direction.1", runDirection))
this.runDirection = runDirection
}

//  [U8] user units
    /**
       * Use this method to set the user unit.
       * A UserUnit is a value that defines the default user space unit.
       * The minimum UserUnit is 1 (1 unit = 1/72 inch).
       * The maximum UserUnit is 75,000.
       * Note that this userunit only works starting with PDF1.6!
       * @param userunit The userunit to set.
      * * 
 * @throws DocumentException on error
 */
     @Throws(DocumentException::class)
 fun setUserunit(userunit:Float) {
if (userunit < 1f || userunit > 75000f) throw DocumentException(MessageLocalization.getComposedMessage("userunit.should.be.a.value.between.1.and.75000"))
addPageDictEntry(PdfName.USERUNIT, PdfNumber(userunit))
setAtLeastPdfVersion(VERSION_1_6)
}

// Miscellaneous topics

//  [M1] Color settings

    /**
       * Use this method to get the default colorspaces.
       * @return the default colorspaces
 */
     var defaultColorspace = PdfDictionary()
protected set

/**
       * Use this method to sets the default colorspace that will be applied
       * to all the document. The colorspace is only applied if another colorspace
       * with the same name is not present in the content.
       * 
 * 
       * The colorspace is applied immediately when creating templates and
       * at the page end for the main document content.
       * @param key the name of the colorspace. It can be PdfName.DEFAULTGRAY, PdfName.DEFAULTRGB
      * * or PdfName.DEFAULTCMYK
      * * 
 * @param cs the colorspace. A null or PdfNull removes any colorspace with the same name
 */
     fun setDefaultColorspace(key:PdfName, cs:PdfObject?) {
if (cs == null || cs.isNull)
defaultColorspace.remove(key)
defaultColorspace.put(key, cs)
}

//  [M2] spot patterns

    protected var documentSpotPatterns = HashMap<ColorDetails, ColorDetails>()
protected var patternColorspaceRGB:ColorDetails? = null
protected var patternColorspaceGRAY:ColorDetails? = null
protected var patternColorspaceCMYK:ColorDetails? = null

internal fun addSimplePatternColorspace(color:BaseColor):ColorDetails {
val type = ExtendedColor.getType(color)
if (type == ExtendedColor.TYPE_PATTERN || type == ExtendedColor.TYPE_SHADING)
throw RuntimeException(MessageLocalization.getComposedMessage("an.uncolored.tile.pattern.can.not.have.another.pattern.or.shading.as.color"))
try
{
when (type) {
ExtendedColor.TYPE_RGB -> {
if (patternColorspaceRGB == null)
{
patternColorspaceRGB = ColorDetails(colorspaceName, body.pdfIndirectReference, null)
val array = PdfArray(PdfName.PATTERN)
array.add(PdfName.DEVICERGB)
addToBody(array, patternColorspaceRGB!!.indirectReference)
}
return patternColorspaceRGB
}
ExtendedColor.TYPE_CMYK -> {
if (patternColorspaceCMYK == null)
{
patternColorspaceCMYK = ColorDetails(colorspaceName, body.pdfIndirectReference, null)
val array = PdfArray(PdfName.PATTERN)
array.add(PdfName.DEVICECMYK)
addToBody(array, patternColorspaceCMYK!!.indirectReference)
}
return patternColorspaceCMYK
}
ExtendedColor.TYPE_GRAY -> {
if (patternColorspaceGRAY == null)
{
patternColorspaceGRAY = ColorDetails(colorspaceName, body.pdfIndirectReference, null)
val array = PdfArray(PdfName.PATTERN)
array.add(PdfName.DEVICEGRAY)
addToBody(array, patternColorspaceGRAY!!.indirectReference)
}
return patternColorspaceGRAY
}
ExtendedColor.TYPE_SEPARATION -> {
val details = addSimple((color as SpotColor).pdfSpotColor)
var patternDetails:ColorDetails? = documentSpotPatterns[details]
if (patternDetails == null)
{
patternDetails = ColorDetails(colorspaceName, body.pdfIndirectReference, null)
val array = PdfArray(PdfName.PATTERN)
array.add(details.indirectReference)
addToBody(array, patternDetails.indirectReference)
documentSpotPatterns.put(details, patternDetails)
}
return patternDetails
}
else -> throw RuntimeException(MessageLocalization.getComposedMessage("invalid.color.type"))
}
}
catch (e:Exception) {
throw RuntimeException(e.message)
}

}

//  [M3] Images

    /**
       * Use this method to get the strictImageSequence status.
       * @return value of property strictImageSequence
 */
    /**
       * Use this method to set the image sequence, so that it follows
       * the text in strict order (or not).
       * @param strictImageSequence new value of property strictImageSequence
 */
     var isStrictImageSequence:Boolean
get() =pdfDocument.isStrictImageSequence
set(strictImageSequence) {
pdfDocument.isStrictImageSequence = strictImageSequence
}

/**
       * Use this method to clear text wrapping around images (if applicable).
       * @throws DocumentException
 */
    @Throws(DocumentException::class)
 fun clearTextWrap() {
pdfDocument.clearTextWrap()
}

/** Dictionary, containing all the images of the PDF document  */
    protected var imageDictionary = PdfDictionary()

/** This is the list with all the images in the document.  */
    private val images = HashMap<Long, PdfName>()

/**
       * Adds an image to the document but not to the page resources.
       * It is used with templates and Document.add(Image).
       * Use this method only if you know what you're doing!
       * @param image the Image to add
      * * 
 * @param fixedRef the reference to used. It may be null,
      * * a PdfIndirectReference or a PRIndirectReference.
      * * 
 * @return the name of the image added
      * * 
 * @throws PdfException on error
      * * 
 * @throws DocumentException on error
 */
    @Throws(PdfException::class, DocumentException::class)
@JvmOverloads  fun addDirectImageSimple(image:Image, fixedRef:PdfIndirectReference? = null):PdfName {
val name:PdfName
// if the images is already added, just retrieve the name
        if (images.containsKey(image.mySerialId))
{
name = images[image.mySerialId]
}
else
{
if (image.isImgTemplate)
{
name = PdfName("img" + images.size)
if (image is ImgWMF)
{
try
{
image.readWMF(PdfTemplate.createTemplate(this, 0f, 0f))
}
catch (e:Exception) {
throw DocumentException(e)
}

}
}
else
{
val dref = image.directReference
if (dref != null)
{
val rname = PdfName("img" + images.size)
images.put(image.mySerialId, rname)
imageDictionary.put(rname, dref)
return rname
}
val maskImage = image.imageMask
var maskRef:PdfIndirectReference? = null
if (maskImage != null)
{
val mname = images[maskImage.mySerialId]
maskRef = getImageReference(mname)
}
val i = PdfImage(image, "img" + images.size, maskRef)
if (image is ImgJBIG2)
{
val globals = image.globalBytes
if (globals != null)
{
val decodeparms = PdfDictionary()
decodeparms.put(PdfName.JBIG2GLOBALS, getReferenceJBIG2Globals(globals))
i.put(PdfName.DECODEPARMS, decodeparms)
}
}
if (image.hasICCProfile())
{
val icc = PdfICCBased(image.iccProfile, image.compressionLevel)
val iccRef = add(icc)
val iccArray = PdfArray()
iccArray.add(PdfName.ICCBASED)
iccArray.add(iccRef)
val colorspace = i.getAsArray(PdfName.COLORSPACE)
if (colorspace != null)
{
if (colorspace.size() > 1 && PdfName.INDEXED == colorspace.getPdfObject(0))
colorspace.set(1, iccArray)
else
i.put(PdfName.COLORSPACE, iccArray)
}
else
i.put(PdfName.COLORSPACE, iccArray)
}
add(i, fixedRef)
name = i.name()
}
images.put(image.mySerialId, name)
}// if it's a new image, add it to the document
return name
}

/**
       * Writes a PdfImage to the outputstream.
     
       * @param pdfImage the image to be added
      * * 
 * @param fixedRef the IndirectReference, may be null then a new indirect reference is returned
      * * 
 * @return a PdfIndirectReference to the encapsulated image
      * * 
 * @throws PdfException when a document isn't open yet, or has been closed
 */

    @Throws(PdfException::class)
internal fun add(pdfImage:PdfImage, fixedRef:PdfIndirectReference?):PdfIndirectReference {
var fixedRef = fixedRef
if (!imageDictionary.contains(pdfImage.name()))
{
PdfWriter.checkPdfIsoConformance(this, PdfIsoKeys.PDFISOKEY_IMAGE, pdfImage)
if (fixedRef is PRIndirectReference)
{
fixedRef = PdfIndirectReference(0, getNewObjectNumber(fixedRef.reader, fixedRef.number, fixedRef.generation))
}
try
{
if (fixedRef == null)
fixedRef = addToBody(pdfImage).indirectReference
else
addToBody(pdfImage, fixedRef)
}
catch (ioe:IOException) {
throw ExceptionConverter(ioe)
}

imageDictionary.put(pdfImage.name(), fixedRef)
return fixedRef
}
return imageDictionary.get(pdfImage.name()) as PdfIndirectReference?
}

/**
       * return the PdfIndirectReference to the image with a given name.
     
       * @param name the name of the image
      * * 
 * @return a PdfIndirectReference
 */

    internal fun getImageReference(name:PdfName):PdfIndirectReference {
return imageDictionary.get(name) as PdfIndirectReference?
}

protected fun add(icc:PdfICCBased):PdfIndirectReference {
val `object`:PdfIndirectObject
try
{
`object` = addToBody(icc)
}
catch (ioe:IOException) {
throw ExceptionConverter(ioe)
}

return `object`.indirectReference
}

/**
       * A HashSet with Stream objects containing JBIG2 Globals
       * @since 2.1.5
 */
    protected var JBIG2Globals = HashMap<PdfStream, PdfIndirectReference>()
/**
       * Gets an indirect reference to a JBIG2 Globals stream.
       * Adds the stream if it hasn't already been added to the writer.
	   * @param	content a byte array that may already been added to the writer inside a stream object.
      * * 
 * @return the PdfIndirectReference of the stream
      * * 
 * @since  2.1.5
 */
    protected fun getReferenceJBIG2Globals(content:ByteArray?):PdfIndirectReference? {
if (content == null) return null
for (stream in JBIG2Globals.keys)
{
if (Arrays.equals(content, stream.bytes))
{
return JBIG2Globals[stream]
}
}
val stream = PdfStream(content)
val ref:PdfIndirectObject
try
{
ref = addToBody(stream)
}
catch (e:IOException) {
return null
}

JBIG2Globals.put(stream, ref.indirectReference)
return ref.indirectReference
}

//  [F12] tagged PDF
    /**
       * A flag indicating the presence of structure elements that contain user properties attributes.
      */
    /**
       * Gets the flag indicating the presence of structure elements that contain user properties attributes.
       * @return the user properties flag
 */
    /**
       * Sets the flag indicating the presence of structure elements that contain user properties attributes.
       * @param userProperties the user properties flag
 */
     var isUserProperties:Boolean = false

/**
       * Holds value of property RGBTranparency.
      */
    /**
       * Gets the transparency blending colorspace.
       * @return `true` if the transparency blending colorspace is RGB, `false`
      * * if it is the default blending colorspace
      * * 
 * @since 2.1.0
 */
    /**
       * Sets the transparency blending colorspace to RGB. The default blending colorspace is
       * CMYK and will result in faded colors in the screen and in printing. Calling this method
       * will return the RGB colors to what is expected. The RGB blending will be applied to all subsequent pages
       * until other value is set.
       * Note that this is a generic solution that may not work in all cases.
       * @param rgbTransparencyBlending `true` to set the transparency blending colorspace to RGB, `false`
      * * to use the default blending colorspace
      * * 
 * @since 2.1.0
 */
     var isRgbTransparencyBlending:Boolean = false

protected var ttfUnicodeWriter:TtfUnicodeWriter? = null

protected fun getTtfUnicodeWriter():TtfUnicodeWriter {
if (ttfUnicodeWriter == null)
ttfUnicodeWriter = TtfUnicodeWriter(this)
return ttfUnicodeWriter
}

@Throws(IOException::class)
protected fun createXmpWriter(baos:ByteArrayOutputStream?, info:PdfDictionary):XmpWriter {
return XmpWriter(baos, info)
}

@Throws(IOException::class)
protected fun createXmpWriter(baos:ByteArrayOutputStream, info:HashMap<String, String>):XmpWriter {
return XmpWriter(baos, info)
}

/**
       * A wrapper around PdfAnnotation constructor.
       * It is recommended to use this wrapper instead of direct constructor as this is a convenient way to override PdfAnnotation construction when needed.
     
       * @param rect
      * * 
 * @param subtype
      * * 
 * @return
 */
     fun createAnnotation(rect:Rectangle, subtype:PdfName?):PdfAnnotation {
val a = PdfAnnotation(this, rect)
if (subtype != null)
a.put(PdfName.SUBTYPE, subtype)
return a
}

/**
       * A wrapper around PdfAnnotation constructor.
       * It is recommended to use this wrapper instead of direct constructor as this is a convenient way to override PdfAnnotation construction when needed.
     
       * @param llx
      * * 
 * @param lly
      * * 
 * @param urx
      * * 
 * @param ury
      * * 
 * @param title
      * * 
 * @param content
      * * 
 * @param subtype
      * * 
 * @return
 */
     fun createAnnotation(llx:Float, lly:Float, urx:Float, ury:Float, title:PdfString, content:PdfString, subtype:PdfName?):PdfAnnotation {
val a = PdfAnnotation(this, llx, lly, urx, ury, title, content)
if (subtype != null)
a.put(PdfName.SUBTYPE, subtype)
return a
}

/**
       * A wrapper around PdfAnnotation constructor.
       * It is recommended to use this wrapper instead of direct constructor as this is a convenient way to override PdfAnnotation construction when needed.
     
       * @param llx
      * * 
 * @param lly
      * * 
 * @param urx
      * * 
 * @param ury
      * * 
 * @param action
      * * 
 * @param subtype
      * * 
 * @return
 */
     fun createAnnotation(llx:Float, lly:Float, urx:Float, ury:Float, action:PdfAction, subtype:PdfName?):PdfAnnotation {
val a = PdfAnnotation(this, llx, lly, urx, ury, action)
if (subtype != null)
a.put(PdfName.SUBTYPE, subtype)
return a
}

 fun checkPdfIsoConformance(key:Int, obj1:Any) {
pdfIsoConformance.checkPdfIsoConformance(key, obj1)
}

private fun completeInfoDictionary(info:PdfDictionary) {
if (isPdfX)
{
if (info.get(PdfName.GTS_PDFXVERSION) == null)
{
if ((pdfIsoConformance as PdfXConformanceImp).isPdfX1A2001)
{
info.put(PdfName.GTS_PDFXVERSION, PdfString("PDF/X-1:2001"))
info.put(PdfName("GTS_PDFXConformance"), PdfString("PDF/X-1a:2001"))
}
else if ((pdfIsoConformance as PdfXConformanceImp).isPdfX32002)
info.put(PdfName.GTS_PDFXVERSION, PdfString("PDF/X-3:2002"))
}
if (info.get(PdfName.TITLE) == null)
{
info.put(PdfName.TITLE, PdfString("Pdf document"))
}
if (info.get(PdfName.CREATOR) == null)
{
info.put(PdfName.CREATOR, PdfString("Unknown"))
}
if (info.get(PdfName.TRAPPED) == null)
{
info.put(PdfName.TRAPPED, PdfName("False"))
}
}
}

private fun completeExtraCatalog(extraCatalog:PdfDictionary) {
if (isPdfX)
{
if (extraCatalog.get(PdfName.OUTPUTINTENTS) == null)
{
val out = PdfDictionary(PdfName.OUTPUTINTENT)
out.put(PdfName.OUTPUTCONDITION, PdfString("SWOP CGATS TR 001-1995"))
out.put(PdfName.OUTPUTCONDITIONIDENTIFIER, PdfString("CGATS TR 001"))
out.put(PdfName.REGISTRYNAME, PdfString("http://www.color.org"))
out.put(PdfName.INFO, PdfString(""))
out.put(PdfName.S, PdfName.GTS_PDFX)
extraCatalog.put(PdfName.OUTPUTINTENTS, PdfArray(out))
}
}
}


/**
       * Gets the list of the standard structure element names (roles).
       * @return
 */
     val standardStructElems:List<PdfName>
get() {
if (pdfVersion.version < VERSION_1_7)
{
return standardStructElems_1_4
}
else
{
return standardStructElems_1_7
}
}

 fun useExternalCacheForTagStructure(fileCache:TempFileCache) {
pdfDocument.useExternalCache(fileCache)
}

companion object {

/**
	   * The highest generation number possible.
	   * @since	iText 2.1.6
 */
	 val GENERATION_MAX = 65535

//	ESSENTIALS
    protected var COUNTER = CounterFactory.getCounter(PdfWriter::class.java)

/**
       * Use this method to get an instance of the PdfWriter.
     
       * @param	document	The Document that has to be written
      * * 
 * @param	os	The OutputStream the writer has to write to.
      * * 
 * @return	a new PdfWriter
      * *
      * * 
 * @throws	DocumentException on error
 */

    @Throws(DocumentException::class)
 fun getInstance(document:Document, os:OutputStream):PdfWriter {
val pdf = PdfDocument()
document.addDocListener(pdf)
val writer = PdfWriter(pdf, os)
pdf.addWriter(writer)
return writer
}

/**
       * Use this method to get an instance of the PdfWriter.
     
       * @return a new PdfWriter
      * * 
 * @param document The Document that has to be written
      * * 
 * @param os The OutputStream the writer has to write to.
      * * 
 * @param listener A DocListener to pass to the PdfDocument.
      * * 
 * @throws DocumentException on error
 */

    @Throws(DocumentException::class)
 fun getInstance(document:Document, os:OutputStream, listener:DocListener):PdfWriter {
val pdf = PdfDocument()
pdf.addDocListener(listener)
document.addDocListener(pdf)
val writer = PdfWriter(pdf, os)
pdf.addWriter(writer)
return writer
}

//	[C2] PdfVersion interface
     /** possible PDF version (header)  */
      val VERSION_1_2 = '2'
/** possible PDF version (header)  */
      val VERSION_1_3 = '3'
/** possible PDF version (header)  */
      val VERSION_1_4 = '4'
/** possible PDF version (header)  */
      val VERSION_1_5 = '5'
/** possible PDF version (header)  */
      val VERSION_1_6 = '6'
/** possible PDF version (header)  */
      val VERSION_1_7 = '7'

/** possible PDF version (catalog)  */
      val PDF_VERSION_1_2 = PdfName("1.2")
/** possible PDF version (catalog)  */
      val PDF_VERSION_1_3 = PdfName("1.3")
/** possible PDF version (catalog)  */
      val PDF_VERSION_1_4 = PdfName("1.4")
/** possible PDF version (catalog)  */
      val PDF_VERSION_1_5 = PdfName("1.5")
/** possible PDF version (catalog)  */
      val PDF_VERSION_1_6 = PdfName("1.6")
/** possible PDF version (catalog)  */
      val PDF_VERSION_1_7 = PdfName("1.7")

//  [C3] PdfViewerPreferences interface

	// page layout (section 13.1.1 of "iText in Action")

	/** A viewer preference  */
	 val PageLayoutSinglePage = 1
/** A viewer preference  */
	 val PageLayoutOneColumn = 2
/** A viewer preference  */
	 val PageLayoutTwoColumnLeft = 4
/** A viewer preference  */
	 val PageLayoutTwoColumnRight = 8
/** A viewer preference  */
	 val PageLayoutTwoPageLeft = 16
/** A viewer preference  */
	 val PageLayoutTwoPageRight = 32

// page mode (section 13.1.2 of "iText in Action")

    /** A viewer preference  */
     val PageModeUseNone = 64
/** A viewer preference  */
     val PageModeUseOutlines = 128
/** A viewer preference  */
     val PageModeUseThumbs = 256
/** A viewer preference  */
     val PageModeFullScreen = 512
/** A viewer preference  */
     val PageModeUseOC = 1024
/** A viewer preference  */
     val PageModeUseAttachments = 2048

// values for setting viewer preferences in iText versions older than 2.x

    /** A viewer preference  */
     val HideToolbar = 1 shl 12
/** A viewer preference  */
     val HideMenubar = 1 shl 13
/** A viewer preference  */
     val HideWindowUI = 1 shl 14
/** A viewer preference  */
     val FitWindow = 1 shl 15
/** A viewer preference  */
     val CenterWindow = 1 shl 16
/** A viewer preference  */
     val DisplayDocTitle = 1 shl 17

/** A viewer preference  */
     val NonFullScreenPageModeUseNone = 1 shl 18
/** A viewer preference  */
     val NonFullScreenPageModeUseOutlines = 1 shl 19
/** A viewer preference  */
     val NonFullScreenPageModeUseThumbs = 1 shl 20
/** A viewer preference  */
     val NonFullScreenPageModeUseOC = 1 shl 21

/** A viewer preference  */
     val DirectionL2R = 1 shl 22
/** A viewer preference  */
     val DirectionR2L = 1 shl 23

/** A viewer preference  */
     val PrintScalingNone = 1 shl 24

// [C6] Actions (open and additional)

     /** action value  */
      val DOCUMENT_CLOSE = PdfName.WC
/** action value  */
      val WILL_SAVE = PdfName.WS
/** action value  */
      val DID_SAVE = PdfName.DS
/** action value  */
      val WILL_PRINT = PdfName.WP
/** action value  */
      val DID_PRINT = PdfName.DP

//  [C8] AcroForm

    /** signature value  */
     val SIGNATURE_EXISTS = 1
/** signature value  */
     val SIGNATURE_APPEND_ONLY = 2

//  [C10] PDFX Conformance
    /** A PDF/X level.  */
     val PDFXNONE = 0
/** A PDF/X level.  */
     val PDFX1A2001 = 1
/** A PDF/X level.  */
     val PDFX32002 = 2

protected fun getNameString(dic:PdfDictionary, key:PdfName):String? {
val obj = PdfReader.getPdfObject(dic.get(key))
if (obj == null || !obj.isString)
return null
return (obj as PdfString).toUnicodeString()
}

// PDF Objects that have an impact on the PDF body

//  [F1] PdfEncryptionSettings interface

    // types of encryption

    /** Type of encryption  */
     val STANDARD_ENCRYPTION_40 = 0
/** Type of encryption  */
     val STANDARD_ENCRYPTION_128 = 1
/** Type of encryption  */
     val ENCRYPTION_AES_128 = 2
/** Type of encryption  */
     val ENCRYPTION_AES_256 = 3
/** Mask to separate the encryption type from the encryption mode.  */
    internal val ENCRYPTION_MASK = 7
/** Add this to the mode to keep the metadata in clear text  */
     val DO_NOT_ENCRYPT_METADATA = 8
/**
       * Add this to the mode to keep encrypt only the embedded files.
       * @since 2.1.3
 */
     val EMBEDDED_FILES_ONLY = 24

// permissions

    /** The operation permitted when the document is opened with the user password
     
       * @since 2.0.7
 */
     val ALLOW_PRINTING = 4 + 2048

/** The operation permitted when the document is opened with the user password
     
       * @since 2.0.7
 */
     val ALLOW_MODIFY_CONTENTS = 8

/** The operation permitted when the document is opened with the user password
     
       * @since 2.0.7
 */
     val ALLOW_COPY = 16

/** The operation permitted when the document is opened with the user password
     
       * @since 2.0.7
 */
     val ALLOW_MODIFY_ANNOTATIONS = 32

/** The operation permitted when the document is opened with the user password
     
       * @since 2.0.7
 */
     val ALLOW_FILL_IN = 256

/** The operation permitted when the document is opened with the user password
     
       * @since 2.0.7
 */
     val ALLOW_SCREENREADERS = 512

/** The operation permitted when the document is opened with the user password
     
       * @since 2.0.7
 */
     val ALLOW_ASSEMBLY = 1024

/** The operation permitted when the document is opened with the user password
     
       * @since 2.0.7
 */
     val ALLOW_DEGRADED_PRINTING = 4


    @Deprecated("")
@Deprecated("As of iText 2.0.7, use {@link #ALLOW_PRINTING} instead. Scheduled for removal at or after 2.2.0 ")
 val AllowPrinting = ALLOW_PRINTING

    @Deprecated("")
@Deprecated("As of iText 2.0.7, use {@link #ALLOW_MODIFY_CONTENTS} instead. Scheduled for removal at or after 2.2.0 ")
 val AllowModifyContents = ALLOW_MODIFY_CONTENTS

    @Deprecated("")
@Deprecated("As of iText 2.0.7, use {@link #ALLOW_COPY} instead. Scheduled for removal at or after 2.2.0 ")
 val AllowCopy = ALLOW_COPY

    @Deprecated("")
@Deprecated("As of iText 2.0.7, use {@link #ALLOW_MODIFY_ANNOTATIONS} instead. Scheduled for removal at or after 2.2.0 ")
 val AllowModifyAnnotations = ALLOW_MODIFY_ANNOTATIONS

    @Deprecated("")
@Deprecated("As of iText 2.0.7, use {@link #ALLOW_FILL_IN} instead. Scheduled for removal at or after 2.2.0 ")
 val AllowFillIn = ALLOW_FILL_IN

    @Deprecated("")
@Deprecated("As of iText 2.0.7, use {@link #ALLOW_SCREENREADERS} instead. Scheduled for removal at or after 2.2.0 ")
 val AllowScreenReaders = ALLOW_SCREENREADERS

    @Deprecated("")
@Deprecated("As of iText 2.0.7, use {@link #ALLOW_ASSEMBLY} instead. Scheduled for removal at or after 2.2.0 ")
 val AllowAssembly = ALLOW_ASSEMBLY

    @Deprecated("")
@Deprecated("As of iText 2.0.7, use {@link #ALLOW_DEGRADED_PRINTING} instead. Scheduled for removal at or after 2.2.0 ")
 val AllowDegradedPrinting = ALLOW_DEGRADED_PRINTING

// Strength of the encryption (kept for historical reasons)
    
    @Deprecated("")
@Deprecated("As of iText 2.0.7, use {@link #STANDARD_ENCRYPTION_40} instead. Scheduled for removal at or after 2.2.0 ")
 val STRENGTH40BITS = false

    @Deprecated("")
@Deprecated("As of iText 2.0.7, use {@link #STANDARD_ENCRYPTION_128} instead. Scheduled for removal at or after 2.2.0 ")
 val STRENGTH128BITS = true

//  [F12] tagged PDF

     val markAll = 0x00
 val markInlineElementsOnly = 0x01

private fun getOCGOrder(order:PdfArray, layer:PdfLayer) {
if (!layer.isOnPanel)
return 
if (layer.title == null)
order.add(layer.ref)
val children = layer.children ?: return
val kids = PdfArray()
if (layer.title != null)
kids.add(PdfString(layer.title, PdfObject.TEXT_UNICODE))
for (k in children.indices)
{
getOCGOrder(kids, children[k])
}
if (kids.size() > 0)
order.add(kids)
}

//  [U3] page actions (open and close)

    /** action value  */
     val PAGE_OPEN = PdfName.O
/** action value  */
     val PAGE_CLOSE = PdfName.C

//  [U6] space char ratio

    /** The default space-char ratio.  */
     val SPACE_CHAR_RATIO_DEFAULT = 2.5f
/** Disable the inter-character spacing.  */
     val NO_SPACE_CHAR_RATIO = 10000000f

//  [U7] run direction (doesn't actually do anything)

    /** Use the default run direction.  */
     val RUN_DIRECTION_DEFAULT = 0
/** Do not use bidirectional reordering.  */
     val RUN_DIRECTION_NO_BIDI = 1
/** Use bidirectional reordering with left-to-right
       * preferential run direction.
      */
     val RUN_DIRECTION_LTR = 2
/** Use bidirectional reordering with right-to-left
       * preferential run direction.
      */
     val RUN_DIRECTION_RTL = 3

@Throws(IOException::class)
protected fun writeKeyInfo(os:OutputStream) {
val version = Version.getInstance()
var k:String? = version.key
if (k == null)
{
k = "iText"
}
os.write(DocWriter.getISOBytes(String.format("%%%s-%s\n", k, version.release)))

}

 fun checkPdfIsoConformance(writer:PdfWriter?, key:Int, obj1:Any) {
writer?.checkPdfIsoConformance(key, obj1)
}

private val standardStructElems_1_4 = Arrays.asList(PdfName.DOCUMENT, PdfName.PART, PdfName.ART, 
PdfName.SECT, PdfName.DIV, PdfName.BLOCKQUOTE, PdfName.CAPTION, PdfName.TOC, PdfName.TOCI, PdfName.INDEX, 
PdfName.NONSTRUCT, PdfName.PRIVATE, PdfName.P, PdfName.H, PdfName.H1, PdfName.H2, PdfName.H3, PdfName.H4, 
PdfName.H5, PdfName.H6, PdfName.L, PdfName.LBL, PdfName.LI, PdfName.LBODY, PdfName.TABLE, PdfName.TR, 
PdfName.TH, PdfName.TD, PdfName.SPAN, PdfName.QUOTE, PdfName.NOTE, PdfName.REFERENCE, PdfName.BIBENTRY, 
PdfName.CODE, PdfName.LINK, PdfName.FIGURE, PdfName.FORMULA, PdfName.FORM)

private val standardStructElems_1_7 = Arrays.asList(PdfName.DOCUMENT, PdfName.PART, PdfName.ART, 
PdfName.SECT, PdfName.DIV, PdfName.BLOCKQUOTE, PdfName.CAPTION, PdfName.TOC, PdfName.TOCI, PdfName.INDEX, 
PdfName.NONSTRUCT, PdfName.PRIVATE, PdfName.P, PdfName.H, PdfName.H1, PdfName.H2, PdfName.H3, PdfName.H4, 
PdfName.H5, PdfName.H6, PdfName.L, PdfName.LBL, PdfName.LI, PdfName.LBODY, PdfName.TABLE, PdfName.TR, 
PdfName.TH, PdfName.TD, PdfName.THEAD, PdfName.TBODY, PdfName.TFOOT, PdfName.SPAN, PdfName.QUOTE, PdfName.NOTE, 
PdfName.REFERENCE, PdfName.BIBENTRY, PdfName.CODE, PdfName.LINK, PdfName.ANNOT, PdfName.RUBY, PdfName.RB, PdfName.RT, 
PdfName.RP, PdfName.WARICHU, PdfName.WT, PdfName.WP, PdfName.FIGURE, PdfName.FORMULA, PdfName.FORM)
}

}/**
        * Use this method to adds a JavaScript action at the document level.
        * When the document opens, all this JavaScript runs.
        * @param code the JavaScript code
 *//**
        * Use this method to adds a JavaScript action at the document level.
        * When the document opens, all this JavaScript runs.
        * @param name	The name of the JS Action in the name tree
       * * 
 * @param code the JavaScript code
 *//**
       * Mark this document for tagging. It must be called before open.
      *//**
       * Use this method to adds an image to the document
       * but not to the page resources. It is used with
       * templates and Document.add(Image).
       * Use this method only if you know what you're doing!
       * @param image the Image to add
      * * 
 * @return the name of the image added
      * * 
 * @throws PdfException on error
      * * 
 * @throws DocumentException on error
 */
