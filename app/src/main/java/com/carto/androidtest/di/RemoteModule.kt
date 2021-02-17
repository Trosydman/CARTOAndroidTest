package com.carto.androidtest.di

import com.carto.androidtest.CARTO_BASE_URL
import com.carto.androidtest.data_source.remote.RemoteDataSource
import com.carto.androidtest.data_source.remote.model.PoiDTOMapper
import com.carto.androidtest.data_source.remote.network.PoiApi
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RemoteModule {

    @Singleton
    @Provides
    fun providePoiAPI(): PoiApi = Retrofit.Builder()
            .baseUrl(CARTO_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .build()
            .create(PoiApi::class.java)

    @Singleton
    @Provides
    fun providePoiDTOMapper() = PoiDTOMapper()

    @Singleton
    @Provides
    fun provideRemoteDataSource(poiAPI: PoiApi) = RemoteDataSource(poiAPI)
}
