package com.diraj.kreddit

import android.app.Application
import com.diraj.kreddit.di.AppComponentsInjector
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.core.FlipperClient
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

    @field:Inject
    lateinit var flipperClient: FlipperClient

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    override fun onCreate() {
        super.onCreate()

        MMKV.initialize(this)
        SoLoader.init(this, false)
        AppComponentsInjector.init(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            if(FlipperUtils.shouldEnableFlipper(this)) {
                flipperClient.start()
            }
        }
    }
}