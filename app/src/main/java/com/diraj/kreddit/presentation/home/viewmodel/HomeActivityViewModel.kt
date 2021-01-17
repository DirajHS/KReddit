package com.diraj.kreddit.presentation.home.viewmodel

import androidx.lifecycle.*
import com.diraj.kreddit.data.network.RedditResponse
import com.diraj.kreddit.data.repo.auth.AuthenticationRepo
import com.diraj.kreddit.data.repo.profile.ProfileRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ExperimentalCoroutinesApi
class HomeActivityViewModel @Inject constructor(
    profileRepo: ProfileRepo,
    private val authenticationRepo: AuthenticationRepo)
    : ViewModel() {

    val userInfoLiveData: LiveData<RedditResponse> = profileRepo.fetchProfileInfo().onStart {
            emit(RedditResponse.Loading)
        }.asLiveData(viewModelScope.coroutineContext)

    fun doLogout() = liveData(context = Dispatchers.IO) {
        emit(RedditResponse.Loading)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                emit(authenticationRepo.logout())
            }
        }
    }
}
