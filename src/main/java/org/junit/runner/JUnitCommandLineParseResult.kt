package org.junit.runner

import java.util.ArrayList
import java.util.Collections

import org.junit.internal.Classes
import org.junit.runner.FilterFactory.FilterNotCreatedException
import org.junit.runner.manipulation.Filter
import org.junit.runners.model.InitializationError

internal class JUnitCommandLineParseResult {
    private val filterSpecs = ArrayList<String>()
    private val classes = ArrayList<Class<*>>()
    private val parserErrors = ArrayList<Throwable>()

    /**
     * Returns filter specs parsed from command line.
     */
    fun getFilterSpecs(): List<String> {
        return Collections.unmodifiableList(filterSpecs)
    }

    /**
     * Returns test classes parsed from command line.
     */
    fun getClasses(): List<Class<*>> {
        return Collections.unmodifiableList(classes)
    }

    private fun parseArgs(args: Array<String>) {
        parseParameters(parseOptions(*args))
    }

    fun parseOptions(vararg args: String): Array<String> {
        var i = 0
        while (i != args.size) {
            val arg = args[i]

            if (arg == "--") {
                return copyArray(args, i + 1, args.size)
            } else if (arg.startsWith("--")) {
                if (arg.startsWith("--filter=") || arg == "--filter") {
                    val filterSpec: String
                    if (arg == "--filter") {
                        ++i

                        if (i < args.size) {
                            filterSpec = args[i]
                        } else {
                            parserErrors.add(CommandLineParserError(arg + " value not specified"))
                            break
                        }
                    } else {
                        filterSpec = arg.substring(arg.indexOf('=') + 1)
                    }

                    filterSpecs.add(filterSpec)
                } else {
                    parserErrors.add(CommandLineParserError("JUnit knows nothing about the $arg option"))
                }
            } else {
                return copyArray(args, i, args.size)
            }
            ++i
        }

        return arrayOf()
    }

    private fun copyArray(args: Array<String>, from: Int, to: Int): Array<String> {
        val result = ArrayList<String>()

        for (j in from..to - 1) {
            result.add(args[j])
        }

        return result.toArray<String>(arrayOfNulls<String>(result.size))
    }

    fun parseParameters(args: Array<String>) {
        for (arg in args) {
            try {
                classes.add(Classes.getClass(arg))
            } catch (e: ClassNotFoundException) {
                parserErrors.add(IllegalArgumentException("Could not find class [$arg]", e))
            }

        }
    }

    private fun errorReport(cause: Throwable): Request {
        return Request.errorReport(JUnitCommandLineParseResult::class.java, cause)
    }

    /**
     * Creates a [Request].

     * @param computer [Computer] to be used.
     */
    fun createRequest(computer: Computer): Request {
        if (parserErrors.isEmpty()) {
            val request = Request.classes(
                    computer, *classes.toArray<Class<*>>(arrayOfNulls<Class<*>>(classes.size)))
            return applyFilterSpecs(request)
        } else {
            return errorReport(InitializationError(parserErrors))
        }
    }

    private fun applyFilterSpecs(request: Request): Request {
        var request = request
        try {
            for (filterSpec in filterSpecs) {
                val filter = FilterFactories.createFilterFromFilterSpec(
                        request, filterSpec)
                request = request.filterWith(filter)
            }
            return request
        } catch (e: FilterNotCreatedException) {
            return errorReport(e)
        }

    }

    /**
     * Exception used if there's a problem parsing the command line.
     */
    class CommandLineParserError(message: String) : Exception(message) {
        companion object {
            private val serialVersionUID = 1L
        }
    }

    companion object {

        /**
         * Parses the arguments.

         * @param args Arguments
         */
        fun parse(args: Array<String>): JUnitCommandLineParseResult {
            val result = JUnitCommandLineParseResult()

            result.parseArgs(args)

            return result
        }
    }
}
/**
 * Do not use. Testing purposes only.
 */
