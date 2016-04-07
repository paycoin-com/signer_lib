package org.junit.rules

import org.junit.runner.Description

/**
 * The TestName Rule makes the current test name available inside test methods:

 *
 * public class TestNameTest {
 * &#064;Rule
 * public TestName name= new TestName();

 * &#064;Test
 * public void testA() {
 * assertEquals(&quot;testA&quot;, name.getMethodName());
 * }

 * &#064;Test
 * public void testB() {
 * assertEquals(&quot;testB&quot;, name.getMethodName());
 * }
 * }
 *

 * @since 4.7
 */
class TestName : TestWatcher() {
    /**
     * @return the name of the currently-running test method
     */
    var methodName: String? = null
        private set

    override fun starting(d: Description) {
        methodName = d.methodName
    }
}
