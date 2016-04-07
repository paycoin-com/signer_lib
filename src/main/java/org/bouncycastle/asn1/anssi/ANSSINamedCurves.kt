package org.bouncycastle.asn1.anssi

import java.math.BigInteger
import java.util.Enumeration
import java.util.Hashtable

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.asn1.x9.X9ECParametersHolder
import org.bouncycastle.asn1.x9.X9ECPoint
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.util.Strings
import org.bouncycastle.util.encoders.Hex

/**
 * ANSSI Elliptic curve table.
 */
object ANSSINamedCurves {
    private fun configureCurve(curve: ECCurve): ECCurve {
        return curve
    }

    private fun fromHex(
            hex: String): BigInteger {
        return BigInteger(1, Hex.decode(hex))
    }

    /*
     * FRP256v1
     */
    internal var FRP256v1: X9ECParametersHolder = object : X9ECParametersHolder() {
        override fun createParameters(): X9ECParameters {
            val p = fromHex("F1FD178C0B3AD58F10126DE8CE42435B3961ADBCABC8CA6DE8FCF353D86E9C03")
            val a = fromHex("F1FD178C0B3AD58F10126DE8CE42435B3961ADBCABC8CA6DE8FCF353D86E9C00")
            val b = fromHex("EE353FCA5428A9300D4ABA754A44C00FDFEC0C9AE4B1A1803075ED967B7BB73F")
            val S: ByteArray? = null
            val n = fromHex("F1FD178C0B3AD58F10126DE8CE42435B53DC67E140D2BF941FFDD459C6D655E1")
            val h = BigInteger.valueOf(1)

            val curve = configureCurve(ECCurve.Fp(p, a, b, n, h))
            val G = X9ECPoint(curve, Hex.decode("04"
                    + "B6B3D4C356C139EB31183D4749D423958C27D2DCAF98B70164C97A2DD98F5CFF"
                    + "6142E0F7C8B204911F9271F0F3ECEF8C2701C307E8E4C9E183115A1554062CFB"))

            return X9ECParameters(curve, G, n, h, S)
        }
    }


    internal val objIds = Hashtable()
    internal val curves = Hashtable()
    internal val names = Hashtable()

    internal fun defineCurve(name: String, oid: ASN1ObjectIdentifier, holder: X9ECParametersHolder) {
        objIds.put(name.toLowerCase(), oid)
        names.put(oid, name)
        curves.put(oid, holder)
    }

    init {
        defineCurve("FRP256v1", ANSSIObjectIdentifiers.FRP256v1, FRP256v1)
    }

    fun getByName(
            name: String): X9ECParameters? {
        val oid = getOID(name)
        return if (oid == null) null else getByOID(oid)
    }

    /**
     * return the X9ECParameters object for the named curve represented by
     * the passed in object identifier. Null if the curve isn't present.

     * @param oid an object identifier representing a named curve, if present.
     */
    fun getByOID(
            oid: ASN1ObjectIdentifier): X9ECParameters? {
        return if (curves.get(oid) == null) null else curves.get(oid).parameters
    }

    /**
     * return the object identifier signified by the passed in name. Null
     * if there is no object identifier associated with name.

     * @return the object identifier associated with name, if present.
     */
    fun getOID(
            name: String): ASN1ObjectIdentifier? {
        return objIds.get(Strings.toLowerCase(name))
    }

    /**
     * return the named curve name represented by the given object identifier.
     */
    fun getName(
            oid: ASN1ObjectIdentifier): String {
        return names.get(oid)
    }

    /**
     * returns an enumeration containing the name strings for curves
     * contained in this structure.
     */
    fun getNames(): Enumeration<Any> {
        return names.elements()
    }
}
