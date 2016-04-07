package org.bouncycastle.asn1.util

import java.io.FileInputStream

import org.bouncycastle.asn1.ASN1InputStream

object Dump {
    @Throws(Exception::class)
    @JvmStatic fun main(
            args: Array<String>) {
        val fIn = FileInputStream(args[0])
        val bIn = ASN1InputStream(fIn)
        var obj: Any? = null

        while ((obj = bIn.readObject()) != null) {
            println(ASN1Dump.dumpAsString(obj))
        }
    }
}
