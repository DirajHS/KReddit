package com.diraj.kreddit.di

import com.diraj.kreddit.network.NetworkLayerTest
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [TestNetworkModule::class])
interface TestComponent: AndroidInjector<NetworkLayerTest> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun appModuleForTest(networkModule: TestNetworkModule): Builder
        fun build(): TestComponent
    }

    override fun inject(networkLayerTest: NetworkLayerTest)
}