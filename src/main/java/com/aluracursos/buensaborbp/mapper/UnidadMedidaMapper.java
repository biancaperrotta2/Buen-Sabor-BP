package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.UnidadMedidaRequest;
import com.aluracursos.buensaborbp.dto.UnidadMedidaResponse;
import com.aluracursos.buensaborbp.entity.UnidadMedida;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UnidadMedidaMapper {
    UnidadMedidaResponse toResponse(UnidadMedida entity);

    UnidadMedida toEntity(UnidadMedidaRequest request);

    List<UnidadMedidaResponse> toResponseList(List<UnidadMedida> entities); // devuelve la lista de las unidades de medida disponibles: kg,lt,gr...

}
