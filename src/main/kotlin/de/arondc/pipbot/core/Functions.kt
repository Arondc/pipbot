package de.arondc.pipbot.core

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.runBlocking

object Functions {
    private const val MAX_ADDITIONAL_ATTEMPTS = 4
    private const val RETRY_DELAY_IN_MILLIS = 500L

    fun <T> retry(throwable: Throwable, func: () -> T?): T {
        return runBlocking {
            flow {
                emit(func.invoke() ?: throw throwable)
            }.retryWhen { cause, attempt ->
                if (cause is RuntimeException && attempt < MAX_ADDITIONAL_ATTEMPTS) {
                    delay(RETRY_DELAY_IN_MILLIS)
                    return@retryWhen true
                } else return@retryWhen false
            }.single()
        }
    }
}
