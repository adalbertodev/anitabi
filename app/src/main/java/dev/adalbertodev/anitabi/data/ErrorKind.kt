package dev.adalbertodev.anitabi.data

import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.exception.ApolloNetworkException

enum class ErrorKind(val userMessage: String) {
    NETWORK("Sin conexión. Comprueba tu red."),
    SERVER("Algo falló. Inténtalo de nuevo.")
}

fun <D : Operation.Data> ApolloResponse<D>.errorKindOrNull(): ErrorKind? =
    when {
        data != null -> null
        exception is ApolloNetworkException -> ErrorKind.NETWORK
        else -> ErrorKind.SERVER
    }