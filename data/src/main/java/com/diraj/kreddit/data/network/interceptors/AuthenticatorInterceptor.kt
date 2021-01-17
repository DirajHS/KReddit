package com.diraj.kreddit.data.network.interceptors

import android.util.Base64
import com.diraj.kreddit.data.BuildConfig
import com.diraj.kreddit.data.utils.DataLayerConstants.ACCESS_TOKEN_BASIC_AUTHORIZATION_PREFIX
import com.diraj.kreddit.data.utils.DataLayerConstants.AUTHORIZATION
import com.diraj.kreddit.data.utils.DataLayerConstants.USER_AGENT_KEY
import com.diraj.kreddit.data.utils.DataLayerConstants.USER_AGENT_VALUE
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthenticatorInterceptor @Inject constructor(): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val authString = BuildConfig.REDDIT_CLIENT_ID + ":"
        val encodedAuthString = Base64.encodeToString(authString.toByteArray(),
            Base64.NO_WRAP)

        val request = chain.request().newBuilder()
            .addHeader(USER_AGENT_KEY, USER_AGENT_VALUE)
            .addHeader(AUTHORIZATION, "$ACCESS_TOKEN_BASIC_AUTHORIZATION_PREFIX $encodedAuthString")
            .build()
        return chain.proceed(request)
    }
}