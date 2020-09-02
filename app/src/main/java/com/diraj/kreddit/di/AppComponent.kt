package com.diraj.kreddit.di

import com.diraj.kreddit.KReddit
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class,
    ActivityBuilderModule::class, NetworkModule::class, AppModule::class])
interface AppComponent: AndroidInjector<KReddit> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: KReddit): Builder
        fun build(): AppComponent
    }

    override fun inject(app: KReddit)
}