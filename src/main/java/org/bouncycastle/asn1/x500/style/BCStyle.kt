package org.bouncycastle.asn1.x500.style

import java.util.Hashtable

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DERPrintableString
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.x500.RDN
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.X500NameStyle
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers

open class BCStyle protected constructor() : AbstractX500NameStyle() {

    protected val defaultLookUp: Hashtable<Any, Any>
    protected val defaultSymbols: Hashtable<Any, Any>

    init {
        defaultSymbols = AbstractX500NameStyle.copyHashTable(DefaultSymbols)
        defaultLookUp = AbstractX500NameStyle.copyHashTable(DefaultLookUp)
    }

    override fun encodeStringValue(oid: ASN1ObjectIdentifier,
                                   value: String): ASN1Encodable {
        if (oid == EmailAddress || oid == DC) {
            return DERIA5String(value)
        } else if (oid == DATE_OF_BIRTH)
        // accept time string as well as # (for compatibility)
        {
            return ASN1GeneralizedTime(value)
        } else if (oid == C || oid == SN || oid == DN_QUALIFIER
                || oid == TELEPHONE_NUMBER) {
            return DERPrintableString(value)
        }

        return super.encodeStringValue(oid, value)
    }

    override fun oidToDisplayName(oid: ASN1ObjectIdentifier): String {
        return DefaultSymbols.get(oid)
    }

    override fun oidToAttrNames(oid: ASN1ObjectIdentifier): Array<String> {
        return IETFUtils.findAttrNamesForOID(oid, defaultLookUp)
    }

    override fun attrNameToOID(attrName: String): ASN1ObjectIdentifier {
        return IETFUtils.decodeAttrName(attrName, defaultLookUp)
    }

    override fun fromString(dirName: String): Array<RDN> {
        return IETFUtils.rDNsFromString(dirName, this)
    }

    override fun toString(name: X500Name): String {
        val buf = StringBuffer()
        var first = true

        val rdns = name.rdNs

        for (i in rdns.indices) {
            if (first) {
                first = false
            } else {
                buf.append(',')
            }

            IETFUtils.appendRDN(buf, rdns[i], defaultSymbols)
        }

        return buf.toString()
    }

    companion object {
        /**
         * country code - StringType(SIZE(2))
         */
        val C = ASN1ObjectIdentifier("2.5.4.6").intern()

        /**
         * organization - StringType(SIZE(1..64))
         */
        val O = ASN1ObjectIdentifier("2.5.4.10").intern()

        /**
         * organizational unit name - StringType(SIZE(1..64))
         */
        val OU = ASN1ObjectIdentifier("2.5.4.11").intern()

        /**
         * Title
         */
        val T = ASN1ObjectIdentifier("2.5.4.12").intern()

        /**
         * common name - StringType(SIZE(1..64))
         */
        val CN = ASN1ObjectIdentifier("2.5.4.3").intern()

        /**
         * device serial number name - StringType(SIZE(1..64))
         */
        val SN = ASN1ObjectIdentifier("2.5.4.5").intern()

        /**
         * street - StringType(SIZE(1..64))
         */
        val STREET = ASN1ObjectIdentifier("2.5.4.9").intern()

        /**
         * device serial number name - StringType(SIZE(1..64))
         */
        val SERIALNUMBER = SN

        /**
         * locality name - StringType(SIZE(1..64))
         */
        val L = ASN1ObjectIdentifier("2.5.4.7").intern()

        /**
         * state, or province name - StringType(SIZE(1..64))
         */
        val ST = ASN1ObjectIdentifier("2.5.4.8").intern()

        /**
         * Naming attributes of type X520name
         */
        val SURNAME = ASN1ObjectIdentifier("2.5.4.4").intern()
        val GIVENNAME = ASN1ObjectIdentifier("2.5.4.42").intern()
        val INITIALS = ASN1ObjectIdentifier("2.5.4.43").intern()
        val GENERATION = ASN1ObjectIdentifier("2.5.4.44").intern()
        val UNIQUE_IDENTIFIER = ASN1ObjectIdentifier("2.5.4.45").intern()

        /**
         * businessCategory - DirectoryString(SIZE(1..128)
         */
        val BUSINESS_CATEGORY = ASN1ObjectIdentifier(
                "2.5.4.15").intern()

        /**
         * postalCode - DirectoryString(SIZE(1..40)
         */
        val POSTAL_CODE = ASN1ObjectIdentifier(
                "2.5.4.17").intern()

        /**
         * dnQualifier - DirectoryString(SIZE(1..64)
         */
        val DN_QUALIFIER = ASN1ObjectIdentifier(
                "2.5.4.46").intern()

        /**
         * RFC 3039 Pseudonym - DirectoryString(SIZE(1..64)
         */
        val PSEUDONYM = ASN1ObjectIdentifier(
                "2.5.4.65").intern()


        /**
         * RFC 3039 DateOfBirth - GeneralizedTime - YYYYMMDD000000Z
         */
        val DATE_OF_BIRTH = ASN1ObjectIdentifier(
                "1.3.6.1.5.5.7.9.1").intern()

        /**
         * RFC 3039 PlaceOfBirth - DirectoryString(SIZE(1..128)
         */
        val PLACE_OF_BIRTH = ASN1ObjectIdentifier(
                "1.3.6.1.5.5.7.9.2").intern()

        /**
         * RFC 3039 Gender - PrintableString (SIZE(1)) -- "M", "F", "m" or "f"
         */
        val GENDER = ASN1ObjectIdentifier(
                "1.3.6.1.5.5.7.9.3").intern()

        /**
         * RFC 3039 CountryOfCitizenship - PrintableString (SIZE (2)) -- ISO 3166
         * codes only
         */
        val COUNTRY_OF_CITIZENSHIP = ASN1ObjectIdentifier(
                "1.3.6.1.5.5.7.9.4").intern()

        /**
         * RFC 3039 CountryOfResidence - PrintableString (SIZE (2)) -- ISO 3166
         * codes only
         */
        val COUNTRY_OF_RESIDENCE = ASN1ObjectIdentifier(
                "1.3.6.1.5.5.7.9.5").intern()


        /**
         * ISIS-MTT NameAtBirth - DirectoryString(SIZE(1..64)
         */
        val NAME_AT_BIRTH = ASN1ObjectIdentifier("1.3.36.8.3.14").intern()

        /**
         * RFC 3039 PostalAddress - SEQUENCE SIZE (1..6) OF
         * DirectoryString(SIZE(1..30))
         */
        val POSTAL_ADDRESS = ASN1ObjectIdentifier("2.5.4.16").intern()

        /**
         * RFC 2256 dmdName
         */
        val DMD_NAME = ASN1ObjectIdentifier("2.5.4.54").intern()

        /**
         * id-at-telephoneNumber
         */
        val TELEPHONE_NUMBER = X509ObjectIdentifiers.id_at_telephoneNumber

        /**
         * id-at-name
         */
        val NAME = X509ObjectIdentifiers.id_at_name

        /**
         * Email address (RSA PKCS#9 extension) - IA5String.
         *
         * Note: if you're trying to be ultra orthodox, don't use this! It shouldn't be in here.
         */
        val EmailAddress = PKCSObjectIdentifiers.pkcs_9_at_emailAddress

        /**
         * more from PKCS#9
         */
        val UnstructuredName = PKCSObjectIdentifiers.pkcs_9_at_unstructuredName
        val UnstructuredAddress = PKCSObjectIdentifiers.pkcs_9_at_unstructuredAddress

        /**
         * email address in Verisign certificates
         */
        val E = EmailAddress

        /*
    * others...
    */
        val DC = ASN1ObjectIdentifier("0.9.2342.19200300.100.1.25")

        /**
         * LDAP User id.
         */
        val UID = ASN1ObjectIdentifier("0.9.2342.19200300.100.1.1")

        /**
         * default look up table translating OID values into their common symbols following
         * the convention in RFC 2253 with a few extras
         */
        private val DefaultSymbols = Hashtable()

        /**
         * look up table translating common symbols into their OIDS.
         */
        private val DefaultLookUp = Hashtable()

        init {
            DefaultSymbols.put(C, "C")
            DefaultSymbols.put(O, "O")
            DefaultSymbols.put(T, "T")
            DefaultSymbols.put(OU, "OU")
            DefaultSymbols.put(CN, "CN")
            DefaultSymbols.put(L, "L")
            DefaultSymbols.put(ST, "ST")
            DefaultSymbols.put(SN, "SERIALNUMBER")
            DefaultSymbols.put(EmailAddress, "E")
            DefaultSymbols.put(DC, "DC")
            DefaultSymbols.put(UID, "UID")
            DefaultSymbols.put(STREET, "STREET")
            DefaultSymbols.put(SURNAME, "SURNAME")
            DefaultSymbols.put(GIVENNAME, "GIVENNAME")
            DefaultSymbols.put(INITIALS, "INITIALS")
            DefaultSymbols.put(GENERATION, "GENERATION")
            DefaultSymbols.put(UnstructuredAddress, "unstructuredAddress")
            DefaultSymbols.put(UnstructuredName, "unstructuredName")
            DefaultSymbols.put(UNIQUE_IDENTIFIER, "UniqueIdentifier")
            DefaultSymbols.put(DN_QUALIFIER, "DN")
            DefaultSymbols.put(PSEUDONYM, "Pseudonym")
            DefaultSymbols.put(POSTAL_ADDRESS, "PostalAddress")
            DefaultSymbols.put(NAME_AT_BIRTH, "NameAtBirth")
            DefaultSymbols.put(COUNTRY_OF_CITIZENSHIP, "CountryOfCitizenship")
            DefaultSymbols.put(COUNTRY_OF_RESIDENCE, "CountryOfResidence")
            DefaultSymbols.put(GENDER, "Gender")
            DefaultSymbols.put(PLACE_OF_BIRTH, "PlaceOfBirth")
            DefaultSymbols.put(DATE_OF_BIRTH, "DateOfBirth")
            DefaultSymbols.put(POSTAL_CODE, "PostalCode")
            DefaultSymbols.put(BUSINESS_CATEGORY, "BusinessCategory")
            DefaultSymbols.put(TELEPHONE_NUMBER, "TelephoneNumber")
            DefaultSymbols.put(NAME, "Name")

            DefaultLookUp.put("c", C)
            DefaultLookUp.put("o", O)
            DefaultLookUp.put("t", T)
            DefaultLookUp.put("ou", OU)
            DefaultLookUp.put("cn", CN)
            DefaultLookUp.put("l", L)
            DefaultLookUp.put("st", ST)
            DefaultLookUp.put("sn", SN)
            DefaultLookUp.put("serialnumber", SN)
            DefaultLookUp.put("street", STREET)
            DefaultLookUp.put("emailaddress", E)
            DefaultLookUp.put("dc", DC)
            DefaultLookUp.put("e", E)
            DefaultLookUp.put("uid", UID)
            DefaultLookUp.put("surname", SURNAME)
            DefaultLookUp.put("givenname", GIVENNAME)
            DefaultLookUp.put("initials", INITIALS)
            DefaultLookUp.put("generation", GENERATION)
            DefaultLookUp.put("unstructuredaddress", UnstructuredAddress)
            DefaultLookUp.put("unstructuredname", UnstructuredName)
            DefaultLookUp.put("uniqueidentifier", UNIQUE_IDENTIFIER)
            DefaultLookUp.put("dn", DN_QUALIFIER)
            DefaultLookUp.put("pseudonym", PSEUDONYM)
            DefaultLookUp.put("postaladdress", POSTAL_ADDRESS)
            DefaultLookUp.put("nameofbirth", NAME_AT_BIRTH)
            DefaultLookUp.put("countryofcitizenship", COUNTRY_OF_CITIZENSHIP)
            DefaultLookUp.put("countryofresidence", COUNTRY_OF_RESIDENCE)
            DefaultLookUp.put("gender", GENDER)
            DefaultLookUp.put("placeofbirth", PLACE_OF_BIRTH)
            DefaultLookUp.put("dateofbirth", DATE_OF_BIRTH)
            DefaultLookUp.put("postalcode", POSTAL_CODE)
            DefaultLookUp.put("businesscategory", BUSINESS_CATEGORY)
            DefaultLookUp.put("telephonenumber", TELEPHONE_NUMBER)
            DefaultLookUp.put("name", NAME)
        }

        /**
         * Singleton instance.
         */
        val INSTANCE: X500NameStyle = BCStyle()
    }


}
