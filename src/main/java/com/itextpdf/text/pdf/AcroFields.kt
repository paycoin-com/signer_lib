/*
 * $Id: 82c89dae7ee533860463b54d35da3788585c6d91 $
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
import com.itextpdf.text.io.RASInputStream
import com.itextpdf.text.io.RandomAccessSourceFactory
import com.itextpdf.text.io.WindowRandomAccessSource
import com.itextpdf.text.pdf.PRTokeniser.TokenType
import com.itextpdf.text.pdf.codec.Base64
import com.itextpdf.text.pdf.security.PdfPKCS7
import com.itextpdf.text.xml.XmlToTxt
import org.w3c.dom.Node

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
   * Query and change fields in existing documents either by method
   * calls or by FDF merging.
 
   * @author Paulo Soares
 */
 class AcroFields internal constructor(internal var reader:

PdfReader, internal var writer:PdfWriter?) {
internal var fields:MutableMap<String, Item>
private var topFirst:Int = 0
private var sigNames:HashMap<String, IntArray>? = null
private var append:Boolean = false
private val extensionFonts = HashMap<Int, BaseFont>()
/**
       * Gets the XFA form processor.
     
       * @return the XFA form processor
 */
     var xfa:XfaForm? = null
private set

private var lastWasString:Boolean = false

/** Holds value of property generateAppearances.  */
    /**
       * Gets the property generateAppearances.
     
       * @return the property generateAppearances
 */
    /**
       * Sets the option to generate appearances. Not generating appearances
       * will speed-up form filling but the results can be
       * unexpected in Acrobat. Don't use it unless your environment is well
       * controlled. The default is true.
     
       * @param generateAppearances the option to generate appearances
 */
     var isGenerateAppearances = true
set(generateAppearances) {
this.isGenerateAppearances = generateAppearances
val top = reader.getCatalog().getAsDict(PdfName.ACROFORM)
if (generateAppearances)
top.remove(PdfName.NEEDAPPEARANCES)
else
top.put(PdfName.NEEDAPPEARANCES, PdfBoolean.PDFTRUE)
}

private val localFonts = HashMap<String, BaseFont>()

private var extraMarginLeft:Float = 0.toFloat()
private var extraMarginTop:Float = 0.toFloat()
/**
       * Gets the list of substitution fonts. The list is composed of BaseFont and can be null. The fonts in this list will be used if the original
       * font doesn't contain the needed glyphs.
     
       * @return the list
 */
    /**
       * Sets a list of substitution fonts. The list is composed of BaseFont and can also be null. The fonts in this list will be used if the original
       * font doesn't contain the needed glyphs.
     
       * @param substitutionFonts the list
 */
     var substitutionFonts:ArrayList<BaseFont>? = null

init{
try
{
xfa = XfaForm(reader)
}
catch (e:Exception) {
throw ExceptionConverter(e)
}

if (writer is PdfStamperImp)
{
append = (writer as PdfStamperImp).isAppend
}
fill()
}

internal fun fill() {
fields = LinkedHashMap<String, Item>()
val top = PdfReader.getPdfObjectRelease(reader.getCatalog().get(PdfName.ACROFORM)) as PdfDictionary ?: return
val needappearances = top.getAsBoolean(PdfName.NEEDAPPEARANCES)
if (needappearances == null || !needappearances.booleanValue())
isGenerateAppearances = true
else
isGenerateAppearances = false
val arrfds = PdfReader.getPdfObjectRelease(top.get(PdfName.FIELDS)) as PdfArray
if (arrfds == null || arrfds.size() == 0)
return 
for (k in 1..reader.numberOfPages)
{
val page = reader.getPageNRelease(k)
val annots = PdfReader.getPdfObjectRelease(page.get(PdfName.ANNOTS), page) as PdfArray ?: continue
for (j in 0..annots.size() - 1)
{
var annot:PdfDictionary? = annots.getAsDict(j)
if (annot == null)
{
PdfReader.releaseLastXrefPartial(annots.getAsIndirectObject(j))
continue
}
if (PdfName.WIDGET != annot.getAsName(PdfName.SUBTYPE))
{
PdfReader.releaseLastXrefPartial(annots.getAsIndirectObject(j))
continue
}
val widget = annot
val dic = PdfDictionary()
dic.putAll(annot)
var name = ""
var value:PdfDictionary? = null
var lastV:PdfObject? = null
while (annot != null)
{
dic.mergeDifferent(annot)
val t = annot.getAsString(PdfName.T)
if (t != null)
name = t.toUnicodeString() + "." + name
if (lastV == null && annot.get(PdfName.V) != null)
lastV = PdfReader.getPdfObjectRelease(annot.get(PdfName.V))
if (value == null && t != null)
{
value = annot
if (annot.get(PdfName.V) == null && lastV != null)
value.put(PdfName.V, lastV) // TODO: seems to be bug (we are going up the hierarchy and setting parent's V entry to child's V value)
}
annot = annot.getAsDict(PdfName.PARENT)
}
if (name.length > 0)
name = name.substring(0, name.length - 1)
var item:Item? = fields[name]
if (item == null)
{
item = Item()
fields.put(name, item)
}
if (value == null)
item.addValue(widget)
else
item.addValue(value)
item.addWidget(widget)
item.addWidgetRef(annots.getAsIndirectObject(j)) // must be a reference
if (top != null)
dic.mergeDifferent(top)
item.addMerged(dic)
item.addPage(k)
item.addTabOrder(j)
}
}
// some tools produce invisible signatures without an entry in the page annotation array
        // look for a single level annotation
        val sigFlags = top.getAsNumber(PdfName.SIGFLAGS)
if (sigFlags == null || sigFlags.intValue() and 1 != 1)
return 
for (j in 0..arrfds.size() - 1)
{
val annot = arrfds.getAsDict(j)
if (annot == null)
{
PdfReader.releaseLastXrefPartial(arrfds.getAsIndirectObject(j))
continue
}
if (PdfName.WIDGET != annot.getAsName(PdfName.SUBTYPE))
{
PdfReader.releaseLastXrefPartial(arrfds.getAsIndirectObject(j))
continue
}
val kids = PdfReader.getPdfObjectRelease(annot.get(PdfName.KIDS)) as PdfArray
if (kids != null)
continue
val dic = PdfDictionary()
dic.putAll(annot)
val t = annot.getAsString(PdfName.T) ?: continue
val name = t.toUnicodeString()
if (fields.containsKey(name))
continue
val item = Item()
fields.put(name, item)
item.addValue(dic)
item.addWidget(dic)
item.addWidgetRef(arrfds.getAsIndirectObject(j)) // must be a reference
item.addMerged(dic)
item.addPage(-1)
item.addTabOrder(-1)
}
}

/**
       * Gets the list of appearance names. Use it to get the names allowed
       * with radio and checkbox fields. If the /Opt key exists the values will
       * also be included. The name 'Off' may also be valid
       * even if not returned in the list.
     
       * For Comboboxes it will return an array of display values. To extract the
       * export values of a Combobox, please refer to [AcroFields.getListOptionExport]
     
       * @param fieldName the fully qualified field name
      * * 
 * @return the list of names or null if the field does not exist
 */
     fun getAppearanceStates(fieldName:String):Array<String>? {
val fd = fields[fieldName] ?: return null
val names = LinkedHashSet<String>()
val vals = fd.getValue(0)
val stringOpt = vals.getAsString(PdfName.OPT)

// should not happen according to specs
        if (stringOpt != null)
{
names.add(stringOpt.toUnicodeString())
}
else
{
val arrayOpt = vals.getAsArray(PdfName.OPT)
if (arrayOpt != null)
{
for (k in 0..arrayOpt.size() - 1)
{
val pdfObject = arrayOpt.getDirectObject(k)
var valStr:PdfString? = null

when (pdfObject.type()) {
PdfObject.ARRAY -> {
val pdfArray = pdfObject as PdfArray
valStr = pdfArray.getAsString(1)
}
PdfObject.STRING -> valStr = pdfObject as PdfString
}

if (valStr != null)
names.add(valStr.toUnicodeString())
}
}
}
for (k in 0..fd.size() - 1)
{
var dic:PdfDictionary? = fd.getWidget(k)
dic = dic!!.getAsDict(PdfName.AP)
if (dic == null)
continue
dic = dic.getAsDict(PdfName.N)
if (dic == null)
continue
for (element in dic.keys)
{
val name = PdfName.decodeName((element as PdfName).toString())
names.add(name)
}
}
val out = arrayOfNulls<String>(names.size)
return names.toArray<String>(out)
}

private fun getListOption(fieldName:String, idx:Int):Array<String>? {
val fd = getFieldItem(fieldName) ?: return null
val ar = fd.getMerged(0).getAsArray(PdfName.OPT) ?: return null
val ret = arrayOfNulls<String>(ar.size())
for (k in 0..ar.size() - 1)
{
var obj = ar.getDirectObject(k)
try
{
if (obj.isArray)
{
obj = (obj as PdfArray).getDirectObject(idx)
}
if (obj.isString)
ret[k] = (obj as PdfString).toUnicodeString()
else
ret[k] = obj.toString()
}
catch (e:Exception) {
ret[k] = ""
}

}
return ret
}

/**
       * Gets the list of export option values from fields of type list or combo.
       * If the field doesn't exist or the field type is not list or combo it will return
       * null.
     
       * @param fieldName the field name
      * * 
 * @return the list of export option values from fields of type list or combo
 */
     fun getListOptionExport(fieldName:String):Array<String> {
return getListOption(fieldName, 0)
}

/**
       * Gets the list of display option values from fields of type list or combo.
       * If the field doesn't exist or the field type is not list or combo it will return
       * null.
     
       * @param fieldName the field name
      * * 
 * @return the list of export option values from fields of type list or combo
 */
     fun getListOptionDisplay(fieldName:String):Array<String> {
return getListOption(fieldName, 1)
}

/**
       * Sets the option list for fields of type list or combo. One of exportValues
       * or displayValues may be null but not both. This method will only
       * set the list but will not set the value or appearance. For that, calling setField()
       * is required.
       * 
 * 
       * An example:
       * 
 * 
       * 
       * PdfReader pdf = new PdfReader("input.pdf");
       * PdfStamper stp = new PdfStamper(pdf, new FileOutputStream("output.pdf"));
       * AcroFields af = stp.getAcroFields();
       * af.setListOption("ComboBox", new String[]{"a", "b", "c"}, new String[]{"first", "second", "third"});
       * af.setField("ComboBox", "b");
       * stp.close();
       * 
     
       * @param fieldName the field name
      * * 
 * @param exportValues the export values
      * * 
 * @param displayValues the display values
      * * 
 * @return true if the operation succeeded, false otherwise
 */
     fun setListOption(fieldName:String, exportValues:Array<String>?, displayValues:Array<String>?):Boolean {
if (exportValues == null && displayValues == null)
return false
if (exportValues != null && displayValues != null && exportValues.size != displayValues.size)
throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.export.and.the.display.array.must.have.the.same.size"))
val ftype = getFieldType(fieldName)
if (ftype != FIELD_TYPE_COMBO && ftype != FIELD_TYPE_LIST)
return false
val fd = fields[fieldName]
var sing:Array<String>? = null
if (exportValues == null && displayValues != null)
sing = displayValues
else if (exportValues != null && displayValues == null)
sing = exportValues
val opt = PdfArray()
if (sing != null)
{
for (k in sing.indices)
opt.add(PdfString(sing[k], PdfObject.TEXT_UNICODE))
}
else
{
for (k in exportValues!!.indices)
{
val a = PdfArray()
a.add(PdfString(exportValues[k], PdfObject.TEXT_UNICODE))
a.add(PdfString(displayValues!![k], PdfObject.TEXT_UNICODE))
opt.add(a)
}
}
fd.writeToAll(PdfName.OPT, opt, Item.WRITE_VALUE or Item.WRITE_MERGED)
return true
}

/**
       * Gets the field type. The type can be one of: FIELD_TYPE_PUSHBUTTON,
       * FIELD_TYPE_CHECKBOX, FIELD_TYPE_RADIOBUTTON,
       * FIELD_TYPE_TEXT, FIELD_TYPE_LIST,
       * FIELD_TYPE_COMBO or FIELD_TYPE_SIGNATURE.
       * 
 * 
       * If the field does not exist or is invalid it returns
       * FIELD_TYPE_NONE.
     
       * @param fieldName the field name
      * * 
 * @return the field type
 */
     fun getFieldType(fieldName:String):Int {
val fd = getFieldItem(fieldName) ?: return FIELD_TYPE_NONE
val merged = fd.getMerged(0)
val type = merged.getAsName(PdfName.FT) ?: return FIELD_TYPE_NONE
var ff = 0
val ffo = merged.getAsNumber(PdfName.FF)
if (ffo != null)
{
ff = ffo.intValue()
}
if (PdfName.BTN == type)
{
if (ff and PdfFormField.FF_PUSHBUTTON != 0)
return FIELD_TYPE_PUSHBUTTON
if (ff and PdfFormField.FF_RADIO != 0)
return FIELD_TYPE_RADIOBUTTON
else
return FIELD_TYPE_CHECKBOX
}
else if (PdfName.TX == type)
{
return FIELD_TYPE_TEXT
}
else if (PdfName.CH == type)
{
if (ff and PdfFormField.FF_COMBO != 0)
return FIELD_TYPE_COMBO
else
return FIELD_TYPE_LIST
}
else if (PdfName.SIG == type)
{
return FIELD_TYPE_SIGNATURE
}
return FIELD_TYPE_NONE
}

/**
       * Export the fields as a FDF.
     
       * @param writer the FDF writer
 */
     fun exportAsFdf(writer:FdfWriter) {
for ((name, item) in fields)
{
val v = item.getMerged(0).get(PdfName.V) ?: continue
val value = getField(name)
if (lastWasString)
writer.setFieldAsString(name, value)
else
writer.setFieldAsName(name, value)
}
}

/**
       * Renames a field. Only the last part of the name can be renamed. For example,
       * if the original field is "ab.cd.ef" only the "ef" part can be renamed.
     
       * @param oldName the old field name
      * * 
 * @param newName the new field name
      * * 
 * @return true if the renaming was successful, false
      * * otherwise
 */
     fun renameField(oldName:String, newName:String):Boolean {
var newName = newName
val idx1 = oldName.lastIndexOf('.') + 1
val idx2 = newName.lastIndexOf('.') + 1
if (idx1 != idx2)
return false
if (oldName.substring(0, idx1) != newName.substring(0, idx2))
return false
if (fields.containsKey(newName))
return false
val item = fields[oldName] ?: return false
newName = newName.substring(idx2)
val ss = PdfString(newName, PdfObject.TEXT_UNICODE)

item.writeToAll(PdfName.T, ss, Item.WRITE_VALUE or Item.WRITE_MERGED)
item.markUsed(this, Item.WRITE_VALUE)

fields.remove(oldName)
fields.put(newName, item)

return true
}

@Throws(IOException::class, DocumentException::class)
 fun decodeGenericDictionary(merged:PdfDictionary, tx:BaseField) {
var flags = 0
// the text size and color
        val da = merged.getAsString(PdfName.DA)
if (da != null)
{
var fontfallback = false
val dab = splitDAelements(da.toUnicodeString())
if (dab[DA_SIZE] != null)
tx.fontSize = (dab[DA_SIZE] as Float).toFloat()
if (dab[DA_COLOR] != null)
tx.textColor = dab[DA_COLOR] as BaseColor
if (dab[DA_FONT] != null)
{
val dr = merged.getAsDict(PdfName.DR)
if (dr != null)
{
val font = dr.getAsDict(PdfName.FONT)
if (font != null)
{
val po = font.get(PdfName(dab[DA_FONT] as String))
if (po != null && po.type() == PdfObject.INDIRECT)
{
val por = po as PRIndirectReference?
val bp = DocumentFont(po as PRIndirectReference?, dr.getAsDict(PdfName.ENCODING))
tx.font = bp
val porkey = Integer.valueOf(por.getNumber())
var porf:BaseFont? = extensionFonts[porkey]
if (porf == null)
{
if (!extensionFonts.containsKey(porkey))
{
val fo = PdfReader.getPdfObject(po) as PdfDictionary
val fd = fo.getAsDict(PdfName.FONTDESCRIPTOR)
if (fd != null)
{
var prs:PRStream? = PdfReader.getPdfObject(fd.get(PdfName.FONTFILE2)) as PRStream
if (prs == null)
prs = PdfReader.getPdfObject(fd.get(PdfName.FONTFILE3)) as PRStream
if (prs == null)
{
extensionFonts.put(porkey, null)
}
else
{
try
{
porf = BaseFont.createFont("font.ttf", BaseFont.IDENTITY_H, true, false, PdfReader.getStreamBytes(prs), null)
}
catch (e:Exception) {}

extensionFonts.put(porkey, porf)
}
}
}
}
if (tx is TextField)
tx.extensionFont = porf
}
else
{
fontfallback = true
}

}
else
{
fontfallback = true
}
}
else
{
fontfallback = true
}
}
if (fontfallback)
{
var bf:BaseFont? = localFonts[dab[DA_FONT]]
if (bf == null)
{
val fn = stdFieldFontNames[dab[DA_FONT]]
if (fn != null)
{
try
{
var enc = "winansi"
if (fn.size > 1)
enc = fn[1]
bf = BaseFont.createFont(fn[0], enc, false)
tx.font = bf
}
catch (e:Exception) {// empty
                        }

}
}
else
tx.font = bf
}
}
//rotation, border and background color
        val mk = merged.getAsDict(PdfName.MK)
if (mk != null)
{
var ar = mk.getAsArray(PdfName.BC)
val border = getMKColor(ar)
tx.borderColor = border
if (border != null)
tx.borderWidth = 1
ar = mk.getAsArray(PdfName.BG)
tx.backgroundColor = getMKColor(ar)
val rotation = mk.getAsNumber(PdfName.R)
if (rotation != null)
tx.rotation = rotation.intValue()
}
//flags
        var nfl:PdfNumber? = merged.getAsNumber(PdfName.F)
flags = 0
tx.visibility = BaseField.VISIBLE_BUT_DOES_NOT_PRINT
if (nfl != null)
{
flags = nfl.intValue()
if (flags and PdfFormField.FLAGS_PRINT != 0 && flags and PdfFormField.FLAGS_HIDDEN != 0)
tx.visibility = BaseField.HIDDEN
else if (flags and PdfFormField.FLAGS_PRINT != 0 && flags and PdfFormField.FLAGS_NOVIEW != 0)
tx.visibility = BaseField.HIDDEN_BUT_PRINTABLE
else if (flags and PdfFormField.FLAGS_PRINT != 0)
tx.visibility = BaseField.VISIBLE
}
//multiline
        nfl = merged.getAsNumber(PdfName.FF)
flags = 0
if (nfl != null)
flags = nfl.intValue()
tx.options = flags
if (flags and PdfFormField.FF_COMB != 0)
{
val maxLen = merged.getAsNumber(PdfName.MAXLEN)
var len = 0
if (maxLen != null)
len = maxLen.intValue()
tx.maxCharacterLength = len
}
//alignment
        nfl = merged.getAsNumber(PdfName.Q)
if (nfl != null)
{
if (nfl.intValue() == PdfFormField.Q_CENTER)
tx.alignment = Element.ALIGN_CENTER
else if (nfl.intValue() == PdfFormField.Q_RIGHT)
tx.alignment = Element.ALIGN_RIGHT
}
//border styles
        val bs = merged.getAsDict(PdfName.BS)
if (bs != null)
{
val w = bs.getAsNumber(PdfName.W)
if (w != null)
tx.borderWidth = w.floatValue()
val s = bs.getAsName(PdfName.S)
if (PdfName.D == s)
tx.borderStyle = PdfBorderDictionary.STYLE_DASHED
else if (PdfName.B == s)
tx.borderStyle = PdfBorderDictionary.STYLE_BEVELED
else if (PdfName.I == s)
tx.borderStyle = PdfBorderDictionary.STYLE_INSET
else if (PdfName.U == s)
tx.borderStyle = PdfBorderDictionary.STYLE_UNDERLINE
}
else
{
val bd = merged.getAsArray(PdfName.BORDER)
if (bd != null)
{
if (bd.size() >= 3)
tx.borderWidth = bd.getAsNumber(2).floatValue()
if (bd.size() >= 4)
tx.borderStyle = PdfBorderDictionary.STYLE_DASHED
}
}
}

@Throws(IOException::class, DocumentException::class)
internal fun getAppearance(merged:PdfDictionary, values:Array<String>, fieldName:String):PdfAppearance {
val fieldType = merged.getAsName(PdfName.FT)

if (PdfName.BTN == fieldType)
{
val fieldFlags = merged.getAsNumber(PdfName.FF)
val isRadio = fieldFlags != null && fieldFlags.intValue() and PdfFormField.FF_RADIO != 0
val field = RadioCheckField(writer, null, null, null)
decodeGenericDictionary(merged, field)
//rect
            val rect = merged.getAsArray(PdfName.RECT)
var box = PdfReader.getNormalizedRectangle(rect)
if (field.rotation == 90 || field.rotation == 270)
box = box.rotate()
field.box = box
if (!isRadio)
field.setCheckType(RadioCheckField.TYPE_CROSS)
return field.getAppearance(isRadio, merged.getAsName(PdfName.AS) != PdfName.Off)
}

topFirst = 0
var text:String? = if (values.size > 0) values[0] else null

var tx:TextField? = null
if (fieldCache == null || !fieldCache!!.containsKey(fieldName))
{
tx = TextField(writer, null, null)
tx.setExtraMargin(extraMarginLeft, extraMarginTop)
tx.borderWidth = 0
tx.substitutionFonts = substitutionFonts
decodeGenericDictionary(merged, tx)
//rect
            val rect = merged.getAsArray(PdfName.RECT)
var box = PdfReader.getNormalizedRectangle(rect)
if (tx.rotation == 90 || tx.rotation == 270)
box = box.rotate()
tx.box = box
if (fieldCache != null)
fieldCache!!.put(fieldName, tx)
}
else
{
tx = fieldCache!![fieldName]
tx!!.writer = writer
}
if (PdfName.TX == fieldType)
{
if (values.size > 0 && values[0] != null)
{
tx.text = values[0]
}
return tx.appearance
}
if (PdfName.CH != fieldType)
throw DocumentException(MessageLocalization.getComposedMessage("an.appearance.was.requested.without.a.variable.text.field"))
val opt = merged.getAsArray(PdfName.OPT)
var flags = 0
val nfl = merged.getAsNumber(PdfName.FF)
if (nfl != null)
flags = nfl.intValue()
if (flags and PdfFormField.FF_COMBO != 0 && opt == null)
{
tx.text = text
return tx.appearance
}
if (opt != null)
{
val choices = arrayOfNulls<String>(opt.size())
val choicesExp = arrayOfNulls<String>(opt.size())
for (k in 0..opt.size() - 1)
{
val obj = opt.getPdfObject(k)
if (obj.isString)
{
choices[k] = choicesExp[k] = (obj as PdfString).toUnicodeString()
}
else
{
val a = obj as PdfArray
choicesExp[k] = a.getAsString(0).toUnicodeString()
choices[k] = a.getAsString(1).toUnicodeString()
}
}
if (flags and PdfFormField.FF_COMBO != 0)
{
for (k in choices.indices)
{
if (text == choicesExp[k])
{
text = choices[k]
break
}
}
tx.text = text
return tx.appearance
}
val indexes = ArrayList<Int>()
for (k in choicesExp.indices)
{
for (j in values.indices)
{
val `val` = values[j]
if (`val` != null && `val` == choicesExp[k])
{
indexes.add(Integer.valueOf(k))
break
}
}
}
tx.choices = choices
tx.choiceExports = choicesExp
tx.choiceSelections = indexes
}
val app = tx.listAppearance
topFirst = tx.topFirst
return app
}

@Throws(IOException::class, DocumentException::class)
internal fun getAppearance(merged:PdfDictionary, text:String, fieldName:String):PdfAppearance {
val valueArr = arrayOfNulls<String>(1)
valueArr[0] = text
return getAppearance(merged, valueArr, fieldName)
}

internal fun getMKColor(ar:PdfArray?):BaseColor? {
if (ar == null)
return null
when (ar.size()) {
1 -> return GrayColor(ar.getAsNumber(0).floatValue())
3 -> return BaseColor(ExtendedColor.normalize(ar.getAsNumber(0).floatValue()), ExtendedColor.normalize(ar.getAsNumber(1).floatValue()), ExtendedColor.normalize(ar.getAsNumber(2).floatValue()))
4 -> return CMYKColor(ar.getAsNumber(0).floatValue(), ar.getAsNumber(1).floatValue(), ar.getAsNumber(2).floatValue(), ar.getAsNumber(3).floatValue())
else -> return null
}
}

/**
       * Retrieve the rich value for the given field
       * @param name
      * * 
 * @return The rich value if present, or null.
      * * 
 * @since 5.0.6
 */
     fun getFieldRichValue(name:String):String? {
if (xfa!!.isXfaPresent)
{
return null
}

val item = fields[name] ?: return null

val merged = item.getMerged(0)
val rich = merged.getAsString(PdfName.RV)

var markup:String? = null
if (rich != null)
{
markup = rich.toString()
}

return markup
}
/**
       * Gets the field value.
     
       * @param name the fully qualified field name
      * * 
 * @return the field value
 */
     fun getField(name:String?):String? {
var name = name
if (xfa!!.isXfaPresent)
{
name = xfa!!.findFieldName(name, this)
if (name == null)
return null
name = XfaForm.Xml2Som.getShortName(name)
return XfaForm.getNodeText(xfa!!.findDatasetsNode(name))
}
val item = fields[name] ?: return null
lastWasString = false
val mergedDict = item.getMerged(0)

// Jose A. Rodriguez posted a fix to the mailing list (May 11, 2009)
        // explaining that the value can also be a stream value
        // the fix was made against an old iText version. Bruno adapted it.
        val v = PdfReader.getPdfObject(mergedDict.get(PdfName.V)) ?: return ""
if (v is PRStream)
{
val valBytes:ByteArray
try
{
valBytes = PdfReader.getStreamBytes(v as PRStream?)
return String(valBytes)
}
catch (e:IOException) {
throw ExceptionConverter(e)
}

}

val type = mergedDict.getAsName(PdfName.FT)
if (PdfName.BTN == type)
{
val ff = mergedDict.getAsNumber(PdfName.FF)
var flags = 0
if (ff != null)
flags = ff.intValue()
if (flags and PdfFormField.FF_PUSHBUTTON != 0)
return ""
var value = ""
if (v is PdfName)
value = PdfName.decodeName(v.toString())
else if (v is PdfString)
value = v.toUnicodeString()
val opts = item.getValue(0).getAsArray(PdfName.OPT)
if (opts != null)
{
var idx = 0
try
{
idx = Integer.parseInt(value)
val ps = opts.getAsString(idx)
value = ps.toUnicodeString()
lastWasString = true
}
catch (e:Exception) {}

}
return value
}
if (v is PdfString)
{
lastWasString = true
return v.toUnicodeString()
}
else if (v is PdfName)
{
return PdfName.decodeName(v.toString())
}
else
return ""
}

/**
       * Gets the field values of a Choice field.
     
       * @param name the fully qualified field name
      * * 
 * @return the field value
      * * 
 * @since 2.1.3
 */
     fun getListSelection(name:String):Array<String> {
var ret:Array<String>
val s = getField(name)
if (s == null)
{
ret = arrayOf<String>()
}
else
{
ret = arrayOf(s)
}
val item = fields[name] ?: return ret
//PdfName type = (PdfName)PdfReader.getPdfObject(((PdfDictionary)item.merged.get(0)).get(PdfName.FT));
        //if (!PdfName.CH.equals(type)) {
        //	return ret;
        //}
        val values = item.getMerged(0).getAsArray(PdfName.I) ?: return ret
ret = arrayOfNulls<String>(values.size())
val options = getListOptionExport(name)
var n:PdfNumber
var idx = 0
val i = values.listIterator()
while (i.hasNext())
{
n = i.next() as PdfNumber
ret[idx++] = options[n.intValue()]
}
return ret
}


/**
       * Sets a field property. Valid property names are:
       * 
 * 
       * 
       *  * textfont - sets the text font. The value for this entry is a BaseFont.
       *  * textcolor - sets the text color. The value for this entry is a BaseColor.
       *  * textsize - sets the text size. The value for this entry is a Float.
       *  * bgcolor - sets the background color. The value for this entry is a BaseColor.
           * If `null` removes the background.
       *  * bordercolor - sets the border color. The value for this entry is a BaseColor.
           * If `null` removes the border.
       * 
     
       * @param field the field name
      * * 
 * @param name the property name
      * * 
 * @param value the property value
      * * 
 * @param inst an array of int indexing into AcroField.Item.merged elements to process.
      * * Set to null to process all
      * * 
 * @return true if the property exists, false otherwise
 */
     fun setFieldProperty(field:String, name:String, value:Any?, inst:IntArray):Boolean {
if (writer == null)
throw RuntimeException(MessageLocalization.getComposedMessage("this.acrofields.instance.is.read.only"))
try
{
val item = fields[field] ?: return false
val hit = InstHit(inst)
var merged:PdfDictionary
var da:PdfString?
if (name.equals("textfont", ignoreCase = true))
{
for (k in 0..item.size() - 1)
{
if (hit.isHit(k))
{
merged = item.getMerged(k)
da = merged.getAsString(PdfName.DA)
var dr:PdfDictionary? = merged.getAsDict(PdfName.DR)
if (da != null)
{
if (dr == null)
{
dr = PdfDictionary()
merged.put(PdfName.DR, dr)
}
val dao = splitDAelements(da.toUnicodeString())
val cb = PdfAppearance()
if (dao[DA_FONT] != null)
{
val bf = value as BaseFont?
var psn:PdfName? = PdfAppearance.stdFieldFontNames[bf.postscriptFontName]
if (psn == null)
{
psn = PdfName(bf.postscriptFontName)
}
var fonts:PdfDictionary? = dr.getAsDict(PdfName.FONT)
if (fonts == null)
{
fonts = PdfDictionary()
dr.put(PdfName.FONT, fonts)
}
val fref = fonts.get(psn) as PdfIndirectReference
val top = reader.getCatalog().getAsDict(PdfName.ACROFORM)
markUsed(top)
dr = top.getAsDict(PdfName.DR)
if (dr == null)
{
dr = PdfDictionary()
top.put(PdfName.DR, dr)
}
markUsed(dr)
var fontsTop:PdfDictionary? = dr.getAsDict(PdfName.FONT)
if (fontsTop == null)
{
fontsTop = PdfDictionary()
dr.put(PdfName.FONT, fontsTop)
}
markUsed(fontsTop)
val frefTop = fontsTop.get(psn) as PdfIndirectReference
if (frefTop != null)
{
if (fref == null)
fonts.put(psn, frefTop)
}
else if (fref == null)
{
val fd:FontDetails
if (bf.fontType == BaseFont.FONT_TYPE_DOCUMENT)
{
fd = FontDetails(null, (bf as DocumentFont).indirectReference, bf)
}
else
{
bf.isSubset = false
fd = writer!!.addSimple(bf)
localFonts.put(psn.toString().substring(1), bf)
}
fontsTop.put(psn, fd.getIndirectReference())
fonts.put(psn, fd.getIndirectReference())
}
val buf = cb.internalBuffer
buf.append(psn.getBytes()).append(' ').append((dao[DA_SIZE] as Float).toFloat()).append(" Tf ")
if (dao[DA_COLOR] != null)
cb.setColorFill(dao[DA_COLOR] as BaseColor)
val s = PdfString(cb.toString())
item.getMerged(k).put(PdfName.DA, s)
item.getWidget(k).put(PdfName.DA, s)
markUsed(item.getWidget(k))
}
}
}
}
}
else if (name.equals("textcolor", ignoreCase = true))
{
for (k in 0..item.size() - 1)
{
if (hit.isHit(k))
{
merged = item.getMerged(k)
da = merged.getAsString(PdfName.DA)
if (da != null)
{
val dao = splitDAelements(da.toUnicodeString())
val cb = PdfAppearance()
if (dao[DA_FONT] != null)
{
val buf = cb.internalBuffer
buf.append(PdfName(dao[DA_FONT] as String).getBytes()).append(' ').append((dao[DA_SIZE] as Float).toFloat()).append(" Tf ")
cb.setColorFill(value as BaseColor?)
val s = PdfString(cb.toString())
item.getMerged(k).put(PdfName.DA, s)
item.getWidget(k).put(PdfName.DA, s)
markUsed(item.getWidget(k))
}
}
}
}
}
else if (name.equals("textsize", ignoreCase = true))
{
for (k in 0..item.size() - 1)
{
if (hit.isHit(k))
{
merged = item.getMerged(k)
da = merged.getAsString(PdfName.DA)
if (da != null)
{
val dao = splitDAelements(da.toUnicodeString())
val cb = PdfAppearance()
if (dao[DA_FONT] != null)
{
val buf = cb.internalBuffer
buf.append(PdfName(dao[DA_FONT] as String).getBytes()).append(' ').append((value as Float).toFloat()).append(" Tf ")
if (dao[DA_COLOR] != null)
cb.setColorFill(dao[DA_COLOR] as BaseColor)
val s = PdfString(cb.toString())
item.getMerged(k).put(PdfName.DA, s)
item.getWidget(k).put(PdfName.DA, s)
markUsed(item.getWidget(k))
}
}
}
}
}
else if (name.equals("bgcolor", ignoreCase = true) || name.equals("bordercolor", ignoreCase = true))
{
val dname = if (name.equals("bgcolor", ignoreCase = true)) PdfName.BG else PdfName.BC
for (k in 0..item.size() - 1)
{
if (hit.isHit(k))
{
merged = item.getMerged(k)
var mk:PdfDictionary? = merged.getAsDict(PdfName.MK)
if (mk == null)
{
if (value == null)
return true
mk = PdfDictionary()
item.getMerged(k).put(PdfName.MK, mk)
item.getWidget(k).put(PdfName.MK, mk)
markUsed(item.getWidget(k))
}
else
{
markUsed(mk)
}
if (value == null)
mk.remove(dname)
else
mk.put(dname, PdfFormField.getMKColor(value as BaseColor?))
}
}
}
else
return false
return true
}
catch (e:Exception) {
throw ExceptionConverter(e)
}

}

/**
       * Sets a field property. Valid property names are:
       * 
 * 
       * 
       *  * flags - a set of flags specifying various characteristics of the field's widget annotation.
       * The value of this entry replaces that of the F entry in the form's corresponding annotation dictionary.
       *  * setflags - a set of flags to be set (turned on) in the F entry of the form's corresponding
       * widget annotation dictionary. Bits equal to 1 cause the corresponding bits in F to be set to 1.
       *  * clrflags - a set of flags to be cleared (turned off) in the F entry of the form's corresponding
       * widget annotation dictionary. Bits equal to 1 cause the corresponding
       * bits in F to be set to 0.
       *  * fflags - a set of flags specifying various characteristics of the field. The value
       * of this entry replaces that of the Ff entry in the form's corresponding field dictionary.
       *  * setfflags - a set of flags to be set (turned on) in the Ff entry of the form's corresponding
       * field dictionary. Bits equal to 1 cause the corresponding bits in Ff to be set to 1.
       *  * clrfflags - a set of flags to be cleared (turned off) in the Ff entry of the form's corresponding
       * field dictionary. Bits equal to 1 cause the corresponding bits in Ff
       * to be set to 0.
       * 
     
       * @param field the field name
      * * 
 * @param name the property name
      * * 
 * @param value the property value
      * * 
 * @param inst an array of int indexing into AcroField.Item.merged elements to process.
      * * Set to null to process all
      * * 
 * @return true if the property exists, false otherwise
 */
     fun setFieldProperty(field:String, name:String, value:Int, inst:IntArray):Boolean {
if (writer == null)
throw RuntimeException(MessageLocalization.getComposedMessage("this.acrofields.instance.is.read.only"))
val item = fields[field] ?: return false
val hit = InstHit(inst)
if (name.equals("flags", ignoreCase = true))
{
val num = PdfNumber(value)
for (k in 0..item.size() - 1)
{
if (hit.isHit(k))
{
item.getMerged(k).put(PdfName.F, num)
item.getWidget(k).put(PdfName.F, num)
markUsed(item.getWidget(k))
}
}
}
else if (name.equals("setflags", ignoreCase = true))
{
for (k in 0..item.size() - 1)
{
if (hit.isHit(k))
{
var num:PdfNumber? = item.getWidget(k).getAsNumber(PdfName.F)
var `val` = 0
if (num != null)
`val` = num.intValue()
num = PdfNumber(`val` or value)
item.getMerged(k).put(PdfName.F, num)
item.getWidget(k).put(PdfName.F, num)
markUsed(item.getWidget(k))
}
}
}
else if (name.equals("clrflags", ignoreCase = true))
{
for (k in 0..item.size() - 1)
{
if (hit.isHit(k))
{
val widget = item.getWidget(k)
var num:PdfNumber? = widget.getAsNumber(PdfName.F)
var `val` = 0
if (num != null)
`val` = num.intValue()
num = PdfNumber(`val` and value.inv())
item.getMerged(k).put(PdfName.F, num)
widget.put(PdfName.F, num)
markUsed(widget)
}
}
}
else if (name.equals("fflags", ignoreCase = true))
{
val num = PdfNumber(value)
for (k in 0..item.size() - 1)
{
if (hit.isHit(k))
{
item.getMerged(k).put(PdfName.FF, num)
item.getValue(k).put(PdfName.FF, num)
markUsed(item.getValue(k))
}
}
}
else if (name.equals("setfflags", ignoreCase = true))
{
for (k in 0..item.size() - 1)
{
if (hit.isHit(k))
{
val valDict = item.getValue(k)
var num:PdfNumber? = valDict.getAsNumber(PdfName.FF)
var `val` = 0
if (num != null)
`val` = num.intValue()
num = PdfNumber(`val` or value)
item.getMerged(k).put(PdfName.FF, num)
valDict.put(PdfName.FF, num)
markUsed(valDict)
}
}
}
else if (name.equals("clrfflags", ignoreCase = true))
{
for (k in 0..item.size() - 1)
{
if (hit.isHit(k))
{
val valDict = item.getValue(k)
var num:PdfNumber? = valDict.getAsNumber(PdfName.FF)
var `val` = 0
if (num != null)
`val` = num.intValue()
num = PdfNumber(`val` and value.inv())
item.getMerged(k).put(PdfName.FF, num)
valDict.put(PdfName.FF, num)
markUsed(valDict)
}
}
}
else
return false
return true
}

/**
       * Merges an XML data structure into this form.
     
       * @param n the top node of the data structure
      * * 
 * @throws java.io.IOException on error
      * * 
 * @throws com.itextpdf.text.DocumentException o error
 */
    @Throws(IOException::class, DocumentException::class)
 fun mergeXfaData(n:Node) {
val data = XfaForm.Xml2SomDatasets(n)
for (string in data.getOrder())
{
val name = string
val text = XfaForm.getNodeText(data.getName2Node()[name])
setField(name, text)
}
}

/**
       * Sets the fields by FDF merging.
     
       * @param fdf the FDF form
      * * 
 * @throws IOException on error
      * * 
 * @throws DocumentException on error
 */
    @Throws(IOException::class, DocumentException::class)
 fun setFields(fdf:FdfReader) {
val fd = fdf.getFields()
for (f in fd.keys)
{
val v = fdf.getFieldValue(f)
if (v != null)
setField(f, v)
}
}

/**
       * Sets the fields by XFDF merging.
     
       * @param xfdf the XFDF form
      * * 
 * @throws IOException on error
      * * 
 * @throws DocumentException on error
 */
    @Throws(IOException::class, DocumentException::class)
 fun setFields(xfdf:XfdfReader) {
val fd = xfdf.getFields()
for (f in fd.keys)
{
val v = xfdf.getFieldValue(f)
if (v != null)
setField(f, v)
val l = xfdf.getListValues(f)
if (l != null)
setListSelection(v, l.toArray<String>(arrayOfNulls<String>(l.size)))
}
}

/**
       * Regenerates the field appearance.
       * This is useful when you change a field property, but not its value,
       * for instance form.setFieldProperty("f", "bgcolor", BaseColor.BLUE, null);
       * This won't have any effect, unless you use regenerateField("f") after changing
       * the property.
     
       * @param name the fully qualified field name or the partial name in the case of XFA forms
      * * 
 * @throws IOException on error
      * * 
 * @throws DocumentException on error
      * * 
 * @return true if the field was found and changed,
      * * false otherwise
 */
    @Throws(IOException::class, DocumentException::class)
 fun regenerateField(name:String):Boolean {
val value = getField(name)
return setField(name, value, value)
}

/**
       * Sets the field value.
     
       * @param name the fully qualified field name or the partial name in the case of XFA forms
      * * 
 * @param value the field value
      * * 
 * @param saveAppearance save the current appearance of the field or not
      * * 
 * @throws IOException on error
      * * 
 * @throws DocumentException on error
      * * 
 * @return true if the field was found and changed,
      * * false otherwise
 */
    @Throws(IOException::class, DocumentException::class)
 fun setField(name:String, value:String, saveAppearance:Boolean):Boolean {
return setField(name, value, null, saveAppearance)
}

/**
       * Sets the rich value for the given field.  See [PDF Reference](http://www.adobe.com/content/dam/Adobe/en/devnet/pdf/pdfs/PDF32000_2008.pdf) chapter 
       * 12.7.3.4 (Rich Text) and 12.7.4.3 (Text Fields) for further details. Note that iText doesn't create an appearance for Rich Text fields.
       * So you either need to use XML Worker to create an appearance (/N entry in the /AP dictionary), or you need to use setGenerateAppearances(false) to tell the viewer
       * that iText didn't create any appearances.
       * @param name  Field name
      * * 
 * @param richValue html markup 
      * * 
 * @return success/failure (will fail if the field isn't found, isn't a text field, or doesn't support rich text)
      * * 
 * @throws DocumentException
      * * 
 * @throws IOException 
      * * 
 * @since 5.0.6
 */
    @Throws(DocumentException::class, IOException::class)
 fun setFieldRichValue(name:String, richValue:String):Boolean {
if (writer == null)
{
// can't set field values: fail
            throw DocumentException(MessageLocalization.getComposedMessage("this.acrofields.instance.is.read.only"))
}

val item = getFieldItem(name) ?: return false // can't find the field: fail.

if (getFieldType(name) != FIELD_TYPE_TEXT)
{
// field isn't a text field: fail
    		return false
}

val merged = item.getMerged(0)
val ffNum = merged.getAsNumber(PdfName.FF)
var flagVal = 0
if (ffNum != null)
{
flagVal = ffNum.intValue()
}
if (flagVal and PdfFormField.FF_RICHTEXT == 0)
{
// text field doesn't support rich text: fail
    		return false
}

val richString = PdfString(richValue)
item.writeToAll(PdfName.RV, richString, Item.WRITE_MERGED or Item.WRITE_VALUE)

val `is` = ByteArrayInputStream(richValue.toByteArray())
val valueString = PdfString(XmlToTxt.parse(`is`))
item.writeToAll(PdfName.V, valueString, Item.WRITE_MERGED or Item.WRITE_VALUE)
return true
}

/**
       * Sets the field value and the display string. The display string
       * is used to build the appearance in the cases where the value
       * is modified by Acrobat with JavaScript and the algorithm is
       * known.
     
       * @param name the fully qualified field name or the partial name in the case of XFA forms
      * * 
 * @param value the field value
      * * 
 * @param display the string that is used for the appearance. If null
      * * the value parameter will be used
      * * 
 * @param saveAppearance save the current appearance of the field or not
      * * 
 * @return true if the field was found and changed,
      * * false otherwise
      * * 
 * @throws IOException on error
      * * 
 * @throws DocumentException on error
 */
    @Throws(IOException::class, DocumentException::class)
@JvmOverloads  fun setField(name:String?, value:String, display:String? = null, saveAppearance:Boolean = false):Boolean {
var name = name
var value = value
var display = display
if (writer == null)
throw DocumentException(MessageLocalization.getComposedMessage("this.acrofields.instance.is.read.only"))
if (xfa!!.isXfaPresent)
{
name = xfa!!.findFieldName(name, this)
if (name == null)
return false
val shortName = XfaForm.Xml2Som.getShortName(name)
var xn:Node? = xfa!!.findDatasetsNode(shortName)
if (xn == null)
{
xn = xfa!!.datasetsSom.insertNode(xfa!!.datasetsNode, shortName)
}
xfa!!.setNodeText(xn, value)
}
val item = fields[name] ?: return false
var merged = item.getMerged(0)
val type = merged.getAsName(PdfName.FT)
if (PdfName.TX == type)
{
val maxLen = merged.getAsNumber(PdfName.MAXLEN)
var len = 0
if (maxLen != null)
len = maxLen.intValue()
if (len > 0)
value = value.substring(0, Math.min(len, value.length))
}
if (display == null)
display = value
if (PdfName.TX == type || PdfName.CH == type)
{
val v = PdfString(value, PdfObject.TEXT_UNICODE)
for (idx in 0..item.size() - 1)
{
val valueDic = item.getValue(idx)
valueDic.put(PdfName.V, v)
valueDic.remove(PdfName.I)
markUsed(valueDic)
merged = item.getMerged(idx)
merged.remove(PdfName.I)
merged.put(PdfName.V, v)
val widget = item.getWidget(idx)
if (isGenerateAppearances)
{
val app = getAppearance(merged, display, name)
if (PdfName.CH == type)
{
val n = PdfNumber(topFirst)
widget.put(PdfName.TI, n)
merged.put(PdfName.TI, n)
}
var appDic:PdfDictionary? = widget.getAsDict(PdfName.AP)
if (appDic == null)
{
appDic = PdfDictionary()
widget.put(PdfName.AP, appDic)
merged.put(PdfName.AP, appDic)
}
appDic.put(PdfName.N, app.getIndirectReference())
writer!!.releaseTemplate(app)
}
else
{
widget.remove(PdfName.AP)
merged.remove(PdfName.AP)
}
markUsed(widget)
}
return true
}
else if (PdfName.BTN == type)
{
val ff = item.getMerged(0).getAsNumber(PdfName.FF)
var flags = 0
if (ff != null)
flags = ff.intValue()
if (flags and PdfFormField.FF_PUSHBUTTON != 0)
{
//we'll assume that the value is an image in base64
                val img:Image
try
{
img = Image.getInstance(Base64.decode(value))
}
catch (e:Exception) {
return false
}

val pb = getNewPushbuttonFromField(name)
pb.setImage(img)
replacePushbuttonField(name, pb.getField())
return true
}
val v = PdfName(value)
val lopt = ArrayList<String>()
val opts = item.getValue(0).getAsArray(PdfName.OPT)
if (opts != null)
{
for (k in 0..opts.size() - 1)
{
val valStr = opts.getAsString(k)
if (valStr != null)
lopt.add(valStr.toUnicodeString())
else
lopt.add(null)
}
}
val vidx = lopt.indexOf(value)
val vt:PdfName
if (vidx >= 0)
vt = PdfName(vidx.toString())
else
vt = v
for (idx in 0..item.size() - 1)
{
merged = item.getMerged(idx)
val widget = item.getWidget(idx)
val valDict = item.getValue(idx)
markUsed(item.getValue(idx))
valDict.put(PdfName.V, vt)
merged.put(PdfName.V, vt)
markUsed(widget)
val appDic = widget.getAsDict(PdfName.AP) ?: return false
val normal = appDic.getAsDict(PdfName.N)
if (isInAP(normal, vt) || normal == null)
{
merged.put(PdfName.AS, vt)
widget.put(PdfName.AS, vt)
}
else
{
merged.put(PdfName.AS, PdfName.Off)
widget.put(PdfName.AS, PdfName.Off)
}
if (isGenerateAppearances && !saveAppearance)
{
val app = getAppearance(merged, display, name)
if (normal != null)
normal.put(merged.getAsName(PdfName.AS), app.getIndirectReference())
else
appDic.put(PdfName.N, app.getIndirectReference())
writer!!.releaseTemplate(app)
}
}
return true
}
return false
}

/**
       * Sets different values in a list selection.
       * No appearance is generated yet; nor does the code check if multiple select is allowed.
     
       * @param	name	the name of the field
      * * 
 * @param	value	an array with values that need to be selected
      * * 
 * @return	true only if the field value was changed
      * * 
 * @since 2.1.4
 */
	@Throws(IOException::class, DocumentException::class)
 fun setListSelection(name:String, value:Array<String>):Boolean {
val item = getFieldItem(name) ?: return false
val merged = item.getMerged(0)
val type = merged.getAsName(PdfName.FT)
if (PdfName.CH != type)
{
return false
}
val options = getListOptionExport(name)
val array = PdfArray()
for (element in value)
{
for (j in options.indices)
{
if (options[j] == element)
{
array.add(PdfNumber(j))
break
}
}
}
item.writeToAll(PdfName.I, array, Item.WRITE_MERGED or Item.WRITE_VALUE)

val vals = PdfArray()
for (i in value.indices)
{
vals.add(PdfString(value[i]))
}
item.writeToAll(PdfName.V, vals, Item.WRITE_MERGED or Item.WRITE_VALUE)

val app = getAppearance(merged, value, name)

val apDic = PdfDictionary()
apDic.put(PdfName.N, app.indirectReference)
item.writeToAll(PdfName.AP, apDic, Item.WRITE_MERGED or Item.WRITE_WIDGET)

writer!!.releaseTemplate(app)

item.markUsed(this, Item.WRITE_VALUE or Item.WRITE_WIDGET)
return true
}

internal fun isInAP(nDic:PdfDictionary?, check:PdfName):Boolean {
return nDic != null && nDic.get(check) != null
}

/**
       * Gets all the fields. The fields are keyed by the fully qualified field name and
       * the value is an instance of AcroFields.Item.
     
       * @return all the fields
 */
     fun getFields():Map<String, Item> {
return fields
}

/**
       * Gets the field structure.
     
       * @param name the name of the field
      * * 
 * @return the field structure or null if the field
      * * does not exist
 */
     fun getFieldItem(name:String?):Item? {
var name = name
if (xfa!!.isXfaPresent)
{
name = xfa!!.findFieldName(name, this)
if (name == null)
return null
}
return fields[name]
}

/**
       * Gets the long XFA translated name.
     
       * @param name the name of the field
      * * 
 * @return the long field name
 */
     fun getTranslatedFieldName(name:String):String {
var name = name
if (xfa!!.isXfaPresent)
{
val namex = xfa!!.findFieldName(name, this)
if (namex != null)
name = namex
}
return name
}

/**
       * Gets the field box positions in the document. The return is an array of float
       * multiple of 5. For each of this groups the values are: [page, llx, lly, urx,
       * ury]. The coordinates have the page rotation in consideration.
     
       * @param name the field name
      * * 
 * @return the positions or null if field does not exist
 */
     fun getFieldPositions(name:String):List<FieldPosition>? {
val item = getFieldItem(name) ?: return null
val ret = ArrayList<FieldPosition>()
for (k in 0..item.size() - 1)
{
try
{
val wd = item.getWidget(k)
val rect = wd.getAsArray(PdfName.RECT) ?: continue
var r = PdfReader.getNormalizedRectangle(rect)
val page = item.getPage(k)!!.toInt()
val rotation = reader.getPageRotation(page)
val fp = FieldPosition()
fp.page = page
if (rotation != 0)
{
val pageSize = reader.getPageSize(page)
when (rotation) {
270 -> r = Rectangle(
pageSize.top - r.bottom, 
r.left, 
pageSize.top - r.top, 
r.right)
180 -> r = Rectangle(
pageSize.right - r.left, 
pageSize.top - r.bottom, 
pageSize.right - r.right, 
pageSize.top - r.top)
90 -> r = Rectangle(
r.bottom, 
pageSize.right - r.left, 
r.top, 
pageSize.right - r.right)
}
r.normalize()
}
fp.position = r
ret.add(fp)
}
catch (e:Exception) {// empty on purpose
            }

}
return ret
}

private fun removeRefFromArray(array:PdfArray, refo:PdfObject?):Int {
if (refo == null || !refo.isIndirect)
return array.size()
val ref = refo as PdfIndirectReference?
var j = 0
while (j < array.size())
{
val obj = array.getPdfObject(j)
if (!obj.isIndirect)
{
++j
continue
}
if ((obj as PdfIndirectReference).getNumber() == ref.getNumber())
array.remove(j--)
++j
}
return array.size()
}

/**
       * Removes all the fields from page.
     
       * @param page the page to remove the fields from
      * * 
 * @return true if any field was removed, false otherwise
 */
     fun removeFieldsFromPage(page:Int):Boolean {
if (page < 1)
return false
val names = arrayOfNulls<String>(fields.size)
fields.keys.toArray<String>(names)
var found = false
for (k in names.indices)
{
val fr = removeField(names[k], page)
found = found || fr
}
return found
}

/**
       * Removes a field from the document. If page equals -1 all the fields with this
       * name are removed from the document otherwise only the fields in
       * that particular page are removed.
     
       * @param name the field name
      * * 
 * @param page the page to remove the field from or -1 to remove it from all the pages
      * * 
 * @return true if the field exists, false otherwise
 */
    @JvmOverloads  fun removeField(name:String, page:Int = -1):Boolean {
val item = getFieldItem(name) ?: return false
val acroForm = PdfReader.getPdfObject(reader.getCatalog().get(PdfName.ACROFORM), reader.getCatalog()) as PdfDictionary ?: return false

val arrayf = acroForm.getAsArray(PdfName.FIELDS) ?: return false
var k = 0
while (k < item.size())
{
val pageV = item.getPage(k)!!.toInt()
if (page != -1 && page != pageV)
{
++k
continue
}
var ref:PdfIndirectReference? = item.getWidgetRef(k)
var wd = item.getWidget(k)
val pageDic = reader.getPageN(pageV)
val annots = pageDic.getAsArray(PdfName.ANNOTS)
if (annots != null)
{
if (removeRefFromArray(annots, ref) == 0)
{
pageDic.remove(PdfName.ANNOTS)
markUsed(pageDic)
}
else
markUsed(annots)
}
PdfReader.killIndirect(ref)
var kid:PdfIndirectReference = ref
while ((ref = wd.getAsIndirectObject(PdfName.PARENT)) != null)
{
wd = wd.getAsDict(PdfName.PARENT)
val kids = wd.getAsArray(PdfName.KIDS)
if (removeRefFromArray(kids, kid) != 0)
break
kid = ref
PdfReader.killIndirect(ref)
}
if (ref == null)
{
removeRefFromArray(arrayf, kid)
markUsed(arrayf)
}
if (page != -1)
{
item.remove(k)
--k
}
++k
}
if (page == -1 || item.size() == 0)
fields.remove(name)
return true
}

/** The field representations for retrieval and modification.  */
     class Item {

/**
           * This function writes the given key/value pair to all the instances
           * of merged, widget, and/or value, depending on the `writeFlags` setting
         
           * @since 2.1.5
          * *
          * * 
 * @param key        you'll never guess what this is for.
          * * 
 * @param value      if value is null, the key will be removed
          * * 
 * @param writeFlags ORed together WRITE_* flags
 */
         fun writeToAll(key:PdfName, value:PdfObject, writeFlags:Int) {
var i:Int
var curDict:PdfDictionary? = null
if (writeFlags and WRITE_MERGED != 0)
{
i = 0
while (i < merged.size)
{
curDict = getMerged(i)
curDict.put(key, value)
++i
}
}
if (writeFlags and WRITE_WIDGET != 0)
{
i = 0
while (i < widgets.size)
{
curDict = getWidget(i)
curDict.put(key, value)
++i
}
}
if (writeFlags and WRITE_VALUE != 0)
{
i = 0
while (i < values.size)
{
curDict = getValue(i)
curDict.put(key, value)
++i
}
}
}

/**
           * Mark all the item dictionaries used matching the given flags
         
           * @since 2.1.5
          * * 
 * @param writeFlags WRITE_MERGED is ignored
 */
         fun markUsed(parentFields:AcroFields, writeFlags:Int) {
if (writeFlags and WRITE_VALUE != 0)
{
for (i in 0..size() - 1)
{
parentFields.markUsed(getValue(i))
}
}
if (writeFlags and WRITE_WIDGET != 0)
{
for (i in 0..size() - 1)
{
parentFields.markUsed(getWidget(i))
}
}
}

/**
           * An array of PdfDictionary where the value tag /V
           * is present.
         
           * @since 5.0.2 public is now protected
 */
        protected var values = ArrayList<PdfDictionary>()

/**
           * An array of PdfDictionary with the widgets.
         
           * @since 5.0.2 public is now protected
 */
        protected var widgets = ArrayList<PdfDictionary>()

/**
           * An array of PdfDictionary with the widget references.
         
           * @since 5.0.2 public is now protected
 */
        protected var widget_refs = ArrayList<PdfIndirectReference>()

/**
           * An array of PdfDictionary with all the field
           * and widget tags merged.
         
           * @since 5.0.2 public is now protected
 */
        protected var merged = ArrayList<PdfDictionary>()

/**
           * An array of Integer with the page numbers where
           * the widgets are displayed.
         
           * @since 5.0.2 public is now protected
 */
        protected var page = ArrayList<Int>()
/**
           * An array of Integer with the tab order of the field in the page.
         
           * @since 5.0.2 public is now protected
 */
        protected var tabOrder = ArrayList<Int>()

/**
           * Preferred method of determining the number of instances
           * of a given field.
         
           * @since 2.1.5
          * * 
 * @return number of instances
 */
         fun size():Int {
return values.size
}

/**
           * Remove the given instance from this item.  It is possible to
           * remove all instances using this function.
         
           * @since 2.1.5
          * * 
 * @param killIdx
 */
        internal fun remove(killIdx:Int) {
values.removeAt(killIdx)
widgets.removeAt(killIdx)
widget_refs.removeAt(killIdx)
merged.removeAt(killIdx)
page.removeAt(killIdx)
tabOrder.removeAt(killIdx)
}

/**
           * Retrieve the value dictionary of the given instance
         
           * @since 2.1.5
          * * 
 * @param idx instance index
          * * 
 * @return dictionary storing this instance's value.  It may be shared across instances.
 */
         fun getValue(idx:Int):PdfDictionary {
return values[idx]
}

/**
           * Add a value dict to this Item
         
           * @since 2.1.5
          * * 
 * @param value new value dictionary
 */
        internal fun addValue(value:PdfDictionary) {
values.add(value)
}

/**
           * Retrieve the widget dictionary of the given instance
         
           * @since 2.1.5
          * * 
 * @param idx instance index
          * * 
 * @return The dictionary found in the appropriate page's Annot array.
 */
         fun getWidget(idx:Int):PdfDictionary {
return widgets[idx]
}

/**
           * Add a widget dict to this Item
         
           * @since 2.1.5
          * * 
 * @param widget
 */
        internal fun addWidget(widget:PdfDictionary) {
widgets.add(widget)
}

/**
           * Retrieve the reference to the given instance
         
           * @since 2.1.5
          * * 
 * @param idx instance index
          * * 
 * @return reference to the given field instance
 */
         fun getWidgetRef(idx:Int):PdfIndirectReference {
return widget_refs[idx]
}

/**
           * Add a widget ref to this Item
         
           * @since 2.1.5
          * * 
 * @param widgRef
 */
        internal fun addWidgetRef(widgRef:PdfIndirectReference) {
widget_refs.add(widgRef)
}

/**
           * Retrieve the merged dictionary for the given instance.  The merged
           * dictionary contains all the keys present in parent fields, though they
           * may have been overwritten (or modified?) by children.
           * Example: a merged radio field dict will contain /V
         
           * @since 2.1.5
          * * 
 * @param idx  instance index
          * * 
 * @return the merged dictionary for the given instance
 */
         fun getMerged(idx:Int):PdfDictionary {
return merged[idx]
}

/**
           * Adds a merged dictionary to this Item.
         
           * @since 2.1.5
          * * 
 * @param mergeDict
 */
        internal fun addMerged(mergeDict:PdfDictionary) {
merged.add(mergeDict)
}

/**
           * Retrieve the page number of the given instance
         
           * @since 2.1.5
          * * 
 * @param idx
          * * 
 * @return remember, pages are "1-indexed", not "0-indexed" like field instances.
 */
         fun getPage(idx:Int):Int? {
return page[idx]
}

/**
           * Adds a page to the current Item.
         
           * @since 2.1.5
          * * 
 * @param pg
 */
        internal fun addPage(pg:Int) {
page.add(Integer.valueOf(pg))
}

/**
           * forces a page value into the Item.
         
           * @since 2.1.5
          * * 
 * @param idx
 */
        internal fun forcePage(idx:Int, pg:Int) {
page[idx] = Integer.valueOf(pg)
}

/**
           * Gets the tabOrder.
         
           * @since 2.1.5
          * * 
 * @param idx
          * * 
 * @return tab index of the given field instance
 */
         fun getTabOrder(idx:Int):Int? {
return tabOrder[idx]
}

/**
           * Adds a tab order value to this Item.
         
           * @since 2.1.5
          * * 
 * @param order
 */
        internal fun addTabOrder(order:Int) {
tabOrder.add(Integer.valueOf(order))
}

companion object {

/**
           * writeToAll constant.
         
            * @since 2.1.5
 */
         val WRITE_MERGED = 1

/**
           * writeToAll and markUsed constant.
         
            * @since 2.1.5
 */
         val WRITE_WIDGET = 2

/**
           * writeToAll and markUsed constant.
         
            * @since 2.1.5
 */
         val WRITE_VALUE = 4
}
}

private class InstHit(inst:IntArray?) {
internal var hits:IntHashtable? = null
init{
if (inst == null)
return 
hits = IntHashtable()
for (k in inst!!.indices)
hits!!.put(inst[k], 1)
}

 fun isHit(n:Int):Boolean {
if (hits == null)
return true
return hits!!.containsKey(n)
}
}

/**
       * Clears a signed field.
       * @param name the field name
      * * 
 * @return true if the field was signed, false if the field was not signed or not found
      * * 
 * @since 5.0.5
 */
     fun clearSignatureField(name:String):Boolean {
sigNames = null
signatureNames
if (!sigNames!!.containsKey(name))
return false
val sig = fields[name]
sig.markUsed(this, Item.WRITE_VALUE or Item.WRITE_WIDGET)
val n = sig.size()
for (k in 0..n - 1)
{
clearSigDic(sig.getMerged(k))
clearSigDic(sig.getWidget(k))
clearSigDic(sig.getValue(k))
}
return true
}

private var orderedSignatureNames:ArrayList<String>? = null

/**
       * Gets the field names that have signatures and are signed.
     
       * @return the field names that have signatures and are signed
 */
     val signatureNames:ArrayList<String>
get() {
if (sigNames != null)
return ArrayList(orderedSignatureNames)
sigNames = HashMap<String, IntArray>()
orderedSignatureNames = ArrayList<String>()
val sorter = ArrayList<Array<Any>>()
for (entry in fields.entries)
{
val item = entry.value
val merged = item.getMerged(0)
if (PdfName.SIG != merged.get(PdfName.FT))
continue
val v = merged.getAsDict(PdfName.V) ?: continue
val contents = v.getAsString(PdfName.CONTENTS) ?: continue
val ro = v.getAsArray(PdfName.BYTERANGE) ?: continue
val rangeSize = ro.size()
if (rangeSize < 2)
continue
val length = ro.getAsNumber(rangeSize - 1).intValue() + ro.getAsNumber(rangeSize - 2).intValue()
sorter.add(arrayOf<Any>(entry.key, intArrayOf(length, 0)))
}
Collections.sort(sorter, AcroFields.SorterComparator())
if (!sorter.isEmpty())
{
if ((sorter[sorter.size - 1][1] as IntArray)[0].toLong() == reader.fileLength)
totalRevisions = sorter.size
else
totalRevisions = sorter.size + 1
for (k in sorter.indices)
{
val objs = sorter[k]
val name = objs[0] as String
val p = objs[1] as IntArray
p[1] = k + 1
sigNames!!.put(name, p)
orderedSignatureNames!!.add(name)
}
}
return ArrayList(orderedSignatureNames)
}

/**
       * Gets the field names that have blank signatures.
     
       * @return the field names that have blank signatures
 */
     val blankSignatureNames:ArrayList<String>
get() {
signatureNames
val sigs = ArrayList<String>()
for (entry in fields.entries)
{
val item = entry.value
val merged = item.getMerged(0)
if (PdfName.SIG != merged.getAsName(PdfName.FT))
continue
if (sigNames!!.containsKey(entry.key))
continue
sigs.add(entry.key)
}
return sigs
}

/**
       * Gets the signature dictionary, the one keyed by /V.
     
       * @param name the field name
      * * 
 * @return the signature dictionary keyed by /V or null if the field is not
      * * a signature
 */
     fun getSignatureDictionary(name:String):PdfDictionary? {
var name = name
signatureNames
name = getTranslatedFieldName(name)
if (!sigNames!!.containsKey(name))
return null
val item = fields[name]
val merged = item.getMerged(0)
return merged.getAsDict(PdfName.V)
}

/**
       * Gets a reference to the normal appearance of a field.
     
       * @param name the field name
      * * 
 * @return a reference to the /N entry of the /AP dictionary or null if the field is not found
 */
     fun getNormalAppearance(name:String):PdfIndirectReference? {
var name = name
signatureNames
name = getTranslatedFieldName(name)
val item = fields[name] ?: return null
val merged = item.getMerged(0)
val ap = merged.getAsDict(PdfName.AP) ?: return null
val ref = ap.getAsIndirectObject(PdfName.N) ?: return null
return ref
}

/**
       * Checks is the signature covers the entire document or just part of it.
     
       * @param name the signature field name
      * * 
 * @return true if the signature covers the entire document,
      * * false otherwise
 */
     fun signatureCoversWholeDocument(name:String):Boolean {
var name = name
signatureNames
name = getTranslatedFieldName(name)
if (!sigNames!!.containsKey(name))
return false
return sigNames!![name][0].toLong() == reader.fileLength
}

/**
       * Verifies a signature. An example usage is:
       * 
 * 
       * 
       * KeyStore kall = PdfPKCS7.loadCacertsKeyStore();
       * PdfReader reader = new PdfReader("my_signed_doc.pdf");
       * AcroFields af = reader.getAcroFields();
       * ArrayList names = af.getSignatureNames();
       * for (int k = 0; k &lt; names.size(); ++k) {
          * String name = (String)names.get(k);
          * System.out.println("Signature name: " + name);
          * System.out.println("Signature covers whole document: " + af.signatureCoversWholeDocument(name));
          * PdfPKCS7 pk = af.verifySignature(name);
          * Calendar cal = pk.getSignDate();
          * Certificate pkc[] = pk.getCertificates();
          * System.out.println("Subject: " + PdfPKCS7.getSubjectFields(pk.getSigningCertificate()));
          * System.out.println("Document modified: " + !pk.verify());
          * Object fails[] = PdfPKCS7.verifyCertificates(pkc, kall, null, cal);
          * if (fails == null)
              * System.out.println("Certificates verified against the KeyStore");
          * else
              * System.out.println("Certificate failed: " + fails[1]);
       * }
       * 
     
       * @param name the signature field name
      * * 
 * @param provider the provider or `null` for the default provider
      * * 
 * @return a PdfPKCS7 class to continue the verification
 */
    @JvmOverloads  fun verifySignature(name:String, provider:String? = null):PdfPKCS7? {
val v = getSignatureDictionary(name) ?: return null
try
{
val sub = v.getAsName(PdfName.SUBFILTER)
val contents = v.getAsString(PdfName.CONTENTS)
var pk:PdfPKCS7? = null
if (sub == PdfName.ADBE_X509_RSA_SHA1)
{
var cert:PdfString? = v.getAsString(PdfName.CERT)
if (cert == null)
cert = v.getAsArray(PdfName.CERT).getAsString(0)
pk = PdfPKCS7(contents.originalBytes, cert!!.getBytes(), provider)
}
else
pk = PdfPKCS7(contents.originalBytes, sub, provider)
updateByteRange(pk, v)
var str:PdfString? = v.getAsString(PdfName.M)
if (str != null)
pk!!.signDate = PdfDate.decode(str.toString())
val obj = PdfReader.getPdfObject(v.get(PdfName.NAME))
if (obj != null)
{
if (obj.isString)
pk!!.signName = (obj as PdfString).toUnicodeString()
else if (obj.isName)
pk!!.signName = PdfName.decodeName(obj.toString())
}
str = v.getAsString(PdfName.REASON)
if (str != null)
pk!!.reason = str.toUnicodeString()
str = v.getAsString(PdfName.LOCATION)
if (str != null)
pk!!.location = str.toUnicodeString()
return pk
}
catch (e:Exception) {
throw ExceptionConverter(e)
}

}

private fun updateByteRange(pkcs7:PdfPKCS7, v:PdfDictionary) {
val b = v.getAsArray(PdfName.BYTERANGE)
val rf = reader.safeFile
var rg:InputStream? = null
try
{
rg = RASInputStream(RandomAccessSourceFactory().createRanged(rf.createSourceView(), b.asLongArray()))
val buf = ByteArray(8192)
var rd:Int
while ((rd = rg.read(buf, 0, buf.size)) > 0)
{
pkcs7.update(buf, 0, rd)
}
}
catch (e:Exception) {
throw ExceptionConverter(e)
}
finally
{
try
{
if (rg != null) rg.close()
}
catch (e:IOException) {
// this really shouldn't ever happen - the source view we use is based on a Safe view, which is a no-op anyway
				throw ExceptionConverter(e)
}

}
}

private fun markUsed(obj:PdfObject) {
if (!append)
return 
(writer as PdfStamperImp).markUsed(obj)
}

/**
       * Gets the total number of revisions this document has.
     
       * @return the total number of revisions
 */
     fun getTotalRevisions():Int {
signatureNames
return this.totalRevisions
}

/**
       * Gets this field revision.
     
       * @param field the signature field name
      * * 
 * @return the revision or zero if it's not a signature field
 */
     fun getRevision(field:String):Int {
var field = field
signatureNames
field = getTranslatedFieldName(field)
if (!sigNames!!.containsKey(field))
return 0
return sigNames!![field][1]
}

/**
       * Extracts a revision from the document.
     
       * @param field the signature field name
      * * 
 * @return an InputStream covering the revision. Returns null if
      * * it's not a signature field
      * * 
 * @throws IOException on error
 */
    @Throws(IOException::class)
 fun extractRevision(field:String):InputStream? {
var field = field
signatureNames
field = getTranslatedFieldName(field)
if (!sigNames!!.containsKey(field))
return null
val length = sigNames!![field][0]
val raf = reader.safeFile
return RASInputStream(WindowRandomAccessSource(raf.createSourceView(), 0, length.toLong()))
}

/**
       * Sets extra margins in text fields to better mimic the Acrobat layout.
     
       * @param extraMarginLeft the extra margin left
      * * 
 * @param extraMarginTop the extra margin top
 */
     fun setExtraMargin(extraMarginLeft:Float, extraMarginTop:Float) {
this.extraMarginLeft = extraMarginLeft
this.extraMarginTop = extraMarginTop
}

/**
       * Adds a substitution font to the list. The fonts in this list will be used if the original
       * font doesn't contain the needed glyphs.
     
       * @param font the font
 */
     fun addSubstitutionFont(font:BaseFont) {
if (substitutionFonts == null)
substitutionFonts = ArrayList<BaseFont>()
substitutionFonts!!.add(font)
}

/**
       * Holds value of property totalRevisions.
      */
    private var totalRevisions:Int = 0

/**
       * Holds value of property fieldCache.
     
       * @since	2.1.5	this used to be a HashMap
 */
    /**
       * Gets the appearances cache.
     
       * @return the appearances cache
      * * 
 * @since	2.1.5	this method used to return a HashMap
 */
    /**
       * Sets a cache for field appearances. Parsing the existing PDF to
       * create a new TextField is time expensive. For those tasks that repeatedly
       * fill the same PDF with different field values the use of the cache has dramatic
       * speed advantages. An example usage:
       * 
 * 
       * 
       * String pdfFile = ...;// the pdf file used as template
       * ArrayList xfdfFiles = ...;// the xfdf file names
       * ArrayList pdfOutFiles = ...;// the output file names, one for each element in xpdfFiles
       * HashMap cache = new HashMap();// the appearances cache
       * PdfReader originalReader = new PdfReader(pdfFile);
       * for (int k = 0; k &lt; xfdfFiles.size(); ++k) {
          * PdfReader reader = new PdfReader(originalReader);
          * XfdfReader xfdf = new XfdfReader((String)xfdfFiles.get(k));
          * PdfStamper stp = new PdfStamper(reader, new FileOutputStream((String)pdfOutFiles.get(k)));
          * AcroFields af = stp.getAcroFields();
          * af.setFieldCache(cache);
          * af.setFields(xfdf);
          * stp.close();
       * }
       * 
     
       * @param fieldCache a Map that will carry the cached appearances
      * * 
 * @since	2.1.5	this method used to take a HashMap as parameter
 */
     var fieldCache:MutableMap<String, TextField>? = null
get() =this.fieldCache

private class SorterComparator:Comparator<Array<Any>> {
override fun compare(o1:Array<Any>, o2:Array<Any>):Int {
val n1 = (o1[1] as IntArray)[0]
val n2 = (o2[1] as IntArray)[0]
return n1 - n2
}
}

/**
       * Removes the XFA stream from the document.
      */
     fun removeXfa() {
val root = reader.getCatalog()
val acroform = root.getAsDict(PdfName.ACROFORM)
acroform.remove(PdfName.XFA)
try
{
xfa = XfaForm(reader)
}
catch (e:Exception) {
throw ExceptionConverter(e)
}

}

/**
       * Creates a new pushbutton from an existing field. This pushbutton can be changed and be used to replace
       * an existing one, with the same name or other name, as long is it is in the same document. To replace an existing pushbutton
       * call [.replacePushbuttonField].
     
       * @param field the field name that should be a pushbutton
      * * 
 * @param order the field order in fields with same name
      * * 
 * @return a new pushbutton or null if the field is not a pushbutton
      * *
      * * 
 * @since 2.0.7
 */
    @JvmOverloads  fun getNewPushbuttonFromField(field:String, order:Int = 0):PushbuttonField? {
try
{
if (getFieldType(field) != FIELD_TYPE_PUSHBUTTON)
return null
val item = getFieldItem(field)
if (order >= item.size())
return null
val pos = getFieldPositions(field)
val box = pos.get(order).position
val newButton = PushbuttonField(writer, box, null)
val dic = item.getMerged(order)
decodeGenericDictionary(dic, newButton)
val mk = dic.getAsDict(PdfName.MK)
if (mk != null)
{
val text = mk.getAsString(PdfName.CA)
if (text != null)
newButton.text = text.toUnicodeString()
val tp = mk.getAsNumber(PdfName.TP)
if (tp != null)
newButton.layout = tp.intValue() + 1
val ifit = mk.getAsDict(PdfName.IF)
if (ifit != null)
{
var sw:PdfName? = ifit.getAsName(PdfName.SW)
if (sw != null)
{
var scale = PushbuttonField.SCALE_ICON_ALWAYS
if (sw == PdfName.B)
scale = PushbuttonField.SCALE_ICON_IS_TOO_BIG
else if (sw == PdfName.S)
scale = PushbuttonField.SCALE_ICON_IS_TOO_SMALL
else if (sw == PdfName.N)
scale = PushbuttonField.SCALE_ICON_NEVER
newButton.scaleIcon = scale
}
sw = ifit.getAsName(PdfName.S)
if (sw != null)
{
if (sw == PdfName.A)
newButton.isProportionalIcon = false
}
val aj = ifit.getAsArray(PdfName.A)
if (aj != null && aj.size() == 2)
{
val left = aj.getAsNumber(0).floatValue()
val bottom = aj.getAsNumber(1).floatValue()
newButton.iconHorizontalAdjustment = left
newButton.iconVerticalAdjustment = bottom
}
val fb = ifit.getAsBoolean(PdfName.FB)
if (fb != null && fb.booleanValue())
newButton.isIconFitToBounds = true
}
val i = mk.get(PdfName.I)
if (i != null && i.isIndirect)
newButton.iconReference = i as PRIndirectReference?
}
return newButton
}
catch (e:Exception) {
throw ExceptionConverter(e)
}

}

/**
       * Replaces the designated field with a new pushbutton. The pushbutton can be created with
       * [.getNewPushbuttonFromField] from the same document or it can be a
       * generic PdfFormField of the type pushbutton.
     
       * @param field the field name
      * * 
 * @param button the PdfFormField representing the pushbutton
      * * 
 * @param order the field order in fields with same name
      * * 
 * @return true if the field was replaced, false if the field
      * * was not a pushbutton
      * *
      * * 
 * @since 2.0.7
 */
    @JvmOverloads  fun replacePushbuttonField(field:String, button:PdfFormField, order:Int = 0):Boolean {
if (getFieldType(field) != FIELD_TYPE_PUSHBUTTON)
return false
val item = getFieldItem(field)
if (order >= item.size())
return false
val merged = item.getMerged(order)
val values = item.getValue(order)
val widgets = item.getWidget(order)
for (k in buttonRemove.indices)
{
merged.remove(buttonRemove[k])
values.remove(buttonRemove[k])
widgets.remove(buttonRemove[k])
}
for (element in button.keys)
{
if (element == PdfName.T)
continue
if (element == PdfName.FF)
values.put(element, button.get(element))
else
widgets.put(element, button.get(element))
merged.put(element, button.get(element))
markUsed(values)
markUsed(widgets)
}
return true
}

/**
       * Checks whether a name exists as a signature field or not. It checks both signed fields and blank signatures.
       * @param name String
      * * 
 * @return boolean does the signature field exist
      * * 
 * @since 5.5.1
 */
     fun doesSignatureFieldExist(name:String):Boolean {
return blankSignatureNames.contains(name) || signatureNames.contains(name)
}

/**
       * A class representing a field position
       * @since 5.0.2
 */
     class FieldPosition {
 var page:Int = 0
 var position:Rectangle
}

companion object {
 val DA_FONT = 0
 val DA_SIZE = 1
 val DA_COLOR = 2

/**
       * A field type invalid or not found.
      */
     val FIELD_TYPE_NONE = 0

/**
       * A field type.
      */
     val FIELD_TYPE_PUSHBUTTON = 1

/**
       * A field type.
      */
     val FIELD_TYPE_CHECKBOX = 2

/**
       * A field type.
      */
     val FIELD_TYPE_RADIOBUTTON = 3

/**
       * A field type.
      */
     val FIELD_TYPE_TEXT = 4

/**
       * A field type.
      */
     val FIELD_TYPE_LIST = 5

/**
       * A field type.
      */
     val FIELD_TYPE_COMBO = 6

/**
       * A field type.
      */
     val FIELD_TYPE_SIGNATURE = 7

 fun splitDAelements(da:String):Array<Any> {
try
{
val tk = PRTokeniser(RandomAccessFileOrArray(RandomAccessSourceFactory().createSource(PdfEncodings.convertToBytes(da, null))))
val stack = ArrayList<String>()
val ret = arrayOfNulls<Any>(3)
while (tk.nextToken())
{
if (tk.tokenType == TokenType.COMMENT)
continue
if (tk.tokenType == TokenType.OTHER)
{
val operator = tk.getStringValue()
if (operator == "Tf")
{
if (stack.size >= 2)
{
ret[DA_FONT] = stack[stack.size - 2]
ret[DA_SIZE] = Float(stack[stack.size - 1])
}
}
else if (operator == "g")
{
if (stack.size >= 1)
{
val gray = Float(stack[stack.size - 1]).toFloat()
if (gray != 0f)
ret[DA_COLOR] = GrayColor(gray)
}
}
else if (operator == "rg")
{
if (stack.size >= 3)
{
val red = Float(stack[stack.size - 3]).toFloat()
val green = Float(stack[stack.size - 2]).toFloat()
val blue = Float(stack[stack.size - 1]).toFloat()
ret[DA_COLOR] = BaseColor(red, green, blue)
}
}
else if (operator == "k")
{
if (stack.size >= 4)
{
val cyan = Float(stack[stack.size - 4]).toFloat()
val magenta = Float(stack[stack.size - 3]).toFloat()
val yellow = Float(stack[stack.size - 2]).toFloat()
val black = Float(stack[stack.size - 1]).toFloat()
ret[DA_COLOR] = CMYKColor(cyan, magenta, yellow, black)
}
}
stack.clear()
}
else
stack.add(tk.getStringValue())
}
return ret
}
catch (ioe:IOException) {
throw ExceptionConverter(ioe)
}

}

private fun clearSigDic(dic:PdfDictionary) {
dic.remove(PdfName.AP)
dic.remove(PdfName.AS)
dic.remove(PdfName.V)
dic.remove(PdfName.DV)
dic.remove(PdfName.SV)
dic.remove(PdfName.FF)
dic.put(PdfName.F, PdfNumber(PdfAnnotation.FLAGS_PRINT))
}

private val stdFieldFontNames = HashMap<String, Array<String>>()

init{
stdFieldFontNames.put("CoBO", arrayOf("Courier-BoldOblique"))
stdFieldFontNames.put("CoBo", arrayOf("Courier-Bold"))
stdFieldFontNames.put("CoOb", arrayOf("Courier-Oblique"))
stdFieldFontNames.put("Cour", arrayOf("Courier"))
stdFieldFontNames.put("HeBO", arrayOf("Helvetica-BoldOblique"))
stdFieldFontNames.put("HeBo", arrayOf("Helvetica-Bold"))
stdFieldFontNames.put("HeOb", arrayOf("Helvetica-Oblique"))
stdFieldFontNames.put("Helv", arrayOf("Helvetica"))
stdFieldFontNames.put("Symb", arrayOf("Symbol"))
stdFieldFontNames.put("TiBI", arrayOf("Times-BoldItalic"))
stdFieldFontNames.put("TiBo", arrayOf("Times-Bold"))
stdFieldFontNames.put("TiIt", arrayOf("Times-Italic"))
stdFieldFontNames.put("TiRo", arrayOf("Times-Roman"))
stdFieldFontNames.put("ZaDb", arrayOf("ZapfDingbats"))
stdFieldFontNames.put("HySm", arrayOf("HYSMyeongJo-Medium", "UniKS-UCS2-H"))
stdFieldFontNames.put("HyGo", arrayOf("HYGoThic-Medium", "UniKS-UCS2-H"))
stdFieldFontNames.put("KaGo", arrayOf("HeiseiKakuGo-W5", "UniKS-UCS2-H"))
stdFieldFontNames.put("KaMi", arrayOf("HeiseiMin-W3", "UniJIS-UCS2-H"))
stdFieldFontNames.put("MHei", arrayOf("MHei-Medium", "UniCNS-UCS2-H"))
stdFieldFontNames.put("MSun", arrayOf("MSung-Light", "UniCNS-UCS2-H"))
stdFieldFontNames.put("STSo", arrayOf("STSong-Light", "UniGB-UCS2-H"))
}

private val buttonRemove = arrayOf(PdfName.MK, PdfName.F, PdfName.FF, PdfName.Q, PdfName.BS, PdfName.BORDER)
}
}/**
       * Sets the field value.
     
       * @param name the fully qualified field name or the partial name in the case of XFA forms
      * * 
 * @param value the field value
      * * 
 * @throws IOException on error
      * * 
 * @throws DocumentException on error
      * * 
 * @return true if the field was found and changed,
      * * false otherwise
 *//**
       * Sets the field value and the display string. The display string
       * is used to build the appearance in the cases where the value
       * is modified by Acrobat with JavaScript and the algorithm is
       * known.
     
       * @param name the fully qualified field name or the partial name in the case of XFA forms
      * * 
 * @param value the field value
      * * 
 * @param display the string that is used for the appearance. If null
      * * the value parameter will be used
      * * 
 * @return true if the field was found and changed,
      * * false otherwise
      * * 
 * @throws IOException on error
      * * 
 * @throws DocumentException on error
 *//**
       * Removes a field from the document.
     
       * @param name the field name
      * * 
 * @return true if the field exists, false otherwise
 *//**
       * Verifies a signature. An example usage is:
       * 
 * 
       * 
       * KeyStore kall = PdfPKCS7.loadCacertsKeyStore();
       * PdfReader reader = new PdfReader("my_signed_doc.pdf");
       * AcroFields af = reader.getAcroFields();
       * ArrayList names = af.getSignatureNames();
       * for (int k = 0; k &lt; names.size(); ++k) {
          * String name = (String)names.get(k);
          * System.out.println("Signature name: " + name);
          * System.out.println("Signature covers whole document: " + af.signatureCoversWholeDocument(name));
          * PdfPKCS7 pk = af.verifySignature(name);
          * Calendar cal = pk.getSignDate();
          * Certificate pkc[] = pk.getCertificates();
          * System.out.println("Subject: " + PdfPKCS7.getSubjectFields(pk.getSigningCertificate()));
          * System.out.println("Document modified: " + !pk.verify());
          * Object fails[] = PdfPKCS7.verifyCertificates(pkc, kall, null, cal);
          * if (fails == null)
              * System.out.println("Certificates verified against the KeyStore");
          * else
              * System.out.println("Certificate failed: " + fails[1]);
       * }
       * 
     
       * @param name the signature field name
      * * 
 * @return a PdfPKCS7 class to continue the verification
 *//**
       * Creates a new pushbutton from an existing field. If there are several pushbuttons with the same name
       * only the first one is used. This pushbutton can be changed and be used to replace
       * an existing one, with the same name or other name, as long is it is in the same document. To replace an existing pushbutton
       * call [.replacePushbuttonField].
     
       * @param field the field name that should be a pushbutton
      * * 
 * @return a new pushbutton or null if the field is not a pushbutton
 *//**
       * Replaces the first field with a new pushbutton. The pushbutton can be created with
       * [.getNewPushbuttonFromField] from the same document or it can be a
       * generic PdfFormField of the type pushbutton.
     
       * @param field the field name
      * * 
 * @param button the PdfFormField representing the pushbutton
      * * 
 * @return true if the field was replaced, false if the field
      * * was not a pushbutton
 */
