package org.bouncycastle.asn1.util

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Primitive


@Deprecated("use ASN1Dump.")
class DERDump : ASN1Dump() {
    companion object {
        /**
         * dump out a DER object as a formatted string

         * @param obj the ASN1Primitive to be dumped out.
         */
        fun dumpAsString(
                obj: ASN1Primitive): String {
            val buf = StringBuffer()

            ASN1Dump._dumpAsString("", false, obj, buf)

            return buf.toString()
        }

        /**
         * dump out a DER object as a formatted string

         * @param obj the ASN1Primitive to be dumped out.
         */
        fun dumpAsString(
                obj: ASN1Encodable): String {
            val buf = StringBuffer()

            ASN1Dump._dumpAsString("", false, obj.toASN1Primitive(), buf)

            return buf.toString()
        }
    }
}
