package org.bouncycastle.asn1.x500.style

import java.util.Hashtable

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DERPrintableString
import org.bouncycastle.asn1.x500.RDN
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.X500NameStyle

class RFC4519Style protected constructor() : AbstractX500NameStyle() {

    protected val defaultLookUp: Hashtable<Any, Any>
    protected val defaultSymbols: Hashtable<Any, Any>

    init {
        defaultSymbols = AbstractX500NameStyle.copyHashTable(DefaultSymbols)
        defaultLookUp = AbstractX500NameStyle.copyHashTable(DefaultLookUp)
    }

    override fun encodeStringValue(oid: ASN1ObjectIdentifier,
                                   value: String): ASN1Encodable {
        if (oid == dc) {
            return DERIA5String(value)
        } else if (oid == c || oid == serialNumber || oid == dnQualifier
                || oid == telephoneNumber) {
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

    // parse backwards
    override fun fromString(dirName: String): Array<RDN> {
        val tmp = IETFUtils.rDNsFromString(dirName, this)
        val res = arrayOfNulls<RDN>(tmp.size)

        for (i in tmp.indices) {
            res[res.size - i - 1] = tmp[i]
        }

        return res
    }

    // convert in reverse
    override fun toString(name: X500Name): String {
        val buf = StringBuffer()
        var first = true

        val rdns = name.rdNs

        for (i in rdns.indices.reversed()) {
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
        val businessCategory = ASN1ObjectIdentifier("2.5.4.15").intern()
        val c = ASN1ObjectIdentifier("2.5.4.6").intern()
        val cn = ASN1ObjectIdentifier("2.5.4.3").intern()
        val dc = ASN1ObjectIdentifier("0.9.2342.19200300.100.1.25").intern()
        val description = ASN1ObjectIdentifier("2.5.4.13").intern()
        val destinationIndicator = ASN1ObjectIdentifier("2.5.4.27").intern()
        val distinguishedName = ASN1ObjectIdentifier("2.5.4.49").intern()
        val dnQualifier = ASN1ObjectIdentifier("2.5.4.46").intern()
        val enhancedSearchGuide = ASN1ObjectIdentifier("2.5.4.47").intern()
        val facsimileTelephoneNumber = ASN1ObjectIdentifier("2.5.4.23").intern()
        val generationQualifier = ASN1ObjectIdentifier("2.5.4.44").intern()
        val givenName = ASN1ObjectIdentifier("2.5.4.42").intern()
        val houseIdentifier = ASN1ObjectIdentifier("2.5.4.51").intern()
        val initials = ASN1ObjectIdentifier("2.5.4.43").intern()
        val internationalISDNNumber = ASN1ObjectIdentifier("2.5.4.25").intern()
        val l = ASN1ObjectIdentifier("2.5.4.7").intern()
        val member = ASN1ObjectIdentifier("2.5.4.31").intern()
        val name = ASN1ObjectIdentifier("2.5.4.41").intern()
        val o = ASN1ObjectIdentifier("2.5.4.10").intern()
        val ou = ASN1ObjectIdentifier("2.5.4.11").intern()
        val owner = ASN1ObjectIdentifier("2.5.4.32").intern()
        val physicalDeliveryOfficeName = ASN1ObjectIdentifier("2.5.4.19").intern()
        val postalAddress = ASN1ObjectIdentifier("2.5.4.16").intern()
        val postalCode = ASN1ObjectIdentifier("2.5.4.17").intern()
        val postOfficeBox = ASN1ObjectIdentifier("2.5.4.18").intern()
        val preferredDeliveryMethod = ASN1ObjectIdentifier("2.5.4.28").intern()
        val registeredAddress = ASN1ObjectIdentifier("2.5.4.26").intern()
        val roleOccupant = ASN1ObjectIdentifier("2.5.4.33").intern()
        val searchGuide = ASN1ObjectIdentifier("2.5.4.14").intern()
        val seeAlso = ASN1ObjectIdentifier("2.5.4.34").intern()
        val serialNumber = ASN1ObjectIdentifier("2.5.4.5").intern()
        val sn = ASN1ObjectIdentifier("2.5.4.4").intern()
        val st = ASN1ObjectIdentifier("2.5.4.8").intern()
        val street = ASN1ObjectIdentifier("2.5.4.9").intern()
        val telephoneNumber = ASN1ObjectIdentifier("2.5.4.20").intern()
        val teletexTerminalIdentifier = ASN1ObjectIdentifier("2.5.4.22").intern()
        val telexNumber = ASN1ObjectIdentifier("2.5.4.21").intern()
        val title = ASN1ObjectIdentifier("2.5.4.12").intern()
        val uid = ASN1ObjectIdentifier("0.9.2342.19200300.100.1.1").intern()
        val uniqueMember = ASN1ObjectIdentifier("2.5.4.50").intern()
        val userPassword = ASN1ObjectIdentifier("2.5.4.35").intern()
        val x121Address = ASN1ObjectIdentifier("2.5.4.24").intern()
        val x500UniqueIdentifier = ASN1ObjectIdentifier("2.5.4.45").intern()

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
            DefaultSymbols.put(businessCategory, "businessCategory")
            DefaultSymbols.put(c, "c")
            DefaultSymbols.put(cn, "cn")
            DefaultSymbols.put(dc, "dc")
            DefaultSymbols.put(description, "description")
            DefaultSymbols.put(destinationIndicator, "destinationIndicator")
            DefaultSymbols.put(distinguishedName, "distinguishedName")
            DefaultSymbols.put(dnQualifier, "dnQualifier")
            DefaultSymbols.put(enhancedSearchGuide, "enhancedSearchGuide")
            DefaultSymbols.put(facsimileTelephoneNumber, "facsimileTelephoneNumber")
            DefaultSymbols.put(generationQualifier, "generationQualifier")
            DefaultSymbols.put(givenName, "givenName")
            DefaultSymbols.put(houseIdentifier, "houseIdentifier")
            DefaultSymbols.put(initials, "initials")
            DefaultSymbols.put(internationalISDNNumber, "internationalISDNNumber")
            DefaultSymbols.put(l, "l")
            DefaultSymbols.put(member, "member")
            DefaultSymbols.put(name, "name")
            DefaultSymbols.put(o, "o")
            DefaultSymbols.put(ou, "ou")
            DefaultSymbols.put(owner, "owner")
            DefaultSymbols.put(physicalDeliveryOfficeName, "physicalDeliveryOfficeName")
            DefaultSymbols.put(postalAddress, "postalAddress")
            DefaultSymbols.put(postalCode, "postalCode")
            DefaultSymbols.put(postOfficeBox, "postOfficeBox")
            DefaultSymbols.put(preferredDeliveryMethod, "preferredDeliveryMethod")
            DefaultSymbols.put(registeredAddress, "registeredAddress")
            DefaultSymbols.put(roleOccupant, "roleOccupant")
            DefaultSymbols.put(searchGuide, "searchGuide")
            DefaultSymbols.put(seeAlso, "seeAlso")
            DefaultSymbols.put(serialNumber, "serialNumber")
            DefaultSymbols.put(sn, "sn")
            DefaultSymbols.put(st, "st")
            DefaultSymbols.put(street, "street")
            DefaultSymbols.put(telephoneNumber, "telephoneNumber")
            DefaultSymbols.put(teletexTerminalIdentifier, "teletexTerminalIdentifier")
            DefaultSymbols.put(telexNumber, "telexNumber")
            DefaultSymbols.put(title, "title")
            DefaultSymbols.put(uid, "uid")
            DefaultSymbols.put(uniqueMember, "uniqueMember")
            DefaultSymbols.put(userPassword, "userPassword")
            DefaultSymbols.put(x121Address, "x121Address")
            DefaultSymbols.put(x500UniqueIdentifier, "x500UniqueIdentifier")

            DefaultLookUp.put("businesscategory", businessCategory)
            DefaultLookUp.put("c", c)
            DefaultLookUp.put("cn", cn)
            DefaultLookUp.put("dc", dc)
            DefaultLookUp.put("description", description)
            DefaultLookUp.put("destinationindicator", destinationIndicator)
            DefaultLookUp.put("distinguishedname", distinguishedName)
            DefaultLookUp.put("dnqualifier", dnQualifier)
            DefaultLookUp.put("enhancedsearchguide", enhancedSearchGuide)
            DefaultLookUp.put("facsimiletelephonenumber", facsimileTelephoneNumber)
            DefaultLookUp.put("generationqualifier", generationQualifier)
            DefaultLookUp.put("givenname", givenName)
            DefaultLookUp.put("houseidentifier", houseIdentifier)
            DefaultLookUp.put("initials", initials)
            DefaultLookUp.put("internationalisdnnumber", internationalISDNNumber)
            DefaultLookUp.put("l", l)
            DefaultLookUp.put("member", member)
            DefaultLookUp.put("name", name)
            DefaultLookUp.put("o", o)
            DefaultLookUp.put("ou", ou)
            DefaultLookUp.put("owner", owner)
            DefaultLookUp.put("physicaldeliveryofficename", physicalDeliveryOfficeName)
            DefaultLookUp.put("postaladdress", postalAddress)
            DefaultLookUp.put("postalcode", postalCode)
            DefaultLookUp.put("postofficebox", postOfficeBox)
            DefaultLookUp.put("preferreddeliverymethod", preferredDeliveryMethod)
            DefaultLookUp.put("registeredaddress", registeredAddress)
            DefaultLookUp.put("roleoccupant", roleOccupant)
            DefaultLookUp.put("searchguide", searchGuide)
            DefaultLookUp.put("seealso", seeAlso)
            DefaultLookUp.put("serialnumber", serialNumber)
            DefaultLookUp.put("sn", sn)
            DefaultLookUp.put("st", st)
            DefaultLookUp.put("street", street)
            DefaultLookUp.put("telephonenumber", telephoneNumber)
            DefaultLookUp.put("teletexterminalidentifier", teletexTerminalIdentifier)
            DefaultLookUp.put("telexnumber", telexNumber)
            DefaultLookUp.put("title", title)
            DefaultLookUp.put("uid", uid)
            DefaultLookUp.put("uniquemember", uniqueMember)
            DefaultLookUp.put("userpassword", userPassword)
            DefaultLookUp.put("x121address", x121Address)
            DefaultLookUp.put("x500uniqueidentifier", x500UniqueIdentifier)

            // TODO: need to add correct matching for equality comparisons.
        }

        /**
         * Singleton instance.
         */
        val INSTANCE: X500NameStyle = RFC4519Style()
    }


}
