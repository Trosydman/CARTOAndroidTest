package com.carto.androidtest.data_source

import kotlinx.coroutines.flow.Flow

interface DataSource<DTO> {
    suspend fun getPois(): Flow<List<DTO>>
}
