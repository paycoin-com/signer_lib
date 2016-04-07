/*
 * $Id: a1d6a387ca4f8b8233a6294f92883d4d71ba1d8b $
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

/**
 * A signature field lock dictionary.
 */
class PdfSigLockDictionary : PdfDictionary {

    /**
     * Enumerates the different actions of a signature lock.
     * Indicates the set of fields that should be locked:
     * all the fields in the document,
     * all the fields specified in the /Fields array
     * all the fields except those specified in the /Fields array
     */
    enum class LockAction private constructor(val value: PdfName) {
        ALL(PdfName.ALL), INCLUDE(PdfName.INCLUDE), EXCLUDE(PdfName.EXCLUDE)
    }

    /**
     * Enumerates the different levels of permissions.
     */
    enum class LockPermissions private constructor(p: Int) {
        NO_CHANGES_ALLOWED(1), FORM_FILLING(2), FORM_FILLING_AND_ANNOTATION(3);

        val value: PdfNumber

        init {
            value = PdfNumber(p)
        }
    }

    /**
     * Creates a signature lock valid for all fields in the document.
     */
    constructor() : super(PdfName.SIGFIELDLOCK) {
        this.put(PdfName.ACTION, LockAction.ALL.value)
    }

    /**
     * Creates a signature lock for all fields in the document,
     * setting specific permissions.
     */
    constructor(p: LockPermissions) : this() {
        this.put(PdfName.P, p.value)
    }

    /**
     * Creates a signature lock for specific fields in the document.
     */
    constructor(action: LockAction, vararg fields: String) : this(action, null, *fields) {
    }

    /**
     * Creates a signature lock for specific fields in the document.
     */
    constructor(action: LockAction, p: LockPermissions?, vararg fields: String) : super(PdfName.SIGFIELDLOCK) {
        this.put(PdfName.ACTION, action.value)
        if (p != null)
            this.put(PdfName.P, p.value)
        val fieldsArray = PdfArray()
        for (field in fields) {
            fieldsArray.add(PdfString(field))
        }
        this.put(PdfName.FIELDS, fieldsArray)
    }
}
