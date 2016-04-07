/*
 * $Id: a834ca3181d1476558a14a21035269cda6b7ccc2 $
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

import java.io.ByteArrayInputStream
import java.io.IOException
import java.security.cert.X509Certificate
import java.util.ArrayList
import java.util.Enumeration
import java.util.HashMap

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.ASN1TaggedObject

import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.error_messages.MessageLocalization

/**
 * Class containing static methods that allow you to get information from
 * an X509 Certificate: the issuer and the subject.
 */
object CertificateInfo {

    // Inner classes

    /**
     * a class that holds an X509 name
     */
    class X500Name {

        /** A HashMap with values  */
        var values: MutableMap<String, ArrayList<String>> = HashMap()

        /**
         * Constructs an X509 name
         * @param seq an ASN1 Sequence
         */
        constructor(seq: ASN1Sequence) {
            @SuppressWarnings("unchecked")
            val e = seq.objects

            while (e.hasMoreElements()) {
                val set = e.nextElement()

                for (i in 0..set.size() - 1) {
                    val s = set.getObjectAt(i) as ASN1Sequence
                    val id = DefaultSymbols[s.getObjectAt(0)] ?: continue
                    var vs: ArrayList<String>? = values[id]
                    if (vs == null) {
                        vs = ArrayList<String>()
                        values.put(id, vs)
                    }
                    vs.add((s.getObjectAt(1) as ASN1String).string)
                }
            }
        }

        /**
         * Constructs an X509 name
         * @param dirName a directory name
         */
        constructor(dirName: String) {
            val nTok = CertificateInfo.X509NameTokenizer(dirName)

            while (nTok.hasMoreTokens()) {
                val token = nTok.nextToken()
                val index = token.indexOf('=')

                if (index == -1) {
                    throw IllegalArgumentException(MessageLocalization.getComposedMessage("badly.formated.directory.string"))
                }

                val id = token.substring(0, index).toUpperCase()
                val value = token.substring(index + 1)
                var vs: ArrayList<String>? = values[id]
                if (vs == null) {
                    vs = ArrayList<String>()
                    values.put(id, vs)
                }
                vs.add(value)
            }

        }

        /**
         * Gets the first entry from the field array retrieved from the values Map.
         * @param    name    the field name
         * *
         * @return    the (first) field value
         */
        fun getField(name: String): String? {
            val vs = values[name]
            return if (vs == null) null else vs[0]
        }

        /**
         * Gets a field array from the values Map
         * @param name
         * *
         * @return an ArrayList
         */
        fun getFieldArray(name: String): List<String> {
            return values[name]
        }

        /**
         * Getter for values
         * @return a Map with the fields of the X509 name
         */
        val fields: Map<String, ArrayList<String>>
            get() = values

        /**
         * @see java.lang.Object.toString
         */
        override fun toString(): String {
            return values.toString()
        }

        companion object {
            /** country code - StringType(SIZE(2))  */
            val C = ASN1ObjectIdentifier("2.5.4.6")

            /** organization - StringType(SIZE(1..64))  */
            val O = ASN1ObjectIdentifier("2.5.4.10")

            /** organizational unit name - StringType(SIZE(1..64))  */
            val OU = ASN1ObjectIdentifier("2.5.4.11")

            /** Title  */
            val T = ASN1ObjectIdentifier("2.5.4.12")

            /** common name - StringType(SIZE(1..64))  */
            val CN = ASN1ObjectIdentifier("2.5.4.3")

            /** device serial number name - StringType(SIZE(1..64))  */
            val SN = ASN1ObjectIdentifier("2.5.4.5")

            /** locality name - StringType(SIZE(1..64))  */
            val L = ASN1ObjectIdentifier("2.5.4.7")

            /** state, or province name - StringType(SIZE(1..64))  */
            val ST = ASN1ObjectIdentifier("2.5.4.8")

            /** Naming attribute of type X520name  */
            val SURNAME = ASN1ObjectIdentifier("2.5.4.4")

            /** Naming attribute of type X520name  */
            val GIVENNAME = ASN1ObjectIdentifier("2.5.4.42")

            /** Naming attribute of type X520name  */
            val INITIALS = ASN1ObjectIdentifier("2.5.4.43")

            /** Naming attribute of type X520name  */
            val GENERATION = ASN1ObjectIdentifier("2.5.4.44")

            /** Naming attribute of type X520name  */
            val UNIQUE_IDENTIFIER = ASN1ObjectIdentifier("2.5.4.45")

            /**
             * Email address (RSA PKCS#9 extension) - IA5String.
             *
             * Note: if you're trying to be ultra orthodox, don't use this! It shouldn't be in here.
             */
            val EmailAddress = ASN1ObjectIdentifier("1.2.840.113549.1.9.1")

            /**
             * email address in Verisign certificates
             */
            val E = EmailAddress

            /** object identifier  */
            val DC = ASN1ObjectIdentifier("0.9.2342.19200300.100.1.25")

            /** LDAP User id.  */
            val UID = ASN1ObjectIdentifier("0.9.2342.19200300.100.1.1")

            /** A Map with default symbols  */
            val DefaultSymbols: MutableMap<ASN1ObjectIdentifier, String> = HashMap()

            init {
                DefaultSymbols.put(C, "C")
                DefaultSymbols.put(O, "O")
                DefaultSymbols.put(T, "T")
                DefaultSymbols.put(OU, "OU")
                DefaultSymbols.put(CN, "CN")
                DefaultSymbols.put(L, "L")
                DefaultSymbols.put(ST, "ST")
                DefaultSymbols.put(SN, "SN")
                DefaultSymbols.put(EmailAddress, "E")
                DefaultSymbols.put(DC, "DC")
                DefaultSymbols.put(UID, "UID")
                DefaultSymbols.put(SURNAME, "SURNAME")
                DefaultSymbols.put(GIVENNAME, "GIVENNAME")
                DefaultSymbols.put(INITIALS, "INITIALS")
                DefaultSymbols.put(GENERATION, "GENERATION")
            }
        }
    }

    /**
     * class for breaking up an X500 Name into it's component tokens,
     * similar to java.util.StringTokenizer. We need this class as some
     * of the lightweight Java environments don't support classes such
     * as StringTokenizer.
     */
    class X509NameTokenizer(private val oid: String) {
        private var index: Int = 0
        private val buf = StringBuffer()

        init {
            this.index = -1
        }

        fun hasMoreTokens(): Boolean {
            return index != oid.length
        }

        fun nextToken(): String? {
            if (index == oid.length) {
                return null
            }

            var end = index + 1
            var quoted = false
            var escaped = false

            buf.setLength(0)

            while (end != oid.length) {
                val c = oid[end]

                if (c == '"') {
                    if (!escaped) {
                        quoted = !quoted
                    } else {
                        buf.append(c)
                    }
                    escaped = false
                } else {
                    if (escaped || quoted) {
                        buf.append(c)
                        escaped = false
                    } else if (c == '\\') {
                        escaped = true
                    } else if (c == ',') {
                        break
                    } else {
                        buf.append(c)
                    }
                }
                end++
            }

            index = end
            return buf.toString().trim { it <= ' ' }
        }
    }

    // Certificate issuer

    /**
     * Get the issuer fields from an X509 Certificate
     * @param cert an X509Certificate
     * *
     * @return an X500Name
     */
    fun getIssuerFields(cert: X509Certificate): X500Name {
        try {
            return X500Name(CertificateInfo.getIssuer(cert.tbsCertificate) as ASN1Sequence)
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    /**
     * Get the "issuer" from the TBSCertificate bytes that are passed in
     * @param enc a TBSCertificate in a byte array
     * *
     * @return a ASN1Primitive
     */
    fun getIssuer(enc: ByteArray): ASN1Primitive {
        try {
            val `in` = ASN1InputStream(ByteArrayInputStream(enc))
            val seq = `in`.readObject() as ASN1Sequence
            return seq.getObjectAt(if (seq.getObjectAt(0) is ASN1TaggedObject) 3 else 2) as ASN1Primitive
        } catch (e: IOException) {
            throw ExceptionConverter(e)
        }

    }

    // Certificate Subject

    /**
     * Get the subject fields from an X509 Certificate
     * @param cert an X509Certificate
     * *
     * @return an X500Name
     */
    fun getSubjectFields(cert: X509Certificate?): X500Name? {
        try {
            if (cert != null)
                return X500Name(CertificateInfo.getSubject(cert.tbsCertificate) as ASN1Sequence)
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

        return null
    }

    /**
     * Get the "subject" from the TBSCertificate bytes that are passed in
     * @param enc A TBSCertificate in a byte array
     * *
     * @return a ASN1Primitive
     */
    fun getSubject(enc: ByteArray): ASN1Primitive {
        try {
            val `in` = ASN1InputStream(ByteArrayInputStream(enc))
            val seq = `in`.readObject() as ASN1Sequence
            return seq.getObjectAt(if (seq.getObjectAt(0) is ASN1TaggedObject) 5 else 4) as ASN1Primitive
        } catch (e: IOException) {
            throw ExceptionConverter(e)
        }

    }

}
