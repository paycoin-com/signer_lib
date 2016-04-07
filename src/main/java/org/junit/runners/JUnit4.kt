package org.junit.runners

import org.junit.runners.model.InitializationError

/**
 * Aliases the current default JUnit 4 class runner, for future-proofing. If
 * future versions of JUnit change the default Runner class, they will also
 * change the definition of this class. Developers wanting to explicitly tag a
 * class as a JUnit 4 class should use `@RunWith(JUnit4.class)`, not,
 * for example in JUnit 4.5, `@RunWith(BlockJUnit4ClassRunner.class)`.
 * This is the only way this class should be used--any extension that
 * depends on the implementation details of this class is likely to break
 * in future versions.

 * @since 4.5
 */
class JUnit4
/**
 * Constructs a new instance of the default runner
 */
@Throws(InitializationError::class)
constructor(klass: Class<*>) : BlockJUnit4ClassRunner(klass)
