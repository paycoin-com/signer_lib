/*
 * $Id: b6a74c3711a0c46360f9f4a306c09c63799ba865 $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Pavel Alay, Bruno Lowagie, et al.
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

object SecurityConstants {

    val XMLNS = "xmlns"
    val XMLNS_XADES = "xmlns:xades"

    val OIDAsURN = "OIDAsURN"
    val OID_DSA_SHA1 = "urn:oid:1.2.840.10040.4.3"
    val OID_DSA_SHA1_DESC = "ANSI X9.57 DSA signature generated with SHA-1 hash (DSA x9.30)"

    val OID_RSA_SHA1 = "urn:oid:1.2.840.113549.1.1.5"
    val OID_RSA_SHA1_DESC = "RSA (PKCS #1 v1.5) with SHA-1 signature"

    val XMLNS_URI = "http://www.w3.org/2000/xmlns/"
    val XMLDSIG_URI = "http://www.w3.org/2000/09/xmldsig#"
    val XADES_132_URI = "http://uri.etsi.org/01903/v1.3.2#"

    val SHA1_URI = "http://www.w3.org/2000/09/xmldsig#sha1"
    val SignedProperties_Type = "http://uri.etsi.org/01903#SignedProperties"

    val DSA = "DSA"
    val RSA = "RSA"
    val SHA1 = "SHA1"

    val DigestMethod = "DigestMethod"
    val DigestValue = "DigestValue"
    val Signature = "Signature"
    val SignatureValue = "SignatureValue"
    val X509SerialNumber = "X509SerialNumber"
    val X509IssuerName = "X509IssuerName"

    val Algorithm = "Algorithm"
    val Id = "Id"
    val ObjectReference = "ObjectReference"
    val Target = "Target"
    val Qualifier = "Qualifier"

    val XADES_Encoding = "xades:Encoding"
    val XADES_MimeType = "xades:MimeType"
    val XADES_Description = "xades:Description"
    val XADES_DataObjectFormat = "xades:DataObjectFormat"
    val XADES_SignedDataObjectProperties = "xades:SignedDataObjectProperties"
    val XADES_IssuerSerial = "xades:IssuerSerial"
    val XADES_CertDigest = "xades:CertDigest"
    val XADES_Cert = "xades:Cert"
    val XADES_SigningCertificate = "xades:SigningCertificate"
    val XADES_SigningTime = "xades:SigningTime"
    val XADES_SignedSignatureProperties = "xades:SignedSignatureProperties"
    val XADES_SignedProperties = "xades:SignedProperties"
    val XADES_QualifyingProperties = "xades:QualifyingProperties"
    val XADES_SignaturePolicyIdentifier = "xades:SignaturePolicyIdentifier"
    val XADES_SignaturePolicyId = "xades:SignaturePolicyId"
    val XADES_SigPolicyId = "xades:SigPolicyId"
    val XADES_Identifier = "xades:Identifier"
    val XADES_SigPolicyHash = "xades:SigPolicyHash"

    val Reference_ = "Reference-"
    val SignedProperties_ = "SignedProperties-"
    val Signature_ = "Signature-"

    val SigningTimeFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
}
