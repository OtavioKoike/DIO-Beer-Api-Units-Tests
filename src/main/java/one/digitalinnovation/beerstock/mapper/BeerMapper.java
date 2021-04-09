package one.digitalinnovation.beerstock.mapper;

import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BeerMapper {

    BeerMapper INSTANCE = Mappers.getMapper(BeerMapper.class);

//  Dado um DTO, converte para modelo
    Beer toModel(BeerDTO beerDTO);

//  Dadu uma entidade, converte para DTO
    BeerDTO toDTO(Beer beer);
}
