package org.bouncycastle.asn1.test

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OutputStream
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DERPrintableString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERSet
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.x500.RDN
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.X500NameBuilder
import org.bouncycastle.asn1.x500.style.BCStrictStyle
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.asn1.x509.X509DefaultEntryConverter
import org.bouncycastle.util.encoders.Hex
import org.bouncycastle.util.test.SimpleTest

class X500NameTest : SimpleTest() {
    internal var subjects = arrayOf("C=AU,ST=Victoria,L=South Melbourne,O=Connect 4 Pty Ltd,OU=Webserver Team,CN=www2.connect4.com.au,E=webmaster@connect4.com.au", "C=AU,ST=Victoria,L=South Melbourne,O=Connect 4 Pty Ltd,OU=Certificate Authority,CN=Connect 4 CA,E=webmaster@connect4.com.au", "C=AU,ST=QLD,CN=SSLeay/rsa test cert", "C=US,O=National Aeronautics and Space Administration,SERIALNUMBER=16+CN=Steve Schoch", "E=cooke@issl.atl.hp.com,C=US,OU=Hewlett Packard Company (ISSL),CN=Paul A. Cooke", "O=Sun Microsystems Inc,CN=store.sun.com", "unstructuredAddress=192.168.1.33,unstructuredName=pixfirewall.ciscopix.com,CN=pixfirewall.ciscopix.com", "CN=*.canal-plus.com,OU=Provided by TBS INTERNET http://www.tbs-certificats.com/,OU=\\ CANAL \\+,O=CANAL\\+DISTRIBUTION,L=issy les moulineaux,ST=Hauts de Seine,C=FR", "O=Bouncy Castle,CN=www.bouncycastle.org\\ ", "O=Bouncy Castle,CN=c:\\\\fred\\\\bob")

    internal var hexSubjects = arrayOf("CN=\\20Test\\20X,O=\\20Test,C=GB", // input
            "CN=\\ Test X,O=\\ Test,C=GB", // expected
            "CN=\\20Test\\20X\\20,O=\\20Test,C=GB", // input
            "CN=\\ Test X\\ ,O=\\ Test,C=GB"          // expected
    )

    override fun getName(): String {
        return "X500Name"
    }

    private fun createEntryValue(oid: ASN1ObjectIdentifier, value: String): ASN1Encodable {
        val builder = X500NameBuilder(BCStyle.INSTANCE)

        builder.addRDN(oid, value)

        val name = builder.build()

        var seq = name.toASN1Primitive() as ASN1Sequence
        val set = ASN1Set.getInstance(seq.getObjectAt(0).toASN1Primitive())
        seq = set.getObjectAt(0) as ASN1Sequence

        return seq.getObjectAt(1)
    }

    private fun createEntryValueFromString(oid: ASN1ObjectIdentifier, value: String): ASN1Encodable {
        val builder = X500NameBuilder(BCStyle.INSTANCE)

        builder.addRDN(oid, value)

        val name = X500Name(builder.build().toString())

        var seq = name.toASN1Primitive() as ASN1Sequence
        val set = ASN1Set.getInstance(seq.getObjectAt(0).toASN1Primitive())
        seq = set.getObjectAt(0) as ASN1Sequence

        return seq.getObjectAt(1)
    }

    private fun testEncodingPrintableString(oid: ASN1ObjectIdentifier, value: String) {
        val converted = createEntryValue(oid, value)
        if (converted !is DERPrintableString) {
            fail("encoding for $oid not printable string")
        }
    }

    private fun testEncodingIA5String(oid: ASN1ObjectIdentifier, value: String) {
        val converted = createEntryValue(oid, value)
        if (converted !is DERIA5String) {
            fail("encoding for $oid not IA5String")
        }
    }

    @Throws(IOException::class)
    private fun testEncodingUTF8String(oid: ASN1ObjectIdentifier, value: String) {
        val converted = createEntryValue(oid, value)
        if (converted !is DERUTF8String) {
            fail("encoding for $oid not IA5String")
        }
        if (value != DERUTF8String.getInstance(converted.toASN1Primitive().encoded).string) {
            fail("decoding not correct")
        }
    }

    private fun testEncodingGeneralizedTime(oid: ASN1ObjectIdentifier, value: String) {
        var converted = createEntryValue(oid, value)
        if (converted !is ASN1GeneralizedTime) {
            fail("encoding for $oid not GeneralizedTime")
        }
        converted = createEntryValueFromString(oid, value)
        if (converted !is ASN1GeneralizedTime) {
            fail("encoding for $oid not GeneralizedTime")
        }
    }

    @Throws(Exception::class)
    override fun performTest() {
        ietfUtilsTest()

        testEncodingPrintableString(BCStyle.C, "AU")
        testEncodingPrintableString(BCStyle.SERIALNUMBER, "123456")
        testEncodingPrintableString(BCStyle.DN_QUALIFIER, "123456")
        testEncodingIA5String(BCStyle.EmailAddress, "test@test.com")
        testEncodingIA5String(BCStyle.DC, "test")
        // correct encoding
        testEncodingGeneralizedTime(BCStyle.DATE_OF_BIRTH, "#180F32303032303132323132323232305A")
        // compatibility encoding
        testEncodingGeneralizedTime(BCStyle.DATE_OF_BIRTH, "20020122122220Z")
        testEncodingUTF8String(BCStyle.CN, "Mörsky")

        //
        // composite
        //
        var builder = X500NameBuilder(BCStyle.INSTANCE)

        builder.addRDN(BCStyle.C, "AU")
        builder.addRDN(BCStyle.O, "The Legion of the Bouncy Castle")
        builder.addRDN(BCStyle.L, "Melbourne")
        builder.addRDN(BCStyle.ST, "Victoria")
        builder.addRDN(BCStyle.E, "feedback-crypto@bouncycastle.org")

        var name1 = builder.build()

        if (name1 != name1) {
            fail("Failed same object test")
        }

        //        if (!name1.equals(name1, true))
        //        {
        //            fail("Failed same object test - in Order");
        //        }

        builder = X500NameBuilder(BCStyle.INSTANCE)

        builder.addRDN(BCStyle.C, "AU")
        builder.addRDN(BCStyle.O, "The Legion of the Bouncy Castle")
        builder.addRDN(BCStyle.L, "Melbourne")
        builder.addRDN(BCStyle.ST, "Victoria")
        builder.addRDN(BCStyle.E, "feedback-crypto@bouncycastle.org")

        var name2 = builder.build()

        if (name1 != name2) {
            fail("Failed same name test")
        }

        //        if (!name1.equals(name2, true))
        //        {
        //            fail("Failed same name test - in Order");
        //        }

        if (name1.hashCode() != name2.hashCode()) {
            fail("Failed same name test - in Order")
        }

        val builder1 = X500NameBuilder(BCStyle.INSTANCE)

        builder.addRDN(BCStyle.C, "AU")
        builder.addRDN(BCStyle.O, "The Legion of the Bouncy Castle")
        builder.addRDN(BCStyle.L, "Melbourne")
        builder.addRDN(BCStyle.ST, "Victoria")
        builder.addRDN(BCStyle.E, "feedback-crypto@bouncycastle.org")

        val builder2 = X500NameBuilder(BCStyle.INSTANCE)

        builder.addRDN(BCStyle.E, "feedback-crypto@bouncycastle.org")
        builder.addRDN(BCStyle.C, "AU")
        builder.addRDN(BCStyle.O, "The Legion of the Bouncy Castle")
        builder.addRDN(BCStyle.L, "Melbourne")
        builder.addRDN(BCStyle.ST, "Victoria")

        name1 = builder1.build()
        name2 = builder2.build()

        if (name1 != name2) {
            fail("Failed reverse name test")
        }

        if (name1.hashCode() != name2.hashCode()) {
            fail("Failed reverse name test hashCode")
        }

        //        if (name1.equals(name2, true))
        //        {
        //            fail("Failed reverse name test - in Order");
        //        }
        //
        //        if (!name1.equals(name2, false))
        //        {
        //            fail("Failed reverse name test - in Order false");
        //        }

        //        Vector oids = name1.getOIDs();
        //        if (!compareVectors(oids, ord1))
        //        {
        //            fail("oid comparison test");
        //        }
        /*
        Vector val1 = new Vector();

        val1.addElement("AU");
        val1.addElement("The Legion of the Bouncy Castle");
        val1.addElement("Melbourne");
        val1.addElement("Victoria");
        val1.addElement("feedback-crypto@bouncycastle.org");

        name1 = new X500Name(ord1, val1);

        Vector values = name1.getValues();
        if (!compareVectors(values, val1))
        {
            fail("value comparison test");
        }

        ord2 = new Vector();

        ord2.addElement(X500Name.ST);
        ord2.addElement(X500Name.ST);
        ord2.addElement(X500Name.L);
        ord2.addElement(X500Name.O);
        ord2.addElement(X500Name.C);

        name1 = new X500Name(ord1, attrs);
        name2 = new X500Name(ord2, attrs);

        if (name1.equals(name2))
        {
            fail("Failed different name test");
        }

        ord2 = new Vector();

        ord2.addElement(X500Name.ST);
        ord2.addElement(X500Name.L);
        ord2.addElement(X500Name.O);
        ord2.addElement(X500Name.C);

        name1 = new X500Name(ord1, attrs);
        name2 = new X500Name(ord2, attrs);

        if (name1.equals(name2))
        {
            fail("Failed subset name test");
        }

        compositeTest();
         */
        var bOut: ByteArrayOutputStream
        var aOut: ASN1OutputStream
        var aIn: ASN1InputStream
        /*
        //
        // getValues test
        //
        Vector v1 = name1.getValues(X500Name.O);

        if (v1.size() != 1 || !v1.elementAt(0).equals("The Legion of the Bouncy Castle"))
        {
            fail("O test failed");
        }

        Vector v2 = name1.getValues(X500Name.L);

        if (v2.size() != 1 || !v2.elementAt(0).equals("Melbourne"))
        {
            fail("L test failed");
        }
       */
        //
        // general subjects test
        //
        for (i in subjects.indices) {
            var name = X500Name(subjects[i])

            bOut = ByteArrayOutputStream()
            aOut = ASN1OutputStream(bOut)

            aOut.writeObject(name)

            aIn = ASN1InputStream(ByteArrayInputStream(bOut.toByteArray()))

            name = X500Name.getInstance(aIn.readObject())
            if (name.toString() != subjects[i]) {
                fail("failed regeneration test " + i + " got: " + name.toString() + " expected " + subjects[i])
            }
        }

        var i = 0
        while (i < hexSubjects.size) {
            var name = X500Name(hexSubjects[i])

            bOut = ByteArrayOutputStream()
            aOut = ASN1OutputStream(bOut)

            aOut.writeObject(name)

            aIn = ASN1InputStream(ByteArrayInputStream(bOut.toByteArray()))

            name = X500Name.getInstance(aIn.readObject())
            if (name.toString() != hexSubjects[i + 1]) {
                fail("failed hex regeneration test " + i + " got: " + name.toString() + " expected " + subjects[i])
            }
            i += 2
        }

        //
        // sort test
        //
        var unsorted = X500Name("SERIALNUMBER=BBB + CN=AA")

        if (fromBytes(unsorted.encoded).toString() != "CN=AA+SERIALNUMBER=BBB") {
            fail("failed sort test 1")
        }

        unsorted = X500Name("CN=AA + SERIALNUMBER=BBB")

        if (fromBytes(unsorted.encoded).toString() != "CN=AA+SERIALNUMBER=BBB") {
            fail("failed sort test 2")
        }

        unsorted = X500Name("SERIALNUMBER=B + CN=AA")

        if (fromBytes(unsorted.encoded).toString() != "SERIALNUMBER=B+CN=AA") {
            fail("failed sort test 3")
        }

        unsorted = X500Name("CN=AA + SERIALNUMBER=B")

        if (fromBytes(unsorted.encoded).toString() != "SERIALNUMBER=B+CN=AA") {
            fail("failed sort test 4")
        }

        //
        // equality tests
        //
        equalityTest(X500Name("CN=The     Legion"), X500Name("CN=The Legion"))
        equalityTest(X500Name("CN=   The Legion"), X500Name("CN=The Legion"))
        equalityTest(X500Name("CN=The Legion   "), X500Name("CN=The Legion"))
        equalityTest(X500Name("CN=  The     Legion "), X500Name("CN=The Legion"))
        equalityTest(X500Name("CN=  the     legion "), X500Name("CN=The Legion"))

        equalityTest(X500Name("CN=  the     legion+C=AU, O=Legion "), X500Name("CN=The Legion+C=AU, O=Legion"))
        // # test

        var n1 = X500Name("SERIALNUMBER=8,O=ABC,CN=ABC Class 3 CA,C=LT")
        var n2 = X500Name("2.5.4.5=8,O=ABC,CN=ABC Class 3 CA,C=LT")
        var n3 = X500Name("2.5.4.5=#130138,O=ABC,CN=ABC Class 3 CA,C=LT")

        equalityTest(n1, n2)
        equalityTest(n2, n3)
        equalityTest(n3, n1)

        n1 = X500Name("2.5.4.5=#130138,CN=SSC Class 3 CA,O=UAB Skaitmeninio sertifikavimo centras,C=LT")
        n2 = X500Name("SERIALNUMBER=#130138,CN=SSC Class 3 CA,O=UAB Skaitmeninio sertifikavimo centras,C=LT")
        n3 = X500Name.getInstance(ASN1Primitive.fromByteArray(Hex.decode("3063310b3009060355040613024c54312f302d060355040a1326" + "55414220536b6169746d656e696e696f20736572746966696b6176696d6f2063656e74726173311730150603550403130e53534320436c6173732033204341310a30080603550405130138")))

        equalityTest(n1, n2)
        equalityTest(n2, n3)
        equalityTest(n3, n1)

        n1 = X500Name("SERIALNUMBER=8,O=XX,CN=ABC Class 3 CA,C=LT")
        n2 = X500Name("2.5.4.5=8,O=,CN=ABC Class 3 CA,C=LT")

        //        if (n1.equals(n2))
        //        {
        //            fail("empty inequality check failed");
        //        }

        n1 = X500Name("SERIALNUMBER=8,O=,CN=ABC Class 3 CA,C=LT")
        n2 = X500Name("2.5.4.5=8,O=,CN=ABC Class 3 CA,C=LT")

        equalityTest(n1, n2)

        equalityTest(X500Name.getInstance(BCStrictStyle.INSTANCE, n1), X500Name.getInstance(BCStrictStyle.INSTANCE, n2))

        n2 = X500Name("C=LT,2.5.4.5=8,O=,CN=ABC Class 3 CA")

        equalityTest(n1, n2)

        if (X500Name.getInstance(BCStrictStyle.INSTANCE, n1) == X500Name.getInstance(BCStrictStyle.INSTANCE, n2)) {
            fail("strict comparison failed")
        }

        //
        // inequality to sequences
        //
        name1 = X500Name("CN=The Legion")

        if (name1 == DERSequence()) {
            fail("inequality test with sequence")
        }

        if (name1 == DERSequence(DERSet())) {
            fail("inequality test with sequence and set")
        }

        var v = ASN1EncodableVector()

        v.add(ASN1ObjectIdentifier("1.1"))
        v.add(ASN1ObjectIdentifier("1.1"))
        if (name1 == DERSequence(DERSet(DERSet(v)))) {
            fail("inequality test with sequence and bad set")
        }

        if (name1 == DERSequence(DERSet(DERSet(v)))) {
            fail("inequality test with sequence and bad set")
        }

        if (name1 == DERSequence(DERSet(DERSequence()))) {
            fail("inequality test with sequence and short sequence")
        }

        if (name1 == DERSequence(DERSet(DERSequence()))) {
            fail("inequality test with sequence and short sequence")
        }

        v = ASN1EncodableVector()

        v.add(ASN1ObjectIdentifier("1.1"))
        v.add(DERSequence())

        if (name1 == DERSequence(DERSet(DERSequence(v)))) {
            fail("inequality test with sequence and bad sequence")
        }

        if (name1 == null) {
            fail("inequality test with null")
        }

        //        if (name1.equals(null, true))
        //        {
        //            fail("inequality test with null");
        //        }

        //
        // this is contrived but it checks sorting of sets with equal elements
        //
        unsorted = X500Name("CN=AA + CN=AA + CN=AA")

        var types = unsorted.attributeTypes
        if (types.size != 3 || types[0] != BCStyle.CN || types[1] != BCStyle.CN || types[2] != BCStyle.CN) {
            fail("types not matched correctly")
        }

        // general type test
        val nested = X500Name("CN=AA + CN=AA, C=AU")

        types = nested.attributeTypes
        if (types.size != 3 || types[0] != BCStyle.CN || types[1] != BCStyle.CN || types[2] != BCStyle.C) {
            fail("nested types not matched correctly")
        }
        //
        // tagging test - only works if CHOICE implemented
        //
        val tag = DERTaggedObject(false, 1, X500Name("CN=AA"))

        if (!tag.isExplicit) {
            fail("failed to explicitly tag CHOICE object")
        }

        val name = X500Name.getInstance(tag, false)

        if (name != X500Name("CN=AA")) {
            fail("failed to recover tagged name")
        }

        val testString = DERUTF8String("The Legion of the Bouncy Castle")
        val encodedBytes = testString.encoded
        val hexEncodedBytes = Hex.encode(encodedBytes)
        val hexEncodedString = "#" + String(hexEncodedBytes)

        var converted = X509DefaultEntryConverter().getConvertedValue(
                BCStyle.L, hexEncodedString) as DERUTF8String

        if (converted != testString) {
            fail("failed X509DefaultEntryConverter test")
        }

        //
        // try escaped.
        //
        converted = X509DefaultEntryConverter().getConvertedValue(
                BCStyle.L, "\\" + hexEncodedString) as DERUTF8String

        if (converted != DERUTF8String(hexEncodedString)) {
            fail("failed X509DefaultEntryConverter test got $converted expected: $hexEncodedString")
        }

        //
        // try a weird value
        //
        var n = X500Name("CN=\\#nothex#string")

        if (n.toString() != "CN=\\#nothex#string") {
            fail("# string not properly escaped.")
        }

        var vls = n.getRDNs(BCStyle.CN)
        if (vls.size != 1 || getValue(vls[0]) != "#nothex#string") {
            fail("escaped # not reduced properly")
        }

        types = n.attributeTypes
        if (types.size != 1 || types[0] != BCStyle.CN) {
            fail("type not matched correctly")
        }

        n = X500Name("CN=\"a+b\"")

        vls = n.getRDNs(BCStyle.CN)
        if (vls.size != 1 || getValue(vls[0]) != "a+b") {
            fail("escaped + not reduced properly")
        }

        n = X500Name("CN=a\\+b")

        vls = n.getRDNs(BCStyle.CN)
        if (vls.size != 1 || getValue(vls[0]) != "a+b") {
            fail("escaped + not reduced properly")
        }

        if (n.toString() != "CN=a\\+b") {
            fail("+ in string not properly escaped.")
        }

        n = X500Name("CN=a\\=b")

        vls = n.getRDNs(BCStyle.CN)
        if (vls.size != 1 || getValue(vls[0]) != "a=b") {
            fail("escaped = not reduced properly")
        }

        if (n.toString() != "CN=a\\=b") {
            fail("= in string not properly escaped.")
        }

        n = X500Name("TELEPHONENUMBER=\"+61999999999\"")

        vls = n.getRDNs(BCStyle.TELEPHONE_NUMBER)
        if (vls.size != 1 || getValue(vls[0]) != "+61999999999") {
            fail("telephonenumber escaped + not reduced properly")
        }

        n = X500Name("TELEPHONENUMBER=\\+61999999999")

        vls = n.getRDNs(BCStyle.TELEPHONE_NUMBER)
        if (vls.size != 1 || getValue(vls[0]) != "+61999999999") {
            fail("telephonenumber escaped + not reduced properly")
        }

        // test query methods
        if ("E" != BCStyle.INSTANCE.oidToDisplayName(BCStyle.EmailAddress)) {
            fail("display name for E incorrect")
        }

        val aliases = BCStyle.INSTANCE.oidToAttrNames(BCStyle.EmailAddress)
        if (aliases.size != 2) {
            fail("no aliases found")
        }
        if (!("e" == aliases[0] || "e" == aliases[1])) {
            fail("first alias name for E incorrect")
        }
        if (!("emailaddress" == aliases[0] || "emailaddress" == aliases[1])) {
            fail("second alias name for E incorrect")
        }

        if (BCStyle.INSTANCE.oidToDisplayName(ASN1ObjectIdentifier("1.2.1")) != null) {
            fail("unknown oid matched!")
        }

        if (BCStyle.INSTANCE.oidToAttrNames(ASN1ObjectIdentifier("1.2.1")).size != 0) {
            fail("unknown oid matched aliases!")
        }

        if (X500Name("CN=\"  CA1 -   CP.04.03\", OU=Testing, OU=Dod, O=U.S. Government, C=US") != X500Name("CN=\"ca1 - CP.04.03  \", OU=Testing, OU=Dod, O=U.S. Government, C=US")) {
            fail("padded equality test failed")
        }
    }

    private fun getValue(vl: RDN): String {
        return (vl.first.value as ASN1String).string
    }

    @Throws(Exception::class)
    private fun ietfUtilsTest() {
        IETFUtils.valueToString(DERUTF8String(" "))
    }

    /*
  private boolean compareVectors(Vector a, Vector b)    // for compatibility with early JDKs
  {
      if (a.size() != b.size())
      {
          return false;
      }

      for (int i = 0; i != a.size(); i++)
      {
          if (!a.elementAt(i).equals(b.elementAt(i)))
          {
              return false;
          }
      }

      return true;
  }

  private void compositeTest()
      throws IOException
  {
      //
      // composite test
      //
      byte[]  enc = Hex.decode("305e310b300906035504061302415531283026060355040a0c1f546865204c6567696f6e206f662074686520426f756e637920436173746c653125301006035504070c094d656c626f75726e653011060355040b0c0a4173636f742056616c65");
      ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(enc));

      X500Name    n = X500Name.getInstance(aIn.readObject());

      if (!n.toString().equals("C=AU,O=The Legion of the Bouncy Castle,L=Melbourne+OU=Ascot Vale"))
      {
          fail("Failed composite to string test got: " + n.toString());
      }

      if (!n.toString(true, X500Name.DefaultSymbols).equals("L=Melbourne+OU=Ascot Vale,O=The Legion of the Bouncy Castle,C=AU"))
      {
          fail("Failed composite to string test got: " + n.toString(true, X500Name.DefaultSymbols));
      }

      n = new X500Name(true, "L=Melbourne+OU=Ascot Vale,O=The Legion of the Bouncy Castle,C=AU");
      if (!n.toString().equals("C=AU,O=The Legion of the Bouncy Castle,L=Melbourne+OU=Ascot Vale"))
      {
          fail("Failed composite to string reversal test got: " + n.toString());
      }

      n = new X500Name("C=AU, O=The Legion of the Bouncy Castle, L=Melbourne + OU=Ascot Vale");

      ByteArrayOutputStream bOut = new ByteArrayOutputStream();
      ASN1OutputStream aOut = new ASN1OutputStream(bOut);

      aOut.writeObject(n);

      byte[]  enc2 = bOut.toByteArray();

      if (!Arrays.areEqual(enc, enc2))
      {
          fail("Failed composite string to encoding test");
      }

      //
      // dud name test - handle empty DN without barfing.
      //
      n = new X500Name("C=CH,O=,OU=dummy,CN=mail@dummy.com");

      n = X500Name.getInstance(ASN1Object.fromByteArray(n.getEncoded()));
  }
    */
    private fun equalityTest(name1: X500Name, name2: X500Name) {
        if (name1 != name2) {
            fail("equality test failed for $name1 : $name2")
        }

        if (name1.hashCode() != name2.hashCode()) {
            fail("hashCodeTest test failed for $name1 : $name2")
        }
    }

    companion object {

        @Throws(IOException::class)
        private fun fromBytes(
                bytes: ByteArray): X500Name {
            return X500Name.getInstance(ASN1InputStream(ByteArrayInputStream(bytes)).readObject())
        }


        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(X500NameTest())
        }
    }
}
