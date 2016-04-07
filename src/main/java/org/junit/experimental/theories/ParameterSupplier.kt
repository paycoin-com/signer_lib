package org.junit.experimental.theories

/**
 * Abstract parent class for suppliers of input data points for theories. Extend this class to customize how [ ] runner
 * finds accepted data points. Then use your class together with **&#064;ParametersSuppliedBy** on input
 * parameters for theories.

 *
 *
 * For example, here is a supplier for values between two integers, and an annotation that references it:

 *
 * &#064;Retention(RetentionPolicy.RUNTIME)
 * **&#064;ParametersSuppliedBy**(BetweenSupplier.class)
 * public @interface Between {
 * int first();

 * int last();
 * }

 * public static class BetweenSupplier extends **ParameterSupplier** {
 * &#064;Override
 * public List&lt;**PotentialAssignment**&gt; getValueSources(**ParameterSignature** sig) {
 * List&lt;**PotentialAssignment**&gt; list = new ArrayList&lt;PotentialAssignment&gt;();
 * Between annotation = (Between) sig.getSupplierAnnotation();

 * for (int i = annotation.first(); i &lt;= annotation.last(); i++)
 * list.add(**PotentialAssignment**.forValue("ints", i));
 * return list;
 * }
 * }
 *
 *

 * @see org.junit.experimental.theories.ParametersSuppliedBy

 * @see org.junit.experimental.theories.Theories

 * @see org.junit.experimental.theories.FromDataPoints
 */
abstract class ParameterSupplier {
    @Throws(Throwable::class)
    abstract fun getValueSources(sig: ParameterSignature): List<PotentialAssignment>
}
