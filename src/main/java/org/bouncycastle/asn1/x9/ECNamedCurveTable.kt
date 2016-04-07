package org.bouncycastle.asn1.x9

import java.util.Enumeration
import java.util.Vector

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.anssi.ANSSINamedCurves
import org.bouncycastle.asn1.cryptopro.ECGOST3410NamedCurves
import org.bouncycastle.asn1.nist.NISTNamedCurves
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.asn1.teletrust.TeleTrusTNamedCurves

/**
 * A general class that reads all X9.62 style EC curve tables.
 */
object ECNamedCurveTable {
    /**
     * return a X9ECParameters object representing the passed in named
     * curve. The routine returns null if the curve is not present.

     * @param name the name of the curve requested
     * *
     * @return an X9ECParameters object or null if the curve is not available.
     */
    fun getByName(
            name: String): X9ECParameters {
        var ecP = X962NamedCurves.getByName(name)

        if (ecP == null) {
            ecP = SECNamedCurves.getByName(name)
        }

        if (ecP == null) {
            ecP = NISTNamedCurves.getByName(name)
        }

        if (ecP == null) {
            ecP = TeleTrusTNamedCurves.getByName(name)
        }

        if (ecP == null) {
            ecP = ANSSINamedCurves.getByName(name)
        }

        return ecP
    }

    /**
     * return the object identifier signified by the passed in name. Null
     * if there is no object identifier associated with name.

     * @return the object identifier associated with name, if present.
     */
    fun getOID(
            name: String): ASN1ObjectIdentifier {
        var oid = X962NamedCurves.getOID(name)

        if (oid == null) {
            oid = SECNamedCurves.getOID(name)
        }

        if (oid == null) {
            oid = NISTNamedCurves.getOID(name)
        }

        if (oid == null) {
            oid = TeleTrusTNamedCurves.getOID(name)
        }

        if (oid == null) {
            oid = ANSSINamedCurves.getOID(name)
        }

        return oid
    }

    /**
     * return a X9ECParameters object representing the passed in named
     * curve.

     * @param oid the object id of the curve requested
     * *
     * @return a standard name for the curve.
     */
    fun getName(
            oid: ASN1ObjectIdentifier): String {
        var name: String? = NISTNamedCurves.getName(oid)

        if (name == null) {
            name = SECNamedCurves.getName(oid)
        }

        if (name == null) {
            name = TeleTrusTNamedCurves.getName(oid)
        }

        if (name == null) {
            name = X962NamedCurves.getName(oid)
        }

        if (name == null) {
            name = ECGOST3410NamedCurves.getName(oid)
        }

        return name
    }

    /**
     * return a X9ECParameters object representing the passed in named
     * curve.

     * @param oid the object id of the curve requested
     * *
     * @return an X9ECParameters object or null if the curve is not available.
     */
    fun getByOID(
            oid: ASN1ObjectIdentifier): X9ECParameters {
        var ecP = X962NamedCurves.getByOID(oid)

        if (ecP == null) {
            ecP = SECNamedCurves.getByOID(oid)
        }

        // NOTE: All the NIST curves are currently from SEC, so no point in redundant OID lookup

        if (ecP == null) {
            ecP = TeleTrusTNamedCurves.getByOID(oid)
        }

        if (ecP == null) {
            ecP = ANSSINamedCurves.getByOID(oid)
        }

        return ecP
    }

    /**
     * return an enumeration of the names of the available curves.

     * @return an enumeration of the names of the available curves.
     */
    val names: Enumeration<Any>
        get() {
            val v = Vector()

            addEnumeration(v, X962NamedCurves.getNames())
            addEnumeration(v, SECNamedCurves.getNames())
            addEnumeration(v, NISTNamedCurves.getNames())
            addEnumeration(v, TeleTrusTNamedCurves.getNames())
            addEnumeration(v, ANSSINamedCurves.getNames())

            return v.elements()
        }

    private fun addEnumeration(
            v: Vector<Any>,
            e: Enumeration<Any>) {
        while (e.hasMoreElements()) {
            v.addElement(e.nextElement())
        }
    }
}
