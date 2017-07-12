package com.rmnivnv.cryptomoon.di

import com.rmnivnv.cryptomoon.model.BASE_CRYPTOCOMPARE_URL
import com.rmnivnv.cryptomoon.network.CryptoCompareAPI
import com.rmnivnv.cryptomoon.network.NetworkManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Created by rmnivnv on 12/07/2017.
 */
@Module
class NetworkModule {

    @Provides @Singleton
    fun provideRetrofit() =
            Retrofit.Builder()
                    .baseUrl(BASE_CRYPTOCOMPARE_URL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

    @Provides @Singleton
    fun provideCrComApi(retrofit: Retrofit) = retrofit.create(CryptoCompareAPI::class.java)

    @Provides @Singleton
    fun provideNetworkManager(api: CryptoCompareAPI) = NetworkManager(api)

}