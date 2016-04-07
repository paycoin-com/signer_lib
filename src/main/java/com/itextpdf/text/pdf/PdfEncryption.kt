/*
 * $Id: 43e517f3a758ceb5fb7c79250d3872ee8eb0d528 $
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

import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.exceptions.BadPasswordException
import com.itextpdf.text.pdf.crypto.AESCipherCBCnoPad
import com.itextpdf.text.pdf.crypto.ARCFOUREncryption
import com.itextpdf.text.pdf.crypto.IVGenerator

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.security.MessageDigest
import java.security.cert.Certificate

/**

 * @author Paulo Soares
 * *
 * @author Kazuya Ujihara
 */
class PdfEncryption() {

    /** The encryption key for a particular object/generation  */
    internal var key: ByteArray? = null

    /** The encryption key length for a particular object/generation  */
    internal var keySize: Int = 0

    /** The global encryption key  */
    internal var mkey = ByteArray(0)

    /** The encryption key for the owner  */
    internal var ownerKey = ByteArray(32)

    /** The encryption key for the user  */
    internal var userKey = ByteArray(32)

    internal var oeKey: ByteArray? = null
    internal var ueKey: ByteArray? = null
    internal var perms: ByteArray? = null

    var permissions: Long = 0
        internal set

    internal var documentID: ByteArray? = null

    private var revision: Int = 0

    /** The generic key length. It may be 40 or 128.  */
    private var keyLength: Int = 0


    /** The public key security handler for certificate encryption  */
    protected var publicKeyHandler: PdfPublicKeySecurityHandler? = null

    /** Work area to prepare the object/generation bytes  */
    internal var extra = ByteArray(5)

    /** The message digest algorithm MD5  */
    internal var md5: MessageDigest

    private val arcfour = ARCFOUREncryption()

    var isMetadataEncrypted: Boolean = false
        private set

    /**
     * Indicates if the encryption is only necessary for embedded files.
     * @since 2.1.3
     */
    /**
     * Indicates if only the embedded files have to be encrypted.
     * @return    if true only the embedded files will be encrypted
     * *
     * @since    2.1.3
     */
    var isEmbeddedFilesOnly: Boolean = false
        private set

    var cryptoMode: Int = 0
        private set

    init {
        try {
            md5 = MessageDigest.getInstance("MD5")
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

        publicKeyHandler = PdfPublicKeySecurityHandler()
    }

    constructor(enc: PdfEncryption) : this() {
        if (enc.key != null)
            key = enc.key!!.clone()
        keySize = enc.keySize
        mkey = enc.mkey.clone()
        ownerKey = enc.ownerKey.clone()
        userKey = enc.userKey.clone()
        permissions = enc.permissions
        if (enc.documentID != null)
            documentID = enc.documentID!!.clone()
        revision = enc.revision
        keyLength = enc.keyLength
        isMetadataEncrypted = enc.isMetadataEncrypted
        isEmbeddedFilesOnly = enc.isEmbeddedFilesOnly
        publicKeyHandler = enc.publicKeyHandler

        if (enc.ueKey != null) {
            ueKey = enc.ueKey!!.clone()
        }
        if (enc.oeKey != null) {
            oeKey = enc.oeKey!!.clone()
        }
        if (enc.perms != null) {
            perms = enc.perms!!.clone()
        }
    }

    fun setCryptoMode(mode: Int, kl: Int) {
        var mode = mode
        cryptoMode = mode
        isMetadataEncrypted = mode and PdfWriter.DO_NOT_ENCRYPT_METADATA != PdfWriter.DO_NOT_ENCRYPT_METADATA
        isEmbeddedFilesOnly = mode and PdfWriter.EMBEDDED_FILES_ONLY == PdfWriter.EMBEDDED_FILES_ONLY
        mode = mode and PdfWriter.ENCRYPTION_MASK
        when (mode) {
            PdfWriter.STANDARD_ENCRYPTION_40 -> {
                isMetadataEncrypted = true
                isEmbeddedFilesOnly = false
                keyLength = 40
                revision = STANDARD_ENCRYPTION_40
            }
            PdfWriter.STANDARD_ENCRYPTION_128 -> {
                isEmbeddedFilesOnly = false
                if (kl > 0)
                    keyLength = kl
                else
                    keyLength = 128
                revision = STANDARD_ENCRYPTION_128
            }
            PdfWriter.ENCRYPTION_AES_128 -> {
                keyLength = 128
                revision = AES_128
            }
            PdfWriter.ENCRYPTION_AES_256 -> {
                keyLength = 256
                keySize = 32
                revision = AES_256
            }
            else -> throw IllegalArgumentException(MessageLocalization.getComposedMessage("no.valid.encryption.mode"))
        }
    }

    /**
     */
    private fun padPassword(userPassword: ByteArray?): ByteArray {
        val userPad = ByteArray(32)
        if (userPassword == null) {
            System.arraycopy(pad, 0, userPad, 0, 32)
        } else {
            System.arraycopy(userPassword, 0, userPad, 0, Math.min(
                    userPassword.size, 32))
            if (userPassword.size < 32)
                System.arraycopy(pad, 0, userPad, userPassword.size,
                        32 - userPassword.size)
        }

        return userPad
    }

    /**
     */
    private fun computeOwnerKey(userPad: ByteArray, ownerPad: ByteArray): ByteArray {
        val ownerKey = ByteArray(32)
        val digest = md5.digest(ownerPad)
        if (revision == STANDARD_ENCRYPTION_128 || revision == AES_128) {
            val mkey = ByteArray(keyLength / 8)
            // only use for the input as many bit as the key consists of
            for (k in 0..49) {
                md5.update(digest, 0, mkey.size)
                System.arraycopy(md5.digest(), 0, digest, 0, mkey.size)
            }
            System.arraycopy(userPad, 0, ownerKey, 0, 32)
            for (i in 0..19) {
                for (j in mkey.indices)
                    mkey[j] = (digest[j] xor i).toByte()
                arcfour.prepareARCFOURKey(mkey)
                arcfour.encryptARCFOUR(ownerKey)
            }
        } else {
            arcfour.prepareARCFOURKey(digest, 0, 5)
            arcfour.encryptARCFOUR(userPad, ownerKey)
        }
        return ownerKey
    }

    /**

     * ownerKey, documentID must be setup
     */
    private fun setupGlobalEncryptionKey(documentID: ByteArray?, userPad: ByteArray,
                                         ownerKey: ByteArray, permissions: Long) {
        this.documentID = documentID
        this.ownerKey = ownerKey
        this.permissions = permissions
        // use variable keylength
        mkey = ByteArray(keyLength / 8)

        // fixed by ujihara in order to follow PDF reference
        md5.reset()
        md5.update(userPad)
        md5.update(ownerKey)

        val ext = ByteArray(4)
        ext[0] = permissions.toByte()
        ext[1] = (permissions shr 8).toByte()
        ext[2] = (permissions shr 16).toByte()
        ext[3] = (permissions shr 24).toByte()
        md5.update(ext, 0, 4)
        if (documentID != null)
            md5.update(documentID)
        if (!isMetadataEncrypted)
            md5.update(metadataPad)

        val digest = ByteArray(mkey.size)
        System.arraycopy(md5.digest(), 0, digest, 0, mkey.size)

        // only use the really needed bits as input for the hash
        if (revision == STANDARD_ENCRYPTION_128 || revision == AES_128) {
            for (k in 0..49)
                System.arraycopy(md5.digest(digest), 0, digest, 0, mkey.size)
        }

        System.arraycopy(digest, 0, mkey, 0, mkey.size)
    }

    /**

     * mkey must be setup
     */
    // use the revision to choose the setup method
    private fun setupUserKey() {
        if (revision == STANDARD_ENCRYPTION_128 || revision == AES_128) {
            md5.update(pad)
            val digest = md5.digest(documentID)
            System.arraycopy(digest, 0, userKey, 0, 16)
            for (k in 16..31)
                userKey[k] = 0
            for (i in 0..19) {
                for (j in mkey.indices)
                    digest[j] = (mkey[j] xor i).toByte()
                arcfour.prepareARCFOURKey(digest, 0, mkey.size)
                arcfour.encryptARCFOUR(userKey, 0, 16)
            }
        } else {
            arcfour.prepareARCFOURKey(mkey)
            arcfour.encryptARCFOUR(pad, userKey)
        }
    }

    // gets keylength and revision and uses revision to choose the initial values
    // for permissions
    fun setupAllKeys(userPassword: ByteArray?, ownerPassword: ByteArray?,
                     permissions: Int) {
        var userPassword = userPassword
        var ownerPassword = ownerPassword
        var permissions = permissions
        if (ownerPassword == null || ownerPassword.size == 0)
            ownerPassword = md5.digest(createDocumentId())
        permissions = permissions or if (revision == STANDARD_ENCRYPTION_128 || revision == AES_128 || revision == AES_256)
            0xfffff0c0.toInt()
        else
            0xffffffc0.toInt()
        permissions = permissions and 0xfffffffc.toInt()
        this.permissions = permissions.toLong()
        if (revision == AES_256) {
            try {
                if (userPassword == null)
                    userPassword = ByteArray(0)
                documentID = createDocumentId()
                val uvs = IVGenerator.getIV(8)
                val uks = IVGenerator.getIV(8)
                key = IVGenerator.getIV(32)
                // Algorithm 3.8.1
                val md = MessageDigest.getInstance("SHA-256")
                md.update(userPassword, 0, Math.min(userPassword.size, 127))
                md.update(uvs)
                userKey = ByteArray(48)
                md.digest(userKey, 0, 32)
                System.arraycopy(uvs, 0, userKey, 32, 8)
                System.arraycopy(uks, 0, userKey, 40, 8)
                // Algorithm 3.8.2
                md.update(userPassword, 0, Math.min(userPassword.size, 127))
                md.update(uks)
                var ac = AESCipherCBCnoPad(true, md.digest())
                ueKey = ac.processBlock(key, 0, key!!.size)
                // Algorithm 3.9.1
                val ovs = IVGenerator.getIV(8)
                val oks = IVGenerator.getIV(8)
                md.update(ownerPassword, 0, Math.min(ownerPassword!!.size, 127))
                md.update(ovs)
                md.update(userKey)
                ownerKey = ByteArray(48)
                md.digest(ownerKey, 0, 32)
                System.arraycopy(ovs, 0, ownerKey, 32, 8)
                System.arraycopy(oks, 0, ownerKey, 40, 8)
                // Algorithm 3.9.2
                md.update(ownerPassword, 0, Math.min(ownerPassword.size, 127))
                md.update(oks)
                md.update(userKey)
                ac = AESCipherCBCnoPad(true, md.digest())
                oeKey = ac.processBlock(key, 0, key!!.size)
                // Algorithm 3.10
                val permsp = IVGenerator.getIV(16)
                permsp[0] = permissions.toByte()
                permsp[1] = (permissions shr 8).toByte()
                permsp[2] = (permissions shr 16).toByte()
                permsp[3] = (permissions shr 24).toByte()
                permsp[4] = 255.toByte()
                permsp[5] = 255.toByte()
                permsp[6] = 255.toByte()
                permsp[7] = 255.toByte()
                permsp[8] = if (isMetadataEncrypted) 'T'.toByte() else 'F'.toByte()
                permsp[9] = 'a'.toByte()
                permsp[10] = 'd'.toByte()
                permsp[11] = 'b'.toByte()
                ac = AESCipherCBCnoPad(true, key)
                perms = ac.processBlock(permsp, 0, permsp.size)
            } catch (ex: Exception) {
                throw ExceptionConverter(ex)
            }

        } else {
            // PDF reference 3.5.2 Standard Security Handler, Algorithm 3.3-1
            // If there is no owner password, use the user password instead.
            val userPad = padPassword(userPassword)
            val ownerPad = padPassword(ownerPassword)

            this.ownerKey = computeOwnerKey(userPad, ownerPad)
            documentID = createDocumentId()
            setupByUserPad(this.documentID, userPad, this.ownerKey, permissions.toLong())
        }
    }

    @Throws(BadPasswordException::class)
    fun readKey(enc: PdfDictionary, password: ByteArray?): Boolean {
        var password = password
        try {
            if (password == null)
                password = ByteArray(0)
            val oValue = com.itextpdf.text.DocWriter.getISOBytes(enc.get(PdfName.O)!!.toString())
            val uValue = com.itextpdf.text.DocWriter.getISOBytes(enc.get(PdfName.U)!!.toString())
            val oeValue = com.itextpdf.text.DocWriter.getISOBytes(enc.get(PdfName.OE)!!.toString())
            val ueValue = com.itextpdf.text.DocWriter.getISOBytes(enc.get(PdfName.UE)!!.toString())
            val perms = com.itextpdf.text.DocWriter.getISOBytes(enc.get(PdfName.PERMS)!!.toString())
            val pValue = enc.get(PdfName.P) as PdfNumber?

            this.oeKey = oeValue
            this.ueKey = ueValue
            this.perms = perms

            this.ownerKey = oValue
            this.userKey = uValue

            this.permissions = pValue.longValue()

            var isUserPass = false
            val md = MessageDigest.getInstance("SHA-256")
            md.update(password, 0, Math.min(password.size, 127))
            md.update(oValue, VALIDATION_SALT_OFFSET, SALT_LENGHT)
            md.update(uValue, 0, OU_LENGHT)
            var hash = md.digest()
            val isOwnerPass = compareArray(hash, oValue, 32)
            if (isOwnerPass) {
                md.update(password, 0, Math.min(password.size, 127))
                md.update(oValue, KEY_SALT_OFFSET, SALT_LENGHT)
                md.update(uValue, 0, OU_LENGHT)
                hash = md.digest()
                val ac = AESCipherCBCnoPad(false, hash)
                key = ac.processBlock(oeValue, 0, oeValue.size)
            } else {
                md.update(password, 0, Math.min(password.size, 127))
                md.update(uValue, VALIDATION_SALT_OFFSET, SALT_LENGHT)
                hash = md.digest()
                isUserPass = compareArray(hash, uValue, 32)
                if (!isUserPass)
                    throw BadPasswordException(MessageLocalization.getComposedMessage("bad.user.password"))
                md.update(password, 0, Math.min(password.size, 127))
                md.update(uValue, KEY_SALT_OFFSET, SALT_LENGHT)
                hash = md.digest()
                val ac = AESCipherCBCnoPad(false, hash)
                key = ac.processBlock(ueValue, 0, ueValue.size)
            }
            val ac = AESCipherCBCnoPad(false, key)
            val decPerms = ac.processBlock(perms, 0, perms.size)
            if (decPerms[9] != 'a'.toByte() || decPerms[10] != 'd'.toByte() || decPerms[11] != 'b'.toByte())
                throw BadPasswordException(MessageLocalization.getComposedMessage("bad.user.password"))
            permissions = decPerms[0] and 0xff or (decPerms[1] and 0xff shl 8)
            or (decPerms[2] and 0xff shl 16) or (decPerms[2] and 0xff shl 24).toLong()
            isMetadataEncrypted = decPerms[8] == 'T'.toByte()
            return isOwnerPass
        } catch (ex: BadPasswordException) {
            throw ex
        } catch (ex: Exception) {
            throw ExceptionConverter(ex)
        }

    }

    /**
     */
    fun setupByUserPassword(documentID: ByteArray, userPassword: ByteArray,
                            ownerKey: ByteArray, permissions: Long) {
        setupByUserPad(documentID, padPassword(userPassword), ownerKey,
                permissions)
    }

    /**
     */
    private fun setupByUserPad(documentID: ByteArray, userPad: ByteArray,
                               ownerKey: ByteArray, permissions: Long) {
        setupGlobalEncryptionKey(documentID, userPad, ownerKey, permissions)
        setupUserKey()
    }

    /**
     */
    fun setupByOwnerPassword(documentID: ByteArray, ownerPassword: ByteArray,
                             userKey: ByteArray, ownerKey: ByteArray, permissions: Long) {
        setupByOwnerPad(documentID, padPassword(ownerPassword), userKey,
                ownerKey, permissions)
    }

    private fun setupByOwnerPad(documentID: ByteArray, ownerPad: ByteArray,
                                userKey: ByteArray, ownerKey: ByteArray, permissions: Long) {
        val userPad = computeOwnerKey(ownerKey, ownerPad) // userPad will
        // be set in
        // this.ownerKey
        setupGlobalEncryptionKey(documentID, userPad, ownerKey, permissions) // step
        // 3
        setupUserKey()
    }

    fun setKey(key: ByteArray) {
        this.key = key
    }

    fun setupByEncryptionKey(key: ByteArray, keylength: Int) {
        mkey = ByteArray(keylength / 8)
        System.arraycopy(key, 0, mkey, 0, mkey.size)
    }

    fun setHashKey(number: Int, generation: Int) {
        if (revision == AES_256)
            return
        md5.reset() // added by ujihara
        extra[0] = number.toByte()
        extra[1] = (number shr 8).toByte()
        extra[2] = (number shr 16).toByte()
        extra[3] = generation.toByte()
        extra[4] = (generation shr 8).toByte()
        md5.update(mkey)
        md5.update(extra)
        if (revision == AES_128)
            md5.update(salt)
        key = md5.digest()
        keySize = mkey.size + 5
        if (keySize > 16)
            keySize = 16
    }

    val encryptionDictionary: PdfDictionary
        get() {
            val dic = PdfDictionary()

            if (publicKeyHandler!!.recipientsSize > 0) {
                var recipients: PdfArray? = null

                dic.put(PdfName.FILTER, PdfName.PUBSEC)
                dic.put(PdfName.R, PdfNumber(revision))

                try {
                    recipients = publicKeyHandler!!.encodedRecipients
                } catch (f: Exception) {
                    throw ExceptionConverter(f)
                }

                if (revision == STANDARD_ENCRYPTION_40) {
                    dic.put(PdfName.V, PdfNumber(1))
                    dic.put(PdfName.SUBFILTER, PdfName.ADBE_PKCS7_S4)
                    dic.put(PdfName.RECIPIENTS, recipients)
                } else if (revision == STANDARD_ENCRYPTION_128 && isMetadataEncrypted) {
                    dic.put(PdfName.V, PdfNumber(2))
                    dic.put(PdfName.LENGTH, PdfNumber(128))
                    dic.put(PdfName.SUBFILTER, PdfName.ADBE_PKCS7_S4)
                    dic.put(PdfName.RECIPIENTS, recipients)
                } else {
                    if (revision == AES_256) {
                        dic.put(PdfName.R, PdfNumber(AES_256))
                        dic.put(PdfName.V, PdfNumber(5))
                    } else {
                        dic.put(PdfName.R, PdfNumber(AES_128))
                        dic.put(PdfName.V, PdfNumber(4))
                    }
                    dic.put(PdfName.SUBFILTER, PdfName.ADBE_PKCS7_S5)

                    val stdcf = PdfDictionary()
                    stdcf.put(PdfName.RECIPIENTS, recipients)
                    if (!isMetadataEncrypted)
                        stdcf.put(PdfName.ENCRYPTMETADATA, PdfBoolean.PDFFALSE)
                    if (revision == AES_128) {
                        stdcf.put(PdfName.CFM, PdfName.AESV2)
                        stdcf.put(PdfName.LENGTH, PdfNumber(128))
                    } else if (revision == AES_256) {
                        stdcf.put(PdfName.CFM, PdfName.AESV3)
                        stdcf.put(PdfName.LENGTH, PdfNumber(256))
                    } else
                        stdcf.put(PdfName.CFM, PdfName.V2)
                    val cf = PdfDictionary()
                    cf.put(PdfName.DEFAULTCRYPTFILTER, stdcf)
                    dic.put(PdfName.CF, cf)
                    if (isEmbeddedFilesOnly) {
                        dic.put(PdfName.EFF, PdfName.DEFAULTCRYPTFILTER)
                        dic.put(PdfName.STRF, PdfName.IDENTITY)
                        dic.put(PdfName.STMF, PdfName.IDENTITY)
                    } else {
                        dic.put(PdfName.STRF, PdfName.DEFAULTCRYPTFILTER)
                        dic.put(PdfName.STMF, PdfName.DEFAULTCRYPTFILTER)
                    }
                }

                var md: MessageDigest? = null
                var encodedRecipient: ByteArray? = null

                try {
                    if (revision == AES_256)
                        md = MessageDigest.getInstance("SHA-256")
                    else
                        md = MessageDigest.getInstance("SHA-1")
                    md!!.update(publicKeyHandler!!.seed)
                    for (i in 0..publicKeyHandler!!.recipientsSize - 1) {
                        encodedRecipient = publicKeyHandler!!.getEncodedRecipient(i)
                        md.update(encodedRecipient)
                    }
                    if (!isMetadataEncrypted)
                        md.update(byteArrayOf(255.toByte(), 255.toByte(), 255.toByte(), 255.toByte()))
                } catch (f: Exception) {
                    throw ExceptionConverter(f)
                }

                val mdResult = md.digest()

                if (revision == AES_256)
                    key = mdResult
                else
                    setupByEncryptionKey(mdResult, keyLength)
            } else {
                dic.put(PdfName.FILTER, PdfName.STANDARD)
                dic.put(PdfName.O, PdfLiteral(StringUtils.escapeString(ownerKey)))
                dic.put(PdfName.U, PdfLiteral(StringUtils.escapeString(userKey)))
                dic.put(PdfName.P, PdfNumber(permissions))
                dic.put(PdfName.R, PdfNumber(revision))

                if (revision == STANDARD_ENCRYPTION_40) {
                    dic.put(PdfName.V, PdfNumber(1))
                } else if (revision == STANDARD_ENCRYPTION_128 && isMetadataEncrypted) {
                    dic.put(PdfName.V, PdfNumber(2))
                    dic.put(PdfName.LENGTH, PdfNumber(128))

                } else if (revision == AES_256) {
                    if (!isMetadataEncrypted)
                        dic.put(PdfName.ENCRYPTMETADATA, PdfBoolean.PDFFALSE)
                    dic.put(PdfName.OE, PdfLiteral(StringUtils.escapeString(oeKey)))
                    dic.put(PdfName.UE, PdfLiteral(StringUtils.escapeString(ueKey)))
                    dic.put(PdfName.PERMS, PdfLiteral(StringUtils.escapeString(perms)))
                    dic.put(PdfName.V, PdfNumber(revision))
                    dic.put(PdfName.LENGTH, PdfNumber(256))
                    val stdcf = PdfDictionary()
                    stdcf.put(PdfName.LENGTH, PdfNumber(32))
                    if (isEmbeddedFilesOnly) {
                        stdcf.put(PdfName.AUTHEVENT, PdfName.EFOPEN)
                        dic.put(PdfName.EFF, PdfName.STDCF)
                        dic.put(PdfName.STRF, PdfName.IDENTITY)
                        dic.put(PdfName.STMF, PdfName.IDENTITY)
                    } else {
                        stdcf.put(PdfName.AUTHEVENT, PdfName.DOCOPEN)
                        dic.put(PdfName.STRF, PdfName.STDCF)
                        dic.put(PdfName.STMF, PdfName.STDCF)
                    }
                    stdcf.put(PdfName.CFM, PdfName.AESV3)
                    val cf = PdfDictionary()
                    cf.put(PdfName.STDCF, stdcf)
                    dic.put(PdfName.CF, cf)
                } else {
                    if (!isMetadataEncrypted)
                        dic.put(PdfName.ENCRYPTMETADATA, PdfBoolean.PDFFALSE)
                    dic.put(PdfName.R, PdfNumber(AES_128))
                    dic.put(PdfName.V, PdfNumber(4))
                    dic.put(PdfName.LENGTH, PdfNumber(128))
                    val stdcf = PdfDictionary()
                    stdcf.put(PdfName.LENGTH, PdfNumber(16))
                    if (isEmbeddedFilesOnly) {
                        stdcf.put(PdfName.AUTHEVENT, PdfName.EFOPEN)
                        dic.put(PdfName.EFF, PdfName.STDCF)
                        dic.put(PdfName.STRF, PdfName.IDENTITY)
                        dic.put(PdfName.STMF, PdfName.IDENTITY)
                    } else {
                        stdcf.put(PdfName.AUTHEVENT, PdfName.DOCOPEN)
                        dic.put(PdfName.STRF, PdfName.STDCF)
                        dic.put(PdfName.STMF, PdfName.STDCF)
                    }
                    if (revision == AES_128)
                        stdcf.put(PdfName.CFM, PdfName.AESV2)
                    else
                        stdcf.put(PdfName.CFM, PdfName.V2)
                    val cf = PdfDictionary()
                    cf.put(PdfName.STDCF, stdcf)
                    dic.put(PdfName.CF, cf)
                }
            }

            return dic
        }

    @Throws(IOException::class)
    fun getFileID(modified: Boolean): PdfObject {
        return createInfoId(documentID, modified)
    }

    fun getEncryptionStream(os: OutputStream): OutputStreamEncryption {
        return OutputStreamEncryption(os, key, 0, keySize, revision)
    }

    fun calculateStreamSize(n: Int): Int {
        if (revision == AES_128 || revision == AES_256)
            return (n and 0x7ffffff0) + 32
        else
            return n
    }

    fun encryptByteArray(b: ByteArray): ByteArray {
        try {
            val ba = ByteArrayOutputStream()
            val os2 = getEncryptionStream(ba)
            os2.write(b)
            os2.finish()
            return ba.toByteArray()
        } catch (ex: IOException) {
            throw ExceptionConverter(ex)
        }

    }

    val decryptor: StandardDecryption
        get() = StandardDecryption(key, 0, keySize, revision)

    fun decryptByteArray(b: ByteArray): ByteArray {
        try {
            val ba = ByteArrayOutputStream()
            val dec = decryptor
            var b2 = dec.update(b, 0, b.size)
            if (b2 != null)
                ba.write(b2)
            b2 = dec.finish()
            if (b2 != null)
                ba.write(b2)
            return ba.toByteArray()
        } catch (ex: IOException) {
            throw ExceptionConverter(ex)
        }

    }

    fun addRecipient(cert: Certificate, permission: Int) {
        documentID = createDocumentId()
        publicKeyHandler!!.addRecipient(PdfPublicKeyRecipient(cert,
                permission))
    }

    fun computeUserPassword(ownerPassword: ByteArray): ByteArray {
        val userPad = computeOwnerKey(ownerKey, padPassword(ownerPassword))
        for (i in userPad.indices) {
            var match = true
            for (j in 0..userPad.size - i - 1) {
                if (userPad[i + j] != pad[j]) {
                    match = false
                    break
                }
            }
            if (!match) continue
            val userPassword = ByteArray(i)
            System.arraycopy(userPad, 0, userPassword, 0, i)
            return userPassword
        }
        return userPad
    }

    companion object {

        val STANDARD_ENCRYPTION_40 = 2

        val STANDARD_ENCRYPTION_128 = 3

        val AES_128 = 4

        val AES_256 = 5

        private val pad = byteArrayOf(0x28.toByte(), 0xBF.toByte(), 0x4E.toByte(), 0x5E.toByte(), 0x4E.toByte(), 0x75.toByte(), 0x8A.toByte(), 0x41.toByte(), 0x64.toByte(), 0x00.toByte(), 0x4E.toByte(), 0x56.toByte(), 0xFF.toByte(), 0xFA.toByte(), 0x01.toByte(), 0x08.toByte(), 0x2E.toByte(), 0x2E.toByte(), 0x00.toByte(), 0xB6.toByte(), 0xD0.toByte(), 0x68.toByte(), 0x3E.toByte(), 0x80.toByte(), 0x2F.toByte(), 0x0C.toByte(), 0xA9.toByte(), 0xFE.toByte(), 0x64.toByte(), 0x53.toByte(), 0x69.toByte(), 0x7A.toByte())

        private val salt = byteArrayOf(0x73.toByte(), 0x41.toByte(), 0x6c.toByte(), 0x54.toByte())

        private val metadataPad = byteArrayOf(255.toByte(), 255.toByte(), 255.toByte(), 255.toByte())

        internal var seq = System.currentTimeMillis()

        private val VALIDATION_SALT_OFFSET = 32
        private val KEY_SALT_OFFSET = 40
        private val SALT_LENGHT = 8
        private val OU_LENGHT = 48

        private fun compareArray(a: ByteArray, b: ByteArray, len: Int): Boolean {
            for (k in 0..len - 1) {
                if (a[k] != b[k]) {
                    return false
                }
            }
            return true
        }

        fun createDocumentId(): ByteArray {
            val md5: MessageDigest
            try {
                md5 = MessageDigest.getInstance("MD5")
            } catch (e: Exception) {
                throw ExceptionConverter(e)
            }

            val time = System.currentTimeMillis()
            val mem = Runtime.getRuntime().freeMemory()
            val s = time + "+" + mem + "+" + seq++
            return md5.digest(s.toByteArray())
        }

        @Throws(IOException::class)
        fun createInfoId(id: ByteArray, modified: Boolean): PdfObject {
            var id = id
            val buf = ByteBuffer(90)
            if (id.size == 0)
                id = createDocumentId()
            buf.append('[').append('<')
            for (k in id.indices)
                buf.appendHex(id[k])
            buf.append('>').append('<')
            if (modified)
                id = createDocumentId()
            for (k in id.indices)
                buf.appendHex(id[k])
            buf.append('>').append(']')
            buf.close()
            return PdfLiteral(buf.toByteArray())
        }
    }
}
