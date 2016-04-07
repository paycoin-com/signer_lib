package org.bouncycastle.asn1

import java.io.OutputStream

abstract class ASN1Generator(protected var _out: OutputStream) {

    abstract val rawOutputStream: OutputStream
}
