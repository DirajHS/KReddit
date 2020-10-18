package com.diraj.kreddit.di

import com.diraj.kreddit.BuildConfig
import com.diraj.kreddit.KReddit
import com.diraj.kreddit.network.AccessTokenAuthenticator
import com.diraj.kreddit.network.interceptors.AuthenticatorInterceptor
import com.diraj.kreddit.network.interceptors.KRedditHeaderInterceptor
import com.diraj.kreddit.network.interceptors.ServerResponseErrorInterceptor
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton


@Module
class NetworkModule {

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
    fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = getOkHttpLogLevel(HttpLoggingInterceptor.Level.HEADERS.toString())
        return interceptor
    }

    @Provides
    @Singleton
    fun providesConnectionPool(): ConnectionPool {
        return ConnectionPool(MAX_IDLE_CONNECTIONS, KEEP_ALIVE_DURATION, TimeUnit.MILLISECONDS)
    }

    @Provides
    @Singleton
    fun providesCache(context: KReddit): Cache {
        return Cache(context.externalCacheDir!!, CACHE_DISK_SIZE_30MB.toLong())
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        serverResponseErrorInterceptor: ServerResponseErrorInterceptor,
        kRedditHeaderInterceptor: KRedditHeaderInterceptor,
        connectionPool: ConnectionPool,
        accessTokenAuthenticator: AccessTokenAuthenticator,
        kReddit: KReddit,
        cache: Cache
    ): OkHttpClient {

        val builder = OkHttpClient.Builder()
        builder.cache(cache)
        builder.connectionPool(connectionPool)
        builder.readTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.writeTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.connectTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.addInterceptor(httpLoggingInterceptor)
        builder.addInterceptor(serverResponseErrorInterceptor)
        builder.addInterceptor(kRedditHeaderInterceptor)
        builder.addNetworkInterceptor(FlipperOkhttpInterceptor(kReddit.networkFlipperPlugin))
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
        kReddit: KReddit,
        cache: Cache
    ): OkHttpClient {

        val builder = OkHttpClient.Builder()
        builder.cache(cache)
        builder.connectionPool(connectionPool)
        builder.readTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.writeTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.connectTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.addInterceptor(httpLoggingInterceptor)
        builder.addInterceptor(serverResponseErrorInterceptor)
        builder.addInterceptor(authenticatorInterceptor)
        builder.addNetworkInterceptor(FlipperOkhttpInterceptor(kReddit.networkFlipperPlugin))
        return builder.build()
    }

    @Provides
    @Named("GlideOkHttpClient")
    @Singleton
    fun providesGlideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        connectionPool: ConnectionPool,
        kReddit: KReddit,
        cache: Cache
    ): OkHttpClient {

        val builder = OkHttpClient.Builder()
        builder.cache(cache)
        builder.connectionPool(connectionPool)
        builder.readTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.writeTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.connectTimeout(REQUEST_TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.addInterceptor(httpLoggingInterceptor)
        builder.addNetworkInterceptor(FlipperOkhttpInterceptor(kReddit.networkFlipperPlugin))
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
    fun providesKRedditRetrofit(
        okHttpClient: OkHttpClient,
        @Named("REDDIT_BASE_URL_OAUTH") baseURL: String,
        jsonConverterFactory: Converter.Factory
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(jsonConverterFactory)
            .client(okHttpClient)
            .baseUrl(baseURL)
            .build()
    }

    @Provides
    @Named("Authenticator")
    @Singleton
    fun providesAuthenticatorKRedditRetrofit(
        @Named("Authenticator") okHttpClient: OkHttpClient,
        @Named("REDDIT_BASE_URL") baseURL: String,
        jsonConverterFactory: Converter.Factory
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(jsonConverterFactory)
            .client(okHttpClient)
            .baseUrl(baseURL)
            .build()
    }

    @Singleton
    @Provides
    @Named("REDDIT_BASE_URL")
    fun redditBaseUrl(): String {
        return BuildConfig.REDDIT_BASE_URL
    }

    @Singleton
    @Provides
    @Named("REDDIT_BASE_URL_OAUTH")
    fun redditBaseUrlOauth(): String {
        return BuildConfig.REDDIT_BASE_URL_OAUTH
    }


    companion object {
        private const val CACHE_DISK_SIZE_30MB = 30 * 1024 * 1024
        const val REQUEST_TIME_OUT = 15
        private const val KEEP_ALIVE_DURATION = (30 * 1000).toLong()
        private const val MAX_IDLE_CONNECTIONS = 10
    }
}