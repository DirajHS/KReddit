package com.diraj.kreddit.network

//import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.diraj.kreddit.KReddit
import okhttp3.Interceptor
import okhttp3.Response
import java.net.UnknownHostException
import javax.inject.Inject

class ServerResponseErrorInterceptor @Inject constructor(var appContext: KReddit) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val response = chain.proceed(chain.request())
            when (response.code) {
                404 -> {
                    handleError()
                }
            }
            return response
        } catch (ex: UnknownHostException) {
            //LocalBroadcastManager.getInstance(appContext).sendBroadcast(Intent(INTENT_ACTION_NETWORK))
            throw ex
        }
    }

    private fun handleError() {
        //LocalBroadcastManager.getInstance(appContext).sendBroadcast(Intent(INTENT_ACTION_DEFAULT))
    }

    companion object {
        const val INTENT_ACTION_DEFAULT = "server_error_action"
        const val INTENT_ACTION_NETWORK = "no_network_broadcast"
    }

}