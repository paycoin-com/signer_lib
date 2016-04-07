package org.bouncycastle.asn1.x9

abstract class X9ECParametersHolder {
    private var params: X9ECParameters? = null

    val parameters: X9ECParameters
        @Synchronized get() {
            if (params == null) {
                params = createParameters()
            }

            return params
        }

    protected abstract fun createParameters(): X9ECParameters
}
