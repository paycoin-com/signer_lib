package org.bouncycastle.asn1.misc

import org.bouncycastle.asn1.DERIA5String

class NetscapeRevocationURL(
        str: DERIA5String) : DERIA5String(str.string) {

    override fun toString(): String {
        return "NetscapeRevocationURL: " + this.string
    }
}
