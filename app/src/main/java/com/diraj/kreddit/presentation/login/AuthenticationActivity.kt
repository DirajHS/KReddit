package com.diraj.kreddit.presentation.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.diraj.kreddit.R
import com.diraj.kreddit.databinding.ActivityAuthenticationBinding
import com.diraj.kreddit.di.ViewModelFactory
import com.diraj.kreddit.network.RedditResponse
import com.diraj.kreddit.presentation.home.HomeActivity
import com.diraj.kreddit.presentation.login.viewmodel.AuthenticationViewModel
import com.diraj.kreddit.utils.KRedditConstants.AUTH_URL
import com.diraj.kreddit.utils.KRedditConstants.STATE
import com.diraj.kreddit.utils.UserSession
import dagger.android.support.DaggerAppCompatActivity
import timber.log.Timber
import javax.inject.Inject

class AuthenticationActivity : DaggerAppCompatActivity() {

    @field:Inject
    lateinit var viewModelFactory: ViewModelFactory<AuthenticationViewModel>

    private lateinit var authenticationViewModel: AuthenticationViewModel

    private lateinit var activityAuthenticationBinding: ActivityAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticationViewModel = ViewModelProvider(this, viewModelFactory).get(AuthenticationViewModel::class.java)
        activityAuthenticationBinding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(activityAuthenticationBinding.root)

        activityAuthenticationBinding.btnLogin.setOnClickListener {
            startLogin()
        }
    }

    override fun onResume() {
        super.onResume()
        if(UserSession.isOpen) {
            startHomeActivity()
        } else {
            if (intent != null && intent.action == Intent.ACTION_VIEW) {
                val uri = intent.data
                if (uri?.getQueryParameter("error") != null) {
                    val error = uri.getQueryParameter("error")
                    Timber.e("Error: $error")
                    showErrorToast(error)
                } else {
                    val state = uri?.getQueryParameter("state")
                    if (state == STATE) {
                        val code = uri.getQueryParameter("code")
                        code?.let { authenticationCode ->
                            authenticationViewModel.processAccessCode(authenticationCode)
                                .observe(this, {
                                    processAccessTokenResponse(it)
                                })
                        }
                    }
                }
            }
        }
    }

    private fun startLogin() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AUTH_URL))
        startActivity(intent)
    }

    private fun processAccessTokenResponse(redditResponse: RedditResponse) {
        when(redditResponse) {
            is RedditResponse.Loading -> {
                activityAuthenticationBinding.btnLogin.isEnabled = false
                activityAuthenticationBinding.btnLogin.text = getString(R.string.processing_login)
            }
            is RedditResponse.Success<*> -> {
                startHomeActivity()
            }
            is RedditResponse.Error -> {
                activityAuthenticationBinding.btnLogin.isEnabled = true
                activityAuthenticationBinding.btnLogin.text = getString(R.string.login)

                val errorMessage = redditResponse.ex.localizedMessage
                showErrorToast(errorMessage)
            }
        }
    }

    private fun startHomeActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finishAffinity()
    }

    private fun showErrorToast(errorMessage: String?) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }
}