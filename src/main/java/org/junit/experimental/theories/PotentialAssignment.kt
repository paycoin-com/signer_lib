package org.junit.experimental.theories

import java.lang.String.format

abstract class PotentialAssignment {
    class CouldNotGenerateValueException : Exception {

        constructor() {
        }

        constructor(e: Throwable) : super(e) {
        }

        companion object {
            private val serialVersionUID = 1L
        }
    }

    abstract val value: Any

    abstract val description: String

    companion object {

        fun forValue(name: String, value: Any?): PotentialAssignment {
            return object : PotentialAssignment() {
                override val value: Any
                    get() = value

                override fun toString(): String {
                    return format("[%s]", value)
                }

                override val description: String
                    get() {
                        val valueString: String

                        if (value == null) {
                            valueString = "null"
                        } else {
                            try {
                                valueString = format("\"%s\"", value)
                            } catch (e: Throwable) {
                                valueString = format("[toString() threw %s: %s]",
                                        e.javaClass.simpleName, e.message)
                            }

                        }

                        return format("%s <from %s>", valueString, name)
                    }
            }
        }
    }
}