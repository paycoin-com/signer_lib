package org.junit.internal.runners.rules

import org.junit.ClassRule
import org.junit.Rule
import org.junit.rules.MethodRule
import org.junit.rules.TestRule
import org.junit.runners.model.FrameworkMember
import org.junit.runners.model.TestClass
import java.lang.reflect.Modifier
import java.util.ArrayList

/**
 * A RuleMemberValidator validates the rule fields/methods of a
 * [org.junit.runners.model.TestClass]. All reasons for rejecting the
 * `TestClass` are written to a list of errors.

 *
 * There are four slightly different validators. The [.CLASS_RULE_VALIDATOR]
 * validates fields with a [ClassRule] annotation and the
 * [.RULE_VALIDATOR] validates fields with a [Rule] annotation.

 *
 * The [.CLASS_RULE_METHOD_VALIDATOR]
 * validates methods with a [ClassRule] annotation and the
 * [.RULE_METHOD_VALIDATOR] validates methods with a [Rule] annotation.
 */
class RuleMemberValidator internal constructor(builder: RuleMemberValidator.Builder) {

    private val annotation: Class<out Annotation>
    private val methods: Boolean
    private val validatorStrategies: List<RuleValidator>

    init {
        this.annotation = builder.annotation
        this.methods = builder.methods
        this.validatorStrategies = builder.validators
    }

    /**
     * Validate the [org.junit.runners.model.TestClass] and adds reasons
     * for rejecting the class to a list of errors.

     * @param target the `TestClass` to validate.
     * *
     * @param errors the list of errors.
     */
    fun validate(target: TestClass, errors: List<Throwable>) {
        val members = if (methods)
            target.getAnnotatedMethods(annotation)
        else
            target.getAnnotatedFields(annotation)

        for (each in members) {
            validateMember(each, errors)
        }
    }

    private fun validateMember(member: FrameworkMember<*>, errors: List<Throwable>) {
        for (strategy in validatorStrategies) {
            strategy.validate(member, annotation, errors)
        }
    }

    private class Builder private constructor(private val annotation: Class<out Annotation>) {
        private var methods: Boolean = false
        private val validators: MutableList<RuleValidator>

        init {
            this.methods = false
            this.validators = ArrayList<RuleValidator>()
        }

        internal fun forMethods(): Builder {
            methods = true
            return this
        }

        internal fun withValidator(validator: RuleValidator): Builder {
            validators.add(validator)
            return this
        }

        internal fun build(): RuleMemberValidator {
            return RuleMemberValidator(this)
        }
    }

    /**
     * Encapsulates a single piece of validation logic, used to determine if [org.junit.Rule] and
     * [org.junit.ClassRule] annotations have been used correctly
     */
    internal interface RuleValidator {
        /**
         * Examine the given member and add any violations of the strategy's validation logic to the given list of errors
         * @param member The member (field or member) to examine
         * *
         * @param annotation The type of rule annotation on the member
         * *
         * @param errors The list of errors to add validation violations to
         */
        fun validate(member: FrameworkMember<*>, annotation: Class<out Annotation>, errors: List<Throwable>)
    }

    /**
     * Requires the validated member to be non-static
     */
    private class MemberMustBeNonStaticOrAlsoClassRule : RuleValidator {
        override fun validate(member: FrameworkMember<*>, annotation: Class<out Annotation>, errors: MutableList<Throwable>) {
            val isMethodRuleMember = isMethodRule(member)
            val isClassRuleAnnotated = member.getAnnotation(ClassRule::class.java) != null

            // We disallow:
            //  - static MethodRule members
            //  - static @Rule annotated members
            //    - UNLESS they're also @ClassRule annotated
            // Note that MethodRule cannot be annotated with @ClassRule
            if (member.isStatic && (isMethodRuleMember || !isClassRuleAnnotated)) {
                val message: String
                if (isMethodRule(member)) {
                    message = "must not be static."
                } else {
                    message = "must not be static or it must be annotated with @ClassRule."
                }
                errors.add(ValidationError(member, annotation, message))
            }
        }
    }

    /**
     * Requires the member to be static
     */
    private class MemberMustBeStatic : RuleValidator {
        override fun validate(member: FrameworkMember<*>, annotation: Class<out Annotation>, errors: MutableList<Throwable>) {
            if (!member.isStatic) {
                errors.add(ValidationError(member, annotation,
                        "must be static."))
            }
        }
    }

    /**
     * Requires the member's declaring class to be public
     */
    private class DeclaringClassMustBePublic : RuleValidator {
        override fun validate(member: FrameworkMember<*>, annotation: Class<out Annotation>, errors: MutableList<Throwable>) {
            if (!isDeclaringClassPublic(member)) {
                errors.add(ValidationError(member, annotation,
                        "must be declared in a public class."))
            }
        }

        private fun isDeclaringClassPublic(member: FrameworkMember<*>): Boolean {
            return Modifier.isPublic(member.declaringClass.modifiers)
        }
    }

    /**
     * Requires the member to be public
     */
    private class MemberMustBePublic : RuleValidator {
        override fun validate(member: FrameworkMember<*>, annotation: Class<out Annotation>, errors: MutableList<Throwable>) {
            if (!member.isPublic) {
                errors.add(ValidationError(member, annotation,
                        "must be public."))
            }
        }
    }

    /**
     * Requires the member is a field implementing [org.junit.rules.MethodRule] or [org.junit.rules.TestRule]
     */
    private class FieldMustBeARule : RuleValidator {
        override fun validate(member: FrameworkMember<*>, annotation: Class<out Annotation>, errors: MutableList<Throwable>) {
            if (!isRuleType(member)) {
                errors.add(ValidationError(member, annotation,
                        "must implement MethodRule or TestRule."))
            }
        }
    }

    /**
     * Require the member to return an implementation of [org.junit.rules.MethodRule] or
     * [org.junit.rules.TestRule]
     */
    private class MethodMustBeARule : RuleValidator {
        override fun validate(member: FrameworkMember<*>, annotation: Class<out Annotation>, errors: MutableList<Throwable>) {
            if (!isRuleType(member)) {
                errors.add(ValidationError(member, annotation,
                        "must return an implementation of MethodRule or TestRule."))
            }
        }
    }

    /**
     * Require the member to return an implementation of [org.junit.rules.TestRule]
     */
    private class MethodMustBeATestRule : RuleValidator {
        override fun validate(member: FrameworkMember<*>,
                              annotation: Class<out Annotation>, errors: MutableList<Throwable>) {
            if (!isTestRule(member)) {
                errors.add(ValidationError(member, annotation,
                        "must return an implementation of TestRule."))
            }
        }
    }

    /**
     * Requires the member is a field implementing [org.junit.rules.TestRule]
     */
    private class FieldMustBeATestRule : RuleValidator {

        override fun validate(member: FrameworkMember<*>,
                              annotation: Class<out Annotation>, errors: MutableList<Throwable>) {
            if (!isTestRule(member)) {
                errors.add(ValidationError(member, annotation,
                        "must implement TestRule."))
            }
        }
    }

    companion object {
        /**
         * Validates fields with a [ClassRule] annotation.
         */
        val CLASS_RULE_VALIDATOR = classRuleValidatorBuilder().withValidator(DeclaringClassMustBePublic()).withValidator(MemberMustBeStatic()).withValidator(MemberMustBePublic()).withValidator(FieldMustBeATestRule()).build()
        /**
         * Validates fields with a [Rule] annotation.
         */
        val RULE_VALIDATOR = testRuleValidatorBuilder().withValidator(MemberMustBeNonStaticOrAlsoClassRule()).withValidator(MemberMustBePublic()).withValidator(FieldMustBeARule()).build()
        /**
         * Validates methods with a [ClassRule] annotation.
         */
        val CLASS_RULE_METHOD_VALIDATOR = classRuleValidatorBuilder().forMethods().withValidator(DeclaringClassMustBePublic()).withValidator(MemberMustBeStatic()).withValidator(MemberMustBePublic()).withValidator(MethodMustBeATestRule()).build()

        /**
         * Validates methods with a [Rule] annotation.
         */
        val RULE_METHOD_VALIDATOR = testRuleValidatorBuilder().forMethods().withValidator(MemberMustBeNonStaticOrAlsoClassRule()).withValidator(MemberMustBePublic()).withValidator(MethodMustBeARule()).build()

        private fun classRuleValidatorBuilder(): Builder {
            return Builder(ClassRule::class.java)
        }

        private fun testRuleValidatorBuilder(): Builder {
            return Builder(Rule::class.java)
        }

        private fun isRuleType(member: FrameworkMember<*>): Boolean {
            return isMethodRule(member) || isTestRule(member)
        }

        private fun isTestRule(member: FrameworkMember<*>): Boolean {
            return TestRule::class.java.isAssignableFrom(member.type)
        }

        private fun isMethodRule(member: FrameworkMember<*>): Boolean {
            return MethodRule::class.java.isAssignableFrom(member.type)
        }
    }
}
