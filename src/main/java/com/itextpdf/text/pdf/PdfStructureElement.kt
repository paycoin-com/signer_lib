/*
 * $Id: 7e0508b5e8888c7131e34098e4e14f156e593ded $
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
import com.itextpdf.text.pdf.interfaces.IAccessibleElement
import com.itextpdf.text.pdf.interfaces.IPdfStructureElement
import com.itextpdf.text.pdf.internal.PdfIsoKeys

import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList
import java.util.HashMap

/**
 * This is a node in a document logical structure. It may contain a mark point or it may contain
 * other nodes.
 * @author Paulo Soares
 */
class PdfStructureElement : PdfDictionary, IPdfStructureElement {

    /**
     * Holds value of property kids.
     */
    @Transient private var parent: PdfStructureElement? = null
    @Transient private var top: PdfStructureTreeRoot? = null

    protected val elementId: AccessibleElementId

    /**
     * Holds value of property reference.
     */
    /**
     * Gets the reference this object will be written to.
     * @return the reference this object will be written to
     * *
     * @since    2.1.6 method removed in 2.1.5, but restored in 2.1.6
     */
    var reference: PdfIndirectReference? = null
        private set

    var structureType: PdfName? = null
        private set

    /**
     * Creates a new instance of PdfStructureElement.
     * @param parent the parent of this node
     * *
     * @param structureType the type of structure. It may be a standard type or a user type mapped by the role map
     */
    constructor(parent: PdfStructureElement, structureType: PdfName) {
        top = parent.top
        init(parent, structureType)
        this.parent = parent
        put(PdfName.P, parent.reference)
        put(PdfName.TYPE, PdfName.STRUCTELEM)
    }

    /**
     * Creates a new instance of PdfStructureElement.
     * @param parent the parent of this node
     * *
     * @param structureType the type of structure. It may be a standard type or a user type mapped by the role map
     */
    constructor(parent: PdfStructureTreeRoot, structureType: PdfName) {
        top = parent
        init(parent, structureType)
        put(PdfName.P, parent.reference)
        put(PdfName.TYPE, PdfName.STRUCTELEM)
    }

    protected constructor(parent: PdfDictionary, structureType: PdfName, elementId: AccessibleElementId) {
        this.elementId = elementId
        if (parent is PdfStructureElement) {
            top = parent.top
            init(parent, structureType)
            this.parent = parent
            put(PdfName.P, parent.reference)
            put(PdfName.TYPE, PdfName.STRUCTELEM)
        } else if (parent is PdfStructureTreeRoot) {
            top = parent
            init(parent, structureType)
            put(PdfName.P, parent.reference)
            put(PdfName.TYPE, PdfName.STRUCTELEM)
        } else {

        }
    }

    private fun init(parent: PdfDictionary, structureType: PdfName) {
        if (!top!!.writer.standardStructElems.contains(structureType)) {
            val roleMap = top!!.getAsDict(PdfName.ROLEMAP)
            if (roleMap == null || !roleMap.contains(structureType))
                throw ExceptionConverter(DocumentException(MessageLocalization.getComposedMessage("unknown.structure.element.role.1", structureType.toString())))
            else
                this.structureType = roleMap.getAsName(structureType)
        } else {
            this.structureType = structureType
        }
        val kido = parent.get(PdfName.K)
        var kids: PdfArray? = null
        if (kido == null) {
            kids = PdfArray()
            parent.put(PdfName.K, kids)
        } else if (kido is PdfArray) {
            kids = kido as PdfArray?
        } else {
            kids = PdfArray()
            kids.add(kido)
            parent.put(PdfName.K, kids)
        }
        if (kids!!.size() > 0) {
            if (kids.getAsNumber(0) != null)
                kids.remove(0)
            if (kids.size() > 0) {
                val mcr = kids.getAsDict(0)
                if (mcr != null && PdfName.MCR == mcr.getAsName(PdfName.TYPE)) {
                    kids.remove(0)
                }
            }
        }
        put(PdfName.S, structureType)
        reference = top!!.writer.pdfIndirectReference
        kids.add(this.reference)
    }

    @JvmOverloads fun getParent(includeStructTreeRoot: Boolean = false): PdfDictionary {
        if (parent == null && includeStructTreeRoot)
            return top
        else
            return parent
    }

    internal fun setPageMark(page: Int, mark: Int) {
        if (mark >= 0)
            put(PdfName.K, PdfNumber(mark))
        top!!.setPageMark(page, reference)
    }

    internal fun setAnnotation(annot: PdfAnnotation, currentPage: PdfIndirectReference) {
        var kArray: PdfArray? = getAsArray(PdfName.K)
        if (kArray == null) {
            kArray = PdfArray()
            val k = get(PdfName.K)
            if (k != null) {
                kArray.add(k)
            }
            put(PdfName.K, kArray)
        }
        val dict = PdfDictionary()
        dict.put(PdfName.TYPE, PdfName.OBJR)
        dict.put(PdfName.OBJ, annot.indirectReference)
        if (annot.role === PdfName.FORM)
            dict.put(PdfName.PG, currentPage)
        kArray.add(dict)
    }

    /**
     * Gets the first entarance of attribute.
     * @returns PdfObject
     * *
     * @since 5.3.4
     */
    fun getAttribute(name: PdfName): PdfObject {
        val attr = getAsDict(PdfName.A)
        if (attr != null) {
            if (attr.contains(name))
                return attr.get(name)
        }
        val parent = getParent()
        if (parent is PdfStructureElement)
            return parent.getAttribute(name)
        if (parent is PdfStructureTreeRoot)
            return parent.getAttribute(name)

        return PdfNull()
    }

    /**
     * Sets the attribute value.
     * @since 5.3.4
     */
    fun setAttribute(name: PdfName, obj: PdfObject) {
        var attr: PdfDictionary? = getAsDict(PdfName.A)
        if (attr == null) {
            attr = PdfDictionary()
            put(PdfName.A, attr)
        }
        attr.put(name, obj)
    }

    fun writeAttributes(element: IAccessibleElement) {
        // I do remember that these lines were necessary to avoid creation of files which are not valid from Acrobat 10 preflight perspective.
        // Now it seems that in Acrobat 11 there's no such problem (I think Acrobat 10 behavior can be considered as a bug) and we can remove those lines.
        //        if (top.getWriter().getPdfVersion().getVersion() < PdfWriter.VERSION_1_7)
        //            return;

        if (element is ListItem) {
            writeAttributes(element)
        } else if (element is Paragraph) {
            writeAttributes(element)
        } else if (element is Chunk) {
            writeAttributes(element)
        } else if (element is Image) {
            writeAttributes(element)
        } else if (element is List) {
            writeAttributes(element)
        } else if (element is ListLabel) {
            writeAttributes(element)
        } else if (element is ListBody) {
            writeAttributes(element)
        } else if (element is PdfPTable) {
            writeAttributes(element)
        } else if (element is PdfPRow) {
            writeAttributes(element)
        } else if (element is PdfPHeaderCell) {
            writeAttributes(element)
        } else if (element is PdfPCell) {
            writeAttributes(element)
        } else if (element is PdfPTableHeader) {
            writeAttributes(element)
        } else if (element is PdfPTableFooter) {
            writeAttributes(element)
        } else if (element is PdfPTableBody) {
            writeAttributes(element)
        } else if (element is PdfDiv) {
            writeAttributes(element)
        } else if (element is PdfTemplate) {
            writeAttributes(element)
        } else if (element is Document) {
            writeAttributes(element)
        }
        if (element.accessibleAttributes != null) {
            for (key in element.accessibleAttributes.keys) {
                if (key == PdfName.ID) {
                    val attr = element.getAccessibleAttribute(key)
                    put(key, attr)
                    top!!.putIDTree(attr.toString(), reference)
                } else if (key == PdfName.LANG || key == PdfName.ALT || key == PdfName.ACTUALTEXT || key == PdfName.E || key == PdfName.T) {
                    put(key, element.getAccessibleAttribute(key))
                } else {
                    setAttribute(key, element.getAccessibleAttribute(key))
                }
            }
        }
    }

    private fun writeAttributes(chunk: Chunk?) {
        if (chunk != null) {
            if (chunk.image != null) {
                writeAttributes(chunk.image)
            } else {
                val attr = chunk.attributes
                if (attr != null) {
                    this.setAttribute(PdfName.O, PdfName.LAYOUT)
                    // Setting non-inheritable attributes
                    if (attr.containsKey(Chunk.UNDERLINE)) {
                        this.setAttribute(PdfName.TEXTDECORATIONTYPE, PdfName.UNDERLINE)
                    }
                    if (attr.containsKey(Chunk.BACKGROUND)) {
                        val back = attr[Chunk.BACKGROUND] as Array<Any>
                        val color = back[0] as BaseColor
                        this.setAttribute(PdfName.BACKGROUNDCOLOR, PdfArray(floatArrayOf(color.red / 255f, color.green / 255f, color.blue / 255f)))
                    }

                    // Setting inheritable attributes
                    val parent = this.getParent(true) as IPdfStructureElement
                    val obj = getParentAttribute(parent, PdfName.COLOR)
                    if (chunk.font != null && chunk.font.color != null) {
                        val c = chunk.font.color
                        setColorAttribute(c, obj, PdfName.COLOR)
                    }
                    val decorThickness = getParentAttribute(parent, PdfName.TEXTDECORATIONTHICKNESS)
                    val decorColor = getParentAttribute(parent, PdfName.TEXTDECORATIONCOLOR)
                    if (attr.containsKey(Chunk.UNDERLINE)) {
                        val unders = attr[Chunk.UNDERLINE] as Array<Array<Any>>
                        val arr = unders[unders.size - 1]
                        val color = arr[0] as BaseColor
                        val floats = arr[1] as FloatArray
                        val thickness = floats[0]
                        // Setting thickness
                        if (decorThickness is PdfNumber) {
                            val t = decorThickness.floatValue()
                            if (java.lang.Float.compare(thickness, t) != 0) {
                                this.setAttribute(PdfName.TEXTDECORATIONTHICKNESS, PdfNumber(thickness))
                            }
                        } else
                            this.setAttribute(PdfName.TEXTDECORATIONTHICKNESS, PdfNumber(thickness))

                        // Setting decoration color
                        if (color != null) {
                            setColorAttribute(color, decorColor, PdfName.TEXTDECORATIONCOLOR)
                        }
                    }

                    if (attr.containsKey(Chunk.LINEHEIGHT)) {
                        val height = attr[Chunk.LINEHEIGHT] as Float
                        val parentLH = getParentAttribute(parent, PdfName.LINEHEIGHT)
                        if (parentLH is PdfNumber) {
                            val pLH = parentLH.floatValue()
                            if (java.lang.Float.compare(pLH, height) != 0) {
                                this.setAttribute(PdfName.LINEHEIGHT, PdfNumber(height))
                            }
                        } else
                            this.setAttribute(PdfName.LINEHEIGHT, PdfNumber(height))
                    }
                }
            }
        }
    }

    private fun writeAttributes(image: Image?) {
        if (image != null) {
            this.setAttribute(PdfName.O, PdfName.LAYOUT)
            if (image.width > 0) {
                this.setAttribute(PdfName.WIDTH, PdfNumber(image.width))
            }
            if (image.height > 0) {
                this.setAttribute(PdfName.HEIGHT, PdfNumber(image.height))
            }
            val rect = PdfRectangle(image, image.rotation)
            this.setAttribute(PdfName.BBOX, rect)
            if (image.backgroundColor != null) {
                val color = image.backgroundColor
                this.setAttribute(PdfName.BACKGROUNDCOLOR, PdfArray(floatArrayOf(color.red / 255f, color.green / 255f, color.blue / 255f)))
            }
        }
    }

    private fun writeAttributes(template: PdfTemplate?) {
        if (template != null) {
            this.setAttribute(PdfName.O, PdfName.LAYOUT)
            if (template.width > 0) {
                this.setAttribute(PdfName.WIDTH, PdfNumber(template.width))
            }
            if (template.height > 0) {
                this.setAttribute(PdfName.HEIGHT, PdfNumber(template.height))
            }
            val rect = PdfRectangle(template.boundingBox)
            this.setAttribute(PdfName.BBOX, rect)
        }
    }

    private fun writeAttributes(paragraph: Paragraph?) {
        if (paragraph != null) {
            this.setAttribute(PdfName.O, PdfName.LAYOUT)
            // Setting non-inheritable attributes
            if (java.lang.Float.compare(paragraph.getSpacingBefore(), 0f) != 0)
                this.setAttribute(PdfName.SPACEBEFORE, PdfNumber(paragraph.getSpacingBefore()))
            if (java.lang.Float.compare(paragraph.getSpacingAfter(), 0f) != 0)
                this.setAttribute(PdfName.SPACEAFTER, PdfNumber(paragraph.getSpacingAfter()))

            // Setting inheritable attributes
            val parent = this.getParent(true) as IPdfStructureElement
            var obj: PdfObject = getParentAttribute(parent, PdfName.COLOR)
            if (paragraph.font != null && paragraph.font.color != null) {
                val c = paragraph.font.color
                setColorAttribute(c, obj, PdfName.COLOR)
            }
            obj = getParentAttribute(parent, PdfName.TEXTINDENT)
            if (java.lang.Float.compare(paragraph.firstLineIndent, 0f) != 0) {
                var writeIndent = true
                if (obj is PdfNumber) {
                    if (java.lang.Float.compare(obj.floatValue(), paragraph.firstLineIndent) == 0)
                        writeIndent = false
                }
                if (writeIndent)
                    this.setAttribute(PdfName.TEXTINDENT, PdfNumber(paragraph.firstLineIndent))
            }
            obj = getParentAttribute(parent, PdfName.STARTINDENT)
            if (obj is PdfNumber) {
                val startIndent = obj.floatValue()
                if (java.lang.Float.compare(startIndent, paragraph.getIndentationLeft()) != 0)
                    this.setAttribute(PdfName.STARTINDENT, PdfNumber(paragraph.getIndentationLeft()))
            } else {
                if (Math.abs(paragraph.getIndentationLeft()) > java.lang.Float.MIN_VALUE)
                    this.setAttribute(PdfName.STARTINDENT, PdfNumber(paragraph.getIndentationLeft()))
            }

            obj = getParentAttribute(parent, PdfName.ENDINDENT)
            if (obj is PdfNumber) {
                val endIndent = obj.floatValue()
                if (java.lang.Float.compare(endIndent, paragraph.getIndentationRight()) != 0)
                    this.setAttribute(PdfName.ENDINDENT, PdfNumber(paragraph.getIndentationRight()))
            } else {
                if (java.lang.Float.compare(paragraph.getIndentationRight(), 0f) != 0)
                    this.setAttribute(PdfName.ENDINDENT, PdfNumber(paragraph.getIndentationRight()))
            }

            setTextAlignAttribute(paragraph.alignment)
        }
    }

    private fun writeAttributes(list: List?) {
        if (list != null) {
            this.setAttribute(PdfName.O, PdfName.LIST)
            if (list!!.isAutoindent()) {
                if (list!!.isNumbered()) {
                    if (list!!.isLettered()) {
                        if (list!!.isLowercase())
                            this.setAttribute(PdfName.LISTNUMBERING, PdfName.LOWERROMAN)
                        else
                            this.setAttribute(PdfName.LISTNUMBERING, PdfName.UPPERROMAN)
                    } else {
                        this.setAttribute(PdfName.LISTNUMBERING, PdfName.DECIMAL)
                    }
                } else if (list!!.isLettered()) {
                    if (list!!.isLowercase())
                        this.setAttribute(PdfName.LISTNUMBERING, PdfName.LOWERALPHA)
                    else
                        this.setAttribute(PdfName.LISTNUMBERING, PdfName.UPPERALPHA)
                }
            }
            var obj: PdfObject = getParentAttribute(parent, PdfName.STARTINDENT)
            if (obj is PdfNumber) {
                val startIndent = obj.floatValue()
                if (java.lang.Float.compare(startIndent, list!!.getIndentationLeft()) != 0)
                    this.setAttribute(PdfName.STARTINDENT, PdfNumber(list!!.getIndentationLeft()))
            } else {
                if (Math.abs(list!!.getIndentationLeft()) > java.lang.Float.MIN_VALUE)
                    this.setAttribute(PdfName.STARTINDENT, PdfNumber(list!!.getIndentationLeft()))
            }

            obj = getParentAttribute(parent, PdfName.ENDINDENT)
            if (obj is PdfNumber) {
                val endIndent = obj.floatValue()
                if (java.lang.Float.compare(endIndent, list!!.getIndentationRight()) != 0)
                    this.setAttribute(PdfName.ENDINDENT, PdfNumber(list!!.getIndentationRight()))
            } else {
                if (java.lang.Float.compare(list!!.getIndentationRight(), 0f) != 0)
                    this.setAttribute(PdfName.ENDINDENT, PdfNumber(list!!.getIndentationRight()))
            }
        }
    }

    private fun writeAttributes(listItem: ListItem?) {
        if (listItem != null) {
            var obj: PdfObject = getParentAttribute(parent, PdfName.STARTINDENT)
            if (obj is PdfNumber) {
                val startIndent = obj.floatValue()
                if (java.lang.Float.compare(startIndent, listItem.getIndentationLeft()) != 0)
                    this.setAttribute(PdfName.STARTINDENT, PdfNumber(listItem.getIndentationLeft()))
            } else {
                if (Math.abs(listItem.getIndentationLeft()) > java.lang.Float.MIN_VALUE)
                    this.setAttribute(PdfName.STARTINDENT, PdfNumber(listItem.getIndentationLeft()))
            }

            obj = getParentAttribute(parent, PdfName.ENDINDENT)
            if (obj is PdfNumber) {
                val endIndent = obj.floatValue()
                if (java.lang.Float.compare(endIndent, listItem.getIndentationRight()) != 0)
                    this.setAttribute(PdfName.ENDINDENT, PdfNumber(listItem.getIndentationRight()))
            } else {
                if (java.lang.Float.compare(listItem.getIndentationRight(), 0f) != 0)
                    this.setAttribute(PdfName.ENDINDENT, PdfNumber(listItem.getIndentationRight()))
            }
        }
    }

    private fun writeAttributes(listBody: ListBody?) {
        if (listBody != null) {

        }
    }

    private fun writeAttributes(listLabel: ListLabel?) {
        if (listLabel != null) {
            val obj = getParentAttribute(parent, PdfName.STARTINDENT)
            if (obj is PdfNumber) {
                val startIndent = obj.floatValue()
                if (java.lang.Float.compare(startIndent, listLabel.indentation) != 0)
                    this.setAttribute(PdfName.STARTINDENT, PdfNumber(listLabel.indentation))
            } else {
                if (Math.abs(listLabel.indentation) > java.lang.Float.MIN_VALUE)
                    this.setAttribute(PdfName.STARTINDENT, PdfNumber(listLabel.indentation))
            }
        }
    }

    private fun writeAttributes(table: PdfPTable?) {
        if (table != null) {
            this.setAttribute(PdfName.O, PdfName.TABLE)
            // Setting non-inheritable attributes
            if (java.lang.Float.compare(table.spacingBefore, 0f) != 0)
                this.setAttribute(PdfName.SPACEBEFORE, PdfNumber(table.spacingBefore))

            if (java.lang.Float.compare(table.spacingAfter, 0f) != 0)
                this.setAttribute(PdfName.SPACEAFTER, PdfNumber(table.spacingAfter))

            if (table.totalHeight > 0) {
                this.setAttribute(PdfName.HEIGHT, PdfNumber(table.totalHeight))
            }
            if (table.totalWidth > 0) {
                this.setAttribute(PdfName.WIDTH, PdfNumber(table.totalWidth))
            }
        }
    }

    private fun writeAttributes(row: PdfPRow?) {
        if (row != null) {
            this.setAttribute(PdfName.O, PdfName.TABLE)
        }
    }

    private fun writeAttributes(cell: PdfPCell?) {
        if (cell != null) {
            this.setAttribute(PdfName.O, PdfName.TABLE)
            if (cell.colspan != 1) {
                this.setAttribute(PdfName.COLSPAN, PdfNumber(cell.colspan))
            }
            if (cell.rowspan != 1) {
                this.setAttribute(PdfName.ROWSPAN, PdfNumber(cell.rowspan))
            }
            if (cell.headers != null) {
                val headers = PdfArray()
                val list = cell.headers
                for (header in list) {
                    if (header.name != null)
                        headers.add(PdfString(header.name))
                }
                if (!headers.isEmpty)
                    this.setAttribute(PdfName.HEADERS, headers)
            }

            if (cell.calculatedHeight > 0) {
                this.setAttribute(PdfName.HEIGHT, PdfNumber(cell.calculatedHeight))
            }

            if (cell.width > 0) {
                this.setAttribute(PdfName.WIDTH, PdfNumber(cell.width))
            }

            if (cell.getBackgroundColor() != null) {
                val color = cell.getBackgroundColor()
                this.setAttribute(PdfName.BACKGROUNDCOLOR, PdfArray(floatArrayOf(color.red / 255f, color.green / 255f, color.blue / 255f)))
            }
        }
    }

    private fun writeAttributes(headerCell: PdfPHeaderCell?) {
        if (headerCell != null) {
            if (headerCell.scope != PdfPHeaderCell.NONE) {
                when (headerCell.scope) {
                    PdfPHeaderCell.ROW -> this.setAttribute(PdfName.SCOPE, PdfName.ROW)
                    PdfPHeaderCell.COLUMN -> this.setAttribute(PdfName.SCOPE, PdfName.COLUMN)
                    PdfPHeaderCell.BOTH -> this.setAttribute(PdfName.SCOPE, PdfName.BOTH)
                }
            }
            if (headerCell.name != null)
                this.setAttribute(PdfName.NAME, PdfName(headerCell.name))
            writeAttributes(headerCell as PdfPCell?)
        }
    }

    private fun writeAttributes(header: PdfPTableHeader?) {
        if (header != null) {
            this.setAttribute(PdfName.O, PdfName.TABLE)
        }
    }

    private fun writeAttributes(body: PdfPTableBody?) {
        if (body != null) {

        }
    }

    private fun writeAttributes(footer: PdfPTableFooter?) {
        if (footer != null) {

        }
    }

    private fun writeAttributes(div: PdfDiv?) {
        if (div != null) {
            // Setting non-inheritable attributes
            if (div.backgroundColor != null)
                setColorAttribute(div.backgroundColor, null, PdfName.BACKGROUNDCOLOR)

            // Setting inheritable attributes
            setTextAlignAttribute(div.textAlignment)
        }
    }

    private fun writeAttributes(document: Document?) {
        if (document != null) {

        }
    }

    private fun colorsEqual(parentColor: PdfArray, color: FloatArray): Boolean {
        if (java.lang.Float.compare(color[0], parentColor.getAsNumber(0).floatValue()) != 0) {
            return false
        }
        if (java.lang.Float.compare(color[1], parentColor.getAsNumber(1).floatValue()) != 0) {
            return false
        }
        if (java.lang.Float.compare(color[2], parentColor.getAsNumber(2).floatValue()) != 0) {
            return false
        }
        return true
    }

    private fun setColorAttribute(newColor: BaseColor, oldColor: PdfObject?, attributeName: PdfName) {
        val colorArr = floatArrayOf(newColor.red / 255f, newColor.green / 255f, newColor.blue / 255f)
        if (oldColor != null && oldColor is PdfArray) {
            if (colorsEqual(oldColor, colorArr)) {
                this.setAttribute(attributeName, PdfArray(colorArr))
            } else
                this.setAttribute(attributeName, PdfArray(colorArr))
        } else
            this.setAttribute(attributeName, PdfArray(colorArr))
    }

    private fun setTextAlignAttribute(elementAlign: Int) {
        var align: PdfName? = null
        when (elementAlign) {
            Element.ALIGN_LEFT -> align = PdfName.START
            Element.ALIGN_CENTER -> align = PdfName.CENTER
            Element.ALIGN_RIGHT -> align = PdfName.END
            Element.ALIGN_JUSTIFIED -> align = PdfName.JUSTIFY
        }
        val obj = getParentAttribute(parent, PdfName.TEXTALIGN)
        if (obj is PdfName) {
            if (align != null && obj != align)
                this.setAttribute(PdfName.TEXTALIGN, align)
        } else {
            if (align != null && PdfName.START != align)
                this.setAttribute(PdfName.TEXTALIGN, align)
        }
    }

    @Throws(IOException::class)
    override fun toPdf(writer: PdfWriter, os: OutputStream) {
        PdfWriter.checkPdfIsoConformance(writer, PdfIsoKeys.PDFISOKEY_STRUCTELEM, this)
        super.toPdf(writer, os)
    }

    private fun getParentAttribute(parent: IPdfStructureElement?, name: PdfName): PdfObject? {
        if (parent == null)
            return null
        return parent.getAttribute(name)
    }

    protected fun setStructureTreeRoot(root: PdfStructureTreeRoot) {
        this.top = root
    }

    protected fun setStructureElementParent(parent: PdfStructureElement) {
        this.parent = parent
    }
}
/**
 * Gets the parent of this node.
 * @return the parent of this node
 */
