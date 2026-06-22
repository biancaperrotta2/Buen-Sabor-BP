package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.EmpresaResponse;
import com.aluracursos.buensaborbp.entity.Empresa;
import org.mapstruct.Mapper;
import com.aluracursos.buensaborbp.dto.EmpresaRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EmpresaMapper {

    EmpresaResponse toResponse(Empresa empresa);

    @Mapping(target = "listaSucursal", ignore = true)
    Empresa toEntity(EmpresaRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "listaSucursal", ignore = true)
    void updateEntity(EmpresaRequest request, @MappingTarget Empresa entity);

    List<EmpresaResponse> toResponseList(List<Empresa> empresas);
}
