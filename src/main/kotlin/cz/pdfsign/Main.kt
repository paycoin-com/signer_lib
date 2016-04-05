package cz.pdfsign

import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.*
import java.security.cert.Certificate

import com.itextpdf.text.DocumentException
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfSignatureAppearance
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.security.BouncyCastleDigest
import com.itextpdf.text.pdf.security.DigestAlgorithms
import com.itextpdf.text.pdf.security.ExternalDigest
import com.itextpdf.text.pdf.security.ExternalSignature
import com.itextpdf.text.pdf.security.MakeSignature
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard
import com.itextpdf.text.pdf.security.PrivateKeySignature
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.IOException


class Main {



    fun sign(src: String, dest: String,
             chain: Array<Certificate>,
             pk: PrivateKey, digestAlgorithm: String, provider: String,
             subfilter: CryptoStandard,
             reason: String, location: String) {
        // Creating the reader and the stamper
        val reader = PdfReader(src)
        val os = FileOutputStream(dest)
        val stamper = PdfStamper.createSignature(reader, os, '0')
        // Creating the appearance
        val appearance = stamper.getSignatureAppearance()
        appearance.setReason(reason)
        appearance.setLocation(location)
        appearance.setVisibleSignature(Rectangle(36f, 748f, 144f, 780f), 1, "sig")
        // Creating the signature
        val digest = BouncyCastleDigest()
        val signature = PrivateKeySignature(pk, digestAlgorithm, provider)
        MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, subfilter)
    }




}

fun main(args: Array<String>) {


    val KEYSTORE = "src/main/resources/pkcs.p12"
    val PASSWORD = "Heslo123,".toCharArray()
    val SRC = "src/main/resources/test.pdf"
    val DEST = "src/main/resources/test_signed_%s.pdf"

    val provider = BouncyCastleProvider()
    Security.addProvider(provider)

    val ks = KeyStore.getInstance("pkcs12")
    ks.load(FileInputStream(KEYSTORE), PASSWORD)

    //        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    //        ks.load(new FileInputStream(KEYSTORE), PASSWORD);
    val pk = ks.getKey(ks.aliases().nextElement(), PASSWORD) as PrivateKey
    val chain = ks.getCertificateChain(ks.aliases().nextElement())

    val app = Main()
    app.sign(SRC, java.lang.String.format(DEST, 1), chain, pk, DigestAlgorithms.SHA256, provider.getName(), CryptoStandard.CMS, "Test 1", "Ghent")
    app.sign(SRC, java.lang.String.format(DEST, 2), chain, pk, DigestAlgorithms.SHA512, provider.getName(), CryptoStandard.CMS, "Test 2", "Ghent")
    app.sign(SRC, java.lang.String.format(DEST, 3), chain, pk, DigestAlgorithms.SHA256, provider.getName(), CryptoStandard.CADES, "Test 3", "Ghent")
    app.sign(SRC, java.lang.String.format(DEST, 4), chain, pk, DigestAlgorithms.RIPEMD160, provider.getName(), CryptoStandard.CADES, "Test 4", "Ghent")
}





