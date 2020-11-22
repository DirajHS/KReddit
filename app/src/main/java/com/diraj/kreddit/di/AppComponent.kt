package com.diraj.kreddit.di

import android.content.Context
import com.diraj.kreddit.KReddit
import com.diraj.kreddit.data.di.DataLayerModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class,
    ActivityBuilderModule::class, DataLayerModule::class, GlideDaggerModule::class])
interface AppComponent: AndroidInjector<KReddit> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: KReddit): Builder

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent
    }

    override fun inject(app: KReddit)
}