package org.bouncycastle.asn1.x500.style

import org.bouncycastle.asn1.x500.RDN
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.X500NameStyle

/**
 * Variation of BCStyle that insists on strict ordering for equality
 * and hashCode comparisons
 */
class BCStrictStyle : BCStyle() {

    override fun areEqual(name1: X500Name, name2: X500Name): Boolean {
        val rdns1 = name1.rdNs
        val rdns2 = name2.rdNs

        if (rdns1.size != rdns2.size) {
            return false
        }

        for (i in rdns1.indices) {
            if (!rdnAreEqual(rdns1[i], rdns2[i])) {
                return false
            }
        }

        return true
    }

    companion object {
        val INSTANCE: X500NameStyle = BCStrictStyle()
    }
}
