package org.bouncycastle.asn1.x509

interface NameConstraintValidator {
    @Throws(NameConstraintValidatorException::class)
    fun checkPermitted(name: GeneralName)

    @Throws(NameConstraintValidatorException::class)
    fun checkExcluded(name: GeneralName)

    fun intersectPermittedSubtree(permitted: GeneralSubtree)

    fun intersectPermittedSubtree(permitted: Array<GeneralSubtree>)

    fun intersectEmptyPermittedSubtree(nameType: Int)

    fun addExcludedSubtree(subtree: GeneralSubtree)
}
