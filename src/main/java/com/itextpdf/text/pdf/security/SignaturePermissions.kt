/*
 * $Id: e75cc2282255fd07cfb2d8f9410f1231e594ed7b $
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
package com.itextpdf.text.pdf.security

import java.util.ArrayList

import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfDictionary
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfNumber

/**
 * A helper class that tells you more about the type of signature
 * (certification or approval) and the signature's DMP settings.
 */
class SignaturePermissions
/**
 * Creates an object that can inform you about the type of signature
 * in a signature dictionary as well as some of the permissions
 * defined by the signature.
 */
(sigDict: PdfDictionary, previous: SignaturePermissions?) {

    /**
     * Class that contains a field lock action and
     * an array of the fields that are involved.
     */
    inner class FieldLock
    /** Creates a FieldLock instance  */
    (action: PdfName, fields: PdfArray) {
        /** Can be /All, /Exclude or /Include  */
        /** Getter for the field lock action.  */
        var action: PdfName
            internal set
        /** An array of PdfString values with fieldnames  */
        /** Getter for the fields involved in the lock action.  */
        var fields: PdfArray? = null
            internal set

        init {
            this.action = action
            this.fields = fields
        }

        /** toString method  */
        override fun toString(): String {
            return action.toString() + if (fields == null) "" else fields!!.toString()
        }
    }

    /** Is the signature a cerification signature (true) or an approval signature (false)?  */
    /**
     * Getter to find out if the signature is a certification signature.
     * @return true if the signature is a certification signature, false for an approval signature.
     */
    var isCertification = false
        internal set
    /** Is form filling allowed by this signature?  */
    /**
     * Getter to find out if filling out fields is allowed after signing.
     * @return true if filling out fields is allowed
     */
    var isFillInAllowed = true
        internal set
    /** Is adding annotations allowed by this signature?  */
    /**
     * Getter to find out if adding annotations is allowed after signing.
     * @return true if adding annotations is allowed
     */
    var isAnnotationsAllowed = true
        internal set
    /** Does this signature lock specific fields?  */
    internal var fieldLocks: MutableList<FieldLock> = ArrayList()

    init {
        if (previous != null) {
            isAnnotationsAllowed = isAnnotationsAllowed and previous.isAnnotationsAllowed
            isFillInAllowed = isFillInAllowed and previous.isFillInAllowed
            fieldLocks.addAll(previous.getFieldLocks())
        }
        val ref = sigDict.getAsArray(PdfName.REFERENCE)
        if (ref != null) {
            for (i in 0..ref.size() - 1) {
                val dict = ref.getAsDict(i)
                val params = dict.getAsDict(PdfName.TRANSFORMPARAMS)
                if (PdfName.DOCMDP == dict.getAsName(PdfName.TRANSFORMMETHOD)) {
                    isCertification = true
                }
                val action = params.getAsName(PdfName.ACTION)
                if (action != null) {
                    fieldLocks.add(FieldLock(action, params.getAsArray(PdfName.FIELDS)))
                }
                val p = params.getAsNumber(PdfName.P) ?: continue
                when (p.intValue()) {
                    else -> {
                    }
                    1 -> {
                        isFillInAllowed = isFillInAllowed and false
                        isAnnotationsAllowed = isAnnotationsAllowed and false
                    }
                    2 -> isAnnotationsAllowed = isAnnotationsAllowed and false
                }
            }
        }
    }

    /**
     * Getter for the field lock actions, and fields that are impacted by the action
     * @return an Array with field names
     */
    fun getFieldLocks(): List<FieldLock> {
        return fieldLocks
    }
}
