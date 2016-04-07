package org.bouncycastle.asn1.test

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.asn1.sec.ECPrivateKey
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.asn1.x9.X962NamedCurves
import org.bouncycastle.asn1.x9.X962Parameters
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.asn1.x9.X9ECPoint
import org.bouncycastle.asn1.x9.X9IntegerConverter
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.util.Arrays
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.encoders.Hex
import org.bouncycastle.util.test.SimpleTest

class X9Test : SimpleTest() {
    private val namedPub = Base64.decode("MDcwEwYHKoZIzj0CAQYIKoZIzj0DAQEDIAADG5xRI+Iki/JrvL20hoDUa7Cggzorv5B9yyqSMjYu")
    private val expPub = Base64.decode(
            "MIH8MIHXBgcqhkjOPQIBMIHLAgEBMCkGByqGSM49AQECHn///////////////3///////4AAAA" +
                    "AAAH///////zBXBB5///////////////9///////+AAAAAAAB///////wEHiVXBfoqMGZUsfTL" +
                    "A9anUKMMJQEC1JiHF9m6FattPgMVAH1zdBaP/jRxtgqFdoahlHXTv6L/BB8DZ2iujhi7ks/PAF" +
                    "yUmqLG2UhT0OZgu/hUsclQX+laAh5///////////////9///+XXetBs6YFfDxDIUZSZVECAQED" +
                    "IAADG5xRI+Iki/JrvL20hoDUa7Cggzorv5B9yyqSMjYu")

    private val namedPriv = Base64.decode("MDkCAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQEEHzAdAgEBBB" + "gAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAo=")

    private val expPriv = Base64.decode(
            "MIIBBAIBADCB1wYHKoZIzj0CATCBywIBATApBgcqhkjOPQEBAh5///////////////9///////" +
                    "+AAAAAAAB///////8wVwQef///////////////f///////gAAAAAAAf//////8BB4lVwX6KjBmVL" +
                    "H0ywPWp1CjDCUBAtSYhxfZuhWrbT4DFQB9c3QWj/40cbYKhXaGoZR107+i/wQfA2doro4Yu5LPzw" +
                    "BclJqixtlIU9DmYLv4VLHJUF/pWgIef///////////////f///l13rQbOmBXw8QyFGUmVRAgEBBC" +
                    "UwIwIBAQQeAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAU")

    @Throws(Exception::class)
    private fun encodePublicKey() {
        val ecP = X962NamedCurves.getByOID(X9ObjectIdentifiers.prime239v3)

        val conv = X9IntegerConverter()

        if (conv.getByteLength(ecP.curve) != 30) {
            fail("wrong byte length reported for curve")
        }

        if (ecP.curve.fieldSize != 239) {
            fail("wrong field size reported for curve")
        }

        //
        // named curve
        //
        var params = X962Parameters(X9ObjectIdentifiers.prime192v1)
        val point = ecP.g.multiply(BigInteger.valueOf(100))

        val p = DEROctetString(point.getEncoded(true))

        var info = SubjectPublicKeyInfo(AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, params), p.octets)
        if (!areEqual(info.encoded, namedPub)) {
            fail("failed public named generation")
        }

        val x9P = X9ECPoint(ecP.curve, p)

        if (!Arrays.areEqual(p.octets, x9P.point.encoded)) {
            fail("point encoding not preserved")
        }

        var o = ASN1Primitive.fromByteArray(namedPub)

        if (info != o) {
            fail("failed public named equality")
        }

        //
        // explicit curve parameters
        //
        params = X962Parameters(ecP)

        info = SubjectPublicKeyInfo(AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, params), p.octets)

        if (!areEqual(info.encoded, expPub)) {
            fail("failed public explicit generation")
        }

        o = ASN1Primitive.fromByteArray(expPub)

        if (info != o) {
            fail("failed public explicit equality")
        }
    }

    @Throws(Exception::class)
    private fun encodePrivateKey() {
        var ecP: X9ECParameters = X962NamedCurves.getByOID(X9ObjectIdentifiers.prime192v1)

        //
        // named curve
        //
        var params = X962Parameters(X9ObjectIdentifiers.prime192v1)

        var info = PrivateKeyInfo(AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, params),
                ECPrivateKey(ecP.n.bitLength(), BigInteger.valueOf(10)))

        if (!areEqual(info.encoded, namedPriv)) {
            fail("failed private named generation")
        }

        var o = ASN1Primitive.fromByteArray(namedPriv)

        if (info != o) {
            fail("failed private named equality")
        }

        //
        // explicit curve parameters
        //
        ecP = X962NamedCurves.getByOID(X9ObjectIdentifiers.prime239v3)

        params = X962Parameters(ecP)

        info = PrivateKeyInfo(AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, params),
                ECPrivateKey(ecP.n.bitLength(), BigInteger.valueOf(20)))

        if (!areEqual(info.encoded, expPriv)) {
            fail("failed private explicit generation")
        }

        o = ASN1Primitive.fromByteArray(expPriv)

        if (info != o) {
            fail("failed private explicit equality")
        }
    }

    @Throws(Exception::class)
    override fun performTest() {
        encodePublicKey()
        encodePrivateKey()
    }

    override fun getName(): String {
        return "X9"
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(X9Test())
        }
    }
}
