package com.diraj.kreddit.network.interceptors

import android.util.Base64
import com.diraj.kreddit.BuildConfig
import com.diraj.kreddit.utils.KRedditConstants
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthenticatorInterceptor @Inject constructor(): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val authString = BuildConfig.REDDIT_CLIENT_ID + ":"
        val encodedAuthString = Base64.encodeToString(authString.toByteArray(),
            Base64.NO_WRAP)

        val request = chain.request().newBuilder()
            .addHeader(KRedditConstants.USER_AGENT_KEY, KRedditConstants.USER_AGENT_VALUE)
            .addHeader(KRedditConstants.AUTHORIZATION, "${KRedditConstants.ACCESS_TOKEN_BASIC_AUTHORIZATION_PREFIX} $encodedAuthString")
            .build()
        return chain.proceed(request)
    }
}