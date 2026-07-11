package dev.adalbertodev.anitabi.data

import com.apollographql.apollo.ApolloClient

object ApolloProvider {
    lateinit var client: ApolloClient
        private set

    fun init(sessionStore: SessionStore) {
        if (::client.isInitialized) return

        client = ApolloClient.Builder()
            .serverUrl("https://graphql.anilist.co")
            .httpInterceptors(listOf(AuthInterceptor(sessionStore)))
            .build()
    }
}