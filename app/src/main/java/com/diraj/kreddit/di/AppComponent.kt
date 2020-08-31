package com.diraj.kreddit.di

import android.app.Application
import com.diraj.kreddit.KReddit
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class,
    ActivityBuilderModule::class, NetworkModule::class])
interface AppComponent: AndroidInjector<KReddit> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun build(): AppComponent
    }

    override fun inject(app: KReddit)
}