/*
 * $Id: bbe32b9bb733d8225f45af444897e00acba059a2 $
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

import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import org.bouncycastle.jcajce.provider.digest.*

/**
 * Implementation for digests accessed directly from the BouncyCastle library bypassing
 * any provider definition.
 */
class BouncyCastleDigest : ExternalDigest {

    @Throws(GeneralSecurityException::class)
    override fun getMessageDigest(hashAlgorithm: String): MessageDigest {
        val oid = DigestAlgorithms.getAllowedDigests(hashAlgorithm) ?: throw NoSuchAlgorithmException(hashAlgorithm)
        if (oid == "1.2.840.113549.2.2") {
            //MD2
            return MD2.Digest()
        } else if (oid == "1.2.840.113549.2.5") {
            //MD5
            return MD5.Digest()
        } else if (oid == "1.3.14.3.2.26") {
            //SHA1
            return SHA1.Digest()
        } else if (oid == "2.16.840.1.101.3.4.2.4") {
            //SHA224
            return SHA224.Digest()
        } else if (oid == "2.16.840.1.101.3.4.2.1") {
            //SHA256
            return SHA256.Digest()
        } else if (oid == "2.16.840.1.101.3.4.2.2") {
            //SHA384
            return SHA384.Digest()
        } else if (oid == "2.16.840.1.101.3.4.2.3") {
            //SHA512
            return SHA512.Digest()
        } else if (oid == "1.3.36.3.2.2") {
            //RIPEMD128
            return RIPEMD128.Digest()
        } else if (oid == "1.3.36.3.2.1") {
            //RIPEMD160
            return RIPEMD160.Digest()
        } else if (oid == "1.3.36.3.2.3") {
            //RIPEMD256
            return RIPEMD256.Digest()
        } else if (oid == "1.2.643.2.2.9") {
            //GOST3411
            return GOST3411.Digest()
        }

        throw NoSuchAlgorithmException(hashAlgorithm) //shouldn't get here
    }
}
