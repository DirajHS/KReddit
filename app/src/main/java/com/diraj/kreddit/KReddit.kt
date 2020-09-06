package com.diraj.kreddit

import android.app.Application
import com.diraj.kreddit.di.AppComponentsInjector
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.soloader.SoLoader
import com.tencent.mmkv.MMKV
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import javax.inject.Inject


class KReddit: Application(), HasAndroidInjector {

    @field:Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    lateinit var networkFlipperPlugin: NetworkFlipperPlugin

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    override fun onCreate() {
        super.onCreate()

        MMKV.initialize(this)
        SoLoader.init(this, false)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            if(FlipperUtils.shouldEnableFlipper(this)) {
                val client = AndroidFlipperClient.getInstance(this)
                networkFlipperPlugin = NetworkFlipperPlugin()
                client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
                client.addPlugin(networkFlipperPlugin)
                client.addPlugin(DatabasesFlipperPlugin(this))
                client.start()
            }
        }
        AppComponentsInjector.init(this)
    }
}