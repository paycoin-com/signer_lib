/*
 * $Id: 4e9c7bddfe561350c24cc605b3a4f4ee8778b4cc $
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
import java.net.URL
import java.util.ArrayList

import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.collection.PdfTargetDictionary
import com.itextpdf.text.pdf.internal.PdfIsoKeys

/**
 * A PdfAction defines an action that can be triggered from a PDF file.

 * @see PdfDictionary
 */

class PdfAction : PdfDictionary {

    // constructors

    /** Create an empty action.
     */
    constructor() {
    }

    /**
     * Constructs a new PdfAction of Subtype URI.

     * @param url the Url to go to
     */

    constructor(url: URL) : this(url.toExternalForm()) {
    }

    /**
     * Construct a new PdfAction of Subtype URI that accepts the x and y coordinate of the position that was clicked.
     * @param url
     * *
     * @param isMap
     */
    constructor(url: URL, isMap: Boolean) : this(url.toExternalForm(), isMap) {
    }

    /**
     * Construct a new PdfAction of Subtype URI that accepts the x and y coordinate of the position that was clicked.
     * @param url
     * *
     * @param isMap
     */

    @JvmOverloads constructor(url: String, isMap: Boolean = false) {
        put(PdfName.S, PdfName.URI)
        put(PdfName.URI, PdfString(url))
        if (isMap)
            put(PdfName.ISMAP, PdfBoolean.PDFTRUE)
    }

    /**
     * Constructs a new PdfAction of Subtype GoTo.
     * @param destination the destination to go to
     */

    internal constructor(destination: PdfIndirectReference) {
        put(PdfName.S, PdfName.GOTO)
        put(PdfName.D, destination)
    }

    /**
     * Constructs a new PdfAction of Subtype GoToR.
     * @param filename the file name to go to
     * *
     * @param name the named destination to go to
     */

    constructor(filename: String, name: String) {
        put(PdfName.S, PdfName.GOTOR)
        put(PdfName.F, PdfString(filename))
        put(PdfName.D, PdfString(name))
    }

    /**
     * Constructs a new PdfAction of Subtype GoToR.
     * @param filename the file name to go to
     * *
     * @param page the page destination to go to
     */

    constructor(filename: String, page: Int) {
        put(PdfName.S, PdfName.GOTOR)
        put(PdfName.F, PdfString(filename))
        put(PdfName.D, PdfLiteral("[" + (page - 1) + " /FitH 10000]"))
    }

    /** Implements name actions. The action can be FIRSTPAGE, LASTPAGE,
     * NEXTPAGE, PREVPAGE and PRINTDIALOG.
     * @param named the named action
     */
    constructor(named: Int) {
        put(PdfName.S, PdfName.NAMED)
        when (named) {
            FIRSTPAGE -> put(PdfName.N, PdfName.FIRSTPAGE)
            LASTPAGE -> put(PdfName.N, PdfName.LASTPAGE)
            NEXTPAGE -> put(PdfName.N, PdfName.NEXTPAGE)
            PREVPAGE -> put(PdfName.N, PdfName.PREVPAGE)
            PRINTDIALOG -> {
                put(PdfName.S, PdfName.JAVASCRIPT)
                put(PdfName.JS, PdfString("this.print(true);\r"))
            }
            else -> throw RuntimeException(MessageLocalization.getComposedMessage("invalid.named.action"))
        }
    }

    /** Launches an application or a document.
     * @param application the application to be launched or the document to be opened or printed.
     * *
     * @param parameters (Windows-specific) A parameter string to be passed to the application.
     * * It can be null.
     * *
     * @param operation (Windows-specific) the operation to perform: "open" - Open a document,
     * * "print" - Print a document.
     * * It can be null.
     * *
     * @param defaultDir (Windows-specific) the default directory in standard DOS syntax.
     * * It can be null.
     */
    constructor(application: String, parameters: String?, operation: String?, defaultDir: String?) {
        put(PdfName.S, PdfName.LAUNCH)
        if (parameters == null && operation == null && defaultDir == null)
            put(PdfName.F, PdfString(application))
        else {
            val dic = PdfDictionary()
            dic.put(PdfName.F, PdfString(application))
            if (parameters != null)
                dic.put(PdfName.P, PdfString(parameters))
            if (operation != null)
                dic.put(PdfName.O, PdfString(operation))
            if (defaultDir != null)
                dic.put(PdfName.D, PdfString(defaultDir))
            put(PdfName.WIN, dic)
        }
    }

    /** Add a chained action.
     * @param na the next action
     */
    fun next(na: PdfAction) {
        val nextAction = get(PdfName.NEXT)
        if (nextAction == null)
            put(PdfName.NEXT, na)
        else if (nextAction.isDictionary) {
            val array = PdfArray(nextAction)
            array.add(na)
            put(PdfName.NEXT, array)
        } else {
            (nextAction as PdfArray).add(na)
        }
    }

    @Throws(IOException::class)
    override fun toPdf(writer: PdfWriter, os: OutputStream) {
        PdfWriter.checkPdfIsoConformance(writer, PdfIsoKeys.PDFISOKEY_ACTION, this)
        super.toPdf(writer, os)
    }

    companion object {

        /** A named action to go to the first page.
         */
        val FIRSTPAGE = 1
        /** A named action to go to the previous page.
         */
        val PREVPAGE = 2
        /** A named action to go to the next page.
         */
        val NEXTPAGE = 3
        /** A named action to go to the last page.
         */
        val LASTPAGE = 4

        /** A named action to open a print dialog.
         */
        val PRINTDIALOG = 5

        /** a possible submitvalue  */
        val SUBMIT_EXCLUDE = 1
        /** a possible submitvalue  */
        val SUBMIT_INCLUDE_NO_VALUE_FIELDS = 2
        /** a possible submitvalue  */
        val SUBMIT_HTML_FORMAT = 4
        /** a possible submitvalue  */
        val SUBMIT_HTML_GET = 8
        /** a possible submitvalue  */
        val SUBMIT_COORDINATES = 16
        /** a possible submitvalue  */
        val SUBMIT_XFDF = 32
        /** a possible submitvalue  */
        val SUBMIT_INCLUDE_APPEND_SAVES = 64
        /** a possible submitvalue  */
        val SUBMIT_INCLUDE_ANNOTATIONS = 128
        /** a possible submitvalue  */
        val SUBMIT_PDF = 256
        /** a possible submitvalue  */
        val SUBMIT_CANONICAL_FORMAT = 512
        /** a possible submitvalue  */
        val SUBMIT_EXCL_NON_USER_ANNOTS = 1024
        /** a possible submitvalue  */
        val SUBMIT_EXCL_F_KEY = 2048
        /** a possible submitvalue  */
        val SUBMIT_EMBED_FORM = 8196
        /** a possible submitvalue  */
        val RESET_EXCLUDE = 1

        /** Launches an application or a document.
         * @param application the application to be launched or the document to be opened or printed.
         * *
         * @param parameters (Windows-specific) A parameter string to be passed to the application.
         * * It can be null.
         * *
         * @param operation (Windows-specific) the operation to perform: "open" - Open a document,
         * * "print" - Print a document.
         * * It can be null.
         * *
         * @param defaultDir (Windows-specific) the default directory in standard DOS syntax.
         * * It can be null.
         * *
         * @return a Launch action
         */
        fun createLaunch(application: String, parameters: String, operation: String, defaultDir: String): PdfAction {
            return PdfAction(application, parameters, operation, defaultDir)
        }

        /**Creates a Rendition action
         * @param file
         * *
         * @param fs
         * *
         * @param mimeType
         * *
         * @param ref
         * *
         * @return a Media Clip action
         * *
         * @throws IOException
         */
        @Throws(IOException::class)
        fun rendition(file: String, fs: PdfFileSpecification, mimeType: String, ref: PdfIndirectReference): PdfAction {
            val js = PdfAction()
            js.put(PdfName.S, PdfName.RENDITION)
            js.put(PdfName.R, PdfRendition(file, fs, mimeType))
            js.put(PdfName("OP"), PdfNumber(0))
            js.put(PdfName("AN"), ref)
            return js
        }

        /** Creates a JavaScript action. If the JavaScript is smaller than
         * 50 characters it will be placed as a string, otherwise it will
         * be placed as a compressed stream.
         * @param code the JavaScript code
         * *
         * @param writer the writer for this action
         * *
         * @param unicode select JavaScript unicode. Note that the internal
         * * Acrobat JavaScript engine does not support unicode,
         * * so this may or may not work for you
         * *
         * @return the JavaScript action
         */
        @JvmOverloads fun javaScript(code: String, writer: PdfWriter, unicode: Boolean = false): PdfAction {
            val js = PdfAction()
            js.put(PdfName.S, PdfName.JAVASCRIPT)
            if (unicode && code.length < 50) {
                js.put(PdfName.JS, PdfString(code, PdfObject.TEXT_UNICODE))
            } else if (!unicode && code.length < 100) {
                js.put(PdfName.JS, PdfString(code))
            } else {
                try {
                    val b = PdfEncodings.convertToBytes(code, if (unicode) PdfObject.TEXT_UNICODE else PdfObject.TEXT_PDFDOCENCODING)
                    val stream = PdfStream(b)
                    stream.flateCompress(writer.compressionLevel)
                    js.put(PdfName.JS, writer.addToBody(stream).indirectReference)
                } catch (e: Exception) {
                    js.put(PdfName.JS, PdfString(code))
                }

            }
            return js
        }

        /**
         * A Hide action hides or shows an object.
         * @param obj object to hide or show
         * *
         * @param hide true is hide, false is show
         * *
         * @return a Hide Action
         */
        internal fun createHide(obj: PdfObject, hide: Boolean): PdfAction {
            val action = PdfAction()
            action.put(PdfName.S, PdfName.HIDE)
            action.put(PdfName.T, obj)
            if (!hide)
                action.put(PdfName.H, PdfBoolean.PDFFALSE)
            return action
        }

        /**
         * A Hide action hides or shows an annotation.
         * @param annot
         * *
         * @param hide
         * *
         * @return A Hide Action
         */
        fun createHide(annot: PdfAnnotation, hide: Boolean): PdfAction {
            return createHide(annot.indirectReference, hide)
        }

        /**
         * A Hide action hides or shows an annotation.
         * @param name
         * *
         * @param hide
         * *
         * @return A Hide Action
         */
        fun createHide(name: String, hide: Boolean): PdfAction {
            return createHide(PdfString(name), hide)
        }

        internal fun buildArray(names: Array<Any>): PdfArray {
            val array = PdfArray()
            for (k in names.indices) {
                val obj = names[k]
                if (obj is String)
                    array.add(PdfString(obj))
                else if (obj is PdfAnnotation)
                    array.add(obj.indirectReference)
                else
                    throw RuntimeException(MessageLocalization.getComposedMessage("the.array.must.contain.string.or.pdfannotation"))
            }
            return array
        }

        /**
         * A Hide action hides or shows objects.
         * @param names
         * *
         * @param hide
         * *
         * @return A Hide Action
         */
        fun createHide(names: Array<Any>, hide: Boolean): PdfAction {
            return createHide(buildArray(names), hide)
        }

        /**
         * Creates a submit form.
         * @param file    the URI to submit the form to
         * *
         * @param names    the objects to submit
         * *
         * @param flags    submit properties
         * *
         * @return A PdfAction
         */
        fun createSubmitForm(file: String, names: Array<Any>?, flags: Int): PdfAction {
            val action = PdfAction()
            action.put(PdfName.S, PdfName.SUBMITFORM)
            val dic = PdfDictionary()
            dic.put(PdfName.F, PdfString(file))
            dic.put(PdfName.FS, PdfName.URL)
            action.put(PdfName.F, dic)
            if (names != null)
                action.put(PdfName.FIELDS, buildArray(names))
            action.put(PdfName.FLAGS, PdfNumber(flags))
            return action
        }

        /**
         * Creates a resetform.
         * @param names    the objects to reset
         * *
         * @param flags    submit properties
         * *
         * @return A PdfAction
         */
        fun createResetForm(names: Array<Any>?, flags: Int): PdfAction {
            val action = PdfAction()
            action.put(PdfName.S, PdfName.RESETFORM)
            if (names != null)
                action.put(PdfName.FIELDS, buildArray(names))
            action.put(PdfName.FLAGS, PdfNumber(flags))
            return action
        }

        /**
         * Creates an Import field.
         * @param file
         * *
         * @return A PdfAction
         */
        fun createImportData(file: String): PdfAction {
            val action = PdfAction()
            action.put(PdfName.S, PdfName.IMPORTDATA)
            action.put(PdfName.F, PdfString(file))
            return action
        }

        /** Creates a GoTo action to an internal page.
         * @param page the page to go. First page is 1
         * *
         * @param dest the destination for the page
         * *
         * @param writer the writer for this action
         * *
         * @return a GoTo action
         */
        fun gotoLocalPage(page: Int, dest: PdfDestination, writer: PdfWriter): PdfAction {
            val ref = writer.getPageReference(page)
            val d = PdfDestination(dest)
            d.addPage(ref)
            val action = PdfAction()
            action.put(PdfName.S, PdfName.GOTO)
            action.put(PdfName.D, d)
            return action
        }

        /**
         * Creates a GoTo action to a named destination.
         * @param dest the named destination
         * *
         * @param isName if true sets the destination as a name, if false sets it as a String
         * *
         * @return a GoTo action
         */
        fun gotoLocalPage(dest: String, isName: Boolean): PdfAction {
            val action = PdfAction()
            action.put(PdfName.S, PdfName.GOTO)
            if (isName)
                action.put(PdfName.D, PdfName(dest))
            else
                action.put(PdfName.D, PdfString(dest, PdfObject.TEXT_UNICODE))
            return action
        }

        /**
         * Creates a GoToR action to a named destination.
         * @param filename the file name to go to
         * *
         * @param dest the destination name
         * *
         * @param isName if true sets the destination as a name, if false sets it as a String
         * *
         * @param newWindow open the document in a new window if true, if false the current document is replaced by the new document.
         * *
         * @return a GoToR action
         */
        fun gotoRemotePage(filename: String, dest: String, isName: Boolean, newWindow: Boolean): PdfAction {
            val action = PdfAction()
            action.put(PdfName.F, PdfString(filename))
            action.put(PdfName.S, PdfName.GOTOR)
            if (isName)
                action.put(PdfName.D, PdfName(dest))
            else
                action.put(PdfName.D, PdfString(dest, PdfObject.TEXT_UNICODE))
            if (newWindow)
                action.put(PdfName.NEWWINDOW, PdfBoolean.PDFTRUE)
            return action
        }

        /**
         * Creates a GoToE action to an embedded file.
         * @param filename    the root document of the target (null if the target is in the same document)
         * *
         * @param dest the named destination
         * *
         * @param isName if true sets the destination as a name, if false sets it as a String
         * *
         * @return a GoToE action
         */
        fun gotoEmbedded(filename: String, target: PdfTargetDictionary, dest: String, isName: Boolean, newWindow: Boolean): PdfAction {
            if (isName)
                return gotoEmbedded(filename, target, PdfName(dest), newWindow)
            else
                return gotoEmbedded(filename, target, PdfString(dest, PdfObject.TEXT_UNICODE), newWindow)
        }

        /**
         * Creates a GoToE action to an embedded file.
         * @param filename    the root document of the target (null if the target is in the same document)
         * *
         * @param target    a path to the target document of this action
         * *
         * @param dest        the destination inside the target document, can be of type PdfDestination, PdfName, or PdfString
         * *
         * @param newWindow    if true, the destination document should be opened in a new window
         * *
         * @return a GoToE action
         */
        fun gotoEmbedded(filename: String?, target: PdfTargetDictionary, dest: PdfObject, newWindow: Boolean): PdfAction {
            val action = PdfAction()
            action.put(PdfName.S, PdfName.GOTOE)
            action.put(PdfName.T, target)
            action.put(PdfName.D, dest)
            action.put(PdfName.NEWWINDOW, PdfBoolean(newWindow))
            if (filename != null) {
                action.put(PdfName.F, PdfString(filename))
            }
            return action
        }

        /**
         * A set-OCG-state action (PDF 1.5) sets the state of one or more optional content
         * groups.
         * @param state an array consisting of any number of sequences beginning with a PdfName
         * * or String (ON, OFF, or Toggle) followed by one or more optional content group dictionaries
         * * PdfLayer or a PdfIndirectReference to a PdfLayer.
         * * The array elements are processed from left to right; each name is applied
         * * to the subsequent groups until the next name is encountered:
         * *
         * *  * ON sets the state of subsequent groups to ON
         * *  * OFF sets the state of subsequent groups to OFF
         * *  * Toggle reverses the state of subsequent groups
         * *
         * *
         * @param preserveRB if true, indicates that radio-button state relationships between optional
         * * content groups (as specified by the RBGroups entry in the current configuration
         * * dictionary) should be preserved when the states in the
         * * state array are applied. That is, if a group is set to ON (either by ON or Toggle) during
         * * processing of the state array, any other groups belong to the same radio-button
         * * group are turned OFF. If a group is set to OFF, there is no effect on other groups.
         * * If false, radio-button state relationships, if any, are ignored
         * *
         * @return the action
         */
        fun setOCGstate(state: ArrayList<Any>, preserveRB: Boolean): PdfAction {
            val action = PdfAction()
            action.put(PdfName.S, PdfName.SETOCGSTATE)
            val a = PdfArray()
            for (k in state.indices) {
                val o = state[k] ?: continue
                if (o is PdfIndirectReference)
                    a.add(o as PdfIndirectReference?)
                else if (o is PdfLayer)
                    a.add(o.ref)
                else if (o is PdfName)
                    a.add(o as PdfName?)
                else if (o is String) {
                    var name: PdfName? = null
                    if (o.equals("on", ignoreCase = true))
                        name = PdfName.ON
                    else if (o.equals("off", ignoreCase = true))
                        name = PdfName.OFF
                    else if (o.equals("toggle", ignoreCase = true))
                        name = PdfName.TOGGLE
                    else
                        throw IllegalArgumentException(MessageLocalization.getComposedMessage("a.string.1.was.passed.in.state.only.on.off.and.toggle.are.allowed", o))
                    a.add(name)
                } else
                    throw IllegalArgumentException(MessageLocalization.getComposedMessage("invalid.type.was.passed.in.state.1", o.javaClass.name))
            }
            action.put(PdfName.STATE, a)
            if (!preserveRB)
                action.put(PdfName.PRESERVERB, PdfBoolean.PDFFALSE)
            return action
        }
    }

}
/**
 * Constructs a new PdfAction of Subtype URI.

 * @param url the url to go to
 */
/** Creates a JavaScript action. If the JavaScript is smaller than
 * 50 characters it will be place as a string, otherwise it will
 * be placed as a compressed stream.
 * @param code the JavaScript code
 * *
 * @param writer the writer for this action
 * *
 * @return the JavaScript action
 */
