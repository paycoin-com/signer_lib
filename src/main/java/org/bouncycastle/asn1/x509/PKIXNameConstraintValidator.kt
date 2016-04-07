package org.bouncycastle.asn1.x509

import java.util.Collections
import java.util.HashMap
import java.util.HashSet

import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.util.Arrays
import org.bouncycastle.util.Integers
import org.bouncycastle.util.Strings

class PKIXNameConstraintValidator : NameConstraintValidator {
    private var excludedSubtreesDN: MutableSet<Any> = HashSet()

    private var excludedSubtreesDNS: MutableSet<Any> = HashSet()

    private var excludedSubtreesEmail: MutableSet<Any> = HashSet()

    private var excludedSubtreesURI: MutableSet<Any> = HashSet()

    private var excludedSubtreesIP: MutableSet<Any> = HashSet()

    private var permittedSubtreesDN: Set<Any>? = null

    private var permittedSubtreesDNS: Set<Any>? = null

    private var permittedSubtreesEmail: Set<Any>? = null

    private var permittedSubtreesURI: Set<Any>? = null

    private var permittedSubtreesIP: Set<Any>? = null

    /**
     * Checks if the given GeneralName is in the permitted set.

     * @param name The GeneralName
     * *
     * @throws NameConstraintValidatorException If the `name`
     */
    @Throws(NameConstraintValidatorException::class)
    override fun checkPermitted(name: GeneralName) {
        when (name.tagNo) {
            GeneralName.rfc822Name -> checkPermittedEmail(permittedSubtreesEmail,
                    extractNameAsString(name))
            GeneralName.dNSName -> checkPermittedDNS(permittedSubtreesDNS, DERIA5String.getInstance(
                    name.name).string)
            GeneralName.directoryName -> checkPermittedDN(X500Name.getInstance(name.name))
            GeneralName.uniformResourceIdentifier -> checkPermittedURI(permittedSubtreesURI, DERIA5String.getInstance(
                    name.name).string)
            GeneralName.iPAddress -> {
                val ip = ASN1OctetString.getInstance(name.name).octets

                checkPermittedIP(permittedSubtreesIP, ip)
            }
        }
    }

    /**
     * Check if the given GeneralName is contained in the excluded set.

     * @param name The GeneralName.
     * *
     * @throws NameConstraintValidatorException If the `name` is
     * * excluded.
     */
    @Throws(NameConstraintValidatorException::class)
    override fun checkExcluded(name: GeneralName) {
        when (name.tagNo) {
            GeneralName.rfc822Name -> checkExcludedEmail(excludedSubtreesEmail, extractNameAsString(name))
            GeneralName.dNSName -> checkExcludedDNS(excludedSubtreesDNS, DERIA5String.getInstance(
                    name.name).string)
            GeneralName.directoryName -> checkExcludedDN(X500Name.getInstance(name.name))
            GeneralName.uniformResourceIdentifier -> checkExcludedURI(excludedSubtreesURI, DERIA5String.getInstance(
                    name.name).string)
            GeneralName.iPAddress -> {
                val ip = ASN1OctetString.getInstance(name.name).octets

                checkExcludedIP(excludedSubtreesIP, ip)
            }
        }
    }

    override fun intersectPermittedSubtree(permitted: GeneralSubtree) {
        intersectPermittedSubtree(arrayOf(permitted))
    }

    /**
     * Updates the permitted set of these name constraints with the intersection
     * with the given subtree.

     * @param permitted The permitted subtrees
     */
    override fun intersectPermittedSubtree(permitted: Array<GeneralSubtree>) {
        val subtreesMap = HashMap()

        // group in sets in a map ordered by tag no.
        for (i in permitted.indices) {
            val subtree = permitted[i]
            val tagNo = Integers.valueOf(subtree.base.tagNo)
            if (subtreesMap.get(tagNo) == null) {
                subtreesMap.put(tagNo, HashSet())
            }
            (subtreesMap.get(tagNo) as Set<Any>).add(subtree)
        }

        val it = subtreesMap.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next() as Entry<Any, Any>

            // go through all subtree groups
            when ((entry.key as Int).toInt()) {
                GeneralName.rfc822Name -> permittedSubtreesEmail = intersectEmail(permittedSubtreesEmail,
                        entry.value as Set<Any>)
                GeneralName.dNSName -> permittedSubtreesDNS = intersectDNS(permittedSubtreesDNS,
                        entry.value as Set<Any>)
                GeneralName.directoryName -> permittedSubtreesDN = intersectDN(permittedSubtreesDN,
                        entry.value as Set<Any>)
                GeneralName.uniformResourceIdentifier -> permittedSubtreesURI = intersectURI(permittedSubtreesURI,
                        entry.value as Set<Any>)
                GeneralName.iPAddress -> permittedSubtreesIP = intersectIP(permittedSubtreesIP,
                        entry.value as Set<Any>)
            }
        }
    }

    override fun intersectEmptyPermittedSubtree(nameType: Int) {
        when (nameType) {
            GeneralName.rfc822Name -> permittedSubtreesEmail = HashSet()
            GeneralName.dNSName -> permittedSubtreesDNS = HashSet()
            GeneralName.directoryName -> permittedSubtreesDN = HashSet()
            GeneralName.uniformResourceIdentifier -> permittedSubtreesURI = HashSet()
            GeneralName.iPAddress -> permittedSubtreesIP = HashSet()
        }
    }

    /**
     * Adds a subtree to the excluded set of these name constraints.

     * @param subtree A subtree with an excluded GeneralName.
     */
    override fun addExcludedSubtree(subtree: GeneralSubtree) {
        val base = subtree.base

        when (base.tagNo) {
            GeneralName.rfc822Name -> excludedSubtreesEmail = unionEmail(excludedSubtreesEmail,
                    extractNameAsString(base))
            GeneralName.dNSName -> excludedSubtreesDNS = unionDNS(excludedSubtreesDNS,
                    extractNameAsString(base))
            GeneralName.directoryName -> excludedSubtreesDN = unionDN(excludedSubtreesDN,
                    base.name.toASN1Primitive() as ASN1Sequence)
            GeneralName.uniformResourceIdentifier -> excludedSubtreesURI = unionURI(excludedSubtreesURI,
                    extractNameAsString(base))
            GeneralName.iPAddress -> excludedSubtreesIP = unionIP(excludedSubtreesIP, ASN1OctetString.getInstance(base.name).octets)
        }
    }

    override fun hashCode(): Int {
        return hashCollection(excludedSubtreesDN)
        +hashCollection(excludedSubtreesDNS)
        +hashCollection(excludedSubtreesEmail)
        +hashCollection(excludedSubtreesIP)
        +hashCollection(excludedSubtreesURI)
        +hashCollection(permittedSubtreesDN)
        +hashCollection(permittedSubtreesDNS)
        +hashCollection(permittedSubtreesEmail)
        +hashCollection(permittedSubtreesIP)
        +hashCollection(permittedSubtreesURI)
    }

    override fun equals(o: Any?): Boolean {
        if (o !is PKIXNameConstraintValidator) {
            return false
        }
        return collectionsAreEqual(o.excludedSubtreesDN, excludedSubtreesDN)
                && collectionsAreEqual(o.excludedSubtreesDNS, excludedSubtreesDNS)
                && collectionsAreEqual(o.excludedSubtreesEmail, excludedSubtreesEmail)
                && collectionsAreEqual(o.excludedSubtreesIP, excludedSubtreesIP)
                && collectionsAreEqual(o.excludedSubtreesURI, excludedSubtreesURI)
                && collectionsAreEqual(o.permittedSubtreesDN, permittedSubtreesDN)
                && collectionsAreEqual(o.permittedSubtreesDNS, permittedSubtreesDNS)
                && collectionsAreEqual(o.permittedSubtreesEmail, permittedSubtreesEmail)
                && collectionsAreEqual(o.permittedSubtreesIP, permittedSubtreesIP)
                && collectionsAreEqual(o.permittedSubtreesURI, permittedSubtreesURI)
    }

    override fun toString(): String {
        var temp = ""
        temp += "permitted:\n"
        if (permittedSubtreesDN != null) {
            temp += "DN:\n"
            temp += permittedSubtreesDN!!.toString() + "\n"
        }
        if (permittedSubtreesDNS != null) {
            temp += "DNS:\n"
            temp += permittedSubtreesDNS!!.toString() + "\n"
        }
        if (permittedSubtreesEmail != null) {
            temp += "Email:\n"
            temp += permittedSubtreesEmail!!.toString() + "\n"
        }
        if (permittedSubtreesURI != null) {
            temp += "URI:\n"
            temp += permittedSubtreesURI!!.toString() + "\n"
        }
        if (permittedSubtreesIP != null) {
            temp += "IP:\n"
            temp += stringifyIPCollection(permittedSubtreesIP) + "\n"
        }
        temp += "excluded:\n"
        if (!excludedSubtreesDN.isEmpty()) {
            temp += "DN:\n"
            temp += excludedSubtreesDN.toString() + "\n"
        }
        if (!excludedSubtreesDNS.isEmpty()) {
            temp += "DNS:\n"
            temp += excludedSubtreesDNS.toString() + "\n"
        }
        if (!excludedSubtreesEmail.isEmpty()) {
            temp += "Email:\n"
            temp += excludedSubtreesEmail.toString() + "\n"
        }
        if (!excludedSubtreesURI.isEmpty()) {
            temp += "URI:\n"
            temp += excludedSubtreesURI.toString() + "\n"
        }
        if (!excludedSubtreesIP.isEmpty()) {
            temp += "IP:\n"
            temp += stringifyIPCollection(excludedSubtreesIP) + "\n"
        }
        return temp
    }

    @Throws(NameConstraintValidatorException::class)
    private fun checkPermittedDN(dns: X500Name) {
        checkPermittedDN(permittedSubtreesDN, ASN1Sequence.getInstance(dns.toASN1Primitive()))
    }

    @Throws(NameConstraintValidatorException::class)
    private fun checkExcludedDN(dns: X500Name) {
        checkExcludedDN(excludedSubtreesDN, ASN1Sequence.getInstance(dns))
    }

    private fun withinDNSubtree(
            dns: ASN1Sequence,
            subtree: ASN1Sequence): Boolean {
        if (subtree.size() < 1) {
            return false
        }

        if (subtree.size() > dns.size()) {
            return false
        }

        for (j in subtree.size() - 1 downTo 0) {
            if (subtree.getObjectAt(j) != dns.getObjectAt(j)) {
                return false
            }
        }

        return true
    }

    @Throws(NameConstraintValidatorException::class)
    private fun checkPermittedDN(permitted: Set<Any>?, dns: ASN1Sequence) {
        if (permitted == null) {
            return
        }

        if (permitted.isEmpty() && dns.size() == 0) {
            return
        }
        val it = permitted.iterator()

        while (it.hasNext()) {
            val subtree = it.next() as ASN1Sequence

            if (withinDNSubtree(dns, subtree)) {
                return
            }
        }

        throw NameConstraintValidatorException(
                "Subject distinguished name is not from a permitted subtree")
    }

    @Throws(NameConstraintValidatorException::class)
    private fun checkExcludedDN(excluded: Set<Any>, dns: ASN1Sequence) {
        if (excluded.isEmpty()) {
            return
        }

        val it = excluded.iterator()

        while (it.hasNext()) {
            val subtree = it.next() as ASN1Sequence

            if (withinDNSubtree(dns, subtree)) {
                throw NameConstraintValidatorException(
                        "Subject distinguished name is from an excluded subtree")
            }
        }
    }

    private fun intersectDN(permitted: Set<Any>?, dns: Set<Any>): Set<Any> {
        val intersect = HashSet()
        val it = dns.iterator()
        while (it.hasNext()) {
            val dn = ASN1Sequence.getInstance((it.next() as GeneralSubtree).base.name.toASN1Primitive())
            if (permitted == null) {
                if (dn != null) {
                    intersect.add(dn)
                }
            } else {
                val _iter = permitted.iterator()
                while (_iter.hasNext()) {
                    val subtree = _iter.next() as ASN1Sequence

                    if (withinDNSubtree(dn, subtree)) {
                        intersect.add(dn)
                    } else if (withinDNSubtree(subtree, dn)) {
                        intersect.add(subtree)
                    }
                }
            }
        }
        return intersect
    }

    private fun unionDN(excluded: MutableSet<Any>, dn: ASN1Sequence?): MutableSet<Any> {
        if (excluded.isEmpty()) {
            if (dn == null) {
                return excluded
            }
            excluded.add(dn)

            return excluded
        } else {
            val intersect = HashSet()

            val it = excluded.iterator()
            while (it.hasNext()) {
                val subtree = it.next() as ASN1Sequence

                if (withinDNSubtree(dn, subtree)) {
                    intersect.add(subtree)
                } else if (withinDNSubtree(subtree, dn)) {
                    intersect.add(dn)
                } else {
                    intersect.add(subtree)
                    intersect.add(dn)
                }
            }

            return intersect
        }
    }

    private fun intersectEmail(permitted: Set<Any>?, emails: Set<Any>): Set<Any> {
        val intersect = HashSet()
        val it = emails.iterator()
        while (it.hasNext()) {
            val email = extractNameAsString((it.next() as GeneralSubtree).base)

            if (permitted == null) {
                if (email != null) {
                    intersect.add(email)
                }
            } else {
                val it2 = permitted.iterator()
                while (it2.hasNext()) {
                    val _permitted = it2.next() as String

                    intersectEmail(email, _permitted, intersect)
                }
            }
        }
        return intersect
    }

    private fun unionEmail(excluded: MutableSet<Any>, email: String?): MutableSet<Any> {
        if (excluded.isEmpty()) {
            if (email == null) {
                return excluded
            }
            excluded.add(email)
            return excluded
        } else {
            val union = HashSet()

            val it = excluded.iterator()
            while (it.hasNext()) {
                val _excluded = it.next() as String

                unionEmail(_excluded, email, union)
            }

            return union
        }
    }

    /**
     * Returns the intersection of the permitted IP ranges in
     * `permitted` with `ip`.

     * @param permitted A `Set` of permitted IP addresses with
     * *                  their subnet mask as byte arrays.
     * *
     * @param ips       The IP address with its subnet mask.
     * *
     * @return The `Set` of permitted IP ranges intersected with
     * * `ip`.
     */
    private fun intersectIP(permitted: Set<Any>?, ips: Set<Any>): Set<Any> {
        val intersect = HashSet()
        val it = ips.iterator()
        while (it.hasNext()) {
            val ip = ASN1OctetString.getInstance(
                    (it.next() as GeneralSubtree).base.name).octets
            if (permitted == null) {
                if (ip != null) {
                    intersect.add(ip)
                }
            } else {
                val it2 = permitted.iterator()
                while (it2.hasNext()) {
                    val _permitted = it2.next() as ByteArray
                    intersect.addAll(intersectIPRange(_permitted, ip))
                }
            }
        }
        return intersect
    }

    /**
     * Returns the union of the excluded IP ranges in `excluded`
     * with `ip`.

     * @param excluded A `Set` of excluded IP addresses with their
     * *                 subnet mask as byte arrays.
     * *
     * @param ip       The IP address with its subnet mask.
     * *
     * @return The `Set` of excluded IP ranges unified with
     * * `ip` as byte arrays.
     */
    private fun unionIP(excluded: MutableSet<Any>, ip: ByteArray?): MutableSet<Any> {
        if (excluded.isEmpty()) {
            if (ip == null) {
                return excluded
            }
            excluded.add(ip)

            return excluded
        } else {
            val union = HashSet()

            val it = excluded.iterator()
            while (it.hasNext()) {
                val _excluded = it.next() as ByteArray
                union.addAll(unionIPRange(_excluded, ip))
            }

            return union
        }
    }

    /**
     * Calculates the union if two IP ranges.

     * @param ipWithSubmask1 The first IP address with its subnet mask.
     * *
     * @param ipWithSubmask2 The second IP address with its subnet mask.
     * *
     * @return A `Set` with the union of both addresses.
     */
    private fun unionIPRange(ipWithSubmask1: ByteArray, ipWithSubmask2: ByteArray): Set<Any> {
        val set = HashSet()

        // difficult, adding always all IPs is not wrong
        if (Arrays.areEqual(ipWithSubmask1, ipWithSubmask2)) {
            set.add(ipWithSubmask1)
        } else {
            set.add(ipWithSubmask1)
            set.add(ipWithSubmask2)
        }
        return set
    }

    /**
     * Calculates the interesction if two IP ranges.

     * @param ipWithSubmask1 The first IP address with its subnet mask.
     * *
     * @param ipWithSubmask2 The second IP address with its subnet mask.
     * *
     * @return A `Set` with the single IP address with its subnet
     * * mask as a byte array or an empty `Set`.
     */
    private fun intersectIPRange(ipWithSubmask1: ByteArray, ipWithSubmask2: ByteArray): Set<Any> {
        if (ipWithSubmask1.size != ipWithSubmask2.size) {
            return Collections.EMPTY_SET
        }
        val temp = extractIPsAndSubnetMasks(ipWithSubmask1, ipWithSubmask2)
        val ip1 = temp[0]
        val subnetmask1 = temp[1]
        val ip2 = temp[2]
        val subnetmask2 = temp[3]

        val minMax = minMaxIPs(ip1, subnetmask1, ip2, subnetmask2)
        val min: ByteArray
        val max: ByteArray
        max = min(minMax[1], minMax[3])
        min = max(minMax[0], minMax[2])

        // minimum IP address must be bigger than max
        if (compareTo(min, max) == 1) {
            return Collections.EMPTY_SET
        }
        // OR keeps all significant bits
        val ip = or(minMax[0], minMax[2])
        val subnetmask = or(subnetmask1, subnetmask2)
        return setOf(ipWithSubnetMask(ip, subnetmask))
    }

    /**
     * Concatenates the IP address with its subnet mask.

     * @param ip         The IP address.
     * *
     * @param subnetMask Its subnet mask.
     * *
     * @return The concatenated IP address with its subnet mask.
     */
    private fun ipWithSubnetMask(ip: ByteArray, subnetMask: ByteArray): ByteArray {
        val ipLength = ip.size
        val temp = ByteArray(ipLength * 2)
        System.arraycopy(ip, 0, temp, 0, ipLength)
        System.arraycopy(subnetMask, 0, temp, ipLength, ipLength)
        return temp
    }

    /**
     * Splits the IP addresses and their subnet mask.

     * @param ipWithSubmask1 The first IP address with the subnet mask.
     * *
     * @param ipWithSubmask2 The second IP address with the subnet mask.
     * *
     * @return An array with two elements. Each element contains the IP address
     * * and the subnet mask in this order.
     */
    private fun extractIPsAndSubnetMasks(
            ipWithSubmask1: ByteArray,
            ipWithSubmask2: ByteArray): Array<ByteArray> {
        val ipLength = ipWithSubmask1.size / 2
        val ip1 = ByteArray(ipLength)
        val subnetmask1 = ByteArray(ipLength)
        System.arraycopy(ipWithSubmask1, 0, ip1, 0, ipLength)
        System.arraycopy(ipWithSubmask1, ipLength, subnetmask1, 0, ipLength)

        val ip2 = ByteArray(ipLength)
        val subnetmask2 = ByteArray(ipLength)
        System.arraycopy(ipWithSubmask2, 0, ip2, 0, ipLength)
        System.arraycopy(ipWithSubmask2, ipLength, subnetmask2, 0, ipLength)
        return arrayOf(ip1, subnetmask1, ip2, subnetmask2)
    }

    /**
     * Based on the two IP addresses and their subnet masks the IP range is
     * computed for each IP address - subnet mask pair and returned as the
     * minimum IP address and the maximum address of the range.

     * @param ip1         The first IP address.
     * *
     * @param subnetmask1 The subnet mask of the first IP address.
     * *
     * @param ip2         The second IP address.
     * *
     * @param subnetmask2 The subnet mask of the second IP address.
     * *
     * @return A array with two elements. The first/second element contains the
     * * min and max IP address of the first/second IP address and its
     * * subnet mask.
     */
    private fun minMaxIPs(
            ip1: ByteArray,
            subnetmask1: ByteArray,
            ip2: ByteArray,
            subnetmask2: ByteArray): Array<ByteArray> {
        val ipLength = ip1.size
        val min1 = ByteArray(ipLength)
        val max1 = ByteArray(ipLength)

        val min2 = ByteArray(ipLength)
        val max2 = ByteArray(ipLength)

        for (i in 0..ipLength - 1) {
            min1[i] = (ip1[i] and subnetmask1[i]).toByte()
            max1[i] = (ip1[i] and subnetmask1[i] or subnetmask1[i].inv()).toByte()

            min2[i] = (ip2[i] and subnetmask2[i]).toByte()
            max2[i] = (ip2[i] and subnetmask2[i] or subnetmask2[i].inv()).toByte()
        }

        return arrayOf(min1, max1, min2, max2)
    }

    @Throws(NameConstraintValidatorException::class)
    private fun checkPermittedEmail(permitted: Set<Any>?, email: String) {
        if (permitted == null) {
            return
        }

        val it = permitted.iterator()

        while (it.hasNext()) {
            val str = it.next() as String

            if (emailIsConstrained(email, str)) {
                return
            }
        }

        if (email.length == 0 && permitted.size == 0) {
            return
        }

        throw NameConstraintValidatorException(
                "Subject email address is not from a permitted subtree.")
    }

    @Throws(NameConstraintValidatorException::class)
    private fun checkExcludedEmail(excluded: Set<Any>, email: String) {
        if (excluded.isEmpty()) {
            return
        }

        val it = excluded.iterator()

        while (it.hasNext()) {
            val str = it.next() as String

            if (emailIsConstrained(email, str)) {
                throw NameConstraintValidatorException(
                        "Email address is from an excluded subtree.")
            }
        }
    }

    /**
     * Checks if the IP `ip` is included in the permitted set
     * `permitted`.

     * @param permitted A `Set` of permitted IP addresses with
     * *                  their subnet mask as byte arrays.
     * *
     * @param ip        The IP address.
     * *
     * @throws NameConstraintValidatorException if the IP is not permitted.
     */
    @Throws(NameConstraintValidatorException::class)
    private fun checkPermittedIP(permitted: Set<Any>?, ip: ByteArray) {
        if (permitted == null) {
            return
        }

        val it = permitted.iterator()

        while (it.hasNext()) {
            val ipWithSubnet = it.next() as ByteArray

            if (isIPConstrained(ip, ipWithSubnet)) {
                return
            }
        }
        if (ip.size == 0 && permitted.size == 0) {
            return
        }
        throw NameConstraintValidatorException(
                "IP is not from a permitted subtree.")
    }

    /**
     * Checks if the IP `ip` is included in the excluded set
     * `excluded`.

     * @param excluded A `Set` of excluded IP addresses with their
     * *                 subnet mask as byte arrays.
     * *
     * @param ip       The IP address.
     * *
     * @throws NameConstraintValidatorException if the IP is excluded.
     */
    @Throws(NameConstraintValidatorException::class)
    private fun checkExcludedIP(excluded: Set<Any>, ip: ByteArray) {
        if (excluded.isEmpty()) {
            return
        }

        val it = excluded.iterator()

        while (it.hasNext()) {
            val ipWithSubnet = it.next() as ByteArray

            if (isIPConstrained(ip, ipWithSubnet)) {
                throw NameConstraintValidatorException(
                        "IP is from an excluded subtree.")
            }
        }
    }

    /**
     * Checks if the IP address `ip` is constrained by
     * `constraint`.

     * @param ip         The IP address.
     * *
     * @param constraint The constraint. This is an IP address concatenated with
     * *                   its subnetmask.
     * *
     * @return `true` if constrained, `false`
     * * otherwise.
     */
    private fun isIPConstrained(ip: ByteArray, constraint: ByteArray): Boolean {
        val ipLength = ip.size

        if (ipLength != constraint.size / 2) {
            return false
        }

        val subnetMask = ByteArray(ipLength)
        System.arraycopy(constraint, ipLength, subnetMask, 0, ipLength)

        val permittedSubnetAddress = ByteArray(ipLength)

        val ipSubnetAddress = ByteArray(ipLength)

        // the resulting IP address by applying the subnet mask
        for (i in 0..ipLength - 1) {
            permittedSubnetAddress[i] = (constraint[i] and subnetMask[i]).toByte()
            ipSubnetAddress[i] = (ip[i] and subnetMask[i]).toByte()
        }

        return Arrays.areEqual(permittedSubnetAddress, ipSubnetAddress)
    }

    private fun emailIsConstrained(email: String, constraint: String): Boolean {
        val sub = email.substring(email.indexOf('@') + 1)
        // a particular mailbox
        if (constraint.indexOf('@') != -1) {
            if (email.equals(constraint, ignoreCase = true)) {
                return true
            }
        } else if (constraint[0] != '.') {
            if (sub.equals(constraint, ignoreCase = true)) {
                return true
            }
        } else if (withinDomain(sub, constraint)) {
            return true
        }// address in sub domain
        // on particular host
        return false
    }

    private fun withinDomain(testDomain: String, domain: String): Boolean {
        var tempDomain = domain
        if (tempDomain.startsWith(".")) {
            tempDomain = tempDomain.substring(1)
        }
        val domainParts = Strings.split(tempDomain, '.')
        val testDomainParts = Strings.split(testDomain, '.')
        // must have at least one subdomain
        if (testDomainParts.size <= domainParts.size) {
            return false
        }
        val d = testDomainParts.size - domainParts.size
        for (i in -1..domainParts.size - 1) {
            if (i == -1) {
                if (testDomainParts[i + d] == "") {
                    return false
                }
            } else if (!domainParts[i].equals(testDomainParts[i + d], ignoreCase = true)) {
                return false
            }
        }
        return true
    }

    @Throws(NameConstraintValidatorException::class)
    private fun checkPermittedDNS(permitted: Set<Any>?, dns: String) {
        if (permitted == null) {
            return
        }

        val it = permitted.iterator()

        while (it.hasNext()) {
            val str = it.next() as String

            // is sub domain
            if (withinDomain(dns, str) || dns.equals(str, ignoreCase = true)) {
                return
            }
        }
        if (dns.length == 0 && permitted.size == 0) {
            return
        }
        throw NameConstraintValidatorException(
                "DNS is not from a permitted subtree.")
    }

    @Throws(NameConstraintValidatorException::class)
    private fun checkExcludedDNS(excluded: Set<Any>, dns: String) {
        if (excluded.isEmpty()) {
            return
        }

        val it = excluded.iterator()

        while (it.hasNext()) {
            val str = it.next() as String

            // is sub domain or the same
            if (withinDomain(dns, str) || dns.equals(str, ignoreCase = true)) {
                throw NameConstraintValidatorException(
                        "DNS is from an excluded subtree.")
            }
        }
    }

    /**
     * The common part of `email1` and `email2` is
     * added to the union `union`. If `email1` and
     * `email2` have nothing in common they are added both.

     * @param email1 Email address constraint 1.
     * *
     * @param email2 Email address constraint 2.
     * *
     * @param union  The union.
     */
    private fun unionEmail(email1: String, email2: String, union: MutableSet<Any>) {
        // email1 is a particular address
        if (email1.indexOf('@') != -1) {
            val _sub = email1.substring(email1.indexOf('@') + 1)
            // both are a particular mailbox
            if (email2.indexOf('@') != -1) {
                if (email1.equals(email2, ignoreCase = true)) {
                    union.add(email1)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            } else if (email2.startsWith(".")) {
                if (withinDomain(_sub, email2)) {
                    union.add(email2)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            } else {
                if (_sub.equals(email2, ignoreCase = true)) {
                    union.add(email2)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            }// email2 specifies a particular host
            // email2 specifies a domain
        } else if (email1.startsWith(".")) {
            if (email2.indexOf('@') != -1) {
                val _sub = email2.substring(email1.indexOf('@') + 1)
                if (withinDomain(_sub, email1)) {
                    union.add(email1)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            } else if (email2.startsWith(".")) {
                if (withinDomain(email1, email2) || email1.equals(email2, ignoreCase = true)) {
                    union.add(email2)
                } else if (withinDomain(email2, email1)) {
                    union.add(email1)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            } else {
                if (withinDomain(email2, email1)) {
                    union.add(email1)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            }// email2 specifies a domain
        } else {
            if (email2.indexOf('@') != -1) {
                val _sub = email2.substring(email1.indexOf('@') + 1)
                if (_sub.equals(email1, ignoreCase = true)) {
                    union.add(email1)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            } else if (email2.startsWith(".")) {
                if (withinDomain(email1, email2)) {
                    union.add(email2)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            } else {
                if (email1.equals(email2, ignoreCase = true)) {
                    union.add(email1)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            }// email2 specifies a particular host
            // email2 specifies a domain
        }// email specifies a host
        // email1 specifies a domain
    }

    private fun unionURI(email1: String, email2: String, union: MutableSet<Any>) {
        // email1 is a particular address
        if (email1.indexOf('@') != -1) {
            val _sub = email1.substring(email1.indexOf('@') + 1)
            // both are a particular mailbox
            if (email2.indexOf('@') != -1) {
                if (email1.equals(email2, ignoreCase = true)) {
                    union.add(email1)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            } else if (email2.startsWith(".")) {
                if (withinDomain(_sub, email2)) {
                    union.add(email2)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            } else {
                if (_sub.equals(email2, ignoreCase = true)) {
                    union.add(email2)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            }// email2 specifies a particular host
            // email2 specifies a domain
        } else if (email1.startsWith(".")) {
            if (email2.indexOf('@') != -1) {
                val _sub = email2.substring(email1.indexOf('@') + 1)
                if (withinDomain(_sub, email1)) {
                    union.add(email1)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            } else if (email2.startsWith(".")) {
                if (withinDomain(email1, email2) || email1.equals(email2, ignoreCase = true)) {
                    union.add(email2)
                } else if (withinDomain(email2, email1)) {
                    union.add(email1)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            } else {
                if (withinDomain(email2, email1)) {
                    union.add(email1)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            }// email2 specifies a domain
        } else {
            if (email2.indexOf('@') != -1) {
                val _sub = email2.substring(email1.indexOf('@') + 1)
                if (_sub.equals(email1, ignoreCase = true)) {
                    union.add(email1)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            } else if (email2.startsWith(".")) {
                if (withinDomain(email1, email2)) {
                    union.add(email2)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            } else {
                if (email1.equals(email2, ignoreCase = true)) {
                    union.add(email1)
                } else {
                    union.add(email1)
                    union.add(email2)
                }
            }// email2 specifies a particular host
            // email2 specifies a domain
        }// email specifies a host
        // email1 specifies a domain
    }

    private fun intersectDNS(permitted: Set<Any>?, dnss: Set<Any>): Set<Any> {
        val intersect = HashSet()
        val it = dnss.iterator()
        while (it.hasNext()) {
            val dns = extractNameAsString((it.next() as GeneralSubtree).base)
            if (permitted == null) {
                if (dns != null) {
                    intersect.add(dns)
                }
            } else {
                val _iter = permitted.iterator()
                while (_iter.hasNext()) {
                    val _permitted = _iter.next() as String

                    if (withinDomain(_permitted, dns)) {
                        intersect.add(_permitted)
                    } else if (withinDomain(dns, _permitted)) {
                        intersect.add(dns)
                    }
                }
            }
        }

        return intersect
    }

    private fun unionDNS(excluded: MutableSet<Any>, dns: String?): MutableSet<Any> {
        if (excluded.isEmpty()) {
            if (dns == null) {
                return excluded
            }
            excluded.add(dns)

            return excluded
        } else {
            val union = HashSet()

            val _iter = excluded.iterator()
            while (_iter.hasNext()) {
                val _permitted = _iter.next() as String

                if (withinDomain(_permitted, dns)) {
                    union.add(dns)
                } else if (withinDomain(dns, _permitted)) {
                    union.add(_permitted)
                } else {
                    union.add(_permitted)
                    union.add(dns)
                }
            }

            return union
        }
    }

    /**
     * The most restricting part from `email1` and
     * `email2` is added to the intersection `intersect`.

     * @param email1    Email address constraint 1.
     * *
     * @param email2    Email address constraint 2.
     * *
     * @param intersect The intersection.
     */
    private fun intersectEmail(email1: String, email2: String, intersect: MutableSet<Any>) {
        // email1 is a particular address
        if (email1.indexOf('@') != -1) {
            val _sub = email1.substring(email1.indexOf('@') + 1)
            // both are a particular mailbox
            if (email2.indexOf('@') != -1) {
                if (email1.equals(email2, ignoreCase = true)) {
                    intersect.add(email1)
                }
            } else if (email2.startsWith(".")) {
                if (withinDomain(_sub, email2)) {
                    intersect.add(email1)
                }
            } else {
                if (_sub.equals(email2, ignoreCase = true)) {
                    intersect.add(email1)
                }
            }// email2 specifies a particular host
            // email2 specifies a domain
        } else if (email1.startsWith(".")) {
            if (email2.indexOf('@') != -1) {
                val _sub = email2.substring(email1.indexOf('@') + 1)
                if (withinDomain(_sub, email1)) {
                    intersect.add(email2)
                }
            } else if (email2.startsWith(".")) {
                if (withinDomain(email1, email2) || email1.equals(email2, ignoreCase = true)) {
                    intersect.add(email1)
                } else if (withinDomain(email2, email1)) {
                    intersect.add(email2)
                }
            } else {
                if (withinDomain(email2, email1)) {
                    intersect.add(email2)
                }
            }// email2 specifies a domain
        } else {
            if (email2.indexOf('@') != -1) {
                val _sub = email2.substring(email2.indexOf('@') + 1)
                if (_sub.equals(email1, ignoreCase = true)) {
                    intersect.add(email2)
                }
            } else if (email2.startsWith(".")) {
                if (withinDomain(email1, email2)) {
                    intersect.add(email1)
                }
            } else {
                if (email1.equals(email2, ignoreCase = true)) {
                    intersect.add(email1)
                }
            }// email2 specifies a particular host
            // email2 specifies a domain
        }// email1 specifies a host
        // email specifies a domain
    }

    @Throws(NameConstraintValidatorException::class)
    private fun checkExcludedURI(excluded: Set<Any>, uri: String) {
        if (excluded.isEmpty()) {
            return
        }

        val it = excluded.iterator()

        while (it.hasNext()) {
            val str = it.next() as String

            if (isUriConstrained(uri, str)) {
                throw NameConstraintValidatorException(
                        "URI is from an excluded subtree.")
            }
        }
    }

    private fun intersectURI(permitted: Set<Any>?, uris: Set<Any>): Set<Any> {
        val intersect = HashSet()
        val it = uris.iterator()
        while (it.hasNext()) {
            val uri = extractNameAsString((it.next() as GeneralSubtree).base)
            if (permitted == null) {
                if (uri != null) {
                    intersect.add(uri)
                }
            } else {
                val _iter = permitted.iterator()
                while (_iter.hasNext()) {
                    val _permitted = _iter.next() as String
                    intersectURI(_permitted, uri, intersect)
                }
            }
        }
        return intersect
    }

    private fun unionURI(excluded: MutableSet<Any>, uri: String?): MutableSet<Any> {
        if (excluded.isEmpty()) {
            if (uri == null) {
                return excluded
            }
            excluded.add(uri)

            return excluded
        } else {
            val union = HashSet()

            val _iter = excluded.iterator()
            while (_iter.hasNext()) {
                val _excluded = _iter.next() as String

                unionURI(_excluded, uri, union)
            }

            return union
        }
    }

    private fun intersectURI(email1: String, email2: String, intersect: MutableSet<Any>) {
        // email1 is a particular address
        if (email1.indexOf('@') != -1) {
            val _sub = email1.substring(email1.indexOf('@') + 1)
            // both are a particular mailbox
            if (email2.indexOf('@') != -1) {
                if (email1.equals(email2, ignoreCase = true)) {
                    intersect.add(email1)
                }
            } else if (email2.startsWith(".")) {
                if (withinDomain(_sub, email2)) {
                    intersect.add(email1)
                }
            } else {
                if (_sub.equals(email2, ignoreCase = true)) {
                    intersect.add(email1)
                }
            }// email2 specifies a particular host
            // email2 specifies a domain
        } else if (email1.startsWith(".")) {
            if (email2.indexOf('@') != -1) {
                val _sub = email2.substring(email1.indexOf('@') + 1)
                if (withinDomain(_sub, email1)) {
                    intersect.add(email2)
                }
            } else if (email2.startsWith(".")) {
                if (withinDomain(email1, email2) || email1.equals(email2, ignoreCase = true)) {
                    intersect.add(email1)
                } else if (withinDomain(email2, email1)) {
                    intersect.add(email2)
                }
            } else {
                if (withinDomain(email2, email1)) {
                    intersect.add(email2)
                }
            }// email2 specifies a domain
        } else {
            if (email2.indexOf('@') != -1) {
                val _sub = email2.substring(email2.indexOf('@') + 1)
                if (_sub.equals(email1, ignoreCase = true)) {
                    intersect.add(email2)
                }
            } else if (email2.startsWith(".")) {
                if (withinDomain(email1, email2)) {
                    intersect.add(email1)
                }
            } else {
                if (email1.equals(email2, ignoreCase = true)) {
                    intersect.add(email1)
                }
            }// email2 specifies a particular host
            // email2 specifies a domain
        }// email1 specifies a host
        // email specifies a domain
    }

    @Throws(NameConstraintValidatorException::class)
    private fun checkPermittedURI(permitted: Set<Any>?, uri: String) {
        if (permitted == null) {
            return
        }

        val it = permitted.iterator()

        while (it.hasNext()) {
            val str = it.next() as String

            if (isUriConstrained(uri, str)) {
                return
            }
        }
        if (uri.length == 0 && permitted.size == 0) {
            return
        }
        throw NameConstraintValidatorException(
                "URI is not from a permitted subtree.")
    }

    private fun isUriConstrained(uri: String, constraint: String): Boolean {
        val host = extractHostFromURL(uri)
        // a host
        if (!constraint.startsWith(".")) {
            if (host.equals(constraint, ignoreCase = true)) {
                return true
            }
        } else if (withinDomain(host, constraint)) {
            return true
        }// in sub domain or domain

        return false
    }

    private fun extractHostFromURL(url: String): String {
        // see RFC 1738
        // remove ':' after protocol, e.g. http:
        var sub = url.substring(url.indexOf(':') + 1)
        // extract host from Common Internet Scheme Syntax, e.g. http://
        if (sub.indexOf("//") != -1) {
            sub = sub.substring(sub.indexOf("//") + 2)
        }
        // first remove port, e.g. http://test.com:21
        if (sub.lastIndexOf(':') != -1) {
            sub = sub.substring(0, sub.lastIndexOf(':'))
        }
        // remove user and password, e.g. http://john:password@test.com
        sub = sub.substring(sub.indexOf(':') + 1)
        sub = sub.substring(sub.indexOf('@') + 1)
        // remove local parts, e.g. http://test.com/bla
        if (sub.indexOf('/') != -1) {
            sub = sub.substring(0, sub.indexOf('/'))
        }
        return sub
    }

    private fun extractNameAsString(name: GeneralName): String? {
        return DERIA5String.getInstance(name.name).string
    }

    /**
     * Returns the maximum IP address.

     * @param ip1 The first IP address.
     * *
     * @param ip2 The second IP address.
     * *
     * @return The maximum IP address.
     */
    private fun max(ip1: ByteArray, ip2: ByteArray): ByteArray {
        for (i in ip1.indices) {
            if (ip1[i] and 0xFFFF > ip2[i] and 0xFFFF) {
                return ip1
            }
        }
        return ip2
    }

    /**
     * Returns the minimum IP address.

     * @param ip1 The first IP address.
     * *
     * @param ip2 The second IP address.
     * *
     * @return The minimum IP address.
     */
    private fun min(ip1: ByteArray, ip2: ByteArray): ByteArray {
        for (i in ip1.indices) {
            if (ip1[i] and 0xFFFF < ip2[i] and 0xFFFF) {
                return ip1
            }
        }
        return ip2
    }

    /**
     * Compares IP address `ip1` with `ip2`. If ip1
     * is equal to ip2 0 is returned. If ip1 is bigger 1 is returned, -1
     * otherwise.

     * @param ip1 The first IP address.
     * *
     * @param ip2 The second IP address.
     * *
     * @return 0 if ip1 is equal to ip2, 1 if ip1 is bigger, -1 otherwise.
     */
    private fun compareTo(ip1: ByteArray, ip2: ByteArray): Int {
        if (Arrays.areEqual(ip1, ip2)) {
            return 0
        }
        if (Arrays.areEqual(max(ip1, ip2), ip1)) {
            return 1
        }
        return -1
    }

    /**
     * Returns the logical OR of the IP addresses `ip1` and
     * `ip2`.

     * @param ip1 The first IP address.
     * *
     * @param ip2 The second IP address.
     * *
     * @return The OR of `ip1` and `ip2`.
     */
    private fun or(ip1: ByteArray, ip2: ByteArray): ByteArray {
        val temp = ByteArray(ip1.size)
        for (i in ip1.indices) {
            temp[i] = (ip1[i] or ip2[i]).toByte()
        }
        return temp
    }

    private fun hashCollection(coll: Collection<Any>?): Int {
        if (coll == null) {
            return 0
        }
        var hash = 0
        val it1 = coll.iterator()
        while (it1.hasNext()) {
            val o = it1.next()
            if (o is ByteArray) {
                hash += Arrays.hashCode(o)
            } else {
                hash += o.hashCode()
            }
        }
        return hash
    }

    private fun collectionsAreEqual(coll1: Collection<Any>?, coll2: Collection<Any>?): Boolean {
        if (coll1 === coll2) {
            return true
        }
        if (coll1 == null || coll2 == null) {
            return false
        }
        if (coll1.size != coll2.size) {
            return false
        }
        val it1 = coll1.iterator()

        while (it1.hasNext()) {
            val a = it1.next()
            val it2 = coll2.iterator()
            var found = false
            while (it2.hasNext()) {
                val b = it2.next()
                if (equals(a, b)) {
                    found = true
                    break
                }
            }
            if (!found) {
                return false
            }
        }
        return true
    }

    private fun equals(o1: Any?, o2: Any?): Boolean {
        if (o1 === o2) {
            return true
        }
        if (o1 == null || o2 == null) {
            return false
        }
        if (o1 is ByteArray && o2 is ByteArray) {
            return Arrays.areEqual(o1 as ByteArray?, o2 as ByteArray?)
        } else {
            return o1 == o2
        }
    }

    /**
     * Stringifies an IPv4 or v6 address with subnet mask.

     * @param ip The IP with subnet mask.
     * *
     * @return The stringified IP address.
     */
    private fun stringifyIP(ip: ByteArray): String {
        var temp = ""
        for (i in 0..ip.size / 2 - 1) {
            temp += Integer.toString(ip[i] and 0x00FF) + "."
        }
        temp = temp.substring(0, temp.length - 1)
        temp += "/"
        for (i in ip.size / 2..ip.size - 1) {
            temp += Integer.toString(ip[i] and 0x00FF) + "."
        }
        temp = temp.substring(0, temp.length - 1)
        return temp
    }

    private fun stringifyIPCollection(ips: Set<Any>): String {
        var temp = ""
        temp += "["
        val it = ips.iterator()
        while (it.hasNext()) {
            temp += stringifyIP(it.next() as ByteArray) + ","
        }
        if (temp.length > 1) {
            temp = temp.substring(0, temp.length - 1)
        }
        temp += "]"
        return temp
    }
}
