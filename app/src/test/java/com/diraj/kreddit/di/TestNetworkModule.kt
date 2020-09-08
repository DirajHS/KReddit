package com.diraj.kreddit.di

import com.diraj.kreddit.KReddit
import com.diraj.kreddit.network.AccessTokenAuthenticator
import com.diraj.kreddit.network.interceptors.KRedditHeaderInterceptor
import com.diraj.kreddit.network.interceptors.ServerResponseErrorInterceptor
import com.diraj.kreddit.network.models.RedditObjectData
import com.diraj.kreddit.utils.RedditObjectDataParser
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import io.mockk.mockk
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class TestNetworkModule {

    @Provides
    @Singleton
    fun provideMockKredditApp() = mockk<KReddit>()

    private fun getOkHttpLogLevel(level: String?): HttpLoggingInterceptor.Level {
        if (level == null) return HttpLoggingInterceptor.Level.NONE

        return when (level) {
            HttpLoggingInterceptor.Level.NONE.toString() -> HttpLoggingInterceptor.Level.NONE
            HttpLoggingInterceptor.Level.BASIC.toString() -> HttpLoggingInterceptor.Level.BASIC
            HttpLoggingInterceptor.Level.HEADERS.toString() -> HttpLoggingInterceptor.Level.HEADERS
            HttpLoggingInterceptor.Level.BODY.toString() -> HttpLoggingInterceptor.Level.BODY
            else -> HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @Singleton
    fun providesConnectionPool(): ConnectionPool {
        return ConnectionPool(
            MAX_IDLE_CONNECTIONS,
            KEEP_ALIVE_DURATION, TimeUnit.MILLISECONDS)
    }

    @Provides
    @Singleton
    fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = getOkHttpLogLevel(HttpLoggingInterceptor.Level.HEADERS.toString())
        return interceptor
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        serverResponseErrorInterceptor: ServerResponseErrorInterceptor,
        kRedditHeaderInterceptor: KRedditHeaderInterceptor,
        connectionPool: ConnectionPool,
        accessTokenAuthenticator: AccessTokenAuthenticator,
    ): OkHttpClient {

        val builder = OkHttpClient.Builder()
        builder.connectionPool(connectionPool)
        builder.readTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.writeTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.connectTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.addInterceptor(httpLoggingInterceptor)
        builder.addInterceptor(serverResponseErrorInterceptor)
        builder.addInterceptor(kRedditHeaderInterceptor)
        builder.authenticator(accessTokenAuthenticator)
        return builder.build()
    }

    @Provides
    @Named("Authenticator")
    @Singleton
    fun providesOkHttpClientForAuthenticator(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        serverResponseErrorInterceptor: ServerResponseErrorInterceptor,
        connectionPool: ConnectionPool,
    ): OkHttpClient {

        val builder = OkHttpClient.Builder()
        builder.connectionPool(connectionPool)
        builder.readTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.writeTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.connectTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.addInterceptor(httpLoggingInterceptor)
        builder.addInterceptor(serverResponseErrorInterceptor)
        return builder.build()
    }

    @Provides
    @Singleton
    fun providesGsonInstance(): Gson {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(RedditObjectData::class.java, RedditObjectDataParser())
        return gsonBuilder.create()
    }

    @Provides
    @Singleton
    fun providesMockWebServer(): MockWebServer {
        return MockWebServer()
    }

    @Provides
    @Singleton
    fun providesKRedditRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
        mockWebServer: MockWebServer
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .baseUrl(mockWebServer.url("/"))
            .build()
    }

    @Provides
    @Named("Authenticator")
    @Singleton
    fun providesAuthenticatorKRedditRetrofit(
        @Named("Authenticator") okHttpClient: OkHttpClient,
        mockWebServer: MockWebServer
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl(mockWebServer.url("/"))
            .build()
    }

    companion object {
        const val REQUEST_TIME_OUT = 15
        private const val KEEP_ALIVE_DURATION = (30 * 1000).toLong()
        private const val MAX_IDLE_CONNECTIONS = 10
    }
}