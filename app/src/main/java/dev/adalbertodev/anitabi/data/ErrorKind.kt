package dev.adalbertodev.anitabi.data

import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.exception.ApolloNetworkException

enum class ErrorKind(val userMessage: String) {
    NETWORK("Sin conexión. Comprueba tu red."),
    RATE_LIMIT("Demasiadas peticiones. Espera un minuto e inténtalo de nuevo."),
    SERVER("Algo falló. Inténtalo de nuevo.")
}

fun <D : Operation.Data> ApolloResponse<D>.errorKindOrNull(): ErrorKind? =
    when {
        data != null -> null
        exception is ApolloNetworkException -> ErrorKind.NETWORK
        (exception as? ApolloHttpException)?.statusCode == 429 -> ErrorKind.RATE_LIMIT
        else -> ErrorKind.SERVER
    }