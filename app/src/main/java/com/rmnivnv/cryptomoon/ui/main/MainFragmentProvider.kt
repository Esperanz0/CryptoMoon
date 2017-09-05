package com.rmnivnv.cryptomoon.ui.main

import com.rmnivnv.cryptomoon.di.PerFragment
import com.rmnivnv.cryptomoon.ui.coins.CoinsFragment
import com.rmnivnv.cryptomoon.ui.coins.CoinsModule
import com.rmnivnv.cryptomoon.ui.topCoins.TopCoinsFragment
import com.rmnivnv.cryptomoon.ui.topCoins.TopCoinsModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by ivanov_r on 05.09.2017.
 */
@Module
abstract class MainFragmentProvider {

    @PerFragment
    @ContributesAndroidInjector(modules = arrayOf(CoinsModule::class))
    abstract fun provideCoinFragmentFactory(): CoinsFragment

    @PerFragment
    @ContributesAndroidInjector(modules = arrayOf(TopCoinsModule::class))
    abstract fun provideTopCoinsFragmentFactory(): TopCoinsFragment
}