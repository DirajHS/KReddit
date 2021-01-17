package com.diraj.kreddit.data.network

sealed class RedditResponse {
    object Loading: RedditResponse()

    class Success<T>(val successData: T ?= null): RedditResponse()

    class Error(val ex: Exception): RedditResponse()
}