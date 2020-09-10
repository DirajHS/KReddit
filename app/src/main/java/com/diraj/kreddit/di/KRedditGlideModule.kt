package com.diraj.kreddit.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.Excludes
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpLibraryGlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.diraj.kreddit.KReddit
import okhttp3.OkHttpClient
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Named

@GlideModule
@Excludes(OkHttpLibraryGlideModule::class)
class KRedditGlideModule : AppGlideModule() {

    @Inject
    @Named("GlideOkHttpClient")
    lateinit var okHttpClient: OkHttpClient

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDefaultRequestOptions(defaultRequestOptions())
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        (context.applicationContext as KReddit).androidInjector().inject(this)
        val factory = OkHttpUrlLoader.Factory(okHttpClient)
        registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }

    private fun defaultRequestOptions(): RequestOptions {
        return RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
    }
}