package com.diraj.kreddit.data.network.interceptors

import com.diraj.kreddit.data.user.UserSession
import com.diraj.kreddit.data.utils.DataLayerConstants.AUTHORIZATION
import com.diraj.kreddit.data.utils.DataLayerConstants.AUTHORIZATION_HEADER_PREFIX_BEARER
import com.diraj.kreddit.data.utils.DataLayerConstants.USER_AGENT_KEY
import com.diraj.kreddit.data.utils.DataLayerConstants.USER_AGENT_VALUE
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