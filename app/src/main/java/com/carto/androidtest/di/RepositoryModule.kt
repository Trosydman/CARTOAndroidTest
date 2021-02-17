package com.carto.androidtest.di

import com.carto.androidtest.data_source.remote.RemoteDataSource
import com.carto.androidtest.data_source.remote.model.PoiDTOMapper
import com.carto.androidtest.repository.PoiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Singleton
    @Provides
    fun provideRepository(poiRemoteDataSource: RemoteDataSource, poiDTOMapper: PoiDTOMapper) =
        PoiRepository(poiRemoteDataSource, poiDTOMapper)
}
