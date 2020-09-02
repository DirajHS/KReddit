package com.diraj.kreddit.network

sealed class FeedApiState {
    object Loading: FeedApiState()

    object Success: FeedApiState()

    class Error(val ex: Exception): FeedApiState()
}