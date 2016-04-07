package org.junit.experimental

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import org.junit.runner.Computer
import org.junit.runner.Runner
import org.junit.runners.ParentRunner
import org.junit.runners.model.InitializationError
import org.junit.runners.model.RunnerBuilder
import org.junit.runners.model.RunnerScheduler

class ParallelComputer(private val classes: Boolean, private val methods: Boolean) : Computer() {

    @Throws(InitializationError::class)
    override fun getSuite(builder: RunnerBuilder, classes: Array<java.lang.Class<*>>): Runner {
        val suite = super.getSuite(builder, classes)
        return if (this.classes) parallelize(suite) else suite
    }

    @Throws(Throwable::class)
    override fun getRunner(builder: RunnerBuilder, testClass: Class<*>): Runner {
        val runner = super.getRunner(builder, testClass)
        return if (methods) parallelize(runner) else runner
    }

    companion object {

        fun classes(): Computer {
            return ParallelComputer(true, false)
        }

        fun methods(): Computer {
            return ParallelComputer(false, true)
        }

        private fun parallelize(runner: Runner): Runner {
            if (runner is ParentRunner<Any>) {
                (runner as ParentRunner<*>).setScheduler(object : RunnerScheduler {
                    private val fService = Executors.newCachedThreadPool()

                    override fun schedule(childStatement: Runnable) {
                        fService.submit(childStatement)
                    }

                    override fun finished() {
                        try {
                            fService.shutdown()
                            fService.awaitTermination(java.lang.Long.MAX_VALUE, TimeUnit.NANOSECONDS)
                        } catch (e: InterruptedException) {
                            e.printStackTrace(System.err)
                        }

                    }
                })
            }
            return runner
        }
    }
}
