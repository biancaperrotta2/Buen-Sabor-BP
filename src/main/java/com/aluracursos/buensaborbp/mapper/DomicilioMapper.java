package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.*;
import com.aluracursos.buensaborbp.entity.*;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface DomicilioMapper {
    DomicilioBaseResponse toBaseResponse(Domicilio entity);

    DomicilioFullResponse toFullResponse(Domicilio entity);

    Domicilio toEntity(DomicilioRequest request);

    List<DomicilioBaseResponse> toBaseResponseList(List<Domicilio> entities);
}
