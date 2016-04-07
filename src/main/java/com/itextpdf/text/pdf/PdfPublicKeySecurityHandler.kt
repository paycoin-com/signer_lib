/*
 * $Id: c8491c63ea19f6fb68e78d9cc88f928c4ceeab76 $
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

/**
 * The below 2 methods are from pdfbox.

 * private ASN1Primitive createDERForRecipient(byte[] in, X509Certificate cert) ;
 * private KeyTransRecipientInfo computeRecipientInfo(X509Certificate x509certificate, byte[] abyte0);

 * 2006-11-22 Aiken Sam.
 */

/**
 * Copyright (c) 2003-2006, www.pdfbox.org
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:

 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 * http://www.pdfbox.org

 */

package com.itextpdf.text.pdf

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.AlgorithmParameterGenerator
import java.security.AlgorithmParameters
import java.security.GeneralSecurityException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.ArrayList

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DEROutputStream
import org.bouncycastle.asn1.DERSet
import org.bouncycastle.asn1.cms.ContentInfo
import org.bouncycastle.asn1.cms.EncryptedContentInfo
import org.bouncycastle.asn1.cms.EnvelopedData
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber
import org.bouncycastle.asn1.cms.KeyTransRecipientInfo
import org.bouncycastle.asn1.cms.RecipientIdentifier
import org.bouncycastle.asn1.cms.RecipientInfo
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.TBSCertificateStructure

/**
 * @author Aiken Sam (aikensam@ieee.org)
 */
class PdfPublicKeySecurityHandler {

    private var recipients: ArrayList<PdfPublicKeyRecipient>? = null

    private var seed = ByteArray(SEED_LENGTH)

    init {
        val key: KeyGenerator
        try {
            key = KeyGenerator.getInstance("AES")
            key.init(192, SecureRandom())
            val sk = key.generateKey()
            System.arraycopy(sk.encoded, 0, seed, 0, SEED_LENGTH) // create the 20 bytes seed
        } catch (e: NoSuchAlgorithmException) {
            seed = SecureRandom.getSeed(SEED_LENGTH)
        }

        recipients = ArrayList<PdfPublicKeyRecipient>()
    }

    fun addRecipient(recipient: PdfPublicKeyRecipient) {
        recipients!!.add(recipient)
    }

    protected fun getSeed(): ByteArray {
        return seed.clone()
    }
    /*
    public PdfPublicKeyRecipient[] getRecipients() {
        recipients.toArray();
        return (PdfPublicKeyRecipient[])recipients.toArray();
    }*/

    val recipientsSize: Int
        get() = recipients!!.size

    @Throws(IOException::class, GeneralSecurityException::class)
    fun getEncodedRecipient(index: Int): ByteArray {
        //Certificate certificate = recipient.getX509();
        val recipient = recipients!![index]
        var cms: ByteArray? = recipient.cms

        if (cms != null) return cms

        val certificate = recipient.certificate
        var permission = recipient.permission//PdfWriter.AllowCopy | PdfWriter.AllowPrinting | PdfWriter.AllowScreenReaders | PdfWriter.AllowAssembly;
        val revision = 3

        permission = permission or if (revision == 3) 0xfffff0c0.toInt() else 0xffffffc0.toInt()
        permission = permission and 0xfffffffc.toInt()
        permission += 1

        val pkcs7input = ByteArray(24)

        val one = permission.toByte()
        val two = (permission shr 8).toByte()
        val three = (permission shr 16).toByte()
        val four = (permission shr 24).toByte()

        System.arraycopy(seed, 0, pkcs7input, 0, 20) // put this seed in the pkcs7 input

        pkcs7input[20] = four
        pkcs7input[21] = three
        pkcs7input[22] = two
        pkcs7input[23] = one

        val obj = createDERForRecipient(pkcs7input, certificate as X509Certificate)

        val baos = ByteArrayOutputStream()

        val k = DEROutputStream(baos)

        k.writeObject(obj)

        cms = baos.toByteArray()

        recipient.cms = cms

        return cms
    }

    val encodedRecipients: PdfArray
        @Throws(IOException::class, GeneralSecurityException::class)
        get() {
            var EncodedRecipients: PdfArray? = PdfArray()
            var cms: ByteArray? = null
            for (i in recipients!!.indices)
                try {
                    cms = getEncodedRecipient(i)
                    EncodedRecipients!!.add(PdfLiteral(StringUtils.escapeString(cms)))
                } catch (e: GeneralSecurityException) {
                    EncodedRecipients = null
                } catch (e: IOException) {
                    EncodedRecipients = null
                }

            return EncodedRecipients
        }

    @Throws(IOException::class, GeneralSecurityException::class)
    private fun createDERForRecipient(`in`: ByteArray, cert: X509Certificate): ASN1Primitive {

        val s = "1.2.840.113549.3.2"

        val algorithmparametergenerator = AlgorithmParameterGenerator.getInstance(s)
        val algorithmparameters = algorithmparametergenerator.generateParameters()
        val bytearrayinputstream = ByteArrayInputStream(algorithmparameters.getEncoded("ASN.1"))
        val asn1inputstream = ASN1InputStream(bytearrayinputstream)
        val derobject = asn1inputstream.readObject()
        val keygenerator = KeyGenerator.getInstance(s)
        keygenerator.init(128)
        val secretkey = keygenerator.generateKey()
        val cipher = Cipher.getInstance(s)
        cipher.init(1, secretkey, algorithmparameters)
        val abyte1 = cipher.doFinal(`in`)
        val deroctetstring = DEROctetString(abyte1)
        val keytransrecipientinfo = computeRecipientInfo(cert, secretkey.encoded)
        val derset = DERSet(RecipientInfo(keytransrecipientinfo))
        val algorithmidentifier = AlgorithmIdentifier(ASN1ObjectIdentifier(s), derobject)
        val encryptedcontentinfo = EncryptedContentInfo(PKCSObjectIdentifiers.data, algorithmidentifier, deroctetstring)
        val set: ASN1Set? = null
        val env = EnvelopedData(null, derset, encryptedcontentinfo, set)
        val contentinfo = ContentInfo(PKCSObjectIdentifiers.envelopedData, env)
        return contentinfo.toASN1Primitive()
    }

    @Throws(GeneralSecurityException::class, IOException::class)
    private fun computeRecipientInfo(x509certificate: X509Certificate, abyte0: ByteArray): KeyTransRecipientInfo {
        val asn1inputstream = ASN1InputStream(ByteArrayInputStream(x509certificate.tbsCertificate))
        val tbscertificatestructure = TBSCertificateStructure.getInstance(asn1inputstream.readObject())
        val algorithmidentifier = tbscertificatestructure.subjectPublicKeyInfo.algorithm
        val issuerandserialnumber = IssuerAndSerialNumber(
                tbscertificatestructure.issuer,
                tbscertificatestructure.serialNumber.value)
        val cipher = Cipher.getInstance(algorithmidentifier.algorithm.id)
        try {
            cipher.init(1, x509certificate)
        } catch (e: InvalidKeyException) {
            cipher.init(1, x509certificate.publicKey)
        }

        val deroctetstring = DEROctetString(cipher.doFinal(abyte0))
        val recipId = RecipientIdentifier(issuerandserialnumber)
        return KeyTransRecipientInfo(recipId, algorithmidentifier, deroctetstring)
    }

    companion object {

        internal val SEED_LENGTH = 20
    }
}
