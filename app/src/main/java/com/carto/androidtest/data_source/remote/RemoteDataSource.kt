package com.carto.androidtest.data_source.remote

import com.carto.androidtest.data_source.DataSource
import com.carto.androidtest.data_source.remote.model.PoiDTO
import com.carto.androidtest.data_source.remote.network.DEFAULT_QUERY
import com.carto.androidtest.data_source.remote.network.PoiApi
import com.carto.androidtest.data_source.remote.network.response.PoisResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RemoteDataSource(
    val poiApi: PoiApi,
    val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
): DataSource<PoiDTO> {

    override suspend fun getPois(): Flow<List<PoiDTO>> = flow<List<PoiDTO>> {
        val poiApiResponse = poiApi.getPois(DEFAULT_QUERY)

        poiApiResponse.enqueue(object: Callback<PoisResponse<PoiDTO>> {
            override fun onResponse(
                call: Call<PoisResponse<PoiDTO>>,
                response: Response<PoisResponse<PoiDTO>>
            ) {
                if (response.isSuccessful) {
                    val pois = response.body()!!.pois

                    if (pois.isNotEmpty()) {
                        CoroutineScope(defaultDispatcher).launch {
                            emit(pois)
                        }
                    } else {
                        throw IllegalStateException("Poi list is empty!")
                    }
                } else {
                    throw IllegalStateException("API response was not successful: " +
                            response.errorBody())
                }
            }

            override fun onFailure(call: Call<PoisResponse<PoiDTO>>, t: Throwable) {
                throw IllegalStateException("API request went wrong: ${t.localizedMessage}")
            }
        })
    }.flowOn(defaultDispatcher)
}
