package org.bouncycastle.asn1.x509

import java.util.Vector

class GeneralNamesBuilder {
    private val names = Vector()

    fun addNames(names: GeneralNames): GeneralNamesBuilder {
        val n = names.names

        for (i in n.indices) {
            this.names.addElement(n[i])
        }

        return this
    }

    fun addName(name: GeneralName): GeneralNamesBuilder {
        names.addElement(name)

        return this
    }

    fun build(): GeneralNames {
        val tmp = arrayOfNulls<GeneralName>(names.size)

        for (i in tmp.indices) {
            tmp[i] = names.elementAt(i) as GeneralName
        }

        return GeneralNames(tmp)
    }
}
