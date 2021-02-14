package com.carto.androidtest.data_source.remote.network

import com.carto.androidtest.data_source.remote.model.PoiDTO
import com.carto.androidtest.data_source.remote.network.response.PoisResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

const val CARTO_BASE_URL = "https://javieraragon.carto.com/api/v2/"
const val DEFAULT_QUERY =
    "SELECT id, direction, href as image, region, title, view as description, " +
            "ST_X(the_geom) as longitude, ST_Y(the_geom) as latitude FROM ios_test"

interface PoiApi {

    @GET("sql")
    suspend fun getPois(@Query("q") query: String): Call<PoisResponse<PoiDTO>>
}
