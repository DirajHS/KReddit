package com.diraj.kreddit.network.interceptors

import com.diraj.kreddit.utils.KRedditConstants.AUTHORIZATION
import com.diraj.kreddit.utils.KRedditConstants.AUTHORIZATION_HEADER_PREFIX_BEARER
import com.diraj.kreddit.utils.KRedditConstants.USER_AGENT_KEY
import com.diraj.kreddit.utils.KRedditConstants.USER_AGENT_VALUE
import com.diraj.kreddit.utils.UserSession
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class KRedditHeaderInterceptor @Inject constructor(): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request().newBuilder()
            .addHeader(USER_AGENT_KEY, USER_AGENT_VALUE)
            .addHeader(AUTHORIZATION, "$AUTHORIZATION_HEADER_PREFIX_BEARER ${UserSession.accessToken}")
            .build()
        return chain.proceed(request)
    }
}