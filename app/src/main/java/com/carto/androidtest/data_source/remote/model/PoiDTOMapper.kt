package com.carto.androidtest.data_source.remote.model

import com.carto.androidtest.domain.model.Poi
import com.carto.androidtest.domain.utils.DomainMapper

class PoiDTOMapper: DomainMapper<PoiDTO, Poi> {

    override fun mapToDomainModel(dtoModel: PoiDTO): Poi {
        return Poi(
            id = dtoModel.id,
            title = dtoModel.title,
            description = dtoModel.description,
            direction = dtoModel.direction,
            region = dtoModel.region,
            image = getFixedImage(dtoModel.image),
            longitude = dtoModel.longitude,
            latitude = dtoModel.latitude
        )
    }

    override fun mapFromDomainModel(domainModel: Poi): PoiDTO {
        return PoiDTO(
            id = domainModel.id,
            title = domainModel.title,
            description = domainModel.description,
            direction = domainModel.direction,
            region = domainModel.region,
            image = getUnfixedImage(domainModel.image),
            longitude = domainModel.longitude,
            latitude = domainModel.latitude
        )
    }

    override fun mapToDomainModelList(dtoModelList: List<PoiDTO>): List<Poi> {
        return dtoModelList.map { mapToDomainModel(it) }
    }

    override fun mapFromDomainModelList(domainModelList: List<Poi>): List<PoiDTO> {
        return domainModelList.map { mapFromDomainModel(it) }
    }

    private fun getFixedImage(image: String) = image.replace("http:", "https:")

    // This is implemented to be consistent and symmetric with the other mapping direction
    private fun getUnfixedImage(image: String) = image.replace("https:", "http:")
}
