package dev.adalbertodev.anitabi.data

import com.apollographql.apollo.ApolloClient

object ApolloProvider {
    val client: ApolloClient = ApolloClient.Builder()
        .serverUrl("https://graphql.anilist.co")
        .build()
}