package com.aluracursos.buensaborbp.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.aluracursos.buensaborbp.dto.ArticuloInsumoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoRequest;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;

@Mapper(componentModel = "spring", uses = {CategoriaMapper.class, UnidadMedidaMapper.class})
public interface ArticuloInsumoMapper {
    ArticuloInsumoBaseResponse toBaseResponse(ArticuloInsumo entity);

    ArticuloInsumoFullResponse toFullResponse(ArticuloInsumo entity);

    ArticuloInsumo toEntity(ArticuloInsumoRequest request);

    List<ArticuloInsumoBaseResponse> toBaseResponseList(List<ArticuloInsumo> entities);

    List<ArticuloInsumoFullResponse> toFullResponseList(List<ArticuloInsumo> entities);
}
