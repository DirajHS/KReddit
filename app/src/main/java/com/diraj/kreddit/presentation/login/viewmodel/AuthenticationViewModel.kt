package com.diraj.kreddit.presentation.login.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.diraj.kreddit.data.network.RedditResponse
import com.diraj.kreddit.data.repo.auth.AuthenticationRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@ExperimentalCoroutinesApi
class AuthenticationViewModel @Inject constructor(private val authRepo: AuthenticationRepo): ViewModel() {

    fun processAccessCode(code: String): LiveData<RedditResponse> {
        val accessCodeResponse = authRepo.getAccessToken(code)
        return accessCodeResponse.onStart {
            emit(RedditResponse.Loading)
        }.asLiveData(viewModelScope.coroutineContext)
    }
}