package com.diraj.kreddit.di

import com.diraj.kreddit.BuildConfig
import com.diraj.kreddit.KReddit
import com.diraj.kreddit.network.ServerResponseErrorInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provides
    @Singleton
    fun providesMoshi(): Moshi = Moshi.Builder().build()

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
        connectionPool: ConnectionPool,
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
        //builder.addNetworkInterceptor(FlipperOkhttpInterceptor(kReddit.networkFlipperPlugin))
        return builder.build()
    }

    @Provides
    @Singleton
    fun providesKRedditRetrofit(okHttpClient: OkHttpClient,
                                @Named("REDDIT_BASE_URL") baseURL: String,
                                moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            //.addCallAdapterFactory(CoroutineCallAdapterFactory())
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


    companion object {
        private const val CACHE_DISK_SIZE_30MB = 30 * 1024 * 1024
        const val REQUEST_TIME_OUT = 15
        private const val KEEP_ALIVE_DURATION = (30 * 1000).toLong()
        private const val MAX_IDLE_CONNECTIONS = 10
    }
}