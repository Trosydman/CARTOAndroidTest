package com.carto.androidtest.data_source.remote

import com.carto.androidtest.data_source.DataSource
import com.carto.androidtest.data_source.remote.model.PoiDTO
import com.carto.androidtest.data_source.remote.network.DEFAULT_QUERY
import com.carto.androidtest.data_source.remote.network.PoiApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class RemoteDataSource(
    private val poiApi: PoiApi,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
): DataSource<PoiDTO> {

    override fun getPois(): Flow<List<PoiDTO>> = flow {
        val poiApiResponse = poiApi.getPois(DEFAULT_QUERY)

        emit(poiApiResponse.pois)
    }.flowOn(defaultDispatcher)
}
