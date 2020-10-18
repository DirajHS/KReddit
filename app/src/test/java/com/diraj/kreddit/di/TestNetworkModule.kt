package com.diraj.kreddit.di

import com.diraj.kreddit.KReddit
import com.diraj.kreddit.network.AccessTokenAuthenticator
import com.diraj.kreddit.network.interceptors.AuthenticatorInterceptor
import com.diraj.kreddit.network.interceptors.KRedditHeaderInterceptor
import com.diraj.kreddit.network.interceptors.ServerResponseErrorInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import io.mockk.mockk
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Converter
import retrofit2.Retrofit
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
        authenticatorInterceptor: AuthenticatorInterceptor,
        connectionPool: ConnectionPool,
    ): OkHttpClient {

        val builder = OkHttpClient.Builder()
        builder.connectionPool(connectionPool)
        builder.readTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.writeTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.connectTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.addInterceptor(httpLoggingInterceptor)
        builder.addInterceptor(serverResponseErrorInterceptor)
        builder.addInterceptor(authenticatorInterceptor)
        return builder.build()
    }

    @Provides
    @Singleton
    fun providesJsonInstance(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    @ExperimentalSerializationApi
    @Provides
    @Singleton
    fun providesJsonConverterFactory(jsonInstance: Json): Converter.Factory {
        return jsonInstance.asConverterFactory("application/json".toMediaType())
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
        jsonConverterFactory: Converter.Factory,
        mockWebServer: MockWebServer
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(jsonConverterFactory)
            .client(okHttpClient)
            .baseUrl(mockWebServer.url("/"))
            .build()
    }

    @Provides
    @Named("Authenticator")
    @Singleton
    fun providesAuthenticatorKRedditRetrofit(
        @Named("Authenticator") okHttpClient: OkHttpClient,
        mockWebServer: MockWebServer,
        jsonConverterFactory: Converter.Factory
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(jsonConverterFactory)
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