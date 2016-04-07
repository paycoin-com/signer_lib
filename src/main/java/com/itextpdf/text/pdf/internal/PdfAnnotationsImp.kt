/*
 * $Id: a06ab7e1e15b535ec64612fb400a7ad46fa48b88 $
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
package com.itextpdf.text.pdf.internal

import java.io.IOException
import java.net.URL
import java.util.ArrayList
import java.util.HashSet

import com.itextpdf.text.Annotation
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfAcroForm
import com.itextpdf.text.pdf.PdfAction
import com.itextpdf.text.pdf.PdfAnnotation
import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfFileSpecification
import com.itextpdf.text.pdf.PdfFormField
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.PdfRectangle
import com.itextpdf.text.pdf.PdfString
import com.itextpdf.text.pdf.PdfTemplate
import com.itextpdf.text.pdf.PdfWriter

class PdfAnnotationsImp(writer: PdfWriter) {

    /**
     * This is the AcroForm object for the complete document.
     */
    /**
     * Gets the AcroForm object.
     * @return the PdfAcroform object of the PdfDocument
     */
    var acroForm: PdfAcroForm
        protected set

    /**
     * This is the array containing the references to annotations
     * that were added to the document.
     */
    protected var annotations = ArrayList<PdfAnnotation>()

    /**
     * This is an array containing references to some delayed annotations
     * (that were added for a page that doesn't exist yet).
     */
    protected var delayedAnnotations = ArrayList<PdfAnnotation>()


    init {
        acroForm = PdfAcroForm(writer)
    }

    /**
     * Checks if the AcroForm is valid.
     */
    fun hasValidAcroForm(): Boolean {
        return acroForm.isValid
    }

    fun setSigFlags(f: Int) {
        acroForm.setSigFlags(f)
    }

    fun addCalculationOrder(formField: PdfFormField) {
        acroForm.addCalculationOrder(formField)
    }

    fun addAnnotation(annot: PdfAnnotation) {
        if (annot.isForm) {
            val field = annot as PdfFormField
            if (field.parent == null)
                addFormFieldRaw(field)
        } else
            annotations.add(annot)
    }

    fun addPlainAnnotation(annot: PdfAnnotation) {
        annotations.add(annot)
    }

    internal fun addFormFieldRaw(field: PdfFormField) {
        annotations.add(field)
        val kids = field.kids
        if (kids != null) {
            for (k in kids.indices) {
                val kid = kids[k]
                if (!kid.isUsed)
                    addFormFieldRaw(kid)
            }
        }
    }

    fun hasUnusedAnnotations(): Boolean {
        return !annotations.isEmpty()
    }

    fun resetAnnotations() {
        annotations = delayedAnnotations
        delayedAnnotations = ArrayList<PdfAnnotation>()
    }

    fun rotateAnnotations(writer: PdfWriter, pageSize: Rectangle): PdfArray {
        val array = PdfArray()
        val rotation = pageSize.rotation % 360
        val currentPage = writer.currentPageNumber
        for (k in annotations.indices) {
            val dic = annotations[k]
            val page = dic.placeInPage
            if (page > currentPage) {
                delayedAnnotations.add(dic)
                continue
            }
            if (dic.isForm) {
                if (!dic.isUsed) {
                    val templates = dic.templates
                    if (templates != null)
                        acroForm.addFieldTemplates(templates)
                }
                val field = dic as PdfFormField
                if (field.parent == null)
                    acroForm.addDocumentField(field.indirectReference)
            }
            if (dic.isAnnotation) {
                array.add(dic.indirectReference)
                if (!dic.isUsed) {
                    val tmp = dic.getAsArray(PdfName.RECT)
                    val rect: PdfRectangle
                    if (tmp.size() == 4) {
                        rect = PdfRectangle(tmp.getAsNumber(0).floatValue(), tmp.getAsNumber(1).floatValue(), tmp.getAsNumber(2).floatValue(), tmp.getAsNumber(3).floatValue())
                    } else {
                        rect = PdfRectangle(tmp.getAsNumber(0).floatValue(), tmp.getAsNumber(1).floatValue())
                    }
                    when (rotation) {
                        90 -> dic.put(PdfName.RECT, PdfRectangle(
                                pageSize.top - rect.bottom(),
                                rect.left(),
                                pageSize.top - rect.top(),
                                rect.right()))
                        180 -> dic.put(PdfName.RECT, PdfRectangle(
                                pageSize.right - rect.left(),
                                pageSize.top - rect.bottom(),
                                pageSize.right - rect.right(),
                                pageSize.top - rect.top()))
                        270 -> dic.put(PdfName.RECT, PdfRectangle(
                                rect.bottom(),
                                pageSize.right - rect.left(),
                                rect.top(),
                                pageSize.right - rect.right()))
                    }
                }
            }
            if (!dic.isUsed) {
                dic.setUsed()
                try {
                    writer.addToBody(dic, dic.indirectReference)
                } catch (e: IOException) {
                    throw ExceptionConverter(e)
                }

            }
        }
        return array
    }

    companion object {

        @Throws(IOException::class)
        fun convertAnnotation(writer: PdfWriter, annot: Annotation, defaultRect: Rectangle): PdfAnnotation {
            when (annot.annotationType()) {
                Annotation.URL_NET -> return writer.createAnnotation(annot.llx(), annot.lly(), annot.urx(), annot.ury(), PdfAction(annot.attributes()[Annotation.URL] as URL), null)
                Annotation.URL_AS_STRING -> return writer.createAnnotation(annot.llx(), annot.lly(), annot.urx(), annot.ury(), PdfAction(annot.attributes()[Annotation.FILE] as String), null)
                Annotation.FILE_DEST -> return writer.createAnnotation(annot.llx(), annot.lly(), annot.urx(), annot.ury(), PdfAction(annot.attributes()[Annotation.FILE] as String, annot.attributes()[Annotation.DESTINATION] as String), null)
                Annotation.SCREEN -> {
                    val sparams = annot.attributes()[Annotation.PARAMETERS] as BooleanArray
                    val fname = annot.attributes()[Annotation.FILE] as String
                    val mimetype = annot.attributes()[Annotation.MIMETYPE] as String
                    val fs: PdfFileSpecification
                    if (sparams[0])
                        fs = PdfFileSpecification.fileEmbedded(writer, fname, fname, null)
                    else
                        fs = PdfFileSpecification.fileExtern(writer, fname)
                    val ann = PdfAnnotation.createScreen(writer, Rectangle(annot.llx(), annot.lly(), annot.urx(), annot.ury()),
                            fname, fs, mimetype, sparams[1])
                    return ann
                }
                Annotation.FILE_PAGE -> return writer.createAnnotation(annot.llx(), annot.lly(), annot.urx(), annot.ury(), PdfAction(annot.attributes()[Annotation.FILE] as String, (annot.attributes()[Annotation.PAGE] as Int).toInt()), null)
                Annotation.NAMED_DEST -> return writer.createAnnotation(annot.llx(), annot.lly(), annot.urx(), annot.ury(), PdfAction((annot.attributes()[Annotation.NAMED] as Int).toInt()), null)
                Annotation.LAUNCH -> return writer.createAnnotation(annot.llx(), annot.lly(), annot.urx(), annot.ury(), PdfAction(annot.attributes()[Annotation.APPLICATION] as String, annot.attributes()[Annotation.PARAMETERS] as String, annot.attributes()[Annotation.OPERATION] as String, annot.attributes()[Annotation.DEFAULTDIR] as String), null)
                else -> return writer.createAnnotation(defaultRect.left, defaultRect.bottom, defaultRect.right, defaultRect.top, PdfString(annot.title(), PdfObject.TEXT_UNICODE), PdfString(annot.content(), PdfObject.TEXT_UNICODE), null)
            }
        }
    }
}
