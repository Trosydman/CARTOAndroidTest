package com.carto.androidtest.repository

import com.carto.androidtest.data_source.remote.RemoteDataSource
import com.carto.androidtest.data_source.remote.model.PoiDTOMapper
import com.carto.androidtest.domain.model.Poi
import com.carto.androidtest.utils.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class PoiRepository(
    private val poiRemoteDataSource: RemoteDataSource,
    private val poiDTOMapper: PoiDTOMapper,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    fun getPois(): Flow<Result<List<Poi>>> = poiRemoteDataSource.getPois()
        .onStart { Result.Loading() }
        .map {
            val mappedList = poiDTOMapper.mapToDomainModelList(it)

            Result.Success(mappedList)
        }
        .flowOn(defaultDispatcher)
        .catch { exception ->
            Result.Error(exception.message ?: "Unknown error")
        }
}
