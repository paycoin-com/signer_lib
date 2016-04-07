package org.bouncycastle.asn1.misc

import org.bouncycastle.asn1.DERIA5String

class VerisignCzagExtension(
        str: DERIA5String) : DERIA5String(str.string) {

    override fun toString(): String {
        return "VerisignCzagExtension: " + this.string
    }
}
