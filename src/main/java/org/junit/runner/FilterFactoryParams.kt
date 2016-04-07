package org.junit.runner

class FilterFactoryParams(val topLevelDescription: Description?, val args: String?) {

    init {
        if (args == null || topLevelDescription == null) {
            throw NullPointerException()
        }
    }
}
