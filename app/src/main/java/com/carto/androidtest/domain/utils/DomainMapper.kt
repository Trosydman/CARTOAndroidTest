package com.carto.androidtest.domain.utils

import com.carto.androidtest.domain.model.Poi

interface DomainMapper <DTO, DomainModel: Poi>{

    fun mapToDomainModel(dtoModel: DTO): DomainModel

    fun mapFromDomainModel(domainModel: DomainModel): DTO

    fun mapToDomainModelList(dtoModelList: List<DTO>): List<DomainModel>

    fun mapFromDomainModelList(domainModelList: List<DomainModel>): List<DTO>
}
